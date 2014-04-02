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
import com.qumasoft.qvcslib.WorkFile;
import mockit.Mocked;
import mockit.NonStrictExpectations;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import org.junit.Test;

/**
 * Use JMockit mocking library to test file size greater than filter.
 * @author JimVoris
 */
public class FileFilterFilesizeGreaterThanFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    WorkFile workFile;

    public FileFilterFilesizeGreaterThanFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterFilesizeGreaterThanFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        // New expectations...
        new NonStrictExpectations() {{
            mergedInfo.getWorkfile();
            result = workFile;
            mergedInfo.getWorkfileSize();
            result = 100;
        }};
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("99", true);
        boolean expResult = true;
        boolean result = instance.passesFilter(mergedInfo, null);
        assertEquals(expResult, result);
    }

    /**
     * Test of passesFilter method, of class FileFilterFilesizeGreaterThanFilter.
     */
    @Test
    public void testNotPassesFilter() {
        System.out.println("not passesFilter");
        // New expectations...
        new NonStrictExpectations() {{
            mergedInfo.getWorkfile();
            result = workFile;
            mergedInfo.getWorkfileSize();
            result = 100;
        }};
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("100", true);
        boolean expResult = false;
        boolean result = instance.passesFilter(mergedInfo, null);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterType method, of class FileFilterFilesizeGreaterThanFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("100", true);
        String expResult = QVCSConstants.FILESIZE_GREATER_THAN_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterFilesizeGreaterThanFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("100", true);
        String expResult = QVCSConstants.FILESIZE_GREATER_THAN_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterFilesizeGreaterThanFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("100", true);
        String expResult = "100";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterFilesizeGreaterThanFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = new FileFilterFilesizeGreaterThanFilter("100", true);
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("100", true);
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
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("100", true);
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
        Object o = new FileFilterFilesizeGreaterThanFilter("100", true);
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("101", true);
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
        Object o = new FileFilterFilesizeGreaterThanFilter("100", true);
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("100", false);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterFilesizeGreaterThanFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("100", true);
        int expResult = 666;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterLastEditByFilter.
     */
    @Test
    public void testDiffHashCode() {
        System.out.println("diff hashCode");
        FileFilterFilesizeGreaterThanFilter instanceA = new FileFilterFilesizeGreaterThanFilter("100", true);
        FileFilterFilesizeGreaterThanFilter instanceB = new FileFilterFilesizeGreaterThanFilter("100", false);
        assertNotEquals(instanceA.hashCode(), instanceB.hashCode());
    }

    /**
     * Test of getRawFilterData method, of class FileFilterFilesizeGreaterThanFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterFilesizeGreaterThanFilter instance = new FileFilterFilesizeGreaterThanFilter("100", true);
        String expResult = "100";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

}
