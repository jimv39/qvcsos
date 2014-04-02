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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Use the JMockit mocking framework to test the exclude LastEditBy filter.
 * @author JimVoris
 */
public class FileFilterExcludeLastEditByFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    public FileFilterExcludeLastEditByFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterExcludeLastEditByFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        // New expectations...
        new NonStrictExpectations() {{
            mergedInfo.getLastEditBy();
            result = "Ralph";
        }};
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertTrue(passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterExcludeLastEditByFilter.
     */
    @Test
    public void testNotPassesFilter() {
        System.out.println("not passesFilter");
        // New expectations...
        new NonStrictExpectations() {{
            mergedInfo.getLastEditBy();
            result = "JimVoris";
        }};
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertFalse(passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterExcludeLastEditByFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        String expResult = QVCSConstants.EXCLUDE_LAST_EDIT_BY_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterExcludeLastEditByFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        String expResult = QVCSConstants.EXCLUDE_LAST_EDIT_BY_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterExcludeLastEditByFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        String expResult = "JimVoris";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterExcludeLastEditByFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
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
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
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
        Object o = new FileFilterExcludeLastEditByFilter("Ralph", true);
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testNotEquals2() {
        System.out.println("no equals2");
        Object o = new FileFilterExcludeLastEditByFilter("Ralph", true);
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("Ralph", false);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterExcludeLastEditByFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        int expResult = 2020972249;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testDiffHashCode() {
        System.out.println("diff hashCode");
        FileFilterExcludeLastEditByFilter instanceA = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        FileFilterExcludeLastEditByFilter instanceB = new FileFilterExcludeLastEditByFilter("JimVoris", false);
        assertNotEquals(instanceA.hashCode(), instanceB.hashCode());
    }

    /**
     * Test of getRawFilterData method, of class FileFilterExcludeLastEditByFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterExcludeLastEditByFilter instance = new FileFilterExcludeLastEditByFilter("JimVoris", true);
        String expResult = "JimVoris";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

}
