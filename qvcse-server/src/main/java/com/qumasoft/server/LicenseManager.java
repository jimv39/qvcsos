/*   Copyright 2004-2015 Jim Voris
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

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a singleton class that we use to manage the licensing scheme for QVCS Enterprise. This really just keeps track of how many users log in. It used to enforce
 * the terms of the license, but since things are now free, it just captures who is logged in, and from where, and puts that information into the activity journal.
 *
 * @author Jim Voris
 */
public final class LicenseManager {
    // Create our logger object.
    private static final Logger LOGGER = LoggerFactory.getLogger(LicenseManager.class);
    private static final LicenseManager LICENSE_MANAGER = new LicenseManager();
    private final Map<String, Integer> userCollection;
    private final Map<String, Integer> connectionMap = Collections.synchronizedMap(new TreeMap<String, Integer>());

    /**
     * Creates a new instance of LicenseManager.
     */
    private LicenseManager() {
        userCollection = Collections.synchronizedMap(new TreeMap<String, Integer>());
    }

    /**
     * Return the singleton instance of the license manager.
     *
     * @return the singleton instance of the license manager.
     */
    public static LicenseManager getInstance() {
        return LICENSE_MANAGER;
    }

    /**
     * Called to login another user to the server. Return true if adding the user is allowed. Return false
     * if the customer does not have enough concurrent users on their license to support this additional user.
     *
     * @param message where to return an error explanation if there is one.
     * @param userName the user's name.
     * @param clientIPAddress the client IP address.
     * @return true. The license manager always allows a login.
     */
    public synchronized boolean loginUser(AtomicReference<String> message, String userName, String clientIPAddress) {

        if (connectionMap.containsKey(clientIPAddress)) {
            Integer ipUseCount = connectionMap.get(clientIPAddress);
            connectionMap.put(clientIPAddress, 1 + ipUseCount);
        } else {
            connectionMap.put(clientIPAddress, 1);
        }

        if (userCollection.containsKey(userName)) {
            Integer userLoginCount = userCollection.get(userName);
            userLoginCount = 1 + userLoginCount;
            userCollection.put(userName, userLoginCount);
        } else {
            userCollection.put(userName, 1);
        }

        String loginMessage = "User: [" + userName + "] logged in from IP address [" + clientIPAddress + "] Concurrent user count: [" + userCollection.size() + "]";
        LOGGER.info(loginMessage);
        ActivityJournalManager.getInstance().addJournalEntry(loginMessage);

        return true;
    }

    /**
     * Called when a user logs out, or otherwise disconnects from the server.
     *
     * @param userName the user's name.
     * @param clientIPAddress the user's client IP address.
     */
    public synchronized void logoutUser(String userName, final String clientIPAddress) {
        if (connectionMap.size() > 0) {
            Integer useCount = connectionMap.get(clientIPAddress);
            if (useCount == 1) {
                connectionMap.remove(clientIPAddress);
            } else {
                useCount = useCount - 1;
                connectionMap.put(clientIPAddress, useCount);
            }
        }

        if (userCollection.size() > 0) {
            Integer userLoginCount = userCollection.get(userName);
            if (userLoginCount == 1) {
                userCollection.remove(userName);
            } else {
                userLoginCount = userLoginCount - 1;
                userCollection.put(userName, userLoginCount);
            }
        }

        String message = "User: [" + userName + "] logged out from IP address [" + clientIPAddress + "] Concurrent user count: " + userCollection.size();
        LOGGER.info(message);
        ActivityJournalManager.getInstance().addJournalEntry(message);
    }

    /**
     * Get the Set of currently logged in users.
     * @return the Set of currently logged in users.
     */
    public Set<String> getUserCollection() {
        return userCollection.keySet();
    }
}
