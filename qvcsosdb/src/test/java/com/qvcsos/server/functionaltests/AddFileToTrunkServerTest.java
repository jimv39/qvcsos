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
package com.qvcsos.server.functionaltests;

import com.qvcsos.CommonTestHelper;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.dataaccess.FileDAO;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.impl.CommitDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileNameDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileRevisionDAOImpl;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.File;
import com.qvcsos.server.datamodel.FileName;
import com.qvcsos.server.datamodel.FileRevision;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class AddFileToTrunkServerTest {

    /**
     * Create our logger object.
     */
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AddFileToTrunkServerTest.class);

    private static DatabaseManager databaseManager;
    private static String schemaName;

    public AddFileToTrunkServerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        CommonTestHelper.getCommonTestHelper().acquireSyncObject();
        CommonTestHelper.getCommonTestHelper().resetTestDatabaseViaPsqlScript();
        CommonTestHelper.getCommonTestHelper().resetQvcsosTestDatabaseViaPsqlScript();
        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeDatabase();
        schemaName = databaseManager.getSchemaName();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        databaseManager.closeConnection();
        databaseManager.shutdownDatabase();
        CommonTestHelper.getCommonTestHelper().releaseSyncObject();
    }

    @Test
    public void testAddFileToTrunk() throws SQLException {
        Connection connection = databaseManager.getConnection();
        connection.setAutoCommit(false);
        Date now = new Date();
        Timestamp nowTimestamp = new Timestamp(now.getTime());

        // Create a commit object, and insert into database.
        Commit commit = new Commit();
        String commitMessage = "Add file to trunk test commit";
        commit.setCommitMessage(commitMessage);
        commit.setUserId(2);
        CommitDAO commitDAO = new CommitDAOImpl(schemaName);
        Integer commitId = commitDAO.insert(commit);

        // Create a file object, and insert into the database.
        File file = new File();
        file.setProjectId(1);
        FileDAO fileDAO = new FileDAOImpl(schemaName);
        Integer fileId = fileDAO.insert(file);

        // Create a fileName object, and insert into the database.
        FileName fileName = new FileName();
        fileName.setCommitId(commitId);
        fileName.setBranchId(1);
        fileName.setDirectoryId(1);
        fileName.setFileId(fileId);
        fileName.setDeletedFlag(Boolean.FALSE);
        fileName.setFileName("Test File");
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        Integer fileNameId = fileNameDAO.insert(fileName);

        // Create the fileRevision object, and insert into the database.
        FileRevision fileRevision = new FileRevision();
        fileRevision.setBranchId(1);
        fileRevision.setCommitId(commitId);
        fileRevision.setWorkfileEditDate(nowTimestamp);
        fileRevision.setFileId(fileId);
        fileRevision.setRevisionDigest("This is a fake digest".getBytes());
        fileRevision.setRevisionData("This is some fake revision data".getBytes());
        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        Integer fileRevisionId = fileRevisionDAO.insert(fileRevision);
        LOGGER.info("CommitId: [{}], FileId: [{}], FileNameId: [{}], FileRevisionId: [{}]", commitId, fileId, fileNameId, fileRevisionId);

        connection.commit();

        // Verify that the commit row exists.
        Commit fetchedCommit = commitDAO.findById(commitId);
        assertEquals("Unexpected id.", commitId, fetchedCommit.getId());
        assertEquals("Unexpected commit message.", commitMessage, fetchedCommit.getCommitMessage());

    }

}
