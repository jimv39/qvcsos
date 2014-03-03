//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
// $FilePath$
//     $Date: Wednesday, March 21, 2012 10:31:06 PM $
//   $Header: TestHelper.java Revision:1.7 Wednesday, March 21, 2012 10:31:06 PM JimVoris $
// $Copyright  2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryBaseClass;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.ServerUtility;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper class for JUnit tests.
 *
 * @author JimVoris
 */
public class TestHelper {

    /**
     * Create our logger object
     */
    private static Logger logger = Logger.getLogger("com.qumasoft.TestHelper");
    private static QVCSEnterpriseServer enterpriseServer = null;
    private static Timer killTimer = null;
    private static TimerTask killServerTask = null;
    private static final long KILL_DELAY = 5000;
    private static final long START_DELAY = 25000;
    public static final String SERVER_NAME = "Test Server";
    public static final String USER_NAME = "JimVoris";
    public static final String PASSWORD = "password";
    public static final String SUBPROJECT_DIR_NAME = "subProjectDirectory";
    public static final String SUBPROJECT_APPENDED_PATH = "subProjectDirectory";
    public static final String SUBPROJECT2_DIR_NAME = "subProjectDirectory2";
    public static final String SUBPROJECT2_APPENDED_PATH = "subProjectDirectory/subProjectDirectory2";
    public static final String SUBPROJECT_FIRST_SHORTWORKFILENAME = "QVCSEnterpriseServer.java";
    public static final String SECOND_SHORTWORKFILENAME = "Server.java";
    public static final String THIRD_SHORTWORKFILENAME = "AnotherServer.java";
    public static final String SUBPROJECT2_FIRST_SHORTWORKFILENAME = "ThirdDirectoryFile.java";

    /**
     * Start the QVCS Enterprise server.
     */
    public static void startServer() {
        if (enterpriseServer == null) {
            // So the server starts fresh.
            initDirectories();

            // So the server uses a project property file useful for the machine the tests are running on.
            initProjectProperties();

            // For unit testing, listen on the 2xxxx ports.
            String args[] = {System.getProperty("user.dir"), "29887", "29888", "29889", "29890", "29080"};
            enterpriseServer = new QVCSEnterpriseServer(args);
            ServerResponseFactoryBaseClass.setShutdownInProgress(false);
            killTimer = new Timer();
            Runnable worker = new Runnable() {

                @Override
                public void run() {
                    try {
                        enterpriseServer.startServer();
                    } catch (SQLException e) {
                        logger.log(Level.SEVERE, null, e);
                    } catch (QVCSException e) {
                        logger.log(Level.SEVERE, null, e);
                    }
                }
            };
            // Put all this on a separate worker thread.
            new Thread(worker).start();
            try {
                Thread.sleep(START_DELAY);
            } catch (InterruptedException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } else {
            // Kill the timer job that will stop the server.
            if (killServerTask != null) {
                killServerTask.cancel();
                killServerTask = null;
            }
        }
        logger.log(Level.INFO, "returning from startServer");
    }

    /**
     * Stop the QVCS Enterprise server. The server will exit after a delay period so that we don't create and destroy the server for every unit test.
     */
    public static synchronized void stopServer() {
        TimerTask killTask = new TimerTask() {

            @Override
            public void run() {
                String args[] = {};
                QVCSEnterpriseServer.stopServer(args);
            }
        };
        Date now = new Date();
        Date whenToRun = new Date(now.getTime() + KILL_DELAY);
        killTimer.schedule(killTask, whenToRun);
        killServerTask = killTask;
    }

    /**
     * Kill the server -- i.e. shut it down immediately. Some tests need the server to have been shutdown so that their initialization code works correctly.
     */
    public static synchronized void stopServerImmediately() {
        String args[] = {};
        QVCSEnterpriseServer.stopServer(args);
        try {
            Thread.sleep(KILL_DELAY);
        } catch (InterruptedException ex) {
            Logger.getLogger(TestHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Get the enterprise server instance.
     *
     * @return the Enterprise server instance.
     */
    public static QVCSEnterpriseServer getServer() {
        return enterpriseServer;
    }

    private static void initDirectories() {
        // Delete the file id store so the server starts fresh.
        String storeName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_META_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_FILEID_STORE_NAME
                + ".dat";
        File storeFile = new File(storeName);
        if (storeFile.exists()) {
            storeFile.delete();
        }
    }

    /**
     * Delete the view store.
     */
    public static void deleteViewStore() {
        String viewStoreName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_VIEW_STORE_NAME + "dat";
        File viewStoreFile = new File(viewStoreName);
        if (viewStoreFile.exists()) {
            viewStoreFile.delete();
        }
        String viewLabelStoreName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_META_DATA_DIRECTORY
                + File.separator
                + QVCSConstants.DIRECTORY_CONTENTS_LABEL_STORE_NAME
                + ".dat";
        File viewLabelStoreFile = new File(viewLabelStoreName);
        if (viewLabelStoreFile.exists()) {
            viewLabelStoreFile.delete();
        }
    }

    /**
     * Create archive files that we'll use for testing.
     */
    static public void initializeArchiveFiles() {
        File sourceFile = new File(System.getProperty("user.dir") + File.separator + "QVCSEnterpriseServer.kbwb");
        String firstDestinationDirName = System.getProperty("user.dir")
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
        try {
            ServerUtility.copyFile(sourceFile, firstDestinationFile);
            ServerUtility.copyFile(sourceFile, secondDestinationFile);
            ServerUtility.copyFile(sourceFile, thirdDestinationFile);
            ServerUtility.copyFile(sourceFile, fourthDestinationFile);
            ServerUtility.copyFile(sourceFile, fifthDestinationFile);
        } catch (IOException ex) {
            Logger.getLogger(TestHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Remove archive files created during testing.
     */
    static public void removeArchiveFiles() {
        String firstDestinationDirName = System.getProperty("user.dir")
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
    static public void deleteDirectory(File directory) {
        File[] firstDirectoryFiles = directory.listFiles();
        if (firstDirectoryFiles != null) {
            for (File file : firstDirectoryFiles) {
                file.delete();
            }
        }
        directory.delete();
    }

    /**
     * Clean out the test directory. This is not fully recursive, since we don't want a run-away delete to wipe out all the contents of the disk by mistake.
     *
     * @param derbyTestDirectory the root directory of a derby db.
     */
    static public void emptyDerbyTestDirectory(final String derbyTestDirectory) {
        // Delete the files in the derbyTestDirectory directory.
        File tempDirectory = new File(derbyTestDirectory);
        File[] files = tempDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    File[] subFiles = file.listFiles();
                    for (File subFile : subFiles) {
                        if (subFile.isDirectory()) {
                            File[] subSubFiles = subFile.listFiles();
                            for (File subSubFile : subSubFiles) {
                                subSubFile.delete();
                            }
                        }
                        subFile.delete();
                    }
                }
                file.delete();
            }
        }
    }

    /**
     * Need this so that we use a different project properties file for testing on Windows.
     *
     * @return the project name that we use for the given platform.
     */
    public static String getTestProjectName() {
        if (Utility.isMacintosh()) {
            return "Test Project";
        } else {
            return "Test ProjectW";
        }
    }

    public static void initProjectProperties() {
        try {
            String propertiesDirectory = System.getProperty("user.dir") + File.separator + QVCSConstants.QVCS_PROPERTIES_DIRECTORY;
            File originFile;
            File destinationFile;
            if (Utility.isMacintosh()) {
                originFile = new File(propertiesDirectory + File.separator + "hide-qvcs.served.project.Test Project.properties");
                destinationFile = new File(propertiesDirectory + File.separator + "qvcs.served.project.Test Project.properties");
            } else {
                originFile = new File(propertiesDirectory + File.separator + "hide-qvcs.served.project.Test ProjectW.properties");
                destinationFile = new File(propertiesDirectory + File.separator + "qvcs.served.project.Test ProjectW.properties");
            }
            ServerUtility.copyFile(originFile, destinationFile);
        } catch (IOException ex) {
            Logger.getLogger(TestHelper.class.getName()).log(Level.SEVERE, null, ex);
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
        boolean compareResult = true;
        if (file1.exists() && file2.exists()) {
            if (file1.length() == file2.length()) {
                FileInputStream file1InputStream = new FileInputStream(file1);
                BufferedInputStream buffered1InputStream = new BufferedInputStream(file1InputStream);
                FileInputStream file2InputStream = new FileInputStream(file2);
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
                file1InputStream.close();
                file2InputStream.close();
            } else {
                compareResult = false;
            }
        } else if (file1.exists() && !file2.exists()) {
            compareResult = false;
        } else if (!file1.exists() && file2.exists()) {
            compareResult = false;
        } else {
            // Neither file exists, so they are 'equal'.
            compareResult = true;
        }
        return compareResult;
    }
}
