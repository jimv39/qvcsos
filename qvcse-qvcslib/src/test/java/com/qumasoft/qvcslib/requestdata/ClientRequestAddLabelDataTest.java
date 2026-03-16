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
 * Client Request Add Label Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestAddLabelDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestAddLabelData instance = new ClientRequestAddLabelData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetLabelText() {
        ClientRequestAddLabelData instance = new ClientRequestAddLabelData();
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
        ClientRequestAddLabelData instance = new ClientRequestAddLabelData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestAddLabelData instance = new ClientRequestAddLabelData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestAddDirectoryData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestAddLabelData instance = new ClientRequestAddLabelData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.ADD_LABEL;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
