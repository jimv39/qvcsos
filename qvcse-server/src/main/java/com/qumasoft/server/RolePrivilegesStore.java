/*   Copyright 2004-2015 Jim Voris
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

import com.qumasoft.qvcslib.RoleType;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Role privileges store.
 * @author Jim Voris
 */
public class RolePrivilegesStore implements java.io.Serializable {
    private static final long serialVersionUID = -4735485968226473670L;

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(RolePrivilegesStore.class);
    /**
     * This root map contains the users who are 'super' users
     */
    private final Map<String, Map<String, Boolean>> privilegesMap = Collections.synchronizedMap(new TreeMap<String, Map<String, Boolean>>());
    /**
     * This map holds the RoleType objects that we know about. We populate it in a lazy way.
     */
    private transient Map<String, RoleType> roleTypeMap;

    /**
     * Creates a new instance of RolePrivilegesStore.
     */
    public RolePrivilegesStore() {
    }

    /**
     * Create the default privileges.
     */
    synchronized void createDefaultPrivileges() {
        // Create the READER privileges
        createReaderPrivileges();

        // Create the WRITER privileges
        createWriterPrivileges();

        // Create the PROJECT_ADMIN privileges
        createProjectAdminPrivileges();

        // Create the CEMETERY_ADMIN privileges
        createCemeteryAdminPrivileges();

        // Create the ADMIN privileges
        createAdminPrivileges();

        // Create the DEVELOPER privileges
        createDeveloperPrivileges();
    }

    private void createReaderPrivileges() {
        Map<String, Boolean> readerPrivileges = new TreeMap<>();
        readerPrivileges.put(RolePrivilegesManager.GET.getAction(), Boolean.TRUE);
        readerPrivileges.put(RolePrivilegesManager.GET_DIRECTORY.getAction(), Boolean.TRUE);
        readerPrivileges.put(RolePrivilegesManager.SHOW_CEMETERY.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.SHOW_BRANCH_ARCHIVES_DIRECTORY.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.CHECK_OUT.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.CHECK_IN.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.LOCK.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.UNLOCK.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.BREAK_LOCK.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.LABEL.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.LABEL_DIRECTORY.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.LABEL_AT_CHECKIN.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.REMOVE_LABEL.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.REMOVE_LABEL_DIRECTORY.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.RENAME_FILE.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.MOVE_FILE.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.SET_OBSOLETE.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.SET_ATTRIBUTES.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.SET_COMMENT_PREFIX.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.SET_MODULE_DESCRIPTION.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.SET_REVISION_DESCRIPTION.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.CREATE_ARCHIVE.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.ADD_DIRECTORY.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.DELETE_DIRECTORY.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.MERGE_FROM_PARENT.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.PROMOTE_TO_PARENT.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.ADD_USER_ROLE.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.REMOVE_USER_ROLE.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.ASSIGN_USER_ROLES.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.LIST_PROJECT_USERS.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.LIST_USER_ROLES.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_PROJECT.getAction(), Boolean.FALSE);
        readerPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction(), Boolean.FALSE);
        privilegesMap.put(RoleManagerInterface.READER, readerPrivileges);
    }

    private void createWriterPrivileges() {
        Map<String, Boolean> writerPrivileges = new TreeMap<>();
        writerPrivileges.put(RolePrivilegesManager.GET.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.GET_DIRECTORY.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.SHOW_CEMETERY.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.SHOW_BRANCH_ARCHIVES_DIRECTORY.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.CHECK_OUT.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.CHECK_IN.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.LOCK.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.UNLOCK.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.BREAK_LOCK.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.LABEL.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.LABEL_DIRECTORY.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.LABEL_AT_CHECKIN.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.REMOVE_LABEL.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.REMOVE_LABEL_DIRECTORY.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.RENAME_FILE.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.MOVE_FILE.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.SET_OBSOLETE.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.SET_ATTRIBUTES.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.SET_COMMENT_PREFIX.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.SET_MODULE_DESCRIPTION.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.SET_REVISION_DESCRIPTION.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.CREATE_ARCHIVE.getAction(), Boolean.TRUE);
        writerPrivileges.put(RolePrivilegesManager.ADD_DIRECTORY.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.DELETE_DIRECTORY.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.MERGE_FROM_PARENT.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.PROMOTE_TO_PARENT.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.ADD_USER_ROLE.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.REMOVE_USER_ROLE.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.ASSIGN_USER_ROLES.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.LIST_PROJECT_USERS.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.LIST_USER_ROLES.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_PROJECT.getAction(), Boolean.FALSE);
        writerPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction(), Boolean.FALSE);
        privilegesMap.put(RoleManagerInterface.WRITER, writerPrivileges);
    }

    private void createProjectAdminPrivileges() {
        Map<String, Boolean> projectAdminPrivileges = new TreeMap<>();
        projectAdminPrivileges.put(RolePrivilegesManager.GET.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.GET_DIRECTORY.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.SHOW_CEMETERY.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.SHOW_BRANCH_ARCHIVES_DIRECTORY.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.CHECK_OUT.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.CHECK_IN.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.LOCK.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.UNLOCK.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.BREAK_LOCK.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.LABEL.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.LABEL_DIRECTORY.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.LABEL_AT_CHECKIN.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.REMOVE_LABEL.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.REMOVE_LABEL_DIRECTORY.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.RENAME_FILE.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.MOVE_FILE.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.SET_OBSOLETE.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.SET_ATTRIBUTES.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.SET_COMMENT_PREFIX.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.SET_MODULE_DESCRIPTION.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.SET_REVISION_DESCRIPTION.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.CREATE_ARCHIVE.getAction(), Boolean.FALSE);
        projectAdminPrivileges.put(RolePrivilegesManager.ADD_DIRECTORY.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.DELETE_DIRECTORY.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.MERGE_FROM_PARENT.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.PROMOTE_TO_PARENT.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.ADD_USER_ROLE.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.REMOVE_USER_ROLE.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.ASSIGN_USER_ROLES.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.LIST_PROJECT_USERS.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.LIST_USER_ROLES.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_PROJECT.getAction(), Boolean.TRUE);
        projectAdminPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction(), Boolean.TRUE);
        privilegesMap.put(RoleManagerInterface.PROJECT_ADMIN, projectAdminPrivileges);
    }

    private void createCemeteryAdminPrivileges() {
        Map<String, Boolean> cemeteryAdminPrivileges = new TreeMap<>();
        cemeteryAdminPrivileges.put(RolePrivilegesManager.GET.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.GET_DIRECTORY.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.SHOW_CEMETERY.getAction(), Boolean.TRUE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.SHOW_BRANCH_ARCHIVES_DIRECTORY.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.CHECK_OUT.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.CHECK_IN.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.LOCK.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.UNLOCK.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.BREAK_LOCK.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.LABEL.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.LABEL_DIRECTORY.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.LABEL_AT_CHECKIN.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.REMOVE_LABEL.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.REMOVE_LABEL_DIRECTORY.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.RENAME_FILE.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.MOVE_FILE.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.SET_OBSOLETE.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.SET_ATTRIBUTES.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.SET_COMMENT_PREFIX.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.SET_MODULE_DESCRIPTION.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.SET_REVISION_DESCRIPTION.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.CREATE_ARCHIVE.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.ADD_DIRECTORY.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.DELETE_DIRECTORY.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.MERGE_FROM_PARENT.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.PROMOTE_TO_PARENT.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.ADD_USER_ROLE.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.REMOVE_USER_ROLE.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.ASSIGN_USER_ROLES.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.LIST_PROJECT_USERS.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.LIST_USER_ROLES.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_PROJECT.getAction(), Boolean.FALSE);
        cemeteryAdminPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction(), Boolean.FALSE);
        privilegesMap.put(RoleManagerInterface.CEMETERY_ADMIN, cemeteryAdminPrivileges);
    }

    void createAdminPrivileges() {
        Map<String, Boolean> adminPrivileges = new TreeMap<>();
        adminPrivileges.put(RolePrivilegesManager.GET.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.GET_DIRECTORY.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.SHOW_CEMETERY.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.SHOW_BRANCH_ARCHIVES_DIRECTORY.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.CHECK_OUT.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.CHECK_IN.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.LOCK.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.UNLOCK.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.BREAK_LOCK.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.LABEL.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.LABEL_DIRECTORY.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.LABEL_AT_CHECKIN.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.REMOVE_LABEL.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.REMOVE_LABEL_DIRECTORY.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.RENAME_FILE.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.MOVE_FILE.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.SET_OBSOLETE.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.SET_ATTRIBUTES.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.SET_COMMENT_PREFIX.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.SET_MODULE_DESCRIPTION.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.SET_REVISION_DESCRIPTION.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.CREATE_ARCHIVE.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.ADD_DIRECTORY.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.DELETE_DIRECTORY.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.MERGE_FROM_PARENT.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.PROMOTE_TO_PARENT.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.ADD_USER_ROLE.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.REMOVE_USER_ROLE.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.ASSIGN_USER_ROLES.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.LIST_PROJECT_USERS.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.LIST_USER_ROLES.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_PROJECT.getAction(), Boolean.FALSE);
        adminPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction(), Boolean.FALSE);
        privilegesMap.put(RoleManagerInterface.ADMIN, adminPrivileges);
    }

    private void createDeveloperPrivileges() {
        Map<String, Boolean> developerPrivileges = new TreeMap<>();
        developerPrivileges.put(RolePrivilegesManager.GET.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.GET_DIRECTORY.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.SHOW_CEMETERY.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.SHOW_BRANCH_ARCHIVES_DIRECTORY.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.CHECK_OUT.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.CHECK_IN.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.LOCK.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.UNLOCK.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.BREAK_LOCK.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.LABEL.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.LABEL_DIRECTORY.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.LABEL_AT_CHECKIN.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.REMOVE_LABEL.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.REMOVE_LABEL_DIRECTORY.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.RENAME_FILE.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.MOVE_FILE.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.SET_OBSOLETE.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.SET_ATTRIBUTES.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.SET_COMMENT_PREFIX.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.SET_MODULE_DESCRIPTION.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.SET_REVISION_DESCRIPTION.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.CREATE_ARCHIVE.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.ADD_DIRECTORY.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.DELETE_DIRECTORY.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.MERGE_FROM_PARENT.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.PROMOTE_TO_PARENT.getAction(), Boolean.TRUE);
        developerPrivileges.put(RolePrivilegesManager.ADD_USER_ROLE.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.REMOVE_USER_ROLE.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.ASSIGN_USER_ROLES.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.LIST_PROJECT_USERS.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.LIST_USER_ROLES.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_PROJECT.getAction(), Boolean.FALSE);
        developerPrivileges.put(RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction(), Boolean.FALSE);
        privilegesMap.put(RoleManagerInterface.DEVELOPER, developerPrivileges);
    }

    synchronized boolean isRolePrivileged(final String roleName, final String actionName) {
        boolean returnValue = false;
        Map localPrivilegesMap = privilegesMap.get(roleName);
        if (localPrivilegesMap != null) {
            Boolean flag = (Boolean) localPrivilegesMap.get(actionName);
            if (flag != null) {
                returnValue = flag;
            }
        }
        return returnValue;
    }

    synchronized String[] getAvailableRoles() {
        String[] availableRoles = new String[privilegesMap.keySet().size() - 1];
        Iterator<String> it = privilegesMap.keySet().iterator();
        int i = 0;
        while (it.hasNext()) {
            String role = it.next();
            if (0 != role.compareTo(RoleManagerInterface.ADMIN)) {
                availableRoles[i++] = role;
            }
        }
        return availableRoles;
    }

    synchronized RoleType getRoleType(final String roleType) {
        RoleType returnedRoleType = null;
        if (privilegesMap.containsKey(roleType)) {
            if (roleTypeMap == null) {
                roleTypeMap = Collections.synchronizedMap(new TreeMap<String, RoleType>());
            }
            if (roleTypeMap.containsKey(roleType)) {
                returnedRoleType = roleTypeMap.get(roleType);
            } else {
                // We need to make a new RoleType object and put it in the map.
                RoleType newRoleType = new RoleType(roleType);
                roleTypeMap.put(roleType, newRoleType);
                returnedRoleType = newRoleType;
            }
        }
        return returnedRoleType;
    }

    synchronized String[] getRolePrivilegesList() {
        Map<String, Boolean> privileges = privilegesMap.get(RoleManagerInterface.ADMIN);
        String[] privilegesList = new String[privileges.size()];
        Iterator<String> privilegesIterator = privileges.keySet().iterator();
        int index = 0;
        while (privilegesIterator.hasNext()) {
            privilegesList[index++] = privilegesIterator.next();
        }
        return privilegesList;
    }

    synchronized Boolean[] getRolePrivilegesFlags(final String role) {
        Map<String, Boolean> allPrivileges = privilegesMap.get(RoleManagerInterface.ADMIN);
        Map<String, Boolean> rolePrivileges = privilegesMap.get(role);
        Boolean[] privilegesFlagList = new Boolean[allPrivileges.size()];
        Iterator<String> privilegesIterator = allPrivileges.keySet().iterator();
        int index = 0;
        while (privilegesIterator.hasNext()) {
            Boolean flag = rolePrivileges.get(privilegesIterator.next());
            if (flag != null) {
                privilegesFlagList[index++] = flag;
            } else {
                privilegesFlagList[index++] = Boolean.FALSE;
            }
        }
        return privilegesFlagList;
    }

    synchronized void updatePrivileges(final String role, final String[] privileges, final Boolean[] privilegesFlags) {
        // Only update non-ADMIN roles.
        if (0 != role.compareTo(RoleManagerInterface.ADMIN)) {
            if (privilegesMap.containsKey(role)) {
                // Delete the existing role
                privilegesMap.remove(role);
            }

            int size = privilegesFlags.length;
            Map<String, Boolean> rolePrivileges = new TreeMap<>();
            for (int i = 0; i < size; i++) {
                rolePrivileges.put(privileges[i], privilegesFlags[i]);
            }
            privilegesMap.put(role, rolePrivileges);
        }
    }

    synchronized void deleteRole(final String role) {
        // Only delete non-ADMIN roles.
        if (0 != role.compareTo(RoleManagerInterface.ADMIN)) {
            if (privilegesMap.containsKey(role)) {
                // Delete the role
                privilegesMap.remove(role);
            }
        }
    }

    synchronized void dumpMaps() {
        LOGGER.info("RolePrivilegesStore.dumpMaps()");
        Iterator<String> keyIterator = privilegesMap.keySet().iterator();
        while (keyIterator.hasNext()) {
            String key = keyIterator.next();
            LOGGER.info("Role: [{}]", key);
            Map<String, Boolean> privileges = privilegesMap.get(key);
            Iterator<Map.Entry<String, Boolean>> privilegesIterator = privileges.entrySet().iterator();
            while (privilegesIterator.hasNext()) {
                Map.Entry<String, Boolean> entry = privilegesIterator.next();
                String action = entry.getKey();
                Boolean flag = entry.getValue();
                LOGGER.info("\t[{}]: [{}]", action, flag.toString());
            }
        }
    }
}
