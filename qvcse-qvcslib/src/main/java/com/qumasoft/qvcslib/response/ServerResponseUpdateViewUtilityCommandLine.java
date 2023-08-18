/*
 * Copyright 2023 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import static com.qumasoft.qvcslib.requestdata.ClientRequestUpdateViewUtilityCommandData.ADD_COMMAND_LINE_REQUEST;
import static com.qumasoft.qvcslib.requestdata.ClientRequestUpdateViewUtilityCommandData.ADD_UTILITY_ASSOCIATION_REQUEST;
import static com.qumasoft.qvcslib.requestdata.ClientRequestUpdateViewUtilityCommandData.REMOVE_UTILITY_ASSOCIATION_REQUEST;

/**
 *
 * @author Jim Voris.
 */
public class ServerResponseUpdateViewUtilityCommandLine extends AbstractServerResponse {
    private String serverName;
    private String commandLine;
    private Integer commandLineId;
    private String extension;
    private Integer extensionId;
    private Boolean associateCommandWithExtension;

    /** The request type is ADD_COMMAND_LINE_REQUEST or REMOVE_UTILITY_ASSOCIATION_REQUEST */
    private Integer requestType;

    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_UPDATE_VIEW_UTILITY_COMMAND;
    }

    /**
     * @return the commandLine
     */
    public String getCommandLine() {
        return commandLine;
    }

    /**
     * @param cmdLine the commandLine to set
     */
    public void setCommandLine(String cmdLine) {
        this.commandLine = cmdLine;
    }

    /**
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * @param ext the extension to set
     */
    public void setExtension(String ext) {
        this.extension = ext;
    }

    /**
     * @return the associateCommandWithExtension
     */
    public Boolean getAssociateCommandWithExtension() {
        return associateCommandWithExtension;
    }

    /**
     * @param flag the associateCommandWithExtension to set
     */
    public void setAssociateCommandWithExtension(Boolean flag) {
        this.associateCommandWithExtension = flag;
    }

    /**
     * @return the requestType
     */
    public Integer getRequestType() {
        return requestType;
    }

    /**
     * @param rqstType the requestType to set
     */
    public void setRequestType(Integer rqstType) {
        if (rqstType == ADD_COMMAND_LINE_REQUEST || rqstType == REMOVE_UTILITY_ASSOCIATION_REQUEST || rqstType == ADD_UTILITY_ASSOCIATION_REQUEST) {
            this.requestType = rqstType;
        } else {
            throw new QVCSRuntimeException("Invalid request type!");
        }
    }

    /**
     * @return the commandLineId
     */
    public Integer getCommandLineId() {
        return commandLineId;
    }

    /**
     * @param cmdLineId the commandLineId to set
     */
    public void setCommandLineId(Integer cmdLineId) {
        this.commandLineId = cmdLineId;
    }

    /**
     * @return the serverName
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param svrName the serverName to set
     */
    public void setServerName(String svrName) {
        this.serverName = svrName;
    }

    /**
     * @return the extensionId
     */
    public Integer getExtensionId() {
        return extensionId;
    }

    /**
     * @param id the extensionId to set
     */
    public void setExtensionId(Integer id) {
        this.extensionId = id;
    }

}
