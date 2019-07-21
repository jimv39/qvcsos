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
import com.qumasoft.qvcslib.commandargs.LabelRevisionCommandArgs;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Label Data Test.
 * @author Jim Voris
 */
public class ClientRequestLabelDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestLabelData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestLabelData instance = new ClientRequestLabelData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestLabelData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestLabelData instance = new ClientRequestLabelData();
        String expResult = "View Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestLabelData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestLabelData instance = new ClientRequestLabelData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestLabelData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestLabelData instance = new ClientRequestLabelData();
        LabelRevisionCommandArgs commandArgs = new LabelRevisionCommandArgs();
        String expResult = "Label String";
        commandArgs.setLabelString(expResult);
        instance.setCommandArgs(commandArgs);
        LabelRevisionCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getLabelString());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestLabelData instance = new ClientRequestLabelData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestLabelData instance = new ClientRequestLabelData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestLabelData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestLabelData instance = new ClientRequestLabelData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.LABEL;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
