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
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterAfterLabelFilter;
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
 * Use JMockit mocking library to test after label filter.
 * @author JimVoris
 */
public class FileFilterAfterLabelFilterTest {

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
    RevisionFilterAfterLabelFilter revisionFilterAfterLabelFilter;

    public FileFilterAfterLabelFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
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
            revisionFilterAfterLabelFilter.passesFilter((FilteredRevisionInfo) any);
            result = true;
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilterLabelMismatch() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
            mergedInfo.getLogfileInfo();
            result = logfileInfo;
            logfileInfo.getLogFileHeaderInfo();
            result = logFileHeaderInfo;
            logFileHeaderInfo.getLabelInfo();
            result = labels;
            labelInfo.getLabelString();
            result = "Test-Label";
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilterRevisionNotFound() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
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
            result = -1;
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilterNoLogfileInfo() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
            mergedInfo.getLogfileInfo();
            result = null;
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilterNoLabels() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
            mergedInfo.getLogfileInfo();
            result = logfileInfo;
            logfileInfo.getLogFileHeaderInfo();
            result = logFileHeaderInfo;
            logFileHeaderInfo.getLabelInfo();
            result = null;
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilterTipRevision() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
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
            result = 0;
            revisionInformation.getRevisionHeader(0);
            result = revHeader;
            revHeader.getDepth();
            result = 0;
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilterDepthGTZero() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
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
            result = 1;
            revisionInformation.getRevisionHeader(2);
            result = branchRevHeader;
            branchRevHeader.getDepth();
            result = 1;
            revHeader.getDepth();
            result = 1;
            revisionFilterAfterLabelFilter.passesFilter((FilteredRevisionInfo) any);
            result = true;
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilterDepthGTZeroFailsRevFilter() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
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
            result = 1;
            revisionInformation.getRevisionHeader(2);
            result = branchRevHeader;
            branchRevHeader.getDepth();
            result = 1;
            revHeader.getDepth();
            result = 1;
            revisionFilterAfterLabelFilter.passesFilter((FilteredRevisionInfo) any);
            result = false;
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilterDepthGTZeroNextRevDepthZero() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
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
            result = 1;
            revisionInformation.getRevisionHeader(2);
            result = branchRevHeader;
            branchRevHeader.getDepth();
            result = 0;
            revHeader.getDepth();
            result = 1;
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testPassesFilterDepthGTZeroNullRevHeader() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            new RevisionFilterAfterLabelFilter("No-Test-Label", true);
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
            result = 1;
            revisionInformation.getRevisionHeader(2);
            result = null;
        }};
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Test-Label", true);
        String expResult = QVCSConstants.AFTER_LABEL_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Test-Label", true);
        String expResult = QVCSConstants.AFTER_LABEL_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Test-Label", true);
        String expResult = "Test-Label";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Test-Label", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testEqualsToSelf() {
        System.out.println("equals to self");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Test-Label", true);
        boolean expResult = true;
        boolean result = instance.equals(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testEqualsToDiffOther() {
        System.out.println("equals to diff other");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Test-Label", true);
        FileFilterAfterLabelFilter other = new FileFilterAfterLabelFilter("Diff-Test-Label", true);
        boolean expResult = false;
        boolean result = instance.equals(other);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testEqualsToDiffAND() {
        System.out.println("equals to diff other");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Test-Label", true);
        FileFilterAfterLabelFilter other = new FileFilterAfterLabelFilter("Test-Label", false);
        boolean expResult = false;
        boolean result = instance.equals(other);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Who cares", true);
        int expResult = -1026842140;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testHashCodeForOR() {
        System.out.println("hashCode");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Who cares", false);
        int expResult = -1026842237;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Who cares", true);
        String expResult = "Who cares";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterAfterLabelFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterAfterLabelFilter instance = new FileFilterAfterLabelFilter("Who cares", true);
        boolean expResult = true;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
