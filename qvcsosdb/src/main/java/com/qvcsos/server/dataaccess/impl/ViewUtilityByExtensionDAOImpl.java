/*
 * Copyright 2023 Jim Voris.
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

import com.qumasoft.qvcslib.ViewUtilityFileExtensionCommandData;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.ViewUtilityByExtensionDAO;
import com.qvcsos.server.datamodel.ViewUtilityByExtension;
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
 * @author Jim Voris.
 */
public class ViewUtilityByExtensionDAOImpl implements ViewUtilityByExtensionDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewUtilityByExtensionDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int USER_AND_COMPUTER_RESULT_SET_INDEX = 2;
    private static final int FILE_EXTENSION_RESULT_SET_INDEX = 3;
    private static final int COMMAND_LINE_ID_RESULT_SET_INDEX = 4;

    private final String schemaName;

    private final String findById;
    private final String findByExtensionAndCommandLineId;
    private final String findCommandLineId;
    private final String findCommandLineExtensionList;

    private final String insert;
    private final String delete;

    public ViewUtilityByExtensionDAOImpl(String schema) {
        this.schemaName = schema;

        String selectSegment = "SELECT ID, USER_AND_COMPUTER_NAME, FILE_EXTENSION, COMMAND_LINE_ID FROM ";

        this.findById = selectSegment + this.schemaName + ".VIEW_UTILITY_BY_EXTENSION WHERE ID = ?";
        this.findByExtensionAndCommandLineId = selectSegment + this.schemaName + ".VIEW_UTILITY_BY_EXTENSION WHERE USER_AND_COMPUTER_NAME = ? AND FILE_EXTENSION = ? AND COMMAND_LINE_ID = ?";
        this.findCommandLineId = selectSegment + this.schemaName + ".VIEW_UTILITY_BY_EXTENSION WHERE USER_AND_COMPUTER_NAME = ? AND FILE_EXTENSION = ?";
        this.findCommandLineExtensionList = selectSegment + this.schemaName + ".VIEW_UTILITY_BY_EXTENSION WHERE USER_AND_COMPUTER_NAME = ?";

        this.insert = "INSERT INTO " + this.schemaName + ".VIEW_UTILITY_BY_EXTENSION (USER_AND_COMPUTER_NAME, FILE_EXTENSION, COMMAND_LINE_ID) VALUES (?, ?, ?) RETURNING ID";
        this.delete = "DELETE FROM " + this.schemaName + ".VIEW_UTILITY_BY_EXTENSION WHERE ID = ?";
    }

    @Override
    public ViewUtilityByExtension findById(Integer id) {
        ViewUtilityByExtension viewUtilityByExtension = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                String fetchedUserAndComputer = rs.getString(USER_AND_COMPUTER_RESULT_SET_INDEX);
                String fetchedFileExtension = rs.getString(FILE_EXTENSION_RESULT_SET_INDEX);
                Integer fetchedCommandLineId = rs.getInt(COMMAND_LINE_ID_RESULT_SET_INDEX);

                viewUtilityByExtension = new ViewUtilityByExtension();
                viewUtilityByExtension.setId(id);
                viewUtilityByExtension.setUserAndComputer(fetchedUserAndComputer);
                viewUtilityByExtension.setFileExtension(fetchedFileExtension);
                viewUtilityByExtension.setCommandLineId(fetchedCommandLineId);
            }
        } catch (SQLException e) {
            LOGGER.error("ViewUtilityByExtensionDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ViewUtilityByExtensionDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return viewUtilityByExtension;
    }

    @Override
    public ViewUtilityByExtension findByExtensionAndCommandLineId(String userAndComputer, String extension, Integer commandLineId) {
        ViewUtilityByExtension viewUtilityByExtension = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByExtensionAndCommandLineId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setString(1, userAndComputer);
            preparedStatement.setString(2, extension);
            preparedStatement.setInt(3, commandLineId);
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserAndComputer = rs.getString(USER_AND_COMPUTER_RESULT_SET_INDEX);
                String fetchedFileExtension = rs.getString(FILE_EXTENSION_RESULT_SET_INDEX);
                Integer fetchedCommandLineId = rs.getInt(COMMAND_LINE_ID_RESULT_SET_INDEX);

                viewUtilityByExtension = new ViewUtilityByExtension();
                viewUtilityByExtension.setId(fetchedId);
                viewUtilityByExtension.setUserAndComputer(fetchedUserAndComputer);
                viewUtilityByExtension.setFileExtension(fetchedFileExtension);
                viewUtilityByExtension.setCommandLineId(fetchedCommandLineId);
            }
        } catch (SQLException e) {
            LOGGER.error("ViewUtilityByExtensionDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ViewUtilityByExtensionDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return viewUtilityByExtension;
    }

    @Override
    public Integer findCommandLineId(String userAndComputer, String fileExtension) {
        Integer commandLineId = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findCommandLineId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userAndComputer);
            preparedStatement.setString(2, fileExtension);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserAndComputer = rs.getString(USER_AND_COMPUTER_RESULT_SET_INDEX);
                String fetchedFileExtension = rs.getString(FILE_EXTENSION_RESULT_SET_INDEX);
                Integer fetchedCommandLineId = rs.getInt(COMMAND_LINE_ID_RESULT_SET_INDEX);

                ViewUtilityByExtension viewUtilityByExtension = new ViewUtilityByExtension();
                viewUtilityByExtension.setId(fetchedId);
                viewUtilityByExtension.setUserAndComputer(fetchedUserAndComputer);
                viewUtilityByExtension.setFileExtension(fetchedFileExtension);
                viewUtilityByExtension.setCommandLineId(fetchedCommandLineId);
                commandLineId = fetchedCommandLineId;
            }
        } catch (SQLException e) {
            LOGGER.error("ViewUtilityByExtensionDAOImpl: SQL exception in findCommandLineId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ViewUtilityByExtensionDAOImpl: exception in findCommandLineId", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return commandLineId;
    }

    @Override
    public List<ViewUtilityFileExtensionCommandData> findCommandLineExtensionList(String userAndComputer) {
        List<ViewUtilityFileExtensionCommandData> vufecdList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findCommandLineExtensionList, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userAndComputer);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String fetchedFileExtension = rs.getString(FILE_EXTENSION_RESULT_SET_INDEX);
                Integer fetchedCommandLineId = rs.getInt(COMMAND_LINE_ID_RESULT_SET_INDEX);

                ViewUtilityFileExtensionCommandData vufecd = new ViewUtilityFileExtensionCommandData();
                vufecd.setFileExtension(fetchedFileExtension);
                vufecd.setCommandLineId(fetchedCommandLineId);

                vufecdList.add(vufecd);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ViewUtilityCommandLineDAOImpl: exception in findCommandLinesForUserComputer", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return vufecdList;
    }

    @Override
    public Integer insert(ViewUtilityByExtension vube) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            preparedStatement.setString(1, vube.getUserAndComputer());
            preparedStatement.setString(2, vube.getFileExtension());
            preparedStatement.setInt(3, vube.getCommandLineId());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("ViewUtilityByExtensionDAOImpl: exception in insert", e);
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
            LOGGER.error("ViewUtilityByExtensionDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

}
