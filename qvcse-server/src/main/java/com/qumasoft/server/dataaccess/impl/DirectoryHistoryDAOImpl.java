/*   Copyright 2004-2015 Jim Voris
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

import com.qumasoft.server.DatabaseManager;
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
    /*
     * + "ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT DIRECTORY_HISTORY_PK PRIMARY KEY," + "DIRECTORY_ID INT NOT NULL," +
     * "ROOT_DIRECTORY_ID INT NOT NULL," + "PARENT_DIRECTORY_ID INT," + "BRANCH_ID INT NOT NULL," + "APPENDED_PATH VARCHAR(2048) NOT
     * NULL," + "INSERT_DATE TIMESTAMP NOT NULL," + "UPDATE_DATE TIMESTAMP NOT NULL," + "DELETED_FLAG BOOLEAN NOT NULL)";
     */
    private static final String FIND_CHILD_DIRECTORIES_ON_OR_BEFORE_VIEW_DATE =
            "SELECT DIRECTORY_ID, ROOT_DIRECTORY_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.DIRECTORY_HISTORY WHERE BRANCH_ID = ? AND "
            + "PARENT_DIRECTORY_ID = ? AND UPDATE_DATE <= ? "
            + "ORDER BY DIRECTORY_ID ASC, UPDATE_DATE DESC";

    @Override
    public DirectoryHistory findById(Integer directoryHistoryId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Find the list of directories that have the given directory as their parent.
     *
     * @param branchId the id of the branch we're going to look on.
     * @param parentDirectoryId the parent directory id.
     * @param viewDate the date for the date based view.
     * @return a list of directories that are children of the given directory updated on or before the given date.
     */
    @Override
    public List<DirectoryHistory> findChildDirectoriesOnOrBeforeViewDate(Integer branchId, Integer parentDirectoryId, Date viewDate) {
        List<DirectoryHistory> directoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_CHILD_DIRECTORIES_ON_OR_BEFORE_VIEW_DATE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, parentDirectoryId);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(viewDate.getTime()));
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer directoryId = resultSet.getInt(1);
                Integer rootDirectoryId = resultSet.getInt(2);
                String appendedPath = resultSet.getString(3);
                Date insertDate = resultSet.getTimestamp(4);
                Date updateDate = resultSet.getTimestamp(5);
                Boolean deleteFlag = resultSet.getBoolean(6);
                // </editor-fold>

                DirectoryHistory directoryHistory = new DirectoryHistory();
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
            LOGGER.error("DirectoryHistoryDAOImpl: exception in findChildDirectoriesOnOrBeforeViewDate", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return directoryList;
    }

    @Override
    public List<DirectoryHistory> findHistoryForDirectoryId(Integer directoryId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void closeDbResources(ResultSet resultSet, PreparedStatement preparedStatement) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.error("DirectoryHistoryDAOImpl: exception closing resultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.error("DirectoryHistoryDAOImpl: exception closing preparedStatment", e);
            }
        }
    }
}
