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
package com.qumasoft.guitools.qwin.filefilter;

import com.qumasoft.guitools.qwin.revisionfilter.FilteredRevisionInfo;
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterUptoLabelFilter;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Up to label filter.
 * @author Jim Voris
 */
public class FileFilterUptoLabelFilter extends AbstractFileFilter {
    private static final long serialVersionUID = 1973477867728256105L;

    private transient RevisionFilterUptoLabelFilter revisionFilterUptoLabelFilter;
    private final String filterLabel;

    FileFilterUptoLabelFilter(String label, boolean isANDFilter) {
        super(isANDFilter);
        filterLabel = label;
        revisionFilterUptoLabelFilter = new RevisionFilterUptoLabelFilter(label, isANDFilter);
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        if (revisionFilterUptoLabelFilter == null) {
            revisionFilterUptoLabelFilter = new RevisionFilterUptoLabelFilter(filterLabel, getIsANDFilter());
        }
        if (logfileInfo != null) {
            LabelInfo[] labels = logfileInfo.getLogFileHeaderInfo().getLabelInfo();
            if (labels != null) {
                for (LabelInfo label : labels) {
                    if (label.getLabelString().equals(filterLabel)) {
                        // The label exists, ergo there is at least one revision
                        // that is 'up to' the named label.
                        retVal = true;
                        break;
                    }
                }
            }
        }

        // Trim the revision map so it contains only revisions that pass this filter
        if (retVal) {
            Iterator<Integer> it = revisionHeaderMap.keySet().iterator();
            while (it.hasNext()) {
                Integer revisionIndexInteger = it.next();
                RevisionHeader revisionHeader = revisionHeaderMap.get(revisionIndexInteger);
                FilteredRevisionInfo filteredRevisionInfo = new FilteredRevisionInfo(mergedInfo, revisionHeader, revisionIndexInteger.intValue());
                boolean flag = revisionFilterUptoLabelFilter.passesFilter(filteredRevisionInfo);
                if (!flag) {
                    it.remove();
                }
            }

            // No revisions are left to pass any filters...
            if (revisionHeaderMap.size() == 0) {
                retVal = false;
            }
        }

        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.UPTO_LABEL_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.UPTO_LABEL_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterLabel;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterUptoLabelFilter) {
            FileFilterUptoLabelFilter filter = (FileFilterUptoLabelFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return computeHash(this.filterLabel, this.getIsANDFilter());
    }

    @Override
    public String getRawFilterData() {
        return filterLabel;
    }

    @Override
    public boolean requiresRevisionDetailInfo() {
        return true;
    }
}
