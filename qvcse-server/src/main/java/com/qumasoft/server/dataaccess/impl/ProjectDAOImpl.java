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
import com.qumasoft.server.dataaccess.ProjectDAO;
import com.qumasoft.server.datamodel.Project;
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
 * Project DAO implementation.
 *
 * @author Jim Voris
 */
public class ProjectDAOImpl implements ProjectDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDAOImpl.class);

    private static final int PROJECT_ID_RESULT_SET_INDEX = 1;
    private static final int PROJECT_NAME_RESULT_SET_INDEX = 2;
    private static final int INSERT_DATE_RESULT_SET_INDEX = 3;

    private final String schemaName;
    private final String findById;
    private final String findByProjectName;
    private final String findAll;
    private final String insertProject;
    private final String deleteProject;

    public ProjectDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT PROJECT_ID, PROJECT_NAME, INSERT_DATE FROM ";

        this.findById = selectSegment + this.schemaName + ".PROJECT WHERE PROJECT_ID = ?";
        this.findByProjectName = selectSegment + this.schemaName + ".PROJECT WHERE PROJECT_NAME = ?";
        this.findAll = selectSegment + this.schemaName + ".PROJECT ORDER BY PROJECT_ID";
        this.insertProject = "INSERT INTO " + this.schemaName + ".PROJECT (PROJECT_NAME, INSERT_DATE) VALUES (?, CURRENT_TIMESTAMP)";
        this.deleteProject = "DELETE FROM " + this.schemaName + ".PROJECT WHERE PROJECT_ID = ?";
    }

    /**
     * Find the project by project id.
     *
     * @param projectId the project id.
     * @return the project with the given id, or null if the project is not found.
     */
    @Override
    public Project findById(Integer projectId) {
        Project project = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, projectId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String projectName = resultSet.getString(PROJECT_NAME_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);

                project = new Project();
                project.setProjectId(projectId);
                project.setProjectName(projectName);
                project.setInsertDate(insertDate);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return project;
    }

    /**
     * Find the project by project name.
     *
     * @param projectName the name of the project to find.
     * @return the project with the given name, or null if the project is not found.
     */
    @Override
    public Project findByProjectName(String projectName) {
        Project project = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findByProjectName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, projectName);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer projectId = resultSet.getInt(PROJECT_ID_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);

                project = new Project();
                project.setProjectId(projectId);
                project.setProjectName(projectName);
                project.setInsertDate(insertDate);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in findByProjectName", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return project;
    }

    /**
     * Find all projects.
     *
     * @return a List of all the projects.
     */
    @Override
    public List<Project> findAll() {
        List<Project> projectList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer projectId = resultSet.getInt(PROJECT_ID_RESULT_SET_INDEX);
                String projectName = resultSet.getString(PROJECT_NAME_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);

                Project project = new Project();
                project.setProjectId(projectId);
                project.setProjectName(projectName);
                project.setInsertDate(insertDate);

                projectList.add(project);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return projectList;
    }

    /**
     * Insert a project.
     *
     * @param project the project to insert. We ignore the project id.
     * @throws SQLException if there is a problem.
     */
    @Override
    public void insert(Project project) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.insertProject);
            preparedStatement.setString(1, project.getProjectName());

            preparedStatement.executeUpdate();
        } catch (IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
    }

    /**
     * Delete a project. This does not cascade, so child objects must be deleted before deleting a project.
     *
     * @param project the project to delete.
     * @throws SQLException if there is a problem.
     */
    @Override
    public void delete(Project project) throws SQLException {
        PreparedStatement preparedStatement = null;
        if (project.getProjectId() != null) {
            try {
                Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
                preparedStatement = connection.prepareStatement(this.deleteProject);
                preparedStatement.setInt(1, project.getProjectId());

                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (IllegalStateException e) {
                LOGGER.error("BranchDAOImp: exception in delete", e);
            } finally {
                DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
            }
        }
    }
}
