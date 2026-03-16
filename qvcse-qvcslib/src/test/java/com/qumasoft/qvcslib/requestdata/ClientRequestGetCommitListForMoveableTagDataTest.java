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
public class ClientRequestGetCommitListForMoveableTagDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetCommitListForMoveableTagData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetCommitListForMoveableTagData instance = new ClientRequestGetCommitListForMoveableTagData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestGetCommitListForMoveableTagData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestGetCommitListForMoveableTagData instance = new ClientRequestGetCommitListForMoveableTagData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetCommitListForMoveableTagData instance = new ClientRequestGetCommitListForMoveableTagData();
        instance.setRevisionString("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetCommitListForMoveableTagData instance = new ClientRequestGetCommitListForMoveableTagData();
        String shortName = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetCommitListForMoveableTagData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestGetCommitListForMoveableTagData instance = new ClientRequestGetCommitListForMoveableTagData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_COMMIT_LIST_FOR_MOVEABLE_TAG_READ_ONLY_BRANCHES;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
