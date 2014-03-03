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

import com.qumasoft.qvcslib.Utility.OverwriteBehavior;
import com.qumasoft.qvcslib.Utility.TimestampBehavior;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Get Revision Data Test.
 * @author Jim Voris
 */
public class ClientRequestGetRevisionDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetRevisionData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetRevisionData instance = new ClientRequestGetRevisionData();
        String expResult = "Project name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestGetRevisionData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestGetRevisionData instance = new ClientRequestGetRevisionData();
        String expResult = "View name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestGetRevisionData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestGetRevisionData instance = new ClientRequestGetRevisionData();
        String expResult = "Appended path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestGetRevisionData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestGetRevisionData instance = new ClientRequestGetRevisionData();
        LogFileOperationGetRevisionCommandArgs commandArgs = new LogFileOperationGetRevisionCommandArgs();
        populateGetRevisionCommandArgs(commandArgs);
        instance.setCommandArgs(commandArgs);
        LogFileOperationGetRevisionCommandArgs result = instance.getCommandArgs();
        assertEquals(commandArgs.getFullWorkfileName(), result.getFullWorkfileName());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetRevisionData instance = new ClientRequestGetRevisionData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetRevisionData instance = new ClientRequestGetRevisionData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetRevisionData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestGetRevisionData instance = new ClientRequestGetRevisionData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_REVISION;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

    /**
     * Populate a the command args with some dummy test data.
     *
     * @param commandArgs the command args to populate.
     */
    private void populateGetRevisionCommandArgs(LogFileOperationGetRevisionCommandArgs commandArgs) {
        commandArgs.setByDateFlag(false);
        commandArgs.setByDateValue(null);
        commandArgs.setByLabelFlag(false);
        commandArgs.setFailureReason(null);
        commandArgs.setFullWorkfileName("Full workfile name");
        commandArgs.setLabel(null);
        commandArgs.setOutputFileName("output file name");
        commandArgs.setOverwriteBehavior(OverwriteBehavior.ASK_BEFORE_OVERWRITE_OF_WRITABLE_FILE);
        commandArgs.setRevisionString(QVCSConstants.QVCS_DEFAULT_REVISION);
        commandArgs.setShortWorkfileName("Short workfile name");
        commandArgs.setTimestampBehavior(TimestampBehavior.SET_TIMESTAMP_TO_NOW);
        commandArgs.setUserName("User name");
    }
}
