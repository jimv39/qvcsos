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

import com.qumasoft.qvcslib.LabelInfo;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import mockit.Expectations;
import mockit.Mocked;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the without label file filter.
 * @author JimVoris
 */
public class FileFilterWithoutLabelFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    LogfileInfo logfileInfo;

    @Mocked
    LogFileHeaderInfo logFileHeaderInfo;

    @Mocked
    LabelInfo labelInfo;

    public FileFilterWithoutLabelFilterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
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
            mergedInfo.getLogfileInfo();
            result = logfileInfo;
            logfileInfo.getLogFileHeaderInfo();
            result = logFileHeaderInfo;
            logFileHeaderInfo.getLabelInfo();
            result = labels;
            labelInfo.getLabelString();
            result = "Test-Label";
        }};
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("No-Test-Label", true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testNotPassesFilter() {
        System.out.println("passesFilter");
        final LabelInfo[] labels = new LabelInfo[1];
        labels[0] = labelInfo;
        // New expectations...
        new Expectations() {{
            mergedInfo.getLogfileInfo();
            result = logfileInfo;
            logfileInfo.getLogFileHeaderInfo();
            result = logFileHeaderInfo;
            logFileHeaderInfo.getLabelInfo();
            result = labels;
            labelInfo.getLabelString();
            result = "Test-Label";
        }};
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("Test-Label", true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("Test-Label", true);
        String expResult = QVCSConstants.WITHOUT_LABEL_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("Test-Label", true);
        String expResult = QVCSConstants.WITHOUT_LABEL_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("Test-Label", true);
        String expResult = "Test-Label";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("Test-Label", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testEqualsOnSameObject() {
        System.out.println("equals on same object");
        Object o = null;
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("Test-Label", true);
        boolean expResult = true;
        boolean result = instance.equals(instance);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testEqualsOnAlmostSameObject() {
        System.out.println("equals on almost same object");
        Object o = null;
        FileFilterWithoutLabelFilter instanceA = new FileFilterWithoutLabelFilter("Test-Label", true);
        FileFilterWithoutLabelFilter instanceB = new FileFilterWithoutLabelFilter("Test-Label", false);
        boolean expResult = false;
        boolean result = instanceA.equals(instanceB);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("Who cares", true);
        int expResult = -1026842288;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testHashCodesDifferent() {
        System.out.println("hashCode");
        FileFilterWithoutLabelFilter instanceA = new FileFilterWithoutLabelFilter("Who cares", true);
        FileFilterWithoutLabelFilter instanceB = new FileFilterWithoutLabelFilter("Who cares", false);
        int resultA = instanceA.hashCode();
        int resultB = instanceB.hashCode();
        assertNotEquals(resultA, resultB);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("Who cares", true);
        String expResult = "Who cares";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterWithoutLabelFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterWithoutLabelFilter instance = new FileFilterWithoutLabelFilter("Who cares", true);
        boolean expResult = false;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
