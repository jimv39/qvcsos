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
import com.qumasoft.qvcslib.RoleType;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Server Delete Role Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerDeleteRoleDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestServerDeleteRoleData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestServerDeleteRoleData instance = new ClientRequestServerDeleteRoleData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerDeleteRoleData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestServerDeleteRoleData instance = new ClientRequestServerDeleteRoleData();
        String expResult = null;
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestServerDeleteRoleData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerDeleteRoleData instance = new ClientRequestServerDeleteRoleData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestServerDeleteRoleData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerDeleteRoleData instance = new ClientRequestServerDeleteRoleData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRole method, of class ClientRequestServerDeleteRoleData.
     */
    @Test
    public void testGetRole() {
        ClientRequestServerDeleteRoleData instance = new ClientRequestServerDeleteRoleData();
        RoleType expResult = new RoleType("Role");
        instance.setRole(expResult);
        String result = instance.getRole().getRoleType();
        assertEquals(expResult.getRoleType(), result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerDeleteRoleData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerDeleteRoleData instance = new ClientRequestServerDeleteRoleData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_DELETE_ROLE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
