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
 * Client Request Server Assign User Roles Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerAssignUserRolesDataTest {

    /**
     * Test of getServerName method, of class ClientRequestServerAssignUserRolesData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerAssignUserRolesData instance = new ClientRequestServerAssignUserRolesData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerAssignUserRolesData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestServerAssignUserRolesData instance = new ClientRequestServerAssignUserRolesData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerAssignUserRolesData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestServerAssignUserRolesData instance = new ClientRequestServerAssignUserRolesData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestServerAssignUserRolesData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerAssignUserRolesData instance = new ClientRequestServerAssignUserRolesData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAssignedRoles method, of class ClientRequestServerAssignUserRolesData.
     */
    @Test
    public void testGetAssignedRoles() {
        ClientRequestServerAssignUserRolesData instance = new ClientRequestServerAssignUserRolesData();
        String[] expResult = new String[]{"ADMIN", "DEVELOPER"};
        instance.setAssignedRoles(expResult);
        String[] result = instance.getAssignedRoles();
        assertEquals(expResult[0], result[0]);
        assertEquals(expResult[1], result[1]);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestServerAssignUserRolesData instance = new ClientRequestServerAssignUserRolesData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerAssignUserRolesData instance = new ClientRequestServerAssignUserRolesData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerAssignUserRolesData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerAssignUserRolesData instance = new ClientRequestServerAssignUserRolesData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.ASSIGN_USER_ROLES;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
