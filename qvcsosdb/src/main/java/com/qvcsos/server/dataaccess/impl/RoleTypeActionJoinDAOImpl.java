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
import com.qvcsos.server.dataaccess.RoleTypeActionJoinDAO;
import com.qvcsos.server.datamodel.RoleTypeActionJoin;
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
public class RoleTypeActionJoinDAOImpl implements RoleTypeActionJoinDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleTypeActionJoinDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int ROLE_TYPE_ID_RESULT_SET_INDEX = 2;
    private static final int ACTION_ID_RESULT_SET_INDEX = 3;
    private static final int ACTION_ENABLED_FLAG_RESULT_SET_INDEX = 4;

    private final String schemaName;

    private final String findByRoleType;
    private final String insert;
    private final String update;

    public RoleTypeActionJoinDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, ROLE_TYPE_ID, ACTION_ID, ACTION_ENABLED_FLAG FROM ";

        this.findByRoleType = selectSegment + this.schemaName + ".ROLE_TYPE_ACTION_JOIN WHERE ROLE_TYPE_ID = ?";

        this.insert = "INSERT INTO " + this.schemaName
                + ".ROLE_TYPE_ACTION_JOIN (ROLE_TYPE_ID, ACTION_ID, ACTION_ENABLED_FLAG) VALUES (?, ?, ?) RETURNING ID";
        this.update = "UPDATE " + this.schemaName + ".ROLE_TYPE_ACTION_JOIN SET ACTION_ENABLED_FLAG = ? WHERE ID = ?";
    }

    @Override
    public List<RoleTypeActionJoin> findByRoleType(Integer roleTypeId) {
        List<RoleTypeActionJoin> roleTypeActionJoinList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByRoleType, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, roleTypeId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedRoleTypeId = rs.getInt(ROLE_TYPE_ID_RESULT_SET_INDEX);
                Integer fetchedActionId = rs.getInt(ACTION_ID_RESULT_SET_INDEX);
                Boolean fetchedActionEnabledFlag = rs.getBoolean(ACTION_ENABLED_FLAG_RESULT_SET_INDEX);

                RoleTypeActionJoin userProjectRole = new RoleTypeActionJoin();
                userProjectRole.setId(id);
                userProjectRole.setRoleTypeId(fetchedRoleTypeId);
                userProjectRole.setActionId(fetchedActionId);
                userProjectRole.setActionEnabledFlag(fetchedActionEnabledFlag);
                roleTypeActionJoinList.add(userProjectRole);
            }
        } catch (SQLException e) {
            LOGGER.error("RoleTypeActionJoinDAOImpl: SQL exception in findByRoleType", e);
        } catch (IllegalStateException e) {
            LOGGER.error("RoleTypeActionJoinDAOImpl: exception in findByRoleType", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return roleTypeActionJoinList;
    }

    @Override
    public Integer insert(RoleTypeActionJoin newRow) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            preparedStatement.setInt(1, newRow.getRoleTypeId());
            preparedStatement.setInt(2, newRow.getActionId());
            preparedStatement.setBoolean(3, newRow.getActionEnabledFlag());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("RoleTypeActionJoinDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public boolean update(RoleTypeActionJoin row) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.update);
            // <editor-fold>
            preparedStatement.setBoolean(1, row.getActionEnabledFlag());
            preparedStatement.setInt(2, row.getId());
            // </editor-fold>

            returnFlag = preparedStatement.execute();
        } catch (IllegalStateException e) {
            LOGGER.error("RoleTypeActionJoinDAOImpl: exception in update", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

}
