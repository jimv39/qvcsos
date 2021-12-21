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

import com.qvcsos.server.datamodel.Tag;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public interface TagDAO {
    /**
     * Find the tag by tag id.
     *
     * @param tagId the tag id.
     * @return the Tag with the given id, or null if the Tag is not found.
     */
    Tag findById(Integer tagId);

    List<Tag> findAll();

    List<Tag> findByBranchId(Integer branchId);

    /**
     * Update the commit id for a moveable tag.
     *
     * @param tagId the id of the tag we're changing.
     * @param newCommitId the new commit id for the given tag.
     * @return the id of the changed tag (should be the same as tagId).
     * @throws java.sql.SQLException if there's a problem.
     */
    Integer updateMoveableCommitId(Integer tagId, Integer newCommitId) throws SQLException;

    /**
     * Insert a tag.
     *
     * @param tag the tag to insert. We ignore the tag id.
     * @return id of the inserted row.
     * @throws SQLException if there is a problem.
     */
    Integer insert(Tag tag) throws SQLException;

    /**
     * Find by parent branch id and tag text.
     * @param id the parent branch id.
     * @param tagText the text of the tag.
     * @return the matching tag, or null if not found.
     */
    Tag findByBranchIdAndTagText(Integer id, String tagText);

}
