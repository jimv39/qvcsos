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
import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import mockit.Expectations;
import mockit.Mocked;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Use the JMockit mocking framework to test the with label filter.
 *
 * @author Jim Voris
 */
public class FileFilterWithLabelFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    LogfileInfo logfileInfo;

    @Mocked
    LogFileHeaderInfo logFileHeaderInfo;

    @Mocked
    LabelInfo labelInfo;

    public FileFilterWithLabelFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterWithLabelFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {
            {
                mergedInfo.getLogfileInfo();
                result = logfileInfo;
                logfileInfo.getLogFileHeaderInfo();
                result = logFileHeaderInfo;
                logFileHeaderInfo.getLabelInfo();
                result = labels;
                labelInfo.getLabelString();
                result = "Test-Label";
            }
        };
        FileFilterWithLabelFilter instance = new FileFilterWithLabelFilter("Test-Label", true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithLabelFilter.
     */
    @Test
    public void testNoPassesFilter() {
        System.out.println("no passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {
            {
                mergedInfo.getLogfileInfo();
                result = logfileInfo;
                logfileInfo.getLogFileHeaderInfo();
                result = logFileHeaderInfo;
                logFileHeaderInfo.getLabelInfo();
                result = labels;
                labelInfo.getLabelString();
                result = "No-Match-Label";
            }
        };
        FileFilterWithLabelFilter instance = new FileFilterWithLabelFilter("Test-Label", true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterWithLabelFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterWithLabelFilter instance = new FileFilterWithLabelFilter("Test-Label", true);
        String expResult = QVCSConstants.WITH_LABEL_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterWithLabelFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterWithLabelFilter instance = new FileFilterWithLabelFilter("Test-Label", true);
        String expResult = QVCSConstants.WITH_LABEL_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterWithLabelFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterWithLabelFilter instance = new FileFilterWithLabelFilter("Test-Label", true);
        String expResult = "Test-Label";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterWithLabelFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterWithLabelFilter instance = new FileFilterWithLabelFilter("Test-Label", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterWithLabelFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterWithLabelFilter instance = new FileFilterWithLabelFilter("Test-Label", true);
        int expResult = -1699115171;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterWithLabelFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterWithLabelFilter instance = new FileFilterWithLabelFilter("Test-Label", true);
        String expResult = "Test-Label";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterWithLabelFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterWithLabelFilter instance = new FileFilterWithLabelFilter("Test-Label", true);
        boolean expResult = true;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
