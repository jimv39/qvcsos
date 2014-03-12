//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JTable;

/**
 * View the workfile operation.
 * @author Jim Voris
 */
public class OperationView extends OperationBaseClass {

    /**
     * Create a view workfile operation.
     * @param fileTable the file table.
     * @param serverName the server name.
     * @param projectName the project name.
     * @param viewName the view name.
     * @param userLocationProperties user location properties.
     */
    public OperationView(JTable fileTable, final String serverName, final String projectName, final String viewName, UserLocationProperties userLocationProperties) {
        super(fileTable, serverName, projectName, viewName, userLocationProperties);
    }

    @Override
    public void executeOperation() {
        if (getFileTable() != null) {
            if (getFileTable().getSelectedRowCount() == 1) {
                try {
                    // Get the selected files...
                    List mergedInfoArray = getSelectedFiles();

                    // TODO.  For now, we only allow the user to 'view' a single
                    // file.
                    MergedInfoInterface mergedInfo = (MergedInfoInterface) mergedInfoArray.get(0);

                    if (mergedInfo.getWorkfileInfo() == null) {
                        QWinUtility.logProblem(Level.INFO, "Workfile does not exist: " + mergedInfo.getShortWorkfileName());
                        return;
                    }
                    view(mergedInfo);
                } catch (Exception e) {
                    QWinUtility.logProblem(Level.WARNING, "OperationView caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }
    }

    void view(MergedInfoInterface mergedInfo) {
        try {
            final String fullWorkfileName = mergedInfo.getWorkfileInfo().getFullWorkfileName();
            String osName = System.getProperty("os.name");

            if (osName.startsWith("Mac OS")) {
                Runtime.getRuntime().exec(new String[]{"open", fullWorkfileName});
            } else if (osName.startsWith("Windows")) {
                File file = new File(fullWorkfileName);
                File parentDirectory = file.getParentFile();

                // Run the associated Windows utility with a working directory
                // set to the file's directory.
                Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + file.getAbsolutePath(), null, parentDirectory);
            } else {
                // Get the command line for the utility we use to view this
                // file.
                String[] commandLine = ViewUtilityManager.getInstance().getViewUtilityCommandLine(fullWorkfileName);

                if (commandLine != null) {
                    // Execute the command
                    File file = new File(fullWorkfileName);
                    File parentDirectory = file.getParentFile();
                    executeExternalCommand(commandLine, parentDirectory);
                }
            }
        } catch (IOException e) {
            QWinUtility.logProblem(Level.WARNING, "OperationView caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
            QWinUtility.logProblem(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }

    private static void executeExternalCommand(final String[] substitutedCommandLine, final File workingDirectory) {
        try {
            // Put this on a separate thread.
            Runnable worker = new Runnable() {

                @Override
                public void run() {
                    try {
                        Process viewWorkfileProcess = Runtime.getRuntime().exec(substitutedCommandLine, null, workingDirectory);
                        viewWorkfileProcess.waitFor();
                        int outputCount = viewWorkfileProcess.getInputStream().available();
                        byte[] output = new byte[outputCount];
                        viewWorkfileProcess.getInputStream().read(output);
                        QWinUtility.logProblem(Level.FINEST, "wrote " + outputCount + " exit status: " + viewWorkfileProcess.exitValue());
                        QWinUtility.logProblem(Level.FINEST, output.toString());
                    } catch (IOException | InterruptedException e) {
                        QWinUtility.logProblem(Level.WARNING, "Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
                    }
                }
            };

            // Put all this on a separate worker thread.
            new Thread(worker).start();
        } catch (Exception e) {
            QWinUtility.logProblem(Level.WARNING, "Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
        }
    }
}