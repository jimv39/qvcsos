/*
 * Copyright 2023 Jim Voris.
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
import com.qvcsos.server.dataaccess.ProvisionalDirectoryLocationDAO;
import com.qvcsos.server.datamodel.ProvisionalDirectoryLocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class ProvisionalDirectoryLocationDAOImpl implements ProvisionalDirectoryLocationDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProvisionalDirectoryLocationDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int DIRECTORY_ID_RESULT_SET_INDEX = 2;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 3;
    private static final int PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX = 4;
    private static final int PROVISIONAL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX = 5;
    private static final int USER_ID_RESULT_SET_INDEX = 6;
    private static final int DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX = 7;
    private static final int APPENDED_PATH_RESULT_SET_INDEX = 8;

    private final String schemaName;
    private final String findById;
    private final String findByParentDirectoryLocationId;
    private final String findChildProvisionalDirectoryLocation;
    private final String findByUserIdAndAppendedPath;

    private final String insert;
    private final String deleteAll;

    public ProvisionalDirectoryLocationDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, DIRECTORY_ID, BRANCH_ID, PARENT_DIRECTORY_LOCATION_ID, PROVISIONAL_PARENT_DIRECTORY_LOCATION_ID, USER_ID, DIRECTORY_SEGMENT_NAME, APPENDED_PATH FROM ";

        findById = selectSegment + this.schemaName + ".PROVISIONAL_DIRECTORY_LOCATION WHERE ID = ?";
        findByParentDirectoryLocationId = selectSegment + this.schemaName + ".PROVISIONAL_DIRECTORY_LOCATION WHERE PARENT_DIRECTORY_LOCATION_ID = ? AND DIRECTORY_SEGMENT_NAME = ?";
        findChildProvisionalDirectoryLocation = selectSegment + this.schemaName + ".PROVISIONAL_DIRECTORY_LOCATION WHERE PROVISIONAL_PARENT_DIRECTORY_LOCATION_ID = ? AND DIRECTORY_SEGMENT_NAME = ?";
        findByUserIdAndAppendedPath = selectSegment + this.schemaName + ".PROVISIONAL_DIRECTORY_LOCATION WHERE USER_ID = ? AND APPENDED_PATH = ?";

        insert = "INSERT INTO " + this.schemaName
                + ".PROVISIONAL_DIRECTORY_LOCATION (DIRECTORY_ID, BRANCH_ID, PARENT_DIRECTORY_LOCATION_ID, PROVISIONAL_PARENT_DIRECTORY_LOCATION_ID, USER_ID, DIRECTORY_SEGMENT_NAME, APPENDED_PATH) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING ID";
        deleteAll = "DELETE FROM " + this.schemaName + ".PROVISIONAL_DIRECTORY_LOCATION WHERE USER_ID = ?";
    }

    @Override
    public ProvisionalDirectoryLocation findById(Integer id) {
        ProvisionalDirectoryLocation provisionalDirectoryLocation = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                provisionalDirectoryLocation = getProvisionalDirectoryLocationFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return provisionalDirectoryLocation;
    }

    @Override
    public ProvisionalDirectoryLocation findByParentDirectoryLocationId(Integer directoryLocationId, String directorySegmentName) {
        ProvisionalDirectoryLocation provisionalDirectoryLocation = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByParentDirectoryLocationId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, directoryLocationId);
            preparedStatement.setString(2, directorySegmentName);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                provisionalDirectoryLocation = getProvisionalDirectoryLocationFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: SQL exception in findByParentDirectoryLocationId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: exception in findByParentDirectoryLocationId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return provisionalDirectoryLocation;
    }

    @Override
    public ProvisionalDirectoryLocation findChildProvisionalDirectoryLocation(Integer branchId, Integer parentProvisionalDirectoryLocationId, String directorySegmentName) {
        ProvisionalDirectoryLocation provisionalDirectoryLocation = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findChildProvisionalDirectoryLocation, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, parentProvisionalDirectoryLocationId);
            preparedStatement.setString(2, directorySegmentName);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                provisionalDirectoryLocation = getProvisionalDirectoryLocationFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: SQL exception in findChildProvisionalDirectoryLocation", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: exception in findChildProvisionalDirectoryLocation", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return provisionalDirectoryLocation;
    }

    @Override
    public ProvisionalDirectoryLocation findByUserIdAndAppendedPath(Integer userId, String appendedPath) {
        ProvisionalDirectoryLocation provisionalDirectoryLocation = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByUserIdAndAppendedPath, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userId);
            preparedStatement.setString(2, appendedPath);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                provisionalDirectoryLocation = getProvisionalDirectoryLocationFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: SQL exception in findByUserIdAndAppendedPath", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: exception in findByUserIdAndAppendedPath", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return provisionalDirectoryLocation;
    }

    @Override
    public Integer insert(ProvisionalDirectoryLocation pdLocation) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            if (pdLocation.getDirectoryId() != null) {
                preparedStatement.setInt(1, pdLocation.getDirectoryId());
            } else {
                preparedStatement.setNull(1, java.sql.Types.INTEGER);
            }
            preparedStatement.setInt(2, pdLocation.getBranchId());
            if (pdLocation.getParentDirectoryLocationId() != null) {
                preparedStatement.setInt(3, pdLocation.getParentDirectoryLocationId());
            } else {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            }
            if (pdLocation.getParentProvisionalDirectoryLocationId() != null) {
                preparedStatement.setInt(4, pdLocation.getParentProvisionalDirectoryLocationId());
            } else {
                preparedStatement.setNull(4, java.sql.Types.INTEGER);
            }
            preparedStatement.setInt(5, pdLocation.getUserId());
            preparedStatement.setString(6, pdLocation.getDirectorySegmentName());
            preparedStatement.setString(7, pdLocation.getAppendedPath());
            // </editor-fold>
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public void deleteAll(Integer userId) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.deleteAll);
            preparedStatement.setInt(1, userId);

            preparedStatement.execute();
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ProvisionalDirectoryLocationDAOImpl: exception in deleteAll", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
    }

    private ProvisionalDirectoryLocation getProvisionalDirectoryLocationFromResultSet(ResultSet resultSet) throws SQLException {
        ProvisionalDirectoryLocation provisionalDirectoryLocation = null;
        Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);

        Integer fetchedProvisionalDirectoryId = null;
        Object fetchedProvisionalDirectoryIdObject = resultSet.getObject(DIRECTORY_ID_RESULT_SET_INDEX);
        if (fetchedProvisionalDirectoryIdObject != null) {
            fetchedProvisionalDirectoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
        }

        Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);

        Integer fetchedParentDirectoryLocationId = null;
        Object fetchedParentDirectoryLocationObject = resultSet.getObject(PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
        if (fetchedParentDirectoryLocationObject != null) {
            fetchedParentDirectoryLocationId = resultSet.getInt(PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
        }

        Integer fetchedProvisionalParentDirectoryLocation = null;
        Object fetchedProvisionalParentDirectoryLocationObject = resultSet.getObject(PROVISIONAL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
        if (fetchedProvisionalParentDirectoryLocationObject != null) {
            fetchedProvisionalParentDirectoryLocation = resultSet.getInt(PROVISIONAL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
        }

        Integer fetchedUserId = resultSet.getInt(USER_ID_RESULT_SET_INDEX);
        String fetchedDirectorySegmentName = resultSet.getString(DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX);
        String fetchedAppendedPath = resultSet.getString(APPENDED_PATH_RESULT_SET_INDEX);

        provisionalDirectoryLocation = new ProvisionalDirectoryLocation();
        provisionalDirectoryLocation.setId(fetchedId);
        provisionalDirectoryLocation.setDirectoryId(fetchedProvisionalDirectoryId);
        provisionalDirectoryLocation.setBranchId(fetchedBranchId);
        provisionalDirectoryLocation.setParentDirectoryLocationId(fetchedParentDirectoryLocationId);
        provisionalDirectoryLocation.setParentProvisionalDirectoryLocationId(fetchedProvisionalParentDirectoryLocation);
        provisionalDirectoryLocation.setUserId(fetchedUserId);
        provisionalDirectoryLocation.setDirectorySegmentName(fetchedDirectorySegmentName);
        provisionalDirectoryLocation.setAppendedPath(fetchedAppendedPath);
        return provisionalDirectoryLocation;
    }
}
