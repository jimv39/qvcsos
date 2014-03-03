//   Copyright 2004-2014 Jim Voris
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
import com.qumasoft.server.dataaccess.BranchDAO;
import com.qumasoft.server.datamodel.Branch;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Branch DAO implementation.
 *
 * @author Jim Voris
 */
public class BranchDAOImpl implements BranchDAO {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server.DatabaseManager");
    private static final String FIND_ALL =
            "SELECT BRANCH_ID, BRANCH_NAME, BRANCH_TYPE_ID, PROJECT_ID, INSERT_DATE FROM QVCSE.BRANCH ORDER BY BRANCH_ID";
    private static final String FIND_BY_ID =
            "SELECT BRANCH_NAME, BRANCH_TYPE_ID, PROJECT_ID, INSERT_DATE FROM QVCSE.BRANCH WHERE BRANCH_ID = ?";
    private static final String FIND_BY_PROJECT_ID_AND_BRANCH_NAME =
            "SELECT BRANCH_ID, BRANCH_TYPE_ID, INSERT_DATE FROM QVCSE.BRANCH WHERE PROJECT_ID = ? AND BRANCH_NAME = ?";
    private static final String INSERT_BRANCH =
            "INSERT INTO QVCSE.BRANCH (BRANCH_NAME, BRANCH_TYPE_ID, PROJECT_ID, INSERT_DATE) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
    private static final String DELETE_BRANCH =
            "DELETE FROM QVCSE.BRANCH WHERE BRANCH_ID = ?";

    /**
     * Find all branches.
     *
     * @return a List of all the branches.
     */
    @Override
    public List<Branch> findAll() {
        List<Branch> branchList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_ALL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer branchId = resultSet.getInt(1);
                String branchName = resultSet.getString(2);
                Integer branchTypeId = resultSet.getInt(3);
                Integer projectId = resultSet.getInt(4);
                Date insertDate = resultSet.getTimestamp(5);
                // </editor-fold>

                Branch branch = new Branch();
                branch.setBranchId(branchId);
                branch.setProjectId(projectId);
                branch.setBranchName(branchName);
                branch.setBranchTypeId(branchTypeId);
                branch.setInsertDate(insertDate);

                branchList.add(branch);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "BranchTypeDAOImpl: exception in findAll", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return branchList;
    }

    /**
     * Find the branch by branch id.
     *
     * @param branchId the branch id.
     * @return the Branch with the given id, or null if the branch is not found.
     */
    @Override
    public Branch findById(Integer branchId) {
        Branch branch = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // <editor-fold>
                String branchName = resultSet.getString(1);
                Integer branchType = resultSet.getInt(2);
                Integer projectId = resultSet.getInt(3);
                Date insertDate = resultSet.getTimestamp(4);
                // </editor-fold>

                branch = new Branch();
                branch.setBranchId(branchId);
                branch.setBranchName(branchName);
                branch.setBranchTypeId(branchType);
                branch.setProjectId(projectId);
                branch.setInsertDate(insertDate);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "BranchDAOImp: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "BranchDAOImp: exception in findById", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return branch;
    }

    /**
     * Find the branch by project id and branch name.
     *
     * @param projectId the project id.
     * @param branchName the name of the branch on the given project.
     * @return the Branch in the given project with the given name, or null if the branch is not found.
     */
    @Override
    public Branch findByProjectIdAndBranchName(Integer projectId, String branchName) {
        Branch branch = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_PROJECT_ID_AND_BRANCH_NAME, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, projectId);
            preparedStatement.setString(2, branchName);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // <editor-fold>
                Integer branchId = resultSet.getInt(1);
                Integer branchType = resultSet.getInt(2);
                Date insertDate = resultSet.getTimestamp(3);
                // </editor-fold>

                branch = new Branch();
                branch.setBranchId(branchId);
                branch.setBranchName(branchName);
                branch.setBranchTypeId(branchType);
                branch.setProjectId(projectId);
                branch.setInsertDate(insertDate);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "BranchDAOImp: exception in findById", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return branch;
    }

    /**
     * Insert a row in the BRANCH table.
     *
     * @param branch the branch to create. Note that we do <b>not</b> honor any branch id passed in with the branch object. A new
     * branch will <b>always</b> be created.
     *
     * @throws SQLException thrown if there is a problem.
     */
    @Override
    public void insert(Branch branch) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(INSERT_BRANCH);
            // <editor-fold>
            preparedStatement.setString(1, branch.getBranchName());
            preparedStatement.setInt(2, branch.getBranchTypeId());
            preparedStatement.setInt(3, branch.getProjectId());
            // </editor-fold>

            preparedStatement.executeUpdate();
        } catch (IllegalStateException e) {
            LOGGER.log(Level.SEVERE, "BranchDAOImp: exception in insert", e);
            throw e;
        } finally {
            closeDbResources(null, preparedStatement);
        }
    }

    /**
     * Delete the given branch object.
     *
     * @param branch the branch object to delete.
     * @throws SQLException thrown if there is a problem.
     */
    @Override
    public void delete(Branch branch) throws SQLException {
        PreparedStatement preparedStatement = null;
        if (branch.getBranchId() != null) {
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(DELETE_BRANCH);
                preparedStatement.setInt(1, branch.getBranchId());

                preparedStatement.executeUpdate();
            } catch (IllegalStateException e) {
                LOGGER.log(Level.SEVERE, "BranchDAOImp: exception in delete", e);
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
                LOGGER.log(Level.SEVERE, "BranchDAOImpl: exception closing resultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "BranchDAOImpl: exception closing preparedStatment", e);
            }
        }
    }
}
