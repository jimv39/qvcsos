/*   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.qvcslib;

import java.util.Date;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * Add Revision Data Test.
 *
 * @author  Jim Voris
 */
public class AddRevisionDataTest
{
    /** now */
    private Date m_Now = null;

    /**
     * Run before each test.
     */
    @Before
    public void setUp()
    {
        m_Now = new Date();
    }

    /**
     * Test of getNewRevisionHeader method, of class AddRevisionData.
     */
    @Test
    public void testGetNewRevisionHeader()
    {
        RevisionHeader newRevisionHeader = new RevisionHeader();
        String expResult = "Revision Description";
        newRevisionHeader.setRevisionDescription(expResult);
        AddRevisionData instance = new AddRevisionData();
        instance.setNewRevisionHeader(newRevisionHeader);
        RevisionHeader result = instance.getNewRevisionHeader();
        assertEquals(expResult, result.getRevisionDescription());
    }

    /**
     * Test of getParentRevisionHeader method, of class AddRevisionData.
     */
    @Test
    public void testGetParentRevisionHeader()
    {
        AddRevisionData instance = new AddRevisionData();
        RevisionHeader parentRevisionHeader = new RevisionHeader();
        String expResult = "Revision Description";
        parentRevisionHeader.setRevisionDescription(expResult);
        instance.setParentRevisionHeader(parentRevisionHeader);
        RevisionHeader result = instance.getParentRevisionHeader();
        assertEquals(expResult, result.getRevisionDescription());
    }

    /**
     * Test of getNewRevisionIndex method, of class AddRevisionData.
     */
    @Test
    public void testGetNewRevisionIndex()
    {
        AddRevisionData instance = new AddRevisionData();
        int expResult = 10;
        instance.setNewRevisionIndex(expResult);
        int result = instance.getNewRevisionIndex();
        assertEquals(expResult, result);
    }

    /**
     * Test of getParentRevisionIndex method, of class AddRevisionData.
     */
    @Test
    public void testGetParentRevisionIndex()
    {
        AddRevisionData instance = new AddRevisionData();
        int expResult = 10;
        instance.setParentRevisionIndex(expResult);
        int result = instance.getParentRevisionIndex();
        assertEquals(expResult, result);
    }
}
