/*   Copyright 2004-2022 Jim Voris
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
package com.qumasoft.qvcslib.notifications;

import java.io.Serializable;

/**
 * Define those methods needed for handling a server notification.
 * @author Jim Voris
 */
public interface ServerNotificationInterface extends Serializable {

    /**
     * Get the server name.
     * @return the server name.
     */
    String getServerName();

    /**
     * Set the server name.
     * @param server the server name.
     */
    void setServerName(String server);

    /**
     * Get the branch name.
     * @return get the branch name.
     */
    String getBranchName();

    /**
     * Set the branch name.
     * @param branchName branch name to set.
     */
    void setBranchName(String branchName);

    /**
     * Set the branch id.
     *
     * @param id the branch id.
     */
    void setBranchId(Integer id);

    /**
     * Get the branch id.
     *
     * @return get the branch id.
     */
    Integer getBranchId();

    /**
     * Get the type of notification.
     * @return the type of notification.
     */
    NotificationType getNotificationType();

    /**
     * The types of notification messages that we can receive from the server.
     */
    enum NotificationType {
        /** Checkin notification. */
        SR_NOTIFY_CHECKIN,
        /** Create notification. */
        SR_NOTIFY_CREATE,
        /** Remove notification. */
        SR_NOTIFY_REMOVE,
        /** Rename notification. */
        SR_NOTIFY_RENAME,
        /** Move notification. */
        SR_NOTIFY_MOVEFILE,
        /** Info notification. */
        SR_NOTIFY_INFO
    }
}
