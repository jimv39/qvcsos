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
package com.qumasoft.guitools.qwin.revisionfilter;

import com.qumasoft.guitools.qwin.QWinFrame;
import com.qumasoft.qvcslib.QVCSConstants;

/**
 *
 * @author Jim Voris.
 */
public class RevisionFilterSearchCommitMessageFilter extends AbstractRevisionFilter {

    public RevisionFilterSearchCommitMessageFilter(String filterData, boolean andFilter) {
        super(andFilter);
    }

    @Override
    public boolean passesFilter(FilteredRevisionInfo filteredRevisionInfo) {
        boolean flag = false;
        String searchString = QWinFrame.getQWinFrame().getCommitMessageSearchString();
        if (filteredRevisionInfo.getRevisionHeader().getRevisionDescription().contains(searchString)) {
            flag = true;
        }
        return flag;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.SEARCH_COMMIT_MESSAGES_FILTER;
    }

    @Override
    public String getFilterData() {
        return QWinFrame.getQWinFrame().getCommitMessageSearchString();
    }

}
