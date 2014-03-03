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
package com.qumasoft.qvcslib;

/**
 * Server response factory interface. Define those methods needed by the server response factory for sending response messages to a specific client. Each client has a separate
 * instance of a class that implements this interface.
 * @author Jim Voris
 */
public interface ServerResponseFactoryInterface {

    /**
     * Add an archive directory manager to the Set of managers associated with the factory instance... meaning that we keep track of the directories that the client is looking at
     * so that when the client goes away, we can get rid of our reference to the associated directory manager.
     * @param archiveDirManager the archive directory manager to add.
     */
    void addArchiveDirManager(ArchiveDirManagerInterface archiveDirManager);

    /**
     * Send the server response object to the client. The message <i>may</i> be compressed before sending.
     * @param responseObject the response object to send to the client.
     */
    void createServerResponse(java.io.Serializable responseObject);

    /**
     * Get the server name. This is the client's name for the server.
     * @return the client's name for the server.
     */
    String getServerName();

    /**
     * Get the user name.
     * @return the user name.
     */
    String getUserName();

    /**
     * Get the client port.
     * @return the client port.
     */
    int getClientPort();

    /**
     * Get the client IP address.
     * @return the client IP address.
     */
    String getClientIPAddress();

    /**
     * Is the client connection still alive.
     * @return true if the client appears to be alive; false otherwise.
     */
    boolean getConnectionAliveFlag();

    /**
     * Poke this method to indicate that we're still able to communicate with the client. This method must be called within every so often, or the server will determine that
     * the client is dead. Normally, the client will send a heartbeat message to the server at some interval. On receipt of the heartbeat from the client, the server will poke
     * this method, and as a result the watchdog timer will get reset, and the client/server connection will be deemed okay. Failure to receive the heartbeat from the client
     * will eventually cause the watchdog timer to kill the connection, and the server will close the socket and reclaim all resources associated with the client. This handles
     * the case where the client silently dies without specifically closing the socket from the client side. More specifically, this will kill the connection more quickly than
     * a TCP keep alive would kill it.
     */
    void clientIsAlive();
}
