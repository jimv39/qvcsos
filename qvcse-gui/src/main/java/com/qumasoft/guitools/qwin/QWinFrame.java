/*   Copyright 2004-2021 Jim Voris
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

import com.qumasoft.guitools.MenuListenerAdapter;
import com.qumasoft.guitools.compare.CompareFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.externalVisualCompare;
import static com.qumasoft.guitools.qwin.QWinUtility.logProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.reportSystemInfo;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.AboutDialog;
import com.qumasoft.guitools.qwin.dialog.ChangeUserPasswordDialog;
import com.qumasoft.guitools.qwin.dialog.DefineFileGroupsDialog;
import com.qumasoft.guitools.qwin.dialog.DefineWorkfileLocationDialog;
import com.qumasoft.guitools.qwin.dialog.MaintainFileFiltersDialog;
import com.qumasoft.guitools.qwin.dialog.ServerLoginDialog;
import com.qumasoft.guitools.qwin.dialog.UserPreferencesTabbedDialog;
import com.qumasoft.guitools.qwin.operation.OperationBaseClass;
import com.qumasoft.guitools.qwin.operation.OperationChangePassword;
import com.qumasoft.guitools.qwin.operation.OperationCheckInArchive;
import com.qumasoft.guitools.qwin.operation.OperationCheckOutArchive;
import com.qumasoft.guitools.qwin.operation.OperationCreateArchive;
import com.qumasoft.guitools.qwin.operation.OperationGet;
import com.qumasoft.guitools.qwin.operation.OperationLabelArchive;
import com.qumasoft.guitools.qwin.operation.OperationLockArchive;
import com.qumasoft.guitools.qwin.operation.OperationRenameFile;
import com.qumasoft.guitools.qwin.operation.OperationUndoCheckOut;
import com.qumasoft.guitools.qwin.operation.OperationVisualCompare;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.CheckInCommentProperties;
import com.qumasoft.qvcslib.CheckOutCommentManager;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManager;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerForRoot;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.ExitAppInterface;
import com.qumasoft.qvcslib.LabelManager;
import com.qumasoft.qvcslib.PasswordChangeListenerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.TimerManager;
import com.qumasoft.qvcslib.TransactionInProgressListenerInterface;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.TransportProxyListenerInterface;
import com.qumasoft.qvcslib.TransportProxyType;
import com.qumasoft.qvcslib.UpdateManager;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.UserProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.VisualCompareInterface;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import com.qumasoft.qvcslib.WorkfileDirectoryManager;
import com.qumasoft.qvcslib.response.ServerResponseChangePassword;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseSuccess;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.SplashScreen;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import org.slf4j.LoggerFactory;

/**
 * The main class for the QVCS-Enterprise client application. This is the main frame window of the application. This is a singleton.
 * @author Jim Voris
 */
public final class QWinFrame extends JFrame implements PasswordChangeListenerInterface, TransportProxyListenerInterface, TransactionInProgressListenerInterface,
                                                                                                                         ExitAppInterface, VisualCompareInterface {

    private static final long serialVersionUID = 11L;

    // Create our logger object
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(QWinFrame.class);

    /** Global project name -- All projects. */
    public static final String GLOBAL_PROJECT_NAME = "All Projects";

    static QWinFrame getQwinFrameSingleton() {
        return qwinFrameSingleton;
    }
    private static final String[] STATUS_BAR_ARRAY = {
        "  File Count: xx  ",
        "  User Name: UNKNOWN ",
        "  Project Name: UNKNOWN  "
    };
    private static QWinFrame qwinFrameSingleton = null;
    private DirectoryManagerInterface[] currentDirectoryManagers;
    private final DirectoryManagerForRoot rootDirectoryManager;
    private String appendedPath = "";
    private String projectType = QVCSConstants.QVCS_REMOTE_PROJECT_TYPE;
    private final String qvcsClientHomeDirectory;
    private final Map<String, String> pendingPasswordMap;
    private final Map<String, UsernamePassword> pendingLoginPasswordMap;
    private final EventListenerList changeListenerArray;
    private String serverName = "";
    private String projectName = "";
    private String branchName = "";
    private String systemUserName;
    private String loggedInUserName = "";
    private RightDetailPane rightDetailPane;
    private LoggingPane revisionInfoPane;
    private LoggingPane labelInfoPane;
    private ActivityPane activityPane;
    private RevAndLabelInfoDetailPane revAndLabelInfoPane;
    private RightParentPane rightParentPane;
    private Component originalGlassPane;

    private final ActionRecurse actionRecurse;
    private final ActionCompare actionCompare;
    private final ActionRenameFile actionRenameFile;
    private final ActionLabel actionLabel;
    private final ActionLock actionLock;
    private final ActionUnLock actionUnLock;
    private final ActionCheckIn actionCheckIn;
    private final ActionAdd actionAdd;
    private final ActionCheckOut actionCheckOut;
    private final ActionGet actionGet;
    private final ActionExit actionExit;

    private final ImageIcon frameIcon;
    // Small toolbar buttons.
    private final ImageIcon smallGetButtonImage;
    private final ImageIcon smallCheckoutButtonImage;
    private final ImageIcon smallAddFileButtonImage;
    private final ImageIcon smallCheckInButtonImage;
    private final ImageIcon smallUndoCheckOutButtonImage;
    private final ImageIcon smallLockToolButtonImage;
    private final ImageIcon smallLabelButtonImage;
    private final ImageIcon smallFileGroupButtonImage;
    private final ImageIcon smallCompareButtonImage;
    private final ImageIcon smallNoRecurseButtonImage;
    private final ImageIcon smallRecurseButtonImage;
    // Large toolbar buttons.
    private final ImageIcon bigGetButtonImage;
    private final ImageIcon bigCheckoutButtonImage;
    private final ImageIcon bigAddFileButtonImage;
    private final ImageIcon bigCheckInButtonImage;
    private final ImageIcon bigUndoCheckOutButtonImage;
    private final ImageIcon bigLockToolButtonImage;
    private final ImageIcon bigLabelButtonImage;
    private final ImageIcon bigFileGroupButtonImage;
    private final ImageIcon bigCompareButtonImage;
    private final ImageIcon bigNoRecurseButtonImage;
    private final ImageIcon bigRecurseButtonImage;
    // Active toolbar buttons
    private ImageIcon getButtonImage;
    private ImageIcon checkoutButtonImage;
    private ImageIcon addFileButtonImage;
    private ImageIcon checkInButtonImage;
    private ImageIcon undoCheckOutButtonImage;
    private ImageIcon lockToolButtonImage;
    private ImageIcon labelButtonImage;
    private ImageIcon fileGroupButtonImage;
    private ImageIcon compareButtonImage;
    private ImageIcon noRecurseButtonImage;
    private ImageIcon recurseButtonImage;
    private com.qumasoft.guitools.qwin.QWinStatusBar frameStatusBar;
    private String[] commandLineArgs;
    private String applicationHomeDirectory;
    private String userWorkfileDirectory;
    private String sortColumn;
    private final UserProperties userProperties;
    private UserLocationProperties userLocationProperties;
    private ServerProperties activeServerProperties;
    private CheckInCommentProperties checkInCommentProperties;
    private ProjectTreePanel projectTreePanel;
    private JTable fileTable;
    private RightFilePane rightFilePane;
    private ProjectTreeModel projectTreeModel;
    private ProjectTreeControl projectTreeControl;
    private boolean initInProgressFlag = true;
    private boolean recurseFlag = false;
    private boolean refreshRequiredFlag = false;
    private boolean ignoreFilterChangeFlag = false;
    private boolean ignoreTreeChangesFlag = false;
    private boolean shutdownHouseKeepingCompletedFlag = false;
    private final Map<String, UsernamePassword> usernamePasswordMap;
    private final ActivityPaneLogLevelButtonGroup logLevelButtonGroup;
    private FilteredFileTableModel filteredFileTableModel;
    private TimerTask refreshTask;
    private TimerTask autoRefreshTimerTask;
    private final Map<Integer, java.awt.Font> fontMap;
    /** The time we wait to refresh the screen. */
    public static final long REFRESH_DELAY = 500; // 1/2 a second
    private static final int MENU_FONT_SIZE = 12;

    private static SplashScreen splashScreen;
    private static Rectangle2D splashTextArea;
    private static Rectangle2D splashProgressArea;
    private static Graphics2D splashGraphics;
    private static Font splashFont;
    private static final int SPLASH_FONT_SIZE = 14;

    /**
     * Creates new form QWinFrame
     * @param args the command line arguments.
     */
    QWinFrame(String[] args) {
        this.bigRecurseButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_recurse.png"));
        this.smallFileGroupButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/filegroup.png"));
        this.bigNoRecurseButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_norecurse.png"));
        this.bigCompareButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_compare.png"));
        this.bigFileGroupButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_filegroup.png"));
        this.bigLabelButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_lblproj.png"));
        this.bigLockToolButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_locktool.png"));
        this.bigUndoCheckOutButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_undochkout.png"));
        this.bigCheckInButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_chkin.png"));
        this.bigAddFileButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_AddFile.png"));
        this.bigCheckoutButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_chkout.png"));
        this.bigGetButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/big_getfile.png"));
        this.smallRecurseButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/recurse.png"));
        this.smallNoRecurseButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/norecurse.png"));
        this.smallCompareButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/compare.png"));
        this.smallLabelButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/lblproj.png"));
        this.smallLockToolButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/locktool.png"));
        this.smallUndoCheckOutButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/undochkout.png"));
        this.smallCheckInButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/chkin.png"));
        this.smallAddFileButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/AddFile.png"));
        this.smallCheckoutButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/chkout.png"));
        this.smallGetButtonImage = new ImageIcon(ClassLoader.getSystemResource("images/getfile.png"));
        this.frameIcon = new ImageIcon(ClassLoader.getSystemResource("images/qwin16.png"), "Quma Software, Inc.");
        this.fontMap = new HashMap<>();
        this.logLevelButtonGroup = new ActivityPaneLogLevelButtonGroup();
        this.usernamePasswordMap = Collections.synchronizedMap(new TreeMap<>());
        this.actionExit = new ActionExit();
        this.actionGet = new ActionGet();
        this.actionCheckOut = new ActionCheckOut();
        this.actionAdd = new ActionAdd();
        this.actionCheckIn = new ActionCheckIn();
        this.actionUnLock = new ActionUnLock();
        this.actionLock = new ActionLock();
        this.actionLabel = new ActionLabel();
        this.actionRenameFile = new ActionRenameFile();
        this.actionCompare = new ActionCompare();
        this.actionRecurse = new ActionRecurse();
        this.changeListenerArray = new EventListenerList();
        this.pendingLoginPasswordMap = Collections.synchronizedMap(new TreeMap<>());
        this.pendingPasswordMap = Collections.synchronizedMap(new TreeMap<>());
        this.rootDirectoryManager = new DirectoryManagerForRoot();

        if (args.length > 0) {
            commandLineArgs = args;
            System.setProperty("user.dir", commandLineArgs[0]);
        } else if (Utility.isMacintosh()) {
            commandLineArgs = new String[1];
            commandLineArgs[0] = System.getProperty("user.home") + "/Library/Application Support/qvcse-client";
            try {
                File file = new File(commandLineArgs[0]);
                if (!file.exists()) {
                    Path path = file.toPath();
                    Files.createDirectory(path);
                }
            } catch (IOException e) {
                LOGGER.warn(null, e);
            }
        } else {
            commandLineArgs = new String[1];
            commandLineArgs[0] = System.getProperty("user.dir");
        }
        qvcsClientHomeDirectory = commandLineArgs[0];
        userProperties = new UserProperties(qvcsClientHomeDirectory);

        // Set the base directory for the transport so it will know where to find and put this user's files.
        TransportProxyFactory.getInstance().setDirectory(qvcsClientHomeDirectory);

        splashText("Finished constructor...");
        // <editor-fold>
        splashProgress(10);
        // </editor-fold>
    }

    /**
     * Entry point for QVCS-Enterprise client application.
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        initSplashScreen();

        // Run this on the swing thread.
        Runnable swingTask = () -> {
            QWinFrame qwinFrame = new QWinFrame(args);
            QWinFrame.setQwinFrameSingleton(qwinFrame);
            qwinFrame.initialize();
            qwinFrame.setVisible(true);
        };
        SwingUtilities.invokeLater(swingTask);
    }

    static void setQwinFrameSingleton(QWinFrame aQwinFrameSingleton) {
        qwinFrameSingleton = aQwinFrameSingleton;
    }

    /**
     * Initialize the splash screen so we can show progress on the splash screen.
     */
    public static void initSplashScreen() {
        splashScreen = SplashScreen.getSplashScreen();
        if (splashScreen != null) {
            Dimension splashScreenDim = splashScreen.getSize();
            int height = splashScreenDim.height;
            int width = splashScreenDim.width;

            // Define the area for our status information.
            // <editor-fold>
            splashTextArea = new Rectangle2D.Double(15., height * 0.88, width * .85, 32.);
            splashProgressArea = new Rectangle2D.Double(15., height * .96, width * .85, 6.);
            // </editor-fold>

            // Create the Graphics environment for drawing the status information.
            splashGraphics = splashScreen.createGraphics();
            splashFont = new Font("Arial", Font.PLAIN, SPLASH_FONT_SIZE);
            splashGraphics.setFont(splashFont);

            // Initialize the status... we're starting.
            splashText("Starting QVCS-Enterprise client...");
            splashProgress(0);
        }
    }

    private static void splashText(String message) {
       if (splashScreen != null && splashScreen.isVisible()) {
            // erase the last status text
            splashGraphics.setPaint(Color.LIGHT_GRAY);
            splashGraphics.fill(splashTextArea);

            // draw the text
            splashGraphics.setPaint(Color.BLACK);
            // <editor-fold>
            splashGraphics.drawString(message, (int)(splashTextArea.getX() + 5),(int)(splashTextArea.getY() + 15));
            // </editor-fold>

            // make sure it's displayed
            splashScreen.update();
        }
    }

    /**
     * Display a (very) basic progress bar.
     * @param pct how much of the progress bar to display 0-100.
     */
    public static void splashProgress(int pct) {
        if (splashScreen != null && splashScreen.isVisible()) {

            // Note: 3 colors are used here to demonstrate steps
            // erase the old one
            splashGraphics.setPaint(Color.LIGHT_GRAY);
            splashGraphics.fill(splashProgressArea);

            // draw an outline
            splashGraphics.setPaint(Color.BLUE);
            splashGraphics.draw(splashProgressArea);

            // Calculate the width corresponding to the correct percentage
            int x = (int) splashProgressArea.getMinX();
            int y = (int) splashProgressArea.getMinY();
            int width = (int) splashProgressArea.getWidth();
            int height = (int) splashProgressArea.getHeight();

            // <editor-fold>
            int doneWidth = Math.round(pct * width / 100.f);
            doneWidth = Math.max(0, Math.min(doneWidth, width - 1));  // limit 0-width
            // </editor-fold>

            // fill the done part one pixel smaller than the outline
            splashGraphics.setPaint(Color.GREEN);
            splashGraphics.fillRect(x, y + 1, doneWidth, height - 1);

            // make sure it's displayed
            splashScreen.update();
        }
    }

    void initialize() {
        // Get the look and feel that the user wants us to use.
        String lookAndFeel = getUserProperties().getLookAndFeel();

        // Adjust the look and feel...
        if (lookAndFeel != null && lookAndFeel.length() > 0) {
            try {
                UIManager.setLookAndFeel(lookAndFeel);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
                warnProblem("Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
            }
        }

        // Set the frame icon to the Quma standard icon.
        this.setIconImage(frameIcon.getImage());

        // Set up which toolbar buttons we'll use.
        initToolbarButtons();

        // Init the form's components.
        initComponents();

        // Perform additional initialization
        initializeTheApplication();

        pack();

        restoreFrame();

        // Set the splitter to the bottom
        getRightParentPane().initSplitter();

        // Report info about the System
        reportSystemInfo();

        // Report the version to the log file.
        logProblem("QVCS-Enterprise client version: '" + QVCSConstants.QVCS_RELEASE_VERSION + "'.");

        // Save the original glass pane.
        originalGlassPane = getGlassPane();

        // Set up the file menu.
        initFileMenu();

        // Set up the project menu.
        initProjectMenu();

        // Set up the server menu.
        initServerMenu();

        // Set up the accelerator keys
        initAcceleratorKeys();

        // Register our shutdown thread. This shutdown thread is used on the Mac in case the user chooses the quit menu option, instead of the File/Exit menu.
        Runtime.getRuntime().addShutdownHook(new QWinFrame.ShutdownThread());

        // Refresh the screen (for Java 6).
        Runnable refresh = () -> {
            repaint();
        };
        if (splashScreen != null) {
            splashScreen.close();
        }
        SwingUtilities.invokeLater(refresh);
    }

    /**
     * Get the 'home' directory for the application. This is set on the command line, or if not defined on the command line,
     * it will be (on the Mac) the ~/Library/Application Support/qvcse-client directory. This directory is where the application stores
     * user specific data.
     * @return the directory where user specific data is stored.
     */
    public String getQvcsClientHomeDirectory() {
        return qvcsClientHomeDirectory;
    }

    private void initFileMenu() {
        Font menuFont = new Font("Arial", 0, MENU_FONT_SIZE);

        // =====================================================================
        getRightFilePane().addPopupMenuItems(fileMainMenu);

        // =====================================================================
        fileMainMenu.add(new javax.swing.JSeparator());
        // =====================================================================

        JMenuItem menuItem = fileMainMenu.add(actionExit);
        menuItem.setFont(menuFont);
        menuItem.setMnemonic(java.awt.event.KeyEvent.VK_X);

        // So we enabled/disable the appropriate menu items.
        fileMainMenu.addMenuListener(new OurFileMenuListener());
        splashText("Initialized File menu...");
        splashProgress(70);
    }

    private void initProjectMenu() {
        // So we enabled/disable the appropriate menu items.
        projectMainMenu.addMenuListener(new OurProjectMenuListener());
        splashText("Initialized Project menu...");
        splashProgress(80);
    }

    private void initServerMenu() {
        // So we enabled/disable the appropriate menu items.
        serverMainMenu.addMenuListener(new OurServerMenuListener());
        splashText("Initialized Server menu...");
        splashProgress(90);
    }

    private void initToolbarButtons() {
        boolean useLargeButtonsFlag = getUserProperties().getUseLargeToolbarButtons();
        if (useLargeButtonsFlag) {
            getButtonImage = bigGetButtonImage;
            checkoutButtonImage = bigCheckoutButtonImage;
            addFileButtonImage = bigAddFileButtonImage;
            checkInButtonImage = bigCheckInButtonImage;
            undoCheckOutButtonImage = bigUndoCheckOutButtonImage;
            lockToolButtonImage = bigLockToolButtonImage;
            labelButtonImage = bigLabelButtonImage;
            fileGroupButtonImage = bigFileGroupButtonImage;
            compareButtonImage = bigCompareButtonImage;
            noRecurseButtonImage = bigNoRecurseButtonImage;
            recurseButtonImage = bigRecurseButtonImage;
        } else {
            getButtonImage = smallGetButtonImage;
            checkoutButtonImage = smallCheckoutButtonImage;
            addFileButtonImage = smallAddFileButtonImage;
            checkInButtonImage = smallCheckInButtonImage;
            undoCheckOutButtonImage = smallUndoCheckOutButtonImage;
            lockToolButtonImage = smallLockToolButtonImage;
            labelButtonImage = smallLabelButtonImage;
            fileGroupButtonImage = smallFileGroupButtonImage;
            compareButtonImage = smallCompareButtonImage;
            noRecurseButtonImage = smallNoRecurseButtonImage;
            recurseButtonImage = smallRecurseButtonImage;
        }
        splashText("Initialized toolbar...");
        splashProgress(20);
    }

    private void restoreFrame() {
        boolean maximizeFlag = getUserProperties().getFrameMaximizeFlag();

        // Restore the frame to the location and size last used by this user.
        int xLocation = getUserProperties().getFrameXLocation();
        int yLocation = getUserProperties().getFrameYLocation();
        setLocation(xLocation, yLocation);

        int frameWidth = getUserProperties().getFrameWidth();
        int frameHeight = getUserProperties().getFrameHeight();
        setSize(frameWidth, frameHeight);

        // Restore the tree width.
        int treeWidth = getUserProperties().getTreeWidth();
        if (treeWidth > 0) {
            verticalSplitPane.setDividerLocation(treeWidth);
        }

        // Restore column widths
        int columnCount = getFileTable().getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            String columnName = getFileTable().getColumnName(i);
            TableColumn column = getFileTable().getColumn(columnName);
            int columnWidth = getUserProperties().getColumnWidth(i);
            if (columnWidth > 0) {
                column.setPreferredWidth(columnWidth);
            }
        }
        if (maximizeFlag) {
            setExtendedState(MAXIMIZED_BOTH);
        }
    }

    /**
     * Get the QWinFrame 'singleton'.
     * @return the QWinFrame 'singleton'.
     */
    public static QWinFrame getQWinFrame() {
        return qwinFrameSingleton;
    }

    synchronized DirectoryManagerInterface[] getCurrentDirectoryManagers() {
        return currentDirectoryManagers;
    }

    private void waitForDirectoryManagersToInit() {
        DirectoryManagerInterface[] directoryManagers;
        synchronized (this) {
            directoryManagers = currentDirectoryManagers;
        }
        int directoryManagerCount = directoryManagers.length;
        for (int i = 0; i < directoryManagerCount; i++) {
            DirectoryManagerInterface directoryManagerInterface = directoryManagers[i];
            if (directoryManagerInterface instanceof DirectoryManager) {
                ArchiveDirManagerProxy archiveDirManagerProxy = (ArchiveDirManagerProxy) directoryManagerInterface.getArchiveDirManager();
                archiveDirManagerProxy.waitForInitToComplete();
            }
        }
    }

    private void initializeTheApplication() {
        // Add the status bar to the bottom of the frame.
        frameStatusBar = new QWinStatusBar(STATUS_BAR_ARRAY);
        getContentPane().add(frameStatusBar, BorderLayout.SOUTH);
        systemUserName = System.getProperty("user.name");
        rootDirectoryManager.setUserName(systemUserName);

        // Register the status bar as a listener of the Client Transaction Manager so we can keep track of in-progress transactions.
        ClientTransactionManager.getInstance().addTransactionInProgressListener(frameStatusBar);

        // Register the Project Tree Panel as a listener so it can display the current archive location.
        addChangeListener(projectTreePanel);

        // If there are no arguments on the command line, default the home directory to the current directory.
        if (commandLineArgs.length == 0) {
            commandLineArgs = new String[1];
            commandLineArgs[0] = System.getProperty("user.dir");
        }
        setApplicationHomeDirectory(commandLineArgs[0]);

        // Get our check-in comments
        checkInCommentProperties = new CheckInCommentProperties(commandLineArgs[0], systemUserName);

        // Initialize the workfile digest manager
        WorkfileDigestManager.getInstance().initialize();

        // Initialize the branch utility manager.
        ViewUtilityManager.getInstance().initialize();

        // Initialize the checkout comment manager
        CheckOutCommentManager.getInstance().initialize();

        // Initialize the file group manager
        FileGroupManager.getInstance().initialize();

        // Initialize the label manager
        LabelManager.setUserName(systemUserName);
        LabelManager.getInstance().initialize();

        // Initialize the file filters combo box.
        initFileFilter();

        // Init the current directory.
        initSortColumn();

        // Make us a listener for password change responses
        TransportProxyFactory.getInstance().addChangedPasswordListener(this);

        ClientTransactionManager.getInstance().addTransactionInProgressListener(this);

        // Install the thread tracking repaint manager.
        Runnable installRepaintManager = () -> {
            RepaintManager.setCurrentManager(new ThreadCheckingRepaintManager());
        };
        SwingUtilities.invokeLater(installRepaintManager);

        // Set the auto-refresh flag.
        setAutoUpdateFlag(getUserProperties().getAutoUpdateFlag());
        splashText("Finished application initialization...");
        splashProgress(50);

    }

    private void initAcceleratorKeys() {
        // Set up 'accelerator' keys
        javax.swing.KeyStroke keyRecurse = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyRecurse, "recurseKeyAction");
        getRootPane().getActionMap().put("recurseKeyAction", actionRecurse);

        javax.swing.KeyStroke keyCompare;
        if (Utility.isMacintosh()) {
            keyCompare = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F8, 0);
            compareButton.setToolTipText("Compare (F8)");
        } else {
            keyCompare = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F10, 0);
            compareButton.setToolTipText("Compare (F10)");
        }
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyCompare, "compareKeyAction");
        getRootPane().getActionMap().put("compareKeyAction", actionCompare);

        // I'd like to use F2 here instead of F12, but it DOES NOT WORK, for some unknown reason. I have tried any number of variations to try to get it to work, but F2 seems to
        // get ignored for some reason, or someone else has captured it.
        javax.swing.KeyStroke keyRenameFile = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F12, 0);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyRenameFile, "renameFileKeyAction");
        getRootPane().getActionMap().put("renameFileKeyAction", actionRenameFile);

        javax.swing.KeyStroke keyLabel = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F3, 0);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyLabel, "labelKeyAction");
        getRootPane().getActionMap().put("labelKeyAction", actionLabel);

        javax.swing.KeyStroke keyLock = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyLock, "lockKeyAction");
        getRootPane().getActionMap().put("lockKeyAction", actionLock);

        javax.swing.KeyStroke keyUnLock = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Z, java.awt.event.InputEvent.ALT_DOWN_MASK);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyUnLock, "unlockKeyAction");
        getRootPane().getActionMap().put("unlockKeyAction", actionUnLock);

        javax.swing.KeyStroke keyCheckIn = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyCheckIn, "checkInKeyAction");
        getRootPane().getActionMap().put("checkInKeyAction", actionCheckIn);

        javax.swing.KeyStroke keyAdd = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.ALT_DOWN_MASK);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyAdd, "addKeyAction");
        getRootPane().getActionMap().put("addKeyAction", actionAdd);

        javax.swing.KeyStroke keyCheckOut = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyCheckOut, "checkOutKeyAction");
        getRootPane().getActionMap().put("checkOutKeyAction", actionCheckOut);

        javax.swing.KeyStroke keyGet = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_G, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyGet, "getKeyAction");
        getRootPane().getActionMap().put("getKeyAction", actionGet);
        splashText("Initialized Accelerator keys...");
        splashProgress(95);
    }

    QWinStatusBar getStatusBar() {
        return frameStatusBar;
    }

    private void initFileFilter() {
        // Initialize the file filter to the one that was in use when the user last used the application.
        FilterManager.getFilterManager().initialize();
        FileFiltersComboModel comboModel = new FileFiltersComboModel();
        String previousFilterCollectionName = getUserProperties().getActiveFileFilterName();
        if (previousFilterCollectionName == null) {
            previousFilterCollectionName = FilterManager.ALL_FILTER;
        }
        setFilterModel(comboModel, previousFilterCollectionName);
    }

    private void initSortColumn() {
        // Set the column we are to sort by.
        String srtColumn = getUserProperties().getCurrentSortColumn();
        if (srtColumn.length() > 0) {
            setSortColumn(srtColumn);
        } else {
            setSortColumn(QVCSConstants.QVCS_FILENAME_COLUMN);
        }
        initInProgressFlag = false;
    }

    void setSortColumn(String srtColumn) {
        this.sortColumn = srtColumn;
    }

    String getSortColumn() {
        return sortColumn;
    }

    /**
     * Get the current (active) font size for the application.
     * @return the current font size for the application.
     */
    public int getFontSize() {
        return getUserProperties().getFontSize();
    }

    /**
     * Set the font size for the application.
     * @param fontSize the font size to use.
     */
    public void setFontSize(int fontSize) {
        getUserProperties().setFontSize(fontSize);

        // Set the font size for the major visual elements of the application.
        getRightFilePane().setFontSize(fontSize);
        getTreeControl().setFontSize(fontSize);
        getProjectTreePanel().setFontSize(fontSize);
        getRightDetailPane().setFontSize(fontSize);
    }

    /**
     * Get a font of the given size.
     * @param fontSize the font size to get.
     * @return a Font of the given size. It will be an Arial font.
     */
    public Font getFont(int fontSize) {
        return fontMap.computeIfAbsent(fontSize, f -> new java.awt.Font("Arial", 0, f));
    }

    void setCurrentAppendedPath(final String project, final String branch, final String path, final String projType, boolean projectNodeSelectedFlag) {
        try {
            final String server = ProjectTreeControl.getInstance().getActiveServerName();
            if (!projectNodeSelectedFlag && server != null) {
                if ((project != null) && (path != null) && (projType != null)) {
                    if (getRefreshRequired()
                            || (0 != server.compareToIgnoreCase(getServerName()))
                            || (0 != project.compareToIgnoreCase(project)) || // Can't use getProjectName() since that gets its value from the tree control!
                            (0 != branch.compareToIgnoreCase(getBranchName()))
                            || (0 != path.compareToIgnoreCase(getAppendedPath()))
                            || (0 != projType.compareToIgnoreCase(getProjectType()))) {
                        setRefreshRequired(false);
                        setServerName(server);
                        setProjectName(project);
                        setBranchName(branch);
                        setProjectType(projType);

                        // For remote projects, we cannot select into any depth on the project since the remote project's child nodes don't exist yet.
                        if (0 == projType.compareTo(QVCSConstants.QVCS_REMOTE_PROJECT_TYPE)) {
                            if (initInProgressFlag) {
                                // If initialization is in progress, and the last project was remote, just select the root node -- the user will have to select the remote node
                                // later -- by hand.
                                setProjectName(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME);
                                setProjectType(QVCSConstants.QVCS_REMOTE_PROJECT_TYPE);
                                setAppendedPath("");
                                synchronized (this) {
                                    currentDirectoryManagers = new DirectoryManagerInterface[1];
                                    currentDirectoryManagers[0] = rootDirectoryManager;
                                }
                            } else {
                                // This is a remote project, get the password string and set it using the line of code below.
                                // We'll want to cache the password in a map here so we don't ask the user for it again.
                                final AbstractProjectProperties projectProperties = ProjectTreeControl.getInstance().getActiveProject();
                                UsernamePassword usernamePassword = getUsernamePassword(server);
                                if (usernamePassword != null) {
                                    updateDirectoryManagerPassword(server, usernamePassword);
                                    setLoggedInUserName(usernamePassword.userName);
                                }
                                setAppendedPath(path);
                                updateUserWorkfileDirectory(path);

                                // Put this on a separate thread since it could take some time.  We will put up a progress dialog.
                                Runnable worker = () -> {
                                    DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), getAppendedPath());
                                    DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), server, directoryCoordinate,
                                            getProjectType(), projectProperties, getUserWorkfileDirectory(), null, false);
                                    getDirectoryManagers(directoryManager);
                                    fireThingsChanged();
                                };

                                // Put all this on a separate worker thread.
                                new Thread(worker).start();
                            }
                        } else {
                            if (0 == project.compareTo(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME)) {
                                setAppendedPath("");
                                synchronized (this) {
                                    currentDirectoryManagers = new DirectoryManagerInterface[1];
                                    currentDirectoryManagers[0] = rootDirectoryManager;
                                    fireThingsChanged();
                                }
                            }
                        }
                    }
                }
            } else {
                setProjectName(QVCSConstants.QWIN_DEFAULT_PROJECT_NAME);
                setProjectType(QVCSConstants.QVCS_REMOTE_PROJECT_TYPE);
                setBranchName("");
                setAppendedPath("");
                synchronized (this) {
                    currentDirectoryManagers = new DirectoryManagerInterface[1];
                    currentDirectoryManagers[0] = rootDirectoryManager;
                    fireThingsChanged();
                }
            }
        } catch (Exception e) {
            warnProblem(e.getMessage());
            warnProblem("Failed to set current directory to: [" + path + "]");
            warnProblem(Utility.expandStackTraceToString(e));
        }
    }

    void setActiveServer(final ServerProperties serverProperties) {
        activeServerProperties = serverProperties;

        // Get the username password for this server.
        final UsernamePassword usernamePassword = getUsernamePassword(serverProperties.getServerName());

        final QWinFrame finalThis = this;

        // Set the server name.
        setServerName(serverProperties.getServerName());

        if (usernamePassword != null) {
            TransportProxyInterface existingTransportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);

            if (existingTransportProxy == null) {
                // The type of transport we'll use...
                final TransportProxyType transportType = serverProperties.getClientTransport();

                // The port we'll connect on...
                final int port = serverProperties.getClientPort();

                // Hash the password...
                final byte[] hashedPassword = Utility.getInstance().hashPassword(usernamePassword.password);

                // Put this on a separate thread since it could take some time.
                Runnable worker = () -> {
                    // And force the login to the transport...
                    TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(transportType, serverProperties, port,
                            usernamePassword.userName, hashedPassword, finalThis, finalThis);
                    QWinFrame.getQWinFrame().setTransactionInProgress(false);
                    if (transportProxy == null) {
                        // The login failed...
                        final String message = "Server is down, or not available at: " + serverProperties.getServerIPAddress() + ":" + port;
                        Runnable later = () -> {
                            // Let the user know that the password change worked.
                            JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), message, "Server Not Available", JOptionPane.PLAIN_MESSAGE);
                        };
                        SwingUtilities.invokeLater(later);
                    } else {
                        TransportProxyFactory.getInstance().requestProjectList(serverProperties);
                    }
                };

                setTransactionInProgress(true);

                // Put all this on a separate worker thread.
                new Thread(worker).start();
            }
        }
    }

    /**
     * Get the server properties of the 'active' (i.e. the one that is currently 'selected') server. The 'active' server is the server whose node is at the root of the
     * currently active node in the tree control.
     * @return the server properties of the 'active' server.
     */
    public ServerProperties getActiveServerProperties() {
        return activeServerProperties;
    }

    private void getDirectoryManagers(DirectoryManagerInterface directoryManager) {
        if (SwingUtilities.isEventDispatchThread()) {
            warnProblem("Wrong Thread for getDirectoryManagers!!");
        }

        DirectoryManagerInterface[] directoryManagers;
        if (recurseFlag && !(directoryManager instanceof DirectoryManagerForRoot)) {
            // Put the selected node as the first element in the array.
            List<DirectoryManagerInterface> directoryManagerList = new ArrayList<>();

            String projType = directoryManager.getArchiveDirManager().getProjectProperties().getProjectType();
            String server = ProjectTreeControl.getInstance().getActiveServerName();
            AbstractProjectProperties projectProperties = ProjectTreeControl.getInstance().getActiveProject();
            String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());

            DefaultMutableTreeNode selectedNode = projectTreeControl.getSelectedNode();
            if (selectedNode != null) {
                Enumeration enumerator = selectedNode.breadthFirstEnumeration();
                while (enumerator.hasMoreElements()) {
                    DirectoryManagerInterface dirManager = null;

                    DefaultMutableTreeNode currentNode = (DefaultMutableTreeNode) enumerator.nextElement();
                    if (currentNode instanceof BranchTreeNode) {
                        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), getAppendedPath());
                        dirManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), server, directoryCoordinate, projType,
                                projectProperties, workfileBase, null, false);
                    } else if (currentNode instanceof DirectoryTreeNode) {
                        DirectoryTreeNode directoryTreeNode = (DirectoryTreeNode) currentNode;
                        DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), directoryTreeNode.getAppendedPath());
                        dirManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(), server, directoryCoordinate,
                                projType, projectProperties, workfileBase + File.separator + directoryTreeNode.getAppendedPath(), null, false);
                    }

                    if (dirManager != null) {
                        directoryManagerList.add(dirManager);
                    }
                }
                directoryManagers = new DirectoryManagerInterface[directoryManagerList.size()];
                for (int i = 0; i < directoryManagers.length; i++) {
                    directoryManagers[i] = directoryManagerList.get(i);
                }
            } else {
                directoryManagers = new DirectoryManagerInterface[1];
                directoryManagers[0] = directoryManager;
            }
        } else {
            directoryManagers = new DirectoryManagerInterface[1];
            directoryManagers[0] = directoryManager;
        }

        synchronized (this) {
            currentDirectoryManagers = directoryManagers;
        }
    }

    // Get the password for the current server.
    private UsernamePassword getUsernamePassword(String server) {
        UsernamePassword usernamePassword = usernamePasswordMap.get(server);

        if (usernamePassword == null) {
            if (getUserProperties().getBypassLoginDialogFlag() && (0 == server.compareTo(getUserProperties().getBypassServerName()))) {
                usernamePassword = new UsernamePassword(getUserProperties().getBypassUserName(), getUserProperties().getBypassPassword());
                pendingLoginPasswordMap.put(server, usernamePassword);
            } else {
                // The user has not logged in to this server/project yet. Display a login dialog, and get the password.
                ServerLoginDialog loginDialog = new ServerLoginDialog(this, true, server);
                loginDialog.setVisible(true);

                if (loginDialog.getIsOK()) {
                    String username = loginDialog.getUserName();
                    String password = loginDialog.getPassword();
                    if ((password != null) && (username != null)) {
                        usernamePassword = new UsernamePassword(username, password);
                        pendingLoginPasswordMap.put(server, usernamePassword);
                    }
                }
            }
        }
        return usernamePassword;
    }

    void clearUsernamePassword(String server) {
        usernamePasswordMap.remove(server);
    }

    private void updateUserWorkfileDirectory(String path) {
        String projectWorkfileDirectory = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());
        if (projectWorkfileDirectory.length() > 0) {
            if (path.length() > 0) {
                setUserWorkfileDirectory(projectWorkfileDirectory + File.separator + path);
            } else {
                setUserWorkfileDirectory(projectWorkfileDirectory);
            }
        } else {
            // The user has not defined a workfile location yet.  Put up this dialog to encourage them to define their workfile location for this project.
            DefineWorkfileLocationDialog defineWorkfileLocationDialog = new DefineWorkfileLocationDialog(this);
            defineWorkfileLocationDialog.setVisible(true);
            if (defineWorkfileLocationDialog.getIsOK()) {
                getUserLocationProperties().setWorkfileLocation(getServerName(), getProjectName(), getBranchName(), defineWorkfileLocationDialog.getWorkfileLocation());
                getUserLocationProperties().saveProperties();

                setUserWorkfileDirectory(defineWorkfileLocationDialog.getWorkfileLocation());
                setRefreshRequired(true);
                setCurrentAppendedPath(getProjectName(), getBranchName(), getAppendedPath(), getProjectType(), false);
            } else {
                setUserWorkfileDirectory(" ");
            }
        }
    }

    private boolean getRefreshRequired() {
        return refreshRequiredFlag;
    }

    void setRefreshRequired(boolean flag) {
        this.refreshRequiredFlag = flag;

        if (flag) {
            // We need to update the workfile directory associated with the collection of directory managers.
            Collection directoryManagerCollection = DirectoryManagerFactory.getInstance().getDirectoryManagersForProject(getServerName(), getProjectName(), getBranchName());
            Iterator it = directoryManagerCollection.iterator();
            while (it.hasNext()) {
                DirectoryManager directoryManager = (DirectoryManager) it.next();
                String path = directoryManager.getAppendedPath();
                String fullWorkfilePath;
                if (path.length() > 0) {
                    fullWorkfilePath = getUserWorkfileDirectory() + File.separator + path;
                } else {
                    fullWorkfilePath = getUserWorkfileDirectory();
                }
                WorkfileDirectoryManager workfileDirManager = new WorkfileDirectoryManager(fullWorkfilePath, directoryManager.getArchiveDirManager(), directoryManager);
                directoryManager.setWorkfileDirectoryManager(workfileDirManager);

                // If recursion is turned off, we may have some directory managers in the collection that will not get merged by the refreshCurrenBranch() call, since that call only
                // iterates over the current directory manager collection, instead of iterating over the entire set of existing directory managers for this project.
                if (!getRecurseFlag()) {
                    try {
                        directoryManager.mergeManagers();
                    } catch (QVCSException e) {
                        warnProblem("Caught exception in setRefreshRequired: " + e.getClass().getName() + ": " + e.getLocalizedMessage());
                    }
                }
            }
            refreshCurrentBranch();
        }
    }

    void fireThingsChanged() {
        final ChangeEvent event = new ChangeEvent(this);

        // Install the thread tracking repaint manager.
        Runnable thingsChanged = () -> {
            // Guaranteed to return a non-null array
            Object[] listeners = changeListenerArray.getListenerList();

            // Process the listeners last to first, notifying those that are interested in this event
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChangeListener.class) {
                    ((ChangeListener) listeners[i + 1]).stateChanged(event);
                }
            }
            refreshCurrentBranch();
        };
        SwingUtilities.invokeLater(thingsChanged);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated
     * by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainToolBar = new javax.swing.JToolBar();
        getButton = new javax.swing.JButton();
        checkOutButton = new javax.swing.JButton();
        addFileButton = new javax.swing.JButton();
        checkInButton = new javax.swing.JButton();
        undoCheckoutButton = new javax.swing.JButton();
        lockButton = new javax.swing.JButton();
        labelButton = new javax.swing.JButton();
        fileGroupsButton = new javax.swing.JButton();
        compareButton = new javax.swing.JButton();
        recurseButton = new javax.swing.JButton();
        filterComboBox = new javax.swing.JComboBox();
        spacerLabel = new javax.swing.JLabel();
        filterActiveLabel = new javax.swing.JLabel();
        spacerPanel = new javax.swing.JPanel();
        verticalSplitPane = new javax.swing.JSplitPane();
        verticalSplitPane.setLeftComponent(projectTreePanel = new ProjectTreePanel());
        verticalSplitPane.setRightComponent(new RightParentPane());
        mainMenuBar = new javax.swing.JMenuBar();
        fileMainMenu = new javax.swing.JMenu();
        projectMainMenu = new javax.swing.JMenu();
        serverMainMenu = new javax.swing.JMenu();
        viewMainMenu = new javax.swing.JMenu();
        viewMenuRefresh = new javax.swing.JMenuItem();
        adminMainMenu = new javax.swing.JMenu();
        changePasswordMenuItem = new javax.swing.JMenuItem();
        setLogLevelMenu = new javax.swing.JMenu();
        logLevelALLRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        logLevelSevereRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        logLevelWarningRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        logLevelInfoRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        logLevelFineRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        logLevelFinerRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        userPreferencesMenuItem = new javax.swing.JMenuItem();
        defineFileGroupsMenuItem = new javax.swing.JMenuItem();
        reportMainMenu = new javax.swing.JMenu();
        generateReportMenuItem = new javax.swing.JMenuItem();
        filterMainMenu = new javax.swing.JMenu();
        maintainFileFiltersMenuItem = new javax.swing.JMenuItem();
        helpMainMenu = new javax.swing.JMenu();
        enterpriseDocumentationMenuItem = new javax.swing.JMenuItem();
        helpMenuSeparator1 = new javax.swing.JSeparator();
        helpMenuAbout = new javax.swing.JMenuItem();

        setTitle("QVCS Enterprise Client 3.1.4-SNAPSHOT"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        mainToolBar.setFloatable(false);

        getButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        getButton.setIcon(getButtonImage);
        getButton.setToolTipText("Get (CTRL-G)");
        getButton.setBorderPainted(false);
        getButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(getButton);

        checkOutButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        checkOutButton.setIcon(checkoutButtonImage);
        checkOutButton.setToolTipText("Check out (CTRL-K)");
        checkOutButton.setBorderPainted(false);
        checkOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkOutButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(checkOutButton);

        addFileButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        addFileButton.setIcon(addFileButtonImage);
        addFileButton.setToolTipText("Add to project (ALT-A)");
        addFileButton.setBorderPainted(false);
        addFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addFileButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(addFileButton);

        checkInButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        checkInButton.setIcon(checkInButtonImage);
        checkInButton.setToolTipText("Check In (CTRL-U)");
        checkInButton.setBorderPainted(false);
        checkInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkInButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(checkInButton);

        undoCheckoutButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        undoCheckoutButton.setIcon(undoCheckOutButtonImage);
        undoCheckoutButton.setToolTipText("Undo checkout (ALT-Z)");
        undoCheckoutButton.setBorderPainted(false);
        undoCheckoutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                undoCheckoutButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(undoCheckoutButton);

        lockButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        lockButton.setIcon(lockToolButtonImage);
        lockButton.setToolTipText("Lock (CTRL-L)");
        lockButton.setBorderPainted(false);
        lockButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lockButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(lockButton);

        labelButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        labelButton.setIcon(labelButtonImage);
        labelButton.setToolTipText("Label (F3)");
        labelButton.setBorderPainted(false);
        labelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                labelButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(labelButton);

        fileGroupsButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        fileGroupsButton.setIcon(fileGroupButtonImage);
        fileGroupsButton.setToolTipText("Define File Groups");
        fileGroupsButton.setBorderPainted(false);
        fileGroupsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileGroupsButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(fileGroupsButton);

        compareButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        compareButton.setIcon(compareButtonImage);
        compareButton.setToolTipText("Compare (F10)");
        compareButton.setBorderPainted(false);
        compareButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compareButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(compareButton);

        recurseButton.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        recurseButton.setIcon(noRecurseButtonImage);
        recurseButton.setToolTipText("Recurse (CTRL-R)");
        recurseButton.setBorderPainted(false);
        recurseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recurseButtonActionPerformed(evt);
            }
        });
        mainToolBar.add(recurseButton);

        filterComboBox.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterComboBox.setMaximumRowCount(12);
        filterComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterComboBoxActionPerformed(evt);
            }
        });
        mainToolBar.add(filterComboBox);

        spacerLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        spacerLabel.setText("   ");
        mainToolBar.add(spacerLabel);

        filterActiveLabel.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        filterActiveLabel.setForeground(new java.awt.Color(255, 0, 0));
        filterActiveLabel.setText("Filter Active");
        mainToolBar.add(filterActiveLabel);
        mainToolBar.add(spacerPanel);

        getContentPane().add(mainToolBar, java.awt.BorderLayout.NORTH);

        verticalSplitPane.setOneTouchExpandable(true);
        getContentPane().add(verticalSplitPane, java.awt.BorderLayout.CENTER);

        mainMenuBar.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        fileMainMenu.setMnemonic('F');
        fileMainMenu.setText("File");
        fileMainMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        mainMenuBar.add(fileMainMenu);

        projectMainMenu.setMnemonic('P');
        projectMainMenu.setText("Project");
        projectMainMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        mainMenuBar.add(projectMainMenu);

        serverMainMenu.setMnemonic('S');
        serverMainMenu.setText("Server");
        serverMainMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        mainMenuBar.add(serverMainMenu);

        viewMainMenu.setMnemonic('V');
        viewMainMenu.setText("View");
        viewMainMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        viewMenuRefresh.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F5, 0));
        viewMenuRefresh.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        viewMenuRefresh.setText("Refresh");
        viewMenuRefresh.setToolTipText("Refresh the current display");
        viewMenuRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                viewMenuRefreshActionPerformed(evt);
            }
        });
        viewMainMenu.add(viewMenuRefresh);

        mainMenuBar.add(viewMainMenu);

        adminMainMenu.setMnemonic('A');
        adminMainMenu.setText("Admin");
        adminMainMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        changePasswordMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        changePasswordMenuItem.setText("Change Password...");
        changePasswordMenuItem.setToolTipText("Change your password");
        changePasswordMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePasswordMenuItemActionPerformed(evt);
            }
        });
        adminMainMenu.add(changePasswordMenuItem);

        setLogLevelMenu.setText("Set Log Level");
        setLogLevelMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        logLevelButtonGroup.add(logLevelALLRadioButtonMenuItem);
        logLevelALLRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        logLevelALLRadioButtonMenuItem.setText("ALL");
        logLevelALLRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelChanged(evt);
            }
        });
        setLogLevelMenu.add(logLevelALLRadioButtonMenuItem);

        logLevelButtonGroup.add(logLevelSevereRadioButtonMenuItem);
        logLevelSevereRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        logLevelSevereRadioButtonMenuItem.setText("Error");
        logLevelSevereRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelChanged(evt);
            }
        });
        setLogLevelMenu.add(logLevelSevereRadioButtonMenuItem);

        logLevelButtonGroup.add(logLevelWarningRadioButtonMenuItem);
        logLevelWarningRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        logLevelWarningRadioButtonMenuItem.setText("Warn");
        logLevelWarningRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelChanged(evt);
            }
        });
        setLogLevelMenu.add(logLevelWarningRadioButtonMenuItem);

        logLevelButtonGroup.add(logLevelInfoRadioButtonMenuItem);
        logLevelInfoRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        logLevelInfoRadioButtonMenuItem.setText("Info");
        logLevelInfoRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelChanged(evt);
            }
        });
        setLogLevelMenu.add(logLevelInfoRadioButtonMenuItem);

        logLevelButtonGroup.add(logLevelFineRadioButtonMenuItem);
        logLevelFineRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        logLevelFineRadioButtonMenuItem.setText("Debug");
        logLevelFineRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelChanged(evt);
            }
        });
        setLogLevelMenu.add(logLevelFineRadioButtonMenuItem);

        logLevelButtonGroup.add(logLevelFinerRadioButtonMenuItem);
        logLevelFinerRadioButtonMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        logLevelFinerRadioButtonMenuItem.setText("Trace");
        logLevelFinerRadioButtonMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logLevelChanged(evt);
            }
        });
        setLogLevelMenu.add(logLevelFinerRadioButtonMenuItem);

        adminMainMenu.add(setLogLevelMenu);

        userPreferencesMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        userPreferencesMenuItem.setText("User Preferences...");
        userPreferencesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                userPreferencesMenuItemActionPerformed(evt);
            }
        });
        adminMainMenu.add(userPreferencesMenuItem);

        defineFileGroupsMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        defineFileGroupsMenuItem.setText("Define File Groups...");
        defineFileGroupsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                defineFileGroupsMenuItemActionPerformed(evt);
            }
        });
        adminMainMenu.add(defineFileGroupsMenuItem);

        mainMenuBar.add(adminMainMenu);

        reportMainMenu.setMnemonic('R');
        reportMainMenu.setText("Reports");
        reportMainMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        generateReportMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        generateReportMenuItem.setText("Generate Report");
        generateReportMenuItem.setToolTipText("Create .html report for revisions that match filter criteria");
        generateReportMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                generateReportMenuItemActionPerformed(evt);
            }
        });
        reportMainMenu.add(generateReportMenuItem);

        mainMenuBar.add(reportMainMenu);

        filterMainMenu.setMnemonic('T');
        filterMainMenu.setText("Filters");
        filterMainMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        maintainFileFiltersMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        maintainFileFiltersMenuItem.setText("Maintain Filters...");
        maintainFileFiltersMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                maintainFileFiltersMenuItemActionPerformed(evt);
            }
        });
        filterMainMenu.add(maintainFileFiltersMenuItem);

        mainMenuBar.add(filterMainMenu);

        helpMainMenu.setMnemonic('H');
        helpMainMenu.setText("Help");
        helpMainMenu.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N

        enterpriseDocumentationMenuItem.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        enterpriseDocumentationMenuItem.setText("QVCS Enterprise Documentation Web Page");
        enterpriseDocumentationMenuItem.setToolTipText("Launches your web browser to view QVCS-Enterprise documentation hosted on the server.");
        enterpriseDocumentationMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enterpriseDocumentationMenuItemActionPerformed(evt);
            }
        });
        helpMainMenu.add(enterpriseDocumentationMenuItem);
        helpMainMenu.add(helpMenuSeparator1);

        helpMenuAbout.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        helpMenuAbout.setText("About...");
        helpMenuAbout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                helpMenuAboutActionPerformed(evt);
            }
        });
        helpMainMenu.add(helpMenuAbout);

        mainMenuBar.add(helpMainMenu);

        setJMenuBar(mainMenuBar);
    }// </editor-fold>//GEN-END:initComponents

    private void fileGroupsButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_fileGroupsButtonActionPerformed
    {//GEN-HEADEREND:event_fileGroupsButtonActionPerformed
        defineFileGroupsMenuItemActionPerformed(evt);
    }//GEN-LAST:event_fileGroupsButtonActionPerformed

    private void defineFileGroupsMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_defineFileGroupsMenuItemActionPerformed
    {//GEN-HEADEREND:event_defineFileGroupsMenuItemActionPerformed
        DefineFileGroupsDialog defineFileGroupsDialog = new DefineFileGroupsDialog(this, true);
        defineFileGroupsDialog.setVisible(true);
    }//GEN-LAST:event_defineFileGroupsMenuItemActionPerformed

    private void enterpriseDocumentationMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_enterpriseDocumentationMenuItemActionPerformed
    {//GEN-HEADEREND:event_enterpriseDocumentationMenuItemActionPerformed
        if (getActiveServerProperties() != null) {
            String serverIP = getActiveServerProperties().getServerIPAddress();
            String serverWebSite = "http://" + serverIP + ":9080/docs/intro.html";
            Utility.openURL(serverWebSite);
        } else {
            // Show the error message on the Swing thread.
            Runnable later = () -> {
                // Let the user know that the password change worked.
                JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "You must login to a server before you can view QVCS-Enterprise documentation.",
                        "Show QVCS-Enterprise Documentation", JOptionPane.PLAIN_MESSAGE);
            };
            SwingUtilities.invokeLater(later);
        }
    }//GEN-LAST:event_enterpriseDocumentationMenuItemActionPerformed

    private void userPreferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_userPreferencesMenuItemActionPerformed
    {//GEN-HEADEREND:event_userPreferencesMenuItemActionPerformed
        UserPreferencesTabbedDialog userPreferencesDialog = new UserPreferencesTabbedDialog(this, true);
        userPreferencesDialog.setVisible(true);
    }//GEN-LAST:event_userPreferencesMenuItemActionPerformed

    private void generateReportMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_generateReportMenuItemActionPerformed
    {//GEN-HEADEREND:event_generateReportMenuItemActionPerformed
        generateReport();
    }//GEN-LAST:event_generateReportMenuItemActionPerformed

    private void maintainFileFiltersMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_maintainFileFiltersMenuItemActionPerformed
    {//GEN-HEADEREND:event_maintainFileFiltersMenuItemActionPerformed
        MaintainFileFiltersDialog maintainFileFiltersDialog = new MaintainFileFiltersDialog(this, true);
        maintainFileFiltersDialog.setVisible(true);
        if (maintainFileFiltersDialog.getIsOK()) {
            // Start with a fresh set of collections, and add the survivors from the dialog.
            FilterManager.getFilterManager().resetCollections();

            FilterCollection[] filterCollections = maintainFileFiltersDialog.getFilterCollections();
            for (FilterCollection filterCollection : filterCollections) {
                if (!filterCollection.getIsBuiltInCollection()) {
                    FilterManager.getFilterManager().addFilterCollection(filterCollection);
                }
            }
            // Save these changes away.
            FilterManager.getFilterManager().writeStore();
            filterComboBox.setModel(new FileFiltersComboModel(getProjectName()));
        }
    }//GEN-LAST:event_maintainFileFiltersMenuItemActionPerformed

    private void filterComboBoxActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_filterComboBoxActionPerformed
    {//GEN-HEADEREND:event_filterComboBoxActionPerformed
        filteredFileTableModel = (FilteredFileTableModel) getRightFilePane().getModel();
        FilterCollection filterCollection = (FilterCollection) filterComboBox.getModel().getSelectedItem();

        filteredFileTableModel.setFilterCollection(filterCollection);
        getUserProperties().setActiveFileFilterName(filterCollection.getCollectionName());
        filteredFileTableModel.setEnableFilters(true);
        if ((currentDirectoryManagers != null) && (ignoreFilterChangeFlag == false)) {
            // Put this on a separate thread since it could take some time.  We will put up a progress dialog.
            Runnable worker = () -> {
                DirectoryManagerInterface directoryManager;
                DirectoryManagerInterface[] directoryManagers;

                synchronized (QWinFrame.getQWinFrame()) {
                    directoryManager = currentDirectoryManagers[0];
                }
                getDirectoryManagers(directoryManager);
                synchronized (QWinFrame.getQWinFrame()) {
                    directoryManagers = currentDirectoryManagers;
                }
                filteredFileTableModel.setDirectoryManagers(directoryManagers, true, false);
            };
            // Put all this on a separate worker thread.
            new Thread(worker).start();
        }

        // We only ignore this once.  The basic problem here is that changing the project causes us to reset the filter collection combo box, and force a selection of the ALL
        // collection.... but the change also causes a subsequent call the the setDirectoryManager() method.
        ignoreFilterChangeFlag = false;
    }//GEN-LAST:event_filterComboBoxActionPerformed

    private void helpMenuAboutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_helpMenuAboutActionPerformed
    {//GEN-HEADEREND:event_helpMenuAboutActionPerformed
        AboutDialog aboutDialog = new AboutDialog(this, true);
        aboutDialog.setVisible(true);
    }//GEN-LAST:event_helpMenuAboutActionPerformed

    private void logLevelChanged(java.awt.event.ActionEvent evt)//GEN-FIRST:event_logLevelChanged
    {//GEN-HEADEREND:event_logLevelChanged
        // Save the selection in user properties.
        getUserProperties().setActivityPaneLogLevel(logLevelButtonGroup.getSelectedLevel().toString());

        // Let the handler filter know about the change.
        ActivityPaneLogFilter.getInstance().setLevel(logLevelButtonGroup.getSelectedLevel());
    }//GEN-LAST:event_logLevelChanged

    private void changePasswordMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_changePasswordMenuItemActionPerformed
    {//GEN-HEADEREND:event_changePasswordMenuItemActionPerformed
        if (getServerName().length() > 0) {
            if (getIsLoggedIn(getServerName())) {
                ChangeUserPasswordDialog changePasswordDialog = new ChangeUserPasswordDialog(this, true, getServerName(), getLoggedInUserName());
                changePasswordDialog.setVisible(true);
                if (changePasswordDialog.getIsOK()) {
                    operationChangePassword(changePasswordDialog.getServerName(), changePasswordDialog.getUserName(), changePasswordDialog.getOldPassword(),
                            changePasswordDialog.getNewPassword());
                }
            } else {
                // Show the error message on the Swing thread.
                Runnable later = () -> {
                    // Let the user know that the password change worked.
                    JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "You must login before you can change your password.", "Password Change Result",
                            JOptionPane.PLAIN_MESSAGE);
                };
                SwingUtilities.invokeLater(later);
            }
        } else {
            // Show the error message on the Swing thread.
            Runnable later = () -> {
                // Let the user know that the password change worked.
                JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "You must login to a server before you can change your password.", "Password Change Result",
                        JOptionPane.PLAIN_MESSAGE);
            };
            SwingUtilities.invokeLater(later);
        }
    }//GEN-LAST:event_changePasswordMenuItemActionPerformed

    private void recurseButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_recurseButtonActionPerformed
    {//GEN-HEADEREND:event_recurseButtonActionPerformed
        if (recurseFlag) {
            recurseButton.setIcon(noRecurseButtonImage);
            recurseFlag = false;
        } else {
            recurseButton.setIcon(recurseButtonImage);
            recurseFlag = true;
        }

        // Put the lengthy stuff on a separate thread.
        Runnable worker = () -> {
            try {
                DirectoryManagerInterface directoryManager;
                synchronized (QWinFrame.this) {
                    if (currentDirectoryManagers != null) {
                        directoryManager = currentDirectoryManagers[0];
                    } else {
                        directoryManager = null;
                    }
                }

                if (directoryManager != null) {
                    // Show busy
                    final int fTransactionID = ClientTransactionManager.getInstance().beginTransaction(getServerName());

                    getDirectoryManagers(directoryManager);
                    waitForDirectoryManagersToInit();

                    // Run the update on the Swing thread.
                    Runnable fireChange = () -> {
                        fireThingsChanged();
                        ClientTransactionManager.getInstance().endTransaction(getServerName(), fTransactionID);
                    };
                    SwingUtilities.invokeLater(fireChange);
                }
            } catch (Exception e) {
                warnProblem("Caught exception in recurse handler: " + e.getClass().getName() + ": " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }//GEN-LAST:event_recurseButtonActionPerformed

    private void viewMenuRefreshActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_viewMenuRefreshActionPerformed
    {//GEN-HEADEREND:event_viewMenuRefreshActionPerformed
        refreshCurrentBranch();
    }//GEN-LAST:event_viewMenuRefreshActionPerformed

    private void compareButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_compareButtonActionPerformed
    {//GEN-HEADEREND:event_compareButtonActionPerformed
        operationVisualCompare();
    }//GEN-LAST:event_compareButtonActionPerformed

    private void labelButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_labelButtonActionPerformed
    {//GEN-HEADEREND:event_labelButtonActionPerformed
        operationLabel();
    }//GEN-LAST:event_labelButtonActionPerformed

    private void undoCheckoutButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_undoCheckoutButtonActionPerformed
    {//GEN-HEADEREND:event_undoCheckoutButtonActionPerformed
        operationUndoCheckOut();
    }//GEN-LAST:event_undoCheckoutButtonActionPerformed

    private void addFileButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_addFileButtonActionPerformed
    {//GEN-HEADEREND:event_addFileButtonActionPerformed
        operationAdd();
    }//GEN-LAST:event_addFileButtonActionPerformed

    private void lockButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_lockButtonActionPerformed
    {//GEN-HEADEREND:event_lockButtonActionPerformed
        operationLock();
    }//GEN-LAST:event_lockButtonActionPerformed

    private void checkInButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkInButtonActionPerformed
    {//GEN-HEADEREND:event_checkInButtonActionPerformed
        operationCheckIn();
    }//GEN-LAST:event_checkInButtonActionPerformed

    private void checkOutButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_checkOutButtonActionPerformed
    {//GEN-HEADEREND:event_checkOutButtonActionPerformed
        operationCheckOut();
    }//GEN-LAST:event_checkOutButtonActionPerformed

    private void getButtonActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_getButtonActionPerformed
    {//GEN-HEADEREND:event_getButtonActionPerformed
        operationGet();
    }//GEN-LAST:event_getButtonActionPerformed

    /**
     * Get the checkin comment properties.
     * @return the checkin comment properties.
     */
    public CheckInCommentProperties getCheckinComments() {
        return checkInCommentProperties;
    }

    @Override
    public void exitTheApp() {
        exitForm(null);
    }

    /**
     * Exit the Application
     */
  private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm

      shutDown();
      System.exit(0);
  }//GEN-LAST:event_exitForm

    private void shutDown() {
        if (shutdownHouseKeepingCompletedFlag == false) {
            saveUserProperties();
            System.out.println("Saved user properties.");

            checkInCommentProperties.saveProperties();
            System.out.println("Saved checkin comments.");

            WorkfileDigestManager.getInstance().writeStore();
            System.out.println("Saved workfile digests.");

            LabelManager.getInstance().writeStore();
            System.out.println("Saved labels.");

            FilterManager.getFilterManager().writeStore();
            System.out.println("Saved file filters.");

            ViewUtilityManager.getInstance().writeStore();
            System.out.println("Saved view utility associations.");

            CheckOutCommentManager.getInstance().writeStore();
            System.out.println("Saved checkout comments.");

            FileGroupManager.getInstance().writeStore();
            System.out.println("Saved file group data.");

            shutdownHouseKeepingCompletedFlag = true;
        }
    }

    /**
     * This is the class that runs at exit time.
     */
    class ShutdownThread extends Thread {
        @Override
        public void run() {
            try {
                shutDown();
            } catch (Exception e) {
                warnProblem(e.getLocalizedMessage());
            }
        }
    }

    private void saveUserProperties() {
        // Save Frame size and position
        getUserProperties().setFrameWidth(getWidth());
        getUserProperties().setFrameHeight(getHeight());
        getUserProperties().setFrameXLocation(getX());
        getUserProperties().setFrameYLocation(getY());
        if ((getExtendedState() & MAXIMIZED_BOTH) == MAXIMIZED_BOTH) {
            getUserProperties().setFrameMaximizeFlag(true);
        } else {
            getUserProperties().setFrameMaximizeFlag(false);
        }

        // Save tree width and file list height.
        getUserProperties().setTreeWidth(getTreeControl().getWidth());
        getUserProperties().setFileListHeight(getRightFilePane().getHeight());

        // Save column widths
        int columnCount = getFileTable().getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            String columnName = getFileTable().getColumnName(i);
            TableColumn column = getFileTable().getColumn(columnName);
            int columnWidth = column.getWidth();
            getUserProperties().setColumnWidth(i, columnWidth);
        }
        // Write things to the user property file.
        getUserProperties().saveProperties();
    }

    @Override
    public void visualCompare(final String file1Name, final String file2Name, final String display1, final String display2) {
        if (getUserProperties().getUseExternalVisualCompareTool()) {
            externalVisualCompare(file1Name, file2Name, display1, display2);
        } else {
            // Run on the Swing thread.
            Runnable later = () -> {
                CompareFrame compareFrame = new CompareFrame(false, QWinFrame.getQWinFrame());
                compareFrame.setFirstFileActualName(file1Name);
                compareFrame.setFirstFileDisplayName(display1);
                compareFrame.setSecondFileActualName(file2Name);
                compareFrame.setSecondFileDisplayName(display2);
                compareFrame.compare();
            };
            SwingUtilities.invokeLater(later);
        }
    }

    void indicateProgress(boolean flag) {
        frameStatusBar.indicateProgress(flag);
        if (flag == false) {
            frameStatusBar.updateStatusInfo();
        }
    }

    synchronized void addChangeListener(ChangeListener l) {
        changeListenerArray.add(ChangeListener.class, l);
    }

    synchronized void removeChangeListener(ChangeListener l) {
        changeListenerArray.remove(ChangeListener.class, l);
    }

    void setApplicationHomeDirectory(String homeDirectory) {
        this.applicationHomeDirectory = homeDirectory;
    }

    String getApplicationHomeDirectory() {
        return applicationHomeDirectory;
    }

    /**
     * Get the user properties.
     * @return the user properties.
     */
    public UserProperties getUserProperties() {
        return userProperties;
    }

    /**
     * Get the user location properties.
     * @return the user location properties.
     */
    public UserLocationProperties getUserLocationProperties() {
        return userLocationProperties;
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return serverName;
    }

    void setServerName(String server) {
        serverName = server;
    }

    void setProjectName(String projectName) {
        if (!this.projectName.equals(projectName)) {
            this.projectName = projectName;
            if (!getIgnoreTreeChanges()) {
                getUserProperties().setMostRecentProjectName(projectName);
            }
            ignoreFilterChangeFlag = true;
            String previousFilterCollectionName = getUserProperties().getActiveFileFilterName();
            setFilterModel(new FileFiltersComboModel(getProjectName()), previousFilterCollectionName);
        }
    }

    /**
     * Set this flag so that we don't save changes to current project/appended path, etc., since we need to do this during startup when we are trying to restore the user's last directory selection.
     *
     * @param flag ignore or not.
     */
    void setIgnoreTreeChanges(boolean flag) {
        this.ignoreTreeChangesFlag = flag;
    }

    boolean getIgnoreTreeChanges() {
        return this.ignoreTreeChangesFlag;
    }

    private void setFilterModel(FileFiltersComboModel filterModel, String selectCollection) {
        filterComboBox.setModel(filterModel);
        FilterCollection filterCollection = null;
        int i;
        boolean indexFound = false;
        for (i = 0; i < filterModel.getSize(); i++) {
            filterCollection = filterModel.getElementAt(i);
            if (selectCollection.equals(filterCollection.getCollectionName())) {
                indexFound = true;
                break;
            }
        }
        if (indexFound) {
            filterComboBox.setSelectedIndex(i);
        } else {
            // We did not find the requested collection in the combo model; Default back to the 'all' collection.
            for (i = 0; i < filterModel.getSize(); i++) {
                filterCollection = filterModel.getElementAt(i);
                if (filterCollection.getCollectionName().equals(FilterManager.ALL_FILTER)) {
                    filterComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        filteredFileTableModel = (FilteredFileTableModel) getRightFilePane().getModel();
        filteredFileTableModel.setFilterCollection(filterCollection);
        filteredFileTableModel.setEnableFilters(true);
    }

    public void setFilterActive(boolean flag) {
        String filterActiveString;
        if (flag) {
            filterActiveString = "Filter Active";
        } else {
            filterActiveString = " ";
        }

        // Run this on the swing thread.
        final String finalFilterActiveString = filterActiveString;
        Runnable swingTask = () -> {
            filterActiveLabel.setText(finalFilterActiveString);
        };
        SwingUtilities.invokeLater(swingTask);
    }

    /**
     * Get the project name. This is the 'active' project -- i.e. the one whose 'tree' the user is currently navigated.
     * @return the project name.
     */
    public String getProjectName() {
        String projName = getTreeControl().getProjectName();
        if (projName == null) {
            projName = "";
        }
        return projName;
    }

    /**
     * Get the view name. The is the 'active' view.
     * @return the 'active' view.
     */
    public String getBranchName() {
        return branchName;
    }

    void setBranchName(final String view) {
        this.branchName = view;
    }

    String getAppendedPath() {
        return appendedPath;
    }

    boolean getRecurseFlag() {
        return recurseFlag;
    }

    /**
     * Set the directory recursion flag.
     * @param flag the directory recursion flag; true to enable recursion; false to disable recursion (the default).
     */
    public void setRecurseFlag(boolean flag) {
        if (flag != recurseFlag) {
            recurseButtonActionPerformed(null);
        }
    }

    void setAppendedPath(String path) {
        this.appendedPath = path;
        if (!getIgnoreTreeChanges()) {
            getUserProperties().setMostRecentAppendedPath(path);
        }
    }

    String getProjectType() {
        return projectType;
    }

    void setProjectType(String prjType) {
        this.projectType = prjType;
        if (!getIgnoreTreeChanges()) {
            getUserProperties().setMostRecentProjectType(prjType);
        }
    }

    void setLoggedInUserName(String userName) {
        if (userName.compareTo(loggedInUserName) != 0) {
            this.loggedInUserName = userName;

            if (userLocationProperties != null) {
                userLocationProperties.saveProperties();
            }
            userLocationProperties = new UserLocationProperties(getApplicationHomeDirectory(), loggedInUserName);
        }
    }

    /**
     * Get the user's login name... i.e. their QVCS user id.
     * @return the user's login name.
     */
    public String getLoggedInUserName() {
        return loggedInUserName;
    }

    void setUserWorkfileDirectory(String workfileDirectory) {
        userWorkfileDirectory = workfileDirectory;
    }

    String getUserWorkfileDirectory() {
        return userWorkfileDirectory;
    }

    void setFileTable(JTable fileTable) {
        this.fileTable = fileTable;
    }

    /**
     * Get the file list JTable.
     * @return the file list JTable.
     */
    public JTable getFileTable() {
        return fileTable;
    }

    public RightFilePane getRightFilePane() {
        return rightFilePane;
    }

    void setRightFilePane(RightFilePane rightFilePane) {
        this.rightFilePane = rightFilePane;
    }

    void setTreeModel(ProjectTreeModel treeModel) {
        this.projectTreeModel = treeModel;
    }

    /**
     * Get the model for the tree control.
     * @return the model for the tree control.
     */
    public ProjectTreeModel getTreeModel() {
        return projectTreeModel;
    }

    void setTreeControl(ProjectTreeControl treeControl) {
        this.projectTreeControl = treeControl;
    }

    /**
     * Get the tree control.
     * @return the tree control.
     */
    public ProjectTreeControl getTreeControl() {
        return projectTreeControl;
    }

    ProjectTreePanel getProjectTreePanel() {
        return projectTreePanel;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addFileButton;
    private javax.swing.JMenu adminMainMenu;
    private javax.swing.JMenuItem changePasswordMenuItem;
    private javax.swing.JButton checkInButton;
    private javax.swing.JButton checkOutButton;
    private javax.swing.JButton compareButton;
    private javax.swing.JMenuItem defineFileGroupsMenuItem;
    private javax.swing.JMenuItem enterpriseDocumentationMenuItem;
    private javax.swing.JButton fileGroupsButton;
    private javax.swing.JMenu fileMainMenu;
    private javax.swing.JLabel filterActiveLabel;
    private javax.swing.JComboBox filterComboBox;
    private javax.swing.JMenu filterMainMenu;
    private javax.swing.JMenuItem generateReportMenuItem;
    private javax.swing.JButton getButton;
    private javax.swing.JMenu helpMainMenu;
    private javax.swing.JMenuItem helpMenuAbout;
    private javax.swing.JSeparator helpMenuSeparator1;
    private javax.swing.JButton labelButton;
    private javax.swing.JButton lockButton;
    private javax.swing.JRadioButtonMenuItem logLevelALLRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem logLevelFineRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem logLevelFinerRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem logLevelInfoRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem logLevelSevereRadioButtonMenuItem;
    private javax.swing.JRadioButtonMenuItem logLevelWarningRadioButtonMenuItem;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JToolBar mainToolBar;
    private javax.swing.JMenuItem maintainFileFiltersMenuItem;
    private javax.swing.JMenu projectMainMenu;
    private javax.swing.JButton recurseButton;
    private javax.swing.JMenu reportMainMenu;
    private javax.swing.JMenu serverMainMenu;
    private javax.swing.JMenu setLogLevelMenu;
    private javax.swing.JLabel spacerLabel;
    private javax.swing.JPanel spacerPanel;
    private javax.swing.JButton undoCheckoutButton;
    private javax.swing.JMenuItem userPreferencesMenuItem;
    private javax.swing.JSplitPane verticalSplitPane;
    private javax.swing.JMenu viewMainMenu;
    private javax.swing.JMenuItem viewMenuRefresh;
    // End of variables declaration//GEN-END:variables

    /**
     * Refresh the current branch.
     */
    public synchronized void refreshCurrentBranch() {
        // Cancel pending refresh
        if (refreshTask != null) {
            refreshTask.cancel();
        }

        // Put the refresh on the Timer thread.  We only want a single refresh to be going on at the same time.  Putting all refresh activities on the timer thread guarantees
        // that only a single refresh is active at any one time.
        refreshTask = new TimerTask() {

            @Override
            public void run() {
                try {
                    DirectoryManagerInterface[] directoryManagers = getCurrentDirectoryManagers();

                    // Call getDirectoryManagers to refresh the set of directoryManagers We do this so that if the recurse button is on, navigating to a different project will
                    // display all the files of that project as expected.  If we didn't do this, and this was the first time we navigated to the project, we wouldn't have the
                    // complete set of sub-directories, and as a result would not display all the files of the selected directory hierarchy.  By refreshing the directory list, we
                    // will actually succeed in displaying all the sub-directory files.
                    if (directoryManagers != null) {
                        synchronized (QWinFrame.getQWinFrame()) {
                            getDirectoryManagers(directoryManagers[0]);
                            directoryManagers = currentDirectoryManagers;
                        }
                        for (DirectoryManagerInterface directoryManager : directoryManagers) {
                            directoryManager.getWorkfileDirectoryManager().refresh();
                            try {
                                directoryManager.mergeManagers();
                            } catch (QVCSException e) {
                                warnProblem("Exception on merging on refresh: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                            }
                        }
                        AbstractFileTableModel dataModel = (AbstractFileTableModel) getFileTable().getModel();
                        dataModel.setDirectoryManagers(directoryManagers, false, false);
                    }
                } catch (Exception e) {
                    warnProblem("Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
                    warnProblem(Utility.expandStackTraceToString(e));
                }
            }
        };
        TimerManager.getInstance().getTimer().schedule(refreshTask, REFRESH_DELAY);
    }

    RightDetailPane getRightDetailPane() {
        return rightDetailPane;
    }

    void setRightDetailPane(RightDetailPane rightDetailPane) {
        this.rightDetailPane = rightDetailPane;
    }

    RightParentPane getRightParentPane() {
        return rightParentPane;
    }

    void setRightParentPane(RightParentPane rightParentPane) {
        this.rightParentPane = rightParentPane;
    }

    LoggingPane getRevisionInfoPane() {
        return revisionInfoPane;
    }

    void setRevisionInfoPane(LoggingPane revisionInfoPane) {
        this.revisionInfoPane = revisionInfoPane;
    }

    LoggingPane getLabelInfoPane() {
        return labelInfoPane;
    }

    void setLabelInfoPane(LoggingPane labelInfoPane) {
        this.labelInfoPane = labelInfoPane;
    }

    ActivityPane getActivityPane() {
        return activityPane;
    }

    void setActivityPane(ActivityPane activityPane) {
        this.activityPane = activityPane;
    }

    RevAndLabelInfoDetailPane getRevAndLabelInfoPane() {
        return revAndLabelInfoPane;
    }

    void setRevAndLabelInfoPane(RevAndLabelInfoDetailPane revAndLabelInfoPane) {
        this.revAndLabelInfoPane = revAndLabelInfoPane;
    }

    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    private void operationGet() {
        OperationBaseClass getOperation = new OperationGet(getFileTable(), getServerName(), getProjectName(), getBranchName(), getUserLocationProperties(), false);
        getOperation.executeOperation();
    }

    private void operationCheckOut() {
        OperationBaseClass checkOutOperation = new OperationCheckOutArchive(getFileTable(), getServerName(), getProjectName(), getBranchName(), getUserLocationProperties(), false);
        checkOutOperation.executeOperation();
    }

    private void operationLock() {
        OperationBaseClass lockOperation = new OperationLockArchive(getFileTable(), getServerName(), getProjectName(), getBranchName(), getUserLocationProperties(), false);
        lockOperation.executeOperation();
    }

    /**
     * Perform a visual compare.
     */
    public void operationVisualCompare() {
        OperationBaseClass visualCompareOperation = new OperationVisualCompare(getFileTable(), getServerName(), getProjectName(), getBranchName(), getUserLocationProperties());
        visualCompareOperation.executeOperation();
    }

    private void operationCheckIn() {
        OperationBaseClass checkInOperation = new OperationCheckInArchive(getFileTable(), getServerName(), getProjectName(), getBranchName(), getUserLocationProperties());
        checkInOperation.executeOperation();
    }

    private void operationAdd() {
        OperationBaseClass addOperation = new OperationCreateArchive(getFileTable(), getServerName(), getProjectName(), getBranchName(), getUserLocationProperties(), false);
        addOperation.executeOperation();
    }

    private void operationLabel() {
        OperationBaseClass labelOperation = new OperationLabelArchive(getFileTable(), getServerName(), getProjectName(), getBranchName(), getUserLocationProperties());
        labelOperation.executeOperation();
    }

    private void operationUndoCheckOut() {
        OperationBaseClass undoCheckOutOperation = new OperationUndoCheckOut(getFileTable(), getServerName(), getProjectName(), getBranchName(), getUserLocationProperties(), false);
        undoCheckOutOperation.executeOperation();
    }

    private void operationChangePassword(String serverName, String userName, String oldPassword, String newPassword) {
        savePendingPassword(serverName, newPassword);
        OperationBaseClass changePasswordOperation = new OperationChangePassword(serverName, userName, oldPassword, newPassword);
        changePasswordOperation.executeOperation();
    }

    private void updateDirectoryManagerPassword(String serverName, UsernamePassword usernamePassword) {
        DirectoryManagerFactory.getInstance().setServerUsername(serverName, usernamePassword.userName);
        DirectoryManagerFactory.getInstance().setServerPassword(serverName, usernamePassword.password);
    }

    @Override
    public void notifyPasswordChange(final ServerResponseChangePassword response) {
        if (response.getSuccess()) {
            // Update the password associated with the given server.
            UsernamePassword usernamePassword = getUsernamePassword(response.getServerName());
            if (usernamePassword == null) {
                // This should never happen... but if it does...
                throw new QVCSRuntimeException("Password change failed because existing username/password not found.");
            }
            usernamePassword.password = getPendingPassword(response.getServerName());
            updateDirectoryManagerPassword(response.getServerName(), usernamePassword);

            // If they are bypassing the login dialog, we need to save the new password.
            if (getUserProperties().getBypassLoginDialogFlag()) {
                getUserProperties().setBypassServerName(response.getServerName());
                getUserProperties().setBypassPassword(usernamePassword.password);
            }

            // Run the update on the Swing thread.
            Runnable later = () -> {
                // Let the user know that the password change worked.
                JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Password change successful", "Password Change Result", JOptionPane.PLAIN_MESSAGE);
            };
            SwingUtilities.invokeLater(later);
        } else {
            // Run the update on the Swing thread.
            Runnable later = () -> {
                // Let the user know that the password change failed.
                JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Password change failed." + response.getResult(), "Password Change Result", JOptionPane.PLAIN_MESSAGE);
            };
            SwingUtilities.invokeLater(later);
        }
    }

    @Override
    public String getPendingPassword(String serverName) {
        return pendingPasswordMap.get(serverName);
    }

    @Override
    public void savePendingPassword(String serverName, String password) {
        pendingPasswordMap.put(serverName, password);
    }

    @Override
    public void notifyLoginResult(final com.qumasoft.qvcslib.response.ServerResponseLogin response) {
        if (response.getLoginResult()) {
            // The password was a good one.  Save it in the right place.
            UsernamePassword usernamePassword = pendingLoginPasswordMap.get(response.getServerName());
            usernamePasswordMap.put(response.getServerName(), usernamePassword);
            setLoggedInUserName(response.getUserName());
            getStatusBar().updateStatusInfo();

            if (!response.getVersionsMatchFlag()) {
                // Run the update on the Swing thread.
                Runnable later = () -> {
                    // Let the user know that the client is out of date.
                    int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Login to server: [" + response.getServerName()
                            + "] succeeded. However, your client is out of date.  Did you want to update your client?",
                            "Client out of date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (answer == JOptionPane.OK_OPTION) {
                        UpdateManager.updateClient(QVCSConstants.QVCS_RELEASE_VERSION, "gui_out.jar", getActiveServerProperties(), false);
                    } else {
                        shutDown();
                        System.exit(0);
                    }
                };
                SwingUtilities.invokeLater(later);
            }
        } else {
            // Run the update on the Swing thread.
            Runnable later = () -> {
                // Let the user know that the login failed.
                JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "Login to server: [" + response.getServerName() + "] failed. " + response.getFailureReason(),
                        "Login Failure", JOptionPane.INFORMATION_MESSAGE);
                getTreeControl().selectRootNode();
            };
            SwingUtilities.invokeLater(later);
        }
    }

    @Override
    public void notifyUpdateComplete() {
        // Run the update on the Swing thread.
        Runnable later = () -> {
            // Time to exit the application.
            JOptionPane.showMessageDialog(null, "Updates received.  Please restart the application.", "Updates Complete", JOptionPane.PLAIN_MESSAGE);
            exitForm(null);
        };
        SwingUtilities.invokeLater(later);
    }

    private boolean getIsLoggedIn(String serverName) {
        boolean retVal = false;
        UsernamePassword usernamePassword = getUsernamePassword(serverName);
        if (usernamePassword != null) {
            retVal = true;
        }
        return retVal;
    }

    private void generateReport() {
        ReportGenerator.getReportGenerator().generateReport();
    }

    @Override
    public void notifyTransportProxyListener(final ServerResponseInterface messageIn) {
        if (messageIn instanceof ServerResponseMessage) {
            final ServerResponseMessage message = (ServerResponseMessage) messageIn;

            // Run the update on the Swing thread.
            Runnable later = () -> {
                if (message.getPriority().equals(ServerResponseMessage.HIGH_PRIORITY)) {
                    JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), message.getMessage(), "Server Message", JOptionPane.INFORMATION_MESSAGE);
                }
            };
            SwingUtilities.invokeLater(later);
            logProblem(message.getMessage());
        } else if (messageIn instanceof ServerResponseSuccess) {
            final ServerResponseSuccess message = (ServerResponseSuccess) messageIn;
            logProblem(message.getMessage());
        }
    }

    @Override
    public void setTransactionInProgress(final boolean flag) {
        if (flag == true) {
            // A lengthy transaction is beginning.  Turn on the busy cursor. Run on the Swing thread.
            Runnable later = () -> {
                originalGlassPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                originalGlassPane.setVisible(true);
            };
            SwingUtilities.invokeLater(later);
        } else {
            // The transaction is complete.  We need to turn off the busy indicator, and refresh the display. Run this on the Swing thread.
            Runnable later = () -> {
                originalGlassPane.setVisible(false);
                originalGlassPane.setCursor(null);
                refreshCurrentBranch();
            };
            SwingUtilities.invokeLater(later);
        }
    }

    public void setAutoUpdateFlag(boolean flag) {
        if (flag) {
            if (autoRefreshTimerTask != null) {
                autoRefreshTimerTask.cancel();
            }

            // Put the auto refresh on the Timer thread.
            autoRefreshTimerTask = new TimerTask() {
                @Override
                public void run() {
                    try {
                        logProblem("Auto-Refresh");
                        refreshCurrentBranch();
                    } catch (Exception e) {
                        warnProblem("Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
                        warnProblem(Utility.expandStackTraceToString(e));
                    }
                }
            };
            long updateInterval = 60L * 1000L * getUserProperties().getAutoUpdateInterval();
            TimerManager.getInstance().getTimer().schedule(autoRefreshTimerTask, 0L, updateInterval);
        } else {
            if (autoRefreshTimerTask != null) {
                autoRefreshTimerTask.cancel();
            }
            autoRefreshTimerTask = null;
        }
    }

    static class UsernamePassword {
        private String userName;
        private String password;

        UsernamePassword(String username, String password) {
            this.userName = username;
            this.password = password;
        }
    }

    class ActionRecurse extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionRecurse() {
            super("Recurse");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            recurseButtonActionPerformed(e);
        }
    }

    class ActionCompare extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionCompare() {
            super("Compare");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            compareButtonActionPerformed(e);
        }
    }

    class ActionRenameFile extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionRenameFile() {
            super("Rename File");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            OperationBaseClass renameFileOperation = new OperationRenameFile(fileTable, QWinFrame.getQWinFrame().getServerName(), QWinFrame.getQWinFrame().getProjectName(),
                    QWinFrame.getQWinFrame().getBranchName(), QWinFrame.getQWinFrame().getUserLocationProperties());
            renameFileOperation.executeOperation();
        }
    }

    class ActionLabel extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionLabel() {
            super("Label");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            labelButtonActionPerformed(e);
        }
    }

    class ActionFileGroup extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionFileGroup() {
            super("File Groups");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            defineFileGroupsMenuItemActionPerformed(e);
        }
    }

    class ActionLock extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionLock() {
            super("Lock");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            lockButtonActionPerformed(e);
        }
    }

    class ActionUnLock extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionUnLock() {
            super("UnLock");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            undoCheckoutButtonActionPerformed(e);
        }
    }

    class ActionCheckIn extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionCheckIn() {
            super("CheckIn");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            checkInButtonActionPerformed(e);
        }
    }

    class ActionAdd extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionAdd() {
            super("Add");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            addFileButtonActionPerformed(e);
        }
    }

    class ActionCheckOut extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionCheckOut() {
            super("CheckOut");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            checkOutButtonActionPerformed(e);
        }
    }

    class ActionGet extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionGet() {
            super("Get");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            getButtonActionPerformed(e);
        }
    }

    class ActionExit extends AbstractAction {
        private static final long serialVersionUID = 10L;
        ActionExit() {
            super("Exit");
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            exitForm(null);
        }
    }

    class OurFileMenuListener extends MenuListenerAdapter {
        @Override
        public void menuSelected(javax.swing.event.MenuEvent e) {
            getRightFilePane().enableMenuItems();
        }
    }

    class OurProjectMenuListener extends MenuListenerAdapter {
        @Override
        public void menuSelected(javax.swing.event.MenuEvent e) {
            getTreeControl().addProjectMenuItems(projectMainMenu);
        }
    }

    class OurServerMenuListener extends MenuListenerAdapter {
        @Override
        public void menuSelected(javax.swing.event.MenuEvent e) {
            getTreeControl().addServerMenuItems(serverMainMenu);
        }
    }
}
