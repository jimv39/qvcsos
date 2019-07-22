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
 * Client Request Set Comment Prefix Data Test.
 * @author Jim Voris
 */
public class ClientRequestSetCommentPrefixDataTest {

    /**
     * Test of getProjectName method, of class ClientRequestSetCommentPrefixData.
     */
    @Test
    public void testGetProjectName() {
        ClientRequestSetCommentPrefixData instance = new ClientRequestSetCommentPrefixData();
        String expResult = "Project Name";
        instance.setProjectName(expResult);
        String result = instance.getProjectName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getBranchName method, of class ClientRequestSetCommentPrefixData.
     */
    @Test
    public void testGetBranchName() {
        ClientRequestSetCommentPrefixData instance = new ClientRequestSetCommentPrefixData();
        String expResult = "Branch Name";
        instance.setBranchName(expResult);
        String result = instance.getBranchName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getAppendedPath method, of class ClientRequestSetCommentPrefixData.
     */
    @Test
    public void testGetAppendedPath() {
        ClientRequestSetCommentPrefixData instance = new ClientRequestSetCommentPrefixData();
        String expResult = "Appended Path";
        instance.setAppendedPath(expResult);
        String result = instance.getAppendedPath();
        assertEquals(expResult, result);
    }

    /**
     * Test of getShortWorkfileName method, of class ClientRequestSetCommentPrefixData.
     */
    @Test
    public void testGetShortWorkfileName() {
        ClientRequestSetCommentPrefixData instance = new ClientRequestSetCommentPrefixData();
        String expResult = "Short Workfilename";
        instance.setShortWorkfileName(expResult);
        String result = instance.getShortWorkfileName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getCommentPrefix method, of class ClientRequestSetCommentPrefixData.
     */
    @Test
    public void testGetCommentPrefix() {
        ClientRequestSetCommentPrefixData instance = new ClientRequestSetCommentPrefixData();
        String expResult = "Comment Prefix";
        instance.setCommentPrefix(expResult);
        String result = instance.getCommentPrefix();
        assertEquals(expResult, result);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidSet() {
        ClientRequestSetCommentPrefixData instance = new ClientRequestSetCommentPrefixData();
        instance.setFileID(1);
    }

    /**
     * Verify that we get a QVCSRuntimeException for at least one of the invalid fields.
     */
    @Test(expected=QVCSRuntimeException.class)
    public void testInvalidGet() {
        ClientRequestSetCommentPrefixData instance = new ClientRequestSetCommentPrefixData();
        Integer fileId = instance.getFileID();
    }

    /**
     * Test of getOperationType method, of class ClientRequestSetCommentPrefixData.
     */
    @Test
    public void testGetOperationType() {
        System.out.println("getOperationType");
        ClientRequestSetCommentPrefixData instance = new ClientRequestSetCommentPrefixData();
        ClientRequestDataInterface.RequestOperationType expResult = ClientRequestDataInterface.RequestOperationType.SET_COMMENT_PREFIX;
        ClientRequestDataInterface.RequestOperationType result = instance.getOperationType();
        assertEquals(expResult, result);
    }
}
