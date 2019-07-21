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
 * Client Request Register Client Listener Data Test.
 * @author Jim Voris
 */
public class ClientRequestRegisterClientListenerDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestRegisterClientListenerData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestRegisterClientListenerData instance = new ClientRequestRegisterClientListenerData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestRegisterClientListenerData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestRegisterClientListenerData instance = new ClientRequestRegisterClientListenerData();
        String expResult = "View Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestRegisterClientListenerData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestRegisterClientListenerData instance = new ClientRequestRegisterClientListenerData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestRegisterClientListenerData instance = new ClientRequestRegisterClientListenerData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestRegisterClientListenerData instance = new ClientRequestRegisterClientListenerData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestRegisterClientListenerData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestRegisterClientListenerData instance = new ClientRequestRegisterClientListenerData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.REGISTER_CLIENT_LISTENER;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
