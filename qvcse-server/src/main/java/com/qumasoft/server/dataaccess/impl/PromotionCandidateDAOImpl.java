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
import com.qumasoft.server.dataaccess.PromotionCandidateDAO;
import com.qumasoft.server.datamodel.PromotionCandidate;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Promotion candidate DAO implementation.
 * @author Jim Voris
 */
public class PromotionCandidateDAOImpl implements PromotionCandidateDAO {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(PromotionCandidateDAOImpl.class);
    private static final String FIND_COUNT_BY_FILE_ID_AND_BRANCH_ID =
            "SELECT COUNT(*) FROM QVCSE.PROMOTION_CANDIDATE WHERE FILE_ID = ? AND BRANCH_ID = ?";
    private static final String INSERT_PROMOTION_CANDIDATE =
            "INSERT INTO QVCSE.PROMOTION_CANDIDATE (FILE_ID, BRANCH_ID, INSERT_DATE) VALUES (?, ?, CURRENT_TIMESTAMP)";
    private static final String DELETE_PROMOTION_CANDIDATE =
            "DELETE FROM QVCSE.PROMOTION_CANDIDATE WHERE FILE_ID = ? AND BRANCH_ID = ?";

    /**
     * Insert a promotion candidate into the database if it doesn't already exist in the database.
     *
     * @param promotionCandidate the promotion candidate object.
     * @throws SQLException if there is a database problem.
     */
    @Override
    public void insertIfMissing(PromotionCandidate promotionCandidate) throws SQLException {
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement insertPreparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_COUNT_BY_FILE_ID_AND_BRANCH_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, promotionCandidate.getFileId());
            preparedStatement.setInt(2, promotionCandidate.getBranchId());

            resultSet = preparedStatement.executeQuery();
            int count = 0;
            if (resultSet.next()) {
                count = resultSet.getInt(1);
            }
            if (count == 0) {
                insertPreparedStatement = connection.prepareStatement(INSERT_PROMOTION_CANDIDATE);
                insertPreparedStatement.setInt(1, promotionCandidate.getFileId());
                insertPreparedStatement.setInt(2, promotionCandidate.getBranchId());

                insertPreparedStatement.executeUpdate();
            }
        } catch (SQLException e) {
            LOGGER.error("PromotionCandidateDAOImpl: SQL exception in insertIfMissing", e);
        } catch (IllegalStateException e) {
            LOGGER.error("PromotionCandidateDAOImpl: illegal state exception in insertIfMissing", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
            if (insertPreparedStatement != null) {
                insertPreparedStatement.close();
            }
        }
    }

    /**
     * Delete the given promotionCandidate object.
     *
     * @param promotionCandidate the promotionCandidate object to delete.
     * @throws SQLException thrown if there is a problem.
     */
    @Override
    public void delete(PromotionCandidate promotionCandidate) throws SQLException {
        PreparedStatement preparedStatement = null;
        if ((promotionCandidate.getFileId() != null) && (promotionCandidate.getBranchId() != null)) {
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(DELETE_PROMOTION_CANDIDATE);
                preparedStatement.setInt(1, promotionCandidate.getFileId());
                preparedStatement.setInt(2, promotionCandidate.getBranchId());

                preparedStatement.executeUpdate();
            } catch (IllegalStateException e) {
                LOGGER.error("PromotionCandidateDAOImpl: illegal state exception in delete", e);
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
                LOGGER.error("PromotionCandidateDAOImpl: exception closing resultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.error("PromotionCandidateDAOImpl: exception closing preparedStatment", e);
            }
        }
    }
}
