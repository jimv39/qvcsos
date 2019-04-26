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
package com.qumasoft.server;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Activity journal manager test.
 * @author Jim Voris
 */
public class ActivityJournalManagerTest {

    public ActivityJournalManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
        ActivityJournalManager.getInstance().initialize();
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
        ActivityJournalManager.getInstance().closeJournal();
    }

    @Before
    public void setUp()
    {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of initialize method, of class ActivityJournalManager.
     */
    @Test
    public void testInitialize()
    {
        System.out.println("initialize");
        ActivityJournalManager instance = ActivityJournalManager.getInstance();
        boolean expResult = true;
        boolean result = instance.initialize();
        assertEquals(expResult, result);
    }

    /**
     * Test of addJournalEntry method, of class ActivityJournalManager.
     */
    @Test
    public void testAddJournalEntry()
    {
        System.out.println("addJournalEntry");
        String journalEntry = "Test journal entry dude.";
        ActivityJournalManager instance = ActivityJournalManager.getInstance();
        instance.addJournalEntry(journalEntry);
    }

    /**
     * Test of closeJournal method, of class ActivityJournalManager.
     */
    @Test
    public void testCloseJournal()
    {
        System.out.println("closeJournal");
        ActivityJournalManager instance = ActivityJournalManager.getInstance();
        instance.closeJournal();
    }
}
