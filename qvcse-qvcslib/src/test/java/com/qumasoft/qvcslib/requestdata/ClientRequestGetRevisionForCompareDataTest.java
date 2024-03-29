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
 * Client Request Get Revision For Compare Data Test.
 * @author Jim Voris
 */
public class ClientRequestGetRevisionForCompareDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetRevisionForCompareData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetRevisionForCompareData instance = new ClientRequestGetRevisionForCompareData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestGetRevisionForCompareData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestGetRevisionForCompareData instance = new ClientRequestGetRevisionForCompareData();
        String expResult = "Branch name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestGetRevisionForCompareData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestGetRevisionForCompareData instance = new ClientRequestGetRevisionForCompareData();
        String expResult = "Appended path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestGetRevisionForCompareData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestGetRevisionForCompareData instance = new ClientRequestGetRevisionForCompareData();
        String expResult = "Short workfile name";
        instance.setShortWorkfileName(expResult);
        String result = instance.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getRevisionString method, of class ClientRequestGetRevisionForCompareData.
     */
    @Test
    public void testGetRevisionString() {
        ClientRequestGetRevisionForCompareData instance = new ClientRequestGetRevisionForCompareData();
        String expResult = "1.1";
        instance.setRevisionString(expResult);
        String result = instance.getRevisionString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getIsLogfileInfoRequired method, of class ClientRequestGetRevisionForCompareData.
     */
    @Test
    public void testGetIsLogfileInfoRequired() {
        ClientRequestGetRevisionForCompareData instance = new ClientRequestGetRevisionForCompareData();
        boolean expResult = true;
        instance.setIsLogfileInfoRequired(expResult);
        boolean result = instance.getIsLogfileInfoRequired();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetRevisionForCompareData instance = new ClientRequestGetRevisionForCompareData();
        instance.setTransactionID(0);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetRevisionForCompareData instance = new ClientRequestGetRevisionForCompareData();
        Integer transactionID = instance.getTransactionID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetRevisionForCompareData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestGetRevisionForCompareData instance = new ClientRequestGetRevisionForCompareData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_REVISION_FOR_COMPARE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
