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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerFactory;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.LogFile;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSServedProjectNamesFilter;
import com.qumasoft.qvcslib.ServedProjectProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.IOException;

import java.net.ServerSocket;
import java.nio.charset.Charset;
import java.util.Set;
import javax.net.ssl.SSLServerSocket;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

import java.net.Socket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.qumasoft.qvcslib.ArchiveDigestManager;
import com.qumasoft.qvcslib.ArchiveDirManager;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.ServerResponseFactoryBaseClass;
import com.qumasoft.qvcslib.SerializableObjectInterface;
import com.qumasoft.qvcslib.ServerResponseLogin;
import com.qumasoft.qvcslib.ServerResponseMessage;
import com.qumasoft.qvcslib.VanillaServerResponseFactory;

import com.qumasoft.webserver.WebServer;

/**
 *
 * @author  $Author$
 */
public class QVCSEnterpriseServer 
{
    // Capture the version of the server.
    private static final String m_Version = "$Label$";

    static final int DEFAULT_VANILLA_LISTEN_PORT    = 9887;
    static final int DEFAULT_NON_SECURE_LISTEN_PORT = 9888;
    static final int DEFAULT_SECURE_LISTEN_PORT     = 9889;
    static final int DEFAULT_ADMIN_LISTEN_PORT      = 9890;
    static final String WEB_SERVER_PORT             = "9080";
	
	// Change on branch.

    static final int m_workerThreads = 50;

    private int m_VanillaPort   = DEFAULT_VANILLA_LISTEN_PORT;
    private int m_NonSecurePort = DEFAULT_NON_SECURE_LISTEN_PORT;
    private int m_SecurePort    = DEFAULT_SECURE_LISTEN_PORT;
    private int m_AdminPort     = DEFAULT_ADMIN_LISTEN_PORT;
    
    private String m_QVCSHomeDirectory = null;
    private String m_args[];

    /* Where worker threads stand idle */
    final Vector<Runnable> m_Threads = new Vector<Runnable>();
    final Vector<Runnable> m_VanillaThreads = new Vector<Runnable>();
    int m_ThreadCount = 1;

    VanillaServer m_VanillaServer = null;
    NonSecureServer m_nonSecureServer = null;
    SecureServer m_secureServer = null;
    SecureServer m_adminServer = null;
    QVCSWebServer m_webServer = null;

    // Server socket listener threads.
    Thread m_VanillaThread   = null;
    Thread m_NonSecureThread = null;
    Thread m_SecureThread    = null;
    Thread m_AdminThread     = null;

    // Web server thread.
    Thread m_WebServerThread = null;
    
    static private QVCSEnterpriseServer m_Server;
    
    // Create our logger object
    private static Logger m_logger = Logger.getLogger("com.qumasoft.server");
    
    static final List<ServerResponseFactoryInterface>  connectedUsersCollection = Collections.synchronizedList(new ArrayList<ServerResponseFactoryInterface>());
    
    public static void main (String args[]) 
    {
        QVCSEnterpriseServer server = new QVCSEnterpriseServer(args);
        server.startServer();
    }
    
    public static Collection getConnectedUsers()
    {
        Vector<ServerResponseFactoryInterface> collection = null;
        synchronized (connectedUsersCollection)
        {
            collection = new Vector<ServerResponseFactoryInterface>();
            Iterator<ServerResponseFactoryInterface> it = connectedUsersCollection.iterator();
            while (it.hasNext())
            {
                collection.add(it.next());
            }
        }
        return collection;
    }
    
    private Collection<ServerResponseFactoryInterface> getConnectedUsersCollection()
    {
        return connectedUsersCollection;
    }
    
    public static void setShutdownInProgress(boolean flag)
    {
        if (flag)
        {
            m_logger.log(Level.INFO, "QVCS Enterprise Server is exiting.");

            if (m_Server.m_VanillaThread != null)
            {
                m_Server.m_VanillaServer.closeServerSocket();
            }
            if (m_Server.m_NonSecureThread != null)
            {
                m_Server.m_nonSecureServer.closeServerSocket();
            }
            if (m_Server.m_SecureThread != null)
            {
                m_Server.m_secureServer.closeServerSocket();
            }
            if (m_Server.m_AdminThread != null)
            {
                m_Server.m_adminServer.closeServerSocket();
            }
        }
    }

    /** Creates a new instance of QVCSEnterpriseServer
     * @param args command line arguments.
     */
    public QVCSEnterpriseServer(String[] args) 
    {
        m_args = args;
        m_Server = this;
        if (m_args.length > 0)
        {
            System.setProperty("user.dir", m_args[0]);
            m_QVCSHomeDirectory = m_args[0];
        }
    }
    
    public void startServer()
    {
        
        try
        {
            if (m_args.length > 1)
            {
                m_VanillaPort = Integer.parseInt(m_args[1]);
            }
        }
        catch (NumberFormatException e)
        {
            m_VanillaPort = DEFAULT_VANILLA_LISTEN_PORT;
        }
        
        try
        {
            if (m_args.length > 2)
            {
                m_NonSecurePort = Integer.parseInt(m_args[2]);
            }
        }
        catch (NumberFormatException e)
        {
            m_NonSecurePort = DEFAULT_NON_SECURE_LISTEN_PORT;
        }
        
        try
        {
            if (m_args.length > 3)
            {
                m_SecurePort = Integer.parseInt(m_args[3]);
            }
        }
        catch (NumberFormatException e)
        {
            m_SecurePort = DEFAULT_SECURE_LISTEN_PORT;
        }
        
        try
        {
            if (m_args.length > 4)
            {
                m_AdminPort = Integer.parseInt(m_args[4]);
            }
        }
        catch (NumberFormatException e)
        {
            m_AdminPort = DEFAULT_ADMIN_LISTEN_PORT;
        }
        
        // Init the logging properties.
        initLoggingProperties();

        // Report the System info.
        reportSystemInfo();

        m_logger.log(Level.INFO, "QVCS Enterprise Server Version: '" + m_Version + "'.");
        m_logger.log(Level.INFO, "QVCS Enterprise Server running with " + Runtime.getRuntime().availableProcessors() + " available processors.");
        
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
        if (FileIDManager.getInstance().getFileIDResetRequiredFlag())
        {
            // Remove the label manager store.
            DirectoryContentsLabelManager.getInstance().resetStore();
            DirectoryContentsLabelManager.getInstance().initialize();

            // Reset the directory id dictionary store.
            DirectoryIDDictionary.getInstance().resetStore();
            DirectoryIDDictionary.getInstance().initialize();

            // Reset the directoryID store.
            DirectoryIDManager.getInstance().resetStore();
            DirectoryIDManager.getInstance().initialize();

            // Reset the file id dictionary store.
            FileIDDictionary.getInstance().resetStore();
            FileIDDictionary.getInstance().initialize();

            // Remove the view manager store.
            ViewManager.getInstance().resetStore();
            ViewManager.getInstance().initialize();

            // We need to reset all file IDs.
            resetFileIDs();
            FileIDManager.getInstance().setFileIDResetRequiredFlag(false);

            // Reset the directory IDs. Note that directory ID's are
            // per project, so there are separate maximum values for each separate
            // project.
            resetDirectoryIDs();

            // Initialize our DirectoryContents objects.
            initializeDirectoryContentsObjects();
        }
        else
        {
            // Initialize the DirectoryIDDictionary.
            DirectoryIDDictionary.getInstance().initialize();

            // Initialize the FileIDDictionary.
            FileIDDictionary.getInstance().initialize();

            // Initialize the directoryID manager.
            DirectoryIDManager.getInstance().initialize();

            // Initialize the LabelHistory manager.
            DirectoryContentsLabelManager.getInstance().initialize();

            // Initialize the ViewManager.
            ViewManager.getInstance().initialize();
        }

        // Register our shutdown thread.
        Runtime.getRuntime().addShutdownHook(new QVCSEnterpriseServer.ShutdownThread());
        
        // Initialize the Activity Journal Manager.
        ActivityJournalManager.getInstance().initialize();
        ActivityJournalManager.getInstance().addJournalEntry("QVCS-Enterprise Server is starting.  Server Version: " + m_Version + ".");
        
        // Launch four separate listener threads; one for vanilla connections,
        // one for non-secure requests, 
        // one for secure requests, and a fourth for admin messages.
        m_VanillaServer   = new VanillaServer(m_VanillaPort);
        m_nonSecureServer = new NonSecureServer(m_NonSecurePort);
        m_secureServer    = new SecureServer(m_SecurePort);
        m_adminServer     = new SecureServer(m_AdminPort);
        m_webServer       = new QVCSWebServer(m_args);
        
        m_VanillaThread   = new Thread(m_VanillaServer, "vanilla server");
        m_NonSecureThread = new Thread(m_nonSecureServer, "non secure server");
        m_SecureThread    = new Thread(m_secureServer, "secure server");
        m_AdminThread     = new Thread(m_adminServer, "admin server");
        m_WebServerThread = new Thread(m_webServer, "web server");
        
        m_VanillaThread.start();
        m_NonSecureThread.start();
        m_SecureThread.start();
        m_AdminThread.start();
        m_WebServerThread.start();
        
        try
        {
            // Wait for all the worker threads to exit.  When a worker thread
            // exits, it calls notify on the m_Threads object.
            synchronized (m_Threads)
            {
                while (m_Threads.size() > 0)
                {
                    m_Threads.wait();
                }
            }

            synchronized (m_VanillaThreads)
            {
                while (m_VanillaThreads.size() > 0)
                {
                    m_VanillaThreads.wait();
                }
            }

            m_VanillaThread.join();
            m_NonSecureThread.join();
            m_SecureThread.join();
            m_AdminThread.join();
            
            // Kill the web server.
            m_WebServerThread.interrupt();
        }
        catch (Exception e)
        {
            m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        finally
        {
            ActivityJournalManager.getInstance().addJournalEntry("QVCS-Enterprise Server is shutting down.");
            ArchiveDigestManager.getInstance().writeStore();
            DirectoryIDManager.getInstance().writeStore();
            FileIDManager.getInstance().writeStore();
            DirectoryContentsLabelManager.getInstance().writeStore();
            DirectoryIDDictionary.getInstance().writeStore();
            FileIDDictionary.getInstance().writeStore();
            ActivityJournalManager.getInstance().closeJournal();
            m_logger.log(Level.INFO, "QVCS Enterprise Server exit complete.");
            System.out.println("QVCS Enterprise Server exit complete.");
            System.exit(0);
        }
    }
    
    public static void stopServer(String args[])
    {
        m_logger.log(Level.INFO, "QVCS Enterprise Server Windows Service beginning shutdown.");
        ServerResponseFactoryBaseClass.setShutdownInProgress(true);
    }
    
    private void initLoggingProperties()
    {
        try
        {
            String logConfigFile = m_QVCSHomeDirectory + File.separator + "serverLogging.properties";
            System.setProperty("java.util.logging.config.file", logConfigFile);
            LogManager.getLogManager().readConfiguration();
        }
        catch (Exception e)
        {
            m_logger.log(Level.SEVERE, "Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
            System.out.println("Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
        }
    }
    
    private void reportSystemInfo()
    {
        java.util.Properties systemProperties = System.getProperties();
        java.util.Set keys = systemProperties.keySet();
        java.util.Iterator it = keys.iterator();
        m_logger.log(Level.FINE, "System properties:");
        while(it.hasNext())
        {
            String key = (String)it.next();
            String message = key + " = " + System.getProperty(key);
            m_logger.log(Level.FINE, message);
        }
        
        // Log what charset is the platform default
        m_logger.log(Level.FINE, "Default charset: " + Charset.defaultCharset().displayName());
    }

    /** This method is called <i>before</i> the server opens any ports to listen
     * for client connections... The goal is to reset the file ids for all files in all
     * existing projects. We do <i>not</i> use the ArchiveDirManager class here
     * as that is heavier-weight than what we want/need to do here.
     */
    private void resetFileIDs()
    {
        m_logger.log(Level.INFO, "QVCSEnterpriseServer: reseting all file id's for all projects.");

        try
        {
            // Get a list of all the projects that this server serves...
            ServedProjectProperties[] projectPropertiesList = getServedProjectPropertiesList();
            
            // Iterate over the list of projects...
            for (int i = 0; i < projectPropertiesList.length; i++)
            {
                String archiveLocation = projectPropertiesList[i].getArchiveLocation();
                File projectBaseDirectory = new File(archiveLocation);
                resetFileIDsForProjectDirectoryTree(projectBaseDirectory);
            }
        }
        catch (Exception e)
        {
            m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }

    /** For use before the server accepts clients connections only!!
     */
    private ServedProjectProperties[] getServedProjectPropertiesList()
    {
        ServedProjectProperties[] servedProjectsProperties = new ServedProjectProperties[0];

        // Where all the property files can be found...
        File propertiesDirectory = new File(System.getProperty("user.dir") + 
                                            System.getProperty("file.separator") + 
                                            QVCSConstants.QVCS_PROPERTIES_DIRECTORY);

        QVCSServedProjectNamesFilter servedProjectNamesFilter = new QVCSServedProjectNamesFilter();
        File[] servedProjectFiles = propertiesDirectory.listFiles(servedProjectNamesFilter);
        if (servedProjectFiles != null)
        {
            servedProjectsProperties = new ServedProjectProperties[servedProjectFiles.length];

            for (int i = 0; i < servedProjectFiles.length; i++)
            {
                String projectName = servedProjectNamesFilter.getProjectName(servedProjectFiles[i].getName());
                try
                {
                    ServedProjectProperties projectProperties = new ServedProjectProperties(projectName);
                    servedProjectsProperties[i] = new ServedProjectProperties(projectName);
                }
                catch (Exception e)
                {
                    m_logger.log(Level.WARNING, "Error finding served project names for project: '" + projectName + "'.");
                }
            }
        }
        return servedProjectsProperties;
    }

    /** 
     * For use by the resetFileIDs() method only!!  Reset fileIDs for the given directory tree.
     */
    private void resetFileIDsForProjectDirectoryTree(File directory)
    {
        m_logger.log(Level.INFO, "Reseting file id's for directory: " + directory.getAbsolutePath());

        File[] fileList = directory.listFiles();

        if (fileList != null)
        {
            for (int i = 0; i < fileList.length; i++)
            {
                if (fileList[i].getName().compareToIgnoreCase(QVCSConstants.QVCS_CACHE_NAME) == 0)
                {
                    // Get rid of the cache file, since we may be changing things here that
                    // would make the cache out of date.
                    if (fileList[i].delete())
                    {
                        m_logger.log(Level.INFO, "Deleting " + QVCSConstants.QVCS_CACHE_NAME + " file from directory: " + directory.getAbsolutePath());
                    }
                    continue;
                }
                if (fileList[i].getName().compareToIgnoreCase(QVCSConstants.QVCS_JOURNAL_NAME) == 0)
                {
                    continue;
                }
                if (fileList[i].isDirectory())
                {
                    // Recurse through the directory tree...
                    resetFileIDsForProjectDirectoryTree(fileList[i]);
                    continue;
                }
                if (fileList[i].getName().compareToIgnoreCase(QVCSConstants.QVCS_DIRECTORYID_FILENAME) == 0)
                {
                    continue;
                }
                if (fileList[i].getName().endsWith(QVCSConstants.QVCS_ARCHIVE_TEMPFILE_SUFFIX))
                {
                    continue;
                }
                if (fileList[i].getName().endsWith(QVCSConstants.QVCS_ARCHIVE_OLDFILE_SUFFIX))
                {
                    continue;
                }
                LogFile logfile = new LogFile(fileList[i].getPath());
                if (logfile.readInformation())
                {
                    // Update the file's file ID. This will cause a re-write of the archive file with an updated
                    // header (supplemental info) that contains the assigned file id.
                    logfile.setFileID(FileIDManager.getInstance().getNewFileID());
                }
                else
                {
                    m_logger.log(Level.WARNING, "Failed to read logfile information for: " + fileList[i].getPath());
                }
            }
        }
    }
    
    /**
     * This method is called <i>before</i> the server opens any ports to listen
     * for client connections... The goal is to reset the directory ids for all existing projects.
     */
    private void resetDirectoryIDs()
    {
        m_logger.log(Level.INFO, "QVCSEnterpriseServer: resetting all directory ids.");

        try
        {
            // Get a list of all the projects that this server serves...
            ServedProjectProperties[] projectPropertiesList = getServedProjectPropertiesList();
            
            // Iterate over the list of projects...
            for (int i = 0; i < projectPropertiesList.length; i++)
            {
                String archiveLocation = projectPropertiesList[i].getArchiveLocation();
                File projectBaseDirectory = new File(archiveLocation);
                resetDirectoryIDsForDirectoryTree(projectBaseDirectory);
                DirectoryIDManager.getInstance().setMaximumDirectoryID(projectPropertiesList[i].getProjectName(), 0);
            }
        }
        catch (Exception e)
        {
            m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }

    /** 
     * For use by the resetDirectoryIDs() method only!!  Reset the given directory ids
     * for the given directory tree.
     */
    private void resetDirectoryIDsForDirectoryTree(File directory)
    {
        m_logger.log(Level.INFO, "Scanning directory: " + directory.getAbsolutePath());

        File[] fileList = directory.listFiles();

        if (fileList != null)
        {
            for (int i = 0; i < fileList.length; i++)
            {
                if (fileList[i].isDirectory())
                {
                    // Recurse through the directory tree...
                    resetDirectoryIDsForDirectoryTree(fileList[i]);
                    continue;
                }
                if (fileList[i].getName().compareToIgnoreCase(QVCSConstants.QVCS_DIRECTORYID_FILENAME) == 0)
                {
                    try
                    {
                        // Delete the directory ID file... It will get re-created when we create the directory
                        // contents object for this directory...
                        fileList[i].delete();
                    }
                    catch (Exception e)
                    {
                        m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
                    }
                    continue;
                }
            }
        }
    }

    /**
     * Used once only when upgrading, or running the server for the very first
     * time. This method should only be run before the  server accepts requests
     * from client.
     */
    private void initializeDirectoryContentsObjects()
    {
        m_logger.log(Level.INFO, "QVCSEnterpriseServer: Initializing DirectoryContents objects...");

        // Delete any/all existing directory contents objects... we're starting 
        // from scratch here.
        deleteExistingDirectoryContentsObjects();

        try
        {
            // Get a list of all the projects that this server serves...
            ServedProjectProperties[] projectPropertiesList = getServedProjectPropertiesList();
            
            // Iterate over the list of projects...
            for (int i = 0; i < projectPropertiesList.length; i++)
            {
                // Wrap this work in a server transaction so the DirectoryContents
                // stuff will behave in a useful way...
                ServerResponseFactoryInterface bogusResponseObject = new BogusResponseObject();
                
                // Keep track that we're in a transaction.
                ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);

                String archiveLocation = projectPropertiesList[i].getArchiveLocation();
                File projectBaseDirectory = new File(archiveLocation);

                // And initialize the directory contents objects for this project tree.
                initializeDirectoryContentsObjectForDirectory(projectBaseDirectory, projectPropertiesList[i], bogusResponseObject);
                
                // Keep track that we ended this transaction.
                ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
            }
            
            // Throw away any archive dir managers we built since we now need
            // to built them again so they will discard (i.e. move) any obsolete
            // files.
            ArchiveDirManagerFactory.getInstance().resetDirectoryMap();
        }
        catch (Exception e)
        {
            m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }

    /**
     * Used only during an upgrade or when product is run for the 1st time.
     */
    private void deleteExistingDirectoryContentsObjects()
    {
        try
        {
            // Get a list of all the projects that this server serves...
            ServedProjectProperties[] projectPropertiesList = getServedProjectPropertiesList();
            
            // Iterate over the list of projects...
            for (int i = 0; i < projectPropertiesList.length; i++)
            {
                String projectName = projectPropertiesList[i].getProjectName();
                
                String fullArchiveDirectory = System.getProperties().getProperty("user.dir") + 
                        File.separator + 
                        QVCSConstants.QVCS_PROJECTS_DIRECTORY + 
                        File.separator +
                        projectName +
                        File.separator +
                        QVCSConstants.QVCS_DIRECTORY_METADATA_DIRECTORY;
                File directoryFile = new File(fullArchiveDirectory);
                if (!directoryFile.exists())
                {
                    if (!directoryFile.mkdirs())
                    {
                        continue;
                    }
                }
                
                // Delete all the files in the QVCS_DIRECTORY_METADATA_DIRECTORY
                File[] fileList = directoryFile.listFiles();

                if (fileList != null)
                {
                    for (int j = 0; j < fileList.length; j++)
                    {
                        if (fileList[j].isDirectory())
                        {
                            continue;
                        }
                        fileList[j].delete();
                    }
                }
            }
        }
        catch (Exception e)
        {
            m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }

    /**
     * Used only during an upgrade or when product is run for the 1st time.
     */
    private void initializeDirectoryContentsObjectForDirectory(File directory, ServedProjectProperties servedProjectProperties, ServerResponseFactoryInterface bogusResponseObject)
    {
        m_logger.log(Level.INFO, "Creating directory contents for: " + directory.getAbsolutePath());
        String projectName = servedProjectProperties.getProjectName();
        String viewName = QVCSConstants.QVCS_TRUNK_VIEW;
        String appendedPath = deduceAppendedPath(directory, servedProjectProperties);

        File[] fileList = directory.listFiles();

        if (fileList != null)
        {
            try
            {
                // Create the archiveDirManager for this directory...
                ArchiveDirManager archiveDirManager = (ArchiveDirManager)ArchiveDirManagerFactory.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME, projectName, viewName, appendedPath, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, bogusResponseObject, false);
                DirectoryContentsManager directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(projectName);
                directoryContentsManager.createDirectoryContentsFromArchiveDirManager(archiveDirManager, bogusResponseObject);
            } 
            catch (QVCSException e)
            {
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
            catch (IOException e)
            {
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }

            // Build all child directories only after creating the DirectoryContents 
            // for this directory.
            for (int i = 0; i < fileList.length; i++)
            {
                if (fileList[i].isDirectory())
                {
                    // Recurse through the directory tree...
                    initializeDirectoryContentsObjectForDirectory(fileList[i], servedProjectProperties, bogusResponseObject);
                }
            }
        }
    }

    private String deduceAppendedPath(File directory, ServedProjectProperties servedProjectProperties)
    {
        String appendedPath = null;
        String projectBaseDirectory = servedProjectProperties.getArchiveLocation();
        
        String directoryPath = null;
        String standardDirectoryPath = null;
        try
        {
            directoryPath = directory.getCanonicalPath();
            standardDirectoryPath = Utility.getInstance().convertToStandardPath(directoryPath);
            if (projectBaseDirectory.length() == standardDirectoryPath.length())
            {
                appendedPath = "";
            }
            else
            {
                appendedPath = standardDirectoryPath.substring(1 + projectBaseDirectory.length());
            }
        } 
        catch (IOException e)
        {
            m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        return appendedPath;
    }
    
    /** This is the class that runs at server exit time. */
    static class ShutdownThread extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                ActivityJournalManager.getInstance().addJournalEntry("QVCS-Enterprise Server: shutdown thread called to shutdown.");
                ArchiveDigestManager.getInstance().writeStore();
                DirectoryIDManager.getInstance().writeStore();
                FileIDManager.getInstance().writeStore();
                DirectoryContentsLabelManager.getInstance().writeStore();
                DirectoryIDDictionary.getInstance().writeStore();
                FileIDDictionary.getInstance().writeStore();
                ActivityJournalManager.getInstance().closeJournal();
                m_logger.log(Level.INFO, "QVCS Enterprise Server exit complete.");
                System.out.println("QVCS Enterprise Server exit complete.");
            }
            catch (Exception e)
            {
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        }
    }
    
    class NonSecureServer implements Runnable
    {
        int localPort = 0;
        ServerSocket m_ServerSocket = null;
        
        NonSecureServer(int port)
        {
            localPort = port;
        }
        
        void closeServerSocket()
        {
            if (m_ServerSocket != null)
            {
                try
                {
                    m_ServerSocket.close();
                }
                catch (Exception e)
                {
                }
            }
        }
        
        public void run()
        {
            try
            {
                m_ServerSocket = new ServerSocket(localPort);
                m_logger.log(Level.INFO, "Non secure server is listening on port: " + localPort);
                while (true &&!ServerResponseFactoryBaseClass.getShutdownInProgress()) 
                {
                    Socket socket = m_ServerSocket.accept();
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);

                    m_logger.log(Level.INFO, "QVCSEnterpriseServer: got non-secure connect");
                    m_logger.log(Level.INFO, "local  socket port: " + socket.getLocalPort());
                    m_logger.log(Level.INFO, "remote socket port: " + socket.getPort());

                    Worker w = null;
                    synchronized (m_Threads)
                    {
                        if (m_Threads.isEmpty()) 
                        {
                            m_logger.log(Level.INFO, "creating new worker thread for non-secure connection");
                            Worker ws = new Worker();
                            ws.setSocket(socket);
                            (new Thread(ws, "worker thread " + m_ThreadCount++)).start();
                        } 
                        else 
                        {
                            m_logger.log(Level.INFO, "re-using worker thread for non-secure connection");
                            w = (Worker) m_Threads.elementAt(0);
                            m_Threads.removeElementAt(0);
                            w.setSocket(socket);
                        }
                    }
                }
            }
            catch (java.net.SocketException e)
            {
                m_logger.log(Level.INFO, "Server non-secure accept thread is exiting.");
            }
            catch (java.io.IOException e)
            {
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
            catch (Exception e)
            {
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
            finally
            {
                if (m_ServerSocket != null)
                {
                    try
                    {
                        m_ServerSocket.close();
                    }
                    catch (java.io.IOException e)
                    {
                    }
                }
                m_logger.log(Level.INFO, "QVCSEnterpriseServer: closing listener thread for port: " + localPort);
            }
        }
    }

    // This server is for connections from clients where the data is in raw
    // data format, instead of being handled as Java serialized objects.
    // The original motivation for this class was to add support for connections
    // from Microsoft SCC compliant clients.
    class VanillaServer implements Runnable
    {
        int localPort = 0;
        ServerSocket m_ServerSocket = null;
        
        VanillaServer(int port)
        {
            localPort = port;
        }
        
        void closeServerSocket()
        {
            if (m_ServerSocket != null)
            {
                try
                {
                    m_ServerSocket.close();
                }
                catch (Exception e)
                {
                }
            }
        }
        
        public void run()
        {
            try
            {
                m_ServerSocket = new ServerSocket(localPort);
                m_logger.log(Level.INFO, "Server is listening on port: " + localPort);
                while (true &&!ServerResponseFactoryBaseClass.getShutdownInProgress()) 
                {
                    Socket socket = m_ServerSocket.accept();
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);

                    m_logger.log(Level.INFO, "QVCSEnterpriseServer: got connect");
                    m_logger.log(Level.INFO, "local  socket port: " + socket.getLocalPort());
                    m_logger.log(Level.INFO, "remote socket port: " + socket.getPort());
                    m_logger.log(Level.INFO, "socket timeout: " + socket.getSoTimeout());
                    m_logger.log(Level.INFO, "receive buffer size: " + socket.getReceiveBufferSize());
                    m_logger.log(Level.INFO, "send buffer size: " + socket.getSendBufferSize());

                    VanillaWorker w = null;
                    synchronized (m_VanillaThreads)
                    {
                        if (m_VanillaThreads.isEmpty()) 
                        {
                            m_logger.log(Level.INFO, "creating new worker thread for connection");
                            VanillaWorker ws = new VanillaWorker();
                            ws.setSocket(socket);
                            (new Thread(ws, "vanilla worker thread " + m_ThreadCount++)).start();
                        } 
                        else 
                        {
                            m_logger.log(Level.INFO, "re-using worker thread for connection");
                            w = (VanillaWorker) m_VanillaThreads.elementAt(0);
                            m_VanillaThreads.removeElementAt(0);
                            w.setSocket(socket);
                        }
                    }
                }
            }
            catch (java.net.SocketException e)
            {
                m_logger.log(Level.INFO, "Server accept thread is exiting.");
            }
            catch (java.io.IOException e)
            {
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
            catch (Exception e)
            {
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
            finally
            {
                if (m_ServerSocket != null)
                {
                    try
                    {
                        m_ServerSocket.close();
                    }
                    catch (java.io.IOException e)
                    {
                    }
                }
                m_logger.log(Level.INFO, "QVCSEnterpriseServer: closing listener thread for port: " + localPort);
            }
        }
    }

    class SecureServer implements Runnable
    {
        int localPort = 0;
        SSLServerSocket m_ServerSocket = null;
        
        SecureServer(int port)
        {
            localPort = port;
        }
        
        void closeServerSocket()
        {
            if (m_ServerSocket != null)
            {
                try
                {
                    m_ServerSocket.close();
                }
                catch (Exception e)
                {
                }
            }
        }
        
        public void run()
        {
            try
            {
                ServerSocketFactory socketFactory = SSLServerSocketFactory.getDefault();
                m_ServerSocket = (SSLServerSocket)socketFactory.createServerSocket(localPort);

                // Select an appropriate cipher suite.
                String [] useSuites = new String[1];
                useSuites[0] = "SSL_DH_anon_WITH_3DES_EDE_CBC_SHA";
                m_ServerSocket.setEnabledCipherSuites(useSuites);

                m_logger.log(Level.INFO, "Secure server is listening on port: " + localPort);
                while (true &&!ServerResponseFactoryBaseClass.getShutdownInProgress()) 
                {
                    Socket socket = m_ServerSocket.accept();
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);

                    m_logger.log(Level.INFO, "QVCSEnterpriseServer: got secure connection");
                    m_logger.log(Level.INFO, "local  socket port: " + socket.getLocalPort());
                    m_logger.log(Level.INFO, "remote socket port: " + socket.getPort());

                    Worker w = null;
                    synchronized (m_Threads) 
                    {
                        if (m_Threads.isEmpty()) 
                        {
                            m_logger.log(Level.INFO, "creating new worker thread for secure connection");
                            Worker ws = new Worker();
                            ws.setSocket(socket);
                            (new Thread(ws, "worker thread " + m_ThreadCount++)).start();
                        } 
                        else 
                        {
                            m_logger.log(Level.INFO, "re-using worker thread for secure connection");
                            w = (Worker) m_Threads.elementAt(0);
                            m_Threads.removeElementAt(0);
                            w.setSocket(socket);
                        }
                    }
                }
            }
            catch (java.net.SocketException e)
            {
                m_logger.log(Level.INFO, "Server secure accept thread is exiting.");
            }
            catch (java.io.IOException e)
            {
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
            catch (Exception e)
            {
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
            finally
            {
                if (m_ServerSocket != null)
                {
                    try
                    {
                        m_ServerSocket.close();
                    }
                    catch (java.io.IOException e)
                    {
                    }
                }
                m_logger.log(Level.INFO, "QVCSEnterpriseServer: closing listener thread for secure port: " + localPort);
            }
        }
    }

    class VanillaWorker implements Runnable 
    {
        /* Socket to client we're handling */
        private Socket m_socket;

        synchronized void setSocket(Socket s) 
        {
            this.m_socket = s;
            notifyAll();
        }
        
        public synchronized void run()
        {
            while(true && !ServerResponseFactoryBaseClass.getShutdownInProgress()) 
            {
                if (m_socket == null) 
                {
                    /* nothing to do */
                    try 
                    {
                        wait();
                    } 
                    catch (InterruptedException e) 
                    {
                        /* should not happen */
                        continue;
                    }
                }
                handleVanillaClientRequests();

                /* go back in wait queue if there's fewer
                 * than numHandler connections.
                 */
                m_socket = null;
                synchronized (m_VanillaThreads) 
                {
                    // If shutdown is not in progress...
                    if (!ServerResponseFactoryBaseClass.getShutdownInProgress())
                    {
                        if (m_VanillaThreads.size() >= m_workerThreads) 
                        {
                            // too many threads, exit this one
                            return;
                        } 
                        else 
                        {
                            m_VanillaThreads.addElement(this);
                        }
                    }
                    else
                    {
                        // Let the server thread know that this thread is done.
                        m_VanillaThreads.notifyAll();
                    }
                }
            }
        }
        
        private void handleVanillaClientRequests()
        {
            String connectedTo = null;

            VanillaClientRequestFactory  requestFactory  = null;
            VanillaServerResponseFactory responseFactory = null;
            try
            {
                requestFactory   = new VanillaClientRequestFactory(m_socket.getInputStream());
                responseFactory  = new VanillaServerResponseFactory(m_socket.getOutputStream(), m_socket.getPort(), m_socket.getInetAddress().getHostAddress());
                connectedTo = m_socket.getInetAddress().getHostAddress();
                m_logger.log(Level.INFO, "Connected to: " + connectedTo);

                while (!ServerResponseFactoryBaseClass.getShutdownInProgress() && responseFactory.getConnectionAliveFlag())
                {
                    try
                    {
                        ClientRequestInterface clientRequest = requestFactory.createClientRequest(responseFactory);
                        if (clientRequest != null)
                        {
                            clientRequest.moveArguments();
                            SerializableObjectInterface returnObject = clientRequest.execute(requestFactory.getUserName(), responseFactory);
                            
                            if (clientRequest instanceof ClientRequestLogin)
                            {
                                ServerResponseLogin serverResponseLogin = (ServerResponseLogin) returnObject;
                                if (serverResponseLogin.getLoginResult())
                                {
                                    requestFactory.setIsUserLoggedIn(true);
                                    requestFactory.setUserName(serverResponseLogin.getUserName());

                                    responseFactory.setIsUserLoggedIn(true);
                                    responseFactory.setUserName(serverResponseLogin.getUserName());
                                    ClientRequestLogin loginRequest = (ClientRequestLogin) clientRequest;
                                    responseFactory.setServerName(loginRequest.getServerName());
                                    
                                    getConnectedUsersCollection().add(responseFactory);
                                }
                            }
                            
                            // Send the response back to the client.
                            responseFactory.createServerResponse(returnObject);
                            
                            // If this was a login request that succeeded, we also
                            // need to send the list of projects for this user.
                            if (clientRequest instanceof ClientRequestLogin)
                            {
                                ClientRequestLogin clientRequestLogin = (ClientRequestLogin)clientRequest;
                                ServerResponseMessage message = null;

                                if (responseFactory.getIsUserLoggedIn() == false)
                                {
                                    // The user failed to login.  Report the problem to the user.
                                    if (clientRequestLogin.getAuthenticationFailedFlag())
                                    {
                                        message = new ServerResponseMessage("Invalid username/password", null, null, null, ServerResponseMessage.HIGH_PRIORITY);
                                        responseFactory.createServerResponse(message);
                                    }
                                }

                                // Report any status information back to the user.
                                if (clientRequestLogin.getMessage() != null)
                                {
                                    message = new ServerResponseMessage(clientRequestLogin.getMessage(), null, null, null, ServerResponseMessage.HIGH_PRIORITY);
                                    responseFactory.createServerResponse(message);
                                }
                            }
                        }
                        else
                        {
                            m_logger.log(Level.INFO, "clientRequest is null!!");
                            m_logger.log(Level.INFO, "Breaking connection to: " + connectedTo);
                            break;
                        }
                    }
                    catch (QVCSShutdownException e)
                    {
                        // We are shutting down this server.
                        m_logger.log(Level.INFO, "Shutting down server at request from: " + connectedTo);
                        break;
                    }
                    catch (RuntimeException e)
                    {
                        m_logger.log(Level.INFO, "Breaking connection to: " + connectedTo);
                        m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
                        break;
                    }
                    catch (Exception e)
                    {
                        m_logger.log(Level.INFO, "Breaking connection to: " + connectedTo);
                        m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                m_logger.log(Level.INFO, "Breaking connection to: " + connectedTo);
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
            finally
            {
                try
                {
                    m_socket.close();
                    
                    // The connection to the client is gone.  Remove the response
                    // factory as a listener for any archive directory managers
                    // so we don't waste time trying to inform a client that we
                    // can no longer talk to.
                    if (responseFactory != null)
                    {
                        Set<ArchiveDirManagerInterface> directoryManagers = responseFactory.getDirectoryManagers();
                        Iterator<ArchiveDirManagerInterface> it = directoryManagers.iterator();
                        while (it.hasNext())
                        {
                            ArchiveDirManagerInterface directoryManagerInterface = it.next();
                            directoryManagerInterface.removeLogFileListener(responseFactory);
                        }

                        getConnectedUsersCollection().remove(responseFactory);
                        
                        // Decrement the number of logged on users with the 
                        // license manager.
                        if (responseFactory.getIsUserLoggedIn())
                        {
                            ServerTransactionManager.getInstance().flushClientTransaction(responseFactory);
                            LicenseManager.getInstance().logoutUser(responseFactory.getUserName(), responseFactory.getClientIPAddress());
                        }
                    }
                }
                catch (Exception e)
                {
                    m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
    }
    
    class Worker implements Runnable 
    {
        /* Socket to client we're handling */
        private Socket m_socket;

        synchronized void setSocket(Socket s) 
        {
            this.m_socket = s;
            notifyAll();
        }
        
        public synchronized void run()
        {
            while(true && !ServerResponseFactoryBaseClass.getShutdownInProgress()) 
            {
                if (m_socket == null) 
                {
                    /* nothing to do */
                    try 
                    {
                        wait();
                    } 
                    catch (InterruptedException e) 
                    {
                        /* should not happen */
                        continue;
                    }
                }
                handleClientRequests();

                /* go back in wait queue if there's fewer
                 * than numHandler connections.
                 */
                m_socket = null;
                synchronized (m_Threads) 
                {
                    // If shutdown is not in progress...
                    if (!ServerResponseFactoryBaseClass.getShutdownInProgress())
                    {
                        if (m_Threads.size() >= m_workerThreads) 
                        {
                            // too many threads, exit this one
                            return;
                        } 
                        else 
                        {
                            m_Threads.addElement(this);
                        }
                    }
                    else
                    {
                        // Let the server thread know that this thread is done.
                        m_Threads.notifyAll();
                    }
                }
            }
        }
        
        private void handleClientRequests()
        {
            String connectedTo = null;

            ServerResponseFactory responseFactory = null;
            ClientRequestFactory  requestFactory  = null;
            try
            {
                requestFactory   = new ClientRequestFactory(m_socket.getInputStream());
                responseFactory  = new ServerResponseFactory(m_socket.getOutputStream(), m_socket.getPort(), m_socket.getInetAddress().getHostAddress());
                connectedTo = m_socket.getInetAddress().getHostAddress();
                m_logger.log(Level.INFO, "Connected to: " + connectedTo);

                while (!ServerResponseFactoryBaseClass.getShutdownInProgress() && responseFactory.getConnectionAliveFlag())
                {
                    try
                    {
                        ClientRequestInterface clientRequest = requestFactory.createClientRequest(responseFactory);
                        if (clientRequest != null)
                        {
                            clientRequest.moveArguments();
                            SerializableObjectInterface returnObject = clientRequest.execute(requestFactory.getUserName(), responseFactory);
                            
                            if (clientRequest instanceof ClientRequestLogin)
                            {
                                ServerResponseLogin serverResponseLogin = (ServerResponseLogin) returnObject;
                                if (serverResponseLogin.getLoginResult())
                                {
                                    requestFactory.setIsUserLoggedIn(true);
                                    requestFactory.setUserName(serverResponseLogin.getUserName());

                                    responseFactory.setIsUserLoggedIn(true);
                                    responseFactory.setUserName(serverResponseLogin.getUserName());
                                    ClientRequestLogin loginRequest = (ClientRequestLogin) clientRequest;
                                    responseFactory.setServerName(loginRequest.getServerName());
                                    
                                    getConnectedUsersCollection().add(responseFactory);
                                }
                            }
                            
                            // Send the response back to the client.
                            responseFactory.createServerResponse(returnObject);
                            
                            // If this was a login request that succeeded, we also
                            // need to send the list of projects for this user.
                            if (clientRequest instanceof ClientRequestLogin)
                            {
                                ClientRequestLogin clientRequestLogin = (ClientRequestLogin)clientRequest;
                                ServerResponseMessage message = null;

                                if (responseFactory.getIsUserLoggedIn() == false)
                                {
                                    // The user failed to login.  Report the problem to the user.
                                    if (clientRequestLogin.getAuthenticationFailedFlag())
                                    {
                                        message = new ServerResponseMessage("Invalid username/password", null, null, null, ServerResponseMessage.HIGH_PRIORITY);
                                        responseFactory.createServerResponse(message);
                                    }
                                }
                                else
                                {
                                    // Report any status information back to the user.
                                    if (clientRequestLogin.getMessage() != null)
                                    {
                                        message = new ServerResponseMessage(clientRequestLogin.getMessage(), null, null, null, ServerResponseMessage.HIGH_PRIORITY);
                                        responseFactory.createServerResponse(message);
                                    }
                                }
                            }
                        }
                        else
                        {
                            m_logger.log(Level.INFO, "clientRequest is null!!");
                            m_logger.log(Level.INFO, "Breaking connection to: " + connectedTo);
                            break;
                        }
                    }
                    catch (QVCSShutdownException e)
                    {
                        // We are shutting down this server.
                        m_logger.log(Level.INFO, "Shutting down server at request from: " + connectedTo);
                        break;
                    }
                    catch (RuntimeException e)
                    {
                        m_logger.log(Level.INFO, "Breaking connection to: " + connectedTo);
                        m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
                        break;
                    }
                    catch (Exception e)
                    {
                        m_logger.log(Level.INFO, "Breaking connection to: " + connectedTo);
                        m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
                        break;
                    }
                }
            }
            catch (Exception e)
            {
                m_logger.log(Level.INFO, "Breaking connection to: " + connectedTo);
                m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
            finally
            {
                try
                {
                    m_socket.close();
                    
                    // The connection to the client is gone.  Remove the response
                    // factory as a listener for any archive directory managers
                    // so we don't waste time trying to inform a client that we
                    // can no longer talk to.
                    if (responseFactory != null)
                    {
                        Set<ArchiveDirManagerInterface> directoryManagers = responseFactory.getDirectoryManagers();
                        Iterator<ArchiveDirManagerInterface> it = directoryManagers.iterator();
                        while (it.hasNext())
                        {
                            ArchiveDirManagerInterface directoryManagerInterface = it.next();
                            directoryManagerInterface.removeLogFileListener(responseFactory);
                        }

                        getConnectedUsersCollection().remove(responseFactory);
                        
                        // Decrement the number of logged on users with the 
                        // license manager.
                        if (responseFactory.getIsUserLoggedIn())
                        {
                            ServerTransactionManager.getInstance().flushClientTransaction(responseFactory);
                            LicenseManager.getInstance().logoutUser(responseFactory.getUserName(), responseFactory.getClientIPAddress());
                        }
                    }
                }
                catch (Exception e)
                {
                    m_logger.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
    }
    
    static class QVCSWebServer implements Runnable
    {
        String[] m_Args;

        QVCSWebServer(String[] args)
        {
            if (args != null && args.length > 0)
            {
                m_Args = new String[2];
                m_Args[0] = args[0];
                if (args.length > 5)
                {
                    m_Args[1] = args[5];
                }
                else
                {
                    m_Args[1] = WEB_SERVER_PORT;
                }
            }
        }
        
        /** When an object implementing interface <code>Runnable</code> is used
         * to create a thread, starting the thread causes the object's
         * <code>run</code> method to be called in that separately executing
         * thread.
         * <p>
         * The general contract of the method <code>run</code> is that it may
         * take any action whatsoever.
         *
         * @see     java.lang.Thread#run()
         *
         */
        public void run()
        {
            try
            {
                WebServer.main(m_Args);
            }
            catch (InterruptedException e)
            {
                m_logger.log(Level.INFO, "Web server exiting.");
            }
            catch (Exception e)
            {
                m_logger.log(Level.INFO, "Web server exiting due to exception: " + e.toString());
            }
        }
    }
}
