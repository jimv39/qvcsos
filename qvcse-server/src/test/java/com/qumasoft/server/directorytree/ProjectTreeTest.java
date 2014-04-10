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

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.Stack;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit test for the ProjectTree class.
 *
 * @author JimVoris
 */
public class ProjectTreeTest {

    public ProjectTreeTest() {
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

    @Test
    public void testAsStringOfDirectoryNode() {
        ProjectTree projectTreeA = new ProjectTree();
        NodeFactory nodeFactory = new NodeFactory(projectTreeA);

        Integer rootId = nodeFactory.addDirectoryNode(null, "root");
        nodeFactory.addFileNode(rootId, "test.txt");
        nodeFactory.addFileNode(rootId, "test1.txt");

        Integer childAId = nodeFactory.addDirectoryNode(rootId, "childA");
        nodeFactory.addFileNode(childAId, "foo.bar");

        Integer childAAId = nodeFactory.addDirectoryNode(rootId, "childAA");
        nodeFactory.addFileNode(childAAId, "bar.foo");

        Integer childBId = nodeFactory.addDirectoryNode(childAId, "childB");
        nodeFactory.addFileNode(childBId, "zanzibar.txt");

        Integer childCId = nodeFactory.addDirectoryNode(childBId, "childC");
        nodeFactory.addFileNode(childCId, "aardvark.txt");

        String outputStringA = projectTreeA.asString();

        ProjectTree projectTreeB = new ProjectTree();
        projectTreeB.fromString(outputStringA);
        String outputStringB = projectTreeB.asString();

        assertEquals(outputStringA, outputStringB);
        System.out.println(outputStringA);
    }

    @Test
    public void testDirectoryHierarchy() throws IOException {
        ProjectTree projectTree = new ProjectTree();
        NodeFactory nodeFactory = new NodeFactory(projectTree);
        Path startingPath = FileSystems.getDefault().getPath("/Users/JimVoris/dev/qvcsos");
        Integer rootId = nodeFactory.addDirectoryNode(null, "root");
        MySimplePathHelper helper = new MySimplePathHelper(rootId, projectTree, nodeFactory);
        MySimpleFileVisitor visitor = new MySimpleFileVisitor(helper);
        Path returnedStartingPath = Files.walkFileTree(startingPath, visitor);
        System.out.println(projectTree.asString());
        DirectoryNode rootNode = (DirectoryNode) projectTree.getNodeMap().get(0);
        System.out.println(rootNode.asTree(0));
    }

    static class MySimpleFileVisitor extends SimpleFileVisitor<Path> {

        MySimplePathHelper pathHelper;
        Stack<Integer> directoryIdStack = new Stack<>();

        MySimpleFileVisitor(MySimplePathHelper pathHelper) {
            this.pathHelper = pathHelper;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            FileVisitResult retVal = FileVisitResult.SKIP_SUBTREE;
            Objects.requireNonNull(dir);
            Objects.requireNonNull(attrs);
            if (!dir.toFile().getName().startsWith(".")) {
                directoryIdStack.push(pathHelper.currentDirectoryNodeId);
                Integer newDirectoryId = pathHelper.nodeFactory.addDirectoryNode(pathHelper.currentDirectoryNodeId, dir.toFile().getName());
                DirectoryNode parentDirectoryNode = (DirectoryNode) pathHelper.projectTree.getNodeMap().get(pathHelper.currentDirectoryNodeId);
                parentDirectoryNode.addNode(pathHelper.projectTree.getNodeMap().get(newDirectoryId));
                pathHelper.currentDirectoryNodeId = newDirectoryId;
                retVal = FileVisitResult.CONTINUE;
            }
            return retVal;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Objects.requireNonNull(dir);
            if (exc != null) {
                throw exc;
            }
            pathHelper.currentDirectoryNodeId = directoryIdStack.pop();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Objects.requireNonNull(file);
            Objects.requireNonNull(attrs);
            DirectoryNode parentDirectoryNode = (DirectoryNode) pathHelper.projectTree.getNodeMap().get(pathHelper.currentDirectoryNodeId);
            Integer fileId = pathHelper.nodeFactory.addFileNode(pathHelper.currentDirectoryNodeId, file.toFile().getName());
            FileNode fileNode = (FileNode) pathHelper.projectTree.getNodeMap().get(fileId);
            parentDirectoryNode.addNode(fileNode);
            return FileVisitResult.CONTINUE;
        }

    }

    static class MySimplePathHelper {

        Integer currentDirectoryNodeId;
        NodeFactory nodeFactory;
        ProjectTree projectTree;

        MySimplePathHelper(Integer rootId, ProjectTree projectTree, NodeFactory nodeFactory) {
            this.currentDirectoryNodeId = rootId;
            this.projectTree = projectTree;
            this.nodeFactory = nodeFactory;
        }
    }

}
