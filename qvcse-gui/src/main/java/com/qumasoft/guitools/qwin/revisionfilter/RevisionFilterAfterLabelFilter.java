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
package com.qumasoft.guitools.qwin.revisionfilter;

import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import java.util.Objects;

/**
 * After label revision filter.
 * @author Jim Voris
 */
public class RevisionFilterAfterLabelFilter extends AbstractRevisionFilter {

    private final String filterLabel;

    /**
     * Create an after label revision filter.
     * @param label the label that defines the filter.
     * @param isANDFilter is this an 'AND' filter.
     */
    public RevisionFilterAfterLabelFilter(String label, boolean isANDFilter) {
        super(isANDFilter);
        filterLabel = label;
    }

    @Override
    public boolean passesFilter(FilteredRevisionInfo filteredRevisionInfo) {
        boolean retVal = false;
        MergedInfoInterface mergedInfo = filteredRevisionInfo.getMergedInfo();
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        RevisionHeader filteredRevision = filteredRevisionInfo.getRevisionHeader();
        int filteredRevisionIndex = filteredRevisionInfo.getRevisionIndex();
        if (logfileInfo != null) {
            LabelInfo[] labels = logfileInfo.getLogFileHeaderInfo().getLabelInfo();
            if (labels != null) {
                for (LabelInfo label : labels) {
                    if (label.getLabelString().equals(filterLabel)) {
                        String labelRevisionString = label.getLabelRevisionString();
                        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
                        int revisionIndex = revisionInformation.getRevisionIndex(labelRevisionString);
                        RevisionHeader revHeader = revisionInformation.getRevisionHeader(revisionIndex);
                        // What we need to do here is see if the revision that is newer than this one is
                        // on the same branch.  If we are on the trunk, then the index we want is closer
                        // to 0, if we are on a branch, then the index we want is further away from 0.
                        if (revHeader.getDepth() == 0) {
                            // The label is on the TRUNK.
                            if (filteredRevision.getDepth() == 0) {
                                if (filteredRevisionIndex < revisionIndex) {
                                    // This TRUNK revision is newer than the TRUNK revision that has the label
                                    retVal = true;
                                }
                            } else {
                                // The label is on the TRUNK, but this revision is
                                // not on the TRUNK.  We need to see if this revision's
                                // branch point on the TRUNK is after the label.
                                int filteredRevisionsTrunkIndex = filteredRevisionIndex;
                                while (filteredRevisionsTrunkIndex > 0) {
                                    RevisionHeader trunkRevisionHeader = revisionInformation.getRevisionHeader(filteredRevisionsTrunkIndex);
                                    if (trunkRevisionHeader.getDepth() == 0) {
                                        if (filteredRevisionsTrunkIndex <= revisionIndex) {
                                            // The branch's TRUNK revision is newer than the TRUNK revision that has the label
                                            retVal = true;
                                        }
                                        break;
                                    }

                                    // Still not to the TRUNK yet... keep going
                                    // until we get to the TRUNK.
                                    filteredRevisionsTrunkIndex--;
                                }
                            }
                        } else {
                            // The label is on a branch.
                            // On a branch, newer revisions occur AFTER in the archive file...
                            if (filteredRevisionIndex > revisionIndex) {
                                // If they are of the same depth... they might be on the same branch...
                                if (filteredRevision.getDepth() == revHeader.getDepth()) {
                                    // They are on the same branch if their revision strings match up to the
                                    // last minor number.
                                    String filteredRevisionString = filteredRevision.getRevisionString();
                                    int end = filteredRevisionString.lastIndexOf('.');
                                    String branchRevisionString = filteredRevisionString.substring(0, end);
                                    if (revHeader.getRevisionString().startsWith(branchRevisionString)) {
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
        if (o instanceof RevisionFilterAfterLabelFilter) {
            RevisionFilterAfterLabelFilter filter = (RevisionFilterAfterLabelFilter) o;
            if (filter.getFilterData().equals(getFilterData())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 5;
        hash = 79 * hash + Objects.hashCode(this.filterLabel);
        // </editor-fold>
        return hash;
    }
}
