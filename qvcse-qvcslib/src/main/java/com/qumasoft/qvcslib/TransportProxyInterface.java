/*   Copyright 2004-2014 Jim Voris
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

/**
 * Transport proxy interface. For a client, each connection between client and server is managed by a TransportProxy instance.
 * Any supported transport must implement this interface.
 * @author Jim Voris
 */
public interface TransportProxyInterface {

    /**
     * Open a connection to the server listening on the given port number. Note that the server IP address is defined in the server properties.
     * @param port the port that the server is listening on.
     * @return true if the open is successful; false if not.
     */
    boolean open(int port);

    /**
     * Close the connection to the server.
     */
    void close();

    /**
     * Is this connection open.
     * @return true if open; false if not open.
     */
    boolean getIsOpen();

    /**
     * Get the server properties.
     * @return the server properties.
     */
    ServerProperties getServerProperties();

    /**
     * Set the flag that indicates whether this connection succeeded in logging in to the server.
     * @param flag true if login succeeded; false otherwise.
     */
    void setIsLoggedInToServer(boolean flag);

    /**
     * Are we logged in to the server.
     * @return true if logged in; false if not logged in.
     */
    boolean getIsLoggedInToServer();

    /**
     * Set the user name associated with this connection.
     * @param username the user name to associate with this connection.
     */
    void setUsername(String username);

    /**
     * Get the user name associated with this connection.
     * @return the user name associated with this connection.
     */
    String getUsername();

    /**
     * Read an Object from the server. This is a blocking read. Any Object read must implement the {@Link java.io.Serializable} interface.
     * @return an Object read from the server.
     */
    Object read();

    /**
     * Write an Object to the server. This is a blocking write. Any Object we write must implement the {@Link java.io.Serializable} interface.
     * @param object the Object to write.
     */
    void write(Object object);

    /**
     * Get an Object that must be used for synchronization of reads.
     * @return an Object that must be used for synchronization of reads.
     */
    Object getReadLock();

    /**
     * Add a read listener.
     * @param listener an archive directory manager that can listen to this connection.
     */
    void addReadListener(ArchiveDirManagerInterface listener);

    /**
     * Remove a read listener.
     * @param listener the listener to remove.
     */
    void removeReadListener(ArchiveDirManagerInterface listener);

    /**
     * Remove all listeners.
     */
    void removeAllListeners();

    /**
     * Get (by lookup) the directory manager for the given project, view, and appendedPath.
     * @param project the project name.
     * @param viewName the view name.
     * @param appendedPath the appended path.
     * @return the directory manager for the given project, view, and appended path.
     */
    ArchiveDirManagerInterface getDirectoryManager(String project, String viewName, String appendedPath);

    /**
     * Get the name of the transport.
     * @return the name of the transport.
     */
    String getTransportName();

    /**
     * Get the proxy listener associated with this connection.
     * @return the proxy listener associated with this connection.
     */
    TransportProxyListenerInterface getProxyListener();

    /**
     * Get the visual compare interface implementation associated with this connection.
     * @return the visual compare interface implementation associated with this connection.
     */
    VisualCompareInterface getVisualCompareInterface();

    /**
     * Set the heartbeat thread for this connection.
     * @param heartbeatThread the heartbeat thread.
     */
    void setHeartBeatThread(HeartbeatThread heartbeatThread);

    /**
     * Get the heartbeat thread associated with this connection.
     * @return the heartbeat thread associated with this connection.
     */
    HeartbeatThread getHeartBeatThread();
}
