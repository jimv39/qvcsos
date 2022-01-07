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

import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class LegacyRevisionInformation {
    private final LegacyAccessList accessList;
    private final LegacyAccessList modifierList;
    private final LegacyRevisionHeader[] revHeaders;
    /**
     * Create our logger object
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyRevisionInformation.class);

    /**
     * Construct a revision information instance.
     *
     * @param revisionCount the number of revisions.
     * @param accessLst the access list.
     * @param modifierLst the modifier list.
     */
    public LegacyRevisionInformation(int revisionCount, LegacyAccessList accessLst, LegacyAccessList modifierLst) {
        revHeaders = new LegacyRevisionHeader[revisionCount];
        accessList = accessLst;
        modifierList = modifierLst;
    }

    public boolean read(RandomAccessFile inStream) {
        boolean returnValue = true;
        LegacyRevisionHeader[] revisionBranches = new LegacyRevisionHeader[LegacyQVCSConstants.QVCS_MAXIMUM_BRANCH_DEPTH];
        LegacyMajorMinorRevisionPair[] majorMinorArray = new LegacyMajorMinorRevisionPair[LegacyQVCSConstants.QVCS_MAXIMUM_BRANCH_DEPTH];
        for (int i = 0; i < revHeaders.length; i++) {
            revHeaders[i] = new LegacyRevisionHeader(accessList, modifierList);
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

    public LegacyRevisionHeader getRevisionHeader(int index) {
        LegacyRevisionHeader revHeader = null;
        if (index < revHeaders.length) {
            revHeader = revHeaders[index];
        }
        return revHeader;
    }

}
