/*   Copyright 2004-2019 Jim Voris
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
 * Combo model for the different types of views/branches that we support.
 * @author Jim Voris
 */
public class ViewOrBranchComboModel extends DefaultComboBoxModel<String> {

    private static final long serialVersionUID = 10L;
    private final Map<String, Integer> viewOrBranchTypeMap = new TreeMap<>();
    // Here are the different types of views/branches that we support.
    /** A read-only date based view. */
    public static final int READ_ONLY_DATE_BASED_VIEW_TYPE = 1;
    /** A read/write translucent view. */
    public static final int TRANSLUCENT_BRANCH_TYPE = 2;
    /** A read/write opaque view. */
    public static final int OPAQUE_BRANCH_TYPE = 3;
    /** Describe the read-only date based view. */
    public static final String READ_ONLY_DATE_BASED_VIEW = "Read-only date-based view";
    /** Describe the translucent branch. */
    public static final String TRANSLUCENT_BRANCH = "Translucent branch";
    /** Describe the opaque branch. */
    public static final String OPAQUE_BRANCH = "Opaque branch";

    /**
     * Default constructor.
     */
    public ViewOrBranchComboModel() {
        addElement(READ_ONLY_DATE_BASED_VIEW);
        viewOrBranchTypeMap.put(READ_ONLY_DATE_BASED_VIEW, Integer.valueOf(READ_ONLY_DATE_BASED_VIEW_TYPE));

        addElement(TRANSLUCENT_BRANCH);
        viewOrBranchTypeMap.put(TRANSLUCENT_BRANCH, Integer.valueOf(TRANSLUCENT_BRANCH_TYPE));

        /* TODO. Not Implemented yet.
        addElement(OPAQUE_BRANCH);
        viewOrBranchTypeMap.put(OPAQUE_BRANCH, Integer.valueOf(OPAQUE_BRANCH_TYPE));
        */
    }

    /**
     * Lookup the view or branch type, given the name of the type of view or branch.
     *
     * @param viewOrBranchName the name of the type of view or branch.
     * @return an integer that represents the constant associated with the given view or branch type.
     */
    public int getViewOrBranchType(String viewOrBranchName) {
        return viewOrBranchTypeMap.get(viewOrBranchName);
    }
}
