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
 * Set is obsolete request data.
 * @author Jim Voris
 */
public class ClientRequestSetIsObsoleteData extends ClientRequestClientData {
    private static final long serialVersionUID = -8350827076820399253L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.APPENDED_PATH,
        ValidRequestElementType.SHORT_WORKFILE_NAME
    };
    private String flag;

    /**
     * Creates a new instance of ClientRequestSetIsObsoleteData.
     */
    public ClientRequestSetIsObsoleteData() {
    }

    /**
     * Get the value of flag.
     * @return the value of flag.
     */
    public boolean getFlag() {
        return flag.equals(QVCSConstants.QVCS_YES);
    }

    /**
     * Set the value of flag.
     * @param f the value of flag.
     */
    public void setFlag(boolean f) {
        if (f) {
            flag = QVCSConstants.QVCS_YES;
        } else {
            flag = QVCSConstants.QVCS_NO;
        }
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SET_OBSOLETE;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
