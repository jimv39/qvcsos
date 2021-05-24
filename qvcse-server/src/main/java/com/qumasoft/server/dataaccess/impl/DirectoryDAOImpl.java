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

    private static final int DIRECTORY_ID_RESULT_SET_INDEX = 1;
    private static final int ROOT_DIRECTORY_ID_RESULT_SET_INDEX = 2;
    private static final int PARENT_DIRECTORY_ID_RESULT_SET_INDEX = 3;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 4;
    private static final int APPENDED_PATH_RESULT_SET_INDEX = 5;
    private static final int INSERT_DATE_RESULT_SET_INDEX = 6;
    private static final int UPDATE_DATE_RESULT_SET_INDEX = 7;
    private static final int DELETED_FLAG_RESULT_SET_INDEX = 8;

    private String schemaName;
    private String findById;
    private String findByAppendedPath;
    private String findByBranchId;
    private String findChildDirectories;
    private String findChildDirectoriesOnOrBeforeBranchDate;
    private String findAll;
    private String insertDirectory;
    private String updateDirectory;
    private String deleteDirectory;

    public DirectoryDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT DIRECTORY_ID, ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, BRANCH_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM ";

        this.findById = selectSegment + this.schemaName + ".DIRECTORY WHERE BRANCH_ID = ? "
            + "AND DIRECTORY_ID = ? AND DELETED_FLAG = false";
        this.findByAppendedPath = selectSegment + this.schemaName + ".DIRECTORY WHERE BRANCH_ID = ? AND APPENDED_PATH = ?";
        this.findByBranchId = selectSegment + this.schemaName + ".DIRECTORY WHERE BRANCH_ID = ?";
        this.findChildDirectories = selectSegment + this.schemaName + ".DIRECTORY WHERE BRANCH_ID = ? AND PARENT_DIRECTORY_ID = ?";
        this.findChildDirectoriesOnOrBeforeBranchDate = selectSegment + this.schemaName + ".DIRECTORY WHERE BRANCH_ID = ? AND PARENT_DIRECTORY_ID = ? AND UPDATE_DATE <= ?";
        this.findAll = selectSegment + this.schemaName + ".DIRECTORY ORDER BY DIRECTORY_ID ASC";
        this.insertDirectory = "INSERT INTO " + this.schemaName + ".DIRECTORY (DIRECTORY_ID, ROOT_DIRECTORY_ID, PARENT_DIRECTORY_ID, BRANCH_ID, APPENDED_PATH, INSERT_DATE, UPDATE_DATE, DELETED_FLAG) "
            + "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";
        this.updateDirectory = "UPDATE " + this.schemaName + ".DIRECTORY SET APPENDED_PATH = ?, ROOT_DIRECTORY_ID = ?, PARENT_DIRECTORY_ID = ?, UPDATE_DATE = CURRENT_TIMESTAMP, DELETED_FLAG = ? "
            + "WHERE DIRECTORY_ID = ? AND BRANCH_ID = ? "
            + "AND DELETED_FLAG = ?";
        this.deleteDirectory = "DELETE FROM " + this.schemaName + ".DIRECTORY WHERE DIRECTORY_ID = ?";
    }

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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, directoryId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer rootDirectoryId = resultSet.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer parentDirectoryId = resultSet.getInt(PARENT_DIRECTORY_ID_RESULT_SET_INDEX);
                String appendedPath = resultSet.getString(APPENDED_PATH_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

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
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findByAppendedPath, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setString(2, appendedPath);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer rootDirectoryId = resultSet.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer parentDirectoryId = resultSet.getInt(PARENT_DIRECTORY_ID_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

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
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findByBranchId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer rootDirectoryId = resultSet.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer parentDirectoryId = resultSet.getInt(PARENT_DIRECTORY_ID_RESULT_SET_INDEX);
                String appendedPath = resultSet.getString(APPENDED_PATH_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deleteFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

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
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findChildDirectories, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, parentDirectoryId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer rootDirectoryId = resultSet.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                String appendedPath = resultSet.getString(APPENDED_PATH_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                Directory directory = new Directory();
                directory.setDirectoryId(directoryId);
                directory.setRootDirectoryId(rootDirectoryId);
                directory.setBranchId(branchId);
                directory.setParentDirectoryId(parentDirectoryId);
                directory.setAppendedPath(appendedPath);
                directory.setInsertDate(insertDate);
                directory.setUpdateDate(updateDate);
                directory.setDeletedFlag(deletedFlag);

                directoryList.add(directory);
                LOGGER.info("\tfindChildDirectories: directoryId: [{}] parentDirectoryId: [{}] appendedPath: [{}]", directoryId, parentDirectoryId, appendedPath);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findChildDirectories", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        LOGGER.info("findChildDirectories: branchId: [{}] parentDirectoryId: [{}] childDirectoryCount: [{}]", branchId, parentDirectoryId, directoryList.size());
        return directoryList;
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
    public List<Directory> findChildDirectoriesOnOrBeforeBranchDate(Integer branchId, Integer parentDirectoryId, Date branchDate) {
        List<Directory> directoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findChildDirectoriesOnOrBeforeBranchDate, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, parentDirectoryId);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(branchDate.getTime()));
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer rootDirectoryId = resultSet.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                String appendedPath = resultSet.getString(APPENDED_PATH_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                Directory directory = new Directory();
                directory.setDirectoryId(directoryId);
                directory.setRootDirectoryId(rootDirectoryId);
                directory.setBranchId(branchId);
                directory.setParentDirectoryId(parentDirectoryId);
                directory.setAppendedPath(appendedPath);
                directory.setInsertDate(insertDate);
                directory.setUpdateDate(updateDate);
                directory.setDeletedFlag(deletedFlag);

                directoryList.add(directory);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findChildDirectoriesOnOrBeforeBranchDate", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directoryList;
    }

    @Override
    public List<Directory> findAll() {
        List<Directory> directoryList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer rootDirectoryId = resultSet.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer parentDirectoryId = resultSet.getInt(PARENT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer branchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                String appendedPath = resultSet.getString(APPENDED_PATH_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);
                Date updateDate = resultSet.getTimestamp(UPDATE_DATE_RESULT_SET_INDEX);
                Boolean deletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                Directory directory = new Directory();
                directory.setDirectoryId(directoryId);
                directory.setRootDirectoryId(rootDirectoryId);
                directory.setBranchId(branchId);
                directory.setParentDirectoryId(parentDirectoryId);
                directory.setAppendedPath(appendedPath);
                directory.setInsertDate(insertDate);
                directory.setUpdateDate(updateDate);
                directory.setDeletedFlag(deletedFlag);

                directoryList.add(directory);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findChildDirectoriesOnOrBeforeBranchDate", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.insertDirectory);
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
            LOGGER.info("Inserting directory: branchId: [{}] directoryId: [{}] rootDirectoryId[{}] parentDirectoryId: [{}] appendedPath: [{}] deletedFlag [{}]",
                    directory.getBranchId(),
                    directory.getDirectoryId(),
                    directory.getRootDirectoryId(),
                    directory.getParentDirectoryId(),
                    directory.getAppendedPath(),
                    directory.isDeletedFlag());
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in insert", e);
            LOGGER.error("Directory insert object:\n" + directory.toString());
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.updateDirectory);
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
            LOGGER.info("Updating directory: branchId: [{}] directoryId: [{}] rootDirectoryId[{}] parentDirectoryId: [{}] appendedPath: [{}] deletedFlag [{}]",
                    directory.getBranchId(),
                    directory.getDirectoryId(),
                    directory.getRootDirectoryId(),
                    directory.getParentDirectoryId(),
                    directory.getAppendedPath(),
                    deletedFlag);
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in update", e);
            LOGGER.error("Directory update object:\n" + directory.toString());
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
    }

    @Override
    public void delete(Directory directory) throws SQLException {
        PreparedStatement preparedStatement = null;
        if (directory.getDirectoryId() != null) {
            try {
                Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
                preparedStatement = connection.prepareStatement(this.deleteDirectory);
                preparedStatement.setInt(1, directory.getDirectoryId());

                preparedStatement.executeUpdate();
            } catch (IllegalStateException e) {
                LOGGER.error("DirectoryDAOImp: exception in delete", e);
            } finally {
                DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
            }
        }
    }
}
