//   Copyright 2004-2015 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.server.dataaccess.impl;

import com.qumasoft.server.DatabaseManager;
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
    /*
     * + "PROJECT_ID INT GENERATED ALWAYS AS IDENTITY CONSTRAINT PROJECT_PK PRIMARY KEY," + "PROJECT_NAME VARCHAR(256) NOT NULL," +
     * "INSERT_DATE TIMESTAMP NOT NULL)";
     */

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectDAOImpl.class);
    private static final String FIND_BY_ID =
            "SELECT PROJECT_NAME, INSERT_DATE FROM QVCSE.PROJECT WHERE PROJECT_ID = ?";
    private static final String FIND_BY_PROJECT_NAME =
            "SELECT PROJECT_ID, INSERT_DATE FROM QVCSE.PROJECT WHERE PROJECT_NAME = ?";
    private static final String FIND_ALL =
            "SELECT PROJECT_ID, PROJECT_NAME, INSERT_DATE FROM QVCSE.PROJECT ORDER BY PROJECT_ID";
    private static final String INSERT_PROJECT =
            "INSERT INTO QVCSE.PROJECT (PROJECT_NAME, INSERT_DATE) VALUES (?, CURRENT_TIMESTAMP)";
    private static final String DELETE_PROJECT =
            "DELETE FROM QVCSE.PROJECT WHERE PROJECT_ID = ?";

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
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, projectId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String projectName = resultSet.getString(1);
                Date insertDate = resultSet.getTimestamp(2);

                project = new Project();
                project.setProjectId(projectId);
                project.setProjectName(projectName);
                project.setInsertDate(insertDate);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in findById", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
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
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_PROJECT_NAME, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, projectName);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer projectId = resultSet.getInt(1);
                Date insertDate = resultSet.getTimestamp(2);

                project = new Project();
                project.setProjectId(projectId);
                project.setProjectName(projectName);
                project.setInsertDate(insertDate);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in findByProjectName", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
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
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_ALL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer projectId = resultSet.getInt(1);
                String projectName = resultSet.getString(2);
                Date insertDate = resultSet.getTimestamp(3);
                // </editor-fold>

                Project project = new Project();
                project.setProjectId(projectId);
                project.setProjectName(projectName);
                project.setInsertDate(insertDate);

                projectList.add(project);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in findAll", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
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
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(INSERT_PROJECT);
            preparedStatement.setString(1, project.getProjectName());

            preparedStatement.executeUpdate();
        } catch (IllegalStateException e) {
            LOGGER.error("ProjectDAOImpl: exception in insert", e);
            throw e;
        } finally {
            closeDbResources(null, preparedStatement);
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
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(DELETE_PROJECT);
                preparedStatement.setInt(1, project.getProjectId());

                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (IllegalStateException e) {
                LOGGER.error("BranchDAOImp: exception in delete", e);
            } finally {
                closeDbResources(null, preparedStatement);
            }
        }
    }

    private void closeDbResources(ResultSet resultSet, PreparedStatement preparedStatement) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.error("ProjectDAOImpl: exception closing resultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.error("ProjectDAOImpl: exception closing preparedStatment", e);
            }
        }
    }
}
