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

import com.qumasoft.qvcslib.CompareFilesEditHeader;
import com.qumasoft.qvcslib.CompareFilesEditInformation;
import com.qumasoft.qvcslib.CompareFilesWithApacheDiff;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.dataaccess.DirectoryDAO;
import com.qvcsos.server.dataaccess.DirectoryLocationDAO;
import com.qvcsos.server.dataaccess.FileDAO;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.TagDAO;
import com.qvcsos.server.dataaccess.UserDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.CommitDAOImpl;
import com.qvcsos.server.dataaccess.impl.DirectoryDAOImpl;
import com.qvcsos.server.dataaccess.impl.DirectoryLocationDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileNameDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileRevisionDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.dataaccess.impl.TagDAOImpl;
import com.qvcsos.server.dataaccess.impl.UserDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.Directory;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.File;
import com.qvcsos.server.datamodel.FileName;
import com.qvcsos.server.datamodel.FileRevision;
import com.qvcsos.server.datamodel.Project;
import com.qvcsos.server.datamodel.Tag;
import com.qvcsos.server.datamodel.User;
import com.qvcsos.server.dbrepair.RepairCompareFilesEditHeader;
import com.qvcsos.server.dbrepair.RepairCompareFilesEditInformation;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public final class SourceControlBehaviorManager implements TransactionParticipantInterface {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SourceControlBehaviorManager.class);

    /**
     * Our singleton SourceControlBehaviorManager instance
     */
    private static final SourceControlBehaviorManager SOURCE_CONTROL_BEHAVIOR_MANAGER = new SourceControlBehaviorManager();

    private final DatabaseManager databaseManager;
    private final String schemaName;
    private MessageDigest messageDigest = null;
    private final Object messageDigestSyncObject = new Object();
    /**
     * Thread local storage for userIds and responses.
     */
    private final ThreadLocal<Integer> threadLocalUserId = new ThreadLocal<>();
    private final ThreadLocal<ServerResponseFactoryInterface> threadLocalResponse = new ThreadLocal<>();
    private final ThreadLocal<Integer> threadLocalCommitId = new ThreadLocal<>();

    /**
     * Private constructor, so no one else can make a
     * SourceControlBehaviorManager object.
     */
    private SourceControlBehaviorManager() {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        try {
            this.messageDigest = MessageDigest.getInstance(QVCSConstants.QVCSOS_DIGEST_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("Failed to create message digest:", e);
        }
    }

    /**
     * Get the one and only database manager.
     *
     * @return the singleton database manager.
     */
    public static SourceControlBehaviorManager getInstance() {
        return SOURCE_CONTROL_BEHAVIOR_MANAGER;
    }

    /**
     * Associate a user and response with the current thread.
     * @param userName the user name.
     * @param response how to talk to the client.
     */
    public void setUserAndResponse(String userName, ServerResponseFactoryInterface response) {
        UserDAO userDAO = new UserDAOImpl(schemaName);
        User user = userDAO.findByUserName(userName);
        Integer currentUserId = getUserId();
        if ((currentUserId != null) && (currentUserId.intValue() != user.getId().intValue())) {
            LOGGER.warn("Found different user id for current thread: existing user id: [{}]; new user id: [{}]", currentUserId, user.getId());
            throw new RuntimeException("Found different user id for current thread");
        } else {
            setUserId(user.getId());
            setResponse(response);
        }
    }

    /**
     * Get the userId associated with the current thread.
     * @return the userId associated with the current thread.
     */
    public Integer getUserId() {
        Integer userId = threadLocalUserId.get();
        return userId;
    }

    /**
     * Set the userId associated with the current thread.
     * @param id the userId to associate with the current thread.
     */
    public void setUserId(Integer id) {
        threadLocalUserId.set(id);
    }

    /**
     * Clear the userId associated with the current thread.
     */
    public void clearUserId() {
        threadLocalUserId.remove();
    }

    public ServerResponseFactoryInterface getResponse() {
        ServerResponseFactoryInterface response = threadLocalResponse.get();
        return response;
    }

    public void setResponse(ServerResponseFactoryInterface response) {
        threadLocalResponse.set(response);
    }

    public void clearResponse() {
        threadLocalResponse.remove();
    }

    public void clearThreadLocals() {
        threadLocalResponse.remove();
        threadLocalUserId.remove();
    }

    /**
     * Create a project on the database.
     * @param projectName the name of the project.
     * @return the projectId.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer createProject(String projectName) throws SQLException {
        Integer commitId;
        Integer projectId;
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Create a commit object, and insert into database.
            Commit commit = new Commit();
            commit.setUserId(getUserId());
            String commitMessage = "Create Project: [" + projectName + "]";
            commit.setCommitMessage(commitMessage);
            Date now = new Date();
            Timestamp timestamp = new Timestamp(now.getTime());
            commit.setCommitDate(timestamp);
            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            commitId = commitDAO.insert(commit);

            // Create a project object.
            Project project = new Project();
            project.setCommitId(commitId);
            project.setDeletedFlag(Boolean.FALSE);
            project.setProjectName(projectName);
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            projectId = projectDAO.insert(project);

            // Create the root directory for the 'trunk' branch.
            Directory directory = new Directory();
            directory.setProjectId(projectId);
            DirectoryDAO directoryDAO = new DirectoryDAOImpl(schemaName);
            Integer rootDirectoryId = directoryDAO.insert(directory);

            // Create the 'trunk' branch for the project.
            Branch branch = new Branch();
            String branchName = "Trunk";
            branch.setBranchName(branchName);
            branch.setBranchTypeId(1);
            branch.setRootDirectoryId(rootDirectoryId);
            branch.setCommitId(commitId);
            branch.setProjectId(projectId);
            branch.setDeletedFlag(Boolean.FALSE);
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Integer branchId = branchDAO.insert(branch);

            // Create the directory_location for the root directory.
            DirectoryLocation directoryLocation = new DirectoryLocation();
            directoryLocation.setCommitId(commitId);
            directoryLocation.setDeletedFlag(Boolean.FALSE);
            directoryLocation.setDirectoryId(rootDirectoryId);
            directoryLocation.setDirectorySegmentName("");
            directoryLocation.setBranchId(branchId);
            directoryLocation.setParentDirectoryLocationId(null);
            DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
            Integer directoryLocationId = directoryLocationDAO.insert(directoryLocation);

            // Commit the transaction.
            connection.commit();
            LOGGER.info("Created Project: [{}] with CommitId: [{}], ProjectId: [{}], BranchId: [{}], DirectoryId: [{}], DirectoryLocationId: [{}]",
                    projectName, commitId, projectId, branchId, rootDirectoryId, directoryLocationId);
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            projectId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return projectId;
    }

    public Integer createFeatureBranch(String branchName, Integer projectId, Integer parentBranchId) throws SQLException {
        Integer commitId;
        Integer branchId;
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Create a commit object, and insert into database.
            Commit commit = new Commit();
            commit.setUserId(getUserId());
            String commitMessage = "Create Feature Branch: [" + branchName + "]";
            commit.setCommitMessage(commitMessage);
            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            commitId = commitDAO.insert(commit);

            // Fetch the parent branch...
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch parentBranch = branchDAO.findById(parentBranchId);

            // Create the branch.
            Branch branch = new Branch();
            branch.setBranchName(branchName);
            branch.setBranchTypeId(QVCSConstants.QVCS_FEATURE_BRANCH_TYPE);
            branch.setRootDirectoryId(parentBranch.getRootDirectoryId());
            branch.setCommitId(commitId);
            branch.setProjectId(projectId);
            branch.setParentBranchId(parentBranchId);
            branch.setDeletedFlag(Boolean.FALSE);
            branchId = branchDAO.insert(branch);

            // Commit the transaction.
            connection.commit();
            LOGGER.info("Created feature branch: [{}] with CommitId: [{}], ProjectId: [{}], BranchId: [{}]", branchName, commitId, projectId, branchId);
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            branchId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return branchId;
    }

    public Integer createTagBasedBranch(String branchName, Integer projectId, Integer parentBranchId, String tagText) throws SQLException {
        Integer commitId;
        Integer branchId;
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Create a commit object, and insert into database.
            Commit commit = new Commit();
            commit.setUserId(getUserId());
            String commitMessage = "Create Tag based branch: [" + branchName + "]" + " with tag: [" + tagText + "]";
            commit.setCommitMessage(commitMessage);
            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            commitId = commitDAO.insert(commit);

            // Fetch the parent branch...
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch parentBranch = branchDAO.findById(parentBranchId);

            // Fetch the tag.
            TagDAO tagDAO = new TagDAOImpl(schemaName);
            Tag tag = tagDAO.findByBranchIdAndTagText(parentBranch.getId(), tagText);

            // Create the branch.
            Branch branch = new Branch();
            branch.setBranchName(branchName);
            branch.setBranchTypeId(QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE);
            branch.setRootDirectoryId(parentBranch.getRootDirectoryId());
            branch.setCommitId(commitId);
            branch.setProjectId(projectId);
            branch.setParentBranchId(parentBranchId);
            branch.setTagId(tag.getId());
            branch.setDeletedFlag(Boolean.FALSE);
            branchId = branchDAO.insert(branch);

            // Commit the transaction.
            connection.commit();
            LOGGER.info("Created read-only tag based branch: [{}] with CommitId: [{}], ProjectId: [{}], BranchId: [{}] TagId: [{}]", branchName, commitId, projectId, branchId, tag.getId());
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            branchId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return branchId;
    }

    public Integer createReleaseBranch(String branchName, Integer projectId, Integer parentBranchId) throws SQLException {
        Integer commitId;
        Integer branchId;
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Create a commit object, and insert into database.
            Commit commit = new Commit();
            commit.setUserId(getUserId());
            String commitMessage = "Create Release Branch: [" + branchName + "]";
            commit.setCommitMessage(commitMessage);
            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            commitId = commitDAO.insert(commit);

            // Fetch the parent branch...
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch parentBranch = branchDAO.findById(parentBranchId);

            // Create the branch.
            Branch branch = new Branch();
            branch.setBranchName(branchName);
            branch.setBranchTypeId(QVCSConstants.QVCS_RELEASE_BRANCH_TYPE);
            branch.setRootDirectoryId(parentBranch.getRootDirectoryId());
            branch.setCommitId(commitId);
            branch.setProjectId(projectId);
            branch.setParentBranchId(parentBranchId);
            branch.setDeletedFlag(Boolean.FALSE);
            branchId = branchDAO.insert(branch);

            // Commit the transaction.
            connection.commit();
            LOGGER.info("Created release branch: [{}] with CommitId: [{}], ProjectId: [{}], BranchId: [{}]", branchName, commitId, projectId, branchId);
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            branchId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return branchId;
    }

    public Integer deleteBranch(Integer projectId, String branchName) throws SQLException {
        Integer returnedBranchId;
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Find the existing Branch record...
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
            String commitMessage = "Deleting branch with branchId: [" + branch.getId() + "]";

            // Create a commit object.
            Commit commit = new Commit();
            commit.setUserId(getUserId());
            commit.setCommitMessage(commitMessage);
            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            Integer commitId = commitDAO.insert(commit);

            // A simple directory delete.
            branchDAO.delete(branch.getId(), commitId);
            returnedBranchId = branch.getId();

            LOGGER.info("Deleted branch: [{}] with CommitId: [{}], directoryLocationId: [{}]", branchName, commitId, returnedBranchId);
            connection.commit();
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            // Try to rollback the transaction.
            returnedBranchId = null;
            if (connection != null) {
                connection.rollback();
            }
        }
        return returnedBranchId;
    }

    /**
     * Add a directory.
     * @param branchId the branch id.
     * @param projectId the project id.
     * @param parentDirectoryLocationId the parent directory location id.
     * @param directoryName the name of the last segment of directory name.
     * @return the directory location id for the added directory.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer addDirectory(Integer branchId, Integer projectId, Integer parentDirectoryLocationId, String directoryName) throws SQLException {
        Integer commitId;
        Integer directoryLocationId;
        Connection connection = null;
        AtomicBoolean performCommit = new AtomicBoolean(false);
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);
            commitId = getCommitId(null, "Add directory: [" + directoryName + "]", performCommit);

            // Create the directory for the project.
            Directory directory = new Directory();
            directory.setProjectId(projectId);
            DirectoryDAO directoryDAO = new DirectoryDAOImpl(schemaName);
            Integer directoryId = directoryDAO.insert(directory);

            // Create the directory_location.
            DirectoryLocation directoryLocation = new DirectoryLocation();
            directoryLocation.setCommitId(commitId);
            directoryLocation.setDeletedFlag(Boolean.FALSE);
            directoryLocation.setDirectoryId(directoryId);
            directoryLocation.setDirectorySegmentName(directoryName);
            directoryLocation.setBranchId(branchId);
            directoryLocation.setParentDirectoryLocationId(parentDirectoryLocationId);
            DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
            directoryLocationId = directoryLocationDAO.insert(directoryLocation);

            // Commit the transaction.
            if (performCommit.get()) {
                connection.commit();
            }
            LOGGER.info("Created Directory: [{}] with CommitId: [{}], ProjectId: [{}], BranchId: [{}], DirectoryId: [{}], DirectoryLocationId: [{}], ParentDirectoryLocationId: [{}]",
                    directoryName, commitId, projectId, branchId, directoryId, directoryLocationId, parentDirectoryLocationId);
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            // Try to rollback the transaction.
            directoryLocationId = null;
            if (connection != null) {
                connection.rollback();
            }
        }
        return directoryLocationId;
    }

    /**
     * Delete directory.
     * @param branchId the branchId where the request to delete comes from.
     * @param directoryLocationId the id of the directoryLocation to delete.
     * @return the directoryLocationId of the deleted directoryLocation.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer deleteDirectory(Integer branchId, Integer directoryLocationId) throws SQLException {
        Integer returnedDirectoryLocationId;
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Find the existing DirectoryLocation record...
            DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
            DirectoryLocation directoryLocation = directoryLocationDAO.findById(directoryLocationId);
            String commitMessage = "Deleting directory with directoryLocationId: [" + directoryLocationId + "]";

            // Create a commit object.
            Commit commit = new Commit();
            commit.setUserId(getUserId());
            commit.setCommitMessage(commitMessage);
            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            Integer commitId = commitDAO.insert(commit);

            if (branchId.intValue() == directoryLocation.getBranchId().intValue()) {
                // A simple directory delete.
                directoryLocationDAO.delete(directoryLocation.getId(), commitId);
                returnedDirectoryLocationId = directoryLocation.getId();

                LOGGER.info("Deleted directory with: CommitId: [{}], directoryLocationId: [{}]", commitId, returnedDirectoryLocationId);
            } else {
                // More complicated delete... add a new directoryLocation record that has the deleteFlag set to true.
                DirectoryLocation newBranchDirectoryLocation = new DirectoryLocation();
                newBranchDirectoryLocation.setBranchId(branchId);
                newBranchDirectoryLocation.setCommitId(commitId);
                newBranchDirectoryLocation.setDeletedFlag(Boolean.TRUE);
                newBranchDirectoryLocation.setDirectoryId(directoryLocation.getDirectoryId());
                newBranchDirectoryLocation.setDirectorySegmentName(directoryLocation.getDirectorySegmentName());
                newBranchDirectoryLocation.setParentDirectoryLocationId(directoryLocation.getParentDirectoryLocationId());
                newBranchDirectoryLocation.setCreatedForReason(QVCSConstants.DIRECTORY_LOCATION_RECORD_CREATED_FOR_DELETE);
                returnedDirectoryLocationId = directoryLocationDAO.insert(newBranchDirectoryLocation);
                LOGGER.info("Added branch directory location for delete directory with: CommitId: [{}], DirectoryId: [{}], new directoryLocationId: [{}]",
                        commitId, directoryLocation.getDirectoryId(), returnedDirectoryLocationId);
            }
            connection.commit();
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            // Try to rollback the transaction.
            returnedDirectoryLocationId = null;
            if (connection != null) {
                connection.rollback();
            }
        }
        return returnedDirectoryLocationId;
    }

    /**
     * Move a directory.
     * @param directoryLocationId the directory to move.
     * @param targetParentDirectoryLocationId the directory's new parent directory location.
     * @return the directoryLocationId of the moved directory. This could be different if the move 'crosses' branch boundaries.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer moveDirectory(Integer directoryLocationId, Integer targetParentDirectoryLocationId) throws SQLException {
        Integer returnedDirectoryLocationId;
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Find the existing DirectoryLocation record...
            DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
            DirectoryLocation directoryLocation = directoryLocationDAO.findById(directoryLocationId);
            String directoryName = directoryLocation.getDirectorySegmentName();

            // Find the target parent DirectoryLocation record...
            DirectoryLocation targetParentDirectoryLocation = directoryLocationDAO.findById(targetParentDirectoryLocationId);
            String newParentDirectoryName = targetParentDirectoryLocation.getDirectorySegmentName();
            String commitMessage = "Moving directory [" + directoryName + "] to be a child of [" + newParentDirectoryName + "] on branch [" + targetParentDirectoryLocation.getBranchId() + "]";

            // Create a commit object.
            Commit commit = new Commit();
            commit.setUserId(getUserId());
            commit.setCommitMessage(commitMessage);
            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            Integer commitId = commitDAO.insert(commit);

            if (targetParentDirectoryLocation.getBranchId().intValue() == directoryLocation.getBranchId().intValue()) {
                // A simple directory move.
                directoryLocationDAO.move(directoryLocation.getId(), commitId, targetParentDirectoryLocationId);
                returnedDirectoryLocationId = directoryLocation.getId();

                LOGGER.info("Moved directory with: CommitId: [{}], directoryLocationId: [{}]", commitId, returnedDirectoryLocationId);
            } else {
                // More complicated move... add a new directoryLocation record.
                DirectoryLocation newBranchDirectoryLocation = new DirectoryLocation();
                newBranchDirectoryLocation.setBranchId(targetParentDirectoryLocation.getBranchId());
                newBranchDirectoryLocation.setCommitId(commitId);
                newBranchDirectoryLocation.setDeletedFlag(Boolean.FALSE);
                newBranchDirectoryLocation.setDirectoryId(directoryLocation.getDirectoryId());
                newBranchDirectoryLocation.setDirectorySegmentName(directoryLocation.getDirectorySegmentName());
                newBranchDirectoryLocation.setParentDirectoryLocationId(directoryLocation.getParentDirectoryLocationId());
                newBranchDirectoryLocation.setCreatedForReason(QVCSConstants.DIRECTORY_LOCATION_RECORD_CREATED_FOR_MOVE);
                returnedDirectoryLocationId = directoryLocationDAO.insert(newBranchDirectoryLocation);
                LOGGER.info("Added branch directory location for move directory with: CommitId: [{}], DirectoryId: [{}], new directoryLocationId: [{}]",
                        commitId, directoryLocation.getDirectoryId(), returnedDirectoryLocationId);
            }
            connection.commit();
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            // Try to rollback the transaction.
            returnedDirectoryLocationId = null;
            if (connection != null) {
                connection.rollback();
            }
        }
        return returnedDirectoryLocationId;
    }

    /**
     * Rename a directory.
     * @param branchId the branchId where the request to rename comes from.
     * @param directoryLocationId the id of the directoryLocation to rename.
     * @param newDirectoryName the new directory name.
     * @return the directoryLocationId of the renamed directoryLocation.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer renameDirectory(Integer branchId, Integer directoryLocationId, String newDirectoryName) throws SQLException {
        Integer returnedDirectoryLocationId;
        Connection connection = null;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Find the existing DirectoryLocation record...
            DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
            DirectoryLocation directoryLocation = directoryLocationDAO.findById(directoryLocationId);
            String oldDirectoryName = directoryLocation.getDirectorySegmentName();
            String commitMessage = "Renaming directory from [" + oldDirectoryName + "] to [" + newDirectoryName + "] on branch [" + branchId + "]";

            // Create a commit object.
            Commit commit = new Commit();
            commit.setUserId(getUserId());
            commit.setCommitMessage(commitMessage);
            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            Integer commitId = commitDAO.insert(commit);

            if (branchId.intValue() == directoryLocation.getBranchId().intValue()) {
                // A simple directory rename.
                directoryLocationDAO.rename(directoryLocation.getId(), commitId, newDirectoryName);
                returnedDirectoryLocationId = directoryLocation.getId();

                LOGGER.info("Renamed directory to: [{}] with CommitId: [{}], DirectoryId: [{}], directoryLocationId: [{}]",
                        newDirectoryName, commitId, directoryLocation.getDirectoryId(), returnedDirectoryLocationId);
            } else {
                // More complicated rename... add a new directoryLocation record.
                DirectoryLocation newBranchDirectoryLocation = new DirectoryLocation();
                newBranchDirectoryLocation.setBranchId(branchId);
                newBranchDirectoryLocation.setCommitId(commitId);
                newBranchDirectoryLocation.setDeletedFlag(Boolean.FALSE);
                newBranchDirectoryLocation.setDirectoryId(directoryLocation.getDirectoryId());
                newBranchDirectoryLocation.setDirectorySegmentName(newDirectoryName);
                newBranchDirectoryLocation.setParentDirectoryLocationId(directoryLocation.getParentDirectoryLocationId());
                newBranchDirectoryLocation.setCreatedForReason(QVCSConstants.DIRECTORY_LOCATION_RECORD_CREATED_FOR_RENAME);
                returnedDirectoryLocationId = directoryLocationDAO.insert(newBranchDirectoryLocation);
                LOGGER.info("Added branch directory location: [{}] for rename directory with CommitId: [{}], DirectoryId: [{}], new directoryLocationId: [{}]",
                        newDirectoryName, commitId, directoryLocation.getDirectoryId(), returnedDirectoryLocationId);
            }
            connection.commit();
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            // Try to rollback the transaction.
            returnedDirectoryLocationId = null;
            if (connection != null) {
                connection.rollback();
            }
        }
        return returnedDirectoryLocationId;
    }

    /**
     * Add a file.
     * @param branchId the branch id.
     * @param projectId the project id.
     * @param directoryId the directory id.
     * @param filename the name of the file (not a full name).
     * @param ioFile the java.io.File (may be a temp file) containing the data of the file to be added.
     * @param createdForReason Usually null, but non-null for branch files that have corresponding 'parent' file on the Trunk; identifies the reason for the add.
     * @param commitId optional commitId if there is already a transaction in progress.
     * @param workfileEditDate the workfile edit date.
     * @param commitMessage the optional commit message.
     * @param mutableFileRevisionId the revisionId of the first FileRevision of the added file.
     * @return the fileId for the added file; the mutableRevisionId value is set to the revisionId of the first FileRevision of the added file.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer addFile(Integer branchId, Integer projectId, Integer directoryId, String filename, java.io.File ioFile, Integer createdForReason,
            Integer commitId, Date workfileEditDate, String commitMessage, AtomicInteger mutableFileRevisionId) throws SQLException {
        Connection connection = null;
        Integer fileId;
        Integer fileNameId;
        AtomicBoolean performCommit = new AtomicBoolean(false);
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);
            if (commitMessage == null || commitMessage.isEmpty()) {
                commitMessage = "Add file";
            }
            commitId = getCommitId(commitId, commitMessage, performCommit);

            // See if there are already any FileName rows for this file on any other branch.
            FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
            List<FileName> fileNameList = fileNameDAO.findByDirectoryIdAndFileName(directoryId, filename);
            if (fileNameList.isEmpty()) {
                // Create a file object, and insert into the database.
                File file = new File();
                file.setProjectId(projectId);
                FileDAO fileDAO = new FileDAOImpl(schemaName);
                fileId = fileDAO.insert(file);

                // Create a fileName object, and insert into the database.
                FileName fileName = new FileName();
                fileName.setBranchId(branchId);
                fileName.setDirectoryId(directoryId);
                fileName.setFileId(fileId);
                fileName.setFileName(filename);
                fileName.setCommitId(commitId);
                fileName.setDeletedFlag(Boolean.FALSE);
                fileNameId = fileNameDAO.insert(fileName);
            } else {
                // Make sure the fileName doesn't already exist (maybe was deleted earlier?).
                boolean earlierRecordFoundFlag = false;
                FileName foundFileName = null;
                for (FileName fileName : fileNameList) {
                    if (fileName.getBranchId().intValue() == branchId.intValue()) {
                        // We created a fileName record for this branch at some time in the past... need to re-use the record.
                        earlierRecordFoundFlag = true;
                        foundFileName = fileName;
                        break;
                    }
                }
                if (earlierRecordFoundFlag) {
                    // Need to re-use the existing fileName record.
                    fileId = foundFileName.getFileId();
                    fileNameId = fileNameDAO.unDeleteFileName(foundFileName.getId(), commitId);
                } else {
                    // Create a fileName object, and insert into the database.
                    fileId = fileNameList.get(0).getFileId();
                    FileName fileName = new FileName();
                    fileName.setBranchId(branchId);
                    fileName.setDirectoryId(directoryId);
                    fileName.setFileId(fileId);
                    fileName.setFileName(filename);
                    fileName.setCommitId(commitId);
                    fileName.setDeletedFlag(Boolean.FALSE);
                    fileNameId = fileNameDAO.insert(fileName);
                }
            }

            // Create the fileRevision object, and insert into the database.
            Timestamp workfileEditTimestamp = new Timestamp(workfileEditDate.getTime());
            Integer fileRevisionId = addRevision(branchId, fileId, ioFile, commitId, workfileEditTimestamp, commitMessage);
            mutableFileRevisionId.set(fileRevisionId);
            LOGGER.info("Added file: [{}] with CommitId: [{}], FileId: [{}], FileNameId: [{}], Filename: [{}], FileRevisionId: [{}]",
                    filename, commitId, fileId, fileNameId, filename, fileRevisionId);

            if (performCommit.get()) {
                connection.commit();
            }
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            fileId = null;
            mutableFileRevisionId.set(-1);
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return fileId;
    }
    /**
     * Add a file.
     * @param branchName the branch name.
     * @param projectName the project name.
     * @param appendedPath the appended path where the new file goes.
     * @param filename the short name of the file (not a full name).
     * @param ioFile the java.io.File (may be a temp file) containing the data of the file to be added.
     * @param workfileEditDate the workfile edit date.
     * @param commitMessage the optional commit message.
     * @param mutableFileRevisionId the revisionId of the first FileRevision of the added file.
     * @return the fileId for the added file; the mutableRevisionId value is set to the revisionId of the first FileRevision of the added file.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer addFile(String branchName, String projectName, String appendedPath, String filename, java.io.File ioFile,
            Date workfileEditDate, String commitMessage, AtomicInteger mutableFileRevisionId) throws SQLException {
        Connection connection = null;
        Integer fileId;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Lookup the projectId.
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(projectName);
            Integer projectId = project.getId();

            // Lookup the branchId.
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
            Integer branchId = branch.getId();
            Integer rootDirectoryId = branch.getRootDirectoryId();

            DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
            DirectoryLocation parentDirectoryLocation = directoryLocationDAO.findByDirectoryId(rootDirectoryId);

            // Figure out the directoryId.
            DirectoryLocation directoryLocation = parentDirectoryLocation;
            if (appendedPath.length() > 0) {
                String[] directorySegments = appendedPath.split(java.io.File.separator);
                for (String segment : directorySegments) {
                    directoryLocation = findChildDirectoryLocation(branchId, parentDirectoryLocation.getId(), segment);
                    parentDirectoryLocation = directoryLocation;
                }
            }
            Integer directoryId = directoryLocation.getDirectoryId();
            Timestamp workfileEditTimestamp = new Timestamp(workfileEditDate.getTime());
            fileId = addFile(branchId, projectId, directoryId, filename, ioFile, null, null, workfileEditTimestamp, commitMessage, mutableFileRevisionId);
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            fileId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return fileId;
    }

    public Integer getDirectoryId(String projectName, String branchName, String appendedPath) throws SQLException {
        Connection connection = null;
        Integer directoryId;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Lookup the projectId.
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(projectName);
            Integer projectId = project.getId();

            // Lookup the branchId.
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
            Integer branchId = branch.getId();
            Integer rootDirectoryId = branch.getRootDirectoryId();

            DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
            DirectoryLocation parentDirectoryLocation = directoryLocationDAO.findByDirectoryId(rootDirectoryId);

            // Figure out the directoryId.
            DirectoryLocation directoryLocation = parentDirectoryLocation;
            if (appendedPath.length() > 0) {
                String[] directorySegments = appendedPath.split(java.io.File.separator);
                for (String segment : directorySegments) {
                    directoryLocation = findChildDirectoryLocation(branchId, parentDirectoryLocation.getId(), segment);
                    parentDirectoryLocation = directoryLocation;
                }
            }
            directoryId = directoryLocation.getDirectoryId();

        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            directoryId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return directoryId;
    }

    /**
     * Add a file revision.
     * @param branchId the branch id.
     * @param fileId the file id.
     * @param file the file we use to create the revision.
     * @param commitId optional commitId if there is already a transaction in progress.
     * @param workfileEditDate the workfile edit date.
     * @param commitMessage the optional commit message.
     * @return the fileRevisionId for the new revision.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer addRevision(Integer branchId, Integer fileId, java.io.File file, Integer commitId, Date workfileEditDate, String commitMessage) throws SQLException {
        Integer fileRevisionId;
        try {
            byte[] fileData = getFileData(file);
            fileRevisionId = addRevision(branchId, fileId, fileData, commitId, workfileEditDate, commitMessage);
        } catch (IOException e) {
            LOGGER.warn("Exception: ", e);
            fileRevisionId = null;
        }
        return fileRevisionId;
    }

    public Integer addRevision(Integer branchId, Integer fileId, byte[] fileData, Integer commitId, Date workfileEditDate, String commitMessage) throws SQLException {
        Connection connection = null;
        Integer fileRevisionId;
        AtomicBoolean performCommit = new AtomicBoolean(false);
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);
            commitId = getCommitId(commitId, commitMessage, performCommit);

            FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
            FileRevision ancestorRevision = fileRevisionDAO.findNewestRevisionAllBranches(fileId);
            Timestamp workfileEditTimeStamp = new Timestamp(workfileEditDate.getTime());

            // Create the fileRevision object, and insert into the database.
            FileRevision fileRevision = new FileRevision();
            fileRevision.setBranchId(branchId);
            fileRevision.setCommitId(commitId);
            fileRevision.setWorkfileEditDate(workfileEditTimeStamp);
            fileRevision.setFileId(fileId);
            fileRevision.setRevisionDigest(computeFileDigest(fileData));
            fileRevision.setRevisionData(fileData);
            if (ancestorRevision != null) {
                fileRevision.setAncestorRevisionId(ancestorRevision.getId());
            }
            fileRevisionId = fileRevisionDAO.insert(fileRevision);
            if (ancestorRevision != null) {
                if (ancestorRevision.getReverseDeltaRevisionId() == null) {
                    byte[] reverseDeltaScript = computeReverseDelta(ancestorRevision.getRevisionData(), fileData);
                    if (reverseDeltaScript != null) {
                        fileRevisionDAO.updateAncestorRevision(ancestorRevision.getId(), fileRevisionId, reverseDeltaScript);
                        LOGGER.debug("-----> Updated ancestor revision: CommitId: [{}], FileId: [{}], AncestorRevisionId: [{}]", commitId, fileId, ancestorRevision.getId());
                    } else {
                        LOGGER.warn("Failed to compute delta for fileId: [{}]", fileId);
                    }
                } else {
                    LOGGER.warn("Non-null reverse delta rev id for file id: [{}]", ancestorRevision.getFileId());
                    throw new QVCSRuntimeException("Non-null reverse delta rev id for file id: " + ancestorRevision.getFileId());
                }
            }
            LOGGER.debug("Added file revision with: CommitId: [{}], FileId: [{}], FileRevisionId: [{}]", commitId, fileId, fileRevisionId);

            if (performCommit.get()) {
                connection.commit();
            }
        } catch (SQLException | IOException e) {
            LOGGER.warn("Exception: ", e);
            fileRevisionId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return fileRevisionId;
    }

    /**
     * Get the requested file revision from the database and return it in a temp file.
     * @param fileRevisionId the revisionId of the revision to get.
     * @return the requested file revision, or null if the revision does not exist.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public java.io.File getFileRevision(Integer fileRevisionId) throws SQLException {
        java.io.File fetchedRevisionFile = null;
        try {
            FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
            FileRevision fileRevision = fileRevisionDAO.findById(fileRevisionId);
            if (fileRevision != null) {
                if (fileRevision.getReverseDeltaRevisionId() != null) {
                    // Get the file we need in order to hydrate the requested revision...
                    java.io.File tempFile = getFileRevision(fileRevision.getReverseDeltaRevisionId());
                    fetchedRevisionFile = hydrateRevision(tempFile, fileRevision.getRevisionData());
                } else {
                    fetchedRevisionFile = createTempFileFromBuffer(fileRevision.getRevisionData());
                }
            }
        } catch (SQLException | IOException e) {
            LOGGER.warn("Exception: ", e);
            fetchedRevisionFile = null;
        }
        return fetchedRevisionFile;
    }

    /**
     * Delete a file.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param appendedPath the appended path where the new file goes.
     * @param shortFilename the short name of the file (not a full name).
     * @return the fileNameId of the deleted file.
     * @throws java.sql.SQLException
     */
    public Integer deleteFile(String projectName, String branchName, String appendedPath, String shortFilename) throws SQLException {
        Connection connection = null;
        Integer fileNameId;
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Lookup the projectId.
            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(projectName);
            Integer projectId = project.getId();

            // Lookup the branchId.
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
            Integer branchId = branch.getId();
            Integer rootDirectoryId = branch.getRootDirectoryId();
            DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
            DirectoryLocation parentDirectoryLocation = directoryLocationDAO.findByDirectoryId(rootDirectoryId);
            // Figure out the directoryId.
            DirectoryLocation directoryLocation = parentDirectoryLocation;
            if (appendedPath.length() > 0) {
                String[] directorySegments = appendedPath.split(java.io.File.separator);
                for (String segment : directorySegments) {
                    directoryLocation = findChildDirectoryLocation(branchId, parentDirectoryLocation.getId(), segment);
                    parentDirectoryLocation = directoryLocation;
                }
            }
            Integer directoryId = directoryLocation.getDirectoryId();
            FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
            List<FileName> fileNameList = fileNameDAO.findByDirectoryIdAndFileName(directoryId, shortFilename);

            Map<Integer, FileName> fileNameMap = new TreeMap<>();
            fileNameList.forEach(fileName -> {
                fileNameMap.put(fileName.getBranchId(), fileName);
            });
            fileNameId = null;
            // Figure out the fileNameId...
            if (fileNameMap.get(branchId) != null) {
                fileNameId = fileNameMap.get(branchId).getId();
            } else {
                FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
                List<Branch> branchList = functionalQueriesDAO.getBranchAncestryList(branchId);
                for (Branch b : branchList) {
                    if (fileNameMap.get(b.getId()) != null) {
                        fileNameId = fileNameMap.get(b.getId()).getId();
                        break;
                    }
                }
            }
            fileNameId = deleteFile(branchId, fileNameId);
        } catch (SQLException e) {
            LOGGER.warn("SQL exception: ", e);
            fileNameId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return fileNameId;
    }

    /**
     * Delete a file.
     * There is some business logic here. If we're deleting on the Trunk, things are simple. If we're deleting on a branch,
     * things become more complex.
     * <p>For the case where we have a feature branch, and the delete of a file that has not yet
     * been edited on the branch, then we need to create a new FileName row.
     * <p>For the case where the file already has a File record on the branch, things are as simple as they are for the Trunk.</p>
     * @param branchId the id of the branch where the delete is done.
     * @param fileNameId the id of the FileName record.
     * @return the fileNameId for the deleted file... i.e. the primary key in the file_name table.
     * @throws SQLException if we cannot rollback the transaction.
     */
    Integer deleteFile(Integer branchId, Integer fileNameId) throws SQLException {
        Integer returnedFileNameId;
        Connection connection = null;
        AtomicBoolean performCommit = new AtomicBoolean(false);
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Find the existing FileName record...
            FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
            FileName fileName = fileNameDAO.findById(fileNameId);
            String filename = fileName.getFileName();

            String commitMessage = "Deleting file [" + filename + "]";
            Integer commitId = getCommitId(null, commitMessage, performCommit);

            if (branchId.intValue() == fileName.getBranchId().intValue()) {
                // A simple delete.
                fileNameDAO.delete(fileName.getId(), commitId);
                returnedFileNameId = fileName.getId();

                LOGGER.info("Deleted file with: CommitId: [{}], FileId: [{}], FileNameId: [{}]", commitId, fileName.getFileId(), returnedFileNameId);
            } else {
                // More complicated delete... it's really just an add fileName to the branch.
                FileName copiedFileName = new FileName();
                copiedFileName.setBranchId(branchId);
                copiedFileName.setDirectoryId(fileName.getDirectoryId());
                copiedFileName.setFileId(fileName.getFileId());
                copiedFileName.setCreatedForReason(QVCSConstants.FILE_NAME_RECORD_CREATED_FOR_DELETE);
                copiedFileName.setCommitId(commitId);
                copiedFileName.setFileName(fileName.getFileName());
                copiedFileName.setDeletedFlag(Boolean.TRUE);
                returnedFileNameId = fileNameDAO.insert(copiedFileName);
                LOGGER.info("Added file name for delete file with: CommitId: [{}], FileId: [{}], FileNameId: [{}]",
                        commitId, copiedFileName.getFileId(), returnedFileNameId);
            }

            // Create a revision on the branch to make it easy to see things that happened (makes figuring out promotion list a lot easier).
            FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
            FileRevision newestRevision = fileRevisionDAO.findNewestRevisionAllBranches(fileName.getFileId());
            Integer fileRevisionId = addRevision(branchId, fileName.getFileId(), newestRevision.getRevisionData(), commitId, newestRevision.getWorkfileEditDate(), commitMessage);
            LOGGER.info("Added file revision id: [{}] for deleted file on branch id: [{}]", fileRevisionId, branchId);

            if (performCommit.get()) {
                connection.commit();
            }
        } catch (SQLException e) {
            LOGGER.warn("Exception: ", e);
            returnedFileNameId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return returnedFileNameId;
    }

    /**
     * Move a file.There is some business logic here.If we're moving on the Trunk, things are simple.
     * If we're moving on a branch, things become more complex.
     * <p>For the case where we have a feature branch, and the move of a file that has not yet
     * been edited on the branch, then we need to create new FileName row.
     * <p>For the case where the file already has a FileName record on the branch, things are as simple as they are for the Trunk.</p>
     * @param branchId the id of the branch where the move is to be done.
     * @param fileNameId the id of the FileName record to be moved.
     * @param destinationDirectoryId the id of the directory to which the file is to be moved.
     * @return the fileNameId for the moved file... i.e. the primary key in the file_name table.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer moveFile(Integer branchId, Integer fileNameId, Integer destinationDirectoryId) throws SQLException {
        Integer returnedFileNameId = null;
        Connection connection = null;
        java.io.File fileRevisionFile = null;
        AtomicBoolean performCommit = new AtomicBoolean(false);
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Need to verify that the destination directory exists.
            DirectoryDAO directoryDAO = new DirectoryDAOImpl(schemaName);
            Directory destinationDirectory = directoryDAO.findById(destinationDirectoryId);
            if (destinationDirectory == null) {
                throw new RuntimeException("Request to move file to non-existant directory with id: " + destinationDirectoryId);
            }

            // Find the existing FileName record...
            FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
            FileName fileName = fileNameDAO.findById(fileNameId);

            Directory originDirectory = directoryDAO.findById(fileName.getDirectoryId());
            String commitMessage = "Moving file from directoryId: [" + originDirectory.getId() + "] to directoryId: [" + destinationDirectory.getId() + "] on branchId: [" + branchId + "]";
            Integer commitId = getCommitId(null, commitMessage, performCommit);

            // Find the existing File record...
            FileDAO fileDAO = new FileDAOImpl(schemaName);
            File file = fileDAO.findById(fileName.getFileId());

            if (branchId.intValue() == fileName.getBranchId().intValue()) {
                // A simple move.
                fileNameDAO.move(fileName.getId(), commitId, destinationDirectoryId);
                returnedFileNameId = fileName.getId();

                LOGGER.info("Moved file with: CommitId: [{}], FileId: [{}], FileNameId: [{}], DestinationDirectoryId: [{}]",
                        commitId, file.getId(), returnedFileNameId, destinationDirectoryId);
            } else {
                // More complicated move... it's really just an add fileName to the branch.
                FileName copiedFileName = new FileName();
                copiedFileName.setBranchId(branchId);
                copiedFileName.setDirectoryId(destinationDirectory.getId());
                copiedFileName.setFileId(fileName.getFileId());
                copiedFileName.setCreatedForReason(QVCSConstants.FILE_NAME_RECORD_CREATED_FOR_MOVE);
                copiedFileName.setCommitId(commitId);
                copiedFileName.setFileName(fileName.getFileName());
                copiedFileName.setDeletedFlag(Boolean.FALSE);
                returnedFileNameId = fileNameDAO.insert(copiedFileName);
                LOGGER.info("Added file name for move file with: CommitId: [{}], FileId: [{}], FileNameId: [{}]",
                        commitId, copiedFileName.getFileId(), returnedFileNameId);
            }

            // Create a revision on the branch to make it easy to see things that happened (makes figuring out promotion list a lot easier).
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            List<FileRevision> revisionList = functionalQueriesDAO.findFileRevisionsInBranches(functionalQueriesDAO.getBranchAncestryList(branchId), fileName.getFileId());
            fileRevisionFile = getFileRevision(revisionList.get(0).getId());
            byte[] revisionData = getFileData(fileRevisionFile);
            Integer fileRevisionId = addRevision(branchId, fileName.getFileId(), revisionData, commitId, revisionList.get(0).getWorkfileEditDate(), commitMessage);
            LOGGER.info("Added file revision id: [{}] for moved file on branch id: [{}]", fileRevisionId, branchId);

            if (performCommit.get()) {
                connection.commit();
            }
        } catch (IOException | SQLException e) {
            LOGGER.warn("Exception: ", e);
            returnedFileNameId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        } finally {
            if (fileRevisionFile != null) {
                fileRevisionFile.delete();
            }
        }
        return returnedFileNameId;
    }

    /**
     * Rename a file.There is some business logic here.If we're renaming on the
     * same branch, things are simple. If we're renaming on a different branch,
     * things become more complex.
     * <p>
     * For the case where we have a feature branch, and the rename of a file
     * that has not yet been edited on the branch, then we need to create new
     * File, FileName rows, and we set the parent_file_id on the FileName object
     * to point to the Trunk File object.</p>
     * <p>
     * For the case where the file already has a File record on the branch,
     * things are as simple as they are for the Trunk.</p>
     *
     * @param branchId the id of the branch where the rename is done.
     * @param fileId the id of the File record.
     * @param newFileName the new name of the file.
     * @return the fileNameId for the renamed file... i.e. the primary key in the file_name table.
     * @throws SQLException if we cannot rollback the transaction.
     */
    public Integer renameFile(Integer branchId, Integer fileId, String newFileName) throws SQLException {
        Integer returnedFileNameId;
        Connection connection = null;
        java.io.File fileRevisionFile = null;
        AtomicBoolean performCommit = new AtomicBoolean(false);
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Find the existing FileName record...
            FileName fileName = null;
            FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
            List<FileName> fileNameList = fileNameDAO.findByFileId(fileId);
            for (FileName oldFileName : fileNameList) {
                if (oldFileName.getBranchId().intValue() == branchId.intValue()) {
                    fileName = oldFileName;
                    break;
                }
            }
            if (fileName == null) {
                fileName = fileNameList.get(0);
            }
            String oldFileName = fileName.getFileName();
            String commitMessage = "Renaming file from [" + oldFileName + "] to [" + newFileName + "]";
            Integer commitId = getCommitId(null, commitMessage, performCommit);

            if (branchId.intValue() == fileName.getBranchId().intValue()) {

                // A simple rename.
                fileNameDAO.rename(fileName.getId(), commitId, newFileName);
                returnedFileNameId = fileName.getId();
                LOGGER.info("Renamed file with: CommitId: [{}], FileId: [{}], FileNameId: [{}]", commitId, fileId, returnedFileNameId);
            } else {
                // More complicated rename... it's really just an add fileName to the branch.
                FileName copiedFileName = new FileName();
                copiedFileName.setBranchId(branchId);
                copiedFileName.setDirectoryId(fileName.getDirectoryId());
                copiedFileName.setFileId(fileName.getFileId());
                copiedFileName.setCreatedForReason(QVCSConstants.FILE_NAME_RECORD_CREATED_FOR_RENAME);
                copiedFileName.setCommitId(commitId);
                copiedFileName.setFileName(newFileName);
                copiedFileName.setDeletedFlag(Boolean.FALSE);
                returnedFileNameId = fileNameDAO.insert(copiedFileName);
                LOGGER.info("Added file name for rename on branch id: [{}] for file with: CommitId: [{}], FileId: [{}], FileNameId: [{}] new file name: [{}]",
                        branchId, commitId, copiedFileName.getFileId(), returnedFileNameId, newFileName);
            }

            // Create a revision on the branch to make it easy to see things that happened (makes figuring out promotion list a lot easier).
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            List<FileRevision> revisionList = functionalQueriesDAO.findFileRevisionsInBranches(functionalQueriesDAO.getBranchAncestryList(branchId), fileName.getFileId());
            fileRevisionFile = getFileRevision(revisionList.get(0).getId());
            byte[] revisionData = getFileData(fileRevisionFile);
            Integer fileRevisionId = addRevision(branchId, fileName.getFileId(), revisionData, commitId, revisionList.get(0).getWorkfileEditDate(), commitMessage);
            LOGGER.info("Added file revision id: [{}] for renamed file on branch id: [{}]", fileRevisionId, branchId);

            if (performCommit.get()) {
                connection.commit();
            }
        } catch (IOException | SQLException e) {
            LOGGER.warn("Exception: ", e);
            returnedFileNameId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        } finally {
            if (fileRevisionFile != null) {
                fileRevisionFile.delete();
            }
        }
        return returnedFileNameId;
    }

    /**
     * Move and rename a file.
     *
     * @param branchId the id of the branch where the move is to be done.
     * @param fileNameId the file_name id of the FileName record to be moved.
     * @param destinationDirectoryId the id the directory to which the file is
     * to be moved.
     * @param newFileName the new name of the file.
     * @return the fileNameId for the renamed file... i.e. the primary key of
     * the row in the file_name table.
     * @throws SQLException if we cannot rollback the transaction (when there is
     * a problem).
     */
    public Integer moveAndRenameFile(Integer branchId, Integer fileNameId, Integer destinationDirectoryId, String newFileName) throws SQLException {
        Integer returnedFileNameId = null;
        Connection connection = null;
        AtomicBoolean performCommit = new AtomicBoolean(false);
        try {
            connection = databaseManager.getConnection();
            connection.setAutoCommit(false);

            // Need to verify that the destination directory exists.
            DirectoryDAO directoryDAO = new DirectoryDAOImpl(schemaName);
            Directory destinationDirectory = directoryDAO.findById(destinationDirectoryId);
            if (destinationDirectory == null) {
                throw new RuntimeException("Request to move file to non-existant directory with id: " + destinationDirectoryId);
            }

            // Find the existing FileName record...
            FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
            FileName fileName = fileNameDAO.findById(fileNameId);

            Directory originDirectory = directoryDAO.findById(fileName.getDirectoryId());
            String commitMessage = String.format("Moving and renaming file; origin directoryId: [%d]; destination directoryId: [%d]; old name: [%s] new name: [%s]; on branchId: [%d]",
                    originDirectory.getId(), destinationDirectory.getId(), fileName.getFileName(), newFileName, branchId);
            Integer commitId = getCommitId(null, commitMessage, performCommit);

            // Find the existing File record...
            FileDAO fileDAO = new FileDAOImpl(schemaName);
            File file = fileDAO.findById(fileName.getFileId());

            if (branchId.intValue() == fileName.getBranchId().intValue()) {
                // A simple move and rename.
                fileNameDAO.moveAndRename(fileName.getId(), commitId, destinationDirectoryId, newFileName);
                returnedFileNameId = fileName.getId();

                LOGGER.info("Moved and renamed file with: CommitId: [{}], FileId: [{}], FileNameId: [{}], DestinationDirectoryId: [{}]",
                        commitId, file.getId(), returnedFileNameId, destinationDirectoryId);
            } else {
                // More complicated move and rename... it's really just an add fileName to the branch.
                FileName copiedFileName = new FileName();
                copiedFileName.setBranchId(branchId);
                copiedFileName.setDirectoryId(destinationDirectory.getId());
                copiedFileName.setFileId(fileName.getFileId());
                copiedFileName.setCreatedForReason(QVCSConstants.FILE_NAME_RECORD_CREATED_FOR_MOVE_AND_RENAME);
                copiedFileName.setCommitId(commitId);
                copiedFileName.setFileName(newFileName);
                copiedFileName.setDeletedFlag(Boolean.FALSE);
                returnedFileNameId = fileNameDAO.insert(copiedFileName);
                LOGGER.info("Added file name for move file with: CommitId: [{}], FileId: [{}], FileNameId: [{}]",
                        commitId, copiedFileName.getFileId(), returnedFileNameId);
            }

            // Create a revision on the branch to make it easy to see things that happened (makes figuring out promotion list a lot easier).
            FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
            FileRevision newestRevision = fileRevisionDAO.findNewestRevisionAllBranches(fileName.getFileId());
            Integer fileRevisionId = addRevision(branchId, fileName.getFileId(), newestRevision.getRevisionData(), commitId, newestRevision.getWorkfileEditDate(), commitMessage);
            LOGGER.info("Added file revision id: [{}] for moved and renamed file on branch id: [{}]", fileRevisionId, branchId);

            if (performCommit.get()) {
                connection.commit();
            }
        } catch (SQLException e) {
            LOGGER.warn("Exception: ", e);
            returnedFileNameId = null;
            // Try to rollback the transaction.
            if (connection != null) {
                connection.rollback();
            }
        }
        return returnedFileNameId;
    }

    private java.io.File createTempFileFromBuffer(byte[] revisionData) throws IOException {
        java.io.File tempFile = java.io.File.createTempFile("qvcsos-", ".tmp");
        Files.write(tempFile.toPath(), revisionData);
        return tempFile;
    }

    private byte[] computeFileDigest(byte[] fileData) {
        byte[] digest;

        synchronized (messageDigestSyncObject) {
            messageDigest.reset();

            LOGGER.trace("computing digest on buffer of size: [{}] ", fileData.length);
            digest = messageDigest.digest(fileData);
        }

        return digest;
    }

    private byte[] getFileData(java.io.File file) throws FileNotFoundException, IOException {
        byte[] buffer;
        try (FileInputStream inStream = new FileInputStream(file)) {
            buffer = new byte[(int) file.length()];
            Utility.readDataFromStream(buffer, inStream);
        }
        return buffer;
    }

    private byte[] computeReverseDelta(byte[] revisionData, byte[] newRevisionData) throws IOException {
        byte[] result = null;
        try {
            java.io.File oldFile = createTempFileFromBuffer(revisionData);
            java.io.File newRevisionFile = createTempFileFromBuffer(newRevisionData);
            java.io.File outFile = java.io.File.createTempFile("qvcsos-compare-out", ".tmp");
            String[] args = {newRevisionFile.getCanonicalPath(), oldFile.getCanonicalPath(), outFile.getCanonicalPath()};
            CompareFilesWithApacheDiff compareFileWithApacheDiff = new CompareFilesWithApacheDiff(args);
            if (compareFileWithApacheDiff.execute()) {
                result = getFileData(outFile);
            }
            oldFile.delete();
            outFile.delete();
        } catch (QVCSOperationException e) {
            LOGGER.warn("Computing reverse delta failed", e);
        }
        return result;
    }

    /**
     * Hydrate a file given a file to which we apply an edit script.
     * @param fileToEdit the file that will be changed by the edit script.
     * @param edits the edit script.
     * @return a new temporary file that is the result of applying the edits to the original file.
     * @throws IOException if the fileToEdit cannot be opened or read.
     */
    private java.io.File hydrateRevision(java.io.File fileToEdit, byte[] edits) throws IOException {
        DataInputStream editStream = new DataInputStream(new ByteArrayInputStream(edits));
        CompareFilesEditInformation editInfo = new CompareFilesEditInformation();
        byte[] originalData = getFileData(fileToEdit);
        byte[] editedBuffer = new byte[edits.length + originalData.length]; // It can't be any bigger than this.
        java.io.File returnedFile = null;
        int inIndex = 0;
        int outIndex = 0;
        int deletedBytesCount;
        int insertedBytesCount;
        int bytesTillChange = 0;

        try {
            // We need to first skip 8 bytes from the stream because there are 8 bytes there that we should ignore (for now).
            byte[] eightBytes = new byte[CompareFilesEditHeader.getEditHeaderSize()];
            editStream.read(eightBytes);
            while (editStream.available() > 0) {
                editInfo.read(editStream);
                bytesTillChange = (int) editInfo.getSeekPosition() - inIndex;
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, bytesTillChange);

                inIndex += bytesTillChange;
                outIndex += bytesTillChange;

                deletedBytesCount = (int) editInfo.getDeletedBytesCount();
                insertedBytesCount = (int) editInfo.getInsertedBytesCount();

                switch (editInfo.getEditType()) {
                    case CompareFilesEditInformation.QVCS_EDIT_DELETE:
                        /*
                         * Delete input
                         */
                        // Just skip over deleted bytes
                        inIndex += deletedBytesCount;
                        break;

                    case CompareFilesEditInformation.QVCS_EDIT_INSERT:
                        /*
                         * Insert edit lines
                         */
                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    case CompareFilesEditInformation.QVCS_EDIT_REPLACE:
                        /*
                         * Replace input line with edit line.
                         * First skip over the bytes to be replaced, then copy the replacing bytes from the edit file to the output
                         * file.
                         */
                        inIndex += deletedBytesCount;

                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    default:
                        break;
                }
            }

            // Copy the rest of the input "file" to the output "file".
            int remainingBytes = originalData.length - inIndex;
            if (remainingBytes > 0) {
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, remainingBytes);
                outIndex += remainingBytes;
            }
            byte[] returnedBuffer = new byte[outIndex];
            System.arraycopy(editedBuffer, 0, returnedBuffer, 0, outIndex);
            returnedFile = createTempFileFromBuffer(returnedBuffer);
        } catch (ArrayIndexOutOfBoundsException | IOException e) {
            LOGGER.warn("Old format revision detected!! Hydrating using alternate hydrator.");
            returnedFile = hydrateOldFormatRevision(fileToEdit, edits);
        } finally {
            try {
                editStream.close();
            } catch (IOException e) {
                LOGGER.warn(Utility.expandStackTraceToString(e));
            }
        }
        return returnedFile;
    }

    /**
     * Hydrate a file stored in old format, given a file to which we apply an
     * edit script.
     *
     * @param fileToEdit the file that will be changed by the edit script.
     * @param edits the edit script.
     * @return a new temporary file that is the result of applying the edits to
     * the original file.
     * @throws IOException if the fileToEdit cannot be opened or read.
     */
    private java.io.File hydrateOldFormatRevision(java.io.File fileToEdit, byte[] edits) throws IOException {
        DataInputStream editStream = new DataInputStream(new ByteArrayInputStream(edits));
        RepairCompareFilesEditInformation editInfo = new RepairCompareFilesEditInformation();
        byte[] originalData = getFileData(fileToEdit);
        byte[] editedBuffer = new byte[edits.length + originalData.length]; // It can't be any bigger than this.
        java.io.File returnedFile = null;
        int inIndex = 0;
        int outIndex = 0;
        int deletedBytesCount;
        int insertedBytesCount;
        int bytesTillChange = 0;

        try {
            // We need to first skip 8 bytes from the stream because there are 8 bytes there that we should ignore (for now).
            byte[] eightBytes = new byte[RepairCompareFilesEditHeader.getEditHeaderSize()];
            editStream.read(eightBytes);
            while (editStream.available() > 0) {
                editInfo.read(editStream);
                bytesTillChange = (int) editInfo.getSeekPosition() - inIndex;
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, bytesTillChange);

                inIndex += bytesTillChange;
                outIndex += bytesTillChange;

                deletedBytesCount = (int) editInfo.getDeletedBytesCount();
                insertedBytesCount = (int) editInfo.getInsertedBytesCount();

                switch (editInfo.getEditType()) {
                    case RepairCompareFilesEditInformation.QVCS_EDIT_DELETE:
                        /*
                         * Delete input
                         */
                        // Just skip over deleted bytes
                        inIndex += deletedBytesCount;
                        break;

                    case RepairCompareFilesEditInformation.QVCS_EDIT_INSERT:
                        /*
                         * Insert edit lines
                         */
                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    case RepairCompareFilesEditInformation.QVCS_EDIT_REPLACE:
                        /*
                         * Replace input line with edit line.
                         * First skip over the bytes to be replaced, then copy the replacing bytes from the edit file to the output
                         * file.
                         */
                        inIndex += deletedBytesCount;

                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    default:
                        break;
                }
            }

            // Copy the rest of the input "file" to the output "file".
            int remainingBytes = originalData.length - inIndex;
            if (remainingBytes > 0) {
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, remainingBytes);
                outIndex += remainingBytes;
            }
            byte[] returnedBuffer = new byte[outIndex];
            System.arraycopy(editedBuffer, 0, returnedBuffer, 0, outIndex);
            returnedFile = createTempFileFromBuffer(returnedBuffer);
        } catch (ArrayIndexOutOfBoundsException | IOException e) {
            LOGGER.warn(Utility.expandStackTraceToString(e));
            LOGGER.warn(" editInfo.seekPosition: " + editInfo.getSeekPosition() + " originalData.length: " + originalData.length + " inIndex: "
                    + inIndex + " editedBuffer.length: "
                    + editedBuffer.length + " outIndex: " + outIndex + " bytesTillChange: " + bytesTillChange);
            LOGGER.warn(e.getLocalizedMessage());
            throw new QVCSRuntimeException("Unable to hydrate file: [{}}");
        } finally {
            try {
                editStream.close();
            } catch (IOException e) {
                LOGGER.warn(Utility.expandStackTraceToString(e));
            }
        }
        return returnedFile;
    }

    /**
     * Find the child directoryLocation given the parent directory location id.
     * This algorithm searches for the deepest matching branch. For example, if
     * the branchId points to a branch that has several ancestor branches, this
     * method will return the child directoryLocation with the best match in its
     * branchId, where 'best' means that branch that is the deepest branch.
     *
     * @param branchId the branch id.
     * @param parentDirectoryLocationId the parent directoryLocationId.
     * @param directorySegmentName the directory segment name of the child directory.
     * @return the child directoryLocation, or null if it does not exist.
     */
    public DirectoryLocation findChildDirectoryLocation(int branchId, Integer parentDirectoryLocationId, String directorySegmentName) {
        DirectoryLocation directoryLocation = null;
        try {
            databaseManager.getConnection();
            FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            List<Branch> branchArray = functionalQueriesDAO.getBranchAncestryList(branchId);

            // Look for a directory location, deepest to shallowest branch. The deepest one found 'wins'.
            DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
            for (int index = branchArray.size() - 1; index >= 0; index--) {
                directoryLocation = directoryLocationDAO.findChildDirectoryLocation(branchArray.get(index).getId(), parentDirectoryLocationId, directorySegmentName);
                if (directoryLocation != null) {
                    break;
                }
            }
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new RuntimeException(e);
        }
        return directoryLocation;
    }

    /**
     * Find the DirectoryLocation, given a branchId, and an appendedPath.
     * @param branchId the branch id.
     * @param appendedPath the appendedPath for the directory.
     * @return the DirectoryLocation, or null if not found.
     */
    public DirectoryLocation findDirectoryLocationByAppendedPath(int branchId, String appendedPath) {
        DirectoryLocation directoryLocation;
        String[] segments = appendedPath.split(java.io.File.separator);
        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findById(branchId);
        DirectoryDAO directoryDAO = new DirectoryDAOImpl(schemaName);
        Directory rootDirectory = directoryDAO.findById(branch.getRootDirectoryId());
        DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
        directoryLocation = directoryLocationDAO.findByDirectoryId(rootDirectory.getId());
        DirectoryLocation parentDirectoryLocation = directoryLocation;
        if (appendedPath.length() > 0) {
            for (String segment: segments) {
                if (parentDirectoryLocation != null) {
                    directoryLocation = findChildDirectoryLocation(branchId, parentDirectoryLocation.getId(), segment);
                    parentDirectoryLocation = directoryLocation;
                }
            }
        }
        return directoryLocation;
    }

    /**
     * Get the list of file revisions for the given branch/fileId. The returned list
     * will include all the file revisions, including any on the parent branch(es).
     * @param branch the branch for which the request is made.
     * @param fileId the fileId of the file that we're interested in.
     * @return a list of FileRevisions (absent the actual revision data) in newest to oldest order.
     */
    public List<FileRevision> getFileRevisionList(Branch branch, Integer fileId) {
        List<FileRevision> fileRevisionList;
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<Branch> branchArray = functionalQueriesDAO.getBranchAncestryList(branch.getId());
        fileRevisionList = functionalQueriesDAO.findFileRevisionsInBranches(branchArray, fileId);
        return fileRevisionList;
    }

    /**
     * Create or get the commit id. This method check for the existence of an enclosing
     * Server transaction. If it exists, then the commitId is retrieved from the thread local
     * storage and this thread is enlisted in that transaction so that multiple operations
     * can share the same commitId.
     * @param commitId a supplied commitId, or null.
     * @param commitMessage the commit message.
     * @param performCommit a flag (returned) that indicates whether the commit should be performed by the caller (true), or at the completion of
     * the transaction.
     * @return the commitId.
     * @throws SQLException if the insert to the commit table fails.
     */
    public Integer getCommitId(Integer commitId, String commitMessage, AtomicBoolean performCommit) throws SQLException {
        // See if we are in a transaction, and if so, we'll enlist so all can share the same commit_id.
        if (ServerTransactionManager.getInstance().transactionIsInProgress(getResponse())) {
            // Create or lookup the commit_id...
            commitId = createOrLookupCommitId(commitMessage);
        } else {
            // Create a commit object if caller did not supply one, and insert into database.
            if (commitId == null) {
                performCommit.set(true);
                Commit commit = new Commit();
                commit.setUserId(getUserId());
                commit.setCommitMessage(commitMessage);
                CommitDAO commitDAO = new CommitDAOImpl(schemaName);
                commitId = commitDAO.insert(commit);
            }
        }
        return commitId;

    }

    private Integer createOrLookupCommitId(String commitMessage) throws SQLException {
        Integer commitId = this.threadLocalCommitId.get();
        if (commitId == null) {
            Commit commit = new Commit();
            commit.setUserId(getUserId());
            commit.setCommitMessage(commitMessage);
            CommitDAO commitDAO = new CommitDAOImpl(schemaName);
            commitId = commitDAO.insert(commit);
            threadLocalCommitId.set(commitId);
            ServerTransactionManager.getInstance().enlistPendingWork(getResponse(), this);
        }
        return commitId;
    }

    @Override
    public void commitPendingChanges(ServerResponseFactoryInterface response, Date date) throws QVCSException {
        LOGGER.debug("Commiting work.");
        if (threadLocalCommitId.get() != null) {
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                connection.commit();
                threadLocalCommitId.remove();
            } catch (SQLException e) {
                LOGGER.warn("SQL exception: ", e);
            }
        }
    }

    @Override
    public int getPriority() {
        return TransactionParticipantInterface.DONT_CARE_PRIORITY;
    }
}
