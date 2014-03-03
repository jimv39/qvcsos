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

import com.qumasoft.qvcslib.Utility;
import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Authentication Store.
 * @author Jim Voris
 */
public final class AuthenticationStore implements Serializable {
    private static final long serialVersionUID = -3418568561041484467L;
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    /**
     * This map contains the users and their hashed passwords
     */
    private Map<String, byte[]> map = Collections.synchronizedMap(new TreeMap<String, byte[]>());

    /**
     * Creates a new instance of AuthenticationStore.
     */
    public AuthenticationStore() {
        // The default is to populate the store with single user ADMIN, with a password of ADMIN
        byte[] hashedPassword = Utility.getInstance().hashPassword(RoleManagerInterface.ADMIN_ROLE.getRoleType());
        map.put(RoleManagerInterface.ADMIN_ROLE.getRoleType(), hashedPassword);
    }

    boolean addUser(String userName, byte[] password) {
        boolean retVal = false;

        if (map.containsKey(userName)) {
            LOGGER.log(Level.WARNING, "AuthenticationStore.addUser -- attempt to add user: [" + userName + "]. User already exists!");
        } else {
            byte[] passwordClone = new byte[password.length];
            System.arraycopy(password, 0, passwordClone, 0, passwordClone.length);
            map.put(userName, passwordClone);
            LOGGER.log(Level.INFO, "AuthenticationStore.addUser -- adding user: [" + userName + "]");
            retVal = true;
        }
        return retVal;
    }

    boolean removeUser(String userName) {
        boolean retVal = false;

        if (userName.equals(RoleManagerInterface.ADMIN_ROLE.getRoleType())) {
            LOGGER.log(Level.WARNING, "Attempt to remove ADMIN is not allowed.");
        } else {
            if (map.containsKey(userName)) {
                map.remove(userName);
                LOGGER.log(Level.INFO, "AuthenticationStore.removeUser -- removing user: [" + userName + "]");
                retVal = true;
            } else {
                LOGGER.log(Level.WARNING, "AuthenticationStore.removeUser -- attempt to remove non-existing user: [" + userName + "]");
            }
        }
        return retVal;
    }

    boolean updateUser(String userName, byte[] newPassword) {
        boolean retVal = false;

        if (map.containsKey(userName)) {
            map.put(userName, newPassword);
            LOGGER.log(Level.INFO, "AuthenticationStore.updateUser -- updating user: [" + userName + "]");
            retVal = true;
        } else {
            LOGGER.log(Level.WARNING, "AuthenticationStore.updateUser -- attempt to update a non-existing user: [" + userName + "]");
        }
        return retVal;
    }

    boolean authenticateUser(String userName, byte[] password) {
        boolean retVal = false;

        if (map.containsKey(userName)) {
            byte[] storedPassword = map.get(userName);

            if ((storedPassword != null) && (storedPassword.length == password.length)) {
                retVal = true;
                for (int i = 0; i < storedPassword.length; i++) {
                    if (storedPassword[i] != password[i]) {
                        LOGGER.log(Level.WARNING, "AuthenticationStore.authenticateUser -- authentication failed for user: [" + userName + "]");
                        retVal = false;
                        break;
                    }
                }
                LOGGER.log(Level.INFO, "AuthenticationStore.authenticateUser -- authenticated user: [" + userName + "]");
            } else {
                LOGGER.log(Level.WARNING, "AuthenticationStore.authenticateUser -- authentication failed for user: [" + userName + "]");
            }
        } else {
            LOGGER.log(Level.WARNING, "AuthenticationStore.authenticateUser -- attempt to authenticate a non-existing user: [" + userName + "]");
        }
        return retVal;
    }

    void dumpMap() {
        LOGGER.log(Level.INFO, "AuthenticationStore.dumpMap()");
        Set keys = map.keySet();
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            LOGGER.log(Level.INFO, i.next().toString());
        }
    }

    String[] listUsers() {
        Set keys = map.keySet();
        String[] users = new String[keys.size()];
        int j = 0;
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            users[j++] = i.next().toString();
        }
        return users;
    }
}
