/*   Copyright 2014 Jim Voris
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

import com.qumasoft.guitools.qwin.revisionfilter.FilteredRevisionInfo;
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterAfterLabelIncludeMissingFilter;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import java.util.TreeMap;
import mockit.Expectations;
import mockit.Mocked;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Use JMockit mocking library to test after label include missing filter.
 * @author JimVoris
 */
public class FileFilterAfterLabelIncludeMissingFilterTest {

    public FileFilterAfterLabelIncludeMissingFilterTest() {
    }

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    LogfileInfo logfileInfo;

    @Mocked
    LogFileHeaderInfo logFileHeaderInfo;

    @Mocked
    LabelInfo labelInfo;

    @Mocked
    RevisionInformation revisionInformation;

    @Mocked
    RevisionHeader revHeader;

    @Mocked
    RevisionHeader branchRevHeader;

    @Mocked
    RevisionFilterAfterLabelIncludeMissingFilter revisionFilterAfterLabelIncludeMissingFilter;

    /**
     * Test of passesFilter method, of class FileFilterAfterLabelIncludeMissingFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelIncludeMissingFilter("No-Test-Label", true);
            mergedInfo.getLogfileInfo();
            result = logfileInfo;
            logfileInfo.getLogFileHeaderInfo();
            result = logFileHeaderInfo;
            logFileHeaderInfo.getLabelInfo();
            result = labels;
            labelInfo.getLabelString();
            result = "No-Test-Label";
            labelInfo.getLabelRevisionString();
            result = "1.5";
            logfileInfo.getRevisionInformation();
            result = revisionInformation;
            revisionInformation.getRevisionIndex("1.5");
            result = 1;
            revisionInformation.getRevisionHeader(1);
            result = revHeader;
            revHeader.getDepth();
            result = 0;
            revisionFilterAfterLabelIncludeMissingFilter.passesFilter((FilteredRevisionInfo) any);
            result = true;
        }};
        FileFilterAfterLabelIncludeMissingFilter instance = new FileFilterAfterLabelIncludeMissingFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterAfterLabelIncludeMissingFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterAfterLabelIncludeMissingFilter instance = new FileFilterAfterLabelIncludeMissingFilter("Test-Label", true);
        String expResult = QVCSConstants.AFTER_LABEL_FILTER_INCLUDE_MISSING;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterAfterLabelIncludeMissingFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterAfterLabelIncludeMissingFilter instance = new FileFilterAfterLabelIncludeMissingFilter("Test-Label", true);
        String expResult = QVCSConstants.AFTER_LABEL_FILTER_INCLUDE_MISSING;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterAfterLabelIncludeMissingFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterAfterLabelIncludeMissingFilter instance = new FileFilterAfterLabelIncludeMissingFilter("Test-Label", true);
        String expResult = "Test-Label";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterAfterLabelIncludeMissingFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterAfterLabelIncludeMissingFilter instance = new FileFilterAfterLabelIncludeMissingFilter("Test-Label", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterAfterLabelIncludeMissingFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterAfterLabelIncludeMissingFilter instance = new FileFilterAfterLabelIncludeMissingFilter("Who cares", true);
        int expResult = -1026842140;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterAfterLabelIncludeMissingFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterAfterLabelIncludeMissingFilter instance = new FileFilterAfterLabelIncludeMissingFilter("Who cares", true);
        String expResult = "Who cares";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterAfterLabelIncludeMissingFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterAfterLabelIncludeMissingFilter instance = new FileFilterAfterLabelIncludeMissingFilter("Who cares", true);
        boolean expResult = true;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
