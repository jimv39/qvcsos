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
 * Client Request Delete Provisional Records Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestDeleteProvisionalRecordsDataTest {

    /**
     * Test of getUserName method, of class ClientRequestDeleteProvisionalRecordsData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestDeleteProvisionalRecordsData instance = new ClientRequestDeleteProvisionalRecordsData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestDeleteProvisionalRecordsData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestDeleteProvisionalRecordsData instance = new ClientRequestDeleteProvisionalRecordsData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestDeleteProvisionalRecordsData instance = new ClientRequestDeleteProvisionalRecordsData();
        instance.setRevisionString("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestDeleteProvisionalRecordsData instance = new ClientRequestDeleteProvisionalRecordsData();
        String shortName = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestDeleteProvisionalRecordsData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestDeleteProvisionalRecordsData instance = new ClientRequestDeleteProvisionalRecordsData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.DELETE_PROVISIONAL_RECORDS;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
