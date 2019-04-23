//   Copyright 2004-2019 Jim Voris
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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Worker implements Runnable, HttpConstants {

    static final int BUF_SIZE = 2048;
    static final byte[] EOL = {(byte) '\r', (byte) '\n'};

    /*
     * buffer to use for requests
     */
    private final byte[] buf;
    /*
     * Socket to client we're handling
     */
    private Socket socket;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

    Worker() {
        buf = new byte[BUF_SIZE];
        socket = null;
    }

    synchronized void setSocket(Socket s) {
        this.socket = s;
        notifyAll();
    }

    @Override
    public synchronized void run() {
        while (true) {
            if (socket == null) {
                /*
                 * nothing to do
                 */
                try {
                    wait();
                } catch (InterruptedException e) {
                    /*
                     * should not happen
                     */
                    continue;
                }
            }
            try {
                handleClient();
            } catch (java.net.SocketTimeoutException e) {
                LOGGER.info("Timeout on read:" + e.getMessage());
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
            /*
             * go back in wait queue if there'm_Socket fewer than numHandler connections.
             */
            socket = null;
            ArrayList<Worker> pool = WebServer.getWorkerThreads();
            synchronized (pool) {
                if (pool.size() >= WebServer.getMaxWorkerThreadCount()) {
                    /*
                     * too many workerThreads, exit this one
                     */
                    return;
                } else {
                    pool.add(this);
                }
            }
        }
    }

    void handleClient() throws IOException {
        InputStream is = new BufferedInputStream(socket.getInputStream());
        PrintStream ps = new PrintStream(socket.getOutputStream());
        /*
         * we will only block in read for this many milliseconds before we fail with java.io.InterruptedIOException, at which point
         * we will abandon the connection.
         */
        socket.setSoTimeout(WebServer.getClientTimeout());
        socket.setTcpNoDelay(true);
        /*
         * zero out the buffer from last time
         */
        for (int i = 0; i < BUF_SIZE; i++) {
            buf[i] = 0;
        }
        try {
            /*
             * We only support HTTP GET/HEAD, and don't support any fancy HTTP options, so we're only interested really in the first
             * line.
             */
            int nread = 0;
            int r;

            outerloop:
            while (nread < BUF_SIZE) {
                r = is.read(buf, nread, BUF_SIZE - nread);
                if (r == -1) {
                    /*
                     * EOF
                     */
                    return;
                }
                int i = nread;
                nread += r;
                for (; i < nread; i++) {
                    if (buf[i] == (byte) '\n' || buf[i] == (byte) '\r') {
                        /*
                         * read one line
                         */
                        break outerloop;
                    }
                }
            }

            /*
             * beginning of file name
             */
            // <editor-fold>
            int index;
            if (buf[0] == (byte) 'G'
                    && buf[1] == (byte) 'E'
                    && buf[2] == (byte) 'T'
                    && buf[3] == (byte) ' ') {
                index = 4;
            } else if (buf[0] == (byte) 'H'
                    && buf[1] == (byte) 'E'
                    && buf[2] == (byte) 'A'
                    && buf[3] == (byte) 'D'
                    && buf[4] == (byte) ' ') {
                index = 5;
                // </editor-fold>
            } else {
                /*
                 * we don't support this method
                 */
                ps.print("HTTP/1.0 " + HTTP_BAD_METHOD
                        + " unsupported method type: ");
                ps.write(buf, 0, 5);
                ps.write(EOL);
                ps.flush();
                socket.close();
                return;
            }

            int i;
            /*
             * find the file name, from: GET /foo/bar.html HTTP/1.0 extract "/foo/bar.html"
             */
            for (i = index; i < nread; i++) {
                if (buf[i] == (byte) ' ') {
                    break;
                }
            }

            String fname = new String(buf, index, i - index);
            if (fname.compareTo("/") == 0) {
                fname = "/index.html";
            }
            String resourceName = "/ServerWebSite" + fname;
            InputStream streamFromJar = this.getClass().getResourceAsStream(resourceName);
            if (printStreamHeaders(streamFromJar, resourceName, ps)) {
                sendStream(streamFromJar, ps);
            } else {
                send404(resourceName, ps);
            }

        }
        finally {
            socket.close();
        }
    }

    boolean printStreamHeaders(InputStream inputStream, String fileName, PrintStream ps) throws IOException {
        boolean ret;
        int rCode;
        if (inputStream == null) {
            rCode = HTTP_NOT_FOUND;
            ps.print("HTTP/1.0 " + HTTP_NOT_FOUND + " not found");
            ps.write(EOL);
            ret = false;
        } else {
            rCode = HTTP_OK;
            ps.print("HTTP/1.0 " + HTTP_OK + " OK");
            ps.write(EOL);
            ret = true;
        }
        WebServer.log("From " + socket.getInetAddress().getHostAddress() + ": GET " + fileName + "-->" + rCode);
        ps.print("Server: Simple QVCS-Enterprise Java Web Server");
        ps.write(EOL);
        ps.print("Date: " + (new Date()));
        ps.write(EOL);
        if (ret && (inputStream != null)) {
            int size = inputStream.available();
            ps.print("Content-length: " + size);
            ps.write(EOL);
            ps.print("Last Modified: " + (new Date()));
            ps.write(EOL);
            String name = fileName;
            int ind = name.lastIndexOf('.');
            String ct = null;
            if (ind > 0) {
                ct = (String) EXTENSION_MAP.get(name.substring(ind).toLowerCase());
            }
            if (ct == null) {
                ct = "unknown/unknown";
            }
            ps.print("Content-type: " + ct);
            ps.write(EOL);
        }
        return ret;
    }

    void send404(String fileName, PrintStream ps) throws IOException {
        ps.write(EOL);
        ps.write(EOL);
        ps.println("Not Found\n\n"
                + "The requested resource'" + fileName + "'was not found.\n");
    }

    void sendStream(InputStream is, PrintStream ps) throws IOException {
        if (is != null) {
            ps.write(EOL);
            try {
                int n;
                while ((n = is.read(buf)) > 0) {
                    ps.write(buf, 0, n);
                }
            }
            finally {
                is.close();
            }
        }
    }

    /*
     * mapping of file extensions to content-types
     */
    private static final java.util.Map EXTENSION_MAP = new java.util.HashMap();

    static {
        fillMap();
    }

    static void setSuffix(String k, String v) {
        EXTENSION_MAP.put(k, v);
    }

    static void fillMap() {
        setSuffix("", "content/unknown");
        setSuffix(".uu", "application/octet-stream");
        setSuffix(".exe", "application/octet-stream");
        setSuffix(".ps", "application/postscript");
        setSuffix(".zip", "application/zip");
        setSuffix(".sh", "application/x-shar");
        setSuffix(".tar", "application/x-tar");
        setSuffix(".snd", "audio/basic");
        setSuffix(".au", "audio/basic");
        setSuffix(".wav", "audio/x-wav");
        setSuffix(".gif", "image/gif");
        setSuffix(".jpg", "image/jpeg");
        setSuffix(".jpeg", "image/jpeg");
        setSuffix(".htm", "text/html");
        setSuffix(".html", "text/html");
        setSuffix(".text", "text/plain");
        setSuffix(".c", "text/plain");
        setSuffix(".cc", "text/plain");
        setSuffix(".c++", "text/plain");
        setSuffix(".cpp", "text/plain");
        setSuffix(".h", "text/plain");
        setSuffix(".pl", "text/plain");
        setSuffix(".txt", "text/plain");
        setSuffix(".java", "text/plain");
    }
}
