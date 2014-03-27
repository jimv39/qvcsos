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

import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Without label filter.
 * @author Jim Voris
 */
public class FileFilterWithoutLabelFilter extends AbstractFileFilter {
    private static final long serialVersionUID = 7463320283800137489L;

    private final String filterLabel;

    FileFilterWithoutLabelFilter(String label, boolean isANDFilter) {
        super(isANDFilter);
        filterLabel = label;
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = true;
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        if (logfileInfo != null) {
            LabelInfo[] labels = logfileInfo.getLogFileHeaderInfo().getLabelInfo();
            if (labels != null) {
                for (LabelInfo label : labels) {
                    if (label.getLabelString().equals(filterLabel)) {
                        retVal = false;
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.WITHOUT_LABEL_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.WITHOUT_LABEL_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterLabel;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterWithoutLabelFilter) {
            FileFilterWithoutLabelFilter filter = (FileFilterWithoutLabelFilter) o;
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
        hash = 37 * hash + Objects.hashCode(this.filterLabel);
        if (this.getIsANDFilter()) {
            hash += 97;
        }
        // </editor-fold>
        return hash;
    }

    @Override
    public String getRawFilterData() {
        return filterLabel;
    }
}
