/*
 * Copyright 2021-2023 Jim Voris.
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
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.datamodel.FileName;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris.
 */
public class FileNameDAOImpl implements FileNameDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileNameDAOImpl.class);

    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 2;
    private static final int DIRECTORY_ID_RESULT_SET_INDEX = 3;
    private static final int FILE_ID_RESULT_SET_INDEX = 4;
    private static final int CREATED_FOR_REASON_RESULT_SET_INDEX = 5;
    private static final int COMMIT_ID_RESULT_SET_INDEX = 6;
    private static final int FILE_NAME_RESULT_SET_INDEX = 7;
    private static final int PROMOTED_FLAG_RESULT_SET_INDEX = 8;
    private static final int DELETED_FLAG_RESULT_SET_INDEX = 9;

    private final String schemaName;
    private final String findById;
    private final String findByFileId;
    private final String findByBranchIdAndFileId;
    private final String findByBranchListAndFileId;
    private final String findByDirectoryIdAndFileName;
    private final String findByFileIdAndCommitId;
    private final String findByFileIdAndBranchId;
    private final String findFileCreatedOnBranch;
    private final String findDeletedFileName;

    private final String wasFileDeletedOnFeatureBranch;
    private final String wasFileDeletedOnReleaseBranch;
    private final String getFileNameIdList;
    private final String getNotInFileIdList;
    private final String isFileNameDifferentOnFeatureBranch;
    private final String unDeleteFileName;
    private final String markPromoted;

    private final String insert;
    private final String delete;
    private final String move;
    private final String rename;
    private final String moveAndRename;

    public FileNameDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, BRANCH_ID, DIRECTORY_ID, FILE_ID, CREATED_FOR_REASON, COMMIT_ID, FILE_NAME, PROMOTED_FLAG, DELETED_FLAG FROM ";

        this.findById = selectSegment + this.schemaName + ".FILE_NAME WHERE ID = ?";
        this.findByFileId = selectSegment + this.schemaName + ".FILE_NAME WHERE FILE_ID = ? ORDER BY BRANCH_ID DESC";
        this.findByBranchIdAndFileId = selectSegment + this.schemaName + ".FILE_NAME WHERE BRANCH_ID = ? AND FILE_ID = ?";
        this.findByBranchListAndFileId = selectSegment + this.schemaName + ".FILE_NAME WHERE BRANCH_ID IN (%s) AND FILE_ID = ? ORDER BY BRANCH_ID DESC LIMIT 1";
        this.findByDirectoryIdAndFileName = selectSegment + this.schemaName + ".FILE_NAME WHERE DIRECTORY_ID = ? AND FILE_NAME = ?";
        this.findByFileIdAndCommitId = selectSegment + this.schemaName + ".FILE_NAME WHERE FILE_ID = ? AND COMMIT_ID <= ? ORDER BY COMMIT_ID DESC";
        this.findByFileIdAndBranchId = selectSegment + this.schemaName + ".FILE_NAME WHERE FILE_ID = ? AND BRANCH_ID = ?";
        this.findFileCreatedOnBranch = selectSegment + this.schemaName + ".FILE_NAME FN WHERE FN.FILE_ID = ? AND FN.PROMOTED_FLAG = FALSE AND (SELECT COUNT(*) FROM "
                + this.schemaName + ".FILE_NAME FNC WHERE FNC.FILE_ID = ? AND FNC.BRANCH_ID < ?) = 0";
        this.findDeletedFileName = selectSegment + this.schemaName + ".FILE_NAME FN WHERE FN.BRANCH_ID = ? AND FN.FILE_ID = ? AND FN.DELETED_FLAG = TRUE ORDER BY ID DESC LIMIT 1";

        this.wasFileDeletedOnFeatureBranch = selectSegment + this.schemaName + ".FILE_NAME FN WHERE FN.FILE_ID = ? AND FN.PROMOTED_FLAG = FALSE AND "
                + "FN.BRANCH_ID IN (%s) ORDER BY FN.COMMIT_ID DESC LIMIT 1";
        this.wasFileDeletedOnReleaseBranch = selectSegment + this.schemaName + ".FILE_NAME FN WHERE FN.FILE_ID = ? AND "
                + "FN.BRANCH_ID = ? ORDER BY FN.COMMIT_ID DESC LIMIT 1";

        this.getFileNameIdList = "SELECT FN.ID, FN.FILE_ID, FN.DELETED_FLAG, FN.FILE_NAME FROM " + this.schemaName
                + ".FILE_NAME FN WHERE FN.PROMOTED_FLAG = FALSE AND FN.DIRECTORY_ID = ? AND FN.BRANCH_ID IN (%s) ORDER BY BRANCH_ID DESC, FILE_ID DESC";
        this.getNotInFileIdList = "SELECT FN.FILE_ID FROM " + this.schemaName + ".FILE_NAME FN WHERE FN.BRANCH_ID = ? AND FN.DIRECTORY_ID != ?";

        this.isFileNameDifferentOnFeatureBranch = "SELECT "
                + "(SELECT FN.FILE_NAME FROM " + this.schemaName + ".FILE_NAME FN WHERE FN.FILE_ID = ? AND FN.BRANCH_ID IN (%s) ORDER BY BRANCH_ID DESC LIMIT 1) != "
                + "(SELECT FN.FILE_NAME FROM " + this.schemaName + ".FILE_NAME FN WHERE FN.FILE_ID = ? AND FN.BRANCH_ID IN (%s) ORDER BY BRANCH_ID DESC LIMIT 1)";
        this.unDeleteFileName = "UPDATE " + this.schemaName + ".FILE_NAME SET DELETED_FLAG = FALSE, COMMIT_ID = ? WHERE ID = ? RETURNING ID";
        this.markPromoted = "UPDATE " + this.schemaName + ".FILE_NAME SET PROMOTED_FLAG = TRUE, DELETED_FLAG = TRUE, PROMOTION_COMMIT_ID = ? WHERE BRANCH_ID = ? AND DIRECTORY_ID = ? AND FILE_ID = ?";

        this.insert = "INSERT INTO " + this.schemaName + ".FILE_NAME (BRANCH_ID, DIRECTORY_ID, FILE_ID, CREATED_FOR_REASON, COMMIT_ID, FILE_NAME, PROMOTED_FLAG, DELETED_FLAG) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING ID";
        this.delete = "UPDATE " + this.schemaName + ".FILE_NAME SET DELETED_FLAG = TRUE, COMMIT_ID = ? WHERE ID = ? RETURNING ID";
        this.move = "UPDATE " + this.schemaName + ".FILE_NAME SET DIRECTORY_ID = ?, DELETED_FLAG = FALSE, COMMIT_ID = ? WHERE ID = ?";
        this.rename = "UPDATE " + this.schemaName + ".FILE_NAME SET FILE_NAME = ?, DELETED_FLAG = FALSE, COMMIT_ID = ? WHERE ID = ?";
        this.moveAndRename = "UPDATE " + this.schemaName + ".FILE_NAME SET DIRECTORY_ID = ?, DELETED_FLAG = FALSE, COMMIT_ID = ?, FILE_NAME = ? WHERE ID = ?";
    }

    @Override
    public FileName findById(Integer id) {
        FileName fileName = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fileName = getFileNameFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileName;
    }

    @Override
    public List<FileName> findByFileId(Integer fileId) {
        List<FileName> fileNameList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByFileId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                fileNameList.add(getFileNameFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in findByFileId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in findByFileId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileNameList;
    }

    @Override
    public FileName findByBranchIdAndFileId(Integer branchId, Integer fileId) {
        FileName fileName = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByBranchIdAndFileId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, fileId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fileName = getFileNameFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileName;
    }

    @Override
    public FileName findByBranchListAndFileId(String branchListString, Integer fileId) {
        FileName fileName = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String queryString = String.format(findByBranchListAndFileId, branchListString);
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fileName = getFileNameFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in findById", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileName;
    }

    @Override
    public List<FileName> findByDirectoryIdAndFileName(Integer dirId, String filename) {
        List<FileName> fileNameList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByDirectoryIdAndFileName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, dirId);
            preparedStatement.setString(2, filename);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                fileNameList.add(getFileNameFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in findByDirectoryIdAndFileName", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in findByDirectoryIdAndFileName", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileNameList;
    }

    @Override
    public FileName findByFileIdAndCommitId(Integer fileId, Integer commitId) {
        FileName fileName = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByFileIdAndCommitId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);
            preparedStatement.setInt(2, commitId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fileName = getFileNameFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in findByFileIdAndCommitId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in findByFileIdAndCommitId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileName;
    }

    @Override
    public FileName findByFileIdAndBranchId(Integer fileId, Integer branchId) {
        FileName fileName = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByFileIdAndBranchId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);
            preparedStatement.setInt(2, branchId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fileName = getFileNameFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in findByFileIdAndBranchId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in findByFileIdAndBranchId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileName;
    }

    @Override
    public FileName findFileCreatedOnBranch(Integer fileId, Integer childBranchId) {
        FileName fileName = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findFileCreatedOnBranch, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, fileId);
            preparedStatement.setInt(2, fileId);
            preparedStatement.setInt(3, childBranchId);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fileName = getFileNameFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in findFileCreatedOnBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in findFileCreatedOnBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileName;
    }

    @Override
    public FileName findDeletedFileName(Integer branchId, Integer fileId) {
        FileName fileName = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findDeletedFileName, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, fileId);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fileName = getFileNameFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in findFileCreatedOnBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in findFileCreatedOnBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileName;
    }

    @Override
    public boolean wasFileDeletedOnFeatureBranch(Integer fileId, Integer childBranchId, String parentBranchList) {
        FileName fileName;
        ResultSet resultSet = null;
        boolean wasDeletedFlag = false;
        PreparedStatement preparedStatement = null;
        try {
            String queryString = String.format(wasFileDeletedOnFeatureBranch, parentBranchList);
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fileName = getFileNameFromResultSet(resultSet);
                wasDeletedFlag = fileName.getDeletedFlag();
            }

        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in wasFileDeletedOnFeatureBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in wasFileDeletedOnFeatureBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return wasDeletedFlag;
    }

    @Override
    public boolean wasFileDeletedOnReleaseBranch(Integer fileId, Integer childBranchId) {
        FileName fileName;
        ResultSet resultSet = null;
        boolean wasDeletedFlag = false;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(wasFileDeletedOnReleaseBranch, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);
            preparedStatement.setInt(2, childBranchId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                fileName = getFileNameFromResultSet(resultSet);
                wasDeletedFlag = fileName.getDeletedFlag();
            }

        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in wasFileDeletedOnReleaseBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in wasFileDeletedOnReleaseBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return wasDeletedFlag;
    }

    @Override
    public List<Integer> getFileNameIdList(String branchList, Integer directoryId, List<Integer> notInFileIdList) {
        List<Integer> fileNameIdList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        String queryString = String.format(getFileNameIdList, branchList);
        Map<Integer, String> fileNameIdMap = new TreeMap<>();
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, directoryId);

            resultSet = preparedStatement.executeQuery();
            // <editor-fold>
            while (resultSet.next()) {
                Integer fetchedFileNameId = resultSet.getInt(1);
                Integer fetchedFileId = resultSet.getInt(2);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(3);
                if (!fileNameIdMap.containsKey(fetchedFileId)) {
                    String fileName = resultSet.getString(4);
                    fileNameIdMap.put(fetchedFileId, fileName);
                    // If the filename has not been 'deleted' on the deepest branch...
                    if (!fetchedDeletedFlag) {
                        fileNameIdList.add(fetchedFileNameId);
                    } else {
                        // The file was deleted on the deepest branch...
                        notInFileIdList.add(fetchedFileId);
                    }
                }
            }
            // </editor-fold>
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in getNotInFileIdList", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in getNotInFileIdList", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileNameIdList;
    }

    @Override
    public List<Integer> getNotInFileIdList(Integer branchId, Integer directoryId, List<Integer> notInFileIdList) {
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.getNotInFileIdList, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, directoryId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                notInFileIdList.add(resultSet.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in getNotInFileIdList", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in getNotInFileIdList", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return notInFileIdList;
    }

    @Override
    public Boolean isFileNameDifferentOnFeatureBranch(Integer fileId, String childBranchList, String parentBranchList) {
        Boolean flag = false;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String queryString = String.format(isFileNameDifferentOnFeatureBranch, childBranchList, parentBranchList);
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, fileId);
            preparedStatement.setInt(2, fileId);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                flag = resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            LOGGER.error("FileNameDAOImpl: SQL exception in isFileNameDifferentOnFeatureBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in isFileNameDifferentOnFeatureBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return flag;
    }

    @Override
    public Integer unDeleteFileName(Integer id, Integer commitId) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.unDeleteFileName);
            // <editor-fold>
            preparedStatement.setInt(1, commitId);
            preparedStatement.setInt(2, id);
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in rename", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public Integer insert(FileName fileName) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            preparedStatement.setInt(1,fileName.getBranchId());
            preparedStatement.setInt(2, fileName.getDirectoryId());
            preparedStatement.setInt(3, fileName.getFileId());
            if (fileName.getCreatedForReason() == null) {
                preparedStatement.setNull(4, java.sql.Types.INTEGER);
            } else {
                preparedStatement.setInt(4, fileName.getCreatedForReason());
            }
            preparedStatement.setInt(5, fileName.getCommitId());
            preparedStatement.setString(6, fileName.getFileName());
            preparedStatement.setBoolean(7, fileName.getPromotedFlag());
            preparedStatement.setBoolean(8, fileName.getDeletedFlag());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public Integer delete(Integer id, Integer commitId) throws SQLException {
        PreparedStatement preparedStatement = null;
        Integer fileNameId = null;
        ResultSet rs = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.delete);
            preparedStatement.setInt(1, commitId);
            preparedStatement.setInt(2, id);
            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                fileNameId = rs.getInt(1);
            }

        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return fileNameId;
    }

    @Override
    public boolean move(Integer id, Integer commitId, Integer destinationDirectoryId) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.move);
            // <editor-fold>
            preparedStatement.setInt(1, destinationDirectoryId);
            preparedStatement.setInt(2, commitId);
            preparedStatement.setInt(3, id);
            // </editor-fold>

            returnFlag = preparedStatement.execute();
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in move", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

    @Override
    public boolean rename(Integer id, Integer commitId, String newName) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.rename);
            // <editor-fold>
            preparedStatement.setString(1, newName);
            preparedStatement.setInt(2, commitId);
            preparedStatement.setInt(3, id);
            // </editor-fold>

            returnFlag = preparedStatement.execute();
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in rename", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

    @Override
    public boolean moveAndRename(Integer id, Integer commitId, Integer destinationDirectoryId, String newFileName) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.moveAndRename);
            // <editor-fold>
            preparedStatement.setInt(1, destinationDirectoryId);
            preparedStatement.setInt(2, commitId);
            preparedStatement.setString(3, newFileName);
            preparedStatement.setInt(4, id);
            // </editor-fold>

            returnFlag = preparedStatement.execute();
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in moveAndRename", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }

    private FileName getFileNameFromResultSet(ResultSet resultSet) throws SQLException {
        Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
        Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
        Integer fetchedDirectoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
        Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
        Integer fetchedCreatedForReason = null;
        Object fetchedCreatedForReasonObject = resultSet.getObject(CREATED_FOR_REASON_RESULT_SET_INDEX);
        if (fetchedCreatedForReasonObject != null) {
            fetchedCreatedForReason = resultSet.getInt(CREATED_FOR_REASON_RESULT_SET_INDEX);
        }
        Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
        String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
        Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
        Boolean fetchedDeletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

        FileName fileName = new FileName();
        fileName.setId(fetchedId);
        fileName.setBranchId(fetchedBranchId);
        fileName.setDirectoryId(fetchedDirectoryId);
        fileName.setFileId(fetchedFileId);
        fileName.setCreatedForReason(fetchedCreatedForReason);
        fileName.setCommitId(fetchedCommitId);
        fileName.setFileName(fetchedFilename);
        fileName.setPromotedFlag(fetchedPromotedFlag);
        fileName.setDeletedFlag(fetchedDeletedFlag);
        return fileName;
    }

    @Override
    public boolean markPromoted(Integer fileNameId, Integer commitId) throws SQLException {
        PreparedStatement preparedStatement = null;
        boolean returnFlag = false;
        try {
            FileName fileName = findById(fileNameId);

            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.markPromoted);
            // <editor-fold>
            preparedStatement.setInt(1, commitId);
            preparedStatement.setInt(2, fileName.getBranchId());
            preparedStatement.setInt(3, fileName.getDirectoryId());
            preparedStatement.setInt(4, fileName.getFileId());
            // </editor-fold>

            returnFlag = preparedStatement.execute();
        } catch (IllegalStateException e) {
            LOGGER.error("FileNameDAOImpl: exception in markPromoted", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
        return returnFlag;
    }
}
