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
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.datamodel.Project;
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
public class ProjectDAOImpl implements ProjectDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int PROJECT_NAME_RESULT_SET_INDEX = 2;
    private static final int COMMIT_ID_RESULT_SET_INDEX = 3;
    private static final int DELETED_FLAG_RESULT_SET_INDEX = 4;

    private final String schemaName;
    private final String findById;
    private final String findAll;
    private final String findByProjectName;
    private final String insertProject;

    public ProjectDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, PROJECT_NAME, COMMIT_ID, DELETED_FLAG FROM ";

        this.findById = selectSegment + this.schemaName + ".PROJECT WHERE ID = ?";
        this.findAll = selectSegment + this.schemaName + ".PROJECT ORDER BY PROJECT_NAME";
        this.findByProjectName = selectSegment + this.schemaName + ".PROJECT WHERE PROJECT_NAME = ?";

        this.insertProject = "INSERT INTO " + this.schemaName + ".PROJECT (PROJECT_NAME, COMMIT_ID, DELETED_FLAG) VALUES (?, ?, ?) RETURNING ID";
    }

    @Override
    public Project findById(Integer projectId) {
        Project project = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, projectId);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                String projectName = rs.getString(PROJECT_NAME_RESULT_SET_INDEX);
                Integer commitId = rs.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean deleteFlag = rs.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                project = new Project();
                project.setId(id);
                project.setProjectName(projectName);
                project.setCommitId(commitId);
                project.setDeletedFlag(deleteFlag);
            }
        } catch (SQLException e) {
            LOGGER.error("ProjectDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return project;
    }

    @Override
    public List<Project> findAll() {
        List<Project> projectList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                String fetchedProjectName = resultSet.getString(PROJECT_NAME_RESULT_SET_INDEX);
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedDeleteFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                Project project = new Project();
                project.setId(fetchedId);
                project.setProjectName(fetchedProjectName);
                project.setCommitId(fetchedCommitId);
                project.setDeletedFlag(fetchedDeleteFlag);

                projectList.add(project);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return projectList;
    }

    @Override
    public Project findByProjectName(String projectName) {
        Project project = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByProjectName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, projectName);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedProjectName = rs.getString(PROJECT_NAME_RESULT_SET_INDEX);
                Integer fetchedCommitId = rs.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedDeleteFlag = rs.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                project = new Project();
                project.setId(fetchedId);
                project.setProjectName(fetchedProjectName);
                project.setCommitId(fetchedCommitId);
                project.setDeletedFlag(fetchedDeleteFlag);
            }
        } catch (SQLException e) {
            LOGGER.error("ProjectDAOImpl: SQL exception in findByProjectName", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in findByProjectName", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return project;
    }

    @Override
    public Integer insert(Project project) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.insertProject);
            // <editor-fold>
            preparedStatement.setString(1, project.getProjectName());
            preparedStatement.setInt(2, project.getCommitId());
            preparedStatement.setBoolean(3, project.getDeletedFlag());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public void delete(Project project) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
