/*
 * Copyright 2021 Jim Voris.
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
package com.qvcsos.server.dataaccess;

import com.qvcsos.server.datamodel.DirectoryLocation;
import java.sql.SQLException;

/**
 *
 * @author Jim Voris
 */
public interface DirectoryLocationDAO {

    DirectoryLocation findById(Integer directoryLocationId);

    DirectoryLocation findByDirectoryId(Integer rootDirectoryId);

    /**
     * Find the directory location where we know the parent directory location id, and the branch id.
     * @param branchId the branch id.
     * @param parentDirectoryLocationId the parent directory location id.
     * @param segment the 'name' of the directory location that we are looking for.
     * @return the DirectoryLocation, or null if not found.
     */
    DirectoryLocation findChildDirectoryLocation(Integer branchId, Integer parentDirectoryLocationId, String segment);

    Integer insert(DirectoryLocation directoryLocation) throws SQLException;

    boolean rename(Integer id, Integer commitId, String newDirectoryName) throws SQLException;

    boolean move(Integer id, Integer commitId, Integer targetParentDirectoryLocationId) throws SQLException;

    boolean delete(Integer id, Integer commitId) throws SQLException;


}
