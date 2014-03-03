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
 * Client Request Server Get Role Names Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerGetRoleNamesDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestServerGetRoleNamesData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestServerGetRoleNamesData instance = new ClientRequestServerGetRoleNamesData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerGetRoleNamesData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestServerGetRoleNamesData instance = new ClientRequestServerGetRoleNamesData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestServerGetRoleNamesData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerGetRoleNamesData instance = new ClientRequestServerGetRoleNamesData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPassword method, of class ClientRequestServerGetRoleNamesData.
     */
    @Test
    public void testGetPassword() {
        ClientRequestServerGetRoleNamesData instance = new ClientRequestServerGetRoleNamesData();
        String password = "password";
        instance.setPassword(password.getBytes());
        byte[] result = instance.getPassword();
        String password2 = new String(result);
        assertEquals(password, password2);
    }

    /**
     * Test of getServerName method, of class ClientRequestServerGetRoleNamesData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerGetRoleNamesData instance = new ClientRequestServerGetRoleNamesData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerGetRoleNamesData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerGetRoleNamesData instance = new ClientRequestServerGetRoleNamesData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_GET_ROLES;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
