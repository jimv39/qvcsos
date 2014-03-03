//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.qvcslib;

/**
 * Move file request data.
 * @author Jim Voris
 */
public class ClientRequestMoveFileData extends ClientRequestClientData {
    private static final long serialVersionUID = -5001241007143150740L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.SHORT_WORKFILE_NAME
    };
    private String originalAppendedPath;
    private String newAppendedPath;

    /**
     * Creates new ClientRequestMoveFileData.
     */
    public ClientRequestMoveFileData() {
    }

    /**
     * Get the original appended path.
     * @return the original appended path.
     */
    public String getOriginalAppendedPath() {
        return originalAppendedPath;
    }

    /**
     * Set the original appended path.
     * @param arg the original appended path.
     */
    public void setOriginalAppendedPath(String arg) {
        originalAppendedPath = arg;
    }

    /**
     * Get the new appended path.
     * @return the new appended path.
     */
    public String getNewAppendedPath() {
        return newAppendedPath;
    }

    /**
     * Set the new appended path.
     * @param newPath the new appended path.
     */
    public void setNewAppendedPath(String newPath) {
        newAppendedPath = newPath;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.MOVE_FILE;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
