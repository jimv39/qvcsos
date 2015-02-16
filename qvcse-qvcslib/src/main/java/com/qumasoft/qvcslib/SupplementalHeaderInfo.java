//   Copyright 2004-2015 Jim Voris
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

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Supplemental header info. This is meant to hold variable amounts of header information in a less structured format than the header elements that have specific header
 * data elements. This is kind of a kludge, a kind of poor man's property collection, but it seemed a good idea at the time.
 * @author Jim Voris
 */
public class SupplementalHeaderInfo implements java.io.Serializable {
    // Create our logger object

    private static final Logger LOGGER = LoggerFactory.getLogger(SupplementalHeaderInfo.class);
    private static final long serialVersionUID = 141L;
    private static final long MILLI_SECONDS_PER_SECOND = 1000L;
    // This is the only thing that gets serialized.
    private byte[] supplementalInfo;
    private transient String workfileCheckedOutToLocation = "";
    private transient int lastModifierIndex = -1;
    private transient Date lastArchiveUpdateDate = null;
    private transient long lastWorkfileSize = -1;
    private transient int fileID = -1;
    private transient boolean valuesExtractedFromBufferFlag = false;
    private final transient byte[] markerString = {'Q', 'V', 'C', 'S', 'F', 'I', 'L', 'E', 'I', 'D', ':'};

    /**
     * Creates a new instance of SupplementalHeaderInfo.
     */
    public SupplementalHeaderInfo() {
        supplementalInfo = new byte[QVCSConstants.QVCS_SUPPLEMENTAL_SIZE];
        lastArchiveUpdateDate = new Date();
    }

    /**
     * Create a new instance with the given buffer size.
     * @param bufferSize define how big the buffer should be.
     */
    public SupplementalHeaderInfo(int bufferSize) {
        supplementalInfo = new byte[bufferSize];
    }

    /**
     * A sort of copy constructor where we create a new SupplementalHeaderInfo object using the buffer we pass in.
     * @param supplementInfo the existing supplementalInfo buffer.
     */
    public SupplementalHeaderInfo(final byte[] supplementInfo) {
        supplementalInfo = new byte[supplementInfo.length];
        System.arraycopy(supplementalInfo, 0, supplementInfo, 0, supplementInfo.length);
        extractValuesFromBuffer();
    }

    /**
     * Get the workfile 'checked out to' location.
     * @return the workfile 'checked out to' location.
     */
    public String getWorkfileCheckedOutToLocation() {
        extractValuesFromBuffer();
        return workfileCheckedOutToLocation;
    }

    /**
     * Set the workfile 'checked out to' location.
     * @param checkedOutToLocation the workfile 'checked out to' location.
     */
    public void setWorkfileCheckedOutToLocation(String checkedOutToLocation) {
        workfileCheckedOutToLocation = checkedOutToLocation;
        saveChangesToBuffer();
    }

    /**
     * Get the last modifier index.
     * @return the last modifier index.
     */
    public int getLastModifierIndex() {
        extractValuesFromBuffer();
        return lastModifierIndex;
    }

    /**
     * Set the last modifier index.
     * @param modifierIndex the last modifier index.
     */
    public void setLastModifierIndex(int modifierIndex) {
        lastModifierIndex = modifierIndex;
        saveChangesToBuffer();
    }

    /**
     * Get the last archive update date.
     * @return the last archive update date.
     */
    public Date getLastArchiveUpdateDate() {
        extractValuesFromBuffer();
        return lastArchiveUpdateDate;
    }

    /**
     * Set the last archive update date.
     * @param date the last archive update date.
     */
    public void setLastArchiveUpdateDate(Date date) {
        lastArchiveUpdateDate = date;
        saveChangesToBuffer();
    }

    /**
     * Get the last workfile size.
     * @return the last workfile size.
     */
    public long getLastWorkfileSize() {
        extractValuesFromBuffer();
        return lastWorkfileSize;
    }

    /**
     * Set the last workfile size.
     * @param workfileSize the last workfile size.
     */
    public void setLastWorkfileSize(long workfileSize) {
        lastWorkfileSize = workfileSize;
        saveChangesToBuffer();
    }

    /**
     * Get the file id.
     * @return the file id.
     */
    public int getFileID() {
        extractValuesFromBuffer();
        return fileID;
    }

    /**
     * Set the file id.
     * @param fileId the file id.
     */
    public void setFileID(int fileId) {
        fileID = fileId;
        saveChangesToBuffer();
    }

    /**
     * Get the size of the supplemental info buffer.
     * @return the size of the supplemental info buffer.
     */
    int getSize() {
        return supplementalInfo.length;
    }

    /**
     * Read the supplemental information from a file.
     * @param inStream the RandomAccessFile to read from.
     * @throws IOException if there are problems reading the stream.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        inStream.read(supplementalInfo);
        valuesExtractedFromBufferFlag = false;
        extractValuesFromBuffer();
    }

    /**
     * Write the supplemental information to a file.
     * @param outStream the RandomAccessFile to write.
     * @throws IOException if there are problems writing the stream.
     */
    public void write(RandomAccessFile outStream) throws IOException {
        outStream.write(supplementalInfo);
    }

    private void extractValuesFromBuffer() {
        if (!valuesExtractedFromBufferFlag) {
            // Make sure the buffer for supplemental info is the expected size.
            // (For really old QVCS/QVCS-Pro archives, it might be a non-standard
            // length);
            adjustBufferSize();

            // Extract the workfile name from the supplemental info.
            int workfileLength = 0;
            int i;
            for (i = 0; i < supplementalInfo.length; i++, workfileLength++) {
                if (supplementalInfo[i] == '\0') {
                    i++;
                    break;
                }
            }
            workfileCheckedOutToLocation = new String(supplementalInfo, 0, workfileLength);

            if (i < supplementalInfo.length) {
                // Extract the last modifier index from the supplemental info.
                int lastModifierLengthStartIndex = i;
                int lastModifierLength;
                for (lastModifierLength = 0; i < supplementalInfo.length; i++, lastModifierLength++) {
                    if (supplementalInfo[i] == '~') {
                        i++;
                        break;
                    }
                }

                try {
                    String lastModifierIndexString = new String(supplementalInfo, lastModifierLengthStartIndex, lastModifierLength);
                    lastModifierIndex = Integer.parseInt(lastModifierIndexString);
                } catch (NumberFormatException e) {
                    lastModifierIndex = -1;
                }

                // Extract the last update timestamp from the supplemental info.
                int lastUpdateTimestampStartIndex = i;
                int lastUpdateTimestampLength;
                for (lastUpdateTimestampLength = 0; i < supplementalInfo.length; i++, lastUpdateTimestampLength++) {
                    if (supplementalInfo[i] == '~') {
                        i++;
                        break;
                    }
                }

                try {
                    String lastUpdateTimestampString = new String(supplementalInfo, lastUpdateTimestampStartIndex, lastUpdateTimestampLength);
                    lastArchiveUpdateDate = new java.util.Date(Integer.parseInt(lastUpdateTimestampString) * MILLI_SECONDS_PER_SECOND);
                } catch (NumberFormatException e) {
                    lastArchiveUpdateDate = new Date();
                }

                // Extract the last workfile size
                int lastWorkfileSizeStartIndex = i;
                int lastWorkfileSizeLength;
                for (lastWorkfileSizeLength = 0; i < supplementalInfo.length; i++, lastWorkfileSizeLength++) {
                    if (supplementalInfo[i] == '\0') {
                        i++;
                        break;
                    }
                }

                try {
                    String lastWorkfileSizeString = new String(supplementalInfo, lastWorkfileSizeStartIndex, lastWorkfileSizeLength);
                    lastWorkfileSize = Integer.parseInt(lastWorkfileSizeString);
                } catch (NumberFormatException e) {
                    lastWorkfileSize = 0;
                }

                // Make sure the FileID marker string is present. We put this
                // in a try/catch so we can scan for file ID's without assigning
                // a new fileID. If scanning is in progress, the FileIDManager will
                // throw an exception if asked to supply a new fileID.
                try {
                    fileID = -1;
                    int fileIDMarkerStringStartIndex = i;
                    for (int j = 0; j < markerString.length; j++, i++) {
                        if (supplementalInfo[i] != markerString[j]) {
                            // Bad, or no marker string. We need to add the marker
                            // and a fileID to the supplemental info.
                            throw new QVCSException("No file ID found.");
                        }
                    }
                    i = fileIDMarkerStringStartIndex + markerString.length;
                    int fileIDValueStartIndex = i;
                    int fileIDValueLength;
                    for (fileIDValueLength = 0; i < supplementalInfo.length; i++, fileIDValueLength++) {
                        if (supplementalInfo[i] == '\0') {
                            i++;
                            break;
                        }
                    }
                    String fileIDString = new String(supplementalInfo, fileIDValueStartIndex, fileIDValueLength);
                    fileID = Integer.parseInt(fileIDString);
                } catch (QVCSException e) {
                    LOGGER.info("No fileID found.");
                }
            }

            valuesExtractedFromBufferFlag = true;
        }
    }

    private void saveChangesToBuffer() {
        // Start with an empty buffer.
        supplementalInfo = new byte[QVCSConstants.QVCS_SUPPLEMENTAL_SIZE];

        // Save the workfile name to the supplemental info buffer
        int i;
        byte[] workfileBuffer = workfileCheckedOutToLocation.getBytes();
        for (i = 0; i < workfileBuffer.length; i++) {
            supplementalInfo[i] = workfileBuffer[i];
        }
        supplementalInfo[i++] = '\0';

        // Save the last modifier index into the buffer.
        Integer lstModifierIndex = lastModifierIndex;
        byte[] lastModifierIndexBuffer = lstModifierIndex.toString().getBytes();
        for (int j = 0; j < lastModifierIndexBuffer.length; i++, j++) {
            supplementalInfo[i] = lastModifierIndexBuffer[j];
        }
        supplementalInfo[i++] = '~';

        // Prevent a null pointer exception.
        if (lastArchiveUpdateDate == null) {
            lastArchiveUpdateDate = new Date();
        }
        // Save the last update timestamp into the buffer.
        long lastUpdateTime = lastArchiveUpdateDate.getTime() / MILLI_SECONDS_PER_SECOND;
        Long lastUpdateTimeValue = lastUpdateTime;
        byte[] lastUpdateTimeBuffer = lastUpdateTimeValue.toString().getBytes();
        for (int j = 0; j < lastUpdateTimeBuffer.length; i++, j++) {
            supplementalInfo[i] = lastUpdateTimeBuffer[j];
        }
        supplementalInfo[i++] = '~';

        // Save the last workfile size into the buffer
        Long lstWorkfileSize = lastWorkfileSize;
        byte[] lastWorkfileSizeBuffer = lstWorkfileSize.toString().getBytes();
        for (int j = 0; j < lastWorkfileSizeBuffer.length; i++, j++) {
            supplementalInfo[i] = lastWorkfileSizeBuffer[j];
        }
        supplementalInfo[i++] = '\0';

        if (fileID != -1) {
            // Write the FileID marker string
            for (int j = 0; j < markerString.length; j++, i++) {
                supplementalInfo[i] = markerString[j];
            }

            Integer fileId = fileID;
            byte[] fileIDBuffer = fileId.toString().getBytes();
            for (int j = 0; j < fileIDBuffer.length; i++, j++) {
                supplementalInfo[i] = fileIDBuffer[j];
            }
            supplementalInfo[i++] = '\0';
        }
    }

    private void adjustBufferSize() {
        if (supplementalInfo.length != QVCSConstants.QVCS_SUPPLEMENTAL_SIZE) {
            byte[] newSupplementalInfo = new byte[QVCSConstants.QVCS_SUPPLEMENTAL_SIZE];
            System.arraycopy(supplementalInfo, 0, newSupplementalInfo, 0, supplementalInfo.length);
            supplementalInfo = newSupplementalInfo;
        }
    }
}
