package com.qumasoft.guitools.qwin.filefilter;

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
import com.qumasoft.guitools.qwin.revisionfilter.FilteredRevisionInfo;
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterUptoLabelFilter;
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.TreeMap;
import mockit.Expectations;
import mockit.Mocked;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Use the JMockit mocking framework to test the up to label filter.
 *
 * @author Jim Voris
 */
public class FileFilterUptoLabelFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    LogfileInfo logfileInfo;

    @Mocked
    LogFileHeaderInfo logFileHeaderInfo;

    @Mocked
    LabelInfo labelInfo;

    @Mocked
    RevisionHeader revHeader;

    @Mocked
    RevisionFilterUptoLabelFilter revisionFilterUptoLabelFilter;

    public FileFilterUptoLabelFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterUptoLabelFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {
            {
                new RevisionFilterUptoLabelFilter("No-Test-Label", true);
                mergedInfo.getLogfileInfo();
                result = logfileInfo;
                logfileInfo.getLogFileHeaderInfo();
                result = logFileHeaderInfo;
                logFileHeaderInfo.getLabelInfo();
                result = labels;
                labelInfo.getLabelString();
                result = "No-Test-Label";
                revisionFilterUptoLabelFilter.passesFilter((FilteredRevisionInfo) any);
                result = true;
            }
        };
        FileFilterUptoLabelFilter instance = new FileFilterUptoLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterUptoLabelFilter.
     */
    @Test
    public void testNoPassesFilter() {
        System.out.println("no passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {
            {
                new RevisionFilterUptoLabelFilter("No-Test-Label", true);
                mergedInfo.getLogfileInfo();
                result = logfileInfo;
                logfileInfo.getLogFileHeaderInfo();
                result = logFileHeaderInfo;
                logFileHeaderInfo.getLabelInfo();
                result = labels;
                labelInfo.getLabelString();
                result = "No-Test-Label";
                revisionFilterUptoLabelFilter.passesFilter((FilteredRevisionInfo) any);
                result = false;
            }
        };
        FileFilterUptoLabelFilter instance = new FileFilterUptoLabelFilter("No-Test-Label", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterUptoLabelFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterUptoLabelFilter instance = new FileFilterUptoLabelFilter("No-Test-Label", true);
        String expResult = QVCSConstants.UPTO_LABEL_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterUptoLabelFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterUptoLabelFilter instance = new FileFilterUptoLabelFilter("No-Test-Label", true);
        String expResult = QVCSConstants.UPTO_LABEL_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterUptoLabelFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterUptoLabelFilter instance = new FileFilterUptoLabelFilter("No-Test-Label", true);
        String expResult = "No-Test-Label";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterUptoLabelFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterUptoLabelFilter instance = new FileFilterUptoLabelFilter("No-Test-Label", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterUptoLabelFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterUptoLabelFilter instance = new FileFilterUptoLabelFilter("No-Test-Label", true);
        int expResult = -924865847;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterUptoLabelFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterUptoLabelFilter instance = new FileFilterUptoLabelFilter("No-Test-Label", true);
        String expResult = "No-Test-Label";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterUptoLabelFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterUptoLabelFilter instance = new FileFilterUptoLabelFilter("No-Test-Label", true);
        boolean expResult = true;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
