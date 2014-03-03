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
 * Client Request Server Delete View Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerDeleteViewDataTest {

    /**
     * Test of getUserName method, of class ClientRequestServerDeleteViewData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerDeleteViewData instance = new ClientRequestServerDeleteViewData();
        String expResult = "User name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestServerDeleteViewData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerDeleteViewData instance = new ClientRequestServerDeleteViewData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerDeleteViewData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestServerDeleteViewData instance = new ClientRequestServerDeleteViewData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerDeleteViewData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestServerDeleteViewData instance = new ClientRequestServerDeleteViewData();
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
        ClientRequestServerDeleteViewData instance = new ClientRequestServerDeleteViewData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerDeleteViewData instance = new ClientRequestServerDeleteViewData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerDeleteViewData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerDeleteViewData instance = new ClientRequestServerDeleteViewData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_DELETE_VIEW;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
