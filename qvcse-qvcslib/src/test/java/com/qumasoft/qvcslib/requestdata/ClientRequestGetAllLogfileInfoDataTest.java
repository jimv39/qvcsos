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
 * Client Request Get All LogfileInfo Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestGetAllLogfileInfoDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetAllLogfileInfoData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetAllLogfileInfoData instance = new ClientRequestGetAllLogfileInfoData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestGetAllLogfileInfoData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestGetAllLogfileInfoData instance = new ClientRequestGetAllLogfileInfoData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestGetAllLogfileInfoData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestGetAllLogfileInfoData instance = new ClientRequestGetAllLogfileInfoData();
        String expResult = "appendedPath/subDirectory";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestGetAllLogfileInfoData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestGetAllLogfileInfoData instance = new ClientRequestGetAllLogfileInfoData();
        String expResult = "aShortWorkfileName";
        instance.setShortWorkfileName(expResult);
        String result = instance.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFileID method, of class ClientRequestGetAllLogfileInfoData.
     */
    @Test
    public void testGetFileID() {
        ClientRequestGetAllLogfileInfoData instance = new ClientRequestGetAllLogfileInfoData();
        Integer expResult = 1002;
        instance.setFileID(expResult);
        Integer result = instance.getFileID();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetAllLogfileInfoData instance = new ClientRequestGetAllLogfileInfoData();
        instance.setRevisionString("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetAllLogfileInfoData instance = new ClientRequestGetAllLogfileInfoData();
        String shortName = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetAllLogfileInfoData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestGetAllLogfileInfoData instance = new ClientRequestGetAllLogfileInfoData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_ALL_LOGFILE_INFO;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
