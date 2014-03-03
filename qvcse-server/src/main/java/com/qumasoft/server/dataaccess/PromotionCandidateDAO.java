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

import com.qumasoft.server.datamodel.PromotionCandidate;
import java.sql.SQLException;

/**
 * Promotion candidate DAO.
 *
 * @author Jim Voris
 */
public interface PromotionCandidateDAO {

    /**
     * Insert a promotionCandidate if it is missing from the database.
     *
     * @param promotionCandidate the promotionCandidate to insert.
     * @throws SQLException if there is a problem.
     */
    void insertIfMissing(PromotionCandidate promotionCandidate) throws SQLException;

    /**
     * Delete a promotionCandidate.
     *
     * @param promotionCandidate the promotionCandidate to delete.
     * @throws SQLException if there is a problem.
     */
    void delete(PromotionCandidate promotionCandidate) throws SQLException;
}
