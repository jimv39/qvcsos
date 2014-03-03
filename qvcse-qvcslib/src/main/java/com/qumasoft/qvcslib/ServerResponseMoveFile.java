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

import java.util.Properties;

/**
 * Move file response.
 * @author Jim Voris
 */
public class ServerResponseMoveFile implements ServerResponseInterface {
    private static final long serialVersionUID = 7493610072461733572L;

    // These are serialized:
    private String serverName;
    private String projectName;
    private String viewName;
    private String originAppendedPath;
    private String destinationAppendedPath;
    private String shortWorkfileName;
    /*
     * Holds the project properties
     */
    private Properties projectPropertiesValues;
    // Send back the skinny logfile info
    private SkinnyLogfileInfo skinnyLogfileInfo;

    /**
     * Creates a new instance of ServerResponseMoveFile.
     */
    public ServerResponseMoveFile() {
    }

    /**
     * Get the server name.
     * @return the server name.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Set the server name.
     * @param server the server name.
     */
    public void setServerName(String server) {
        serverName = server;
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
     * Get the origin appended path.
     * @return the origin appended path.
     *
     */
    public String getOriginAppendedPath() {
        return originAppendedPath;
    }

    /**
     * Set the origin appended path.
     * @param origin the origin appended path.
     */
    public void setOriginAppendedPath(String origin) {
        originAppendedPath = origin;
    }

    /**
     * Get the destination appended path.
     * @return the destination appended path.
     */
    public String getDestinationAppendedPath() {
        return destinationAppendedPath;
    }

    /**
     * Set the destination appended path.
     * @param destination the destination appended path.
     *
     */
    public void setDestinationAppendedPath(String destination) {
        destinationAppendedPath = destination;
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
     * Get the project properties.
     * @return the project properties.
     */
    public Properties getProjectProperties() {
        return projectPropertiesValues;
    }

    /**
     * Set the project properties.
     * @param projectProperties the project properties.
     */
    public void setProjectProperties(Properties projectProperties) {
        projectPropertiesValues = projectProperties;
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_MOVE_FILE;
    }
}
