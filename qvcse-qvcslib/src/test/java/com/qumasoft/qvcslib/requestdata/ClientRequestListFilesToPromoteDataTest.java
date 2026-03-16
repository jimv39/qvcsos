/*
 * Copyright 2026 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.qvcslib.requestdata;

import com.qumasoft.qvcslib.QVCSRuntimeException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request List Files To Promote Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestListFilesToPromoteDataTest {

    /**
     * Test of getUserName method, of class ClientRequestListFilesToPromoteData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestListFilesToPromoteData instance = new ClientRequestListFilesToPromoteData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestListFilesToPromoteData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestListFilesToPromoteData instance = new ClientRequestListFilesToPromoteData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestListFilesToPromoteData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestListFilesToPromoteData instance = new ClientRequestListFilesToPromoteData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPromoteToBranchName method, of class ClientRequestListFilesToPromoteData.
     */
    @Test
    public void testGetPromoteToBranchName() {
        ClientRequestListFilesToPromoteData instance = new ClientRequestListFilesToPromoteData();
        String expResult = "Promote To Branch Name";
        instance.setPromoteToBranchName(expResult);
        String result = instance.getPromoteToBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestListFilesToPromoteData instance = new ClientRequestListFilesToPromoteData();
        instance.setRevisionString("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestListFilesToPromoteData instance = new ClientRequestListFilesToPromoteData();
        String shortName = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestListFilesToPromoteData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestListFilesToPromoteData instance = new ClientRequestListFilesToPromoteData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.LIST_FILES_TO_PROMOTE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
