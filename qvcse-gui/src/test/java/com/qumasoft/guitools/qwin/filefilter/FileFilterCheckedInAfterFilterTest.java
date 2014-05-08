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

import com.qumasoft.guitools.qwin.revisionfilter.FilteredRevisionInfo;
import com.qumasoft.guitools.qwin.revisionfilter.RevisionFilterCheckedInAfterFilter;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.TreeMap;
import mockit.Expectations;
import mockit.Mocked;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Use JMockit mocking library to test checked in after filter.
 * @author Jim Voris
 */
public class FileFilterCheckedInAfterFilterTest {

    public FileFilterCheckedInAfterFilterTest() {
    }

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    RevisionHeader revHeader;

    @Mocked
    RevisionFilterCheckedInAfterFilter revisionFilterCheckedInAfterFilter;

    /**
     * Test of passesFilter method, of class FileFilterCheckedInAfterFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        // New expectations...
        new Expectations() {{
            new RevisionFilterCheckedInAfterFilter((String) any, true);
            revisionFilterCheckedInAfterFilter.passesFilter((FilteredRevisionInfo) any);
            result = true;
        }};
        FileFilterCheckedInAfterFilter instance = new FileFilterCheckedInAfterFilter("01/01/14", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterCheckedInAfterFilter.
     */
    @Test
    public void testNoPassesFilter() {
        System.out.println("no passesFilter");
        // New expectations...
        new Expectations() {{
            new RevisionFilterCheckedInAfterFilter((String) any, true);
            revisionFilterCheckedInAfterFilter.passesFilter((FilteredRevisionInfo) any);
            result = false;
        }};
        FileFilterCheckedInAfterFilter instance = new FileFilterCheckedInAfterFilter("01/01/14", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterCheckedInAfterFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterCheckedInAfterFilter instance = new FileFilterCheckedInAfterFilter("01/01/14", true);
        String expResult = QVCSConstants.CHECKED_IN_AFTER_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterCheckedInAfterFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterCheckedInAfterFilter instance = new FileFilterCheckedInAfterFilter("01/01/14", true);
        String expResult = QVCSConstants.CHECKED_IN_AFTER_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterCheckedInAfterFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterCheckedInAfterFilter instance = new FileFilterCheckedInAfterFilter("01/01/14", true);
        String expResult = "Jan 1, 2014 12:00:00 AM";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterCheckedInAfterFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterCheckedInAfterFilter instance = new FileFilterCheckedInAfterFilter("01/01/14", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterCheckedInAfterFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterCheckedInAfterFilter instance = new FileFilterCheckedInAfterFilter("01/01/14", true);
        int expResult = 1277964071;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterCheckedInAfterFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterCheckedInAfterFilter instance = new FileFilterCheckedInAfterFilter("01/01/14", true);
        String expResult = "1388552400000";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterCheckedInAfterFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterCheckedInAfterFilter instance = new FileFilterCheckedInAfterFilter("01/01/14", true);
        boolean expResult = true;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
