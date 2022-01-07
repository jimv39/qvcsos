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

import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to manage authentication actions for QVCS-Enterprise. Note that all
 * passwords passed in to the methods of this class are already in hashed form,
 * or at least they should be.
 * <P>
 * See the Utility method hashPassword().
 *
 * Note that we also store the web server port here since it is a convenient
 * place to pass it from the web server to be visible at login time.
 *
 * @author Jim Voris
 */
public final class AuthenticationManager {

    private static final AuthenticationManager AUTHENTICATION_MANAGER = new AuthenticationManager();
    private boolean isInitializedFlag = false;
    private AuthenticationStore store = null;
    private int clientPort;
    private int webServerPort;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationManager.class);

    /**
     * Creates a new instance of Authentication Manager.
     */
    private AuthenticationManager() {
    }

    /**
     * Get the Authentication Manager singleton.
     * @return the Authentication Manager singleton.
     */
    public static AuthenticationManager getAuthenticationManager() {
        return AUTHENTICATION_MANAGER;
    }

    /**
     * Initialize the authentication manager.
     * @return true if we initialized successfully.
     */
    public synchronized boolean initialize() throws SQLException {
        if (!isInitializedFlag) {
            store = new AuthenticationStore();
            isInitializedFlag = true;
        }
        return isInitializedFlag;
    }

    /**
     * Authenticate a user.
     * @param userName user's name.
     * @param password the user's hashed password.
     * @return true for valid username/password; false otherwise.
     */
    public synchronized boolean authenticateUser(String userName, byte[] password) {
        return store.authenticateUser(userName, password);
    }

    /**
     * Add a user.
     * @param callerUserName caller's user name.
     * @param userName user name to add.
     * @param password user's password.
     * @return true if add was successful.
     */
    public synchronized boolean addUser(String callerUserName, String userName, byte[] password) {
        boolean retVal = false;

        // This can only be done by an admin user
        if (0 == callerUserName.compareTo(RoleManager.ADMIN)) {
            retVal = store.addUser(userName, password);
        }

        return retVal;
    }

    /**
     * Update a user's password.
     * @param callerUserName caller's user name.
     * @param userName user's name.
     * @param oldPassword user's old hashed password. This is not needed/used if the ADMIN user is making the change.
     * @param newPassword user's new hashed password.
     * @return true if successful; false otherwise.
     */
    public synchronized boolean updateUser(String callerUserName, String userName, byte[] oldPassword, byte[] newPassword) {
        boolean retVal = false;

        // An admin user can force this change...
        if (0 == callerUserName.compareTo(RoleManager.ADMIN)) {
            retVal = store.updateUserPassword(userName, newPassword);
        } else {
            // Or the existing user can change their own password
            if (authenticateUser(callerUserName, oldPassword)) {
                if (0 == callerUserName.compareTo(userName)) {
                    retVal = store.updateUserPassword(userName, newPassword);
                }
            }
        }

        return retVal;
    }

    /**
     * Remove a user.
     * @param callerUserName caller's user name. The caller <i>must</i> be the ADMIN user.
     * @param userName user name.
     * @return true if the user is removed.
     */
    public synchronized boolean removeUser(String callerUserName, String userName) {
        boolean retVal = false;

        // This can only be done by an admin user
        if (0 == callerUserName.compareTo(RoleManager.ADMIN)) {
            retVal = store.removeUser(userName);
        }

        return retVal;
    }

    /**
     * List users.
     * @return a String[] of user names.
     */
    public synchronized String[] listUsers() {
        return store.listUsers();
    }

    /**
     * @return the webServerPort
     */
    public int getWebServerPort() {
        return webServerPort;
    }

    /**
     * @param port the webServerPort to set
     */
    public void setWebServerPort(int port) {
        this.webServerPort = port;
    }

    /**
     * @return the clientPort
     */
    public int getClientPort() {
        return clientPort;
    }

    /**
     * @param port the clientPort to set
     */
    public void setClientPort(int port) {
        this.clientPort = port;
    }
}
