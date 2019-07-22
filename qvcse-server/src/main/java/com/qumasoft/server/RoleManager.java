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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RoleType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Role Manager. This is a singleton.
 * @author Jim Voris
 */
public final class RoleManager implements RoleManagerInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleManager.class);
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
                    + QVCSConstants.QVCS_ROLE_PROJECT_BRANCH_STORE_NAME + "dat";

            roleProjectViewStoreNameOld = roleProjectViewStoreName + ".old";

            loadRoleStore();
            isInitializedFlag = true;
        }
        return isInitializedFlag;
    }

    private void loadRoleStore() {
        File roleStoreFile;

        try {
            roleStoreFile = new File(roleStoreName);
            if (roleStoreFile.exists()) {
                populateRoleProjectViewStoreFromRoleStore();
            } else {
                roleStoreFile = new File(roleProjectViewStoreName);

                // Use try with resources so we're guaranteed the File input stream is closed.
                try (FileInputStream fileInputStream = new FileInputStream(roleStoreFile)) {

                    // Use try with resources so we're guaranteed the object input stream is closed.
                    try (ObjectInputStream inStream = new ObjectInputStream(fileInputStream)) {
                        roleProjectViewStore = (RoleProjectViewStore) inStream.readObject();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            roleProjectViewStore = new RoleProjectViewStore();
            writeRoleStore();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Failed to read role store: [{}]", e.getLocalizedMessage());

            // Serialization failed.  Create a default store.
            roleProjectViewStore = new RoleProjectViewStore();
            LOGGER.info("Creating default role store.");
            writeRoleStore();
        } finally {
            roleProjectViewStore.dumpMaps();
        }
    }

    @Override
    public synchronized void writeRoleStore() {

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

            // Use try with resources so we're guaranteed the file output stream is closed.
            try (FileOutputStream fileOutputStream = new FileOutputStream(newStoreFile)) {

                // Use try with resources so we're guaranteed the object output stream is closed.
                try (ObjectOutputStream outStream = new ObjectOutputStream(fileOutputStream)) {
                    outStream.writeObject(roleProjectViewStore);
                }
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
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

        if (projectUsers != null) {
            for (int i = 0; (i < projectUsers.length) && retVal; i++) {
                retVal = removeAllUserRoles(callerUserName, projectName, projectUsers[i]);
            }
        }
        return retVal;
    }

    @Override
    public RoleType getRoleType(String roleType) {
        return RolePrivilegesManager.getInstance().getRoleType(roleType);
    }

    /**
     * Get the list of available roles.
     * @return the list of available roles.
     */
    public String[] getAvailableRoles() {
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
        FileInputStream fileInputStream = null;
        try {
            roleStoreFile = new File(roleStoreName);
            fileInputStream = new FileInputStream(roleStoreFile);

            // Use try with resources so we're guaranteed the file output stream is closed.
            try (ObjectInputStream inStream = new ObjectInputStream(fileInputStream)) {
                roleStore = (RoleStore) inStream.readObject();
            }
            roleProjectViewStore = new RoleProjectViewStore();

            Set projectKeys = roleStore.getProjectUserMapKeySet();
            Iterator j = projectKeys.iterator();
            while (j.hasNext()) {
                String projectName = (String) j.next();
                LOGGER.info(projectName);

                Map projectUserMap = roleStore.getProjectUserMap(projectName);
                Set userKeys = projectUserMap.keySet();
                Iterator k = userKeys.iterator();
                while (k.hasNext()) {
                    String userAndRole = (String) k.next();
                    int separatorIndex = userAndRole.lastIndexOf('.');
                    String userRole = userAndRole.substring(1 + separatorIndex);
                    String userName = userAndRole.substring(0, separatorIndex);
                    roleProjectViewStore.addProjectViewUser(ADMIN, projectName, userName, getRoleType(userRole));
                    LOGGER.info("Converting project: [{}] user: [{}] role: [{}]", projectName, userName, userRole);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw e;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
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
