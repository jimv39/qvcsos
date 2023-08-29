/*   Copyright 2004-2023 Jim Voris
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

import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.QVCSConstants;

/**
 * Filter factory.
 * @author Jim Voris
 */
public final class FilterFactory {

    // Hide the default constructor.
    private FilterFactory() {
    }

    /**
     * Build a file filter based on the supplied parameters.
     * @param filterType the type of file filter.
     * @param filterData the data used to build the filter.
     * @param isANDFilter flag to indicate if this is an AND filter.
     * @param filterId the id of the filter instance that we create.
     * @return a file filter.
     */
    public static FileFilterInterface buildFilter(String filterType, String filterData, boolean isANDFilter, Integer filterId) {
        FileFilterInterface filter = null;
        switch (filterType) {
            case QVCSConstants.EXTENSION_FILTER:
                filter = new FileFilterExtensionFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.EXCLUDE_EXTENSION_FILTER:
                filter = new FileFilterExcludeExtensionFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.REG_EXP_FILENAME_FILTER:
                filter = new FileFilterRegularExpressionFilenameFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.EXCLUDE_REG_EXP_FILENAME_FILTER:
                filter = new FileFilterExcludeRegularExpressionFilenameFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.REG_EXP_REV_DESC_FILTER:
                filter = new FileFilterRevisionDescriptionRegExpressionFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER:
                filter = new FileFilterExcludeRevisionDescriptionRegExpressionFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.STATUS_FILTER:
                filter = new FileFilterStatusFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.EXCLUDE_STATUS_FILTER:
                filter = new FileFilterExcludeStatusFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.CHECKED_IN_AFTER_COMMIT_ID_FILTER:
                filter = new FileFilterCheckedInAfterCommitFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.CHECKED_IN_BEFORE_COMMIT_ID_FILTER:
                filter = new FileFilterCheckedInBeforeCommitFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.FILESIZE_GREATER_THAN_FILTER:
                filter = new FileFilterFilesizeGreaterThanFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.FILESIZE_LESS_THAN_FILTER:
                filter = new FileFilterFilesizeLessThanFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.LAST_EDIT_BY_FILTER:
                filter = new FileFilterLastEditByFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.EXCLUDE_LAST_EDIT_BY_FILTER:
                filter = new FileFilterExcludeLastEditByFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER:
                filter = new FileFilterExcludeWorkfileFilter(isANDFilter, filterId);
                break;
            case QVCSConstants.SEARCH_COMMIT_MESSAGES_FILTER:
                filter = new FileFilterSearchCommitMessageFilter(filterData, isANDFilter, filterId);
                break;
            case QVCSConstants.BY_COMMIT_ID_FILTER:
                filter = new FileFilterByCommitIdFilter(isANDFilter, filterId);
                break;
            default:
                warnProblem("Internal error: Unsupported filter type: " + filterType);
                break;
        }
        return filter;
    }
}
