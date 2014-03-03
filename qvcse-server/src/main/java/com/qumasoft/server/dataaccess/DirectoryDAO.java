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

import com.qumasoft.server.datamodel.Directory;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Directory DAO interface.
 * @author Jim Voris
 */
public interface DirectoryDAO {

    /**
     * Find directory by directory ID and branch ID.
     *
     * @param branchId the branch id.
     * @param directoryId the directory id.
     * @return the directory if found; null if not found.
     */
    Directory findById(Integer branchId, Integer directoryId);

    /**
     * Find directory by branch id and appended path.
     *
     * @param branchId the branch id.
     * @param appendedPath the appended path.
     * @return the directory if found; null if not found.
     */
    Directory findByAppendedPath(Integer branchId, String appendedPath);

    /**
     * Find the list of directories on a given branch.
     *
     * @param branchId the id of the branch we're going to look on.
     * @return a list of directories on the given branch.
     */
    List<Directory> findByBranchId(Integer branchId);

    /**
     * Find the list of directories that have the given directory as their parent.
     *
     * @param branchId the id of the branch we're going to look on.
     * @param directoryId the parent directory id.
     * @return a list of directories that are children of the given directory.
     */
    List<Directory> findChildDirectories(Integer branchId, Integer directoryId);

    /**
     * Find the list of directories that have the given directory as their parent.
     *
     * @param branchId the id of the branch we're going to look on.
     * @param directoryId the parent directory id.
     * @param viewDate the date for the date based view.
     * @return a list of directories that are children of the given directory updated on or before the given date.
     */
    List<Directory> findChildDirectoriesOnOrBeforeViewDate(Integer branchId, Integer directoryId, Date viewDate);

    /**
     * Insert a row in the DIRECTORY table.
     *
     * @param directory the directory to create.
     *
     * @throws SQLException thrown if there is a problem.
     */
    void insert(Directory directory) throws SQLException;

    /**
     * Update a row in the DIRECTORY table.
     *
     * @param directory the directory to update.
     * @param deletedFlag the current state of the deleted flag.
     *
     * @throws SQLException thrown if there is a problem.
     */
    void update(Directory directory, boolean deletedFlag) throws SQLException;
}
