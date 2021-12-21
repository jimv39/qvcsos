/*
 * Copyright 2021 Jim Voris.
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
 * @author Jim Voris
 */
public class User {
    private Integer userId;
    private String userName;
    private byte[] password;
    private Boolean deletedFlag;

    /**
     * @return the id
     */
    public Integer getId() {
        return userId;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.userId = id;
    }

    /**
     * @return the userName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param name the userName to set
     */
    public void setUserName(String name) {
        this.userName = name;
    }

    /**
     * @return the deletedFlag
     */
    public Boolean getDeletedFlag() {
        return deletedFlag;
    }

    /**
     * @param flag the deletedFlag to set
     */
    public void setDeletedFlag(Boolean flag) {
        this.deletedFlag = flag;
    }

    /**
     * @return the password
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * @param pw the password to set
     */
    public void setPassword(byte[] pw) {
        this.password = pw;
    }

}
