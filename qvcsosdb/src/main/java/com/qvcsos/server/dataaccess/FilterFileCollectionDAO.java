/*
 * Copyright 2023 Jim Voris.
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

import com.qvcsos.server.datamodel.FilterFileCollection;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris.
 */
public interface FilterFileCollectionDAO {

    /**
     * Find the FilterCollection by id.
     *
     * @param id the filter collection id.
     * @return the FilterCollection with the given id, or null if the FilterCollection is not found.
     */
    FilterFileCollection findById(Integer id);

    /**
     * Find all collections by user id. This will include the built-in non-user specific filter collections as well.
     * @param userId the user id.
     * @return a list of filter collections for the given user.
     */
    List<FilterFileCollection> findAllByUserId(Integer userId);

    /**
     * Insert a new filter collection.
     * @param filterCollection the filter collection to insert.
     * @return the id of the newly inserted filter collection.
     * @throws SQLException if there are SQL problems.
     */
    Integer insert(FilterFileCollection filterCollection) throws SQLException;

    /**
     * Delete the given filter collection. Built in collections will not be deleted, nor will other user's filter collections.
     * @param filterCollectionId the id of the collection to be deleted.
     * @throws SQLException if there are SQL problems.
     */
    void delete(Integer filterCollectionId) throws SQLException;
}
