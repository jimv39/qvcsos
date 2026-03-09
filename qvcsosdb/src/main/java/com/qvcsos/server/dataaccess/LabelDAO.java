/*
 * Copyright 2026 Jim Voris..
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

import com.qvcsos.server.datamodel.Label;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public interface LabelDAO {
    /**
     * Find the label by id.
     *
     * @param id the label id.
     * @return the Label with the given id, or null if the Label is not found.
     */
    Label findById(Integer id);

    List<Label> findAll();

    Label findByLabelText(String text);

    /**
     * Insert a label.
     *
     * @param label the Label to insert. We ignore the label id.
     * @return id of the inserted row.
     * @throws SQLException if there is a problem.
     */
    Integer insert(Label label) throws SQLException;

    /**
     * Delete the given label. This will <i>also</i> delete all records from the
     * label-fileId join table that use this label.
     * @param label the label to delete.
     * @return true if the delete was successful... false otherwise.
     * @throws SQLException if there is a problem.
     */
    boolean delete(Label label) throws SQLException;
}
