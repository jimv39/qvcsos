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

import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.TreeMap;

/**
 * File size less than filter.
 * @author Jim Voris
 */
public class FileFilterFilesizeLessThanFilter extends AbstractFileFilter {
    private static final long serialVersionUID = 9150689982538121600L;

    private long filterFilesize;

    FileFilterFilesizeLessThanFilter(String size, boolean isANDFilter) {
        super(isANDFilter);
        try {
            filterFilesize = Long.parseLong(size);
        } catch (NumberFormatException e) {
            filterFilesize = 0;
        }
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;
        if (mergedInfo.getWorkfile() != null) {
            if (mergedInfo.getWorkfileSize() < filterFilesize) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.FILESIZE_LESS_THAN_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.FILESIZE_LESS_THAN_FILTER;
    }

    @Override
    public String getFilterData() {
        return Long.toString(filterFilesize);
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterFilesizeLessThanFilter) {
            FileFilterFilesizeLessThanFilter filter = (FileFilterFilesizeLessThanFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 71 * hash + (int) (this.filterFilesize ^ (this.filterFilesize >>> 32));
        if (this.getIsANDFilter()) {
            hash += 97;
        }
        // </editor-fold>
        return hash;
    }

    @Override
    public String getRawFilterData() {
        return Long.toString(filterFilesize);
    }
}
