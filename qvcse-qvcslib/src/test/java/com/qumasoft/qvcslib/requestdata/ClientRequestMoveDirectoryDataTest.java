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
 * Client request move directory data test.
 * @author Jim Voris
 */
public class ClientRequestMoveDirectoryDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestMoveDirectoryData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestMoveDirectoryData instance = new ClientRequestMoveDirectoryData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestMoveDirectoryData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestMoveDirectoryData instance = new ClientRequestMoveDirectoryData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOriginalAppendedPath method, of class ClientRequestMoveDirectoryData.
     */
    @Test
    public void testGetOriginalAppendedPath() {
        ClientRequestMoveDirectoryData instance = new ClientRequestMoveDirectoryData();
        String expResult = "Original Appended Path";
        instance.setOriginalAppendedPath(expResult);
        String result = instance.getOriginalAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNewAppendedPath method, of class ClientRequestMoveDirectoryData.
     */
    @Test
    public void testGetNewAppendedPath() {
        ClientRequestMoveDirectoryData instance = new ClientRequestMoveDirectoryData();
        String expResult = "New Appended Path";
        instance.setNewAppendedPath(expResult);
        String result = instance.getNewAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestMoveDirectoryData instance = new ClientRequestMoveDirectoryData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestMoveDirectoryData instance = new ClientRequestMoveDirectoryData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestMoveDirectoryData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestMoveDirectoryData instance = new ClientRequestMoveDirectoryData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.MOVE_DIRECTORY;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
