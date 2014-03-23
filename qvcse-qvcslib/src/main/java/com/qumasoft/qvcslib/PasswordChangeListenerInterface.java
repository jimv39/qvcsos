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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.response.ServerResponseChangePassword;
import com.qumasoft.qvcslib.response.ServerResponseLogin;

/**
 * Password change listener interface. Implemented by those clients that need notification on receipt of a login response message.
 * @author Jim Voris
 */
public interface PasswordChangeListenerInterface {

    /**
     * Notify that password has changed. Listener's implementation will be called on receipt of a password change response from the server.
     * @param response the password change response from the server.
     */
    void notifyPasswordChange(ServerResponseChangePassword response);

    /**
     * Notify that login response has been received. Listener's implementation will be called on receipt of a login response message from the server.
     * @param response the login response message.
     */
    void notifyLoginResult(ServerResponseLogin response);

    /**
     * Notify listeners that an update has completed. Listener's implementation will be called on receipt of an update response message from the server... i.e. the client just
     * received an updated jar file.
     */
    void notifyUpdateComplete();

    /**
     * Save the pending password.
     * @param serverName the name of the server (from the client's perspective).
     * @param password the password to save (in the clear).
     */
    void savePendingPassword(String serverName, String password);

    /**
     * Get the pending password.
     * @param serverName the name of the server (from the client's perspective).
     * @return the pending password (in the clear).
     */
    String getPendingPassword(String serverName);
}
