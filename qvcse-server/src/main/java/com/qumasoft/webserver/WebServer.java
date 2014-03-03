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
package com.qumasoft.webserver;

import com.qumasoft.qvcslib.QVCSConstants;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An example of a very simple, multi-threaded HTTP server.
 * Implementation notes are in WebServer.html, and also
 * as comments in the source code.
 */
public final class WebServer implements HttpConstants {

    /** Hide this. */
    private WebServer() {
    }

    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.webserver");
    private static final int DEFAULT_WEB_SERVER_PORT = 9080;
    private static final int INITIAL_MAX_WORKER_THREAD_COUNT = 5;
    private static final int ONE_SECOND_IN_MILLISECONDS = 1000;
    private static final int DEFAULT_TIMEOUT_IN_MILLISECONDS = 5000;
    private static final int MAXIMUM_NUMBER_OF_WORKER_THREADS = 25;
    private static PrintStream webServerLog = null;
    /**
     * Where worker workerThreads stand idle
     */
    private static Vector<Worker> workerThreads = new Vector<>();
    /**
     * the web server's virtual webServerRootDirectory
     */
    private static File webServerRootDirectory;
    /**
     * clientTimeout on client connections
     */
    private static int clientTimeout = 0;
    /**
     * max # worker workerThreads
     */
    private static int maxWorkerThreadCount = INITIAL_MAX_WORKER_THREAD_COUNT;

    /*
     * print to stdout
     */
    protected static void printLogMessage(String s) {
        LOGGER.log(Level.INFO, s);
    }

    protected static int getClientTimeout() {
        return clientTimeout;
    }

    protected static Vector<Worker> getWorkerThreads() {
        return workerThreads;
    }

    protected static int getMaxWorkerThreadCount() {
        return maxWorkerThreadCount;
    }

    /*
     * print to the webServerLog file
     */
    static void log(String s) {
        synchronized (webServerLog) {
            webServerLog.println(s);
            webServerLog.flush();
        }
    }

    /*
     * load www-server.properties
     */
    static void loadProperties() throws IOException {
        String rootDirectoryName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_WEB_SERVER_ROOT_DIRECTORY;

        String webServerLogfileName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_WEB_SERVER_LOGFILE;


        /*
         * Use these hard coded defaults
         */
        webServerRootDirectory = new File(rootDirectoryName);
        if (clientTimeout <= ONE_SECOND_IN_MILLISECONDS) {
            clientTimeout = DEFAULT_TIMEOUT_IN_MILLISECONDS;
        }
        if (maxWorkerThreadCount < MAXIMUM_NUMBER_OF_WORKER_THREADS) {
            maxWorkerThreadCount = INITIAL_MAX_WORKER_THREAD_COUNT;
        }

        // Open the file we'll webServerLog to...
        printLogMessage("opening log file: " + webServerLogfileName);
        webServerLog = new PrintStream(new BufferedOutputStream(new FileOutputStream(webServerLogfileName)));
    }

    static void printProperties() {
        printLogMessage("root=" + webServerRootDirectory);
        printLogMessage("timeout=" + clientTimeout);
        printLogMessage("workers=" + maxWorkerThreadCount);
    }

    /**
     * Start the very simple web server.
     * @param args user directory, and port
     * @throws IOException if we can't load properties.
     */
    public static void start(String[] args) throws IOException {
        int port = DEFAULT_WEB_SERVER_PORT;
        if (args.length > 0) {
            System.setProperty("user.dir", args[0]);
        }

        if (args.length > 1) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                port = DEFAULT_WEB_SERVER_PORT;
            }
        }
        loadProperties();
        printProperties();

        /*
         * start worker workerThreads
         */
        for (int i = 0; i < maxWorkerThreadCount; ++i) {
            Worker w = new Worker();
            Thread workerThread = new Thread(w, "worker #" + i);
            workerThread.setDaemon(true);
            workerThread.start();
            workerThreads.addElement(w);
        }

        ServerSocket ss = new ServerSocket(port);
        while (true) {
            Socket s = ss.accept();

            Worker worker;
            synchronized (workerThreads) {
                if (workerThreads.isEmpty()) {
                    Worker ws = new Worker();
                    ws.setSocket(s);
                    Thread workerThread = new Thread(ws, "additional worker");
                    workerThread.setDaemon(true);
                    workerThread.start();
                } else {
                    worker = workerThreads.elementAt(0);
                    workerThreads.removeElementAt(0);
                    worker.setSocket(s);
                }
            }
        }
    }
}
