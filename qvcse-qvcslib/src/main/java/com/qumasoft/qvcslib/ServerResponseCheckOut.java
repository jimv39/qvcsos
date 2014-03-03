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

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checkout response.
 * @author Jim Voris
 */
public class ServerResponseCheckOut implements ServerResponseInterface {
    private static final long serialVersionUID = -6006510573797418188L;

    // These are serialized:
    private String clientWorkfileName = null;
    private String shortWorkfileName = null;
    private String appendedPath = null;
    private String projectName = null;
    private String viewName = null;
    private String revisionString = null;
    private String labelString = null;
    // Send back the skinny logfile info
    private SkinnyLogfileInfo skinnyLogfileInfo = null;
    // This is the actual file revision as a byte array;
    private byte[] buffer = null;
    // Optionally sent back if needed to expand keywords.
    private LogfileInfo logfileInfo = null;
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");

    /**
     * Creates new ServerResponseFetchFileRevision.
     */
    public ServerResponseCheckOut() {
    }

    /**
     * Get the logfile info.
     * @return the logfile info.
     */
    public LogfileInfo getLogfileInfo() {
        return logfileInfo;
    }

    /**
     * Set the logfile info.
     * @param info the logfile info.
     */
    public void setLogfileInfo(LogfileInfo info) {
        logfileInfo = info;
    }

    /**
     * Get the buffer.
     * @return the buffer.
     */
    public byte[] getBuffer() {
        return buffer;
    }

    /**
     * Set the buffer.
     * @param buff the buffer.
     */
    public void setBuffer(byte[] buff) {
        buffer = buff;
    }

    /**
     * Get the client workfile name.
     * @return the client workfile name.
     */
    public String getClientWorkfileName() {
        return clientWorkfileName;
    }

    /**
     * Set the client workfile name.
     * @param workfileName the client workfile name.
     */
    public void setClientWorkfileName(String workfileName) {
        clientWorkfileName = workfileName;
    }

    /**
     * Get the short workfile name.
     * @return the short workfile name.
     */
    public String getShortWorkfileName() {
        return shortWorkfileName;
    }

    /**
     * Set the short workfile name.
     * @param shortName the short workfile name.
     */
    public void setShortWorkfileName(String shortName) {
        shortWorkfileName = shortName;
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
        appendedPath = path;
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
        projectName = project;
    }

    /**
     * Get the view name.
     * @return the view name.
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * Set the view name.
     * @param view the view name.
     */
    public void setViewName(final String view) {
        viewName = view;
    }

    /**
     * Get the revision string.
     * @return the revision string.
     */
    public String getRevisionString() {
        return revisionString;
    }

    /**
     * Set the revision string.
     * @param revString the revision string.
     */
    public void setRevisionString(String revString) {
        revisionString = revString;
    }

    /**
     * Get the label string.
     * @return the label string.
     */
    public String getLabelString() {
        return labelString;
    }

    /**
     * Set the label string.
     * @param label the label string.
     */
    public void setLabelString(String label) {
        labelString = label;
    }

    /**
     * Get the skinny logfile info.
     * @return the skinny logfile info.
     */
    public SkinnyLogfileInfo getSkinnyLogfileInfo() {
        return skinnyLogfileInfo;
    }

    /**
     * Set the skinny logfile info.
     * @param skinnyInfo the skinny logfile info.
     */
    public void setSkinnyLogfileInfo(SkinnyLogfileInfo skinnyInfo) {
        skinnyLogfileInfo = skinnyInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        StringBuilder message = new StringBuilder("Checked out ");
        message.append(getShortWorkfileName());
        message.append(" from project directory: ");
        message.append(getAppendedPath());

        WorkfileDirectoryManagerInterface workfileDirManager = directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager();
        if (workfileDirManager != null) {
            try {
                String fullWorkfileName = workfileDirManager.getWorkfileDirectory() + File.separator + getShortWorkfileName();
                WorkfileInfo workfileInfo = new WorkfileInfo(fullWorkfileName, getSkinnyLogfileInfo().getAttributes().getIsExpandKeywords(),
                        getSkinnyLogfileInfo().getAttributes().getIsBinaryfile(), getProjectName());
                workfileInfo.setFetchedDate(workfileInfo.getWorkfile().lastModified());
                workfileInfo.setWorkfileRevisionString(getRevisionString());

                // Set the archiveInfo on the workfileInfo object so we can
                // contract (actually expand) keywords for a binary file for
                // computing a useful digest.
                MergedInfoInterface mergedInfo = directoryManagerProxy.getDirectoryManager().getMergedInfo(getShortWorkfileName());
                ArchiveInfoInterface archiveInfo = mergedInfo.getArchiveInfo();
                workfileInfo.setArchiveInfo(archiveInfo);

                // Update the logfile info on the logFileProxy object before
                // we update the workfile digest (which happens via the
                // updateWorkfileInfo() call...
                if (getLogfileInfo() != null) {
                    LogFileProxy logFileProxy = (LogFileProxy) archiveInfo;
                    synchronized (logFileProxy) {
                        logFileProxy.setLogfileInfo(getLogfileInfo());

                        // We potentially received some label information.  Store
                        // it away with the Label Manager...
                        LabelManager.getInstance().addLabels(getProjectName(), getLogfileInfo());

                        // Notify the other thread that it can continue.
                        logFileProxy.notifyAll();
                    }
                }

                workfileDirManager.updateWorkfileInfo(workfileInfo);
            } catch (QVCSException | IOException e) {
                LOGGER.log(Level.WARNING, "Caught exception trying to update workfile info: " + e.getLocalizedMessage());
            }
        }

        // This goes to the status line
        directoryManagerProxy.updateInfo(message.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_CHECK_OUT;
    }
}
