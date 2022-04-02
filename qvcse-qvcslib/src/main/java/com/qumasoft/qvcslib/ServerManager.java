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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.qvcslib.response.ServerResponseListProjectUsers;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.qvcslib.response.ServerResponseListRoleNames;
import com.qumasoft.qvcslib.response.ServerResponseListRolePrivileges;
import com.qumasoft.qvcslib.response.ServerResponseListUserRoles;
import com.qumasoft.qvcslib.response.ServerResponseListUsers;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server Manager. Handles those messages that manage the server -- like adding users. This class is used on clients.
 * @author Jim Voris
 */
public final class ServerManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerManager.class);
    private Set<ChangeListener> listeners = null;
    private static final ServerManager SERVER_MANAGER = new ServerManager();

    /**
     * Creates a new instance of ServerManager.
     */
    private ServerManager() {
        listeners = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Get the server manager singleton.
     * @return the server manager singleton.
     */
    public static ServerManager getServerManager() {
        return SERVER_MANAGER;
    }

    /**
     * Add a change listener. A listener gets notified on receipt of server management responses.
     * @param listener the listener.
     */
    public void addChangeListener(ChangeListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a change listener.
     * @param listener a change listener.
     */
    public void removeChangeListener(ChangeListener listener) {
        listeners.remove(listener);
    }

    void handleServerManagement(Object object) {
        if (object instanceof ServerResponseListUsers) {
            handleManagementListUsers(object);
        } else if (object instanceof ServerResponseListProjectUsers) {
            handleManagementListProjectUsers(object);
        } else if (object instanceof ServerResponseListUserRoles) {
            handleManagementListUserRoles(object);
        } else if (object instanceof ServerResponseListProjects) {
            handleManagementListProjects(object);
        } else if (object instanceof ServerResponseListBranches) {
            handleManagementListBranches(object);
        } else if (object instanceof ServerResponseListRoleNames) {
            handleManagementListRoleNames(object);
        } else if (object instanceof ServerResponseListRolePrivileges) {
            handleManagementListRolePrivileges(object);
        } else {
            if (object != null) {
                LOGGER.warn("unknown or unsupported server management message: " + object.getClass().toString());
            }
        }
    }

    void handleManagementListUsers(Object object) {
        ServerResponseListUsers listUsersResponse = (ServerResponseListUsers) object;
        Object[] array = listeners.toArray();
        for (Object o : array) {
            ChangeListener changeListener = (ChangeListener) o;
            try {
                changeListener.stateChanged(new ChangeEvent(listUsersResponse));
            } catch (Exception e) {
                LOGGER.warn("caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                listeners.remove(changeListener);
            }
        }
    }

    void handleManagementListProjectUsers(Object object) {
        ServerResponseListProjectUsers listProjectUsersResponse = (ServerResponseListProjectUsers) object;
        Object[] array = listeners.toArray();
        for (Object o : array) {
            ChangeListener changeListener = (ChangeListener) o;
            try {
                changeListener.stateChanged(new ChangeEvent(listProjectUsersResponse));
            } catch (Exception e) {
                LOGGER.warn("caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                listeners.remove(changeListener);
            }
        }
    }

    void handleManagementListUserRoles(Object object) {
        ServerResponseListUserRoles listUserRolesResponse = (ServerResponseListUserRoles) object;
        Object[] array = listeners.toArray();
        for (Object o : array) {
            ChangeListener changeListener = (ChangeListener) o;
            try {
                changeListener.stateChanged(new ChangeEvent(listUserRolesResponse));
            } catch (Exception e) {
                LOGGER.warn("caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                listeners.remove(changeListener);
            }
        }
    }

    void handleManagementListProjects(Object object) {
        ServerResponseListProjects listProjectsResponse = (ServerResponseListProjects) object;
        Object[] array = listeners.toArray();
        for (Object o : array) {
            ChangeListener changeListener = (ChangeListener) o;
            try {
                changeListener.stateChanged(new ChangeEvent(listProjectsResponse));
            } catch (Exception e) {
                LOGGER.warn("caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                listeners.remove(changeListener);
            }
        }
    }

    void handleManagementListBranches(Object object) {
        ServerResponseListBranches listBranchesResponse = (ServerResponseListBranches) object;
        ClientBranchManager.getInstance().updateBranchInfo(listBranchesResponse);
        Object[] array = listeners.toArray();
        for (Object o : array) {
            ChangeListener changeListener = (ChangeListener) o;
            try {
                changeListener.stateChanged(new ChangeEvent(listBranchesResponse));
            } catch (Exception e) {
                LOGGER.warn("caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                listeners.remove(changeListener);
            }
        }
    }

    void handleManagementListRoleNames(Object object) {
        ServerResponseListRoleNames listRoleNames = (ServerResponseListRoleNames) object;
        Object[] array = listeners.toArray();
        for (Object o : array) {
            ChangeListener changeListener = (ChangeListener) o;
            try {
                changeListener.stateChanged(new ChangeEvent(listRoleNames));
            } catch (Exception e) {
                LOGGER.warn("caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                listeners.remove(changeListener);
            }
        }
    }

    void handleManagementListRolePrivileges(Object object) {
        ServerResponseListRolePrivileges listRolePrivileges = (ServerResponseListRolePrivileges) object;
        Object[] array = listeners.toArray();
        for (Object o : array) {
            ChangeListener changeListener = (ChangeListener) o;
            try {
                changeListener.stateChanged(new ChangeEvent(listRolePrivileges));
            } catch (Exception e) {
                LOGGER.warn("caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                listeners.remove(changeListener);
            }
        }
    }
}

