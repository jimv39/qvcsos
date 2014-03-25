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

import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.requestdata.ClientRequestSetRevisionDescriptionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Set Revision Description Data Test.
 * @author Jim Voris
 */
public class ClientRequestSetRevisionDescriptionDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestSetRevisionDescriptionData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestSetRevisionDescriptionData instance = new ClientRequestSetRevisionDescriptionData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestSetRevisionDescriptionData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestSetRevisionDescriptionData instance = new ClientRequestSetRevisionDescriptionData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestSetRevisionDescriptionData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestSetRevisionDescriptionData instance = new ClientRequestSetRevisionDescriptionData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestSetRevisionDescriptionData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestSetRevisionDescriptionData instance = new ClientRequestSetRevisionDescriptionData();
        SetRevisionDescriptionCommandArgs logFileOperationSetRevisionDescriptionCommandArgs = new SetRevisionDescriptionCommandArgs();
        String expResult = "Short workfile name";
        logFileOperationSetRevisionDescriptionCommandArgs.setShortWorkfileName(expResult);
        instance.setCommandArgs(logFileOperationSetRevisionDescriptionCommandArgs);
        SetRevisionDescriptionCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getShortWorkfileName());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestSetRevisionDescriptionData instance = new ClientRequestSetRevisionDescriptionData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestSetRevisionDescriptionData instance = new ClientRequestSetRevisionDescriptionData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestSetRevisionDescriptionData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestSetRevisionDescriptionData instance = new ClientRequestSetRevisionDescriptionData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SET_REVISION_DESCRIPTION;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
