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

import java.io.File;
import java.util.Properties;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Compare files with apache diff test.
 * @author Jim Voris
 */
public class CompareFilesWithApacheDiffTest {

    private String baseCompareTestFilesDirectory;

    public CompareFilesWithApacheDiffTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        Properties systemProperties = System.getProperties();
        baseCompareTestFilesDirectory = systemProperties.getProperty("user.dir") + File.separator;
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testCompareFiles1() {
        System.out.println("testCompareFiles1");

        try {
            compareFiles("CompareTest1a.txt", "CompareTest1b.txt");
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareTest1 failed with an exception: " + e.getMessage());
        }
    }

    @Test
    public void testCompareFiles2() {
        System.out.println("testCompareFiles2");

        try {
            compareFiles("CompareTest2a.txt", "CompareTest2b.txt");
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareTest2 failed with an exception: " + e.getMessage());
        }
    }

    @Test
    public void testCompareFiles3() {
        System.out.println("testCompareFiles3");

        try {
            compareFiles("CompareTest3a.txt", "CompareTest3b.txt");
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareTest3 failed with an exception: " + e.getMessage());
        }
    }

    @Test
    public void testCompareFiles4() {
        System.out.println("testCompareFiles4");
        File tempFileForCompareResults;
        String[] args = null;

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest4a.txt", "CompareTest4b.txt");
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareTest4 failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest4b.txt", "CompareTest4a.txt");
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareTest4 failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
    }

    @Test
    public void testCompareFiles5IgnoreWhiteSpace() {
        System.out.println("testCompareFiles5IgnoreWhiteSpace");
        File tempFileForCompareResults;
        String[] args = null;

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest5a.txt", "CompareTest5b.txt");
            compareFilesWithApacheDiff.setIgnoreAllWhiteSpace(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles5IgnoreWhiteSpace failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest5b.txt", "CompareTest5a.txt");
            compareFilesWithApacheDiff.setIgnoreAllWhiteSpace(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles5IgnoreWhiteSpace failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
    }

    @Test
    public void testCompareFiles6IgnoreEOLChanges() {
        System.out.println("testCompareFiles6IgnoreEOLChanges");
        File tempFileForCompareResults;
        String[] args = null;

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest6a.txt", "CompareTest6b.txt");
            compareFilesWithApacheDiff.setIgnoreEOLChangesFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles6IgnoreEOLChanges failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest6b.txt", "CompareTest6a.txt");
            compareFilesWithApacheDiff.setIgnoreEOLChangesFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles6IgnoreEOLChanges failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
    }

    @Test
    public void testCompareFiles7IgnoreLeadingWhiteSpaceChanges() {
        System.out.println("testCompareFiles7IgnoreLeadingWhiteSpaceChanges");
        File tempFileForCompareResults;
        String[] args = null;

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest7a.txt", "CompareTest7b.txt");
            compareFilesWithApacheDiff.setIgnoreLeadingWhiteSpace(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles7IgnoreLeadingWhiteSpaceChanges failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest7b.txt", "CompareTest7a.txt");
            compareFilesWithApacheDiff.setIgnoreLeadingWhiteSpace(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles7IgnoreLeadingWhiteSpaceChanges failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
    }

    @Test
    public void testCompareFiles8IgnoreCase() {
        System.out.println("testCompareFiles8IgnoreCase");
        File tempFileForCompareResults;
        String[] args = null;

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest8a.txt", "CompareTest8b.txt");
            compareFilesWithApacheDiff.setIgnoreCaseFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles8IgnoreCase failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest8b.txt", "CompareTest8a.txt");
            compareFilesWithApacheDiff.setIgnoreCaseFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles8IgnoreCase failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
    }

    @Test
    public void testCompareFiles9() {
        System.out.println("testCompareFiles9");
        File tempFileForCompareResults;
        String[] args = null;

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest9a.txt", "CompareTest9b.txt");
            compareFilesWithApacheDiff.setIgnoreCaseFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(!compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles9 failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest9b.txt", "CompareTest9a.txt");
            compareFilesWithApacheDiff.setIgnoreCaseFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(!compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles9 failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
    }

    @Test
    public void testCompareFiles10() {
        System.out.println("testCompareFiles10");
        File tempFileForCompareResults;
        String[] args = null;

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest10a.txt", "CompareTest10b.txt");
            compareFilesWithApacheDiff.setIgnoreCaseFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(!compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles10 failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest10b.txt", "CompareTest10a.txt");
            compareFilesWithApacheDiff.setIgnoreCaseFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(!compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles10 failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
    }

    @Test
    public void testCompareFiles11() {
        System.out.println("testCompareFiles11");
        File tempFileForCompareResults;
        String[] args = null;

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("DoesNotExista.txt", "DoesNotExistb.txt");
            compareFilesWithApacheDiff.setIgnoreCaseFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(!result);
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles11 failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }
    }

    @Test
    public void testCompareFiles12() {
        System.out.println("testCompareFiles12");
        File tempFileForCompareResults;
        String[] args = null;

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest12a.txt", "CompareTest12b.txt");
            compareFilesWithApacheDiff.setIgnoreCaseFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(!compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles12 failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
        }

        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = compareFilesSetup("CompareTest12b.txt", "CompareTest12a.txt");
            compareFilesWithApacheDiff.setIgnoreCaseFlag(true);
            args = compareFilesWithApacheDiff.getArgs();
            boolean result = compareFilesWithApacheDiff.execute();
            assertTrue(result);
            assertTrue(!compareFilesWithApacheDiff.isEqual());
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            fail("testCompareFiles12 failed with an exception: " + e.getMessage());
        } finally {
            if (args != null && (args[2] != null)) {
                String tempFileName = args[2];
                tempFileForCompareResults = new File(tempFileName);
                tempFileForCompareResults.delete();
            }
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
            System.err.println(Utility.expandStackTraceToString(e));
            throw new QVCSException("Failed to create QVCS temp file: " + e.getMessage());
        }
        String tempFileNameForCompareResults = tempFileForCompareResults.getAbsolutePath();
        String[] args = new String[3];
        args[0] = fullFile1Name;
        args[1] = fullFile2Name;
        args[2] = tempFileNameForCompareResults;
        try {
            CompareFilesWithApacheDiff compareFilesWithApacheDiff = new CompareFilesWithApacheDiff(args);
            if (!compareFilesWithApacheDiff.execute(args)) {
                throw new QVCSException("Failed to compare '" + file1Name + "' to '" + file2Name + "'");
            }
        } catch (QVCSException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            throw new QVCSException(e.getMessage());
        } finally {
            tempFileForCompareResults.delete();
        }
    }

    private CompareFilesWithApacheDiff compareFilesSetup(String file1Name, String file2Name) throws QVCSException {
        String fullFile1Name;
        String fullFile2Name;

        File tempFileForCompareResults = null;
        try {
            tempFileForCompareResults = File.createTempFile("QVCS", ".tmp");
            fullFile1Name = baseCompareTestFilesDirectory + file1Name;
            fullFile2Name = baseCompareTestFilesDirectory + file2Name;
        } catch (java.io.IOException e) {
            System.err.println(Utility.expandStackTraceToString(e));
            throw new QVCSException("Failed to create QVCS temp file: " + e.getMessage());
        }
        String tempFileNameForCompareResults = tempFileForCompareResults.getAbsolutePath();
        String[] args = new String[3];
        args[0] = fullFile1Name;
        args[1] = fullFile2Name;
        args[2] = tempFileNameForCompareResults;
        return new CompareFilesWithApacheDiff(args);
    }
}
