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
import com.qumasoft.qvcslib.requestdata.ClientRequestUpdateClientData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Update Client Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestUpdateClientDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestUpdateClientData.
     */
    @Test(expected = QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestUpdateClientData instance = new ClientRequestUpdateClientData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestUpdateClientData.
     */
    @Test(expected = QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestUpdateClientData instance = new ClientRequestUpdateClientData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getClientVersionString method, of class ClientRequestUpdateClientData.
     */
    @Test
    public void testGetClientVersionString() {
        ClientRequestUpdateClientData instance = new ClientRequestUpdateClientData();
        String expResult = "Client version string";
        instance.setClientVersionString(expResult);
        String result = instance.getClientVersionString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRequestedFileName method, of class ClientRequestUpdateClientData.
     */
    @Test
    public void testGetRequestedFileName() {
        ClientRequestUpdateClientData instance = new ClientRequestUpdateClientData();
        String expResult = "Requested Filename";
        instance.setRequestedFileName(expResult);
        String result = instance.getRequestedFileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRestartFlag method, of class ClientRequestUpdateClientData.
     */
    @Test
    public void testGetRestartFlag() {
        ClientRequestUpdateClientData instance = new ClientRequestUpdateClientData();
        boolean expResult = true;
        instance.setRestartFlag(expResult);
        boolean result = instance.getRestartFlag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestUpdateClientData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestUpdateClientData instance = new ClientRequestUpdateClientData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.UPDATE_CLIENT_JAR;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
