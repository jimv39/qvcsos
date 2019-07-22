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
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request UnLabel Data Test.
 * @author Jim Voris
 */
public class ClientRequestUnLabelDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestUnLabelData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestUnLabelData instance = new ClientRequestUnLabelData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestUnLabelData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestUnLabelData instance = new ClientRequestUnLabelData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestUnLabelData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestUnLabelData instance = new ClientRequestUnLabelData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestUnLabelData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestUnLabelData instance = new ClientRequestUnLabelData();
        UnLabelRevisionCommandArgs commandArgs = new UnLabelRevisionCommandArgs();
        String expResult = "User Name";
        commandArgs.setUserName(expResult);
        instance.setCommandArgs(commandArgs);
        UnLabelRevisionCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getUserName());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestUnLabelData instance = new ClientRequestUnLabelData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestUnLabelData instance = new ClientRequestUnLabelData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestUnLabelData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestUnLabelData instance = new ClientRequestUnLabelData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.REMOVE_LABEL;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
