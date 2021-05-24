/*   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.server;

import com.qumasoft.server.dataaccess.BranchDAO;
import com.qumasoft.server.dataaccess.ProjectDAO;
import com.qumasoft.server.dataaccess.impl.BranchDAOImpl;
import com.qumasoft.server.dataaccess.impl.ProjectDAOImpl;
import com.qumasoft.server.datamodel.Branch;
import com.qumasoft.server.datamodel.Project;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton database cache to cache the project and branch id's so we don't need a db round trip to look these guys up.
 *
 * @author Jim Voris
 */
public final class DatabaseCache {

    private static final DatabaseCache DATABASE_CACHE = new DatabaseCache();
    private final ProjectDAO projectDAO;
    private final BranchDAO branchDAO;
    private final Map<String, Integer> projectMap;
    private final Map<String, Integer> branchMap;

    private DatabaseCache() {
        String schemaName = QVCSEnterpriseServer.getDatabaseManager().getSchemaName();
        projectDAO = new ProjectDAOImpl(schemaName);
        branchDAO = new BranchDAOImpl(schemaName);
        projectMap = new ConcurrentHashMap<>();
        branchMap = new ConcurrentHashMap<>();
    }

    /**
     * Get the singleton instance of the Database cache.
     *
     * @return the singleton instance of the Database cache.
     */
    public static DatabaseCache getInstance() {
        return DATABASE_CACHE;
    }

    /**
     * Get the project id for the given project name.
     *
     * @param projectName the project name.
     * @return the project id for the given project name. null if it doesn't exist in the database.
     */
    public Integer getProjectId(final String projectName) {
        Integer projectId = projectMap.get(projectName);
        if (projectId == null) {
            Project project = projectDAO.findByProjectName(projectName);
            if (project != null) {
                projectId = project.getProjectId();
                projectMap.put(projectName, projectId);
            }
        }
        return projectId;
    }

    /**
     * Get the branch id for the given project/branch name.
     *
     * @param projectId the project id.
     * @param branchName the branch name.
     * @return the branch id for the given project/branch name. null if it doesn't exist in the database.
     */
    public Integer getBranchId(final Integer projectId, final String branchName) {
        String branchKey = buildBranchKey(projectId, branchName);
        Integer branchId = branchMap.get(branchKey);
        if (branchId == null) {
            Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
            if (branch != null) {
                branchId = branch.getBranchId();
                branchMap.put(branchKey, branchId);
            }
        }
        return branchId;
    }

    /**
     * Build the key we use for the branch map.
     *
     * @param projectId the project id.
     * @param branchName the name of the branch.
     * @return the key we use for our map of branch id's.
     */
    private String buildBranchKey(Integer projectId, String branchName) {
        return projectId.toString() + ":" + branchName;
    }
}
