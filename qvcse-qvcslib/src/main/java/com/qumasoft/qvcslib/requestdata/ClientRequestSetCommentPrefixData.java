/*   Copyright 2004-2019 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.qvcslib.requestdata;

/**
 * Set comment prefix request data.
 * @author Jim Voris
 */
public class ClientRequestSetCommentPrefixData extends ClientRequestClientData {
    private static final long serialVersionUID = 4989107775188477332L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.APPENDED_PATH,
        ValidRequestElementType.SHORT_WORKFILE_NAME
    };
    private String commentPrefix;

    /**
     * Creates a new instance of ClientRequestSetAttributesData.
     */
    public ClientRequestSetCommentPrefixData() {
    }

    /**
     * Get the comment prefix.
     * @return the comment prefix.
     */
    public String getCommentPrefix() {
        return commentPrefix;
    }

    /**
     * Set the comment prefix.
     * @param prefix the comment prefix.
     */
    public void setCommentPrefix(String prefix) {
        commentPrefix = prefix;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SET_COMMENT_PREFIX;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
