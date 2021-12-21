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

import com.qvcsos.server.datamodel.UserProjectRole;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public interface UserProjectRoleDAO {

    /**
     * Find the roles for a given user/project.
     * @param userId the user id.
     * @param projectId the project id.
     * @return a List of user project roles.
     */
    List<UserProjectRole> findByUserAndProject(Integer userId, Integer projectId);

    /**
     * Find the user project role given user, project, and role type.
     * @param userId the user id.
     * @param projectId the project id.
     * @param roleTypeId the role type id.
     * @return the matching UserProjectRole row.
     */
    UserProjectRole findByUserProjectAndRoleType(Integer userId, Integer projectId, Integer roleTypeId);

    List<UserProjectRole> findByProject(Integer projectId);

    /**
     * Insert a UserProjectRole.
     *
     * @param userProjectRole what will be inserted.
     * @return id of the inserted row.
     * @throws SQLException if there is a problem.
     */
    Integer insert(UserProjectRole userProjectRole) throws SQLException;

    boolean delete(Integer id) throws SQLException;

}
