/*
 * Copyright 2021-2022 Jim Voris.
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
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.datamodel.Branch;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class BranchDAOImpl implements BranchDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BranchDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int PARENT_BRANCH_ID_RESULT_SET_INDEX = 2;
    private static final int PROJECT_ID_RESULT_SET_INDEX = 3;
    private static final int ROOT_DIRECTORY_ID_RESULT_SET_INDEX = 4;
    private static final int COMMIT_ID_RESULT_SET_INDEX = 5;
    private static final int BRANCH_NAME_RESULT_SET_INDEX = 6;
    private static final int BRANCH_TYPE_ID_RESULT_SET_INDEX = 7;
    private static final int TAG_ID_RESULT_SET_INDEX = 8;
    private static final int DELETED_FLAG_RESULT_SET_INDEX = 9;

    private final String schemaName;
    private final String findById;
    private final String findByProjectIdAndBranchName;
    private final String findProjectBranches;

    private final String getWriteableChildBranchIdList;
    private final String getChildBranchCount;

    private final String insertBranch;
    private final String deleteBranch;

    public BranchDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, PARENT_BRANCH_ID, PROJECT_ID, ROOT_DIRECTORY_ID, COMMIT_ID, BRANCH_NAME, BRANCH_TYPE_ID, TAG_ID, DELETED_FLAG FROM ";

        this.findById = selectSegment + this.schemaName + ".BRANCH WHERE ID = ?";
        this.findByProjectIdAndBranchName = selectSegment + this.schemaName + ".BRANCH WHERE PROJECT_ID = ? AND BRANCH_NAME = ? AND DELETED_FLAG = FALSE";
        this.findProjectBranches = selectSegment + this.schemaName + ".BRANCH WHERE PROJECT_ID = ? AND DELETED_FLAG = FALSE ORDER BY BRANCH_TYPE_ID, ID";

        this.getWriteableChildBranchIdList = selectSegment + this.schemaName + ".BRANCH WHERE PARENT_BRANCH_ID = ? AND BRANCH_TYPE_ID = 2 AND DELETED_FLAG = FALSE ORDER BY ID";
        this.getChildBranchCount = "SELECT COUNT(*) FROM " + this.schemaName + ".BRANCH WHERE PROJECT_ID = ? AND PARENT_BRANCH_ID = ? AND DELETED_FLAG = FALSE";

        this.insertBranch = "INSERT INTO " + this.schemaName
                + ".BRANCH (PARENT_BRANCH_ID, PROJECT_ID, ROOT_DIRECTORY_ID, COMMIT_ID, BRANCH_NAME, BRANCH_TYPE_ID, TAG_ID, DELETED_FLAG) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING ID";
        this.deleteBranch = "UPDATE " + this.schemaName + ".BRANCH SET DELETED_FLAG = TRUE, COMMIT_ID = ? WHERE ID = ? RETURNING ID";

    }

    @Override
    public Branch findById(Integer branchId) {
        Branch branch = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer parentBranchId = null;
                Object parentBranchIdObject = rs.getObject(PARENT_BRANCH_ID_RESULT_SET_INDEX);
                if (parentBranchIdObject != null) {
                    parentBranchId = rs.getInt(PARENT_BRANCH_ID_RESULT_SET_INDEX);
                }
                Integer projectId = rs.getInt(PROJECT_ID_RESULT_SET_INDEX);
                Integer rootDirectoryId = rs.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer commitId = rs.getInt(COMMIT_ID_RESULT_SET_INDEX);
                String branchName = rs.getString(BRANCH_NAME_RESULT_SET_INDEX);
                Integer branchTypeId = rs.getInt(BRANCH_TYPE_ID_RESULT_SET_INDEX);
                Integer tagId = null;
                Object tagIdObject = rs.getObject(TAG_ID_RESULT_SET_INDEX);
                if (tagIdObject != null) {
                    tagId = rs.getInt(TAG_ID_RESULT_SET_INDEX);
                }
                Boolean deletedFlag = rs.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                branch = new Branch();
                branch.setId(id);
                branch.setParentBranchId(parentBranchId);
                branch.setProjectId(projectId);
                branch.setRootDirectoryId(rootDirectoryId);
                branch.setCommitId(commitId);
                branch.setBranchName(branchName);
                branch.setBranchTypeId(branchTypeId);
                branch.setTagId(tagId);
                branch.setDeletedFlag(deletedFlag);
            }
        } catch (SQLException e) {
            LOGGER.error("BranchDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("BranchDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return branch;
    }

    @Override
    public Branch findByProjectIdAndBranchName(Integer projectId, String branchName) {
        Branch branch = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByProjectIdAndBranchName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, projectId);
            preparedStatement.setString(2, branchName);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedParentBranchId = null;
                Object parentBranchIdObject = rs.getObject(PARENT_BRANCH_ID_RESULT_SET_INDEX);
                if (parentBranchIdObject != null) {
                    fetchedParentBranchId = rs.getInt(PARENT_BRANCH_ID_RESULT_SET_INDEX);
                }
                Integer fetchedProjectId = rs.getInt(PROJECT_ID_RESULT_SET_INDEX);
                Integer fetchedRootDirectoryId = rs.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = rs.getInt(COMMIT_ID_RESULT_SET_INDEX);
                String fetchedBranchName = rs.getString(BRANCH_NAME_RESULT_SET_INDEX);
                Integer fetchedBranchTypeId = rs.getInt(BRANCH_TYPE_ID_RESULT_SET_INDEX);
                Integer fetchedTagId = null;
                Object tagIdObject = rs.getObject(TAG_ID_RESULT_SET_INDEX);
                if (tagIdObject != null) {
                    fetchedTagId = rs.getInt(TAG_ID_RESULT_SET_INDEX);
                }
                Boolean fetchedDeletedFlag = rs.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                branch = new Branch();
                branch.setId(id);
                branch.setParentBranchId(fetchedParentBranchId);
                branch.setProjectId(fetchedProjectId);
                branch.setRootDirectoryId(fetchedRootDirectoryId);
                branch.setCommitId(fetchedCommitId);
                branch.setBranchName(fetchedBranchName);
                branch.setBranchTypeId(fetchedBranchTypeId);
                branch.setTagId(fetchedTagId);
                branch.setDeletedFlag(fetchedDeletedFlag);
            }
        } catch (SQLException e) {
            LOGGER.error("BranchDAOImpl: SQL exception in findByProjectIdAndBranchName", e);
        } catch (IllegalStateException e) {
            LOGGER.error("BranchDAOImpl: exception in findByProjectIdAndBranchName", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return branch;
    }

    @Override
    public List<Branch> findProjectBranches(Integer projectId) {
        List<Branch> branchList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findProjectBranches, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, projectId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedParentBranchId = null;
                Object parentBranchIdObject = rs.getObject(PARENT_BRANCH_ID_RESULT_SET_INDEX);
                if (parentBranchIdObject != null) {
                    fetchedParentBranchId = rs.getInt(PARENT_BRANCH_ID_RESULT_SET_INDEX);
                }
                Integer fetchedProjectId = rs.getInt(PROJECT_ID_RESULT_SET_INDEX);
                Integer fetchedRootDirectoryId = rs.getInt(ROOT_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = rs.getInt(COMMIT_ID_RESULT_SET_INDEX);
                String fetchedBranchName = rs.getString(BRANCH_NAME_RESULT_SET_INDEX);
                Integer fetchedBranchTypeId = rs.getInt(BRANCH_TYPE_ID_RESULT_SET_INDEX);
                Integer fetchedTagId = null;
                Object tagIdObject = rs.getObject(TAG_ID_RESULT_SET_INDEX);
                if (tagIdObject != null) {
                    fetchedTagId = rs.getInt(TAG_ID_RESULT_SET_INDEX);
                }
                Boolean fetchedDeletedFlag = rs.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                Branch branch = new Branch();
                branch.setId(id);
                branch.setParentBranchId(fetchedParentBranchId);
                branch.setProjectId(fetchedProjectId);
                branch.setRootDirectoryId(fetchedRootDirectoryId);
                branch.setCommitId(fetchedCommitId);
                branch.setBranchName(fetchedBranchName);
                branch.setBranchTypeId(fetchedBranchTypeId);
                branch.setTagId(fetchedTagId);
                branch.setDeletedFlag(fetchedDeletedFlag);
                branchList.add(branch);
            }
        } catch (SQLException e) {
            LOGGER.error("BranchDAOImpl: SQL exception in findProjectBranches", e);
        } catch (IllegalStateException e) {
            LOGGER.error("BranchDAOImpl: exception in findProjectBranches", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return branchList;
    }

    @Override
    public Integer getChildBranchCount(Integer projectId, Integer parentBranchId) {
        Integer childBranchCount = 0;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.getChildBranchCount, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, projectId);
            preparedStatement.setInt(2, parentBranchId);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                childBranchCount = rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("BranchDAOImpl: SQL exception in getChildBranchCount", e);
        } catch (IllegalStateException e) {
            LOGGER.error("BranchDAOImpl: exception in getChildBranchCount", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return childBranchCount;
    }

    @Override
    public Integer insert(Branch branch) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insertBranch);
            if (branch.getParentBranchId() != null) {
                preparedStatement.setInt(1, branch.getParentBranchId());
            } else {
                preparedStatement.setNull(1, java.sql.Types.INTEGER);
            }
            // <editor-fold>
            preparedStatement.setInt(2, branch.getProjectId());
            preparedStatement.setInt(3, branch.getRootDirectoryId());
            preparedStatement.setInt(4, branch.getCommitId());
            preparedStatement.setString(5, branch.getBranchName());
            preparedStatement.setInt(6,branch.getBranchTypeId());
            if (branch.getTagId() != null) {
                preparedStatement.setInt(7, branch.getTagId());
            } else {
                preparedStatement.setNull(7, java.sql.Types.INTEGER);
            }
            preparedStatement.setBoolean(8, false);
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("BranchDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public Integer delete(Integer branchId, Integer commitId) throws SQLException {
        Integer returnedId = null;
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.deleteBranch);
            preparedStatement.setInt(1, commitId);
            preparedStatement.setInt(2, branchId);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnedId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("BranchDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnedId;
    }

    @Override
    public void getWriteableChildBranchIdList(Integer branchId, Map<Integer, String> branchMap) {
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.getWriteableChildBranchIdList, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer childBranchId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedBranchName = rs.getString(BRANCH_NAME_RESULT_SET_INDEX);
                branchMap.put(childBranchId, fetchedBranchName);
                // And recurse to find the family tree of branch decendents.
                getWriteableChildBranchIdList(childBranchId, branchMap);
            }
        } catch (SQLException e) {
            LOGGER.error("BranchDAOImpl: SQL exception in getWriteableChildBranchIdList", e);
        } catch (IllegalStateException e) {
            LOGGER.error("BranchDAOImpl: exception in getWriteableChildBranchIdList", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
    }

}
