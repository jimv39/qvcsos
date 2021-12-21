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
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jim Voris
 */
public class FileMergeTest {
    private String baseName;
    private String firstDescFileName;
    private String secondDescFileName;
    private String outputName;
    private String[] arguments;

    public FileMergeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        String userDir = System.getProperty("user.dir");
        baseName = userDir + File.separator + "MergeTestBaseFile.txt";
        firstDescFileName = userDir + File.separator + "MergeTestChild1.txt";
        secondDescFileName = userDir + File.separator + "MergeTestChild2.txt";
        outputName = userDir + File.separator + "MergeTestOutputFile1-test.txt";
        String[] args = {baseName, firstDescFileName, secondDescFileName, outputName};
        this.arguments = args;
    }

    @After
    public void tearDown() {
        File output = new File(outputName);
        if (output.exists()) {
            output.delete();
        }
    }

    /**
     * Test of execute method, of class FileMerge.
     */
    @Test
    public void testExecute_StringArr() throws Exception {
        FileMerge instance = new FileMerge();
        boolean expResult = true;
        boolean result = instance.execute(arguments);
        assertEquals(expResult, result);
    }

    /**
     * Test of mergeFiles method, of class FileMerge.
     */
    @Test
    public void testMergeFiles() throws Exception {
        FileMerge instance = new FileMerge();
        boolean expResult = true;
        boolean result = instance.mergeFiles(baseName, firstDescFileName, secondDescFileName, outputName);
        assertEquals(expResult, result);
    }

    /**
     * Test of execute method, of class FileMerge.
     */
    @Test
    public void testExecute_0args() throws Exception {
        FileMerge instance = new FileMerge(this.arguments);
        boolean expResult = true;
        boolean result = instance.execute();
        assertEquals(expResult, result);
    }

}
