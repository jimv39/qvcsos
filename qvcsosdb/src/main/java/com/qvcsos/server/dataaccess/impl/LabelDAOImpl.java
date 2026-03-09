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
import com.qvcsos.server.dataaccess.LabelDAO;
import com.qvcsos.server.dataaccess.LabelFileJoinDAO;
import com.qvcsos.server.datamodel.Label;
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
public class LabelDAOImpl implements LabelDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(LabelDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int LABEL_TEXT_RESULT_SET_INDEX = 2;

    private final String schemaName;
    private final String insert;
    private final String delete;
    private final String findById;
    private final String findByLabelText;
    private final String findAll;

    public LabelDAOImpl(String schema) {
        this.schemaName = schema;

        String selectSegment = "SELECT ID, LABEL FROM ";

        this.findById = selectSegment + this.schemaName + ".LABEL WHERE ID = ?";
        this.findByLabelText = selectSegment + this.schemaName + ".LABEL WHERE LABEL = ?";
        this.findAll = selectSegment + this.schemaName + ".LABEL ORDER BY ID";
        this.insert = "INSERT INTO " + this.schemaName + ".LABEL (LABEL) VALUES (?) RETURNING ID";
        this.delete = "DELETE FROM " + this.schemaName + ".LABEL WHERE ID = ?";
    }

    @Override
    public Label findById(Integer id) {
        Label label = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedLabelText = rs.getString(LABEL_TEXT_RESULT_SET_INDEX);

                label = new Label();
                label.setLabelId(fetchedId);
                label.setLabelText(fetchedLabelText);
            }
        } catch (SQLException e) {
            LOGGER.error("LabelDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("LabelDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return label;
    }

    @Override
    public List<Label> findAll() {
        List<Label> labelList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedLabelText = rs.getString(LABEL_TEXT_RESULT_SET_INDEX);

                Label label = new Label();
                label.setLabelId(fetchedId);
                label.setLabelText(fetchedLabelText);

                labelList.add(label);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("LabelDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return labelList;
    }

    @Override
    public Label findByLabelText(String text) {
        Label label = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByLabelText, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, text);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedLabelText = rs.getString(LABEL_TEXT_RESULT_SET_INDEX);

                label = new Label();
                label.setLabelId(fetchedId);
                label.setLabelText(fetchedLabelText);
            }
        } catch (SQLException e) {
            LOGGER.error("LabelDAOImpl: SQL exception in findByLabelText", e);
        } catch (IllegalStateException e) {
            LOGGER.error("LabelDAOImpl: exception in findByLabelText", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return label;
    }

    @Override
    public Integer insert(Label label) throws SQLException {
        Integer returnId = null;
        Label existingLabel = findByLabelText(label.getLabelText());
        if (existingLabel != null) {
            returnId = existingLabel.getLabelId();
            LOGGER.info("Label already exists: [{}]", label.getLabelText());
        } else {
            PreparedStatement preparedStatement = null;
            ResultSet rs = null;
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(this.insert);
                preparedStatement.setString(1, label.getLabelText());

                rs = preparedStatement.executeQuery();
                if (rs.next()) {
                    returnId = rs.getInt(1);
                }
            } catch (IllegalStateException e) {
                LOGGER.error("LabelDAOImpl: exception in insert", e);
                throw e;
            } finally {
                DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
            }
        }
        return returnId;
    }

    @Override
    public boolean delete(Label label) throws SQLException {
        boolean successFlag = false;
        LabelFileJoinDAO labelFileJoinDAO = new LabelFileJoinDAOImpl(schemaName);
        if (labelFileJoinDAO.deleteByLabelId(label.getLabelId())) {
            PreparedStatement preparedStatement = null;
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(this.delete);
                preparedStatement.setInt(1, label.getLabelId());

                preparedStatement.execute();
                successFlag = true;
            } catch (SQLException | IllegalStateException e) {
                LOGGER.error("FilterCollectionDAOImpl: exception in delete", e);
                throw e;
            } finally {
                DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
            }
        }
        return successFlag;
    }
}
