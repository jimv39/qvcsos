/*   Copyright 2004-2015 Jim Voris
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

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the LogFileImpl class. These are tests that take a long time to run, so we call them ServerTest, even though they do <b>not</b> start the server.
 *
 * @author Jim Voris
 */
public class LogFileImplServerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogFileImplServerTest.class);

    /**
     * The goal of this test is to verify that we can copy small, medium, and large files using the copyFile method. This test will also show the timing for the respective copies
     * so that we can capture some benchmark of how long it takes to copy a file.
     */
    @Test
    @Ignore
    public void testCopyFileBigfile() {
        File testDestinationFile = null;
        try {
            String dummyArchiveFileName = "dummyArchiveFile";
            String testDestinationFileName = "testDestinationFile";
            LogFileImpl logFileImpl = new LogFileImpl(dummyArchiveFileName);
            File bigFile = new File("bigFile.dat");
            testDestinationFile = File.createTempFile(testDestinationFileName, ".tmp");
            logFileImpl.copyFile(bigFile, testDestinationFile);
            assertEquals("File size mismatch", bigFile.length(), testDestinationFile.length());
        } catch (IOException ex) {
            LOGGER.error(ex.getLocalizedMessage(), ex);
            fail("IOException");
        } finally {
            if (testDestinationFile != null && testDestinationFile.exists()) {
                testDestinationFile.delete();
            }
        }
    }

    /**
     * The goal of this test is to verify that we can copy small, medium, and large files using the copyFile method. This test will also show the timing for the respective copies
     * so that we can capture some benchmark of how long it takes to copy a file.
     */
    @Ignore
    @Test
    public void testCopyFileBiggerfile() {
        File testDestinationFile = null;
        try {
            String dummyArchiveFileName = "dummyArchiveFile";
            String testDestinationFileName = "testDestinationFile";
            LogFileImpl logFileImpl = new LogFileImpl(dummyArchiveFileName);
            File biggerFile = new File("biggerFile.dat");
            testDestinationFile = File.createTempFile(testDestinationFileName, ".tmp");
            logFileImpl.copyFile(biggerFile, testDestinationFile);
            assertEquals("File size mismatch", biggerFile.length(), testDestinationFile.length());
        }
        catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            fail("IOException");
        } finally {
            if (testDestinationFile != null && testDestinationFile.exists()) {
                testDestinationFile.delete();
            }
        }
    }

    /**
     * The goal of this test is to verify that we can copy small, medium, and large files using the copyFile method. This test will also show the timing for the respective copies
     * so that we can capture some benchmark of how long it takes to copy a file.
     */
    @Ignore
    @Test
    public void testCopyFileBiggestfile() {
        File testDestinationFile = null;
        try {
            String dummyArchiveFileName = "dummyArchiveFile";
            String testDestinationFileName = "testDestinationFile";
            LogFileImpl logFileImpl = new LogFileImpl(dummyArchiveFileName);
            File biggestFile = new File("biggestFile.dat");
            testDestinationFile = File.createTempFile(testDestinationFileName, ".tmp");
            logFileImpl.copyFile(biggestFile, testDestinationFile);
            assertEquals("File size mismatch", biggestFile.length(), testDestinationFile.length());
        }
        catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            fail("IOException");
        } finally {
            if (testDestinationFile != null && testDestinationFile.exists()) {
                testDestinationFile.delete();
            }
        }
    }

    /**
     * The goal of this test is to verify that we can copy small, medium, and large files using the copyFile method. This test will also show the timing for the respective copies
     * so that we can capture some benchmark of how long it takes to copy a file.
     */
    @Ignore
    @Test
    public void testCopyFileHugefile() {
        File testDestinationFile = null;
        try {
            String dummyArchiveFileName = "dummyArchiveFile";
            String testDestinationFileName = "testDestinationFile";
            LogFileImpl logFileImpl = new LogFileImpl(dummyArchiveFileName);
            File hugeFile = new File("hugeFile.dat");
            testDestinationFile = File.createTempFile(testDestinationFileName, ".tmp");
            logFileImpl.copyFile(hugeFile, testDestinationFile);
            assertEquals("File size mismatch", hugeFile.length(), testDestinationFile.length());
        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            fail("IOException");
        } finally {
            if (testDestinationFile != null && testDestinationFile.exists()) {
                testDestinationFile.delete();
            }
        }
    }
}
