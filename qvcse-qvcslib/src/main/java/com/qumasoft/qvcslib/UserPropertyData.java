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
 * @author Jim Voris.
 */
public class UserPropertyData implements java.io.Serializable {
    private Integer id;
    private String userAndComputer;
    private String propertyName;
    private String propertyValue;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param pid the id to set
     */
    public void setId(Integer pid) {
        this.id = pid;
    }

    /**
     * @return the userAndComputer
     */
    public String getUserAndComputer() {
        return userAndComputer;
    }

    /**
     * @param uid the userAndComputer to set
     */
    public void setUserAndComputer(String uid) {
        this.userAndComputer = uid;
    }

    /**
     * @return the propertyName
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * @param pName the propertyName to set
     */
    public void setPropertyName(String pName) {
        this.propertyName = pName;
    }

    /**
     * @return the propertyValue
     */
    public String getPropertyValue() {
        return propertyValue;
    }

    /**
     * @param pValue the propertyValue to set
     */
    public void setPropertyValue(String pValue) {
        this.propertyValue = pValue;
    }

}
