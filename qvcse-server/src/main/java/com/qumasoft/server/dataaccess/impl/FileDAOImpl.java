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

import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.server.DatabaseManager;
import com.qumasoft.server.dataaccess.FileDAO;
import com.qumasoft.server.datamodel.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File DAO implementation.
 * @author Jim Voris
 */
public class FileDAOImpl implements FileDAO {
    /*
     * + "FILE_ID INT NOT NULL," + "BRANCH_ID INT NOT NULL," + "DIRECTORY_ID INT NOT NULL," + "FILE_NAME VARCHAR(256) NOT NULL," +
     * "INSERT_DATE TIMESTAMP NOT NULL," + "UPDATE_DATE TIMESTAMP NOT NULL," + "DELETED_FLAG BOOLEAN NOT NULL,"
     */

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FileDAOImpl.class);
    private static final String FIND_BY_ID =
            "SELECT DIRECTORY_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE FROM QVCSE.FILE WHERE BRANCH_ID = ? AND FILE_ID = ? AND DELETED_FLAG = false";
    private static final String FIND_IS_DELETED_BY_ID =
            "SELECT DIRECTORY_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE FROM QVCSE.FILE WHERE BRANCH_ID = ? AND FILE_ID = ? AND DELETED_FLAG = true";
    private static final String FIND_BY_BRANCH_ID =
            "SELECT FILE_ID, DIRECTORY_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.FILE WHERE BRANCH_ID = ?";
    private static final String FIND_PROMOTION_INFO_BY_BRANCH_ID =
            "SELECT p.FILE_ID, f.FILE_NAME, f.BRANCH_ID, d.APPENDED_PATH, f.DELETED_FLAG FROM QVCSE.PROMOTION_CANDIDATE p, QVCSE.DIRECTORY d, QVCSE.FILE f "
            + "WHERE f.FILE_ID = p.FILE_ID AND "
            + "f.DELETED_FLAG = FALSE AND f.DIRECTORY_ID = d.DIRECTORY_ID AND p.BRANCH_ID = ? order by FILE_ID";
    private static final String FIND_BY_BRANCH_AND_DIRECTORY_ID =
            "SELECT FILE_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.FILE WHERE BRANCH_ID = ? AND DIRECTORY_ID = ?";
    private static final String FIND_BY_BRANCH_AND_DIRECTORY_ID_AND_VIEW_DATE =
            "SELECT FILE_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG FROM QVCSE.FILE WHERE BRANCH_ID = ? AND DIRECTORY_ID = ? AND UPDATE_DATE <= ?";
    private static final String INSERT_FILE =
            "INSERT INTO QVCSE.FILE (FILE_ID, BRANCH_ID, DIRECTORY_ID, FILE_NAME, INSERT_DATE, UPDATE_DATE, DELETED_FLAG) VALUES (?, ?, ?, ?, "
            + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)";
    private static final String UPDATE_FILE =
            "UPDATE QVCSE.FILE SET FILE_NAME = ?, DIRECTORY_ID = ?, UPDATE_DATE = CURRENT_TIMESTAMP, DELETED_FLAG = ? WHERE FILE_ID = ? AND BRANCH_ID = ? AND DELETED_FLAG = ?";
    private static final String DELETE_WITH_IS_DELETED_FLAG =
            "DELETE FROM QVCSE.FILE WHERE FILE_ID = ? AND BRANCH_ID = ? AND DELETED_FLAG = true";

    /**
     * Find file by file ID.
     *
     * @param branchId the branch id.
     * @param fileId the file id.
     * @return the file if found; null if not found.
     */
    @Override
    public File findById(Integer branchId, Integer fileId) {
        File file = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, fileId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // <editor-fold>
                Integer directoryId = resultSet.getInt(1);
                String fileName = resultSet.getString(2);
                Date insertDate = resultSet.getTimestamp(3);
                Date updateDate = resultSet.getTimestamp(4);
                // </editor-fold>

                file = new File();
                file.setBranchId(branchId);
                file.setFileId(fileId);
                file.setDirectoryId(directoryId);
                file.setFileName(fileName);
                file.setInsertDate(insertDate);
                file.setUpdateDate(updateDate);
                file.setDeletedFlag(false);
            }
        } catch (SQLException e) {
            LOGGER.error("FileDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in findById", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return file;
    }

    /**
     * Find the list of files associated with a given branch.
     *
     * @param branchId the branch where the file lives.
     * @return the List of files that are on the given branch. The list may be empty if there are no files.
     */
    @Override
    public List<File> findByBranchId(Integer branchId) {
        List<File> fileList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_BRANCH_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer fileId = resultSet.getInt(1);
                Integer directoryId = resultSet.getInt(2);
                String fileName = resultSet.getString(3);
                Date insertDate = resultSet.getTimestamp(4);
                Date updateDate = resultSet.getTimestamp(5);
                Boolean deletedFlag = resultSet.getBoolean(6);
                // </editor-fold>

                File file = new File();
                file.setBranchId(branchId);
                file.setFileId(fileId);
                file.setDirectoryId(directoryId);
                file.setFileName(fileName);
                file.setInsertDate(insertDate);
                file.setUpdateDate(updateDate);
                file.setDeletedFlag(deletedFlag);
                fileList.add(file);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in findByBranchId", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return fileList;
    }

    /**
     * Find the list of file promotion info for a given branch. Note that we do not completely populate the returned
     * FilePromotionInfo objects... we only fill in what we can from the database.
     *
     * @param branchId the branch id.
     * @return the List of FilePromotionInfo objects for the given branch.
     */
    @Override
    public List<FilePromotionInfo> findFilePromotionInfoByBranchId(Integer branchId) {
        List<FilePromotionInfo> filePromotionInfoList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Map<Integer, FilePromotionInfo> filePromotionMap = new TreeMap<>();
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_PROMOTION_INFO_BY_BRANCH_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer fileId = resultSet.getInt(1);
                String fileName = resultSet.getString(2);
                Integer fileBranchId = resultSet.getInt(3);
                String appendedPath = resultSet.getString(4);
                Boolean deletedFlag = resultSet.getBoolean(5);
                // </editor-fold>

                FilePromotionInfo filePromotionInfo = new FilePromotionInfo();
                filePromotionInfo.setAppendedPath(appendedPath);
                filePromotionInfo.setFileId(fileId);
                filePromotionInfo.setFileBranchId(fileBranchId);
                filePromotionInfo.setShortWorkfileName(fileName);
                filePromotionInfo.setDeletedFlag(deletedFlag);
                if (filePromotionMap.containsKey(fileId)) {
                    FilePromotionInfo mapFilePromotionInfo = filePromotionMap.get(fileId);
                    if (mapFilePromotionInfo.getFileBranchId().equals(branchId)) {
                        if (filePromotionInfo.getFileBranchId().equals(branchId)) {
                            if (!filePromotionInfo.getDeletedFlag()) {
                                // Only a non-deleted record wins.
                                filePromotionMap.put(fileId, filePromotionInfo);
                            }
                        }
                    } else {
                        // The existing record's branch id does not match our branch id, so
                        // a record that does match branch id's 'wins'.
                        if (filePromotionInfo.getFileBranchId().equals(branchId)) {
                            filePromotionMap.put(fileId, filePromotionInfo);
                        } else {
                            if (!filePromotionInfo.getDeletedFlag()) {
                                // A non-deleted record wins.
                                filePromotionMap.put(fileId, filePromotionInfo);
                            }
                        }
                    }
                } else {
                    filePromotionMap.put(fileId, filePromotionInfo);
                }
            }
            Iterator<FilePromotionInfo> it = filePromotionMap.values().iterator();
            while (it.hasNext()) {
                FilePromotionInfo filePromotionInfo = it.next();
                filePromotionInfoList.add(filePromotionInfo);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in findFilePromotionInfoByBranchId", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return filePromotionInfoList;
    }

    /**
     * Find the list of files associated with a given branch and directory.
     *
     * @param branchId the branch where the file lives.
     * @param directoryId the directory where the file lives.
     * @return the List of files that are in the given directory on the given branch. The list may be empty if there are no files.
     */
    @Override
    public List<File> findByBranchAndDirectoryId(Integer branchId, Integer directoryId) {
        List<File> fileList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_BRANCH_AND_DIRECTORY_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, directoryId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer fileId = resultSet.getInt(1);
                String fileName = resultSet.getString(2);
                Date insertDate = resultSet.getTimestamp(3);
                Date updateDate = resultSet.getTimestamp(4);
                Boolean deletedFlag = resultSet.getBoolean(5);
                // </editor-fold>

                File file = new File();
                file.setBranchId(branchId);
                file.setFileId(fileId);
                file.setDirectoryId(directoryId);
                file.setFileName(fileName);
                file.setInsertDate(insertDate);
                file.setUpdateDate(updateDate);
                file.setDeletedFlag(deletedFlag);
                fileList.add(file);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in findByBranchAndDirectoryId", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return fileList;
    }

    /**
     * Find the list of files associated with a given branch and directory that existed on or before the given date.
     *
     * @param branchId the branch where the file lives.
     * @param directoryId the directory where the file lives.
     * @param viewDate the date of the view.
     * @return the List of files that are in the given directory on the given branch. The list may be empty if there are no files.
     */
    @Override
    public List<File> findByBranchAndDirectoryIdAndViewDate(Integer branchId, Integer directoryId, Date viewDate) {
        List<File> fileList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_BY_BRANCH_AND_DIRECTORY_ID_AND_VIEW_DATE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, directoryId);
            preparedStatement.setTimestamp(3, new java.sql.Timestamp(viewDate.getTime()));

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                // <editor-fold>
                Integer fileId = resultSet.getInt(1);
                String fileName = resultSet.getString(2);
                Date insertDate = resultSet.getTimestamp(3);
                Date updateDate = resultSet.getTimestamp(4);
                Boolean deletedFlag = resultSet.getBoolean(5);
                // </editor-fold>

                File file = new File();
                file.setBranchId(branchId);
                file.setFileId(fileId);
                file.setDirectoryId(directoryId);
                file.setFileName(fileName);
                file.setInsertDate(insertDate);
                file.setUpdateDate(updateDate);
                file.setDeletedFlag(deletedFlag);
                fileList.add(file);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in findByBranchAndDirectoryIdAndViewDate", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return fileList;
    }

    /**
     * Find the File record on the given branch that has the IsDeletedFlag set to true.
     *
     * @param branchId the branch id.
     * @param fileId the file id.
     * @return the File record with is deleted set to true, or null if no record exists.
     */
    @Override
    public File findIsDeletedById(Integer branchId, Integer fileId) {
        File file = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(FIND_IS_DELETED_BY_ID, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, fileId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                // <editor-fold>
                Integer directoryId = resultSet.getInt(1);
                String fileName = resultSet.getString(2);
                Date insertDate = resultSet.getTimestamp(3);
                Date updateDate = resultSet.getTimestamp(4);
                // </editor-fold>

                file = new File();
                file.setBranchId(branchId);
                file.setFileId(fileId);
                file.setDirectoryId(directoryId);
                file.setFileName(fileName);
                file.setInsertDate(insertDate);
                file.setUpdateDate(updateDate);
                file.setDeletedFlag(true);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in findById", e);
        } finally {
            closeDbResources(resultSet, preparedStatement);
        }
        return file;
    }

    /**
     * Insert a file into the database.
     *
     * @param file the file object to insert, including the fileId.
     * @throws SQLException if we could not insert the record.
     */
    @Override
    public void insert(File file) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(INSERT_FILE);
            // <editor-fold>
            preparedStatement.setInt(1, file.getFileId());
            preparedStatement.setInt(2, file.getBranchId());
            preparedStatement.setInt(3, file.getDirectoryId());
            preparedStatement.setString(4, file.getFileName());
            preparedStatement.setBoolean(5, file.isDeletedFlag());
            // </editor-fold>

            preparedStatement.executeUpdate();
        } catch (IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in insert", e);
            throw e;
        } finally {
            closeDbResources(null, preparedStatement);
        }
    }

    /**
     * Update a file record in the FILE table.
     *
     * @param file the file to update.
     * @param deletedFlag the current state of the deleted flag.
     * @throws SQLException if there is a problem performing the update.
     */
    @Override
    public void update(File file, boolean deletedFlag) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(UPDATE_FILE);
            preparedStatement.setString(1, file.getFileName());
            preparedStatement.setInt(2, file.getDirectoryId());
            preparedStatement.setBoolean(3, file.isDeletedFlag());
            preparedStatement.setInt(4, file.getFileId());
            preparedStatement.setInt(5, file.getBranchId());
            preparedStatement.setBoolean(6, deletedFlag);

            preparedStatement.executeUpdate();
        } catch (IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in update", e);
            throw e;
        } finally {
            closeDbResources(null, preparedStatement);
        }
    }

    /**
     * Delete the given file record that has the is deleted flag set to true.
     *
     * @param file the file record that has the is deleted flag set to true.
     */
    @Override
    public void deleteWithIsDeletedFlag(File file) {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(DELETE_WITH_IS_DELETED_FLAG);
            preparedStatement.setInt(1, file.getFileId());
            preparedStatement.setInt(2, file.getBranchId());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            LOGGER.error("FileDAOImpl: sql exception in deleteWithIsDeletedFlag", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FileDAOImpl: exception in deleteWithIsDeletedFlag", e);
            throw e;
        } finally {
            closeDbResources(null, preparedStatement);
        }
    }

    private void closeDbResources(ResultSet resultSet, PreparedStatement preparedStatement) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                LOGGER.error("FileDAOImpl: exception closing resultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                LOGGER.error("FileDAOImpl: exception closing preparedStatment", e);
            }
        }
    }
}
