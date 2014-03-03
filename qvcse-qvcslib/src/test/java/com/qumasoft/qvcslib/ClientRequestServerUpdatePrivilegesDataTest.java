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
 * Client Request Server Update Privileges Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerUpdatePrivilegesDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestServerUpdatePrivilegesData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestServerUpdatePrivilegesData instance = new ClientRequestServerUpdatePrivilegesData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerUpdatePrivilegesData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestServerUpdatePrivilegesData instance = new ClientRequestServerUpdatePrivilegesData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestServerUpdatePrivilegesData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerUpdatePrivilegesData instance = new ClientRequestServerUpdatePrivilegesData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestServerUpdatePrivilegesData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerUpdatePrivilegesData instance = new ClientRequestServerUpdatePrivilegesData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRoleName method, of class ClientRequestServerUpdatePrivilegesData.
     */
    @Test
    public void testGetRoleName() {
        ClientRequestServerUpdatePrivilegesData instance = new ClientRequestServerUpdatePrivilegesData();
        RoleType expResult = new RoleType("Role Name");
        instance.setRole(expResult);
        String result = instance.getRole().getRoleType();
        assertEquals("Role Name", result);
    }

    /**
     * Test of getPrivileges method, of class ClientRequestServerUpdatePrivilegesData.
     */
    @Test
    public void testGetPrivileges() {
        ClientRequestServerUpdatePrivilegesData instance = new ClientRequestServerUpdatePrivilegesData();
        String[] privileges = new String[]{"one", "two", "three"};
        instance.setPrivileges(privileges);
        String[] result = instance.getPrivileges();
        assertEquals(privileges[0], result[0]);
    }

    /**
     * Test of getPrivilegesFlags method, of class ClientRequestServerUpdatePrivilegesData.
     */
    @Test
    public void testGetPrivilegesFlags() {
        ClientRequestServerUpdatePrivilegesData instance = new ClientRequestServerUpdatePrivilegesData();
        Boolean[] privilegeFlags = new Boolean[]{Boolean.TRUE, Boolean.TRUE, Boolean.TRUE};
        instance.setPrivilegesFlags(privilegeFlags);
        Boolean[] result = instance.getPrivilegesFlags();
        assertEquals(privilegeFlags[0], result[0]);
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerUpdatePrivilegesData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerUpdatePrivilegesData instance = new ClientRequestServerUpdatePrivilegesData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_UPDATE_ROLE_PRIVILEGES;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
