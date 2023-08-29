/*
 * Copyright 2022-2023 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.guitools.qwin.filefilter;

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.CommonFilterFile;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author Jim Voris
 */
public class FileFilterByCommitIdFilter extends AbstractFileFilter {

    public FileFilterByCommitIdFilter(boolean isANDFilter, Integer id) {
        super(isANDFilter, id, CommonFilterFile.BY_COMMIT_ID);
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;

        Set<Integer> fileIdSet = QWinFrame.getQWinFrame().getFileIdSetForGivenCommitId();
        if (mergedInfo.getArchiveInfo() != null) {
            if (fileIdSet.contains(mergedInfo.getFileID())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.BY_COMMIT_ID_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.BY_COMMIT_ID_FILTER;
    }

    @Override
    public String getFilterData() {
        return "";
    }

    @Override
    public String getRawFilterData() {
        return "";
    }
}
