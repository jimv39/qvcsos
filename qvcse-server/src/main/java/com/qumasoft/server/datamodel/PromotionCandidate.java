//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.server.datamodel;

/**
 * Promotion candidate.
 *
 * @author Jim Voris
 */
public class PromotionCandidate {
    /*
     * The SQL snippet used to create the PromotionCandidate table:
     * FILE_ID INT NOT NULL, BRANCH_ID INT NOT NULL, CONSTRAINT PROMOTION_PK PRIMARY KEY (FILE_ID, BRANCH_ID));
     */

    private Integer fileId;
    private Integer branchId;

    /**
     * Create a promotion candidate instance.
     * @param fId the file id.
     * @param bId the branch id.
     */
    public PromotionCandidate(final Integer fId, final Integer bId) {
        this.fileId = fId;
        this.branchId = bId;
    }

    /**
     * Get the file id.
     * @return the file id.
     */
    public Integer getFileId() {
        return fileId;
    }

    /**
     * Set the file id.
     * @param fId the file id.
     */
    public void setFileId(Integer fId) {
        this.fileId = fId;
    }

    /**
     * Get the branch id.
     * @return the branch id.
     */
    public Integer getBranchId() {
        return branchId;
    }

    /**
     * Set the branch id.
     * @param bId the branch id.
     */
    public void setBranchId(Integer bId) {
        this.branchId = bId;
    }
}
