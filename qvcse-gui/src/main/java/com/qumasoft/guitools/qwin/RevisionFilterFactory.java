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

        if (filterType.equals(QVCSConstants.EXTENSION_FILTER)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.EXCLUDE_EXTENSION_FILTER)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.STATUS_FILTER)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.EXCLUDE_STATUS_FILTER)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.EXCLUDE_OBSOLETE_FILTER)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.REG_EXP_FILENAME_FILTER)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.EXCLUDE_REG_EXP_FILENAME_FILTER)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.REG_EXP_REV_DESC_FILTER)) {
            filter = new RevisionFilterRevDescRegExpressionFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER)) {
            filter = new RevisionFilterExcludeRevDescRegExpressionFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.LOCKED_BY_FILTER)) {
            filter = new RevisionFilterLockedByFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_LOCKED_BY_FILTER)) {
            filter = new RevisionFilterExcludeLockedByFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.CHECKED_IN_AFTER_FILTER)) {
            filter = new RevisionFilterCheckedInAfterFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.CHECKED_IN_BEFORE_FILTER)) {
            filter = new RevisionFilterCheckedInBeforeFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.FILESIZE_GREATER_THAN_FILTER)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.FILESIZE_LESS_THAN_FILTER)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.LAST_EDIT_BY_FILTER)) {
            filter = new RevisionFilterEditByFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_LAST_EDIT_BY_FILTER)) {
            filter = new RevisionFilterExcludeEditByFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.WITH_LABEL_FILTER)) {
            filter = new RevisionFilterWithLabelFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.WITH_LABEL_FILTER_WITH_ALL_REVISIONS)) {
            filter = null;
        } else if (filterType.equals(QVCSConstants.WITHOUT_LABEL_FILTER)) {
            filter = new RevisionFilterWithoutLabelFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.AFTER_LABEL_FILTER)) {
            filter = new RevisionFilterAfterLabelFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.AFTER_LABEL_FILTER_INCLUDE_MISSING)) {
            filter = new RevisionFilterAfterLabelIncludeMissingFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.UPTO_LABEL_FILTER)) {
            filter = new RevisionFilterUptoLabelFilter(filterData, isANDFilter);
        } else if (filterType.equals(QVCSConstants.EXCLUDE_UNCONTROLLED_FILE_FILTER)) {
            filter = null;
        } else {
            QWinUtility.logProblem(Level.WARNING, "Internal error: Unsupported revision filter type: " + filterType);
        }
        return filter;
    }
}
