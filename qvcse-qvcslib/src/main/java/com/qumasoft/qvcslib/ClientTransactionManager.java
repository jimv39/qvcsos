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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.requestdata.ClientRequestTransactionBeginData;
import com.qumasoft.qvcslib.requestdata.ClientRequestTransactionEndData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to manage the outstanding transactions for the client. It is a singleton.
 *
 * @author Jim Voris
 */
public final class ClientTransactionManager {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientTransactionManager.class);
    private static final int STARTING_TRANSACTION_ID = 1000;

    private static final ClientTransactionManager CLIENT_TRANSACTION_MANAGER = new ClientTransactionManager();
    private final List<TransactionInProgressListenerInterface> transactionInProgressListeners;
    private final Map<Integer, TransactionIdentifier> transactionIDMap;
    private int nextTransactionID;

    /**
     * Creates a new instance of WorkfileDigestDictionary.
     */
    private ClientTransactionManager() {
        this.transactionIDMap = Collections.synchronizedMap(new HashMap<>());
        this.transactionInProgressListeners = Collections.synchronizedList(new ArrayList<>());
        nextTransactionID = STARTING_TRANSACTION_ID;
    }

    /**
     * Get the singleton instance.
     *
     * @return the singleton instance.
     */
    public static ClientTransactionManager getInstance() {
        return CLIENT_TRANSACTION_MANAGER;
    }

    private int getNextTransactionID() {
        return nextTransactionID++;
    }

    /**
     * Create the transaction id for a given server.
     * @param serverName the server name.
     * @return a client transaction id.
     */
    public synchronized int createTransactionIdentifier(String serverName) {
        int transactionID = getNextTransactionID();
        TransactionIdentifier transactionIdentifier = new TransactionIdentifier(serverName, transactionID);
        Integer integerTransactionID = transactionID;
        transactionIDMap.put(integerTransactionID, transactionIdentifier);

        return transactionID;
    }

    /**
     * If the server connection goes away, we need to discard any pending transactions associated with that server. If as a result of discarding those transactions, the global
     * transaction count goes to zero, we need to let any listeners know that there are no remaining open transactions (so the progress bar will go away, for example).
     * @param transportProxy identify the connection for which we discard transactions.
     */
    public synchronized void discardServerTransactions(TransportProxyInterface transportProxy) {
        LOGGER.info("discarding transactions for: [{}] open transaction count: [{}]", transportProxy.getServerProperties().getServerName(), getOpenTransactionCount());
        String serverName = transportProxy.getServerProperties().getServerName();
        Iterator it = transactionIDMap.values().iterator();
        while (it.hasNext()) {
            TransactionIdentifier transactionIdentifier = (TransactionIdentifier) it.next();
            if (transactionIdentifier.getServerName().equals(serverName)) {
                it.remove();
            }
        }

        if (getOpenTransactionCount() == 0) {
            it = transactionInProgressListeners.iterator();
            while (it.hasNext()) {
                TransactionInProgressListenerInterface listener = (TransactionInProgressListenerInterface) it.next();
                LOGGER.info("Setting transaction in progress to false for listener: [" + listener.getClass().getSimpleName() + "]");
                listener.setTransactionInProgress(false);
            }
        }
    }

    /**
     * Set a begin transaction message to the server.
     * @param transportProxy the server connection.
     * @return the client transaction id.
     */
    public int sendBeginTransaction(TransportProxyInterface transportProxy) {
        int transactionID = 0;
        if (transportProxy != null) {
            String serverName = transportProxy.getServerProperties().getServerName();
            transactionID = createTransactionIdentifier(serverName);
            ClientRequestTransactionBeginData beginTransactionData = new ClientRequestTransactionBeginData();
            beginTransactionData.setTransactionID(transactionID);
            beginTransactionData.setServerName(serverName);
            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, beginTransactionData);
        } else {
            LOGGER.warn("Missing transportProxy at 121!!!");
        }
        return transactionID;
    }

    /**
     * Set a begin transaction message to the server.
     * @param transportProxy the server connection.
     * @param transactionID the transaction id.
     * @return the client transaction id.
     */
    public int sendBeginTransaction(TransportProxyInterface transportProxy, int transactionID) {
        if (transportProxy != null) {
            QumaAssert.isTrue(getTransactionIdentifier(transactionID) != null);
            String serverName = transportProxy.getServerProperties().getServerName();
            ClientRequestTransactionBeginData beginTransactionData = new ClientRequestTransactionBeginData();
            beginTransactionData.setTransactionID(transactionID);
            beginTransactionData.setServerName(serverName);
            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, beginTransactionData);
        } else {
            LOGGER.warn("Missing transportProxy at 141!!!");
        }
        return transactionID;
    }

    /**
     * Send a begin transaction message to the server.
     * @param serverProperties the server properties.
     * @return the transaction id.
     */
    public int sendBeginTransaction(final ServerProperties serverProperties) {
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
        return sendBeginTransaction(transportProxy);
    }

    /**
     * Send an end transaction message to the server.
     * @param transportProxy the server connection.
     * @param transactionID the transaction id.
     */
    public void sendEndTransaction(TransportProxyInterface transportProxy, int transactionID) {
        if ((transportProxy != null) && (transactionID != 0)) {
            String serverName = transportProxy.getServerProperties().getServerName();
            ClientRequestTransactionEndData endTransactionData = new ClientRequestTransactionEndData();
            endTransactionData.setTransactionID(transactionID);
            endTransactionData.setServerName(serverName);
            SynchronizationManager.getSynchronizationManager().waitOnToken(transportProxy, endTransactionData);
        } else {
            LOGGER.info("Missing transportProxy 169");
        }
    }

    /**
     * Send an end transaction message to the server.
     * @param serverProperties the server properties.
     * @param transactionID the transaction id.
     */
    public void sendEndTransaction(final ServerProperties serverProperties, int transactionID) {
        TransportProxyInterface transportProxy = TransportProxyFactory.getInstance().getTransportProxy(serverProperties);
        sendEndTransaction(transportProxy, transactionID);
    }

    /**
     * Mark the start of a transaction on a given server. As a side effect, we let listeners know that a transaction is in progress.
     * @param serverName the name of the server.
     * @param transactionID the transaction id for this transaction.
     */
    public synchronized void beginTransaction(String serverName, int transactionID) {
        LOGGER.debug("Begin transaction for serverName: [" + serverName + "] transactionID: [" + transactionID + "]");
        Integer integerTransactionID = transactionID;
        TransactionIdentifier transactionIdentifier = transactionIDMap.get(integerTransactionID);
        if (transactionIdentifier == null) {
            createTransactionIdentifier(serverName, transactionID);
        }
        Iterator<TransactionInProgressListenerInterface> it = transactionInProgressListeners.iterator();
        while (it.hasNext()) {
            TransactionInProgressListenerInterface listener = it.next();
            listener.setTransactionInProgress(true);
        }
    }

    /**
     * Mark the start of a transaction on a given server. This method will also let listeners know that a transaction is in progress.
     * @param serverName the name of the server.
     * @return the transaction id.
     */
    public synchronized int beginTransaction(String serverName) {
        int transactionID = createTransactionIdentifier(serverName);
        LOGGER.debug("Begin transaction for serverName: [" + serverName + "] transactionID: [" + transactionID + "]");
        Iterator<TransactionInProgressListenerInterface> it = transactionInProgressListeners.iterator();
        while (it.hasNext()) {
            TransactionInProgressListenerInterface listener = it.next();
            listener.setTransactionInProgress(true);
        }
        return transactionID;
    }

    /**
     * End the given transaction.
     * @param serverName the server name.
     * @param transactionID the transaction id.
     */
    public synchronized void endTransaction(String serverName, int transactionID) {
        Integer integerTransactionID = transactionID;
        transactionIDMap.remove(integerTransactionID);
        int openTransactionCount = getOpenTransactionCount();
        if (openTransactionCount == 0) {
            Iterator<TransactionInProgressListenerInterface> it = transactionInProgressListeners.iterator();
            while (it.hasNext()) {
                TransactionInProgressListenerInterface listener = it.next();
                listener.setTransactionInProgress(false);
            }
        }
        LOGGER.debug("End transaction for serverName: [" + serverName + "] transactionID: [" + transactionID + "]");
    }

    /**
     * Add a transaction in progress listener. The listener receives notifications for the start and end of transactions.
     * @param listener a transaction in progress listener.
     */
    public synchronized void addTransactionInProgressListener(TransactionInProgressListenerInterface listener) {
        transactionInProgressListeners.add(listener);
    }

    /**
     * Remove a transaction in progress listener. The listener will no longer be notified when a client transaction starts or completes.
     * @param listener the listener to remove.
     */
    public synchronized void removeTransactionInProgressListener(TransactionInProgressListenerInterface listener) {
        transactionInProgressListeners.remove(listener);
    }

    /**
     * Get the number of open transactions.
     * @return the number of open transactions.
     */
    public synchronized int getOpenTransactionCount() {
        return transactionIDMap.size();
    }

    private TransactionIdentifier createTransactionIdentifier(String serverName, int transactionID) {
        TransactionIdentifier transactionIdentifier = new TransactionIdentifier(serverName, transactionID);
        Integer integerTransactionID = transactionID;
        transactionIDMap.put(integerTransactionID, transactionIdentifier);

        return transactionIdentifier;
    }

    private TransactionIdentifier getTransactionIdentifier(int transactionID) {
        return transactionIDMap.get(transactionID);
    }

    class TransactionIdentifier {

        private final String serverName;
        private final Integer transactionID;

        TransactionIdentifier(String server, Integer transID) {
            serverName = server;
            transactionID = transID;
        }

        String getServerName() {
            return serverName;
        }

        Integer getTransactionID() {
            return transactionID;
        }
    }

}
