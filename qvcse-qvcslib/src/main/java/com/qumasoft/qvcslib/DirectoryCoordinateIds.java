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
    private final int directoryId;
    private final int directoryLocationId;
    private final Map<Integer, String> childWriteableBranchMap;
    private final DirectoryCoordinate directoryCoordinate;

    public DirectoryCoordinateIds(int pId, int bId, int dId, int dlId, DirectoryCoordinate dc, Map<Integer, String> cwbm) {
        this.projectId = pId;
        this.branchId = bId;
        this.directoryId = dId;
        this.directoryLocationId = dlId;
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
    public int getDirectoryId() {
        return directoryId;
    }

    /**
     * @return the directoryLocationId
     */
    public int getDirectoryLocationId() {
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
}
