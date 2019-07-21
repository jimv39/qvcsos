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
 * Client Request Rename Data Test.
 * @author Jim Voris
 */
public class ClientRequestRenameDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestRenameData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestRenameData instance = new ClientRequestRenameData();
        String expResult = "Set project name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestRenameData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestRenameData instance = new ClientRequestRenameData();
        String expResult = "Set View name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestRenameData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestRenameData instance = new ClientRequestRenameData();
        String expResult = "Set appended path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestRenameData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestRenameData instance = new ClientRequestRenameData();
        String expResult = "Set user name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of setOriginalShortWorkfileName method, of class ClientRequestRenameData.
     */
    @Test
    public void testGetOriginalShortWorkfileName() {
        ClientRequestRenameData instance = new ClientRequestRenameData();
        String expResult = "Set original short workfile name";
        instance.setOriginalShortWorkfileName(expResult);
        String result = instance.getOriginalShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNewShortWorkfileName method, of class ClientRequestRenameData.
     */
    @Test
    public void testGetNewShortWorkfileName() {
        ClientRequestRenameData instance = new ClientRequestRenameData();
        String expResult = "Set new short workfile name";
        instance.setNewShortWorkfileName(expResult);
        String result = instance.getNewShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestRenameData instance = new ClientRequestRenameData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestRenameData instance = new ClientRequestRenameData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestRenameData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestRenameData instance = new ClientRequestRenameData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.RENAME_FILE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
