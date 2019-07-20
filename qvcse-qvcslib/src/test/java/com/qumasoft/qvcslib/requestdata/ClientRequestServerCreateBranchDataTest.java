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
 * Client Request Server Create View Data Test.
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
     * Test of getViewName method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsReadOnlyViewFlag method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetIsReadOnlyViewFlag() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        boolean expResult = true;
        instance.setIsReadOnlyViewFlag(expResult);
        boolean result = instance.getIsReadOnlyViewFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsDateBasedViewFlag method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetIsDateBasedViewFlag() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        boolean expResult = true;
        instance.setIsDateBasedViewFlag(expResult);
        boolean result = instance.getIsDateBasedViewFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsTranslucentBranchFlag method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetIsTranslucentBranchFlag() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        boolean expResult = true;
        instance.setIsTranslucentBranchFlag(expResult);
        boolean result = instance.getIsTranslucentBranchFlag();
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
     * Test of getDateBasedViewLabel method, of class ClientRequestServerCreateBranchData.
     */
    @Test
    public void testGetDateBasedViewBranch() {
        ClientRequestServerCreateBranchData instance = new ClientRequestServerCreateBranchData();
        String expResult = "Date based view branch";
        instance.setDateBasedViewBranch(expResult);
        String result = instance.getDateBasedViewBranch();
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
