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
package com.qumasoft.qvcslib;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Server Manager. Handles those messages that manage the server -- like adding users. This class is used on clients.
 * @author Jim Voris
 */
public final class ServerManager {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib.ServerManager");
    private Set<ChangeListener> listeners = null;
    private static final ServerManager SERVER_MANAGER = new ServerManager();

    /**
     * Creates a new instance of ServerManager.
     */
    private ServerManager() {
        listeners = Collections.synchronizedSet(new HashSet<ChangeListener>());
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
        } else if (object instanceof ServerResponseListViews) {
            handleManagementListViews(object);
        } else if (object instanceof ServerResponseListRoleNames) {
            handleManagementListRoleNames(object);
        } else if (object instanceof ServerResponseListRolePrivileges) {
            handleManagementListRolePrivileges(object);
        } else {
            LOGGER.log(Level.WARNING, "unknown or unsupported server management message: " + object.getClass().toString());
        }
    }

    void handleManagementListUsers(Object object) {
        ServerResponseListUsers listUsersResponse = (ServerResponseListUsers) object;
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(new ChangeEvent(listUsersResponse));
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                listeners.remove(listener);
            }
        }
    }

    void handleManagementListProjectUsers(Object object) {
        ServerResponseListProjectUsers listProjectUsersResponse = (ServerResponseListProjectUsers) object;
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(new ChangeEvent(listProjectUsersResponse));
            } catch (Exception e) {
                listeners.remove(listener);
            }
        }
    }

    void handleManagementListUserRoles(Object object) {
        ServerResponseListUserRoles listUserRolesResponse = (ServerResponseListUserRoles) object;
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(new ChangeEvent(listUserRolesResponse));
            } catch (Exception e) {
                listeners.remove(listener);
            }
        }
    }

    void handleManagementListProjects(Object object) {
        ServerResponseListProjects listProjectsResponse = (ServerResponseListProjects) object;
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(new ChangeEvent(listProjectsResponse));
            } catch (Exception e) {
                listeners.remove(listener);
            }
        }
    }

    void handleManagementListViews(Object object) {
        ServerResponseListViews listViewsResponse = (ServerResponseListViews) object;
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(new ChangeEvent(listViewsResponse));
            } catch (Exception e) {
                listeners.remove(listener);
            }
        }
    }

    void handleManagementListRoleNames(Object object) {
        ServerResponseListRoleNames listRoleNames = (ServerResponseListRoleNames) object;
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(new ChangeEvent(listRoleNames));
            } catch (Exception e) {
                listeners.remove(listener);
            }
        }
    }

    void handleManagementListRolePrivileges(Object object) {
        ServerResponseListRolePrivileges listRolePrivileges = (ServerResponseListRolePrivileges) object;
        for (ChangeListener listener : listeners) {
            try {
                listener.stateChanged(new ChangeEvent(listRolePrivileges));
            } catch (Exception e) {
                listeners.remove(listener);
            }
        }
    }
}

