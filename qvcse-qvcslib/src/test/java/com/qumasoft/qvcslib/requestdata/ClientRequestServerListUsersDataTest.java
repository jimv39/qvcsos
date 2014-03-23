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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListUsersData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Server List Users Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerListUsersDataTest {

    /**
     * Test of getServerName method, of class ClientRequestServerListUsersData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerListUsersData instance = new ClientRequestServerListUsersData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerListUsersData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestServerListUsersData instance = new ClientRequestServerListUsersData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerListUsersData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestServerListUsersData instance = new ClientRequestServerListUsersData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerListUsersData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerListUsersData instance = new ClientRequestServerListUsersData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.LIST_USERS;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
