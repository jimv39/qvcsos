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

/**
 * Response Message. Used for generic responses. The priority of the message can be used by the client to help it figure out whether to display it, or  how to display it.
 * @author Jim Voris
 */
public class ServerResponseMessage implements ServerResponseInterface {
    private static final long serialVersionUID = -5513356057612416301L;

    // TODO -- these should be an enum instead of Strings.
    /** A high priority message. */
    public static final String HIGH_PRIORITY = "HIGH";
    /** A medium priority message. */
    public static final String MEDIUM_PRIORITY = "MEDIUM";
    /** A low priority message. */
    public static final String LO_PRIORITY = "LO";
    // These are serialized:
    private String message = null;
    private String projectName = null;
    private String branchName = null;
    private String appendedPath = null;
    private String shortWorkfileName = null;
    private String priority = null;

    /**
     * Build a server response message.
     */
    public ServerResponseMessage() {
    }

    /**
     * Creates a new instance of ServerResponseError.
     * @param msg the message.
     * @param project the project name.
     * @param branch the branch name.
     * @param path the appended path.
     * @param prty the priority.
     */
    public ServerResponseMessage(final String msg, final String project, final String branch, final String path, final String prty) {
        message = msg;
        projectName = project;
        branchName = branch;
        appendedPath = path;
        priority = prty;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        if (directoryManagerProxy != null) {
            directoryManagerProxy.updateInfo(this);
        }
    }

    /**
     * Get the message.
     * @return the message.
     */
    public String getMessage() {
        return message;
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
     * Get the priority.
     * @return the priority.
     */
    public String getPriority() {
        return priority;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     */
    public void setShortWorkfileName(String shortName) {
        shortWorkfileName = shortName;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_RESPONSE_MESSAGE;
    }
}
