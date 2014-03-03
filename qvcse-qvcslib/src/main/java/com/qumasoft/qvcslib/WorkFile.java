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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Representation of a workfile to make it easier to work with workfiles.
 * @author Jim Voris
 */
public class WorkFile extends java.io.File {
    private static final long serialVersionUID = 5329299107896621732L;
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");

    private static final String COMMAND;          // chmod OR attrib
    private static final String COMMAND_SWITCH;   // -r or +rw
    private static final int COMMAND_ARRAY_SIZE = 3;

    // Static block that figures out what command to use to change the attributes of a workfile.
    static {
        if (separatorChar == '/') {
            // *NIX system.
            COMMAND = "chmod";
            COMMAND_SWITCH = "+rw";
        } else {
            // Windows system.
            COMMAND = "attrib";
            COMMAND_SWITCH = "-r";
        }
    }

    /**
     * Creates a new instance of WorkFile.
     * @param pathname the path to the workfile.
     */
    public WorkFile(String pathname) {
        super(Utility.convertToStandardPath(pathname));
    }

    /**
     * Set this workfile to be read/write.
     * @return true if we succeeded in changing the r/w attributes of the workfile; false otherwise.
     */
    public boolean setReadWrite() {
        boolean retVal = true;
        Process setReadWrite = null;

        try {
            // We only do this to vanilla files.
            if (isFile()) {
                String[] commandArray = new String[COMMAND_ARRAY_SIZE];
                commandArray[0] = COMMAND;
                commandArray[1] = COMMAND_SWITCH;
                commandArray[2] = getName();
                File parentDirectory = getParentFile();
                setReadWrite = Runtime.getRuntime().exec(commandArray, null, parentDirectory);
                setReadWrite.waitFor();
                int outputCount = setReadWrite.getInputStream().available();
                byte[] output = new byte[outputCount];
                setReadWrite.getInputStream().read(output);
                String commandResult = new String(output, "UTF-8");
                LOGGER.log(Level.FINEST, "wrote " + outputCount + " exit status: " + setReadWrite.exitValue());
                LOGGER.log(Level.FINEST, commandResult);
            }
        } catch (IOException | InterruptedException e) {
            retVal = false;
            LOGGER.log(Level.WARNING, "Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
        }

        if (setReadWrite != null) {
            if (setReadWrite.exitValue() != 0) {
                retVal = false;
            }
        }

        return retVal;
    }

    /**
     * Rename a workfile.
     * @param directoryName the directory name.
     * @param oldFilename the old file name.
     * @param newFilename the new file name.
     * @return true if the rename worked; false otherwise.
     */
    public static boolean renameFile(String directoryName, String oldFilename, String newFilename) {
        boolean retVal = false;
        String fullOldFilename = directoryName + File.separator + oldFilename;
        String fullNewFilename = directoryName + File.separator + newFilename;

        WorkFile oldFile = new WorkFile(fullOldFilename);
        if (oldFile.exists()) {
            boolean readOnlyFlag = !oldFile.canWrite();

            // Make sure we can read/write this file.
            oldFile.setReadWrite();

            // Construct the destination File object.
            WorkFile newFile = new WorkFile(fullNewFilename);

            // And rename the file.
            retVal = oldFile.renameTo(newFile);

            if (readOnlyFlag) {
                newFile.setReadOnly();
            }
        }
        return retVal;
    }

    /**
     * Move a workfile from one directory to another.
     * @param originDirectoryName origin directory name.
     * @param destinationDirectoryName destination directory.
     * @param shortFilename the short filename.
     * @return true if the move was successful; false otherwise.
     */
    public static boolean moveFile(String originDirectoryName, String destinationDirectoryName, String shortFilename) {
        boolean retVal = false;
        String fullOldFilename = originDirectoryName + File.separator + shortFilename;
        String fullNewFilename = destinationDirectoryName + File.separator + shortFilename;

        WorkFile oldFile = new WorkFile(fullOldFilename);
        if (oldFile.exists()) {
            boolean readOnlyFlag = !oldFile.canWrite();

            // Make sure we can read/write this file.
            oldFile.setReadWrite();

            // Construct the destination File object.
            WorkFile newFile = new WorkFile(fullNewFilename);

            // Make sure the directory exists for the moved file...
            File newFileParent = newFile.getParentFile();
            if (!newFileParent.exists()) {
                newFileParent.mkdirs();
            }

            // And rename the file.
            retVal = oldFile.renameTo(newFile);

            if (readOnlyFlag) {
                newFile.setReadOnly();
            }
        }
        return retVal;
    }
}
