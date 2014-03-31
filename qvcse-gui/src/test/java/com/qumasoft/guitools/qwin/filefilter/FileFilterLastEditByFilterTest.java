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
import mockit.Expectations;
import mockit.Mocked;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Use the JMockit mocking framework to test the LastEditBy filter.
 * @author JimVoris
 */
public class FileFilterLastEditByFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    public FileFilterLastEditByFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        // New expectations...
        new Expectations() {{
            mergedInfo.getLastEditBy();
            result = "JimVoris";
        }};
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertTrue(passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testNotPassesFilter() {
        System.out.println("not pass Filter");
        // New expectations...
        new Expectations() {{
            mergedInfo.getLastEditBy();
            result = "Ralph";
        }};
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertTrue(!passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
        String expResult = QVCSConstants.LAST_EDIT_BY_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
        String expResult = QVCSConstants.LAST_EDIT_BY_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
        String expResult = "JimVoris";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = new FileFilterLastEditByFilter("JimVoris", true);
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
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
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
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
        Object o = new FileFilterLastEditByFilter("Ralph", true);
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
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
        Object o = new FileFilterLastEditByFilter("Ralph", true);
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("Ralph", false);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
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
        FileFilterLastEditByFilter instanceA = new FileFilterLastEditByFilter("JimVoris", true);
        FileFilterLastEditByFilter instanceB = new FileFilterLastEditByFilter("JimVoris", false);
        assertNotEquals(instanceA.hashCode(), instanceB.hashCode());
    }

    /**
     * Test of getRawFilterData method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterLastEditByFilter instance = new FileFilterLastEditByFilter("JimVoris", true);
        String expResult = "JimVoris";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

}
