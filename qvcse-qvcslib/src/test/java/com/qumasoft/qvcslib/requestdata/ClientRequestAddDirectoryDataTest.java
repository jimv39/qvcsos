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
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Add Directory Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestAddDirectoryDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestAddDirectoryData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestAddDirectoryData instance = new ClientRequestAddDirectoryData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestAddDirectoryData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestAddDirectoryData instance = new ClientRequestAddDirectoryData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestAddDirectoryData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestAddDirectoryData instance = new ClientRequestAddDirectoryData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestAddDirectoryData instance = new ClientRequestAddDirectoryData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestAddDirectoryData instance = new ClientRequestAddDirectoryData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestAddDirectoryData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestAddDirectoryData instance = new ClientRequestAddDirectoryData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.ADD_DIRECTORY;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
