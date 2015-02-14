//   Copyright 2004-2015 Jim Voris
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
import com.qumasoft.server.dataaccess.DirectoryDAO;
import com.qumasoft.server.datamodel.Directory;
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
 * Directory DAO implementation.
 *
 * @author Jim Voris
 */
public class DirectoryDAOImpl implements DirectoryDAO {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryDAOImpl.class);
    /*
     * + "DIRECTORY_ID INT NOT NULL," + "ROOT_DIRECTORY_ID INT NOT NULL," + "PARENT_DIRECTORY_ID INT," + "BRANCH_ID INT NOT NULL," +
     * "APPENDED_PATH VARCHAR(2048) NOT NULL," + "INSERT_DATE TIMESTAMP NOT NULL," + "UPDATE_DATE TIMESTAMP NOT NULL," +
     * "DELETED_FLAG BOOLEAN NOT NULL,"
     */
    private static final String FIND_BY_ID =
            "SELECT ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.DIRECTORY WHERE BRANCH_ID = ? "
            + "AND DIRECTORY_ID = ? AND DELETED_FLAG = false";
    private static final String FIND_BY_APPENDED_PATH =
            "SELECT DIRECTORY_ID, ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.DIRECTORY WHERE BRANCH_ID = ? AND APPENDED_PATH = ?";
    private static final String FIND_BY_BRANCH_ID =
            "SELECT DIRECTORY_ID, ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.DIRECTORY WHERE BRANCH_ID = ?";
    private static final String FIND_CHILD_DIRECTORIES =
            "SELECT DIRECTORY_ID, ROOT_DIRECTORY_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.DIRECTORY WHERE BRANCH_ID = ? AND PARENT_DIRECTORY_ID = ?";
    private static final String FIND_CHILD_DIRECTORIES_ON_OR_BEFORE_VIEW_DATE =
            "SELECT DIRECTORY_ID, ROOT_DIRECTORY_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.DIRECTORY WHERE BRANCH_ID = ? AND PARENT_DIRECTORY_ID = ? "
            + "AND UPDATE_DATE <= ?";
    private static final String INSERT_DIRECTORY =
            "INSERT INTO QVCSE.DIRECTORY (DIRECTORY_ID, ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, BRANCH_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG) "
            + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";
    private static final String UPDATE_DIRECTORY =
            "UPDATE QVCSE.DIRECTORY SET APPENDED_PATH = ?, ROOT_DIRECTORY_ID = ?, PARENT_DIRECTORY_ID = ?, UPDATE_DATE = CURRENT_TIMESTAMP, DELETED_FLAG = ? "
            + "WHERE DIRECTORY_ID = ? AND BRANCH_ID = ? "
            + "AND DELETED_FLAG = ?";

    /**
     * Find directory by directory ID.
     *
     * @param branchId the branch id.
     * @param directoryId the directory id.
     * @return the directory if found; null if not found.
     */
    @Override
    public Directory findById(Integer branchId, Integer directoryId) {
        Directory directory = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, directoryId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // <editor-fold>
                Integer rootDirectoryId = resultSet.getInt(1);
                Integer parentDirectoryId = resultSet.getInt(2);
                String appendedPath = resultSet.getString(3);
                Date insertDate = resultSet.getTimestamp(4);
                Date updateDate = resultSet.getTimestamp(5);
                Boolean deletedFlag = resultSet.getBoolean(6);
                // </editor-fold>

                directory = new Directory();
                directory.setDirectoryId(directoryId);
                directory.setRootDirectoryId(rootDirectoryId);
                directory.setParentDirectoryId(parentDirectoryId);
                directory.setBranchId(branchId);
                directory.setAppendedPath(appendedPath);
                directory.setInsertDate(insertDate);
                directory.setUpdateDate(updateDate);
                directory.setDeletedFlag(deletedFlag);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findById", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return directory;
    }

    /**
     * Find directory by branch id and appended path.
     *
     * @param branchId the branch id.
     * @param appendedPath the appended path.
     * @return the directory if found; null if not found.
     */
    @Override
    public Directory findByAppendedPath(Integer branchId, String appendedPath) {
        Directory directory = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_APPENDED_PATH, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setString(2, appendedPath);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // <editor-fold>
                Integer directoryId = resultSet.getInt(1);
                Integer rootDirectoryId = resultSet.getInt(2);
                Integer parentDirectoryId = resultSet.getInt(3);
                Date insertDate = resultSet.getTimestamp(4);
                Date updateDate = resultSet.getTimestamp(5);
                Boolean deletedFlag = resultSet.getBoolean(6);
                // </editor-fold>

                directory = new Directory();
                directory.setDirectoryId(directoryId);
                directory.setRootDirectoryId(rootDirectoryId);
                directory.setParentDirectoryId(parentDirectoryId);
                directory.setBranchId(branchId);
                directory.setAppendedPath(appendedPath);
                directory.setInsertDate(insertDate);
                directory.setUpdateDate(updateDate);
                directory.setDeletedFlag(deletedFlag);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findByAppendedPath", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return directory;
    }

    /**
     * Find the list of directories on a given branch.
     *
     * @param branchId the id of the branch we're going to look on.
     * @return a list of directories on the given branch.
     */
    @Override
    public List<Directory> findByBranchId(Integer branchId) {
        List<Directory> directoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_BRANCH_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer directoryId = resultSet.getInt(1);
                Integer rootDirectoryId = resultSet.getInt(2);
                Integer parentDirectoryId = resultSet.getInt(3);
                String appendedPath = resultSet.getString(4);
                Date insertDate = resultSet.getTimestamp(5);
                Date updateDate = resultSet.getTimestamp(6);
                Boolean deleteFlag = resultSet.getBoolean(7);
                // </editor-fold>

                Directory directory = new Directory();
                directory.setDirectoryId(directoryId);
                directory.setRootDirectoryId(rootDirectoryId);
                directory.setBranchId(branchId);
                directory.setParentDirectoryId(parentDirectoryId);
                directory.setAppendedPath(appendedPath);
                directory.setInsertDate(insertDate);
                directory.setUpdateDate(updateDate);
                directory.setDeletedFlag(deleteFlag);

                directoryList.add(directory);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findByBranchId", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return directoryList;
    }

    /**
     * Find the list of directories that have the given directory as their parent.
     *
     * @param branchId the id of the branch where we are looking.
     * @param parentDirectoryId the parent directory id.
     * @return a list of directories that are children of the given directory.
     */
    @Override
    public List<Directory> findChildDirectories(Integer branchId, Integer parentDirectoryId) {
        List<Directory> directoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_CHILD_DIRECTORIES, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, parentDirectoryId);

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

                Directory directory = new Directory();
                directory.setDirectoryId(directoryId);
                directory.setRootDirectoryId(rootDirectoryId);
                directory.setBranchId(branchId);
                directory.setParentDirectoryId(parentDirectoryId);
                directory.setAppendedPath(appendedPath);
                directory.setInsertDate(insertDate);
                directory.setUpdateDate(updateDate);
                directory.setDeletedFlag(deleteFlag);

                directoryList.add(directory);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findChildDirectories", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return directoryList;
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
    public List<Directory> findChildDirectoriesOnOrBeforeViewDate(Integer branchId, Integer parentDirectoryId, Date viewDate) {
        List<Directory> directoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_CHILD_DIRECTORIES_ON_OR_BEFORE_VIEW_DATE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, parentDirectoryId);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(viewDate.getTime()));

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

                Directory directory = new Directory();
                directory.setDirectoryId(directoryId);
                directory.setRootDirectoryId(rootDirectoryId);
                directory.setBranchId(branchId);
                directory.setParentDirectoryId(parentDirectoryId);
                directory.setAppendedPath(appendedPath);
                directory.setInsertDate(insertDate);
                directory.setUpdateDate(updateDate);
                directory.setDeletedFlag(deleteFlag);

                directoryList.add(directory);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findChildDirectoriesOnOrBeforeViewDate", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return directoryList;
    }

    /**
     * Insert a row in the DIRECTORY table.
     *
     * @param directory the directory to create.
     *
     * @throws SQLException thrown if there is a problem.
     */
    @Override
    public void insert(Directory directory) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(INSERT_DIRECTORY);
            // <editor-fold>
            preparedStatement.setInt(1, directory.getDirectoryId());
            preparedStatement.setInt(2, directory.getRootDirectoryId());
            if (directory.getParentDirectoryId() != null) {
                preparedStatement.setInt(3, directory.getParentDirectoryId());
            } else {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            }
            preparedStatement.setInt(4, directory.getBranchId());
            preparedStatement.setString(5, directory.getAppendedPath());
            preparedStatement.setBoolean(6, directory.isDeletedFlag());
            // </editor-fold>

            preparedStatement.executeUpdate();
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in insert", e);
            LOGGER.error("Directory insert object:\n" + directory.toString());
            throw e;
        } finally {
            closeDbResources(null, preparedStatement);
        }
    }

    /**
     * Update a row in the DIRECTORY table.
     *
     * @param directory the directory to update.
     * @param deletedFlag the current state of the deleted flag.
     *
     * @throws SQLException thrown if there is a problem.
     */
    @Override
    public void update(Directory directory, boolean deletedFlag) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(UPDATE_DIRECTORY);
            // <editor-fold>
            preparedStatement.setString(1, directory.getAppendedPath());
            preparedStatement.setInt(2, directory.getRootDirectoryId());
            preparedStatement.setInt(3, directory.getParentDirectoryId());
            preparedStatement.setBoolean(4, directory.isDeletedFlag());
            preparedStatement.setInt(5, directory.getDirectoryId());
            preparedStatement.setInt(6, directory.getBranchId());
            preparedStatement.setBoolean(7, deletedFlag);
            // </editor-fold>

            preparedStatement.executeUpdate();
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in update", e);
            LOGGER.error("Directory update object:\n" + directory.toString());
            throw e;
        } finally {
            closeDbResources(null, preparedStatement);
        }
    }

    private void closeDbResources(ResultSet resultSet, PreparedStatement preparedStatement) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.error("DirectoryDAOImpl: exception closing resultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.error("DirectoryDAOImpl: exception closing preparedStatment", e);
            }
        }
    }
}
