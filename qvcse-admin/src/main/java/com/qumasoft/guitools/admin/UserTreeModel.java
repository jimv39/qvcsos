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
package com.qumasoft.guitools.admin;

import com.qumasoft.qvcslib.ServerManager;
import com.qumasoft.qvcslib.response.ServerResponseListProjectUsers;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.qvcslib.response.ServerResponseListRoleNames;
import com.qumasoft.qvcslib.response.ServerResponseListRolePrivileges;
import com.qumasoft.qvcslib.response.ServerResponseListUserRoles;
import com.qumasoft.qvcslib.response.ServerResponseListUsers;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model class for the user tree.
 *
 * @author Jim Voris
 */
public class UserTreeModel implements ChangeListener {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(UserTreeModel.class);
    private final javax.swing.tree.DefaultTreeModel model;
    private String serverName = null;
    /** The set of users for the currently selected server */
    private Set serverUsers = null;
    /** The set of users for the currently selected project */
    private Set projectUsers = null;

    /** Creates new User Tree Model. */
    public UserTreeModel() {
        DefaultUserTreeNode rootNode = new DefaultUserTreeNode("No");

        // Create the tree model
        this.model = new javax.swing.tree.DefaultTreeModel(rootNode, false);
        ServerManager.getServerManager().addChangeListener(this);
    }

    javax.swing.tree.DefaultTreeModel getTreeModel() {
        return this.model;
    }

    @Override
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        Object change = changeEvent.getSource();

        if (change instanceof ServerResponseListUsers) {
            ServerResponseListUsers response = (ServerResponseListUsers) change;
            updateRootNameForServer(response.getServerName());
            DefaultUserTreeNode rootNode = (DefaultUserTreeNode) getTreeModel().getRoot();
            String[] userList = response.getUserList();
            Set<String> localServerUsers = new TreeSet<>();
            for (String userList1 : userList) {
                rootNode.add(new UserTreeNode(userList1));
                // Save the server users for later use (add users to project
                // for example).
                localServerUsers.add(userList1);
            }
            this.serverUsers = localServerUsers;
            getTreeModel().nodeStructureChanged(rootNode);
        } else if (change instanceof ServerResponseListProjectUsers) {
            ServerResponseListProjectUsers response = (ServerResponseListProjectUsers) change;
            updateRootNameForProject(response.getServerName(), response.getProjectName());
            DefaultProjectUserTreeNode rootNode = (DefaultProjectUserTreeNode) getTreeModel().getRoot();
            String[] userList = response.getUserList();
            Set<String> localProjectUsers = new TreeSet<>();
            for (String userList1 : userList) {
                rootNode.add(new UserTreeNode(userList1));
                // Save the project users for later use (add users to project
                // for example).
                localProjectUsers.add(userList1);
            }
            this.projectUsers = localProjectUsers;
            getTreeModel().nodeStructureChanged(rootNode);
        } else if (change instanceof ServerResponseListUserRoles) {
            // We don't do anything with users on this side of the display...
            LOGGER.info("Ignoring list user roles state change in UserTreeModel");
        } else if (change instanceof ServerResponseListRoleNames) {
            // We don't do anything with roles on this side of the display...
            LOGGER.info("Ignoring list role names state change in UserTreeModel");
        } else if (change instanceof ServerResponseListRolePrivileges) {
            // We don't do anything with roles on this side of the display...
            LOGGER.info("Ignoring list role privileges state change in UserTreeModel");
        } else if (change instanceof ServerResponseListProjects) {
            // Wipe the model, since we don't know which project is available
            // or selected anymore
            updateRootNameForServer("");
        } else {
            LOGGER.warn("unknown source of state change in UserTreeModel");
        }
    }

    void updateRootNameForServer(String rootName) {
        DefaultMutableTreeNode oldRootNode = (DefaultMutableTreeNode) getTreeModel().getRoot();
        oldRootNode.removeAllChildren();
        DefaultUserTreeNode rootNode = new DefaultUserTreeNode(rootName);

        getTreeModel().setRoot(rootNode);
        getTreeModel().nodeStructureChanged(rootNode);
        this.serverName = rootName;
    }

    void updateRootNameForProject(String argServerName, String rootName) {
        DefaultMutableTreeNode oldRootNode = (DefaultMutableTreeNode) getTreeModel().getRoot();
        oldRootNode.removeAllChildren();
        DefaultProjectUserTreeNode rootNode = new DefaultProjectUserTreeNode(rootName, getServerName());

        getTreeModel().setRoot(rootNode);
        getTreeModel().nodeStructureChanged(rootNode);
        this.serverName = argServerName;
    }

    String getServerName() {
        return this.serverName;
    }

    Set getServerUsers() {
        return this.serverUsers;
    }

    void removeServerUser(String userName) {
        this.serverUsers.remove(userName);
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getTreeModel().getRoot();
        rootNode.removeAllChildren();

        java.util.Iterator i = this.serverUsers.iterator();
        while (i.hasNext()) {
            String name = (String) i.next();
            rootNode.add(new UserTreeNode(name));
        }

        getTreeModel().nodeStructureChanged(rootNode);
    }

    Set getProjectUsers() {
        return this.projectUsers;
    }
}
