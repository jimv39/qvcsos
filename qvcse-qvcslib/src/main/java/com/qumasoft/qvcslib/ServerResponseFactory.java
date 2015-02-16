/*   Copyright 2004-2015 Jim Voris
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

import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.qvcslib.response.ServerResponseTransactionBegin;
import com.qumasoft.qvcslib.response.ServerResponseTransactionEnd;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server response factory.
 * @author Jim Voris
 */
public class ServerResponseFactory implements ServerResponseFactoryInterface {
    // Create our logger object

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerResponseFactory.class);
    private java.io.ObjectOutputStream objectOutputStream = null;
    private java.io.OutputStream outputStream = null;
    private final Object outputStreamSyncObject = new Object();
    private final Set<ArchiveDirManagerInterface> directoryManagers = new HashSet<>();
    private String userName = null;
    private String serverName = null;
    private boolean isUserLoggedInFlag = false;
    private int clientPort = -1;
    private String clientIPAddress = null;
    private boolean connectionAliveFlag = false;
    private HeartBeatTimerTask heartBeatTimerTask = null;
    private static final long HEART_BEAT_COUNT_BEFORE_DECLARING_FAILURE = 8;
    /** This can be static because there is only one server. */
    private static boolean shutdownInProgressFlag = false;

    /**
     * Creates new ServerResponseFactory.
     * @param oStream the output stream. Typically, this will be the output stream of a Socket.
     * @param cPort the client port associated with the socket.
     * @param cIPAddress the client IP address associated with the socket.
     */
    public ServerResponseFactory(java.io.OutputStream oStream, final int cPort, final String cIPAddress) {
        try {
            outputStream = oStream;
            objectOutputStream = new ObjectOutputStream(oStream);
            clientPort = cPort;
            clientIPAddress = cIPAddress;
            connectionAliveFlag = true;
            initKeepAliveTimer();
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            objectOutputStream = null;
        }
    }

    /**
     * Set the shutdown in progress flag.
     * @param flag the shutdown in progress flag.
     */
    public static void setShutdownInProgress(boolean flag) {
        shutdownInProgressFlag = flag;
    }

    /**
     * Get the shutdown in progress flag.
     * @return the shutdown in progress flag.
     */
    public static boolean getShutdownInProgress() {
        return shutdownInProgressFlag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void createServerResponse(java.io.Serializable responseObject) {
        if (null != responseObject) {
            try {
                MutableByteArray responseArray = new MutableByteArray();

                // Make sure the user is logged in before we actually share any
                // info with them.
                if (!getIsUserLoggedIn()) {
                    if (responseObject instanceof ServerResponseLogin) {
                        ServerResponseLogin response = (ServerResponseLogin) responseObject;
                        LOGGER.warn("User [" + response.getUserName() + "] failed to login.");
                    } else if (responseObject instanceof ServerResponseTransactionBegin) {
                        LOGGER.trace("Sending transaction begin without being logged in.");
                    } else if (responseObject instanceof ServerResponseTransactionEnd) {
                        LOGGER.trace("Sending transaction end without being logged in.");
                    } else {
                        ServerResponseError error = new ServerResponseError("Not logged in!!", null, null, null);
                        responseObject = error;
                    }
                }

                synchronized (outputStreamSyncObject) {
                    // Cancel the heartbeat timer so we won't kill the connection for really long/big responses.
                    heartBeatTimerTask.cancel();

                    // Compress the response
                    if (compress(responseObject, responseArray)) {
                        // Things compressed... send the compressed result.
                        objectOutputStream.writeObject(responseArray.getValue());
                    } else {
                        // Things would not compress... just send the original object.
                        objectOutputStream.writeObject(responseObject);
                    }
                    objectOutputStream.flush();
                    objectOutputStream.reset();
                }
                clientIsAlive();
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                try {
                    outputStream.close();
                } catch (IOException ioe) {
                    LOGGER.warn(ioe.getLocalizedMessage(), ioe);
                } finally {
                    connectionAliveFlag = false;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addArchiveDirManager(ArchiveDirManagerInterface archiveDirManager) {
        directoryManagers.add(archiveDirManager);
    }

    /**
     * Get the directory managers associated with this client connection.
     * @return the directory managers associated with this client connection.
     */
    public Set<ArchiveDirManagerInterface> getDirectoryManagers() {
        return directoryManagers;
    }

    private boolean compress(java.io.Serializable responseObject, MutableByteArray compressedArray) {
        boolean retVal;
        try {
            Compressor compressor = new ZlibCompressor();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream compressedObjectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            compressedObjectOutputStream.writeObject(responseObject);
            compressedObjectOutputStream.flush();
            compressedObjectOutputStream.close();
            byteArrayOutputStream.close();
            byte[] inputByteArray = byteArrayOutputStream.toByteArray();
            retVal = compressor.compress(inputByteArray);

            if (retVal) {
                compressedArray.setValue(compressor.getCompressedBuffer());
                LOGGER.trace("Compressed server response for " + responseObject.getClass().toString() + " from: " + inputByteArray.length + " to: "
                        + compressedArray.getValue().length);
            }
        } catch (java.lang.OutOfMemoryError e) {
            retVal = false;

            // If they are trying to create an archive for a really big file,
            // we might have problems.
            LOGGER.warn("Out of memory trying to compress response object");
        } catch (IOException e) {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Is the user logged in.
     * @return true if the user is logged in; false if not logged in.
     */
    public boolean getIsUserLoggedIn() {
        return isUserLoggedInFlag;
    }

    /**
     * Set the logged in flag.
     * @param flag the logged in flag.
     */
    public void setIsUserLoggedIn(boolean flag) {
        isUserLoggedInFlag = flag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name.
     * @param name the user name.
     */
    public void setUserName(String name) {
        userName = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getServerName() {
        return serverName;
    }

    /**
     * Set the server name.
     * @param server the server name.
     */
    public void setServerName(String server) {
        serverName = server;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getClientPort() {
        return clientPort;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getClientIPAddress() {
        return clientIPAddress;
    }

    private void initKeepAliveTimer() {
        heartBeatTimerTask = new HeartBeatTimerTask();
        TimerManager.getInstance().getTimer().schedule(heartBeatTimerTask, HEART_BEAT_COUNT_BEFORE_DECLARING_FAILURE * QVCSConstants.HEART_BEAT_SLEEP_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clientIsAlive() {
        heartBeatTimerTask.cancel();
        heartBeatTimerTask = new HeartBeatTimerTask();
        TimerManager.getInstance().getTimer().schedule(heartBeatTimerTask, HEART_BEAT_COUNT_BEFORE_DECLARING_FAILURE * QVCSConstants.HEART_BEAT_SLEEP_TIME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getConnectionAliveFlag() {
        return connectionAliveFlag;
    }

    private void setConnectionAliveFlag(boolean flag) {
        connectionAliveFlag = flag;
    }

    class HeartBeatTimerTask extends TimerTask {

        @Override
        public void run() {
            setConnectionAliveFlag(false);
            try {
                outputStream.close();
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
    }

}
