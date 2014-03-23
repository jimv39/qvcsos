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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerRemoveUserData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Server Remove User Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerRemoveUserDataTest {

    /**
     * Test of getUserName method, of class ClientRequestServerRemoveUserData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerRemoveUserData instance = new ClientRequestServerRemoveUserData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestServerRemoveUserData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerRemoveUserData instance = new ClientRequestServerRemoveUserData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerRemoveUserData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestServerRemoveUserData instance = new ClientRequestServerRemoveUserData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerRemoveUserData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestServerRemoveUserData instance = new ClientRequestServerRemoveUserData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerRemoveUserData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerRemoveUserData instance = new ClientRequestServerRemoveUserData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.REMOVE_USER;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
