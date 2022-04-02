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

import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.FileRevision;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public interface FileRevisionDAO {
    /**
     * Find the revision by id.
     * @param id the revision id.
     * @return the file revision, or null if there are no revision with the given id.
     */
    FileRevision findById(Integer id);

    /**
     * Find the newest revision for a given file.
     * @param fileId find this file's newest revision.
     * @return the newest file revision.
     */
    FileRevision findNewestRevisionAllBranches(Integer fileId);

    /**
     * Find the newest revision for a given branch and file.
     * @param branchId the branch id.
     * @param fileId find this file's newest revision.
     * @return the newest file revision.
     */
    FileRevision findNewestRevisionOnBranch(Integer branchId, Integer fileId);

    /**
     * Find all the file revisions of the given file. The returned list will be newest to oldest.
     * @param branchesToSearch the branches to search.
     * @param fileId the file id.
     * @return the list of file revisions; newest to oldest.
     */
    List<FileRevision> findFileRevisions(String branchesToSearch, Integer fileId);

    /**
     * Find all file revisions across all branches.
     *
     * @param fileId the file id of the file we're interested in.
     * @return the list of file revisions for the given file.
     */
    List<FileRevision> findAllFileRevisions(Integer fileId);

    FileRevision findNewestBranchRevision(int branchId);

    /**
     * Find file promotion candidates.
     * @param promoteFromBranch the branch we're promoting from.
     * @param promoteToBranch the branch we are promoting to.
     * @return a list of promotion candidates.
     */
    List<FileRevision> findPromotionCandidates(Branch promoteFromBranch, Branch promoteToBranch);

    FileRevision findCommonAncestorRevision(Integer promoteToBranchId, Integer newestBranchAncestorId, Integer newestPromoteToAncestorId, Integer fileId);

    FileRevision findNewestPromotedRevision(int promoteFromBranchId, Integer fileId);

    FileRevision findByBranchIdAndAncestorRevisionAndFileId(int promoteToBranchId, Integer ancestorRevisionId, Integer fileId);

    List<Integer> findFileIdListForCommitId(Integer commitId);

    /**
     * Insert a fileRevision record.
     * @param fileRevision the file to insert.
     * @return the id of the inserted fileRevision.
     * @throws SQLException thrown if there is a problem.
     */
    Integer insert(FileRevision fileRevision) throws SQLException;

    /**
     * Update the ancestor revision so it points to its reverse delta revision, and its
     * data is updated to be a reverse delta script, instead of the the actual file contents.
     * @param id the id of the ancestor revision.
     * @param reverseDeltaRevisionId the id of the revision which must be hydrated in order to hydrate the ancestor revision.
     * @param reverseDeltaScript the script which can be applied to the reverse delta revision to hydrate the ancestor revision.
     * @return the id of the updated ancestor revision.
     * @throws SQLException thrown if there is a problem.
     */
    Integer updateAncestorRevision(Integer id, Integer reverseDeltaRevisionId, byte[] reverseDeltaScript) throws SQLException;

    boolean markPromoted(Integer fileFevisionId) throws SQLException;

}
