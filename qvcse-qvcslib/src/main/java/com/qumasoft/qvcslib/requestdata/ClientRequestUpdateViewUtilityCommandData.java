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
package com.qumasoft.qvcslib.requestdata;

import com.qumasoft.qvcslib.QVCSRuntimeException;

/**
 *
 * @author Jim Voris.
 */
public class ClientRequestUpdateViewUtilityCommandData extends ClientRequestClientData {
    /** Add a command line request. */
    public static final int ADD_COMMAND_LINE_REQUEST = 1;
    /** Remove utility association to the given extension. */
    public static final int REMOVE_UTILITY_ASSOCIATION_REQUEST = 2;
    /** Add a utility association. */
    public static final int ADD_UTILITY_ASSOCIATION_REQUEST = 3;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.SYNC_TOKEN
    };
    private String clientComputerName;
    private String commandLine;
    private String extension;
    private Boolean associateCommandWithExtension;

    /** The request type is ADD_COMMAND_LINE_REQUEST or REMOVE_UTILITY_ASSOCIATION_REQUEST */
    private Integer requestType;

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.UPDATE_VIEW_UTILITY_COMMAND;
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
     * @return the clientComputerName
     */
    public String getClientComputerName() {
        return clientComputerName;
    }

    /**
     * @param computerName the clientComputerName to set
     */
    public void setClientComputerName(String computerName) {
        this.clientComputerName = computerName;
    }

}
