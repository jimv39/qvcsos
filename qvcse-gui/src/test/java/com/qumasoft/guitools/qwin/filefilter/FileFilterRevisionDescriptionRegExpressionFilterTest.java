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

import com.qumasoft.qvcslib.ArchiveInfoInterface;
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
 * Use the JMockit mocking framework to test the revision description regular expression filter.
 * @author Jim Voris
 */
public class FileFilterRevisionDescriptionRegExpressionFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    LogfileInfo logfileInfo;

    @Mocked
    RevisionHeader revHeader;

    @Mocked
    ArchiveInfoInterface archiveInfoInterface;

    public FileFilterRevisionDescriptionRegExpressionFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("passesFilter");
        new Expectations() {
            {
                mergedInfo.getArchiveInfo();
                result = archiveInfoInterface;
                revHeader.getRevisionDescription();
                result = "Some checkin comment";
            }
        };
        FileFilterRevisionDescriptionRegExpressionFilter instance = new FileFilterRevisionDescriptionRegExpressionFilter(".*checkin.*", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testNoPassesFilter() {
        System.out.println("passesFilter");
        new Expectations() {
            {
                mergedInfo.getArchiveInfo();
                result = archiveInfoInterface;
                revHeader.getRevisionDescription();
                result = "Some checkin comment";
            }
        };
        FileFilterRevisionDescriptionRegExpressionFilter instance = new FileFilterRevisionDescriptionRegExpressionFilter(".*checkout.*", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterRevisionDescriptionRegExpressionFilter instance = new FileFilterRevisionDescriptionRegExpressionFilter(".*checkout.*", true);
        String expResult = QVCSConstants.REG_EXP_REV_DESC_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterRevisionDescriptionRegExpressionFilter instance = new FileFilterRevisionDescriptionRegExpressionFilter(".*checkout.*", true);
        String expResult = QVCSConstants.REG_EXP_REV_DESC_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterRevisionDescriptionRegExpressionFilter instance = new FileFilterRevisionDescriptionRegExpressionFilter(".*checkout.*", true);
        String expResult = ".*checkout.*";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterRevisionDescriptionRegExpressionFilter instance = new FileFilterRevisionDescriptionRegExpressionFilter(".*checkout.*", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterRevisionDescriptionRegExpressionFilter instance = new FileFilterRevisionDescriptionRegExpressionFilter(".*checkout.*", true);
        int expResult = -1318181470;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterRevisionDescriptionRegExpressionFilter instance = new FileFilterRevisionDescriptionRegExpressionFilter(".*checkout.*", true);
        String expResult = ".*checkout.*";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterRevisionDescriptionRegExpressionFilter instance = new FileFilterRevisionDescriptionRegExpressionFilter(".*checkout.*", true);
        boolean expResult = true;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
