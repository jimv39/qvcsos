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

import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

/**
 * Use JMockit mocking library to test exclude Status filter.
 * @author JimVoris
 */
public class FileFilterExcludeStatusFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    public FileFilterExcludeStatusFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterExcludeStatusFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        // New expectations...
        new NonStrictExpectations() {{
            mergedInfo.getStatusString();
            result = "Stale";
        }};
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Current", true);
        boolean expResult = true;
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertEquals(expResult, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterExcludeStatusFilter.
     */
    @Test
    public void testNotPassesFilter() {
        System.out.println("not passesFilter");
        // New expectations...
        new NonStrictExpectations() {{
            mergedInfo.getStatusString();
            result = "Stale";
        }};
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Stale", true);
        boolean expResult = false;
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertEquals(expResult, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterExcludeStatusFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Current", true);
        String expResult = QVCSConstants.EXCLUDE_STATUS_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterExcludeStatusFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Current", true);
        String expResult = QVCSConstants.EXCLUDE_STATUS_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterExcludeStatusFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Current", true);
        String expResult = "Current";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterExcludeStatusFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = new FileFilterExcludeStatusFilter("Current", true);
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Current", true);
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
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Current", true);
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
        Object o = new FileFilterExcludeStatusFilter("Current", true);
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Stale", true);
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
        Object o = new FileFilterExcludeStatusFilter("Current", true);
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Current", false);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterExcludeStatusFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Stale", false);
        int expResult = 80204924;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testDiffHashCode() {
        System.out.println("diff hashCode");
        FileFilterExcludeStatusFilter instanceA = new FileFilterExcludeStatusFilter("Stale", true);
        FileFilterExcludeStatusFilter instanceB = new FileFilterExcludeStatusFilter("Stale", false);
        assertNotEquals(instanceA.hashCode(), instanceB.hashCode());
    }

    /**
     * Test of getRawFilterData method, of class FileFilterExcludeStatusFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterExcludeStatusFilter instance = new FileFilterExcludeStatusFilter("Current", true);
        String expResult = "Current";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

}
