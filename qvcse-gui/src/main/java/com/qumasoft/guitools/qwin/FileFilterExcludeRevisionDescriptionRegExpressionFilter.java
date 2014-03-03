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

import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Iterator;
import java.util.Objects;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Exclude revision description regular expression filter.
 * @author Jim Voris
 */
public class FileFilterExcludeRevisionDescriptionRegExpressionFilter extends AbstractFileFilter {
    private static final long serialVersionUID = -4548699988289893541L;

    private Pattern filterRegularExpressionPattern;
    private String filterData;

    FileFilterExcludeRevisionDescriptionRegExpressionFilter(String regularExpression, boolean isANDFilter) {
        super(isANDFilter);
        try {
            filterRegularExpressionPattern = Pattern.compile(regularExpression);
            filterData = regularExpression;
        } catch (PatternSyntaxException e) {
            filterData = "Bad Expression: [" + regularExpression + "] exception: " + e.getLocalizedMessage();
        }
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = true;

        if (mergedInfo.getArchiveInfo() != null) {
            LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();

            Iterator<Integer> it = revisionHeaderMap.keySet().iterator();
            while (it.hasNext()) {
                Integer revisionIndexInteger = it.next();
                RevisionHeader revisionHeader = revisionHeaderMap.get(revisionIndexInteger);
                String revisionDescription = revisionHeader.getRevisionDescription();
                Matcher matcher = filterRegularExpressionPattern.matcher(revisionDescription);
                if (matcher.matches()) {
                    retVal = false;
                    break;
                }
            }
        } else {
            // If there is no archive for this file, then it won't pass the filter either.
            retVal = false;
        }
        return retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.REG_EXP_REV_DESC_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.REG_EXP_REV_DESC_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterData;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterExcludeRevisionDescriptionRegExpressionFilter) {
            FileFilterExcludeRevisionDescriptionRegExpressionFilter filter = (FileFilterExcludeRevisionDescriptionRegExpressionFilter) o;
            if (filter.getFilterData().equals(getFilterData())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 5;
        hash = 73 * hash + Objects.hashCode(this.filterData);
        // </editor-fold>
        return hash;
    }

    @Override
    public String getRawFilterData() {
        return filterData;
    }

    @Override
    public boolean requiresRevisionDetailInfo() {
        return true;
    }
}
