/*
 * Copyright 2021 jimv.
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

import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jimv
 */
public class LegacyRevisionHeader {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyRevisionHeader.class);

    // Revision header info that is stored in the archive file.
    private final LegacyAccessList accessList;
    private final LegacyAccessList modifierList;
    private LegacyRevisionDescriptor revisionDescriptor;
    private LegacyRevisionCompressionHeader compressionInformation;
    private String revisionDescription;
    private LegacyCommonShort descriptionSize = new LegacyCommonShort();
    private LegacyCommonShort creatorIndex = new LegacyCommonShort();
    private LegacyCommonShort lockerIndex = new LegacyCommonShort();
    private LegacyCommon32Long revisionSize = new LegacyCommon32Long();
    private LegacyCommonTime editDate = new LegacyCommonTime();
    private LegacyCommonTime checkInDate = new LegacyCommonTime();
    private byte newLineCharacter;
    private byte newLineFlag;
    private byte compressFlag;
    private byte isTipFlag;
    private LegacyCommonShort depth = new LegacyCommonShort();
    private LegacyCommonShort childCount = new LegacyCommonShort();
    private LegacyCommonShort lockFlag = new LegacyCommonShort();
    private LegacyCommonShort minorNumber = new LegacyCommonShort();
    private LegacyCommonShort majorNumber = new LegacyCommonShort();
    // Where in the archive to find this revision.
    private transient long revisionHeaderStartPosition;
    private transient long revisionDataStartPosition;
    private transient int revisionIndex;
    private transient LegacyRevisionHeader parentRevisionHeader = null;
    private static final long MILLI_SECONDS_PER_SECOND = 1000L;

    /**
     * Create a revision header.
     *
     * @param accessLst the access list.
     * @param modifierLst the modifier list.
     */
    public LegacyRevisionHeader(LegacyAccessList accessLst, LegacyAccessList modifierLst) {
        revisionHeaderStartPosition = -1;
        revisionDataStartPosition = -1;
        this.accessList = accessLst;
        this.modifierList = modifierLst;
    }

    public int getDepth() {
        return depth.getValue();
    }

    /**
     * Read the revision header from disk.
     *
     * @param inStream the stream to read from.
     * @param majorMinorArray array of revision pairs used to figure out the
     * revision descriptor for this revision.
     * @return true if the read was successful; false otherwise.
     */
    public boolean read(RandomAccessFile inStream, LegacyMajorMinorRevisionPair[] majorMinorArray) {
        boolean returnValue = true;
        try {
            // Save where this revision starts.
            revisionHeaderStartPosition = inStream.getFilePointer();

            majorNumber.read(inStream);
            minorNumber.read(inStream);
            lockFlag.read(inStream);
            childCount.read(inStream);

            // Get the information we need to figure out
            // the revision descriptor for this revision
            depth.read(inStream);
            majorMinorArray[depth.getValue()] = new LegacyMajorMinorRevisionPair(majorNumber.getValue(), minorNumber.getValue());
            revisionDescriptor = new LegacyRevisionDescriptor(depth.getValue() + 1, majorMinorArray);

            isTipFlag = inStream.readByte();
            compressFlag = inStream.readByte();
            newLineFlag = inStream.readByte();
            newLineCharacter = inStream.readByte();
            checkInDate.read(inStream);
            editDate.read(inStream);
            revisionSize.read(inStream);
            lockerIndex.read(inStream);
            creatorIndex.read(inStream);
            descriptionSize.read(inStream);

            // Read the revision description
            if (descriptionSize.getValue() > 0) {
                byte[] localRevisionDescription = new byte[descriptionSize.getValue()];
                int bytesRead = inStream.read(localRevisionDescription);
                this.revisionDescription = new String(localRevisionDescription, 0, localRevisionDescription.length - 1);
            }

            // If this revision is compressed, read in the compression information
            // and reposition the archive back to the start of the revision.
            if (compressFlag != 0) {
                compressionInformation = new LegacyRevisionCompressionHeader();
                if (revisionSize.getValue() > 0) {
                    long currentFilePosition = inStream.getFilePointer();
                    compressionInformation.read(inStream);

                    // Seek back to the start of this revision
                    inStream.seek(currentFilePosition);
                }
            }

            // Save the location where the data of the revision starts.
            revisionDataStartPosition = inStream.getFilePointer();

            // Skip forward to the next revision.
            inStream.skipBytes(revisionSize.getValue());
        } catch (IOException ioProblem) {
            LOGGER.warn(ioProblem.getLocalizedMessage(), ioProblem);
            returnValue = false;
        }
        return returnValue;
    }

    public LegacyRevisionDescriptor getRevisionDescriptor() {
        return revisionDescriptor;
    }

    public int getMajorNumber() {
        return majorNumber.getValue();
    }

    public int getMinorNumber() {
        return minorNumber.getValue();
    }

    public void setRevisionIndex(int index) {
        this.revisionIndex = index;
    }

    public void setParentRevisionHeader(LegacyRevisionHeader parent) {
        parentRevisionHeader = parent;
    }

    public LegacyRevisionHeader getParentRevisionHeader() {
        return parentRevisionHeader;
    }

    public String getRevisionString() {
        return revisionDescriptor.toString();
    }

    public int getRevisionSize() {
        return revisionSize.getValue();
    }

    public long getRevisionDataStartPosition() {
        return revisionDataStartPosition;
    }

    public LegacyRevisionCompressionHeader getCompressionHeader() {
        return compressionInformation;
    }

    public int getCreatorIndex() {
        return creatorIndex.getValue();
    }

    public java.util.Date getCheckInDate() {
        return new java.util.Date(MILLI_SECONDS_PER_SECOND * checkInDate.getValue());
    }

    public java.util.Date getEditDate() {
        return new java.util.Date(MILLI_SECONDS_PER_SECOND * editDate.getValue());
    }

    public String getRevisionDescription() {
        return revisionDescription;
    }

    public boolean isCompressed() {
        boolean isCompressed = false;
        if (compressFlag != 0) {
            isCompressed = true;
        }
        return isCompressed;
    }

    public boolean isTip() {
        boolean isTip = false;
        if (isTipFlag != 0) {
            isTip = true;
        }
        return isTip;
    }

}
