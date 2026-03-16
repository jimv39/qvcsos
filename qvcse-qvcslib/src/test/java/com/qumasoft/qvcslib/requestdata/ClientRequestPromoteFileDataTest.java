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

import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Promote File Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestPromoteFileDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestPromoteFileData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestPromoteFileData instance = new ClientRequestPromoteFileData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestPromoteFileData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestPromoteFileData instance = new ClientRequestPromoteFileData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFileID method, of class ClientRequestPromoteFileData.
     */
    @Test
    public void testGetFileId() {
        ClientRequestPromoteFileData instance = new ClientRequestPromoteFileData();
        Integer expResult = 1000;
        instance.setFileID(expResult);
        Integer result = instance.getFileID();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestPromoteFileData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestPromoteFileData instance = new ClientRequestPromoteFileData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getParentBranchName method, of class ClientRequestPromoteFileData.
     */
    @Test
    public void testGetParentBranchName() {
        ClientRequestPromoteFileData instance = new ClientRequestPromoteFileData();
        String expResult = "Parent Branch Name";
        instance.setParentBranchName(expResult);
        String result = instance.getParentBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFilePromotionInfo method, of class ClientRequestPromoteFileData.
     */
    @Test
    public void testGetFilePromotionInfo() {
        ClientRequestPromoteFileData instance = new ClientRequestPromoteFileData();
        String expResult = "Promoted From Branch Name";
        FilePromotionInfo filePromotionInfo = new FilePromotionInfo();
        filePromotionInfo.setPromotedFromBranchName(expResult);
        instance.setFilePromotionInfo(filePromotionInfo);
        String result = instance.getFilePromotionInfo().getPromotedFromBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestPromoteFileData instance = new ClientRequestPromoteFileData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestPromoteFileData instance = new ClientRequestPromoteFileData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestPromoteFileData instance = new ClientRequestPromoteFileData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.PROMOTE_FILE;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
