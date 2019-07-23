/*   Copyright 2004-2019 Jim Voris
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

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Directory contents server test.
 * @author Jim Voris
 */
public class DirectoryContentsServerTest {

    private DirectoryContentsManager directoryContentsManager = null;
    private ServerResponseFactoryInterface bogusResponseObject = null;
    private DirectoryContents directoryContents = null;
    private static Object serverSyncObject = null;

    /**
     * Default ctor.
     */
    public DirectoryContentsServerTest() {
    }

    /**
     * Set up stuff once for these tests.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.stopServerImmediately(null);
        TestHelper.initProjectProperties();
        TestHelper.initializeArchiveFiles();
        serverSyncObject = TestHelper.startServer();
    }

    /**
     * Tear down the class level stuff.
     *
     * @throws java.lang.Exception if anything goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer(serverSyncObject);
        TestHelper.removeArchiveFiles();
    }

    /**
     * Run before each test.
     *
     * @throws QVCSException for QVCS exceptions.
     * @throws IOException for IO exceptions.
     */
    @Before
    public void setUp() throws QVCSException, IOException {
        directoryContentsManager = DirectoryContentsManagerFactory.getInstance().getDirectoryContentsManager(TestHelper.getTestProjectName());
        bogusResponseObject = new BogusResponseObject();
        ServerTransactionManager.getInstance().flushClientTransaction(bogusResponseObject);
        ServerTransactionManager.getInstance().clientBeginTransaction(bogusResponseObject);
        directoryContents = directoryContentsManager.getDirectoryContentsForTrunk("", 1, bogusResponseObject);
    }

    /**
     * Run after each test.
     */
    @After
    public void tearDown() {
        ServerTransactionManager.getInstance().clientEndTransaction(bogusResponseObject);
    }

    /**
     * Test of getProjectName method, of class DirectoryContents.
     */
    @Test
    public void testGetProjectName() {
        DirectoryContents instance = directoryContents;
        String expResult = TestHelper.getTestProjectName();
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDirectoryID method, of class DirectoryContents.
     */
    @Test
    public void testGetDirectoryID() {
        DirectoryContents instance = directoryContents;
        int expResult = 1;
        int result = instance.getDirectoryID();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class DirectoryContents.
     */
    @Test
    public void testGetAppendedPath() {
        DirectoryContents instance = directoryContents;
        String result = instance.getAppendedPath();
        assertNotNull("Had a null appended path!!", result);
    }

    /**
     * Test of getAppendedPath method for null value of appended path.
     */
    @Test
    public void testGetNullAppendedPath() {
        DirectoryContents instance = new DirectoryContents("", 1, null);
        String result = instance.getAppendedPath();
        assertNull(result);
    }

    /**
     * Test of getter and setter of label parent flag.
     */
    @Test
    public void testGetAndSetLabelParentFlag() {
        DirectoryContents instance = directoryContents;
        instance.setLabelParentFlag(true);
        boolean expResult = true;
        boolean result = instance.getLabelParentFlag();
        assertEquals(expResult, result);
        expResult = false;
        instance.setLabelParentFlag(false);
        result = instance.getLabelParentFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of addFileID method, of class DirectoryContents.
     */
    @Test
    public void testAddAndRemoveFileID() {
        int fileID = 10;
        String shortWorkfileName = "TestWorkfileName.test";
        DirectoryContents instance = directoryContents;
        instance.addFileID(fileID, shortWorkfileName);
        assertEquals(shortWorkfileName, instance.getFiles().get(10));
        assertTrue(instance.containsFile(shortWorkfileName));
        instance.removeFileID(fileID);
        assertEquals(null, instance.getFiles().get(10));
    }

    /**
     * Test of updateFileID method, of class DirectoryContents.
     */
    @Test
    public void testUpdateFileID() {
        int fileID = 10;
        String shortWorkfileName = "TestWorkfileName.test";
        String newShortWorkfileName = "NewTestWorkfileName.test";
        DirectoryContents instance = directoryContents;
        instance.addFileID(fileID, shortWorkfileName);
        assertEquals(shortWorkfileName, instance.getFiles().get(10));
        instance.updateFileID(fileID, newShortWorkfileName);
        assertEquals(newShortWorkfileName, instance.getFiles().get(10));
        // Just to make sure that the useful toString() method sort of works.
        instance.toString();
    }

    /**
     * Test of addDirectoryID method, of class DirectoryContents.
     */
    @Test
    public void testAddAndRemoveDirectoryID() {
        int directoryID = 10;
        String directoryName = "AddedDirectory";
        DirectoryContents instance = directoryContents;
        instance.addDirectoryID(directoryID, directoryName);
        assertEquals(directoryName, instance.getChildDirectories().get(10));
        instance.removeDirectoryID(directoryID);
        assertEquals(null, instance.getChildDirectories().get(10));
    }

    /**
     * Test of updateDirectoryID method, of class DirectoryContents.
     */
    @Test
    public void testUpdateDirectoryID() {
        int directoryID = 10;
        String directoryName = "AddedDirectory";
        String newDirectoryName = "NewAddedDirectory";
        DirectoryContents instance = directoryContents;
        instance.addDirectoryID(directoryID, directoryName);
        assertEquals(directoryName, instance.getChildDirectories().get(10));
        instance.updateDirectoryID(directoryID, newDirectoryName);
        assertEquals(newDirectoryName, instance.getChildDirectories().get(10));
    }
}
