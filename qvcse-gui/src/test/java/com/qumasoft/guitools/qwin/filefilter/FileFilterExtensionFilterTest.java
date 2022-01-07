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
import mockit.Expectations;
import mockit.Mocked;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Use JMockit mocking library to test file extension filter.
 * @author JimVoris
 */
public class FileFilterExtensionFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    AbstractProjectProperties projectProperties;

    public FileFilterExtensionFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterExtensionFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        // New expectations...
        new Expectations() {{
            mergedInfo.getShortWorkfileName();
            result = "foo.bar";
        }};
        FileFilterExtensionFilter instance = new FileFilterExtensionFilter(".bar", true);
        boolean expResult = true;
        boolean result = instance.passesFilter(mergedInfo, null);
        assertEquals(expResult, result);
    }

    /**
     * Test of passesFilter method, of class FileFilterExtensionFilter.
     */
    @Test
    public void testPassesFilterNotPass() {
        System.out.println("passesFilter not pass");
        // New expectations...
        new Expectations() {{
            mergedInfo.getShortWorkfileName();
            result = "foo.bar";
        }};
        FileFilterExtensionFilter instance = new FileFilterExtensionFilter(".foo", true);
        boolean expResult = false;
        boolean result = instance.passesFilter(mergedInfo, null);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterType method, of class FileFilterExtensionFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterExtensionFilter instance = new FileFilterExtensionFilter(".bar", true);
        String expResult = QVCSConstants.EXTENSION_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterExtensionFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterExtensionFilter instance = new FileFilterExtensionFilter(".bar", true);
        String expResult = QVCSConstants.EXTENSION_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterExtensionFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterExtensionFilter instance = new FileFilterExtensionFilter(".bar", true);
        String expResult = ".bar";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterExtensionFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = new FileFilterExtensionFilter(".bar", true);
        FileFilterExtensionFilter instance = new FileFilterExtensionFilter(".bar", true);
        boolean expResult = true;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterExtensionFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterExtensionFilter instance = new FileFilterExtensionFilter(".bar", false);
        int expResult = 1467944;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterExtensionFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterExtensionFilter instance = new FileFilterExtensionFilter(".bar", true);
        String expResult = ".bar";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

}
