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
 * Ask the server for information that will help the client figure out what kind of merge we have to perform on the client. The request basically consists of the fileID, and the
 * response should include the location (appended path), and short workfile name of that file on the parent branch (typically the trunk).
 *
 * @author Jim Voris
 */
public class ClientRequestGetInfoForMergeData extends ClientRequestClientData {
    private static final long serialVersionUID = -72798186550312711L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.APPENDED_PATH,
        ValidRequestElementType.FILE_ID
    };

    /**
     * Creates new ClientRequestGetInfoForMergeData.
     */
    public ClientRequestGetInfoForMergeData() {
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.GET_INFO_FOR_MERGE;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
