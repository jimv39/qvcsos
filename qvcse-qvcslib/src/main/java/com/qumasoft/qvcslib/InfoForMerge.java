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
 * Information for determining the type of merge to perform.
 *  We use four flag to capture the possible alternatives, and enumerate those alternatives here:
 * <ul>
 * <li>0000 -- no renames or moves.</li>
 * <li>0001 -- parent rename.</li>
 * <li>0010 -- parent move </li>
 * <li>0011 -- parent rename && parent move.</li>
 * <li>0100 -- branch rename.</li>
 * <li>0101 -- branch rename && parent rename.</li>
 * <li>0110 -- parent move && branch rename.</li>
 * <li>0111 -- parent rename && parent move && branch rename.</li>
 * <li>1000 -- branch move.</li>
 * <li>1001 -- branch move && parent rename.</li>
 * <li>1010 -- branch move && parent move.</li>
 * <li>1011 -- branch move && parent move && parent rename.</li>
 * <li>1100 -- branch move && branch rename.</li>
 * <li>1101 -- branch move && branch rename && parent rename.</li>
 * <li>1110 -- branch move && branch rename && parent move.</li>
 * <li>1111 -- branch move && branch rename && parent move && parent rename.</li>
 * </ul>
 * @author Jim Voris
 */
public class InfoForMerge {

    private final String parentAppendedPath;
    private final String parentShortWorkfileName;
    private final boolean parentRenamedFlag;
    private final boolean branchRenamedFlag;
    private final boolean parentMovedFlag;
    private final boolean branchMovedFlag;
    private final boolean branchCreatedFlag;

    /**
     * Constructor that makes this invariant object.
     *
     * @param parentPath the parent appended path.
     * @param parentShortName the parent short workfile name.
     * @param parentRenamedFlg the parent file was renamed.
     * @param branchRenamedFlg the branch file was renamed.
     * @param parentMovedFlg the parent file was moved.
     * @param branchMovedFlg the branch file was moved.
     * @param branchCreatedFlg the file was created on the branch.
     */
    public InfoForMerge(final String parentPath, final String parentShortName, boolean parentRenamedFlg, boolean branchRenamedFlg, boolean parentMovedFlg, boolean branchMovedFlg,
                        boolean branchCreatedFlg) {
        this.parentAppendedPath = parentPath;
        this.parentShortWorkfileName = parentShortName;
        this.parentRenamedFlag = parentRenamedFlg;         // 0001
        this.parentMovedFlag = parentMovedFlg;             // 0010
        this.branchRenamedFlag = branchRenamedFlg;         // 0100
        this.branchMovedFlag = branchMovedFlg;             // 1000
        this.branchCreatedFlag = branchCreatedFlg;
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
    public boolean getParentRenamedFlag() {
        return this.parentRenamedFlag;
    }

    /**
     * Get the parent moved flag.
     * @return the parent moved flag.
     */
    public boolean getParentMovedFlag() {
        return this.parentMovedFlag;
    }

    /**
     * Get the branch renamed flag.
     * @return the branch renamed flag.
     */
    public boolean getBranchRenamedFlag() {
        return this.branchRenamedFlag;
    }

    /**
     * Get the branch moved flag.
     * @return the branch moved flag.
     */
    public boolean getBranchMovedFlag() {
        return this.branchMovedFlag;
    }

    /**
     * Get the branch created flag.
     * @return the branch created flag.
     */
    public boolean getBranchCreatedFlag() {
        return this.branchCreatedFlag;
    }
}
