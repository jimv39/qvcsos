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
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateViewData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Server Create View Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerCreateViewDataTest {

    /**
     * Test of getUserName method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsReadOnlyViewFlag method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetIsReadOnlyViewFlag() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        boolean expResult = true;
        instance.setIsReadOnlyViewFlag(expResult);
        boolean result = instance.getIsReadOnlyViewFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsDateBasedViewFlag method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetIsDateBasedViewFlag() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        boolean expResult = true;
        instance.setIsDateBasedViewFlag(expResult);
        boolean result = instance.getIsDateBasedViewFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsTranslucentBranchFlag method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetIsTranslucentBranchFlag() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        boolean expResult = true;
        instance.setIsTranslucentBranchFlag(expResult);
        boolean result = instance.getIsTranslucentBranchFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsOpaqueBranchFlag method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetIsOpaqueBranchFlag() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        boolean expResult = true;
        instance.setIsOpaqueBranchFlag(expResult);
        boolean result = instance.getIsOpaqueBranchFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDateBasedDate method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetDateBasedDate() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        Date expResult = new Date();
        instance.setDateBasedDate(expResult);
        Date result = instance.getDateBasedDate();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDateBasedViewLabel method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetDateBasedViewBranch() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        String expResult = "Date based view branch";
        instance.setDateBasedViewBranch(expResult);
        String result = instance.getDateBasedViewBranch();
        assertEquals(expResult, result);
    }

    /**
     * Test of getParentBranchName method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetParentBranchName() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
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
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerCreateViewData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerCreateViewData instance = new ClientRequestServerCreateViewData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_CREATE_VIEW;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
