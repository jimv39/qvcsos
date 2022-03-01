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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.merge.MergeFrame;
import com.qumasoft.guitools.qwin.QWinFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkFile;
import com.qumasoft.qvcslib.WorkfileDigestManager;
import com.qumasoft.qvcslib.WorkfileInfo;
import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 * Visual merge operation.
 * @author Jim Voris
 */
public class OperationVisualMerge extends OperationBaseClass {

    /**
     * Create a visual merge operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param userLocationProperties user location properties.
     */
    public OperationVisualMerge(JTable fileTable, String serverName, String projectName, String branchName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, branchName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                final List mergedInfoArray = getSelectedFiles();
                if (mergedInfoArray.size() == 1) {
                    // Run the update on the Swing thread.
                    Runnable later = () -> {
                        Iterator it = mergedInfoArray.iterator();

                        while (it.hasNext()) {
                            MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();
                            if ((!mergedInfo.getAttributes().getIsBinaryfile()) && (mergedInfo.getStatusIndex() == MergedInfoInterface.MERGE_REQUIRED_STATUS_INDEX)) {
                                try {
                                    visualMerge(mergedInfo);
                                } catch (QVCSOperationException e) {
                                    JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), e.getLocalizedMessage(), "Visual merge failed!", JOptionPane.WARNING_MESSAGE);
                                } catch (IOException e) {
                                    warnProblem(Utility.expandStackTraceToString(e));
                                }
                            } else {
                                if (mergedInfo.getAttributes().getIsBinaryfile()) {
                                    JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "Merging for binary files is not supported.", "Binary merge not supported",
                                            JOptionPane.WARNING_MESSAGE);
                                } else {
                                    if (mergedInfo.getStatusIndex() != MergedInfoInterface.MERGE_REQUIRED_STATUS_INDEX) {
                                        JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "Visual merge is only supported for files with 'Merged Required' status.",
                                                "Merge not required", JOptionPane.WARNING_MESSAGE);
                                    }
                                }
                            }
                        }
                    };
                    SwingUtilities.invokeLater(later);
                } else {
                    JOptionPane.showMessageDialog(QWinFrame.getQWinFrame(), "Please select just a single file to merge.", "Merge one file only", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (HeadlessException e) {
                warnProblem("OperationMergeFile caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    private void visualMerge(MergedInfoInterface mergedInfo) throws IOException, QVCSOperationException {
        // Get the base revision string for the current workfile.
        String baseRevisionString = WorkfileDigestManager.getInstance().getDigestWorkfileInfo(mergedInfo.getWorkfileInfo()).getWorkfileRevisionString();
        String currentDefaultRevisionString = mergedInfo.getDefaultRevisionString();

        // Get workfile buffer...
        byte[] baseBuffer = mergedInfo.getRevisionAsByteArray(baseRevisionString);
        byte[] defaultBuffer = mergedInfo.getRevisionAsByteArray(currentDefaultRevisionString);

        // Create a new workfileInfo that we use in the case that the user saves the result of the merge...
        WorkfileInfo workfileInfo = new WorkfileInfo(mergedInfo.getFullWorkfileName(), mergedInfo.getAttributes().getIsBinaryfile(), getProjectName(), getBranchName());
        Date now = new Date();
        workfileInfo.setFetchedDate(now.getTime());
        workfileInfo.setWorkfileRevisionString(currentDefaultRevisionString);

        if ((baseBuffer != null) && (defaultBuffer != null)) {
            File expandedBaseBufferFile = Utility.writeBufferToFile(baseBuffer);
            File expandedDefaultBufferFile = Utility.writeBufferToFile(defaultBuffer);
            File currentWorkFile = mergedInfo.getWorkfile();
            File afterMergeWorkFile = new File(currentWorkFile.getCanonicalPath() + ".qvcsAfterMerge");
            WorkFile workFile = new WorkFile(currentWorkFile.getCanonicalPath());

            MergeFrame mergeFrame = new MergeFrame(QWinFrame.getQWinFrame());
            mergeFrame.mergeFiles(expandedBaseBufferFile.getCanonicalPath(),
                    expandedDefaultBufferFile.getCanonicalPath(), mergedInfo.getShortWorkfileName() + " Revision: " + currentDefaultRevisionString,
                    currentWorkFile.getCanonicalPath(), mergedInfo.getShortWorkfileName() + " Revision: " + baseRevisionString + " plus your edits.",
                    afterMergeWorkFile.getCanonicalPath(), "Merged Result: " + mergedInfo.getShortWorkfileName(),
                    this, workFile, workfileInfo, defaultBuffer);
        }
    }

    /**
     * Update the workfile info as a result of the merge.
     * @param workfileInfo the workfile info.
     * @param defaultWorkfileBuffer the bytes of the default workfile for the given file.
     */
    public void updateWorkfileInfo(final WorkfileInfo workfileInfo, final byte[] defaultWorkfileBuffer) {
        // Run the update on the Swing thread.
        Runnable later = () -> {
            try {
                // Update the Workfile digest manager.
                WorkfileDigestManager.getInstance().updateWorkfileDigestForMerge(defaultWorkfileBuffer, workfileInfo);
                QWinFrame.getQWinFrame().refreshCurrentBranch();
            } catch (QVCSException e) {
                warnProblem(Utility.expandStackTraceToString(e));
            }
        };
        SwingUtilities.invokeLater(later);
    }
}
