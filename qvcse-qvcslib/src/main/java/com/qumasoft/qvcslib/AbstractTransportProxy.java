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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract transport proxy. Abstract class that supplies default implementations for most of the behavior required by the transport proxy interface.
 * @author Jim Voris
 */
public abstract class AbstractTransportProxy implements TransportProxyInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransportProxy.class);
    private final ServerProperties serverProperties;
    private final Object readLock;
    private final Map<String, ArchiveDirManagerInterface> listeners = Collections.synchronizedMap(new TreeMap<>());
    private boolean isLoggedInToServerFlag = false;
    private String username = null; // The name the user is logged in as.
    private ObjectOutputStream objectRequestStream = null;
    private ObjectInputStream objectResponseStream = null;
    private final Compressor decompressor;
    private final Compressor compressor;
    private TransportProxyListenerInterface proxyListener = null;
    private boolean isOpenFlag = false;
    private final Object requestStreamSyncObject = new Object();
    private final Object responseStreamSyncObject = new Object();
    private VisualCompareInterface visualCompareInterface = null;
    private HeartbeatThread heartbeatThread = null;
    private Socket socket = null;
    private String proxyKeyValue;

    /**
     * Construct the common parts of a transport.
     * @param keyValue the key used to identify this transport proxy.
     * @param serverPropertiesArg the server properties.
     * @param proxyListenerArg the proxy listener.
     * @param visualCompareInterfaceArg the visual compare interface.
     */
    public AbstractTransportProxy(String keyValue, ServerProperties serverPropertiesArg, TransportProxyListenerInterface proxyListenerArg, VisualCompareInterface visualCompareInterfaceArg) {
        this.readLock = new Object();
        serverProperties = serverPropertiesArg;
        proxyListener = proxyListenerArg;

        compressor = new ZlibCompressor();
        decompressor = new ZlibCompressor();
        visualCompareInterface = visualCompareInterfaceArg;
        this.proxyKeyValue = keyValue;
    }

    @Override
    public ServerProperties getServerProperties() {
        return serverProperties;
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return serverProperties.getServerName();
    }

    @Override
    public Object getReadLock() {
        return readLock;
    }

    @Override
    public VisualCompareInterface getVisualCompareInterface() {
        return this.visualCompareInterface;
    }

    @Override
    public void setHeartBeatThread(HeartbeatThread thread) {
        this.heartbeatThread = thread;
    }

    @Override
    public HeartbeatThread getHeartBeatThread() {
        return this.heartbeatThread;
    }

    private String buildKeyValue(ArchiveDirManagerInterface listener) {
        return buildKeyValue(listener.getProjectName(), listener.getBranchName(), listener.getAppendedPath());
    }

    private String buildKeyValue(final String projectName, final String branchName, final String appendedPath) {
        String standardAppendedPath = Utility.convertToStandardPath(appendedPath);
        return projectName + ":" + branchName + ":" + standardAppendedPath;
    }

    @Override
    public String getTransportProxyKey() {
        return proxyKeyValue;
    }

    @Override
    public void addReadListener(ArchiveDirManagerInterface listener) {
        String keyValue = buildKeyValue(listener);
        listeners.put(keyValue, listener);
    }

    @Override
    public void removeReadListener(ArchiveDirManagerInterface listener) {
        String keyValue = buildKeyValue(listener);
        listeners.remove(keyValue);
    }

    @Override
    public void removeAllListeners() {
        TransportProxyFactory proxyFactory = TransportProxyFactory.getInstance();
        String projectName;
        String branchName;
        for (ArchiveDirManagerInterface directoryManager : listeners.values()) {
            LOGGER.trace("Removing directory manager for: " + directoryManager.getProjectName() + ":" + directoryManager.getAppendedPath());
            DirectoryManagerFactory.getInstance().removeDirectoryManager(getServerName(), directoryManager.getProjectName(), directoryManager.getBranchName(),
                    directoryManager.getAppendedPath());
            projectName = directoryManager.getProjectName();
            branchName = directoryManager.getBranchName();

            // We're looking at the project node.
            String appendedPath = directoryManager.getAppendedPath();
            if (appendedPath.length() == 0) {
                ServerResponseProjectControl projectControl = new ServerResponseProjectControl();
                projectControl.setServerName(getServerName());
                projectControl.setProjectName(projectName);
                projectControl.setBranchName(branchName);
                projectControl.setRemoveFlag(true);
                proxyFactory.notifyListeners(projectControl);
            }
        }

        // No more listeners here.
        listeners.clear();
    }

    @Override
    public ArchiveDirManagerInterface getDirectoryManager(final String projectName, final String branchName, final String appendedPath) {
        return listeners.get(buildKeyValue(projectName, branchName, appendedPath));
    }

    @Override
    public void setIsLoggedInToServer(boolean flag) {
        isLoggedInToServerFlag = flag;
    }

    @Override
    public boolean getIsLoggedInToServer() {
        return isLoggedInToServerFlag;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String user) {
        this.username = user;
    }

    @Override
    public boolean getIsOpen() {
        return isOpenFlag;
    }

    protected void setIsOpen(boolean flag) {
        isOpenFlag = flag;
    }

    protected Socket getSocket() {
        return socket;
    }

    protected void setSocket(Socket s) {
        socket = s;
    }

    @Override
    public Object read() {
        Object retVal = null;
        boolean closeFlag = false;

        synchronized (responseStreamSyncObject) {
            if (objectResponseStream != null) {
                try {
                    retVal = objectResponseStream.readObject();
                    if (retVal instanceof byte[]) {
                        // We'll need to de-compress this.
                        byte[] compressedInput = (byte[]) retVal;
                        retVal = decompress(compressedInput);
                    }
                } catch (java.io.EOFException e) {
                    // Server has shut down...
                    LOGGER.info("*=*=*=*=*=*= EORException: Client thinks server has shut down: [{}]", e.getLocalizedMessage());
                    retVal = null;
                    closeFlag = true;
                } catch (java.net.SocketException e) {
                    // Server has died...
                    LOGGER.info("#=#=#=#=#=#= SocketException: Client thinks server has died: [{}]", e.getLocalizedMessage());
                    retVal = null;
                    closeFlag = true;
                } catch (java.io.IOException | ClassNotFoundException e) {
                    // Server has died...
                    LOGGER.warn("#=#=#=#=#=#=#= Something died: [{}]", e.getLocalizedMessage());
                    retVal = null;
                    closeFlag = true;
                }
            }
            if (closeFlag) {
                close();
            }
        }
        return retVal;
    }

    @Override
    public void write(Object object) {
        boolean closeFlag = false;
        synchronized (requestStreamSyncObject) {
            if (objectRequestStream != null) {
                try {
                    Object retVal = compress(object);
                    objectRequestStream.writeObject(retVal);
                    objectRequestStream.flush();

                    // To avoid memory leaks.
                    objectRequestStream.reset();
                } catch (IOException e) {
                    closeFlag = true;
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
            if (closeFlag) {
                close();
            }
        }
    }

    @Override
    public abstract boolean open(int port);

    /**
     * Close the connection to the server.
     */
    @Override
    public void close() {
        setIsOpen(false);
        try {
            closeObjectRequestStream();
            closeObjectResponseStream();
            if (getHeartBeatThread() != null) {
                getHeartBeatThread().terminateHeartBeatThread();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            socket = null;
            setObjectRequestStream(null);
            setObjectResponseStream(null);
        }
    }

    private Object compress(Object object) {
        Object retVal = object;
        byte[] inputBuffer;
        try {
            try (ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream(); ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteOutputStream)) {
                objectOutputStream.writeObject(object);
                objectOutputStream.flush();
                inputBuffer = byteOutputStream.toByteArray();
            }
            if (compressor.compress(inputBuffer)) {
                retVal = compressor.getCompressedBuffer();
                LOGGER.debug("* * * * * * * * * Compressed * * * * * * * * * * * *" + object.getClass().toString() + " from " + inputBuffer.length + " to "
                        + compressor.getCompressedBuffer().length);
            }
        } catch (java.lang.OutOfMemoryError e) {
            // If they are trying to create an archive for a really big file,
            // we might have problems.
            LOGGER.warn("Out of memory trying to compress object for transport layer.");
        } catch (IOException e) {
            LOGGER.warn("Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
        }
        return retVal;
    }

    private Object decompress(byte[] compressedInput) {
        Object retVal = null;
        byte[] expandedBuffer = decompressor.expand(compressedInput);
        ByteArrayInputStream byteInputStream;
        ObjectInputStream objectInputStream;
        try {
            byteInputStream = new ByteArrayInputStream(expandedBuffer);
            objectInputStream = new ObjectInputStream(byteInputStream);
            retVal = objectInputStream.readObject();
            objectInputStream.close();
            byteInputStream.close();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Caught exception trying to decompress an object: " + e.getClass().toString() + " " + e.getLocalizedMessage());
        }
        return retVal;
    }

    @Override
    public TransportProxyListenerInterface getProxyListener() {
        return proxyListener;
    }

    /**
     * Get the object request stream.
     * @return the object request stream.
     */
    public ObjectOutputStream getObjectRequestStream() {
        synchronized (requestStreamSyncObject) {
            return objectRequestStream;
        }
    }

    /**
     * Set the object request stream.
     * @param requestStream the object request stream.
     */
    public void setObjectRequestStream(ObjectOutputStream requestStream) {
        synchronized (requestStreamSyncObject) {
            objectRequestStream = requestStream;
        }
    }

    private void closeObjectRequestStream() throws IOException {
        synchronized (requestStreamSyncObject) {
            if (objectRequestStream != null) {
                objectRequestStream.close();
            }
        }
    }

    /**
     * Get the object response stream.
     * @return the object response stream.
     */
    public ObjectInputStream getObjectResponseStream() {
        synchronized (responseStreamSyncObject) {
            return objectResponseStream;
        }
    }

    private void closeObjectResponseStream() throws IOException {
        synchronized (responseStreamSyncObject) {
            if (objectResponseStream != null) {
                objectResponseStream.close();
            }
        }
    }

    /**
     * Set the object response stream.
     * @param responseStream the object response stream.
     */
    public void setObjectResponseStream(ObjectInputStream responseStream) {
        synchronized (responseStreamSyncObject) {
            objectResponseStream = responseStream;
        }
    }
}
