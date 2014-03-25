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

import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestBreakLockData;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Break Lock Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestBreakLockDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestBreakLockData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestBreakLockData instance = new ClientRequestBreakLockData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestBreakLockData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestBreakLockData instance = new ClientRequestBreakLockData();
        String expResult = "View Name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestBreakLockData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestBreakLockData instance = new ClientRequestBreakLockData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandArgs method, of class ClientRequestBreakLockData.
     */
    @Test
    public void testGetCommandArgs() {
        ClientRequestBreakLockData instance = new ClientRequestBreakLockData();
        UnlockRevisionCommandArgs logFileOperationUnlockRevisionCommandArgs = new UnlockRevisionCommandArgs();
        String expResult = "ShortWorkfileName";
        logFileOperationUnlockRevisionCommandArgs.setShortWorkfileName(expResult);
        instance.setCommandArgs(logFileOperationUnlockRevisionCommandArgs);
        UnlockRevisionCommandArgs result = instance.getCommandArgs();
        assertEquals(expResult, result.getShortWorkfileName());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestBreakLockData instance = new ClientRequestBreakLockData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestBreakLockData instance = new ClientRequestBreakLockData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestBreakLockData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestBreakLockData instance = new ClientRequestBreakLockData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.BREAK_LOCK;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
