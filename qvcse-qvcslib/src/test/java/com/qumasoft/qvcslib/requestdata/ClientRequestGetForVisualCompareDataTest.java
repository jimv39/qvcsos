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
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Get For Visual Compare Data Test.
 * @author Jim Voris
 */
public class ClientRequestGetForVisualCompareDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetForVisualCompareData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetForVisualCompareData instance = new ClientRequestGetForVisualCompareData();
        String expResult = "Project name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestGetForVisualCompareData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestGetForVisualCompareData instance = new ClientRequestGetForVisualCompareData();
        String expResult = "Branch name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestGetForVisualCompareData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestGetForVisualCompareData instance = new ClientRequestGetForVisualCompareData();
        String expResult = "Appended path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestGetForVisualCompareData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestGetForVisualCompareData instance = new ClientRequestGetForVisualCompareData();
        GetRevisionCommandArgs commandArgs = new GetRevisionCommandArgs();
        String expResult = "User name";
        commandArgs.setUserName(expResult);
        instance.setCommandArgs(commandArgs);
        GetRevisionCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getUserName());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetForVisualCompareData instance = new ClientRequestGetForVisualCompareData();
        instance.setRevisionString("this should fail");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetForVisualCompareData instance = new ClientRequestGetForVisualCompareData();
        String revisionString = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetForVisualCompareData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestGetForVisualCompareData instance = new ClientRequestGetForVisualCompareData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_FOR_VISUAL_COMPARE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
