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
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Role project view store.
 * @author Jim Voris
 */
public class RoleProjectViewStore implements Serializable {
    private static final long serialVersionUID = -2594064413091246198L;

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleProjectViewStore.class);
    /**
     * This is the map that contains project level maps that define/contain role maps for users
     */
    private final Map<String, Map<String, RoleType>> projectUserMap = Collections.synchronizedMap(new TreeMap<String, Map<String, RoleType>>());
    /**
     * This is the set of projects
     */
    private final Set<String> projectSet = Collections.synchronizedSet(new HashSet<String>());

    /**
     * Creates a new instance of RoleStore.
     */
    public RoleProjectViewStore() {
    }

    synchronized boolean addProjectViewUser(String callerUserName, String projectName, String userName, RoleType role) {
        boolean actionResult;
        String userRole = userName + "." + role.getRoleType();

        Map<String, RoleType> projectViewMap = projectUserMap.get(projectName);
        if (null != projectViewMap) {
            projectViewMap.put(userRole, role);
            actionResult = true;
        } else {
            // First user to be added for this project...
            projectViewMap = Collections.synchronizedMap(new TreeMap<String, RoleType>());
            projectUserMap.put(projectName, projectViewMap);
            projectViewMap.put(userRole, role);

            // And put this project in the project set.
            projectSet.add(projectName);

            actionResult = true;
        }
        return actionResult;
    }

    synchronized boolean removeProjectViewUser(String callerUserName, String projectName, String userName, RoleType role) {
        boolean actionResult;
        String userRole = userName + "." + role.getRoleType();

        Map projectViewMap = projectUserMap.get(projectName);
        if (null != projectViewMap) {
            projectViewMap.remove(userRole);
        }
        actionResult = true;
        return actionResult;
    }

    synchronized boolean isUserInRole(String projectName, String userName, RoleType role) {
        boolean actionAllowed = false;
        String userRole = userName + "." + role.getRoleType();

        Map projectViewMap = projectUserMap.get(projectName);
        if (null != projectViewMap) {
            actionAllowed = projectViewMap.containsKey(userRole);
        }
        return actionAllowed;
    }

    synchronized String[] listProjectUsers(String projectName) {
        String[] projectViewUsers = null;
        Set<String> userSet = new HashSet<>();

        Map<String, RoleType> projectViewMap = projectUserMap.get(projectName);
        if (null != projectViewMap) {
            // Get the set of user.role for this project.
            Set<String> userKeys = projectViewMap.keySet();
            Iterator<String> it = userKeys.iterator();
            while (it.hasNext()) {
                String userAndRole = it.next();
                int separatorIndex = userAndRole.lastIndexOf('.');
                String user = userAndRole.substring(0, separatorIndex);
                userSet.add(user);
            }
            projectViewUsers = new String[userSet.size()];
            Iterator userIterator = userSet.iterator();
            int j = 0;
            while (userIterator.hasNext()) {
                projectViewUsers[j++] = (String) userIterator.next();
            }
        }
        return projectViewUsers;
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
            Map<String, RoleType> projectViewMap = projectUserMap.get(projectName);
            if (null != projectViewMap) {
                // Get the set of user.role for this project/view.
                Set<String> userKeys = projectViewMap.keySet();
                Iterator<String> it = userKeys.iterator();
                while (it.hasNext()) {
                    String userAndRole = it.next();
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
        Iterator<String> j = projectSet.iterator();
        while (j.hasNext()) {
            projectList.add(j.next());
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
            projectUserMap.keySet().stream().map((projectViewKey) -> projectUserMap.get(projectViewKey)).map((roleMap) -> roleMap.values().iterator()).forEach((projectMapIt) -> {
                while (projectMapIt.hasNext()) {
                    RoleType assignedRole = projectMapIt.next();
                    if (0 == assignedRole.getRoleType().compareTo(role)) {
                        projectMapIt.remove();
                    }
                }
            });
        }
    }

    synchronized void dumpMaps() {
        LOGGER.info("RoleStore.dumpMaps()");
        LOGGER.info("\tProject stores:");
        Set<String> projectKeys = projectUserMap.keySet();
        Iterator<String> j = projectKeys.iterator();
        while (j.hasNext()) {
            String o = j.next();
            LOGGER.info(o);
            Map<String, RoleType> projUserMap = this.projectUserMap.get(o);
            Set<String> userKeys = projUserMap.keySet();
            Iterator<String> k = userKeys.iterator();
            while (k.hasNext()) {
                LOGGER.info("\t[{}]", k.next());
            }
        }
    }
}
