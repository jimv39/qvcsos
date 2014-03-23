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
import com.qumasoft.qvcslib.requestdata.ClientRequestSetModuleDescriptionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Set Module Description Data Test.
 * @author Jim Voris
 */
public class ClientRequestSetModuleDescriptionDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestSetModuleDescriptionData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestSetModuleDescriptionData instance = new ClientRequestSetModuleDescriptionData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestSetModuleDescriptionData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestSetModuleDescriptionData instance = new ClientRequestSetModuleDescriptionData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestSetModuleDescriptionData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestSetModuleDescriptionData instance = new ClientRequestSetModuleDescriptionData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestSetModuleDescriptionData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestSetModuleDescriptionData instance = new ClientRequestSetModuleDescriptionData();
        String expResult = "Short workfile Name";
        instance.setShortWorkfileName(expResult);
        String result = instance.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getModuleDescription method, of class ClientRequestSetModuleDescriptionData.
     */
    @Test
    public void testGetModuleDescription() {
        ClientRequestSetModuleDescriptionData instance = new ClientRequestSetModuleDescriptionData();
        String expResult = "Module Description";
        instance.setModuleDescription(expResult);
        String result = instance.getModuleDescription();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestSetModuleDescriptionData instance = new ClientRequestSetModuleDescriptionData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestSetModuleDescriptionData instance = new ClientRequestSetModuleDescriptionData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestSetModuleDescriptionData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestSetModuleDescriptionData instance = new ClientRequestSetModuleDescriptionData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SET_MODULE_DESCRIPTION;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
