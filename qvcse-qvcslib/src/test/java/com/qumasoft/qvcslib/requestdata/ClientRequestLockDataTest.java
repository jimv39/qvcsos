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

import com.qumasoft.qvcslib.LogFileOperationLockRevisionCommandArgs;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.requestdata.ClientRequestLockData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Lock Data Test.
 * @author Jim Voris
 */
public class ClientRequestLockDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestLockData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestLockData instance = new ClientRequestLockData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestLockData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestLockData instance = new ClientRequestLockData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestLockData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestLockData instance = new ClientRequestLockData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestLockData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestLockData instance = new ClientRequestLockData();
        LogFileOperationLockRevisionCommandArgs commandArgs = new LogFileOperationLockRevisionCommandArgs();
        populateLockCommandArgs(commandArgs);
        instance.setCommandArgs(commandArgs);
        LogFileOperationLockRevisionCommandArgs result = instance.getCommandArgs();
        assertEquals(commandArgs.getUserName(), result.getUserName());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestLockData instance = new ClientRequestLockData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestLockData instance = new ClientRequestLockData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestLockData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestLockData instance = new ClientRequestLockData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.LOCK;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

    /**
     * Populate the lock command argument with test data.
     *
     * @param commandArgs the command arguments to populate.
     */
    private void populateLockCommandArgs(LogFileOperationLockRevisionCommandArgs commandArgs) {
        commandArgs.setCheckOutComment("Checkout comment");
        commandArgs.setFullWorkfileName("Full workfile name");
        commandArgs.setLabel("Label dude");
        commandArgs.setOutputFileName("Output filename");
        commandArgs.setRevisionString("1.1");
        commandArgs.setShortWorkfileName("Short workfile name");
        commandArgs.setUserName("User name");
    }
}
