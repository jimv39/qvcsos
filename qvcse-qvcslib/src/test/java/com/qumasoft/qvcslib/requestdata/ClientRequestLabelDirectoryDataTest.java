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
import com.qumasoft.qvcslib.commandargs.LabelDirectoryCommandArgs;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Label Directory Data Test.
 * @author Jim Voris
 */
public class ClientRequestLabelDirectoryDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestLabelDirectoryData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestLabelDirectoryData instance = new ClientRequestLabelDirectoryData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestLabelDirectoryData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestLabelDirectoryData instance = new ClientRequestLabelDirectoryData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestLabelDirectoryData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestLabelDirectoryData instance = new ClientRequestLabelDirectoryData();
        String expResult = "Appended path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestLabelDirectoryData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestLabelDirectoryData instance = new ClientRequestLabelDirectoryData();
        LabelDirectoryCommandArgs commandArgs = new LabelDirectoryCommandArgs();
        String expResult = "New Label String";
        commandArgs.setNewLabelString(expResult);
        instance.setCommandArgs(commandArgs);
        LabelDirectoryCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getNewLabelString());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestLabelDirectoryData instance = new ClientRequestLabelDirectoryData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestLabelDirectoryData instance = new ClientRequestLabelDirectoryData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestLabelDirectoryData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestLabelDirectoryData instance = new ClientRequestLabelDirectoryData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.LABEL_DIRECTORY;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
