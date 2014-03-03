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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ClientRequestHeartBeatData;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseHeartBeat;
import com.qumasoft.qvcslib.ServerResponseInterface;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Process a heartbeat message from the client.
 *
 * @author Jim Voris
 */
public class ClientRequestHeartBeat implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestHeartBeatData request;

    /**
     * Creates a new instance of ClientRequestHeartBeat.
     *
     * @param data instance of super class that has command arguments, etc.
     */
    public ClientRequestHeartBeat(ClientRequestHeartBeatData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface responseFactory) {
        ServerResponseHeartBeat heartBeatResponse = new ServerResponseHeartBeat();
        heartBeatResponse.setServerName(responseFactory.getServerName());

        LOGGER.log(Level.FINE, "Processed heartbeat message from user: [" + userName + "] at IP address: [" + responseFactory.getClientIPAddress() + "]");

        return heartBeatResponse;
    }
}
