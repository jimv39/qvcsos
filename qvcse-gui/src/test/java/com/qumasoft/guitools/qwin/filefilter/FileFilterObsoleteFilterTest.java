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

import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import mockit.Expectations;
import mockit.Mocked;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Use the JMockit mocking framework to test the obsolete filter.
 * @author Jim Voris
 */
public class FileFilterObsoleteFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    public FileFilterObsoleteFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterObsoleteFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        new Expectations() {{
            mergedInfo.getIsObsolete();
            result = true;
        }};
        FileFilterObsoleteFilter instance = new FileFilterObsoleteFilter(true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterObsoleteFilter.
     */
    @Test
    public void testNoPassesFilter() {
        System.out.println("no passesFilter");
        new Expectations() {{
            mergedInfo.getIsObsolete();
            result = false;
        }};
        FileFilterObsoleteFilter instance = new FileFilterObsoleteFilter(true);
        boolean passesFlag = instance.passesFilter(mergedInfo, null);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of toString method, of class FileFilterObsoleteFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterObsoleteFilter instance = new FileFilterObsoleteFilter(true);
        String expResult = QVCSConstants.OBSOLETE_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterObsoleteFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterObsoleteFilter instance = new FileFilterObsoleteFilter(true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterType method, of class FileFilterObsoleteFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterObsoleteFilter instance = new FileFilterObsoleteFilter(true);
        String expResult = QVCSConstants.OBSOLETE_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterObsoleteFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterObsoleteFilter instance = new FileFilterObsoleteFilter(true);
        String expResult = QVCSConstants.OBSOLETE_FILTER;
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterObsoleteFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterObsoleteFilter instance = new FileFilterObsoleteFilter(true);
        String expResult = QVCSConstants.OBSOLETE_FILTER;
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterObsoleteFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterObsoleteFilter instance = new FileFilterObsoleteFilter(true);
        int expResult = 104;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterExcludeLockedByFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterObsoleteFilter instance = new FileFilterObsoleteFilter(true);
        boolean expResult = false;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
