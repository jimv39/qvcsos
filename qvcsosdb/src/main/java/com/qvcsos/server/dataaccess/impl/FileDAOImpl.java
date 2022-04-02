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
import com.qvcsos.server.dataaccess.FileDAO;
import com.qvcsos.server.datamodel.File;
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
public class FileDAOImpl implements FileDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int PROJECT_ID_RESULT_SET_INDEX = 2;

    private final String schemaName;
    private final String findById;
    private final String insertFile;

    public FileDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, PROJECT_ID FROM ";

        this.findById = selectSegment + this.schemaName + ".FILE WHERE ID = ?";

        this.insertFile = "INSERT INTO " + this.schemaName + ".FILE (PROJECT_ID) VALUES (?) RETURNING ID";
    }

    @Override
    public File findById(Integer id) {
        File file = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fileId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer projectId = resultSet.getInt(PROJECT_ID_RESULT_SET_INDEX);

                file = new File();
                file.setId(fileId);
                file.setProjectId(projectId);
            }
        } catch (SQLException e) {
            LOGGER.error("FileDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return file;
    }

    @Override
    public Integer insert(File file) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insertFile);
            preparedStatement.setInt(1, file.getProjectId());

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

}
