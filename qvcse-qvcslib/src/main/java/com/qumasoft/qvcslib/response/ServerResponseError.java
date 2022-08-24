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
import com.qumasoft.qvcslib.SynchronizationManager;

/**
 * Error response.
 * @author Jim Voris
 */
public class ServerResponseError extends AbstractServerResponse {
    private static final long serialVersionUID = -2102046368002657490L;

    // These are serialized:
    private String errorMessage = null;
    private String projectName = null;
    private String branchName = null;
    private String appendedPath = null;
    private Integer syncToken = null;

    /**
     * Default constructor.
     */
    public ServerResponseError() {
    }

    /**
     * Creates a new instance of ServerResponseError.
     * @param errorMsg the error message.
     * @param project the project name.
     * @param branch the branch name.
     * @param path the appended path.
     */
    public ServerResponseError(String errorMsg, String project, String branch, String path) {
        errorMessage = errorMsg;
        projectName = project;
        branchName = branch;
        appendedPath = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        if (directoryManagerProxy != null) {
            directoryManagerProxy.updateInfo(getErrorMessage());
        }
        SynchronizationManager.getSynchronizationManager().notifyOnToken(syncToken);
    }

    /**
     * Get the error message.
     * @return the error message.
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Get the branch name.
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Get the appended path.
     * @return the appended path.
     */
    public String getAppendedPath() {
        return appendedPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_RESPONSE_ERROR;
    }

    /**
     * @return the syncToken
     */
    public Integer getSyncToken() {
        return syncToken;
    }

    /**
     * @param token the syncToken to set
     */
    public void setSyncToken(Integer token) {
        this.syncToken = token;
    }
}
