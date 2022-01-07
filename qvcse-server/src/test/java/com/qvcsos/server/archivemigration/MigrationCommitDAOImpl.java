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
package com.qvcsos.server.archivemigration;

import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.impl.DAOHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation to allow an update to the commit date of a record in the comit
 * table.
 *
 * @author Jim Voris
 */
public class MigrationCommitDAOImpl {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationCommitDAOImpl.class);

    private final String schemaName;
    private final String updateCommitDate;

    public MigrationCommitDAOImpl(String schema) {
        this.schemaName = schema;

        this.updateCommitDate = "UPDATE " + this.schemaName + ".COMIT SET COMMIT_DATE = ? WHERE ID = ? RETURNING ID";
    }

    public Integer updateCommitDate(Integer commitId, Date commitDate) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Connection connection = null;
        Integer returnId = null;
        try {
            java.sql.Timestamp newDate = new java.sql.Timestamp(commitDate.getTime());
            connection = DatabaseManager.getInstance().getConnection();
            connection.setAutoCommit(false);
            preparedStatement = connection.prepareStatement(this.updateCommitDate);
            preparedStatement.setTimestamp(1, newDate);
            preparedStatement.setInt(2, commitId);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("CommitDAOImpl: exception in updateCommitMessage", e);
            throw e;
        }
        finally {
            if (connection != null) {
                connection.commit();
            }
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

}
