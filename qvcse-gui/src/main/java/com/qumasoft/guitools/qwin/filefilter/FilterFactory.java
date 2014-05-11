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

import com.qumasoft.guitools.qwin.QWinUtility;
import com.qumasoft.qvcslib.QVCSConstants;
import java.util.logging.Level;

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
     * @return a file filter.
     */
    public static FileFilterInterface buildFilter(String filterType, String filterData, boolean isANDFilter) {
        FileFilterInterface filter = null;
        switch (filterType) {
            case QVCSConstants.EXCLUDE_OBSOLETE_FILTER:
                filter = new FileFilterExcludeObsoleteFilter(isANDFilter);
                break;
            case QVCSConstants.OBSOLETE_FILTER:
                filter = new FileFilterObsoleteFilter(isANDFilter);
                break;
            case QVCSConstants.EXTENSION_FILTER:
                filter = new FileFilterExtensionFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_EXTENSION_FILTER:
                filter = new FileFilterExcludeExtensionFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.REG_EXP_FILENAME_FILTER:
                filter = new FileFilterRegularExpressionFilenameFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_REG_EXP_FILENAME_FILTER:
                filter = new FileFilterExcludeRegularExpressionFilenameFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.REG_EXP_REV_DESC_FILTER:
                filter = new FileFilterRevisionDescriptionRegExpressionFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER:
                filter = new FileFilterExcludeRevisionDescriptionRegExpressionFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.STATUS_FILTER:
                filter = new FileFilterStatusFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_STATUS_FILTER:
                filter = new FileFilterExcludeStatusFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.LOCKED_BY_FILTER:
                filter = new FileFilterLockedByFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_LOCKED_BY_FILTER:
                filter = new FileFilterExcludeLockedByFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.CHECKED_IN_AFTER_FILTER:
                filter = new FileFilterCheckedInAfterFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.CHECKED_IN_BEFORE_FILTER:
                filter = new FileFilterCheckedInBeforeFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.FILESIZE_GREATER_THAN_FILTER:
                filter = new FileFilterFilesizeGreaterThanFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.FILESIZE_LESS_THAN_FILTER:
                filter = new FileFilterFilesizeLessThanFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.LAST_EDIT_BY_FILTER:
                filter = new FileFilterLastEditByFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_LAST_EDIT_BY_FILTER:
                filter = new FileFilterExcludeLastEditByFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.WITH_LABEL_FILTER:
                filter = new FileFilterWithLabelFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.WITHOUT_LABEL_FILTER:
                filter = new FileFilterWithoutLabelFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.AFTER_LABEL_FILTER:
                filter = new FileFilterAfterLabelFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.AFTER_LABEL_FILTER_INCLUDE_MISSING:
                filter = new FileFilterAfterLabelIncludeMissingFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.UPTO_LABEL_FILTER:
                filter = new FileFilterUptoLabelFilter(filterData, isANDFilter);
                break;
            case QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER:
                filter = new FileFilterExcludeWorkfileFilter(isANDFilter);
                break;
            default:
                QWinUtility.logProblem(Level.WARNING, "Internal error: Unsupported filter type: " + filterType);
                break;
        }
        return filter;
    }
}
