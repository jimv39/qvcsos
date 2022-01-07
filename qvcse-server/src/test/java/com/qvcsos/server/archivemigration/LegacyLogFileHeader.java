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

import com.qvcsos.server.archivemigration.LegacyLogFileReadException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 *
 * @author Jim Voris.
 */
public class LegacyLogFileHeader {
    /**
     * The QVCSVersion. This is defines the version of the structure of the
     * archive file. It should be a 6.
     */
    private LegacyCommonShort iQVCSVersion = new LegacyCommonShort();  // QVCS Version
    /**
     * The Major Number. This is the major number of the default revision.
     */
    private LegacyCommonShort iMajorNumber = new LegacyCommonShort();  // Major revision number of trunk
    /**
     * The Minor Number. This is the minor number of the default revision.
     */
    private LegacyCommonShort iMinorNumber = new LegacyCommonShort();  // Minor revision number of trunk
    /**
     * The QVCS archive attributes.
     */
    private LegacyCommonShort iAttributes = new LegacyCommonShort();  // Attributes for this log file
    /**
     * The number of versions (a.k.a. labels) that have been applied to this
     * archive
     */
    private LegacyCommonShort iVersionCount = new LegacyCommonShort();  // Number of version stamps
    /**
     * The number of revisions in this archive file
     */
    private LegacyCommonShort iRevisionCount = new LegacyCommonShort();  // Number of revisions here
    /**
     * The default depth of this archive. A depth of 0 implies that the default
     * branch for this file is the trunk.
     */
    private LegacyCommonShort iDefaultDepth = new LegacyCommonShort();  // Depth of default branch (0 implies trunk)
    /**
     * The number of revisions that are locked in this archive file.
     */
    private LegacyCommonShort iLockCount = new LegacyCommonShort();  // Number of locks on the logfile
    /**
     * The number of bytes to reserve for the file's access list.
     */
    private LegacyCommonShort iAccessSize = new LegacyCommonShort();  // Strlen of Access list string including NULL
    /**
     * The number of bytes to reserve for the file's modifier list.
     */
    private LegacyCommonShort iModifierSize = new LegacyCommonShort();  // Strlen of Modifier list string including NULL
    /**
     * The number of bytes to reserve for this file's comment prefix.
     */
    private LegacyCommonShort iCommentSize = new LegacyCommonShort();  // Strlen of comment prefix string including NULL
    /**
     * The number of bytes to reserve for the owner's user name.
     */
    private LegacyCommonShort iOwnerSize = new LegacyCommonShort();  // Strlen of Owner string including NULL
    /**
     * The number of bytes to reserve for the module description.
     */
    private LegacyCommonShort iDescSize = new LegacyCommonShort();  // Strlen of Module desc including NULL
    /**
     * The number of bytes to reserve for the supplemental information section.
     */
    private LegacyCommonShort iSupplementalInfoSize = new LegacyCommonShort();  // Length of the supplemental info section
    /**
     * The header checksum.
     */
    private LegacyCommonShort iCheckSum = new LegacyCommonShort();  // Header checksum word
    /**
     * The archive attributes. Note that this member variable is not serialized
     * to disk, as it is an alternate representation of the iAttributes data
     * element.
     */
    private LegacyArchiveAttributes attributes;

    boolean read(RandomAccessFile inStream) throws LegacyLogFileReadException {
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
            attributes = new LegacyArchiveAttributes(iAttributes.getValue());

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
                throw new LegacyLogFileReadException("Bad logfile checksum");
            }
        } catch (IOException ioProblem) {
            returnValue = false;
        }
        return returnValue;
    }

    public short accessSize() {
        return (short) iAccessSize.getValue();
    }

    public short modifierSize() {
        return (short) iModifierSize.getValue();
    }

    public short commentSize() {
        return (short) iCommentSize.getValue();
    }

    public short defaultDepth() {
        return (short) iDefaultDepth.getValue();
    }
    public short ownerSize() {
        return (short) iOwnerSize.getValue();
    }

    /**
     * Get the module description size (in bytes).
     *
     * @return the module description size.
     */
    public short descriptionSize() {
        return (short) iDescSize.getValue();
    }

    /**
     * Get the supplementalInfo size (in bytes).
     *
     * @return the supplementalInfo size.
     */
    public short supplementalInfoSize() {
        return (short) iSupplementalInfoSize.getValue();
    }

    /**
     * Get the header checkSum.
     *
     * @return the header checkSum.
     */
    public short checkSum() {
        return (short) iCheckSum.getValue();
    }

    public short versionCount() {
        return (short) iVersionCount.getValue();
    }

    public LegacyCommonShort getSupplementalInfoSize() {
        return iSupplementalInfoSize;
    }

    public LegacyArchiveAttributes attributes() {
        return attributes;
    }

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

    public short revisionCount() {
        return (short) iRevisionCount.getValue();
    }

}
