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
 * Role types. There can be any number of different role types. By default there are several created automatically when a server is started for the very first time. The default
 * types are:
 * <ul>
 * <li>ADMIN</li>
 * <li>PROJECT_ADMIN</li>
 * <li>READER</li>
 * <li>WRITER</li>
 * <li>DEVELOPER</li>
 * <li>CEMETERY_ADMIN</li>
 * </ul>
 * @author Jim Voris
 */
public class RoleType {
    private Integer roleTypeId;
    private String roleName;

    /**
     * @return the Id
     */
    public Integer getId() {
        return roleTypeId;
    }

    /**
     * @param id the roleTypeId to set
     */
    public void setId(Integer id) {
        this.roleTypeId = id;
    }

    /**
     * @return the roleName
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Get the role type.
     * @return the role type.
     */
    public String getRoleType() {
        return roleName;
    }


    /**
     * @param name the roleName to set
     */
    public void setRoleName(String name) {
        this.roleName = name;
    }

}
