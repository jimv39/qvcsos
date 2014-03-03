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

package com.qumasoft.qvcslib;

import java.util.Collection;

/**
 * Workfile directory manager interface. Define the behaviors that we need from the Workfile Directory Manager.
 * @author Jim Voris
 */
public interface WorkfileDirectoryManagerInterface {

    /**
     * Create a workfile directory.
     * @return true on success; false otherwise.
     */
    boolean createDirectory();

    /**
     * Get the full path of the workfile directory.
     * @return the path of the workfile directory.
     */
    String getWorkfileDirectory();

    /**
     * Get the Collection of workfiles.
     * @return a Collection of WorkfileInfo objects.
     */
    Collection<WorkfileInfoInterface> getWorkfileCollection();

    /**
     * Lookup the workfile info for the given short workfile name.
     * @param shortWorkfileName the short workfile name.
     * @return the workfile info for the given workfile.
     */
    WorkfileInfoInterface lookupWorkfileInfo(String shortWorkfileName);

    /**
     * Update the workfile info for the given workfile.
     * @param workfileInfo the workfile info to update for this Workfile Directory Manager.
     * @throws QVCSException if something goes wrong.
     */
    void updateWorkfileInfo(WorkfileInfoInterface workfileInfo) throws QVCSException;

    /**
     * Re-read the workfile directory from the file system and update the workfile information for this Workfile Directory Manager.
     */
    void refresh();
}
