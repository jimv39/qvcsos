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

import java.io.File;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Compare test.
 *
 * @author Jim Voris
 */
public class CompareTest {

    private CompareFilesWithApacheDiff compareFilesOperator;
    private static String baseCompareTestFilesDirectory;

    @BeforeClass
    public static void setUpClass() throws Exception {
        Properties systemProperties = System.getProperties();
        baseCompareTestFilesDirectory = systemProperties.getProperty("user.dir") + File.separator;
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of convertArchiveNameToShortWorkfileName method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testCompareTest1() {
        System.out.println("testCompareTest1");
        compareFilesOperator = new CompareFilesWithApacheDiff();

        try {
            compareFiles("CompareTest1a.txt", "CompareTest1b.txt");
        } catch (QVCSException e) {
            e.printStackTrace();
            fail("testCompareTest1 failed with an exception: " + e.getMessage());
        }
    }

    /**
     * Test of convertWorkfileNameToShortArchiveName method, of class com.qumasoft.qvcslib.LogFile.
     */
    @Test
    public void testCompareTest2() {
        System.out.println("testCompareTest2");
        compareFilesOperator = new CompareFilesWithApacheDiff();

        try {
            compareFiles("CompareTest2a.txt", "CompareTest2b.txt");
        } catch (QVCSException e) {
            e.printStackTrace();
            fail("testCompareTest2 failed with an exception: " + e.getMessage());
        }
    }

    private void compareFiles(String file1Name, String file2Name) throws QVCSException {
        String fullFile1Name;
        String fullFile2Name;

        File tempFileForCompareResults = null;
        try {
            tempFileForCompareResults = File.createTempFile("QVCS", ".tmp");
            fullFile1Name = baseCompareTestFilesDirectory + file1Name;
            fullFile2Name = baseCompareTestFilesDirectory + file2Name;
        } catch (java.io.IOException e) {
            e.printStackTrace();
            throw new QVCSException("Failed to create QVCS temp file: " + e.getMessage());
        }
        String tempFileNameForCompareResults = tempFileForCompareResults.getAbsolutePath();
        String[] args = new String[3];
        args[0] = fullFile1Name;
        args[1] = fullFile2Name;
        args[2] = tempFileNameForCompareResults;
        if (!compareFilesOperator.execute(args)) {
            tempFileForCompareResults.delete();
            throw new QVCSException("Failed to compare '" + file1Name + "' to '" + file2Name + "'");
        }
        tempFileForCompareResults.delete();
    }
}
