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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RoleType;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Role privileges manager. This is a singleton.
 * @author Jim Voris
 */
public final class RolePrivilegesManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(RolePrivilegesManager.class);
    // This is a singleton.
    private static final RolePrivilegesManager ROLE_PRIVILEGES_MANAGER = new RolePrivilegesManager();
    // These are the actions that we know about. The privilege to perform these actions can be enabled/disabled per separately defined role.
    /** Get a file action. */
    public static final ServerAction GET = new ServerAction("Get file", false);
    /** Get a directory action. */
    public static final ServerAction GET_DIRECTORY = new ServerAction("Get directory", false);
    /** Show the cemetery directory action. */
    public static final ServerAction SHOW_CEMETERY = new ServerAction("Show cemetery", false);
    /** Show branch archives directory action. */
    public static final ServerAction SHOW_BRANCH_ARCHIVES_DIRECTORY = new ServerAction("Show branch archives directory", false);
    /** Check out a file revision action. */
    public static final ServerAction CHECK_OUT = new ServerAction("Check out", true);
    /** Check in a file revision action. */
    public static final ServerAction CHECK_IN = new ServerAction("Check in", true);
    /** Lock a file revision action. */
    public static final ServerAction LOCK = new ServerAction("Lock", true);
    /** Unlock a file revision action. */
    public static final ServerAction UNLOCK = new ServerAction("Unlock", true);
    /** Break a revision lock action. */
    public static final ServerAction BREAK_LOCK = new ServerAction("Break lock", true);
    /** Label a file action. */
    public static final ServerAction LABEL = new ServerAction("Label", true);
    /** Label the files in a directory action. */
    public static final ServerAction LABEL_DIRECTORY = new ServerAction("Label directory", true);
    /** Apply a label at checkin time action. */
    public static final ServerAction LABEL_AT_CHECKIN = new ServerAction("Label at checkin", true);
    /** Remove a label action. */
    public static final ServerAction REMOVE_LABEL = new ServerAction("Remove label", true);
    /** Remove a label from the files in a directory action. */
    public static final ServerAction REMOVE_LABEL_DIRECTORY = new ServerAction("Remove label from directory", true);
    /** Rename a file action. */
    public static final ServerAction RENAME_FILE = new ServerAction("Rename file", true);
    /** Move a file action. */
    public static final ServerAction MOVE_FILE = new ServerAction("Move file", true);
    /** Delete a file action. */
    public static final ServerAction SET_OBSOLETE = new ServerAction("Delete file", true);
    /** Set QVCS archive attributes action. */
    public static final ServerAction SET_ATTRIBUTES = new ServerAction("Set file attributes", true);
    /** Set the comment prefix for an archive file action. */
    public static final ServerAction SET_COMMENT_PREFIX = new ServerAction("Set comment prefix", true);
    /** Set the module description action. */
    public static final ServerAction SET_MODULE_DESCRIPTION = new ServerAction("Set file description", true);
    /** Set a revision description action. */
    public static final ServerAction SET_REVISION_DESCRIPTION = new ServerAction("Set revision description", true);
    /** Create a new QVCS archive action (put a file under source control). */
    public static final ServerAction CREATE_ARCHIVE = new ServerAction("Create archive", true);
    /** Add a directory action. */
    public static final ServerAction ADD_DIRECTORY = new ServerAction("Add directory", true);
    /** Merge changes from parent to branch action. */
    public static final ServerAction MERGE_FROM_PARENT = new ServerAction("Merge from parent", true);
    /** Promote to parent action. */
    public static final ServerAction PROMOTE_TO_PARENT = new ServerAction("Promote to parent", true);
    /** Delete a directory action. */
    public static final ServerAction DELETE_DIRECTORY = new ServerAction("Delete directory", true);
    /** Maintain a view's properties action. */
    public static final ServerAction SERVER_MAINTAIN_VIEW = new ServerAction("Maintain view", true);
    /** Add a user role action. */
    public static final ServerAction ADD_USER_ROLE = new ServerAction("(Admin tool): Add user role", true);
    /** Remove a user role action. */
    public static final ServerAction REMOVE_USER_ROLE = new ServerAction("(Admin tool): Remove user role", true);
    /** Assign user roles action. */
    public static final ServerAction ASSIGN_USER_ROLES = new ServerAction("(Admin tool): Assign user roles", true);
    /** List the users of a project action. */
    public static final ServerAction LIST_PROJECT_USERS = new ServerAction("(Admin tool): List project users", false);
    /** List user roles action. */
    public static final ServerAction LIST_USER_ROLES = new ServerAction("(Admin tool): List user roles", false);
    /** Maintain a project's properties action. */
    public static final ServerAction SERVER_MAINTAIN_PROJECT = new ServerAction("(Admin tool): Maintain project", true);
    private boolean isInitializedFlag = false;
    private String storeName = null;
    private String storeNameOld = null;
    private RolePrivilegesStore rolePrivilegesStore = null;

    /**
     * Creates a new instance of RolePrivilegesManager.
     */
    private RolePrivilegesManager() {
    }

    /**
     * Initialize the role privileges manager.
     * @return true if initialization was successful; false otherwise.
     */
    public synchronized boolean initialize() {
        if (!isInitializedFlag) {
            storeName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_ROLE_PRIVILEGES_STORE_NAME + "dat";

            storeNameOld = storeName + ".old";

            loadRoleStore();
            isInitializedFlag = true;
        }
        return isInitializedFlag;
    }

    /**
     * Get the singleton instance of the role privileges manager.
     * @return the singleton instance of the role privileges manager.
     */
    public static RolePrivilegesManager getInstance() {
        return ROLE_PRIVILEGES_MANAGER;
    }

    private synchronized void loadRoleStore() {
        FileInputStream fileStream = null;

        try {
            File storeFile = new File(storeName);
            fileStream = new FileInputStream(storeFile);
            ObjectInputStream inStream = new ObjectInputStream(fileStream);
            rolePrivilegesStore = (RolePrivilegesStore) inStream.readObject();
            rolePrivilegesStore.createAdminPrivileges();
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            rolePrivilegesStore = new RolePrivilegesStore();
            rolePrivilegesStore.createDefaultPrivileges();
            writeStore();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Failed to read role privileges store: [{}]", e.getLocalizedMessage());

            if (fileStream != null) {
                try {
                    fileStream.close();
                    fileStream = null;
                } catch (IOException ex) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }

            // Serialization failed.  Create a default store.
            rolePrivilegesStore = new RolePrivilegesStore();
            rolePrivilegesStore.createDefaultPrivileges();
            LOGGER.info("Creating default role privileges store.");
            writeStore();
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Write the role privileges store to disk.
     */
    public synchronized void writeStore() {
        FileOutputStream fileStream = null;

        try {
            File storeFile = new File(storeName);
            File oldStoreFile = new File(storeNameOld);

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
            outStream.writeObject(rolePrivilegesStore);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Does the user have the privileges needed to perform the requested action.
     * @param projectName the project name.
     * @param userName the QVCS user name.
     * @param action the action they wish to perform.
     * @return true if the user is allowed to perform the requested operation; false if not allowed.
     */
    public synchronized boolean isUserPrivileged(String projectName, String userName, ServerAction action) {
        String[] userRoles = RoleManager.getRoleManager().listUserRoles(projectName, userName);
        boolean returnValue = false;
        for (String userRole : userRoles) {
            returnValue = rolePrivilegesStore.isRolePrivileged(userRole, action.getAction());
            if (returnValue) {
                break;
            }
        }
        return returnValue;
    }

    /**
     * Get the list of available roles.
     * @return the list of available roles.
     */
    public synchronized String[] getAvailableRoles() {
        return rolePrivilegesStore.getAvailableRoles();
    }

    /**
     * For a given String, return the associated RoleType instance.
     * @param roleType role type as a String.
     * @return the associated RoleType instance.
     */
    public synchronized RoleType getRoleType(String roleType) {
        return rolePrivilegesStore.getRoleType(roleType);
    }

    /**
     * Get the list of role privileges.
     * @return the list of role privileges.
     */
    public synchronized String[] getRolePrivilegesList() {
        return rolePrivilegesStore.getRolePrivilegesList();
    }

    /**
     * Get the role privileges flags for a given role.
     * @param role the role.
     * @return the role privileges flags for a given role.
     */
    public synchronized Boolean[] getRolePrivilegesFlags(String role) {
        return rolePrivilegesStore.getRolePrivilegesFlags(role);
    }

    /**
     * Update the privileges flags for a given role.
     * @param role the role.
     * @param privileges the role privileges.
     * @param privilegesFlags the role privileges flags.
     */
    public synchronized void updatePrivileges(final String role, final String[] privileges, final Boolean[] privilegesFlags) {
        rolePrivilegesStore.updatePrivileges(role, privileges, privilegesFlags);
        writeStore();
    }

    /**
     * Delete a role.
     * @param role the role to delete.
     */
    public synchronized void deleteRole(final String role) {
        rolePrivilegesStore.deleteRole(role);
        writeStore();
    }
}
