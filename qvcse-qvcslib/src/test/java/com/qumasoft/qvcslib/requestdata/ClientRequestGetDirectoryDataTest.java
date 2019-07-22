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
import com.qumasoft.qvcslib.commandargs.GetDirectoryCommandArgs;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Get Directory Data Test.
 * @author Jim Voris
 */
public class ClientRequestGetDirectoryDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetDirectoryData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetDirectoryData instance = new ClientRequestGetDirectoryData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestGetDirectoryData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestGetDirectoryData instance = new ClientRequestGetDirectoryData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestGetDirectoryData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestGetDirectoryData instance = new ClientRequestGetDirectoryData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTransactionID method, of class ClientRequestGetDirectoryData.
     */
    @Test
    public void testGetTransactionID() {
        ClientRequestGetDirectoryData instance = new ClientRequestGetDirectoryData();
        int expResult = 10;
        instance.setTransactionID(expResult);
        int result = instance.getTransactionID();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestGetDirectoryData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestGetDirectoryData instance = new ClientRequestGetDirectoryData();
        GetDirectoryCommandArgs logFileOperationGetDirectoryCommandArgs = new GetDirectoryCommandArgs();
        String expResult = "User name";
        logFileOperationGetDirectoryCommandArgs.setUserName(expResult);
        instance.setCommandArgs(logFileOperationGetDirectoryCommandArgs);
        GetDirectoryCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getUserName());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetDirectoryData instance = new ClientRequestGetDirectoryData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetDirectoryData instance = new ClientRequestGetDirectoryData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetDirectoryData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestGetDirectoryData instance = new ClientRequestGetDirectoryData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_DIRECTORY;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
