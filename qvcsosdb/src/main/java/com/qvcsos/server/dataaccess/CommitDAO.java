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

import com.qvcsos.server.datamodel.Commit;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public interface CommitDAO {

    /**
     * Find the commit by id.
     * @param id the id (primary key) of the Commit object to find.
     * @return the Commit object with the given id; or null if not found.
     */
    Commit findById(Integer id);

    /**
     * Insert a row in the Commit table.
     *
     * @param commit the commit to insert.
     * A new commit row will <b>always</b> be created.
     * @return the id of the inserted row.
     * @throws SQLException thrown if there is a problem.
     */
    Integer insert(Commit commit) throws SQLException;

    /**
     * Update the commit message.
     * @param commitId the commitId of the commit to update.
     * @param commitMessage the new commit message.
     * @return the commitId of the updated record.
     * @throws SQLException thrown if there is a problem.
     */
    Integer updateCommitMessage(Integer commitId, String commitMessage) throws SQLException;

    /**
     * Get a list of commit's that surround the given commit id on the given
     * branch.
     *
     * @param commitId the commit id that should 'center' the returned list.
     * @param branchesToSearch the branches to search.
     * @return a list of commits that surround the given commit id.
     */
    List<Commit> getCommitList(Integer commitId, String branchesToSearch);
}
