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

import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.requestdata.ClientRequestHeartBeatData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Heart Beat Data Test.
 * @author Jim Voris
 */
public class ClientRequestHeartBeatDataTest {

    /**
     * Test of getServerName method, of class ClientRequestHeartBeatData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestHeartBeatData instance = new ClientRequestHeartBeatData();
        String expResult = "Server name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestHeartBeatData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestHeartBeatData instance = new ClientRequestHeartBeatData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.HEARTBEAT;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestHeartBeatData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestHeartBeatData instance = new ClientRequestHeartBeatData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestHeartBeatData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestHeartBeatData instance = new ClientRequestHeartBeatData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }
}
