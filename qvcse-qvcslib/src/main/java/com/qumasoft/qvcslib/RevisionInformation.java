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

import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Revision information. This class is a convenience wrapper around the revision information contained in a QVCS archive file.
 * @author Jim Voris
 */
public class RevisionInformation implements java.io.Serializable {
    private static final long serialVersionUID = 1738246718497875838L;

    private final AccessList accessList;
    private final AccessList modifierList;
    private final RevisionHeader[] revHeaders;
    /** Create our logger object */
    private static final Logger LOGGER = LoggerFactory.getLogger(RevisionInformation.class);

    /**
     * Construct a revision information instance.
     * @param revisionCount the number of revisions.
     * @param accessLst the access list.
     * @param modifierLst the modifier list.
     */
    public RevisionInformation(int revisionCount, AccessList accessLst, AccessList modifierLst) {
        revHeaders = new RevisionHeader[revisionCount];
        accessList = accessLst;
        modifierList = modifierLst;
    }

    /**
     * Get a given revision header.
     * @param index the revision header's index. Index of 0 is the newest revision.
     * @return the revision header associated with the given index.
     */
    public RevisionHeader getRevisionHeader(int index) {
        RevisionHeader revHeader = null;
        if (index < revHeaders.length) {
            revHeader = revHeaders[index];
        }
        return revHeader;
    }

    /**
     * Read revision information from the QVCS archive. This routine assumes the file is positioned at the beginning of the revision information.
     * @param inStream the RandomAccessFile stream that we read from.
     * @return true if the revision information was read successfully; false otherwise.
     */
    public boolean read(RandomAccessFile inStream) {
        boolean returnValue = true;
        RevisionHeader[] revisionBranches = new RevisionHeader[QVCSConstants.QVCS_MAXIMUM_BRANCH_DEPTH];
        MajorMinorRevisionPair[] majorMinorArray = new MajorMinorRevisionPair[QVCSConstants.QVCS_MAXIMUM_BRANCH_DEPTH];
        for (int i = 0; i < revHeaders.length; i++) {
            revHeaders[i] = new RevisionHeader(accessList, modifierList);
            revHeaders[i].read(inStream, majorMinorArray);
            revHeaders[i].setRevisionIndex(i);

            // Allow the child revision to keep track of its parent.
            if (i > 0) {
                if (revHeaders[i].getDepth() == 0) {
                    revHeaders[i].setParentRevisionHeader(revisionBranches[0]);
                } else if (revHeaders[i].getDepth() == revHeaders[i - 1].getDepth()) {
                    if (revHeaders[i].getMajorNumber() == revHeaders[i - 1].getMajorNumber()) {
                        // This revision is on the same branch as the immediately preceeding revision.
                        revHeaders[i].setParentRevisionHeader(revHeaders[i - 1]);
                    } else {
                        //  This is the beginning of another branch that has the same
                        // 'parent' as a sibling branch.
                        revHeaders[i].setParentRevisionHeader(revisionBranches[revHeaders[i].getDepth() - 1]);
                    }
                } else {
                    // This revision is on a different branch than the preceeding revision...
                    if (revHeaders[i].getDepth() > revHeaders[i - 1].getDepth()) {
                        // This revision is the first revision on a branch... so it's parent
                        // is the immediately preceeding branch point revision.
                        revHeaders[i].setParentRevisionHeader(revHeaders[i - 1]);
                    } else {
                        // This revision is closer to the TRUNK than the immediately preceeding
                        // revision.  It's parent revision is the most recently encountered
                        // revision that was at the same depth as this revision.
                        revHeaders[i].setParentRevisionHeader(revisionBranches[revHeaders[i].getDepth()]);
                    }
                }
            }
            // Save the most recently encountered revision for a given depth.
            revisionBranches[revHeaders[i].getDepth()] = revHeaders[i];
        }
        return returnValue;
    }

    /**
     * Get the modifier list.
     * @return the modifier list.
     */
    public AccessList getModifierList() {
        return modifierList;
    }

    /**
     * Get the access list.
     * @return the access list.
     */
    AccessList getAccessList() {
        return accessList;
    }

    /**
     * Get the String of the revision locked by userName.
     *
     * @param userName the QVCS user to check for locks
     * @return the String for that user's locked revision. null if the user holds no locks.
     */
    public String getLockedRevision(String userName) {
        String lockedRevision = null;
        try {
            for (RevisionHeader revHeader : revHeaders) {
                if (revHeader.isLocked()) {
                    if (0 == getModifierList().indexToUser(revHeader.getLockerIndex()).compareTo(userName)) {
                        lockedRevision = revHeader.getRevisionString();
                    }
                }
            }
        } catch (QVCSRuntimeException e) {
            // Nothing to do here.  The user's has no locked revisions
            // as far as we are concerned.
            LOGGER.trace("No locked revisions: " + e.getLocalizedMessage());
        }
        return lockedRevision;
    }

    /**
     * Convenience toString to report on the revision header information.
     * @return report on the revision header information.
     */
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();
        string.append("\n");
        for (RevisionHeader revisionHeader : revHeaders) {
            string.append("------------------\n");
            string.append(revisionHeader.toString());
        }
        return string.toString();
    }

    /**
     * Return the index for the given revision string. Return a -1 if the revision string is not found.
     *
     * @param revisionString the revision to search for.
     * @return return the index associated with the given revision string.
     */
    public int getRevisionIndex(String revisionString) {
        int returnedIndex = -1;
        for (int i = 0; i < revHeaders.length; i++) {
            RevisionHeader revHeader = revHeaders[i];
            String currentRevString = revHeader.getRevisionString();
            if (0 == revisionString.compareTo(currentRevString)) {
                returnedIndex = i;
                break;
            }
        }
        return returnedIndex;
    }

    /**
     * Update a given revision header.
     * @param revisionIndex the revision index for the header that is to be updated.
     * @param updatedRevHeader the updated header.
     */
    public void updateRevision(int revisionIndex, RevisionHeader updatedRevHeader) {
        revHeaders[revisionIndex] = updatedRevHeader;
    }
}
