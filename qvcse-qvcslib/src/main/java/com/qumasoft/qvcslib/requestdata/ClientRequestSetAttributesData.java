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
package com.qumasoft.qvcslib.requestdata;

import com.qumasoft.qvcslib.ArchiveAttributes;

/**
 * Set attributes request data.
 * @author Jim Voris
 */
public class ClientRequestSetAttributesData extends ClientRequestClientData {
    private static final long serialVersionUID = 3155828882018802461L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.VIEW_NAME,
        ValidRequestElementType.APPENDED_PATH,
        ValidRequestElementType.SHORT_WORKFILE_NAME
    };
    private ArchiveAttributes attributes;

    /**
     * Creates a new instance of ClientRequestSetAttributesData.
     */
    public ClientRequestSetAttributesData() {
    }

    /**
     * Get the QVCS archive attributes.
     * @return the QVCS archive attributes.
     */
    public ArchiveAttributes getAttributes() {
        return attributes;
    }

    /**
     * Set the QVCS archive attributes.
     * @param attribs the QVCS archive attributes.
     */
    public void setAttributes(ArchiveAttributes attribs) {
        attributes = attribs;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.SET_ATTRIBUTES;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
