/*   Copyright 2004-2014 Jim Voris
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
 * Client Request Set Is Obsolete Data Test.
 * @author Jim Voris
 */
public class ClientRequestSetIsObsoleteDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestDeleteFileData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestDeleteFileData instance = new ClientRequestDeleteFileData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestDeleteFileData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestDeleteFileData instance = new ClientRequestDeleteFileData();
        String expResult = "View Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestDeleteFileData.
     */
    @Test
    public void testGetAppendedPath() {
        System.out.println("getAppendedPath");
        ClientRequestDeleteFileData instance = new ClientRequestDeleteFileData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestDeleteFileData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestDeleteFileData instance = new ClientRequestDeleteFileData();
        String expResult = "Short workfile name";
        instance.setShortWorkfileName(expResult);
        String result = instance.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestDeleteFileData instance = new ClientRequestDeleteFileData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestDeleteFileData instance = new ClientRequestDeleteFileData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestDeleteFileData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestDeleteFileData instance = new ClientRequestDeleteFileData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SET_OBSOLETE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
