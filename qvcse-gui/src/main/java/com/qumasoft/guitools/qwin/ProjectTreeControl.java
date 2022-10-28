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
import com.qumasoft.guitools.qwin.dialog.DefineWorkfileLocationDialog;
import com.qumasoft.guitools.qwin.operation.OperationAddDirectory;
import com.qumasoft.guitools.qwin.operation.OperationAddServer;
import com.qumasoft.guitools.qwin.operation.OperationAutoAddFiles;
import com.qumasoft.guitools.qwin.operation.OperationBaseClass;
import com.qumasoft.guitools.qwin.operation.OperationCreateTag;
import com.qumasoft.guitools.qwin.operation.OperationDefineBranch;
import com.qumasoft.guitools.qwin.operation.OperationDeleteBranch;
import com.qumasoft.guitools.qwin.operation.OperationDeleteDirectory;
import com.qumasoft.guitools.qwin.operation.OperationEditServerProperties;
import com.qumasoft.guitools.qwin.operation.OperationGetDirectory;
import com.qumasoft.guitools.qwin.operation.OperationMaintainBranch;
import com.qumasoft.guitools.qwin.operation.OperationPromoteFilesFromChildBranch;
import com.qumasoft.guitools.qwin.operation.OperationRemoveServer;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.WorkfileDirectoryManagerInterface;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * Project tree control. JPanel container of our tree control. This is a singleton.
 *
 * @author Jim Voris
 */
public final class ProjectTreeControl extends javax.swing.JPanel {
    private static final long serialVersionUID = 5244866537942643753L;

    private static final ProjectTreeControl PROJECT_TREE_CONTROL = new ProjectTreeControl();
    private DefaultMutableTreeNode previousSelectedNode;
    private DefaultMutableTreeNode lastSelectedNode;
    private final ProjectTreeModel projectTreeModel;
    private AbstractProjectProperties activeProject;
    private String activeBranch;
    private ServerProperties serverProperties;
    private final ImageIcon serversIcon;
    private final ImageIcon serverIcon;
    private final ImageIcon projectIcon;
    private final ImageIcon readOnlyBranchIcon;
    private final ImageIcon readOnlyMoveableTagBranchIcon;
    private final ImageIcon readWriteBranchIconForTrunk;
    private final ImageIcon readWriteBranchIconForFeature;
    private final ImageIcon readWriteBranchIconForRelease;
    private final ImageIcon branchCemeteryIcon;
    // Popup menu items.
    private final ActionDefineWorkfileLocation actionDefineWorkfileLocation;
    private final ActionAddDirectory actionAddDirectory;
    private final ActionDeleteDirectory actionDeleteDirectory;
    private final ActionAutoAddFiles actionAutoAddFiles;
    private final ActionGetDirectory actionGetDirectory;
    private final ActionDefineBranchFromProject actionDefineBranchFromProject;
    private final ActionDefineBranch actionDefineBranch;
    private final ActionMaintainBranch actionMaintainBranch;
    private final ActionDeleteBranch actionDeleteBranch;
    private final ActionExpandTree actionExpandTree;
    private final ActionCollapseTree actionCollapseTree;
    private final ActionPromoteFromChild actionPromoteFromChild;
    private final ActionCreateTag actionCreateTag;
    // Other popup menu items.
    private final ActionAddServer actionAddServer;
    private final ActionRemoveServer actionRemoveServer;
    private final ActionServerProperties actionServerProperties;

    /**
     * Get the singleton instance of the project tree control.
     *
     * @return the singleton instance of the project tree control.
     */
    public static ProjectTreeControl getInstance() {
        return PROJECT_TREE_CONTROL;
    }

    /**
     * Creates new form ProjectTreeControl.
     */
    private ProjectTreeControl() {
        this.actionServerProperties = new ActionServerProperties("Server Properties...");
        this.actionRemoveServer = new ActionRemoveServer("Remove Server...");
        this.actionAddServer = new ActionAddServer("Add Server...");
        this.actionPromoteFromChild = new ActionPromoteFromChild("Promote changes from child branch");
        this.actionCollapseTree = new ActionCollapseTree("Collapse Tree");
        this.actionExpandTree = new ActionExpandTree("Expand Tree");
        this.actionDeleteBranch = new ActionDeleteBranch("Delete Branch...");
        this.actionMaintainBranch = new ActionMaintainBranch("Branch Properties...");
        this.actionDefineBranch = new ActionDefineBranch("Define Branch...");
        this.actionDefineBranchFromProject = new ActionDefineBranchFromProject("Define Branch...");
        this.actionGetDirectory = new ActionGetDirectory("Get...");
        this.actionAutoAddFiles = new ActionAutoAddFiles("Auto-Add Files/Directories...");
        this.actionDeleteDirectory = new ActionDeleteDirectory("Delete Directory...");
        this.actionAddDirectory = new ActionAddDirectory("Add Directory...");
        this.actionDefineWorkfileLocation = new ActionDefineWorkfileLocation("Define Workfile Location...");
        this.actionCreateTag = new ActionCreateTag("Create Tag...");
        this.readWriteBranchIconForTrunk = new ImageIcon(ClassLoader.getSystemResource("images/readwriteview.png"), "Trunk Branch");
        this.readWriteBranchIconForFeature = new ImageIcon(ClassLoader.getSystemResource("images/readwriteviewFeature.png"), "Read Write Feature Branch");
        this.readWriteBranchIconForRelease = new ImageIcon(ClassLoader.getSystemResource("images/readwriteviewRelease.png"), "Read Write Release Branch");
        this.readOnlyBranchIcon = new ImageIcon(ClassLoader.getSystemResource("images/readonlyview.png"), "Read Only Branch");
        this.readOnlyMoveableTagBranchIcon = new ImageIcon(ClassLoader.getSystemResource("images/readonlymoveabletagview.png"), "Read Only Moveable Tag Branch");
        this.branchCemeteryIcon = new ImageIcon(ClassLoader.getSystemResource("images/headstone-icon.png"), "Branch Cemetery");
        this.projectIcon = new ImageIcon(ClassLoader.getSystemResource("images/project.png"), "Project");
        this.serverIcon = new ImageIcon(ClassLoader.getSystemResource("images/server.png"), "Server");
        this.serversIcon = new ImageIcon(ClassLoader.getSystemResource("images/servers.png"), "Servers");
        initComponents();
        addPopupMenuItems();
        projectTreeModel = new ProjectTreeModel();
        projectTree.setModel(projectTreeModel.getTreeModel());
        projectTree.setShowsRootHandles(true);
        projectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addListeners();

        // Set the tree model so others can easily find it.
        QWinFrame.getQWinFrame().setTreeModel(projectTreeModel);

        // Set the tree control so others can easily find it.
        QWinFrame.getQWinFrame().setTreeControl(this);

        projectTree.setFont(QWinFrame.getQWinFrame().getFont(QWinFrame.getQWinFrame().getFontSize()));
    }

    JTree getProjectJTreeControl() {
        return projectTree;
    }

    /**
     * Set the font size that we'll use.
     *
     * @param fontSize the font size.
     */
    public void setFontSize(int fontSize) {
        projectTree.setFont(QWinFrame.getQWinFrame().getFont(fontSize + 1));
        setMenusFontSize(fontSize + 1);
    }

    private void setMenusFontSize(int fontSize) {
        setMenuFontSize(fontSize, directoryPopupMenu);
        setMenuFontSize(fontSize, projectPopupMenu);
        setMenuFontSize(fontSize, rootServerPopupMenu);
        setMenuFontSize(fontSize, serverPopupMenu);
        setMenuFontSize(fontSize, branchPopupMenu);
    }

    private void setMenuFontSize(int fontSize, JPopupMenu popupMenu) {
        Font font = QWinFrame.getQWinFrame().getFont(fontSize);
        MenuElement[] menuElements = popupMenu.getSubElements();
        for (MenuElement menuElement : menuElements) {
            menuElement.getComponent().setFont(font);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        projectPopupMenu = new javax.swing.JPopupMenu();
        rootServerPopupMenu = new javax.swing.JPopupMenu();
        serverPopupMenu = new javax.swing.JPopupMenu();
        directoryPopupMenu = new javax.swing.JPopupMenu();
        branchPopupMenu = new javax.swing.JPopupMenu();
        scrollPane = new javax.swing.JScrollPane();
        projectTree = new javax.swing.JTree();

        projectPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        rootServerPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        serverPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        directoryPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        branchPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        setLayout(new java.awt.BorderLayout());

        projectTree.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        EnterpriseTreeCellRenderer renderer = new EnterpriseTreeCellRenderer(serversIcon, serverIcon, projectIcon, readOnlyBranchIcon, readOnlyMoveableTagBranchIcon,
            readWriteBranchIconForTrunk, readWriteBranchIconForFeature, readWriteBranchIconForRelease, branchCemeteryIcon);
        renderer.setLeafIcon(renderer.getClosedIcon());
        projectTree.setCellRenderer(renderer);
        scrollPane.setViewportView(projectTree);

        add(scrollPane);
    }// </editor-fold>//GEN-END:initComponents

    private void addPopupMenuItems() {
        Font menuFont = QWinFrame.getQWinFrame().getFont(QWinFrame.getQWinFrame().getFontSize() + 1);

        // The root popup menu
        JMenuItem menuItem = rootServerPopupMenu.add(actionAddServer);
        menuItem.setFont(menuFont);

        // The server popup menu
        menuItem = serverPopupMenu.add(actionServerProperties);
        menuItem.setFont(menuFont);

        menuItem = serverPopupMenu.add(actionRemoveServer);
        menuItem.setFont(menuFont);

        // =====================================================================
        // =====================================================================

        // The project popup menu
        menuItem = projectPopupMenu.add(actionDefineBranchFromProject);
        menuItem.setFont(menuFont);

        // =====================================================================
        // =====================================================================

        // The view popup menu
        menuItem = branchPopupMenu.add(actionDefineWorkfileLocation);
        menuItem.setFont(menuFont);

        menuItem = branchPopupMenu.add(actionDefineBranch);
        menuItem.setFont(menuFont);

        menuItem = branchPopupMenu.add(actionMaintainBranch);
        menuItem.setFont(menuFont);

        menuItem = branchPopupMenu.add(actionDeleteBranch);
        menuItem.setFont(menuFont);

        // =====================================================================
        branchPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = branchPopupMenu.add(actionExpandTree);
        menuItem.setFont(menuFont);

        menuItem = branchPopupMenu.add(actionCollapseTree);
        menuItem.setFont(menuFont);

        // =====================================================================
        branchPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = branchPopupMenu.add(actionAddDirectory);
        menuItem.setFont(menuFont);

        menuItem = branchPopupMenu.add(actionAutoAddFiles);
        menuItem.setFont(menuFont);

        // =====================================================================
        branchPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = branchPopupMenu.add(actionGetDirectory);
        menuItem.setFont(menuFont);

        // =====================================================================
        branchPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = branchPopupMenu.add(actionCreateTag);
        menuItem.setFont(menuFont);

        // =====================================================================
        branchPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = branchPopupMenu.add(actionPromoteFromChild);
        menuItem.setFont(menuFont);

        // =====================================================================
        // =====================================================================

        // The directory popup menu
        menuItem = directoryPopupMenu.add(actionAddDirectory);
        menuItem.setFont(menuFont);

        menuItem = directoryPopupMenu.add(actionDeleteDirectory);
        menuItem.setFont(menuFont);

        menuItem = directoryPopupMenu.add(actionAutoAddFiles);
        menuItem.setFont(menuFont);

        // =====================================================================
        directoryPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = directoryPopupMenu.add(actionExpandTree);
        menuItem.setFont(menuFont);

        menuItem = directoryPopupMenu.add(actionCollapseTree);
        menuItem.setFont(menuFont);

        // =====================================================================
        directoryPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = directoryPopupMenu.add(actionGetDirectory);
        menuItem.setFont(menuFont);
    }

    private void enableAllPopUpOperations() {
        actionAddDirectory.setEnabled(true);
        actionAutoAddFiles.setEnabled(true);
    }

    private void disableAllDirectoryOperations() {
        actionAddDirectory.setEnabled(false);
        actionAutoAddFiles.setEnabled(false);
    }

    private AbstractProjectProperties getProjectProperties() {
        AbstractProjectProperties projectProperties = null;
        if (lastSelectedNode instanceof DirectoryTreeNode) {
            DirectoryTreeNode directoryNode = (DirectoryTreeNode) lastSelectedNode;
            projectProperties = directoryNode.getProjectProperties();
        } else if (lastSelectedNode instanceof ProjectTreeNode) {
            ProjectTreeNode projectTreeNode = (ProjectTreeNode) lastSelectedNode;
            projectProperties = projectTreeNode.getProjectProperties();
        } else if (lastSelectedNode instanceof BranchTreeNode) {
            BranchTreeNode branchTreeNode = (BranchTreeNode) lastSelectedNode;
            projectProperties = branchTreeNode.getProjectProperties();
        }
        return projectProperties;
    }

    private String getAppendedPath() {
        String retVal = "";
        if (lastSelectedNode instanceof DirectoryTreeNode) {
            DirectoryTreeNode directoryNode = (DirectoryTreeNode) lastSelectedNode;
            retVal = directoryNode.getAppendedPath();
        }
        return retVal;
    }

    private String getBranchName() {
        String retVal = QVCSConstants.QVCS_TRUNK_BRANCH;
        if (lastSelectedNode instanceof DirectoryTreeNode) {
            DirectoryTreeNode directoryNode = (DirectoryTreeNode) lastSelectedNode;
            retVal = directoryNode.getBranchName();
        } else if (lastSelectedNode instanceof BranchTreeNode) {
            BranchTreeNode branchTreeNode = (BranchTreeNode) lastSelectedNode;
            retVal = branchTreeNode.getBranchName();
        }
        return retVal;
    }

    /**
     * Get the selected node.
     * @return the selected node.
     */
    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) projectTree.getLastSelectedPathComponent();
    }

    /**
     * Get the active branch node.
     * @return the active branch node.
     */
    public BranchTreeNode getActiveBranchNode() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) projectTree.getLastSelectedPathComponent();
        while (!(node instanceof BranchTreeNode) && node != null) {
            node = (DefaultMutableTreeNode) node.getParent();
        }
        BranchTreeNode branchTreeNode = (BranchTreeNode) node;
        return branchTreeNode;
    }

    /**
     * Get an Enumeration of the branches for the active project.
     * @return an Enumeration of the branches for the active project. This may return null if the user is not within a project (e.g. maybe they have the server node selected).
     */
    public Enumeration getCurrentBranches() {
        TreeNode projectTreeNode;
        DefaultMutableTreeNode node = getSelectedNode();
        if (node instanceof BranchTreeNode) {
            projectTreeNode = node.getParent();
        } else if (node instanceof ProjectTreeNode) {
            projectTreeNode = node;
        } else {
            return null;
        }
        return projectTreeNode.children();
    }

    void addServerMenuItems(javax.swing.JMenu menu) {
        Font menuFont = new Font("Arial", 0, 12);

        menu.removeAll();

        JMenuItem menuItem = menu.add(actionAddServer);
        menuItem.setFont(menuFont);
        menuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);

        if ((lastSelectedNode != null)
                && (lastSelectedNode instanceof ServerTreeNode)) {
            menuItem = menu.add(actionRemoveServer);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_R);

            menuItem = menu.add(actionServerProperties);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_P);
        } else {
            menuItem = menu.add(actionRemoveServer);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_R);
            menuItem.setEnabled(false);

            menuItem = menu.add(actionServerProperties);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_P);
            menuItem.setEnabled(false);
        }
    }

    void addProjectMenuItems(javax.swing.JMenu menu) {
        Font menuFont = new Font("Arial", 0, 12);

        menu.removeAll();

        if (lastSelectedNode != null) {
            if (lastSelectedNode instanceof ProjectTreeNode) {
                JMenuItem menuItem = menu.add(actionDefineBranchFromProject);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
            } else if (lastSelectedNode instanceof BranchTreeNode) {
                JMenuItem menuItem = menu.add(actionDefineWorkfileLocation);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
                menuItem.setEnabled(true);

                menuItem = menu.add(actionAddDirectory);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);

                menuItem = menu.add(actionAutoAddFiles);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_U);

                menuItem = menu.add(actionDefineBranch);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_B);

                menuItem = menu.add(actionPromoteFromChild);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_P);
                BranchTreeNode branchNode = (BranchTreeNode) lastSelectedNode;
                enableBranchMenuItems(branchNode);
            } else if (lastSelectedNode instanceof DirectoryTreeNode) {
                JMenuItem menuItem = menu.add(actionDefineWorkfileLocation);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
                menuItem.setEnabled(false);

                menuItem = menu.add(actionAddDirectory);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);

                menuItem = menu.add(actionAutoAddFiles);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_U);
            } else {
                JMenuItem menuItem = menu.add(actionDefineWorkfileLocation);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
                menuItem.setEnabled(false);

                menuItem = menu.add(actionAddDirectory);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
                menuItem.setEnabled(false);

                menuItem = menu.add(actionAutoAddFiles);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_U);
                menuItem.setEnabled(false);
            }
        } else {
            JMenuItem menuItem = menu.add(actionDefineBranch);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
        }
    }

    private void enableBranchMenuItems(BranchTreeNode branchNode) {
        if (branchNode.isReadOnlyBranch()) {
            actionDefineBranch.setEnabled(false);
            actionAddDirectory.setEnabled(false);
            actionAutoAddFiles.setEnabled(false);
            actionCreateTag.setEnabled(false);
            actionPromoteFromChild.setEnabled(false);
        } else {
            if (branchNode.isReleaseBranch()) {
                actionDefineBranch.setEnabled(false);
                actionAddDirectory.setEnabled(true);
                actionAutoAddFiles.setEnabled(true);
                actionCreateTag.setEnabled(false);
                actionPromoteFromChild.setEnabled(false);
            } else {
                actionDefineBranch.setEnabled(true);
                actionAddDirectory.setEnabled(true);
                actionAutoAddFiles.setEnabled(true);
                actionCreateTag.setEnabled(true);
                actionPromoteFromChild.setEnabled(true);
            }
        }
    }

    private void addListeners() {
        projectTree.addTreeSelectionListener((TreeSelectionEvent treeSelectionEvent) -> {
            previousSelectedNode = lastSelectedNode;
            lastSelectedNode = (DefaultMutableTreeNode) projectTree.getLastSelectedPathComponent();
            if (lastSelectedNode != null) {
                if (lastSelectedNode instanceof ServerTreeNode serverTreeNode) {
                    serverProperties = serverTreeNode.getServerProperties();

                    // There is no active project or branch.
                    activeProject = null;
                    activeBranch = null;

                    // See if we are already logged in to this server...
                    boolean loggedInAlreadyFlag = TransportProxyFactory.getInstance().getTransportProxy(serverProperties) != null;

                    QWinFrame.getQWinFrame().setActiveServer(serverProperties);
                    if (loggedInAlreadyFlag) {
                        // If we are already logged in, then the user is manually
                        // navigating to the server node.... so we clear the
                        // data model with the following call.
                        //
                        // If the user is not already logged in, then the login
                        // process will try to restore the project tree so that
                        // the selected node will be the one the user had
                        // selected when last using the application.  In that
                        // case, we need to skip this next line of code.
                        QWinFrame.getQWinFrame().setCurrentAppendedPath(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME, QVCSConstants.QVCS_TRUNK_BRANCH, "", QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, true);
                    }
                } else if (lastSelectedNode instanceof ProjectTreeNode projectTreeNode) {
                    activeProject = projectTreeNode.getProjectProperties();
                    activeBranch = null;
                    String projectName = projectTreeNode.getProjectName();
                    TransportProxyFactory.getInstance().requestBranchList(serverProperties, projectName);
                    // hide the combo box.
                    QWinFrame.getQWinFrame().getRightFilePane().setCommitComboBoxVisible(false, "");
                    QWinFrame.getQWinFrame().setCurrentAppendedPath(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME, QVCSConstants.QVCS_TRUNK_BRANCH, "", QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, true);
                } else if (lastSelectedNode instanceof BranchTreeNode branchTreeNode) {
                    activeProject = branchTreeNode.getProjectProperties();
                    activeBranch = branchTreeNode.getBranchName();
                    serverProperties = findServerProperties();
                    if (previousSelectedNode instanceof BranchTreeNode previousBranchTreeNode) {
                        QWinFrame.getQWinFrame().setPreviousProjectName(previousBranchTreeNode.getProjectName());
                    }
                    if (lastSelectedNode instanceof ReadOnlyMoveableTagBranchNode branchNode) {
                        // show the combo box.
                        String branchName = branchNode.getBranchName();
                        QWinFrame.getQWinFrame().getRightFilePane().setCommitComboBoxVisible(true, branchName);
                    } else {
                        // hide the combo box.
                        QWinFrame.getQWinFrame().getRightFilePane().setCommitComboBoxVisible(false, "");
                    }
                    QWinFrame.getQWinFrame().setCurrentAppendedPath(branchTreeNode.getProjectProperties().getProjectName(), branchTreeNode.getBranchName(), "", activeProject.getProjectType(), false);
                } else if (lastSelectedNode instanceof DefaultProjectTreeNode) {
                    QWinFrame.getQWinFrame().setCurrentAppendedPath(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME, QVCSConstants.QVCS_TRUNK_BRANCH, "", QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, true);
                    // hide the combo box.
                    QWinFrame.getQWinFrame().getRightFilePane().setCommitComboBoxVisible(false, "");
                    activeProject = null;
                    activeBranch = null;
                } else if (lastSelectedNode instanceof DefaultServerTreeNode) {
                    QWinFrame.getQWinFrame().setCurrentAppendedPath(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME, QVCSConstants.QVCS_TRUNK_BRANCH, "", QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, true);
                    // hide the combo box.
                    QWinFrame.getQWinFrame().getRightFilePane().setCommitComboBoxVisible(false, "");
                    activeProject = null;
                    activeBranch = null;
                } else if (lastSelectedNode instanceof DirectoryTreeNode directoryNode) {
                    activeProject = directoryNode.getProjectProperties();
                    activeBranch = directoryNode.getBranchName();
                    serverProperties = findServerProperties();

                    // Expand the new selection...
                    Enumeration expandEnumeration = directoryNode.children();
                    while (expandEnumeration.hasMoreElements()) {
                        DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) expandEnumeration.nextElement();
                        TreePath childTreePath = treeSelectionEvent.getNewLeadSelectionPath().pathByAddingChild(childNode);
                        getProjectJTreeControl().makeVisible(childTreePath);
                    }

                    // Collapse the old selection only if the new selection is not a child of the old selection...
                    if (treeSelectionEvent.getOldLeadSelectionPath() != null) {
                        if (!treeSelectionEvent.getOldLeadSelectionPath().isDescendant(treeSelectionEvent.getNewLeadSelectionPath())) {
                            DefaultMutableTreeNode oldSelection = projectTreeModel.findNode(treeSelectionEvent.getOldLeadSelectionPath());
                            if (oldSelection instanceof DirectoryTreeNode) {
                                DirectoryTreeNode oldDirectoryTreeNode = (DirectoryTreeNode) oldSelection;
                                if (0 == activeProject.getProjectName().compareTo(oldDirectoryTreeNode.getProjectProperties().getProjectName())
                                        && (0 == activeBranch.compareTo(oldDirectoryTreeNode.getBranchName()))) {
                                    getProjectJTreeControl().collapsePath(treeSelectionEvent.getOldLeadSelectionPath());
                                }
                            }
                        }
                    }

                    QWinFrame.getQWinFrame().setCurrentAppendedPath(directoryNode.getProjectProperties().getProjectName(), directoryNode.getBranchName(), directoryNode.getAppendedPath(),
                            activeProject.getProjectType(), false);
                } else if (lastSelectedNode instanceof CemeteryTreeNode cemeteryNode) {
                    logMessage("Cemetery selected.");
                    QWinFrame.getQWinFrame().setCurrentAppendedPath(cemeteryNode.getProjectProperties().getProjectName(), cemeteryNode.getBranchName(), cemeteryNode.getAppendedPath(),
                            activeProject.getProjectType(), false);
                }
            }
        });
        projectTree.addMouseListener(new MouseInputAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || ((e.getButton() == MouseEvent.BUTTON3) && (0 != (e.getModifiers() & MouseEvent.MOUSE_RELEASED)))) {
                    // Make sure the node under the mouse is the one
                    // that is selected.
                    TreePath selPath = projectTree.getPathForLocation(e.getX(), e.getY());
                    projectTree.setSelectionPath(selPath);

                    // Turn on all popups, and then figure out which ones
                    // to turn off
                    enableAllPopUpOperations();

                    if (lastSelectedNode != null) {
                        if (lastSelectedNode instanceof ProjectTreeNode) {
                            projectPopupMenu.show(projectTree, e.getX(), e.getY());
                        } else if (lastSelectedNode instanceof BranchTreeNode) {
                            actionDefineWorkfileLocation.setEnabled(true);
                            BranchTreeNode branchNode = (BranchTreeNode) lastSelectedNode;
                            enableBranchMenuItems(branchNode);
                            branchPopupMenu.show(projectTree, e.getX(), e.getY());
                        } else if (lastSelectedNode instanceof DirectoryTreeNode) {
                            directoryPopupMenu.show(projectTree, e.getX(), e.getY());
                        } else if (lastSelectedNode instanceof DefaultProjectTreeNode) {
                            disableAllDirectoryOperations();
                        } else if (lastSelectedNode instanceof DefaultServerTreeNode) {
                            rootServerPopupMenu.show(projectTree, e.getX(), e.getY());
                        } else if (lastSelectedNode instanceof ServerTreeNode) {
                            serverPopupMenu.show(projectTree, e.getX(), e.getY());
                        }
                    }
                }
            }
        });
    }

    /**
     * Get the active project.
     *
     * @return the project properties for the active project.
     */
    public AbstractProjectProperties getActiveProject() {
        return activeProject;
    }

    /**
     * Get the active branch.
     *
     * @return the name of the active branch.
     */
    public String getActiveBranch() {
        return activeBranch;
    }

    String getProjectName() {
        String projectName = null;
        if (getActiveProject() != null) {
            projectName = getActiveProject().getProjectName();
        }
        return projectName;
    }

    /**
     * Get the string for the active server. The active server is the server node that is the parent (or ancestor) of the currently selected node.
     *
     * @return the name of the active server, i.e. the one that is the ancestor of the currently selected node.
     */
    public String getActiveServerName() {
        String serverName = null;
        ServerProperties serverProps = getActiveServer();
        if (serverProps != null) {
            serverName = serverProps.getServerName();
        }
        return serverName;
    }

    /**
     * Get the server properties for the active server. The active server is the server node that is the parent (or ancestor) of the currently selected node.
     *
     * @return the server properties for the current server; i.e. the server properties for the server node that is the ancestor of the currently selected node.
     */
    public ServerProperties getActiveServer() {
        return serverProperties;
    }

    private ServerProperties findServerProperties() {
        ServerProperties serverProps = null;
        if (lastSelectedNode != null) {
            DefaultMutableTreeNode node = lastSelectedNode;

            while (node != null) {
                if (node instanceof ServerTreeNode) {
                    ServerTreeNode serverTreeNode = (ServerTreeNode) node;
                    serverProps = serverTreeNode.getServerProperties();
                    break;
                }
                node = (DefaultMutableTreeNode) node.getParent();
            }
        }
        return serverProps;
    }

    /**
     * Force selection of the root node.
     */
    public void selectRootNode() {
        // Select the root node -- there is no active project.
        projectTree.setSelectionPath(new TreePath(projectTree.getModel().getRoot()));
    }

    /**
     * Select a given node on the tree control.
     *
     * @param nodeToSelect the node to select.
     */
    public void selectNode(TreeNode nodeToSelect) {
        // First, figure out the depth of the node
        if (nodeToSelect != null) {
            int depth = 0;
            TreeNode parent = nodeToSelect;
            while ((parent = parent.getParent()) != null) {
                depth++;
            }

            Object[] path = new Object[depth + 1];
            parent = nodeToSelect;
            for (int i = depth; i >= 0; i--) {
                path[depth--] = parent;
                parent = parent.getParent();
            }

            // Select the node and make it visible.
            TreePath treePath = new TreePath(path);
            projectTree.setSelectionPath(treePath);
            projectTree.scrollPathToVisible(treePath);
        }
    }

    File getCurrentWorkfileDirectory() {
        File currentWorkfileDirectory = null;
        // Get the current array of directory managers.  The first directory manager is the one
        // associated with the currently selected node on the tree...
        DirectoryManagerInterface directoryManagers[] = QWinFrame.getQWinFrame().getCurrentDirectoryManagers();
        if (directoryManagers != null) {
            DirectoryManagerInterface directoryManager = directoryManagers[0];
            WorkfileDirectoryManagerInterface workfileDirectoryManager = directoryManager.getWorkfileDirectoryManager();
            currentWorkfileDirectory = new File(workfileDirectoryManager.getWorkfileDirectory());
        }
        return currentWorkfileDirectory;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu branchPopupMenu;
    private javax.swing.JPopupMenu directoryPopupMenu;
    private javax.swing.JPopupMenu projectPopupMenu;
    private javax.swing.JTree projectTree;
    private javax.swing.JPopupMenu rootServerPopupMenu;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JPopupMenu serverPopupMenu;
    // End of variables declaration//GEN-END:variables

    static class ActionDefineWorkfileLocation extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionDefineWorkfileLocation(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            QWinFrame qWinFrame = QWinFrame.getQWinFrame();
            DefineWorkfileLocationDialog defineWorkfileLocationDialog = new DefineWorkfileLocationDialog(qWinFrame);
            defineWorkfileLocationDialog.setVisible(true);
            if (defineWorkfileLocationDialog.getIsOK()) {
                qWinFrame.getUserLocationProperties().setWorkfileLocation(qWinFrame.getServerName(), qWinFrame.getProjectName(), qWinFrame.getBranchName(),
                        defineWorkfileLocationDialog.getWorkfileLocation());
                qWinFrame.getUserLocationProperties().saveProperties();
                qWinFrame.setUserWorkfileDirectory(defineWorkfileLocationDialog.getWorkfileLocation());
                qWinFrame.setRefreshRequired(true);
                qWinFrame.setCurrentAppendedPath(qWinFrame.getProjectName(), qWinFrame.getBranchName(), qWinFrame.getAppendedPath(), qWinFrame.getProjectType(), false);
            }
        }
    }

    class ActionAddDirectory extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionAddDirectory(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass addDirectoryOperation = new OperationAddDirectory(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(), getBranchName(),
                    getAppendedPath(), QWinFrame.getQWinFrame().getUserLocationProperties(), getProjectProperties(), getCurrentWorkfileDirectory());
            addDirectoryOperation.executeOperation();
        }
    }

    class ActionDeleteDirectory extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionDeleteDirectory(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationDeleteDirectory deleteDirectoryOperation = new OperationDeleteDirectory(QWinFrame.getQWinFrame().getActiveServerProperties(), getProjectName(), getBranchName(), getAppendedPath());
            deleteDirectoryOperation.executeOperation();
        }
    }

    class ActionAutoAddFiles extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionAutoAddFiles(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass autoAddFilesOperation = new OperationAutoAddFiles(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(), getBranchName(),
                    getAppendedPath(), QWinFrame.getQWinFrame().getUserLocationProperties(), getCurrentWorkfileDirectory());
            autoAddFilesOperation.executeOperation();
        }
    }

    class ActionGetDirectory extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionGetDirectory(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass getDirectoryOperation = new OperationGetDirectory(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(), getBranchName(),
                    getAppendedPath(), QWinFrame.getQWinFrame().getUserLocationProperties(), getProjectProperties(), getCurrentWorkfileDirectory());
            getDirectoryOperation.executeOperation();
        }
    }

    static class ActionAddServer extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionAddServer(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass addServerOperation = new OperationAddServer(QWinFrame.getQWinFrame().getUserLocationProperties());
            addServerOperation.executeOperation();
        }
    }

    static class ActionServerProperties extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionServerProperties(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass editServerPropertiesOperation = new OperationEditServerProperties(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            editServerPropertiesOperation.executeOperation();
        }
    }

    static class ActionRemoveServer extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionRemoveServer(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass removeServerOperation = new OperationRemoveServer(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            removeServerOperation.executeOperation();
        }
    }

    class ActionDefineBranchFromProject extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionDefineBranchFromProject(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationDefineBranch defineBranchOperation = new OperationDefineBranch(QWinFrame.getQWinFrame().getActiveServerProperties(), getProjectName(), QVCSConstants.QVCS_TRUNK_BRANCH);
            defineBranchOperation.executeOperation();
        }
    }

    class ActionDefineBranch extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionDefineBranch(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationDefineBranch defineBranchOperation = new OperationDefineBranch(QWinFrame.getQWinFrame().getActiveServerProperties(), getProjectName(), getBranchName());
            defineBranchOperation.executeOperation();
        }
    }

    class ActionMaintainBranch extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionMaintainBranch(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationMaintainBranch maintainBranchOperation = new OperationMaintainBranch(QWinFrame.getQWinFrame().getActiveServerProperties(), getProjectName(), getBranchName(),
                    (RemoteBranchProperties) getProjectProperties());
            maintainBranchOperation.executeOperation();
        }
    }

    class ActionDeleteBranch extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionDeleteBranch(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationDeleteBranch deleteBranchOperation = new OperationDeleteBranch(QWinFrame.getQWinFrame().getActiveServerProperties(), getProjectName(), getBranchName());
            deleteBranchOperation.executeOperation();
        }
    }

    class ActionExpandTree extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionExpandTree(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath selectionPath = getProjectJTreeControl().getSelectionPath();
            getProjectJTreeControl().expandPath(selectionPath);
            expandNode(selectionPath, lastSelectedNode);
        }

        /**
         * Recursive method used to expand a node (and all its children).
         *
         * @param node the node to expand.
         */
        private void expandNode(TreePath anchorTreePath, DefaultMutableTreeNode node) {
            Enumeration enumeration = node.children();
            while (enumeration.hasMoreElements()) {
                DefaultMutableTreeNode childNode = (DefaultMutableTreeNode) enumeration.nextElement();
                TreePath childTreePath = anchorTreePath.pathByAddingChild(childNode);
                getProjectJTreeControl().makeVisible(childTreePath);
                if (!childNode.isLeaf()) {
                    expandNode(childTreePath, childNode);
                }
            }
        }
    }

    class ActionCollapseTree extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionCollapseTree(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            TreePath selectionPath = getProjectJTreeControl().getSelectionPath();
            collapseNode(selectionPath);
        }

        /**
         * Recursive method used to expand a node (and all its children).
         *
         * @param node the node to expand.
         */
        private void collapseNode(TreePath anchorTreePath) {
            TreeMap<String, TreePath> map = new TreeMap<>();
            Enumeration<TreePath> enumeration = getProjectJTreeControl().getExpandedDescendants(anchorTreePath);
            while (enumeration.hasMoreElements()) {
                TreePath childTreePath = enumeration.nextElement();
                // Subtract the path count from 10,000 so the deeper children will sort first -- i.e.
                // we want to collapse the deepest children first, which will happen as a consequence of
                // subtracting the path count from 10,000.
                String mapKey = String.format("%010d%s", 10000 - childTreePath.getPathCount(), childTreePath.toString());
                map.put(mapKey, childTreePath);
            }
            // Collapse the deepest children first.
            Iterator<TreePath> it = map.values().iterator();
            while (it.hasNext()) {
                TreePath childPath = it.next();
                getProjectJTreeControl().collapsePath(childPath);
            }
            getProjectJTreeControl().collapsePath(anchorTreePath);
        }
    }

    class ActionPromoteFromChild extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionPromoteFromChild(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass promoteFromChildBranchOperation = new OperationPromoteFilesFromChildBranch(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties(), getCurrentWorkfileDirectory());
            promoteFromChildBranchOperation.executeOperation();
        }
    }

    class ActionCreateTag extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionCreateTag(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass createTagOperation = new OperationCreateTag(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            createTagOperation.executeOperation();
        }
    }

    static class EnterpriseTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 3531447549589503366L;

        private final ImageIcon iconServers;
        private final ImageIcon iconServer;
        private final ImageIcon iconProject;
        private final ImageIcon iconReadOnlyBranch;
        private final ImageIcon iconReadOnlyMoveableTagBranch;
        private final ImageIcon iconReadWriteTrunkBranch;
        private final ImageIcon iconReadWriteFeatureBranch;
        private final ImageIcon iconReadWriteReleaseBranch;
        private final ImageIcon iconBranchCemetery;

        EnterpriseTreeCellRenderer(ImageIcon servers, ImageIcon server, ImageIcon project, ImageIcon readOnlyBranch, ImageIcon readOnlyMoveableTagBranch, ImageIcon readWriteBranch,
                ImageIcon featureBranch, ImageIcon releaseBranch, ImageIcon branchCemetery) {
            iconServers = servers;
            iconServer = server;
            iconProject = project;
            iconReadOnlyBranch = readOnlyBranch;
            iconReadOnlyMoveableTagBranch = readOnlyMoveableTagBranch;
            iconReadWriteTrunkBranch = readWriteBranch;
            iconReadWriteFeatureBranch = featureBranch;
            iconReadWriteReleaseBranch = releaseBranch;
            iconBranchCemetery = branchCemetery;
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree,
                                                      Object value,
                                                      boolean sel,
                                                      boolean expanded,
                                                      boolean leaf,
                                                      int row,
                                                      boolean hasFocus) {

            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            if (value instanceof DefaultServerTreeNode) {
                setIcon(iconServers);
            } else if (value instanceof ServerTreeNode) {
                setIcon(iconServer);
            } else if (value instanceof ProjectTreeNode) {
                setIcon(iconProject);
            } else if (value instanceof CemeteryTreeNode) {
                setIcon(iconBranchCemetery);
            } else if (value instanceof ReadOnlyBranchNode branchNode) {
                setText(branchNode.getBranchName());
                setIcon(iconReadOnlyBranch);
            } else if (value instanceof ReadOnlyMoveableTagBranchNode branchNode) {
                setText(branchNode.getBranchName());
                setIcon(iconReadOnlyMoveableTagBranch);
            } else if (value instanceof ReadWriteBranchNode branchNode) {
                String branchName = branchNode.getBranchName();
                setText(branchNode.getBranchName());
                if (0 == branchName.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
                    setIcon(iconReadWriteTrunkBranch);
                } else {
                    setIcon(iconReadWriteFeatureBranch);
                }
            } else if (value instanceof ReleaseBranchNode branchNode) {
                setText(branchNode.getBranchName());
                setIcon(iconReadWriteReleaseBranch);
            }

            return this;
        }
    }

}
