//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.guitools.qwin;

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

    static FileFilterInterface buildFilter(String filterType, String filterData, boolean isANDFilter) {
        FileFilterInterface filter = null;

        if (filterType.equals(QVCSConstants.EXCLUDE_OBSOLETE_FILTER)) {
            filter = new FileFilterExcludeObsoleteFilter(isANDFilter);
        } else if (filterType.equals(QVCSConstants.OBSOLETE_FILTER)) {
            filter = new FileFilterObsoleteFilter(isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXTENSION_FILTER)) {
            filter = new FileFilterExtensionFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_EXTENSION_FILTER)) {
            filter = new FileFilterExcludeExtensionFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.REG_EXP_FILENAME_FILTER)) {
            filter = new FileFilterRegularExpressionFilenameFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_REG_EXP_FILENAME_FILTER)) {
            filter = new FileFilterExcludeRegularExpressionFilenameFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.REG_EXP_REV_DESC_FILTER)) {
            filter = new FileFilterRevisionDescriptionRegExpressionFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER)) {
            filter = new FileFilterExcludeRevisionDescriptionRegExpressionFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.STATUS_FILTER)) {
            filter = new FileFilterStatusFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_STATUS_FILTER)) {
            filter = new FileFilterExcludeStatusFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.LOCKED_BY_FILTER)) {
            filter = new FileFilterLockedByFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_LOCKED_BY_FILTER)) {
            filter = new FileFilterExcludeLockedByFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.CHECKED_IN_AFTER_FILTER)) {
            filter = new FileFilterCheckedInAfterFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.CHECKED_IN_BEFORE_FILTER)) {
            filter = new FileFilterCheckedInBeforeFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.FILESIZE_GREATER_THAN_FILTER)) {
            filter = new FileFilterFilesizeGreaterThanFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.FILESIZE_LESS_THAN_FILTER)) {
            filter = new FileFilterFilesizeLessThanFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.LAST_EDIT_BY_FILTER)) {
            filter = new FileFilterLastEditByFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_LAST_EDIT_BY_FILTER)) {
            filter = new FileFilterExcludeLastEditByFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.WITH_LABEL_FILTER)) {
            filter = new FileFilterWithLabelFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.WITH_LABEL_FILTER_WITH_ALL_REVISIONS)) {
            filter = new FileFilterWithLabelWithAllRevisionsFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.WITHOUT_LABEL_FILTER)) {
            filter = new FileFilterWithoutLabelFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.AFTER_LABEL_FILTER)) {
            filter = new FileFilterAfterLabelFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.AFTER_LABEL_FILTER_INCLUDE_MISSING)) {
            filter = new FileFilterAfterLabelIncludeMissingFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.UPTO_LABEL_FILTER)) {
            filter = new FileFilterUptoLabelFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER)) {
            filter = new FileFilterExcludeWorkfileFilter(isANDFilter);
        } else {
            QWinUtility.logProblem(Level.WARNING, "Internal error: Unsupported filter type: " + filterType);
        }
        return filter;
    }
}
