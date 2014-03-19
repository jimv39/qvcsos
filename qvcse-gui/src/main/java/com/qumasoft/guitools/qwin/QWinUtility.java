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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Put some QWin static logic here so QWinFrame.java is not too big.
 * @author Jim Voris
 */
public final class QWinUtility {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.guitools.qwin");

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
            if (parsedCommandLine[i].equals("file1Name")) {
                parsedCommandLine[i] = file1Name;
            } else if (parsedCommandLine[i].equals("file2Name")) {
                parsedCommandLine[i] = file2Name;
            } else if (parsedCommandLine[i].equals("display1")) {
                parsedCommandLine[i] = display1;
            } else if (parsedCommandLine[i].equals("display2")) {
                parsedCommandLine[i] = display2;
            }
        }
        return parsedCommandLine;
    }

    static void executeExternalVisualCompareCommand(String[] substitutedCommandLine) {
        Process visualCompareProcess;

        try {
            String userDirectory = System.getProperty("user.dir");
            File parentDirectory = new File(userDirectory);
            visualCompareProcess = Runtime.getRuntime().exec(substitutedCommandLine, null, parentDirectory);
            visualCompareProcess.waitFor();
            int outputCount = visualCompareProcess.getInputStream().available();
            byte[] output = new byte[outputCount];
            visualCompareProcess.getInputStream().read(output);
            logProblem(Level.FINEST, "wrote " + outputCount + " exit status: " + visualCompareProcess.exitValue());
            logProblem(Level.FINEST, output.toString());
        } catch (IOException | InterruptedException e) {
            logProblem(Level.WARNING, "Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
        }
    }

    static void externalVisualCompare(final String file1Name, final String file2Name, final String display1, final String display2) {
        // Put this on a separate thread.
        Runnable worker = new Runnable() {
            @Override
            public void run() {
                String commandLine = QWinFrame.getQWinFrame().getUserProperties().getExternalVisualCommandLine();

                // First we need to parse the command line.
                String[] parsedCommandLine = QWinUtility.parseExternalVisualCompareCommandLine(commandLine);

                // Substitute the actual file names into the command array.
                String[] substitutedCommandLine = QWinUtility.substituteCommandLine(parsedCommandLine, file1Name, file2Name, display1, display2);

                // Execute the command
                QWinUtility.executeExternalVisualCompareCommand(substitutedCommandLine);
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }

    static void reportSystemInfo() {
        java.util.Properties systemProperties = System.getProperties();
        java.util.Set keys = systemProperties.keySet();
        java.util.Iterator it = keys.iterator();
        logProblem(Level.INFO, "System properties:");
        while (it.hasNext()) {
            String key = (String) it.next();
            String message = key + " = " + System.getProperty(key);
            logProblem(Level.INFO, message);
        }
    }

    /**
     * Use this method to avoid potential deadlocks between logging thread and Swing thread.
     * @param level the log level.
     * @param logMessage the log message.
     */
    public static void logProblem(final Level level, final String logMessage) {
        Runnable logProblem = new Runnable() {

            @Override
            public void run() {
                LOGGER.log(level, logMessage);
            }
        };
        SwingUtilities.invokeLater(logProblem);
    }
}
