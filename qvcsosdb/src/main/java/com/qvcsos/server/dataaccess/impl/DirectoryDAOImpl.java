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
import com.qvcsos.server.dataaccess.DirectoryDAO;
import com.qvcsos.server.datamodel.Directory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class DirectoryDAOImpl implements DirectoryDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int PROJECT_ID_RESULT_SET_INDEX = 2;

    private final String schemaName;
    private final String insertDirectory;
    private final String findById;

    public DirectoryDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, PROJECT_ID FROM ";

        this.insertDirectory = "INSERT INTO " + this.schemaName + ".DIRECTORY (PROJECT_ID) VALUES (?) RETURNING ID";
        this.findById = selectSegment + this.schemaName + ".DIRECTORY WHERE ID = ?";
    }

    @Override
    public Directory findById(Integer id) {
        Directory directory = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer directoryId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer projectId = resultSet.getInt(PROJECT_ID_RESULT_SET_INDEX);

                directory = new Directory();
                directory.setId(directoryId);
                directory.setProjectId(projectId);
            }
        } catch (SQLException e) {
            LOGGER.error("DirectoryDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directory;
    }

    @Override
    public Integer insert(Directory directory) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.insertDirectory);
            preparedStatement.setInt(1, directory.getProjectId());
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

}
