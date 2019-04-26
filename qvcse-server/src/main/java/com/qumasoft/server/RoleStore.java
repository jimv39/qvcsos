/*   Copyright 2004-2014 Jim Voris
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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Role store.
 * @author Jim Voris
 */
public class RoleStore implements Serializable {
    private static final long serialVersionUID = 7056393618311375177L;

    /**
     * This root map contains the users who are 'super' users.
     */
    private final Map<String, RoleType> adminUserMap = Collections.synchronizedMap(new TreeMap<String, RoleType>());
    /**
     * This is the map that contains project level maps that define/contain role maps for users.
     */
    private final Map<String, Map<String, RoleType>> projectUserMap = Collections.synchronizedMap(new TreeMap<String, Map<String, RoleType>>());

    /**
     * Creates a new instance of RoleStore.
     */
    public RoleStore() {
        adminUserMap.put(RoleManagerInterface.ADMIN_ROLE.getRoleType(), RoleManagerInterface.ADMIN_ROLE);
    }

    synchronized boolean addSuperUser(String callerUserName, String userName) {
        boolean retVal = false;

        if (isSuperUser(callerUserName)) {
            // Only an existing admin is allowed to add someone else as
            // an admin.
            adminUserMap.put(userName, RoleManagerInterface.ADMIN_ROLE);
            retVal = true;
        }

        return retVal;
    }

    synchronized boolean removeSuperUser(String callerUserName, String userName) {
        boolean retVal = false;

        if (isSuperUser(callerUserName)) {
            // Only an existing admin is allowed to remove someone else as
            // an admin.
            adminUserMap.remove(userName);
            retVal = true;
        }

        return retVal;
    }

    synchronized boolean isSuperUser(String userName) {
        return adminUserMap.containsKey(userName);
    }

    synchronized boolean addProjectUser(String callerUserName, String projectName, String userName, RoleType role) {
        boolean actionResult;
        String userRole = userName + "." + role.getRoleType();

        Map<String, RoleType> projectMap = projectUserMap.get(projectName);
        if (null != projectMap) {
            projectMap.put(userRole, role);
            actionResult = true;
        } else {
            // First user to be added for this project...
            projectMap = Collections.synchronizedMap(new TreeMap<String, RoleType>());
            projectUserMap.put(projectName, projectMap);
            projectMap.put(userRole, role);
            actionResult = true;
        }
        return actionResult;
    }

    synchronized boolean removeProjectUser(String callerUserName, String projectName, String userName, RoleType role) {
        boolean actionResult;
        String userRole = userName + "." + role.getRoleType();

        Map projectMap = (Map) projectUserMap.get(projectName);
        if (null != projectMap) {
            projectMap.remove(userRole);
        }
        actionResult = true;
        return actionResult;
    }

    synchronized boolean isUserInRole(String projectName, String userName, RoleType role) {
        boolean actionAllowed = false;
        String userRole = userName + "." + role.getRoleType();

        Map projectMap = (Map) projectUserMap.get(projectName);
        if (null != projectMap) {
            actionAllowed = projectMap.containsKey(userRole);
        }
        return actionAllowed;
    }

    synchronized String[] listProjectUsers(String projectName) {
        String[] projectUsers = null;
        Set<String> userSet = new HashSet<>();

        Map<String, RoleType> projectMap = projectUserMap.get(projectName);
        if (null != projectMap) {
            // Get the set of user.role for this project.
            Set<String> userKeys = projectMap.keySet();
            Iterator<String> it = userKeys.iterator();
            while (it.hasNext()) {
                String userAndRole = it.next();
                int separatorIndex = userAndRole.lastIndexOf('.');
                String user = userAndRole.substring(0, separatorIndex);
                userSet.add(user);
            }
            projectUsers = new String[userSet.size()];
            Iterator<String> userIterator = userSet.iterator();
            int j = 0;
            while (userIterator.hasNext()) {
                projectUsers[j++] = userIterator.next();
            }
        }
        return projectUsers;
    }

    synchronized String[] listUserRoles(String projectName, String userName) {
        String[] userRoles = new String[0];
        ArrayList<String> roleArray = new ArrayList<>();
        if (null == projectName) {
            if (userName.equals(RoleManagerInterface.ADMIN_ROLE.getRoleType())) {
                roleArray.add(RoleManagerInterface.ADMIN_ROLE.getRoleType());
            }
        }

        if (projectName != null) {
            Map<String, RoleType> projectMap = projectUserMap.get(projectName);
            if (null != projectMap) {
                // Get the set of user.role for this project.
                Set userKeys = projectMap.keySet();
                Iterator it = userKeys.iterator();
                while (it.hasNext()) {
                    String userAndRole = (String) it.next();
                    int separatorIndex = userAndRole.lastIndexOf('.');
                    String userRole = userAndRole.substring(1 + separatorIndex);
                    String user = userAndRole.substring(0, separatorIndex);
                    if (user.equals(userName)) {
                        roleArray.add(userRole);
                    }
                }
            }
        }

        if (roleArray.size() > 0) {
            userRoles = new String[roleArray.size()];
            for (int i = 0; i < userRoles.length; i++) {
                userRoles[i] = roleArray.get(i);
            }
        }
        return userRoles;
    }

    synchronized String[] getProjectList() {
        ArrayList<String> projectList = new ArrayList<>();
        Set<String> projectKeys = projectUserMap.keySet();
        Iterator j = projectKeys.iterator();
        while (j.hasNext()) {
            Object o = j.next();
            projectList.add(o.toString());
        }

        String[] projectStringArray = new String[projectList.size()];
        for (int i = 0; i < projectStringArray.length; i++) {
            projectStringArray[i] = projectList.get(i);
        }
        return projectStringArray;
    }

    synchronized void deleteRole(String role) {
        // We can only delete non ADMIN roles...
        if (0 != role.compareTo(RoleManager.ADMIN)) {
            for (String projectName : projectUserMap.keySet()) {
                Map roleMap = (Map) projectUserMap.get(projectName);
                Iterator projectMapIt = roleMap.values().iterator();
                while (projectMapIt.hasNext()) {
                    RoleType assignedRole = (RoleType) projectMapIt.next();
                    if (0 == assignedRole.getRoleType().compareTo(role)) {
                        projectMapIt.remove();
                    }
                }
            }
        }
    }

    Set getProjectUserMapKeySet() {
        return projectUserMap.keySet();
    }

    Map getProjectUserMap(String projectName) {
        return (Map) projectUserMap.get(projectName);
    }
}
