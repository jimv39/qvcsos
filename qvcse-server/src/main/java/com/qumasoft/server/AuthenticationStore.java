/*   Copyright 2004-2021 Jim Voris
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

import com.qumasoft.qvcslib.Utility;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.UserDAO;
import com.qvcsos.server.dataaccess.impl.UserDAOImpl;
import com.qvcsos.server.datamodel.User;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Authentication Store.
 * @author Jim Voris
 */
public final class AuthenticationStore implements Serializable {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationStore.class);

    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of AuthenticationStore. Create the ADMIN user
     * if the ADMIN user does not yet exist.
     * @throws java.sql.SQLException on database connection problems.
     */
    public AuthenticationStore() throws SQLException {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = this.databaseManager.getSchemaName();
        UserDAO userDAO = new UserDAOImpl(schemaName);
        User adminUser = userDAO.findByUserName(RoleManager.ADMIN);
        if (adminUser == null) {
            // The default is to populate the store with single user ADMIN, with a password of ADMIN
            byte[] hashedPassword = Utility.getInstance().hashPassword(RoleManager.ADMIN);
            addUser(RoleManager.ADMIN, hashedPassword);
        } else {
            // The ADMIN user already exists... See if we need to update it to the default password.
            if (adminUser.getPassword().length == 1) {
                // The default is to populate the store with single user ADMIN, with a password of ADMIN
                byte[] hashedPassword = Utility.getInstance().hashPassword(RoleManager.ADMIN);
                updateUserPassword(RoleManager.ADMIN, hashedPassword);
            }
        }
    }

    boolean addUser(String userName, byte[] hashedPassword) throws SQLException {
        boolean retVal = true;
        try {
            UserDAO userDAO = new UserDAOImpl(schemaName);
            User newUser = new User();
            newUser.setUserName(userName);
            newUser.setDeletedFlag(Boolean.FALSE);
            newUser.setPassword(hashedPassword);
            Integer newUserId = userDAO.insert(newUser);
            LOGGER.info("Added user [{}] to database with id of: [{}]", userName, newUserId);
        } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            retVal = false;
        }
        return retVal;
    }

    boolean removeUser(String userName) {
        boolean retVal = false;
        if (userName.equals(RoleManager.ADMIN)) {
            LOGGER.warn("Attempt to remove ADMIN is not allowed.");
        } else {
            UserDAO userDAO = new UserDAOImpl(schemaName);
            User user = userDAO.findByUserName(userName);
            if (user != null) {
                if (user.getDeletedFlag()) {
                    LOGGER.warn("AuthenticationStore.removeUser -- user: [{}] already deleted.", userName);
                } else {
                    retVal = userDAO.delete(user);
                }
            } else {
                LOGGER.warn("AuthenticationStore.removeUser -- attempt to remove non-existing user: [{}]", userName);
            }
        }
        return retVal;
    }

    boolean updateUserPassword(String userName, byte[] newPassword) {
        boolean retVal = false;
        if (newPassword.length > 1) {
            UserDAO userDAO = new UserDAOImpl(schemaName);
            User user = userDAO.findByUserName(userName);
            if (user != null) {
                userDAO.updateUserPassword(user.getId(), newPassword);
                retVal = true;
            } else {
                LOGGER.warn("AuthenticationStore.updateUserPassword -- attempt to change password for non-existing user: [{}]", userName);
            }
        } else {
            LOGGER.warn("Password must be longer than a single character");
        }

        return retVal;
    }

    boolean authenticateUser(String userName, byte[] password) {
        boolean retVal = false;
        UserDAO userDAO = new UserDAOImpl(schemaName);
        User user = userDAO.findByUserName(userName);
        if (user != null) {
            byte[] storedPassword = user.getPassword();
            if ((storedPassword != null) && (storedPassword.length == password.length)) {
                retVal = true;
                for (int i = 0; i < storedPassword.length; i++) {
                    if (storedPassword[i] != password[i]) {
                        LOGGER.warn("AuthenticationStore.authenticateUser -- authentication failed for user: [{}]", userName);
                        retVal = false;
                        break;
                    }
                }
                LOGGER.info("AuthenticationStore.authenticateUser -- authenticated user: [{}]", userName);
            } else {
                LOGGER.warn("AuthenticationStore.authenticateUser -- authentication failed for user: [{}]", userName);
            }
        } else {
            LOGGER.warn("AuthenticationStore.authenticateUser -- attempt to authenticate a non-existing user: [{}]", userName);
        }
        return retVal;
    }

    String[] listUsers() {
        UserDAO userDAO = new UserDAOImpl(schemaName);
        List<User> userList = userDAO.findAll();
        String[] users = new String[userList.size()];
        int j = 0;
        for (User user : userList) {
            users[j++] = user.getUserName();
        }
        return users;
    }
}
