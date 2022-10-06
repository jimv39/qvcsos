/*
 * Copyright 2022 Jim Voris.
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
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.TreeMap;

/**
 *
 * @author Jim Voris
 */
public class FileFilterSearchCommitMessageFilter extends AbstractFileFilter {
    private final String searchByCommitMessage;

    public FileFilterSearchCommitMessageFilter(String searchString, boolean isANDFilter) {
        super(isANDFilter);
        this.searchByCommitMessage = searchString;
    }

    @Override
    public String getFilterData() {
        return QWinFrame.getQWinFrame().getCommitMessageSearchString();
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean flag = false;
        String searchString = QWinFrame.getQWinFrame().getCommitMessageSearchString();
        for (RevisionHeader revHeader : revisionHeaderMap.values()) {
            if (revHeader.getRevisionDescription().contains(searchString)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.SEARCH_COMMIT_MESSAGES_FILTER;
    }

    @Override
    public String getRawFilterData() {
        return QWinFrame.getQWinFrame().getCommitMessageSearchString();
    }

    @Override
    public boolean requiresRevisionDetailInfo() {
        return true;
    }

    @Override
    public String toString() {
        return QVCSConstants.SEARCH_COMMIT_MESSAGES_FILTER;
    }

}
