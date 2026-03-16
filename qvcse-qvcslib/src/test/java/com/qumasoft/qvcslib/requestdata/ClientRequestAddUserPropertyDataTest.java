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
import com.qumasoft.qvcslib.UserPropertyData;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Add User Property Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestAddUserPropertyDataTest {

    /**
     * Test of getUserName method, of class ClientRequestAddUserPropertyData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getClientComputerName method, of class ClientRequestAddUserPropertyData.
     */
    @Test
    public void testGetClientComputerName() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String expResult = "Client Computer Name";
        instance.setClientComputerName(expResult);
        String result = instance.getClientComputerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPropertiesKey method, of class ClientRequestAddUserPropertyData.
     */
    @Test
    public void testGetPropertiesKey() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String expResult = "Property key";
        instance.setPropertiesKey(expResult);
        String result = instance.getPropertiesKey();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPropertiesKey method, of class ClientRequestAddUserPropertyData.
     */
    @Test
    public void testGetUserPropertyData() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String expResult = "User and Computer";
        UserPropertyData userPropertyData = new UserPropertyData();
        userPropertyData.setUserAndComputer(expResult);
        instance.setUserPropertyData(userPropertyData);
        String result = instance.getUserPropertyData().getUserAndComputer();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet2() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        instance.setProjectName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet2() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String shortName = instance.getProjectName();
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet3() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        instance.setBranchName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet3() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String shortName = instance.getBranchName();
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet4() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        instance.setPassword(null);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet4() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        byte[] shortName = instance.getPassword();
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet5() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        instance.setRole("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet5() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String shortName = instance.getRole();
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet6() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        instance.setLabelId(100);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet6() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        Integer shortName = instance.getLabelId();
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet7() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        instance.setLabelText("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet7() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String shortName = instance.getLabelText();
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet8() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        instance.setServerName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet8() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        String shortName = instance.getServerName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestUpdateFilterFileCollectionData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestAddUserPropertyData instance = new ClientRequestAddUserPropertyData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.ADD_USER_PROPERTY;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
