/*
 * Copyright 2021 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qvcsos.server.archivemigration;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Jim Voris.
 */
public class LegacyLogFileHeaderInfo {
    private LegacyRevisionDescriptor revisionDescriptor = null;
    private LegacyLogFileHeader logFileHeader = null;
    private LegacyLabelInfo[] labelInfoArray = null;
    private String accessList = null;
    private String modifierList = null;
    private String commentPrefix = null;
    private String owner = null;
    private String moduleDescription = null;
    private LegacySupplementalHeaderInfo supplementalInfo = null;
    private transient boolean sizeChanged = false;

    public LegacyLogFileHeaderInfo() {
        logFileHeader = new LegacyLogFileHeader();
    }

    boolean read(RandomAccessFile inStream) throws LegacyLogFileReadException {
        boolean returnValue = true;
        try {
            long bytesAvailable = inStream.length();
            if (bytesAvailable > 0) {
                returnValue = logFileHeader.read(inStream);
                if (returnValue) {
                    // Read the variable length information.
                    returnValue = readVariableInfo(inStream);
                }
            }
        } catch (FileNotFoundException notFound) {
            returnValue = false;
        } catch (IOException ioProblem) {
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Return the access list as a String.
     *
     * @return a String representation of the access list (comma separated user
     * names).
     */
    public String getAccessList() {
        return accessList;
    }

    public String getModifierList() {
        return modifierList;
    }

    public int getRevisionCount() {
        return logFileHeader.revisionCount();
    }

    public LegacyLogFileHeader getLogFileHeader() {
        return logFileHeader;
    }

    private boolean readVariableInfo(RandomAccessFile inStream) throws IOException {
        boolean returnValue = true;

        // Read in the default branch information
        if (logFileHeader.defaultDepth() > 0) {
            revisionDescriptor = new LegacyRevisionDescriptor(logFileHeader.defaultDepth());
            revisionDescriptor.read(inStream);
        }

        // Read in the access list
        if (logFileHeader.accessSize() > 0) {
            byte[] accessLst = new byte[logFileHeader.accessSize()];
            int bytesRead = inStream.read(accessLst);
            accessList = new String(accessLst, 0, accessLst.length - 1);
        }

        // Read in the modifier list
        if (logFileHeader.modifierSize() > 0) {
            byte[] modifierLst = new byte[logFileHeader.modifierSize()];
            int bytesRead = inStream.read(modifierLst);
            modifierList = new String(modifierLst, 0, modifierLst.length - 1);
        }

        // Read in the comment prefix
        if (logFileHeader.commentSize() > 0) {
            byte[] commentPfx = new byte[logFileHeader.commentSize()];
            int bytesRead = inStream.read(commentPfx);
            commentPrefix = new String(commentPfx, 0, commentPfx.length - 1);
        }

        // Read in the owner
        if (logFileHeader.ownerSize() > 0) {
            byte[] ownr = new byte[logFileHeader.ownerSize()];
            int bytesRead = inStream.read(ownr);
            owner = new String(ownr, 0, ownr.length - 1);
        }

        // Read in the Module description
        if (logFileHeader.descriptionSize() > 0) {
            byte[] moduleDesc = new byte[logFileHeader.descriptionSize()];
            int bytesRead = inStream.read(moduleDesc);
            moduleDescription = new String(moduleDesc, 0, moduleDesc.length - 1);
        }

        // Read in the supplemental information
        if (logFileHeader.supplementalInfoSize() > 0) {
            supplementalInfo = new LegacySupplementalHeaderInfo(logFileHeader.supplementalInfoSize());
            supplementalInfo.read(inStream);

            // For really old QVCS/QVCS-Pro archives, we may need to adjust the size
            // of the supplemental info area.
            if (logFileHeader.supplementalInfoSize() != supplementalInfo.getSize()) {
                logFileHeader.getSupplementalInfoSize().setValue(supplementalInfo.getSize());
            }
        }

        // Read in any label information
        if (logFileHeader.versionCount() > 0) {
            labelInfoArray = new LegacyLabelInfo[logFileHeader.versionCount()];

            for (int i = 0; i < logFileHeader.versionCount(); i++) {
                labelInfoArray[i] = new LegacyLabelInfo();
                labelInfoArray[i].read(inStream);
            }
        }

        return returnValue;
    }

}
