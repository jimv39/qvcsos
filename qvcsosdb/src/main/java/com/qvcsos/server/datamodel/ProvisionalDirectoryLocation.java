/*
 * Copyright 2023 Jim Voris.
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
 * @author Jim Voris.
 */
public class ProvisionalDirectoryLocation {

    private Integer provisionalDirectoryLocationId;
    private Integer branchId;
    private Integer directoryId;
    private Integer parentDirectoryLocationId;
    private Integer parentProvisionalDirectoryLocationId;
    private Integer userId;
    private String directorySegmentName;
    private String appendedPath;

    /**
     * @return the provisionalDirectoryLocationId
     */
    public Integer getId() {
        return provisionalDirectoryLocationId;
    }

    /**
     * @param pdLocationId the provisionalDirectoryLocationId to set
     */
    public void setId(Integer pdLocationId) {
        this.provisionalDirectoryLocationId = pdLocationId;
    }

    /**
     * @return the branchId
     */
    public Integer getBranchId() {
        return branchId;
    }

    /**
     * @param brnchId the branchId to set
     */
    public void setBranchId(Integer brnchId) {
        this.branchId = brnchId;
    }

    /**
     * @return the provisionalDirectoryId
     */
    public Integer getDirectoryId() {
        return directoryId;
    }

    /**
     * @param pdId the provisionalDirectoryId to set
     */
    public void setDirectoryId(Integer pdId) {
        this.directoryId = pdId;
    }

    /**
     * @return the parentDirectoryLocationId
     */
    public Integer getParentDirectoryLocationId() {
        return parentDirectoryLocationId;
    }

    /**
     * @param pdlId the parentDirectoryLocationId to set
     */
    public void setParentDirectoryLocationId(Integer pdlId) {
        this.parentDirectoryLocationId = pdlId;
    }

    /**
     * @return the provisionalParentDirectoryLocationId
     */
    public Integer getParentProvisionalDirectoryLocationId() {
        return parentProvisionalDirectoryLocationId;
    }

    /**
     * @param ppdlId the provisionalParentDirectoryLocationId to set
     */
    public void setParentProvisionalDirectoryLocationId(Integer ppdlId) {
        this.parentProvisionalDirectoryLocationId = ppdlId;
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param uId the userId to set
     */
    public void setUserId(Integer uId) {
        this.userId = uId;
    }

    /**
     * @return the directorySegmentName
     */
    public String getDirectorySegmentName() {
        return directorySegmentName;
    }

    /**
     * @param dirSegmentName the directorySegmentName to set
     */
    public void setDirectorySegmentName(String dirSegmentName) {
        this.directorySegmentName = dirSegmentName;
    }

    /**
     * @return the appendedPath
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * @param path the appendedPath to set
     */
    public void setAppendedPath(String path) {
        this.appendedPath = path;
    }

}
