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
 * Client Request Server Change Server LogLevel Data Test.
 *
 * @author Jim Voris
 */
public class ClientRequestServerChangeServerLogLevelDataTest {

    /**
     * Test of getServerName method, of class ClientRequestServerChangeServerLogLevelData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestServerChangeServerLogLevelData instance = new ClientRequestServerChangeServerLogLevelData();
        String expResult = "Server Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getUserName method, of class ClientRequestServerChangeServerLogLevelData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestServerChangeServerLogLevelData instance = new ClientRequestServerChangeServerLogLevelData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPassword method, of class ClientRequestServerChangeServerLogLevelData.
     */
    @Test
    public void testGetPassword() {
        ClientRequestServerChangeServerLogLevelData instance = new ClientRequestServerChangeServerLogLevelData();
        String expResult = "User Name";
        instance.setPassword(expResult.getBytes());
        byte[] result = instance.getPassword();
        String resultString = new String(result);
        assertEquals(expResult, resultString);
    }

    /**
     * Test of getLogLevel method, of class ClientRequestServerChangeServerLogLevelData.
     */
    @Test
    public void testGetLogLevel() {
        ClientRequestServerChangeServerLogLevelData instance = new ClientRequestServerChangeServerLogLevelData();
        String expResult = "ERROR";
        instance.setLogLevel(expResult);
        String result = instance.getLogLevel();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestServerChangeServerLogLevelData instance = new ClientRequestServerChangeServerLogLevelData();
        instance.setShortWorkfileName("foobar");
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestServerChangeServerLogLevelData instance = new ClientRequestServerChangeServerLogLevelData();
        String shortName = instance.getShortWorkfileName();
    }

    /**
     * Test of getOperationType method, of class ClientRequestServerChangeServerLogLevelData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestServerChangeServerLogLevelData instance = new ClientRequestServerChangeServerLogLevelData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SERVER_CHANGE_LOG_LEVEL;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

}
