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
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterAfterLabelFilter;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeMap;

/**
 * After label filter.
 *
 * @author Jim Voris
 */
public class FileFilterAfterLabelFilter extends AbstractFileFilter {

    private static final long serialVersionUID = 4702780636975322680L;
    private transient RevisionFilterAfterLabelFilter revisionFilterAfterLabelFilter = null;

    private final String filterLabel;

    FileFilterAfterLabelFilter(String label, boolean isANDFilter) {
        super(isANDFilter);
        filterLabel = label;
        revisionFilterAfterLabelFilter = new RevisionFilterAfterLabelFilter(label, isANDFilter);
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        if (revisionFilterAfterLabelFilter == null) {
            revisionFilterAfterLabelFilter = new RevisionFilterAfterLabelFilter(filterLabel, getIsANDFilter());
        }
        if (logfileInfo != null) {
            LabelInfo[] labels = logfileInfo.getLogFileHeaderInfo().getLabelInfo();
            if (labels != null) {
                for (LabelInfo label : labels) {
                    if (label.getLabelString().equals(filterLabel)) {
                        String labelRevisionString = label.getLabelRevisionString();
                        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
                        int revisionIndex = revisionInformation.getRevisionIndex(labelRevisionString);
                        if (revisionIndex >= 0) {
                            RevisionHeader revHeader = revisionInformation.getRevisionHeader(revisionIndex);

                            // What we need to do here is see if the revision that is newer than this one is
                            // on the same branch.  If we are on the trunk, then the index we want is closer
                            // to 0, if we are on a branch, then the index we want is further away from 0.
                            if (revHeader.getDepth() == 0) {
                                // The label is on the TRUNK.  If the revision Index is > 0, then
                                // this file has revisions (on the TRUNK) that are newer than when
                                // the label was applied.... ergo, the return value for the filter
                                // is true.
                                if (revisionIndex > 0) {
                                    retVal = true;
                                }
                            } else {
                                // The label is on a branch.  This is a little trickier.  If the next revision
                                // in the archive is on the same branch, then it is newer than the revision
                                // that has the label.
                                RevisionHeader branchRevHeader = revisionInformation.getRevisionHeader(revisionIndex + 1);
                                if (branchRevHeader != null) {
                                    if (branchRevHeader.getDepth() == revHeader.getDepth()) {
                                        retVal = true;
                                    }
                                }
                            }
                        }
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
                boolean flag = revisionFilterAfterLabelFilter.passesFilter(filteredRevisionInfo);
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
        return QVCSConstants.AFTER_LABEL_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.AFTER_LABEL_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterLabel;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterAfterLabelFilter) {
            FileFilterAfterLabelFilter filter = (FileFilterAfterLabelFilter) o;
            if (filter.getFilterData().equals(getFilterData())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 17 * hash + Objects.hashCode(this.filterLabel);
        // </editor-fold>
        return hash;
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
