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
 * Client Request Server Add User Role Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerAddUserRoleDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestServerAddUserRoleData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestServerAddUserRoleData instance = new ClientRequestServerAddUserRoleData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestServerAddUserRoleData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestServerAddUserRoleData instance = new ClientRequestServerAddUserRoleData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestServerAddUserRoleData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerAddUserRoleData instance = new ClientRequestServerAddUserRoleData();
        String expResult = "User name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRole method, of class ClientRequestServerAddUserRoleData.
     */
    @Test
    public void testGetRole() {
        ClientRequestServerAddUserRoleData instance = new ClientRequestServerAddUserRoleData();
        String expResult = "Role Type";
        instance.setRole(expResult);
        String result = instance.getRole();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestServerAddUserRoleData instance = new ClientRequestServerAddUserRoleData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerAddUserRoleData instance = new ClientRequestServerAddUserRoleData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerAddUserRoleData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerAddUserRoleData instance = new ClientRequestServerAddUserRoleData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.ADD_USER_ROLE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
