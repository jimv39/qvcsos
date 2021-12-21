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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Revision information. This class is a convenience wrapper around the revision information contained in a QVCS archive file.
 * @author Jim Voris
 */
public class RevisionInformation implements java.io.Serializable {
    private static final long serialVersionUID = 1738246718497875838L;

    private final RevisionHeader[] revHeaders;
    /** Create our logger object */
    private static final Logger LOGGER = LoggerFactory.getLogger(RevisionInformation.class);

    /**
     * Construct a revision information instance.
     * @param revisionCount the number of revisions.
     */
    public RevisionInformation(int revisionCount) {
        revHeaders = new RevisionHeader[revisionCount];
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
