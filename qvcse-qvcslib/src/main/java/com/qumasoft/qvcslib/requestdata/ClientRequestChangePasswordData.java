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

/**
 * Client request change password data.
 * @author Jim Voris
 */
public class ClientRequestChangePasswordData extends ClientRequestClientData {
    private static final long serialVersionUID = 1653724409261805353L;

    private static final ValidRequestElementType[] VALID_ELEMENTS = {
        ValidRequestElementType.SERVER_NAME,
        ValidRequestElementType.USER_NAME
    };
    private byte[] oldPassword;
    private byte[] newPassword;

    /**
     * Creates a new instance of ClientRequestChangePasswordData.
     */
    public ClientRequestChangePasswordData() {
    }

    /**
     * Get the old password.
     * @return the old password.
     */
    public byte[] getOldPassword() {
        return oldPassword;
    }

    /**
     * Set the old password.
     * @param password the old password.
     */
    public void setOldPassword(byte[] password) {
        oldPassword = password;
    }

    /**
     * Get the new password.
     * @return the new password.
     */
    public byte[] getNewPassword() {
        return newPassword;
    }

    /**
     * Set the new password.
     * @param password the new password.
     */
    public void setNewPassword(byte[] password) {
        newPassword = password;
    }

    @Override
    public RequestOperationType getOperationType() {
        return RequestOperationType.CHANGE_USER_PASSWORD;
    }

    @Override
    public ValidRequestElementType[] getValidElements() {
        return VALID_ELEMENTS;
    }
}
