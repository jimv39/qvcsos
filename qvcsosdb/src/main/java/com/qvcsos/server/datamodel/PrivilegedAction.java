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
public class PrivilegedAction {
    private Integer actionId;
    private String actionName;
    private Boolean adminOnlyFlag;

    /**
     * @return the Id
     */
    public Integer getId() {
        return actionId;
    }

    /**
     * @param id the actionId to set
     */
    public void setId(Integer id) {
        this.actionId = id;
    }

    /**
     * @return the actionName
     */
    public String getActionName() {
        return actionName;
    }

    /**
     * @param action the actionName to set
     */
    public void setActionName(String action) {
        this.actionName = action;
    }

    /**
     * @return the adminOnlyFlag
     */
    public Boolean getAdminOnlyFlag() {
        return adminOnlyFlag;
    }

    /**
     * @param flag the adminOnlyFlag to set
     */
    public void setAdminOnlyFlag(Boolean flag) {
        this.adminOnlyFlag = flag;
    }
}
