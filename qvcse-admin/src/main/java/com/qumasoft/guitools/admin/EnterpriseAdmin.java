/*   Copyright 2004-2019 Jim Voris
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

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ExitAppInterface;
import com.qumasoft.qvcslib.PasswordChangeListenerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerManager;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.TransportProxyListenerInterface;
import com.qumasoft.qvcslib.TransportProxyType;
import com.qumasoft.qvcslib.UpdateManager;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.VisualCompareInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestChangePasswordData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerAddUserData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerAssignUserRolesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateProjectData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteProjectData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerGetRoleNamesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListProjectUsersData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListProjectsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListUserRolesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListUsersData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerMaintainProjectData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerRemoveUserData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerShutdownData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QVCS-Enterprise admin application. This is the main class for the admin Swing application that a user must use to administer a QVCS-Enterprise server.
 *
 * @author Jim Voris
 */
public class EnterpriseAdmin extends javax.swing.JFrame implements PasswordChangeListenerInterface, TransportProxyListenerInterface, ExitAppInterface, VisualCompareInterface {

    private static final String USER_DIR = "user.dir";

    private static final long serialVersionUID = 904068619108638580L;

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(EnterpriseAdmin.class);

    private static EnterpriseAdmin enterpriseAdminSelfRef = null;

    private String userNameMember = null;

    private final String[] argsMember;

    private final ImageIcon frameIcon = new ImageIcon(ClassLoader.getSystemResource("images/qvcsadmin.png"), "Quma Software, Inc.");
    private final ImageIcon usersIcon = new ImageIcon(ClassLoader.getSystemResource("images/users.png"), "Users");
    private final ImageIcon userIcon = new ImageIcon(ClassLoader.getSystemResource("images/user.png"), "User");

    private final ImageIcon serversIcon = new ImageIcon(ClassLoader.getSystemResource("images/servers.png"), "Servers");
    private final ImageIcon serverIcon = new ImageIcon(ClassLoader.getSystemResource("images/server.png"), "Server");
    private final ImageIcon projectIcon = new ImageIcon(ClassLoader.getSystemResource("images/project.png"), "Project");

    private final ActionServerAddServer actionServerAddServer = new ActionServerAddServer();
    private final ActionServerRemoveServer actionServerRemoveServer = new ActionServerRemoveServer();
    private final ActionServerProperties actionServerProperties = new ActionServerProperties();
    private final ActionServerNewProject actionServerNewProject = new ActionServerNewProject();
    private final ActionServerShutdown actionServerShutdown = new ActionServerShutdown();
    private final ActionServerExit actionServerExit = new ActionServerExit();

    private final ActionProjectRemoveProject actionProjectRemoveProject = new ActionProjectRemoveProject();
    private final ActionProjectProjectProperties actionProjectProjectProperties = new ActionProjectProjectProperties();

    private final ActionUserAddUserToServer actionUserAddUserToServer = new ActionUserAddUserToServer();
    private final ActionUserRemoveUserFromServer actionUserRemoveUserFromServer = new ActionUserRemoveUserFromServer();
    private final ActionUserAddUserToProject actionUserAddUserToProject = new ActionUserAddUserToProject();
    private final ActionUserMaintainProjectRoles actionUserMaintainProjectRoles = new ActionUserMaintainProjectRoles();
    private final ActionUserChangePassword actionUserChangePassword = new ActionUserChangePassword();

    private final ActionMaintainRole actionMaintainRole = new ActionMaintainRole();

    // A map to contain our active transport proxies
    private Map<String, TransportProxyInterface> transportProxyMapMember = null;

    // A map to hold on to hashed passwords.
    private Map<String, byte[]> serverPasswordMapMember = null;

    // A map to hold pending passwords.
    private Map<String, String> pendingPasswordMapMember = null;

    // A map to hold pending login passwords
    private Map<String, byte[]> pendingLoginPasswordMapMember = null;

    // Our server tree model
    private ServerTreeModel serverModelMember = null;

    // Our user tree model
    private UserTreeModel userModelMember = null;

    private MaintainUserRolesDialog maintainUserRolesDialogMember = null;

    private AddUserToProjectDialog addUserToProjectDialogMember = null;

    private MaintainRolePrivilegesDialog maintainRolePrivilegesDialogMember = null;

    /**
     * Creates new form EnterpriseAdmin.
     *
     * @param args the command line arguments.
     */
    public EnterpriseAdmin(String[] args) {
        if (args.length > 0) {
            this.argsMember = args;
            System.setProperty(USER_DIR, argsMember[0]);
        } else {
            this.argsMember = new String[1];
            this.argsMember[0] = System.getProperty(USER_DIR);
        }

        // Set the frame icon to the Quma standard icon.
        this.setIconImage(frameIcon.getImage());

        // Set this as early as we can...
        enterpriseAdminSelfRef = this;

        initComponents();

        // Create the server model.
        serverModelMember = new ServerTreeModel();

        // Attach the model to the control.
        serverTree.setModel(serverModelMember.getTreeModel());

        // Create the user model.
        userModelMember = new UserTreeModel();

        // Attach the model to the control.
        usersTree.setModel(userModelMember.getTreeModel());

        // Create the transport proxy map.
        transportProxyMapMember = Collections.synchronizedMap(new TreeMap<>());

        // Create the map of hashed passwords.
        serverPasswordMapMember = Collections.synchronizedMap(new TreeMap<>());

        // Create the pending password map.
        pendingPasswordMapMember = Collections.synchronizedMap(new TreeMap<>());

        // Create the map of pending login passwords.
        pendingLoginPasswordMapMember = Collections.synchronizedMap(new TreeMap<>());

        maintainUserRolesDialogMember = new MaintainUserRolesDialog(this, true);

        addUserToProjectDialogMember = new AddUserToProjectDialog(this, true);

        maintainRolePrivilegesDialogMember = new MaintainRolePrivilegesDialog(this, true);

        // Register listeners to the server manager so the dialogs will get
        // the role information.
        ServerManager.getServerManager().addChangeListener(maintainUserRolesDialogMember);
        ServerManager.getServerManager().addChangeListener(addUserToProjectDialogMember);
        ServerManager.getServerManager().addChangeListener(maintainRolePrivilegesDialogMember);

        // Make us a listener for password change responses
        TransportProxyFactory.getInstance().addChangedPasswordListener(this);

        pack();

        // Size the frame to a useful size.
        sizeMainWindow();
        center();

        initServerMenu();
        initProjectMenu();
        initUserMenu();
        initRoleMenu();
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Set the base directory for the transport so it will know where to find and put this user's files.
        TransportProxyFactory.getInstance().setDirectory(System.getProperty(USER_DIR));

        // Report the version to the log file.
        LOGGER.info("QVCS-Enterprise admin tool version: [" + QVCSConstants.QVCS_RELEASE_VERSION + "].");
    }

    /**
     * Get the singleton instance of the admin application.
     * @return the singleton instance of the admin application.
     */
    public static EnterpriseAdmin getInstance() {
        return enterpriseAdminSelfRef;
    }

    private void initServerMenu() {
        // So we enabled/disable the appropriate menu items.
        frameServersMenu.addMenuListener(new OurServerMenuListener());
    }

    private void initProjectMenu() {
        // So we enabled/disable the appropriate menu items.
        frameProjectsMenu.addMenuListener(new OurProjectMenuListener());
    }

    private void initUserMenu() {
        // So we enabled/disable the appropriate menu items.
        frameUsersMenu.addMenuListener(new OurUserMenuListener());
    }

    private void initRoleMenu() {
        // So we enabled/disable the appropriate menu items.
        frameRolesMenu.addMenuListener(new OurRoleMenuListener());
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form
     * Editor.
     */
// <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serverInstancePopupMenu = new javax.swing.JPopupMenu();
        defineNewProjectMenuItem = new javax.swing.JMenuItem();
        serverPropertiesMenuItem = new javax.swing.JMenuItem();
        maintainServerRolesMenuItem = new javax.swing.JMenuItem();
        removeServerMenuItem = new javax.swing.JMenuItem();
        bottomSeparator = new javax.swing.JSeparator();
        serverPopupShutdownMenuItem = new javax.swing.JMenuItem();
        serverPopupMenu = new javax.swing.JPopupMenu();
        addServerMenuItem = new javax.swing.JMenuItem();
        userServerPopupMenu = new javax.swing.JPopupMenu();
        changeUserPasswordMenuItem = new javax.swing.JMenuItem();
        deleteUserMenuItem = new javax.swing.JMenuItem();
        userProjectPopupMenu = new javax.swing.JPopupMenu();
        maintainRolesMenuItem = new javax.swing.JMenuItem();
        usersServerPopupMenu = new javax.swing.JPopupMenu();
        addServerUserMenuItem = new javax.swing.JMenuItem();
        usersProjectPopupMenu = new javax.swing.JPopupMenu();
        addProjectsUserMenuItem = new javax.swing.JMenuItem();
        projectInstancePopupMenu = new javax.swing.JPopupMenu();
        removeProjectMenuItem = new javax.swing.JMenuItem();
        projectPropertiesMenuItem = new javax.swing.JMenuItem();
        splitPane = new javax.swing.JSplitPane();
        leftParentPanel = new javax.swing.JPanel();
        leftScrollPane = new javax.swing.JScrollPane();
        leftChildPanel = new javax.swing.JPanel();
        serverTree = new javax.swing.JTree();
        serverLabel = new javax.swing.JLabel();
        rightParentPanel = new javax.swing.JPanel();
        usersLabel = new javax.swing.JLabel();
        rightScrollPane = new javax.swing.JScrollPane();
        rightChildPanel = new javax.swing.JPanel();
        usersTree = new javax.swing.JTree();
        frameMenuBar = new javax.swing.JMenuBar();
        frameServersMenu = new javax.swing.JMenu();
        frameProjectsMenu = new javax.swing.JMenu();
        frameUsersMenu = new javax.swing.JMenu();
        frameRolesMenu = new javax.swing.JMenu();

        defineNewProjectMenuItem.setText("New Project...");
        defineNewProjectMenuItem.setToolTipText("Define new project for this server");
        defineNewProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defineNewProjectMenuItemActionPerformed(evt);
            }
        });
        serverInstancePopupMenu.add(defineNewProjectMenuItem);

        serverPropertiesMenuItem.setText("Properties...");
        serverPropertiesMenuItem.setToolTipText("Edit this servers properties");
        serverPropertiesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverPropertiesMenuItemActionPerformed(evt);
            }
        });
        serverInstancePopupMenu.add(serverPropertiesMenuItem);

        maintainServerRolesMenuItem.setText("Maintain Server Roles...");
        maintainServerRolesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maintainServerRolesMenuItemActionPerformed(evt);
            }
        });
        serverInstancePopupMenu.add(maintainServerRolesMenuItem);

        removeServerMenuItem.setText("Remove server definition...");
        removeServerMenuItem.setToolTipText("Remove the definition of this server");
        removeServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeServerMenuItemActionPerformed(evt);
            }
        });
        serverInstancePopupMenu.add(removeServerMenuItem);
        serverInstancePopupMenu.add(bottomSeparator);

        serverPopupShutdownMenuItem.setText("Shutdown server...");
        serverPopupShutdownMenuItem.setToolTipText("Shutdown this server");
        serverPopupShutdownMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                serverPopupShutdownMenuItemActionPerformed(evt);
            }
        });
        serverInstancePopupMenu.add(serverPopupShutdownMenuItem);

        addServerMenuItem.setText("Add server definition...");
        addServerMenuItem.setToolTipText("Define a new server");
        addServerMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerMenuItemActionPerformed(evt);
            }
        });
        serverPopupMenu.add(addServerMenuItem);

        changeUserPasswordMenuItem.setMnemonic('C');
        changeUserPasswordMenuItem.setText("Change password...");
        changeUserPasswordMenuItem.setToolTipText("Change this user's password");
        changeUserPasswordMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeUserPasswordMenuItemActionPerformed(evt);
            }
        });
        userServerPopupMenu.add(changeUserPasswordMenuItem);

        deleteUserMenuItem.setMnemonic('D');
        deleteUserMenuItem.setText("Delete User...");
        deleteUserMenuItem.setToolTipText("Delete this user from the server");
        deleteUserMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteUserMenuItemActionPerformed(evt);
            }
        });
        userServerPopupMenu.add(deleteUserMenuItem);

        maintainRolesMenuItem.setMnemonic('M');
        maintainRolesMenuItem.setText("Maintain roles...");
        maintainRolesMenuItem.setToolTipText("Maintain roles for this user for this project");
        maintainRolesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maintainRolesMenuItemActionPerformed(evt);
            }
        });
        userProjectPopupMenu.add(maintainRolesMenuItem);

        addServerUserMenuItem.setMnemonic('A');
        addServerUserMenuItem.setText("Add User...");
        addServerUserMenuItem.setToolTipText("Add a user to this server");
        addServerUserMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addServerUserMenuItemActionPerformed(evt);
            }
        });
        usersServerPopupMenu.add(addServerUserMenuItem);

        addProjectsUserMenuItem.setMnemonic('A');
        addProjectsUserMenuItem.setText("Add User...");
        addProjectsUserMenuItem.setToolTipText("Add a user to this project");
        addProjectsUserMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addProjectsUserMenuItemActionPerformed(evt);
            }
        });
        usersProjectPopupMenu.add(addProjectsUserMenuItem);

        removeProjectMenuItem.setMnemonic('R');
        removeProjectMenuItem.setText("Remove Project...");
        removeProjectMenuItem.setToolTipText("Remove this project from the server");
        removeProjectMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeProjectMenuItemActionPerformed(evt);
            }
        });
        projectInstancePopupMenu.add(removeProjectMenuItem);

        projectPropertiesMenuItem.setMnemonic('P');
        projectPropertiesMenuItem.setText("Project Properties...");
        projectPropertiesMenuItem.setToolTipText("Change project properties");
        projectPropertiesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                projectPropertiesMenuItemActionPerformed(evt);
            }
        });
        projectInstancePopupMenu.add(projectPropertiesMenuItem);

        setTitle("QVCS-Enterprise Admin Tool");
        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setName("title\tQVCS-Enterprise Admin Tool"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        leftParentPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        leftParentPanel.setLayout(new java.awt.BorderLayout());

        leftScrollPane.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        leftChildPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        leftChildPanel.setLayout(new java.awt.BorderLayout());

        serverTree.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        AdminServerTreeCellRenderer renderer = new AdminServerTreeCellRenderer(serversIcon, serverIcon, projectIcon);
        renderer.setLeafIcon(renderer.getClosedIcon());
        serverTree.setCellRenderer(renderer);
        serverTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                serverTreeValueChanged(evt);
            }
        });
        serverTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                serverTreeMouseReleased(evt);
            }
        });
        leftChildPanel.add(serverTree, java.awt.BorderLayout.CENTER);

        leftScrollPane.setViewportView(leftChildPanel);

        leftParentPanel.add(leftScrollPane, java.awt.BorderLayout.CENTER);

        serverLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        serverLabel.setText("Servers:");
        serverLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        leftParentPanel.add(serverLabel, java.awt.BorderLayout.NORTH);

        splitPane.setLeftComponent(leftParentPanel);

        rightParentPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        rightParentPanel.setLayout(new java.awt.BorderLayout());

        usersLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        usersLabel.setText("Users:");
        rightParentPanel.add(usersLabel, java.awt.BorderLayout.NORTH);

        rightScrollPane.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        rightChildPanel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        rightChildPanel.setLayout(new java.awt.BorderLayout());

        usersTree.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        AdminUserTreeCellRenderer userRenderer = new AdminUserTreeCellRenderer(usersIcon, userIcon);
        renderer.setLeafIcon(renderer.getClosedIcon());
        usersTree.setCellRenderer(userRenderer);
        usersTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                usersTreeMouseReleased(evt);
            }
        });
        rightChildPanel.add(usersTree, java.awt.BorderLayout.CENTER);

        rightScrollPane.setViewportView(rightChildPanel);

        rightParentPanel.add(rightScrollPane, java.awt.BorderLayout.CENTER);

        splitPane.setRightComponent(rightParentPanel);

        getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

        frameMenuBar.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        frameServersMenu.setMnemonic('S');
        frameServersMenu.setText("Servers");
        frameServersMenu.setComponentPopupMenu(userProjectPopupMenu);
        frameServersMenu.setFocusable(false);
        frameServersMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        frameMenuBar.add(frameServersMenu);

        frameProjectsMenu.setMnemonic('P');
        frameProjectsMenu.setText("Projects");
        frameProjectsMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        frameMenuBar.add(frameProjectsMenu);

        frameUsersMenu.setMnemonic('U');
        frameUsersMenu.setText("Users");
        frameUsersMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        frameMenuBar.add(frameUsersMenu);

        frameRolesMenu.setMnemonic('R');
        frameRolesMenu.setText("Roles");
        frameRolesMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        frameMenuBar.add(frameRolesMenu);

        setJMenuBar(frameMenuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void maintainServerRolesMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_maintainServerRolesMenuItemActionPerformed
    {//GEN-HEADEREND:event_maintainServerRolesMenuItemActionPerformed
        maintainRoles();
    }//GEN-LAST:event_maintainServerRolesMenuItemActionPerformed

    private void projectPropertiesMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_projectPropertiesMenuItemActionPerformed
    {//GEN-HEADEREND:event_projectPropertiesMenuItemActionPerformed
        // Change project properties
        TreeNode node = (TreeNode) serverTree.getSelectionPath().getLastPathComponent();
        if (node instanceof ProjectTreeNode) {
            ProjectTreeNode projectTreeNode = (ProjectTreeNode) node;
            String serverName = projectTreeNode.getServerName();
            String projectName = projectTreeNode.getProjectName();
            AbstractProjectProperties projectProperties = projectTreeNode.getProjectProperties();

            // Lookup the transport proxy for this server.
            TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

            // We can change the project properties if we have a connection to the server.
            if (transportProxy != null) {
                MaintainProjectPropertiesDialog maintainProjectPropertiesDialog = new MaintainProjectPropertiesDialog(this, true, projectName, projectProperties);
                maintainProjectPropertiesDialog.setVisible(true);
                if (maintainProjectPropertiesDialog.getIsOK()) {
                    ClientRequestServerMaintainProjectData maintainProjectRequest = new ClientRequestServerMaintainProjectData();
                    maintainProjectRequest.setProjectName(projectName);
                    maintainProjectRequest.setUserName(transportProxy.getUsername());
                    maintainProjectRequest.setServerName(serverName);
                    maintainProjectRequest.setPassword(serverPasswordMapMember.get(serverName));
                    maintainProjectRequest.setCreateReferenceCopyFlag(maintainProjectPropertiesDialog.getCreateReferenceCopiesFlag());
                    maintainProjectRequest.setCreateOrDeleteCurrentReferenceFilesFlag(maintainProjectPropertiesDialog.getCreateOrDeleteCurrentReferenceFilesFlag());
                    maintainProjectRequest.setIgnoreCaseFlag(maintainProjectPropertiesDialog.getIgnoreCaseFlag());
                    maintainProjectRequest.setDefineAlternateReferenceLocationFlag(maintainProjectPropertiesDialog.getDefineAlternateReferenceLocationFlag());
                    maintainProjectRequest.setAlternateReferenceLocation(maintainProjectPropertiesDialog.getAlternateReferenceLocation());
                    synchronized (transportProxy) {
                        transportProxy.write(maintainProjectRequest);
                    }
                }
            }
        }
    }//GEN-LAST:event_projectPropertiesMenuItemActionPerformed

    private void addProjectsUserMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addProjectsUserMenuItemActionPerformed
    {//GEN-HEADEREND:event_addProjectsUserMenuItemActionPerformed
        // Add a user to a project.  In order to add a user to a project, that
        // user must already have been defined for the server.
        // Get the name of the active server and project.
        DefaultProjectUserTreeNode root = (DefaultProjectUserTreeNode) usersTree.getModel().getRoot();
        String serverName = root.getServerName();
        String projectName = root.getProjectName();

        // Request the user roles from the server.
        ClientRequestServerListUserRolesData requestData = new ClientRequestServerListUserRolesData();
        requestData.setUserName("guest");
        requestData.setServerName(serverName);
        requestData.setProjectName(projectName);
        requestData.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);

        TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);
        synchronized (transportProxy) {
            transportProxy.write(requestData);
        }

        // Display the dialog.
        addUserToProjectDialogMember.centerDialog();
        addUserToProjectDialogMember.setVisible(true);

        if (addUserToProjectDialogMember.getIsOK()) {
            ClientRequestServerAssignUserRolesData clientRequestServerAssignUserRolesData = new ClientRequestServerAssignUserRolesData();
            clientRequestServerAssignUserRolesData.setServerName(serverName);
            clientRequestServerAssignUserRolesData.setProjectName(projectName);
            clientRequestServerAssignUserRolesData.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
            clientRequestServerAssignUserRolesData.setUserName(addUserToProjectDialogMember.getUserName());
            clientRequestServerAssignUserRolesData.setAssignedRoles(addUserToProjectDialogMember.getAssignedRoles());
            synchronized (transportProxy) {
                transportProxy.write(clientRequestServerAssignUserRolesData);
            }
        }
    }//GEN-LAST:event_addProjectsUserMenuItemActionPerformed

    private void removeProjectMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeProjectMenuItemActionPerformed
    {//GEN-HEADEREND:event_removeProjectMenuItemActionPerformed
        // Delete a project.
        DefaultProjectUserTreeNode root = (DefaultProjectUserTreeNode) usersTree.getModel().getRoot();
        String serverName = root.getServerName();
        String projectName = root.getProjectName();
        String projectMessage = "Delete project '" + projectName + "' ?";
        int choice = JOptionPane.showConfirmDialog(this, projectMessage, "Delete Project", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            // Lookup the transport proxy for this server.
            TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

            // We can only create a new project if we have a connection to the server
            if (transportProxy != null) {
                ClientRequestServerDeleteProjectData deleteProjectRequest = new ClientRequestServerDeleteProjectData();
                deleteProjectRequest.setDeleteProjectName(projectName);
                deleteProjectRequest.setUserName(transportProxy.getUsername());
                deleteProjectRequest.setServerName(serverName);
                deleteProjectRequest.setPassword(serverPasswordMapMember.get(serverName));
                synchronized (transportProxy) {
                    transportProxy.write(deleteProjectRequest);
                }
            }
        }
    }//GEN-LAST:event_removeProjectMenuItemActionPerformed

    private void addServerUserMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addServerUserMenuItemActionPerformed
    {//GEN-HEADEREND:event_addServerUserMenuItemActionPerformed
        // Add a user to the currently selected server.

        // Get the name of the active server.
        DefaultUserTreeNode root = (DefaultUserTreeNode) usersTree.getModel().getRoot();
        String serverName = root.getServerName();

        AddServerUserDialog addUserToServerDialog = new AddServerUserDialog(this, true);
        addUserToServerDialog.setVisible(true);

        String password = addUserToServerDialog.getPassword();
        if (password != null) {
            TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

            // Request the addition of the user to the server.
            ClientRequestServerAddUserData requestData = new ClientRequestServerAddUserData();
            requestData.setServerName(serverName);
            requestData.setUserName(addUserToServerDialog.getUserName());
            byte[] hashedPassword = Utility.getInstance().hashPassword(addUserToServerDialog.getPassword());
            requestData.setPassword(hashedPassword);
            synchronized (transportProxy) {
                transportProxy.write(requestData);
            }
        }
    }//GEN-LAST:event_addServerUserMenuItemActionPerformed

    private void serverTreeValueChanged(javax.swing.event.TreeSelectionEvent evt)//GEN-FIRST:event_serverTreeValueChanged
    {//GEN-HEADEREND:event_serverTreeValueChanged
        TreePath treePath = evt.getNewLeadSelectionPath();

        if (treePath == null) {
            getUserModel().updateRootNameForServer("");
        } else {
            TreeNode node = (TreeNode) treePath.getLastPathComponent();
            if (node instanceof DefaultServerTreeNode) {
                getUserModel().updateRootNameForServer("");
            } else if (node instanceof ServerTreeNode) {
                ServerTreeNode serverTreeNode = (ServerTreeNode) node;
                ServerProperties serverProperties = serverTreeNode.getServerProperties();
                String serverName = serverProperties.getServerName();
                if (null == transportProxyMapMember.get(serverName)) {
                    // We need to login to this server.
                    showLoginToServerDialog();
                } else {
                    requestProjectsAndUsersFromServer(serverName);
                }
            } else if (node instanceof ProjectTreeNode) {
                ProjectTreeNode projectNode = (ProjectTreeNode) node;
                ServerTreeNode serverTreeNode = (ServerTreeNode) projectNode.getParent();
                ServerProperties serverProperties = serverTreeNode.getServerProperties();
                String serverName = serverProperties.getServerName();
                getUserModel().updateRootNameForProject(serverName, projectNode.toString());
                requestProjectUsersFromServer(serverName, projectNode.toString());
            }
        }
    }//GEN-LAST:event_serverTreeValueChanged

    private void maintainRolesMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_maintainRolesMenuItemActionPerformed
    {//GEN-HEADEREND:event_maintainRolesMenuItemActionPerformed
        // Maintain a user's roles for the selected project.  This method gets called
        // from the context menu when an individual user node is selected on the
        // list of users associated with a specific project.

        // Send the request to the server to get the roles for this user.
        // Figure out the user name.
        UserTreeNode userNode = (UserTreeNode) usersTree.getSelectionPath().getLastPathComponent();
        String userName = userNode.toString();

        // Get the name of the active server and project.
        DefaultProjectUserTreeNode root = (DefaultProjectUserTreeNode) usersTree.getModel().getRoot();
        String serverName = root.getServerName();
        String projectName = root.getProjectName();

        // Request the user roles from the server.
        ClientRequestServerListUserRolesData requestData = new ClientRequestServerListUserRolesData();
        requestData.setUserName(userName);
        requestData.setServerName(serverName);
        requestData.setProjectName(projectName);
        requestData.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);

        TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);
        synchronized (transportProxy) {
            transportProxy.write(requestData);
        }

        // Display the dialog.
        maintainUserRolesDialogMember.centerDialog();
        maintainUserRolesDialogMember.setVisible(true);

        if (maintainUserRolesDialogMember.getIsOK()) {
            ClientRequestServerAssignUserRolesData clientRequestServerAssignUserRolesData = new ClientRequestServerAssignUserRolesData();
            clientRequestServerAssignUserRolesData.setAssignedRoles(maintainUserRolesDialogMember.getAssignedRoles());
            clientRequestServerAssignUserRolesData.setUserName(userName);
            clientRequestServerAssignUserRolesData.setProjectName(projectName);
            clientRequestServerAssignUserRolesData.setServerName(serverName);
            clientRequestServerAssignUserRolesData.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);

            synchronized (transportProxy) {
                transportProxy.write(clientRequestServerAssignUserRolesData);
            }
        }
    }//GEN-LAST:event_maintainRolesMenuItemActionPerformed

    private void deleteUserMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_deleteUserMenuItemActionPerformed
    {//GEN-HEADEREND:event_deleteUserMenuItemActionPerformed
        // Delete a user from the server.  This is meant to delete the selected
        // user completely from the server.  The user's authentication record
        // will be deleted, as well as all the user's roles in ALL projects.
        UserTreeNode userNode = (UserTreeNode) usersTree.getSelectionPath().getLastPathComponent();
        String userName = userNode.toString();

        if (userName.compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0) {
            JOptionPane.showMessageDialog(this, "You cannot delete the ADMIN user!");
            return;
        }

        // Get the name of the active server.
        DefaultUserTreeNode root = (DefaultUserTreeNode) usersTree.getModel().getRoot();
        String serverName = root.getServerName();

        String userMessage = "Delete user '" + userName + "' from server '" + serverName + "'?";
        int choice = JOptionPane.showConfirmDialog(this, userMessage, "Delete User", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

            // Request the deletion of the user from the server.
            ClientRequestServerRemoveUserData requestData = new ClientRequestServerRemoveUserData();
            requestData.setUserName(userName);
            requestData.setServerName(serverName);
            synchronized (transportProxy) {
                transportProxy.write(requestData);
            }
        }
    }//GEN-LAST:event_deleteUserMenuItemActionPerformed

    private void usersTreeMouseReleased(MouseEvent evt)//GEN-FIRST:event_usersTreeMouseReleased
    {//GEN-HEADEREND:event_usersTreeMouseReleased
        boolean adminUserFlag = false;

        if (getUserName() != null) {
            adminUserFlag = getUserName().compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0;
        }
        TreePath treePath = usersTree.getClosestPathForLocation(evt.getX(), evt.getY());

        if (evt.isPopupTrigger() || ((evt.getButton() == MouseEvent.BUTTON3) && (0 != (evt.getModifiers() & MouseEvent.MOUSE_RELEASED)))) {
            if (treePath != null) {
                // Make sure the item we clicked on is selected!!
                usersTree.setSelectionPath(treePath);

                // Figure out which node this is.
                TreeNode node = (TreeNode) treePath.getLastPathComponent();
                if (node instanceof UserTreeNode) {
                    // We're showing a project's users, and they right clicked
                    // over a user node.
                    UserTreeNode userNode = (UserTreeNode) node;
                    String userName = userNode.toString();

                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) usersTree.getModel().getRoot();
                    if (root instanceof DefaultProjectUserTreeNode) {
                        if (userName.compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0) {
                            actionUserMaintainProjectRoles.setEnabled(false);
                        } else {
                            actionUserMaintainProjectRoles.setEnabled(true);
                            userProjectPopupMenu.show(usersTree, evt.getX(), evt.getY());
                        }
                    } else if (root instanceof DefaultUserTreeNode) {
                        // We're showing the server's users and they right clicked
                        // over a user node.
                        if (userName.compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0) {
                            actionUserRemoveUserFromServer.setEnabled(false);
                            deleteUserMenuItem.setEnabled(false);
                        } else {
                            actionUserRemoveUserFromServer.setEnabled(true);
                            deleteUserMenuItem.setEnabled(true);
                        }

                        // Only the ADMIN user is allowed to delete users...
                        if (adminUserFlag) {
                            userServerPopupMenu.show(usersTree, evt.getX(), evt.getY());
                        }
                    }
                } else if (node instanceof DefaultUserTreeNode) {
                    // We're showing the server's users and they right clicked
                    // over the root user node.
                    // Only the ADMIN user is allowed to do this...
                    if (adminUserFlag) {
                        usersServerPopupMenu.show(usersTree, evt.getX(), evt.getY());
                    }
                } else if (node instanceof DefaultProjectUserTreeNode) {
                    // We're showing a project's users, and they right clicked
                    // over the root user node.
                    usersProjectPopupMenu.show(usersTree, evt.getX(), evt.getY());
                }
            }
        } else if (!evt.isPopupTrigger()) {
            if (treePath != null) {
                // Make sure the item we clicked on is selected!!
                usersTree.setSelectionPath(treePath);

                // Figure out which node this is.
                TreeNode node = (TreeNode) treePath.getLastPathComponent();
                if (node instanceof UserTreeNode) {
                    UserTreeNode userNode = (UserTreeNode) node;
                    String userName = userNode.toString();

                    DefaultMutableTreeNode root = (DefaultMutableTreeNode) usersTree.getModel().getRoot();
                    if (root instanceof DefaultProjectUserTreeNode) {
                        // We're showing a project's users, and they clicked
                        // over a user node.
                        if (userName.compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0) {
                            actionUserMaintainProjectRoles.setEnabled(false);
                        } else {
                            actionUserMaintainProjectRoles.setEnabled(true);
                        }
                    } else if (root instanceof DefaultUserTreeNode) {
                        // We're showing the server's users and they clicked
                        // over a user node.
                        if (userName.compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0) {
                            actionUserRemoveUserFromServer.setEnabled(false);
                        } else {
                            actionUserRemoveUserFromServer.setEnabled(true);
                        }
                    }
                }
            }
        }
    }//GEN-LAST:event_usersTreeMouseReleased

    private void changeUserPasswordMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_changeUserPasswordMenuItemActionPerformed
    {//GEN-HEADEREND:event_changeUserPasswordMenuItemActionPerformed
        // Get the name of the active server.
        DefaultUserTreeNode root = (DefaultUserTreeNode) usersTree.getModel().getRoot();
        String serverName = root.getServerName();

        // Get the name of the currently selected user.
        UserTreeNode userNode = (UserTreeNode) usersTree.getSelectionPath().getLastPathComponent();
        String userName = userNode.toString();

        // Display the dialog.
        ChangeUserPasswordDialog changeUserPasswordDialog = new ChangeUserPasswordDialog(this, true, userName);
        changeUserPasswordDialog.setVisible(true);

        String password = changeUserPasswordDialog.getPassword();
        if (password != null) {
            TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

            // Request the addition of the user to the server.
            ClientRequestChangePasswordData requestData = new ClientRequestChangePasswordData();
            requestData.setServerName(serverName);
            requestData.setUserName(changeUserPasswordDialog.getUserName());
            byte[] hashedPassword = Utility.getInstance().hashPassword(password);
            requestData.setNewPassword(hashedPassword);

            // If they are changing the ADMIN password, hang on to the new password, so
            // we can update the password map here with the new password.
            if (changeUserPasswordDialog.getUserName().equals(QVCSConstants.QVCS_ADMIN_USER)) {
                savePendingPassword(serverName, password);
            }
            synchronized (transportProxy) {
                transportProxy.write(requestData);
            }
        }
    }//GEN-LAST:event_changeUserPasswordMenuItemActionPerformed

    private void addServerMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addServerMenuItemActionPerformed
    {//GEN-HEADEREND:event_addServerMenuItemActionPerformed
        ServerPropertiesDialog serverPropertiesDialog = new ServerPropertiesDialog(this, true);
        serverPropertiesDialog.setVisible(true);
        saveServerProperties(serverPropertiesDialog);
    }//GEN-LAST:event_addServerMenuItemActionPerformed

    private void removeServerMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_removeServerMenuItemActionPerformed
    {//GEN-HEADEREND:event_removeServerMenuItemActionPerformed
        ServerTreeNode node = (ServerTreeNode) serverTree.getSelectionPath().getLastPathComponent();
        ServerProperties serverProperties = node.getServerProperties();
        int choice = JOptionPane.showConfirmDialog(this, "Remove server definition for " + serverProperties.getServerName() + "?", "Remove Server Definition", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            serverProperties.removePropertiesFile();
            serverModelMember.loadModel();
        }
    }//GEN-LAST:event_removeServerMenuItemActionPerformed

    private void defineNewProjectMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_defineNewProjectMenuItemActionPerformed
    {//GEN-HEADEREND:event_defineNewProjectMenuItemActionPerformed
        // Create a new project...
        ServerTreeNode node = (ServerTreeNode) serverTree.getSelectionPath().getLastPathComponent();
        ServerProperties serverProperties = node.getServerProperties();
        String serverName = serverProperties.getServerName();
        DefineNewProjectDialog newProjectDialog = new DefineNewProjectDialog(this, true);
        newProjectDialog.setVisible(true);

        if (newProjectDialog.getIsOK()) {
            // Lookup the transport proxy for this server.
            TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

            // We can only create a new project if we have a connection to the server
            if (transportProxy != null) {
                ClientRequestServerCreateProjectData createProjectRequest = new ClientRequestServerCreateProjectData();
                createProjectRequest.setNewProjectName(newProjectDialog.getProjectName());
                createProjectRequest.setUserName(transportProxy.getUsername());
                createProjectRequest.setServerName(serverName);
                createProjectRequest.setPassword(serverPasswordMapMember.get(serverName));
                createProjectRequest.setCreateReferenceCopyFlag(newProjectDialog.getCreateReferenceCopyFlag());
                createProjectRequest.setIgnoreCaseFlag(newProjectDialog.getIgnoreCaseFlag());
                createProjectRequest.setDefineAlternateReferenceLocationFlag(newProjectDialog.getDefineAlternateReferenceLocationFlag());
                createProjectRequest.setAlternateReferenceLocation(newProjectDialog.getAlternateReferenceLocation());
                synchronized (transportProxy) {
                    transportProxy.write(createProjectRequest);
                }
            }
        }
    }//GEN-LAST:event_defineNewProjectMenuItemActionPerformed

    private void serverPropertiesMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_serverPropertiesMenuItemActionPerformed
    {//GEN-HEADEREND:event_serverPropertiesMenuItemActionPerformed
        ServerTreeNode node = (ServerTreeNode) serverTree.getSelectionPath().getLastPathComponent();
        ServerProperties serverProperties = node.getServerProperties();
        String serverName = serverProperties.getServerName();
        ServerPropertiesDialog serverPropertiesDialog = new ServerPropertiesDialog(this, true, serverName);
        serverPropertiesDialog.setVisible(true);
        saveServerProperties(serverPropertiesDialog);
    }//GEN-LAST:event_serverPropertiesMenuItemActionPerformed

    private void serverPopupShutdownMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_serverPopupShutdownMenuItemActionPerformed
    {//GEN-HEADEREND:event_serverPopupShutdownMenuItemActionPerformed
        ServerTreeNode serverTreeNode = (ServerTreeNode) serverTree.getSelectionPath().getLastPathComponent();
        ServerProperties serverProperties = serverTreeNode.getServerProperties();
        String serverName = serverProperties.getServerName();

        // Lookup the transport proxy for this server.
        TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

        // We can only shut this thing down if we have a connection to the server
        if (transportProxy != null) {
            int choice = JOptionPane.showConfirmDialog(this, "Shutdown server " + serverName + "?", "Shutdown Server", JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
                ClientRequestServerShutdownData shutDownRequest = new ClientRequestServerShutdownData();
                shutDownRequest.setUserName(transportProxy.getUsername());
                shutDownRequest.setServerName(serverName);
                shutDownRequest.setPassword(serverPasswordMapMember.get(serverName));
                synchronized (transportProxy) {
                    transportProxy.write(shutDownRequest);
                }

                serverPasswordMapMember.remove(serverName);
                serverModelMember.logoffServer(serverName);
                transportProxyMapMember.remove(serverName);

                // And select the root node.
                serverTree.setSelectionPath(serverTree.getPathForRow(0));
            }
        }
    }//GEN-LAST:event_serverPopupShutdownMenuItemActionPerformed

    private void serverTreeMouseReleased(java.awt.event.MouseEvent evt)//GEN-FIRST:event_serverTreeMouseReleased
    {//GEN-HEADEREND:event_serverTreeMouseReleased
        if (evt.isPopupTrigger() || ((evt.getButton() == MouseEvent.BUTTON3) && (0 != (evt.getModifiers() & MouseEvent.MOUSE_RELEASED)))) {
            TreePath treePath = serverTree.getClosestPathForLocation(evt.getX(), evt.getY());
            boolean adminUserFlag = true;
            if (getUserName() != null) {
                adminUserFlag = getUserName().compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0;
            }
            if (treePath != null) {
                // Make sure the item we clicked on is selected!!
                serverTree.setSelectionPath(treePath);

                // Figure out which node this is.
                TreeNode node = (TreeNode) treePath.getLastPathComponent();
                if (node instanceof DefaultServerTreeNode) {
                    serverPopupMenu.show(serverTree, evt.getX(), evt.getY());
                } else if (node instanceof ServerTreeNode) {
                    if (adminUserFlag) {
                        defineNewProjectMenuItem.setEnabled(true);
                        serverPopupShutdownMenuItem.setEnabled(true);
                        maintainServerRolesMenuItem.setEnabled(true);
                    } else {
                        defineNewProjectMenuItem.setEnabled(false);
                        serverPopupShutdownMenuItem.setEnabled(false);
                        maintainServerRolesMenuItem.setEnabled(false);
                    }
                    serverInstancePopupMenu.show(serverTree, evt.getX(), evt.getY());
                } else if (node instanceof ProjectTreeNode) {
                    if (adminUserFlag) {
                        removeProjectMenuItem.setEnabled(true);
                    } else {
                        removeProjectMenuItem.setEnabled(false);
                    }
                    projectInstancePopupMenu.show(serverTree, evt.getX(), evt.getY());
                }
            }
        }
    }//GEN-LAST:event_serverTreeMouseReleased

    private void maintainRoles() {
        TreeNode node = null;

        if ((serverTree != null)
                && (serverTree.getSelectionPath() != null)) {
            node = (TreeNode) serverTree.getSelectionPath().getLastPathComponent();
        }

        if (node != null) {
            if (node instanceof ServerTreeNode) {
                ServerTreeNode serverTreeNode = (ServerTreeNode) node;
                ServerProperties serverProperties = serverTreeNode.getServerProperties();
                String serverName = serverProperties.getServerName();
                if (isLoggedInToServer(serverName)) {
                    // Lookup the transport proxy for this server.
                    TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

                    // We can only maintain roles if we're logged in, etc.
                    if (transportProxy != null) {
                        ClientRequestServerGetRoleNamesData clientRequestServerGetRoleNamesData = new ClientRequestServerGetRoleNamesData();
                        clientRequestServerGetRoleNamesData.setServerName(serverName);
                        clientRequestServerGetRoleNamesData.setUserName(transportProxy.getUsername());
                        clientRequestServerGetRoleNamesData.setPassword(serverPasswordMapMember.get(serverName));
                        synchronized (transportProxy) {
                            transportProxy.write(clientRequestServerGetRoleNamesData);
                        }

                        maintainRolePrivilegesDialogMember.setServerName(serverName);
                        maintainRolePrivilegesDialogMember.centerDialog();
                        maintainRolePrivilegesDialogMember.setVisible(true);
                    }
                }
            }
        }
    }

    private void saveServerProperties(ServerPropertiesDialog serverPropertiesDialog) {
        if (serverPropertiesDialog.isOK()) {
            // Save the information to the server properties file.
            ServerProperties serverProperties = new ServerProperties(System.getProperty(USER_DIR), serverPropertiesDialog.getServerName());
            serverProperties.setServerName(serverPropertiesDialog.getServerName());
            serverProperties.setServerIPAddress(serverPropertiesDialog.getServerIPAddress());
            serverProperties.setClientPort(serverPropertiesDialog.getClientPort());
            serverProperties.setServerAdminPort(serverPropertiesDialog.getServerAdminPort());
            serverProperties.saveProperties();
            serverModelMember.loadModel();
        }
    }

    /**
     * Exit the Application.
     */
    private void exitForm(java.awt.event.WindowEvent evt)
    {//GEN-FIRST:event_exitForm
        // Need to close any transports.
        transportProxyMapMember.values().stream().forEach((transportProxy) -> {
            transportProxy.close();
        });
        transportProxyMapMember.clear();
    }//GEN-LAST:event_exitForm

    /**
     * This is the main entry point for the admin application.
     *
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        // Run this on the swing thread.
        Runnable application = () -> {
            new EnterpriseAdmin(args).setVisible(true);
        };
        SwingUtilities.invokeLater(application);
    }

    private void loginToServer(String serverName, String userName, String password) {
        userNameMember = userName;
        ServerProperties serverProperties = new ServerProperties(System.getProperty(USER_DIR), serverName);

        int port = serverProperties.getServerAdminPort();

        // Make sure the admin port is defined.
        if (port == 0) {
            JOptionPane.showConfirmDialog(this, "You must define the admin port to connect to the server", "Define admin port required", JOptionPane.PLAIN_MESSAGE);

            // The user needs to define the server admin port before proceeding.
            serverPropertiesMenuItemActionPerformed(null);
            serverProperties = new ServerProperties(System.getProperty(USER_DIR), serverName);
            port = serverProperties.getServerAdminPort();
        }

        // Build the transport, and start a separate thread for reading.
        TransportProxyType transportType = serverProperties.getServerAdminTransport();
        byte[] hashedPassword = Utility.getInstance().hashPassword(password);
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(transportType, serverProperties, port, userName, hashedPassword, this, this);
        if (transportProxy != null) {
            transportProxyMapMember.put(serverName, transportProxy);
            pendingLoginPasswordMapMember.put(serverName, hashedPassword);
            requestProjectsAndUsersFromServer(serverName);
        } else {
            JOptionPane.showConfirmDialog(this, "Unable to login to server.  Server may be down.", "Server down message", JOptionPane.PLAIN_MESSAGE);

            // And select the root node.
            serverTree.setSelectionPath(serverTree.getPathForRow(0));
        }
    }

    public String getUserName() {
        return userNameMember;
    }

    public TransportProxyInterface getTransportProxyInterface(final String serverName) {
        return transportProxyMapMember.get(serverName);
    }

    private void requestProjectsAndUsersFromServer(String serverName) {
        TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

        // Request the project list from the server
        synchronized (transportProxy) {
            ClientRequestServerListProjectsData requestData = new ClientRequestServerListProjectsData();
            requestData.setServerName(serverName);
            transportProxy.write(requestData);

            // Request the user list from the server
            ClientRequestServerListUsersData requestUsersData = new ClientRequestServerListUsersData();
            requestUsersData.setServerName(serverName);
            transportProxy.write(requestUsersData);
        }
    }

    private void requestProjectUsersFromServer(String serverName, String projectName) {
        TransportProxyInterface transportProxy = transportProxyMapMember.get(serverName);

        // Request the project users from the server
        ClientRequestServerListProjectUsersData requestData = new ClientRequestServerListProjectUsersData();
        requestData.setServerName(serverName);
        requestData.setProjectName(projectName);
        requestData.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);
        synchronized (transportProxy) {
            transportProxy.write(requestData);
        }
    }

    private void showLoginToServerDialog() {
        ServerTreeNode node = (ServerTreeNode) serverTree.getSelectionPath().getLastPathComponent();
        ServerProperties serverProperties = node.getServerProperties();
        String serverName = serverProperties.getServerName();
        ServerLoginDialog loginDialog = new ServerLoginDialog(this, true, serverName);
        loginDialog.setVisible(true);

        String password = loginDialog.getPassword();
        String userName = loginDialog.getUserName();
        if (password != null) {
            loginToServer(serverName, userName, password);
        }
    }

    private void center() {
        Dimension parentSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension size = getSize();

        // Figure out the left boundaries
        double left = (parentSize.getWidth() - size.getWidth()) / 2.;
        double top = (parentSize.getHeight() - size.getHeight()) / 2.;

        Point myLocation = new Point();
        double x = left;
        double y = top;
        myLocation.setLocation(x, y);
        setLocation(myLocation);
    }

    UserTreeModel getUserModel() {
        return userModelMember;
    }

    @Override
    public String getPendingPassword(String serverName) {
        return pendingPasswordMapMember.get(serverName);
    }

    @Override
    public void notifyPasswordChange(com.qumasoft.qvcslib.response.ServerResponseChangePassword response) {
        if (response.getSuccess()) {
            if (response.getUserName().equals(QVCSConstants.QVCS_ADMIN_USER)) {
                // Update the password associated with the given server.
                String pendingPassword = getPendingPassword(response.getServerName());
                byte[] hashedPassword = Utility.getInstance().hashPassword(pendingPassword);
                serverPasswordMapMember.put(response.getServerName(), hashedPassword);
                JOptionPane.showConfirmDialog(this, "Password change successful for " + QVCSConstants.QVCS_ADMIN_USER, "Password Change Result", JOptionPane.PLAIN_MESSAGE);
            } else {
                JOptionPane.showConfirmDialog(this, "Password change successful for " + response.getUserName(), "Password Change Result", JOptionPane.PLAIN_MESSAGE);
            }
        } else {
            JOptionPane.showConfirmDialog(this, "Password change failed for " + response.getUserName() + "." + response.getResult(), "Password Change Result",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    @Override
    public void savePendingPassword(String serverName, String password) {
        pendingPasswordMapMember.put(serverName, password);
    }

    @Override
    public void notifyLoginResult(final com.qumasoft.qvcslib.response.ServerResponseLogin response) {
        if (response.getLoginResult()) {
            // The password was a good one.  Save it in the right place.
            byte[] hashedPassword = pendingLoginPasswordMapMember.get(response.getServerName());
            serverPasswordMapMember.put(response.getServerName(), hashedPassword);

            if (!response.getVersionsMatchFlag()) {
                // Run the update on the Swing thread.
                Runnable later = () -> {
                    ServerTreeNode node = (ServerTreeNode) serverTree.getSelectionPath().getLastPathComponent();
                    ServerProperties serverProperties = node.getServerProperties();

                    // Let the user know that the client is out of date.
                    int answer = JOptionPane.showConfirmDialog(null, "Login to server: " + response.getServerName()
                            + " succeeded. However, your admin client is out of date.  Did you want to update your admin client?", "Client out of date",
                            JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (answer == JOptionPane.OK_OPTION) {
                        UpdateManager.updateAdminClient(QVCSConstants.QVCS_RELEASE_VERSION, "admin_out.jar", serverProperties, true);
                    } else {
                        System.exit(0);
                    }
                };
                SwingUtilities.invokeLater(later);
            }
        } else {
            // Let the user know that the login failed.
            transportProxyMapMember.remove(response.getServerName());
            JOptionPane.showMessageDialog(this, "Login to server: " + response.getServerName() + " failed. " + response.getFailureReason(), "Login Failure",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    @Override
    public void notifyUpdateComplete() {
        // Run the update on the Swing thread.
        Runnable later = new Runnable() {

            @Override
            public void run() {
                // Time to exit the application.
                JOptionPane.showMessageDialog(null, "Updates received.  Please restart the application.", "Updates Complete", JOptionPane.PLAIN_MESSAGE);
                exitForm(null);
            }
        };
        SwingUtilities.invokeLater(later);
    }

    @Override
    public void exitTheApp() {
        exitForm(null);
    }

    private boolean isLoggedInToServer(final String serverName) {
        Object password = serverPasswordMapMember.get(serverName);
        return (password != null);
    }

    @Override
    public void notifyTransportProxyListener(ServerResponseInterface messageIn) {
        if (messageIn instanceof ServerResponseMessage) {
            ServerResponseMessage message = (ServerResponseMessage) messageIn;
            if (message.getPriority().equals(ServerResponseMessage.HIGH_PRIORITY)) {
                JOptionPane.showMessageDialog(this, message.getMessage(), "Server Message", JOptionPane.INFORMATION_MESSAGE);
            }
            LOGGER.info(message.getMessage());
        }
    }

    private void sizeMainWindow() {
        setSize(500, 300);
        splitPane.setDividerLocation(0.8);
    }
// Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem addProjectsUserMenuItem;
    private javax.swing.JMenuItem addServerMenuItem;
    private javax.swing.JMenuItem addServerUserMenuItem;
    private javax.swing.JSeparator bottomSeparator;
    private javax.swing.JMenuItem changeUserPasswordMenuItem;
    private javax.swing.JMenuItem defineNewProjectMenuItem;
    private javax.swing.JMenuItem deleteUserMenuItem;
    private javax.swing.JMenuBar frameMenuBar;
    private javax.swing.JMenu frameProjectsMenu;
    private javax.swing.JMenu frameRolesMenu;
    private javax.swing.JMenu frameServersMenu;
    private javax.swing.JMenu frameUsersMenu;
    private javax.swing.JPanel leftChildPanel;
    private javax.swing.JPanel leftParentPanel;
    private javax.swing.JScrollPane leftScrollPane;
    private javax.swing.JMenuItem maintainRolesMenuItem;
    private javax.swing.JMenuItem maintainServerRolesMenuItem;
    private javax.swing.JPopupMenu projectInstancePopupMenu;
    private javax.swing.JMenuItem projectPropertiesMenuItem;
    private javax.swing.JMenuItem removeProjectMenuItem;
    private javax.swing.JMenuItem removeServerMenuItem;
    private javax.swing.JPanel rightChildPanel;
    private javax.swing.JPanel rightParentPanel;
    private javax.swing.JScrollPane rightScrollPane;
    private javax.swing.JPopupMenu serverInstancePopupMenu;
    private javax.swing.JLabel serverLabel;
    private javax.swing.JPopupMenu serverPopupMenu;
    private javax.swing.JMenuItem serverPopupShutdownMenuItem;
    private javax.swing.JMenuItem serverPropertiesMenuItem;
    private javax.swing.JTree serverTree;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JPopupMenu userProjectPopupMenu;
    private javax.swing.JPopupMenu userServerPopupMenu;
    private javax.swing.JLabel usersLabel;
    private javax.swing.JPopupMenu usersProjectPopupMenu;
    private javax.swing.JPopupMenu usersServerPopupMenu;
    private javax.swing.JTree usersTree;
// End of variables declaration//GEN-END:variables

    @Override
    public void visualCompare(String file1Name, String file2Name, String display1, String display2) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    class AdminServerTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1L;
        ImageIcon m_ServersIcon;
        ImageIcon m_ServerIcon;
        ImageIcon m_ProjectIcon;

        AdminServerTreeCellRenderer(ImageIcon serversIcon, ImageIcon serverIcon, ImageIcon projectIcon) {
            m_ServersIcon = serversIcon;
            m_ServerIcon = serverIcon;
            m_ProjectIcon = projectIcon;
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
                setIcon(m_ServersIcon);
            }
            if (value instanceof ServerTreeNode) {
                setIcon(m_ServerIcon);
            } else if (value instanceof ProjectTreeNode) {
                setIcon(m_ProjectIcon);
            }
            return this;
        }
    }

    class AdminUserTreeCellRenderer extends DefaultTreeCellRenderer {

        private static final long serialVersionUID = 1L;
        ImageIcon m_UsersIcon;
        ImageIcon m_UserIcon;

        AdminUserTreeCellRenderer(ImageIcon usersIcon, ImageIcon userIcon) {
            m_UsersIcon = usersIcon;
            m_UserIcon = userIcon;
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
            if (value instanceof DefaultProjectUserTreeNode) {
                setIcon(m_UsersIcon);
            } else if (value instanceof DefaultUserTreeNode) {
                setIcon(m_UsersIcon);
            } else if (value instanceof UserTreeNode) {
                setIcon(m_UserIcon);
            }
            return this;
        }
    }

    class OurServerMenuListener extends com.qumasoft.guitools.MenuListenerAdapter {

        @Override
        public void menuSelected(javax.swing.event.MenuEvent e) {
            Font menuFont = new Font("Arial", 0, 12);

            frameServersMenu.removeAll();

            boolean projectFlag = false;
            boolean serverFlag = false;
            boolean serverLoggedInFlag = false;
            boolean adminUserFlag = false;
            TreeNode node = null;

            if ((serverTree != null)
                    && (serverTree.getSelectionPath() != null)) {
                node = (TreeNode) serverTree.getSelectionPath().getLastPathComponent();
            }

            if (node != null) {
                if (node instanceof ProjectTreeNode) {
                    projectFlag = true;
                } else if (node instanceof ServerTreeNode) {
                    serverFlag = true;
                    ServerTreeNode serverTreeNode = (ServerTreeNode) node;
                    ServerProperties serverProperties = serverTreeNode.getServerProperties();
                    serverLoggedInFlag = isLoggedInToServer(serverProperties.getServerName());
                }
            }

            if ((getUserName() != null) && (getUserName().compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0)) {
                adminUserFlag = true;
            }

            JMenuItem menuItem = frameServersMenu.add(actionServerAddServer);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);

            menuItem = frameServersMenu.add(actionServerRemoveServer);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_R);
            menuItem.setEnabled(serverFlag && adminUserFlag);

            menuItem = frameServersMenu.add(actionServerProperties);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_P);
            menuItem.setEnabled(serverFlag && adminUserFlag);

            menuItem = frameServersMenu.add(actionServerNewProject);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_N);
            menuItem.setEnabled(serverLoggedInFlag && adminUserFlag);

            menuItem = frameServersMenu.add(actionServerShutdown);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_S);
            menuItem.setEnabled(serverLoggedInFlag && adminUserFlag);

            // =====================================================================
            frameServersMenu.add(new javax.swing.JSeparator());
            // =====================================================================

            menuItem = frameServersMenu.add(actionServerExit);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_X);
        }
    }

    class OurProjectMenuListener extends com.qumasoft.guitools.MenuListenerAdapter {

        @Override
        public void menuSelected(javax.swing.event.MenuEvent e) {
            Font menuFont = new Font("Arial", 0, 12);

            frameProjectsMenu.removeAll();

            boolean projectFlag = false;
            boolean serverFlag = false;
            boolean serverLoggedInFlag = false;
            boolean adminUserFlag = false;
            TreeNode node = null;

            if ((serverTree != null)
                    && (serverTree.getSelectionPath() != null)) {
                node = (TreeNode) serverTree.getSelectionPath().getLastPathComponent();
            }

            if (node != null) {
                if (node instanceof ProjectTreeNode) {
                    projectFlag = true;
                } else if (node instanceof ServerTreeNode) {
                    serverFlag = true;
                    ServerTreeNode serverTreeNode = (ServerTreeNode) node;
                    ServerProperties serverProperties = serverTreeNode.getServerProperties();
                    serverLoggedInFlag = isLoggedInToServer(serverProperties.getServerName());
                }
            }

            if ((getUserName() != null) && (getUserName().compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0)) {
                adminUserFlag = true;
            }

            JMenuItem menuItem = frameProjectsMenu.add(actionProjectRemoveProject);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_R);
            menuItem.setEnabled(projectFlag && adminUserFlag);

            menuItem = frameProjectsMenu.add(actionProjectProjectProperties);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_P);
            menuItem.setEnabled(projectFlag);
        }
    }

    class OurUserMenuListener extends com.qumasoft.guitools.MenuListenerAdapter {

        @Override
        public void menuSelected(javax.swing.event.MenuEvent e) {
            Font menuFont = new Font("Arial", 0, 12);

            frameUsersMenu.removeAll();

            boolean projectFlag = false;
            boolean serverFlag = false;
            boolean userFlag = false;
            boolean changePasswordFlag = false;
            boolean serverLoggedInFlag = false;
            boolean adminUserFlag = false;
            TreeNode node = null;
            TreeNode userNode = null;

            if ((serverTree != null)
                    && (serverTree.getSelectionPath() != null)) {
                node = (TreeNode) serverTree.getSelectionPath().getLastPathComponent();
            }

            if (node != null) {
                if (node instanceof ProjectTreeNode) {
                    projectFlag = true;
                } else if (node instanceof ServerTreeNode) {
                    serverFlag = true;
                    ServerTreeNode serverTreeNode = (ServerTreeNode) node;
                    ServerProperties serverProperties = serverTreeNode.getServerProperties();
                    serverLoggedInFlag = isLoggedInToServer(serverProperties.getServerName());
                }
            }

            if ((usersTree != null)
                    && (usersTree.getSelectionPath() != null)) {
                userNode = (TreeNode) usersTree.getSelectionPath().getLastPathComponent();
            }

            if (userNode != null) {
                if (userNode instanceof UserTreeNode) {
                    String userName = userNode.toString();
                    userFlag = userName.compareTo(QVCSConstants.QVCS_ADMIN_USER) != 0;
                    changePasswordFlag = true;
                }
            }

            if ((getUserName() != null) && (getUserName().compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0)) {
                adminUserFlag = true;
            }

            JMenuItem menuItem = frameUsersMenu.add(actionUserAddUserToServer);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_A);
            menuItem.setEnabled(serverLoggedInFlag && adminUserFlag);

            menuItem = frameUsersMenu.add(actionUserRemoveUserFromServer);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_R);
            menuItem.setEnabled(serverLoggedInFlag && userFlag && adminUserFlag);

            menuItem = frameUsersMenu.add(actionUserAddUserToProject);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_P);
            menuItem.setEnabled(projectFlag);

            menuItem = frameUsersMenu.add(actionUserMaintainProjectRoles);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_F);
            menuItem.setEnabled(userFlag && projectFlag);

            menuItem = frameUsersMenu.add(actionUserChangePassword);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_C);
            menuItem.setEnabled(changePasswordFlag && serverLoggedInFlag && adminUserFlag);
        }
    }

    class OurRoleMenuListener extends com.qumasoft.guitools.MenuListenerAdapter {

        @Override
        public void menuSelected(javax.swing.event.MenuEvent e) {
            Font menuFont = new Font("Arial", 0, 12);

            frameRolesMenu.removeAll();

            boolean serverFlag = false;
            boolean serverLoggedInFlag = false;
            boolean adminUserFlag = false;
            TreeNode node = null;

            if ((serverTree != null)
                    && (serverTree.getSelectionPath() != null)) {
                node = (TreeNode) serverTree.getSelectionPath().getLastPathComponent();
            }

            if (node != null) {
                if (node instanceof ServerTreeNode) {
                    serverFlag = true;
                    ServerTreeNode serverTreeNode = (ServerTreeNode) node;
                    ServerProperties serverProperties = serverTreeNode.getServerProperties();
                    serverLoggedInFlag = isLoggedInToServer(serverProperties.getServerName());
                }
            }

            if ((getUserName() != null) && (getUserName().compareTo(QVCSConstants.QVCS_ADMIN_USER) == 0)) {
                adminUserFlag = true;
            }

            JMenuItem menuItem = frameRolesMenu.add(actionMaintainRole);
            menuItem.setFont(menuFont);
            menuItem.setMnemonic(java.awt.event.KeyEvent.VK_M);
            menuItem.setEnabled(serverLoggedInFlag && adminUserFlag && serverFlag);
        }
    }

    class ActionServerAddServer extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionServerAddServer() {
            super("Add Server Definition...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addServerMenuItemActionPerformed(null);
        }
    }

    class ActionServerRemoveServer extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionServerRemoveServer() {
            super("Remove Server Definition...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            removeServerMenuItemActionPerformed(null);
        }
    }

    class ActionServerShutdown extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionServerShutdown() {
            super("Shutdown server...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            serverPopupShutdownMenuItemActionPerformed(null);
        }
    }

    class ActionServerProperties extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionServerProperties() {
            super("Properties...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            serverPropertiesMenuItemActionPerformed(null);
        }
    }

    class ActionServerNewProject extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionServerNewProject() {
            super("New Project...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            defineNewProjectMenuItemActionPerformed(null);
        }
    }

    class ActionServerExit extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionServerExit() {
            super("Exit");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            exitForm(null);
            dispose();
        }
    }

    class ActionProjectRemoveProject extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionProjectRemoveProject() {
            super("Remove Project...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            removeProjectMenuItemActionPerformed(null);
        }
    }

    class ActionProjectProjectProperties extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionProjectProjectProperties() {
            super("Project Properties...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            projectPropertiesMenuItemActionPerformed(null);
        }
    }

    class ActionUserAddUserToServer extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionUserAddUserToServer() {
            super("Add user to server...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addServerUserMenuItemActionPerformed(null);
        }
    }

    class ActionUserRemoveUserFromServer extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionUserRemoveUserFromServer() {
            super("Remove user from server...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteUserMenuItemActionPerformed(null);
        }
    }

    class ActionUserAddUserToProject extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionUserAddUserToProject() {
            super("Add user to project...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            addProjectsUserMenuItemActionPerformed(null);
        }
    }

    class ActionUserMaintainProjectRoles extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionUserMaintainProjectRoles() {
            super("Maintain user roles...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            maintainRolesMenuItemActionPerformed(null);
        }
    }

    class ActionUserChangePassword extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionUserChangePassword() {
            super("Change Password...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            changeUserPasswordMenuItemActionPerformed(null);
        }
    }

    class ActionMaintainRole extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ActionMaintainRole() {
            super("Maintain Server Roles...");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            maintainRoles();
        }
    }

}
