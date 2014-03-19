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
package com.qumasoft.guitools.qwin.revisionfilter;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Exclude a revision description by regular expression revision filter.
 * @author Jim Voris
 */
public class RevisionFilterExcludeRevDescRegExpressionFilter extends AbstractRevisionFilter {

    private Pattern regularExpressionPattern;
    private String filterData;

    /**
     * Create an exclude revision description by regular expression revision filter.
     * @param regularExpression the regular expression that defines the filter.
     * @param isANDFilter is this an 'AND' filter.
     */
    public RevisionFilterExcludeRevDescRegExpressionFilter(String regularExpression, boolean isANDFilter) {
        super(isANDFilter);
        try {
            regularExpressionPattern = Pattern.compile(regularExpression);
            filterData = regularExpression;
        } catch (PatternSyntaxException e) {
            filterData = "Bad Expression: [" + regularExpression + "] exception: " + e.getLocalizedMessage();
        }
    }

    @Override
    public boolean passesFilter(FilteredRevisionInfo filteredRevisionInfo) {
        boolean retVal = true;
        RevisionHeader filteredRevision = filteredRevisionInfo.getRevisionHeader();
        String revisionDescription = filteredRevision.getRevisionDescription();
        Matcher matcher = regularExpressionPattern.matcher(revisionDescription);
        if (matcher.matches()) {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterData;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof RevisionFilterExcludeRevDescRegExpressionFilter) {
            RevisionFilterExcludeRevDescRegExpressionFilter filter = (RevisionFilterExcludeRevDescRegExpressionFilter) o;
            if (filter.getFilterData().equals(getFilterData())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.filterData);
        // </editor-fold>
        return hash;
    }
}
