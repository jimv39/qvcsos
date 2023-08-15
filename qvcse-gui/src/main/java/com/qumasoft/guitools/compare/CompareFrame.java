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
package com.qumasoft.guitools.compare;

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;
import com.qumasoft.qvcslib.RemotePropertiesManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.commons.jrcs.diff.Delta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Compare frame. Show file compare results in a frame window.
 * @author Jim Voris
 */
public final class CompareFrame extends javax.swing.JFrame {
    private static final long serialVersionUID = 411936148168031639L;

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareFrame.class);
    private static final int DEFAULT_FONT_SIZE = 12;
    private static final int DEFAULT_X_COORDINATE = 50;
    private static final int DEFAULT_Y_COORDINATE = 50;
    private FileContentsListModel file1ContentsListModel;
    private FileContentsListModel file2ContentsListModel;
    private FileContentsList file1ContentsList;
    private FileContentsList file2ContentsList;
    private ChangeMarkerPanel file1ChangeMarkerPanel;
    private ChangeMarkerPanel file2ChangeMarkerPanel;
    private int rowHeight;
    private boolean exitOnCloseFlag = true;
    private CompareFilesForGUI compareFilesForGUI;
    // Used to keep track of which difference is the current one.
    private int currentDifferenceIndex = -1;
    private final BevelBorder bevelBorder = new BevelBorder(BevelBorder.LOWERED);
    private final transient OurComponentListener ourComponentListener = new OurComponentListener();
    private final transient OurViewportChangeListener viewportChangeListener = new OurViewportChangeListener();

    private final EditCopyAction editCopyAction = new EditCopyAction();
    private final MoveToNextDifferenceAction moveToNextDifferenceAction = new MoveToNextDifferenceAction();
    private final MoveToPreviousDifferenceAction moveToPreviousDifferenceAction = new MoveToPreviousDifferenceAction();
    private final FindStringAction findStringAction = new FindStringAction();
    private final PropertiesAction propertiesAction = new PropertiesAction();
    private final ReCompareAction reCompareAction = new ReCompareAction();
    private final EscapeAction escapeAction = new EscapeAction();

    private final OurToolBar toolBar = new OurToolBar();
    private final Color blackColor = new Color(0, 0, 0);

    private static final ImageIcon FRAME_ICON = new ImageIcon(ClassLoader.getSystemResource("images/qwin16.png"), "Quma Software, Inc.");
    private static final ImageIcon RECOMPARE_ACTION_ICON = new ImageIcon(ClassLoader.getSystemResource("images/recompare.png"), "");
    private static final ImageIcon PROPERTIES_ACTION_ICON = new ImageIcon(ClassLoader.getSystemResource("images/properties.png"), "Define Properties");
    private static final ImageIcon FIND_STRING_ACTION_ICON = new ImageIcon(ClassLoader.getSystemResource("images/search.png"), "Find string");
    private static final ImageIcon MOVE_TO_PREVIOUS_DIFFERENCE_ICON = new ImageIcon(ClassLoader.getSystemResource("images/TriUp.png"), "Move to previous difference (SHIFT-F2)");
    private static final ImageIcon MOVE_TO_NEXT_DIFFERENCE_ICON = new ImageIcon(ClassLoader.getSystemResource("images/TriDown.png"), "Move to next difference (F2)");
    private static final ImageIcon COPY_ICON = new ImageIcon(ClassLoader.getSystemResource("images/copy.png"), "Copy selection to clipboard (CTRL-C)");

    private JViewport leftScrollPaneViewPort;
    private JViewport rightScrollPaneViewPort;
    private int verticalLinesInViewPort;
    private boolean ignoreAllWhiteSpaceFlag = false;
    private boolean ignoreLeadingWhiteSpaceFlag = false;
    private boolean ignoreCaseFlag = false;
    private boolean ignoreEOLChangesFlag = false;
    private final String[] statusBarStrings = {
        "  Deleted Lines  ",
        "  Changed Lines  ",
        "  Inserted Lines  "
    };
    // Used by addNotify
    private boolean frameSizeAdjustedFlag = false;
    private final javax.swing.JPanel leftParentPanel = new javax.swing.JPanel(new BorderLayout(5, 5), true);
    private final javax.swing.JPanel rightParentPanel = new javax.swing.JPanel(new BorderLayout(5, 5), true);
    private final javax.swing.JLabel firstFileDisplayName = new javax.swing.JLabel();
    private final javax.swing.JLabel secondFileDisplayName = new javax.swing.JLabel();
    private final javax.swing.JPanel leftPanel = new javax.swing.JPanel(new GridLayout(1, 1), true);
    private final javax.swing.JPanel rightPanel = new javax.swing.JPanel(new GridLayout(1, 1), true);
    private final javax.swing.JScrollPane leftScrollPane = new javax.swing.JScrollPane(leftPanel);
    private final javax.swing.JScrollPane rightScrollPane = new javax.swing.JScrollPane(rightPanel);
    private final javax.swing.JSplitPane splitPane = new javax.swing.JSplitPane(javax.swing.JSplitPane.HORIZONTAL_SPLIT, leftParentPanel, rightParentPanel);
    private final StatusBar statusBar = new StatusBar(statusBarStrings);
    private SearchDialog searchDialog;
    private ComparePropertiesDialog comparePropertiesDialog;
    private QWinFrame parentFrame;
    private RemotePropertiesBaseClass remoteProperties;
    private String firstFileActualName;
    private String secondFileActualName;

    /**
     * Create a compare frame.
     * @param qWinFrame the QWinFrame parent frame.
     */
    private CompareFrame(QWinFrame qWinFrame) {
        setTitle("QVCS Enterprise Visual Compare Utility");
        getContentPane().setLayout(new BorderLayout(0, 0));
        Font font = new Font("SansSerif", Font.PLAIN, DEFAULT_FONT_SIZE);
        getContentPane().setFont(font);
        // <editor-fold>
        setSize(405, 305);
        setVisible(false);

        splitPane.setBounds(0, 0, 405, 305);
        splitPane.setContinuousLayout(true);
        splitPane.setPreferredSize(new Dimension(400, 100));
        // </editor-fold>
        getContentPane().add(splitPane, BorderLayout.CENTER);

        statusBar.setPaneColor(0, FileContentsList.getDeleteColor());
        statusBar.setPaneColor(1, FileContentsList.getReplaceColor());
        statusBar.setPaneColor(2, FileContentsList.getInsertColor());
        getContentPane().add(statusBar, BorderLayout.SOUTH);
        getContentPane().add(toolBar, BorderLayout.NORTH);

        SymWindow aSymWindow = new SymWindow();
        this.addWindowListener(aSymWindow);

        addComponentListener(ourComponentListener);

        // Disable the 'previous' action
        moveToPreviousDifferenceAction.setEnabled(false);

        // Set the frame icon to the Quma standard icon.
        this.setIconImage(FRAME_ICON.getImage());

        // Load our property settings.
        TransportProxyInterface proxy = TransportProxyFactory.getInstance().getTransportProxy(qWinFrame.getActiveServerProperties());
        remoteProperties = RemotePropertiesManager.getInstance().getRemoteProperties(System.getProperty("user.name"), proxy);

        // Init our white space flags.
        ignoreAllWhiteSpaceFlag = remoteProperties.getIgnoreAllWhitespace("", "");
        ignoreLeadingWhiteSpaceFlag = remoteProperties.getIgnoreLeadingWhitespace("", "");
        ignoreCaseFlag = remoteProperties.getIgnoreCase("", "");
        ignoreEOLChangesFlag = remoteProperties.getIgnoreEOLChanges("", "");

        // Set up 'accelerator' keys
        javax.swing.KeyStroke keyNext = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyNext, "nextDifferenceKeyAction");
        getRootPane().getActionMap().put("nextDifferenceKeyAction", moveToNextDifferenceAction);

        javax.swing.KeyStroke keyPrevious = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, java.awt.event.InputEvent.SHIFT_DOWN_MASK);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyPrevious, "previousDifferenceKeyAction");
        getRootPane().getActionMap().put("previousDifferenceKeyAction", moveToPreviousDifferenceAction);

        javax.swing.KeyStroke keyFind = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyFind, "FindKeyAction");
        getRootPane().getActionMap().put("FindKeyAction", findStringAction);

        javax.swing.KeyStroke keyEscape = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0);
        getRootPane().getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(keyEscape, "EscapeKeyAction");
        getRootPane().getActionMap().put("EscapeKeyAction", escapeAction);
    }

    /**
     * Create a compare frame.
     * @param flag exit on close flag.
     * @param pFrame the parent frame.
     */
    public CompareFrame(boolean flag, QWinFrame pFrame) {
        this(pFrame);
        exitOnCloseFlag = flag;
        this.parentFrame = pFrame;
    }

    @Override
    public void setVisible(boolean b) {
        if (b) {
            setLocation(DEFAULT_X_COORDINATE, DEFAULT_Y_COORDINATE);
        }
        super.setVisible(b);
    }

    /**
     * Compare two files.
     */
    public void compare() {
        fitToScreen();
        currentDifferenceIndex = -1;

        // Compare the files
        // <editor-fole>
        String[] compareArgs = new String[3];
        compareArgs[0] = getFirstFileActualName();
        compareArgs[1] = getSecondFileActualName();
        compareArgs[2] = "junk";
        // </editor-fold>
        compareFilesForGUI = new CompareFilesForGUI(compareArgs);
        compareFilesForGUI.setIgnoreAllWhiteSpace(ignoreAllWhiteSpaceFlag);
        compareFilesForGUI.setIgnoreLeadingWhiteSpace(ignoreLeadingWhiteSpaceFlag);
        compareFilesForGUI.setIgnoreCaseFlag(ignoreCaseFlag);
        compareFilesForGUI.setIgnoreEOLChangesFlag(ignoreEOLChangesFlag);
        try {
            Cursor currentCursor = getCursor();
            setCursor(new Cursor(Cursor.WAIT_CURSOR));
            compareFilesForGUI.execute();

            // Enable/disable the forward/backward actions.
            setNextPreviousActionStates(compareFilesForGUI.getNumberOfChanges());

            if (file1ContentsList != null) {
                leftPanel.remove(file1ContentsList);
            }

            if (file2ContentsList != null) {
                rightPanel.remove(file2ContentsList);
            }

            // Set up the model object for the list JList objects.
            file1ContentsListModel = new FileContentsListModel(getFirstFileActualName(), compareFilesForGUI, true, null);
            file2ContentsListModel = new FileContentsListModel(getSecondFileActualName(), compareFilesForGUI, false, file1ContentsListModel);
            addBlanksToShorterModel();

            file1ContentsList = new FileContentsList(file1ContentsListModel, this);
            leftPanel.add(file1ContentsList);
            rowHeight = file1ContentsList.getRowHeight();
            file1ChangeMarkerPanel = new ChangeMarkerPanel(file1ContentsListModel, rowHeight);

            file2ContentsList = new FileContentsList(file2ContentsListModel, this);
            rightPanel.add(file2ContentsList);
            file2ChangeMarkerPanel = new ChangeMarkerPanel(file2ContentsListModel, rowHeight);

            firstFileDisplayName.setForeground(blackColor);
            firstFileDisplayName.setFont(new java.awt.Font("Arial", 0, DEFAULT_FONT_SIZE));
            firstFileDisplayName.setBorder(bevelBorder);
            leftParentPanel.add(firstFileDisplayName, BorderLayout.NORTH);
            leftParentPanel.add(leftScrollPane, BorderLayout.CENTER);
            leftParentPanel.add(file1ChangeMarkerPanel, BorderLayout.EAST);

            secondFileDisplayName.setForeground(blackColor);
            secondFileDisplayName.setBorder(bevelBorder);
            secondFileDisplayName.setFont(new java.awt.Font("Arial", 0, DEFAULT_FONT_SIZE));
            rightParentPanel.add(secondFileDisplayName, BorderLayout.NORTH);
            rightParentPanel.add(rightScrollPane, BorderLayout.CENTER);
            rightParentPanel.add(file2ChangeMarkerPanel, BorderLayout.EAST);

            // Center the splitter bar
            centerSplitterDivider();

            // Hook the scroll panes up...
            hookScrollPanes();

            setCursor(currentCursor);
            if (!isVisible()) {
                setVisible(true);
            }
        } catch (QVCSOperationException e) {
            LOGGER.warn("Caught QVCSOperationException: [{}]", e.getMessage());
        }
    }

    @Override
    public void addNotify() {
        // Record the size of the window prior to calling parents addNotify.
        Dimension size = getSize();

        super.addNotify();

        if (frameSizeAdjustedFlag) {
            return;
        }
        frameSizeAdjustedFlag = true;

        // Adjust size of frame according to the insets and menu bar
        Insets insets = getInsets();
        javax.swing.JMenuBar menuBar = getRootPane().getJMenuBar();
        int menuBarHeight = 0;
        if (menuBar != null) {
            menuBarHeight = menuBar.getPreferredSize().height;
        }
        setSize(insets.left + insets.right + size.width, insets.top + insets.bottom + size.height + menuBarHeight);
    }

    void addBlanksToShorterModel() {
        FileContentsListModel modelToAddTo = null;
        int rowsToAdd = 0;
        if (file1ContentsListModel.size() > file2ContentsListModel.size()) {
            rowsToAdd = file1ContentsListModel.size() - file2ContentsListModel.size();
            modelToAddTo = file2ContentsListModel;
        } else if (file2ContentsListModel.size() > file1ContentsListModel.size()) {
            rowsToAdd = file2ContentsListModel.size() - file1ContentsListModel.size();
            modelToAddTo = file1ContentsListModel;
        }

        // Any extras at the end must be associated with the last change which is the first
        // change in the changes Vector.
        if ((compareFilesForGUI.getNumberOfChanges() > 0) && (modelToAddTo != null)) {
            Delta delta = compareFilesForGUI.getLastDelta();
            for (int i = 0; i < rowsToAdd; i++) {
                modelToAddTo.addBlankRow(delta);
            }
        }
    }

    /**
     * Set the display string for the first file.
     * @param filename the display string for the first file.
     */
    public void setFirstFileDisplayName(String filename) {
        firstFileDisplayName.setText("    " + filename);
    }

    /**
     * Get the actual name of the first file.
     * @return the actual name of the first file.
     */
    public String getFirstFileActualName() {
        return firstFileActualName;
    }

    /**
     * Set the actual name of the first file.
     * @param filename the actual name of the first file.
     */
    public void setFirstFileActualName(String filename) {
        firstFileActualName = filename;
    }

    /**
     * Set the name to be displayed for the 2nd file in the comparison.
     * @param filename the name to be displayed for the 2nd file in the comparison.
     */
    public void setSecondFileDisplayName(String filename) {
        secondFileDisplayName.setText("    " + filename);
    }

    /**
     * Get the name of the 2nd file in the comparison.
     * @return the name of the 2nd file in the comparison.
     */
    public String getSecondFileActualName() {
        return secondFileActualName;
    }

    /**
     * Set the name of the 2nd file in the comparison.
     * @param filename the name of the 2nd file in the comparison.
     */
    public void setSecondFileActualName(String filename) {
        secondFileActualName = filename;
    }

    void centerSplitterDivider() {
        Dimension currentSize = this.getContentPane().getSize();
        splitPane.setDividerLocation(currentSize.width / 2);
    }

    void fitToScreen() {
        if (parentFrame == null) {
            Toolkit screenToolkit = java.awt.Toolkit.getDefaultToolkit();
            Dimension screenSize = screenToolkit.getScreenSize();
            // <editor-fold>
            setLocation(0, 20);
            screenSize.setSize((screenSize.width * 90) / 100, (screenSize.height * 90) / 100);
            // </editor-fold>
            setSize(screenSize);
        } else {
            setLocation(parentFrame.getLocation());
            setSize(parentFrame.getSize());
        }
    }

    void copySelectedContentsToClipboard() {
        int[] selectedRows = null;
        FileContentsListModel listModel = null;
        if (FileContentsList.getLastFocus() == file1ContentsList) {
            selectedRows = file1ContentsList.getSelectedIndices();
            listModel = file1ContentsListModel;
        } else if (FileContentsList.getLastFocus() == file2ContentsList) {
            selectedRows = file2ContentsList.getSelectedIndices();
            listModel = file2ContentsListModel;
        }

        if ((selectedRows != null) && (listModel != null)) {
            StringBuilder selection = new StringBuilder();
            for (int i = 0; i < selectedRows.length; i++) {
                ContentRow rowContents = listModel.getElementAt(selectedRows[i]);
                if (rowContents.getRowType() != ContentRow.ROWTYPE_BLANK) {
                    selection.append(rowContents.getActualText()).append("\n");
                }
            }
            StringSelection stringSelection = new StringSelection(selection.toString());
            Toolkit screenToolkit = java.awt.Toolkit.getDefaultToolkit();
            screenToolkit.getSystemClipboard().setContents(stringSelection, stringSelection);
        }
    }

    void moveToNextDifference() {
        int numberOfChanges = compareFilesForGUI.getNumberOfChanges();
        if ((numberOfChanges - 1) > currentDifferenceIndex) {
            currentDifferenceIndex++;
            moveToCurrentDifference();
        }
    }

    void moveToPreviousDifference() {
        if (currentDifferenceIndex > 0) {
            currentDifferenceIndex--;
            moveToCurrentDifference();
        }
    }

    void moveToCurrentDifference() {
        // Recall that the changes vector is in reverse order, the LAST changes are first, the first
        // changes are last.
        Delta delta = compareFilesForGUI.getDelta(currentDifferenceIndex);

        int file1LineNumber = delta.getOriginal().first() + 1;
        int i;
        int j = -1;
        int maximumRow = file1ContentsListModel.size();
        ContentRow row;
        for (i = 0; (i + file1LineNumber) < maximumRow; i++) {
            row = file1ContentsListModel.elementAt(file1LineNumber + i);
            if (row.getDelta() == delta) {
                j = i;
                break;
            }
        }

        file1ContentsListModel.setCurrentDifferenceIndex(file1LineNumber + j);
        file2ContentsListModel.setCurrentDifferenceIndex(file1LineNumber + j);
        positionViewPort(file1LineNumber + j);

        setNextPreviousActionStates(compareFilesForGUI.getNumberOfChanges());
    }

    // The goal here is to position the view port to put index in the center (vertically)
    // if we can.
    void positionViewPort(int index) {
        captureViewPortSize();
        int midScreenRow = verticalLinesInViewPort / 2;
        int topRow;
        if ((file1ContentsListModel.size() - index) < midScreenRow) {
            // We're positioning near the end of the file, so there's no place to scroll down.
            // Just show the last full screen of lines.
            topRow = file1ContentsListModel.size() - verticalLinesInViewPort;
        } else {
            // We're able to scroll around.
            topRow = index - midScreenRow;
        }
        if (topRow < 0) {
            topRow = 0;
        }
        leftScrollPaneViewPort.setViewPosition(new Point(0, topRow * file1ContentsList.getRowHeight()));
    }

    void captureViewPortSize() {
        if ((leftScrollPaneViewPort != null) && (file1ContentsList != null)) {
            Dimension newSize = leftScrollPaneViewPort.getSize();
            int height = newSize.height;
            verticalLinesInViewPort = height / file1ContentsList.getRowHeight();
        }
    }

    void setNextPreviousActionStates(int numberOfChanges) {
        // Set the state for the move to previous difference.
        if (currentDifferenceIndex <= 0) {
            moveToPreviousDifferenceAction.setEnabled(false);
        } else {
            moveToPreviousDifferenceAction.setEnabled(true);
        }

        // Set the state for the move to next difference.
        if (numberOfChanges > 0) {
            if (currentDifferenceIndex < (numberOfChanges - 1)) {
                moveToNextDifferenceAction.setEnabled(true);
            } else {
                moveToNextDifferenceAction.setEnabled(false);
            }
        } else {
            moveToNextDifferenceAction.setEnabled(false);
        }
    }

    void searchForString() {
        if (searchDialog == null) {
            searchDialog = new SearchDialog(this);
        }
        searchDialog.setVisible(true);
    }

    void hookScrollPanes() {
        leftScrollPaneViewPort = leftScrollPane.getViewport();
        rightScrollPaneViewPort = rightScrollPane.getViewport();

        leftScrollPaneViewPort.addChangeListener(viewportChangeListener);
        rightScrollPaneViewPort.addChangeListener(viewportChangeListener);

        leftScrollPane.getVerticalScrollBar().setUnitIncrement(file1ContentsList.getRowHeight());
        file1ChangeMarkerPanel.setScrollBar(leftScrollPane.getVerticalScrollBar());
        rightScrollPane.getVerticalScrollBar().setUnitIncrement(file1ContentsList.getRowHeight());
        file2ChangeMarkerPanel.setScrollBar(rightScrollPane.getVerticalScrollBar());

        leftScrollPane.getHorizontalScrollBar().setUnitIncrement(file1ContentsList.getRowHeight());
        rightScrollPane.getHorizontalScrollBar().setUnitIncrement(file1ContentsList.getRowHeight());
    }

    RemotePropertiesBaseClass getRemoteProperties() {
        return remoteProperties;
    }

    EditCopyAction getEditCopyAction() {
        return editCopyAction;
    }

    class SymWindow extends java.awt.event.WindowAdapter {

        @Override
        public void windowClosing(java.awt.event.WindowEvent event) {
            Object object = event.getSource();
            if (object == CompareFrame.this) {
                compareFrameWindowClosing(event);
            }
        }
    }

    void compareFrameWindowClosing(java.awt.event.WindowEvent event) {
        remoteProperties.setMRUFile1Name("", "", firstFileActualName);
        remoteProperties.setMRUFile2Name("", "", secondFileActualName);

        if (exitOnCloseFlag) {
            System.exit(0);
        }
    }

    class OurComponentListener extends ComponentAdapter {

        @Override
        public void componentResized(ComponentEvent e) {
            centerSplitterDivider();
        }

        @Override
        public void componentShown(ComponentEvent e) {
            fitToScreen();
        }
    }

    class OurViewportChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            if (e.getSource() == leftScrollPaneViewPort) {
                rightScrollPaneViewPort.repaint();
                rightScrollPaneViewPort.setViewPosition(leftScrollPaneViewPort.getViewPosition());
            } else {
                leftScrollPaneViewPort.repaint();
                leftScrollPaneViewPort.setViewPosition(rightScrollPaneViewPort.getViewPosition());
            }
        }
    }

    class EditCopyAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        EditCopyAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            copySelectedContentsToClipboard();
        }
    }

    class MoveToNextDifferenceAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        MoveToNextDifferenceAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            moveToNextDifference();
        }
    }

    class MoveToPreviousDifferenceAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        MoveToPreviousDifferenceAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            moveToPreviousDifference();
        }
    }

    class FindStringAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        FindStringAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            searchForString();
        }
    }

    class EscapeAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        EscapeAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
            compareFrameWindowClosing(null);
        }
    }

    class PropertiesAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        PropertiesAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            defineProperties();
        }
    }

    class ReCompareAction extends AbstractAction {

        private static final long serialVersionUID = 1L;

        ReCompareAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            leftParentPanel.removeAll();
            rightParentPanel.removeAll();

            // Reload our property settings.
            TransportProxyInterface proxy = TransportProxyFactory.getInstance().getTransportProxy(parentFrame.getActiveServerProperties());
            remoteProperties = RemotePropertiesManager.getInstance().getRemoteProperties(System.getProperty("user.name"), proxy);

            // Init our white space flags.
            ignoreAllWhiteSpaceFlag = getRemoteProperties().getIgnoreAllWhitespace("", "");
            ignoreLeadingWhiteSpaceFlag = getRemoteProperties().getIgnoreLeadingWhitespace("", "");
            ignoreCaseFlag = getRemoteProperties().getIgnoreCase("", "");
            ignoreEOLChangesFlag = getRemoteProperties().getIgnoreEOLChanges("", "");
            compare();
        }
    }

    class OurToolBar extends JToolBar {

        private static final long serialVersionUID = 1L;

        OurToolBar() {
            super();
            JButton copyButton = add(getEditCopyAction());
            copyButton.setIcon(COPY_ICON);
            copyButton.setToolTipText("Copy selection to clipboard (CTRL-C)");
            copyButton.setBorderPainted(false);

            addSeparator();

            JButton moveToNext = add(moveToNextDifferenceAction);
            moveToNext.setIcon(MOVE_TO_NEXT_DIFFERENCE_ICON);
            moveToNext.setToolTipText("Move to next difference (F2)");
            moveToNext.setBorderPainted(false);

            JButton moveToPrevious = add(moveToPreviousDifferenceAction);
            moveToPrevious.setIcon(MOVE_TO_PREVIOUS_DIFFERENCE_ICON);
            moveToPrevious.setToolTipText("Move to previous difference (SHIFT-F2)");
            moveToPrevious.setBorderPainted(false);

            addSeparator();

            JButton searchButton = add(findStringAction);
            searchButton.setIcon(FIND_STRING_ACTION_ICON);
            searchButton.setToolTipText("Search for... (CTRL-F)");
            searchButton.setBorderPainted(false);

            JButton propertiesButton = add(propertiesAction);
            propertiesButton.setIcon(PROPERTIES_ACTION_ICON);
            propertiesButton.setToolTipText("Define compare properties...");
            propertiesButton.setBorderPainted(false);

            JButton reCompareButton = add(reCompareAction);
            reCompareButton.setIcon(RECOMPARE_ACTION_ICON);
            reCompareButton.setToolTipText("Refresh compare");
            reCompareButton.setBorderPainted(false);

            setFloatable(false);
        }
    }

    private void defineProperties() {
        if (comparePropertiesDialog == null) {
            comparePropertiesDialog = new ComparePropertiesDialog(this, remoteProperties);
        }
        comparePropertiesDialog.setVisible(true);
    }
}
