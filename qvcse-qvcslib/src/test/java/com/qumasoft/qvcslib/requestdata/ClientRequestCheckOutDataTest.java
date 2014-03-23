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

import com.qumasoft.qvcslib.LogFileOperationCheckOutCommandArgs;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckOutData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client request checkout data test.
 *
 * @author Jim Voris
 */
public class ClientRequestCheckOutDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestCheckOutData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestCheckOutData instance = new ClientRequestCheckOutData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestCheckOutData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestCheckOutData instance = new ClientRequestCheckOutData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestCheckOutData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestCheckOutData instance = new ClientRequestCheckOutData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestCheckOutData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestCheckOutData instance = new ClientRequestCheckOutData();
        LogFileOperationCheckOutCommandArgs commandArgs = new LogFileOperationCheckOutCommandArgs();
        String expResult = "FullWorkfileName";
        commandArgs.setFullWorkfileName(expResult);
        instance.setCommandArgs(commandArgs);
        LogFileOperationCheckOutCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getFullWorkfileName());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestCheckOutData instance = new ClientRequestCheckOutData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestCheckOutData instance = new ClientRequestCheckOutData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestCheckOutData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestCheckOutData instance = new ClientRequestCheckOutData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.CHECK_OUT;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
