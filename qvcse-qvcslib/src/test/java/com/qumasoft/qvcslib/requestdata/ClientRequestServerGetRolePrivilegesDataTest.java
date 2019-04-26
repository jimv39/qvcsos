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
 * Client Request Server Get Role Privileges Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerGetRolePrivilegesDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestServerGetRolePrivilegesData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestServerGetRolePrivilegesData instance = new ClientRequestServerGetRolePrivilegesData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerGetRolePrivilegesData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestServerGetRolePrivilegesData instance = new ClientRequestServerGetRolePrivilegesData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestServerGetRolePrivilegesData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerGetRolePrivilegesData instance = new ClientRequestServerGetRolePrivilegesData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestServerGetRolePrivilegesData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerGetRolePrivilegesData instance = new ClientRequestServerGetRolePrivilegesData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRoleName method, of class ClientRequestServerGetRolePrivilegesData.
     */
    @Test
    public void testGetRoleName() {
        ClientRequestServerGetRolePrivilegesData instance = new ClientRequestServerGetRolePrivilegesData();
        RoleType expResult = new RoleType("Role Name");
        instance.setRole(expResult);
        RoleType result = instance.getRole();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerGetRolePrivilegesData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerGetRolePrivilegesData instance = new ClientRequestServerGetRolePrivilegesData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_GET_ROLE_PRIVILEGES;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
