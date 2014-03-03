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
 * Client Request List Client Views Data Test.
 * @author Jim Voris
 */
public class ClientRequestListClientViewsDataTest {

    /**
     * Test of getServerName method, of class ClientRequestListClientViewsData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestListClientViewsData instance = new ClientRequestListClientViewsData();
        String expResult = "Server name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestListClientViewsData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestListClientViewsData instance = new ClientRequestListClientViewsData();
        String expResult = "Project name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestListClientViewsData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestListClientViewsData instance = new ClientRequestListClientViewsData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestListClientViewsData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestListClientViewsData instance = new ClientRequestListClientViewsData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.LIST_CLIENT_VIEWS;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
