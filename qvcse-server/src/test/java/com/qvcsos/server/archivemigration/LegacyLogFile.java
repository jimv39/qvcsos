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

import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkFile;
import com.qvcsos.server.archivemigration.LegacyLogFileHeaderInfo;
import com.qvcsos.server.archivemigration.LegacyLogFileReadException;
import com.qvcsos.server.archivemigration.LegacyQVCSConstants;
import com.qvcsos.server.archivemigration.LegacyRevisionHeader;
import com.qvcsos.server.archivemigration.LegacyRevisionInformation;
import com.qvcsos.server.archivemigration.LegacyUtility;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Minimum implementation of old logfile.
 *
 * @author Jim Voris
 */
public class LegacyLogFile {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyLogFile.class);
    private RandomAccessFile inStream;
    private boolean isOpen;
    private boolean isHeaderInfoReadFlag;
    private boolean isRevisionInfoReadFlag;
    private LegacyRevisionInformation revisionInfo;
    private final String fullArchiveFileName;
    private final String shortArchiveName;
    private final java.io.File archiveFile;
    private final String tempFileName;
    private final java.io.File tempFile;
    private final String oldArchiveFileName;
    private final java.io.File oldArchiveFile;
    private final String shortWorkfileName;
    private LegacyLogFileHeaderInfo headerInfo;
    private LegacyAccessList accessList = null;
    private LegacyAccessList modifierList = null;
    private boolean mustReadArchiveFileFlag = true;
//    private LogfileInfo logfileInfo = null;

    LegacyLogFile(String fullArchiveFilename) {
        isOpen = false;
        isHeaderInfoReadFlag = false;
        isRevisionInfoReadFlag = false;

        // Set the filename
        fullArchiveFileName = fullArchiveFilename;
        archiveFile = new File(fullArchiveFileName);

        // Set the name of the temp file for the archive
        tempFileName = fullArchiveFileName + LegacyQVCSConstants.QVCS_ARCHIVE_TEMPFILE_SUFFIX;
        tempFile = new java.io.File(tempFileName);

        // Set the name of the old file for the archive
        oldArchiveFileName = fullArchiveFileName + LegacyQVCSConstants.QVCS_ARCHIVE_OLDFILE_SUFFIX;
        oldArchiveFile = new java.io.File(oldArchiveFileName);

        // Figure out the short workfile name
        shortWorkfileName = LegacyUtility.convertArchiveNameToShortWorkfileName(fullArchiveFilename);

        // Figure out the short archive name
        shortArchiveName = LegacyUtility.convertWorkfileNameToShortArchiveName(shortWorkfileName);
    }

    String getFileName() {
        return fullArchiveFileName;
    }

    String getShortWorkfileName() {
        return shortWorkfileName;
    }

    LegacyLogFileHeaderInfo getLogFileHeaderInfo() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }
        return headerInfo;
    }

    LegacyArchiveAttributes getAttributes() {
        if (mustReadArchiveFileFlag && !isArchiveInformationRead()) {
            readInformation();
        }

        return headerInfo.getLogFileHeader().attributes();
    }

    synchronized boolean open() {
        if (!isOpen) {
            // Make sure the file exists
            if (archiveFile.exists()) {
                try {
                    inStream = new RandomAccessFile(archiveFile, "r");
                    isOpen = true;
                } catch (FileNotFoundException e) {
                    isOpen = false;
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
        return isOpen;
    }

    boolean readInformation() {
        boolean bRetValue = true;
        try {
            if (open()) {
                bRetValue = readHeaderInformation();
                if (bRetValue) {
                    bRetValue = readRevisionInformation();
                }
            }
        } catch (Exception e) {
            bRetValue = false;
        } finally {
            close();
        }
        return bRetValue;
    }

    private boolean readHeaderInformation() {
        try {
            if (open() && !isHeaderInfoReadFlag) {
                headerInfo = new LegacyLogFileHeaderInfo();
                isHeaderInfoReadFlag = headerInfo.read(inStream);
                if (isHeaderInfoReadFlag) {
                    accessList = new LegacyAccessList(headerInfo.getAccessList());
                    modifierList = new LegacyAccessList(headerInfo.getModifierList());
                }
            }
        } catch (LegacyLogFileReadException e) {
            LOGGER.warn("Failed to read header for: [{}]", getFileName());
            LOGGER.warn(e.getLocalizedMessage(), e);
            isHeaderInfoReadFlag = false;
        }
        return isHeaderInfoReadFlag;
    }

    private boolean readRevisionInformation() {
        try {
            if (open() && !isHeaderInfoReadFlag) {
                headerInfo = new LegacyLogFileHeaderInfo();
                headerInfo.read(inStream);
            }
            if (isHeaderInfoReadFlag && !isRevisionInfoReadFlag) {
                revisionInfo = new LegacyRevisionInformation(headerInfo.getRevisionCount(),
                        new LegacyAccessList(headerInfo.getAccessList()),
                        new LegacyAccessList(headerInfo.getModifierList()));
                revisionInfo.read(inStream);
                isRevisionInfoReadFlag = true;
            }
        } catch (LegacyLogFileReadException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            isRevisionInfoReadFlag = false;
        }
        return isRevisionInfoReadFlag;
    }

    private void close() {
        if (isOpen) {
            try {
                inStream.close();
                isOpen = false;
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
    }

    synchronized boolean fetchRevision(LegacyRevisionHeader revisionHeader, String outputFilename, boolean recurseFlag, MutableByteArray processedBuffer) {
        boolean bRetVal = false;

        try {
            if ((revisionHeader.getDepth() == 0) && revisionHeader.isTip()) {
                // Read the archive file to retrieve the revision.
                byte[] unCompressedRevisionData;
                byte[] revisionData = new byte[revisionHeader.getRevisionSize()];
                inStream.seek(revisionHeader.getRevisionDataStartPosition());
                readRevisionData(revisionData);

                // Decompress the buffer if we need to.
                if (revisionHeader.isCompressed()) {
                    unCompressedRevisionData = deCompressRevisionData(revisionHeader, revisionData);
                } else {
                    unCompressedRevisionData = revisionData;
                }
                processedBuffer.setValue(unCompressedRevisionData);
                bRetVal = true;
            } else {
                // They are requesting an older revision.
                if (revisionHeader.getParentRevisionHeader() == null) {
                    // We've reached the tip revision for the requested revision.
                    // but we're recursing and this must be a non-tip revision.
                    // All non-tip revisions MUST have a parent, so to get here
                    // is a boo-boo.
                    throw new QVCSRuntimeException("Oops.");
                } else {
                    // We're still working our way to the tip revision.
                    bRetVal = fetchRevision(revisionHeader.getParentRevisionHeader(), outputFilename, true, processedBuffer);

                    // We got our parent.  Now apply our edits to our parent to get the result.
                    if (bRetVal) {
                        byte[] editBuffer = new byte[revisionHeader.getRevisionSize()];
                        byte[] uncompressedEditBuffer;
                        inStream.seek(revisionHeader.getRevisionDataStartPosition());
                        readRevisionData(editBuffer);
                        if (revisionHeader.isCompressed()) {
                            uncompressedEditBuffer = deCompressRevisionData(revisionHeader, editBuffer);
                        } else {
                            uncompressedEditBuffer = editBuffer;
                        }
                        byte[] afterEdits = applyEdits(uncompressedEditBuffer, processedBuffer.getValue());
                        processedBuffer.setValue(afterEdits);
                        bRetVal = true;
                    }
                }
            }
        } catch (IOException | QVCSException e) {
            LOGGER.warn("Failed to fetch revision: [{}]", revisionHeader.getRevisionString());
            LOGGER.warn(e.getLocalizedMessage(), e);
            bRetVal = false;
        }

        // Write the result.
        if (!recurseFlag && bRetVal && (outputFilename != null)) {
            try {
                WorkFile outputFile = new WorkFile(outputFilename);

                // Overwrite if it is write protected.
                if (outputFile.exists()) {
                    if (!outputFile.canWrite()) {
                        outputFile.setReadWrite();
                    }
                }
                try (FileOutputStream outStream = new FileOutputStream(outputFile)) {
                    Utility.writeDataToStream(processedBuffer.getValue(), outStream);
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                bRetVal = false;
            }
        }
        return bRetVal;
    }

    synchronized boolean findRevision(String revision, AtomicInteger revisionIndex) {

        boolean bRetVal = false;
        int revisionCount = headerInfo.getRevisionCount();
        for (int i = 0; i < revisionCount; i++) {
            LegacyRevisionHeader revHeader = revisionInfo.getRevisionHeader(i);
            String revisionString = revHeader.getRevisionString();
            if (revision.compareTo(revisionString) == 0) {
                bRetVal = true;
                revisionIndex.set(i);
                break;
            }
        }
        return bRetVal;
    }

    synchronized boolean isArchiveInformationRead() {
        return isHeaderInfoReadFlag && isRevisionInfoReadFlag;
    }

    int getRevisionCount() {
        int revisionCount = -1;
        try {
            if (!isArchiveInformationRead()) {
                readInformation();
            }
            revisionCount = headerInfo.getRevisionCount();
        } catch (NullPointerException e) {
            LOGGER.warn("Failed to get revision count for: [{}]", getFileName());
            throw e;
        }
        return revisionCount;
    }

    LegacyRevisionInformation getRevisionInformation() {
        if (!isArchiveInformationRead()) {
            readInformation();
        }
        return revisionInfo;
    }

    LegacyRevisionHeader getRevisionHeader(int index) {
        LegacyRevisionHeader revisionHeader = null;
        if (index < getRevisionCount()) {
            revisionHeader = revisionInfo.getRevisionHeader(index);
        }
        return revisionHeader;
    }

    byte[] getRevisionAsByteArray(String revisionString) {
        byte[] workfileBuffer = null;
        try {
            if (open()) {
                AtomicInteger revisionIndex = new AtomicInteger();
                if (findRevision(revisionString, revisionIndex)) {
                    LegacyRevisionHeader revInfo = getRevisionHeader(revisionIndex.get());
                    MutableByteArray processedBuffer = new MutableByteArray();
                    if (fetchRevision(revInfo, null, false, processedBuffer)) {
                        workfileBuffer = processedBuffer.getValue();
                    }
                }
            }
        } finally {
            close();
        }
        return workfileBuffer;
    }

    private void readRevisionData(byte[] revisionData) throws QVCSException {
        try {
            int offset = 0;

            while (true) {
                int bytesLeft = revisionData.length - offset;
                if (bytesLeft > LegacyQVCSConstants.BYTES_TO_XFER) {
                    int bytesToRead = LegacyQVCSConstants.BYTES_TO_XFER;
                    inStream.readFully(revisionData, offset, bytesToRead);
                    offset += bytesToRead;
                    continue;
                } else {
                    int bytesToRead = bytesLeft;
                    inStream.readFully(revisionData, offset, bytesToRead);
                    break;
                }
            }
        } catch (java.lang.OutOfMemoryError e) {
            // If they are trying to create an archive for a really big file,
            // we might have problems.
            LOGGER.warn("Out of memory trying to read revision data.");
            throw new QVCSException("Out of memory trying to read revision data.");
        } catch (IOException e) {
            LOGGER.warn("Caught exception: " + e.getClass().toString() + " " + e.getLocalizedMessage());
            throw new QVCSException("Exception in readRevisionData.\n" + Utility.expandStackTraceToString(e));
        }
    }

    private byte[] deCompressRevisionData(LegacyRevisionHeader revisionHeader, byte[] revisionData) {
        LegacyCompressor compressor = LegacyCompressionFactory.getCompressor(revisionHeader.getCompressionHeader());
        return compressor.expand(revisionHeader.getCompressionHeader(), revisionData);
    }

    private byte[] applyEdits(byte[] edits, byte[] originalData) throws QVCSException {
        DataInputStream editStream = new DataInputStream(new ByteArrayInputStream(edits));
        LegacyCompareFilesEditInformation editInfo = new LegacyCompareFilesEditInformation();
        byte[] editedBuffer = new byte[edits.length + originalData.length]; // It can't be any bigger than this.
        byte[] returnedBuffer = null;
        int inIndex = 0;
        int outIndex = 0;
        int deletedBytesCount;
        int insertedBytesCount;
        int bytesTillChange = 0;

        try {
            while (editStream.available() > 0) {
                editInfo.read(editStream);
                bytesTillChange = (int) editInfo.getSeekPosition() - inIndex;
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, bytesTillChange);

                inIndex += bytesTillChange;
                outIndex += bytesTillChange;

                deletedBytesCount = (int) editInfo.getDeletedBytesCount();
                insertedBytesCount = (int) editInfo.getInsertedBytesCount();

                switch (editInfo.getEditType()) {
                    case LegacyCompareFilesEditInformation.QVCS_EDIT_DELETE:
                        /*
                         * Delete input
                         */
                        // Just skip over deleted bytes
                        inIndex += deletedBytesCount;
                        break;

                    case LegacyCompareFilesEditInformation.QVCS_EDIT_INSERT:
                        /*
                         * Insert edit lines
                         */
                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    case LegacyCompareFilesEditInformation.QVCS_EDIT_REPLACE:
                        /*
                         * Replace input line with edit line.
                         * First skip over the bytes to be replaced, then copy the replacing bytes from the edit file to the output
                         * file.
                         */
                        inIndex += deletedBytesCount;

                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    default:
                        continue;
                }
            }

            // Copy the rest of the input "file" to the output "file".
            int remainingBytes = originalData.length - inIndex;
            if (remainingBytes > 0) {
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, remainingBytes);
                outIndex += remainingBytes;
            }
            returnedBuffer = new byte[outIndex];
            System.arraycopy(editedBuffer, 0, returnedBuffer, 0, outIndex);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            LOGGER.warn(" editInfo.seekPosition: " + editInfo.getSeekPosition() + " originalData.length: " + originalData.length + " inIndex: "
                    + inIndex + " editedBuffer.length: "
                    + editedBuffer.length + " outIndex: " + outIndex + " bytesTillChange: " + bytesTillChange);
            throw new QVCSException("Internal error!! for " + getShortWorkfileName());
        } finally {
            try {
                editStream.close();
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
        return returnedBuffer;
    }

}
