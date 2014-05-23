package com.qumasoft.qvcslib;
/*
 * Copyright 2014 JimVoris.
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for the Utility class.
 *
 * @author Jim Voris
 */
public class UtilityTest {

    public UtilityTest() {
    }

    /**
     * Test of hashPassword method, of class Utility.
     */
    @Ignore
    @Test
    public void testHashPassword() {
        System.out.println("hashPassword");
        String password = "";
        Utility instance = null;
        byte[] expResult = null;
        byte[] result = instance.hashPassword(password);
        assertArrayEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deducePathSeparator method, of class Utility.
     */
    @Test
    public void testDeducePathSeparator() {
        System.out.println("deducePathSeparator");
        {
            String appendedPath = "this/is/the/appended/path";
            byte expResult = (byte) '/';
            byte result = Utility.deducePathSeparator(appendedPath);
            assertEquals(expResult, result);
        }
        {
            String appendedPath = "this\\is\\the\\appended\\path";
            byte expResult = (byte) '\\';
            byte result = Utility.deducePathSeparator(appendedPath);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of convertToStandardPath method, of class Utility.
     */
    @Test
    public void testConvertToStandardPath() {
        System.out.println("convertToStandardPath");
        String appendedPath = "this\\is\\the\\appended\\path";
        String expResult = "this/is/the/appended/path";
        String result = Utility.convertToStandardPath(appendedPath);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertToLocalPath method, of class Utility.
     */
    @Ignore
    @Test
    public void testConvertToLocalPath() {
        System.out.println("convertToLocalPath");
        String appendedPath = "";
        String expResult = "";
        String result = Utility.convertToLocalPath(appendedPath);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getArchiveKey method, of class Utility.
     */
    @Ignore
    @Test
    public void testGetArchiveKey() {
        System.out.println("getArchiveKey");
        AbstractProjectProperties properties = null;
        String shortWorkfileName = "";
        String expResult = "";
        String result = Utility.getArchiveKey(properties, shortWorkfileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getDirectorySegments method, of class Utility.
     */
    @Test
    public void testGetDirectorySegments() {
        System.out.println("getDirectorySegments");
        String appendedPath = "this/is/a/test";
        String[] expResult = {"this", "is", "a", "test"};
        String[] result = Utility.getDirectorySegments(appendedPath);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getLastDirectorySegment method, of class Utility.
     */
    @Test
    public void testGetLastDirectorySegment() {
        System.out.println("getLastDirectorySegment");
        String appendedPath = "this/is/a/test";
        String expResult = "test";
        String result = Utility.getLastDirectorySegment(appendedPath);
        assertEquals(expResult, result);
    }

    /**
     * Test of convertArchiveNameToShortWorkfileName method, of class Utility.
     */
    @Test
    public void testConvertArchiveNameToShortWorkfileName() {
        System.out.println("convertArchiveNameToShortWorkfileName");
        {
            String archiveName = "abc.b";
            String expResult = "abc.a";
            String result = Utility.convertArchiveNameToShortWorkfileName(archiveName);
            assertEquals(expResult, result);
        }
        {
            String archiveName = "abc.(+)";
            String expResult = "abc.(+)";
            String result = Utility.convertArchiveNameToShortWorkfileName(archiveName);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of convertWorkfileNameToShortArchiveName method, of class Utility.
     */
    @Test
    public void testConvertWorkfileNameToShortArchiveName() {
        System.out.println("convertWorkfileNameToShortArchiveName");
        {
            String workfileName = "abc.a";
            String expResult = "abc.b";
            String result = Utility.convertWorkfileNameToShortArchiveName(workfileName);
            assertEquals(expResult, result);
        }
        {
            String workfileName = "abc.(+)";
            String expResult = "abc.(+)";
            String result = Utility.convertWorkfileNameToShortArchiveName(workfileName);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of convertWorkfileNameToShortWorkfileName method, of class Utility.
     */
    @Test
    public void testConvertWorkfileNameToShortWorkfileName() {
        System.out.println("convertWorkfileNameToShortWorkfileName");
        String workfileName = "abc/def/ghi/jklm/filename.java";
        String expResult = "filename.java";
        String result = Utility.convertWorkfileNameToShortWorkfileName(workfileName);
        assertEquals(expResult, result);
    }

    /**
     * Test of formatFilenameForActivityJournal method, of class Utility.
     */
    @Ignore
    @Test
    public void testFormatFilenameForActivityJournal() {
        System.out.println("formatFilenameForActivityJournal");
        String projectName = "";
        String viewName = "";
        String appendedPath = "";
        String shortWorkfileName = "";
        String expResult = "";
        String result = Utility.formatFilenameForActivityJournal(projectName, viewName, appendedPath, shortWorkfileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getFileExtension method, of class Utility.
     */
    @Test
    public void testGetFileExtension() {
        System.out.println("getFileExtension");
        String filename = "weird.filename.java";
        String expResult = "java";
        String result = Utility.getFileExtension(filename);
        assertEquals(expResult, result);
    }

    /**
     * Test of stripFileExtension method, of class Utility.
     */
    @Test
    public void testStripFileExtension() {
        System.out.println("stripFileExtension");
        String filename = "weird.filename.java";
        String expResult = "weird.filename";
        String result = Utility.stripFileExtension(filename);
        assertEquals(expResult, result);
    }

    /**
     * Test of expandBuffer method, of class Utility.
     */
    @Ignore
    @Test
    public void testExpandBuffer() {
        System.out.println("expandBuffer");
        byte[] inputBuffer = null;
        MergedInfoInterface mergedInfo = null;
        ClientExpansionContext clientExpansionContext = null;
        File expResult = null;
        File result = Utility.expandBuffer(inputBuffer, mergedInfo, clientExpansionContext);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of openURL method, of class Utility.
     */
    @Ignore
    @Test
    public void testOpenURL() {
        System.out.println("openURL");
        String url = "";
        Utility.openURL(url);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isMacintosh method, of class Utility.
     */
    @Ignore
    @Test
    public void testIsMacintosh() {
        System.out.println("isMacintosh");
        boolean expResult = false;
        boolean result = Utility.isMacintosh();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of isLinux method, of class Utility.
     */
    @Ignore
    @Test
    public void testIsLinux() {
        System.out.println("isLinux");
        boolean expResult = false;
        boolean result = Utility.isLinux();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of expandStackTraceToString method, of class Utility.
     */
    @Ignore
    @Test
    public void testExpandStackTraceToString() {
        System.out.println("expandStackTraceToString");
        Throwable throwable = null;
        String expResult = "";
        String result = Utility.expandStackTraceToString(throwable);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of endsWithPathSeparator method, of class Utility.
     */
    @Test
    public void testEndsWithPathSeparator() {
        System.out.println("endsWithPathSeparator");
        {
            String path = "path";
            String expResult = "path/";
            String result = Utility.endsWithPathSeparator(path);
            assertEquals(expResult, result);
        }
        {
            String path = "path/";
            String expResult = "path/";
            String result = Utility.endsWithPathSeparator(path);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of endsWithoutPathSeparator method, of class Utility.
     */
    @Ignore
    @Test
    public void testEndsWithoutPathSeparator() {
        System.out.println("endsWithoutPathSeparator");
        {
            String path = "path/";
            String expResult = "path";
            String result = Utility.endsWithoutPathSeparator(path);
            assertEquals(expResult, result);
        }
        {
            String path = "path";
            String expResult = "path";
            String result = Utility.endsWithoutPathSeparator(path);
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of createCemeteryShortArchiveName method, of class Utility.
     */
    @Ignore
    @Test
    public void testCreateCemeteryShortArchiveName() {
        System.out.println("createCemeteryShortArchiveName");
        int fileID = 0;
        String expResult = "";
        String result = Utility.createCemeteryShortArchiveName(fileID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createBranchShortArchiveName method, of class Utility.
     */
    @Ignore
    @Test
    public void testCreateBranchShortArchiveName() {
        System.out.println("createBranchShortArchiveName");
        int fileID = 0;
        String expResult = "";
        String result = Utility.createBranchShortArchiveName(fileID);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of createAppendedPathFromSegments method, of class Utility.
     */
    @Test
    public void testCreateAppendedPathFromSegments() {
        System.out.println("createAppendedPathFromSegments");
        String[] segments = {"this", "is", "the", "appended", "path"};
        String expResult = "this/is/the/appended/path";
        String result = Utility.createAppendedPathFromSegments(segments);
        assertEquals(expResult, result);
    }

    /**
     * Test of deduceOriginalFilenameForUndeleteFromCemetery method, of class Utility.
     */
    @Ignore
    @Test
    public void testDeduceOriginalFilenameForUndeleteFromCemetery() {
        System.out.println("deduceOriginalFilenameForUndeleteFromCemetery");
        ArchiveInfoInterface archiveInfo = null;
        String expResult = "";
        String result = Utility.deduceOriginalFilenameForUndeleteFromCemetery(archiveInfo);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deduceOriginalFilenameForUndeleteFromTranslucentBranchCemetery method, of class Utility.
     */
    @Ignore
    @Test
    public void testDeduceOriginalFilenameForUndeleteFromTranslucentBranchCemetery() {
        System.out.println("deduceOriginalFilenameForUndeleteFromTranslucentBranchCemetery");
        ArchiveInfoInterface archiveInfo = null;
        String expResult = "";
        String result = Utility.deduceOriginalFilenameForUndeleteFromTranslucentBranchCemetery(archiveInfo);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of deduceTypeOfMerge method, of class Utility.
     */
    @Ignore
    @Test
    public void testDeduceTypeOfMerge() throws Exception {
        System.out.println("deduceTypeOfMerge");
        InfoForMerge infoForMerge = null;
        String shortWorkfileName = "";
        MergeType expResult = null;
        MergeType result = Utility.deduceTypeOfMerge(infoForMerge, shortWorkfileName);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of readDataFromStream method, of class Utility.
     */
    @Ignore
    @Test
    public void testReadDataFromStream() throws Exception {
        System.out.println("readDataFromStream");
        byte[] data = null;
        FileInputStream inputStream = null;
        Utility.readDataFromStream(data, inputStream);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of writeDataToStream method, of class Utility.
     */
    @Ignore
    @Test
    public void testWriteDataToStream() throws Exception {
        System.out.println("writeDataToStream");
        byte[] data = null;
        FileOutputStream outputStream = null;
        Utility.writeDataToStream(data, outputStream);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

}
