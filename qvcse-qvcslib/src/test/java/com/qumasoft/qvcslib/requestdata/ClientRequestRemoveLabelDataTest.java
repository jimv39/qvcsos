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
 * Client Request Remove Label Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestRemoveLabelDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestRemoveLabelData instance = new ClientRequestRemoveLabelData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getLabelId method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetLabelId() {
        ClientRequestRemoveLabelData instance = new ClientRequestRemoveLabelData();
        Integer expResult = 10001;
        instance.setLabelId(expResult);
        Integer result = instance.getLabelId();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFileID method, of class ClientRequestAddLabelData.
     */
    @Test
    public void testGetFileId() {
        ClientRequestRemoveLabelData instance = new ClientRequestRemoveLabelData();
        Integer expResult = 10001;
        instance.setFileID(expResult);
        Integer result = instance.getFileID();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestRemoveLabelData instance = new ClientRequestRemoveLabelData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestRemoveLabelData instance = new ClientRequestRemoveLabelData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestAddDirectoryData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestRemoveLabelData instance = new ClientRequestRemoveLabelData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.REMOVE_LABEL;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
