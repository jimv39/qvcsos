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
 * Client Request Server Create Project Data Test.
 * @author Jim Voris
 */
public class ClientRequestServerCreateProjectDataTest {

    /**
     * Test of getUserName method, of class ClientRequestServerCreateProjectData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPassword method, of class ClientRequestServerCreateProjectData.
     */
    @Test
    public void testGetPassword() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        String password = "password";
        byte[] expResult = password.getBytes();
        instance.setPassword(expResult);
        byte[] result = instance.getPassword();
        assertEquals(expResult.length, result.length);
        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
    }

    /**
     * Test of getServerName method, of class ClientRequestServerCreateProjectData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestServerCreateProjectData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestServerCreateProjectData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNewProjectName method, of class ClientRequestServerCreateProjectData.
     */
    @Test
    public void testGetNewProjectName() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        String expResult = "New Project Name";
        instance.setNewProjectName(expResult);
        String result = instance.getNewProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCreateReferenceCopyFlag method, of class ClientRequestServerCreateProjectData.
     */
    @Test
    public void testGetCreateReferenceCopyFlag() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        boolean expResult = true;
        instance.setCreateReferenceCopyFlag(expResult);
        boolean result = instance.getCreateReferenceCopyFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIgnoreCaseFlag method, of class ClientRequestServerCreateProjectData.
     */
    @Test
    public void testGetIgnoreCaseFlag() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        boolean expResult = true;
        instance.setIgnoreCaseFlag(expResult);
        boolean result = instance.getIgnoreCaseFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDefineAlternateReferenceLocationFlag method, of class ClientRequestServerCreateProjectData.
     */
    @Test
    public void testGetDefineAlternateReferenceLocationFlag() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        boolean expResult = true;
        instance.setDefineAlternateReferenceLocationFlag(expResult);
        boolean result = instance.getDefineAlternateReferenceLocationFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAlternateReferenceLocation method, of class ClientRequestServerCreateProjectData.
     */
    @Test
    public void testGetAlternateReferenceLocation() {
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        String expResult = "Alternate Reference Location";
        instance.setAlternateReferenceLocation(expResult);
        String result = instance.getAlternateReferenceLocation();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerCreateProjectData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestServerCreateProjectData instance = new ClientRequestServerCreateProjectData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_CREATE_PROJECT;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
