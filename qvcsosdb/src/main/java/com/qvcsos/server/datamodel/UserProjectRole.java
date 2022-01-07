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
package com.qvcsos.server.datamodel;

/**
 *
 * @author Jim Voris
 */
public class UserProjectRole {
    private Integer userProjectRoleId;
    private Integer userId;
    private Integer projectId;
    private Integer roleTypeId;

    /**
     * @return the id
     */
    public Integer getId() {
        return userProjectRoleId;
    }

    /**
     * @param id the userProjectRoleId to set
     */
    public void setId(Integer id) {
        this.userProjectRoleId = id;
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param id the userId to set
     */
    public void setUserId(Integer id) {
        this.userId = id;
    }

    /**
     * @return the projectId
     */
    public Integer getProjectId() {
        return projectId;
    }

    /**
     * @param id the projectId to set
     */
    public void setProjectId(Integer id) {
        this.projectId = id;
    }

    /**
     * @return the roleTypeId
     */
    public Integer getRoleTypeId() {
        return roleTypeId;
    }

    /**
     * @param id the roleTypeId to set
     */
    public void setRoleTypeId(Integer id) {
        this.roleTypeId = id;
    }

}
