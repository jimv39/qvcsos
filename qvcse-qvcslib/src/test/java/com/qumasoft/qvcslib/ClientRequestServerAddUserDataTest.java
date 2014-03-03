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
package com.qumasoft.qvcslib;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Server Add User Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerAddUserDataTest {

    /**
     * Test of getServerName method, of class ClientRequestServerAddUserData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerAddUserData instance = new ClientRequestServerAddUserData();
        String expResult = "Server name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestServerAddUserData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerAddUserData instance = new ClientRequestServerAddUserData();
        String expResult = "User name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPassword method, of class ClientRequestServerAddUserData.
     */
    @Test
    public void testGetPassword() {
        ClientRequestServerAddUserData instance = new ClientRequestServerAddUserData();
        String password = "password";
        instance.setPassword(password.getBytes());
        byte[] result = instance.getPassword();
        String password2 = new String(result);
        assertEquals(password, password2);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerAddUserData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestServerAddUserData instance = new ClientRequestServerAddUserData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerAddUserData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestServerAddUserData instance = new ClientRequestServerAddUserData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestServerAddUserData instance = new ClientRequestServerAddUserData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerAddUserData instance = new ClientRequestServerAddUserData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerAddUserData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestServerAddUserData instance = new ClientRequestServerAddUserData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.ADD_USER;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
