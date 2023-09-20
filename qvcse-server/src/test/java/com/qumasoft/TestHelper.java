/*   Copyright 2004-2023 Jim Voris
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

import com.qumasoft.qvcslib.BogusResponseObject;
import com.qumasoft.qvcslib.ClientBranchManager;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientBranchesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerShutdownData;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.QVCSShutdownException;
import com.qumasoft.server.RoleManager;
import com.qumasoft.server.clientrequest.ClientRequestListClientBranches;
import com.qumasoft.server.clientrequest.ClientRequestServerShutdown;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.ServerTransactionManager;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
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
    private static final long KILL_DELAY = 11000L;
    private static final long ONE_SECOND = 1000L;
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
     * Start server in separate JVM.
     * @return the pid of the server application.
     */
    public static long startServerInSeparateJVM() {
        long pid = -1L;
        String originalUserDir = System.getProperty("user.dir");
        String newUserDir = String.format("%s../testenterprise/testDeploy", originalUserDir);
        System.setProperty("user.dir", newUserDir);
        try {
            // java -Xmx512m -Xms512m -jar qvcse-server-$QVCS_VERSION.jar "$QVCS_HOME" 29889 29890 29080 postgresql
            String execString = String.format("java -Xmx512m -Xms512m -jar qvcse-server-%s.jar \"%s\" 39889 39890 39080 postgresql", "4.1.7-SNAPSHOT", newUserDir);
            Thread.sleep(ONE_SECOND);
//            String execString = String.format("psql -f %s/postgres_qvcsos410_test_script.sql postgresql://postgres:postgres@localhost:5432/postgres", userDir);
            Process p = Runtime.getRuntime().exec(execString);
//            p.waitFor();
//            LOGGER.info("Reset test database process exit value: [{}]", p.exitValue());
            pid = p.pid();
            LOGGER.info("Server pid: [{}]", pid);
            System.setProperty("user.dir", originalUserDir);
        }
        catch (IOException | InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
        return pid;
    }

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
            initRoleProjectBranchStore();

            // The database should only be accessed by the server thread, etc.
            DatabaseManager.getInstance().shutdownDatabase();

            // For unit testing, listen on the 2xxxx ports.
            serverStartSyncString = "Sync server start";
            String userDir = System.getProperty(USER_DIR);
            File userDirFile = new File(userDir);
            String canonicalUserDir = userDirFile.getCanonicalPath();
            final String args[] = {canonicalUserDir, "39889", "39890", "39080", serverStartSyncString};
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
                } catch (InterruptedException e) {
                    LOGGER.error(e.getLocalizedMessage(), e);
                }

            }
            try {
                // Give the server a little time to really start.
                Thread.sleep(ONE_SECOND);
            } catch (InterruptedException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
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

    public static synchronized void stopServerByMessage() {
        ClientRequestServerShutdownData request = new ClientRequestServerShutdownData();
        request.setUserName(RoleManager.ADMIN);
        request.setPassword(Utility.getInstance().hashPassword(RoleManager.ADMIN));
        ClientRequestServerShutdown clientRequestServerShutdown = new ClientRequestServerShutdown(request);
        try {
            clientRequestServerShutdown.execute(RoleManager.ADMIN, new BogusResponseObject());
        }
        catch (QVCSShutdownException e) {
            LOGGER.info("Got expected shutdown exception");
        }
    }

    /**
     * Kill the server -- i.e. shut it down immediately. Some tests need the server to have been shutdown so that their initialization code works
     * @param syncObject an object the server will use to notify us on shutdown.
     * It <b>MUST</b> be the same object returned from startServer!!!
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

    public static String getFeatureBranchName() {
        return "2.2.2";
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
                            LOGGER.info("False at 280: [{}] : [{}]", file1.getName(), i);
                            break;
                        }
                    }
                }
                file2InputStream.close();
            } else {
                compareResult = false;
                LOGGER.info("False at 287 -- lengths are different; file1 [{}] length: [{}] file2 length: [{}]", file1.getName(), file1.length(), file2.length());
            }
        } else if (file1.exists() && !file2.exists()) {
            compareResult = false;
            LOGGER.info("False at 287; file1 [{}] exists: [{}] file2 exists: [{}]", file1.getName(), file1.exists(), file2.exists());
        } else {
            compareResult = file1.exists() || !file2.exists();
            LOGGER.info("False at 294; file1 [{}] exists: [{}] file2 exists: [{}]", file1.getName(), file1.exists(), file2.exists());
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

    public static Integer updateUserPassword(String userName, String newPassword) throws SQLException {
        Integer userId;
        byte[] hashedPassword = Utility.getInstance().hashPassword(newPassword);
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

    public static Integer updateAdminPassword() throws SQLException {
        Integer userId;
        byte[] hashedPassword = Utility.getInstance().hashPassword(RoleManager.ADMIN);
        databaseManager = DatabaseManager.getInstance();
        schemaName = databaseManager.getSchemaName();

        UserDAO userDAO = new UserDAOImpl(schemaName);
        User existingUser = userDAO.findByUserName(RoleManager.ADMIN);
        if (existingUser == null) {
            User user = new User();
            user.setUserName(RoleManager.ADMIN);
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
        BogusResponseObject bogusResponse = new BogusResponseObject();
        sourceControlBehaviorManager.setUserAndResponse(USER_NAME, bogusResponse);
        File workfile1 = new File(SUBPROJECT_FIRST_SHORTWORKFILENAME);
        File workfile2 = new File(SECOND_SHORTWORKFILENAME);
        TestHelper.beginTransaction(bogusResponse);
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
        TestHelper.endTransaction(bogusResponse);
    }

    public static void beginTransaction(ServerResponseFactoryInterface response) {
        ServerTransactionManager.getInstance().clientBeginTransaction(response);
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
        }
        catch (SQLException e) {
            LOGGER.warn(null, e);
            throw new QVCSRuntimeException(e.getLocalizedMessage());
        }
    }

    public static void endTransaction(ServerResponseFactoryInterface response) {
        ServerTransactionManager.getInstance().clientEndTransaction(response);
        try {
            if (!ServerTransactionManager.getInstance().transactionIsInProgress(response)) {
                DatabaseManager.getInstance().closeConnection();
            }
        }
        catch (SQLException e) {
            LOGGER.warn(null, e);
            throw new QVCSRuntimeException(e.getLocalizedMessage());
        }
    }
}
