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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to manage authentication actions for QVCS-Enterprise. Note that all passwords passed in to the methods of this class are
 * already in hashed form, or at least they should be. <P> See the Utility method hashPassword().
 *
 * @author Jim Voris
 */
public final class AuthenticationManager {

    private static final AuthenticationManager AUTHENTICATION_MANAGER = new AuthenticationManager();
    private boolean isInitializedFlag = false;
    private String storeName = null;
    private String oldStoreName = null;
    private AuthenticationStore store = null;
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");

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
    public synchronized boolean initialize() {
        if (!isInitializedFlag) {
            storeName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_AUTHENTICATION_STORE_NAME + "dat";

            oldStoreName = storeName + ".old";

            loadStore();
            isInitializedFlag = true;
        }
        return isInitializedFlag;
    }

    private synchronized void loadStore() {
        File storeFile;
        FileInputStream fileStream = null;

        try {
            storeFile = new File(storeName);
            fileStream = new FileInputStream(storeFile);
            ObjectInputStream inStream = new ObjectInputStream(fileStream);
            store = (AuthenticationStore) inStream.readObject();
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            store = new AuthenticationStore();
            writeStore();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));

            if (fileStream != null) {
                try {
                    fileStream.close();
                    fileStream = null;
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(ex));
                }
            }

            // Serialization failed.  Create a default store.
            store = new AuthenticationStore();
            writeStore();
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    private void writeStore() {
        FileOutputStream fileStream = null;

        try {
            File storeFile = new File(storeName);
            File oldStoreFile = new File(oldStoreName);

            if (oldStoreFile.exists()) {
                oldStoreFile.delete();
            }

            if (storeFile.exists()) {
                storeFile.renameTo(oldStoreFile);
            }

            File newStoreFile = new File(storeName);

            // Make sure the needed directories exists
            if (!newStoreFile.getParentFile().exists()) {
                newStoreFile.getParentFile().mkdirs();
            }

            fileStream = new FileOutputStream(newStoreFile);
            ObjectOutputStream outStream = new ObjectOutputStream(fileStream);
            outStream.writeObject(store);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
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

        if (retVal) {
            writeStore();
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
            retVal = store.updateUser(userName, newPassword);
        } else {
            // Or the existing user can change their own password
            if (authenticateUser(callerUserName, oldPassword)) {
                if (0 == callerUserName.compareTo(userName)) {
                    retVal = store.updateUser(userName, newPassword);
                }
            }
        }

        if (retVal) {
            writeStore();
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

        if (retVal) {
            writeStore();
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
}
