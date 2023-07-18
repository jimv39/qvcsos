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
package com.qvcsos.server;

import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.response.ServerResponseTransactionBegin;
import com.qumasoft.qvcslib.response.ServerResponseTransactionEnd;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to manage the outstanding transactions for the server. It is a singleton.
 *
 * @author Jim Voris
 */
public final class ServerTransactionManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerTransactionManager.class);

    private static final int STARTING_SERVER_TRANSACTION_NUMBER = 100000000;

    private static final ServerTransactionManager SERVER_TRANSACTION_MANAGER = new ServerTransactionManager();
    private final AtomicReference<Integer> nextTransactionId = new AtomicReference<>(STARTING_SERVER_TRANSACTION_NUMBER);
    /**
     * Our transaction counter collection
     */
    private final Map<String, Integer> openTransactionCounterMap = Collections.synchronizedMap(new HashMap<String, Integer>());
    /**
     * The list of participants in an active transaction
     */
    private final Map<String, Map<Integer, TransactionParticipantInterface>> pendingWorkToCompleteMap = Collections.synchronizedMap(new HashMap<String, Map<Integer,
            TransactionParticipantInterface>>());
    /**
     * A map of timestamps to associate with pending transactions.
     */
    private final Map<String, Date> pendingWorkTimestampMap = Collections.synchronizedMap(new HashMap<String, Date>());

    /**
     * Creates a new instance of ServerTransactionManager
     */
    private ServerTransactionManager() {
    }

    /**
     * Get the server transaction manager singleton.
     * @return the server transaction manager singleton.
     */
    public static ServerTransactionManager getInstance() {
        return SERVER_TRANSACTION_MANAGER;
    }

    /**
     * Set a begin transaction to the client.
     * @param response link to the client.
     * @return the transaction id.
     */
    public int sendBeginTransaction(ServerResponseFactoryInterface response) {
        String serverName = response.getServerName();
        int transactionID = getNextTransactionID();
        ServerResponseTransactionBegin beginTransaction = new ServerResponseTransactionBegin();
        beginTransaction.setTransactionID(transactionID);
        beginTransaction.setServerName(serverName);
        clientBeginTransaction(response);
        response.createServerResponse(beginTransaction);
        return transactionID;
    }

    /**
     * Send an end transaction to the client.
     * @param response link to the client.
     * @param transactionID the transaction id.
     */
    public void sendEndTransaction(ServerResponseFactoryInterface response, int transactionID) {
        String serverName = response.getServerName();
        ServerResponseTransactionEnd endTransaction = new ServerResponseTransactionEnd();
        endTransaction.setTransactionID(transactionID);
        endTransaction.setServerName(serverName);
        response.createServerResponse(endTransaction);
        clientEndTransaction(response);
    }

    private int getNextTransactionID() {
        return nextTransactionId.getAndSet(nextTransactionId.get() + 1);
    }

    /**
     * Mark the beginning of a client 'transaction'.
     * @param response the link to the client.
     */
    public synchronized void clientBeginTransaction(ServerResponseFactoryInterface response) {
        String key = getMapKey(response);
        Integer useCount = openTransactionCounterMap.get(key);
        if (useCount == null) {
            useCount = 1;
            pendingWorkTimestampMap.put(key, new Date());
        } else {
            useCount = 1 + useCount;
        }
        openTransactionCounterMap.put(key, useCount);
        LOGGER.debug("Transaction count for {} grows to: [{}]", key, useCount.toString());
    }

    /**
     * Mark the end of a client 'transaction'. Commit any pending work by invoking the commit pending work call back on any registered transaction participants.
     * @param response the link to the client.
     */
    public synchronized void clientEndTransaction(ServerResponseFactoryInterface response) {
        String key = getMapKey(response);
        Integer useCount = openTransactionCounterMap.get(key);
        if (useCount != null) {
            useCount = useCount - 1;
            if (useCount == 0) {
                openTransactionCounterMap.remove(key);
                Date date = pendingWorkTimestampMap.remove(key);
                commitPendingWork(key, date, response);
            } else {
                openTransactionCounterMap.put(key, useCount);
            }
            LOGGER.debug("Transaction count for {} shrinks to: [{}]", key, useCount.toString());
        } else {
            LOGGER.warn("Unexpected client end transaction");
        }
    }

    /**
     * Does the given client have a transaction in progress.
     * @param response the link to the client.
     * @return true if there is a transaction in progress for the given client; false otherwise.
     */
    public synchronized boolean transactionIsInProgress(ServerResponseFactoryInterface response) {
        boolean flag = false;
        String key = getMapKey(response);
        Integer useCount = openTransactionCounterMap.get(key);
        if (useCount != null && useCount > 0) {
            flag = true;
        }
        return flag;
    }

    /**
     * Get the timestamp of the active transaction for the given client.
     * @param response link to the client.
     * @return the timestamp of the active transaction for the given client, or null if there is no active transaction.
     */
    public synchronized Date getTransactionTimeStamp(ServerResponseFactoryInterface response) {
        String key = getMapKey(response);
        return pendingWorkTimestampMap.get(key);
    }

    /**
     * Used when the client connection goes away to throw away any client references that we may have.
     *
     * @param response link to the client.
     */
    public synchronized void flushClientTransaction(ServerResponseFactoryInterface response) {
        String key = getMapKey(response);
        openTransactionCounterMap.remove(key);
        Date date = pendingWorkTimestampMap.remove(key);
        commitPendingWork(key, date, response);
    }

    /**
     * Join as a participant in the in-progress client transaction.
     * @param response link to the client.
     * @param participant the transaction participant. The participant's commitPendingChanges method will be called when the transaction completes.
     */
    public synchronized void enlistPendingWork(ServerResponseFactoryInterface response, TransactionParticipantInterface participant) {
        String key = getMapKey(response);
        if (pendingWorkToCompleteMap.containsKey(key)) {
            pendingWorkToCompleteMap.get(key).put(participant.getPriority(), participant);
        } else {
            Map<Integer, TransactionParticipantInterface> workMap = new HashMap<>();
            workMap.put(participant.getPriority(), participant);
            pendingWorkToCompleteMap.put(key, workMap);
        }
    }

    private String getMapKey(ServerResponseFactoryInterface response) {
        return response.getClientIPAddress() + ":" + Integer.toString(response.getClientPort());
    }

    private synchronized void commitPendingWork(final String key, final Date date, ServerResponseFactoryInterface response) {
        if (pendingWorkToCompleteMap.containsKey(key)) {
            Map<Integer, TransactionParticipantInterface> workMap = pendingWorkToCompleteMap.get(key);
            Iterator<TransactionParticipantInterface> it = workMap.values().iterator();
            while (it.hasNext()) {
                TransactionParticipantInterface participant = it.next();
                try {
                    participant.commitPendingChanges(response, date);
                } catch (QVCSException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
            pendingWorkToCompleteMap.remove(key);
            // And close this thread's database connection.
            try {
                DatabaseManager.getInstance().closeConnection();
            } catch (SQLException e) {
                LOGGER.warn("Failed to close connection.", e);
                throw new QVCSRuntimeException("Failed to close database connection.");
            }
        }
    }
}
