/*   Copyright 2004-2014 Jim Voris
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
import java.io.Serializable;

/**
 * The constant size portion of the QVCS archive header. This appears at the start of every QVCS archive file. Most of the elements are represented using {@link CommonShort}
 * instances. This is for historical reasons for compatibility with the C++ version of QVCS, and to make it easy to support a platform neutral way of marshalling these
 * objects when communicating with a non-Java client. These days, we only have Java based clients, but haven't taken the time to re-factor this code to take advantage of that.
 * @author Jim Voris
 */
public final class LogFileHeader implements Serializable {
    private static final long serialVersionUID = 6195018363104659070L;
    private static final int QVCS_ARCHIVE_VERSION = 6;

    /** The QVCSVersion. This is defines the version of the structure of the archive file. It should be a 6. */
    private CommonShort iQVCSVersion = new CommonShort();  // QVCS Version
    /** The Major Number. This is the major number of the default revision. */
    private CommonShort iMajorNumber = new CommonShort();  // Major revision number of trunk
    /** The Minor Number. This is the minor number of the default revision. */
    private CommonShort iMinorNumber = new CommonShort();  // Minor revision number of trunk
    /** The QVCS archive attributes. */
    private CommonShort iAttributes = new CommonShort();  // Attributes for this log file
    /** The number of versions (a.k.a. labels) that have been applied to this archive */
    private CommonShort iVersionCount = new CommonShort();  // Number of version stamps
    /** The number of revisions in this archive file */
    private CommonShort iRevisionCount = new CommonShort();  // Number of revisions here
    /** The default depth of this archive. A depth of 0 implies that the default branch for this file is the trunk. */
    private CommonShort iDefaultDepth = new CommonShort();  // Depth of default branch (0 implies trunk)
    /** The number of revisions that are locked in this archive file. */
    private CommonShort iLockCount = new CommonShort();  // Number of locks on the logfile
    /** The number of bytes to reserve for the file's access list. */
    private CommonShort iAccessSize = new CommonShort();  // Strlen of Access list string including NULL
    /** The number of bytes to reserve for the file's modifier list. */
    private CommonShort iModifierSize = new CommonShort();  // Strlen of Modifier list string including NULL
    /** The number of bytes to reserve for this file's comment prefix. */
    private CommonShort iCommentSize = new CommonShort();  // Strlen of comment prefix string including NULL
    /** The number of bytes to reserve for the owner's user name. */
    private CommonShort iOwnerSize = new CommonShort();  // Strlen of Owner string including NULL
    /** The number of bytes to reserve for the module description. */
    private CommonShort iDescSize = new CommonShort();  // Strlen of Module desc including NULL
    /** The number of bytes to reserve for the supplemental information section. */
    private CommonShort iSupplementalInfoSize = new CommonShort();  // Length of the supplemental info section
    /** The header checksum. */
    private CommonShort iCheckSum = new CommonShort();  // Header checksum word
    /** The archive attributes. Note that this member variable is not serialized to disk, as it is an alternate representation of the iAttributes data element. */
    private ArchiveAttributes attributes;

    /**
     * Default constructor.
     */
    public LogFileHeader() {
        iQVCSVersion.setValue((short) QVCS_ARCHIVE_VERSION);
        iMajorNumber.setValue((short) 0);
        iMinorNumber.setValue((short) 0);
        iAttributes.setValue((short) 0);
        iVersionCount.setValue((short) 0);
        iRevisionCount.setValue((short) 0);
        iDefaultDepth.setValue((short) 0);
        iLockCount.setValue((short) 0);
        iAccessSize.setValue((short) 0);
        iModifierSize.setValue((short) 0);
        iCommentSize.setValue((short) 0);
        iOwnerSize.setValue((short) 0);
        iDescSize.setValue((short) 0);
        iSupplementalInfoSize.setValue((short) 0);
        iCheckSum.setValue(computeCheckSum());
    }

    /**
     * Get the QVCSVersion. This is defines the version of the structure of the archive file. It should be a 6.
     * @return the version of this archive file.
     */
    public short qvcsVersion() {
        return (short) iQVCSVersion.getValue();
    }

    /**
     * Get the Major Number. This is the major number of the default revision.
     * @return the MajorNumber. This is the major number of the default revision.
     */
    public short majorNumber() {
        return (short) iMajorNumber.getValue();
    }

    /**
     * Set the Major Number. This is the major number of the default revision.
     * @param number the MajorNumber. This is the major number of the default revision.
     */
    public void setMajorNumber(int number) {
        iMajorNumber.setValue(number);
    }

    /**
     * Get the Minor Number. This is the minor number of the default revision.
     * @return the Minor Number. This is the minor number of the default revision.
     */
    public short minorNumber() {
        return (short) iMinorNumber.getValue();
    }

    /**
     * Increment the minor number.
     */
    public void incrementMinorNumber() {
        iMinorNumber.setValue(iMinorNumber.getValue() + 1);
    }

    /**
     * Set the minor number.
     * @param number the minor number.
     */
    public void setMinorNumber(int number) {
        iMinorNumber.setValue(number);
    }

    /**
     * Get the QVCS archive attributes.
     * @return the QVCS archive attributes.
     */
    public ArchiveAttributes attributes() {
        return attributes;
    }

    /**
     * Get the version count. (a.k.a. the label count)... i.e. the number of labels that have been applied to this archive.
     * @return the version count.
     */
    public short versionCount() {
        return (short) iVersionCount.getValue();
    }

    /**
     * Set the version count.
     * @param count the number of labels applied to this archive.
     */
    public void setVersionCount(int count) {
        iVersionCount.setValue(count);
    }

    /**
     * Get the revision count.
     * @return the revision count.
     */
    public short revisionCount() {
        return (short) iRevisionCount.getValue();
    }

    /**
     * Increment the revision count.
     */
    public void incrementRevisionCount() {
        short revisionCount = (short) iRevisionCount.getValue();
        revisionCount++;
        iRevisionCount.setValue(revisionCount);
    }

    /**
     * Get the default depth. If the default branch is the trunk, this will be 0; if the default branch is not the trunk, then this will be non-zero.
     * @return the default depth.
     */
    public short defaultDepth() {
        return (short) iDefaultDepth.getValue();
    }

    /**
     * Get the lock count.
     * @return the number of locked revisions in this archive.
     */
    public short lockCount() {
        return (short) iLockCount.getValue();
    }

    /**
     * Increment the lock count.
     */
    public void incrementLockCount() {
        iLockCount.setValue(1 + iLockCount.getValue());
    }

    /**
     * Decrement the lock count.
     */
    public void decrementLockCount() {
        int lockCount = iLockCount.getValue();
        if (lockCount > 0) {
            lockCount--;
        }
        iLockCount.setValue(lockCount);
    }

    /**
     * Get the size of  the access list (in bytes).
     * @return the size of  the access list.
     */
    public short accessSize() {
        return (short) iAccessSize.getValue();
    }

    /**
     * Get the size of  the modifier list (in bytes).
     * @return the size of  the modifier list.
     */
    public short modifierSize() {
        return (short) iModifierSize.getValue();
    }

    /**
     * Get the size of the comment prefix (in bytes).
     * @return the size of the comment prefix.
     */
    public short commentSize() {
        return (short) iCommentSize.getValue();
    }

    /**
     * Get the owner size (in bytes).
     * @return the owner size.
     */
    public short ownerSize() {
        return (short) iOwnerSize.getValue();
    }

    /**
     * Get the module description size (in bytes).
     * @return the module description size.
     */
    public short descriptionSize() {
        return (short) iDescSize.getValue();
    }

    /**
     * Get the supplementalInfo size (in bytes).
     * @return the supplementalInfo size.
     */
    public short supplementalInfoSize() {
        return (short) iSupplementalInfoSize.getValue();
    }

    /**
     * Get the header checkSum.
     * @return the header checkSum.
     */
    public short checkSum() {
        return (short) iCheckSum.getValue();
    }

    /**
     * Read the QVCS archive header from a stream. <b>This positions the stream to the beginning.</b>
     * @param inStream the stream to read from.
     * @return true if the read is successful; false otherwise.
     * @throws com.qumasoft.qvcslib.LogFileReadException if the header checksum is bad.
     */
    public boolean read(RandomAccessFile inStream) throws LogFileReadException {
        boolean returnValue = true;
        try {
            // Make sure we start at the beginning of the file.
            inStream.seek(0L);

            // Read the version
            iQVCSVersion.read(inStream);

            // Read the major rev on the trunk
            iMajorNumber.read(inStream);

            // Read the minor rev on the trunk
            iMinorNumber.read(inStream);

            // Read the attributes
            iAttributes.read(inStream);

            // Create the associated archive attributes object
            attributes = new ArchiveAttributes(iAttributes.getValue());

            // Read the number of labels
            iVersionCount.read(inStream);

            // Read the number of revisions
            iRevisionCount.read(inStream);

            // Read the default depth
            iDefaultDepth.read(inStream);

            // Read the number of locks
            iLockCount.read(inStream);

            // Read the size of the access list
            iAccessSize.read(inStream);

            // Read the size of the modifier list
            iModifierSize.read(inStream);

            // Read the size of the comment prefix string
            iCommentSize.read(inStream);

            // Read the size of the owner string
            iOwnerSize.read(inStream);

            // Read the size of the module description
            iDescSize.read(inStream);

            // Read the size of the supplemental information
            iSupplementalInfoSize.read(inStream);

            // Read the header checksum
            iCheckSum.read(inStream);

            // Compare the checksum we read with the checksum we compute
            if (iCheckSum.getValue() != computeCheckSum()) {
                throw new LogFileReadException("Bad logfile checksum");
            }
        } catch (IOException ioProblem) {
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Write the QVCS archive header to a stream. <b>This positions the stream to the beginning.</b>
     * @param outStream the stream to write to.
     * @return true on success; false otherwise.
     */
    public boolean write(RandomAccessFile outStream) {
        boolean returnValue = true;
        try {
            // Make sure we start at the beginning of the file.
            outStream.seek(0L);

            // Write the version
            iQVCSVersion.write(outStream);

            // Write the major rev on the trunk
            iMajorNumber.write(outStream);

            // Write the minor rev on the trunk
            iMinorNumber.write(outStream);

            // Write the attributes
            iAttributes.write(outStream);

            // Write the number of labels
            iVersionCount.write(outStream);

            // Write the number of revisions
            iRevisionCount.write(outStream);

            // Write the default depth
            iDefaultDepth.write(outStream);

            // Write the number of locks
            iLockCount.write(outStream);

            // Write the size of the access list
            iAccessSize.write(outStream);

            // Write the size of the modifier list
            iModifierSize.write(outStream);

            // Write the size of the comment prefix string
            iCommentSize.write(outStream);

            // Write the size of the owner string
            iOwnerSize.write(outStream);

            // Write the size of the module description
            iDescSize.write(outStream);

            // Write the size of the supplemental information
            iSupplementalInfoSize.write(outStream);

            // Write the header checksum
            iCheckSum.setValue(computeCheckSum());
            iCheckSum.write(outStream);
        } catch (IOException ioProblem) {
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Compute the checksum.
     * @return the computed checksum.
     */
    public short computeCheckSum() {
        short checkSum;

        checkSum = (short) (iAccessSize.getValue()
                + iAttributes.getValue()
                + iCommentSize.getValue()
                + iDefaultDepth.getValue()
                + iDescSize.getValue()
                + iLockCount.getValue()
                + iMajorNumber.getValue()
                + iMinorNumber.getValue()
                + iModifierSize.getValue()
                + iOwnerSize.getValue()
                + iQVCSVersion.getValue()
                + iRevisionCount.getValue()
                + iVersionCount.getValue()
                + iSupplementalInfoSize.getValue());
        return checkSum;
    }

    /**
     * Get the revision string of the newest trunk revision.
     * @return the revision string of the newest trunk revision.
     */
    public String latestTrunkRevision() {
        String returnString = majorNumber() + "." + minorNumber();
        return returnString;
    }

    /**
     * Set the archive attributes.
     * @param attribs the archive attributes.
     */
    public void setAttributes(ArchiveAttributes attribs) {
        this.attributes = new ArchiveAttributes(attribs);
        iAttributes = new CommonShort(attribs.getAttributesAsInt());
    }

    /**
     * Get the QVCS version.
     * @return the QVCS version.
     */
    public CommonShort getQVCSVersion() {
        return iQVCSVersion;
    }

    /**
     * Set the QVCS version.
     * @param qvcsVersion the QVCS version.
     */
    public void setQVCSVersion(CommonShort qvcsVersion) {
        this.iQVCSVersion = qvcsVersion;
    }

    /**
     * Get the major number.
     * @return the major number.
     */
    public CommonShort getMajorNumber() {
        return iMajorNumber;
    }

    /**
     * Set the major number.
     * @param majorNumber the major number.
     */
    public void setMajorNumber(CommonShort majorNumber) {
        this.iMajorNumber = majorNumber;
    }

    /**
     * Get the minor number.
     * @return the minor number.
     */
    public CommonShort getMinorNumber() {
        return iMinorNumber;
    }

    /**
     * Set the minor number.
     * @param minorNumber the minor number.
     */
    public void setMinorNumber(CommonShort minorNumber) {
        this.iMinorNumber = minorNumber;
    }

    /**
     * Get the attribute bits.
     * @return the attribute bits.
     */
    public CommonShort getAttributeBits() {
        return iAttributes;
    }

    /**
     * Set the attribute bits.
     * @param attribs the attribute bits.
     */
    public void setAttributeBits(CommonShort attribs) {
        this.iAttributes = attribs;
    }

    /**
     * Get the label count.
     * @return the label count.
     */
    public CommonShort getVersionCount() {
        return iVersionCount;
    }

    /**
     * Set the label count.
     * @param versionCount the label count.
     */
    public void setVersionCount(CommonShort versionCount) {
        this.iVersionCount = versionCount;
    }

    /**
     * Get the revision count.
     * @return the revision count.
     */
    public CommonShort getRevisionCount() {
        return iRevisionCount;
    }

    /**
     * Set the revision count.
     * @param revisionCount the revision count.
     */
    public void setRevisionCount(CommonShort revisionCount) {
        this.iRevisionCount = revisionCount;
    }

    /**
     * Get the default depth.
     * @return the default depth.
     */
    public CommonShort getDefaultDepth() {
        return iDefaultDepth;
    }

    /**
     * Set the default depth.
     * @param defaultDepth the default depth.
     */
    public void setDefaultDepth(CommonShort defaultDepth) {
        this.iDefaultDepth = defaultDepth;
    }

    /**
     * Get the lock count.
     * @return the lock count.
     */
    public CommonShort getLockCount() {
        return iLockCount;
    }

    /**
     * Set the lock count.
     * @param lockCount the lock count.
     */
    public void setLockCount(CommonShort lockCount) {
        this.iLockCount = lockCount;
    }

    /**
     * Get the access list size.
     * @return the access list size.
     */
    public CommonShort getAccessSize() {
        return iAccessSize;
    }

    /**
     * Set the access list size.
     * @param accessSize the access list size.
     */
    public void setAccessSize(CommonShort accessSize) {
        this.iAccessSize = accessSize;
    }

    /**
     * Get the modifier list size.
     * @return the modifier list size.
     */
    public CommonShort getModifierSize() {
        return iModifierSize;
    }

    /**
     * Set the modifier list size.
     * @param modifierSize the modifier list size.
     */
    public void setModifierSize(CommonShort modifierSize) {
        this.iModifierSize = modifierSize;
    }

    /**
     * Get the comment prefix size.
     * @return the comment prefix size.
     */
    public CommonShort getCommentSize() {
        return iCommentSize;
    }

    /**
     * Set the comment prefix size.
     * @param commentSize the comment prefix size.
     */
    public void setCommentSize(CommonShort commentSize) {
        this.iCommentSize = commentSize;
    }

    /**
     * Get the owner size.
     * @return the owner size.
     */
    public CommonShort getOwnerSize() {
        return iOwnerSize;
    }

    /**
     * Set the owner size.
     * @param ownerSize the owner size.
     */
    public void setOwnerSize(CommonShort ownerSize) {
        this.iOwnerSize = ownerSize;
    }

    /**
     * Get the module description size.
     * @return the module description size.
     */
    public CommonShort getDescSize() {
        return iDescSize;
    }

    /**
     * Set the module description size.
     * @param descSize the module description size.
     */
    public void setDescSize(CommonShort descSize) {
        this.iDescSize = descSize;
    }

    /**
     * Get the size of the supplemental information.
     * @return the size of the supplemental information.
     */
    public CommonShort getSupplementalInfoSize() {
        return iSupplementalInfoSize;
    }

    /**
     * Set the size of the supplemental information.
     * @param supplementalInfoSize the size of the supplemental information.
     */
    public void setSupplementalInfoSize(CommonShort supplementalInfoSize) {
        this.iSupplementalInfoSize = supplementalInfoSize;
    }

    /**
     * Get the header checksum.
     * @return the header checksum.
     */
    public CommonShort getCheckSum() {
        return iCheckSum;
    }

    /**
     * Set the header checksum.
     * @param checkSum the header checksum.
     */
    public void setCheckSum(CommonShort checkSum) {
        this.iCheckSum = checkSum;
    }

    /**
     * Get the QVCS archive attributes.
     * @return the QVCS archive attributes.
     */
    public ArchiveAttributes getAttributes() {
        return attributes;
    }
}
