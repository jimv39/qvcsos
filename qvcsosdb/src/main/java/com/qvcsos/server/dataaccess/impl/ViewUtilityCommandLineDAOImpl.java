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

import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.ViewUtilityCommandLineDAO;
import com.qvcsos.server.datamodel.ViewUtilityCommandLine;
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
public class ViewUtilityCommandLineDAOImpl implements ViewUtilityCommandLineDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewUtilityCommandLineDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int USER_AND_COMPUTER_RESULT_SET_INDEX = 2;
    private static final int COMMAND_LINE_RESULT_SET_INDEX = 3;

    private final String schemaName;

    private final String findById;
    private final String findCommandLinesForUserComputer;
    private final String findByCommandLine;

    private final String insert;
    private final String delete;

    public ViewUtilityCommandLineDAOImpl(String schema) {
        this.schemaName = schema;

        String selectSegment = "SELECT ID, USER_AND_COMPUTER_NAME, COMMAND_LINE FROM ";

        this.findById = selectSegment + this.schemaName + ".VIEW_UTILITY_COMMAND_LINE WHERE ID = ?";
        this.findCommandLinesForUserComputer = selectSegment + this.schemaName + ".VIEW_UTILITY_COMMAND_LINE WHERE USER_AND_COMPUTER_NAME = ? ";
        this.findByCommandLine = selectSegment + this.schemaName + ".VIEW_UTILITY_COMMAND_LINE WHERE USER_AND_COMPUTER_NAME = ? AND COMMAND_LINE = ? ";

        this.insert = "INSERT INTO " + this.schemaName + ".VIEW_UTILITY_COMMAND_LINE (USER_AND_COMPUTER_NAME, COMMAND_LINE) VALUES (?, ?) RETURNING ID";
        this.delete = "DELETE FROM " + this.schemaName + ".VIEW_UTILITY_COMMAND_LINE WHERE ID = ?";
    }

    @Override
    public ViewUtilityCommandLine findById(Integer id) {
        ViewUtilityCommandLine viewUtilityCommandLine = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                String fetchedUserAndComputer = rs.getString(USER_AND_COMPUTER_RESULT_SET_INDEX);
                String fetchedCommandLine = rs.getString(COMMAND_LINE_RESULT_SET_INDEX);

                viewUtilityCommandLine = new ViewUtilityCommandLine();
                viewUtilityCommandLine.setId(id);
                viewUtilityCommandLine.setUserAndComputer(fetchedUserAndComputer);
                viewUtilityCommandLine.setCommandLine(fetchedCommandLine);
            }
        } catch (SQLException e) {
            LOGGER.error("ViewUtilityCommandLineDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ViewUtilityCommandLineDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return viewUtilityCommandLine;
    }

    @Override
    public List<ViewUtilityCommandLine> findCommandLinesForUserComputer(String userAndComputer) {
        List<ViewUtilityCommandLine> commandList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findCommandLinesForUserComputer, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userAndComputer);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserAndComputer = rs.getString(USER_AND_COMPUTER_RESULT_SET_INDEX);
                String fetchedCommandLine = rs.getString(COMMAND_LINE_RESULT_SET_INDEX);

                ViewUtilityCommandLine viewUtilityCommandLine = new ViewUtilityCommandLine();
                viewUtilityCommandLine.setId(fetchedId);
                viewUtilityCommandLine.setUserAndComputer(fetchedUserAndComputer);
                viewUtilityCommandLine.setCommandLine(fetchedCommandLine);

                commandList.add(viewUtilityCommandLine);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("ViewUtilityCommandLineDAOImpl: exception in findCommandLinesForUserComputer", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return commandList;
    }

    @Override
    public ViewUtilityCommandLine findByCommandLine(String userAndComputer, String commandLine) {
        ViewUtilityCommandLine viewUtilityCommandLine = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByCommandLine, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, userAndComputer);
            preparedStatement.setString(2, commandLine);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedUserAndComputer = rs.getString(USER_AND_COMPUTER_RESULT_SET_INDEX);
                String fetchedCommandLine = rs.getString(COMMAND_LINE_RESULT_SET_INDEX);

                viewUtilityCommandLine = new ViewUtilityCommandLine();
                viewUtilityCommandLine.setId(fetchedId);
                viewUtilityCommandLine.setUserAndComputer(fetchedUserAndComputer);
                viewUtilityCommandLine.setCommandLine(fetchedCommandLine);
            }
        } catch (SQLException e) {
            LOGGER.error("ViewUtilityCommandLineDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("ViewUtilityCommandLineDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return viewUtilityCommandLine;
    }

    @Override
    public Integer insert(ViewUtilityCommandLine vucl) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            preparedStatement.setString(1, vucl.getUserAndComputer());
            preparedStatement.setString(2, vucl.getCommandLine());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("ViewUtilityCommandLineDAOImpl: exception in insert", e);
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
            LOGGER.error("ViewUtilityCommandLineDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

}
