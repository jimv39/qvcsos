/*   Copyright 2004-2021 Jim Voris
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

import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.dataaccess.BranchDAO;
import com.qumasoft.server.datamodel.Branch;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Branch DAO implementation.
 *
 * @author Jim Voris
 */
public class BranchDAOImpl implements BranchDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BranchDAOImpl.class);

    private static final int BRANCH_ID_RESULT_SET_INDEX = 1;
    private static final int PROJECT_ID_RESULT_SET_INDEX = 2;
    private static final int BRANCH_NAME_RESULT_SET_INDEX = 3;
    private static final int BRANCH_TYPE_ID_RESULT_SET_INDEX = 4;
    private static final int INSERT_DATE_RESULT_SET_INDEX = 5;

    private String schemaName;
    private String findAll;
    private String findById;
    private String findByProjectIdAndBranchName;
    private String insertBranch;
    private String deleteBranch;

    public BranchDAOImpl() {
        this("qvcse");
    }

    public BranchDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT BRANCH_ID, PROJECT_ID, BRANCH_NAME, BRANCH_TYPE_ID, INSERT_DATE FROM ";

        this.findAll = selectSegment + this.schemaName + ".BRANCH ORDER BY BRANCH_ID";
        this.findById = selectSegment + this.schemaName + ".BRANCH WHERE BRANCH_ID = ?";
        this.findByProjectIdAndBranchName = selectSegment + this.schemaName + ".BRANCH WHERE PROJECT_ID = ? AND BRANCH_NAME = ?";
        this.insertBranch = "INSERT INTO " + this.schemaName + ".BRANCH (BRANCH_NAME, BRANCH_TYPE_ID, PROJECT_ID, INSERT_DATE) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
        this.deleteBranch = "DELETE FROM " + this.schemaName + ".BRANCH WHERE BRANCH_ID = ?";
    }

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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer branchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer projectId = resultSet.getInt(PROJECT_ID_RESULT_SET_INDEX);
                String branchName = resultSet.getString(BRANCH_NAME_RESULT_SET_INDEX);
                Integer branchTypeId = resultSet.getInt(BRANCH_TYPE_ID_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);

                Branch branch = new Branch();
                branch.setBranchId(branchId);
                branch.setProjectId(projectId);
                branch.setBranchName(branchName);
                branch.setBranchTypeId(branchTypeId);
                branch.setInsertDate(insertDate);

                branchList.add(branch);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("BranchTypeDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer projectId = resultSet.getInt(PROJECT_ID_RESULT_SET_INDEX);
                String branchName = resultSet.getString(BRANCH_NAME_RESULT_SET_INDEX);
                Integer branchTypeId = resultSet.getInt(BRANCH_TYPE_ID_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);

                branch = new Branch();
                branch.setBranchId(branchId);
                branch.setProjectId(projectId);
                branch.setBranchName(branchName);
                branch.setBranchTypeId(branchTypeId);
                branch.setInsertDate(insertDate);
            }
        } catch (SQLException e) {
            LOGGER.error("BranchDAOImp: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("BranchDAOImp: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.findByProjectIdAndBranchName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, projectId);
            preparedStatement.setString(2, branchName);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer branchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer branchTypeId = resultSet.getInt(BRANCH_TYPE_ID_RESULT_SET_INDEX);
                Date insertDate = resultSet.getTimestamp(INSERT_DATE_RESULT_SET_INDEX);

                branch = new Branch();
                branch.setBranchId(branchId);
                branch.setProjectId(projectId);
                branch.setBranchName(branchName);
                branch.setBranchTypeId(branchTypeId);
                branch.setInsertDate(insertDate);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("BranchDAOImp: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
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
            Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
            preparedStatement = connection.prepareStatement(this.insertBranch);
            // <editor-fold>
            preparedStatement.setString(1, branch.getBranchName());
            preparedStatement.setInt(2, branch.getBranchTypeId());
            preparedStatement.setInt(3, branch.getProjectId());
            // </editor-fold>

            preparedStatement.executeUpdate();
        } catch (IllegalStateException e) {
            LOGGER.error("BranchDAOImp: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
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
                Connection connection = QVCSEnterpriseServer.getDatabaseManager().getConnection();
                preparedStatement = connection.prepareStatement(this.deleteBranch);
                preparedStatement.setInt(1, branch.getBranchId());

                preparedStatement.executeUpdate();
            } catch (IllegalStateException e) {
                LOGGER.error("BranchDAOImp: exception in delete", e);
            } finally {
                DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
            }
        }
    }
}
