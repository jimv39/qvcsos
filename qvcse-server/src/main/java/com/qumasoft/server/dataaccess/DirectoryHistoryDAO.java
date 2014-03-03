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

import com.qumasoft.server.datamodel.DirectoryHistory;
import java.util.Date;
import java.util.List;

/**
 * Directory history DAO interface.
 *
 * @author Jim Voris
 */
public interface DirectoryHistoryDAO {

    /**
     * Find by id.
     *
     * @param directoryHistoryId the primary key.
     * @return the associated directory history instance, or null if not found.
     */
    DirectoryHistory findById(Integer directoryHistoryId);

    /**
     * Find the list of directories that have the given directory as their parent.
     *
     * @param branchId the id of the branch we're going to look on.
     * @param directoryId the parent directory id.
     * @param viewDate the date for the date based view.
     * @return a list of directories that are children of the given directory updated on or before the given date.
     */
    List<DirectoryHistory> findChildDirectoriesOnOrBeforeViewDate(Integer branchId, Integer directoryId, Date viewDate);

    /**
     * Find the list of DirectoryHistory records for the given directory.
     * @param directoryId the directory id of for which we want history.
     * @return the list of history records for the given directory.
     */
    List<DirectoryHistory> findHistoryForDirectoryId(Integer directoryId);
}
