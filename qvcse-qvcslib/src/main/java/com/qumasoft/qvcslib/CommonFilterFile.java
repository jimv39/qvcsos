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
public class CommonFilterFile implements java.io.Serializable {
    /** These constants need to match the SQL script that initially populates the filter_type table. */
    /** Include files with given file extension. */
    public static final Integer INCLUDE_EXTENSION = 1;
    /** Exclude files with the given file extension. */
    public static final Integer EXCLUDE_EXTENSION = 2;
    /** Include those files with filenames that match the given regular expression. */
    public static final Integer INCLUDE_REGEX_FILENAME = 3;
    /** Exclude those files with filenames that match the given regular expression. */
    public static final Integer EXCLUDE_REGEX_FILENAME = 4;
    /** Include those files that have a commit message that matches the given regular expression. */
    public static final Integer INCLUDE_REGEX_REV_DESCRIPTION = 5;
    /** Exclude those files that have a commit message that matches the given regular expression. */
    public static final Integer EXCLUDE_REGEX_REV_DESCRIPTION = 6;
    /** Include those files that have the given file status. */
    public static final Integer INCLUDE_FILE_STATUS = 7;
    /** Exclude those files that have the given file status. */
    public static final Integer EXCLUDE_FILE_STATUS = 8;
    /** Include those files that have a revision committed after the given commit id. */
    public static final Integer CHECKED_IN_AFTER_COMMIT_ID = 9;
    /** Include those files that have a revision committed before the given commit id. */
    public static final Integer CHECKED_IN_BEFORE_COMMIT_ID = 10;
    /** Include those files with a file size greater than the given size. */
    public static final Integer FILESIZE_GREATER_THAN = 11;
    /** Include those files with a file size less than the given size. */
    public static final Integer FILESIZE_LESS_THAN = 12;
    /** Include those files last edited by the given user. */
    public static final Integer INCLUDE_LAST_EDIT_BY = 13;
    /** Exclude those files last edited by the given user. */
    public static final Integer EXCLUDE_LAST_EDIT_BY = 14;
    /** Exclude uncontrolled files. */
    public static final Integer EXCLUDE_UNCONTROLLED_FILES = 15;
    /** Search commit messages. */
    public static final Integer SEARCH_COMMIT_MESSAGES = 16;
    /** Show those files with revisions that have the given commit id. */
    public static final Integer BY_COMMIT_ID = 17;

    private Integer id;
    private Integer filterCollectionId;
    private Integer filterTypeId;
    private String filterType;
    private Boolean isAndFlag;
    private String filterData;

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param filterFileId the id to set
     */
    public void setId(Integer filterFileId) {
        this.id = filterFileId;
    }

    /**
     * @return the filterCollectionId
     */
    public Integer getFilterCollectionId() {
        return filterCollectionId;
    }

    /**
     * @param fcId the filterCollectionId to set
     */
    public void setFilterCollectionId(Integer fcId) {
        this.filterCollectionId = fcId;
    }

    /**
     * @return the filterTypeId
     */
    public Integer getFilterTypeId() {
        return filterTypeId;
    }

    /**
     * @param ftId the filterTypeId to set
     */
    public void setFilterTypeId(Integer ftId) {
        this.filterTypeId = ftId;
    }

    /**
     * @return the isAndFlag
     */
    public Boolean getIsAndFlag() {
        return isAndFlag;
    }

    /**
     * @param flag the isAndFlag to set
     */
    public void setIsAndFlag(Boolean flag) {
        this.isAndFlag = flag;
    }

    /**
     * @return the filterData
     */
    public String getFilterData() {
        return filterData;
    }

    /**
     * @param fData the filterData to set
     */
    public void setFilterData(String fData) {
        this.filterData = fData;
    }

    /**
     * @return the filterType
     */
    public String getFilterType() {
        return filterType;
    }

    /**
     * @param fType the filterType to set
     */
    public void setFilterType(String fType) {
        this.filterType = fType;
    }

}
