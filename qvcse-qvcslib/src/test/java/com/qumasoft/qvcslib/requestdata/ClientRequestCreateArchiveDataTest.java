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

import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestCreateArchiveData;
import java.util.Date;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client request create archive data test.
 * @author Jim Voris
 */
public class ClientRequestCreateArchiveDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestCreateArchiveData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestCreateArchiveData instance = new ClientRequestCreateArchiveData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestCreateArchiveData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestCreateArchiveData instance = new ClientRequestCreateArchiveData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestCreateArchiveData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestCreateArchiveData instance = new ClientRequestCreateArchiveData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestCreateArchiveData.
     */
    @Test
    public void testGetCommandArgs() {
        CreateArchiveCommandArgs commandArgs = new CreateArchiveCommandArgs();
        String expResult = "Archive Description";
        commandArgs.setArchiveDescription(expResult);
        Date now = new Date();
        commandArgs.setCheckInTimestamp(now);
        ClientRequestCreateArchiveData instance = new ClientRequestCreateArchiveData();
        instance.setCommandArgs(commandArgs);
        CreateArchiveCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getArchiveDescription());
    }

    /**
     * Test of getBuffer method, of class ClientRequestCreateArchiveData.
     */
    @Test
    public void testGetBuffer() {
        ClientRequestCreateArchiveData instance = new ClientRequestCreateArchiveData();
        String stringBuffer = "This is a buffer";
        byte[] expResult = stringBuffer.getBytes();
        instance.setBuffer(expResult);
        byte[] result = instance.getBuffer();
        assertEquals(expResult.length, result.length);
        for (int i = 0; i < expResult.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
    }

    /**
     * Test of getIndex method, of class ClientRequestCreateArchiveData.
     */
    @Test
    public void testGetIndex() {
        ClientRequestCreateArchiveData instance = new ClientRequestCreateArchiveData();
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
        ClientRequestCreateArchiveData instance = new ClientRequestCreateArchiveData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestCreateArchiveData instance = new ClientRequestCreateArchiveData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestCreateArchiveData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestCreateArchiveData instance = new ClientRequestCreateArchiveData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.CREATE_ARCHIVE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
