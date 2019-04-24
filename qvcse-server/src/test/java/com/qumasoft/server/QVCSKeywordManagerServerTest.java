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
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.KeywordExpansionContext;
import com.qumasoft.qvcslib.KeywordManagerFactory;
import com.qumasoft.qvcslib.KeywordManagerInterface;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test keyword expansion.
 * @author Jim Voris
 */
public class QVCSKeywordManagerServerTest {
    private static Object serverSyncObject = null;

    @BeforeClass
    public static void setUpClass() throws Exception {
        serverSyncObject = TestHelper.startServer();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        TestHelper.stopServer(serverSyncObject);
    }

    @Test
    public void testRunInOrder() {
        testExpandKeywords();
        testExpandKeywordsNonTip();
        testExpandKeywordsBranchTip();
        testExpandKeywordsBranchNonTip();
        testBinaryExpandKeywords();
        testContractKeywords();
        testContractKeywordsNoKeywords();
    }

    /**
     * Test of expandKeywords method, of class com.qumasoft.qvcslib.QVCSKeywordManager.
     */
    public void testExpandKeywords() {
        System.out.println("testExpandKeywords");

        // The test
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            // Open a test archive file.
            LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestKeywordExpansion.dqq");

            // Create the directory manager for this.  This is contrived... but it
            // should work.  (Use the directory manager for the project where we
            // stole the archive file above... ).
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(TestHelper.getTestProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "qvcsroot/Source/Get");
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_DEFAULT_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, null);

            KeywordManagerInterface keywordManager = KeywordManagerFactory.getInstance().getKeywordManager();
            inStream = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "TestKeywordExpansion.cpp"));
            File outputFile = new File(System.getProperty("user.dir") + File.separator + "TestKeywordExpansion.expanded.cpp");
            outStream = new FileOutputStream(outputFile);
            LogfileInfo logfileInfo = new LogfileInfo(testArchive.getLogFileHeaderInfo(), testArchive.getRevisionInformation(), testArchive.getFileID(),
                    testArchive.getFullArchiveFilename());
            KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outStream, outputFile, logfileInfo, 0, null,
                    directoryManager.getAppendedPath(), directoryManager.getProjectProperties());
            keywordManager.expandKeywords(inStream, keywordExpansionContext);
        } catch (java.io.IOException e) {
            fail("testExpandKeywords failed due to IOException: " + e.getMessage());
        } catch (QVCSException e) {
            fail("testExpandKeywords failed due to Exception: " + e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Test of expandKeywords method, of class com.qumasoft.qvcslib.QVCSKeywordManager.
     */
    public void testExpandKeywordsNonTip() {
        System.out.println("testExpandKeywordsNonTip");

        // The test
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            // Open a test archive file.
            LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestKeywordExpansion.dqq");

            // Create the directory manager for this.  This is contrived... but it
            // should work.  (Use the directory manager for the project where we
            // stole the archive file above... ).
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(TestHelper.getTestProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "qvcsroot/Source/Get");
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_DEFAULT_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, null);

            KeywordManagerInterface keywordManager = KeywordManagerFactory.getInstance().getKeywordManager();
            inStream = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "TestKeywordExpansion.cpp"));
            File outputFile = new File(System.getProperty("user.dir") + File.separator + "TestKeywordExpansion.expanded.cpp");
            outStream = new FileOutputStream(outputFile);
            LogfileInfo logfileInfo = new LogfileInfo(testArchive.getLogFileHeaderInfo(), testArchive.getRevisionInformation(), testArchive.getFileID(),
                    testArchive.getFullArchiveFilename());
            KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outStream, outputFile, logfileInfo, 1, null, directoryManager.getAppendedPath(),
                    directoryManager.getProjectProperties());
            keywordManager.expandKeywords(inStream, keywordExpansionContext);
        } catch (java.io.IOException e) {
            fail("testExpandKeywords failed due to IOException: " + e.getMessage());
        } catch (QVCSException e) {
            fail("testExpandKeywords failed due to Exception: " + e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Test of expandKeywords method, of class com.qumasoft.qvcslib.QVCSKeywordManager.
     */
    public void testExpandKeywordsBranchTip() {
        System.out.println("testExpandKeywordsBranchTip");

        // The test
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            // Open a test archive file.
            LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestKeywordExpansionBranchNonTip.dqq");

            // Create the directory manager for this.  This is contrived... but it
            // should work.  (Use the directory manager for the project where we
            // stole the archive file above... ).
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(TestHelper.getTestProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "qvcsroot/Source/Get");
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_DEFAULT_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, null);

            KeywordManagerInterface keywordManager = KeywordManagerFactory.getInstance().getKeywordManager();
            inStream = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "TestKeywordBranchTipExpansion.cpp"));
            File outputFile = new File(System.getProperty("user.dir") + File.separator + "TestKeywordBranchTipExpansion.expanded.cpp");
            outStream = new FileOutputStream(outputFile);
            LogfileInfo logfileInfo = new LogfileInfo(testArchive.getLogFileHeaderInfo(), testArchive.getRevisionInformation(), testArchive.getFileID(),
                    testArchive.getFullArchiveFilename());
            KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outStream, outputFile, logfileInfo, 105, null, directoryManager.getAppendedPath(),
                    directoryManager.getProjectProperties());
            keywordManager.expandKeywords(inStream, keywordExpansionContext);  // Need to change the index to the one where the tip Branch revision is located.
        } catch (java.io.IOException e) {
            fail("testExpandKeywordsBranchTip failed due to IOException: " + e.getMessage());
        } catch (QVCSException e) {
            fail("testExpandKeywordsBranchTip failed due to Exception: " + e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Test of expandKeywords method, of class com.qumasoft.qvcslib.QVCSKeywordManager.
     */
    public void testExpandKeywordsBranchNonTip() {
        System.out.println("testExpandKeywordsBranchNonTip");

        // The test
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            // Open a test archive file.
            LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestKeywordExpansionBranchNonTip.dqq");

            // Create the directory manager for this.  This is contrived... but it
            // should work.  (Use the directory manager for the project where we
            // stole the archive file above... ).
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(TestHelper.getTestProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "qvcsroot/Source/Get");
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_DEFAULT_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, null);

            KeywordManagerInterface keywordManager = KeywordManagerFactory.getInstance().getKeywordManager();
            inStream = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "TestKeywordBranchNonTipExpansion.cpp"));
            File outputFile = new File(System.getProperty("user.dir") + File.separator + "TestKeywordBranchNonTipExpansion.expanded.cpp");
            outStream = new FileOutputStream(outputFile);
            LogfileInfo logfileInfo = new LogfileInfo(testArchive.getLogFileHeaderInfo(), testArchive.getRevisionInformation(), testArchive.getFileID(),
                    testArchive.getFullArchiveFilename());
            KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outStream, outputFile, logfileInfo, 104, null, directoryManager.getAppendedPath(),
                    directoryManager.getProjectProperties());
            keywordManager.expandKeywords(inStream, keywordExpansionContext);  // Need to change the index to the one where the tip Branch revision is located.
        } catch (java.io.IOException e) {
            fail("testExpandKeywordsBranchNonTip failed due to IOException: " + e.getMessage());
        } catch (QVCSException e) {
            fail("testExpandKeywordsBranchNonTip failed due to Exception: " + e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Test of testBinaryExpandKeywords
     */
    public void testBinaryExpandKeywords() {
        System.out.println("testBinaryExpandKeywords");

        // The test
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            // Open a test archive file.
            LogFile testArchive = new LogFile(System.getProperty("user.dir") + File.separator + "TestBinaryKeywordExpansion.fpd");

            // Create the directory manager for this.  This is contrived... but it
            // should work.  (Use the directory manager for the project where we
            // stole the archive file above... ).
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(TestHelper.getTestProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "qvcsroot/Source/Get");
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_DEFAULT_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, null);

            KeywordManagerInterface keywordManager = KeywordManagerFactory.getInstance().getKeywordManager();
            inStream = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "BinaryKeywordOriginal.doc"));
            File outputFile = new File(System.getProperty("user.dir") + File.separator + "BinaryKeywordExpanded.doc");
            outStream = new FileOutputStream(outputFile);
            LogfileInfo logfileInfo = new LogfileInfo(testArchive.getLogFileHeaderInfo(), testArchive.getRevisionInformation(), testArchive.getFileID(),
                    testArchive.getFullArchiveFilename());
            KeywordExpansionContext keywordExpansionContext = new KeywordExpansionContext(outStream, outputFile, logfileInfo, 0, null, directoryManager.getAppendedPath(),
                    directoryManager.getProjectProperties());
            keywordManager.expandKeywords(inStream, keywordExpansionContext);
        } catch (java.io.IOException e) {
            fail("testBinaryExpandKeywords failed due to IOException: " + e.getMessage());
        } catch (QVCSException e) {
            fail("testBinaryExpandKeywords failed due to Exception: " + e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Test of contractKeywords method, of class com.qumasoft.qvcslib.QVCSKeywordManager.
     */
    public void testContractKeywords() {
        System.out.println("testContractKeywords");

        // The test
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        AtomicReference<String> embeddedComments = new AtomicReference<>();
        try {
            // Create the directory manager for this.  This is contrived... but it
            // should work.  (Use the directory manager for the project where we
            // stole the archive file above... ).
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(TestHelper.getTestProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "qvcsroot/Source/Get");
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_DEFAULT_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, null);

            KeywordManagerInterface keywordManager = KeywordManagerFactory.getInstance().getKeywordManager();
            inStream = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "TestKeywordExpansion.expanded.cpp"));
            outStream = new FileOutputStream(new File(System.getProperty("user.dir") + File.separator + "TestKeywordExpansion.contracted.cpp"));

            keywordManager.contractKeywords(inStream, outStream, embeddedComments, directoryManager.getProjectProperties(), false);
        } catch (java.io.IOException e) {
            fail("testContractKeywords failed due to IOException: " + e.getMessage());
        } catch (QVCSException e) {
            fail("testContractKeywords failed due to Exception: " + e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    /**
     * Test of contractKeywords method, of class com.qumasoft.qvcslib.QVCSKeywordManager.
     */
    public void testContractKeywordsNoKeywords() {
        System.out.println("testContractKeywordsNoKeywords");

        // The test
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        AtomicReference<String> embeddedComments = new AtomicReference<>();
        try {
            // Create the directory manager for this.  This is contrived... but it
            // should work.  (Use the directory manager for the project where we
            // stole the archive file above... ).
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(TestHelper.getTestProjectName(), QVCSConstants.QVCS_TRUNK_VIEW, "qvcsroot/Source/Get");
            ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_DEFAULT_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, null);

            KeywordManagerInterface keywordManager = KeywordManagerFactory.getInstance().getKeywordManager();
            inStream = new FileInputStream(new File(System.getProperty("user.dir") + File.separator + "readme.expanded.txt"));
            outStream = new FileOutputStream(new File(System.getProperty("user.dir") + File.separator + "readme.contracted.txt"));

            keywordManager.contractKeywords(inStream, outStream, embeddedComments, directoryManager.getProjectProperties(), false);
        } catch (java.io.IOException e) {
            fail("testContractKeywordsNoKeywords failed due to IOException: " + e.getMessage());
        } catch (QVCSException e) {
            fail("testContractKeywordsNoKeywords failed due to Exception: " + e.getMessage());
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException e) {
                }
            }
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                }
            }
        }

        // Now make sure that both files are the same size!
        File originalFile = new File(System.getProperty("user.dir") + File.separator + "readme.txt.expanded");
        File contractedFile = new File(System.getProperty("user.dir") + File.separator + "readme.txt.contracted");

        if (originalFile.length() != contractedFile.length()) {
            fail("testContractKeywordsNoKeywords failed! Output file is different in size from input file");
        }
    }
}
