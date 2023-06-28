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
package com.qvcsos.server;

import com.qumasoft.qvcslib.BogusResponseObject;
import com.qvcsos.CommonTestHelper;
import com.qvcsos.server.dataaccess.DirectoryLocationDAO;
import com.qvcsos.server.dataaccess.impl.DirectoryLocationDAOImpl;
import com.qvcsos.server.datamodel.Directory;
import com.qvcsos.server.datamodel.DirectoryLocation;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Walk a directory tree, populating the database along the way with the files/directories that we find there.
 * @author Jim Voris
 */
@Ignore
public class PopulateDatabaseServerTest {
    /**
     * Create our LOGGER.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PopulateDatabaseServerTest.class);

    private static DatabaseManager databaseManager;
    private static final BogusResponseObject BOGUS_RESPONSE = new BogusResponseObject();

    @BeforeClass
    public static void setUpClass() throws Exception {
        CommonTestHelper.getCommonTestHelper().acquireSyncObject();
        CommonTestHelper.getCommonTestHelper().resetTestDatabaseViaPsqlScript();
        CommonTestHelper.getCommonTestHelper().resetQvcsosTestDatabaseViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        String uname = "qvcsosdbtest";
        databaseManager.setUsername(uname);
        String pword = "qvcsosdbtestPG$Admin";
        databaseManager.setPassword(pword);
        String url = "jdbc:postgresql://localhost:5432/qvcsosdbtest";
        databaseManager.setUrl(url);
        databaseManager.initializeDatabase();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        databaseManager.closeConnection();
        databaseManager.shutdownDatabase();
        CommonTestHelper.getCommonTestHelper().releaseSyncObject();
    }


    @Test
    public void testPopulateDatabaseFromDirectoryTree() throws Exception {

        // Look at a directory that we don't care about.
        Path startingPath = FileSystems.getDefault().getPath("/home/jimv/dev/one-time-pad");
        Directory startingDirectoryObject = new Directory();
        startingDirectoryObject.setId(1);
        startingDirectoryObject.setProjectId(1);
        MySimpleDirectoryHelper helper = new MySimpleDirectoryHelper(startingDirectoryObject);
        PopulateDatabaseVisitor populateDatabaseVisitor = new PopulateDatabaseVisitor(helper);
        Path returnedStartingPath = Files.walkFileTree(startingPath, populateDatabaseVisitor);
        LOGGER.info(returnedStartingPath.toString());

    }

    static class PopulateDatabaseVisitor extends SimpleFileVisitor<Path> {

        MySimpleDirectoryHelper directoryHelper;
        Stack<Directory> directoryStack = new Stack<>();
        Stack<DirectoryLocation> directoryLocationStack = new Stack<>();
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();

        PopulateDatabaseVisitor(MySimpleDirectoryHelper mySimpleDirectoryHelper) {
            this.directoryHelper = mySimpleDirectoryHelper;
            sourceControlBehaviorManager.setUserId(2);
            sourceControlBehaviorManager.setResponse(new BogusResponseObject());
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            FileVisitResult retVal = FileVisitResult.SKIP_SUBTREE;
            Objects.requireNonNull(dir);
            Objects.requireNonNull(attrs);
            if (!dir.toFile().getName().startsWith(".")) {
                directoryStack.push(directoryHelper.currentDirectory);
                directoryLocationStack.push(directoryHelper.currentDirectoryLocation);
                Integer newDirectoryLocationId;
                DirectoryLocation directoryLocation;
                try {
                    DbTestHelper.beginTransaction(BOGUS_RESPONSE);
                    newDirectoryLocationId = sourceControlBehaviorManager.addDirectory(directoryHelper.currentDirectoryLocation.getBranchId(), directoryHelper.currentDirectory.getProjectId(),
                            directoryHelper.currentDirectoryLocation.getId(), dir.toFile().getName());
                    DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(databaseManager.getSchemaName());
                    directoryLocation = directoryLocationDAO.findById(newDirectoryLocationId);
                } catch (SQLException e) {
                    LOGGER.warn("Failed to add directory: [{}]", dir.toString(), e);
                    throw new IOException("Get out of here.");
                }
                Directory directory = new Directory();
                directory.setId(directoryLocation.getDirectoryId());
                directory.setProjectId(directoryHelper.currentDirectory.getProjectId());
                directoryHelper.currentDirectory = directory;
                directoryHelper.currentDirectoryLocation = directoryLocation;
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
            DbTestHelper.endTransaction(BOGUS_RESPONSE);
            directoryHelper.currentDirectory = directoryStack.pop();
            directoryHelper.currentDirectoryLocation = directoryLocationStack.pop();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
            Objects.requireNonNull(path);
            Objects.requireNonNull(attrs);
            String canonicalPath = path.toFile().getCanonicalPath();
            if (path.toFile().exists()) {
                BasicFileAttributeView basicFileAttributeView = Files.getFileAttributeView(path, BasicFileAttributeView.class);
                BasicFileAttributes basicFileAttributes = basicFileAttributeView.readAttributes();
                if (basicFileAttributes.isSymbolicLink()) {
                    LOGGER.info("Skipping symbolic link: [{}]", canonicalPath);
                } else if (!canonicalPath.endsWith("DirectoryID.dat") && (!canonicalPath.endsWith("qvcs.jou"))) {
                    // Add a file.
                    try {
                        Date now = new Date();
                        Timestamp nowTimestamp = new Timestamp(now.getTime());
                        sourceControlBehaviorManager.addFile(directoryHelper.currentDirectoryLocation.getBranchId(), directoryHelper.currentDirectory.getProjectId(),
                                directoryHelper.currentDirectory.getId(),
                                path.toFile().getName(), path.toFile(), null, directoryHelper.currentDirectoryLocation.getCommitId(), nowTimestamp, null, new AtomicInteger());
                    } catch (SQLException e) {
                        LOGGER.warn("Failed to add file.", e);
                    }
                } else {
                    LOGGER.info("Skipping: [{}]", canonicalPath);
                }
            } else {
                LOGGER.info("Skipping non-existant file: [{}]", canonicalPath);
            }
            return FileVisitResult.CONTINUE;
        }
    }

    static class MySimpleDirectoryHelper {

        Directory currentDirectory;
        DirectoryLocation currentDirectoryLocation;

        MySimpleDirectoryHelper(Directory directory) {
            this.currentDirectory = directory;
            DirectoryLocation directoryLocation = new DirectoryLocation();
            directoryLocation.setId(1);
            directoryLocation.setBranchId(1);
            directoryLocation.setCommitId(1);
            directoryLocation.setDeletedFlag(Boolean.FALSE);
            directoryLocation.setDirectorySegmentName("");
            directoryLocation.setParentDirectoryLocationId(null);
            this.currentDirectoryLocation = directoryLocation;
        }
    }
}
