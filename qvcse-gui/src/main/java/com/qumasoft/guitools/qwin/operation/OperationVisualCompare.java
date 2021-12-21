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

import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import java.io.IOException;
import javax.swing.JTable;
import static com.qumasoft.guitools.qwin.QWinUtility.logMessage;
import static com.qumasoft.guitools.qwin.QWinUtility.traceMessage;

/**
 * Visual compare operation.
 * @author Jim Voris
 */
public class OperationVisualCompare extends OperationBaseClass {

    /**
     * Create a visual compare operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param userLocationProperties user location properties.
     */
    public OperationVisualCompare(JTable fileTable, String serverName, String projectName, final String branchName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, branchName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            if (getFocusedFile() != null) {
                try {
                    // Get the focused file...
                    MergedInfoInterface mergedInfo = getFocusedFile();

                    if (mergedInfo.getWorkfileInfo() == null) {
                        logMessage("Workfile does not exist: " + mergedInfo.getShortWorkfileName());
                        return;
                    }

                    if (mergedInfo.getArchiveInfo() == null) {
                        logMessage("Archive does not exist for: " + mergedInfo.getShortWorkfileName());
                        return;
                    }
                    compare(mergedInfo);
                } catch (Exception e) {
                    warnProblem("operationVisualCompare caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    warnProblem(Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    /**
     * Visually compare the latest revision in the given file to the current workfile.
     * @param mergedInfo the file of interest.
     */
    public void compare(MergedInfoInterface mergedInfo) {
        try {
            String fullWorkfileName = mergedInfo.getWorkfileInfo().getFullWorkfileName();
            String extension = Utility.getFileExtension(fullWorkfileName);
            String revisionString = mergedInfo.getDefaultRevisionString();

            // This is the full name of the temp file we use to fetch the trunk tip revision into.
            java.io.File tempFile;
            if (extension.length() > 0) {
                tempFile = java.io.File.createTempFile("QVCSTEMP_", ".Revision." + revisionString + "." + extension);
            } else {
                tempFile = java.io.File.createTempFile("QVCSTEMP_", ".Revision." + revisionString + "." + "tmp");
            }
            tempFile.deleteOnExit();
            if (mergedInfo.getIsRemote()) {
                GetRevisionCommandArgs commandArgs = new GetRevisionCommandArgs();

                commandArgs.setRevisionString(mergedInfo.getDefaultRevisionString());
                commandArgs.setFullWorkfileName(fullWorkfileName);
                commandArgs.setShortWorkfileName(mergedInfo.getShortWorkfileName());
                commandArgs.setOutputFileName(tempFile.getCanonicalPath());
                if (mergedInfo.getForVisualCompare(commandArgs, tempFile.getCanonicalPath())) {
                    traceMessage("Requested revision " + commandArgs.getRevisionString() + " for visual compare for " + fullWorkfileName);
                }
            } else {
                warnProblem("Local visual compare operation not supported!!");
            }
        } catch (QVCSException | IOException e) {
            warnProblem("operationVisualCompare caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
            warnProblem(Utility.expandStackTraceToString(e));
        }
    }
}
