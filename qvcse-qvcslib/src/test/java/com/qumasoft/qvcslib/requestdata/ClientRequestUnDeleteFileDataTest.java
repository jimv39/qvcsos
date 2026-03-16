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
 * Client Request UnDelete File Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestUnDeleteFileDataTest {

    /**
     * Test of getServerName method, of class ClientRequestUnDeleteFileData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestUnDeleteFileData instance = new ClientRequestUnDeleteFileData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestUnDeleteFileData instance = new ClientRequestUnDeleteFileData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestUnDeleteFileData instance = new ClientRequestUnDeleteFileData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestUnDeleteFileData instance = new ClientRequestUnDeleteFileData();
        String expResult = "ShortWorkfileName";
        instance.setShortWorkfileName(expResult);
        String result = instance.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFileID method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetFileID() {
        ClientRequestUnDeleteFileData instance = new ClientRequestUnDeleteFileData();
        Integer expResult = 1001;
        instance.setFileID(expResult);
        Integer result = instance.getFileID();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestUnDeleteFileData instance = new ClientRequestUnDeleteFileData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestUnDeleteFileData instance = new ClientRequestUnDeleteFileData();
        instance.setAppendedPath("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestUnDeleteFileData instance = new ClientRequestUnDeleteFileData();
        String shortName = instance.getAppendedPath();
    }

    /**
     * Test of getOperationType method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestUnDeleteFileData instance = new ClientRequestUnDeleteFileData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.UNDELETE_FILE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
