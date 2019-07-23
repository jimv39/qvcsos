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
import java.util.Date;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Server Create Branch Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerCreateBranchDataTest {

    /**
     * Test of getUserName method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsReadOnlyBranchFlag method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetIsReadOnlyBranchFlag() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        boolean expResult = true;
        instance.setIsReadOnlyBranchFlag(expResult);
        boolean result = instance.getIsReadOnlyBranchFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsDateBasedBranchFlag method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetIsDateBasedBranchFlag() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        boolean expResult = true;
        instance.setIsDateBasedBranchFlag(expResult);
        boolean result = instance.getIsDateBasedBranchFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsFeatureBranchFlag method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetIsFeatureBranchFlag() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        boolean expResult = true;
        instance.setIsFeatureBranchFlag(expResult);
        boolean result = instance.getIsFeatureBranchFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsOpaqueBranchFlag method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetIsOpaqueBranchFlag() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        boolean expResult = true;
        instance.setIsOpaqueBranchFlag(expResult);
        boolean result = instance.getIsOpaqueBranchFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDateBasedDate method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetDateBasedDate() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        Date expResult = new Date();
        instance.setDateBasedDate(expResult);
        Date result = instance.getDateBasedDate();
        assertEquals(expResult, result);
    }

    /**
     * Test of getParentBranchName method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetParentBranchName() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        String expResult = "Parent branch name";
        instance.setParentBranchName(expResult);
        String result = instance.getParentBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_CREATE_BRANCH;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
