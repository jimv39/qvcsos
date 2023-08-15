/*   Copyright 2004-2023 Jim Voris
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

import com.qumasoft.qvcslib.DefaultServerProperties;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSServerNamesFilter;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.RemotePropertiesManager;
import com.qumasoft.qvcslib.ServerManager;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.response.ServerResponseListProjectUsers;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.qvcslib.response.ServerResponseListRoleNames;
import com.qumasoft.qvcslib.response.ServerResponseListRolePrivileges;
import com.qumasoft.qvcslib.response.ServerResponseListUserRoles;
import com.qumasoft.qvcslib.response.ServerResponseListUsers;
import java.io.File;
import java.util.TreeMap;
import javax.swing.event.ChangeListener;
import javax.swing.tree.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server tree model class.
 *
 * @author Jim Voris
 */
public final class ServerTreeModel implements ChangeListener {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerTreeModel.class);
    private javax.swing.tree.DefaultTreeModel model = null;
    private final TreeMap<String, ServerTreeNode> serverNodeMap = new TreeMap<>();

    /**
     * Creates new ServerTreeModel.
     */
    public ServerTreeModel() {
        loadModel();
        ServerManager.getServerManager().addChangeListener(this);
    }

    javax.swing.tree.DefaultTreeModel getTreeModel() {
        return model;
    }

    @Override
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        Object change = changeEvent.getSource();

        if (change instanceof ServerResponseListUsers) {
            // We don't do anything with users on this side of the display...
            LOGGER.info("Ignoring List Users state change");
        } else if (change instanceof ServerResponseListProjectUsers) {
            // We don't do anything with users on this side of the display...
            LOGGER.info("Ignoring List Project Users state change");
        } else if (change instanceof ServerResponseListUserRoles) {
            // We don't do anything with users on this side of the display...
            LOGGER.info("Ignoring List User Roles state change");
        } else if (change instanceof ServerResponseListRoleNames) {
            // We don't do anything with users on this side of the display...
            LOGGER.info("Ignoring List Role Names state change");
        } else if (change instanceof ServerResponseListRolePrivileges) {
            // We don't do anything with role privileges on this side...
            LOGGER.info("Ignoring List Role Privileges state change");
        } else if (change instanceof ServerResponseListProjects) {
            ServerResponseListProjects response = (ServerResponseListProjects) change;
            TreeNode changedNode = loadRemoteProjects(response);
            if (changedNode != null) {
                model.nodeStructureChanged(changedNode);
            }
        } else {
            LOGGER.warn("unknown source of state change in ServerTreeModel");
        }
    }

    void loadModel() {
        // Where all the property files can be found...
        File propertiesDirectory = new java.io.File(System.getProperty("user.dir")
                + System.getProperty("file.separator")
                + QVCSConstants.QVCS_SERVERS_DIRECTORY);
        DefaultServerTreeNode rootNode;

        if (model == null) {
            // Create the root node for the tree
            rootNode = new DefaultServerTreeNode(DefaultServerProperties.getInstance());

            // Create the tree model
            model = new javax.swing.tree.DefaultTreeModel(rootNode, false);
        } else {
            rootNode = (DefaultServerTreeNode) model.getRoot();
            rootNode.removeAllChildren();
        }

        // Load the server nodes.
        loadServerNodes(rootNode, propertiesDirectory);

        // And the view needs to change.
        model.nodeStructureChanged(rootNode);
    }

    void loadServerNodes(DefaultServerTreeNode rootNode, File projectsDirectory) {
        QVCSServerNamesFilter serverNameFilter = new QVCSServerNamesFilter();
        java.io.File[] serverFiles = projectsDirectory.listFiles(serverNameFilter);
        if (serverFiles != null) {
            for (File serverFile : serverFiles) {
                String serverName = serverNameFilter.getServerName(serverFile.getName());
                try {
                    ServerProperties serverProperties = new ServerProperties(projectsDirectory.getParent(), serverName);
                    ServerTreeNode serverNode = new ServerTreeNode(serverProperties);
                    rootNode.add(serverNode);
                    serverNodeMap.put(serverName, serverNode);
                } catch (Exception e) {
                    LOGGER.warn("Failed to load server [" + serverName + "] into tree model.");
                }
            }
        }
    }

    TreeNode loadRemoteProjects(ServerResponseListProjects response) {
        TreeNode treeNode = null;

        try {

            // Find the server for this project, and add it as a child of the
            // server's node.
            String serverName = response.getServerName();
            ServerTreeNode serverNode = serverNodeMap.get(serverName);
            if (serverNode != null) {
                // We'll replace any existing children with the list we just
                // received.
                serverNode.removeAllChildren();

                // Add all the projects that we received.
                for (String projectName : response.getProjectList()) {
                    TransportProxyInterface transportProxy = EnterpriseAdmin.getInstance().getTransportProxyInterface(serverName);
                    RemotePropertiesBaseClass remoteProperties =
                            RemotePropertiesManager.getInstance().getRemoteProperties(response.getUserName(), transportProxy);
                    ProjectTreeNode projectNode = new ProjectTreeNode(serverName, remoteProperties, projectName);
                    // Add this as a child of the server's node.
                    serverNode.add(projectNode);
                }
                treeNode = serverNode;
            } else {
                LOGGER.warn("received project list from unknown server: [{}]", serverName);
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to load projects for server: [{}] exception: [{}]", response.getServerName(), e.getLocalizedMessage());
        }
        return treeNode;
    }

    void logoffServer(String serverName) {
        try {
            ServerTreeNode serverNode = serverNodeMap.get(serverName);
            if (serverNode != null) {
                // We'll replace any existing children with the list we just
                // received.
                serverNode.removeAllChildren();

                // And the view needs to change.
                model.nodeStructureChanged(serverNode);
            }
        } catch (Exception e) {
            LOGGER.warn("Caught exception in logoffServer(): " + e.getLocalizedMessage());
        }
    }
}
