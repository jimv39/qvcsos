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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Raw socket transport proxy. Basically this is used by clients for non-encrypted traffic between client and server. Objects are compressed before they are sent (if possible).
 * @author Jim Voris
 */
public class RawSocketTransportProxy extends AbstractTransportProxy {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(RawSocketTransportProxy.class);

    /**
     * Create a raw socket transport proxy.
     * @param keyValue the key used to identify this transport proxy.
     * @param serverProperties the server properties for the server that the client will try to connect to.
     * @param listener a listener for out-of-band notification messages.
     * @param visualCompareInterface the visual compare utility to use for asynchronous visual compare responses.
     */
    RawSocketTransportProxy(String keyValue, ServerProperties serverProperties, TransportProxyListenerInterface listener, VisualCompareInterface visualCompareInterface) {
        super(keyValue, serverProperties, listener, visualCompareInterface);
    }

    /**
     * Open the transport. This establishes a socket connection to the server.
     * @param port the server port number to attempt to connect to.
     * @return true if the socket connection is established; false otherwise.
     */
    @Override
    public synchronized boolean open(int port) {
        boolean retVal = true;
        Socket socket = null;
        try {
            socket = new java.net.Socket(java.net.InetAddress.getByName(getServerProperties().getServerIPAddress()), port);
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            LOGGER.info("RawSocketTransportProxy connected to [" + getServerProperties().getServerIPAddress() + "] on port [" + port + "]");
            setObjectRequestStream(new ObjectOutputStream(socket.getOutputStream()));
            LOGGER.trace("\tgot output stream");
            setObjectResponseStream(new ObjectInputStream(socket.getInputStream()));
            LOGGER.trace("\tgot input stream");
            LOGGER.trace("\tserver IP  address: " + socket.getInetAddress().getHostAddress());
            LOGGER.trace("\tlocal  socket port: " + socket.getLocalPort());
            LOGGER.trace("\tremote socket port: " + socket.getPort());
        } catch (IOException e) {
            LOGGER.warn("Failed to connect to server: [{}] at IP: [{}] on port: [{}] ", getServerProperties().getServerName(), getServerProperties().getServerIPAddress(), port);
            LOGGER.warn(e.getLocalizedMessage(), e);
            retVal = false;
        } finally {
            try {
                if (!retVal) {
                    if (socket != null) {
                        socket.close();
                    }
                    socket = null;
                    setObjectRequestStream(null);
                    setObjectResponseStream(null);
                } else {
                    setSocket(socket);
                }
            } catch (IOException e) {
                retVal = false;
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }

        if (retVal) {
            setIsOpen(true);
        }
        return retVal;
    }

    /**
     * Get the name of this proxy type.
     * @return that this is a raw socket proxy.
     */
    @Override
    public String getTransportName() {
        return TransportProxyFactory.RAW_SOCKET_PROXY.getTransportType();
    }
}
