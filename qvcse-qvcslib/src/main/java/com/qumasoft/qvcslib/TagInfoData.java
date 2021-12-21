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
package com.qumasoft.qvcslib;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Used to report tag information.
 *
 * @author Jim Voris
 */
public class TagInfoData implements java.io.Serializable {

    private String tagText;
    private String description;
    private Date creationDate;
    private String creatorName;
    private String branchName;

    /**
     * @return the tagText
     */
    public String getTagText() {
        return tagText;
    }

    /**
     * @param text the tagText to set
     */
    public void setTagText(String text) {
        this.tagText = text;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param text the description to set
     */
    public void setDescription(String text) {
        this.description = text;
    }

    /**
     * @return the creationDate
     */
    public Date getCreationDate() {
        return creationDate;
    }

    /**
     * @param date the creationDate to set
     */
    public void setCreationDate(Date date) {
        this.creationDate = date;
    }

    /**
     * @return the creatorName
     */
    public String getCreatorName() {
        return creatorName;
    }

    /**
     * @param name the creatorName to set
     */
    public void setCreatorName(String name) {
        this.creatorName = name;
    }

    /**
     * @return the branchName
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * @param name the branchName to set
     */
    public void setBranchName(String name) {
        this.branchName = name;
    }

    @Override
    public String toString() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM dd, yyyy hh:mm a");

        // format date to string
        String dateString = formatter.format(this.creationDate);
        String stringValue = String.format("%s\t%s\t%s\t%s\t%s", this.tagText, this.description, dateString, this.creatorName, this.branchName);
        return stringValue;
    }

}
