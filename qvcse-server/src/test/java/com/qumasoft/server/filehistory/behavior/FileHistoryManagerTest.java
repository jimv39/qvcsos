/*
 * Copyright 2014-2021 JimVoris.
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
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.server.LogFile;
import com.qumasoft.server.filehistory.CommitIdentifier;
import com.qumasoft.server.filehistory.CompressionType;
import com.qumasoft.server.filehistory.FileHistoryHeader;
import com.qumasoft.server.filehistory.Revision;
import com.qumasoft.server.filehistory.WorkfileAttributes;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
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
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class FileHistoryManagerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHistoryManagerTest.class);

    private static final AtomicInteger fileIdCounter = new AtomicInteger(0);
    private int fileCount = 0;
    private int totalRevisionCount = 0;
    private int goodRevisionCount = 0;
    private int badRevisionCount = 0;
    private int exceptionCount = 0;

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
     * Test code to convert existing QVCS archive files to their FileHistory representation, and then verify that we can retrieve the FileHistory revisions and that those revisions
     * are identical to the revisions from the old-style QVCS archive file.
     *
     * @throws java.io.IOException for IO problems.
     */
    @Ignore
    @Test
    public void testConvertToFileHistory() throws IOException {
//        Path startingPath = FileSystems.getDefault().getPath("../testenterprise/testDeploy/qvcsProjectsArchiveData/qvcsos");
//        Path startingPath = FileSystems.getDefault().getPath("../../../qRoot/qRootFromVista/qvcsEnterpriseServer/qvcsProjectsArchiveData/qvcse-maven");
//        Path startingPath = FileSystems.getDefault().getPath("/Users/JimVoris/qRoot/qRootFromVista/qvcsEnterpriseServer/qvcsProjectsArchiveData/Remote Secure Java Project");
        Path startingPath = FileSystems.getDefault().getPath("/home/jimv/Tools/qvcsos/testDeploy/qvcsProjectsArchiveData");
//        Path startingPath = FileSystems.getDefault().getPath("/Users/JimVoris/qRoot/qRootFromVista/qvcsEnterpriseServer/qvcsProjectsArchiveData/Remote Secure Java Project/Distribution/ServerWebSite/docs");
        MySimplePathHelper helper = new MySimplePathHelper(startingPath);
        CreateFileHistoryFileVisitor createFileHistoryVisitor = new CreateFileHistoryFileVisitor(helper);
        Path returnedStartingPath = Files.walkFileTree(startingPath, createFileHistoryVisitor);
        LOGGER.info(returnedStartingPath.toString());
        boolean result = verifyRevisionsAreTheSame(helper.mapArchivePathToFileHistoryFile);
        assertTrue(result);
    }

    private boolean verifyRevisionsAreTheSame(Map<String, File> mapArchivePathToFileHistoryFile) throws IOException {
        boolean result = true;
        for (Map.Entry<String, File> entry : mapArchivePathToFileHistoryFile.entrySet()) {
            fileCount++;
            LogFile logfile = new LogFile(entry.getKey());
            FileHistoryManager fileHistoryManager = new FileHistoryManager(entry.getValue());
            // This just guarantees that we have read the history file.
            fileHistoryManager.readFileHistory();
            int revisionCount = logfile.getRevisionCount();
            int historyRevisionCount = fileHistoryManager.readFileHistory().getRevisionByIdMap().size();
            if (revisionCount == historyRevisionCount) {
                LOGGER.info("\nVerifying [" + entry.getKey() + "] ---> [" + entry.getValue().getCanonicalPath() + "], revision count: [" + historyRevisionCount + "]");
                totalRevisionCount += revisionCount;
                RevisionInformation revisionInformation = logfile.getRevisionInformation();
                MutableByteArray mutableByteArray = new MutableByteArray();
                int revisionId = revisionCount;
                for (int j = 0; j < revisionCount; j++) {
                    com.qumasoft.qvcslib.RevisionHeader revHeader = revisionInformation.getRevisionHeader(j);
                    if (revHeader.getDepth() == 0) {
                        LOGGER.trace(revHeader.getRevisionDescriptor().toString());
                        byte[] revisionContent;
                        try {
                            revisionContent = logfile.getRevisionAsByteArray(revHeader.getRevisionString());
                            if (fileHistoryManager.getRevision(revisionId, mutableByteArray)) {
                                byte[] historyContent = mutableByteArray.getValue();
                                boolean equalContentFlag = checkForEqualContent(revisionContent, historyContent);
                                if (!equalContentFlag) {
                                    result = false;
                                    LOGGER.info("\tfailure for: [" + revHeader.getRevisionString() + "] ----> [" + revisionId + "] for file: [" + entry.getKey() + "]");
                                    badRevisionCount++;
                                    break;
                                } else {
                                    goodRevisionCount++;
                                    LOGGER.trace("\t[" + revHeader.getRevisionString() + "] ----> [" + revisionId + "]");
                                }
                            }
                            revisionId--;
                        }
                        catch (QVCSRuntimeException | ArrayIndexOutOfBoundsException e) {
                            LOGGER.warn(logfile.getFullArchiveFilename());
                            LOGGER.warn(Utility.expandStackTraceToString(e), e);
                            exceptionCount++;
                            break;
                        }
                    }
                }
            } else {
                LOGGER.info("Skipping verification of branched file: [" + entry.getKey() + "]");
            }
        }
        LOGGER.info("File Count: [" + fileCount + "]");
        LOGGER.info("Total revision count: [" + totalRevisionCount + "]");
        LOGGER.info("Good revision count: [" + goodRevisionCount + "]");
        LOGGER.info("Bad revision count: [" + badRevisionCount + "]");
        LOGGER.info("Exception count: [" + exceptionCount + "]");
        return result;
    }

    private boolean checkForEqualContent(byte[] revisionContent, byte[] historyContent) {
        boolean compareResult = true;
        if (revisionContent.length == historyContent.length) {
            for (int i = 0; i < revisionContent.length; i++) {
                if (revisionContent[i] != historyContent[i]) {
                    compareResult = false;
                    LOGGER.info("\tFiles begin to differ at index: [" + i + "]");
                    break;
                }
            }
        } else {
            compareResult = false;
            LOGGER.info("\tFile sizes different. Expected size: [" + revisionContent.length + "] actual size: [" + historyContent.length + "]");
            LOGGER.info("\tLast 2 bytes of historyContent: [" + historyContent[historyContent.length - 2] + "]" + "[" + historyContent[historyContent.length - 1] + "]");
            try {
                LOGGER.info("Expected content: \n" + new String(revisionContent, "UTF-8"));
                LOGGER.info("\nActual content: \n" + new String(historyContent, "UTF-8"));
            }
            catch (UnsupportedEncodingException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
            }
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
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            Objects.requireNonNull(path);
            Objects.requireNonNull(attrs);
            String canonicalPath = path.toFile().getCanonicalPath();
            BasicFileAttributeView basicFileAttributeView = Files.getFileAttributeView(path, BasicFileAttributeView.class);
            BasicFileAttributes basicFileAttributes = basicFileAttributeView.readAttributes();
            if (!canonicalPath.endsWith("DirectoryID.dat") && (!canonicalPath.endsWith("qvcs.jou"))) {
                LOGGER.debug("Filename: [{}]", canonicalPath);
                LogFile logfile = new LogFile(path.toFile().getCanonicalPath());
                logfile.readInformation();
                File fileHistoryFile = createFileHistoryFile(fileIdCounter.incrementAndGet());
                FileHistoryManager fileHistoryManager = new FileHistoryManager(fileHistoryFile);
                if (populateFileHistory(fileHistoryManager, logfile)) {
                    pathHelper.mapArchivePathToFileHistoryFile.put(path.toFile().getCanonicalPath(), fileHistoryFile);
                }
            } else {
                LOGGER.info("Skipping: [" + canonicalPath + "]");
            }
            return FileVisitResult.CONTINUE;
        }

        private boolean populateFileHistory(FileHistoryManager fileHistoryManager, LogFile logfile) throws IOException {
            boolean retVal = false;
            populateFileHistoryHeader(fileHistoryManager.readFileHistory().getHeader(), logfile);
            if (addRevisions(fileHistoryManager, logfile)) {
                fileHistoryManager.commit(new CommitIdentifier(1, 1, 1, 1));
                retVal = true;
            }
            return retVal;
        }

        private void populateFileHistoryHeader(FileHistoryHeader header, LogFile logfile) {
            header.setCreator(logfile.getLogFileHeaderInfo().getOwner());
            header.setDescription(logfile.getLogFileHeaderInfo().getModuleDescription());
            header.setFileId(fileIdCounter.get());
            header.setIsBinaryFileFlag(logfile.getLogFileHeaderInfo().getLogFileHeader().getAttributes().getIsBinaryfile());
        }

        private boolean addRevisions(FileHistoryManager fileHistoryManager, LogFile logfile) {
            boolean retVal = true;
            int revisionCount = logfile.getRevisionCount();
            RevisionInformation revisionInformation = logfile.getRevisionInformation();
            int revisionId = 1;
            CommitIdentifier commitIdentifier = new CommitIdentifier(1, 1, 1, 1);
            for (int j = revisionCount - 1; j >= 0; j--) {
                com.qumasoft.qvcslib.RevisionHeader revHeader = revisionInformation.getRevisionHeader(j);
                if (revHeader.getDepth() == 0) {
                    LOGGER.trace(revHeader.toString());
                    byte[] revisionContent;
                    try {
                        revisionContent = logfile.getRevisionAsByteArray(revHeader.getRevisionString());
                        com.qumasoft.server.filehistory.Revision fileHistoryRevision = new com.qumasoft.server.filehistory.Revision();
                        populateRevision(revHeader, logfile, fileHistoryRevision, revisionContent, revisionId, commitIdentifier);
                        boolean computeDeltaFlag = !logfile.getAttributes().getIsBinaryfile();
                        fileHistoryManager.addRevision(null, fileHistoryRevision, computeDeltaFlag);
                        revisionId++;
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        LOGGER.warn("Out of bounds exception for: [" + logfile.getFullArchiveFilename() + "]");
                        LOGGER.warn(Utility.expandStackTraceToString(e));
                        retVal = false;
                        break;
                    }
                }
            }
            return retVal;
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
            revisionHeader.setCompressionType(CompressionType.NOT_COMPRESSED);
            revisionHeader.setDataSize(revisionContent.length);
            fileHistoryRevision.setRevisionData(revisionContent);
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
