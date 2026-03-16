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
 * Client Request Apply Label Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestUpdateViewUtilityCommandDataTest {

    /**
     * Test of getUserName method, of class ClientRequestUpdateViewUtilityCommandData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getServerName method, of class ClientRequestUpdateViewUtilityCommandData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommandLine method, of class ClientRequestUpdateViewUtilityCommandData.
     */
    @Test
    public void testGetCommandLine() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        String expResult = "Command Line";
        instance.setCommandLine(expResult);
        String result = instance.getCommandLine();
        assertEquals(expResult, result);
    }

    /**
     * Test of getExtension method, of class ClientRequestUpdateViewUtilityCommandData.
     */
    @Test
    public void testGetExtension() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        String expResult = "Extension";
        instance.setExtension(expResult);
        String result = instance.getExtension();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAssociateCommandWithExtension method, of class ClientRequestUpdateViewUtilityCommandData.
     */
    @Test
    public void testGetAssociateCommandWithExtension() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        Boolean expResult = Boolean.TRUE;
        instance.setAssociateCommandWithExtension(expResult);
        Boolean result = instance.getAssociateCommandWithExtension();
        assertEquals(expResult, result);
    }

    /**
     * Test of getClientComputerName method, of class ClientRequestUpdateViewUtilityCommandData.
     */
    @Test
    public void testGetClientComputerName() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        String expResult = "Super cool computer";
        instance.setClientComputerName(expResult);
        String result = instance.getClientComputerName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for an invalid request type.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSetRequestType() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        instance.setRequestType(ClientRequestUpdateViewUtilityCommandData.REMOVE_UTILITY_ASSOCIATION_REQUEST + 1000);
    }

    /**
     * Test of getRequestType method, of class ClientRequestUpdateViewUtilityCommandData.
     */
    @Test
    public void testGetRequestType() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        Integer expResult = ClientRequestUpdateViewUtilityCommandData.ADD_COMMAND_LINE_REQUEST;
        instance.setRequestType(expResult);
        Integer result = instance.getRequestType();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestUpdateViewUtilityCommandData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestUpdateViewUtilityCommandData instance = new ClientRequestUpdateViewUtilityCommandData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.UPDATE_VIEW_UTILITY_COMMAND;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
