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
public class ViewUtilityCommandLine {
    private Integer id;
    private String userAndComputer;
    private String commandLine;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param commandLineId the id to set
     */
    public void setId(Integer commandLineId) {
        this.id = commandLineId;
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
}
