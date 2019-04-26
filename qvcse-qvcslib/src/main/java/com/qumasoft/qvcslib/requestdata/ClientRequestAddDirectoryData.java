/*   Copyright 2004-2014 Jim Voris
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
 * Client request add directory data.
 * @author Jim Voris
 */
public class ClientRequestAddDirectoryData extends ClientRequestClientData {
    private static final long serialVersionUID = -5375898280636340161L;

    private final ValidRequestElementType[] validElements = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.APPENDED_PATH
    };

    /**
     * Creates a new instance of ClientRequestCreateArchiveData.
     */
    public ClientRequestAddDirectoryData() {
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.ADD_DIRECTORY;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return validElements;
    }
}
