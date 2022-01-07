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

import com.qvcsos.server.archivemigration.LegacyRevisionHeader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 *
 * @author Jim Voris
 */
public class LegacyRevisionDescriptor implements java.lang.Comparable {
    private final int elementCount;
    private final LegacyCommonShort csElementCount = new LegacyCommonShort();
    private final LegacyCommonShort csRevisionAttributes = new LegacyCommonShort();
    private LegacyMajorMinorRevisionPair[] revisionPairs;
    private transient String sortableString = null;

    /**
     * Create a revision descriptor.
     *
     * @param elementCnt the number of elements in this descriptor.
     * @param majorMinorArray the major/minor array that will be used to
     * initialize the descriptor.
     */
    public LegacyRevisionDescriptor(int elementCnt, LegacyMajorMinorRevisionPair[] majorMinorArray) {
        elementCount = elementCnt;
        if (elementCount > 0) {
            csElementCount.setValue(elementCount);
            csRevisionAttributes.setValue(0);
            revisionPairs = new LegacyMajorMinorRevisionPair[elementCount];

            for (int i = 0; i < elementCount; i++) {
                revisionPairs[i] = new LegacyMajorMinorRevisionPair(majorMinorArray[i].getMajorNumber(), majorMinorArray[i].getMinorNumber());
            }
        }
    }

    /**
     * Create a revision descriptor with the given number of elements.
     *
     * @param elementCnt the element count for this descriptor.
     */
    public LegacyRevisionDescriptor(int elementCnt) {
        elementCount = elementCnt;
    }

    /**
     * Create a revision descriptor for the given revision and revision parent.
     *
     * @param newRevision the revision for which this will be the descriptor.
     * @param parentRevision the revision's parent revision.
     */
    public LegacyRevisionDescriptor(LegacyRevisionHeader newRevision, LegacyRevisionHeader parentRevision) {
        elementCount = newRevision.getDepth() + 1;
        if (elementCount > 0) {
            csElementCount.setValue(elementCount);
            csRevisionAttributes.setValue(0);
            revisionPairs = new LegacyMajorMinorRevisionPair[elementCount];

            LegacyRevisionDescriptor parentRevisionDescriptor = parentRevision.getRevisionDescriptor();
            LegacyMajorMinorRevisionPair[] parentRevisionPairs = parentRevisionDescriptor.revisionPairs;
            for (int i = 0; i < parentRevisionDescriptor.getElementCount(); i++) {
                revisionPairs[i] = new LegacyMajorMinorRevisionPair(parentRevisionPairs[i].getMajorNumber(), parentRevisionPairs[i].getMinorNumber());
            }
            revisionPairs[elementCount - 1] = new LegacyMajorMinorRevisionPair(newRevision.getMajorNumber(), newRevision.getMinorNumber());
        }
    }

    /**
     * A Copy constructor.
     *
     * @param revisionDescriptorToCopy the descriptor to copy.
     */
    public LegacyRevisionDescriptor(final LegacyRevisionDescriptor revisionDescriptorToCopy) {
        elementCount = revisionDescriptorToCopy.elementCount;
        if (elementCount > 0) {
            csElementCount.setValue(elementCount);
            csRevisionAttributes.setValue(0);
            revisionPairs = new LegacyMajorMinorRevisionPair[elementCount];

            for (int i = 0; i < elementCount; i++) {
                revisionPairs[i] = new LegacyMajorMinorRevisionPair(revisionDescriptorToCopy.revisionPairs[i].getMajorNumber(),
                        revisionDescriptorToCopy.revisionPairs[i].getMinorNumber());
            }
        }
    }

    /**
     * A convenient toString.
     *
     * @return a convenient string representation of this revision descriptor.
     */
    @Override
    public String toString() {
        StringBuilder returnString = new StringBuilder();
        for (int i = 0; i < elementCount; i++) {
            if (i != 0) {
                returnString.append(".");
            }
            returnString.append(revisionPairs[i]);
        }
        return returnString.toString();
    }

    /**
     * Return a sortable String representation for this revision descriptor.
     *
     * @return a sortable String representation for this revision descriptor.
     */
    public synchronized String toSortableString() {
        if (sortableString == null) {
            DecimalFormat formatter = new DecimalFormat("0000");
            StringBuilder resultBuffer = new StringBuilder();
            for (int i = 0; i < elementCount; i++) {
                resultBuffer.append(formatter.format(revisionPairs[i].getMajorNumber()));
                resultBuffer.append(formatter.format(revisionPairs[i].getMinorNumber()));
            }
            sortableString = resultBuffer.toString();
        }
        return sortableString;
    }

    /**
     * Read the revision descriptor information from the archive file.
     *
     * @param inStream the stream to read from.
     * @throws IOException for read problems.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        if (elementCount > 0) {
            csElementCount.read(inStream);
            csRevisionAttributes.read(inStream);
            revisionPairs = new LegacyMajorMinorRevisionPair[elementCount];

            for (int i = 0; i < elementCount; i++) {
                revisionPairs[i] = new LegacyMajorMinorRevisionPair();
                revisionPairs[i].read(inStream);
            }

            // There's a bug in the archive format. So read (and discard) some stuff.
            LegacyMajorMinorRevisionPair pair = new LegacyMajorMinorRevisionPair();
            pair.read(inStream);

            int junkInteger = inStream.readInt();
        }
    }

    /**
     * Get the trunk minor number.
     *
     * @return the trunk minor number.
     */
    public int getTrunkMinorNumber() {
        return revisionPairs[0].getMinorNumber();
    }

    /**
     * Get the element count.
     *
     * @return the element count.
     */
    public int getElementCount() {
        return elementCount;
    }

    @Override
    public int compareTo(Object o) {
        LegacyRevisionDescriptor otherDescriptor = (LegacyRevisionDescriptor) o;
        String ourSortable = toSortableString();
        String otherSortable = otherDescriptor.toSortableString();
        return ourSortable.compareTo(otherSortable);
    }

    @Override
    public int hashCode() {
        // Guarantee the sortable string has been set.
        toSortableString();
        // <editor-fold>
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.sortableString);
        // </editor-fold>
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        // Guarantee the sortable string has been set.
        toSortableString();
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final LegacyRevisionDescriptor other = (LegacyRevisionDescriptor) obj;
        return Objects.equals(this.sortableString, other.sortableString);
    }

    /**
     * Get the revision pairs of this revision descriptor.
     *
     * @return the revision pairs of this revision descriptor.
     */
    public LegacyMajorMinorRevisionPair[] getRevisionPairs() {
        return revisionPairs;
    }
}
