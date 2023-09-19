/*
 * Copyright 2021-2023 Jim Voris.
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
import com.qvcsos.server.dataaccess.DirectoryLocationDAO;
import com.qvcsos.server.datamodel.DirectoryLocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class DirectoryLocationDAOImpl implements DirectoryLocationDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryLocationDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int DIRECTORY_ID_RESULT_SET_INDEX = 2;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 3;
    private static final int PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX = 4;
    private static final int CREATED_FOR_REASON_RESULT_SET_INDEX = 5;
    private static final int COMMIT_ID_RESULT_SET_INDEX = 6;
    private static final int DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX = 7;
    private static final int DELETED_FLAG_RESULT_SET_INDEX = 8;

    private final String schemaName;
    private final String findById;
    private final String findByDirectoryId;
    private final String findChildDirectoryLocation;
    private final String findByBranchIdAndDirectoryId;
    private final String promoteToParentBranch;

    private final String insertDirectoryLocation;
    private final String deleteDirectory;
    private final String moveDirectory;
    private final String renameDirectory;

    public DirectoryLocationDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, DIRECTORY_ID, BRANCH_ID, PARENT_DIRECTORY_LOCATION_ID, CREATED_FOR_REASON, COMMIT_ID, DIRECTORY_SEGMENT_NAME, DELETED_FLAG FROM ";

        this.findById = selectSegment + this.schemaName + ".DIRECTORY_LOCATION WHERE ID = ?";
        this.findByDirectoryId = selectSegment + this.schemaName + ".DIRECTORY_LOCATION WHERE DIRECTORY_ID = ?";
        this.findChildDirectoryLocation = selectSegment + this.schemaName + ".DIRECTORY_LOCATION WHERE BRANCH_ID = ? AND PARENT_DIRECTORY_LOCATION_ID = ? AND DIRECTORY_SEGMENT_NAME = ?";
        this.findByBranchIdAndDirectoryId = selectSegment + this.schemaName + ".DIRECTORY_LOCATION WHERE BRANCH_ID = ? AND DIRECTORY_ID = ?";

        this.insertDirectoryLocation = "INSERT INTO " + this.schemaName
                + ".DIRECTORY_LOCATION (DIRECTORY_ID, BRANCH_ID, PARENT_DIRECTORY_LOCATION_ID, CREATED_FOR_REASON, COMMIT_ID, DIRECTORY_SEGMENT_NAME, DELETED_FLAG) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING ID";
        this.deleteDirectory = "UPDATE " + this.schemaName + ".DIRECTORY_LOCATION SET DELETED_FLAG = TRUE, COMMIT_ID = ? WHERE ID = ?";
        this.moveDirectory = "UPDATE " + this.schemaName + ".DIRECTORY_LOCATION SET PARENT_DIRECTORY_LOCATION_ID = ?, COMMIT_ID = ? WHERE ID = ?";
        this.renameDirectory = "UPDATE " + this.schemaName + ".DIRECTORY_LOCATION SET DIRECTORY_SEGMENT_NAME = ?, COMMIT_ID = ? WHERE ID = ?";
        this.promoteToParentBranch = "UPDATE " + this.schemaName + ".DIRECTORY_LOCATION SET BRANCH_ID = ?, COMMIT_ID = ? WHERE ID = ?";
    }

    @Override
    public DirectoryLocation findById(Integer id) {
        DirectoryLocation directoryLocation = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                directoryLocation = getDirectoryLocationFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("DirectoryLocationDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryLocationDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directoryLocation;
    }

    @Override
    public DirectoryLocation findByDirectoryId(Integer dirId) {
        DirectoryLocation directoryLocation = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByDirectoryId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, dirId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                directoryLocation = getDirectoryLocationFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("DirectoryLocationDAOImpl: SQL exception in findByDirectoryId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryLocationDAOImpl: exception in findByDirectoryId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directoryLocation;
    }

    @Override
    public DirectoryLocation findChildDirectoryLocation(Integer branchId, Integer parentDirectoryLocationId, String segment) {
        DirectoryLocation directoryLocation = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findChildDirectoryLocation, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, parentDirectoryLocationId);
            preparedStatement.setString(3, segment);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                directoryLocation = getDirectoryLocationFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("DirectoryLocationDAOImpl: SQL exception in findChildDirectoryLocation", e);
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryLocationDAOImpl: exception in findChildDirectoryLocation", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directoryLocation;
    }

    @Override
    public DirectoryLocation findByBranchIdAndDirectoryId(Integer promotedFromBranchId, Integer directoryId) {
        DirectoryLocation directoryLocation = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByBranchIdAndDirectoryId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, promotedFromBranchId);
            preparedStatement.setInt(2, directoryId);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                directoryLocation = getDirectoryLocationFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("DirectoryLocationDAOImpl: SQL exception in findByBranchIdAndDirectoryId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryLocationDAOImpl: exception in findByBranchIdAndDirectoryId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directoryLocation;
    }

    @Override
    public Integer insert(DirectoryLocation directoryLocation) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insertDirectoryLocation);
            // <editor-fold>
            preparedStatement.setInt(1, directoryLocation.getDirectoryId());
            preparedStatement.setInt(2, directoryLocation.getBranchId());
            if (directoryLocation.getParentDirectoryLocationId() != null) {
                preparedStatement.setInt(3, directoryLocation.getParentDirectoryLocationId());
            } else {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            }
            if (directoryLocation.getCreatedForReason() != null) {
                preparedStatement.setInt(4, directoryLocation.getCreatedForReason());
            } else {
                preparedStatement.setNull(4, java.sql.Types.INTEGER);
            }
            preparedStatement.setInt(5, directoryLocation.getCommitId());
            preparedStatement.setString(6, directoryLocation.getDirectorySegmentName());
            preparedStatement.setBoolean(7, directoryLocation.getDeletedFlag());
            // </editor-fold>
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryLocationDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public boolean delete(Integer id, Integer commitId) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.deleteDirectory);
            preparedStatement.setInt(1, commitId);
            preparedStatement.setInt(2, id);

            returnFlag = preparedStatement.execute();
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryLocationDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

    @Override
    public boolean move(Integer id, Integer commitId, Integer targetParentDirectoryLocationId) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.moveDirectory);
            preparedStatement.setInt(1, targetParentDirectoryLocationId);
            preparedStatement.setInt(2, commitId);
            preparedStatement.setInt(3, id);

            returnFlag = preparedStatement.execute();
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryLocationDAOImpl: exception in move", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

    @Override
    public boolean rename(Integer id, Integer commitId, String newDirectoryName) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.renameDirectory);
            // <editor-fold>
            preparedStatement.setString(1, newDirectoryName);
            preparedStatement.setInt(2, commitId);
            preparedStatement.setInt(3, id);
            // </editor-fold>

            returnFlag = preparedStatement.execute();
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryLocationDAOImpl: exception in rename", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

    @Override
    public void promoteToParentBranch(Integer directoryLocationId, Integer promotedFromBranchId, Integer promotedToBranchId, Integer commitId)  throws SQLException {
        DirectoryLocation dl = findById(directoryLocationId);
        if (dl != null && Objects.equals(dl.getBranchId(), promotedFromBranchId)) {
            PreparedStatement preparedStatement = null;
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(this.promoteToParentBranch);
                // <editor-fold>
                preparedStatement.setInt(1, promotedToBranchId);
                preparedStatement.setInt(2, commitId);
                preparedStatement.setInt(3, directoryLocationId);
                // </editor-fold>

                preparedStatement.execute();
            } catch (IllegalStateException e) {
                LOGGER.error("DirectoryLocationDAOImpl: exception in rename", e);
                throw e;
            } finally {
                DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
            }
        }
    }

    private DirectoryLocation getDirectoryLocationFromResultSet(ResultSet resultSet) throws SQLException {
        Integer directoryLocationId = resultSet.getInt(ID_RESULT_SET_INDEX);
        Integer directoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
        Integer branchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
        Object parentDirectoryLocationObject = resultSet.getObject(PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
        Integer parentDirectoryLocationId = null;
        if (parentDirectoryLocationObject != null) {
            parentDirectoryLocationId = resultSet.getInt(PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
        }
        Object createdForReasonObject = resultSet.getObject(CREATED_FOR_REASON_RESULT_SET_INDEX);
        Integer createdForReason = null;
        if (createdForReasonObject != null) {
            createdForReason = resultSet.getInt(CREATED_FOR_REASON_RESULT_SET_INDEX);
        }
        Integer commitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
        String directorySegmentName = resultSet.getString(DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX);
        Boolean deletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

        DirectoryLocation directoryLocation = new DirectoryLocation();
        directoryLocation.setId(directoryLocationId);
        directoryLocation.setDirectoryId(directoryId);
        directoryLocation.setBranchId(branchId);
        directoryLocation.setParentDirectoryLocationId(parentDirectoryLocationId);
        directoryLocation.setCreatedForReason(createdForReason);
        directoryLocation.setCommitId(commitId);
        directoryLocation.setDirectorySegmentName(directorySegmentName);
        directoryLocation.setDeletedFlag(deletedFlag);
        return directoryLocation;
    }
}
