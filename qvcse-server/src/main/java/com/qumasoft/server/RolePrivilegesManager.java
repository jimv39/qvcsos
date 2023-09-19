/*   Copyright 2004-2022 Jim Voris
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

import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.PrivilegedActionDAO;
import com.qvcsos.server.dataaccess.RoleTypeActionJoinDAO;
import com.qvcsos.server.dataaccess.RoleTypeDAO;
import com.qvcsos.server.dataaccess.impl.PrivilegedActionDAOImpl;
import com.qvcsos.server.dataaccess.impl.RoleTypeActionJoinDAOImpl;
import com.qvcsos.server.dataaccess.impl.RoleTypeDAOImpl;
import com.qvcsos.server.datamodel.PrivilegedAction;
import com.qvcsos.server.datamodel.RoleType;
import com.qvcsos.server.datamodel.RoleTypeActionJoin;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
    /** Check in a file revision action. */
    public static final ServerAction CHECK_IN = new ServerAction("Check in", true);
    /** Rename a file action. */
    public static final ServerAction RENAME_FILE = new ServerAction("Rename file", true);
    /** Move a file action. */
    public static final ServerAction MOVE_FILE = new ServerAction("Move file", true);
    /** Delete a file action. */
    public static final ServerAction DELETE_FILE = new ServerAction("Delete file", true);
    /** Create a new QVCS archive action (put a file under source control). */
    public static final ServerAction ADD_FILE = new ServerAction("Add file", true);
    /** Add a directory action. */
    public static final ServerAction ADD_DIRECTORY = new ServerAction("Add directory", true);
    /** Merge changes from parent to branch action. */
    public static final ServerAction MERGE_FROM_PARENT = new ServerAction("Merge from parent", true);
    /** Promote to parent action. */
    public static final ServerAction PROMOTE_TO_PARENT = new ServerAction("Promote to parent", true);
    /** Delete provisional records action. */
    public static final ServerAction DELETE_PROVISIONAL_RECORDS = new ServerAction("Delete provisional records", true);
    /** Delete a directory action. */
    public static final ServerAction DELETE_DIRECTORY = new ServerAction("Delete directory", true);
    /** Maintain a branch's properties action. */
    public static final ServerAction SERVER_MAINTAIN_BRANCH = new ServerAction("Maintain branch", true);
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

    /**
     * A map of maps... keyed first to the role name; the 2nd contained maps are keyed by the action name, with a boolean value
     * to indicate whether that action is allowed for the given role.
     */
    private final Map<String, Map<String, Boolean>> privilegesMap = Collections.synchronizedMap(new TreeMap<>());
    private Map<Integer, PrivilegedAction> privilegedActionByIdMap;
    private Map<String, PrivilegedAction> privilegedActionByStringMap;

    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of RolePrivilegesManager.
     */
    private RolePrivilegesManager() {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
    }

    /**
     * Initialize the role privileges manager.
     * @return true if initialization was successful; false otherwise.
     */
    public synchronized boolean initialize() {
        if (!isInitializedFlag) {
            // Populate the maps of privileges...
            populatePrivilegesMaps();

            // Populate the map of maps...
            RoleTypeDAO roleTypeDAO = new RoleTypeDAOImpl(schemaName);
            List<RoleType> roleTypeList = roleTypeDAO.findAll();
            for (RoleType rt : roleTypeList) {
                Map<String, Boolean> privilegeMapForRole = populatePrivilegeMapForRole(rt);
                privilegesMap.put(rt.getRoleName(), privilegeMapForRole);
            }
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

    /**
     * Does the user have the privileges needed to perform the requested action.
     * @param projectName the project name.
     * @param userName the QVCS user name.
     * @param action the action they wish to perform.
     * @return true if the user is allowed to perform the requested operation; false if not allowed.
     */
    public synchronized boolean isUserPrivileged(String projectName, String userName, ServerAction action) {
        String[] userRoles = RoleManager.getRoleManager().listUserRoles(projectName, userName);
        Boolean returnValue = false;
        for (String userRole : userRoles) {
            Map<String, Boolean> privilegeMapForRole = privilegesMap.get(userRole);
            returnValue = privilegeMapForRole.get(action.getAction());
            if ((returnValue != null) && returnValue) {
                break;
            } else {
                returnValue = false;
            }
        }
        return returnValue;
    }

    /**
     * Get the list of role privileges.
     * @return the list of role privileges.
     */
    public synchronized String[] getRolePrivilegesList() {
        String[] privilegesList = new String[privilegedActionByIdMap.size()];
        int index = 0;
        for (PrivilegedAction pa : privilegedActionByIdMap.values()) {
            privilegesList[index++] = pa.getActionName();
        }
        return privilegesList;
    }

    /**
     * Get the role privileges flags for a given role.
     * @param role the role.
     * @return the role privileges flags for a given role.
     */
    public synchronized Boolean[] getRolePrivilegesFlags(String role) {
        Boolean[] privilegesFlagList = new Boolean[privilegedActionByIdMap.size()];
        Map<String, Boolean> flagMap = privilegesMap.get(role);
        int index = 0;
        // Make sure to go through the flag list in the same order as the list of privileges.
        for (PrivilegedAction pa : privilegedActionByIdMap.values()) {
            Boolean flag = flagMap.get(pa.getActionName());
            privilegesFlagList[index++] = flag;
        }
        return privilegesFlagList;
    }

    /**
     * Update the privileges flags for a given role.
     * @param role the role.
     * @param privileges the role privileges.
     * @param privilegesFlags the role privileges flags.
     * @throws java.sql.SQLException
     */
    public synchronized void updatePrivileges(final String role, final String[] privileges, final Boolean[] privilegesFlags) throws SQLException {
        Map<String, Boolean> flagMap = privilegesMap.get(role);
        if (flagMap == null) {
            flagMap = new TreeMap<>();
            privilegesMap.put(role, flagMap);
        }
        // Update what's in memory...
        for (int index = 0; index < privileges.length; index++) {
            flagMap.put(privileges[index], privilegesFlags[index]);
        }

        // Update the database...
        RoleTypeDAO roleTypeDAO = new RoleTypeDAOImpl(schemaName);
        RoleType roleType = roleTypeDAO.findByRoleName(role);
        if (roleType == null) {
            roleType = new RoleType();
            roleType.setRoleName(role);
            Integer roleTypeId = roleTypeDAO.insert(roleType);
            roleType.setId(roleTypeId);
        }
        RoleTypeActionJoinDAO roleTypeActionJoinDAO = new RoleTypeActionJoinDAOImpl(schemaName);
        List<RoleTypeActionJoin> roleTypeActionList = roleTypeActionJoinDAO.findByRoleType(roleType.getId());
        for (RoleTypeActionJoin rtaj : roleTypeActionList) {
            try {
                PrivilegedAction action = privilegedActionByIdMap.get(rtaj.getActionId());
                Boolean flag = flagMap.get(action.getActionName());
                rtaj.setActionEnabledFlag(flag);
                roleTypeActionJoinDAO.update(rtaj);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void populatePrivilegesMaps() {
        PrivilegedActionDAO privilegedActionDAO = new PrivilegedActionDAOImpl(schemaName);
        privilegedActionByIdMap = new HashMap<>();
        privilegedActionByStringMap = new TreeMap<>();
        List<PrivilegedAction> paList = privilegedActionDAO.findAll();
        for (PrivilegedAction pa : paList) {
            privilegedActionByIdMap.put(pa.getId(), pa);
            privilegedActionByStringMap.put(pa.getActionName(), pa);
        }
    }

    private Map<String, Boolean> populatePrivilegeMapForRole(RoleType rt) {
        RoleTypeActionJoinDAO roleTypeActionJoinDAO = new RoleTypeActionJoinDAOImpl(schemaName);
        List<RoleTypeActionJoin> rtActionJoinList = roleTypeActionJoinDAO.findByRoleType(rt.getId());
        Map<String, Boolean> privilegeMapForRole = new TreeMap<>();
        for (RoleTypeActionJoin rtActionJoin : rtActionJoinList) {
            PrivilegedAction pa = privilegedActionByIdMap.get(rtActionJoin.getActionId());
            privilegeMapForRole.put(pa.getActionName(), rtActionJoin.getActionEnabledFlag());
        }
        return privilegeMapForRole;
    }
}
