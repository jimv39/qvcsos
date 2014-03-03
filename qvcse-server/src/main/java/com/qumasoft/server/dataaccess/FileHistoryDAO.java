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

import com.qumasoft.server.datamodel.FileHistory;
import java.util.Date;
import java.util.List;

/**
 * File history DAO interface.
 * @author Jim Voris
 */
public interface FileHistoryDAO {

    /**
     * Find the list of files associated with a given branch and directory that existed on or before the given date.
     *
     * @param branchId the branch where the file lives.
     * @param directoryId the directory where the file lives.
     * @param viewDate the date of the view.
     * @return the List of files that are in the given directory on the given branch. The list may be empty if there are no files.
     */
    List<FileHistory> findByBranchAndDirectoryIdAndViewDate(Integer branchId, Integer directoryId, Date viewDate);

    /**
     * Find the list of history records for a given branch and file.
     *
     * @param branchId the branch id.
     * @param fileId the file id.
     * @return the list of file history records for the given branch and file, newest to oldest.
     */
    List<FileHistory> findHistoryForFileId(Integer branchId, Integer fileId);
}
