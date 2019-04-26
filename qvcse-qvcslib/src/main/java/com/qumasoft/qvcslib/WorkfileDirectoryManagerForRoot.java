/*   Copyright 2004-2014 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.qvcslib;

import java.util.Collection;

/**
 * Workfile directory manager for the project root directory. This special class for the root directory implements the needed WorkfileDirectoryManagerInterface,
 * but no-ops all of the interface-required methods.
 * @author Jim Voris
 */
public class WorkfileDirectoryManagerForRoot implements WorkfileDirectoryManagerInterface {

    /**
     * Creates a new instance of WorkfileDirectoryManagerForRoot.
     */
    public WorkfileDirectoryManagerForRoot() {
    }

    /**
     * Get the workfile collection.
     *
     * @return always returns null.
     */
    @Override
    public Collection<WorkfileInfoInterface> getWorkfileCollection() {
        return null;
    }

    /**
     * Get the workfile directory.
     *
     * @return for the project root directory, this always returns the empty string.
     */
    @Override
    public String getWorkfileDirectory() {
        return "";
    }

    /**
     * Update the workfile info for the given workfile. This is a no-op operation.
     *
     * @param workfileInfo the workfile info.
     */
    @Override
    public void updateWorkfileInfo(WorkfileInfoInterface workfileInfo) {
    }

    /**
     * Refresh the project root directory. This is a no-op operation for the project root directory.
     */
    @Override
    public void refresh() {
    }

    /**
     * Create the directory for the project root.
     *
     * @return false.
     */
    @Override
    public boolean createDirectory() {
        return false;
    }

    /**
     * Lookup the workfile info for the given short workfile name.
     *
     * @param shortWorkfileName the short workfile name.
     * @return null.
     */
    @Override
    public WorkfileInfo lookupWorkfileInfo(String shortWorkfileName) {
        return null;
    }
}
