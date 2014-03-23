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
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.LabelManager;
import com.qumasoft.qvcslib.LogFileProxy;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.WorkfileDirectoryManagerInterface;
import com.qumasoft.qvcslib.WorkfileInfo;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Get revision response.
 * @author Jim Voris
 */
public class ServerResponseGetRevision implements ServerResponseInterface {
    private static final long serialVersionUID = 5040229585662644384L;

    // These are serialized:
    private String clientWorkfileName = null;
    private String shortWorkfileName = null;
    private String appendedPath = null;
    private String projectName = null;
    private String viewName = null;
    private String revisionString = null;
    private String labelString = null;
    private boolean directoryLevelOperationFlag = false;
    private int directoryLevelTransactionID = 0;
    // Send back the skinny logfile info
    private SkinnyLogfileInfo skinnyLogfileInfo = null;
    // Send back the timestamp behavior and overwrite behavior.
    private Utility.TimestampBehavior timestampBehavior = null;
    private Utility.OverwriteBehavior overwriteBehavior = null;
    // Send back the timestamp we should use for timestamp behavior.
    private long timestamp = 0L;
    // This is the actual file revision as a byte array;
    private byte[] buffer = null;
    // Optionally sent back if needed to expand keywords.
    private LogfileInfo logfileInfo = null;
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");

    /**
     * Creates new ServerResponseFetchFileRevision.
     */
    public ServerResponseGetRevision() {
        // Default these to reasonable values.  Note that these do NOT get
        // serialized for use by the C++ IDE client.
        timestampBehavior = Utility.TimestampBehavior.SET_TIMESTAMP_TO_NOW;
        overwriteBehavior = Utility.OverwriteBehavior.REPLACE_WRITABLE_FILE;
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
     * Get the timestamp behavior.
     * @return the timestamp behavior.
     */
    public Utility.TimestampBehavior getTimestampBehavior() {
        return timestampBehavior;
    }

    /**
     * Set the timestamp behavior.
     * @param tsBehavior the timestamp behavior.
     */
    public void setTimestampBehavior(Utility.TimestampBehavior tsBehavior) {
        timestampBehavior = tsBehavior;
    }

    /**
     * Get the overwrite behavior.
     * @return the overwrite behavior.
     */
    public Utility.OverwriteBehavior getOverwriteBehavior() {
        return overwriteBehavior;
    }

    /**
     * Set the overwrite behavior.
     * @param ovBehavior the overwrite behavior.
     */
    public void setOverwriteBehavior(Utility.OverwriteBehavior ovBehavior) {
        overwriteBehavior = ovBehavior;
    }

    /**
     * Get the timestamp.
     * @return the timestamp.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp.
     * @param time the timestamp.
     */
    public void setTimestamp(long time) {
        timestamp = time;
    }

    /**
     * Get the directory level operation flag.
     * @return the directory level operation flag.
     */
    public boolean getDirectoryLevelOperationFlag() {
        return directoryLevelOperationFlag;
    }

    /**
     * Set the directory level operation flag. If set to true, we use this message for directory level operations, and there winds up being one of these response messages for
     * each separate file that we return from the server.
     * @param flag the directory level operation flag.
     */
    public void setDirectoryLevelOperationFlag(boolean flag) {
        directoryLevelOperationFlag = flag;
    }

    /**
     * Get the directory level transaction ID.
     * @return the directory level transaction ID.
     */
    public int getDirectoryLevelTransactionID() {
        return directoryLevelTransactionID;
    }

    /**
     * Set the directory level transaction ID.
     * @param transID the directory level transaction ID.
     */
    public void setDirectoryLevelTransactionID(int transID) {
        directoryLevelTransactionID = transID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        StringBuilder message = new StringBuilder("Fetched revision ");
        message.append(revisionString);
        message.append(" of ");
        message.append(getShortWorkfileName());
        message.append(" from project directory: ");
        message.append(getAppendedPath());

        WorkfileDirectoryManagerInterface workfileDirManager = directoryManagerProxy.getDirectoryManager().getWorkfileDirectoryManager();
        if (workfileDirManager != null) {
            try {
                String fullWorkfileName = workfileDirManager.getWorkfileDirectory() + File.separator + getShortWorkfileName();
                WorkfileInfo workfileInfo = new WorkfileInfo(fullWorkfileName, getSkinnyLogfileInfo().getAttributes().getIsExpandKeywords(),
                        getSkinnyLogfileInfo().getAttributes().getIsBinaryfile(),
                        getProjectName());
                Date now = new Date();
                workfileInfo.setFetchedDate(now.getTime());
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
                LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
            }
        }

        directoryManagerProxy.updateInfo(message.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_REVISION;
    }
}
