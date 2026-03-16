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
 * Client Request Update Tag Commit Id Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestUpdateTagCommitIdDataTest {

    /**
     * Test of getServerName method, of class ClientRequestApplyTagData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        String expResult = "Server Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestUpdateTagCommitIdData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestUpdateTagCommitIdData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestUpdateTagCommitIdData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTag method, of class ClientRequestUpdateTagCommitIdData.
     */
    @Test
    public void testGetTag() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        String expResult = "Tag";
        instance.setTag(expResult);
        String result = instance.getTag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOldCommitId method, of class ClientRequestUpdateTagCommitIdData.
     */
    @Test
    public void testGetOldCommitId() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        Integer expResult = 2000;
        instance.setOldCommitId(expResult);
        Integer result = instance.getOldCommitId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNewCommitId method, of class ClientRequestUpdateTagCommitIdData.
     */
    @Test
    public void testGetNewCommitId() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        Integer expResult = 3000;
        instance.setNewCommitId(expResult);
        Integer result = instance.getNewCommitId();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestUpdateTagCommitIdData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestUpdateTagCommitIdData instance = new ClientRequestUpdateTagCommitIdData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.UPDATE_TAG_COMMIT_ID;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
