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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListProjectUsersData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Server List Project Users Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerListProjectUsersDataTest {

    /**
     * Test of getServerName method, of class ClientRequestServerListProjectUsersData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerListProjectUsersData instance = new ClientRequestServerListProjectUsersData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerListProjectUsersData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestServerListProjectUsersData instance = new ClientRequestServerListProjectUsersData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerListProjectUsersData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestServerListProjectUsersData instance = new ClientRequestServerListProjectUsersData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestServerListProjectUsersData instance = new ClientRequestServerListProjectUsersData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerListProjectUsersData instance = new ClientRequestServerListProjectUsersData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerListProjectUsersData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerListProjectUsersData instance = new ClientRequestServerListProjectUsersData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.LIST_PROJECT_USERS;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
