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

import com.qvcsos.server.datamodel.User;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public interface UserDAO {

    /**
     * Find the user by user id.
     *
     * @param userId the user id.
     * @return the user with the given id, or null if the user is not found.
     */
    User findById(Integer userId);

    /**
     * Find the user by user name.
     *
     * @param userName the name of the user to find.
     * @return the user with the given name, or null if the user is not found.
     */
    User findByUserName(String userName);

    /**
     * Find all users.
     *
     * @return a List of all the users.
     */
    List<User> findAll();

    /**
     * Insert a user.
     *
     * @param user the user to insert. We ignore the user id.
     * @return id of the inserted row.
     * @throws SQLException if there is a problem.
     */
    Integer insert(User user) throws SQLException;

    /**
     * Delete a user.This just sets the deleted_flag to TRUE.
     *
     * @param user the user to delete.
     * @return true if successful; false if not.
     */
    boolean delete(User user);

    /**
     * Update the user password.
     * @param id the id of the user record.
     * @param newHashedPassword the new hashed password.
     * @return true if password was changed.
     */
    boolean updateUserPassword(Integer id, byte[] newHashedPassword);

}
