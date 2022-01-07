/*   Copyright 2004-2021 Jim Voris
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
 * Information for determining the type of merge to perform.
 *  We use four flag to capture the possible alternatives, and enumerate those alternatives here:
 * <ul>
 * <li>0000 -- no renames or moves.</li>
 * <li>0001 -- name changed.</li>
 * <li>0010 -- location changed.</li>
 * <li>0011 -- name changed && location changed.</li>
 * <li>xxxx -- created on branch.</li>
 * <li>xxxx -- deleted on branch.</li>
 * </ul>
 * @author Jim Voris
 */
public class InfoForMerge {

    private final String parentAppendedPath;
    private final String parentShortWorkfileName;
    private final boolean nameChangeFlag;
    private final boolean locationChangeFlag;
    private final boolean createdOnBranchFlag;
    private final boolean deletedOnBranchFlag;

    /**
     * Constructor that makes this invariant object.
     *
     * @param parentPath the parent appended path.
     * @param parentShortName the parent short workfile name.
     * @param nameChangeFlg the parent file was renamed.
     * @param locationChangeFlg the parent file was moved.
     * @param createdOnBranchFlg the file was created on the branch.
     * @param deletedOnBranchFlg the file was deleted on the branch.
     */
    public InfoForMerge(final String parentPath, final String parentShortName, boolean nameChangeFlg, boolean locationChangeFlg, boolean createdOnBranchFlg, boolean deletedOnBranchFlg) {
        this.parentAppendedPath = parentPath;
        this.parentShortWorkfileName = parentShortName;
        this.nameChangeFlag = nameChangeFlg;
        this.locationChangeFlag = locationChangeFlg;
        this.createdOnBranchFlag = createdOnBranchFlg;
        this.deletedOnBranchFlag = deletedOnBranchFlg;
    }

    /**
     * Get the parent appended path.
     * @return the parent appended path.
     */
    public String getParentAppendedPath() {
        return this.parentAppendedPath;
    }

    /**
     * Get the parent short workfile name.
     * @return the parent short workfile name.
     */
    public String getParentShortWorkfileName() {
        return this.parentShortWorkfileName;
    }

    /**
     * Get the parent renamed flag.
     * @return the parent renamed flag.
     */
    public boolean getNameChangedFlag() {
        return this.nameChangeFlag;
    }

    /**
     * Get the parent moved flag.
     * @return the parent moved flag.
     */
    public boolean getLocationChangedFlag() {
        return this.locationChangeFlag;
    }

    /**
     * Get the created on branch flag.
     * @return the created on branch flag.
     */
    public boolean getCreatedOnBranchFlag() {
        return this.createdOnBranchFlag;
    }

    /**
     * Get the deleted on branch flag.
     * @return the deleted on branch flag.
     */
    public boolean getDeletedOnBranchFlag() {
        return this.deletedOnBranchFlag;
    }
}
