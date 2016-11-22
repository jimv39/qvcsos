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
import org.junit.Ignore;
import org.junit.Test;

/**
 * Unit test for the ProjectTree class.
 *
 * @author JimVoris
 */
public class ProjectTreeTest {

    ProjectTree instance;
    Integer file1Id;
    Integer file2Id;
    Integer dir1Id;
    Integer dir2Id;
    static final String DIR1_NAME = "dir1Name";
    static final String DIR2_NAME = "dir2Name";
    static final String FILE1_NAME = "File1";
    static final String FILE2_NAME = "File2";

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
        instance = populateTestTree();
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAsStringOfDirectoryNode() {
        ProjectTree projectTreeA = new ProjectTree();

        Integer rootId = projectTreeA.addDirectory(null, "root");
        projectTreeA.addFile(rootId, "test.txt");
        projectTreeA.addFile(rootId, "test1.txt");

        Integer childAId = projectTreeA.addDirectory(rootId, "childA");
        projectTreeA.addFile(childAId, "foo.bar");

        Integer childAAId = projectTreeA.addDirectory(rootId, "childAA");
        projectTreeA.addFile(childAAId, "bar.foo");

        Integer childBId = projectTreeA.addDirectory(childAId, "childB");
        projectTreeA.addFile(childBId, "zanzibar.txt");

        Integer childCId = projectTreeA.addDirectory(childBId, "childC");
        projectTreeA.addFile(childCId, "aardvark.txt");

        String outputStringA = projectTreeA.asString();

        ProjectTree projectTreeB = new ProjectTree();
        projectTreeB.fromString(outputStringA);
        String outputStringB = projectTreeB.asString();

        assertEquals(outputStringA, outputStringB);
    }

    @Ignore
    @Test
    public void testDirectoryHierarchy() throws IOException {
        ProjectTree projectTree = new ProjectTree();
        Path startingPath = FileSystems.getDefault().getPath("/Users/JimVoris/dev/qvcsos");
        Integer rootId = projectTree.addDirectory(null, "root");
        MySimplePathHelper helper = new MySimplePathHelper(rootId, projectTree);
        MySimpleFileVisitor visitor = new MySimpleFileVisitor(helper);
        Path returnedStartingPath = Files.walkFileTree(startingPath, visitor);
        System.out.println(projectTree.asString());
        DirectoryNode rootNode = (DirectoryNode) projectTree.getNode(0);
    }

    private ProjectTree populateTestTree() {
        ProjectTree projectTree = new ProjectTree();
        Integer rootId = projectTree.addDirectory(null, "root");
        dir1Id = projectTree.addDirectory(rootId, DIR1_NAME);
        file1Id = projectTree.addFile(dir1Id, FILE1_NAME);
        dir2Id = projectTree.addDirectory(rootId, DIR2_NAME);
        file2Id = projectTree.addFile(dir2Id, FILE2_NAME);
        return projectTree;
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
                Integer newDirectoryId = pathHelper.projectTree.addDirectory(pathHelper.currentDirectoryNodeId, dir.toFile().getName());
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
            if (!file.toFile().getName().startsWith(".")) {
                pathHelper.projectTree.addFile(pathHelper.currentDirectoryNodeId, file.toFile().getName());
            }
            return FileVisitResult.CONTINUE;
        }

    }

    static class MySimplePathHelper {

        Integer currentDirectoryNodeId;
        ProjectTree projectTree;

        MySimplePathHelper(Integer rootId, ProjectTree projectTree) {
            this.currentDirectoryNodeId = rootId;
            this.projectTree = projectTree;
        }
    }

}
