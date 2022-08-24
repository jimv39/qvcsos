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
package com.qumasoft.qvcslib.requestdata;

/**
 *
 * @author Jim Voris
 */
public class ClientRequestUpdateTagCommitIdData extends ClientRequestClientData {

    private final ValidRequestElementType[] validElements = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.USER_NAME,
        ValidRequestElementType.SYNC_TOKEN
    };
    private String tag;
    private Integer oldCommitId;
    private Integer newCommitId;

    /**
     * Creates new ClientRequestUpdateTagCommitIdData.
     */
    public ClientRequestUpdateTagCommitIdData() {
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return validElements;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.UPDATE_TAG_COMMIT_ID;
    }

    /**
     * @return the tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * @param tagText the tag to set
     */
    public void setTag(String tagText) {
        this.tag = tagText;
    }

    /**
     * @return the oldCommitId
     */
    public Integer getOldCommitId() {
        return oldCommitId;
    }

    /**
     * @param id the oldCommitId to set
     */
    public void setOldCommitId(Integer id) {
        this.oldCommitId = id;
    }

    /**
     * @return the newCommitId
     */
    public Integer getNewCommitId() {
        return newCommitId;
    }

    /**
     * @param id the newCommitId to set
     */
    public void setNewCommitId(Integer id) {
        this.newCommitId = id;
    }
}
