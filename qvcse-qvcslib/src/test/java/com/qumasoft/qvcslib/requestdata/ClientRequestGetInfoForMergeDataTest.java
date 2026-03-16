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
 * Client Request Get Info For Merge Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestGetInfoForMergeDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetInfoForMergeData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetInfoForMergeData instance = new ClientRequestGetInfoForMergeData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestGetInfoForMergeData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestGetInfoForMergeData instance = new ClientRequestGetInfoForMergeData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestGetInfoForMergeData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestGetInfoForMergeData instance = new ClientRequestGetInfoForMergeData();
        String expResult = "appendedPath/subDirectory";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFileID method, of class ClientRequestGetInfoForMergeData.
     */
    @Test
    public void testGetFileID() {
        ClientRequestGetInfoForMergeData instance = new ClientRequestGetInfoForMergeData();
        Integer expResult = 1003;
        instance.setFileID(expResult);
        Integer result = instance.getFileID();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetInfoForMergeData instance = new ClientRequestGetInfoForMergeData();
        instance.setRevisionString("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetInfoForMergeData instance = new ClientRequestGetInfoForMergeData();
        String shortName = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetInfoForMergeData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestGetInfoForMergeData instance = new ClientRequestGetInfoForMergeData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_INFO_FOR_MERGE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
