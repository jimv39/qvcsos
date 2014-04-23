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
package com.qumasoft.server.filehistory.behavior;

import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.CompressionFactory;
import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.RevisionCompressionHeader;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.server.LogFile;
import com.qumasoft.server.filehistory.CommitIdentifier;
import com.qumasoft.server.filehistory.CompressionType;
import com.qumasoft.server.filehistory.FileHistoryHeader;
import com.qumasoft.server.filehistory.Revision;
import com.qumasoft.server.filehistory.WorkfileAttributes;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author Jim Voris
 */
public class FileHistoryManagerTest {

    private static final AtomicInteger fileIdCounter = new AtomicInteger(0);

    public FileHistoryManagerTest() {
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
     * Test of fetchRevision method, of class FileHistoryManager.
     */
    @Test
    public void testFetchRevision() {
        System.out.println("fetchRevision");
//        FileHistorySummary summary = null;
//        Integer revisionId = null;
//        MutableByteArray result_2 = null;
//        FileHistoryManager instance = new FileHistoryManager();
//        boolean expResult = false;
//        boolean result = instance.fetchRevision(summary, revisionId, result_2);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of addRevision method, of class FileHistoryManager.
     */
    @Test
    public void testAddRevision() {
//        System.out.println("addRevision");
//        FileHistorySummary summary = null;
//        BehaviorContext context = null;
//        Revision revisionToAdd = null;
//        FileHistoryManager instance = new FileHistoryManager();
//        Integer expResult = null;
//        Integer result = instance.addRevision(summary, context, revisionToAdd);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of commit method, of class FileHistoryManager.
     */
    @Test
    public void testCommit() {
//        System.out.println("commit");
//        CommitIdentifier commitIdentifier = null;
//        FileHistoryManager instance = new FileHistoryManager();
//        boolean expResult = false;
//        boolean result = instance.commit(commitIdentifier);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test code to convert existing QVCS archive files to their FileHistory representation, and then verify that we can retrieve the FileHistory revisions and that those revisions
     * are identical to the revisions from the old-style QVCS archive file.
     */
    @Test
    public void testConvertToFileHistory() throws IOException {
//        Path startingPath = FileSystems.getDefault().getPath("../testenterprise/testDeploy/qvcsProjectsArchiveData/qvcsos");
//        Path startingPath = FileSystems.getDefault().getPath("../../../qRoot/qRootFromVista/qvcsEnterpriseServer/qvcsProjectsArchiveData/qvcse-maven");
        Path startingPath = FileSystems.getDefault().getPath("../../../qRoot/qRootFromVista/qvcsEnterpriseServer/qvcsProjectsArchiveData/Remote Secure Java Project");
        MySimplePathHelper helper = new MySimplePathHelper(startingPath);
        CreateFileHistoryFileVisitor createFileHistoryVisitor = new CreateFileHistoryFileVisitor(helper);
        Path returnedStartingPath = Files.walkFileTree(startingPath, createFileHistoryVisitor);
        boolean result = verifyRevisionsAreTheSame(helper.mapArchivePathToFileHistoryFile);
        assertTrue(result);
    }

    private boolean verifyRevisionsAreTheSame(Map<String, File> mapArchivePathToFileHistoryFile) throws IOException {
        boolean result = true;
        for (Map.Entry<String, File> entry : mapArchivePathToFileHistoryFile.entrySet()) {
            LogFile logfile = new LogFile(entry.getKey());
            FileHistoryManager fileHistoryManager = new FileHistoryManager(entry.getValue());
            System.out.println("Verifying [" + entry.getKey() +"] ---> [" + entry.getValue().getCanonicalPath() + "]");
            // This just guarantees that we have read the history file.
            fileHistoryManager.readFileHistory();
            int revisionCount = logfile.getRevisionCount();
            RevisionInformation revisionInformation = logfile.getRevisionInformation();
            MutableByteArray mutableByteArray = new MutableByteArray();
            int revisionId = 1;
            for (int j = revisionCount - 1; j >= 0; j--) {
                com.qumasoft.qvcslib.RevisionHeader revHeader = revisionInformation.getRevisionHeader(j);
                if (revHeader.getDepth() == 0) {
//                    System.out.println(revHeader.toString());
                    byte[] revisionContent;
                    try {
                        revisionContent = logfile.getRevisionAsByteArray(revHeader.getRevisionString());
                        if (fileHistoryManager.fetchRevision(null, revisionId, mutableByteArray)) {
                            byte[] historyContent = mutableByteArray.getValue();
                            boolean equalContentFlag = checkForEqualContent(revisionContent, historyContent);
                            if (!equalContentFlag) {
                                result = false;
                                break;
                            } else {
                                System.out.println("\t[" + revHeader.getRevisionString() + "] ----> [" + revisionId + "]");
                            }
                        }
                        revisionId++;
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(e.getLocalizedMessage());
                    }
                }
            }
        }
        return result;
    }

    private boolean checkForEqualContent(byte[] revisionContent, byte[] historyContent) {
        boolean compareResult = true;
        if (revisionContent.length == historyContent.length) {
            for (int i = 0; i < revisionContent.length; i++) {
                if (revisionContent[i] != historyContent[i]) {
                    compareResult = false;
                    break;
                }
            }
        } else {
            compareResult = false;
        }
        return compareResult;
    }

    static class CreateFileHistoryFileVisitor extends SimpleFileVisitor<Path> {

        MySimplePathHelper pathHelper;
        Stack<Path> directoryIdStack = new Stack<>();

        CreateFileHistoryFileVisitor(MySimplePathHelper pathHelper) {
            this.pathHelper = pathHelper;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            FileVisitResult retVal = FileVisitResult.SKIP_SUBTREE;
            Objects.requireNonNull(dir);
            Objects.requireNonNull(attrs);
            if (!dir.toFile().getName().startsWith(".")) {
                directoryIdStack.push(pathHelper.currentDirectory);
                pathHelper.currentDirectory = dir;
                retVal = FileVisitResult.CONTINUE;
            }
            return retVal;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Objects.requireNonNull(dir);
            if (exc != null) {
                throw exc;
            }
            pathHelper.currentDirectory = directoryIdStack.pop();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Objects.requireNonNull(file);
            Objects.requireNonNull(attrs);
            String canonicalPath = file.toFile().getCanonicalPath();
            if (!canonicalPath.endsWith("DirectoryID.dat") && (!canonicalPath.endsWith("qvcs.jou"))) {
                System.out.println("Filename: [" + canonicalPath + "]");
                LogFile logfile = new LogFile(file.toFile().getCanonicalPath());
                logfile.readInformation();
                File fileHistoryFile = createFileHistoryFile(fileIdCounter.incrementAndGet());
                FileHistoryManager fileHistoryManager = new FileHistoryManager(fileHistoryFile);
                populateFileHistory(fileHistoryManager, logfile);
                pathHelper.mapArchivePathToFileHistoryFile.put(file.toFile().getCanonicalPath(), fileHistoryFile);
            } else {
                System.out.println("Skipping: [" + canonicalPath + "]");
            }
            return FileVisitResult.CONTINUE;
        }

        private void populateFileHistory(FileHistoryManager fileHistoryManager, LogFile logfile) throws IOException {
            populateFileHistoryHeader(fileHistoryManager.readFileHistory().getHeader(), logfile);
            addRevisions(fileHistoryManager, logfile);
            fileHistoryManager.commit(new CommitIdentifier(1, 1, 1));
        }

        private void populateFileHistoryHeader(FileHistoryHeader header, LogFile logfile) {
            header.setCreator(logfile.getLogFileHeaderInfo().getOwner());
            header.setDescription(logfile.getLogFileHeaderInfo().getModuleDescription());
            header.setFileId(fileIdCounter.get());
            header.setIsBinaryFileFlag(logfile.getLogFileHeaderInfo().getLogFileHeader().getAttributes().getIsBinaryfile());
        }

        private void addRevisions(FileHistoryManager fileHistoryManager, LogFile logfile) {
            int revisionCount = logfile.getRevisionCount();
            RevisionInformation revisionInformation = logfile.getRevisionInformation();
            int revisionId = 1;
            CommitIdentifier commitIdentifier = new CommitIdentifier(1, 1, 1);
            for (int j = revisionCount - 1; j >= 0; j--) {
                com.qumasoft.qvcslib.RevisionHeader revHeader = revisionInformation.getRevisionHeader(j);
                if (revHeader.getDepth() == 0) {
//                    System.out.println(revHeader.toString());
                    byte[] revisionContent;
                    try {
                        revisionContent = logfile.getRevisionAsByteArray(revHeader.getRevisionString());
                        com.qumasoft.server.filehistory.Revision fileHistoryRevision = new com.qumasoft.server.filehistory.Revision();
                        populateRevision(revHeader, logfile, fileHistoryRevision, revisionContent, revisionId, commitIdentifier);
                        fileHistoryManager.addRevision(null, null, fileHistoryRevision);
                        revisionId++;
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println(e.getLocalizedMessage());
                    }
                }
            }
        }

        private void populateRevision(RevisionHeader revHeader, LogFile logfile, Revision fileHistoryRevision, byte[] revisionContent, int revisionId, CommitIdentifier cid) {
            AccessList accessList = new AccessList(logfile.getLogFileHeaderInfo().getModifierList());
            com.qumasoft.server.filehistory.RevisionHeader revisionHeader = fileHistoryRevision.getHeader();
            revisionHeader.setAuthor(accessList.indexToUser(revHeader.getCreatorIndex()));
            revisionHeader.setCommitDate(revHeader.getCheckInDate());
            revisionHeader.setWorkfileEditDate(revHeader.getEditDate());
            revisionHeader.setCommitIdentifier(cid);
            revisionHeader.setAttributes(new WorkfileAttributes());
            revisionHeader.setDescription(revHeader.getRevisionDescription());
            // we're not doing deltas at all!!
            revisionHeader.setReverseDeltaRevisionId(-1);
            revisionHeader.setId(revisionId);
            revisionHeader.setAncestorRevisionId(revisionId == 1 ? -1 : revisionId - 1);
            RevisionCompressionHeader revisionCompressionHeader = new RevisionCompressionHeader();
            revisionCompressionHeader.setCompressionType(RevisionCompressionHeader.COMPRESS_ALGORITHM_2);
            revisionCompressionHeader.setInputSize(revisionContent.length);
            Compressor compressor = CompressionFactory.getCompressor(revisionCompressionHeader);
            if (compressor.compress(revisionContent)) {
                revisionHeader.setCompressionType(CompressionType.ZLIB_COMPRESSED);
                fileHistoryRevision.setRevisionData(compressor.getCompressedBuffer());
                revisionHeader.setDataSize(compressor.getCompressedBuffer().length);
            } else {
                revisionHeader.setCompressionType(CompressionType.NOT_COMPRESSED);
                revisionHeader.setDataSize(revisionContent.length);
                fileHistoryRevision.setRevisionData(revisionContent);
            }
        }

        private File createFileHistoryFile(int fileId) {
            int directory = fileId % 50;
            String directoryName = String.format("testArchives/%04d", directory);
            File directoryFile = new File(directoryName);
            String filename = String.format("testArchives/%04d/%08d", directory, fileId);
            File historyFile = new File(filename);
            directoryFile.mkdirs();
            return historyFile;
        }

    }

    static class MySimplePathHelper {

        Path currentDirectory;
        Map<String, File> mapArchivePathToFileHistoryFile;

        MySimplePathHelper(Path path) {
            this.currentDirectory = path;
            mapArchivePathToFileHistoryFile = new HashMap<>();
        }
    }

}
