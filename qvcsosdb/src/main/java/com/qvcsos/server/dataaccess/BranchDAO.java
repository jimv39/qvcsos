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
import java.sql.SQLException;
import java.util.List;

/**
 * Branch DAO interface.
 * @author Jim Voris
 */
public interface BranchDAO {

    /**
     * Find the branch by branch id.
     *
     * @param branchId the branch id.
     * @return the Branch with the given id, or null if the branch is not found.
     */
    Branch findById(Integer branchId);

    /**
     * Find the branch by project id and branch name.
     *
     * @param projectId the project id.
     * @param branchName the name of the branch on the given project.
     * @return the Branch in the given project with the given name, or null if the branch is not found.
     */
    Branch findByProjectIdAndBranchName(Integer projectId, String branchName);

    /**
     * Insert a row in the BRANCH table.
     *
     * @param branch the branch to create. Note that we do <b>not</b> honor any branch id passed in with the branch object. A new
     * branch will <b>always</b> be created.
     * @return the id of the inserted branch.
     *
     * @throws SQLException thrown if there is a problem.
     */
    Integer insert(Branch branch) throws SQLException;

    /**
     * Delete the given branch object.This just sets the deleted_flag to TRUE.
     *
     * @param branchId the branch id.
     * @param commitId the commit id.
     * @return the branch id.
     * @throws SQLException thrown if there is a problem.
     */
    Integer delete(Integer branchId, Integer commitId) throws SQLException;

    List<Branch> findProjectBranches(Integer id);

    Integer getChildBranchCount(Integer projectId, Integer parentBranchId);

}
