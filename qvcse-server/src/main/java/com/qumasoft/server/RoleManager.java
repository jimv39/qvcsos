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

import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.RoleTypeDAO;
import com.qvcsos.server.dataaccess.UserDAO;
import com.qvcsos.server.dataaccess.UserProjectRoleDAO;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.dataaccess.impl.RoleTypeDAOImpl;
import com.qvcsos.server.dataaccess.impl.UserDAOImpl;
import com.qvcsos.server.dataaccess.impl.UserProjectRoleDAOImpl;
import com.qvcsos.server.datamodel.Project;
import com.qvcsos.server.datamodel.RoleType;
import com.qvcsos.server.datamodel.User;
import com.qvcsos.server.datamodel.UserProjectRole;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    // <editor-fold>
    /** This is like the root role. Can admin other admins. */
    public RoleType ADMIN_ROLE;
    /** This is a role that can administer a given project. */
    public RoleType PROJECT_ADMIN_ROLE;
    /** This is a role that can read the QVCS archives for a project. */
    public RoleType READER_ROLE;
    /** This is a role that can update the QVCS archives for a project. */
    public RoleType WRITER_ROLE;
    /** A sample DEVELOPER role. */
    public RoleType DEVELOPER_ROLE;
    /** A sample CEMETERY ADMIN role. */
    public RoleType CEMETERY_ADMIN_ROLE;
    // </editor-fold>

    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of the RoleManager.
     */
    private RoleManager() {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
    }

    /**
     * Get the role manager singleton.
     * @return the role manager singleton.
     */
    public static RoleManager getRoleManager() {
        return ROLE_MANAGER;
    }

    /**
     * Add a role for the given user for the given project.
     * @param callerUserName the user requesting this admin type operation. We assume that the privilege check has already been done.
     * @param projectName the project name.
     * @param userName the user name.
     * @param role the role being given to the user for the project.
     * @return true if successful; false otherwise.
     */
    @Override
    public synchronized boolean addUserRole(String callerUserName, String projectName, String userName, RoleType role) {
        boolean retVal = false;
        UserDAO userDAO = new UserDAOImpl(schemaName);
        User user = userDAO.findByUserName(userName);

        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(projectName);

        if (user != null && project != null) {
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                UserProjectRoleDAO userProjectRoleDAO = new UserProjectRoleDAOImpl(schemaName);
                UserProjectRole userProjectRole = new UserProjectRole();
                userProjectRole.setProjectId(project.getId());
                userProjectRole.setUserId(user.getId());
                userProjectRole.setRoleTypeId(role.getId());
                Integer newId = userProjectRoleDAO.insert(userProjectRole);
                LOGGER.info("Inserted new role [{}] with id: [{}]", role.getRoleName(), newId);
                if (!connection.getAutoCommit()) {
                    connection.commit();
                }
                retVal = true;
            } catch (SQLException e) {
                LOGGER.warn("Failed insert: ", e);
            }
        } else {
            LOGGER.warn("Invalid user, project in addUserRole: [{}], [{}]", userName, projectName);
        }
        return retVal;
    }

    @Override
    public synchronized boolean removeUserRole(String callerUserName, String projectName, String userName, RoleType role) {
        boolean retVal = false;
        UserDAO userDAO = new UserDAOImpl(schemaName);
        User user = userDAO.findByUserName(userName);

        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(projectName);

        RoleTypeDAO roleTypeDAO = new RoleTypeDAOImpl(schemaName);
        RoleType roleType = roleTypeDAO.findByRoleName(role.getRoleName());
        if (user != null && project != null && roleType != null) {
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                UserProjectRoleDAO userProjectRoleDAO = new UserProjectRoleDAOImpl(schemaName);
                UserProjectRole userProjectRole = userProjectRoleDAO.findByUserProjectAndRoleType(user.getId(), project.getId(), roleType.getId());
                if (userProjectRole != null) {
                    retVal = userProjectRoleDAO.delete(userProjectRole.getId());
                    connection.commit();
                }
            } catch (SQLException e) {
                LOGGER.warn("Failed delete: ", e);
            }
        } else {
            LOGGER.warn("Invalid user, project, or roleType in removeUserRole: [{}], [{}], [{}]", userName, projectName, role.getRoleName());
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
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(projectName);

            UserProjectRoleDAO userProjectRoleDAO = new UserProjectRoleDAOImpl(schemaName);
            List<UserProjectRole> userProjectRoleList = userProjectRoleDAO.findByProject(project.getId());

            Set<Integer> userIdSet = new HashSet<>();
            for (UserProjectRole userProjectRole : userProjectRoleList) {
                userIdSet.add(userProjectRole.getUserId());
            }

            UserDAO userDAO = new UserDAOImpl(schemaName);
            projectUsers = new String[userIdSet.size()];
            int index = 0;
            for (Integer userId : userIdSet) {
                User user = userDAO.findById(userId);
                projectUsers[index++] = user.getUserName();
            }
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
        String[] userRoles = {};
        if (initialize()) {
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(projectName);

            UserDAO userDAO = new UserDAOImpl(schemaName);
            User user = userDAO.findByUserName(userName);

            if (user != null && project != null) {
                UserProjectRoleDAO userProjectRoleDAO = new UserProjectRoleDAOImpl(schemaName);
                List<UserProjectRole> userProjectRoleList = userProjectRoleDAO.findByUserAndProject(user.getId(), project.getId());

                RoleTypeDAO roleTypeDAO = new RoleTypeDAOImpl(schemaName);

                userRoles = new String[userProjectRoleList.size()];
                int index = 0;
                for (UserProjectRole userProjectRole : userProjectRoleList) {
                    RoleType roleType = roleTypeDAO.findById(userProjectRole.getRoleTypeId());
                    userRoles[index++] = roleType.getRoleName();
                }
            }

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
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            List<Project> projectList = projectDAO.findAll();

            if (projectList != null) {
                retVal = true;
                for (int i = 0; (i < projectList.size()) && retVal; i++) {
                    retVal = removeAllUserRoles(callerUserName, projectList.get(i).getProjectName(), userName);
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
                if (0 == projectUsers[i].compareTo(ADMIN)) {
                    LOGGER.info("Skipping ADMIN for deleting roles for project: [{}]", projectName);
                } else {
                    retVal = removeAllUserRoles(callerUserName, projectName, projectUsers[i]);
                }
            }
        }
        return retVal;
    }

    @Override
    public RoleType getRoleType(String roleTypeName) {
        RoleTypeDAO roleTypeDAO = new RoleTypeDAOImpl(schemaName);
        RoleType roleType = roleTypeDAO.findByRoleName(roleTypeName);
        if (roleType == null) {
            try {
                // We need to make a new role type with the requested name.
                roleType = new RoleType();
                roleType.setRoleName(roleTypeName);
                Integer id = roleTypeDAO.insert(roleType);
                roleType.setId(id);
            } catch (SQLException e) {
                LOGGER.warn("Exception inserting role: [{}]", roleTypeName, e);
            }
        }
        return roleType;
    }

    /**
     * Get the list of available roles.
     * @return the list of available roles.
     */
    public String[] getAvailableRoles() {
        RoleTypeDAO roleTypeDAO = new RoleTypeDAOImpl(schemaName);
        List<String> roleList = new ArrayList<>();
        List<RoleType> roleTypeList = roleTypeDAO.findAll();

        for (RoleType roleType : roleTypeList) {
            if (0 != roleType.getRoleName().compareTo(ADMIN)) {
                roleList.add(roleType.getRoleName());
            }
        }
        String[] returnedList = new String[roleList.size()];
        for (int i = 0; i < roleList.size(); i++) {
            returnedList[i] = roleList.get(i);
        }
        return returnedList;
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
            try {
                RoleTypeDAO roleTypeDAO = new RoleTypeDAOImpl(schemaName);
                RoleType roleType = roleTypeDAO.findByRoleName(role);
                roleTypeDAO.delete(roleType.getId());
            } catch (SQLException e) {
                LOGGER.warn("Exception deleting role: [{}]", role, e);
            }
        }
    }

    public synchronized boolean initialize() {
        if (!isInitializedFlag) {
            ADMIN_ROLE = getRoleType(ADMIN);
            PROJECT_ADMIN_ROLE = getRoleType(PROJECT_ADMIN);
            READER_ROLE = getRoleType(READER);
            WRITER_ROLE = getRoleType(WRITER);
            DEVELOPER_ROLE = getRoleType(DEVELOPER);
            CEMETERY_ADMIN_ROLE = getRoleType(CEMETERY_ADMIN);
            isInitializedFlag = true;
        }
        return isInitializedFlag;
    }
}
