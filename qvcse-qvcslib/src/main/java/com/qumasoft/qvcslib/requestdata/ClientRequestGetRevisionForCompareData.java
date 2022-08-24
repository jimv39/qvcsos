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
 * Get revision for compare request data.
 * @author Jim Voris
 */
public class ClientRequestGetRevisionForCompareData extends ClientRequestClientData {
    private static final long serialVersionUID = 7277731777955658296L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.PROJECT_NAME,
        ValidRequestElementType.BRANCH_NAME,
        ValidRequestElementType.APPENDED_PATH,
        ValidRequestElementType.SHORT_WORKFILE_NAME,
        ValidRequestElementType.REVISION_STRING,
        ValidRequestElementType.FILE_ID,
        ValidRequestElementType.SYNC_TOKEN
    };
    private Boolean logfileInfoRequiredFlag;

    /**
     * Creates new ClientRequestGetRevisionForCompareData.
     */
    public ClientRequestGetRevisionForCompareData() {
    }

    /**
     * Get the is logfile info required flag.
     * @return the is logfile info required flag.
     */
    public boolean getIsLogfileInfoRequired() {
        return logfileInfoRequiredFlag.booleanValue();
    }

    /**
     * Set the is logfile info required flag.
     * @param flag the is logfile info required flag.
     */
    public void setIsLogfileInfoRequired(boolean flag) {
        logfileInfoRequiredFlag = Boolean.valueOf(flag);
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.GET_REVISION_FOR_COMPARE;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
