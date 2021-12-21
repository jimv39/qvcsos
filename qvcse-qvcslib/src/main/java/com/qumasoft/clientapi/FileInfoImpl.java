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
package com.qumasoft.clientapi;

import com.qumasoft.qvcslib.ArchiveInfoInterface;
import java.util.Date;

/**
 * File info implementation.
 * @author Jim Voris
 */
class FileInfoImpl implements FileInfo {

    private final ArchiveInfoInterface archiveInfo;
    private final String appendedPath;

    FileInfoImpl(ArchiveInfoInterface info, final String path) {
        this.archiveInfo = info;
        this.appendedPath = path;
    }

    @Override
    public String getAttributes() {
        return archiveInfo.getAttributes().toPropertyString();
    }

    @Override
    public Date getLastCheckInDate() {
        return archiveInfo.getLastCheckInDate();
    }

    @Override
    public String getLastEditBy() {
        return archiveInfo.getLastEditBy();
    }

    @Override
    public int getRevisionCount() {
        return archiveInfo.getRevisionCount();
    }

    @Override
    public String getShortWorkfileName() {
        return archiveInfo.getShortWorkfileName();
    }

    @Override
    public String getAppendedPath() {
        return this.appendedPath;
    }
}
