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
package com.qvcsos.server.dataaccess.impl;

import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.FileRevision;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class FileRevisionDAOImpl implements FileRevisionDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileRevisionDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 2;
    private static final int FILE_ID_RESULT_SET_INDEX = 3;
    private static final int ANCESTOR_REVISION_ID_RESULT_SET_INDEX = 4;
    private static final int REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX = 5;
    private static final int COMMIT_ID_RESULT_SET_INDEX = 6;
    private static final int PROMOTED_FLAG_RESULT_SET_INDEX = 7;
    private static final int WORKFILE_EDIT_DATE_RESULT_SET_INDEX = 8;
    private static final int REVISION_DIGEST_RESULT_SET_INDEX = 9;
    private static final int REVISION_SIZE_RESULT_SET_INDEX = 10;
    private static final int REVISION_DATA_RESULT_SET_INDEX = 11;

    private final String schemaName;

    private final String findById;
    private final String findFileRevisions;
    private final String findAllFileRevisions;
    private final String findNewestRevisionOnBranch;
    private final String findNewestRevisionAllBranches;
    private final String findNewestBranchRevision;
    private final String findPromotionCandidates;
    private final String findNewestPromotedRevision;
    private final String findByBranchIdAndAncestorRevisionAndFileId;
    private final String findCommonAncestorRevision;
    private final String findFileIdListForCommitId;

    private final String insertFileRevision;
    private final String updateAncestorRevision;
    private final String markPromoted;

    public FileRevisionDAOImpl(String schema) {
        this.schemaName = schema;
        String selectAllSegment = "SELECT ID, BRANCH_ID, FILE_ID, ANCESTOR_REVISION_ID, REVERSE_DELTA_REVISION_ID, COMMIT_ID, PROMOTED_FLAG, WORKFILE_EDIT_DATE, REVISION_DIGEST, "
                + "LENGTH(REVISION_DATA) AS REVISION_SIZE, "
                + "REVISION_DATA FROM ";
        String selectHeaderSegment = "SELECT ID, BRANCH_ID, FILE_ID, ANCESTOR_REVISION_ID, REVERSE_DELTA_REVISION_ID, COMMIT_ID, PROMOTED_FLAG, WORKFILE_EDIT_DATE, REVISION_DIGEST, "
                + "LENGTH(REVISION_DATA) AS REVISION_SIZE FROM ";

        this.findById = selectAllSegment + this.schemaName + ".FILE_REVISION WHERE ID = ?";
        this.findFileRevisions = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE FILE_ID = ? AND BRANCH_ID IN (%s) ORDER BY ID DESC";
        this.findAllFileRevisions = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE FILE_ID = ? ORDER BY ID DESC";
        this.findNewestRevisionOnBranch = selectAllSegment + this.schemaName + ".FILE_REVISION WHERE FILE_ID = ? AND BRANCH_ID = ? ORDER BY ID DESC LIMIT 1";
        this.findNewestRevisionAllBranches = selectAllSegment + this.schemaName + ".FILE_REVISION WHERE FILE_ID = ? ORDER BY ID DESC LIMIT 1";
        this.findNewestBranchRevision = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID = ? ORDER BY ID DESC LIMIT 1";
        this.findPromotionCandidates = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID = ? AND PROMOTED_FLAG = FALSE ORDER BY FILE_ID, ID DESC";
        this.findNewestPromotedRevision = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID = ? AND FILE_ID = ? AND PROMOTED_FLAG = TRUE ORDER BY ID DESC LIMIT 1";
        this.findByBranchIdAndAncestorRevisionAndFileId = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID = ? AND ANCESTOR_REVISION_ID = ? AND "
                + "FILE_ID = ? ORDER BY ID DESC LIMIT 1";
        this.findCommonAncestorRevision = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID = ? AND ID <= ? AND "
                + "ID <= ? AND "
                + "FILE_ID = ? ORDER BY ID DESC LIMIT 1";
        this.findFileIdListForCommitId = "SELECT FILE_ID FROM " + this.schemaName + ".FILE_REVISION WHERE COMMIT_ID = ?";

        this.insertFileRevision = "INSERT INTO " + this.schemaName
                + ".FILE_REVISION (BRANCH_ID, FILE_ID, ANCESTOR_REVISION_ID, REVERSE_DELTA_REVISION_ID, COMMIT_ID, PROMOTED_FLAG, WORKFILE_EDIT_DATE, REVISION_DIGEST, REVISION_DATA) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING ID";
        this.updateAncestorRevision = "UPDATE " + this.schemaName + ".FILE_REVISION SET REVERSE_DELTA_REVISION_ID = ?, REVISION_DATA = ? WHERE ID = ? RETURNING ID";
        this.markPromoted = "UPDATE " + this.schemaName + ".FILE_REVISION SET PROMOTED_FLAG = TRUE WHERE BRANCH_ID = ? AND FILE_ID = ?";
    }

    @Override
    public FileRevision findById(Integer id) {
        FileRevision revision = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);
                byte[] fetchedData = resultSet.getBytes(REVISION_DATA_RESULT_SET_INDEX);

                revision = new FileRevision();
                revision.setId(fetchedId);
                revision.setBranchId(fetchedBranchId);
                revision.setFileId(fetchedFileId);
                revision.setAncestorRevisionId(fetchedAncestorRevisionId);
                revision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                revision.setCommitId(fetchedCommitId);
                revision.setPromotedFlag(fetchedPromotedFlag);
                revision.setWorkfileEditDate(fetchedWorkfileEditDate);
                revision.setRevisionDigest(fetchedDigest);
                revision.setRevisionDataSize(fetchedSize);
                revision.setRevisionData(fetchedData);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return revision;
    }

    @Override
    public List<FileRevision> findFileRevisions(String branchesToSearch, Integer fileId) {
        List<FileRevision> fileRevisionList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String queryString = String.format(findFileRevisions, branchesToSearch);
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                FileRevision fileRevision = new FileRevision();
                fileRevision.setId(fetchedId);
                fileRevision.setBranchId(fetchedBranchId);
                fileRevision.setFileId(fetchedFileId);
                fileRevision.setAncestorRevisionId(fetchedAncestorRevisionId);
                fileRevision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                fileRevision.setCommitId(fetchedCommitId);
                fileRevision.setPromotedFlag(fetchedPromotedFlag);
                fileRevision.setWorkfileEditDate(fetchedWorkfileEditDate);
                fileRevision.setRevisionDigest(fetchedDigest);
                fileRevision.setRevisionDataSize(fetchedSize);
                fileRevisionList.add(fileRevision);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findFileRevisions", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findFileRevisions", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileRevisionList;
    }

    @Override
    public List<FileRevision> findAllFileRevisions(Integer fileId) {
        List<FileRevision> fileRevisionList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(findAllFileRevisions, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                FileRevision fileRevision = new FileRevision();
                fileRevision.setId(fetchedId);
                fileRevision.setBranchId(fetchedBranchId);
                fileRevision.setFileId(fetchedFileId);
                fileRevision.setAncestorRevisionId(fetchedAncestorRevisionId);
                fileRevision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                fileRevision.setCommitId(fetchedCommitId);
                fileRevision.setPromotedFlag(fetchedPromotedFlag);
                fileRevision.setWorkfileEditDate(fetchedWorkfileEditDate);
                fileRevision.setRevisionDigest(fetchedDigest);
                fileRevision.setRevisionDataSize(fetchedSize);
                fileRevisionList.add(fileRevision);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findAllFileRevisions", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findAllFileRevisions", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileRevisionList;
    }

    @Override
    public FileRevision findNewestRevisionOnBranch(Integer branchId, Integer fileId) {
        FileRevision newestRevision = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findNewestRevisionOnBranch, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);
            preparedStatement.setInt(2, branchId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);
                byte[] fetchedData = resultSet.getBytes(REVISION_DATA_RESULT_SET_INDEX);

                newestRevision = new FileRevision();
                newestRevision.setId(fetchedId);
                newestRevision.setBranchId(fetchedBranchId);
                newestRevision.setFileId(fetchedFileId);
                newestRevision.setAncestorRevisionId(fetchedAncestorRevisionId);
                newestRevision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                newestRevision.setCommitId(fetchedCommitId);
                newestRevision.setPromotedFlag(fetchedPromotedFlag);
                newestRevision.setWorkfileEditDate(fetchedWorkfileEditDate);
                newestRevision.setRevisionDigest(fetchedDigest);
                newestRevision.setRevisionDataSize(fetchedSize);
                newestRevision.setRevisionData(fetchedData);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findNewestRevisionOnBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findNewestRevisionOnBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return newestRevision;
    }

    @Override
    public FileRevision findNewestRevisionAllBranches(Integer fileId) {
        FileRevision newestRevision = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findNewestRevisionAllBranches, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);
                byte[] fetchedData = resultSet.getBytes(REVISION_DATA_RESULT_SET_INDEX);

                newestRevision = new FileRevision();
                newestRevision.setId(fetchedId);
                newestRevision.setBranchId(fetchedBranchId);
                newestRevision.setFileId(fetchedFileId);
                newestRevision.setAncestorRevisionId(fetchedAncestorRevisionId);
                newestRevision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                newestRevision.setCommitId(fetchedCommitId);
                newestRevision.setPromotedFlag(fetchedPromotedFlag);
                newestRevision.setWorkfileEditDate(fetchedWorkfileEditDate);
                newestRevision.setRevisionDigest(fetchedDigest);
                newestRevision.setRevisionDataSize(fetchedSize);
                newestRevision.setRevisionData(fetchedData);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findNewestRevisionAllBranches", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findNewestRevisionAllBranches", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return newestRevision;
    }

    @Override
    public FileRevision findNewestBranchRevision(int branchId) {
        FileRevision revision = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findNewestBranchRevision, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                revision = new FileRevision();
                revision.setId(fetchedId);
                revision.setBranchId(fetchedBranchId);
                revision.setFileId(fetchedFileId);
                revision.setAncestorRevisionId(fetchedAncestorRevisionId);
                revision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                revision.setCommitId(fetchedCommitId);
                revision.setPromotedFlag(fetchedPromotedFlag);
                revision.setWorkfileEditDate(fetchedWorkfileEditDate);
                revision.setRevisionDigest(fetchedDigest);
                revision.setRevisionDataSize(fetchedSize);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findNewestBranchRevision", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findNewestBranchRevision", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return revision;
    }

    @Override
    public List<FileRevision> findPromotionCandidates(Branch promoteFromBranch, Branch promoteToBranch) {
        List<FileRevision> fileRevisionList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findPromotionCandidates, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, promoteFromBranch.getId());

            Map<Integer, FileRevision> fileRevisionMap = new TreeMap<>();
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                // We only need the newest revision.
                if (!fileRevisionMap.containsKey(fetchedFileId)) {
                    FileRevision fileRevision = new FileRevision();
                    fileRevision.setId(fetchedId);
                    fileRevision.setBranchId(fetchedBranchId);
                    fileRevision.setFileId(fetchedFileId);
                    fileRevision.setAncestorRevisionId(fetchedAncestorRevisionId);
                    fileRevision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                    fileRevision.setCommitId(fetchedCommitId);
                    fileRevision.setPromotedFlag(fetchedPromotedFlag);
                    fileRevision.setWorkfileEditDate(fetchedWorkfileEditDate);
                    fileRevision.setRevisionDigest(fetchedDigest);
                    fileRevision.setRevisionDataSize(fetchedSize);
                    fileRevisionMap.put(fetchedFileId, fileRevision);
                }
            }
            fileRevisionList = prunePromotedRevisions(fileRevisionMap);
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findPromotionCandidates", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findPromotionCandidates", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileRevisionList;
    }

    @Override
    public FileRevision findCommonAncestorRevision(Integer promoteToBranchId, Integer newestBranchAncestorId, Integer newestPromoteToAncestorId, Integer fileId) {
        FileRevision revision = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findCommonAncestorRevision, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, promoteToBranchId);
            preparedStatement.setInt(2, newestBranchAncestorId);
            preparedStatement.setInt(3, newestPromoteToAncestorId);
            preparedStatement.setInt(4, fileId);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                revision = new FileRevision();
                revision.setId(fetchedId);
                revision.setBranchId(fetchedBranchId);
                revision.setFileId(fetchedFileId);
                revision.setAncestorRevisionId(fetchedAncestorRevisionId);
                revision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                revision.setCommitId(fetchedCommitId);
                revision.setPromotedFlag(fetchedPromotedFlag);
                revision.setWorkfileEditDate(fetchedWorkfileEditDate);
                revision.setRevisionDigest(fetchedDigest);
                revision.setRevisionDataSize(fetchedSize);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findCommonAncestorRevision", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findCommonAncestorRevision", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return revision;
    }

    @Override
    public FileRevision findNewestPromotedRevision(int promoteFromBranchId, Integer fileId) {
        FileRevision revision = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findNewestPromotedRevision, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, promoteFromBranchId);
            preparedStatement.setInt(2, fileId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                revision = new FileRevision();
                revision.setId(fetchedId);
                revision.setBranchId(fetchedBranchId);
                revision.setFileId(fetchedFileId);
                revision.setAncestorRevisionId(fetchedAncestorRevisionId);
                revision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                revision.setCommitId(fetchedCommitId);
                revision.setPromotedFlag(fetchedPromotedFlag);
                revision.setWorkfileEditDate(fetchedWorkfileEditDate);
                revision.setRevisionDigest(fetchedDigest);
                revision.setRevisionDataSize(fetchedSize);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findBranchTipRevisionByBranchIdAndFileId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findBranchTipRevisionByBranchIdAndFileId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return revision;
    }

    @Override
    public FileRevision findByBranchIdAndAncestorRevisionAndFileId(int promoteToBranchId, Integer ancestorRevisionId, Integer fileId) {
        FileRevision revision = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByBranchIdAndAncestorRevisionAndFileId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, promoteToBranchId);
            preparedStatement.setInt(2, ancestorRevisionId);
            preparedStatement.setInt(3, fileId);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                revision = new FileRevision();
                revision.setId(fetchedId);
                revision.setBranchId(fetchedBranchId);
                revision.setFileId(fetchedFileId);
                revision.setAncestorRevisionId(fetchedAncestorRevisionId);
                revision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                revision.setCommitId(fetchedCommitId);
                revision.setPromotedFlag(fetchedPromotedFlag);
                revision.setWorkfileEditDate(fetchedWorkfileEditDate);
                revision.setRevisionDigest(fetchedDigest);
                revision.setRevisionDataSize(fetchedSize);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findBranchTipRevisionByBranchIdAndFileId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findBranchTipRevisionByBranchIdAndFileId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return revision;
    }

    @Override
    public List<Integer> findFileIdListForCommitId(Integer commitId) {
        List<Integer> fileIdList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findFileIdListForCommitId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, commitId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedFileId = resultSet.getInt(1);
                fileIdList.add(fetchedFileId);
            }
        } catch (SQLException e) {
            LOGGER.error("FileRevisionDAOImpl: SQL exception in findPromotionCandidates", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in findPromotionCandidates", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileIdList;
    }

    @Override
    public Integer insert(FileRevision fileRevision) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.insertFileRevision);
            // <editor-fold>
            preparedStatement.setInt(1, fileRevision.getBranchId());
            preparedStatement.setInt(2, fileRevision.getFileId());
            if (fileRevision.getAncestorRevisionId() != null) {
                preparedStatement.setInt(3, fileRevision.getAncestorRevisionId());
            } else {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            }
            if (fileRevision.getReverseDeltaRevisionId() != null) {
                preparedStatement.setInt(4, fileRevision.getReverseDeltaRevisionId());
            } else {
                preparedStatement.setNull(4, java.sql.Types.INTEGER);
            }
            preparedStatement.setInt(5, fileRevision.getCommitId());
            preparedStatement.setBoolean(6, false);
            preparedStatement.setTimestamp(7, fileRevision.getWorkfileEditDate());
            preparedStatement.setBytes(8, fileRevision.getRevisionDigest());
            preparedStatement.setBytes(9, fileRevision.getRevisionData());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public boolean updateAncestorRevision(Integer id, Integer reverseDeltaRevisionId, byte[] reverseDeltaScript) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.updateAncestorRevision);
            // <editor-fold>
            preparedStatement.setInt(1, reverseDeltaRevisionId);
            preparedStatement.setBytes(2, reverseDeltaScript);
            preparedStatement.setInt(3, id);
            // </editor-fold>

            returnFlag = preparedStatement.execute();
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in updateAncestorRevision", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

    @Override
    public boolean markPromoted(Integer fileRevisionId) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            FileRevision fileRevision = findById(fileRevisionId);

            Connection connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.markPromoted);
            // <editor-fold>
            preparedStatement.setInt(1, fileRevision.getBranchId());
            preparedStatement.setInt(2, fileRevision.getFileId());
            // </editor-fold>

            returnFlag = preparedStatement.execute();
            connection.commit();
        } catch (IllegalStateException e) {
            LOGGER.error("FileRevisionDAOImpl: exception in markPromoted", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

    private List<FileRevision> prunePromotedRevisions(Map<Integer, FileRevision> fileRevisionMap) {
        List<FileRevision> fileRevisionList = new ArrayList<>();

        // Discard any revisions that have been promoted already.
        Set<Integer> fileIdSet = fileRevisionMap.keySet();

        // Prune any revisions already promoted...
        Map<Integer, FileRevision> prunedRevisionMap = new TreeMap<>();
        for (Integer fileId : fileIdSet) {
            FileRevision fileRevision = fileRevisionMap.get(fileId);
            if (!fileRevision.getPromotedFlag()) {
                prunedRevisionMap.put(fileId, fileRevision);
            }
        }
        fileIdSet = prunedRevisionMap.keySet();
        for (Integer revisionId : fileIdSet) {
            fileRevisionList.add(prunedRevisionMap.get(revisionId));
        }
        return fileRevisionList;
    }
}
