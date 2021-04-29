/*
 * Copyright 2019 JimVoris.
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
package com.qumasoft.server.dataaccess;

import com.qumasoft.server.datamodel.CommitHistory;
import java.sql.SQLException;

/**
 *
 * @author JimVoris
 */
public interface CommitHistoryDAO {

    // Insert
    /**
     * Insert a row in the BRANCH table.
     *
     * @param commitHistory the commitHistory to create. Note that we do <b>not</b> honor any id passed in with the commitHistory object. A new
     * commitHistory will <b>always</b> be created.
     *
     * @throws SQLException thrown if there is a problem.
     */
    void insert(CommitHistory commitHistory) throws SQLException;
    // Find by date descending --- the idea is to find the most recent list of commits, returning their commit id, commit date, and commit message.

}
