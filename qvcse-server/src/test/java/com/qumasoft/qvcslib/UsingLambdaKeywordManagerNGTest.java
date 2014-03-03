//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.qvcslib;

import com.qumasoft.TestHelper;
import com.qumasoft.server.LogFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test the Using Lamba flavor of the keyword manager.
 */
public class UsingLambdaKeywordManagerNGTest {
    private static final String TEST_ARCHIVE_FILENAME = "QVCSEnterpriseServer.kbwb";

    public UsingLambdaKeywordManagerNGTest() {
    }

    @org.testng.annotations.BeforeClass
    public static void setUpClass() throws Exception {
    }

    @org.testng.annotations.AfterClass
    public static void tearDownClass() throws Exception {
    }

    @org.testng.annotations.BeforeMethod
    public void setUpMethod() throws Exception {
    }

    @org.testng.annotations.AfterMethod
    public void tearDownMethod() throws Exception {
    }

//    /**
//     * Test of expandKeywords method, of class UsingLambdaKeywordManager.
//     * @throws IOException for IO problems.
//     * @throws QVCSException for QVCS specific problems.
//     */
//    @org.testng.annotations.Test
//    public void testExpandKeywordsFromFileInputStream() throws IOException, QVCSException {
//        System.out.println("expandKeywords from FileInputStream");
//        FileInputStream inStream = null;
//
//        File expandedOutputFile = File.createTempFile("QVCS", "tmp");
//        expandedOutputFile.deleteOnExit();
//        OutputStream outStream = new FileOutputStream(expandedOutputFile);
//
//        LogfileInfo logfileInfo = null;
//        int revisionIndex = 0;
//        String labelString = "";
//        String appendedPath = "test/expandkeywords/from/fileinputstream";
//        AbstractProjectProperties projectProperties = null;
//        UsingLambdaKeywordManager instance = new UsingLambdaKeywordManager();
//        instance.expandKeywords(inStream, outStream, expandedOutputFile, logfileInfo, revisionIndex, labelString, appendedPath, projectProperties);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of expandKeywords for Author keyword.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    @org.testng.annotations.Test(enabled=false)
    public void testExpandKeywordsFromByteArrayForAuthor() throws IOException, QVCSException {
        System.out.println("expand Author keyword");

        String keywordToExpand = "Foo $Author$ Bar";
        String expectedResult = "Foo $Author: JimVoris $ Bar";
        String expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, expectedResult);
    }

    /**
     * Test of expandKeywords for Date keyword.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    @org.testng.annotations.Test(enabled=false)
    public void testExpandKeywordsFromByteArrayForDate() throws IOException, QVCSException {
        System.out.println("expand Date keyword");

        String keywordToExpand = "Foo $Date$ Bar";
        String expectedResult = "Foo $Date: Saturday, June 07, 2008 9:26:33 PM $ Bar";
        String expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, expectedResult);
    }

    /**
     * Test of expandKeywords for Filename keyword.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    @org.testng.annotations.Test(enabled=false)
    public void testExpandKeywordsFromByteArrayForFilename() throws IOException, QVCSException {
        System.out.println("expand Filename keyword");

        String keywordToExpand = "Foo $Filename$ Bar";
        String expandedLine = testKeywordHelper(keywordToExpand);
        assertTrue(expandedLine.startsWith("Foo $Filename: "));
        assertTrue(expandedLine.endsWith("tmp $ Bar"));
    }

    /**
     * Test of expandKeywords for FilePath keyword.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    @org.testng.annotations.Test(enabled=false)
    public void testExpandKeywordsFromByteArrayForFilePath() throws IOException, QVCSException {
        System.out.println("expand Date keyword");

        String keywordToExpand = "Foo $FilePath$ Bar";
        String expectedResult = "Foo $FilePath: test/expandkeywords/from/bytearray/QVCSEnterpriseServer.java $ Bar";
        String expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, expectedResult);
    }

    /**
     * Test of expandKeywords for Header keyword.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    @org.testng.annotations.Test(enabled=false)
    public void testExpandKeywordsFromByteArrayForHeader() throws IOException, QVCSException {
        System.out.println("expand Header keyword");

        String keywordToExpand = "Foo $Header$ Bar";
        String expectedResult = "Revision: 1.91 Saturday, June 07, 2008 9:26:33 PM JimVoris $ Bar";
        String expandedLine = testKeywordHelper(keywordToExpand);
        System.out.println(expandedLine);
        assertTrue(expandedLine.endsWith(expectedResult));
    }

    /**
     * Test of expandKeywords for HeaderPath keyword.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    @org.testng.annotations.Test(enabled=false)
    public void testExpandKeywordsFromByteArrayForHeaderPath() throws IOException, QVCSException {
        System.out.println("expand HeaderPath keyword");

        String keywordToExpand = "Foo $HeaderPath$ Bar";
        String expectedResult = "Foo $HeaderPath: test/expandkeywords/from/bytearray/QVCSEnterpriseServer.java Revision: 1.91 Saturday, June 07, 2008 9:26:33 PM JimVoris $ Bar";
        String expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, expectedResult);
    }

    /**
     * Test of expandKeywords for Label keyword.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    @org.testng.annotations.Test(enabled=false)
    public void testExpandKeywordsFromByteArrayForLabel() throws IOException, QVCSException {
        System.out.println("expand HeaderPath keyword");

        String keywordToExpand = "Foo $Label$ Bar";
        String expectedResult = "Foo $Label: NONE $ Bar";
        String expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, expectedResult);
    }

    /**
     * Test of expandKeywords for Revision keyword.
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    @org.testng.annotations.Test(enabled=false)
    public void testExpandKeywordsFromByteArrayForRevision() throws IOException, QVCSException {
        System.out.println("expand Revision keyword");

        String keywordToExpand = "Foo $Revision$ Bar";
        String expectedResult = "Foo $Revision: 1.91 $ Bar";
        String expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, expectedResult);
    }

    /**
     * Test of expandKeywords for lines containing $ character. None of these strings have keywords, so
     * there should be no expansion of the input string.
     *
     * @throws IOException for IO problems.
     * @throws QVCSException for QVCS specific problems.
     */
    @org.testng.annotations.Test(enabled=false)
    public void testExpandKeywordsFromByteArrayForDollarramma() throws IOException, QVCSException {
        System.out.println("expand non keyword $ test");

        String keywordToExpand = "Foo $ Bar";
        String expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = "Foo $$ Bar";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = "Foo $$$ Bar";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = "$";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = "$$";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = " $ $ ";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = "$ $ $ ";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = " $ $ $ ";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = " $ $$ $ ";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = " $ $goofball$ $ ";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);

        keywordToExpand = " $ $ $";
        expandedLine = testKeywordHelper(keywordToExpand);
        assertEquals(expandedLine, keywordToExpand);
    }

    private String testKeywordHelper(final String contractedString) throws IOException, QVCSException {
        byte[] inBuffer = contractedString.getBytes("UTF-8");
        String expandedLine;

        File expandedOutputFile = File.createTempFile("QVCS", "tmp");
        expandedOutputFile.deleteOnExit();
        try (OutputStream outStream = new FileOutputStream(expandedOutputFile)) {
            LogFile testArchive = new LogFile(System.getProperty(TestHelper.USER_DIR) + File.separator + TEST_ARCHIVE_FILENAME);
            LogfileInfo logfileInfo = new LogfileInfo(testArchive.getLogFileHeaderInfo(), testArchive.getRevisionInformation(), 1, testArchive.getFullArchiveFilename());

            int revisionIndex = 0;
            String labelString = "";
            String appendedPath = "test/expandkeywords/from/bytearray";

            AbstractProjectProperties projectProperties = null;
            UsingLambdaKeywordManager instance = new UsingLambdaKeywordManager();
            KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outStream, expandedOutputFile, logfileInfo, revisionIndex, labelString,
                    appendedPath, projectProperties);
            instance.expandKeywords(inBuffer, keywordExpansionContext);
        }
        try (FileInputStream inputStream = new FileInputStream(expandedOutputFile)) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            expandedLine = bufferedReader.readLine();
        }
        return expandedLine;
    }
}
