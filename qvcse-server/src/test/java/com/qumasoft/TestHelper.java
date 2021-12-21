/*   Copyright 2004-2021 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft;

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.BogusResponseObject;
import com.qumasoft.qvcslib.ClientBranchManager;
import com.qumasoft.qvcslib.ProjectPropertiesFactory;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientBranchesData;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.RoleManager;
import com.qumasoft.server.ServerUtility;
import com.qumasoft.server.clientrequest.ClientRequestListClientBranches;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.UserDAO;
import com.qvcsos.server.dataaccess.impl.UserDAOImpl;
import com.qvcsos.server.datamodel.User;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for JUnit tests.
 *
 * @author Jim Voris
 */
public final class TestHelper {

    private static DatabaseManager databaseManager;
    private static SourceControlBehaviorManager sourceControlBehaviorManager;
    private static String schemaName;

    /**
     * Hide the default constructor so it cannot be used.
     */
    private TestHelper() {
    }

    /**
     * Create our logger object
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TestHelper.class);
    private static Object serverProxyObject = null;
    private static final long KILL_DELAY = 11000;
    public static final String SERVER_NAME = "Test Server";
    public static final String USER_DIR = "user.dir";
    public static final String USER_NAME = "ScriptedTestUser";
    public static final String PASSWORD = "password";
    public static final String SUBPROJECT_DIR_NAME = "1st Scripted Child Directory Name";
    public static final String SUBPROJECT_APPENDED_PATH = "subProjectDirectory";
    public static final String SUBPROJECT2_DIR_NAME = "2nd Scripted Child Directory Name";
    public static final String SUBPROJECT2_APPENDED_PATH = "subProjectDirectory/subProjectDirectory2";
    public static final String SUBPROJECT_FIRST_SHORTWORKFILENAME = "QVCSEnterpriseServer.java";
    public static final String SECOND_SHORTWORKFILENAME = "Serverb.java";
    public static final String THIRD_SHORTWORKFILENAME = "AnotherServer.java";
    public static final String SUBPROJECT2_FIRST_SHORTWORKFILENAME = "ThirdDirectoryFile.java";
    public static final String BASE_DIR_SHORTWOFILENAME_A = "ServerB.java";
    public static final String BASE_DIR_SHORTWOFILENAME_B = "ServerC.java";

    /**
     * Start the QVCS Enterprise server.
     * @return We return an object that can be used to synchronize the shutdown of the server. Pass this same object to the stopServer method so the server can notify
     * when it has shutdown, instead of having to wait some fuzzy amount of time for the server to exit.
     * @throws QVCSException for QVCS problems.
     * @throws java.io.IOException if we can't get the canonical path for the user.dir
     */
    public static String startServer() throws QVCSException, IOException {
        LOGGER.info("[{}] ********************************************************* TestHelper.startServer", Thread.currentThread().getName());
        String serverStartSyncString = null;
        if (serverProxyObject == null) {
            // So the server starts fresh.
            initDirectories();

            // So the server uses a project property file useful for the machine the tests are running on.
            initProjectProperties();

            // For unit testing, listen on the 2xxxx ports.
            serverStartSyncString = "Sync server start";
            String userDir = System.getProperty(USER_DIR);
            File userDirFile = new File(userDir);
            String canonicalUserDir = userDirFile.getCanonicalPath();
            final String args[] = {canonicalUserDir, "29889", "29890", "29080", serverStartSyncString};
            serverProxyObject = new Object();
            ServerResponseFactory.setShutdownInProgress(false);
            Runnable worker = () -> {
                try {
                    QVCSEnterpriseServer.main(args);
                } catch (Exception e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                }
            };
            synchronized (serverStartSyncString) {
                try {
                    // Put all this on a separate worker thread.
                    new Thread(worker).start();
                    serverStartSyncString.wait();
                }
                catch (InterruptedException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                }
            }
        } else {
            if (QVCSEnterpriseServer.getServerIsRunningFlag()) {
                LOGGER.info("[{}] ********************************************************* TestHelper.startServer -- server already running.", Thread.currentThread().getName());
                serverProxyObject = null;
                throw new QVCSRuntimeException("Starting server when server already running!");
            }
        }
        LOGGER.info("********************************************************* TestHelper returning from startServer");
        return (serverStartSyncString);
    }

    /**
     * Stop the QVCS Enterprise server.
     * @param syncObject an object the server will use to notify us on shutdown. It <b>NUST</b> be the same object returned from startServer!!!
     */
    public static synchronized void stopServer(Object syncObject) {
        LOGGER.info("[{}] ********************************************************* TestHelper.stopServer", Thread.currentThread().getName());
        stopServerImmediately(syncObject);
    }

    /**
     * Kill the server -- i.e. shut it down immediately. Some tests need the server to have been shutdown so that their initialization code works
     * @param syncObject an object the server will use to notify us on shutdown. It <b>NUST</b> be the same object returned from startServer!!!
     */
    public static void stopServerImmediately(Object syncObject) {
        LOGGER.info("[{}] ********************************************************* TestHelper.stopServerImmediately", Thread.currentThread().getName());
        if (syncObject != null) {
            if (QVCSEnterpriseServer.getServerIsRunningFlag()) {
                LOGGER.info("[{}] shutdown immediately with sync object [{}]", Thread.currentThread().getName(), syncObject.hashCode());
                synchronized (syncObject) {
                    ServerResponseFactory.setShutdownInProgress(true);
                    QVCSEnterpriseServer.setShutdownInProgress(true);
                    try {
                        LOGGER.info("waiting on [{}]", syncObject.hashCode());
                        syncObject.wait();
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getLocalizedMessage(), e);
                    } finally {
                        serverProxyObject = null;
                    }
                }
            } else {
                serverProxyObject = null;
            }
        } else {
            if (QVCSEnterpriseServer.getServerIsRunningFlag()) {
                try {
                    LOGGER.info("[{}] shutdown immediately with NO sync object.", Thread.currentThread().getName());
                    ServerResponseFactory.setShutdownInProgress(true);
                    QVCSEnterpriseServer.setShutdownInProgress(true);
                    Thread.sleep(KILL_DELAY);
                    serverProxyObject = null;
                }
                catch (InterruptedException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                }
            } else {
                serverProxyObject = null;
            }
        }
    }

    /**
     * Use a psql script to reset the test database.
     */
    public static void resetTestDatabaseViaPsqlScript() {
        String userDir = System.getProperty("user.dir");
        try {
            String execString = String.format("psql -f %s/postgres_qvcsos410_test_script.sql postgresql://postgres:postgres@localhost:5433/postgres", userDir);
            Process p = Runtime.getRuntime().exec(execString);
            p.waitFor();
            LOGGER.info("Reset test database process exit value: [{}]", p.exitValue());
        } catch (IOException | InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Use a psql script to reset the test database.
     */
    public static void resetQvcsosTestDatabaseViaPsqlScript() {
        String userDir = System.getProperty("user.dir");
        try {
            String execString = String.format("psql -f %s/postgres_qvcsos410_create_test_project_script.sql postgresql://postgres:postgres@localhost:5433/postgres", userDir);
            Process p = Runtime.getRuntime().exec(execString);
            p.waitFor();
            LOGGER.info("Reset test database process exit value: [{}]", p.exitValue());
        } catch (IOException | InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
    }

    private static void initDirectories() {
        // Delete the file id store so the server starts fresh.
        String storeName = System.getProperty(USER_DIR)
                + File.separator
                + QVCSConstants.QVCS_META_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_FILEID_STORE_NAME
                + ".dat";
        File storeFile = new File(storeName);
        if (storeFile.exists()) {
            storeFile.delete();
        }
        deleteRoleProjectBranchStore();
        initRoleProjectBranchStore();

        initProjectProperties();
    }

    /**
     * Delete the branch store.
     */
    public static synchronized void deleteBranchStore() {
        LOGGER.info("[{}] ********************************************************* TestHelper.deleteBranchStore", Thread.currentThread().getName());
        String branchStoreName = System.getProperty(USER_DIR)
                + File.separator
                + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_BRANCH_STORE_NAME + "dat";
        File branchStoreFile = new File(branchStoreName);
        if (branchStoreFile.exists()) {
            branchStoreFile.delete();
        }
    }

    public static synchronized void deleteAuthenticationStore() {
        String storeName = System.getProperty(USER_DIR)
                + File.separator
                + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_AUTHENTICATION_STORE_NAME + "dat";
        File storeFile = new File(storeName);
        if (storeFile.exists()) {
            storeFile.delete();
        }
    }

    /**
     * Create archive files that we'll use for testing.
     */
    public static synchronized void initializeArchiveFiles() {
        LOGGER.info("[{}] ********************************************************* TestHelper.initializeArchiveFiles", Thread.currentThread().getName());
        File sourceFile = new File(System.getProperty(USER_DIR) + File.separator + "QVCSEnterpriseServer.kbwb");
        String firstDestinationDirName = System.getProperty(USER_DIR)
                + File.separator
                + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                + File.separator
                + getTestProjectName();
        File firstDestinationDirectory = new File(firstDestinationDirName);
        firstDestinationDirectory.mkdirs();
        File firstDestinationFile = new File(firstDestinationDirName + File.separator + "QVCSEnterpriseServer.kbwb");

        String secondDestinationDirName = firstDestinationDirName + File.separator + SUBPROJECT_DIR_NAME;
        File secondDestinationDirectory = new File(secondDestinationDirName);
        secondDestinationDirectory.mkdirs();
        File secondDestinationFile = new File(secondDestinationDirName + File.separator + "QVCSEnterpriseServer.kbwb");

        String thirdDestinationDirName = secondDestinationDirName + File.separator + SUBPROJECT2_DIR_NAME;
        File thirdDestinationDirectory = new File(thirdDestinationDirName);
        thirdDestinationDirectory.mkdirs();
        File thirdDestinationFile = new File(thirdDestinationDirName + File.separator + "ThirdDirectoryFile.kbwb");

        File fourthDestinationFile = new File(firstDestinationDirName + File.separator + "Server.kbwb");
        File fifthDestinationFile = new File(firstDestinationDirName + File.separator + "AnotherServer.kbwb");
        File sixthDestinationFile = new File(firstDestinationDirName + File.separator + "ServerB.kbwb");
        File seventhDestinationFile = new File(firstDestinationDirName + File.separator + "ServerC.kbwb");
        try {
            ServerUtility.copyFile(sourceFile, firstDestinationFile);
            ServerUtility.copyFile(sourceFile, secondDestinationFile);
            ServerUtility.copyFile(sourceFile, thirdDestinationFile);
            ServerUtility.copyFile(sourceFile, fourthDestinationFile);
            ServerUtility.copyFile(sourceFile, fifthDestinationFile);
            ServerUtility.copyFile(sourceFile, sixthDestinationFile);
            ServerUtility.copyFile(sourceFile, seventhDestinationFile);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Create archive files that we'll use for testing.
     */
    public static synchronized void initializeApacheCompareTestArchiveFiles() {
        LOGGER.info("[{}] ********************************************************* TestHelper.initializeApacheCompareTestArchiveFiles", Thread.currentThread().getName());

        File oneaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest1a.uyu");
        File onebSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest1b.uyu");

        File twoaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest2a.uyu");
        File twobSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest2b.uyu");

        File threeaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest3a.uyu");
        File threebSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest3b.uyu");

        File fouraSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest4a.uyu");
        File fourbSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest4b.uyu");

        File fiveaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest5a.uyu");
        File fivebSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest5b.uyu");

        File sixaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest6a.uyu");
        File sixbSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest6b.uyu");

        File sevenaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest7a.uyu");
        File sevenbSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest7b.uyu");

        File eightaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest8a.uyu");
        File eightbSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest8b.uyu");

        File nineaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest9a.uyu");
        File ninebSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest9b.uyu");

        File tenaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest10a.uyu");
        File tenbSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest10b.uyu");

        File twelveaSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest12a.uyu");
        File twelvebSourceFile = new File(System.getProperty(USER_DIR) + File.separator + "CompareTest12b.uyu");

        String destinationDirName = System.getProperty(USER_DIR)
                + File.separator
                + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                + File.separator
                + getTestProjectName();

        File firstDestinationDirectory = new File(destinationDirName);
        firstDestinationDirectory.mkdirs();

        File oneaDestinationFile = new File(destinationDirName + File.separator + "CompareTest1a.uyu");
        File onebDestinationFile = new File(destinationDirName + File.separator + "CompareTest1b.uyu");

        File twoaDestinationFile = new File(destinationDirName + File.separator + "CompareTest2a.uyu");
        File twobDestinationFile = new File(destinationDirName + File.separator + "CompareTest2b.uyu");

        File threeaDestinationFile = new File(destinationDirName + File.separator + "CompareTest3a.uyu");
        File threebDestinationFile = new File(destinationDirName + File.separator + "CompareTest3b.uyu");

        File fouraDestinationFile = new File(destinationDirName + File.separator + "CompareTest4a.uyu");
        File fourbDestinationFile = new File(destinationDirName + File.separator + "CompareTest4b.uyu");

        File fiveaDestinationFile = new File(destinationDirName + File.separator + "CompareTest5a.uyu");
        File fivebDestinationFile = new File(destinationDirName + File.separator + "CompareTest5b.uyu");

        File sixaDestinationFile = new File(destinationDirName + File.separator + "CompareTest6a.uyu");
        File sixbDestinationFile = new File(destinationDirName + File.separator + "CompareTest6b.uyu");

        File sevenaDestinationFile = new File(destinationDirName + File.separator + "CompareTest7a.uyu");
        File sevenbDestinationFile = new File(destinationDirName + File.separator + "CompareTest7b.uyu");

        File eightaDestinationFile = new File(destinationDirName + File.separator + "CompareTest8a.uyu");
        File eightbDestinationFile = new File(destinationDirName + File.separator + "CompareTest8b.uyu");

        File nineaDestinationFile = new File(destinationDirName + File.separator + "CompareTest9a.uyu");
        File ninebDestinationFile = new File(destinationDirName + File.separator + "CompareTest9b.uyu");

        File tenaDestinationFile = new File(destinationDirName + File.separator + "CompareTest10a.uyu");
        File tenbDestinationFile = new File(destinationDirName + File.separator + "CompareTest10b.uyu");

        File twelveaDestinationFile = new File(destinationDirName + File.separator + "CompareTest12a.uyu");
        File twelvebDestinationFile = new File(destinationDirName + File.separator + "CompareTest12b.uyu");

        try {
            ServerUtility.copyFile(oneaSourceFile, oneaDestinationFile);
            ServerUtility.copyFile(onebSourceFile, onebDestinationFile);

            ServerUtility.copyFile(twoaSourceFile, twoaDestinationFile);
            ServerUtility.copyFile(twobSourceFile, twobDestinationFile);

            ServerUtility.copyFile(threeaSourceFile, threeaDestinationFile);
            ServerUtility.copyFile(threebSourceFile, threebDestinationFile);

            ServerUtility.copyFile(fouraSourceFile, fouraDestinationFile);
            ServerUtility.copyFile(fourbSourceFile, fourbDestinationFile);

            ServerUtility.copyFile(fiveaSourceFile, fiveaDestinationFile);
            ServerUtility.copyFile(fivebSourceFile, fivebDestinationFile);

            ServerUtility.copyFile(sixaSourceFile, sixaDestinationFile);
            ServerUtility.copyFile(sixbSourceFile, sixbDestinationFile);

            ServerUtility.copyFile(sevenaSourceFile, sevenaDestinationFile);
            ServerUtility.copyFile(sevenbSourceFile, sevenbDestinationFile);

            ServerUtility.copyFile(eightaSourceFile, eightaDestinationFile);
            ServerUtility.copyFile(eightbSourceFile, eightbDestinationFile);

            ServerUtility.copyFile(nineaSourceFile, nineaDestinationFile);
            ServerUtility.copyFile(ninebSourceFile, ninebDestinationFile);

            ServerUtility.copyFile(tenaSourceFile, tenaDestinationFile);
            ServerUtility.copyFile(tenbSourceFile, tenbDestinationFile);

            ServerUtility.copyFile(twelveaSourceFile, twelveaDestinationFile);
            ServerUtility.copyFile(twelvebSourceFile, twelvebDestinationFile);

        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    public static String getFeatureBranchName() {
        return "2.2.2";
    }

    public static synchronized void initializeFeatureBranch() throws QVCSException {
        LOGGER.info("[{}] ********************************************************* TestHelper.initializeFeatureBranch", Thread.currentThread().getName());
        Properties projectProperties = new Properties();
        RemoteBranchProperties featureBranchProperties = new RemoteBranchProperties(getTestProjectName(), getFeatureBranchName(), projectProperties);
        featureBranchProperties.setIsReadOnlyBranchFlag(false);
        featureBranchProperties.setIsTagBasedBranchFlag(false);
        featureBranchProperties.setIsFeatureBranchFlag(true);
        featureBranchProperties.setIsReleaseBranchFlag(false);
        featureBranchProperties.setBranchParent(QVCSConstants.QVCS_TRUNK_BRANCH);
    }

    /**
     * Remove archive files created during testing.
     */
    public static synchronized void removeArchiveFiles() {
        LOGGER.info("[{}] ********************************************************* TestHelper.removeArchiveFiles", Thread.currentThread().getName());
        String firstDestinationDirName = System.getProperty(USER_DIR)
                + File.separator
                + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                + File.separator
                + getTestProjectName();
        File firstDestinationDirectory = new File(firstDestinationDirName);

        String secondDestinationDirName = firstDestinationDirName + File.separator + "subProjectDirectory";
        File secondDestinationDirectory = new File(secondDestinationDirName);

        String thirdDestinationDirName = secondDestinationDirName + File.separator + "subProjectDirectory2";
        File thirdDestinationDirectory = new File(thirdDestinationDirName);

        String fourthDestinationDirName = firstDestinationDirName + File.separator + QVCSConstants.QVCS_CEMETERY_DIRECTORY;
        File fourthDestinationDirectory = new File(fourthDestinationDirName);

        String fifthDestinationDirName = firstDestinationDirName + File.separator + QVCSConstants.QVCS_DIRECTORY_METADATA_DIRECTORY;
        File fifthDestinationDirectory = new File(fifthDestinationDirName);

        String sixthDestinationDirName = firstDestinationDirName + File.separator + QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY;
        File sixthDestinationDirectory = new File(sixthDestinationDirName);

        deleteDirectory(sixthDestinationDirectory);
        deleteDirectory(fifthDestinationDirectory);
        deleteDirectory(fourthDestinationDirectory);
        deleteDirectory(thirdDestinationDirectory);
        deleteDirectory(secondDestinationDirectory);
        deleteDirectory(firstDestinationDirectory);
    }

    /**
     * Delete all the files in a directory and then delete the directory itself.
     *
     * @param directory the directory to delete.
     */
    public static synchronized void deleteDirectory(File directory) {
        LOGGER.info("[{}] ********************************************************* TestHelper.deleteDirectory: [{}]", Thread.currentThread().getName(), directory.getPath());
        File[] firstDirectoryFiles = directory.listFiles();
        if (firstDirectoryFiles != null) {
            for (File file : firstDirectoryFiles) {
                file.delete();
            }
        }
        directory.delete();
    }

    /**
     * Need this so that we use a different project properties file for testing on Windows.
     *
     * @return the project name that we use for the given platform.
     */
    public static synchronized String getTestProjectName() {
        String retVal;
        LOGGER.info(Thread.currentThread().getName() + "********************************************************* TestHelper.getTestProjectName");
        if (Utility.isMacintosh()) {
            retVal = "Test Project";
        } else if (Utility.isLinux()) {
            retVal = "Test Project";
        } else {
            retVal = "Test ProjectW";
        }
        return retVal;
    }

    /**
     * Always create a new project property file for the test project.
     */
    public static synchronized void initProjectProperties() {
        LOGGER.info("[{}] ********************************************************* TestHelper.initProjectProperties", Thread.currentThread().getName());
        try {
            String projectPropertiesFilename = System.getProperty(USER_DIR)
                    + File.separator
                    + QVCSConstants.QVCS_PROPERTIES_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_SERVED_PROJECTNAME_PREFIX + getTestProjectName() + ".properties";
            File projectPropertiesFile = new File(projectPropertiesFilename);
            LOGGER.info("[{}] ********************************************************* TestHelper.initProjectProperties project properties file name: [{}] ", Thread.currentThread().getName(), projectPropertiesFilename);

            if (projectPropertiesFile.exists()) {
                // If the properties file exists, delete it, so we can create a fresh one.
                projectPropertiesFile.delete();
                LOGGER.info("[{}] ********************************************************* TestHelper.initProjectProperties deleting existing property file.", Thread.currentThread().getName());
            }
            // Make sure the properties directory exists...
            if (!projectPropertiesFile.getParentFile().exists()) {
                projectPropertiesFile.getParentFile().mkdirs();
            }

            // Make sure the property file exists. (This should create it.)
            if (projectPropertiesFile.createNewFile()) {
                AbstractProjectProperties projectProperties = ProjectPropertiesFactory.getProjectPropertiesFactory().buildProjectProperties(System.getProperty("user.dir"),
                        getTestProjectName(), QVCSConstants.QVCS_SERVED_PROJECT_TYPE);

                // This is where the archives go...
                String projectLocation = System.getProperty(USER_DIR)
                        + File.separator
                        + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                        + File.separator
                        + getTestProjectName();

                File archiveLocation = new File (projectLocation);
                String canonicalArchiveLocation = archiveLocation.getCanonicalPath();

                // This the root directory for the archives for this project.
                projectProperties.setArchiveLocation(canonicalArchiveLocation);

                // Make sure the directory exists.
                File projectDirectory = new File(projectLocation);
                projectDirectory.mkdirs();

                // Set the project info for the reference copies
                projectProperties.setCreateReferenceCopyFlag(false);

                projectProperties.setDirectoryContentsInitializedFlag(true);

                projectProperties.saveProperties();
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Compare 2 files to see if they have the same contents.
     *
     * @param file1 the first file
     * @param file2 the 2nd file.
     * @return true if they have exactly the same contents; false if they are different.
     * @throws FileNotFoundException if either file cannot be found
     */
    public static boolean compareFilesByteForByte(File file1, File file2) throws FileNotFoundException, IOException {
        LOGGER.info("[{}] ********************************************************* TestHelper.compareFilesByteForByte", Thread.currentThread().getName());
        boolean compareResult = true;
        if (file1.exists() && file2.exists()) {
            if (file1.length() == file2.length()) {
                FileInputStream file2InputStream;
                try (FileInputStream file1InputStream = new FileInputStream(file1)) {
                    BufferedInputStream buffered1InputStream = new BufferedInputStream(file1InputStream);
                    file2InputStream = new FileInputStream(file2);
                    BufferedInputStream buffered2InputStream = new BufferedInputStream(file2InputStream);
                    byte[] file1Buffer = new byte[(int) file1.length()];
                    byte[] file2Buffer = new byte[(int) file2.length()];
                    buffered1InputStream.read(file1Buffer);
                    buffered2InputStream.read(file2Buffer);
                    for (int i = 0; i < file1.length(); i++) {
                        if (file1Buffer[i] != file2Buffer[i]) {
                            compareResult = false;
                            break;
                        }
                    }
                }
                file2InputStream.close();
            } else {
                compareResult = false;
            }
        } else if (file1.exists() && !file2.exists()) {
            compareResult = false;
        } else {
            compareResult = file1.exists() || !file2.exists();
        }
        return compareResult;
    }

    public static boolean copyFile(File fromFile, File toFile) {
        boolean retVal = true;
        try {
            Path source = fromFile.toPath();
            Path destination = toFile.toPath();
            Path dest = Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        }
        catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            retVal = false;
        }
        return retVal;
    }

    public static String addThreadAndTimeStamp() {
        Date now = new Date();
        StringBuilder s = new StringBuilder(Thread.currentThread().getName());
        s.append(" ").append(now.toString());
        return s.toString();
    }

    public static String buildTestDirectoryName(String testDirectorySuffix) {
        StringBuilder testDirectoryBuilder = new StringBuilder("/tmp/");
        testDirectoryBuilder.append(System.getProperty("user.name"))
                .append("/qvcse/")
                .append(testDirectorySuffix);
        return testDirectoryBuilder.toString();
    }

    private static void deleteRoleProjectBranchStore() {
        String roleProjectBranchStoreName =
                System.getProperty(USER_DIR)
                + File.separator
                + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_ROLE_PROJECT_BRANCH_STORE_NAME + "dat";
        File storeFile = new File(roleProjectBranchStoreName);
        if (storeFile.exists()) {
            storeFile.delete();
        }
    }

    public static void initRoleProjectBranchStore() {
        RoleManager.getRoleManager().initialize();
        RoleManager.getRoleManager().addUserRole(RoleManager.ADMIN, getTestProjectName(), USER_NAME, RoleManager.getRoleManager().DEVELOPER_ROLE);
    }

    public static Integer addUserToDatabase(String userName, String password) throws SQLException {
        Integer userId;
        byte[] hashedPassword = Utility.getInstance().hashPassword(password);
        databaseManager = DatabaseManager.getInstance();
        schemaName = databaseManager.getSchemaName();

        UserDAO userDAO = new UserDAOImpl(schemaName);
        User existingUser = userDAO.findByUserName(userName);
        if (existingUser == null) {
            User user = new User();
            user.setUserName(userName);
            user.setPassword(hashedPassword);
            user.setDeletedFlag(Boolean.FALSE);
            userId = userDAO.insert(user);
        } else {
            userId = existingUser.getId();
            userDAO.updateUserPassword(userId, hashedPassword);
        }
        return userId;
    }

    public static void initClientBranchManager() {
        ClientRequestListClientBranchesData data = new ClientRequestListClientBranchesData();
        data.setServerName(SERVER_NAME);
        data.setProjectName(getTestProjectName());
        ClientRequestListClientBranches listBranches = new ClientRequestListClientBranches(data);
        ServerResponseListBranches listBranchesResponse = (ServerResponseListBranches) listBranches.execute(USER_NAME, new BogusResponseObject());
        ClientBranchManager.getInstance().updateBranchInfo(listBranchesResponse);
    }

    public static void addTestFilesToTestProject() throws SQLException {
        sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserId(2);
        sourceControlBehaviorManager.setUserAndResponse(USER_NAME, new BogusResponseObject());
        File workfile1 = new File(SUBPROJECT_FIRST_SHORTWORKFILENAME);
        File workfile2 = new File(SECOND_SHORTWORKFILENAME);
        AtomicInteger mutableFileRevisionId = new AtomicInteger(-1);
        Integer file1Id = sourceControlBehaviorManager.addFile(QVCSConstants.QVCS_TRUNK_BRANCH, getTestProjectName(), "", SUBPROJECT_FIRST_SHORTWORKFILENAME, workfile1, new Date(), "Test Commit",
                mutableFileRevisionId);
        LOGGER.info("Added file: [{}] with id: [{}] and revision id: [{}]", SUBPROJECT_FIRST_SHORTWORKFILENAME, file1Id, mutableFileRevisionId.get());

        Integer file2Id = sourceControlBehaviorManager.addFile(QVCSConstants.QVCS_TRUNK_BRANCH, getTestProjectName(), SUBPROJECT_DIR_NAME, SECOND_SHORTWORKFILENAME, workfile2, new Date(), "Test Commit 2nd file",
                mutableFileRevisionId);
        LOGGER.info("Added file: [{}] with id: [{}] and revision id: [{}]", SECOND_SHORTWORKFILENAME, file2Id, mutableFileRevisionId.get());

        Integer file3Id = sourceControlBehaviorManager.addFile(QVCSConstants.QVCS_TRUNK_BRANCH, getTestProjectName(), SUBPROJECT2_DIR_NAME, THIRD_SHORTWORKFILENAME, workfile2, new Date(), "Test Commit 3rd file",
                mutableFileRevisionId);
        LOGGER.info("Added file: [{}] with id: [{}] and revision id: [{}]", THIRD_SHORTWORKFILENAME, file3Id, mutableFileRevisionId.get());

        Integer file4Id = sourceControlBehaviorManager.addFile(QVCSConstants.QVCS_TRUNK_BRANCH, getTestProjectName(), "", "Server.java", workfile2, new Date(), "Test Commit 4th file",
                mutableFileRevisionId);
        LOGGER.info("Added file: [{}] with id: [{}] and revision id: [{}]", "Server.java", file4Id, mutableFileRevisionId.get());

        Integer file5Id = sourceControlBehaviorManager.addFile(QVCSConstants.QVCS_TRUNK_BRANCH, getTestProjectName(), "", "AnotherServer.java", workfile2, new Date(), "Test Commit 5th file",
                mutableFileRevisionId);
        LOGGER.info("Added file: [{}] with id: [{}] and revision id: [{}]", "AnotherServer.java", file5Id, mutableFileRevisionId.get());

        Integer file6Id = sourceControlBehaviorManager.addFile(QVCSConstants.QVCS_TRUNK_BRANCH, getTestProjectName(), "", "ServerB.java", workfile2, new Date(), "Test Commit 6th file",
                mutableFileRevisionId);
        LOGGER.info("Added file: [{}] with id: [{}] and revision id: [{}]", "ServerB.java", file6Id, mutableFileRevisionId.get());

        Integer file7Id = sourceControlBehaviorManager.addFile(QVCSConstants.QVCS_TRUNK_BRANCH, getTestProjectName(), "", "ServerC.java", workfile2, new Date(), "Test Commit 7th file",
                mutableFileRevisionId);
        LOGGER.info("Added file: [{}] with id: [{}] and revision id: [{}]", "ServerC.java", file7Id, mutableFileRevisionId.get());

        sourceControlBehaviorManager.clearThreadLocals();
    }

}
