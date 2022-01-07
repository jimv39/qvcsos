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
package com.qumasoft.qvcslib;

import java.util.Properties;

/**
 * Branch information for use on the client.
 *
 * @author Jim Voris.
 */
public class ClientBranchInfo implements java.io.Serializable {

    private Integer projectId;
    private String branchName;
    private Integer branchId;
    private Properties branchProperties;

    /**
     * @return the branchName
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * @param name the branchName to set
     */
    public void setBranchName(String name) {
        this.branchName = name;
    }

    /**
     * @return the branchId
     */
    public Integer getBranchId() {
        return branchId;
    }

    /**
     * @param id the branchId to set
     */
    public void setBranchId(Integer id) {
        this.branchId = id;
    }

    /**
     * @return the branchProperties
     */
    public Properties getBranchProperties() {
        return branchProperties;
    }

    /**
     * @param properties the branchProperties to set
     */
    public void setBranchProperties(Properties properties) {
        this.branchProperties = properties;
    }

    /**
     * @param id the projectId to set.
     */
    public void setProjectId(Integer id) {
        this.projectId = id;
    }

    /**
     * @return the projectId
     */
    public Integer getProjectId() {
        return projectId;
    }
}
