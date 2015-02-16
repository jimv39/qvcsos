//   Copyright 2004-2015 Jim Voris
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server properties.
 * @author Jim Voris
 */
public class ServerProperties extends com.qumasoft.qvcslib.QumaProperties {
    // Create our logger object

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerProperties.class);
    private static final String SERVER_NAME_TAG = "QVCS_SERVER_NAME";
    private static final String SERVER_IP_ADDRESS_TAG = "QVCS_SERVER_IP_ADDRESS";
    private static final String CLIENT_PORT_TAG = "QVCS_SERVER_PORT";
    private static final String SERVER_ADMIN_PORT_TAG = "QVCS_SERVER_ADMIN_PORT";
    private String serverName;

    /**
     * Creates a new instance of ServerProperties.
     * @param server the server name.
     */
    public ServerProperties(String server) {
        serverName = server;

        setPropertyFileName(System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_PROPERTIES_DIRECTORY
                + File.separator
                + QVCSConstants.QVCS_SERVERNAME_PROPERTIES_PREFIX + server + ".properties");

        loadProperties(getPropertyFileName());

        setServerName(server);
    }

    /**
     * Default constructor. This constructor is used by the {@link com.qumasoft.clientapi.ClientAPI ClientAPI}
     */
    public ServerProperties() {
        setActualProperties(new java.util.Properties());
    }

    /**
     * Load the properties for the server.
     * @param propertyFilename the name of the property file from which we load the properties.
     */
    final void loadProperties(String propertyFilename) {
        java.util.Properties defaultProperties = DefaultServerProperties.getInstance().getServerProperties();
        FileInputStream inStream = null;

        // The default properties are from the default server's properties
        setActualProperties(new java.util.Properties(defaultProperties));
        try {
            inStream = new FileInputStream(new File(propertyFilename));
            getActualProperties().load(inStream);
        } catch (IOException e) {
            LOGGER.warn("Exception: " + e.getLocalizedMessage() + ". Server properties file not found: " + propertyFilename);
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Exception in closing server properties file: " + propertyFilename + ". Exception: " + e.getClass().toString() + ": "
                            + e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Save the property file to disk.
     */
    public void saveProperties() {
        FileOutputStream outStream = null;
        if (getActualProperties() != null) {
            try {
                File propertyFile = new File(getPropertyFileName());
                propertyFile.getParentFile().mkdirs();
                outStream = new FileOutputStream(propertyFile);
                getActualProperties().store(outStream, "QVCS Server Properties for server: " + serverName);
            } catch (IOException e) {
                // Catch any exception.  If the property file is missing, we'll just go
                // with the defaults.
                LOGGER.warn(e.getLocalizedMessage(), e);
            } finally {
                if (outStream != null) {
                    try {
                        outStream.close();
                    } catch (IOException e) {
                        LOGGER.warn("Exception in closing server properties file: " + getPropertyFileName() + ". Exception: " + e.getClass().toString() + ": "
                                + e.getLocalizedMessage());
                    }
                }
            }
        }
    }

    /**
     * Delete the property file.
     */
    public void removePropertiesFile() {
        try {
            File propertyFile = new File(getPropertyFileName());
            propertyFile.delete();
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
    }

    /**
     * Get the server properties.
     * @return the server properties.
     */
    public java.util.Properties getServerProperties() {
        return getActualProperties();
    }

    protected String getServerNameTag() {
        return SERVER_NAME_TAG;
    }

    protected String getServerIPAddressTag() {
        return SERVER_IP_ADDRESS_TAG;
    }

    protected String getClientPortTag() {
        return CLIENT_PORT_TAG;
    }

    protected String getServerAdminPortTag() {
        return SERVER_ADMIN_PORT_TAG;
    }

////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return getStringValue(getServerNameTag());
    }

    /**
     * Set the server name.
     * @param server the server name.
     */
    public final void setServerName(String server) {
        setStringValue(getServerNameTag(), server);
    }

    /**
     * Get the server IP address.
     * @return the server IP address.
     */
    public String getServerIPAddress() {
        return getStringValue(getServerIPAddressTag());
    }

    /**
     * Set the server IP address.
     * @param serverIPAddress the server IP address.
     */
    public void setServerIPAddress(String serverIPAddress) {
        setStringValue(getServerIPAddressTag(), serverIPAddress);
    }

    /**
     * Get the port used for client connections.
     * @return the port used for client connections.
     */
    public int getClientPort() {
        return getIntegerValue(getClientPortTag());
    }

    /**
     * Set the port used for client connections.
     * @param port the port used for client connections.
     */
    public void setClientPort(int port) {
        setIntegerValue(getClientPortTag(), port);
    }

    /**
     * Get the port used for admin connections.
     * @return the port used for admin connections.
     */
    public int getServerAdminPort() {
        return getIntegerValue(getServerAdminPortTag());
    }

    /**
     * Set the port used for admin connections.
     * @param port the port used for admin connections.
     */
    public void setServerAdminPort(int port) {
        setIntegerValue(getServerAdminPortTag(), port);
    }

    /**
     * Get the type of transport used for client connections.
     * @return the type of transport used for client connections.
     */
    public TransportProxyType getClientTransport() {
        return TransportProxyFactory.RAW_SOCKET_PROXY;
    }

    /**
     * Get the type of transport used for admin connections.
     * @return the type of transport used for admin connections.
     */
    public TransportProxyType getServerAdminTransport() {
        return TransportProxyFactory.RAW_SOCKET_PROXY;
    }

    /**
     * String representation of the server properties. For now we just report the server name.
     * @return the server name.
     */
    @Override
    public String toString() {
        return getServerName();
    }
}
