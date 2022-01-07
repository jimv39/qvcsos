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

import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.RoleTypeDAO;
import com.qvcsos.server.datamodel.RoleType;
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
public class RoleTypeDAOImpl implements RoleTypeDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleTypeDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int ROLE_NAME_RESULT_SET_INDEX = 2;

    private final String schemaName;

    private final String findById;
    private final String findAll;
    private final String findByRoleName;

    private final String insert;
    private final String delete;

    public RoleTypeDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, ROLE_NAME FROM ";

        this.findById = selectSegment + this.schemaName + ".ROLE_TYPE WHERE ID = ?";
        this.findAll = selectSegment + this.schemaName + ".ROLE_TYPE";
        this.findByRoleName = selectSegment + this.schemaName + ".ROLE_TYPE WHERE ROLE_NAME = ?";

        this.insert = "INSERT INTO " + this.schemaName + ".ROLE_TYPE (ROLE_NAME) VALUES (?) RETURNING ID";
        this.delete = "DELETE FROM " + this.schemaName + "ROLE_TYPE WHERE ID = ?";
    }

    @Override
    public RoleType findById(Integer id) {
        RoleType roleType = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedRoleName = rs.getString(ROLE_NAME_RESULT_SET_INDEX);

                roleType = new RoleType();
                roleType.setId(fetchedId);
                roleType.setRoleName(fetchedRoleName);
            }
        } catch (SQLException e) {
            LOGGER.error("RoleTypeDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("RoleTypeDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return roleType;
    }

    @Override
    public List<RoleType> findAll() {
        List<RoleType> roleTypeList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedRoleName = rs.getString(ROLE_NAME_RESULT_SET_INDEX);

                RoleType roleType = new RoleType();
                roleType.setId(id);
                roleType.setRoleName(fetchedRoleName);
                roleTypeList.add(roleType);
            }
        } catch (SQLException e) {
            LOGGER.error("RoleTypeDAOImpl: SQL exception in findAll", e);
        } catch (IllegalStateException e) {
            LOGGER.error("RoleTypeDAOImpl: exception in findAll", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return roleTypeList;
    }

    @Override
    public RoleType findByRoleName(String roleName) {
        RoleType roleType = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByRoleName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, roleName);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedRoleName = rs.getString(ROLE_NAME_RESULT_SET_INDEX);

                roleType = new RoleType();
                roleType.setId(id);
                roleType.setRoleName(fetchedRoleName);
            }
        } catch (SQLException e) {
            LOGGER.error("RoleTypeDAOImpl: SQL exception in findByRoleName", e);
        } catch (IllegalStateException e) {
            LOGGER.error("RoleTypeDAOImpl: exception in findByRoleName", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return roleType;
    }

    @Override
    public Integer insert(RoleType roleType) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.insert);
            preparedStatement.setString(1, roleType.getRoleName());

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("RoleTypeDAOImpl: exception in insert", e);
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
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.delete);
            preparedStatement.setInt(1, id);

            preparedStatement.execute();
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("RoleTypeDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

}
