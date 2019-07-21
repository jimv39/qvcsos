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
 * Client Request Rename Directory Data Test.
 * @author Jim Voris
 */
public class ClientRequestRenameDirectoryDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestRenameDirectoryData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestRenameDirectoryData instance = new ClientRequestRenameDirectoryData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestRenameDirectoryData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestRenameDirectoryData instance = new ClientRequestRenameDirectoryData();
        String expResult = "View Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOriginalAppendedPath method, of class ClientRequestRenameDirectoryData.
     */
    @Test
    public void testGetOriginalAppendedPath() {
        ClientRequestRenameDirectoryData instance = new ClientRequestRenameDirectoryData();
        String expResult = "Original appended path";
        instance.setOriginalAppendedPath(expResult);
        String result = instance.getOriginalAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNewAppendedPath method, of class ClientRequestRenameDirectoryData.
     */
    @Test
    public void testGetNewAppendedPath() {
        ClientRequestRenameDirectoryData instance = new ClientRequestRenameDirectoryData();
        String expResult = "New appended path";
        instance.setNewAppendedPath(expResult);
        String result = instance.getNewAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestRenameDirectoryData instance = new ClientRequestRenameDirectoryData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestRenameDirectoryData instance = new ClientRequestRenameDirectoryData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestRenameDirectoryData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestRenameDirectoryData instance = new ClientRequestRenameDirectoryData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.RENAME_DIRECTORY;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
