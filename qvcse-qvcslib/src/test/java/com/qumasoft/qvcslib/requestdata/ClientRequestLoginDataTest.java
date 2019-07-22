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
 * Client Request Login Data Test.
 * @author Jim Voris
 */
public class ClientRequestLoginDataTest {

    /**
     * Test of getUserName method, of class ClientRequestLoginData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestLoginData instance = new ClientRequestLoginData();
        String expResult = "Username";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPassword method, of class ClientRequestLoginData.
     */
    @Test
    public void testGetPassword() {
        ClientRequestLoginData instance = new ClientRequestLoginData();
        String password = "Pass word";
        instance.setPassword(password.getBytes());
        byte[] result = instance.getPassword();
        String password2 = new String(result);
        assertEquals(password, password2);
    }

    /**
     * Test of getServerName method, of class ClientRequestLoginData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestLoginData instance = new ClientRequestLoginData();
        String expResult = "Server name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getVersion method, of class ClientRequestLoginData.
     */
    @Test
    public void testGetVersion() {
        ClientRequestLoginData instance = new ClientRequestLoginData();
        String expResult = "Version";
        instance.setVersion(expResult);
        String result = instance.getVersion();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestLoginData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestLoginData instance = new ClientRequestLoginData();
        String expResult = "";
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestLoginData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestLoginData instance = new ClientRequestLoginData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.LOGIN;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestLoginData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetBranchName() {
        ClientRequestLoginData instance = new ClientRequestLoginData();
        String expResult = null;
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }
}
