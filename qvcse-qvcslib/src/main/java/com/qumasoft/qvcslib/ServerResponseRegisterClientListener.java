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

import java.util.Collections;

/**
 * Register client listener response.
 * @author Jim Voris
 */
public class ServerResponseRegisterClientListener implements ServerResponseInterface {
    private static final long serialVersionUID = -2632721847301972810L;

    // This is what gets serialized.
    private String appendedPath;
    private String projectName;
    private String viewName;
    private int directoryID;
    private final java.util.List<SkinnyLogfileInfo> logfileInformationArray = Collections.synchronizedList(new java.util.ArrayList<SkinnyLogfileInfo>());

    /**
     * Creates a new instance of ServerResponseRegisterClientListener.
     */
    public ServerResponseRegisterClientListener() {
    }

    /**
     * Add logfile information.
     * @param skinnyInfo information to add.
     */
    public void addLogfileInformation(SkinnyLogfileInfo skinnyInfo) {
        logfileInformationArray.add(skinnyInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        for (int i = 0; i < logfileInformationArray.size(); i++) {
            SkinnyLogfileInfo skinnyLogfileInfo = logfileInformationArray.get(i);
            directoryManagerProxy.updateArchiveInfo(skinnyLogfileInfo.getShortWorkfileName(), skinnyLogfileInfo);
        }
        directoryManagerProxy.setDirectoryID(getDirectoryID());
        directoryManagerProxy.setInitComplete();
        if (directoryManagerProxy.getFastNotify()) {
            directoryManagerProxy.notifyListeners();
        }
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
     * Get the directory ID.
     * @return the directory ID.
     */
    public int getDirectoryID() {
        return directoryID;
    }

    /**
     * Set the directory ID.
     * @param dirID the directory ID.
     */
    public void setDirectoryID(int dirID) {
        directoryID = dirID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_REGISTER_CLIENT_LISTENER;
    }
}
