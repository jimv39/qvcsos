/*   Copyright 2004-2015 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.server.dataaccess.impl;

import com.qumasoft.server.DatabaseManager;
import com.qumasoft.server.dataaccess.BranchTypeDAO;
import com.qumasoft.server.datamodel.BranchType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Branch type DAO implementation.
 *
 * @author Jim Voris
 */
public class BranchTypeDAOImpl implements BranchTypeDAO {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BranchTypeDAOImpl.class);
    private static final String FIND_BY_ID =
            "SELECT BRANCH_TYPE_ID, BRANCH_TYPE_NAME FROM QVCSE.BRANCH_TYPE WHERE BRANCH_TYPE_ID = ?";
    private static final String FIND_ALL =
            "SELECT BRANCH_TYPE_ID, BRANCH_TYPE_NAME FROM QVCSE.BRANCH_TYPE ORDER BY BRANCH_TYPE_ID";

    /**
     * Find the branch type by branch type id.
     *
     * @param branchTypeId the branch type id.
     * @return the BranchType with the given id, or null if the branchType is not found.
     */
    @Override
    public BranchType findById(Integer branchTypeId) {
        BranchType branchType = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchTypeId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String branchTypeName = resultSet.getString(2);

                branchType = new BranchType();
                branchType.setBranchTypeId(branchTypeId);
                branchType.setBranchTypeName(branchTypeName);
            }
        } catch (SQLException e) {
            LOGGER.error("BranchTypeDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("BranchTypeDAOImpl: exception in findById", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return branchType;
    }

    /**
     * Find all branch types.
     *
     * @return a List of all the branch types.
     */
    @Override
    public List<BranchType> findAll() {
        List<BranchType> branchTypeList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_ALL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer branchTypeId = resultSet.getInt(1);
                String branchTypeName = resultSet.getString(2);

                BranchType branchType = new BranchType();
                branchType.setBranchTypeId(branchTypeId);
                branchType.setBranchTypeName(branchTypeName);

                branchTypeList.add(branchType);
            }
        } catch (SQLException e) {
            LOGGER.error("BranchTypeDAOImpl: SQL exception in findAll", e);
        } catch (IllegalStateException e) {
            LOGGER.error("BranchTypeDAOImpl: exception in findAll", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return branchTypeList;
    }

    private void closeDbResources(ResultSet resultSet, PreparedStatement preparedStatement) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.error("BranchTypeDAOImpl: exception closing resultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.error("BranchTypeDAOImpl: exception closing preparedStatment", e);
            }
        }
    }
}
