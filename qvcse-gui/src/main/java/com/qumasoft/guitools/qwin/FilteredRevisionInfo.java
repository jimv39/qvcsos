//   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.RevisionHeader;

/**
 * Filtered revision information. Instances of this class are immutable.
 * @author Jim Voris
 */
public class FilteredRevisionInfo {

    private final MergedInfoInterface mergedInfo;
    private final RevisionHeader revisionHeader;
    private final int revisionIndex;

    /**
     * Create a new filtered revision info instance.
     * @param info the file from which to create this object.
     * @param revHeader the revision header that we'll filter.
     * @param revisionIdx the revision index.
     */
    public FilteredRevisionInfo(MergedInfoInterface info, RevisionHeader revHeader, int revisionIdx) {
        mergedInfo = info;
        revisionHeader = revHeader;
        revisionIndex = revisionIdx;
    }

    MergedInfoInterface getMergedInfo() {
        return mergedInfo;
    }

    RevisionHeader getRevisionHeader() {
        return revisionHeader;
    }

    int getRevisionIndex() {
        return revisionIndex;
    }
}
