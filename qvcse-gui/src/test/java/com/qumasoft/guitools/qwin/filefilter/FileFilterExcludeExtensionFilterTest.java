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

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

/**
 * Use JMockit mocking library to test file exclude extension filter.
 * @author JimVoris
 */
public class FileFilterExcludeExtensionFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    AbstractProjectProperties projectProperties;

    public FileFilterExcludeExtensionFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterExcludeExtensionFilter.
     */
    @Test
    public void testNotPassesFilter() {
        System.out.println("not passesFilter");
        // New expectations...
        new NonStrictExpectations() {{
            mergedInfo.getShortWorkfileName();
            result = "foo.bar";
            mergedInfo.getProjectProperties();
            result = projectProperties;
            projectProperties.getIgnoreCaseFlag();
            result = true;
        }};
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".bar", true);
        boolean expResult = false;
        boolean result = instance.passesFilter(mergedInfo, null);
        assertEquals(expResult, result);
    }

    /**
     * Test of passesFilter method, of class FileFilterExcludeExtensionFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        // New expectations...
        new NonStrictExpectations() {{
            mergedInfo.getShortWorkfileName();
            result = "foo.bar";
            mergedInfo.getProjectProperties();
            result = projectProperties;
            projectProperties.getIgnoreCaseFlag();
            result = true;
        }};
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".foo", true);
        boolean expResult = true;
        boolean result = instance.passesFilter(mergedInfo, null);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterType method, of class FileFilterExcludeExtensionFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".bar", true);
        String expResult = QVCSConstants.EXCLUDE_EXTENSION_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterExcludeExtensionFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".bar", true);
        String expResult = QVCSConstants.EXCLUDE_EXTENSION_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterExcludeExtensionFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".bar", true);
        String expResult = ".bar";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterExcludeExtensionFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = new FileFilterExcludeExtensionFilter(".bar", true);
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".bar", true);
        boolean expResult = true;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testEqualsToNull() {
        System.out.println("equals to null");
        Object o = null;
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".bar", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testNotEquals() {
        System.out.println("no equals");
        Object o = new FileFilterExcludeExtensionFilter(".bar", true);
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".foo", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testNotEquals2() {
        System.out.println("no equals AND different");
        Object o = new FileFilterExcludeExtensionFilter(".bar", true);
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".bar", false);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterExcludeExtensionFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".bar", true);
        int expResult = 1468041;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testDiffHashCode() {
        System.out.println("diff hashCode");
        FileFilterExcludeExtensionFilter instanceA = new FileFilterExcludeExtensionFilter(".bar", true);
        FileFilterExcludeExtensionFilter instanceB = new FileFilterExcludeExtensionFilter(".bar", false);
        assertNotEquals(instanceA.hashCode(), instanceB.hashCode());
    }

    /**
     * Test of getRawFilterData method, of class FileFilterExcludeExtensionFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterExcludeExtensionFilter instance = new FileFilterExcludeExtensionFilter(".bar", true);
        String expResult = ".bar";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

}
