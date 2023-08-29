/*   Copyright 2022-2023 Jim Voris
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
package com.qumasoft.guitools.qwin.filefilter;

import com.qumasoft.guitools.qwin.revisionfilter.FilteredRevisionInfo;
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterCheckedInAfterCommitFilter;
import com.qumasoft.qvcslib.CommonFilterFile;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Checked in after filter.
 * @author Jim Voris
 */
public class FileFilterCheckedInAfterCommitFilter extends AbstractFileFilter {
    private static final long serialVersionUID = -2068967872772050356L;

    private transient RevisionFilterCheckedInAfterCommitFilter revisionFilterCheckedInAfterCommitFilter;
    private final String commitIdString;
    private transient Integer commitId;

    FileFilterCheckedInAfterCommitFilter(String commitIdAsString, boolean isANDFilter, Integer id) {
        super(isANDFilter, id, CommonFilterFile.CHECKED_IN_AFTER_COMMIT_ID);
        this.commitIdString = commitIdAsString;
        this.commitId = Integer.decode(commitIdString);
        revisionFilterCheckedInAfterCommitFilter = new RevisionFilterCheckedInAfterCommitFilter(commitIdAsString, isANDFilter);
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        if (this.commitId == null && this.commitIdString != null) {
            this.commitId = Integer.decode(commitIdString);
            revisionFilterCheckedInAfterCommitFilter = new RevisionFilterCheckedInAfterCommitFilter(this.commitIdString, getIsANDFilter());
        }
        boolean retVal = true;

        if ((revisionFilterCheckedInAfterCommitFilter == null) && (commitIdString != null)) {
            revisionFilterCheckedInAfterCommitFilter = new RevisionFilterCheckedInAfterCommitFilter(commitIdString, getIsANDFilter());
        }
        if ((commitId != null) && (revisionFilterCheckedInAfterCommitFilter != null)) {
            Iterator<Integer> it = revisionHeaderMap.keySet().iterator();
            while (it.hasNext()) {
                Integer revisionIndexInteger = it.next();
                RevisionHeader revisionHeader = revisionHeaderMap.get(revisionIndexInteger);
                FilteredRevisionInfo filteredRevisionInfo = new FilteredRevisionInfo(mergedInfo, revisionHeader, revisionIndexInteger);
                boolean flag = revisionFilterCheckedInAfterCommitFilter.passesFilter(filteredRevisionInfo);
                if (!flag) {
                    it.remove();
                }
            }

            // No revisions are left to pass any filters...
            if (revisionHeaderMap.isEmpty()) {
                retVal = false;
            }
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.CHECKED_IN_AFTER_COMMIT_ID_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.CHECKED_IN_AFTER_COMMIT_ID_FILTER;
    }

    @Override
    public String getFilterData() {
        return this.commitIdString;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterCheckedInAfterCommitFilter) {
            FileFilterCheckedInAfterCommitFilter filter = (FileFilterCheckedInAfterCommitFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return computeHash(this.commitIdString, this.getIsANDFilter());
    }

    @Override
    public String getRawFilterData() {
        return commitIdString;
    }

    @Override
    public boolean requiresRevisionDetailInfo() {
        return true;
    }
}
