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
package com.qumasoft.guitools.qwin;

import com.qumasoft.guitools.qwin.operation.OperationAddDirectory;
import com.qumasoft.guitools.qwin.operation.OperationAddServer;
import com.qumasoft.guitools.qwin.operation.OperationAutoAddFiles;
import com.qumasoft.guitools.qwin.operation.OperationBaseClass;
import com.qumasoft.guitools.qwin.operation.OperationDefineView;
import com.qumasoft.guitools.qwin.operation.OperationDeleteDirectory;
import com.qumasoft.guitools.qwin.operation.OperationDeleteView;
import com.qumasoft.guitools.qwin.operation.OperationEditServerProperties;
import com.qumasoft.guitools.qwin.operation.OperationGetDirectory;
import com.qumasoft.guitools.qwin.operation.OperationLabelDirectory;
import com.qumasoft.guitools.qwin.operation.OperationMaintainView;
import com.qumasoft.guitools.qwin.operation.OperationPromoteFilesFromChildBranch;
import com.qumasoft.guitools.qwin.operation.OperationRemoveServer;
import com.qumasoft.guitools.qwin.operation.OperationUnLabelDirectory;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ClientRequestMoveFileData;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkfileDirectoryManagerInterface;
import java.awt.Component;
import java.awt.Font;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.MenuElement;
import javax.swing.SwingUtilities;
import javax.swing.TransferHandler;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
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
    private DefaultMutableTreeNode lastSelectedNode;
    private final ProjectTreeModel projectTreeModel;
    private AbstractProjectProperties activeProject;
    private String activeView;
    private ServerProperties serverProperties;
    private final ImageIcon serversIcon;
    private final ImageIcon serverIcon;
    private final ImageIcon projectIcon;
    private final ImageIcon readOnlyViewIcon;
    private final ImageIcon readWriteViewIcon;
    // Popup menu items.
    private final ActionDefineWorkfileLocation actionDefineWorkfileLocation;
    private final ActionAddDirectory actionAddDirectory;
    private final ActionDeleteDirectory actionDeleteDirectory;
    private final ActionAutoAddFiles actionAutoAddFiles;
    private final ActionGetDirectory actionGetDirectory;
    private final ActionLabelDirectory actionLabelDirectory;
    private final ActionUnLabelDirectory actionUnLabelDirectory;
    private final ActionDefineView actionDefineView;
    private final ActionMaintainView actionMaintainView;
    private final ActionDeleteView actionDeleteView;
    private final ActionExpandTree actionExpandTree;
    private final ActionCollapseTree actionCollapseTree;
    private final ActionPromoteFromChild actionPromoteFromChild;
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
        this.actionDeleteView = new ActionDeleteView("Delete View...");
        this.actionMaintainView = new ActionMaintainView("View Properties...");
        this.actionDefineView = new ActionDefineView("Define View...");
        this.actionUnLabelDirectory = new ActionUnLabelDirectory("Remove Label...");
        this.actionLabelDirectory = new ActionLabelDirectory("Apply Label...");
        this.actionGetDirectory = new ActionGetDirectory("Get...");
        this.actionAutoAddFiles = new ActionAutoAddFiles("Auto-Add Files/Directories...");
        this.actionDeleteDirectory = new ActionDeleteDirectory("Delete Directory...");
        this.actionAddDirectory = new ActionAddDirectory("Add Directory...");
        this.actionDefineWorkfileLocation = new ActionDefineWorkfileLocation("Define Workfile Location...");
        this.readWriteViewIcon = new ImageIcon(ClassLoader.getSystemResource("images/readwriteview.png"), "Read Write View");
        this.readOnlyViewIcon = new ImageIcon(ClassLoader.getSystemResource("images/readonlyview.png"), "Read Only View");
        this.projectIcon = new ImageIcon(ClassLoader.getSystemResource("images/project.png"), "Project");
        this.serverIcon = new ImageIcon(ClassLoader.getSystemResource("images/server.png"), "Server");
        this.serversIcon = new ImageIcon(ClassLoader.getSystemResource("images/servers.png"), "Servers");
        initComponents();
        addPopupMenuItems();
        projectTreeModel = new ProjectTreeModel();
        m_ProjectTree.setModel(projectTreeModel.getTreeModel());
        m_ProjectTree.setShowsRootHandles(true);
        m_ProjectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        addListeners();

        // Set the tree model so others can easily find it.
        QWinFrame.getQWinFrame().setTreeModel(projectTreeModel);

        // Set the tree control so others can easily find it.
        QWinFrame.getQWinFrame().setTreeControl(this);

        // Enable the tree as a drop target.
        initDragAndDrop();

        m_ProjectTree.setFont(QWinFrame.getQWinFrame().getFont(QWinFrame.getQWinFrame().getFontSize()));
    }

    JTree getProjectJTreeControl() {
        return m_ProjectTree;
    }

    /**
     * Set the font size that we'll use.
     *
     * @param fontSize the font size.
     */
    public void setFontSize(int fontSize) {
        m_ProjectTree.setFont(QWinFrame.getQWinFrame().getFont(fontSize + 1));
        setMenusFontSize(fontSize + 1);
    }

    private void setMenusFontSize(int fontSize) {
        setMenuFontSize(fontSize, m_DirectoryPopupMenu);
        setMenuFontSize(fontSize, m_ProjectPopupMenu);
        setMenuFontSize(fontSize, m_RootServerPopupMenu);
        setMenuFontSize(fontSize, m_ServerPopupMenu);
        setMenuFontSize(fontSize, m_ViewPopupMenu);
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

        m_ProjectPopupMenu = new javax.swing.JPopupMenu();
        m_RootServerPopupMenu = new javax.swing.JPopupMenu();
        m_ServerPopupMenu = new javax.swing.JPopupMenu();
        m_DirectoryPopupMenu = new javax.swing.JPopupMenu();
        m_ViewPopupMenu = new javax.swing.JPopupMenu();
        m_ScrollPane = new javax.swing.JScrollPane();
        m_ProjectTree = new javax.swing.JTree();

        m_ProjectPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_RootServerPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_ServerPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_DirectoryPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        m_ViewPopupMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        setLayout(new java.awt.BorderLayout());

        m_ProjectTree.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        EnterpriseTreeCellRenderer renderer = new EnterpriseTreeCellRenderer(serversIcon, serverIcon, projectIcon, readOnlyViewIcon, readWriteViewIcon);
        renderer.setLeafIcon(renderer.getClosedIcon());
        m_ProjectTree.setCellRenderer(renderer);
        m_ScrollPane.setViewportView(m_ProjectTree);

        add(m_ScrollPane);
    }// </editor-fold>//GEN-END:initComponents

    private void addPopupMenuItems() {
        Font menuFont = QWinFrame.getQWinFrame().getFont(QWinFrame.getQWinFrame().getFontSize() + 1);

        // The root popup menu
        JMenuItem menuItem = m_RootServerPopupMenu.add(actionAddServer);
        menuItem.setFont(menuFont);

        // The server popup menu
        menuItem = m_ServerPopupMenu.add(actionServerProperties);
        menuItem.setFont(menuFont);

        menuItem = m_ServerPopupMenu.add(actionRemoveServer);
        menuItem.setFont(menuFont);

        // =====================================================================
        // =====================================================================

        // The project popup menu
        menuItem = m_ProjectPopupMenu.add(actionDefineView);
        menuItem.setFont(menuFont);

        // =====================================================================
        // =====================================================================

        // The view popup menu
        menuItem = m_ViewPopupMenu.add(actionDefineWorkfileLocation);
        menuItem.setFont(menuFont);

        menuItem = m_ViewPopupMenu.add(actionMaintainView);
        menuItem.setFont(menuFont);

        menuItem = m_ViewPopupMenu.add(actionDeleteView);
        menuItem.setFont(menuFont);

        // =====================================================================
        m_ViewPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = m_ViewPopupMenu.add(actionExpandTree);
        menuItem.setFont(menuFont);

        menuItem = m_ViewPopupMenu.add(actionCollapseTree);
        menuItem.setFont(menuFont);

        // =====================================================================
        m_ViewPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = m_ViewPopupMenu.add(actionAddDirectory);
        menuItem.setFont(menuFont);

        menuItem = m_ViewPopupMenu.add(actionAutoAddFiles);
        menuItem.setFont(menuFont);

        // =====================================================================
        m_ViewPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = m_ViewPopupMenu.add(actionGetDirectory);
        menuItem.setFont(menuFont);

        menuItem = m_ViewPopupMenu.add(actionLabelDirectory);
        menuItem.setFont(menuFont);

        menuItem = m_ViewPopupMenu.add(actionUnLabelDirectory);
        menuItem.setFont(menuFont);

        // =====================================================================
        m_ViewPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = m_ViewPopupMenu.add(actionPromoteFromChild);
        menuItem.setFont(menuFont);

        // =====================================================================
        // =====================================================================

        // The directory popup menu
        menuItem = m_DirectoryPopupMenu.add(actionAddDirectory);
        menuItem.setFont(menuFont);

        menuItem = m_DirectoryPopupMenu.add(actionDeleteDirectory);
        menuItem.setFont(menuFont);

        menuItem = m_DirectoryPopupMenu.add(actionAutoAddFiles);
        menuItem.setFont(menuFont);

        // =====================================================================
        m_DirectoryPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = m_DirectoryPopupMenu.add(actionExpandTree);
        menuItem.setFont(menuFont);

        menuItem = m_DirectoryPopupMenu.add(actionCollapseTree);
        menuItem.setFont(menuFont);

        // =====================================================================
        m_DirectoryPopupMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        menuItem = m_DirectoryPopupMenu.add(actionGetDirectory);
        menuItem.setFont(menuFont);

        menuItem = m_DirectoryPopupMenu.add(actionLabelDirectory);
        menuItem.setFont(menuFont);

        menuItem = m_DirectoryPopupMenu.add(actionUnLabelDirectory);
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
        } else if (lastSelectedNode instanceof ViewTreeNode) {
            ViewTreeNode viewTreeNode = (ViewTreeNode) lastSelectedNode;
            projectProperties = viewTreeNode.getProjectProperties();
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

    private String getViewName() {
        String retVal = QVCSConstants.QVCS_TRUNK_VIEW;
        if (lastSelectedNode instanceof DirectoryTreeNode) {
            DirectoryTreeNode directoryNode = (DirectoryTreeNode) lastSelectedNode;
            retVal = directoryNode.getViewName();
        } else if (lastSelectedNode instanceof ViewTreeNode) {
            ViewTreeNode viewTreeNode = (ViewTreeNode) lastSelectedNode;
            retVal = viewTreeNode.getViewName();
        }
        return retVal;
    }

    /**
     * Get the selected node.
     * @return the selected node.
     */
    public DefaultMutableTreeNode getSelectedNode() {
        return (DefaultMutableTreeNode) m_ProjectTree.getLastSelectedPathComponent();
    }

    ViewTreeNode getActiveViewNode() {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) m_ProjectTree.getLastSelectedPathComponent();
        while (!(node instanceof ViewTreeNode)) {
            node = (DefaultMutableTreeNode) node.getParent();
        }
        ViewTreeNode viewTreeNode = (ViewTreeNode) node;
        return viewTreeNode;
    }

    Enumeration getCurrentViews() {
        TreeNode projectTreeNode;
        DefaultMutableTreeNode node = getSelectedNode();
        if (node instanceof ViewTreeNode) {
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
                JMenuItem menuItem = menu.add(actionDefineView);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
            } else if (lastSelectedNode instanceof ViewTreeNode) {
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

                menuItem = menu.add(actionPromoteFromChild);
                menuItem.setFont(menuFont);
                menuItem.setMnemonic(java.awt.event.KeyEvent.VK_P);
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
            JMenuItem menuItem = menu.add(actionDefineView);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_D);
        }
    }

    private void addListeners() {
        m_ProjectTree.addTreeSelectionListener(
                new TreeSelectionListener() {

                    @Override
                    public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
                        lastSelectedNode = (DefaultMutableTreeNode) m_ProjectTree.getLastSelectedPathComponent();
                        if (lastSelectedNode != null) {
                            if (lastSelectedNode instanceof ServerTreeNode) {
                                ServerTreeNode serverTreeNode = (ServerTreeNode) lastSelectedNode;
                                serverProperties = serverTreeNode.getServerProperties();

                                // There is no active project or view.
                                activeProject = null;
                                activeView = null;

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
                                    QWinFrame.getQWinFrame().setCurrentAppendedPath(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME, QVCSConstants.QVCS_TRUNK_VIEW, "", QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, true);
                                }
                            } else if (lastSelectedNode instanceof ProjectTreeNode) {
                                ProjectTreeNode projectTreeNode = (ProjectTreeNode) lastSelectedNode;
                                activeProject = projectTreeNode.getProjectProperties();
                                activeView = null;
                                String projectName = projectTreeNode.getProjectName();
                                TransportProxyFactory.getInstance().requestViewList(serverProperties, projectName);
                                QWinFrame.getQWinFrame().setCurrentAppendedPath(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME, QVCSConstants.QVCS_TRUNK_VIEW, "", QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, true);
                            } else if (lastSelectedNode instanceof ViewTreeNode) {
                                ViewTreeNode viewTreeNode = (ViewTreeNode) lastSelectedNode;
                                activeProject = viewTreeNode.getProjectProperties();
                                activeView = viewTreeNode.getViewName();
                                serverProperties = findServerProperties();
                                QWinFrame.getQWinFrame().setCurrentAppendedPath(viewTreeNode.getProjectProperties().getProjectName(), viewTreeNode.getViewName(), "", activeProject.getProjectType(), false);
                            } else if (lastSelectedNode instanceof DefaultProjectTreeNode) {
                                QWinFrame.getQWinFrame().setCurrentAppendedPath(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME, QVCSConstants.QVCS_TRUNK_VIEW, "", QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, true);
                                activeProject = null;
                                activeView = null;
                            } else if (lastSelectedNode instanceof DefaultServerTreeNode) {
                                QWinFrame.getQWinFrame().setCurrentAppendedPath(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME, QVCSConstants.QVCS_TRUNK_VIEW, "", QVCSConstants.QVCS_REMOTE_PROJECT_TYPE, true);
                                activeProject = null;
                                activeView = null;
                            } else if (lastSelectedNode instanceof DirectoryTreeNode) {
                                DirectoryTreeNode directoryNode = (DirectoryTreeNode) lastSelectedNode;
                                activeProject = directoryNode.getProjectProperties();
                                activeView = directoryNode.getViewName();
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
                                                    && (0 == activeView.compareTo(oldDirectoryTreeNode.getViewName()))) {
                                                getProjectJTreeControl().collapsePath(treeSelectionEvent.getOldLeadSelectionPath());
                                            }
                                        }
                                    }
                                }

                                QWinFrame.getQWinFrame().setCurrentAppendedPath(directoryNode.getProjectProperties().getProjectName(), directoryNode.getViewName(), directoryNode.getAppendedPath(),
                                        activeProject.getProjectType(), false);
                            }
                        }
                    }
                });
        m_ProjectTree.addMouseListener(new MouseInputAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger() || ((e.getButton() == MouseEvent.BUTTON3) && (0 != (e.getModifiers() & MouseEvent.MOUSE_RELEASED)))) {
                    // Make sure the node under the mouse is the one
                    // that is selected.
                    TreePath selPath = m_ProjectTree.getPathForLocation(e.getX(), e.getY());
                    m_ProjectTree.setSelectionPath(selPath);

                    // Turn on all popups, and then figure out which ones
                    // to turn off
                    enableAllPopUpOperations();

                    if (lastSelectedNode != null) {
                        if (lastSelectedNode instanceof ProjectTreeNode) {
                            m_ProjectPopupMenu.show(m_ProjectTree, e.getX(), e.getY());
                        } else if (lastSelectedNode instanceof ViewTreeNode) {
                            m_ViewPopupMenu.show(m_ProjectTree, e.getX(), e.getY());
                        } else if (lastSelectedNode instanceof DirectoryTreeNode) {
                            m_DirectoryPopupMenu.show(m_ProjectTree, e.getX(), e.getY());
                        } else if (lastSelectedNode instanceof DefaultProjectTreeNode) {
                            disableAllDirectoryOperations();
                        } else if (lastSelectedNode instanceof DefaultServerTreeNode) {
                            m_RootServerPopupMenu.show(m_ProjectTree, e.getX(), e.getY());
                        } else if (lastSelectedNode instanceof ServerTreeNode) {
                            m_ServerPopupMenu.show(m_ProjectTree, e.getX(), e.getY());
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
     * Get the active view.
     *
     * @return the name of the active view.
     */
    public String getActiveView() {
        return activeView;
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
        m_ProjectTree.setSelectionPath(new TreePath(m_ProjectTree.getModel().getRoot()));
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
            m_ProjectTree.setSelectionPath(treePath);
            m_ProjectTree.scrollPathToVisible(treePath);
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

    private void initDragAndDrop() {
        m_ProjectTree.setDragEnabled(true);
        m_ProjectTree.setTransferHandler(new MyTreeTransferHandler());
    }

    class MyTreeTransferHandler extends TransferHandler {

        private static final long serialVersionUID = 10L;

        @Override
        protected Transferable createTransferable(JComponent c) {
            // We are a drop target only. The user cannot drag from the tree
            // control
            return null;
        }

        @Override
        public boolean canImport(JComponent c, DataFlavor[] flavors) {
            for (DataFlavor flavor : flavors) {
                if (flavor.equals(QWinFrame.getQWinFrame().getRightFilePane().getDropDataFlavor())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public int getSourceActions(JComponent c) {
            // We are a drop target only.
            return NONE;
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {
            try {
                if ((lastSelectedNode instanceof DirectoryTreeNode)
                        || (lastSelectedNode instanceof ViewTreeNode)) {
                    final DropTransferData dropTransferData = (DropTransferData) t.getTransferData(QWinFrame.getQWinFrame().getRightFilePane().getDropDataFlavor());

                    // Make sure we're dropping on the same project/view
                    if ((0 == getActiveProject().getProjectName().compareTo(dropTransferData.getProjectName()))
                            && (0 == getActiveView().compareTo(dropTransferData.getViewName()))
                            && (0 != getAppendedPath().compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY))
                            && (0 != getAppendedPath().compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY))) {
                        Runnable later = new Runnable() {

                            @Override
                            public void run() {
                                TransportProxyInterface transportProxy = null;
                                int transactionID = 0;

                                // Verify that the user wants to drop here...
                                int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Do you want to move " + dropTransferData.getShortWorkfileName() + " to this directory?\n"
                                        + getAppendedPath(), "Confirm File Move", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                                if (answer == JOptionPane.YES_OPTION) {
                                    ClientRequestMoveFileData clientRequestMoveFileData = new ClientRequestMoveFileData();
                                    clientRequestMoveFileData.setOriginalAppendedPath(dropTransferData.getAppendedPath());
                                    clientRequestMoveFileData.setProjectName(dropTransferData.getProjectName());
                                    clientRequestMoveFileData.setViewName(dropTransferData.getViewName());
                                    clientRequestMoveFileData.setShortWorkfileName(dropTransferData.getShortWorkfileName());
                                    clientRequestMoveFileData.setNewAppendedPath(getAppendedPath());

                                    String serverName = QWinFrame.getQWinFrame().getServerName();
                                    String fullWorkfilePath = QWinFrame.getQWinFrame().getUserWorkfileDirectory();
                                    try {
                                        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(dropTransferData.getProjectName(), getViewName(), getAppendedPath());
                                        DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(serverName, directoryCoordinate,
                                                getActiveProject().getProjectType(), getActiveProject(), fullWorkfilePath, null, false);
                                        ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) directoryManager.getArchiveDirManager();

                                        transportProxy = archiveDirManagerProxy.getTransportProxy();
                                        // Make sure this is synchronized
                                        synchronized (transportProxy) {
                                            transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transportProxy);
                                            transportProxy.write(clientRequestMoveFileData);
                                        }
                                    } catch (QVCSException e) {
                                        QWinUtility.logProblem(Level.WARNING, "importData caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                                        QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                                    } finally {
                                        ClientTransactionManager.getInstance().sendEndTransaction(transportProxy, transactionID);
                                    }
                                }
                            }
                        };
                        SwingUtilities.invokeLater(later);
                    } else {
                        if (0 != getActiveProject().getProjectName().compareTo(dropTransferData.getProjectName())) {
                            QWinUtility.logProblem(Level.INFO, "Cannot move a file from one project to another.");
                        } else if (0 != getActiveView().compareTo(dropTransferData.getViewName())) {
                            QWinUtility.logProblem(Level.INFO, "Cannot move a file from one view to another.");
                        } else if (0 == getAppendedPath().compareTo(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Cannot move a file to the cemetery.");
                        } else if (0 == getAppendedPath().compareTo(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                            QWinUtility.logProblem(Level.INFO, "Cannot move a file to the branch archives directory.");
                        }
                        return false;
                    }
                } else {
                    return false;
                }

            } catch (UnsupportedFlavorException | IOException e) {
                return false;
            }
            return true;
        }

        @Override
        protected void exportDone(JComponent c, Transferable data, int action) {
            switch (action) {
                case MOVE: {
                    break;
                }
                default: {
                    // Don't do anything else.
                    break;
                }
            }
        }
    }

// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu m_DirectoryPopupMenu;
    private javax.swing.JPopupMenu m_ProjectPopupMenu;
    private javax.swing.JTree m_ProjectTree;
    private javax.swing.JPopupMenu m_RootServerPopupMenu;
    private javax.swing.JScrollPane m_ScrollPane;
    private javax.swing.JPopupMenu m_ServerPopupMenu;
    private javax.swing.JPopupMenu m_ViewPopupMenu;
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
                qWinFrame.getUserLocationProperties().setWorkfileLocation(qWinFrame.getServerName(), qWinFrame.getProjectName(), qWinFrame.getViewName(),
                        defineWorkfileLocationDialog.getWorkfileLocation());
                qWinFrame.getUserLocationProperties().saveProperties();
                qWinFrame.setUserWorkfileDirectory(defineWorkfileLocationDialog.getWorkfileLocation());
                qWinFrame.setRefreshRequired(true);
                qWinFrame.setCurrentAppendedPath(qWinFrame.getProjectName(), qWinFrame.getViewName(), qWinFrame.getAppendedPath(), qWinFrame.getProjectType(), false);
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
            OperationBaseClass addDirectoryOperation = new OperationAddDirectory(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(), getViewName(),
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
            OperationDeleteDirectory deleteDirectoryOperation = new OperationDeleteDirectory(QWinFrame.getQWinFrame().getActiveServerProperties(), getProjectName(), getViewName(), getAppendedPath());
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
            OperationBaseClass autoAddFilesOperation = new OperationAutoAddFiles(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(), getViewName(),
                    getAppendedPath(), QWinFrame.getQWinFrame().getUserLocationProperties(), getProjectProperties(), getCurrentWorkfileDirectory());
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
            OperationBaseClass getDirectoryOperation = new OperationGetDirectory(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(), getViewName(),
                    getAppendedPath(), QWinFrame.getQWinFrame().getUserLocationProperties(), getProjectProperties(), getCurrentWorkfileDirectory());
            getDirectoryOperation.executeOperation();
        }
    }

    class ActionLabelDirectory extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionLabelDirectory(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass labelDirectoryOperation = new OperationLabelDirectory(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(), getViewName(),
                    getAppendedPath(), QWinFrame.getQWinFrame().getUserLocationProperties(), getProjectProperties(), getCurrentWorkfileDirectory());
            labelDirectoryOperation.executeOperation();
        }
    }

    class ActionUnLabelDirectory extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionUnLabelDirectory(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass unlabelDirectoryOperation = new OperationUnLabelDirectory(QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(), getViewName(),
                    getAppendedPath(), QWinFrame.getQWinFrame().getUserLocationProperties(), getProjectProperties(), getCurrentWorkfileDirectory());
            unlabelDirectoryOperation.executeOperation();
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

    class ActionDefineView extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionDefineView(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationDefineView defineViewOperation = new OperationDefineView(QWinFrame.getQWinFrame().getActiveServerProperties(), getProjectName());
            defineViewOperation.executeOperation();
        }
    }

    class ActionMaintainView extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionMaintainView(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationMaintainView maintainViewOperation = new OperationMaintainView(QWinFrame.getQWinFrame().getActiveServerProperties(), getProjectName(), getViewName(),
                    (RemoteViewProperties) getProjectProperties());
            maintainViewOperation.executeOperation();
        }
    }

    class ActionDeleteView extends AbstractAction {

        private static final long serialVersionUID = 10L;

        ActionDeleteView(String actionName) {
            super(actionName);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            OperationDeleteView deleteViewOperation = new OperationDeleteView(QWinFrame.getQWinFrame().getActiveServerProperties(), getProjectName(), getViewName());
            deleteViewOperation.executeOperation();
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
                    getViewName(), QWinFrame.getQWinFrame().getUserLocationProperties(), getCurrentWorkfileDirectory());
            promoteFromChildBranchOperation.executeOperation();
        }
    }

    static class EnterpriseTreeCellRenderer extends DefaultTreeCellRenderer {
        private static final long serialVersionUID = 3531447549589503366L;

        private final ImageIcon iconServers;
        private final ImageIcon iconServer;
        private final ImageIcon iconProject;
        private final ImageIcon iconReadOnlyView;
        private final ImageIcon iconReadWriteView;

        EnterpriseTreeCellRenderer(ImageIcon servers, ImageIcon server, ImageIcon project, ImageIcon readOnlyView, ImageIcon readWriteView) {
            iconServers = servers;
            iconServer = server;
            iconProject = project;
            iconReadOnlyView = readOnlyView;
            iconReadWriteView = readWriteView;
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
            } else if (value instanceof ReadOnlyViewNode) {
                ReadOnlyViewNode viewNode = (ReadOnlyViewNode) value;
                setText(viewNode.getViewName());
                setIcon(iconReadOnlyView);
            } else if (value instanceof ReadWriteViewNode) {
                ReadWriteViewNode viewNode = (ReadWriteViewNode) value;
                setText(viewNode.getViewName());
                setIcon(iconReadWriteView);
            }

            return this;
        }
    }

}
