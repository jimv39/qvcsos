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

import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.TreeMap;
import mockit.Expectations;
import mockit.Mocked;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Use the JMockit mocking framework to test the exclude LockedBy filter.
 * @author Jim Voris
 */
public class FileFilterLockedByFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    LogfileInfo logfileInfo;

    @Mocked
    RevisionHeader revHeader;

    @Mocked
    LogFileHeaderInfo logFileHeaderInfo;

    public FileFilterLockedByFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterLockedByFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        new Expectations() {{
            mergedInfo.getLogfileInfo();
            result = logfileInfo;
            logfileInfo.getLockCount();
            result = 1;
            revHeader.isLocked();
            result = true;
            revHeader.getLockerIndex();
            result = 0;
            logfileInfo.getLogFileHeaderInfo();
            result = logFileHeaderInfo;
            logFileHeaderInfo.getModifierList();
            result = "Unit Tester";
        }};
        FileFilterLockedByFilter instance = new FileFilterLockedByFilter("Unit Tester", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterExcludeLockedByFilter.
     */
    @Test
    public void testNoPassesFilter() {
        System.out.println("no passesFilter");
        new Expectations() {{
            mergedInfo.getLogfileInfo();
            result = logfileInfo;
            logfileInfo.getLockCount();
            result = 1;
            revHeader.isLocked();
            result = true;
            revHeader.getLockerIndex();
            result = 0;
            logfileInfo.getLogFileHeaderInfo();
            result = logFileHeaderInfo;
            logFileHeaderInfo.getModifierList();
            result = "Unit Tester-A";
        }};
        FileFilterLockedByFilter instance = new FileFilterLockedByFilter("Unit Tester", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterLockedByFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterLockedByFilter instance = new FileFilterLockedByFilter("Unit Tester", true);
        String expResult = QVCSConstants.LOCKED_BY_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterLockedByFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterLockedByFilter instance = new FileFilterLockedByFilter("Unit Tester", true);
        String expResult = QVCSConstants.LOCKED_BY_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterLockedByFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterLockedByFilter instance = new FileFilterLockedByFilter("Unit Tester", true);
        String expResult = "Unit Tester";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterLockedByFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterLockedByFilter instance = new FileFilterLockedByFilter("Unit Tester", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterLockedByFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterLockedByFilter instance = new FileFilterLockedByFilter("Unit Tester", true);
        int expResult = 2071698495;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterLockedByFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterLockedByFilter instance = new FileFilterLockedByFilter("Unit Tester", true);
        String expResult = "Unit Tester";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterLockedByFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterLockedByFilter instance = new FileFilterLockedByFilter("Unit Tester", true);
        boolean expResult = true;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
