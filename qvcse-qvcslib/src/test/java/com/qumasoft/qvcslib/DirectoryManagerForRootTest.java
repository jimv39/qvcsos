/*
 * Copyright 2019 JimVoris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import java.util.Collection;
import javax.swing.event.ChangeListener;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author JimVoris
 */
public class DirectoryManagerForRootTest {

    public DirectoryManagerForRootTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getUserName method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testGetSetUserName() {
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        String expResult = "testUser";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testGetAppendedPath() {
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        String expResult = "";
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of addChangeListener method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testAddChangeListener() {
        ChangeListener listener = null;
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        instance.addChangeListener(listener);
    }

    /**
     * Test of removeChangeListener method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testRemoveChangeListener() {
        ChangeListener listener = null;
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        instance.removeChangeListener(listener);
    }

    /**
     * Test of getCount method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testGetCount() {
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        int expResult = 0;
        int result = instance.getCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMergedInfoCollection method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testGetMergedInfoCollection() {
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        Collection<MergedInfoInterface> expResult = null;
        Collection<MergedInfoInterface> result = instance.getMergedInfoCollection();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testGetProjectName() {
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        String expResult = QVCSConstants.QWIN_DEFAULT_PROJECT_NAME;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testGetViewName() {
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        String expResult = QVCSConstants.QVCS_TRUNK_BRANCH;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMergedInfo method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testGetMergedInfo() {
        String shortWorkfileName = "test";
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        MergedInfoInterface expResult = null;
        MergedInfoInterface result = instance.getMergedInfo(shortWorkfileName);
        assertEquals(expResult, result);
    }


    /**
     * Test of getHasChanged method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testGetHasChanged() {
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        boolean expResult = false;
        boolean result = instance.getHasChanged();
        assertEquals(expResult, result);
    }

    /**
     * Test of createArchive method, of class DirectoryManagerForRoot.
     */
    @Test
    public void testCreateArchive() {
        CreateArchiveCommandArgs commandLineArgs = null;
        String filename = "";
        DirectoryManagerForRoot instance = new DirectoryManagerForRoot();
        boolean expResult = false;
        boolean result = instance.createArchive(commandLineArgs, filename);
        assertEquals(expResult, result);
    }

}
