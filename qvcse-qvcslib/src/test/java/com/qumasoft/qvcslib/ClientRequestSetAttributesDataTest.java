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
 * Client Request Set Attributes Data Test.
 * @author Jim Voris
 */
public class ClientRequestSetAttributesDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestSetAttributesData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestSetAttributesData instance = new ClientRequestSetAttributesData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestSetAttributesData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestSetAttributesData instance = new ClientRequestSetAttributesData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestSetAttributesData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestSetAttributesData instance = new ClientRequestSetAttributesData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestSetAttributesData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestSetAttributesData instance = new ClientRequestSetAttributesData();
        String expResult = "Short Workfilename";
        instance.setShortWorkfileName(expResult);
        String result = instance.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAttributes method, of class ClientRequestSetAttributesData.
     */
    @Test
    public void testGetAttributes() {
        ClientRequestSetAttributesData instance = new ClientRequestSetAttributesData();
        ArchiveAttributes expResult = new ArchiveAttributes();
        instance.setAttributes(expResult);
        ArchiveAttributes result = instance.getAttributes();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestSetAttributesData instance = new ClientRequestSetAttributesData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestSetAttributesData instance = new ClientRequestSetAttributesData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestSetAttributesData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestSetAttributesData instance = new ClientRequestSetAttributesData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SET_ATTRIBUTES;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
