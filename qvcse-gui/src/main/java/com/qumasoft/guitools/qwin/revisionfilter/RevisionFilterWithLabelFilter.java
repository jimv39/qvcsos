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
import com.qumasoft.qvcslib.RevisionInformation;
import java.util.Objects;

/**
 * With label revision filter.
 * @author Jim Voris
 */
public class RevisionFilterWithLabelFilter extends AbstractRevisionFilter {

    private final String filterLabel;

    /**
     * Create a with label revision filter.
     * @param label the label that defines the filter.
     * @param isANDFilter is this an 'AND' filter.
     */
    public RevisionFilterWithLabelFilter(String label, boolean isANDFilter) {
        super(isANDFilter);
        filterLabel = label;
    }

    @Override
    public boolean passesFilter(FilteredRevisionInfo filteredRevisionInfo) {
        boolean retVal = false;
        MergedInfoInterface mergedInfo = filteredRevisionInfo.getMergedInfo();
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        int filteredRevisionIndex = filteredRevisionInfo.getRevisionIndex();
        if (logfileInfo != null) {
            LabelInfo[] labels = logfileInfo.getLogFileHeaderInfo().getLabelInfo();
            if (labels != null) {
                for (LabelInfo label : labels) {
                    if (label.getLabelString().equals(filterLabel)) {
                        String labelRevisionString = label.getLabelRevisionString();
                        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
                        int revisionIndex = revisionInformation.getRevisionIndex(labelRevisionString);
                        // The file has the label... the revision that the label points to should
                        // pass the filter.
                        if (revisionIndex == filteredRevisionIndex) {
                            retVal = true;
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
        return QVCSConstants.WITH_LABEL_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.WITH_LABEL_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterLabel;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof RevisionFilterWithLabelFilter) {
            RevisionFilterWithLabelFilter filter = (RevisionFilterWithLabelFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.filterLabel);
        if (this.getIsANDFilter()) {
            hash += 97;
        }
        // </editor-fold>
        return hash;
    }
}
