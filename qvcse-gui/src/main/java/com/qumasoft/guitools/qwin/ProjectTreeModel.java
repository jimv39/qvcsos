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
package com.qumasoft.guitools.qwin;

import static com.qumasoft.guitools.qwin.QWinUtility.logMessage;
import static com.qumasoft.guitools.qwin.QWinUtility.traceMessage;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ClientBranchInfo;
import com.qumasoft.qvcslib.DefaultServerProperties;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSServerNamesFilter;
import com.qumasoft.qvcslib.QvcsosClientIgnoreManager;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.RemoteProjectProperties;
import com.qumasoft.qvcslib.ServerManager;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TimerManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.qvcslib.response.ServerResponseListProjects;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 * Project tree model.
 * @author Jim Voris
 */
public class ProjectTreeModel implements ChangeListener {

    private javax.swing.tree.DefaultTreeModel projectTreeModel;
    // A map of the servers we know about.
    private final TreeMap<String, ServerTreeNode> serverNodeMap;
    // A map of the projects that we know about.
    private final TreeMap<String, ProjectTreeNode> projectNodeMap;
    // A Map of the Set of appended paths.
    private final TreeMap<String, Set<String>> appendedPathsMap;
    // Timer and TimerTask we use to aggregate model updates so screen won't
    // flicker when we get a ton of state changed calls in succession.
    private final Timer timerDaemon;
    private TimerTask notifyTask;
    private static final long UPDATE_DELAY = 300;
    private static final String USER_DIR = "user.dir";
    private final Object modelSyncObject = new Object();
    /**
     * Used to capture the deepest existing parent so when a subproject is added we don't need to refresh the entire branch's tree, but can update the model from this node
     * and below.
     */
    private DefaultMutableTreeNode deepestParent;
    /**
     * Used to hold the directory node we select after receiving all the directories from the server.
     */
    private DefaultMutableTreeNode pendingDirectoryNode;

    /**
     * Creates new ProjectTreeModel.
     */
    public ProjectTreeModel() {
        this.projectNodeMap = new TreeMap<>();
        this.serverNodeMap = new TreeMap<>();
        this.appendedPathsMap = new TreeMap<>();
        loadModel();
        TransportProxyFactory.getInstance().addChangeListener(this);
        ServerManager.getServerManager().addChangeListener(this);

        // Create our daemon timer task so we can aggregate updates.
        timerDaemon = TimerManager.getInstance().getTimer();
    }

    javax.swing.tree.DefaultTreeModel getTreeModel() {
        return projectTreeModel;
    }

    @Override
    public void stateChanged(final javax.swing.event.ChangeEvent changeEvent) {
        // Install the thread tracking repaint manager.
        Runnable stateChangedTask = () -> {
            Object o = changeEvent.getSource();
            if (o instanceof ServerResponseProjectControl controlMessage) {
                QWinFrame.getQWinFrame().setIgnoreTreeChanges(true);
                if (controlMessage.getShowCemeteryFlag()) {
                    logMessage("Adding cemetery node");
                    BranchTreeNode branchTreeNode = findProjectBranchTreeNode(controlMessage.getServerName(), controlMessage.getProjectName(), controlMessage.getBranchName());
                    CemeteryTreeNode cemeteryTreeNode = new CemeteryTreeNode(controlMessage.getBranchName(), branchTreeNode.getProjectProperties());
                    branchTreeNode.add(cemeteryTreeNode);
                } else if (controlMessage.getAddFlag()) {
                    // Add node to the tree.
                    addSubProject(controlMessage.getServerName(), controlMessage.getProjectName(), controlMessage.getBranchName(), controlMessage.getDirectorySegments());
                    String appendedPath = buildAppendedPath(controlMessage.getDirectorySegments());
                    traceMessage("Adding subdirectory; server: [" + controlMessage.getServerName() + "] for project: [" + controlMessage.getProjectName()
                            + "] branch name: [" + controlMessage.getBranchName() + "] appended path: [" + appendedPath + "]");
                } else if (controlMessage.getRemoveFlag()) {
                    BranchTreeNode branchTreeNode = findProjectBranchTreeNode(controlMessage.getServerName(), controlMessage.getProjectName(), controlMessage.getBranchName());
                    if (branchTreeNode != null) {
                        if ((controlMessage.getDirectorySegments() == null) || (controlMessage.getDirectorySegments().length == 0)) {
                            // Remove node from the tree.
                            branchTreeNode.removeAllChildren();
                            DefaultServerTreeNode rootNode = (DefaultServerTreeNode) getTreeModel().getRoot();
                            ProjectTreeControl.getInstance().selectRootNode();
                            projectTreeModel.nodeStructureChanged(rootNode);
                            QWinFrame.getQWinFrame().clearUsernamePassword(controlMessage.getServerName());

                            // Throw away the appended paths for this server/project/branch.
                            appendedPathsMap.remove(getAppendedPathSetKey(controlMessage.getServerName(), controlMessage.getProjectName(), controlMessage.getBranchName()));

                            logMessage("Removing project [" + controlMessage.getProjectName() + "] from project tree");
                        } else {
                            deleteAppendedPathEntry(controlMessage.getServerName(), controlMessage.getProjectName(), controlMessage.getBranchName(), controlMessage.getDirectorySegments());
                            deleteSubprojectNode(branchTreeNode, branchTreeNode.getProjectProperties(), controlMessage.getDirectorySegments());
                            logMessage("Removing directory [" + buildAppendedPath(controlMessage.getDirectorySegments()) + "] from project tree");
                        }
                    }
                }
                QWinFrame.getQWinFrame().setIgnoreTreeChanges(false);
            } else if (o instanceof ServerResponseListProjects serverResponseListProjects) {
                TreeNode changedNode = loadRemoteProjects(serverResponseListProjects);
                QWinFrame.getQWinFrame().setIgnoreTreeChanges(true);
                if (changedNode != null) {
                    projectTreeModel.nodeStructureChanged(changedNode);
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) changedNode;
                    QWinFrame.getQWinFrame().getTreeControl().getProjectJTreeControl().expandPath(new TreePath(node.getPath()));

                    // Select the project/branch that was active the last time the
                    // user ran the program.
                    String projectName = QWinFrame.getQWinFrame().getUserProperties().getMostRecentProjectName();
                    if (projectName != null && projectName.length() > 0) {
                        TreeNode projectNode = findProjectTreeNode(serverResponseListProjects.getServerName(), projectName);
                        ProjectTreeControl.getInstance().selectNode(projectNode);
                    }
                }
                QWinFrame.getQWinFrame().setIgnoreTreeChanges(false);
            } else if (o instanceof ServerResponseListBranches serverResponseListBranches) {
                TreeNode changedNode = loadRemoteBranches(serverResponseListBranches);
                QWinFrame.getQWinFrame().setIgnoreTreeChanges(true);
                if (changedNode != null) {
                    projectTreeModel.nodeStructureChanged(changedNode);
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) changedNode;
                    QWinFrame.getQWinFrame().getTreeControl().getProjectJTreeControl().expandPath(new TreePath(node.getPath()));

                    // Discard any DirectoryManagers that we have for this project...
                    // They will get rebuilt as the user navigates into the project.
                    DirectoryManagerFactory.getInstance().discardDirectoryManagersForProject(serverResponseListBranches.getServerName(),
                            serverResponseListBranches.getProjectName());

                    // Select the project/branch that was active the last time the
                    // user ran the program.
                    String projectName = QWinFrame.getQWinFrame().getUserProperties().getMostRecentProjectName();
                    String branchName = QWinFrame.getQWinFrame().getUserProperties().getMostRecentBranchName();
                    if ((projectName != null && projectName.length() > 0)
                            && (branchName != null && branchName.length() > 0)) {
                        TreeNode directoryNode = findProjectBranchTreeNode(serverResponseListBranches.getServerName(), projectName, branchName);
                        ProjectTreeControl.getInstance().selectNode(directoryNode);
                    }
                }
                QWinFrame.getQWinFrame().setIgnoreTreeChanges(false);
            }
        };
        SwingUtilities.invokeLater(stateChangedTask);
    }

    private void addSubProject(final String serverName, final String projectName, final String branchName, String[] segments) {
        BranchTreeNode branchNode;
        synchronized (modelSyncObject) {
            branchNode = findProjectBranchTreeNode(serverName, projectName, branchName);
            if (branchNode != null) {
                addSubprojectNode(branchNode, branchNode.getProjectProperties(), segments);
                String appendedPath = buildAppendedPath(segments);
                if (0 == appendedPath.compareTo(QWinFrame.getQWinFrame().getUserProperties().getMostRecentAppendedPath())) {
                    DefaultMutableTreeNode directoryTreeNode = findContainingDirectoryTreeNode(serverName, projectName, branchName, appendedPath);
                    pendingDirectoryNode = directoryTreeNode;
                }
                String appendedPathSetKey = getAppendedPathSetKey(serverName, projectName, branchName);
                Set<String> appendedPathSet = appendedPathsMap.get(appendedPathSetKey);
                if (appendedPathSet == null) {
                    appendedPathSet = new TreeSet<>();
                    // Make sure the root directory is there.
                    appendedPathSet.add("");
                    appendedPathsMap.put(appendedPathSetKey, appendedPathSet);
                }
                appendedPathSet.add(appendedPath);
            }
        }
    }

    ProjectTreeNode findProjectTreeNode(final String serverName, final String projectName) {
        ProjectTreeNode foundProject = null;
        ServerTreeNode serverNode = serverNodeMap.get(serverName);
        Enumeration enumeration = serverNode.children();
        while (enumeration.hasMoreElements()) {
            ProjectTreeNode projectNode = (ProjectTreeNode) enumeration.nextElement();
            AbstractProjectProperties projectProperties = projectNode.getProjectProperties();
            if (projectProperties.getProjectName().equals(projectName)) {
                foundProject = projectNode;
                break;
            }
        }
        return foundProject;
    }

    BranchTreeNode findProjectBranchTreeNode(final String serverName, final String projectName, final String branchName) {
        BranchTreeNode foundBranch = null;
        ServerTreeNode serverNode = serverNodeMap.get(serverName);
        Enumeration enumeration = serverNode.children();
        while (enumeration.hasMoreElements()) {
            ProjectTreeNode projectNode = (ProjectTreeNode) enumeration.nextElement();
            AbstractProjectProperties projectProperties = projectNode.getProjectProperties();
            if (projectProperties.getProjectName().equals(projectName)) {
                Enumeration branchEnumeration = projectNode.children();
                while (branchEnumeration.hasMoreElements()) {
                    BranchTreeNode branchTreeNode = (BranchTreeNode) branchEnumeration.nextElement();
                    if (branchTreeNode.getBranchName().equals(branchName)) {
                        foundBranch = branchTreeNode;
                        break;
                    }
                }
                break;
            }
        }
        return foundBranch;
    }

    /**
     * Find the tree node given the server, project, branch, and appended path.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param appendedPath the appended path.
     * @return the tree node for the given parameters. This will return null if the node cannot be found.
     */
    public DefaultMutableTreeNode findContainingDirectoryTreeNode(final String serverName, final String projectName, final String branchName, final String appendedPath) {
        BranchTreeNode foundBranch;
        DefaultMutableTreeNode foundDirectory = null;
        ServerTreeNode serverNode = serverNodeMap.get(serverName);
        Enumeration enumeration = serverNode.children();
        while (enumeration.hasMoreElements()) {
            ProjectTreeNode projectNode = (ProjectTreeNode) enumeration.nextElement();
            AbstractProjectProperties projectProperties = projectNode.getProjectProperties();
            if (projectProperties.getProjectName().equals(projectName)) {
                Enumeration branchEnumeration = projectNode.children();
                while (branchEnumeration.hasMoreElements()) {
                    BranchTreeNode branchTreeNode = (BranchTreeNode) branchEnumeration.nextElement();
                    if (branchTreeNode.getBranchName().equals(branchName)) {
                        foundBranch = branchTreeNode;
                        if (appendedPath.length() == 0) {
                            foundDirectory = foundBranch;
                        } else {
                            Enumeration directoryEnumeration = foundBranch.depthFirstEnumeration();
                            while (directoryEnumeration.hasMoreElements()) {
                                DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) directoryEnumeration.nextElement();
                                if (currentNode instanceof DirectoryTreeNode directoryTreeNode) {
                                    if (appendedPath.compareToIgnoreCase(directoryTreeNode.getAppendedPath()) == 0) {
                                        foundDirectory = currentNode;
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
        return foundDirectory;
    }

    private DefaultMutableTreeNode findDeepestParent(final String serverName, final String projectName, final String branchName, final String appendedPath) {
        BranchTreeNode foundBranch;
        DefaultMutableTreeNode foundDirectory = null;
        String deepestAppendedPath = "";
        ServerTreeNode serverNode = serverNodeMap.get(serverName);
        Enumeration enumeration = serverNode.children();
        while (enumeration.hasMoreElements()) {
            ProjectTreeNode projectNode = (ProjectTreeNode) enumeration.nextElement();
            AbstractProjectProperties projectProperties = projectNode.getProjectProperties();
            if (projectProperties.getProjectName().equals(projectName)) {
                Enumeration branchEnumeration = projectNode.children();
                while (branchEnumeration.hasMoreElements()) {
                    BranchTreeNode branchTreeNode = (BranchTreeNode) branchEnumeration.nextElement();
                    if (branchTreeNode.getBranchName().equals(branchName)) {
                        foundBranch = branchTreeNode;
                        if (appendedPath.length() == 0) {
                            foundDirectory = foundBranch;
                        } else {
                            Enumeration directoryEnumeration = foundBranch.depthFirstEnumeration();
                            while (directoryEnumeration.hasMoreElements()) {
                                DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) directoryEnumeration.nextElement();
                                if (currentNode instanceof DirectoryTreeNode directoryTreeNode) {
                                    String directoryTreeNodeAppendedPath = directoryTreeNode.getAppendedPath();
                                    if (appendedPath.startsWith(directoryTreeNodeAppendedPath)) {
                                        if (directoryTreeNode.getAppendedPath().length() > deepestAppendedPath.length()) {
                                            foundDirectory = directoryTreeNode;
                                            deepestAppendedPath = directoryTreeNode.getAppendedPath();
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
        return foundDirectory;
    }

    private void addSubprojectNode(BranchTreeNode branchTreeNode, AbstractProjectProperties projectProperties, String[] segments) {
        // We need to find the parent for this node.
        synchronized (ProjectTreeModel.class) {
            if (deepestParent == null) {
                final String serverName = QWinFrame.getQWinFrame().getServerName();
                deepestParent = findDeepestParent(serverName, branchTreeNode.getProjectName(), branchTreeNode.getBranchName(), buildAppendedPath(segments));
            }
        }
        DefaultMutableTreeNode node = branchTreeNode;
        for (int i = 0; (node != null) && (i < segments.length); i++) {
            node = getNode(branchTreeNode, node, projectProperties, segments, i);
        }
    }

    private String buildAppendedPath(String[] segments) {
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            stringBuffer.append(segments[i]);
            if (i + 1 < segments.length) {
                stringBuffer.append(QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING);
            }
        }
        return stringBuffer.toString();
    }

    private void deleteSubprojectNode(BranchTreeNode branchTreeNode, AbstractProjectProperties projectProperties, String[] segments) {
        // Find the node to delete...
        DefaultMutableTreeNode node = branchTreeNode;
        for (int i = 0; i < segments.length; i++) {
            node = getNode(branchTreeNode, node, projectProperties, segments, i);
        }
        DefaultMutableTreeNode nodeToDelete = node;
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) nodeToDelete.getParent();
        parentNode.remove(nodeToDelete);
        scheduleUpdate(parentNode);
    }

    private DefaultMutableTreeNode getNode(BranchTreeNode branchTreeNode, DefaultMutableTreeNode node, AbstractProjectProperties projectProperties, String[] segments, int index) {
        DirectoryTreeNode foundNode = null;
        boolean foundNodeFlag = false;
        String segment = segments[index];

        Enumeration enumeration = node.children();
        while (enumeration.hasMoreElements()) {
            TreeNode candidate = (TreeNode) enumeration.nextElement();
            if (candidate instanceof DirectoryTreeNode directoryTreeNode) {
                if (0 == candidate.toString().compareTo(segment)) {
                    foundNode = directoryTreeNode;
                    foundNodeFlag = true;
                }
            }
        }

        // The segment doesn't exist yet.  We'll need to create it.
        if (!foundNodeFlag) {
            // Figure out the appendedPath.
            StringBuilder appendedPath = new StringBuilder();
            for (int i = 0; i <= index; i++) {
                appendedPath.append(segments[i]);
                if (i <= index - 1) {
                    appendedPath.append(File.separator);
                }
            }

            // Figure out if we ignore this directory due to an entry in .qvcsosignore
            String userWorkfileDirectory = QWinFrame.getQWinFrame().getUserWorkfileDirectory();
            boolean ignoreDirFlag = false;
            String appendedPathString = appendedPath.toString();
            String fullWorkfileDirectoryName = userWorkfileDirectory + File.separator + appendedPathString;
            try {
                ignoreDirFlag = QvcsosClientIgnoreManager.getInstance().ignoreDirectory(fullWorkfileDirectoryName, appendedPathString);
            } catch (IOException e) {
                logMessage("Error when evaluating ignore directory: " + e.getLocalizedMessage());
            }
            if (ignoreDirFlag) {
                foundNode = null;
                logMessage("Ignoring server response for this directory because of entry in .qvcsosignore: " + fullWorkfileDirectoryName);
            } else {
                DirectoryTreeNode child = new DirectoryTreeNode(branchTreeNode.getBranchName(), appendedPathString, projectProperties);
                foundNode = child;
                node.add(child);
                scheduleUpdate(branchTreeNode);
            }
        }
        return foundNode;
    }

    private void scheduleUpdate(DefaultMutableTreeNode node) {
        // Cancel pending notify task.
        if (notifyTask != null) {
            if (notifyTask.cancel()) {
                traceMessage("Cancelled node structure changed tree model");
            }
            this.notifyTask = null;
        }
        final DefaultMutableTreeNode finalDefaultMutableTreeNode = node;

        TimerTask newNotifyTask = new TimerTask() {

            @Override
            public void run() {
                // Run this on the swing thread.
                Runnable swingTask = () -> {
                    synchronized (ProjectTreeModel.class) {
                        if (pendingDirectoryNode != null) {
                            ProjectTreeControl.getInstance().selectNode(pendingDirectoryNode);
                            pendingDirectoryNode = null;
                        } else if (deepestParent != null) {
                            projectTreeModel.nodeStructureChanged(deepestParent);
                        } else {
                            projectTreeModel.nodeStructureChanged(finalDefaultMutableTreeNode);
                        }
                        deepestParent = null;
                    }
                };
                SwingUtilities.invokeLater(swingTask);
            }
        };
        Date now = new Date();
        Date whenToRun = new Date(now.getTime() + UPDATE_DELAY);
        timerDaemon.schedule(newNotifyTask, whenToRun);
        this.notifyTask = newNotifyTask;
    }

    private void loadModel() {
        // Where all the property files can be found...
        File serversDirectory = new java.io.File(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory()
                + System.getProperty("file.separator")
                + QVCSConstants.QVCS_SERVERS_DIRECTORY);

        // Create the root node for the tree
        DefaultServerTreeNode rootNode = new DefaultServerTreeNode(DefaultServerProperties.getInstance());

        // Create the tree model
        projectTreeModel = new javax.swing.tree.DefaultTreeModel(rootNode, false);

        // Load the server nodes.
        loadServerNodes(rootNode, serversDirectory);

        if (QWinFrame.getQWinFrame().getUserProperties().getBypassLoginDialogFlag()) {
            String serverName = QWinFrame.getQWinFrame().getUserProperties().getBypassServerName();
            if ((serverName != null) && (serverName.length() > 0)) {
                final ServerTreeNode serverNode = serverNodeMap.get(serverName);
                if (serverNode != null) {
                    Runnable later = () -> {
                        ProjectTreeControl.getInstance().selectNode(serverNode);
                    };
                    SwingUtilities.invokeLater(later);
                }
            }
        }
    }

    private void loadServerNodes(DefaultServerTreeNode rootNode, File serversDirectory) {
        QVCSServerNamesFilter serverNameFilter = new QVCSServerNamesFilter();
        java.io.File[] serverFiles = serversDirectory.listFiles(serverNameFilter);

        if (serverFiles != null) {
            for (File serverFile : serverFiles) {
                String serverName = serverNameFilter.getServerName(serverFile.getName());
                try {
                    ServerProperties serverProperties = new ServerProperties(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), serverName);
                    ServerTreeNode serverNode = new ServerTreeNode(serverProperties);
                    rootNode.add(serverNode);
                    serverNodeMap.put(serverName, serverNode);
                } catch (NullPointerException | ClassCastException e) {
                    warnProblem("Failed to load project " + serverName + " into tree model." + Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    /**
     * Reload the server nodes from the property files.
     */
    public void reloadServerNodes() {
        // Make sure these are empty.
        serverNodeMap.clear();
        projectNodeMap.clear();

        // Where all the property files can be found...
        File projectsDirectory = new java.io.File(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory()
                + System.getProperty("file.separator")
                + QVCSConstants.QVCS_SERVERS_DIRECTORY);
        QVCSServerNamesFilter serverNameFilter = new QVCSServerNamesFilter();
        java.io.File[] serverFiles = projectsDirectory.listFiles(serverNameFilter);
        DefaultServerTreeNode rootNode = (DefaultServerTreeNode) getTreeModel().getRoot();

        Map<String, ServerProperties> newServers = new TreeMap<>();
        Map<String, ServerProperties> oldServers = new TreeMap<>();
        Map<String, ServerTreeNode> oldServerNodes = new TreeMap<>();
        for (File serverFile : serverFiles) {
            String serverName = serverNameFilter.getServerName(serverFile.getName());
            try {
                ServerProperties serverProperties = new ServerProperties(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), serverName);
                newServers.put(serverName, serverProperties);
            } catch (Exception e) {
                warnProblem("Failed to load project " + serverName + " into tree model.");
            }
        }

        // Find the old servers...
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            ServerTreeNode serverNode = (ServerTreeNode) rootNode.getChildAt(i);
            ServerProperties serverProperties = serverNode.getServerProperties();
            String serverName = serverProperties.getServerName();
            oldServers.put(serverName, serverProperties);
            oldServerNodes.put(serverName, serverNode);
        }
        newServers.values().stream().forEach((newServerProperties) -> {
            String serverName = newServerProperties.getServerName();
            ServerProperties oldServerProperties = oldServers.get(serverName);
            if (oldServerProperties != null) {
                // There is an existing server with this name.  Update the properties
                ServerTreeNode oldServerTreeNode = oldServerNodes.get(serverName);
                rootNode.remove(oldServerTreeNode);
                ServerTreeNode newServerNode = new ServerTreeNode(newServerProperties);
                rootNode.add(newServerNode);
                serverNodeMap.put(serverName, newServerNode);
            } else {
                // This one does not exist in the current list. We'll need to add
                // it.
                ServerTreeNode serverNode = new ServerTreeNode(newServerProperties);
                rootNode.add(serverNode);
                serverNodeMap.put(serverName, serverNode);
            }
        });
        oldServers.values().stream().map((oldServerProperties) -> oldServerProperties.getServerName()).forEach((serverName) -> {
            ServerProperties serverProperties = newServers.get(serverName);
            if (serverProperties == null) {
                // This one does not exist in the new list. We'll need to delete
                // it.
                ServerTreeNode serverNode = oldServerNodes.get(serverName);
                rootNode.remove(serverNode);
            }
        });
        // We need to close all transports since the server tree will get reset, and we'll lose all of our tree nodes.
        TransportProxyFactory.getInstance().closeAllTransports();

        projectTreeModel.nodeStructureChanged(rootNode);
    }

    TreeNode loadRemoteProjects(ServerResponseListProjects response) {
        TreeNode treeNode = null;
        projectNodeMap.clear();

        try {
            String[] projectList = response.getProjectList();
            Properties[] propertiesList = response.getPropertiesList();

            // Find the server for this project, and add it as a child of the
            // server's node.
            String serverName = response.getServerName();
            ServerTreeNode serverNode = serverNodeMap.get(serverName);
            if (serverNode != null) {
                // We'll replace any existing children with the list we just
                // received.
                serverNode.removeAllChildren();

                // Add all the projects that we received.
                for (int i = 0; i < response.getProjectList().length; i++) {
                    RemoteProjectProperties projectProperties = new RemoteProjectProperties(projectList[i], propertiesList[i]);
                    ProjectTreeNode projectNode = new ProjectTreeNode(projectProperties);

                    // Add this as a child of the server's node.
                    serverNode.add(projectNode);
                    treeNode = serverNode;

                    // And hang on to this for easy reference.
                    projectNodeMap.put(getProjectNodeKey(serverName, projectList[i]), projectNode);
                }
            } else {
                warnProblem("received project list from unknown server: " + serverName);
            }
        } catch (Exception e) {
            warnProblem("Failed to load projects for server: " + response.getServerName());
        }
        return treeNode;
    }

    TreeNode loadRemoteBranches(ServerResponseListBranches response) {
        TreeNode treeNode = null;

        try {
            // Find the server for this project, and add it as a child of the
            // server's node.
            String serverName = response.getServerName();
            ServerTreeNode serverNode = serverNodeMap.get(serverName);
            if (serverNode != null) {
                // Find the project node...
                ProjectTreeNode projectNode = projectNodeMap.get(getProjectNodeKey(serverName, response.getProjectName()));
                if (projectNode != null) {
                    // We'll replace any existing children with the list we just
                    // received.
                    projectNode.removeAllChildren();

                    // Add all the branches that we received.
                    List<ClientBranchInfo> clientBranchInfoList = response.getClientBranchInfoList();
                    for (ClientBranchInfo clientBranchInfo : clientBranchInfoList) {
                        RemoteBranchProperties branchProperties = new RemoteBranchProperties(response.getProjectName(), clientBranchInfo.getBranchName(), clientBranchInfo.getBranchProperties());
                        BranchTreeNode branchNode;
                        if (branchProperties.getIsReadOnlyBranchFlag()) {
                            if (branchProperties.getIsMoveableTagBranchFlag()) {
                                branchNode = new ReadOnlyMoveableTagBranchNode(branchProperties, clientBranchInfo.getBranchName());
                            } else {
                                branchNode = new ReadOnlyBranchNode(branchProperties, clientBranchInfo.getBranchName());
                            }
                        } else {
                            if (branchProperties.getIsReleaseBranchFlag()) {
                                branchNode = new ReleaseBranchNode(branchProperties, clientBranchInfo.getBranchName());
                            } else {
                                branchNode = new ReadWriteBranchNode(branchProperties, clientBranchInfo.getBranchName());
                            }
                        }

                        // Add this as a child of the project's node.
                        projectNode.add(branchNode);

                        // Add .qvcsosignore listener...
                        UserLocationProperties userLocationProperties = new UserLocationProperties(System.getProperty(USER_DIR), QWinFrame.getQWinFrame().getLoggedInUserName());
                        String workfileBaseDirectory = userLocationProperties.getWorkfileLocation(response.getServerName(), response.getProjectName(),
                            clientBranchInfo.getBranchName());
                        IgnoreListenersManager.getInstance().createOrResetListener(workfileBaseDirectory + File.separator);
                    }
                    treeNode = projectNode;
                }
            } else {
                warnProblem("received project list from unknown server: " + serverName);
            }
        } catch (Exception e) {
            warnProblem("Failed to load projects for server: " + response.getServerName());
        }
        return treeNode;
    }

    private String getProjectNodeKey(final String serverName, final String projectName) {
        return serverName + ":" + projectName;
    }

    /**
     * Return an alphabetical list of projects that we know about.
     * @return an alphabetical list of projects that we know about.
     */
    public final synchronized List<String> getProjectNames() {
        List<String> projectList = Collections.synchronizedList(new ArrayList<>());
        DefaultServerTreeNode rootNode = (DefaultServerTreeNode) getTreeModel().getRoot();
        Enumeration enumerator = rootNode.preorderEnumeration();
        while (enumerator.hasMoreElements()) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enumerator.nextElement();
            if (currentNode instanceof ProjectTreeNode projectNode) {
                projectList.add(projectNode.getProjectName());
            }
        }
        Collections.sort(projectList);
        return projectList;
    }

    synchronized DefaultMutableTreeNode findNode(TreePath treePath) {
        DefaultMutableTreeNode foundNode = null;
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) projectTreeModel.getRoot();
        Enumeration enumeration = rootNode.depthFirstEnumeration();
        while (enumeration.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) enumeration.nextElement();
            TreePath nodePath = new TreePath(node.getPath());
            if (0 == nodePath.toString().compareTo(treePath.toString())) {
                foundNode = node;
                break;
            }
        }
        return foundNode;
    }

    /**
     * Get a map of peer branches.
     *
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @return a map of branch nodes that are peers to the given branch.
     */
    public final synchronized Map<String, BranchTreeNode> getPeerBranches(final String serverName, final String projectName,
                                                              final String branchName) {
        Map<String, BranchTreeNode> branchList = Collections.synchronizedMap(new TreeMap<>());
        ProjectTreeNode projectNode = findProjectTreeNode(serverName, projectName);
        Enumeration enumerator = projectNode.preorderEnumeration();
        while (enumerator.hasMoreElements()) {
            DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enumerator.nextElement();
            if (currentNode instanceof BranchTreeNode branchNode) {
                if (branchNode.getBranchName().equals(branchName)) {
                    continue;
                }
                branchList.put(branchNode.getBranchName(), branchNode);
            }
        }
        return branchList;
    }

    private void deleteAppendedPathEntry(String serverName, String projectName, String branchName, String[] directorySegments) {
        String appendedPathSetKey = getAppendedPathSetKey(serverName, projectName, branchName);
        String appendedPath = buildAppendedPath(directorySegments);
        Set<String> appendedPathSet = appendedPathsMap.get(appendedPathSetKey);
        if (appendedPathSet != null) {
            appendedPathSet.remove(appendedPath);
        }
    }

    /**
     * Get the key used to get the set of appended paths for a given server/project/branch.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @return a key to the Set of appendedPath Strings for the given server/project/branch.
     */
    private String getAppendedPathSetKey(String serverName, String projectName, String branchName) {
        return serverName + ":" + projectName + ":" + branchName;
    }

    /**
     * Get the Set of appendedPaths for the given server/project/branch.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @return the Set of appended paths for the given server/project/branch.
     */
    public Set<String> getAppendedPathsSet(String serverName, String projectName, String branchName) {
        return appendedPathsMap.get(getAppendedPathSetKey(serverName, projectName, branchName));
    }
}
