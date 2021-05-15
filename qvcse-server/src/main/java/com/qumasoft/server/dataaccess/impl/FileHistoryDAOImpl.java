/*   Copyright 2004-2021 Jim Voris
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
package com.qumasoft.server.dataaccess.impl;

import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.dataaccess.FileHistoryDAO;
import com.qumasoft.server.datamodel.FileHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File history DAO implementation.
 * @author Jim Voris
 */
public class FileHistoryDAOImpl implements FileHistoryDAO {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHistoryDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int FILE_ID_RESULT_SET_INDEX = 2;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 3;
    private static final int DIRECTORY_ID_RESULT_SET_INDEX = 4;
    private static final int FILE_NAME_RESULT_SET_INDEX = 5;
    private static final int INSERT_DATE_RESULT_SET_INDEX = 6;
    private static final int UPDATE_DATE_RESULT_SET_INDEX = 7;
    private static final int DELETED_FLAG_RESULT_SET_INDEX = 8;

    private String schemaName;
    private String findAll;
    private String findByBranchAndDirectoryIdAndBranchDate;
    private String findByBranchAndFileId;

    public FileHistoryDAOImpl() {
        this("qvcse");
    }

    public FileHistoryDAOImpl(String schema) {
        this.schemaName = schema;

        String selectSegment = "SELECT ID, FILE_ID, BRANCH_ID, DIRECTORY_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM ";
        this.findAll = selectSegment + this.schemaName + ".FILE_HISTORY ORDER BY FILE_ID, ID";
        this.findByBranchAndDirectoryIdAndBranchDate = selectSegment + this.schemaName
            + ".FILE_HISTORY WHERE BRANCH_ID = ? AND DIRECTORY_ID = ? AND UPDATE_DATE <= ? ORDER BY FILE_ID ASC, ID DESC";
        this.findByBranchAndFileId = selectSegment + this.schemaName
            + ".FILE_HISTORY WHERE BRANCH_ID = ? AND FILE_ID = ? ORDER BY ID DESC";
    }

    /**
     * Find the list of files associated with a given branch and directory that existed on or before the given date.
     *
     * @param branchId the branch where the fileHistory lives.
     * @param directoryId the directory where the fileHistory lives.
     * @param branchDate the date of the branch.
     * @return the List of files that are in the given directory on the given branch. The list may be empty if there are no files.
     */
    @Override
    public List<FileHistory> findByBranchAndDirectoryIdAndBranchDate(Integer branchId, Integer directoryId, Date branchDate) {
        List<FileHistory> fileHistoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findByBranchAndDirectoryIdAndBranchDate, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, directoryId);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(branchDate.getTime()));
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                String fileName = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                FileHistory fileHistory = new FileHistory();
                fileHistory.setBranchId(branchId);
                fileHistory.setFileId(fileId);
                fileHistory.setDirectoryId(directoryId);
                fileHistory.setFileName(fileName);
                fileHistory.setInsertDate(insertDate);
                fileHistory.setUpdateDate(updateDate);
                fileHistory.setDeletedFlag(deletedFlag);
                fileHistoryList.add(fileHistory);
            }
        } catch (SQLException e) {
            LOGGER.error("FileHistoryDAOImpl: SQL exception in findByBranchAndDirectoryIdAndBranchDate", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileHistoryDAOImpl: exception in findByBranchAndDirectoryIdAndBranchDate", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileHistoryList;
    }

    /**
     * Find the list of history records for a given branch and file.
     *
     * @param branchId the branch id.
     * @param fileId the file id.
     * @return the list of file history records for the given branch and file, newest to oldest.
     */
    @Override
    public List<FileHistory> findHistoryForFileId(Integer branchId, Integer fileId) {
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        List<FileHistory> fileHistoryList = new ArrayList<>();
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findByBranchAndFileId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, fileId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                String fileName = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                FileHistory fileHistory = new FileHistory();
                fileHistory.setBranchId(branchId);
                fileHistory.setFileId(fileId);
                fileHistory.setDirectoryId(directoryId);
                fileHistory.setFileName(fileName);
                fileHistory.setInsertDate(insertDate);
                fileHistory.setUpdateDate(updateDate);
                fileHistory.setDeletedFlag(deletedFlag);
                fileHistoryList.add(fileHistory);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FileHistoryDAOImpl: exception in findHistoryForFileId", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileHistoryList;
    }

    @Override
    public List<FileHistory> findAll() {
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        List<FileHistory> fileHistoryList = new ArrayList<>();
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer id = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Integer branchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                String fileName = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                FileHistory fileHistory = new FileHistory();
                fileHistory.setId(id);
                fileHistory.setBranchId(branchId);
                fileHistory.setFileId(fileId);
                fileHistory.setDirectoryId(directoryId);
                fileHistory.setFileName(fileName);
                fileHistory.setInsertDate(insertDate);
                fileHistory.setUpdateDate(updateDate);
                fileHistory.setDeletedFlag(deletedFlag);
                fileHistoryList.add(fileHistory);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FileHistoryDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileHistoryList;
    }
}
