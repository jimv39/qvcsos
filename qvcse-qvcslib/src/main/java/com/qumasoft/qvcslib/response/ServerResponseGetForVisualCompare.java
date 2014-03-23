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
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.VisualCompareInterface;
import java.io.File;

/**
 * Get for visual compare response.
 * @author Jim Voris
 */
public class ServerResponseGetForVisualCompare implements ServerResponseInterface {
    private static final long serialVersionUID = -2153152009540667047L;

    // These are serialized:
    private String clientOutputFileName = null;
    private String fullWorkfileName = null;
    private String appendedPath = null;
    private String projectName = null;
    private String viewName = null;
    private String revisionString = null;
    // Send back the logfile header, and all the revision info so we can
    // expand keywords on the client.
    private LogfileInfo logfileInfo = null;
    // This is the actual file revision as a byte array;
    private byte[] buffer = null;
    /** This is set by the client in the request message, and echoed by the server so the client can use it on receipt of the response. */
    private transient VisualCompareInterface visualCompareInterface = null;

    /**
     * Default constructor.
     */
    public ServerResponseGetForVisualCompare() {
    }

    /**
     * Set the visual compare interface.
     * @param visualCompInterface the visual compare interface.
     */
    public void setVisualCompareInterface(VisualCompareInterface visualCompInterface) {
        this.visualCompareInterface = visualCompInterface;
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
     * Get the client output filename.
     * @return the client output filename.
     */
    public String getClientOutputFileName() {
        return clientOutputFileName;
    }

    /**
     * Set the client output filename.
     * @param fileName the client output filename.
     */
    public void setClientOutputFileName(String fileName) {
        clientOutputFileName = fileName;
    }

    /**
     * Get the full workfile name.
     * @return the full workfile name.
     */
    public String getFullWorkfileName() {
        return fullWorkfileName;
    }

    /**
     * Set the full workfile name.
     * @param workfileName the full workfile name.
     */
    public void setFullWorkfileName(String workfileName) {
        fullWorkfileName = workfileName;
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
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        String shortWorkfileName = getFullWorkfileName().substring(1 + getFullWorkfileName().lastIndexOf(File.separatorChar));
        String display1 = shortWorkfileName + ": " + "Revision " + getRevisionString();
        visualCompareInterface.visualCompare(getClientOutputFileName(), getFullWorkfileName(), display1, getFullWorkfileName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_FOR_VISUAL_COMPARE;
    }
}
