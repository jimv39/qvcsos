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
import com.qvcsos.server.dataaccess.UserPropertyDAO;
import com.qvcsos.server.datamodel.UserProperty;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class UserPropertyDAOImpl implements UserPropertyDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserPropertyDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int USER_AND_COMPUTER_RESULT_SET_INDEX = 2;
    private static final int PROPERTY_NAME_RESULT_SET_INDEX = 3;
    private static final int PROPERTY_VALUE_RESULT_SET_INDEX = 4;

    private final String schemaName;

    private final String findById;
    private final String findByUserAndComputerAndPropertyName;
    private final String findUserProperties;

    private final String updateUserProperty;
    private final String insert;
    private final String delete;

    public UserPropertyDAOImpl(String schema) {
        this.schemaName = schema;

        String selectSegment = "SELECT ID, USER_AND_COMPUTER_NAME, PROPERTY_NAME, PROPERTY_VALUE FROM ";

        this.findById = selectSegment + this.schemaName + ".USER_PROPERTIES WHERE ID = ?";
        this.findByUserAndComputerAndPropertyName = selectSegment + this.schemaName + ".USER_PROPERTIES WHERE USER_AND_COMPUTER_NAME = ? AND PROPERTY_NAME = ?";
        this.findUserProperties = selectSegment + this.schemaName + ".USER_PROPERTIES WHERE USER_AND_COMPUTER_NAME = ? ORDER BY ID DESC";

        this.updateUserProperty = "UPDATE " + this.schemaName + ".USER_PROPERTIES SET PROPERTY_VALUE = ? WHERE ID = ? RETURNING ID";
        this.insert = "INSERT INTO " + this.schemaName + ".USER_PROPERTIES (USER_AND_COMPUTER_NAME, PROPERTY_NAME, PROPERTY_VALUE) VALUES (?, ?, ?) RETURNING ID";
        this.delete = "DELETE FROM " + this.schemaName + ".USER_PROPERTIES WHERE ID = ?";
    }

    @Override
    public UserProperty findById(Integer userPropertyId) {
        UserProperty userProperty = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userPropertyId);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserAndComputer = rs.getString(USER_AND_COMPUTER_RESULT_SET_INDEX);
                String fetchedPropertyName = rs.getString(PROPERTY_NAME_RESULT_SET_INDEX);
                String fetchedPropertyValue = rs.getString(PROPERTY_VALUE_RESULT_SET_INDEX);

                userProperty = new UserProperty();
                userProperty.setId(id);
                userProperty.setUserAndComputer(fetchedUserAndComputer);
                userProperty.setPropertyName(fetchedPropertyName);
                userProperty.setPropertyValue(fetchedPropertyValue);
            }
        } catch (SQLException e) {
            LOGGER.error("UserPropertyDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("UserPropertyDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return userProperty;
    }

    @Override
    public UserProperty findByUserAndComputerAndPropertyName(String userAndComputer, String propertyName) {
        UserProperty userProperty = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByUserAndComputerAndPropertyName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userAndComputer);
            preparedStatement.setString(2, propertyName);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserAndComputer = rs.getString(USER_AND_COMPUTER_RESULT_SET_INDEX);
                String fetchedPropertyName = rs.getString(PROPERTY_NAME_RESULT_SET_INDEX);
                String fetchedPropertyValue = rs.getString(PROPERTY_VALUE_RESULT_SET_INDEX);

                userProperty = new UserProperty();
                userProperty.setId(id);
                userProperty.setUserAndComputer(fetchedUserAndComputer);
                userProperty.setPropertyName(fetchedPropertyName);
                userProperty.setPropertyValue(fetchedPropertyValue);
            }
        } catch (SQLException e) {
            LOGGER.error("UserPropertyDAOImpl: SQL exception in findByUserIdAndPropertyName", e);
        } catch (IllegalStateException e) {
            LOGGER.error("UserPropertyDAOImpl: exception in findByUserIdAndPropertyName", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return userProperty;
    }

    @Override
    public List<UserProperty> findUserProperties(String userAndComputer) {
        List<UserProperty> userPropertyList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findUserProperties, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userAndComputer);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserAndComputer = rs.getString(USER_AND_COMPUTER_RESULT_SET_INDEX);
                String fetchedPropertyName = rs.getString(PROPERTY_NAME_RESULT_SET_INDEX);
                String fetchedPropertyValue = rs.getString(PROPERTY_VALUE_RESULT_SET_INDEX);

                UserProperty userProperty = new UserProperty();
                userProperty.setId(id);
                userProperty.setUserAndComputer(fetchedUserAndComputer);
                userProperty.setPropertyName(fetchedPropertyName);
                userProperty.setPropertyValue(fetchedPropertyValue);

                userPropertyList.add(userProperty);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("UserPropertyDAOImpl: exception in findUserProperties", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return userPropertyList;
    }

    @Override
    public Integer updateUserProperty(UserProperty updatedUserProperty) throws SQLException {
        Integer returnedId = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.updateUserProperty);
            preparedStatement.setString(1, updatedUserProperty.getPropertyValue());
            preparedStatement.setInt(2, updatedUserProperty.getId());

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnedId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("UserPropertyDAOImpl: exception in updateUserProperty", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnedId;
    }

    @Override
    public Integer insert(UserProperty userProperty) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            preparedStatement.setString(1, userProperty.getUserAndComputer());
            preparedStatement.setString(2, userProperty.getPropertyName());
            preparedStatement.setString(3, userProperty.getPropertyValue());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("UserPropertyDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public boolean delete(Integer id) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = true;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.delete);
            preparedStatement.setInt(1, id);

            preparedStatement.execute();
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("UserPropertyDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

}
