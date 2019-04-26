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
package com.qumasoft.qvcslib;

/**
 * Helper class that encapsulates the results of a promote file response.
 * @author Jim Voris
 */
public class PromoteFileResults {

    private final String projectName;
    private final String branchName;
    private final String appendedPath;
    private final String shortWorkfileName;
    private MergeType mergeType;
    private final SkinnyLogfileInfo skinnyLogfileInfo;
    private final byte[] mergedResultBuffer;
    private final byte[] commonAncestorBuffer;
    private final byte[] branchParentTipRevisionBuffer;
    private final byte[] branchTipRevisionBuffer;
    private final LogfileInfo logfileInfo;
    /**
     * Promote file conflict results.
     *
     * @param project the project name.
     * @param branch the branch name.
     * @param path the appended path.
     * @param shortName the short workfile name.
     * @param type the type of merge.
     * @param skinnyInfo the skinny logfile info.
     * @param mergedBuffer the merged result buffer.
     * @param ancestorBuffer the common ancestor buffer (may be null).
     * @param branchParentTipBuffer the branch parent tip revision buffer (may be null).
     * @param branchTipBuffer the branch tip revision buffer (may be null).
     * @param info the logfile info. (may be null if we don't need to expand keywords).
     */
    // <editor-fold defaultstate="expanded" desc="Turn off checkstyle for too many arguments.">
    public PromoteFileResults(String project, String branch, String path, String shortName, MergeType type, SkinnyLogfileInfo skinnyInfo,
                              byte[] mergedBuffer, byte[] ancestorBuffer,
                              byte[] branchParentTipBuffer, byte[] branchTipBuffer, LogfileInfo info) {
    // </editor-fold>
        this.projectName = project;
        this.branchName = branch;
        this.appendedPath = path;
        this.shortWorkfileName = shortName;
        this.mergeType = type;
        this.skinnyLogfileInfo = skinnyInfo;
        this.mergedResultBuffer = mergedBuffer;
        this.commonAncestorBuffer = ancestorBuffer;
        this.branchParentTipRevisionBuffer = branchParentTipBuffer;
        this.branchTipRevisionBuffer = branchTipBuffer;
        this.logfileInfo = info;
    }
    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Get the skinny logfile info.
     * @return the skinny logfile info.
     */
    public SkinnyLogfileInfo getSkinnyLogfileInfo() {
        return skinnyLogfileInfo;
    }

    /**
     * Get the merged result buffer.
     * @return the merged result buffer.
     */
    public byte[] getMergedResultBuffer() {
        return mergedResultBuffer;
    }

    /**
     * Get the common ancestor buffer.
     * @return the common ancestor buffer.
     */
    public byte[] getCommonAncestorBuffer() {
        return commonAncestorBuffer;
    }

    /**
     * Get the branch parent tip revision buffer.
     * @return the branch parent tip revision buffer.
     */
    public byte[] getBranchParentTipRevisionBuffer() {
        return branchParentTipRevisionBuffer;
    }

    /**
     * Get the branch tip revision buffer.
     * @return the branch tip revision buffer.
     */
    public byte[] getBranchTipRevisionBuffer() {
        return branchTipRevisionBuffer;
    }

    /**
     * Get the logfile info.
     * @return the logfile info.
     */
    public LogfileInfo getLogfileInfo() {
        return logfileInfo;
    }

    /**
     * Get the merge type.
     * @return the merge type.
     */
    public MergeType getMergeType() {
        return mergeType;
    }

    /**
     * Set the merge type.
     * @param type the merge type.
     */
    public void setMergeType(MergeType type) {
        this.mergeType = type;
    }
}
