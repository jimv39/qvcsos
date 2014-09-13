/*
 * Copyright 2014 JimVoris.
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
package com.qumasoft.server.directorytree;

import com.qumasoft.qvcslib.QVCSRuntimeException;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the directory node class.
 * @author JimVoris
 */
public class DirectoryNodeTest {

    public DirectoryNodeTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getId method, of class DirectoryNode.
     */
    @Test
    public void testGetId() {
        Integer id = 1;
        DirectoryNode instance = new DirectoryNode(id, null, "name");
        assertEquals(instance.getId(), id);
    }

    /**
     * Test of setParentId/getParentId method, of class DirectoryNode.
     */
    @Test
    public void testSetParentId() {
        Integer id = 1;
        DirectoryNode instance = new DirectoryNode(1, null, "name");
        instance.setParentId(id);
        assertEquals(instance.getParentId(), id);
    }

    /**
     * Test of setName/getName method, of class DirectoryNode.
     */
    @Test
    public void testSetGetName() {
        String originalName = "test";
        DirectoryNode instance = new DirectoryNode(1, null, originalName);
        assertEquals(instance.getName(), originalName);
        instance.setName("newName");
        assertEquals(instance.getName(), "newName");
    }

    /**
     * Test of getNodeType method, of class DirectoryNode.
     */
    @Test
    public void testGetNodeType() {
        DirectoryNode instance = new DirectoryNode(1, null, "name");
        NodeType expResult = NodeType.DIRECTORY;
        NodeType result = instance.getNodeType();
        assertEquals(expResult, result);
    }

    /**
     * Test of addNode method, of class DirectoryNode.
     */
    @Test
    public void testAddNode() {
        Node node = new FileNode(100, null, "Test");
        Integer id = 1;
        DirectoryNode instance = new DirectoryNode(id, null, "name");
        instance.addNode(node);
        assertEquals(node.getParentId(), id);
        assertEquals(instance.getNode(100), node);
        assertEquals(instance.getNode("Test"), node);
        instance.removeNode(node);
        assertNull(instance.getNode(100));
        assertNull(instance.getNode("Test"));
    }

    /**
     * Test of addNode method, of class DirectoryNode.
     */
    @Test(expected = QVCSRuntimeException.class)
    public void testAddNodeDupIdShouldFail() {
        Node node = new FileNode(100, null, "Test");
        Node nodeIdDup = new FileNode(100, null, "Foo");
        Integer id = 1;
        DirectoryNode instance = new DirectoryNode(id, null, "name");
        instance.addNode(node);
        instance.addNode(nodeIdDup);
    }

    /**
     * Test of addNode method, of class DirectoryNode.
     */
    @Test(expected = QVCSRuntimeException.class)
    public void testAddNodeDupNameShouldFail() {
        Node node = new FileNode(100, null, "Test");
        Node nodeNameDup = new FileNode(200, null, "Test");
        Integer id = 1;
        DirectoryNode instance = new DirectoryNode(id, null, "name");
        instance.addNode(node);
        instance.addNode(nodeNameDup);
    }

    /**
     * Test of asString method, of class DirectoryNode.
     */
    @Test
    public void testAsString() {
        Integer id = 100;
        DirectoryNode instance = new DirectoryNode(id, null, "directory");
        String expResult = "<D id=:100: parentId=:null: name=:directory:/>\n";
        String result = instance.asString();
        assertEquals(expResult, result);
    }

}
