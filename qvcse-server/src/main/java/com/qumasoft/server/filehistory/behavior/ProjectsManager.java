/*
 * Copyright 2014 JimVoris.
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
package com.qumasoft.server.filehistory.behavior;

import java.util.List;

/**
 * Projects Manager interface. Define the behavior of the projects manager.
 * @author Jim Voris
 */
public interface ProjectsManager {
    /**
     * Get a list of the projects on the server.
     * @return a list of projects on the server.
     */
    List<Project> getProjects();

    /**
     * Get the given project.
     * @param projectName the name of the project to fetch.
     * @return the given project object.
     */
    Project getProject(String projectName);

    /**
     * Delete the given project.
     * @param projectName the name of the project to delete.
     */
    void deleteProject(String projectName);
}
