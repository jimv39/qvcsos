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

import com.qumasoft.qvcslib.commandargs.UnLabelDirectoryCommandArgs;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnLabelDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Unlabel Directory Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestUnLabelDirectoryDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestUnLabelDirectoryData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestUnLabelDirectoryData instance = new ClientRequestUnLabelDirectoryData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestUnLabelDirectoryData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestUnLabelDirectoryData instance = new ClientRequestUnLabelDirectoryData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestUnLabelDirectoryData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestUnLabelDirectoryData instance = new ClientRequestUnLabelDirectoryData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestUnLabelDirectoryData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestUnLabelDirectoryData instance = new ClientRequestUnLabelDirectoryData();
        UnLabelDirectoryCommandArgs commandArgs = new UnLabelDirectoryCommandArgs();
        String expResult = "User name";
        commandArgs.setUserName(expResult);
        instance.setCommandArgs(commandArgs);
        UnLabelDirectoryCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getUserName());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestUnLabelDirectoryData instance = new ClientRequestUnLabelDirectoryData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestUnLabelDirectoryData instance = new ClientRequestUnLabelDirectoryData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestUnLabelDirectoryData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestUnLabelDirectoryData instance = new ClientRequestUnLabelDirectoryData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.REMOVE_LABEL_DIRECTORY;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
