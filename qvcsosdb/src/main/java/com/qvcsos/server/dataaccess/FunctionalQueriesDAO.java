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
package com.qvcsos.server.dataaccess;

import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.TagInfoData;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.FileRevision;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public interface FunctionalQueriesDAO {

    List<SkinnyLogfileInfo> getSkinnyLogfileInfo(Integer branchId, Integer directoryId);

    /**
     * Get the skinny info after the addition of a file to source control, or after a checkin.
     * @param fileRevisionId the file revision id of the newly created file revision.
     * @return the skinny info for the given file revision.
     */
    SkinnyLogfileInfo getSkinnyLogfileInfo(Integer fileRevisionId);

    /**
     * Get the skinny info for a get operation.
     * @param fileRevisionId the file revision id for the revision being fetched.
     * @return a populated SkinnyLogfileInfo object.
     */
    SkinnyLogfileInfo getSkinnyLogfileInfoForGet(Integer fileRevisionId);

    DirectoryCoordinateIds getDirectoryCoordinateIds(DirectoryCoordinate directoryCoordinate);

    LogfileInfo getLogfileInfo(DirectoryCoordinate directoryCoordinate, String shortFilename, Integer fileId);

    LogfileInfo getLogfileInfo(DirectoryCoordinateIds directoryCoordinateIds, String shortFilename, Integer fileId);

    /**
     * Get the list of branches that are ancestor branches to the given branch.
     * The list is in descending order with the first entry equal to the requested branchId.
     * If a branch is the Trunk, the list will have just one entry -- the branch with the given branchId.
     * @param branchId the branch id.
     * @return a list of branches (including the branch with the given branchId that are ancestors of the given branch).
     */
    List<Branch> getBranchAncestryList(Integer branchId);

    /**
     * Find the branch by project name and branch name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @return the Branch, or null if not found.
     */
    Branch findBranchByProjectNameAndBranchName(String projectName, String branchName);

    /**
     * Find the list of file revisions for a given fileId. The list spans the list of branches that may contain file revisions
     * for the given file.
     * @param branchArray The array of branches to search.
     * @param fileId the file id.
     * @return a list of file revisions, ordered by descending branchId, revisionId. Newer revisions on a parent branch are pruned from the result.
     */
    List<FileRevision> findFileRevisionsInBranches(List<Branch> branchArray, Integer fileId);

    List<Branch> findBranchesForProjectName(String projectName);

    List<DirectoryLocation> findChildDirectoryLocations(List<Branch> branchArray, Integer parentDirectoryLocationId);

    Integer getChildBranchCount(String projectName, String branchName);

    /**
     * Get a List of Commit comments, newest to oldest.
     * @param userName the name of the user.
     * @param count the maximum number of comments to fetch.
     * @return a List of user commit comments.
     */
    List<String> getMostRecentUserCommits(String userName, Integer count);

    Commit findNewestFileRevisionCommitOnBranch(int branchId);


    /**
     * Find the tip file revision on a branch (or its parents) given the branch id, and file id.
     * @param fromBranchId the branch to start with (may need to search its parents as well).
     * @param fileId the file id.
     * @return the tip file revision for the given branch (may be on a parent branch).
     */
    FileRevision findBranchTipRevisionByBranchIdAndFileId(Integer fromBranchId, Integer fileId);

    /**
     * Build the string of branch id's from the array of branches.
     * @param branchArray the array of branches.
     * @return a String suitable for use in formatting a query.
     */
    String buildBranchesToSearchString(List<Branch> branchArray);

    /**
     * Build the string of file id's from the array of file ids.
     * @param fileIdArray the array of file ids.
     * @return a String suitable for use in formatting a query.
     */
    String buildIdsToSearchString(List<Integer> fileIdArray);

    /**
     * Get the list of tags information.
     *
     * @param branchId the branch id.
     * @return the list of TagInfoData for the given branch.
     */
    List<TagInfoData> getTagsInfoData(Integer branchId);

    /**
     * Get all the revision history for the given fileId.
     *
     * @param dc the directory coordinate.
     * @param shortWorkfileName the short workfile name.
     * @param fileId the file id.
     * @return a LogfileInfo object that has <i>all</i> revision history for the
     * given file.
     */
    LogfileInfo getAllLogfileInfo(DirectoryCoordinate dc, String shortWorkfileName, Integer fileId);

    /**
     * Get the list of skinny info for a branch's cemetery.
     *
     * @param branch the branch that we're interested in.
     * @return the list of skinnyInfo for the given branch's cemetery.
     */
    List<SkinnyLogfileInfo> getSkinnyLogfileInfoForCemetery(Branch branch);
}
