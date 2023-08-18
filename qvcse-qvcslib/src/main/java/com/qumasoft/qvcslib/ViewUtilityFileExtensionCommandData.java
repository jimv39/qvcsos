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
package com.qumasoft.qvcslib;

/**
 *
 * @author Jim Voris
 */
public class ViewUtilityFileExtensionCommandData implements java.io.Serializable {
    private String fileExtension;
    private Integer commandLineId;

    /**
     * @return the fileExtension
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * @param ext the fileExtension to set
     */
    public void setFileExtension(String ext) {
        this.fileExtension = ext;
    }

    /**
     * @return the commandLineId
     */
    public Integer getCommandLineId() {
        return commandLineId;
    }

    /**
     * @param cl the commandLineId to set
     */
    public void setCommandLineId(Integer cl) {
        this.commandLineId = cl;
    }

}
