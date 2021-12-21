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
package com.qumasoft.guitools.qwin;

import com.qumasoft.guitools.qwin.filefilter.FileFilterInterface;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import java.util.TreeMap;

/**
 * Filtered file table model.
 * @author Jim Voris
 */
public class FilteredFileTableModel extends FileTableModel {
    private static final long serialVersionUID = 1L;

    private FilterCollection filterCollection;
    private boolean isEnabledFlag = false;
    private boolean collectionRequiresRevisionDetailInfoFlag = false;
    private final Object syncObject = new Object();

    FilteredFileTableModel() {
        super();
    }

    void setFilterCollection(FilterCollection filters) {
        synchronized (syncObject) {
            this.filterCollection = filters;
        }
        if (filters.getCollectionName().equals(FilterManager.ALL_FILTER)) {
            QWinFrame.getQWinFrame().setFilterActive(false);
        } else {
            QWinFrame.getQWinFrame().setFilterActive(true);
        }
        deduceIfCollectionRequireRevisionDetail();
    }

    /**
     * Get the filter collection.
     * @return the filter collection.
     */
    public FilterCollection getFilterCollection() {
        return filterCollection;
    }

    void setEnableFilters(boolean flag) {
        isEnabledFlag = flag;
    }

    boolean getEnableFilters() {
        return isEnabledFlag;
    }

    @Override
    public boolean passesFileFilters(MergedInfoInterface mergedInfo) {
        // Default to passing filter
        boolean retVal = true;

        // Iterate over the known filters to see if the file
        // passes the filters.
        if (getEnableFilters()) {
            synchronized (syncObject) {
                // Create the Set of revisionHeaders...
                TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
                if (collectionRequiresRevisionDetail()) {
                    if (mergedInfo.getArchiveInfo() != null) {
                        LogfileInfo logfileInfo = mergedInfo.getArchiveInfo().getLogfileInfo();
                        int revisionCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
                        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
                        for (int i = 0; i < revisionCount; i++) {
                            revisionHeaderMap.put(i, revisionInformation.getRevisionHeader(i));
                        }
                    }
                }

                FileFilterInterface[] fileFilters = filterCollection.listFilters();
                for (FileFilterInterface filter : fileFilters) {
                    if (filter.getIsANDFilter()) {
                        boolean flag = filter.passesFilter(mergedInfo, revisionHeaderMap);
                        if (!flag) {
                            retVal = false;
                            break;
                        }
                    }
                }

                // It passed all the AND filters.  See if it passes the OR filters...
                if (retVal) {
                    boolean flag = false;
                    boolean orFilterFound = false;
                    for (FileFilterInterface filter : fileFilters) {
                        if (filter.getIsORFilter()) {
                            orFilterFound = true;

                            if (filter.passesFilter(mergedInfo, revisionHeaderMap)) {
                                flag = true;
                                break;
                            }
                        }
                    }
                    if (orFilterFound) {
                        retVal = flag;
                    }
                } else {
                    retVal = false;
                }
            }
        }
        return retVal;
    }

    private boolean collectionRequiresRevisionDetail() {
        return collectionRequiresRevisionDetailInfoFlag;
    }

    private void deduceIfCollectionRequireRevisionDetail() {
        FileFilterInterface[] fileFilters = filterCollection.listFilters();
        collectionRequiresRevisionDetailInfoFlag = false;
        for (FileFilterInterface filter : fileFilters) {
            if (filter.requiresRevisionDetailInfo()) {
                collectionRequiresRevisionDetailInfoFlag = true;
                break;
            }
        }
    }
}
