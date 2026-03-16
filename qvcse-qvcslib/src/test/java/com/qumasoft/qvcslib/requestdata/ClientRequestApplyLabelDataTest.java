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
 * Client Request Apply Label Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestApplyLabelDataTest {

    /**
     * Test of getUserName method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestApplyLabelData instance = new ClientRequestApplyLabelData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestApplyLabelData instance = new ClientRequestApplyLabelData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFileID method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetFileId() {
        ClientRequestApplyLabelData instance = new ClientRequestApplyLabelData();
        Integer expResult = 10001;
        instance.setFileID(expResult);
        Integer result = instance.getFileID();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLabelId method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetLabelId() {
        ClientRequestApplyLabelData instance = new ClientRequestApplyLabelData();
        Integer expResult = 10001;
        instance.setLabelId(expResult);
        Integer result = instance.getLabelId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetLabelText() {
        ClientRequestApplyLabelData instance = new ClientRequestApplyLabelData();
        String expResult = "Label Text";
        instance.setLabelText(expResult);
        String result = instance.getLabelText();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestApplyLabelData instance = new ClientRequestApplyLabelData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestApplyLabelData instance = new ClientRequestApplyLabelData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestAddDirectoryData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestApplyLabelData instance = new ClientRequestApplyLabelData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.APPLY_LABEL;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
