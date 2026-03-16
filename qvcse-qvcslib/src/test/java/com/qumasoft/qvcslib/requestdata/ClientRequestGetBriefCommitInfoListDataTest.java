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
 * Client Request Get Info For Merge Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestGetBriefCommitInfoListDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetBriefCommitInfoListData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetBriefCommitInfoListData instance = new ClientRequestGetBriefCommitInfoListData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestGetBriefCommitInfoListData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestGetBriefCommitInfoListData instance = new ClientRequestGetBriefCommitInfoListData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommitId method, of class ClientRequestGetBriefCommitInfoListData.
     */
    @Test
    public void testGetCommitId() {
        ClientRequestGetBriefCommitInfoListData instance = new ClientRequestGetBriefCommitInfoListData();
        Integer expResult = 101;
        instance.setCommitId(expResult);
        Integer result = instance.getCommitId();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetBriefCommitInfoListData instance = new ClientRequestGetBriefCommitInfoListData();
        instance.setRevisionString("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetBriefCommitInfoListData instance = new ClientRequestGetBriefCommitInfoListData();
        String shortName = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetBriefCommitInfoListData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestGetBriefCommitInfoListData instance = new ClientRequestGetBriefCommitInfoListData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_BRIEF_COMMIT_LIST;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
