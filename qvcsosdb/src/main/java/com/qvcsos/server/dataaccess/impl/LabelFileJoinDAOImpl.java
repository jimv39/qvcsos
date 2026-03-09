/*
 * Copyright 2026 Jim Voris.
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
import com.qvcsos.server.dataaccess.LabelFileJoinDAO;
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
public class LabelFileJoinDAOImpl implements LabelFileJoinDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelFileJoinDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int FILE_ID_RESULT_SET_INDEX = 2;
    private static final int LABEL_ID_RESULT_SET_INDEX = 3;

    private final String schemaName;
    private final String findFilesByLabelId;
    private final String findFilesByLabelIdAndFileId;
    private final String insert;
    private final String deleteByLabelId;
    private final String deleteFileByLabelIdAndFileId;

    public LabelFileJoinDAOImpl(String schema) {
        this.schemaName = schema;

        String selectSegment = "SELECT ID, FILE_ID, LABEL_ID FROM ";

        this.findFilesByLabelId = selectSegment + this.schemaName + ".LABEL_FILE_JOIN WHERE LABEL_ID = ?";
        this.findFilesByLabelIdAndFileId = selectSegment + this.schemaName + ".LABEL_FILE_JOIN WHERE LABEL_ID = ? AND FILE_ID = ?";
        this.insert = "INSERT INTO " + this.schemaName + ".LABEL_FILE_JOIN (FILE_ID, LABEL_ID) VALUES (?, ?) RETURNING ID";
        this.deleteByLabelId = "DELETE FROM " + this.schemaName + ".LABEL_FILE_JOIN WHERE LABEL_ID = ?";
        this.deleteFileByLabelIdAndFileId = "DELETE FROM " + this.schemaName + ".LABEL_FILE_JOIN WHERE LABEL_ID = ? AND FILE_ID = ?";
    }

    @Override
    public List<Integer> findFilesByLabelId(Integer labelId) {
        List<Integer> fileIdList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findFilesByLabelId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, labelId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedFileId = rs.getInt(FILE_ID_RESULT_SET_INDEX);
                Integer fetchedLabelId = rs.getInt(LABEL_ID_RESULT_SET_INDEX);

                fileIdList.add(fetchedFileId);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("LabelFileJoinDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return fileIdList;
    }

    private int findFilesByLabelIdAndFileId(Integer labelId, Integer fileId) {
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        int rowIndex = -1;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findFilesByLabelIdAndFileId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, labelId);
            preparedStatement.setInt(2, fileId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                rowIndex = fetchedId;
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("LabelFileJoinDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return rowIndex;
    }

    @Override
    public Integer insert(Integer labelId, Integer fileId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            int foundIndex = findFilesByLabelIdAndFileId(labelId, fileId);
            if (foundIndex == -1) {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(this.insert);
                preparedStatement.setInt(1, fileId);
                preparedStatement.setInt(2, labelId);

                rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    returnId = rs.getInt(1);
                }
            } else {
                LOGGER.warn("Join row [{}] already exists for labelId: [{}], fileId: [{}]", foundIndex, labelId, fileId);
                returnId = foundIndex;
            }
        } catch (IllegalStateException e) {
            LOGGER.error("LabelFileJoinDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public boolean deleteByLabelId(Integer labelId) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.deleteByLabelId);
            preparedStatement.setInt(1, labelId);

            preparedStatement.execute();
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("LabelFileJoinDAOImpl: exception in deleteByLabelId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return true;
    }

    @Override
    public boolean deleteFileByLabelIdAndFileId(Integer labelId, Integer fileId) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.deleteFileByLabelIdAndFileId);
            preparedStatement.setInt(1, labelId);
            preparedStatement.setInt(2, fileId);

            preparedStatement.execute();
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("LabelFileJoinDAOImpl: exception in deleteByLabelId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return true;
    }
}
