/*
 * Copyright 2021 Jim Voris.
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

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Jim Voris
 */
public class QvcsosClientIgnoreManagerTest {

    private String baseTestDirectory;
    private File baseTestDirectoryAsFile;

    public QvcsosClientIgnoreManagerTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        Properties systemProperties = System.getProperties();
        baseTestDirectory = systemProperties.getProperty("user.dir") + File.separator;
        baseTestDirectoryAsFile = new File(baseTestDirectory);
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of ignoreDirectoryForDirectoryAdd method, of class QvcsosClientIgnoreManager.
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreDirectoryTrue() throws IOException {
        String appendedPath = "";
        String directoryName = "logs";
        boolean expResult = true;
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreDirectoryForDirectoryAdd(baseTestDirectoryAsFile, appendedPath, directoryName);
        assertEquals(expResult, result);
    }

    /**
     * Test of ignoreDirectoryForDirectoryAdd method, of class QvcsosClientIgnoreManager.
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreDirectoryFalse() throws IOException {
        String appendedPath = "";
        String directoryName = "qvcsActivityJournal";
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreDirectoryForDirectoryAdd(baseTestDirectoryAsFile, appendedPath, directoryName);
        boolean expResult = false;
        assertEquals(expResult, result);
    }

    /**
     * Test of ignoreFile method, of class QvcsosClientIgnoreManager.
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreFileFalse() throws IOException {
        String appendedPath = "";
        File file = new File(baseTestDirectory + File.separator + "CompareTest10a.txt");
        boolean expResult = false;
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreFile(appendedPath, file);
        assertEquals(expResult, result);
    }

    /**
     * Test of ignoreFile method, of class QvcsosClientIgnoreManager. Ignore derby.log
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreFileDerbyLog() throws IOException {
        String appendedPath = "";
        File file = new File(baseTestDirectory + File.separator + "derby.log");
        boolean expResult = true;
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreFile(appendedPath, file);
        assertEquals(expResult, result);
    }

    /**
     * Test of ignoreFile method, of class QvcsosClientIgnoreManager. Ignore derby.log
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreFileBinaryKeyword() throws IOException {
        String appendedPath = "";
        File file = new File(baseTestDirectory + File.separator + "BinaryKeywordExpanded.uyu.org");
        boolean expResult = true;
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreFile(appendedPath, file);
        assertEquals(expResult, result);
    }

    /**
     * Test of ignoreFile method, of class QvcsosClientIgnoreManager.
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreFileEndsInStar() throws IOException {
        String appendedPath = "qvcsProjectProperties";
        File file = new File(baseTestDirectory + File.separator + "qvcsProjectProperties/qvcsos-server.2.log.zip");
        boolean expResult = true;
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreFile(appendedPath, file);
        assertEquals(expResult, result);
    }


    /**
     * Test of ignoreFile method, of class QvcsosClientIgnoreManager.
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreFileEndsInStarAtProjectRoot() throws IOException {
        String appendedPath = "";
        File file = new File(baseTestDirectory + File.separator + "qvcsos-server.1.log.zip");
        boolean expResult = true;
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreFile(appendedPath, file);
        assertEquals(expResult, result);
    }

    /**
     * Test of ignoreFile method, of class QvcsosClientIgnoreManager.
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreFileEndsInStarDeeperInProject() throws IOException {
        String appendedPath = "qvcsProjectProperties";
        File file = new File(baseTestDirectory + File.separator + "qvcsProjectProperties/hide-qvcs.served.project.Test Project.properties");
        boolean expResult = true;
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreFile(appendedPath, file);
        assertEquals(expResult, result);
    }

    /**
     * Test of ignoreFile method, of class QvcsosClientIgnoreManager.
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreFileInIgnoredDirectory() throws IOException {
        String appendedPath = "somedirectory/target";
        File file = new File(baseTestDirectory + File.separator + "somedirectory/target/hide-qvcs.served.project.Test Project.properties");
        boolean expResult = true;
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreFile(appendedPath, file);
        assertEquals(expResult, result);
    }

    /**
     * Test of ignoreFile method, of class QvcsosClientIgnoreManager.
     * @throws java.io.IOException
     */
    @Test
    public void testIgnoreDeepIgnoredDirectory() throws IOException {
        String appendedPath = "target/somedirectory";
        File file = new File(baseTestDirectory + File.separator + "target/somedirectory");
        boolean expResult = true;
        boolean result = QvcsosClientIgnoreManager.getInstance().ignoreDirectory(file.getCanonicalPath(), appendedPath);
        assertEquals(expResult, result);
    }

    /**
     * Test of getQvcsosIgnoreFilename method, of class QvcsosClientIgnoreManager.
     */
    @Test
    public void testGetQvcsosIgnoreFilename() {
        String expResult = ".qvcsosignore";
        String result = QvcsosClientIgnoreManager.getQvcsosIgnoreFilename();
        assertEquals(expResult, result);
    }

}
