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

import com.qumasoft.qvcslib.ClientRequestUpdateClientData;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseUpdateClient;
import com.qumasoft.qvcslib.Utility;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request update client.
 * @author Jim Voris
 */
public class ClientRequestUpdateClient implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestUpdateClientData request;

    /**
     * Creates a new instance of ClientRequestUpdateClient.
     *
     * @param data command line data, etc.
     */
    public ClientRequestUpdateClient(ClientRequestUpdateClientData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseUpdateClient serverResponse = null;
        InputStream streamFromJar = null;
        try {
            String resourceName = "/" + request.getRequestedFileName();
            streamFromJar = this.getClass().getResourceAsStream(resourceName);
            if (streamFromJar != null) {
                byte[] buffer = new byte[streamFromJar.available()];

                int bytesRemaining = buffer.length;
                int offset = 0;
                while (bytesRemaining > 0) {
                    int bytesRead = streamFromJar.read(buffer, offset, bytesRemaining);
                    bytesRemaining -= bytesRead;
                    offset += bytesRead;
                }

                serverResponse = new ServerResponseUpdateClient();
                serverResponse.setBuffer(buffer);
                serverResponse.setRequestedFileName(request.getRequestedFileName());
                serverResponse.setRestartFlag(request.getRestartFlag());
            }
        } catch (IOException e) {
            serverResponse = null;
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        } finally {
            if (streamFromJar != null) {
                try {
                    streamFromJar.close();
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
        }

        return serverResponse;
    }
}
