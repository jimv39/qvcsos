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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import java.util.Date;

/**
 * Transaction participant interface.
 * @author Jim Voris
 */
public interface TransactionParticipantInterface {

    /** A high priority participant. */
    int HIGH_PRIORITY = 1;
    /** A don't care priority participant. */
    int DONT_CARE_PRIORITY = 2;
    /** A low priority participant. */
    int LOW_PRIORITY = 3;

    /**
     * Call back to commit any pending changes.
     * @param response link to the client.
     * @param date the datestamp of the transaction so all participants can use the same date.
     * @throws QVCSException if something goes wrong.
     */
    void commitPendingChanges(ServerResponseFactoryInterface response, Date date) throws QVCSException;

    /**
     * Get the priority of the participant. Higher priority participants are processed before lower priority participants.
     * @return the participants priority.
     */
    int getPriority();
}
