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
 * Client Request Get User Commit Comments Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestGetUserCommitCommentsDataTest {

    /**
     * Test of getUserName method, of class ClientRequestGetUserCommitCommentsData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestGetUserCommitCommentsData instance = new ClientRequestGetUserCommitCommentsData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestGetUserCommitCommentsData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetUserCommitCommentsData instance = new ClientRequestGetUserCommitCommentsData();
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
        ClientRequestGetUserCommitCommentsData instance = new ClientRequestGetUserCommitCommentsData();
        instance.setRevisionString("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetUserCommitCommentsData instance = new ClientRequestGetUserCommitCommentsData();
        String shortName = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetUserCommitCommentsData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestGetUserCommitCommentsData instance = new ClientRequestGetUserCommitCommentsData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_USER_COMMIT_COMMENTS;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
