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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.response.ServerResponseInterface;

/**
 * Directory operation interface.
 * @author Jim Voris
 */
public interface DirectoryOperationInterface {

    /**
     * Process a file.
     * @param archiveDirManager the archive directory manager for the directory.
     * @param archiveInfo the archive info for the file of interest.
     * @param appendedPath the appended path.
     * @param response object to identify the client.
     * @return the response object.
     */
    ServerResponseInterface processFile(ArchiveDirManagerInterface archiveDirManager, ArchiveInfoInterface archiveInfo, String appendedPath,
            ServerResponseFactoryInterface response);

    /**
     * Get the project name.
     * @return the project name.
     */
    String getProjectName();
}
