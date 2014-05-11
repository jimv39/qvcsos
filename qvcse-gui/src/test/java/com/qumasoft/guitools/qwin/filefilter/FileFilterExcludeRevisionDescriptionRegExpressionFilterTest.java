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
 * Use the JMockit mocking framework to test the exclude revision description regular expression filter.
 *
 * @author Jim Voris
 */
public class FileFilterExcludeRevisionDescriptionRegExpressionFilterTest {

    @Mocked
    MergedInfoInterface mergedInfo;

    @Mocked
    LogfileInfo logfileInfo;

    @Mocked
    RevisionHeader revHeader;

    @Mocked
    ArchiveInfoInterface archiveInfoInterface;

    public FileFilterExcludeRevisionDescriptionRegExpressionFilterTest() {
    }

    /**
     * Test of passesFilter method, of class FileFilterExcludeRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testPassesFilter() {
        System.out.println("no passesFilter");
        new Expectations() {
            {
                mergedInfo.getArchiveInfo();
                result = archiveInfoInterface;
                revHeader.getRevisionDescription();
                result = "Some checkin comment";
            }
        };
        FileFilterExcludeRevisionDescriptionRegExpressionFilter instance = new FileFilterExcludeRevisionDescriptionRegExpressionFilter("Some checkout comment", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(true, passesFlag);
    }

    /**
     * Test of passesFilter method, of class FileFilterExcludeRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testNoPassesFilter() {
        System.out.println("no passesFilter");
        new Expectations() {
            {
                mergedInfo.getArchiveInfo();
                result = archiveInfoInterface;
                revHeader.getRevisionDescription();
                result = "Some checkin comment";
            }
        };
        FileFilterExcludeRevisionDescriptionRegExpressionFilter instance = new FileFilterExcludeRevisionDescriptionRegExpressionFilter("Some checkin comment", true);
        TreeMap<Integer, RevisionHeader> revisionHeaderMap = new TreeMap<>();
        revisionHeaderMap.put(1, revHeader);
        boolean passesFlag = instance.passesFilter(mergedInfo, revisionHeaderMap);
        assertEquals(false, passesFlag);
    }

    /**
     * Test of getFilterType method, of class FileFilterExcludeRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testGetFilterType() {
        System.out.println("getFilterType");
        FileFilterExcludeRevisionDescriptionRegExpressionFilter instance = new FileFilterExcludeRevisionDescriptionRegExpressionFilter("Some checkin comment", true);
        String expResult = QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER;
        String result = instance.getFilterType();
        assertEquals(expResult, result);
    }

    /**
     * Test of toString method, of class FileFilterExcludeRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        FileFilterExcludeRevisionDescriptionRegExpressionFilter instance = new FileFilterExcludeRevisionDescriptionRegExpressionFilter("Some checkin comment", true);
        String expResult = QVCSConstants.EXCLUDE_REG_EXP_REV_DESC_FILTER;
        String result = instance.toString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilterData method, of class FileFilterExcludeRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testGetFilterData() {
        System.out.println("getFilterData");
        FileFilterExcludeRevisionDescriptionRegExpressionFilter instance = new FileFilterExcludeRevisionDescriptionRegExpressionFilter("Some checkin comment", true);
        String expResult = "Some checkin comment";
        String result = instance.getFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of equals method, of class FileFilterExcludeRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testEquals() {
        System.out.println("equals");
        Object o = null;
        FileFilterExcludeRevisionDescriptionRegExpressionFilter instance = new FileFilterExcludeRevisionDescriptionRegExpressionFilter("Some checkin comment", true);
        boolean expResult = false;
        boolean result = instance.equals(o);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashCode method, of class FileFilterExcludeRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testHashCode() {
        System.out.println("hashCode");
        FileFilterExcludeRevisionDescriptionRegExpressionFilter instance = new FileFilterExcludeRevisionDescriptionRegExpressionFilter("Some checkin comment", true);
        int expResult = -352822332;
        int result = instance.hashCode();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRawFilterData method, of class FileFilterExcludeRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testGetRawFilterData() {
        System.out.println("getRawFilterData");
        FileFilterExcludeRevisionDescriptionRegExpressionFilter instance = new FileFilterExcludeRevisionDescriptionRegExpressionFilter("Some checkin comment", true);
        String expResult = "Some checkin comment";
        String result = instance.getRawFilterData();
        assertEquals(expResult, result);
    }

    /**
     * Test of requiresRevisionDetailInfo method, of class FileFilterExcludeRevisionDescriptionRegExpressionFilter.
     */
    @Test
    public void testRequiresRevisionDetailInfo() {
        System.out.println("requiresRevisionDetailInfo");
        FileFilterExcludeRevisionDescriptionRegExpressionFilter instance = new FileFilterExcludeRevisionDescriptionRegExpressionFilter("Some checkin comment", true);
        boolean expResult = true;
        boolean result = instance.requiresRevisionDetailInfo();
        assertEquals(expResult, result);
    }

}
