/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.qvcslib.requestdata;

import com.qumasoft.qvcslib.QVCSRuntimeException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Server Shutdown Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerShutdownDataTest {

    /**
     * Test of getUserName method, of class ClientRequestServerShutdownData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerShutdownData instance = new ClientRequestServerShutdownData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPassword method, of class ClientRequestServerShutdownData.
     */
    @Test
    public void testGetPassword() {
        ClientRequestServerShutdownData instance = new ClientRequestServerShutdownData();
        String password = "Password";
        byte[] expResult = password.getBytes();
        instance.setPassword(expResult);
        byte[] result = instance.getPassword();
        assertEquals(result.length, expResult.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
    }

    /**
     * Test of getServerName method, of class ClientRequestServerShutdownData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerShutdownData instance = new ClientRequestServerShutdownData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerShutdownData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestServerShutdownData instance = new ClientRequestServerShutdownData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerShutdownData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestServerShutdownData instance = new ClientRequestServerShutdownData();
        String expResult = null;
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerShutdownData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestServerShutdownData instance = new ClientRequestServerShutdownData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_SHUTDOWN;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
