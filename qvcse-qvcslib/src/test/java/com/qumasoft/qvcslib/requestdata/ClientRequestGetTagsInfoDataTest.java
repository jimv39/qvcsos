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
 * Client Request Get Tags Info Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestGetTagsInfoDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetTagsInfoData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetTagsInfoData instance = new ClientRequestGetTagsInfoData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestGetTagsInfoData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestGetTagsInfoData instance = new ClientRequestGetTagsInfoData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestGetTagsInfoData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestGetTagsInfoData instance = new ClientRequestGetTagsInfoData();
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
        ClientRequestGetTagsInfoData instance = new ClientRequestGetTagsInfoData();
        instance.setRevisionString("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetTagsInfoData instance = new ClientRequestGetTagsInfoData();
        String shortName = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetTagsInfoData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestGetTagsInfoData instance = new ClientRequestGetTagsInfoData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_TAGS_INFO;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
