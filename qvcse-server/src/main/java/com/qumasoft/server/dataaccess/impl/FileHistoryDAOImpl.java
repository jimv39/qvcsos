//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.server.dataaccess.impl;

import com.qumasoft.server.DatabaseManager;
import com.qumasoft.server.dataaccess.FileHistoryDAO;
import com.qumasoft.server.datamodel.FileHistory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * File history DAO implementation.
 * @author Jim Voris
 */
public class FileHistoryDAOImpl implements FileHistoryDAO {
    /*
     * + "ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT ID_PK PRIMARY KEY," + "FILE_ID INT," + "BRANCH_ID INT NOT NULL," +
     * "DIRECTORY_ID INT NOT NULL," + "FILE_NAME VARCHAR(256) NOT NULL," + "INSERT_DATE TIMESTAMP NOT NULL," + "UPDATE_DATE
     * TIMESTAMP NOT NULL," + "DELETED_FLAG BOOLEAN NOT NULL)";
     */

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server.DatabaseManager");
    private static final String FIND_BY_BRANCH_AND_DIRECTORY_ID_AND_VIEW_DATE =
            "SELECT FILE_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.FILE_HISTORY WHERE BRANCH_ID = ? AND DIRECTORY_ID = ? AND UPDATE_DATE <= ? "
            + "ORDER BY FILE_ID ASC, ID DESC";
    private static final String FIND_BY_BRANCH_AND_FILE_ID =
            "SELECT DIRECTORY_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.FILE_HISTORY WHERE BRANCH_ID = ? AND FILE_ID = ? "
            + "ORDER BY ID DESC";

    /**
     * Find the list of files associated with a given branch and directory that existed on or before the given date.
     *
     * @param branchId the branch where the fileHistory lives.
     * @param directoryId the directory where the fileHistory lives.
     * @param viewDate the date of the view.
     * @return the List of files that are in the given directory on the given branch. The list may be empty if there are no files.
     */
    @Override
    public List<FileHistory> findByBranchAndDirectoryIdAndViewDate(Integer branchId, Integer directoryId, Date viewDate) {
        List<FileHistory> fileHistoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_BRANCH_AND_DIRECTORY_ID_AND_VIEW_DATE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, directoryId);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(viewDate.getTime()));
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer fileId = resultSet.getInt(1);
                String fileName = resultSet.getString(2);
                Date insertDate = resultSet.getTimestamp(3);
                Date updateDate = resultSet.getTimestamp(4);
                Boolean deletedFlag = resultSet.getBoolean(5);
                // </editor-fold>

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
            LOGGER.log(Level.SEVERE, "FileHistoryDAOImpl: SQL exception in findByBranchAndDirectoryIdAndViewDate", e);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "FileHistoryDAOImpl: exception in findByBranchAndDirectoryIdAndViewDate", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
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
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_BRANCH_AND_FILE_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, fileId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer directoryId = resultSet.getInt(1);
                String fileName = resultSet.getString(2);
                Date insertDate = resultSet.getTimestamp(3);
                Date updateDate = resultSet.getTimestamp(4);
                Boolean deletedFlag = resultSet.getBoolean(5);
                // </editor-fold>

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
            LOGGER.log(Level.SEVERE, "FileHistoryDAOImpl: exception in findHistoryForFileId", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return fileHistoryList;
    }

    private void closeDbResources(ResultSet resultSet, PreparedStatement preparedStatement) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "FileHistoryDAOImpl: exception closing resultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "FileHistoryDAOImpl: exception closing preparedStatment", e);
            }
        }
    }
}
