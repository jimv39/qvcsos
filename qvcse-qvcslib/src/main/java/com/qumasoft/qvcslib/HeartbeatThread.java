/*   Copyright 2004-2021 Jim Voris
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

import com.qumasoft.qvcslib.requestdata.ClientRequestHeartBeatData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Heartbeat thread.
 * @author Jim Voris
 */
public class HeartbeatThread extends java.lang.Thread {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatThread.class);

    private final TransportProxyInterface localProxy;
    private boolean continueFlag = true;

    /**
     * Create a heartbeat thread for the given connection.
     * @param transportProxy the server connection.
     */
    public HeartbeatThread(TransportProxyInterface transportProxy) {
        localProxy = transportProxy;
        setDaemon(true);
    }

    /**
     * Method to shut down the heartbeat thread.
     */
    public void terminateHeartBeatThread() {
        this.continueFlag = false;
        interrupt();
    }

    /**
     * Send heartbeat messages to server every 120 seconds.
     */
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        ClientRequestHeartBeatData heartBeat = new ClientRequestHeartBeatData();

        while (this.continueFlag) {

            if (localProxy != null) {
                heartBeat.setServerName(localProxy.getServerProperties().getServerName());
                try {
                    sleep(QVCSConstants.HEART_BEAT_SLEEP_TIME);
                    if (localProxy.getIsOpen()) {
                        localProxy.write(heartBeat);
                        LOGGER.trace("Sent heartbeat to server for heartbeat thread [" + this.getName() + "]");
                    } else {
                        LOGGER.warn("Local proxy is closed for heartbeat thread [" + this.getName() + "]");
                        continueFlag = false;
                    }
                } catch (InterruptedException e) {
                    LOGGER.warn("Caught exception: [" + e.getClass().getName() + "] QVCS-Enterprise client heartbeat thread exiting for heartbeat thread ["
                            + this.getName() + "]");
                    continueFlag = false;

                    // Restore interrupted state...
                    Thread.currentThread().interrupt();
                }
            } else {
                continueFlag = false;
            }
        }
        LOGGER.warn("QVCS-Enterprise client heartbeat thread exiting for heartbeat thread [" + this.getName() + "]");
    }
}
