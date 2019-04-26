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

import com.qumasoft.server.datamodel.Project;
import java.sql.SQLException;
import java.util.List;

/**
 * Project DAO interface.
 *
 * @author Jim Voris
 */
public interface ProjectDAO {

    /**
     * Find the project by project id.
     *
     * @param projectId the project id.
     * @return the project with the given id, or null if the project is not found.
     */
    Project findById(Integer projectId);

    /**
     * Find the project by project name.
     *
     * @param projectName the name of the project to find.
     * @return the project with the given name, or null if the project is not found.
     */
    Project findByProjectName(String projectName);

    /**
     * Find all projects.
     *
     * @return a List of all the projects.
     */
    List<Project> findAll();

    /**
     * Insert a project.
     *
     * @param project the project to insert. We ignore the project id.
     * @throws SQLException if there is a problem.
     */
    void insert(Project project) throws SQLException;

    /**
     * Delete a project. This does not cascade, so child objects must be deleted before deleting a project.
     *
     * @param project the project to delete.
     * @throws SQLException if there is a problem.
     */
    void delete(Project project) throws SQLException;
}
