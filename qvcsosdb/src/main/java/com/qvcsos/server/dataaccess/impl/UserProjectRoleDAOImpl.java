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
import com.qvcsos.server.dataaccess.UserProjectRoleDAO;
import com.qvcsos.server.datamodel.UserProjectRole;
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
public class UserProjectRoleDAOImpl implements UserProjectRoleDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserProjectRoleDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int USER_ID_RESULT_SET_INDEX = 2;
    private static final int PROJECT_ID_RESULT_SET_INDEX = 3;
    private static final int ROLE_TYPE_ID_RESULT_SET_INDEX = 4;

    private final String schemaName;

    private final String findByUserAndProject;
    private final String findByProject;
    private final String findByUserProjectAndRoleType;
    private final String insert;
    private final String delete;

    public UserProjectRoleDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, USER_ID, PROJECT_ID, ROLE_TYPE_ID FROM ";

        this.findByUserAndProject = selectSegment + this.schemaName + ".USER_PROJECT_ROLE WHERE USER_ID = ? AND PROJECT_ID = ?";
        this.findByProject = selectSegment + this.schemaName + ".USER_PROJECT_ROLE WHERE PROJECT_ID = ?";
        this.findByUserProjectAndRoleType = selectSegment + this.schemaName + ".USER_PROJECT_ROLE WHERE USER_ID = ? AND PROJECT_ID = ? AND ROLE_TYPE_ID = ?";

        this.insert = "INSERT INTO " + this.schemaName
                + ".USER_PROJECT_ROLE (USER_ID, PROJECT_ID, ROLE_TYPE_ID) VALUES (?, ?, ?) RETURNING ID";
        this.delete = "DELETE FROM " + this.schemaName + ".USER_PROJECT_ROLE WHERE ID = ?";
    }

    @Override
    public List<UserProjectRole> findByUserAndProject(Integer userId, Integer projectId) {
        List<UserProjectRole> userProjectList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByUserAndProject, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, projectId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedUserId = rs.getInt(USER_ID_RESULT_SET_INDEX);
                Integer fetchedProjectId = rs.getInt(PROJECT_ID_RESULT_SET_INDEX);
                Integer fetchedRoleTypeId = rs.getInt(ROLE_TYPE_ID_RESULT_SET_INDEX);

                UserProjectRole userProjectRole = new UserProjectRole();
                userProjectRole.setId(id);
                userProjectRole.setUserId(fetchedUserId);
                userProjectRole.setProjectId(fetchedProjectId);
                userProjectRole.setRoleTypeId(fetchedRoleTypeId);
                userProjectList.add(userProjectRole);
            }
        } catch (SQLException e) {
            LOGGER.error("UserProjectRoleDAOImpl: SQL exception in findByUserAndProject", e);
        } catch (IllegalStateException e) {
            LOGGER.error("UserProjectRoleDAOImpl: exception in findByUserAndProject", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return userProjectList;
    }

    @Override
    public List<UserProjectRole> findByProject(Integer projectId) {
        List<UserProjectRole> userProjectList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByProject, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, projectId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedUserId = rs.getInt(USER_ID_RESULT_SET_INDEX);
                Integer fetchedProjectId = rs.getInt(PROJECT_ID_RESULT_SET_INDEX);
                Integer fetchedRoleTypeId = rs.getInt(ROLE_TYPE_ID_RESULT_SET_INDEX);

                UserProjectRole userProjectRole = new UserProjectRole();
                userProjectRole.setId(id);
                userProjectRole.setUserId(fetchedUserId);
                userProjectRole.setProjectId(fetchedProjectId);
                userProjectRole.setRoleTypeId(fetchedRoleTypeId);
                userProjectList.add(userProjectRole);
            }
        } catch (SQLException e) {
            LOGGER.error("UserProjectRoleDAOImpl: SQL exception in findByUserAndProject", e);
        } catch (IllegalStateException e) {
            LOGGER.error("UserProjectRoleDAOImpl: exception in findByUserAndProject", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return userProjectList;
    }

    @Override
    public UserProjectRole findByUserProjectAndRoleType(Integer userId, Integer projectId, Integer roleTypeId) {
        UserProjectRole userProjectRole = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByUserProjectAndRoleType, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, userId);
            preparedStatement.setInt(2, projectId);
            preparedStatement.setInt(3, roleTypeId);
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedUserId = rs.getInt(USER_ID_RESULT_SET_INDEX);
                Integer fetchedProjectId = rs.getInt(PROJECT_ID_RESULT_SET_INDEX);
                Integer fetchedRoleTypeId = rs.getInt(ROLE_TYPE_ID_RESULT_SET_INDEX);

                userProjectRole = new UserProjectRole();
                userProjectRole.setId(id);
                userProjectRole.setUserId(fetchedUserId);
                userProjectRole.setProjectId(fetchedProjectId);
                userProjectRole.setRoleTypeId(fetchedRoleTypeId);
            }
        } catch (SQLException e) {
            LOGGER.error("UserProjectRoleDAOImpl: SQL exception in findByUserProjectAndRoleType", e);
        } catch (IllegalStateException e) {
            LOGGER.error("UserProjectRoleDAOImpl: exception in findByUserProjectAndRoleType", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return userProjectRole;
    }

    @Override
    public Integer insert(UserProjectRole userProjectRole) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            preparedStatement.setInt(1, userProjectRole.getUserId());
            preparedStatement.setInt(2, userProjectRole.getProjectId());
            preparedStatement.setInt(3, userProjectRole.getRoleTypeId());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("UserProjectRoleDAOImpl: exception in insert", e);
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
            LOGGER.error("UserProjectRoleDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

}
