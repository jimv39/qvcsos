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
 * Client Request Server Remove User Role Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerRemoveUserRoleDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestServerRemoveUserRoleData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestServerRemoveUserRoleData instance = new ClientRequestServerRemoveUserRoleData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerRemoveUserRoleData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestServerRemoveUserRoleData instance = new ClientRequestServerRemoveUserRoleData();
        String expResult = "View Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestServerRemoveUserRoleData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerRemoveUserRoleData instance = new ClientRequestServerRemoveUserRoleData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRole method, of class ClientRequestServerRemoveUserRoleData.
     */
    @Test
    public void testGetRole() {
        ClientRequestServerRemoveUserRoleData instance = new ClientRequestServerRemoveUserRoleData();
        String expResult = "User Role";
        RoleType roleType = new RoleType(expResult);
        instance.setRole(roleType);
        RoleType result = instance.getRole();
        assertEquals(expResult, result.toString());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestServerRemoveUserRoleData instance = new ClientRequestServerRemoveUserRoleData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerRemoveUserRoleData instance = new ClientRequestServerRemoveUserRoleData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerRemoveUserRoleData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerRemoveUserRoleData instance = new ClientRequestServerRemoveUserRoleData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.REMOVE_USER_ROLE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
