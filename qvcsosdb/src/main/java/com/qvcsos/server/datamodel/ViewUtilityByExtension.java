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
package com.qvcsos.server.datamodel;

/**
 *
 * @author Jim Voris.
 */
public class ViewUtilityByExtension {
    private Integer id;
    private String userAndComputer;
    private String fileExtension;
    private Integer commandLineId;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param byExtensionId the id to set
     */
    public void setId(Integer byExtensionId) {
        this.id = byExtensionId;
    }

    /**
     * @return the userAndComputer
     */
    public String getUserAndComputer() {
        return userAndComputer;
    }

    /**
     * @param usrAndComputer the userAndComputer to set
     */
    public void setUserAndComputer(String usrAndComputer) {
        this.userAndComputer = usrAndComputer;
    }

    /**
     * @return the fileExtension
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * @param fileExt the fileExtension to set
     */
    public void setFileExtension(String fileExt) {
        this.fileExtension = fileExt;
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
}
