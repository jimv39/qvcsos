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
package com.qumasoft.guitools.qwin;

import java.util.Map;
import java.util.TreeMap;
import javax.swing.DefaultComboBoxModel;

/**
 * Combo model for the different types of branches that we support.
 * @author Jim Voris
 */
public class BranchComboModel extends DefaultComboBoxModel<String> {

    private static final long serialVersionUID = 10L;
    private final Map<String, Integer> branchTypeMap = new TreeMap<>();

    // Here are the different types of branches that we support.
    /** A read-only date based branch. */
    public static final int READ_ONLY_TAG_BASED_BRANCH_TYPE = 1;
    /** A read/write feature branch. */
    public static final int FEATURE_BRANCH_TYPE = 2;
    /** A read/write release branch. */
    public static final int RELEASE_BRANCH_TYPE = 3;

    /** Describe the read-only date based branch. */
    public static final String READ_ONLY_TAG_BASED_BRANCH = "Read-only tag based branch";
    /** Describe the feature branch. */
    public static final String FEATURE_BRANCH = "Feature branch";
    /** Describe the opaque branch. */
    public static final String RELEASE_BRANCH = "Release branch";

    /**
     * Default constructor.
     */
    public BranchComboModel() {

        addElement(FEATURE_BRANCH);
        branchTypeMap.put(FEATURE_BRANCH, FEATURE_BRANCH_TYPE);

        addElement(READ_ONLY_TAG_BASED_BRANCH);
        branchTypeMap.put(READ_ONLY_TAG_BASED_BRANCH, READ_ONLY_TAG_BASED_BRANCH_TYPE);

        addElement(RELEASE_BRANCH);
        branchTypeMap.put(RELEASE_BRANCH, RELEASE_BRANCH_TYPE);
    }

    /**
     * Lookup the branch type, given the name of the type of branch.
     *
     * @param branchTypeName the name of the type of branch.
     * @return an integer that represents the constant associated with the given branch type.
     */
    public int getBranchType(String branchTypeName) {
        return branchTypeMap.get(branchTypeName);
    }
}
