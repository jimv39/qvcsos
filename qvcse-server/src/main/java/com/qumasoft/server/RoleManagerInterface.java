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

import com.qumasoft.qvcslib.RoleType;

/**
 * Role Manager Interface. Define the behaviors that the Role Manager must supply.
 * @author Jim Voris
 */
public interface RoleManagerInterface {
    /*
     * These are the built-in ROLES that QVCS-Enterprise understands.
     *
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     * NOTE - When adding roles here, make sure to also modify RoleManager.getRoleType()
     * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
     */
    /** The built-in admin role. */
    String ADMIN = "ADMIN";
    /** The built-in project admin role. */
    String PROJECT_ADMIN = "PROJECT_ADMIN";
    /** The built-in reader role. */
    String READER = "READER";
    /** The built-in writer role. */
    String WRITER = "WRITER";
    /** The built-in developer role. */
    String DEVELOPER = "DEVELOPER";
    /** The built-in cemetery admin role. */
    String CEMETERY_ADMIN = "CEMETERY_ADMIN";
    /** This is like the root role. Can admin other admins. */
    RoleType ADMIN_ROLE = new RoleType(ADMIN);
    /** This is a role that can administer a given project. */
    RoleType PROJECT_ADMIN_ROLE = new RoleType(PROJECT_ADMIN);
    /** This is a role that can read the QVCS archives for a project. */
    RoleType READER_ROLE = new RoleType(READER);
    /** This is a role that can update the QVCS archives for a project. */
    RoleType WRITER_ROLE = new RoleType(WRITER);
    /** A sample DEVELOPER role. */
    RoleType DEVELOPER_ROLE = new RoleType(DEVELOPER);
    /** A sample CEMETERY ADMIN role. */
    RoleType CEMETERY_ADMIN_ROLE = new RoleType(CEMETERY_ADMIN);
    /** This is the name of the always present administrative user. */
    String DEFAULT_ADMIN_USER_NAME = "admin";

    /**
     * Initialize the role store. This could be used to read the store from a file, or to open a database, etc.
     *
     * @return true if initialization succeeded.
     */
    boolean initialize();

    /**
     * Save the role store to a persistant store. The server will call this method before exiting.
     */
    void writeRoleStore();

    /**
     * Return true if the add succeeds (it could fail if the caller doesn't have the needed authority to perform the operation). If
     * the projectName is null, then the operation applies to the admin user and the role must be the ADMIN_ROLE.
     *
     * @param callerUserName the caller user name (probably ADMIN).
     * @param projectName the project name.
     * @param userName the user for whom we're adding a role.
     * @param role the role to add.
     * @return true if things work okay.
     */
    boolean addUserRole(String callerUserName, String projectName, String userName, RoleType role);

    /**
     * Return true if the assignment succeeds. Assign the set of roles to the given user. Any existing roles are replaced with the
     * set of roles contained in the roles array.
     *
     * @param callerUserName the caller user name (probably ADMIN).
     * @param projectName the project name.
     * @param userName the user who is having roles assigned.
     * @param roles the list of roles to be assigned.
     * @return true if things work okay.
     */
    boolean assignUserRoles(String callerUserName, String projectName, String userName, String[] roles);

    /**
     * Return true if the remove succeeds (it could fail if the caller doesn't have the needed authority to perform the operation).
     * If the projectName is null, then the operation applies to the admin user and the role must be the ADMIN_ROLE.
     *
     * @param callerUserName the caller user name (probably ADMIN).
     * @param projectName the project name.
     * @param userName the user whose role is getting removed.
     * @param role the role that is being removed for the given user/project.
     * @return true if things worked okay.
     */
    boolean removeUserRole(String callerUserName, String projectName, String userName, RoleType role);

    /**
     * Remove all user roles for the given project. Use this method to remove a user's access to a project.
     *
     * @param callerUserName the caller user name (probably ADMIN).
     * @param projectName the project name.
     * @param userName the user who roles are getting removed.
     * @return true if things worked okay.
     */
    boolean removeAllUserRoles(String callerUserName, String projectName, String userName);

    /**
     * Use this method when removing a user from all access to a server. It will iterate over all projects, and remove all of that
     * user's roles for all the projects on the server
     *
     * @param callerUserName the caller user name. (probably ADMIN).
     * @param userName the user whose roles are getting removed.
     * @return true if things worked okay.
     */
    boolean removeAllUserRolesInAllProjects(String callerUserName, String userName);

    /**
     * Convert a string role type representation into a role type instance. The goal is to always return the single static instances
     * that are instantiated above.
     *
     * @param roleType the role type (as a String).
     * @return the role type instance for the given string.
     */
    RoleType getRoleType(String roleType);
}
