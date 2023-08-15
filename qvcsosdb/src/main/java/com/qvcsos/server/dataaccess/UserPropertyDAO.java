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

import com.qvcsos.server.datamodel.UserProperty;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris.
 */
public interface UserPropertyDAO {
    /**
     * Find the UserProperty with the given id.
     * @param id the id of the UserProperty.
     * @return the UserProperty with the given id, or null if not found.
     */
    UserProperty findById(Integer id);

    /**
     * Find the UserProperty with the given userNameAndComputer and propertyName.
     * @param userAndComputer the userName and the computer they're using (a sort of compound key composed in code).
     * @param propertyName the property name to find for the given user.
     * @return the UserProperty for the given userId and propertyName, null if not found.
     */
    UserProperty findByUserAndComputerAndPropertyName(String userAndComputer, String propertyName);

    /**
     * Find the properties for a given user on a given computer.
     * @param userAndComputer the userName and the computer they're using (a sort of compound key composed in code).
     * @return a List of UserProperties for the given userId.
     */
    List<UserProperty> findUserProperties(String userAndComputer);

    /**
     * Store an updated user property to the database.
     * @param updatedUserProperty the UserProperty with its new value.
     * @return the id of the user property.
     * @throws SQLException if there is a problem.
     */
    Integer updateUserProperty(UserProperty updatedUserProperty) throws SQLException;

    /**
     * Insert a user property.
     * @param userProperty
     * @return the id of the inserted row.
     * @throws SQLException if there is a problem.
     */
    Integer insert(UserProperty userProperty) throws SQLException;

    /**
     * Delete the given user property with the given id.
     * @param id the id of the user property that should be deleted.
     * @return true if the deletion was successful.
     * @throws SQLException if there is a problem.
     */
    boolean delete(Integer id) throws SQLException;

}
