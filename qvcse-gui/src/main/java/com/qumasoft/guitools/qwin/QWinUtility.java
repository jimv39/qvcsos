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
package com.qumasoft.guitools.qwin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Put some QWin static logic here so QWinFrame.java is not too big.
 * @author Jim Voris
 */
public final class QWinUtility {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(QWinUtility.class);

    // Hide the constructor.
    private QWinUtility() {
    }

    static String[] parseExternalVisualCompareCommandLine(String commandLine) {
        String[] retArray;

        if (commandLine.indexOf(".exe") > 0) {
            retArray = buildExternalCompareCommandArray(commandLine, "exe");
        } else if (commandLine.indexOf(".bat") > 0) {
            retArray = buildExternalCompareCommandArray(commandLine, "bat");
        } else if (commandLine.indexOf(".cmd") > 0) {
            retArray = buildExternalCompareCommandArray(commandLine, "cmd");
        } else if (commandLine.indexOf(".sh") > 0) {
            retArray = buildExternalCompareCommandArray(commandLine, "sh");
        } else {
            retArray = commandLine.split(" ");
        }
        return retArray;
    }

    static String[] buildExternalCompareCommandArray(String commandLine, String splitString) {
        String[] retArray;
        String[] scratch1Array;
        scratch1Array = commandLine.split(splitString + " ");
        String[] scratch2Array;
        scratch2Array = scratch1Array[1].split(" ");
        retArray = new String[scratch2Array.length + 1];
        retArray[0] = scratch1Array[0] + splitString;
        for (int i = 1; i < retArray.length; i++) {
            retArray[i] = scratch2Array[i - 1];
        }
        return retArray;
    }

    static String[] substituteCommandLine(String[] parsedCommandLine, String file1Name, String file2Name, String display1, String display2) {
        for (int i = 0; i < parsedCommandLine.length; i++) {
            switch (parsedCommandLine[i]) {
                case "file1Name":
                    parsedCommandLine[i] = file1Name;
                    break;
                case "file2Name":
                    parsedCommandLine[i] = file2Name;
                    break;
                case "display1":
                    parsedCommandLine[i] = display1;
                    break;
                case "display2":
                    parsedCommandLine[i] = display2;
                    break;
                default:
                    break;
            }
        }
        return parsedCommandLine;
    }

    static void executeExternalVisualCompareCommand(String[] substitutedCommandLine) {
        Process visualCompareProcess;

        try {
            File parentDirectory = new File(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory());
            visualCompareProcess = Runtime.getRuntime().exec(substitutedCommandLine, null, parentDirectory);
            visualCompareProcess.waitFor();
            int outputCount = visualCompareProcess.getInputStream().available();
            byte[] output = new byte[outputCount];
            visualCompareProcess.getInputStream().read(output);
            traceMessage("wrote " + outputCount + " exit status: " + visualCompareProcess.exitValue());
            traceMessage(Arrays.toString(output));
        } catch (IOException ioe) {
            warnProblem("Caught IOException: " + ioe.getClass().toString() + " " + ioe.getLocalizedMessage());
        } catch (InterruptedException e) {
            warnProblem("Caught InterruptedException: " + e.getClass().toString() + " " + e.getLocalizedMessage());
            Thread.currentThread().interrupt();
        }
    }

    static void externalVisualCompare(final String file1Name, final String file2Name, final String display1, final String display2) {
        // Put this on a separate thread.
        Runnable worker = () -> {
            String commandLine = QWinFrame.getQWinFrame().getRemoteProperties(QWinFrame.getQWinFrame().getActiveServerProperties().getServerName()).getExternalVisualCommandLine("", "");

            // First we need to parse the command line.
            String[] parsedCommandLine = QWinUtility.parseExternalVisualCompareCommandLine(commandLine);

            // Substitute the actual file names into the command array.
            String[] substitutedCommandLine = QWinUtility.substituteCommandLine(parsedCommandLine, file1Name, file2Name, display1, display2);

            // Execute the command
            QWinUtility.executeExternalVisualCompareCommand(substitutedCommandLine);
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }

    static void reportSystemInfo() {
        java.util.Properties systemProperties = System.getProperties();
        java.util.Set keys = systemProperties.keySet();
        java.util.Iterator it = keys.iterator();
        logMessage("System properties:");
        while (it.hasNext()) {
            String key = (String) it.next();
            String message = key + " = " + System.getProperty(key);
            logMessage(message);
        }
    }

    /**
     * Use this method to avoid potential deadlocks between logging thread and Swing thread.
     * @param logMessage the log message.
     */
    public static void logMessage(final String logMessage) {
        Runnable logProblem = () -> {
            LOGGER.info(logMessage);
        };
        SwingUtilities.invokeLater(logProblem);
    }

    /**
     * Use this method to avoid potential deadlocks between logging thread and Swing thread.
     *
     * @param logMessage the log message.
     */
    public static void traceMessage(final String logMessage) {
        Runnable logProblem = () -> {
            LOGGER.trace(logMessage);
        };
        SwingUtilities.invokeLater(logProblem);
    }

    /**
     * Use this method to avoid potential deadlocks between logging thread and Swing thread.
     *
     * @param logMessage the log message.
     */
    public static void warnProblem(final String logMessage) {
        Runnable logProblem = () -> {
            LOGGER.warn(logMessage);
        };
        SwingUtilities.invokeLater(logProblem);
    }
}
