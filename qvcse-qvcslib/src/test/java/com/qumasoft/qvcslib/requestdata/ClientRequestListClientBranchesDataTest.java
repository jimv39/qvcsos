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
 * Client Request List Client Branches Data Test.
 * @author Jim Voris
 */
public class ClientRequestListClientBranchesDataTest {

    /**
     * Test of getServerName method, of class ClientRequestListClientBranchesData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestListClientBranchesData instance = new ClientRequestListClientBranchesData();
        String expResult = "Server name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestListClientBranchesData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestListClientBranchesData instance = new ClientRequestListClientBranchesData();
        String expResult = "Project name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestListClientBranchesData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetBranchName() {
        ClientRequestListClientBranchesData instance = new ClientRequestListClientBranchesData();
        String expResult = null;
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestListClientBranchesData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestListClientBranchesData instance = new ClientRequestListClientBranchesData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.LIST_CLIENT_BRANCHES;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
