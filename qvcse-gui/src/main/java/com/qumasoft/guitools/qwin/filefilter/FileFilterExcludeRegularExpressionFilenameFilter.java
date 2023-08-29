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

import com.qumasoft.qvcslib.CommonFilterFile;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Exclude regular expression in filename filter.
 * @author Jim Voris
 */
public class FileFilterExcludeRegularExpressionFilenameFilter extends AbstractFileFilter {
    private static final long serialVersionUID = 3524045531558865970L;

    private Pattern filterRegularExpressionPattern;
    private String filterData;

    FileFilterExcludeRegularExpressionFilenameFilter(String regularExpression, boolean isANDFilter, Integer id) {
        super(isANDFilter, id, CommonFilterFile.EXCLUDE_REGEX_FILENAME);
        try {
            filterRegularExpressionPattern = Pattern.compile(regularExpression);
            filterData = regularExpression;
        } catch (PatternSyntaxException e) {
            filterData = "Bad Expression: [" + regularExpression + "] exception: " + e.getLocalizedMessage();
        }
    }

    @Override
    public boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap) {
        boolean retVal;
        String shortWorkfileName = mergedInfo.getShortWorkfileName();
        Matcher matcher = filterRegularExpressionPattern.matcher(shortWorkfileName);
        retVal = matcher.matches();
        return !retVal;
    }

    @Override
    public String getFilterType() {
        return QVCSConstants.EXCLUDE_REG_EXP_FILENAME_FILTER;
    }

    @Override
    public String toString() {
        return QVCSConstants.EXCLUDE_REG_EXP_FILENAME_FILTER;
    }

    @Override
    public String getFilterData() {
        return filterData;
    }

    @Override
    public boolean equals(Object o) {
        boolean retVal = false;
        if (o instanceof FileFilterExcludeRegularExpressionFilenameFilter) {
            FileFilterExcludeRegularExpressionFilenameFilter filter = (FileFilterExcludeRegularExpressionFilenameFilter) o;
            if (filter.getFilterData().equals(getFilterData()) && (filter.getIsANDFilter() == this.getIsANDFilter())) {
                retVal = true;
            }
        }
        return retVal;
    }

    @Override
    public int hashCode() {
        return computeHash(this.filterData, this.getIsANDFilter());
    }

    @Override
    public String getRawFilterData() {
        return filterData;
    }
}
