/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.server.dataaccess;

import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.server.datamodel.File;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * File DAO interface.
 * @author Jim Voris
 */
public interface FileDAO {

    /**
     * Find file by file ID.
     *
     * @param branchId the branch id.
     * @param fileId the file id.
     * @return the file if found; null if not found.
     */
    File findById(Integer branchId, Integer fileId);

    /**
     * Find the list of files associated with a given branch.
     *
     * @param branchId the branch where the file lives.
     * @return the List of files that are on the given branch. The list may be empty if there are no files.
     */
    List<File> findByBranchId(Integer branchId);

    /**
     * Find the list of files associated with a given branch and return the file promotion info for those files. The DAO does
     * <i>not</i> fill in all the elements of the FilePromotionInfo object... just those that are stored in the database.
     *
     * @param branchId the branch where the file lives.
     * @return the List of files that are on the given branch. The list may be empty if there are no files.
     */
    List<FilePromotionInfo> findFilePromotionInfoByBranchId(Integer branchId);

    /**
     * Find the list of files associated with a given branch and directory.
     *
     * @param branchId the branch where the file lives.
     * @param directoryId the directory where the file lives.
     * @return the List of files that are in the given directory on the given branch. The list may be empty if there are no files.
     */
    List<File> findByBranchAndDirectoryId(Integer branchId, Integer directoryId);

    /**
     * Find the list of files associated with a given branch and directory that existed on or before the given date.
     *
     * @param branchId the branch where the file lives.
     * @param directoryId the directory where the file lives.
     * @param branchDate the date of the branch.
     * @return the List of files that are in the given directory on the given branch. The list may be empty if there are no files.
     */
    List<File> findByBranchAndDirectoryIdAndBranchDate(Integer branchId, Integer directoryId, Date branchDate);

    /**
     * Find all files, ordered by ascending fileId.
     * @return a list of all files, ordered by ascending fileId.
     */
    List<File> findAll();

    /**
     * Find the File record on the given branch that has the IsDeletedFlag set to true.
     *
     * @param branchId the branch id.
     * @param fileId the file id.
     * @return the File record with is deleted set to true, or null if no record exists.
     */
    File findIsDeletedById(Integer branchId, Integer fileId);

    /**
     * Insert a file record into the FILE table.
     *
     * @param file the file to insert (including the file id).
     * @throws SQLException if there is a problem performing the insert.
     */
    void insert(File file) throws SQLException;

    /**
     * Delete the given file record that has the is deleted flag set to true.
     *
     * @param file the file record that has the is deleted flag set to true.
     */
    void deleteWithIsDeletedFlag(File file);

    /**
     * Update a file record in the FILE table.
     *
     * @param file the file to update.
     * @param deletedFlag the current state of the deleted flag.
     * @throws SQLException if there is a problem performing the update.
     */
    void update(File file, boolean deletedFlag) throws SQLException;
}
