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
import com.qumasoft.server.dataaccess.DirectoryHistoryDAO;
import com.qumasoft.server.datamodel.DirectoryHistory;
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
 * Directory History DAO implementation.
 *
 * @author Jim Voris
 */
public class DirectoryHistoryDAOImpl implements DirectoryHistoryDAO {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryHistoryDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int DIRECTORY_ID_RESULT_SET_INDEX = 2;
    private static final int ROOT_DIRECTORY_ID_RESULT_SET_INDEX = 3;
    private static final int PARENT_DIRECTORY_ID_RESULT_SET_INDEX = 4;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 5;
    private static final int APPENDED_PATH_RESULT_SET_INDEX = 6;
    private static final int INSERT_DATE_RESULT_SET_INDEX = 7;
    private static final int UPDATE_DATE_RESULT_SET_INDEX = 8;
    private static final int DELETED_FLAG_RESULT_SET_INDEX = 9;

    private String schemaName;
    private String findChildDirectoriesOnOrBeforeBranchDate;
    private String findHistoryForDirectoryId;
    private String findById;

    public DirectoryHistoryDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, DIRECTORY_ID, ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, BRANCH_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM ";

        this.findChildDirectoriesOnOrBeforeBranchDate = selectSegment
            + this.schemaName + ".DIRECTORY_HISTORY WHERE BRANCH_ID = ? AND "
            + "PARENT_DIRECTORY_ID = ? AND UPDATE_DATE <= ? "
            + "ORDER BY DIRECTORY_ID ASC, UPDATE_DATE DESC";
        this.findById = selectSegment
            + this.schemaName + ".DIRECTORY_HISTORY WHERE ID = ?";
        this.findHistoryForDirectoryId = selectSegment
            + this.schemaName + ".DIRECTORY_HISTORY WHERE DIRECTORY_ID = ? ORDER BY ID ASC";
    }

    @Override
    public DirectoryHistory findById(Integer directoryHistoryId) {
        DirectoryHistory directoryHistory = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, directoryHistoryId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer id = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer rootDirectoryId = resultSet.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer parentDirectoryId = resultSet.getInt(PARENT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer branchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                String appendedPath = resultSet.getString(APPENDED_PATH_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deleteFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                directoryHistory = new DirectoryHistory();
                directoryHistory.setId(id);
                directoryHistory.setDirectoryId(directoryId);
                directoryHistory.setRootDirectoryId(rootDirectoryId);
                directoryHistory.setParentDirectoryId(parentDirectoryId);
                directoryHistory.setBranchId(branchId);
                directoryHistory.setAppendedPath(appendedPath);
                directoryHistory.setInsertDate(insertDate);
                directoryHistory.setUpdateDate(updateDate);
                directoryHistory.setDeletedFlag(deleteFlag);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directoryHistory;
    }

    /**
     * Find the list of directories that have the given directory as their parent.
     *
     * @param branchId the id of the branch we're going to look on.
     * @param parentDirectoryId the parent directory id.
     * @param branchDate the date for the date based branch.
     * @return a list of directories that are children of the given directory updated on or before the given date.
     */
    @Override
    public List<DirectoryHistory> findChildDirectoriesOnOrBeforeBranchDate(Integer branchId, Integer parentDirectoryId, Date branchDate) {
        List<DirectoryHistory> directoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(findChildDirectoriesOnOrBeforeBranchDate, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, parentDirectoryId);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(branchDate.getTime()));
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer id = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer rootDirectoryId = resultSet.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                String appendedPath = resultSet.getString(APPENDED_PATH_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deleteFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                DirectoryHistory directoryHistory = new DirectoryHistory();
                directoryHistory.setId(id);
                directoryHistory.setDirectoryId(directoryId);
                directoryHistory.setRootDirectoryId(rootDirectoryId);
                directoryHistory.setBranchId(branchId);
                directoryHistory.setParentDirectoryId(parentDirectoryId);
                directoryHistory.setAppendedPath(appendedPath);
                directoryHistory.setInsertDate(insertDate);
                directoryHistory.setUpdateDate(updateDate);
                directoryHistory.setDeletedFlag(deleteFlag);

                directoryList.add(directoryHistory);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryHistoryDAOImpl: exception in findChildDirectoriesOnOrBeforeBranchDate", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directoryList;
    }

    @Override
    public List<DirectoryHistory> findHistoryForDirectoryId(Integer directoryId) {
        List<DirectoryHistory> directoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(findHistoryForDirectoryId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, directoryId);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer id = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer rootDirectoryId = resultSet.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer parentDirectoryId = resultSet.getInt(PARENT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer branchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                String appendedPath = resultSet.getString(APPENDED_PATH_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deleteFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                DirectoryHistory directoryHistory = new DirectoryHistory();
                directoryHistory.setId(id);
                directoryHistory.setDirectoryId(directoryId);
                directoryHistory.setRootDirectoryId(rootDirectoryId);
                directoryHistory.setBranchId(branchId);
                directoryHistory.setParentDirectoryId(parentDirectoryId);
                directoryHistory.setAppendedPath(appendedPath);
                directoryHistory.setInsertDate(insertDate);
                directoryHistory.setUpdateDate(updateDate);
                directoryHistory.setDeletedFlag(deleteFlag);

                directoryList.add(directoryHistory);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryHistoryDAOImpl: exception in findHistoryForDirectoryId", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directoryList;
    }
}
