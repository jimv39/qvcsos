/*   Copyright 2004-2015 Jim Voris
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
import static com.qumasoft.guitools.qwin.QWinUtility.logProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.CompareRevisionsDialog;
import com.qumasoft.qvcslib.ClientExpansionContext;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.IOException;
import javax.swing.JTable;

/**
 * Compare revisions operation.
 * @author Jim Voris
 */
public class OperationCompareRevisions extends OperationBaseClass {

    /**
     * Create a compare revisions operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    public OperationCompareRevisions(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            try {
                // Get the focused file...
                MergedInfoInterface mergedInfo = getFocusedFile();

                if (getFocusedFile() != null) {
                    if (mergedInfo.getArchiveInfo() == null) {
                        logProblem("Archive does not exist for: " + mergedInfo.getShortWorkfileName());
                        return;
                    }
                    CompareRevisionsDialog compareRevisionsDialog = new CompareRevisionsDialog(QWinFrame.getQWinFrame(), mergedInfo, this);
                    compareRevisionsDialog.setVisible(true);
                }
            } catch (Exception e) {
                warnProblem("Caught exception in OperationCompareRevisions: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
                warnProblem(Utility.expandStackTraceToString(e));
            }
        }
    }

    /**
     * Process the dialog choices.
     * @param mergedInfo the file to operate on.
     * @param compareRevisionsDialog the dialog from which we may get additional information.
     */
    public void processDialogResult(MergedInfoInterface mergedInfo, CompareRevisionsDialog compareRevisionsDialog) {
        try {
            Object[] selectedRevisions = compareRevisionsDialog.getSelectedRevisions();
            File selectedFile = compareRevisionsDialog.getSelectedFile();
            String buffer1DisplayName;
            String buffer2DisplayName;
            boolean keywordExpandBuffer1Flag;
            int buffer1RevisionIndex;
            int buffer2RevisionIndex;
            String labelString1;
            String labelString2;

            if (selectedFile != null) {
                // We'll be comparing to another file that the user has selected.
                File expandedBuffer1 = selectedFile;
                buffer1DisplayName = selectedFile.getCanonicalPath();

                File expandedBuffer2 = null;
                if (selectedRevisions != null) {
                    // Compare that other file to the revision that the user
                    // selected.
                    byte[] buffer2;
                    String revisionString = getSelectedRevisionString(selectedRevisions[0], mergedInfo);
                    labelString2 = getSelectedLabelString(selectedRevisions[0]);
                    // Get workfile buffer... not keyword expanded.
                    buffer2 = mergedInfo.getRevisionAsByteArray(revisionString);
                    buffer2DisplayName = mergedInfo.getShortWorkfileName() + ": Revision " + revisionString;
                    buffer2RevisionIndex = mergedInfo.getLogfileInfo().getRevisionInformation().getRevisionIndex(revisionString);
                    if (buffer2 != null) {
                        ClientExpansionContext clientExpansionContext = new ClientExpansionContext(getServerName(),
                                QWinFrame.getQWinFrame().getUserProperties(),
                                QWinFrame.getQWinFrame().getUserLocationProperties(),
                                buffer2RevisionIndex, labelString2, true);
                        expandedBuffer2 = Utility.expandBuffer(buffer2, mergedInfo, clientExpansionContext);
                    }
                } else {
                    // Compare that other file to the current workfile of the
                    // selected file.
                    expandedBuffer2 = new File(mergedInfo.getFullWorkfileName());
                    buffer2DisplayName = mergedInfo.getFullWorkfileName();
                }
                if (expandedBuffer2 != null) {
                    QWinFrame.getQWinFrame().visualCompare(expandedBuffer2.getCanonicalPath(),
                            expandedBuffer1.getCanonicalPath(),
                            buffer2DisplayName,
                            buffer1DisplayName);
                }
            } else if (selectedRevisions.length == 1) {
                byte[] buffer2;

                String firstRevisionString = getSelectedRevisionString(selectedRevisions[0], mergedInfo);
                labelString2 = getSelectedLabelString(selectedRevisions[0]);

                // Get workfile buffer... not keyword expanded.
                buffer2 = mergedInfo.getRevisionAsByteArray(firstRevisionString);

                buffer1DisplayName = mergedInfo.getFullWorkfileName();
                buffer2RevisionIndex = mergedInfo.getLogfileInfo().getRevisionInformation().getRevisionIndex(firstRevisionString);
                buffer2DisplayName = mergedInfo.getShortWorkfileName() + ": Revision " + firstRevisionString;

                if (buffer2 != null) {
                    File expandedBuffer1 = new File(mergedInfo.getFullWorkfileName());
                    ClientExpansionContext clientExpansionContext = new ClientExpansionContext(getServerName(), QWinFrame.getQWinFrame().getUserProperties(),
                            QWinFrame.getQWinFrame().getUserLocationProperties(), buffer2RevisionIndex, labelString2, true);
                    File expandedBuffer2 = Utility.expandBuffer(buffer2, mergedInfo, clientExpansionContext);

                    QWinFrame.getQWinFrame().visualCompare(expandedBuffer2.getCanonicalPath(),
                            expandedBuffer1.getCanonicalPath(),
                            buffer2DisplayName,
                            buffer1DisplayName);
                }
            } else {
                byte[] buffer1;
                byte[] buffer2;

                String firstRevisionString = getSelectedRevisionString(selectedRevisions[0], mergedInfo);
                labelString1 = getSelectedLabelString(selectedRevisions[0]);
                String secondRevisionString = getSelectedRevisionString(selectedRevisions[1], mergedInfo);
                labelString2 = getSelectedLabelString(selectedRevisions[1]);
                keywordExpandBuffer1Flag = true;

                // Get workfile buffer... not keyword expanded.
                buffer1 = mergedInfo.getRevisionAsByteArray(firstRevisionString);
                buffer2 = mergedInfo.getRevisionAsByteArray(secondRevisionString);

                buffer1DisplayName = mergedInfo.getShortWorkfileName() + ": Revision " + firstRevisionString;
                buffer2DisplayName = mergedInfo.getShortWorkfileName() + ": Revision " + secondRevisionString;

                buffer1RevisionIndex = mergedInfo.getLogfileInfo().getRevisionInformation().getRevisionIndex(firstRevisionString);
                buffer2RevisionIndex = mergedInfo.getLogfileInfo().getRevisionInformation().getRevisionIndex(secondRevisionString);

                if ((buffer1 != null) && (buffer2 != null)) {
                    ClientExpansionContext context1 = new ClientExpansionContext(getServerName(), QWinFrame.getQWinFrame().getUserProperties(),
                            QWinFrame.getQWinFrame().getUserLocationProperties(), buffer1RevisionIndex, labelString1, keywordExpandBuffer1Flag);
                    File expandedBuffer1 = Utility.expandBuffer(buffer1, mergedInfo, context1);
                    ClientExpansionContext context2 = new ClientExpansionContext(getServerName(), QWinFrame.getQWinFrame().getUserProperties(),
                            QWinFrame.getQWinFrame().getUserLocationProperties(), buffer2RevisionIndex, labelString2, true);
                    File expandedBuffer2 = Utility.expandBuffer(buffer2, mergedInfo, context2);

                    QWinFrame.getQWinFrame().visualCompare(expandedBuffer2.getCanonicalPath(),
                            expandedBuffer1.getCanonicalPath(),
                            buffer2DisplayName,
                            buffer1DisplayName);
                }
            }
        } catch (IOException e) {
            warnProblem("Caught exception in OperationCompareRevisions: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            warnProblem(Utility.expandStackTraceToString(e));
        }
    }

    private String getSelectedRevisionString(final java.lang.Object selection, MergedInfoInterface mergedInfo) {
        String revisionString = null;
        if (selection instanceof String) {
            revisionString = (String) selection;
        } else if (selection instanceof LabelInfo) {
            LabelInfo labelInfo = (LabelInfo) selection;
            if (labelInfo.isFloatingLabel()) {
                String labelRevisionString = labelInfo.getLabelRevisionString();
                RevisionInformation revisionInformation = mergedInfo.getLogfileInfo().getRevisionInformation();
                int revisionCount = mergedInfo.getLogfileInfo().getLogFileHeaderInfo().getRevisionCount();
                for (int i = 0; i < revisionCount; i++) {
                    RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
                    if (revHeader.getDepth() == labelInfo.getDepth()) {
                        if (revHeader.isTip()) {
                            String revString = revHeader.getRevisionString();
                            if (revString.startsWith(labelRevisionString)) {
                                revisionString = revString;
                                break;
                            }
                        }
                    }
                }
            } else {
                revisionString = labelInfo.getLabelRevisionString();
            }
        }
        return revisionString;
    }

    private String getSelectedLabelString(final java.lang.Object selection) {
        String labelString = null;
        if (selection instanceof LabelInfo) {
            LabelInfo labelInfo = (LabelInfo) selection;
            labelString = labelInfo.getLabelString();
        }
        return labelString;
    }
}
