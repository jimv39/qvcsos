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

import com.qumasoft.qvcslib.CommonFilterFileCollection;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Update Filter File Collection Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestUpdateFilterFileCollectionDataTest {

    /**
     * Test of getUserName method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestUpdateFilterFileCollectionData instance = new ClientRequestUpdateFilterFileCollectionData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestUpdateFilterFileCollectionData instance = new ClientRequestUpdateFilterFileCollectionData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestUpdateFilterFileCollectionData instance = new ClientRequestUpdateFilterFileCollectionData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUpdateType method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetUpdateType() {
        ClientRequestUpdateFilterFileCollectionData instance = new ClientRequestUpdateFilterFileCollectionData();
        Integer expResult = ClientRequestUpdateFilterFileCollectionData.ADD_FILE_FILTER_COLLECTION_REQUEST;
        instance.setUpdateType(expResult);
        Integer result = instance.getUpdateType();
        assertEquals(expResult, result);
    }

    /**
     * Test of using invalid value to setUpdateType method of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testSetInvalidUpdateType() {
        ClientRequestUpdateFilterFileCollectionData instance = new ClientRequestUpdateFilterFileCollectionData();
        instance.setUpdateType(ClientRequestUpdateFilterFileCollectionData.ADD_FILE_FILTER_COLLECTION_REQUEST + 1000);
    }

    /**
     * Test of getUpdateType method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetCommonFilterFileCollectionList() {
        ClientRequestUpdateFilterFileCollectionData instance = new ClientRequestUpdateFilterFileCollectionData();
        List<CommonFilterFileCollection> filterFileCollection = new ArrayList<>();
        filterFileCollection.add(new CommonFilterFileCollection());
        List<CommonFilterFileCollection> expResult = filterFileCollection;
        instance.setCommonFilterFileCollectionList(expResult);
        List<CommonFilterFileCollection> result = instance.getCommonFilterFileCollectionList();
        Integer beginningSize = expResult.size();
        assertEquals(expResult.size(), result.size());
        instance.addCommonFilterFileCollection(new CommonFilterFileCollection());
        result = instance.getCommonFilterFileCollectionList();
        assertEquals(beginningSize + 1, result.size());
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestUpdateFilterFileCollectionData instance = new ClientRequestUpdateFilterFileCollectionData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestUpdateFilterFileCollectionData instance = new ClientRequestUpdateFilterFileCollectionData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestUpdateFilterFileCollectionData instance = new ClientRequestUpdateFilterFileCollectionData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.UPDATE_FILTER_FILE_COLLECTION;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
