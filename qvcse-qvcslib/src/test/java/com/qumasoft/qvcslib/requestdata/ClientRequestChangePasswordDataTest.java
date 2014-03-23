//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//

package com.qumasoft.qvcslib.requestdata;

import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.requestdata.ClientRequestChangePasswordData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client request change password data test.
 *
 * @author Jim Voris
 */
public class ClientRequestChangePasswordDataTest {

    /**
     * Test of getUserName method, of class ClientRequestChangePasswordData.
     */
    @Test
    public void testGetUserName() {
        ClientRequestChangePasswordData instance = new ClientRequestChangePasswordData();
        String expResult = "User Name";
        instance.setUserName(expResult);
        String result = instance.getUserName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOldPassword method, of class ClientRequestChangePasswordData.
     */
    @Test
    public void testGetOldPassword() {
        ClientRequestChangePasswordData instance = new ClientRequestChangePasswordData();
        String oldPassword = "Old Password";
        byte[] expResult = oldPassword.getBytes();
        instance.setOldPassword(expResult);
        byte[] result = instance.getOldPassword();
        assertEquals(result.length, expResult.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
    }

    /**
     * Test of getNewPassword method, of class ClientRequestChangePasswordData.
     */
    @Test
    public void testGetNewPassword() {
        ClientRequestChangePasswordData instance = new ClientRequestChangePasswordData();
        String newPassword = "New Password";
        byte[] expResult = newPassword.getBytes();
        instance.setNewPassword(expResult);
        byte[] result = instance.getNewPassword();
        assertEquals(result.length, expResult.length);
        for (int i = 0; i < result.length; i++) {
            assertEquals(expResult[i], result[i]);
        }
    }

    /**
     * Test of getServerName method, of class ClientRequestChangePasswordData.
     */
    @Test
    public void testGetServerName() {
        ClientRequestChangePasswordData instance = new ClientRequestChangePasswordData();
        String expResult = "Server Name";
        instance.setServerName(expResult);
        String result = instance.getServerName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getOperationType method, of class ClientRequestChangePasswordData.
     */
    @Test
    public void testGetOperationType() {
        ClientRequestChangePasswordData instance = new ClientRequestChangePasswordData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.CHANGE_USER_PASSWORD;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getProjectName method, of class ClientRequestChangePasswordData.
     */
    @Test(expected = QVCSRuntimeException.class)
    public void testGetProjectName() {
        ClientRequestChangePasswordData instance = new ClientRequestChangePasswordData();
        String expResult = null;
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestChangePasswordData.
     */
    @Test(expected = QVCSRuntimeException.class)
    public void testGetViewName() {
        ClientRequestChangePasswordData instance = new ClientRequestChangePasswordData();
        String expResult = null;
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }
}
