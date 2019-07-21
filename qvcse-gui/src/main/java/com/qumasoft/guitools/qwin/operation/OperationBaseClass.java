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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.FileTableModel;
import com.qumasoft.guitools.qwin.ParentProgressDialogInterface;
import com.qumasoft.guitools.qwin.ProgressDialogInterface;
import com.qumasoft.guitools.qwin.QWinFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.logProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.ParentChildProgressDialog;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.KeywordExpansionContext;
import com.qumasoft.qvcslib.KeywordManagerFactory;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkFile;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Operation base class.
 * @author Jim Voris
 */
public abstract class OperationBaseClass {

    static final int MINIMUM_TO_SHOW_PROGRESS = 10;
    private final JTable fileTable;
    private final String serverName;
    private final String projectName;
    private final String branchName;
    private final UserLocationProperties userLocationProperties;
    // KLUDGE -- use this to store the progress dialog that we create on the Swing thread.
    private static ProgressDialog staticProgressDialog;
    private static ParentChildProgressDialog staticParentChildProgressDialog;
    private boolean workCompletedFlag;

    /**
     * Creates a new instance of OperationBaseClass.
     *
     * @param ft the file table.
     * @param server name of the server
     * @param project name of the project.
     * @param branch name of the branch.
     * @param userLocationProps user location properties.
     */
    public OperationBaseClass(JTable ft, final String server, final String project, final String branch, UserLocationProperties userLocationProps) {
        fileTable = ft;
        serverName = server;
        projectName = project;
        branchName = branch;
        userLocationProperties = userLocationProps;
        workCompletedFlag = false;
    }

    /**
     * Perform the operation.
     */
    public abstract void executeOperation();

    // Return an ArrayList of MergedInfo objects that are associated with
    // the selected files.
    protected List<MergedInfoInterface> getSelectedFiles() {
        int[] selectedRows = getFileTable().getSelectedRows();
        FileTableModel dataModel = (FileTableModel) getFileTable().getModel();
        List<MergedInfoInterface> mergedInfoArray = new ArrayList<>();

        // Create a list of the selected files.
        for (int i = 0; i < selectedRows.length; i++) {
            // Save the names of the archives we'll work on.
            int selectedRowIndex = selectedRows[i];
            MergedInfoInterface mergedInfo = dataModel.getMergedInfo(selectedRowIndex);
            mergedInfoArray.add(mergedInfo);
        }
        return mergedInfoArray;
    }

    protected int getSelectedFileCount() {
        int retVal = 0;
        if (getFileTable() != null) {
            retVal = getFileTable().getSelectedRowCount();
        }
        return retVal;
    }

    protected MergedInfoInterface getFocusedFile() {
        MergedInfoInterface mergedInfo = null;

        if (getSelectedFileCount() > 0) {
            int focusIndex = QWinFrame.getQWinFrame().getRightFilePane().getFocusIndex();
            if (focusIndex >= 0) {
                FileTableModel dataModel = (FileTableModel) getFileTable().getModel();
                mergedInfo = dataModel.getMergedInfo(focusIndex);
            }
        }

        return mergedInfo;
    }

    protected JTable getFileTable() {
        return fileTable;
    }

    protected String getServerName() {
        return serverName;
    }

    protected String getProjectName() {
        return projectName;
    }

    protected String getBranchName() {
        return branchName;
    }

    protected boolean createWorkfileDirectory(MergedInfoInterface mergedInfo) {
        boolean retVal = false;
        String workfileBase = getUserLocationProperties().getWorkfileLocation(getServerName(), getProjectName(), getBranchName());
        String fullWorkfileDirectory = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath();

        File workfileFile = new File(fullWorkfileDirectory);
        if (!workfileFile.exists()) {
            if (!workfileFile.mkdirs()) {
                logProblem("Could not create workfile directory: " + fullWorkfileDirectory);
            } else {
                retVal = true;
            }
        } else {
            retVal = true;
        }
        return retVal;
    }

    protected UserLocationProperties getUserLocationProperties() {
        return userLocationProperties;
    }

    /**
     * Create a progress dialog.
     * @param progressAction what we're doing.
     * @param size the maximum progress value.
     * @return the created ProgressDialog.
     */
    public static ProgressDialog createProgressDialog(final String progressAction, final int size) {
        ProgressDialog progressMonitor = null;
        if (SwingUtilities.isEventDispatchThread()) {
            // Create a progress dialog.
            progressMonitor = new ProgressDialog(QWinFrame.getQWinFrame(), true, 0, size);
            progressMonitor.setAction(progressAction);
        } else {
            Runnable create = () -> {
                // Create a progress dialog.
                staticProgressDialog = new ProgressDialog(QWinFrame.getQWinFrame(), true, 0, size);
                staticProgressDialog.setAction(progressAction);
            };

            try {
                SwingUtilities.invokeAndWait(create);
                progressMonitor = staticProgressDialog;
            } catch (InvocationTargetException e) {
                warnProblem("Caught InvocationTargetException: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
            } catch (InterruptedException ie) {
                warnProblem("Caught InterruptedException: " + ie.getClass().toString() + " : " + ie.getLocalizedMessage());
                Thread.currentThread().interrupt();
            }
        }

        return progressMonitor;
    }

    /**
     * Initialize the given progress dialog.
     * @param action to say what is going on.
     * @param min the minimum value for the progress bar.
     * @param max the maximum value for the progress bar.
     * @param progressDialog the progress dialog to be initialized.
     */
    public static void initProgressDialog(final String action, final int min, final int max, final ProgressDialogInterface progressDialog) {
        // Update the progress monitor
        Runnable later = () -> {
            progressDialog.initProgressBar(min, max);
            progressDialog.setAction(action);
        };
        javax.swing.SwingUtilities.invokeLater(later);
    }

    /**
     * Update the given progress dialog.
     * @param progress how far along we are.
     * @param activity what we're doing.
     * @param progressDialog the progress dialog to update.
     */
    public static void updateProgressDialog(final int progress, final String activity, final ProgressDialogInterface progressDialog) {
        // Update the progress monitor
        Runnable later = () -> {
            if ((progress > MINIMUM_TO_SHOW_PROGRESS) && !progressDialog.getProgressDialogVisibleFlag()) {
                progressDialog.setVisible(true);
                progressDialog.setProgressDialogVisibleFlag(true);
            }
            progressDialog.setProgress(progress);
            progressDialog.setActivity(activity);
        };
        javax.swing.SwingUtilities.invokeLater(later);
    }

    /**
     * Create a parent-child progress dialog.
     * @param progressAction the action to show.
     * @param size the maximum value for the parent's progress bar.
     * @return a parent-child progress dialog.
     */
    public static ParentChildProgressDialog createParentProgressDialog(final String progressAction, final int size) {
        ParentChildProgressDialog progressMonitor = null;
        if (SwingUtilities.isEventDispatchThread()) {
            // Create a progress dialog.
            progressMonitor = new ParentChildProgressDialog(QWinFrame.getQWinFrame(), true, 0, size);
            progressMonitor.setParentAction(progressAction);
        } else {
            Runnable create = () -> {
                // Create a progress dialog.
                staticParentChildProgressDialog = new ParentChildProgressDialog(QWinFrame.getQWinFrame(), true, 0, size);
                staticParentChildProgressDialog.setParentAction(progressAction);
            };

            try {
                SwingUtilities.invokeAndWait(create);
                progressMonitor = staticParentChildProgressDialog;
            } catch (InvocationTargetException e) {
                warnProblem("Caught InvocationTargetException: " + e.getClass().toString() + " : " + e.getLocalizedMessage());
            } catch (InterruptedException ie) {
                warnProblem("Caught InterruptedException: " + ie.getClass().toString() + " : " + ie.getLocalizedMessage());
                Thread.currentThread().interrupt();
            }
        }

        return progressMonitor;
    }

    static void initParentChildProgressDialog(final String action, final int min, final int max, final ParentProgressDialogInterface progressDialog) {
        // Update the progress monitor
        Runnable later = () -> {
            progressDialog.initParentProgressBar(min, max);
            progressDialog.setParentAction(action);
        };
        javax.swing.SwingUtilities.invokeLater(later);
    }

    /**
     * Update a parent-child progress dialog.
     * @param i the parent progress.
     * @param activity what is happening.
     * @param progressDialog the dialog that we are to update.
     */
    public static void updateParentChildProgressDialog(final int i, final String activity, final ParentProgressDialogInterface progressDialog) {
        // Update the progress monitor
        Runnable later = () -> {
            progressDialog.setParentProgress(i);
            progressDialog.setParentActivity(activity);
        };
        javax.swing.SwingUtilities.invokeLater(later);
    }

    protected void writeMergedResultToWorkfile(MergedInfoInterface mergedInfo, String workfileBase, byte[] buffer) {
        java.io.FileOutputStream outputStream = null;
        ArchiveDirManagerProxy dirManagerProxy;

        try {
            String fullWorkfileName = constructFullWorkfileName(mergedInfo, workfileBase);
            WorkFile workfile = new WorkFile(fullWorkfileName);
            if (!mergedInfo.getWorkfileExists()) {
                createWorkfileDirectory(mergedInfo);
            }
            if (workfile.exists()) {
                workfile.setReadWrite();
            }

            // Figure out our directory manager.
            dirManagerProxy = (ArchiveDirManagerProxy) mergedInfo.getArchiveDirManager();

            // See if we have to worry about keyword expansion...
            if (mergedInfo.getKeywordExpansionAttribute() && dirManagerProxy != null) {
                try {
                    outputStream = new java.io.FileOutputStream(workfile);
                    KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outputStream,
                            workfile,
                            mergedInfo.getLogfileInfo(),
                            mergedInfo.getLogfileInfo().getRevisionInformation().getRevisionIndex(mergedInfo.getDefaultRevisionString()),
                            "",
                            dirManagerProxy.getAppendedPath(),
                            dirManagerProxy.getProjectProperties());
                    KeywordManagerFactory.getInstance().getKeywordManager().expandKeywords(buffer, keywordExpansionContext);
                } catch (QVCSException e) {
                    warnProblem(Utility.expandStackTraceToString(e));
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } else {
                try {
                    outputStream = new java.io.FileOutputStream(workfile);
                    Utility.writeDataToStream(buffer, outputStream);
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
        } catch (java.io.IOException e) {
            warnProblem(Utility.expandStackTraceToString(e));
        }
    }

    protected void writeConflictFile(MergedInfoInterface mergedInfo, String conflictFileType, String workfileBase, byte[] buffer) {
        java.io.FileOutputStream outputStream = null;

        try {
            String fileExtension = Utility.getFileExtension(mergedInfo.getShortWorkfileName());
            String fullWorkfileName = constructFullWorkfileName(mergedInfo, workfileBase);
            int indexOfExtension = fullWorkfileName.lastIndexOf(".");
            String constructedFileName = fullWorkfileName.substring(0, indexOfExtension) + "." + conflictFileType + "." + fileExtension;
            WorkFile workfile = new WorkFile(constructedFileName);
            if (!mergedInfo.getWorkfileExists()) {
                createWorkfileDirectory(mergedInfo);
            }
            if (workfile.exists()) {
                workfile.setReadWrite();
            }

            try {
                outputStream = new java.io.FileOutputStream(workfile);
                Utility.writeDataToStream(buffer, outputStream);
            } finally {
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (java.io.IOException e) {
            warnProblem(Utility.expandStackTraceToString(e));
        }
    }

    private String constructFullWorkfileName(MergedInfoInterface mergedInfo, String workfileBase) {
        String fullWorkfileName = workfileBase + File.separator + mergedInfo.getArchiveDirManager().getAppendedPath() + File.separator + mergedInfo.getShortWorkfileName();
        return fullWorkfileName;
    }

    /**
     * Is the operation complete.
     *
     * @return true if the operation has completed; false if it has not completed.
     */
    public boolean isWorkCompleted() {
        return this.workCompletedFlag;
    }

    /**
     * Used by child classes to indicate that they have finished their work.
     */
    protected void setWorkCompleted() {
        this.workCompletedFlag = true;
    }
}
