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
package com.qvcsos.server.dataaccess;

import com.qvcsos.server.datamodel.FileName;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public interface FileNameDAO {

    /**
     * Find by id.
     * @param id the id of the FileName record.
     * @return the matching FileName record, or null if not found.
     */
    FileName findById(Integer id);

    /**
     * Find by fileId.
     * @param fileId the fileId for the requested FileName record.
     * @return the matching FileName record(s), or an empty list if not found.
     */
    List<FileName> findByFileId(Integer fileId);

    /**
     * Find by fileId.
     * @param branchId the branchId of the requested FileName record.
     * @param fileId the fileId for the requested FileName record.
     * @return the matching FileName record, or null if not found.
     */
    FileName findByBranchIdAndFileId(Integer branchId, Integer fileId);


    /**
     * Find by fileId.
     * @param branchListString the list of branchIds to search for the requested FileName record.
     * @param fileId the fileId for the requested FileName record.
     * @return the first matching FileName record, or null if not found.
     */
    FileName findByBranchListAndFileId(String branchListString, Integer fileId);

    /**
     * Find by directoryId and filename.
     * @param directoryId the directory id.
     * @param fileName the short file name.
     * @return the matching FileName record, or null if not found.
     */
    List<FileName> findByDirectoryIdAndFileName(Integer directoryId, String fileName);

    /**
     * Find the name that was used before the supplied commitId.
     * @param fileId the file id.
     * @param commitId the commit id.
     * @return the FileName 'active' at just before the given commitId.
     */
    FileName findByFileIdAndCommitId(Integer fileId, Integer commitId);

    /**
     * Find the FileName record for a file created on the given branch.
     * @param fileId the file id.
     * @param childBranchId the branch id where the file may have been created.
     * @return the FileName record if the file was created on the given branch; null otherwise.
     */
    FileName findFileCreatedOnBranch(Integer fileId, Integer childBranchId);

    /**
     * Was the FileName record for a file deleted on the given feature branch.
     *
     * @param fileId the file id.
     * @param childBranchId the branch on which the file may have been deleted.
     * @param parentBranchList the list of parent branches where we need to check that the file was not also deleted there.
     * @return true if the file was deleted on the feature branch; false if not
     * deleted on the feature branch.
     */
    boolean wasFileDeletedOnFeatureBranch(Integer fileId, Integer childBranchId, String parentBranchList);

    /**
     * Was the FileName record for a file deleted on the given release branch.
     *
     * @param fileId the file id.
     * @param childBranchId the branch on which the file may have been deleted.
     * @return true if the file was deleted on the release branch; false if not
     * deleted on the release branch.
     */
    boolean wasFileDeletedOnReleaseBranch(Integer fileId, Integer childBranchId);

    /**
     * Get the list of fileNameId's for the given branch/directory.
     * @param branchList the list of branches.
     * @param directoryId the directory id.
     * @return the list of file name id's for the given branch/directory.
     */
    List<Integer> getFileNameIdList(String branchList, Integer directoryId);

    /**
     * Get the list of fileId's that should be ignored for a given directory.
     * @param branchId the branch of interest.
     * @param directoryId the directory of interest.
     * @return a list of fileId's that should be ignored, or an empty list.
     */
    List<Integer> getNotInFileIdList(Integer branchId, Integer directoryId);

    /**
     * Is the name of the file identified by fileId different on the branch than on its parent branches.
     * @param fileId the file id.
     * @param childBranchList the child branch list.
     * @param parentBranchList the parent branch list.
     * @return true if the files names are different; false if they are the same.
     */
    Boolean isFileNameDifferentOnFeatureBranch(Integer fileId, String childBranchList, String parentBranchList);

    /**
     * Undelete the given fileName record.
     * @param id the fileName id.
     * @param commitId the commit id.
     * @return the fileName id.
     * @throws SQLException thrown if there is a problem.
     */
    Integer unDeleteFileName(Integer id, Integer commitId) throws SQLException;

    /**
     * Insert a fileName record.
     * @param fileName the file to insert.
     * @return the id of the inserted fileName.
     * @throws SQLException thrown if there is a problem.
     */
    Integer insert(FileName fileName) throws SQLException;

    /**
     * Rename a file.
     * @param id the id of the FileName record.
     * @param commitId the commit id for this change.
     * @param newName the new name.
     * @return true if rename is successful.
     * @throws SQLException thrown if there is a problem.
     */
    boolean rename(Integer id, Integer commitId, String newName) throws SQLException;

    /**
     * Move a file.
     *
     * @param id the id of the FileName record.
     * @param commitId the commit id for this change.
     * @param destinationDirectoryId the destination directory id.
     * @return true if rename is successful.
     * @throws SQLException thrown if there is a problem.
     */
    boolean move(Integer id, Integer commitId, Integer destinationDirectoryId) throws SQLException;

    /**
     * Move and rename a file.
     *
     * @param id the id of the FileName record.
     * @param commitId the commit id for this change.
     * @param destinationDirectoryId the destination directory id.
     * @param newFileName the new name of the file.
     * @return true if move and rename is successful.
     * @throws SQLException thrown if there is a problem.
     */
    boolean moveAndRename(Integer id, Integer commitId, Integer destinationDirectoryId, String newFileName) throws SQLException;

    /**
     * Delete a file.
     * @param id the id of the FileName record.
     * @param commitId the commit id for this change.
     * @return the fileNameId of the deleted row.
     * @throws SQLException thrown if there is a problem.
     */
    Integer delete(Integer id, Integer commitId) throws SQLException;

}
