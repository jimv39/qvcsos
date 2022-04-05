/*   Copyright 2004-2015 Jim Voris
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

import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.qvcslib.QVCSConstants;

/**
 * Revision filter factory.
 * @author Jim Voris
 */
public final class RevisionFilterFactory {

    // Hide the default constructor.
    private RevisionFilterFactory() {
    }

    /**
     * Build a revision filter.
     * @param filterType the type of filter to create.
     * @param filterData the filter data used to define the filter.
     * @param isANDFilter is this an 'AND' type of filter.
     * @return the revision filter.
     */
    public static RevisionFilterInterface buildFilter(String filterType, String filterData, boolean isANDFilter) {
        RevisionFilterInterface filter = null;

        switch (filterType) {
            case QVCSConstants.EXTENSION_FILTER:
                filter = null;
                break;
            case QVCSConstants.EXCLUDE_EXTENSION_FILTER:
                filter = null;
                break;
            case QVCSConstants.STATUS_FILTER:
                filter = null;
                break;
            case QVCSConstants.EXCLUDE_STATUS_FILTER:
                filter = null;
                break;
            case QVCSConstants.EXCLUDE_OBSOLETE_FILTER:
                filter = null;
                break;
            case QVCSConstants.REG_EXP_FILENAME_FILTER:
                filter = null;
                break;
            case QVCSConstants.EXCLUDE_REG_EXP_FILENAME_FILTER:
                filter = null;
                break;
            case QVCSConstants.REG_EXP_REV_DESC_FILTER:
                filter = new RevisionFilterRevDescRegExpressionFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER:
                filter = new RevisionFilterExcludeRevDescRegExpressionFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.CHECKED_IN_AFTER_FILTER:
                filter = new RevisionFilterCheckedInAfterFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.CHECKED_IN_AFTER_COMMIT_ID_FILTER:
                filter = new RevisionFilterCheckedInAfterCommitFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.CHECKED_IN_BEFORE_FILTER:
                filter = new RevisionFilterCheckedInBeforeFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.CHECKED_IN_BEFORE_COMMIT_ID_FILTER:
                filter = new RevisionFilterCheckedInBeforeCommitFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.FILESIZE_GREATER_THAN_FILTER:
                filter = null;
                break;
            case QVCSConstants.FILESIZE_LESS_THAN_FILTER:
                filter = null;
                break;
            case QVCSConstants.LAST_EDIT_BY_FILTER:
                filter = new RevisionFilterEditByFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_LAST_EDIT_BY_FILTER:
                filter = new RevisionFilterExcludeEditByFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER:
                filter = null;
                break;
            default:
                warnProblem("Internal error: Unsupported revision filter type: " + filterType);
                break;
        }
        return filter;
    }
}
