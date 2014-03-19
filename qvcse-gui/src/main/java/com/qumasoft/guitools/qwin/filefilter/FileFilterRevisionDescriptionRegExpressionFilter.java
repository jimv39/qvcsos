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
 * Revision description regular expression filter.
 * @author Jim Voris
 */
public class FileFilterRevisionDescriptionRegExpressionFilter extends AbstractFileFilter {
    private static final long serialVersionUID = 635377746996542233L;

    private transient Pattern filterRegularExpressionPattern;
    private String filterData;

    FileFilterRevisionDescriptionRegExpressionFilter(String regularExpression, boolean isANDFilter) {
        super(isANDFilter);
        try {
            filterRegularExpressionPattern = Pattern.compile(regularExpression);
            filterData = regularExpression;
        } catch (PatternSyntaxException e) {
            filterData = "Bad Expression [" + regularExpression + "] exception: " + e.getLocalizedMessage();
        }
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal = false;
        if (mergedInfo.getArchiveInfo() != null) {
            LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();

            Iterator<Integer> it = revisionHeaderMap.keySet().iterator();
            while (it.hasNext()) {
                Integer revisionIndexInteger = it.next();
                RevisionHeader revisionHeader = revisionHeaderMap.get(revisionIndexInteger);
                String revisionDescription = revisionHeader.getRevisionDescription();
                Matcher matcher = filterRegularExpressionPattern.matcher(revisionDescription);
                if (matcher.matches()) {
                    retVal = true;
                    break;
                }
            }
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
        if (o instanceof FileFilterRevisionDescriptionRegExpressionFilter) {
            FileFilterRevisionDescriptionRegExpressionFilter filter = (FileFilterRevisionDescriptionRegExpressionFilter) o;
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
        hash = 83 * hash + Objects.hashCode(this.filterData);
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
