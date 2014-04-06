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
import java.util.concurrent.atomic.AtomicInteger;
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
        int nodeId = 0;
        DirectoryNode rootA = new DirectoryNode(nodeId++, "root");
        rootA.addNode(new FileNode(nodeId++, "test.txt"));
        rootA.addNode(new FileNode(nodeId++, "test1.txt"));

        DirectoryNode childA = new DirectoryNode(nodeId++, "childA");
        rootA.addNode(childA);
        childA.addNode(new FileNode(nodeId++, "foo.bar"));

        DirectoryNode childAA = new DirectoryNode(nodeId++, "childAA");
        childAA.addNode(new FileNode(nodeId++, "bar.foo"));
        rootA.addNode(childAA);

        DirectoryNode childB = new DirectoryNode(nodeId++, "childB");
        childA.addNode(childB);
        childB.addNode(new FileNode(nodeId++, "zanzibar.txt"));

        DirectoryNode childC = new DirectoryNode(nodeId++, "childC");
        childB.addNode(childC);
        childC.addNode(new FileNode(nodeId++, "aardvark.txt"));

        String outputStringA = rootA.asString();

        DirectoryNode rootB = new DirectoryNode(outputStringA);
        assertEquals(outputStringA, rootB.asString());
        System.out.println(rootA.asString());
    }

    @Test
    public void testDirectoryHierarchy() throws IOException {
        AtomicInteger nodeIdInteger = new AtomicInteger();
        Path startingPath = FileSystems.getDefault().getPath("/Users/JimVoris/dev/qvcsos");
        DirectoryNode root = new DirectoryNode(nodeIdInteger.getAndIncrement(), "root");
        MySimplePathHelper helper = new MySimplePathHelper(root, nodeIdInteger);
        MySimpleFileVisitor visitor = new MySimpleFileVisitor(helper);
        Path returnedStartingPath = Files.walkFileTree(startingPath, visitor);
        System.out.println(root.asString());
    }

    static class MySimpleFileVisitor extends SimpleFileVisitor<Path> {

        MySimplePathHelper pathHelper;
        Stack<DirectoryNode> directoryStack = new Stack<>();

        MySimpleFileVisitor(MySimplePathHelper pathHelper) {
            this.pathHelper = pathHelper;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            FileVisitResult retVal = FileVisitResult.SKIP_SUBTREE;
            Objects.requireNonNull(dir);
            Objects.requireNonNull(attrs);
            if (!dir.toFile().getName().startsWith(".")) {
                directoryStack.push(pathHelper.currentDirectoryNode);
                DirectoryNode newDirectory = new DirectoryNode(pathHelper.nodeIdInteger.getAndIncrement(), dir.toFile().getName());
                pathHelper.currentDirectoryNode.addNode(newDirectory);
                pathHelper.currentDirectoryNode = newDirectory;
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
            pathHelper.currentDirectoryNode = directoryStack.pop();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Objects.requireNonNull(file);
            Objects.requireNonNull(attrs);
            pathHelper.currentDirectoryNode.addNode(new FileNode(pathHelper.nodeIdInteger.getAndIncrement(), file.toFile().getName()));
            return FileVisitResult.CONTINUE;
        }

    }

    static class MySimplePathHelper {

        DirectoryNode currentDirectoryNode;
        AtomicInteger nodeIdInteger;

        MySimplePathHelper(DirectoryNode currentDirectoryNode, AtomicInteger nodeIdInteger) {
            this.currentDirectoryNode = currentDirectoryNode;
            this.nodeIdInteger = nodeIdInteger;
        }
    }

}
