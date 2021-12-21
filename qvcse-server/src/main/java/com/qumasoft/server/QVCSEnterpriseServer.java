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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.webserver.WebServer;
import com.qvcsos.server.DatabaseManager;
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
    static final String POSTGRESQL_DB_SERVER = "postgresql";
    static final String WEB_SERVER_PORT = "9080";
    static final int WORKER_THREAD_COUNT = 50;
    private static final int ARGS_LENGTH_WITH_SYNC_OBJECT = 5;
    private static final int ARGS_SYNC_OBJECT_INDEX = 4;
    private int nonSecurePort = DEFAULT_NON_SECURE_LISTEN_PORT;
    private int adminPort = DEFAULT_ADMIN_LISTEN_PORT;
    private static final long THREAD_POOL_AWAIT_TERMINATION_DELAY = 5;
    private final String[] arguments;
    private static boolean serverIsRunningFlag;

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

    /**
     * Main entry point to the QVCS-Enterprise server.
     * @param args command line arguments. The command arguments are: [0] QVCS home directory [1] non-secure listen port (9889) [2] admin listen port (9890)
     * [3] web server port (9080) [4] a sync object used for ant task synchronization.
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

    public static QVCSEnterpriseServer getInstance() {
        return qvcsEnterpriseServer;
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
        AuthenticationManager.getAuthenticationManager().setClientPort(nonSecurePort);

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
            ActivityJournalManager.getInstance().addJournalEntry("QVCS-Enterprise Server is shutting down.");
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
     * This is the class that runs at server exit time.
     */
    static class ShutdownThread extends Thread {

        @Override
        public void run() {
            try {
                ActivityJournalManager.getInstance().addJournalEntry("QVCS-Enterprise Server: shutdown thread called to shutdown.");
                DatabaseManager.getInstance().shutdownDatabase();
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
                int webServerPort = Integer.parseInt(webServerArguments[1]);
                AuthenticationManager.getAuthenticationManager().setWebServerPort(webServerPort);
                WebServer.start(webServerArguments);
            } catch (IOException e) {
                LOGGER.info("Web server exiting due to exception: " + e.toString());
            }
        }
    }
}
