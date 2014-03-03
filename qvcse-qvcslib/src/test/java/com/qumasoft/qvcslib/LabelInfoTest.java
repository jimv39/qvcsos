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

package com.qumasoft.qvcslib;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Label info test.
 *
 * @author Jim Voris
 */
public class LabelInfoTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test the ctor of com.qumasoft.qvcslib.LabelInfo
     */
    @Test
    public void testConstructor() {
        System.out.println("testConstructor");

        LabelInfo labelInfo1 = new LabelInfo("This is the 1st label", "1.0", false, 0);
        LabelInfo labelInfo2 = new LabelInfo("This is the 2nd label", "1.0", true, 0);

        LabelInfo labelInfo3 = new LabelInfo("This is the 3rd label", "1.0.1.1", false, 0);
        LabelInfo labelInfo4 = new LabelInfo("This is the 4th label", "1.0.1.1", true, 0);

        LabelInfo labelInfo5 = new LabelInfo("This is the 5th label", "1.0.1.2.1.1", false, 0);
        LabelInfo labelInfo6 = new LabelInfo("This is the 6th label", "1.0.1.2.1.1", true, 0);
    }

    /**
     * Test of isFloatingLabel method, of class com.qumasoft.qvcslib.LabelInfo.
     */
    @Test
    public void testIsFloatingLabel() {
        System.out.println("testIsFloatingLabel");

        LabelInfo labelInfo1 = new LabelInfo("This is the 1st label", "1.0", false, 0);
        assertFalse("Expected false for floating label, but got true instead.", labelInfo1.isFloatingLabel());

        LabelInfo labelInfo2 = new LabelInfo("This is the 2nd label", "1.0", true, 0);
        assertTrue("Expected true for floating labe, but got true instead.", labelInfo2.isFloatingLabel());
    }

    /**
     * Test of getIsObsolete method, of class com.qumasoft.qvcslib.LabelInfo.
     */
    @Test
    public void testGetIsObsolete() {
        System.out.println("testGetIsObsolete");

        LabelInfo labelInfo1 = new LabelInfo("This is the 1st label", "1.0", false, 0);
        assertFalse("Expected false for obsolete flag, but got true instead.", labelInfo1.getIsObsolete());

        LabelInfo labelInfo2 = new LabelInfo("This is the 2nd label", "-1.-1", false, 0);
        assertTrue("Expected true for obsolete flag, but got true instead.", labelInfo2.getIsObsolete());
    }

    /**
     * Test of setIsObsolete method, of class com.qumasoft.qvcslib.LabelInfo.
     */
    @Test
    public void testSetIsObsolete() {
        System.out.println("testSetIsObsolete");

        LabelInfo labelInfo1 = new LabelInfo("This is the 1st label", "1.0", false, 0);
        labelInfo1.setIsObsolete(true, 0);
        assertTrue("Expected true for obsolete flag, but got true instead.", labelInfo1.getIsObsolete());
    }

    /**
     * Test of getLabelRevisionString method, of class com.qumasoft.qvcslib.LabelInfo.
     */
    @Test
    public void testGetLabelRevisionString() {
        System.out.println("testGetLabelRevisionString");

        LabelInfo labelInfo1 = new LabelInfo("This is the 1st label", "1.0", false, 0);
        assertTrue("Compare of revision strings failed", (0 == labelInfo1.getLabelRevisionString().compareTo("1.0")));
    }

    /**
     * Test of getLabelString method, of class com.qumasoft.qvcslib.LabelInfo.
     */
    @Test
    public void testGetLabelString() {
        System.out.println("testGetLabelString");

        LabelInfo labelInfo1 = new LabelInfo("2.2.2", "1.0", false, 0);
        assertTrue("Compare of revision strings failed", (0 == labelInfo1.getLabelString().compareTo("2.2.2")));
    }
}
