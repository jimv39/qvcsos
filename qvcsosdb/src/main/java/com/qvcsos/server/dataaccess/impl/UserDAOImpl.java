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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.UserDAO;
import com.qvcsos.server.datamodel.User;
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
 * @author Jim Voris
 */
public class UserDAOImpl implements UserDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int USER_NAME_RESULT_SET_INDEX = 2;
    private static final int PASSWORD_RESULT_SET_INDEX = 3;
    private static final int DELETED_FLAG_RESULT_SET_INDEX = 4;

    private final String schemaName;
    private final String findById;
    private final String findAll;
    private final String findByUserName;
    private final String insertUser;
    private final String deleteUser;
    private final String updateUserPassword;

    public UserDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, USER_NAME, PASSWORD, DELETED_FLAG FROM ";

        this.findById = selectSegment + this.schemaName + ".USER WHERE ID = ?";
        this.findAll = selectSegment + this.schemaName + ".USER ORDER BY USER_NAME";
        this.findByUserName = selectSegment + this.schemaName + ".USER WHERE USER_NAME = ?";

        this.insertUser = "INSERT INTO " + this.schemaName + ".USER (USER_NAME, PASSWORD, DELETED_FLAG) VALUES (?, ?, ?) RETURNING ID";
        this.deleteUser = "UPDATE " + this.schemaName + ".USER SET DELETED_FLAG = ? WHERE ID = ? RETURNING ID";
        this.updateUserPassword = "UPDATE " + this.schemaName + ".USER SET PASSWORD = ? WHERE ID = ? RETURNING ID";
    }

    @Override
    public User findById(Integer userId) {
        User user = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userId);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserName = rs.getString(USER_NAME_RESULT_SET_INDEX);
                byte[] fetchedPassword = rs.getBytes(PASSWORD_RESULT_SET_INDEX);
                Boolean deleteFlag = rs.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                user = new User();
                user.setId(id);
                user.setUserName(fetchedUserName);
                user.setPassword(fetchedPassword);
                user.setDeletedFlag(deleteFlag);
            }
        } catch (SQLException e) {
            LOGGER.error("UserDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("UserDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return user;
    }

    @Override
    public User findByUserName(String userName) {
        User user = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByUserName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userName);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserName = rs.getString(USER_NAME_RESULT_SET_INDEX);
                byte[] fetchedPassword = rs.getBytes(PASSWORD_RESULT_SET_INDEX);
                Boolean deleteFlag = rs.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                user = new User();
                user.setId(id);
                user.setUserName(fetchedUserName);
                user.setPassword(fetchedPassword);
                user.setDeletedFlag(deleteFlag);
            }
        } catch (SQLException e) {
            LOGGER.error("UserDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("UserDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return user;
    }

    @Override
    public List<User> findAll() {
        List<User> userList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserName = resultSet.getString(USER_NAME_RESULT_SET_INDEX);
                byte[] fetchedPassword = resultSet.getBytes(PASSWORD_RESULT_SET_INDEX);
                Boolean fetchedDeleteFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                User user = new User();
                user.setId(fetchedId);
                user.setUserName(fetchedUserName);
                user.setPassword(fetchedPassword);
                user.setDeletedFlag(fetchedDeleteFlag);

                userList.add(user);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("UserDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return userList;
    }

    @Override
    public Integer insert(User user) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insertUser);
            // <editor-fold>
            preparedStatement.setString(1, user.getUserName());
            preparedStatement.setBytes(2, user.getPassword());
            preparedStatement.setBoolean(3, user.getDeletedFlag());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("UserDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public boolean delete(User user) {
        boolean retVal = true;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        if (user.getUserName().equals(QVCSConstants.QVCS_ADMIN_USER)) {
            LOGGER.warn("UserDAOImpl: Attempt to remove ADMIN is not allowed.");
            retVal = false;
        } else {
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(this.deleteUser);
                // <editor-fold>
                preparedStatement.setBoolean(1, Boolean.TRUE);
                preparedStatement.setInt(2, user.getId());
                // </editor-fold>

                rs = preparedStatement.executeQuery();
            } catch (IllegalStateException | SQLException  e) {
                LOGGER.error("UserDAOImpl: exception in delete", e);
                retVal = false;
            } finally {
                DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
            }
        }
        return retVal;
    }

    @Override
    public boolean updateUserPassword(Integer id, byte[] newHashedPassword) {
        ResultSet rs = null;
        boolean retVal = true;

        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.updateUserPassword);
            // <editor-fold>
            preparedStatement.setBytes(1, newHashedPassword);
            preparedStatement.setInt(2, id);
            // </editor-fold>

            rs = preparedStatement.executeQuery();
        } catch (IllegalStateException | SQLException  e) {
            LOGGER.error("UserDAOImpl: exception in updateUserPassword", e);
            retVal = false;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return retVal;
    }

}
