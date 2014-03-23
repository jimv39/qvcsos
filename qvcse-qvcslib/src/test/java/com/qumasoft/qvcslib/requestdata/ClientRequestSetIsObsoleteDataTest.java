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
import com.qumasoft.qvcslib.requestdata.ClientRequestSetIsObsoleteData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Set Is Obsolete Data Test.
 * @author Jim Voris
 */
public class ClientRequestSetIsObsoleteDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestSetIsObsoleteData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestSetIsObsoleteData instance = new ClientRequestSetIsObsoleteData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestSetIsObsoleteData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestSetIsObsoleteData instance = new ClientRequestSetIsObsoleteData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestSetIsObsoleteData.
     */
    @Test
    public void testGetAppendedPath() {
        System.out.println("getAppendedPath");
        ClientRequestSetIsObsoleteData instance = new ClientRequestSetIsObsoleteData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestSetIsObsoleteData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestSetIsObsoleteData instance = new ClientRequestSetIsObsoleteData();
        String expResult = "Short workfile name";
        instance.setShortWorkfileName(expResult);
        String result = instance.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFlag method, of class ClientRequestSetIsObsoleteData.
     */
    @Test
    public void testGetFlag() {
        ClientRequestSetIsObsoleteData instance = new ClientRequestSetIsObsoleteData();
        boolean expResult = false;
        instance.setFlag(expResult);
        boolean result = instance.getFlag();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestSetIsObsoleteData instance = new ClientRequestSetIsObsoleteData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestSetIsObsoleteData instance = new ClientRequestSetIsObsoleteData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestSetIsObsoleteData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestSetIsObsoleteData instance = new ClientRequestSetIsObsoleteData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SET_OBSOLETE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
