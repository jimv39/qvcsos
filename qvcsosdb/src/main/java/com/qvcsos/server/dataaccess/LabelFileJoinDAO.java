/*
 * Copyright 2026 Jim Voris.
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

import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public interface LabelFileJoinDAO {

    /**
     * Return a list of file id's.
     * @param labelId the label id that is associated with a set of files.
     * @return a list of file id's that are associated with the given label id.
     */
    List<Integer> findFilesByLabelId(Integer labelId);

    /**
     * Add a row to the join table.
     * @param labelId the label id.
     * @param fileId the file id.
     * @return the id of the inserted row.
     * @throws SQLException if there is a problem.
     */
    Integer insert(Integer labelId, Integer fileId) throws SQLException;

    /**
     * Delete all records associated with the given labelId.
     * @param labelId the label id.
     * @return true if successful; false otherwise.
     * @throws java.sql.SQLException on a SQL exception.
     */
    boolean deleteByLabelId(Integer labelId) throws SQLException;

    /**
     * Delete the single label/file record associated with the given labelId and fileId.
     * @param labelId the label id.
     * @param fileId the file id.
     * @return true if successful; false otherwise.
     * @throws java.sql.SQLException on a SQL exception.
     */
    boolean deleteFileByLabelIdAndFileId(Integer labelId, Integer fileId) throws SQLException;
}
