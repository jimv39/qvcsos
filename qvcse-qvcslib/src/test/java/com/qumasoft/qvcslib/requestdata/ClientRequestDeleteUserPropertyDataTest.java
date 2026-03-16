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
 * Client Request Delete User Property Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestDeleteUserPropertyDataTest {

    /**
     * Test of getUserName method, of class ClientRequestDeleteUserPropertyData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestDeleteUserPropertyData instance = new ClientRequestDeleteUserPropertyData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPropertiesKey method, of class ClientRequestDeleteUserPropertyData.
     */
    @Test
    public void testGetUserPropertyData() {
        ClientRequestDeleteUserPropertyData instance = new ClientRequestDeleteUserPropertyData();
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
        ClientRequestDeleteUserPropertyData instance = new ClientRequestDeleteUserPropertyData();
        instance.setRevisionString("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestDeleteUserPropertyData instance = new ClientRequestDeleteUserPropertyData();
        String shortName = instance.getRevisionString();
    }

    /**
     * Test of getOperationType method, of class ClientRequestDeleteUserPropertyData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestDeleteUserPropertyData instance = new ClientRequestDeleteUserPropertyData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.DELETE_USER_PROPERTY;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
