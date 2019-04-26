/*   Copyright 2004-2019 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.qvcslib.requestdata;

import com.qumasoft.qvcslib.QVCSRuntimeException;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Client Request Get logfileInfo Data Test.
 * @author Jim Voris
 */
public class ClientRequestGetLogfileInfoDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestGetLogfileInfoData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestGetLogfileInfoData instance = new ClientRequestGetLogfileInfoData();
        String expResult = "Project name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getViewName method, of class ClientRequestGetLogfileInfoData.
     */
    @Test
    public void testGetViewName() {
        ClientRequestGetLogfileInfoData instance = new ClientRequestGetLogfileInfoData();
        String expResult = "View name";
        instance.setViewName(expResult);
        String result = instance.getViewName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestGetLogfileInfoData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestGetLogfileInfoData instance = new ClientRequestGetLogfileInfoData();
        String expResult = "Appended path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestGetLogfileInfoData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestGetLogfileInfoData instance = new ClientRequestGetLogfileInfoData();
        String expResult = "Short workfile name";
        instance.setShortWorkfileName(expResult);
        String result = instance.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestGetLogfileInfoData instance = new ClientRequestGetLogfileInfoData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestGetLogfileInfoData instance = new ClientRequestGetLogfileInfoData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestGetLogfileInfoData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestGetLogfileInfoData instance = new ClientRequestGetLogfileInfoData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.GET_LOGFILE_INFO;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
