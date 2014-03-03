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
package com.qumasoft.server.dataaccess;

import com.qumasoft.server.datamodel.BranchType;
import java.util.List;

/**
 * Branch Type DAO interface definition.
 *
 * @author Jim Voris
 */
public interface BranchTypeDAO {

    /**
     * Find the branch type by branch type id.
     *
     * @param branchTypeId the branch type id.
     * @return the BranchType with the given id, or null if the branchType is not found.
     */
    BranchType findById(Integer branchTypeId);

    /**
     * Find all branch types.
     *
     * @return a List of all the branch types.
     */
    List<BranchType> findAll();
}
