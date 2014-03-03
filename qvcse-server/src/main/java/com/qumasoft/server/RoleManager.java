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
import com.qumasoft.qvcslib.RoleType;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Role Manager. This is a singleton.
 * @author Jim Voris
 */
public final class RoleManager implements RoleManagerInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private static final RoleManager ROLE_MANAGER = new RoleManager();
    private boolean isInitializedFlag = false;
    private String roleStoreName = null;
    private RoleStore roleStore = null;
    private String roleProjectViewStoreName = null;
    private String roleProjectViewStoreNameOld = null;
    private RoleProjectViewStore roleProjectViewStore = null;

    /**
     * Creates a new instance of the RoleManager.
     */
    private RoleManager() {
    }

    /**
     * Get the role manager singleton.
     * @return the role manager singleton.
     */
    public static RoleManager getRoleManager() {
        return ROLE_MANAGER;
    }

    @Override
    public synchronized boolean initialize() {
        if (!isInitializedFlag) {
            roleStoreName = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_ROLE_STORE_NAME + "dat";

            roleProjectViewStoreName =
                    System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_ROLE_PROJECT_VIEW_STORE_NAME + "dat";

            roleProjectViewStoreNameOld = roleProjectViewStoreName + ".old";

            loadRoleStore();
            isInitializedFlag = true;
        }
        return isInitializedFlag;
    }

    private void loadRoleStore() {
        File roleStoreFile;
        FileInputStream fileStream = null;

        try {
            roleStoreFile = new File(roleStoreName);
            if (roleStoreFile.exists()) {
                populateRoleProjectViewStoreFromRoleStore();
            } else {
                roleStoreFile = new File(roleProjectViewStoreName);
                fileStream = new FileInputStream(roleStoreFile);
                ObjectInputStream inStream = new ObjectInputStream(fileStream);
                roleProjectViewStore = (RoleProjectViewStore) inStream.readObject();
            }
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            roleProjectViewStore = new RoleProjectViewStore();
            writeRoleStore();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Failed to read role store: " + e.getLocalizedMessage());

            if (fileStream != null) {
                try {
                    fileStream.close();
                    fileStream = null;
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(ex));
                }
            }

            // Serialization failed.  Create a default store.
            roleProjectViewStore = new RoleProjectViewStore();
            LOGGER.log(Level.INFO, "Creating default role store.");
            writeRoleStore();
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
            roleProjectViewStore.dumpMaps();
        }
    }

    @Override
    public synchronized void writeRoleStore() {
        FileOutputStream fileStream = null;

        try {
            File storeFile = new File(roleProjectViewStoreName);
            File oldStoreFile = new File(roleProjectViewStoreNameOld);

            if (oldStoreFile.exists()) {
                oldStoreFile.delete();
            }

            if (storeFile.exists()) {
                storeFile.renameTo(oldStoreFile);
            }

            File newStoreFile = new File(roleProjectViewStoreName);

            // Make sure the needed directories exists
            if (!newStoreFile.getParentFile().exists()) {
                newStoreFile.getParentFile().mkdirs();
            }

            fileStream = new FileOutputStream(newStoreFile);
            ObjectOutputStream outStream = new ObjectOutputStream(fileStream);
            outStream.writeObject(roleProjectViewStore);
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

    @Override
    public synchronized boolean addUserRole(String callerUserName, String projectName, String userName, RoleType role) {
        boolean retVal = false;

        if (initialize()) {
            retVal = roleProjectViewStore.addProjectViewUser(callerUserName, projectName, userName, role);
        }

        if (retVal) {
            writeRoleStore();
        }

        return retVal;
    }

    @Override
    public synchronized boolean removeUserRole(String callerUserName, String projectName, String userName, RoleType role) {
        boolean retVal = false;

        if (initialize()) {
            retVal = roleProjectViewStore.removeProjectViewUser(callerUserName, projectName, userName, role);
        }

        if (retVal) {
            writeRoleStore();
        }

        return retVal;
    }

    /**
     * Get the list of project users.
     * @param projectName the project name.
     * @return the list of users for the given project.
     */
    public synchronized String[] listProjectUsers(String projectName) {
        String[] projectUsers = null;
        if (initialize()) {
            projectUsers = roleProjectViewStore.listProjectUsers(projectName);
        }
        return projectUsers;
    }

    /**
     * List user roles for a given project.
     * @param projectName the project name.
     * @param userName the user name.
     * @return a list of the users roles for the given project.
     */
    public synchronized String[] listUserRoles(String projectName, String userName) {
        String[] userRoles = null;
        if (initialize()) {
            userRoles = roleProjectViewStore.listUserRoles(projectName, userName);
        }
        return userRoles;
    }

    @Override
    public synchronized boolean removeAllUserRoles(String callerUserName, String projectName, String userName) {
        boolean retVal = false;

        String[] userRoles = listUserRoles(projectName, userName);
        if (userRoles != null) {
            retVal = true;
            for (int i = 0; i < userRoles.length && retVal; i++) {
                retVal = removeUserRole(callerUserName, projectName, userName, getRoleType(userRoles[i]));
            }
        }

        return retVal;
    }

    @Override
    public synchronized boolean removeAllUserRolesInAllProjects(String callerUserName, String userName) {
        boolean retVal = false;

        if (initialize()) {
            String[] projectList = roleProjectViewStore.getProjectList();

            if (projectList != null) {
                retVal = true;
                for (int i = 0; (i < projectList.length) && retVal; i++) {
                    retVal = removeAllUserRoles(callerUserName, projectList[i], userName);
                }
            }
        }
        return retVal;
    }

    /**
     * Use this method when deleting a project from the server. This method will remove all user roles associated with that project.
     *
     * @param projectName the project name.
     * @param callerUserName the caller user name.
     * @return true if things work okay.
     */
    public synchronized boolean removeAllProjectRoles(String projectName, String callerUserName) {
        boolean retVal = true;

        String[] projectUsers = listProjectUsers(projectName);

        for (int i = 0; (i < projectUsers.length) && retVal; i++) {
            retVal = removeAllUserRoles(callerUserName, projectName, projectUsers[i]);
        }
        return retVal;
    }

    @Override
    public RoleType getRoleType(String roleType) {
        return RolePrivilegesManager.getInstance().getRoleType(roleType);
    }

    String[] getAvailableRoles() {
        return RolePrivilegesManager.getInstance().getAvailableRoles();
    }

    @Override
    public synchronized boolean assignUserRoles(String callerUserName, String projectName, String userName, String[] roles) {
        boolean retVal = false;

        // Remove any existing roles
        if (removeAllUserRoles(callerUserName, projectName, userName)) {
            retVal = true;
            for (int i = 0; (i < roles.length) && retVal; i++) {
                retVal = addUserRole(callerUserName, projectName, userName, getRoleType(roles[i]));
            }
        }

        return retVal;
    }

    /**
     * Delete a role.
     * @param role the role to delete.
     */
    public synchronized void deleteRole(final String role) {
        if (0 != role.compareTo(ADMIN)) {
            RolePrivilegesManager.getInstance().deleteRole(role);
            roleProjectViewStore.deleteRole(role);
        }
    }

    private void populateRoleProjectViewStoreFromRoleStore() throws IOException, ClassNotFoundException {
        File roleStoreFile = null;
        FileInputStream fileStream = null;
        try {
            roleStoreFile = new File(roleStoreName);
            fileStream = new FileInputStream(roleStoreFile);
            ObjectInputStream inStream = new ObjectInputStream(fileStream);
            roleStore = (RoleStore) inStream.readObject();
            roleProjectViewStore = new RoleProjectViewStore();

            Set projectKeys = roleStore.getProjectUserMapKeySet();
            Iterator j = projectKeys.iterator();
            while (j.hasNext()) {
                String projectName = (String) j.next();
                LOGGER.log(Level.INFO, projectName);

                Map projectUserMap = roleStore.getProjectUserMap(projectName);
                Set userKeys = projectUserMap.keySet();
                Iterator k = userKeys.iterator();
                while (k.hasNext()) {
                    String userAndRole = (String) k.next();
                    int separatorIndex = userAndRole.lastIndexOf('.');
                    String userRole = userAndRole.substring(1 + separatorIndex);
                    String userName = userAndRole.substring(0, separatorIndex);
                    roleProjectViewStore.addProjectViewUser(ADMIN, projectName, userName, getRoleType(userRole));
                    LOGGER.log(Level.INFO, "Converting project: " + projectName + " user: " + userName + " role: " + userRole);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            throw e;
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }

                // We don't need the old role store anymore
                if (roleStoreFile != null && roleStoreFile.exists()) {
                    roleStoreFile.delete();
                }
            }
            writeRoleStore();
            roleProjectViewStore.dumpMaps();
        }
    }
}
