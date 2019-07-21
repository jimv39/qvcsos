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
 * Client Request Transaction End Data Test.
 * @author Jim Voris
 */
public class ClientRequestTransactionEndDataTest {

    /**
     * Test of getTransactionID method, of class ClientRequestTransactionEndData.
     */
    @Test
    public void testGetTransactionID() {
        ClientRequestTransactionEndData instance = new ClientRequestTransactionEndData();
        Integer expResult = Integer.valueOf(100);
        instance.setTransactionID(expResult);
        Integer result = instance.getTransactionID();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestTransactionEndData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestTransactionEndData instance = new ClientRequestTransactionEndData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestTransactionEndData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestTransactionEndData instance = new ClientRequestTransactionEndData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestTransactionEndData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestTransactionEndData instance = new ClientRequestTransactionEndData();
        String expResult = null;
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestTransactionEndData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestTransactionEndData instance = new ClientRequestTransactionEndData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.END_TRANSACTION;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
