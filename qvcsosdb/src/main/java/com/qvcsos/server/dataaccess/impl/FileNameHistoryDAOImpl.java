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
import com.qvcsos.server.dataaccess.FileNameHistoryDAO;
import com.qvcsos.server.datamodel.FileNameHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class FileNameHistoryDAOImpl implements FileNameHistoryDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileNameHistoryDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int FILE_NAME_ID_RESULT_SET_INDEX = 2;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 3;
    private static final int DIRECTORY_ID_RESULT_SET_INDEX = 4;
    private static final int FILE_ID_RESULT_SET_INDEX = 5;
    private static final int CREATED_FOR_REASON_RESULT_SET_INDEX = 6;
    private static final int COMMIT_ID_RESULT_SET_INDEX = 7;
    private static final int FILE_NAME_RESULT_SET_INDEX = 8;
    private static final int DELETED_FLAG_RESULT_SET_INDEX = 9;

    private final String schemaName;
    private final String findByFileIdAndCommitId;
    private final String findByBranchListAndFileId;
    private final String getFileNameIdListForReadOnlyBranch;

    public FileNameHistoryDAOImpl(String schema) {
        this.schemaName = schema;

        String selectSegment = "SELECT ID, FILE_NAME_ID, BRANCH_ID, DIRECTORY_ID, FILE_ID, CREATED_FOR_REASON, COMMIT_ID, FILE_NAME, DELETED_FLAG FROM ";
        this.findByFileIdAndCommitId = selectSegment + this.schemaName + ".FILE_NAME_HISTORY WHERE FILE_ID = ? AND COMMIT_ID <= ? ORDER BY ID DESC LIMIT 1";
        this.findByBranchListAndFileId = selectSegment + this.schemaName + ".FILE_NAME WHERE BRANCH_ID IN (%s) AND FILE_ID = ? ORDER BY BRANCH_ID DESC, ID DESC LIMIT 1";
        this.getFileNameIdListForReadOnlyBranch = "SELECT FN.FILE_NAME_ID, FN.FILE_ID, FN.DIRECTORY_ID, FN.FILE_NAME FROM " + this.schemaName
                + ".FILE_NAME_HISTORY FN WHERE FN.DELETED_FLAG = FALSE AND FN.BRANCH_ID IN (%s) AND FN.COMMIT_ID < ? ORDER BY BRANCH_ID DESC, ID DESC";
    }

    @Override
    public FileNameHistory findByFileIdAndCommitId(Integer fileId, Integer commitId) {
        FileNameHistory fileNameHistory = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByFileIdAndCommitId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);
            preparedStatement.setInt(2, commitId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedFileNameId = resultSet.getInt(FILE_NAME_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedDirectoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedCreatedForReasonObject = resultSet.getObject(CREATED_FOR_REASON_RESULT_SET_INDEX);
                Integer fetchedCreatedForReason = null;
                if (fetchedCreatedForReasonObject != null) {
                    fetchedCreatedForReason = resultSet.getInt(CREATED_FOR_REASON_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                fileNameHistory = new FileNameHistory();
                fileNameHistory.setId(fetchedId);
                fileNameHistory.setFileNameId(fetchedFileNameId);
                fileNameHistory.setBranchId(fetchedBranchId);
                fileNameHistory.setDirectoryId(fetchedDirectoryId);
                fileNameHistory.setFileId(fetchedFileId);
                fileNameHistory.setCreatedForReason(fetchedCreatedForReason);
                fileNameHistory.setCommitId(fetchedCommitId);
                fileNameHistory.setFileName(fetchedFilename);
                fileNameHistory.setDeletedFlag(fetchedDeletedFlag);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameHistoryDAOImpl: SQL exception in findByFileIdAndCommitId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameHistoryDAOImpl: exception in findByFileIdAndCommitId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileNameHistory;
    }

    @Override
    public FileNameHistory findByBranchListAndFileId(String branchList, Integer fileId) {
        FileNameHistory fileNameHistory = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        String queryString = String.format(findByBranchListAndFileId, branchList);
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedFileNameId = resultSet.getInt(FILE_NAME_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedDirectoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedCreatedForReasonObject = resultSet.getObject(CREATED_FOR_REASON_RESULT_SET_INDEX);
                Integer fetchedCreatedForReason = null;
                if (fetchedCreatedForReasonObject != null) {
                    fetchedCreatedForReason = resultSet.getInt(CREATED_FOR_REASON_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                fileNameHistory = new FileNameHistory();
                fileNameHistory.setId(fetchedId);
                fileNameHistory.setFileNameId(fetchedFileNameId);
                fileNameHistory.setBranchId(fetchedBranchId);
                fileNameHistory.setDirectoryId(fetchedDirectoryId);
                fileNameHistory.setFileId(fetchedFileId);
                fileNameHistory.setCreatedForReason(fetchedCreatedForReason);
                fileNameHistory.setCommitId(fetchedCommitId);
                fileNameHistory.setFileName(fetchedFilename);
                fileNameHistory.setDeletedFlag(fetchedDeletedFlag);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameHistoryDAOImpl: SQL exception in findByBranchListAndFileId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameHistoryDAOImpl: exception in findByBranchListAndFileId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileNameHistory;
    }

    @Override
    public List<Integer> getFileNameIdListForReadOnlyBranch(String branchesToSearchString, int directoryId, Integer tagBranchCommitId) {
        List<Integer> fileNameIdList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        String queryString = String.format(getFileNameIdListForReadOnlyBranch, branchesToSearchString);
        Map<Integer, String> fileNameIdMap = new TreeMap<>();
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, tagBranchCommitId);

            resultSet = preparedStatement.executeQuery();
            // <editor-fold>
            while (resultSet.next()) {
                Integer fetchedFileNameId = resultSet.getInt(1);
                Integer fetchedFileId = resultSet.getInt(2);
                if (!fileNameIdMap.containsKey(fetchedFileId)) {
                    String fileName = resultSet.getString(4);
                    fileNameIdMap.put(fetchedFileId, fileName);
                    int fetchedDirectoryId = resultSet.getInt(3);
                    if (fetchedDirectoryId == directoryId) {
                        fileNameIdList.add(fetchedFileNameId);
                    }
                }
            }
            // </editor-fold>
        } catch (SQLException e) {
            LOGGER.error("FileNameHistoryDAOImpl: SQL exception in getFileNameIdListForReadOnlyBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameHistoryDAOImpl: exception in getFileNameIdListForReadOnlyBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileNameIdList;
    }
}
