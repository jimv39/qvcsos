/*
 * Copyright 2021-2022 Jim Voris.
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

import java.util.Map;

/**
 *
 * @author Jim Voris
 */
public class DirectoryCoordinateIds {

    private final int projectId;
    private final int branchId;
    private final Integer directoryId;
    private final Integer directoryLocationId;
    private Integer provisionalDirectoryLocationId = null;
    private final Map<Integer, String> childWriteableBranchMap;
    private final DirectoryCoordinate directoryCoordinate;

    /**
     * Constructor for a DirectoryCoordinateIds object.
     *
     * @param pId the project id.
     * @param bId the branch id.
     * @param dId the directory id.
     * @param dlId the directory location id.
     * @param dc the directory coordinates.
     * @param cwbm a map keyed by branch id with the branch name as the value.
     */
    public DirectoryCoordinateIds(int pId, int bId, int dId, int dlId, DirectoryCoordinate dc, Map<Integer, String> cwbm) {
        this.projectId = pId;
        this.branchId = bId;
        this.directoryId = dId;
        this.directoryLocationId = dlId;
        this.directoryCoordinate = dc;
        this.childWriteableBranchMap = cwbm;
    }

    /**
     * Constructor for a DirectoryCoordinateIds for provisional directories.
     *
     * @param pId the project id.
     * @param bId the branch id.
     * @param dId the directory id.
     * @param dc the directory coordinates.
     * @param cwbm a map keyed by branch id with the branch name as the value.
     */
    public DirectoryCoordinateIds(int pId, int bId, int dId, DirectoryCoordinate dc, Map<Integer, String> cwbm) {
        this.projectId = pId;
        this.branchId = bId;
        this.directoryId = dId;
        this.directoryLocationId = null;
        this.directoryCoordinate = dc;
        this.childWriteableBranchMap = cwbm;
    }

    /**
     * @return the projectId
     */
    public int getProjectId() {
        return projectId;
    }

    /**
     * @return the branchId
     */
    public int getBranchId() {
        return branchId;
    }

    /**
     * @return the directoryId
     */
    public Integer getDirectoryId() {
        return directoryId;
    }

    /**
     * @return the directoryLocationId
     */
    public Integer getDirectoryLocationId() {
        return directoryLocationId;
    }

    /**
     * @return the directory coordinate.
     */
    public DirectoryCoordinate getDirectoryCoordinate() {
        return directoryCoordinate;
    }

    /**
     * @return the childWriteableBranchMap
     */
    public Map<Integer, String> getChildWriteableBranchMap() {
        return childWriteableBranchMap;
    }

    /**
     * @return the provisionalDirectoryLocationId
     */
    public Integer getProvisionalDirectoryLocationId() {
        return provisionalDirectoryLocationId;
    }

    /**
     * @param id the provisionalDirectoryLocationId to set
     */
    public void setProvisionalDirectoryLocationId(Integer id) {
        this.provisionalDirectoryLocationId = id;
    }
}
