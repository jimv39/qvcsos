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

import com.qvcsos.server.datamodel.Directory;
import java.sql.SQLException;

/**
 *
 * @author Jim Voris
 */
public interface DirectoryDAO {

    /**
     * Insert a row in the Commit table.
     *
     * @param directory the Directory to insert.
     * A new directory row will <b>always</b> be created.
     * @return the id of the inserted Directory.
     *
     * @throws SQLException thrown if there is a problem.
     */
    Integer insert(Directory directory) throws SQLException;

    Directory findById(Integer directoryId);

}
