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
package com.qvcsos.server.archivemigration;

import com.qumasoft.qvcslib.BogusResponseObject;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestAddDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerAddUserData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateProjectData;
import com.qumasoft.server.AuthenticationManager;
import com.qumasoft.server.RoleManager;
import com.qumasoft.server.ServerUtility;
import com.qumasoft.server.clientrequest.ClientRequestAddDirectory;
import com.qumasoft.server.clientrequest.ClientRequestServerAddUser;
import com.qumasoft.server.clientrequest.ClientRequestServerCreateProject;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.UserDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileRevisionDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.dataaccess.impl.UserDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.FileRevision;
import com.qvcsos.server.datamodel.Project;
import com.qvcsos.server.datamodel.User;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Migrate file revision history from QVCS archive files into postgres database.
 *
 * @author Jim Voris
 */
@Ignore
public class ArchiveMigrationTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveMigrationTest.class);
    private static DatabaseManager databaseManager;

    private static final String BASEDIRECTORY = "/home/jimv/tmp/qvcsProjectsArchiveData";
    private static final String PROJECTNAME = "qvcsos";
    private static int fileCounter = 0;
    private static final StringBuilder currentProjectName = new StringBuilder();
    private static final StringBuilder currentAppendedPath = new StringBuilder();
    private static Integer currentProjectId;
    private static Integer currentBranchId;
    private static Integer currentFileId;

    public ArchiveMigrationTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        resetMigrationDatabaseViaPsqlScript();
        setUpForMigrationViaPsqlScript();

        databaseManager = DatabaseManager.getInstance();
        databaseManager.initializeToMigrationDatabase();
        updateAdminPassword();
        try {
            AuthenticationManager.getAuthenticationManager().initialize();
            RoleManager.getRoleManager().initialize();
        }
        catch (SQLException ex) {
            LOGGER.warn("Failed to initialize RoleManager.", ex);
        }
    }

    /**
     * Use a psql script to reset the test database.
     */
    public static void resetMigrationDatabaseViaPsqlScript() {
        String userDir = System.getProperty("user.dir");
        try {
            String execString = String.format("psql -f %s/postgres_qvcsos410_legacy_script.sql postgresql://postgres:postgres@localhost:5433/postgres", userDir);
            Process p = Runtime.getRuntime().exec(execString);
            p.waitFor();
            LOGGER.info("Reset test database process exit value: [{}]", p.exitValue());
        } catch (IOException | InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
    }

    /**
     * Use a psql script to reset the test database.
     */
    public static void setUpForMigrationViaPsqlScript() {
        String userDir = System.getProperty("user.dir");
        try {
            String execString = String.format("psql -f %s/postgres_qvcsos410_migration_legacy_script.sql postgresql://postgres:postgres@localhost:5433/postgres", userDir);
            Process p = Runtime.getRuntime().exec(execString);
            p.waitFor();
            LOGGER.info("Migration script exit value: [{}]", p.exitValue());
        } catch (IOException | InterruptedException ex) {
            LOGGER.warn(ex.getLocalizedMessage(), ex);
        }
    }

    private static boolean updateAdminPassword() {
        boolean flag = false;
        UserDAO userDAO = new UserDAOImpl(databaseManager.getSchemaName());
        User user = userDAO.findByUserName("ADMIN");
        if (user != null) {
            byte[] hashedPassword = Utility.getInstance().hashPassword("ADMIN");
            flag = userDAO.updateUserPassword(user.getId(), hashedPassword);
        } else {
            LOGGER.info("failed to update ADMIN password");
        }
        return flag;
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
     * Test code to capture file revision data from QVCS archive files into the
     * postgres database representation, and then verify that we can retrieve
     * the file's revisions and that those revisions are identical to the
     * revisions from the old-style QVCS archive file.
     *
     * @throws java.io.IOException for IO problems.
     */
//    @Ignore
    @Test
    public void testConvertToFileHistory() throws IOException {
        Path startingPath = FileSystems.getDefault().getPath(BASEDIRECTORY + File.separator + PROJECTNAME);
//        Path startingPath = FileSystems.getDefault().getPath(baseDirectory);
        MySimplePathHelper helper = new MySimplePathHelper(startingPath);
        PopulateDatabaseFileVisitor createFileHistoryVisitor = new PopulateDatabaseFileVisitor(helper);
        Path returnedStartingPath = Files.walkFileTree(startingPath, createFileHistoryVisitor);
        LOGGER.info(returnedStartingPath.toString());
    }

    static class PopulateDatabaseFileVisitor extends SimpleFileVisitor<Path> {

        MySimplePathHelper pathHelper;
        Stack<Path> directoryIdStack = new Stack<>();

        PopulateDatabaseFileVisitor(MySimplePathHelper pathHelper) {
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
                LegacyLogFile logfile = new LegacyLogFile(path.toFile().getCanonicalPath());
                logfile.readInformation();
                boolean ignoreFlag = createProjectsAndDirectories(canonicalPath);
                if (!ignoreFlag) {
                    if (populateDatabaseForArchiveFile(logfile)) {
                        LOGGER.info("populated db for: [{}]", logfile.getFileName());
                    }
                }
            } else {
                LOGGER.info("Skipping: [" + canonicalPath + "]");
            }
            return FileVisitResult.CONTINUE;
        }

        private boolean populateDatabaseForArchiveFile(LegacyLogFile logfile) throws IOException {
            boolean retVal = false;
            if (addRevisions(logfile)) {
                retVal = true;
            }
            return retVal;
        }

        private boolean addRevisions(LegacyLogFile logfile) {
            boolean retVal = true;
            int revisionCount = logfile.getRevisionCount();
            LegacyRevisionInformation revisionInformation = logfile.getRevisionInformation();
            if (revisionInformation != null) {
                int revisionId = 1;
                for (int j = revisionCount - 1; j >= 0; j--) {
                    LegacyRevisionHeader revHeader = revisionInformation.getRevisionHeader(j);
                    if (revHeader.getDepth() == 0) {
                        LOGGER.trace(revHeader.toString());
                        byte[] revisionContent;
                        try {
                            revisionContent = logfile.getRevisionAsByteArray(revHeader.getRevisionString());
                            if (revisionId == 1) {
                                addFile(logfile, revHeader, revisionContent);
                            } else {
                                addRevision(logfile, revHeader, revisionContent);
                            }
                            revisionId++;
                        }
                        catch (SQLException | ArrayIndexOutOfBoundsException e) {
                            LOGGER.warn("Exception for: [" + logfile.getFileName() + "]");
                            LOGGER.warn(Utility.expandStackTraceToString(e));
                            retVal = false;
                            break;
                        }
                    }
                }
            } else {
                LOGGER.info("Failed to read logfile: [{}]", logfile.getShortWorkfileName());
                retVal = false;
            }
            return retVal;
        }

        private boolean createProjectsAndDirectories(String canonicalPath) {
            boolean ignoreFlag = false;
            String interestingPartOfPath = canonicalPath.substring(1 + BASEDIRECTORY.length());
            String[] segments = interestingPartOfPath.split("/");
            String projectName = segments[0];
            String archiveFileName = segments[segments.length - 1];
            String shortWorkfileName = LegacyUtility.convertArchiveNameToShortWorkfileName(archiveFileName);
            StringBuilder appendedPathBuilder = new StringBuilder();
            int segmentIndex = 0;
            for (int i = 1; i < segments.length - 1; i++) {
                appendedPathBuilder.append(segments[i]).append("/");
                segmentIndex++;
            }
            String appendedPath = appendedPathBuilder.toString();
            if (!appendedPath.isEmpty()) {
                appendedPath = appendedPath.substring(0, appendedPath.length() - 1);
            }
            if ((0 == appendedPath.compareTo("qvcsDirectoryMetaDataDirectory")) || (0 == appendedPath.compareTo("qvcsCemeteryDirectory"))) {
                ignoreFlag = true;
            } else {
                fileCounter++;
                LOGGER.info("File counter: [{}]; ProjectName: [{}]; appendedPath: [{}]; fileName: [{}]", fileCounter, projectName, appendedPath, shortWorkfileName);
            }
            if (0 != currentProjectName.toString().compareTo(projectName)) {
                LOGGER.info("Project name changed from: [{}] to [{}]", currentProjectName.toString(), projectName);
                currentProjectName.replace(0, currentProjectName.toString().length(), projectName);
                // Create the Project record in the database.
                createProjectRecord(projectName);
            }
            if (!ignoreFlag) {
                if (0 != currentAppendedPath.toString().compareTo(appendedPath)) {
                    LOGGER.info("Appended path changed from: [{}] to [{}]", currentAppendedPath.toString(), appendedPath);
                    currentAppendedPath.replace(0, currentAppendedPath.toString().length(), appendedPath);
                    // Create the directory in the database.
                    createProjectDirectory(appendedPath);
                }
            }
            return ignoreFlag;
        }

        private void createProjectRecord(String projectName) {
            ClientRequestServerCreateProjectData data = new ClientRequestServerCreateProjectData();
            data.setUserName("ADMIN");
            byte[] hashedPassword = Utility.getInstance().hashPassword("ADMIN");
            data.setPassword(hashedPassword);
            data.setNewProjectName(projectName);
            ClientRequestServerCreateProject clientRequestServerCreateProject = new ClientRequestServerCreateProject(data);
            clientRequestServerCreateProject.execute("ADMIN", new BogusResponseObject());

            ProjectDAO projectDAO = new ProjectDAOImpl(databaseManager.getSchemaName());
            Project currentProject = projectDAO.findByProjectName(projectName);
            currentProjectId = currentProject.getId();

            BranchDAO branchDAO = new BranchDAOImpl(databaseManager.getSchemaName());
            Branch currentBranch = branchDAO.findByProjectIdAndBranchName(currentProjectId, QVCSConstants.QVCS_TRUNK_BRANCH);
            currentBranchId = currentBranch.getId();
            LOGGER.info("Created project:[{}]; currentProjectId: [{}]; currentBranchId: [{}]", projectName, currentProjectId, currentBranchId);
        }

        private void createProjectDirectory(String appendedPath) {
            ClientRequestAddDirectoryData data = new ClientRequestAddDirectoryData();
            data.setProjectName(currentProjectName.toString());
            data.setAppendedPath(appendedPath);
            data.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);

            ClientRequestAddDirectory addDirectory = new ClientRequestAddDirectory(data);
            addDirectory.execute("Migrator", new BogusResponseObject());
        }

        private void addFile(LegacyLogFile logfile, LegacyRevisionHeader revHeader, byte[] revisionContent) throws SQLException {
            LegacyAccessList accessList = new LegacyAccessList(logfile.getLogFileHeaderInfo().getModifierList());
            String userName = accessList.indexToUser(revHeader.getCreatorIndex());
            int userId = addOrVerifyUser(userName);

            SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
            instance.setResponse(new BogusResponseObject());
            instance.setUserId(userId);

            AtomicInteger migratedRevisionId = new AtomicInteger();
            String commitMessage = "Migrated revision: " + revHeader.getRevisionString() + " : " + revHeader.getRevisionDescription();
            File addingFile = ServerUtility.createTempFileFromBuffer("migration_temp_file", revisionContent);
            currentFileId = instance.addFile(QVCSConstants.QVCS_TRUNK_BRANCH, currentProjectName.toString(), currentAppendedPath.toString(),
                    logfile.getShortWorkfileName(), addingFile, revHeader.getEditDate(), commitMessage, migratedRevisionId);
            LOGGER.info("Created file: [{}] with fileId: [{}] and revisionId: [{}]", logfile.getShortWorkfileName(), currentFileId, migratedRevisionId.intValue());
            instance.clearThreadLocals();

            FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(databaseManager.getSchemaName());
            FileRevision justInsertedRevision = fileRevisionDAO.findById(migratedRevisionId.intValue());
            MigrationCommitDAOImpl migrationCommitDAOImpl = new MigrationCommitDAOImpl(databaseManager.getSchemaName());
            migrationCommitDAOImpl.updateCommitDate(justInsertedRevision.getCommitId(), revHeader.getCheckInDate());
        }

        private void addRevision(LegacyLogFile logfile, LegacyRevisionHeader revHeader, byte[] revisionContent) throws SQLException {
            LegacyAccessList accessList = new LegacyAccessList(logfile.getLogFileHeaderInfo().getModifierList());
            String userName = accessList.indexToUser(revHeader.getCreatorIndex());
            int userId = addOrVerifyUser(userName);

            SourceControlBehaviorManager instance = SourceControlBehaviorManager.getInstance();
            instance.setResponse(new BogusResponseObject());
            instance.setUserId(userId);

            String commitMessage = "Migrated revision: " + revHeader.getRevisionString() + " : " + revHeader.getRevisionDescription();
            Integer revisionId = instance.addRevision(currentBranchId, currentFileId, revisionContent, null, revHeader.getEditDate(), commitMessage);
            instance.clearThreadLocals();

            FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(databaseManager.getSchemaName());
            FileRevision justInsertedRevision = fileRevisionDAO.findById(revisionId);
            MigrationCommitDAOImpl migrationCommitDAOImpl = new MigrationCommitDAOImpl(databaseManager.getSchemaName());
            migrationCommitDAOImpl.updateCommitDate(justInsertedRevision.getCommitId(), revHeader.getCheckInDate());

            LOGGER.info("File: [{}]; Added revision: [{}] for old revision: [{}]", logfile.getShortWorkfileName(), revisionId, revHeader.getRevisionString());
        }

        private int addOrVerifyUser(String userName) {
            Integer userId;
            UserDAO userDAO = new UserDAOImpl(databaseManager.getSchemaName());
            User user = userDAO.findByUserName(userName);
            if (user == null) {
                ClientRequestServerAddUserData data = new ClientRequestServerAddUserData();
                byte[] hashedPassword = Utility.getInstance().hashPassword("password");
                data.setUserName(userName);
                data.setPassword(hashedPassword);
                ClientRequestServerAddUser clientRequestServerAddUser = new ClientRequestServerAddUser(data);
                clientRequestServerAddUser.execute("ADMIN", new BogusResponseObject());
                user = userDAO.findByUserName(userName);
                if (user != null) {
                    userId = user.getId();
                } else {
                    userId = -1;
                }
                // TODO -- Should give the user needed roles for the current project.

            } else {
                userId = user.getId();
            }
            return userId;
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
