/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSServedProjectNamesFilter;
import com.qumasoft.qvcslib.ServedProjectProperties;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.server.dataaccess.BranchDAO;
import com.qumasoft.server.dataaccess.DirectoryDAO;
import com.qumasoft.server.dataaccess.FileDAO;
import com.qumasoft.server.dataaccess.ProjectDAO;
import com.qumasoft.server.dataaccess.impl.BranchDAOImpl;
import com.qumasoft.server.dataaccess.impl.DirectoryDAOImpl;
import com.qumasoft.server.dataaccess.impl.FileDAOImpl;
import com.qumasoft.server.dataaccess.impl.ProjectDAOImpl;
import com.qumasoft.server.datamodel.Branch;
import com.qumasoft.server.datamodel.Directory;
import com.qumasoft.server.datamodel.Project;
import com.qumasoft.webserver.WebServer;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The QVCS Enterprise server class. This is the main class for the QVCS Enterprise server.
 *
 * @author Jim Voris
 */
public final class QVCSEnterpriseServer {
    private static final String USER_DIR = "user.dir";
    static final int DEFAULT_NON_SECURE_LISTEN_PORT = 9889;
    static final int DEFAULT_ADMIN_LISTEN_PORT = 9890;
    static final String DERBY_DB_SERVER = "derby";
    static final String POSTGRESQL_DB_SERVER = "postgresql";
    static final String WEB_SERVER_PORT = "9080";
    static final int WORKER_THREAD_COUNT = 50;
    private static final int ARGS_DB_SERVER_TYPE_INDEX = 4;
    private static final int ARGS_LENGTH_WITH_SYNC_OBJECT = 6;
    private static final int ARGS_SYNC_OBJECT_INDEX = 5;
    private int nonSecurePort = DEFAULT_NON_SECURE_LISTEN_PORT;
    private int adminPort = DEFAULT_ADMIN_LISTEN_PORT;
    private static final long THREAD_POOL_AWAIT_TERMINATION_DELAY = 5;
    private String qvcsHomeDirectory = null;
    private final String[] arguments;
    private static boolean serverIsRunningFlag;
    private static boolean useDerbyFlag = false;

    private final ExecutorService threadPool = Executors.newCachedThreadPool();
    private NonSecureServer nonSecureServer = null;
    private NonSecureServer adminServer = null;
    private QVCSWebServer webServer = null;
    // Server socket listener threads.
    private Thread nonSecureThread = null;
    private Thread adminThread = null;
    // Web server thread.
    private Thread webServerThread = null;
    private static QVCSEnterpriseServer qvcsEnterpriseServer;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(QVCSEnterpriseServer.class);
    private static final List<ServerResponseFactoryInterface> CONNECTED_USERS_COLLECTION = Collections.synchronizedList(new ArrayList<ServerResponseFactoryInterface>());

    private final Object syncObject;
    private static DatabaseManagerInterface databaseManagerInterface = null;

    /**
     * Main entry point to the QVCS-Enterprise server.
     * @param args command line arguments. The command arguments are: [0] QVCS home directory [1] non-secure listen port (9889) [2] admin listen port (9890)
     * [3] web server port (9080) [4] {"derby" | "postgresql" } which db server to use. [5] a sync object used for ant task synchronization.
     */
    @SuppressWarnings("SynchronizeOnNonFinalField")
    public static void main(String[] args) {
        qvcsEnterpriseServer = new QVCSEnterpriseServer(args);
        try {
            qvcsEnterpriseServer.startServer();
        } catch (SQLException | ClassNotFoundException e) {
            LOGGER.error("Failed to initialize the database. " + e.getLocalizedMessage());
            if (qvcsEnterpriseServer.syncObject != null) {
                synchronized (qvcsEnterpriseServer.syncObject) {
                    qvcsEnterpriseServer.syncObject.notifyAll();
                }
            }
        } catch (QVCSException e) {
            LOGGER.error("Caught QVCSException. " + e.getLocalizedMessage());
            if (qvcsEnterpriseServer.syncObject != null) {
                synchronized (qvcsEnterpriseServer.syncObject) {
                    qvcsEnterpriseServer.syncObject.notifyAll();
                }
            }
        } finally {
            LOGGER.info("Server exit complete.");
        }
    }

    /**
     * Is the server running.
     * @return true if the server is running; false if it is not running.
     */
    public static boolean getServerIsRunningFlag() {
        return serverIsRunningFlag;
    }

    /**
     * Get a collection of client connections.
     * @return a collection of client connections.
     */
    public static Collection<ServerResponseFactoryInterface> getConnectedUsers() {
        List<ServerResponseFactoryInterface> collection;
        synchronized (CONNECTED_USERS_COLLECTION) {
            collection = new ArrayList<>();
            Iterator<ServerResponseFactoryInterface> it = CONNECTED_USERS_COLLECTION.iterator();
            while (it.hasNext()) {
                collection.add(it.next());
            }
        }
        return collection;
    }

    /**
     * Get the database manager. It could be one for derby, or for postgresql.
     * @return the database manager we'll be using.
     */
    public static DatabaseManagerInterface getDatabaseManager() {
        return databaseManagerInterface;
    }

    /**
     * Set the database manager. We need this one for our test code.
     * @param databaseManager which database manager to use... derby or postgresql.
     */
    public static void setDatabaseManager(DatabaseManagerInterface databaseManager) {
        databaseManagerInterface = databaseManager;
    }

    static Collection<ServerResponseFactoryInterface> getConnectedUsersCollection() {
        return CONNECTED_USERS_COLLECTION;
    }

    /**
     * Set the shutdown in progress flag. The server will stop accepting client connections if the flag is true.
     * @param flag set to true to shutdown the server.
     */
    public static void setShutdownInProgress(boolean flag) {
        if (flag) {
            LOGGER.info("QVCS Enterprise Server is exiting.");

            if ((qvcsEnterpriseServer != null) && (qvcsEnterpriseServer.nonSecureThread != null)) {
                // Don't accept any more client connection requests on standard client port.
                qvcsEnterpriseServer.nonSecureServer.closeServerSocket();
            }
            if ((qvcsEnterpriseServer != null) && (qvcsEnterpriseServer.adminThread != null)) {
                // Don't accept any more client connection requests on admin client port.
                qvcsEnterpriseServer.adminServer.closeServerSocket();
            }
        }
    }

    /**
     * Creates a new instance of QVCSEnterpriseServer. Only accessible via calls to main().
     *
     * @param args command line arguments.
     */
    private QVCSEnterpriseServer(String[] args) {
        if (args != null) {
            String[] localArgs = new String[args.length];
            System.arraycopy(args, 0, localArgs, 0, args.length);
            this.arguments = localArgs;
            if (args.length == ARGS_LENGTH_WITH_SYNC_OBJECT) {
                syncObject = args[ARGS_SYNC_OBJECT_INDEX];
            } else {
                syncObject = new Object();
            }
        } else {
            this.arguments = new String[0];
            syncObject = new Object();
        }
        if (arguments.length > 0) {
            System.setProperty(USER_DIR, arguments[0]);
        }
        qvcsHomeDirectory = System.getProperty(USER_DIR);
    }

    private void startServer() throws SQLException, QVCSException, ClassNotFoundException {
        try {
            if (arguments.length > 1) {
                nonSecurePort = Integer.parseInt(arguments[1]);
            }
        } catch (NumberFormatException e) {
            nonSecurePort = DEFAULT_NON_SECURE_LISTEN_PORT;
        }

        try {
            if (arguments.length > 2) {
                adminPort = Integer.parseInt(arguments[2]);
            }
        } catch (NumberFormatException e) {
            adminPort = DEFAULT_ADMIN_LISTEN_PORT;
        }

        if (arguments.length > ARGS_DB_SERVER_TYPE_INDEX) {
            LOGGER.info(arguments[ARGS_DB_SERVER_TYPE_INDEX]);
            if (0 == arguments[ARGS_DB_SERVER_TYPE_INDEX].compareToIgnoreCase(POSTGRESQL_DB_SERVER)) {
                databaseManagerInterface = PostgresDatabaseManager.getInstance();
            } else {
                useDerbyFlag = true;
            }
        } else {
            useDerbyFlag = true;
        }
        if (useDerbyFlag) {
            databaseManagerInterface = DatabaseManager.getInstance();

            // Define the derby database location.
            DatabaseManager.getInstance().setDerbyHomeDirectory(getDerbyHomeDirectory());
        }


        // Report the System info.
        reportSystemInfo();

        LOGGER.info("QVCS Enterprise Server Version: '" + QVCSConstants.QVCS_RELEASE_VERSION + "'.");
        LOGGER.info("QVCS Enterprise Server running with " + Runtime.getRuntime().availableProcessors() + " available processors.");


        // Initialize the role privileges manager
        RolePrivilegesManager.getInstance().initialize();

        // Initialize the role manager.
        RoleManager.getRoleManager().initialize();

        // Initialize the authentication manager.
        AuthenticationManager.getAuthenticationManager().initialize();

        // Initialize the archive digest manager.
        ArchiveDigestManager.getInstance().initialize(QVCSConstants.QVCS_SERVED_PROJECT_TYPE);

        // Initialize the file ID manager.
        FileIDManager.getInstance().initialize();

        // See if we need to scan for fileID, etc.
        if (FileIDManager.getInstance().getFileIDResetRequiredFlag()) {
            // If we're using derby... wipe out the database.
            if (useDerbyFlag) {
                wipeDatabase();
            }

            // Initialize the database.
            getDatabaseManager().initializeDatabase();

            // Reset the directory id dictionary store.
            DirectoryIDDictionary.getInstance().resetStore();
            DirectoryIDDictionary.getInstance().initialize();

            // Reset the directoryID store.
            DirectoryIDManager.getInstance().resetStore();
            DirectoryIDManager.getInstance().initialize();

            // Reset the file id dictionary store.
            FileIDDictionary.getInstance().resetStore();
            FileIDDictionary.getInstance().initialize();

            // Remove the branch manager store.
            BranchManager.getInstance().resetStore();
            BranchManager.getInstance().initialize();

            // We need to reset all file IDs. This actually sets the files ids in the archive files.
            resetFileIDs();

            // Reset the directory IDs. This actually clears all the directory ids. They get assigned later.
            resetDirectoryIDs();

            // Initialize our DirectoryContents objects.
            initializeDirectoryContentsObjects();

            FileIDManager.getInstance().setFileIDResetRequiredFlag(false);
        } else {
            // Initialize the database.
            getDatabaseManager().initializeDatabase();

            // Initialize the DirectoryIDDictionary.
            DirectoryIDDictionary.getInstance().initialize();

            // Initialize the FileIDDictionary.
            FileIDDictionary.getInstance().initialize();

            // Initialize the directoryID manager.
            DirectoryIDManager.getInstance().initialize();

            // Initialize the BranchManager.
            BranchManager.getInstance().initialize();

        }
        // Validate/Update database to match what exists on the trunk.
        DatabaseVerificationManager.getInstance().verifyTrunkToDatabase(getServedProjectPropertiesList());

        // Register our shutdown thread.
        Runtime.getRuntime().addShutdownHook(new QVCSEnterpriseServer.ShutdownThread());

        // Initialize the Activity Journal Manager.
        ActivityJournalManager.getInstance().initialize();
        ActivityJournalManager.getInstance().addJournalEntry("QVCS-Enterprise Server is starting.  Server Version: " + QVCSConstants.QVCS_RELEASE_VERSION + ".");

        // Launch three separate listener threads
        // one for non-secure requests,
        // one for admin messages.
        // one for the embedded web server.
        nonSecureServer = new NonSecureServer(nonSecurePort);
        adminServer = new NonSecureServer(adminPort);
        webServer = new QVCSWebServer(arguments);

        nonSecureThread = new Thread(nonSecureServer, "non secure server");
        adminThread = new Thread(adminServer, "admin server");
        webServerThread = new Thread(webServer, "web server");
        webServerThread.setDaemon(true);

        nonSecureThread.start();
        adminThread.start();
        webServerThread.start();

        // This will notify the TestHelper that the server is ready for use.
        if (syncObject != null) {
            synchronized (syncObject) {
                syncObject.notifyAll();
            }
        }
        serverIsRunningFlag = true;

        try {
            nonSecureThread.join();
            adminThread.join();

            // Kill the web server.
            webServerThread.interrupt();

            // Shut down the thread pool and wait for all the worker threads to exit.
            threadPool.shutdown(); // Disable new tasks from being submitted
            LOGGER.info("Threadpool shutdown called.");
            try {
                // Wait a while for existing tasks to terminate
                if (!threadPool.awaitTermination(THREAD_POOL_AWAIT_TERMINATION_DELAY, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow(); // Cancel currently executing tasks
                    // Wait a while for tasks to respond to being cancelled
                    if (!threadPool.awaitTermination(THREAD_POOL_AWAIT_TERMINATION_DELAY, TimeUnit.SECONDS)) {
                        LOGGER.warn("Thread pool did not terminate");
                    }
                }
            } catch (InterruptedException e) {
                // (Re-)Cancel if current thread also interrupted
                threadPool.shutdownNow();
                // Preserve interrupt status
                Thread.currentThread().interrupt();
            }
        } catch (InterruptedException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            getDatabaseManager().shutdownDatabase();
            ActivityJournalManager.getInstance().addJournalEntry("QVCS-Enterprise Server is shutting down.");
            ArchiveDigestManager.getInstance().writeStore();
            DirectoryIDManager.getInstance().writeStore();
            FileIDManager.getInstance().writeStore();
            DirectoryIDDictionary.getInstance().writeStore();
            FileIDDictionary.getInstance().writeStore();
            ActivityJournalManager.getInstance().closeJournal();
            LOGGER.info("QVCS Enterprise Server exit complete.");
            System.out.println("QVCS Enterprise Server exit complete.");
            serverIsRunningFlag = false;
            if (syncObject != null) {
                synchronized (syncObject) {
                    syncObject.notifyAll();
                }
            }
        }
    }

    /**
     * Figure out where we put the derby database.
     *
     * @return the directory name for the root of the derby database.
     */
    private String getDerbyHomeDirectory() {
        return qvcsHomeDirectory + File.separator + QVCSConstants.QVCS_DERBY_DB_DIRECTORY;
    }

    /**
     * Report the system's information to the log file.... basically all the system properties.
     */
    private void reportSystemInfo() {
        java.util.Properties systemProperties = System.getProperties();
        java.util.Set keys = systemProperties.keySet();
        java.util.Iterator it = keys.iterator();
        StringBuilder messageString = new StringBuilder();
        messageString.append("\nSystem properties:\n");
        while (it.hasNext()) {
            String key = (String) it.next();
            String message = key + " = " + System.getProperty(key);
            messageString.append(message);
            messageString.append("\n");
        }
        LOGGER.info(messageString.toString());

        // Log what charset is the platform default
        LOGGER.info("Default charset: " + Charset.defaultCharset().displayName());
    }

    /**
     * This method is called <i>before</i> the server opens any ports to listen for client connections... The goal is to reset the
     * file ids for all files in all existing projects. We do <i>not</i> use the ArchiveDirManager class here as that is
     * heavier-weight than what we want/need to do here.
     */
    private void resetFileIDs() {
        LOGGER.info("QVCSEnterpriseServer: resetting all file id's for all projects.");

        try {
            // Get a list of all the projects that this server serves...
            ServedProjectProperties[] projectPropertiesList = getServedProjectPropertiesList();
            for (ServedProjectProperties projectPropertiesList1 : projectPropertiesList) {
                String archiveLocation = projectPropertiesList1.getArchiveLocation();
                File projectBaseDirectory = new File(archiveLocation);
                resetFileIDsForProjectDirectoryTree(projectBaseDirectory);
                resetCemeteryFilenames(projectBaseDirectory);
                // Remove any branch archive files, since they are now worthless since we
                // have destroyed the directory contents, and the ability to see branches.
                removeProjectBranchArchiveFiles(projectBaseDirectory);
            }
        } finally {
            LOGGER.info("QVCSEnterpriseServer: reset file id's completed.");
        }
    }

    /**
     * <p>This method is called <i>before</i> the server opens any ports to listen for client connections. This method is only
     * called when resetting the file id's and has the task of renaming the cemetery files of a project so that the cemetery file's
     * name matches the file id that has be re-assigned to that cemetery file. This method should be called <i>after</i> the
     * resetFileIDs.</p> <p>Note that we have to iterate over the cemetery twice; first to rename the files to a name that is
     * guaranteed not to collide with any existing cemetery file name; and a 2nd time to rename them to their correct cemetery file
     * name.</p>
     *
     * @param projectBaseDirectory the base directory of a project's archive files.
     */
    private void resetCemeteryFilenames(File projectBaseDirectory) {
        File cemeteryDirectory = new File(projectBaseDirectory.getAbsolutePath() + File.separator + QVCSConstants.QVCS_CEMETERY_DIRECTORY);

        LOGGER.info("Renaming cemetery files for directory: [" + cemeteryDirectory.getAbsolutePath() + "]");

        File[] fileList = cemeteryDirectory.listFiles();

        if (fileList != null) {
            for (File fileList1 : fileList) {
                if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_CACHE_NAME) == 0) {
                    // Get rid of the cache file, since we may be changing things here that
                    // would make the cache out of date.
                    if (fileList1.delete()) {
                        LOGGER.info("Deleting " + QVCSConstants.QVCS_CACHE_NAME + " file from directory: " + cemeteryDirectory.getAbsolutePath());
                    }
                    continue;
                }
                if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_JOURNAL_NAME) == 0) {
                    continue;
                }
                if (fileList1.isDirectory()) {
                    // Do not traverse a directory tree in the cemetery. It should be flat.
                    continue;
                }
                if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_DIRECTORYID_FILENAME) == 0) {
                    continue;
                }
                if (fileList1.getName().endsWith(QVCSConstants.QVCS_ARCHIVE_TEMPFILE_SUFFIX)) {
                    continue;
                }
                if (fileList1.getName().endsWith(QVCSConstants.QVCS_ARCHIVE_OLDFILE_SUFFIX)) {
                    continue;
                }
                if (Utility.isMacintosh() && fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_MAC_DS_STORE_FILENAME) == 0) {
                    continue;
                }
                LogFile logfile = new LogFile(fileList1.getPath());
                if (logfile.readInformation()) {
                    try {
                        int fileId = logfile.getFileID();
                        String transientCemeteryFilename = cemeteryDirectory.getCanonicalPath() + File.separator + "TRANSIENT_QVCS_CEMETERY_NAME_" + fileId;
                        File transientCemeteryFile = new File(transientCemeteryFilename);
                        fileList1.renameTo(transientCemeteryFile);
                    } catch (IOException ex) {
                        LOGGER.error(ex.getLocalizedMessage(), ex);
                    }
                } else {
                    LOGGER.warn("Failed to read logfile information for: [" + fileList1.getPath() + "]");
                    LOGGER.warn("Deleting corrupt logfile from cemetery: [" + fileList1.getPath() + "]");
                    fileList1.delete();
                }
            }

            // Go through the files in the cemetery again, this time, rename to the name they should have.
            File[] renamedFileList = cemeteryDirectory.listFiles();

            if (renamedFileList != null) {
                for (File renamedFileList1 : renamedFileList) {
                    if (renamedFileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_CACHE_NAME) == 0) {
                        // Get rid of the cache file, since we may be changing things here that
                        // would make the cache out of date.
                        if (renamedFileList1.delete()) {
                            LOGGER.info("Deleting " + QVCSConstants.QVCS_CACHE_NAME + " file from directory: " + cemeteryDirectory.getAbsolutePath());
                        }
                        continue;
                    }
                    if (renamedFileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_JOURNAL_NAME) == 0) {
                        continue;
                    }
                    if (renamedFileList1.isDirectory()) {
                        // Do not traverse a directory tree in the cemetery. It should be flat.
                        continue;
                    }
                    if (renamedFileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_DIRECTORYID_FILENAME) == 0) {
                        continue;
                    }
                    if (renamedFileList1.getName().endsWith(QVCSConstants.QVCS_ARCHIVE_TEMPFILE_SUFFIX)) {
                        continue;
                    }
                    if (renamedFileList1.getName().endsWith(QVCSConstants.QVCS_ARCHIVE_OLDFILE_SUFFIX)) {
                        continue;
                    }
                    if (Utility.isMacintosh() && renamedFileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_MAC_DS_STORE_FILENAME) == 0) {
                        continue;
                    }
                    LogFile logfile = new LogFile(renamedFileList1.getPath());
                    if (logfile.readInformation()) {
                        try {
                            int fileId = logfile.getFileID();
                            String permanentCemeteryFilename = cemeteryDirectory.getCanonicalPath() + File.separator + Utility.createCemeteryShortArchiveName(fileId);
                            File permanentCemeteryFile = new File(permanentCemeteryFilename);
                            renamedFileList1.renameTo(permanentCemeteryFile);
                        } catch (IOException e) {
                            LOGGER.error("Failed to get the canonical path for [" + cemeteryDirectory.getAbsolutePath() + "]", e);
                        }
                    } else {
                        LOGGER.warn("Failed to read logfile information for: [" + renamedFileList1.getPath() + "]");
                        LOGGER.warn("Deleting corrupt logfile from cemetery: [" + renamedFileList1.getPath() + "]");
                        renamedFileList1.delete();
                    }
                }
            }
        }
        LOGGER.info("Completed renaming cemetery files for directory: [" + cemeteryDirectory.getAbsolutePath() + "]");
    }

    /**
     * Remove the project's branch archive files. This are archive files that capture archive history for files that exist
     * <i>only</i> on a branch -- i.e. the file was never 'promoted' to the trunk. Since this method is called only if/when we are
     * resetting the file id's -- we have to throw these archives away since they are now orphaned. (since the branch will be gone
     * after the reset of the file ids).
     *
     * <p><b>For use before the server accepts clients connections only!!</b></p>
     *
     * @param projectBaseDirectory the base directory of a project's archive files.
     */
    private void removeProjectBranchArchiveFiles(File projectBaseDirectory) {
        File branchArchiveDirectory = new File(projectBaseDirectory.getAbsolutePath() + File.separator + QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY);

        LOGGER.info("Deleting branch archive files for directory: [" + branchArchiveDirectory.getAbsolutePath() + "]");

        File[] fileList = branchArchiveDirectory.listFiles();

        if (fileList != null) {
            for (File fileList1 : fileList) {
                LOGGER.info("Deleting branch archive file: [" + fileList1.getAbsolutePath() + "]");
                fileList1.delete();
            }
        }
    }

    /**
     * Get the list of projects that are 'served' by this server application. <p><b>For use before the server accepts clients
     * connections only!!</b></p>
     * @return the list of projects that this server has.
     */
    private ServedProjectProperties[] getServedProjectPropertiesList() {
        ServedProjectProperties[] servedProjectsProperties = new ServedProjectProperties[0];

        // Where all the property files can be found...
        File propertiesDirectory = new File(System.getProperty(USER_DIR)
                + System.getProperty("file.separator")
                + QVCSConstants.QVCS_PROPERTIES_DIRECTORY);

        QVCSServedProjectNamesFilter servedProjectNamesFilter = new QVCSServedProjectNamesFilter();
        File[] servedProjectFiles = propertiesDirectory.listFiles(servedProjectNamesFilter);
        if (servedProjectFiles != null) {
            servedProjectsProperties = new ServedProjectProperties[servedProjectFiles.length];

            for (int i = 0; i < servedProjectFiles.length; i++) {
                String projectName = servedProjectNamesFilter.getProjectName(servedProjectFiles[i].getName());
                try {
                    servedProjectsProperties[i] = new ServedProjectProperties(System.getProperty(USER_DIR), projectName);
                } catch (QVCSException e) {
                    LOGGER.warn("Error finding served project names for project: [{}]", projectName, e);
                }
            }
        }
        return servedProjectsProperties;
    }

    /**
     * For use by the resetFileIDs() method only!! Reset fileIDs for the given directory tree.
     * @param directory the directory wherein the file ids will get reset; this is a recursive file id reset.
     */
    @SuppressWarnings("LoggerStringConcat")
    private void resetFileIDsForProjectDirectoryTree(File directory) {
        LOGGER.info("Resetting file id's for directory: " + directory.getAbsolutePath());

        File[] fileList = directory.listFiles();

        if (fileList != null) {
            for (File fileList1 : fileList) {
                if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_CACHE_NAME) == 0) {
                    // Get rid of the cache file, since we may be changing things here that
                    // would make the cache out of date.
                    if (fileList1.delete()) {
                        LOGGER.info("Deleting " + QVCSConstants.QVCS_CACHE_NAME + " file from directory: " + directory.getAbsolutePath());
                    }
                    continue;
                }
                if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_JOURNAL_NAME) == 0) {
                    continue;
                }
                if (fileList1.isDirectory()) {
                    // Recurse through the directory tree...
                    resetFileIDsForProjectDirectoryTree(fileList1);
                    continue;
                }
                if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_DIRECTORYID_FILENAME) == 0) {
                    continue;
                }
                if (fileList1.getName().endsWith(QVCSConstants.QVCS_ARCHIVE_TEMPFILE_SUFFIX)) {
                    continue;
                }
                if (fileList1.getName().endsWith(QVCSConstants.QVCS_ARCHIVE_OLDFILE_SUFFIX)) {
                    continue;
                }
                if (Utility.isMacintosh() && fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_MAC_DS_STORE_FILENAME) == 0) {
                    continue;
                }
                LogFile logfile = new LogFile(fileList1.getPath());
                if (logfile.readInformation()) {
                    // Update the file's file ID. This will cause a re-write of the archive file with an updated
                    // header (supplemental info) that contains the assigned file id.
                    int newFileId = FileIDManager.getInstance().getNewFileID();

                    // Reset the file id and discard all branch labels that may have been applied to the archive files.
                    // we need to do this because we are starting over, and all branches have been discarded.
                    logfile.setFileIDAndRemoveBranchLabels(newFileId);
                    LOGGER.info("Reset file id for [" + logfile.getFullArchiveFilename() + "] to [" + newFileId + "]");
                } else {
                    LOGGER.warn("Failed to read logfile information for: [" + fileList1.getPath() + "]");
                }
            }
        }
    }

    /**
     * This method is called <i>before</i> the server opens any ports to listen for client connections... The goal is to reset the
     * directory ids for all existing projects.
     */
    private void resetDirectoryIDs() {
        LOGGER.info("QVCSEnterpriseServer: resetting all directory ids.");
        try {
            // Get a list of all the projects that this server serves...
            ServedProjectProperties[] projectPropertiesList = getServedProjectPropertiesList();
            for (ServedProjectProperties projectPropertiesList1 : projectPropertiesList) {
                String archiveLocation = projectPropertiesList1.getArchiveLocation();
                File projectBaseDirectory = new File(archiveLocation);
                removeDirectoryContentsArchiveFiles(projectPropertiesList1.getProjectName());
                resetDirectoryIDsForDirectoryTree(projectBaseDirectory);
            }
            DirectoryIDManager.getInstance().setMaximumDirectoryID(0);
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            LOGGER.info("QVCSEnterpriseServer: reset all directory ids complete.");
        }
    }

    /**
     * For use by the resetDirectoryIDs() method only!! Reset the given directory ids for the given directory tree.
     * @param directory directory for which directory id will get reset; recursive.
     */
    private void resetDirectoryIDsForDirectoryTree(File directory) {
        LOGGER.info("Resetting directory id for directory: [" + directory.getAbsolutePath() + "]");
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            for (File fileList1 : fileList) {
                if (fileList1.isDirectory()) {
                    // Recurse through the directory tree...
                    resetDirectoryIDsForDirectoryTree(fileList1);
                    continue;
                }
                if (fileList1.getName().compareToIgnoreCase(QVCSConstants.QVCS_DIRECTORYID_FILENAME) == 0) {
                    try {
                        // Delete the directory ID file... It will get re-created when we create the directory
                        // contents object for this directory...
                        fileList1.delete();
                    } catch (Exception e) {
                        LOGGER.warn(e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * For use only when resetting all directory contents!!
     *
     * @param projectName the project name.
     */
    private void removeDirectoryContentsArchiveFiles(String projectName) {
        String fullArchiveDirectory = System.getProperties().getProperty(USER_DIR)
                + File.separator
                + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                + File.separator
                + projectName
                + File.separator
                + QVCSConstants.QVCS_DIRECTORY_METADATA_DIRECTORY;
        File directoryFile = new File(fullArchiveDirectory);
        File[] fileList = directoryFile.listFiles();

        // Delete all the files in the directory used for storing directory contents archive files.
        if (fileList != null) {
            for (File fileList1 : fileList) {
                if (fileList1.isFile()) {
                    fileList1.delete();
                }
            }
        }
    }

    /**
     * Used once only when upgrading, or running the server for the very first time. This method should only be run before the
     * server accepts requests from client.
     */
    private void initializeDirectoryContentsObjects() throws SQLException, QVCSException {
        LOGGER.info("QVCSEnterpriseServer: Initializing DirectoryContents objects...");

        // Delete any/all existing directory contents objects... we're starting
        // from scratch here.
        deleteExistingDirectoryContentsObjects();

        try {
            // Get a list of all the projects that this server serves...
            ServedProjectProperties[] projectPropertiesList = getServedProjectPropertiesList();
            for (ServedProjectProperties projectPropertiesList1 : projectPropertiesList) {
                // Wrap this work in a server transaction so the DirectoryContents
                // stuff will behave in a useful way...
                ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
                // Keep track that we're in a transaction.
                ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
                String archiveLocation = projectPropertiesList1.getArchiveLocation();
                File projectBaseDirectory = new File(archiveLocation);
                // Create the project and its Trunk branch in the database.
                int branchId = createProjectAndTrunkDbRecords(projectPropertiesList1);
                // And initialize the directory contents objects for this project tree.
                initializeDirectoryContentsObjectForDirectory(projectBaseDirectory, projectPropertiesList1, branchId, -1, bogusResponseObject);
                // Keep track that we ended this transaction.
                ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            }

            // Throw away any archive dir managers we built since we now need
            // to built them again so they will discard (i.e. move) any obsolete
            // files.
            ArchiveDirManagerFactoryForServer.getInstance().resetDirectoryMap();
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw e;
        }
    }

    /**
     * Create the project record in the database. This method should only be called when starting the server for the first time, or
     * when rebuilding the database.
     *
     * @param servedProjectProperties the project properties for the project that we're initializing in the database.
     * @return the branchId of the Trunk branch for the given project.
     * @throws SQLException if the project already exists.
     */
    private int createProjectAndTrunkDbRecords(ServedProjectProperties servedProjectProperties) throws SQLException {
        Project project = new Project();
        project.setProjectName(servedProjectProperties.getProjectName());
        String schemaName = getDatabaseManager().getSchemaName();
        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        projectDAO.insert(project);

        Project foundProject = projectDAO.findByProjectName(servedProjectProperties.getProjectName());
        Branch branch = new Branch();
        branch.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
        branch.setBranchTypeId(1);
        branch.setProjectId(foundProject.getProjectId());
        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        branchDAO.insert(branch);

        Branch foundBranch = branchDAO.findByProjectIdAndBranchName(foundProject.getProjectId(), QVCSConstants.QVCS_TRUNK_BRANCH);
        return foundBranch.getBranchId();
    }

    /**
     * Used only during an upgrade or when product is run for the 1st time.
     */
    private void deleteExistingDirectoryContentsObjects() {
        try {
            // Get a list of all the projects that this server serves...
            ServedProjectProperties[] projectPropertiesList = getServedProjectPropertiesList();
            for (ServedProjectProperties projectPropertiesList1 : projectPropertiesList) {
                String projectName = projectPropertiesList1.getProjectName();
                String fullArchiveDirectory = System.getProperties().getProperty(USER_DIR)
                        + File.separator
                        + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                        + File.separator
                        + projectName
                        + File.separator
                        + QVCSConstants.QVCS_DIRECTORY_METADATA_DIRECTORY;
                File directoryFile = new File(fullArchiveDirectory);
                if (!directoryFile.exists()) {
                    if (!directoryFile.mkdirs()) {
                        continue;
                    }
                }
                // Delete all the files in the QVCS_DIRECTORY_METADATA_DIRECTORY
                File[] fileList = directoryFile.listFiles();
                if (fileList != null) {
                    for (File fileList1 : fileList) {
                        if (fileList1.isDirectory()) {
                            continue;
                        }
                        fileList1.delete();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Used only during an upgrade or when product is run for the 1st time.
     * @param directory the file system directory being initialized.
     * @param servedProjectProperties the properties of the project that we're working on.
     * @param branchId the branch id.
     * @param rootDirectoryId the directory id of the root directory.
     * @param bogusResponseObject a bogus (placeholder) response object.
     */
    private void initializeDirectoryContentsObjectForDirectory(File directory, ServedProjectProperties servedProjectProperties, int branchId, int rootDirectoryId,
            ServerResponseFactoryInterface bogusResponseObject) throws SQLException, QVCSException {
        LOGGER.info("Populating database for directory: [" + directory.getAbsolutePath() + "]");
        String projectName = servedProjectProperties.getProjectName();
        String branchName = QVCSConstants.QVCS_TRUNK_BRANCH;
        String appendedPath = ServerUtility.deduceAppendedPath(directory, servedProjectProperties);

        File[] fileList = directory.listFiles();

        if (fileList != null) {
            try {
                // Create the archiveDirManager for this directory...
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
                ArchiveDirManager archiveDirManager = (ArchiveDirManager) ArchiveDirManagerFactoryForServer.getInstance()
                        .getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, directoryCoordinate,
                        QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject);
                if (rootDirectoryId == -1) {
                    rootDirectoryId = archiveDirManager.getDirectoryID();
                }
                populateDatabaseFromArchiveDirManager(archiveDirManager, branchId, rootDirectoryId);
            } catch (QVCSException | SQLException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                throw e;
            }
            for (File fileList1 : fileList) {
                if (fileList1.isDirectory()) {
                    // Recurse through the directory tree...
                    initializeDirectoryContentsObjectForDirectory(fileList1, servedProjectProperties, branchId, rootDirectoryId, bogusResponseObject);
                }
            }
        }
    }

    /**
     * Create a Directory row, and the File rows for the given archive directory manager. This method should only be called during
     * initialization when the server is run for the first time, or if the server has been 'reset'.
     *
     * @param archiveDirManager the archive directory manager that we're looking at.
     * @param branchId the branchId for the current project's Trunk branch.
     * @param rootDirectoryId the root directory id for this directory tree.
     * @throws SQLException if there is some problem with the database.
     */
    private void populateDatabaseFromArchiveDirManager(ArchiveDirManager archiveDirManager, int branchId, int rootDirectoryId) throws SQLException {
        // Wrap this in a transaction.
        getDatabaseManager().getConnection().setAutoCommit(false);
        DirectoryDAO directoryDAO = new DirectoryDAOImpl(getDatabaseManager().getSchemaName());
        FileDAO fileDAO = new FileDAOImpl(getDatabaseManager().getSchemaName());

        try {
            // Create the Directory row.
            Directory directory = new Directory();
            directory.setDirectoryId(archiveDirManager.getDirectoryID());
            directory.setRootDirectoryId(rootDirectoryId);
            if (archiveDirManager.getParent() != null) {
                directory.setParentDirectoryId(archiveDirManager.getParent().getDirectoryID());
            }
            directory.setBranchId(branchId);
            directory.setAppendedPath(archiveDirManager.getAppendedPath());
            directory.setDeletedFlag(false);
            directoryDAO.insert(directory);
            LOGGER.info("Project: [" + archiveDirManager.getProjectName() + "] -- Created directory record for: [" + directory.getAppendedPath() + "]");

            // Insert all the files in this directory.
            Collection<ArchiveInfoInterface> archiveInfoCollection = archiveDirManager.getArchiveInfoCollection().values();
            for (ArchiveInfoInterface archiveInfo : archiveInfoCollection) {
                com.qumasoft.server.datamodel.File file = new com.qumasoft.server.datamodel.File();
                file.setFileId(archiveInfo.getFileID());
                file.setBranchId(branchId);
                file.setDeletedFlag(false);
                file.setDirectoryId(archiveDirManager.getDirectoryID());
                file.setFileName(archiveInfo.getShortWorkfileName());
                fileDAO.insert(file);
                LOGGER.info("Created file record for: [" + file.getFileName() + "]");
            }
            getDatabaseManager().getConnection().commit();
        } finally {
            getDatabaseManager().getConnection().setAutoCommit(true);
        }
    }

    /**
     * Wipe out the derby database. This is only called via the reset path, i.e. when we are starting from scratch.
     */
    private void wipeDatabase() {
        File derbyDirectory = new File(getDerbyHomeDirectory());
        File[] files = derbyDirectory.listFiles();
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
     * This is the class that runs at server exit time.
     */
    static class ShutdownThread extends Thread {

        @Override
        public void run() {
            try {
                getDatabaseManager().shutdownDatabase();
                ActivityJournalManager.getInstance().addJournalEntry("QVCS-Enterprise Server: shutdown thread called to shutdown.");
                ArchiveDigestManager.getInstance().writeStore();
                DirectoryIDManager.getInstance().writeStore();
                FileIDManager.getInstance().writeStore();
                DirectoryIDDictionary.getInstance().writeStore();
                FileIDDictionary.getInstance().writeStore();
                ActivityJournalManager.getInstance().closeJournal();
            } catch (Exception e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                LOGGER.info("QVCS Enterprise Server exit complete.");
            }
        }
    }

    class NonSecureServer implements Runnable {

        private final int localPort;
        private ServerSocket nonSecureServerSocket = null;

        NonSecureServer(int port) {
            this.localPort = port;
        }

        void closeServerSocket() {
            if (nonSecureServerSocket != null) {
                try {
                    nonSecureServerSocket.close();
                } catch (IOException e) {
                    LOGGER.trace("QVCS Enterprise IOException when closing server socket: [{}]", e.getLocalizedMessage());
                } finally {
                    nonSecureServerSocket = null;
                }
            }
        }

        @Override
        public void run() {
            try {
                nonSecureServerSocket = new ServerSocket(localPort);
                LOGGER.info("Non secure server is listening on port: [" + localPort + "]");
                while (!ServerResponseFactory.getShutdownInProgress()) {
                    Socket socket = nonSecureServerSocket.accept();
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);

                    LOGGER.info("QVCSEnterpriseServer: got non-secure connect");
                    LOGGER.info("local  socket port: [" + socket.getLocalPort() + "]");
                    LOGGER.info("remote socket port: [" + socket.getPort() + "]");

                    LOGGER.info("Launching worker thread for non-secure connection");
                    ServerWorker ws = new ServerWorker(socket);
                    threadPool.execute(ws);
                }
            } catch (RejectedExecutionException | java.io.IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                closeServerSocket();
                LOGGER.info("QVCSEnterpriseServer: closing listener thread for port: [" + localPort + "]");
            }
        }
    }


    static class QVCSWebServer implements Runnable {

        private final String[] webServerArguments;

        QVCSWebServer(String[] args) {
            // <editor-fold>
            if (args != null && args.length > 0) {
                webServerArguments = new String[2];
                webServerArguments[0] = args[0];
                if (args.length > 3) {
                    webServerArguments[1] = args[3];
                } else {
                    webServerArguments[1] = WEB_SERVER_PORT;
                }
            } else {
                webServerArguments = new String[2];
                webServerArguments[0] = System.getProperty(USER_DIR);
                webServerArguments[1] = WEB_SERVER_PORT;
            }
            // </editor-fold>
        }

        @Override
        public void run() {
            try {
                WebServer.start(webServerArguments);
            } catch (IOException e) {
                LOGGER.info("Web server exiting due to exception: " + e.toString());
            }
        }
    }
}
