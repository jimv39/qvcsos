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
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.datamodel.Commit;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class CommitDAOImpl implements CommitDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(CommitDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int USER_ID_RESULT_SET_INDEX = 2;
    private static final int COMMIT_DATE_RESULT_SET_INDEX = 3;
    private static final int COMMIT_MESSAGE_RESULT_SET_INDEX = 4;

    private final String schemaName;
    private final String findById;
    private final String getCommitList;
    private final String insertCommit;
    private final String updateCommitMessage;

    public CommitDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, USER_ID, COMMIT_DATE, COMMIT_MESSAGE FROM ";

        this.findById = selectSegment + this.schemaName + ".COMIT WHERE ID = ?";
        this.getCommitList = "SELECT DISTINCT C.ID, C.USER_ID, C.COMMIT_DATE, C.COMMIT_MESSAGE FROM " + this.schemaName + ".COMIT C, " + this.schemaName
                + ".FILE_REVISION FR WHERE C.ID > ? AND FR.BRANCH_ID IN (%s) AND FR.COMMIT_ID = C.ID ORDER BY C.ID LIMIT 200";

        this.insertCommit = "INSERT INTO " + this.schemaName + ".COMIT (commit_message, user_id, commit_date) VALUES (?, ?, CURRENT_TIMESTAMP) RETURNING ID";
        this.updateCommitMessage = "UPDATE " + this.schemaName + ".COMIT SET commit_message = ? WHERE ID = ? RETURNING ID";
    }

    @Override
    public Commit findById(Integer id) {
        Commit commit = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedUserId = resultSet.getInt(USER_ID_RESULT_SET_INDEX);
                Timestamp fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                String fetchedCommitMessage = resultSet.getString(COMMIT_MESSAGE_RESULT_SET_INDEX);

                commit = new Commit();
                commit.setId(fetchedId);
                commit.setUserId(fetchedUserId);
                commit.setCommitDate(fetchedCommitDate);
                commit.setCommitMessage(fetchedCommitMessage);
            }
        } catch (SQLException e) {
            LOGGER.error("CommitDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("CommitDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return commit;
    }

    @Override
    public List<Commit> getCommitList(Integer commitId, String branchesToSearch) {
        List<Commit> commitList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            String queryString = String.format(this.getCommitList, branchesToSearch);
            LOGGER.debug("CommitDAO.getCommitList query: [{}]", queryString);
            LOGGER.debug("CommitDAO.getCommitList commitId: [{}]", commitId);
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, commitId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedUserId = resultSet.getInt(USER_ID_RESULT_SET_INDEX);
                Timestamp fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                String fetchedCommitMessage = resultSet.getString(COMMIT_MESSAGE_RESULT_SET_INDEX);

                Commit commit = new Commit();
                commit.setId(fetchedId);
                commit.setUserId(fetchedUserId);
                commit.setCommitDate(fetchedCommitDate);
                commit.setCommitMessage(fetchedCommitMessage);
                commitList.add(commit);
            }
        } catch (SQLException e) {
            LOGGER.error("CommitDAOImpl: SQL exception in getCommitList", e);
        } catch (IllegalStateException e) {
            LOGGER.error("CommitDAOImpl: exception in getCommitList", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return commitList;
    }

    @Override
    public Integer insert(Commit commit) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insertCommit);
            preparedStatement.setString(1, commit.getCommitMessage());
            preparedStatement.setInt(2, commit.getUserId());
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("CommitDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public Integer updateCommitMessage(Integer commitId, String commitMessage) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.updateCommitMessage);
            preparedStatement.setString(1, commitMessage);
            preparedStatement.setInt(2, commitId);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("CommitDAOImpl: exception in updateCommitMessage", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

}
