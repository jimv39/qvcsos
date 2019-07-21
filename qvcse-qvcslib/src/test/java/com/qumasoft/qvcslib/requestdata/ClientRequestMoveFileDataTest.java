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
 * Client Request Move File Data Test.
 * @author Jim Voris
 */
public class ClientRequestMoveFileDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestMoveFileData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestMoveFileData instance = new ClientRequestMoveFileData();
        String expResult = "Project name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestMoveFileData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestMoveFileData instance = new ClientRequestMoveFileData();
        String expResult = "View name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOriginalAppendedPath method, of class ClientRequestMoveFileData.
     */
    @Test
    public void testGetOriginalAppendedPath() {
        ClientRequestMoveFileData instance = new ClientRequestMoveFileData();
        String expResult = "Original Appended path";
        instance.setOriginalAppendedPath(expResult);
        String result = instance.getOriginalAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNewAppendedPath method, of class ClientRequestMoveFileData.
     */
    @Test
    public void testGetNewAppendedPath() {
        ClientRequestMoveFileData instance = new ClientRequestMoveFileData();
        String expResult = "New Appended path";
        instance.setNewAppendedPath(expResult);
        String result = instance.getNewAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestMoveFileData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestMoveFileData instance = new ClientRequestMoveFileData();
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
        ClientRequestMoveFileData instance = new ClientRequestMoveFileData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestMoveFileData instance = new ClientRequestMoveFileData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestMoveFileData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestMoveFileData instance = new ClientRequestMoveFileData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.MOVE_FILE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
