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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.QWinFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.logMessage;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.FileMerge;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import com.qumasoft.qvcslib.WorkfileInfo;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Merge a file.
 * @author Jim Voris
 */
public class OperationMergeFile extends OperationBaseClass {

    /**
     * Create a merge file operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param userLocationProperties user location properties.
     */
    public OperationMergeFile(JTable fileTable, String serverName, String projectName, String branchName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, branchName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                final List mergedInfoArray = getSelectedFiles();
                final int fileCount = mergedInfoArray.size();
                if (mergedInfoArray.size() > 0) {
                    // Run the update on the Swing thread.
                    Runnable later = () -> {
                        // Ask the user to confirm the merge operation.
                        int answer = JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), "Merge the selected file(s)?", "Merge Selected Workfiles",
                                JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                        if (answer == JOptionPane.YES_OPTION) {
                            Iterator it = mergedInfoArray.iterator();

                            while (it.hasNext()) {
                                MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();
                                if ((!mergedInfo.getAttributes().getIsBinaryfile())
                                        && (mergedInfo.getStatusIndex() == MergedInfoInterface.MERGE_REQUIRED_STATUS_INDEX)) {
                                    try {
                                        mergeFile(mergedInfo, fileCount);
                                    } catch (IOException | QVCSException e) {
                                        warnProblem(Utility.expandStackTraceToString(e));
                                    }
                                } else {
                                    if (mergedInfo.getAttributes().getIsBinaryfile()) {
                                        logMessage("Skipping merge of binary file: " + mergedInfo.getShortWorkfileName());
                                    }
                                }
                            }

                            QWinFrame.getQWinFrame().refreshCurrentBranch();
                        }
                    };
                    SwingUtilities.invokeLater(later);

                }
            } catch (Exception e) {
                warnProblem("OperationMergeFile caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    private void mergeFile(MergedInfoInterface mergedInfo, final int fileCount) throws IOException, QVCSException {
        // Get the base revision string for the current workfile.
        String baseRevisionString = WorkfileDigestManager.getInstance().getDigestWorkfileInfo(mergedInfo.getWorkfileInfo()).getWorkfileRevisionString();
        String currentDefaultRevisionString = mergedInfo.getDefaultRevisionString();

        // Get workfile buffer... not keyword expanded.
        byte[] baseBuffer = mergedInfo.getRevisionAsByteArray(baseRevisionString);
        byte[] defaultBuffer = mergedInfo.getRevisionAsByteArray(currentDefaultRevisionString);

        if ((baseBuffer != null) && (defaultBuffer != null)) {
            File expandedBaseBufferFile = Utility.writeBufferToFile(baseBuffer);
            File expandedDefaultBufferFile = Utility.writeBufferToFile(defaultBuffer);
            File currentWorkFile = mergedInfo.getWorkfile();
            File afterMergeWorkFile = new File(currentWorkFile.getCanonicalPath() + ".qvcsAfterMerge");

            FileMerge instance = new FileMerge();
            boolean result;
            try {
                result = instance.mergeFiles(expandedBaseBufferFile.getCanonicalPath(), expandedDefaultBufferFile.getCanonicalPath(), currentWorkFile.getCanonicalPath(),
                        afterMergeWorkFile.getCanonicalPath());
                if (result) {
                    // Rename the current workfile to a backup copy.
                    File beforeMergeWorkFile = new File(currentWorkFile.getCanonicalPath() + ".qvcsBeforeMerge");
                    beforeMergeWorkFile.delete();
                    currentWorkFile.renameTo(beforeMergeWorkFile);

                    // Rename the merged workfile to be the new workfile
                    currentWorkFile = mergedInfo.getWorkfile();
                    afterMergeWorkFile.renameTo(currentWorkFile);

                    // Create a new workfileInfo that we use in the case that the user saves the result of the merge...
                    WorkfileInfo workfileInfo = new WorkfileInfo(mergedInfo.getFullWorkfileName(),
                            mergedInfo.getAttributes().getIsBinaryfile(), getProjectName(), getBranchName());
                    Date now = new Date();
                    workfileInfo.setFetchedDate(now.getTime());
                    workfileInfo.setWorkfileRevisionString(currentDefaultRevisionString);

                    // Update the Workfile digest manager.
                    WorkfileDigestManager.getInstance().updateWorkfileDigestForMerge(defaultBuffer, workfileInfo);
                }
            } catch (QVCSOperationException e) {
                // We expect a QVCSOperationException to be thrown to flag the overlap.

                // If they are only merging one file, then we can display a TextPane to warn them about the overlap.
                if (fileCount == 1) {
                    JOptionPane.showConfirmDialog(QWinFrame.getQWinFrame(), e.getLocalizedMessage(), "Merge: Overlap detected!", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }
}
