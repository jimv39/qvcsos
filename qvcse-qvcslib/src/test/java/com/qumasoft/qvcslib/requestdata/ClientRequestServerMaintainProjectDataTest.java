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
 * Client Request Server Maintain Project Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerMaintainProjectDataTest {

    /**
     * Test of getUserName method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPassword method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetPassword() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        String password = "PassWord";
        byte[] expResult = password.getBytes();
        instance.setPassword(expResult);
        byte[] result = instance.getPassword();
        assertEquals(result.length, expResult.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
    }

    /**
     * Test of getServerName method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCreateReferenceCopyFlag method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetCreateReferenceCopyFlag() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        boolean expResult = true;
        instance.setCreateReferenceCopyFlag(expResult);
        boolean result = instance.getCreateReferenceCopyFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCreateOrDeleteCurrentReferenceFilesFlag method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetCreateOrDeleteCurrentReferenceFilesFlag() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        boolean expResult = true;
        instance.setCreateOrDeleteCurrentReferenceFilesFlag(expResult);
        boolean result = instance.getCreateOrDeleteCurrentReferenceFilesFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIgnoreCaseFlag method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetIgnoreCaseFlag() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        boolean expResult = true;
        instance.setIgnoreCaseFlag(expResult);
        boolean result = instance.getIgnoreCaseFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDefineAlternateReferenceLocationFlag method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetDefineAlternateReferenceLocationFlag() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        boolean expResult = true;
        instance.setDefineAlternateReferenceLocationFlag(expResult);
        boolean result = instance.getDefineAlternateReferenceLocationFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAlternateReferenceLocation method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetAlternateReferenceLocation() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        String expResult = "Alternate reference file location";
        instance.setAlternateReferenceLocation(expResult);
        String result = instance.getAlternateReferenceLocation();
        assertEquals(expResult, result);
        expResult = "";
        instance.setAlternateReferenceLocation(null);
        result = instance.getAlternateReferenceLocation();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerMaintainProjectData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerMaintainProjectData instance = new ClientRequestServerMaintainProjectData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_MAINTAIN_PROJECT;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
