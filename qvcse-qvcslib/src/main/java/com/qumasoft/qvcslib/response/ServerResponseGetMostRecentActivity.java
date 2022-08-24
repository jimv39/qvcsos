/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import java.util.Date;

/**
 * Get most recent activity response. This can be used by build systems as a cheap poll to determine the most recent check in on the given project/appendedPath.
 * @author Jim Voris
 */
public class ServerResponseGetMostRecentActivity extends AbstractServerResponse {
    private static final long serialVersionUID = 2034406593289183842L;

    // These are serialized:
    private String appendedPath = null;
    private String projectName = null;
    private String branchName = null;
    // Send back the date of the most recent activity for the requested directory.
    private Date mostRecentActivityDate = null;

    /**
     * Creates new ServerResponseGetMostRecentActivity.
     */
    public ServerResponseGetMostRecentActivity() {
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * Set the appended path.
     * @param path the appended path.
     */
    public void setAppendedPath(String path) {
        this.appendedPath = path;
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Set the project name.
     * @param project the project name.
     */
    public void setProjectName(String project) {
        this.projectName = project;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Set the branch name.
     * @param branch the branch name.
     */
    public void setBranchName(final String branch) {
        this.branchName = branch;
    }

    /**
     * Get the most recent activity date.
     * @return the most recent activity date.
     */
    public Date getMostRecentActivityDate() {
        return mostRecentActivityDate;
    }

    /**
     * Set the most recent activity date.
     * @param timestamp the most recent activity date.
     */
    public void setMostRecentActivityDate(Date timestamp) {
        mostRecentActivityDate = timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        directoryManagerProxy.updateMostRecentActivityDate(getMostRecentActivityDate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_MOST_RECENT_ACTIVITY;
    }
}
