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
 * Client Request Apply Tag Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestApplyTagDataTest {

    /**
     * Test of getServerName method, of class ClientRequestApplyTagData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        String expResult = "Server Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestApplyTagData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestApplyTagData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestApplyTagData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTag method, of class ClientRequestApplyTagData.
     */
    @Test
    public void testGetTag() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        String expResult = "Tag";
        instance.setTag(expResult);
        String result = instance.getTag();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDescription method, of class ClientRequestApplyTagData.
     */
    @Test
    public void testGetDescription() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        String expResult = "A cool tag";
        instance.setDescription(expResult);
        String result = instance.getDescription();
        assertEquals(expResult, result);
    }

    /**
     * Test of getMoveableTagFlag method, of class ClientRequestApplyTagData.
     */
    @Test
    public void testGetMoveableTagFlag() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        Boolean expResult = Boolean.TRUE;
        instance.setMoveableTagFlag(Boolean.TRUE);
        Boolean result = instance.getMoveableTagFlag();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestApplyTagData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestApplyTagData instance = new ClientRequestApplyTagData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.APPLY_TAG;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
