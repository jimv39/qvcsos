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
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client request checkin data test.
 * @author Jim Voris
 */
public class ClientRequestCheckInDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestCheckInData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestCheckInData instance = new ClientRequestCheckInData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestCheckInData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestCheckInData instance = new ClientRequestCheckInData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestCheckInData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestCheckInData instance = new ClientRequestCheckInData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBuffer method, of class ClientRequestCheckInData.
     */
    @Test
    public void testGetBuffer() {
        ClientRequestCheckInData instance = new ClientRequestCheckInData();
        String buffer = "This is a test buffer";
        byte[] expResult = buffer.getBytes();
        instance.setBuffer(expResult);
        byte[] result = instance.getBuffer();
        assertEquals(result.length, expResult.length);
        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestCheckInData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestCheckInData instance = new ClientRequestCheckInData();
        CheckInCommandArgs logFileOperationCheckInCommandArgs = new CheckInCommandArgs();
        String expResult = "Project Name";
        logFileOperationCheckInCommandArgs.setProjectName(expResult);
        instance.setCommandArgs(logFileOperationCheckInCommandArgs);
        CheckInCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getProjectName());
    }

    /**
     * Test of getIndex method, of class ClientRequestCheckInData.
     */
    @Test
    public void testGetIndex() {
        ClientRequestCheckInData instance = new ClientRequestCheckInData();
        int expResult = 10;
        instance.setIndex(expResult);
        int result = instance.getIndex();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetLogfileInfoData instance = new ClientRequestGetLogfileInfoData();
        instance.setRevisionString("This should fail.");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetLogfileInfoData instance = new ClientRequestGetLogfileInfoData();
        String revisionString = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestCheckInData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestCheckInData instance = new ClientRequestCheckInData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.CHECK_IN;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
