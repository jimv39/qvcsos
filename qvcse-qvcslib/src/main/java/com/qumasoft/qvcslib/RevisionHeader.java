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
package com.qumasoft.qvcslib;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Revision Header. Wrap the revision header that is stored for each revision within a QVCS archive file.
 * @author Jim Voris
 */
public final class RevisionHeader implements java.io.Serializable {
    private static final long serialVersionUID = -3233746302839526442L;
    /** Create our logger object */
    private static final Logger LOGGER = LoggerFactory.getLogger(RevisionHeader.class);

    // Revision header info that is stored in the archive file.
    private final AccessList accessList;
    private final AccessList modifierList;
    private RevisionDescriptor revisionDescriptor;
    private RevisionCompressionHeader compressionInformation;
    private String revisionDescription;
    private CommonShort descriptionSize = new CommonShort();
    private CommonShort creatorIndex = new CommonShort();
    private CommonShort lockerIndex = new CommonShort();
    private Common32Long revisionSize = new Common32Long();
    private CommonTime editDate = new CommonTime();
    private CommonTime checkInDate = new CommonTime();
    private byte newLineCharacter;
    private byte newLineFlag;
    private byte compressFlag;
    private byte isTipFlag;
    private CommonShort depth = new CommonShort();
    private CommonShort childCount = new CommonShort();
    private CommonShort lockFlag = new CommonShort();
    private CommonShort minorNumber = new CommonShort();
    private CommonShort majorNumber = new CommonShort();
    // Where in the archive to find this revision.
    private transient long revisionHeaderStartPosition;
    private transient long revisionDataStartPosition;
    private transient int revisionIndex;
    private transient RevisionHeader parentRevisionHeader = null;
    private static final long MILLI_SECONDS_PER_SECOND = 1000L;

    /**
     * Create a revision header.
     * @param accessLst the access list.
     * @param modifierLst the modifier list.
     */
    public RevisionHeader(AccessList accessLst, AccessList modifierLst) {
        revisionHeaderStartPosition = -1;
        revisionDataStartPosition = -1;
        this.accessList = accessLst;
        this.modifierList = modifierLst;
    }

    /**
     * A copy constructor.
     * @param copyThisRevisionHeader the revision header to copy.
     */
    public RevisionHeader(RevisionHeader copyThisRevisionHeader) {
        this.accessList = new AccessList(copyThisRevisionHeader.accessList.getAccessListAsCommaSeparatedString());
        this.modifierList = new AccessList(copyThisRevisionHeader.modifierList.getAccessListAsCommaSeparatedString());
        this.revisionDescriptor = new RevisionDescriptor(copyThisRevisionHeader.revisionDescriptor);
        this.compressFlag = copyThisRevisionHeader.compressFlag;
        if (compressFlag != 0) {
            this.compressionInformation = new RevisionCompressionHeader(copyThisRevisionHeader.compressionInformation);
        } else {
            this.compressionInformation = null;
        }
        this.revisionDescription = copyThisRevisionHeader.revisionDescription;
        this.descriptionSize = new CommonShort(copyThisRevisionHeader.descriptionSize.getValue());
        this.creatorIndex = new CommonShort(copyThisRevisionHeader.creatorIndex.getValue());
        this.lockerIndex = new CommonShort(copyThisRevisionHeader.lockerIndex.getValue());
        this.revisionSize = new Common32Long(copyThisRevisionHeader.revisionSize.getValue());
        this.editDate = new CommonTime((int) copyThisRevisionHeader.editDate.getValue());
        this.checkInDate = new CommonTime((int) copyThisRevisionHeader.checkInDate.getValue());
        this.newLineCharacter = copyThisRevisionHeader.newLineCharacter;
        this.newLineFlag = copyThisRevisionHeader.newLineFlag;
        this.isTipFlag = copyThisRevisionHeader.isTipFlag;
        this.depth = new CommonShort(copyThisRevisionHeader.depth.getValue());
        this.childCount = new CommonShort(copyThisRevisionHeader.childCount.getValue());
        this.lockFlag = new CommonShort(copyThisRevisionHeader.lockFlag.getValue());
        this.minorNumber = new CommonShort(copyThisRevisionHeader.minorNumber.getValue());
        this.majorNumber = new CommonShort(copyThisRevisionHeader.majorNumber.getValue());
        this.revisionHeaderStartPosition = copyThisRevisionHeader.revisionHeaderStartPosition;
        this.revisionDataStartPosition = copyThisRevisionHeader.revisionDataStartPosition;
        this.revisionIndex = copyThisRevisionHeader.revisionIndex;
        this.parentRevisionHeader = null;
    }

    /**
     * Convenience implementation of toString.
     * @return convenient String representation of this revision header.
     */
    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();

        // Report the revision
        returnString.append("Revision ");
        returnString.append(revisionDescriptor);
        returnString.append(" created by ");
        returnString.append(modifierList.indexToUser(creatorIndex.getValue()));
        Date localEditDate = new Date(MILLI_SECONDS_PER_SECOND * this.editDate.getValue());
        returnString.append("\nLast File edit: ");
        returnString.append(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(localEditDate));
        Date localCheckInDate = new Date(MILLI_SECONDS_PER_SECOND * this.checkInDate.getValue());
        returnString.append("\nCheck-in date: ");
        returnString.append(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(localCheckInDate));
        if (compressFlag != 0) {
            returnString.append("\nRevision storage compressed from: ");
            returnString.append(compressionInformation.getInputSize());
            returnString.append(" bytes to: ");
            returnString.append(compressionInformation.getCompressedSize());
            returnString.append(" bytes\n");
        } else {
            returnString.append("\nRevision storage requires: ");
            returnString.append(revisionSize.getValue());
            returnString.append(" bytes\n");
        }
        returnString.append("Revision description:\n");
        returnString.append(revisionDescription).append("\n");

        return returnString.toString();
    }

    /**
     * Get the revision string.
     * @return the revision string.
     */
    public String getRevisionString() {
        return revisionDescriptor.toString();
    }

    /**
     * Get the revision size. The is the number of bytes it takes to store this revision on disk.
     * @return the revision size. The is the number of bytes it takes to store this revision on disk.
     */
    public int getRevisionSize() {
        return revisionSize.getValue();
    }

    /**
     * Set the revision size.
     * @param size the revision size.
     */
    public void setRevisionSize(int size) {
        revisionSize.setValue(size);
    }

    /**
     * Get the 'depth' of this revision. A depth of 0 means this is a trunk revision; a depth of 1, means it's a branch revision; 2 means it a deeper branch, etc.
     * @return the 'depth' of this revision.
     */
    public int getDepth() {
        return depth.getValue();
    }

    /**
     * Set the 'depth' of this revision.
     * @param inDepth the 'depth' of this revision.
     */
    public void setDepth(int inDepth) {
        this.depth.setValue(inDepth);
    }

    /**
     * Update only those portions of the revision header that are invariant in size. This method cannot be used to change the revision description since that could
     * change the size of the revision header stuff. This method is meant to be used to lock/unlock a revision. It could also be used to change the revision author; that's all.
     * @param outStream the stream to write the update to.
     * @return true if the update was successful.
     */
    public boolean updateInPlace(RandomAccessFile outStream) {
        boolean retVal;
        try {
            outStream.seek(revisionHeaderStartPosition);
            retVal = writeConstantLengthPortion(outStream);
        } catch (IOException e) {
            retVal = false;
        }

        return retVal;
    }

    /**
     * Write this revision header to the the output stream. This method assumes that the output stream is positioned correctly.
     * @param outStream the stream to write to.
     * @return true if the write was successful; false otherwise.
     */
    public boolean write(RandomAccessFile outStream) {
        boolean retVal;
        try {
            retVal = writeConstantLengthPortion(outStream);
            if (retVal) {
                // Write the revision description
                if (descriptionSize.getValue() > 0) {
                    outStream.write(revisionDescription.getBytes());
                    outStream.writeByte(0);
                }
            }
        } catch (IOException e) {
            retVal = false;
        }

        return retVal;
    }

    /**
     * Read the revision header from disk.
     * @param inStream the stream to read from.
     * @param majorMinorArray array of revision pairs used to figure out the revision descriptor for this revision.
     * @return true if the read was successful; false otherwise.
     */
    public boolean read(RandomAccessFile inStream, MajorMinorRevisionPair[] majorMinorArray) {
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
            majorMinorArray[depth.getValue()] = new MajorMinorRevisionPair(majorNumber.getValue(), minorNumber.getValue());
            revisionDescriptor = new RevisionDescriptor(depth.getValue() + 1, majorMinorArray);

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
                compressionInformation = new RevisionCompressionHeader();
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

    /**
     * Write the revision header.
     */
    private boolean writeConstantLengthPortion(RandomAccessFile outStream) {
        boolean retVal = true;
        try {
            majorNumber.write(outStream);
            minorNumber.write(outStream);
            lockFlag.write(outStream);
            childCount.write(outStream);
            depth.write(outStream);
            outStream.writeByte(isTipFlag);
            outStream.writeByte(compressFlag);
            outStream.writeByte(newLineFlag);
            outStream.writeByte(newLineCharacter);
            checkInDate.write(outStream);
            editDate.write(outStream);
            revisionSize.write(outStream);
            lockerIndex.write(outStream);
            creatorIndex.write(outStream);
            descriptionSize.write(outStream);
        } catch (IOException e) {
            retVal = false;
        }

        return retVal;
    }

    /**
     * Set the parent revision header.
     * @param parent the parent revision header.
     */
    public void setParentRevisionHeader(RevisionHeader parent) {
        parentRevisionHeader = parent;
    }

    /**
     * Get the parent revision header.
     * @return the parent revision header.
     */
    public RevisionHeader getParentRevisionHeader() {
        return parentRevisionHeader;
    }

    /**
     * Get the revision start position. This is the seek position for the start of this revision header within the archive file.
     * @return the revision start position.
     */
    public long getRevisionStartPosition() {
        return revisionHeaderStartPosition;
    }

    /**
     * Get the revision data start position. This is the seek position for the start of the content data associated with this revision header... i.e. that that that is the
     * raw bytes of the file under version control, or the series of deltas that represent the edit script that can be used to reconstitute the given revision.
     * @return the revision data start position.
     */
    public long getRevisionDataStartPosition() {
        return revisionDataStartPosition;
    }

    /**
     * Get the revision compression header. Meaningful only if the revision is compressed.
     * @return the revision compression header.
     */
    public RevisionCompressionHeader getCompressionHeader() {
        return compressionInformation;
    }

    /**
     * Flag to indicate if this is a tip revision.
     * @return true if this is a tip revision; false otherwise.
     */
    public boolean isTip() {
        boolean isTip = false;
        if (isTipFlag != 0) {
            isTip = true;
        }
        return isTip;
    }

    /**
     * Set the 'is tip' flag.
     * @param flag the 'is tip' flag.
     */
    public void setIsTip(boolean flag) {
        if (flag) {
            isTipFlag = 1;
        } else {
            isTipFlag = 0;
        }
    }

    /**
     * Flag to indicate if the revision is compressed.
     * @return true if this revision is compressed; false otherwise.
     */
    public boolean isCompressed() {
        boolean isCompressed = false;
        if (compressFlag != 0) {
            isCompressed = true;
        }
        return isCompressed;
    }

    /**
     * Set the 'is compressed' flag.
     * @param flag the 'is compressed' flag.
     */
    public void setIsCompressed(boolean flag) {
        if (flag) {
            compressFlag = 1;
        } else {
            compressFlag = 0;
        }
    }

    /**
     * Flag to indicate if the revision is locked.
     * @return true if the revision is locked; false otherwise.
     */
    public boolean isLocked() {
        boolean isLocked = false;
        if (lockFlag.getValue() != 0) {
            isLocked = true;
        }
        return isLocked;
    }

    /**
     * Set the 'is locked' flag.
     * @param flag the 'is locked' flag.
     */
    public void setIsLocked(boolean flag) {
        if (flag) {
            lockFlag.setValue(1);
        } else {
            lockFlag.setValue(0);
        }
    }

    /**
     * Get the locker index. This is the index within the modifier list of the user holding the lock on this revision.
     * @return the locker index.
     */
    public int getLockerIndex() {
        return lockerIndex.getValue();
    }

    /**
     * Set the locker index.
     * @param index the locker index.
     */
    public void setLockerIndex(int index) {
        this.lockerIndex.setValue(index);
    }

    /**
     * Set the locker.
     * @param locker the user name of the locker.
     * @throws QVCSException if the locker is not a valid user.
     */
    public void setLocker(String locker) throws QVCSException {
        int localLockerIndex = modifierList.userToIndex(locker);
        if (localLockerIndex < 0) {
            throw new QVCSException("Invalid revision locker: " + locker);
        } else {
            setLockerIndex(localLockerIndex);
        }
    }

    /**
     * Get the creator index. This is the index within the modifier list of the user who created this revision.
     * @return the creator index.
     */
    public int getCreatorIndex() {
        return creatorIndex.getValue();
    }

    /**
     * Get the number of branch revisions... i.e. the number of branches that are anchored by this revision.
     * @return the number of branch revisions anchored by this revision.
     */
    public int getChildCount() {
        return childCount.getValue();
    }

    /**
     * Increment the child count.
     */
    public void incrementChildCount() {
        childCount.setValue(childCount.getValue() + 1);
    }

    /**
     * Decrement the child count.
     */
    public void decrementChildCount() {
        if (childCount.getValue() > 0) {
            childCount.setValue(childCount.getValue() - 1);
        }
    }

    /**
     * Set the creator.
     * @param creator the QVCS user name of the creator of this revision.
     * @throws QVCSException if the creator is not a valid QVCS user name.
     */
    public void setCreator(String creator) throws QVCSException {
        int localCreatorIndex = modifierList.userToIndex(creator);
        if (localCreatorIndex < 0) {
            throw new QVCSException("Invalid revision creator: " + creator);
        } else {
            this.creatorIndex.setValue(localCreatorIndex);
        }
    }

    /**
     * Get the checkin data for this revision.
     * @return the checkin data for this revision.
     */
    public java.util.Date getCheckInDate() {
        return new java.util.Date(MILLI_SECONDS_PER_SECOND * checkInDate.getValue());
    }

    /**
     * Set the checkin data for this revision.
     * @param time the checkin data for this revision.
     */
    public void setCheckInDate(java.util.Date time) {
        int dateTime = (int) (time.getTime() / MILLI_SECONDS_PER_SECOND);
        checkInDate.setValue(dateTime);
    }

    /**
     * Get the edit date for this revision. This is the last edit time of the workfile used to create this revision.
     * @return the edit date for this revision.
     */
    public java.util.Date getEditDate() {
        return new java.util.Date(MILLI_SECONDS_PER_SECOND * editDate.getValue());
    }

    /**
     * Set the edit date for this revision.
     * @param time the edit date for this revision.
     */
    public void setEditDate(java.util.Date time) {
        int dateTime = (int) (time.getTime() / MILLI_SECONDS_PER_SECOND);
        editDate.setValue(dateTime);
    }

    /**
     * Get this revision's RevisionDescriptor.
     * @return this revision's RevisionDescriptor.
     */
    public RevisionDescriptor getRevisionDescriptor() {
        return revisionDescriptor;
    }

    /**
     * Set this revision's RevisionDescriptor.
     * @param revDescriptor this revision's RevisionDescriptor.
     */
    public void setRevisionDescriptor(RevisionDescriptor revDescriptor) {
        this.revisionDescriptor = revDescriptor;
    }

    /**
     * Get the revision description (i.e. the checkin comment).
     * @return the revision description (i.e. the checkin comment).
     */
    public String getRevisionDescription() {
        return revisionDescription;
    }

    /**
     * Set the revision description (i.e. the checkin comment).
     * @param description the revision description (i.e. the checkin comment).
     */
    public void setRevisionDescription(String description) {
        revisionDescription = description;
        descriptionSize.setValue(1 + description.getBytes().length);
    }

    /**
     * Get the revision's major revision number.
     * @return the revision's major revision number.
     */
    public int getMajorNumber() {
        return majorNumber.getValue();
    }

    /**
     * Set the revision's major revision number.
     * @param number the revision's major revision number.
     */
    public void setMajorNumber(int number) {
        majorNumber.setValue(number);
    }

    /**
     * Get the revision's minor revision number.
     * @return the revision's minor revision number.
     */
    public int getMinorNumber() {
        return minorNumber.getValue();
    }

    /**
     * Set the revision's minor revision number.
     * @param number the revision's minor revision number.
     */
    public void setMinorNumber(int number) {
        minorNumber.setValue(number);
    }

    /**
     * Get the revision's revision index... i.e. its position within the QVCS archive file. An index of 0 is the newest revision.
     * @return the revison's revision index.
     */
    public int getRevisionIndex() {
        return this.revisionIndex;
    }

    /**
     * Set the revision's revision index.
     * @param index the revision's revision index.
     */
    public void setRevisionIndex(int index) {
        this.revisionIndex = index;
    }
}
