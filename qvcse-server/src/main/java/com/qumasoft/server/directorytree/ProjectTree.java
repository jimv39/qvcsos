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
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Manage a String representation of the directory tree for a project/view. There will one instance of a ProjectTree for each separate view.
 *
 * @author Jim Voris
 */
public class ProjectTree {
    private static final int ARG_LIST_SIZE = 3;
    private static final int ID_INDEX = 0;
    private static final int PARENTID_INDEX = 1;
    private static final int NAME_INDEX = 2;

    private static final int ID_EXTRACTION_INDEX = 1;
    private static final int PARENTID_EXTRACTION_INDEX = 3;
    private static final int NAME_EXTRACTION_INDEX = 5;

    private final Map<Integer, Node> idToNodeMap;
    // TODO -- this needs to be global so that it can work across branches; i.e. we want to guarantee unique nodeId's across branches so we can merge easily.
    private AtomicInteger nodeId;

    /**
     * Default constructor.
     */
    public ProjectTree() {
        idToNodeMap = new TreeMap<>();
        this.nodeId = new AtomicInteger(0);
    }

    /**
     * Get this project tree's node map.
     * @return this project tree's node map.
     */
    public Map<Integer, Node> getNodeMap() {
        return this.idToNodeMap;
    }

    /**
     * Create a String representation of this ProjectTree.
     * @return a String representation of this ProjectTree.
     */
    public synchronized String asString() {
        StringBuilder asString = new StringBuilder();
        for (Node node : idToNodeMap.values()) {
            asString.append(node.asString());
        }
        return asString.toString();
    }

    /**
     * Populate this ProjectTree from a String representation of a ProjectTree.
     * @param asString A String representation of a ProjectTree (typically created by a call to asString() ).
     */
    public synchronized void fromString(String asString) {
        int maxNodeId = -1;
        String[] lines = asString.split("\n");
        for (String line : lines) {
            String[] idsAndName = parseLine(line);
            Integer id = Integer.parseInt(idsAndName[ID_INDEX]);
            if (id > maxNodeId) {
                maxNodeId = id;
            }
            String name = idsAndName[NAME_INDEX];
            if (line.startsWith("<D")) {
                Integer parentId;
                DirectoryNode directoryNode;
                try {
                    parentId = Integer.parseInt(idsAndName[PARENTID_INDEX]);
                    directoryNode = new DirectoryNode(id, (DirectoryNode) idToNodeMap.get(parentId), name);
                } catch (NumberFormatException e) {
                    System.out.println("Number format exception for [" + idsAndName[PARENTID_INDEX] + "]");
                    directoryNode = new DirectoryNode(id, null, name);
                }
                idToNodeMap.put(id, directoryNode);
            } else if (line.startsWith("<F")) {
                Integer parentId;
                FileNode fileNode;
                try {
                    parentId = Integer.parseInt(idsAndName[1]);
                    fileNode = new FileNode(id, (DirectoryNode) idToNodeMap.get(parentId), name);
                } catch (NumberFormatException e) {
                    System.out.println("Number format exception for [" + idsAndName[PARENTID_INDEX] + "]");
                    fileNode = new FileNode(id, null, name);
                }
                idToNodeMap.put(id, fileNode);
            }
        }
        this.nodeId = new AtomicInteger(maxNodeId);
    }

    /**
     * Add a directory node.
     * @param parentId the directory's parent node id.
     * @param name the name of the directory.
     * @return the node id for the created directory node.
     */
    public synchronized Integer addDirectory(Integer parentId, String name) {
        Integer id = nodeId.getAndIncrement();
        DirectoryNode directoryNode;
        if (parentId != null) {
            directoryNode = new DirectoryNode(id, (DirectoryNode) idToNodeMap.get(parentId), name);
        } else {
            directoryNode = new DirectoryNode(id, (DirectoryNode) null, name);
        }
        idToNodeMap.put(id, directoryNode);
        return id;
    }

    /**
     * Move a directory.
     * @param node the directory to move.
     * @param newParent the directory's new parent directory.
     */
    public synchronized void moveDirectory(DirectoryNode node, DirectoryNode newParent) {
        // Verify that the directory is where we expect.
        DirectoryNode nodeInMap = (DirectoryNode) idToNodeMap.get(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("Directory node mismatch on moveDirectory.");
        }
        nodeInMap.setParentId(newParent.getId());
    }

    /**
     * Rename a directory.
     * @param node the directory to rename.
     * @param newName the new name of the directory.
     */
    public synchronized void renameDirectory(DirectoryNode node, String newName) {
        // Verify that the directory is where we expect.
        DirectoryNode nodeInMap = (DirectoryNode) idToNodeMap.get(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("Directory node mismatch on renameDirectory.");
        }
        nodeInMap.setName(newName);
    }

    /**
     * Delete a directory. Any child nodes of the directory will be orphaned.
     * @param node the directory to delete.
     */
    public synchronized void deleteDirectory(DirectoryNode node) {
        // Verify that the directory is where we expect.
        DirectoryNode nodeInMap = (DirectoryNode) idToNodeMap.get(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("Directory node mismatch on deleteDirectory.");
        }
        if (node.getParentId() != -1) {
            DirectoryNode directoryNode = (DirectoryNode) idToNodeMap.get(node.getParentId());
            directoryNode.removeNode(node);
        } else {
            throw new QVCSRuntimeException("Attempt to delete root directory not allowed.");
        }
    }

    /**
     * Add a file node.
     * @param parentId the file's parent node id.
     * @param name the file name.
     * @return the node id for the created file node.
     */
    public synchronized Integer addFile(Integer parentId, String name) {
        Integer id = nodeId.getAndIncrement();
        FileNode fileNode = new FileNode(id, (DirectoryNode) idToNodeMap.get(parentId), name);
        idToNodeMap.put(id, fileNode);
        return id;
    }

    /**
     * Move a file.
     * @param node the file to move.
     * @param newParent the file's new parent directory.
     */
    public synchronized void moveFile(FileNode node, DirectoryNode newParent) {
        // Verify the file is where we expect.
        FileNode nodeInMap = (FileNode) idToNodeMap.get(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("File node mismatch on moveFile.");
        }
        nodeInMap.setParentId(newParent.getId());
    }

    /**
     * Rename a file.
     * @param node the file to rename.
     * @param newName the file's new name.
     */
    public synchronized void renameFile(FileNode node, String newName) {
        // Verify the file is where we expect.
        FileNode nodeInMap = (FileNode) idToNodeMap.get(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("File node mismatch on renameFile.");
        }
        nodeInMap.setName(newName);
    }

    /**
     * Delete a file.
     * @param node the file to delete.
     */
    public synchronized void deleteFile(FileNode node) {
        // Verify the file is where we expect.
        FileNode nodeInMap = (FileNode) idToNodeMap.get(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("File node mismatch on deleteFile.");
        }
        DirectoryNode directoryNode = (DirectoryNode) idToNodeMap.get(node.getParentId());
        directoryNode.removeNode(node);
    }

    private String[] parseLine(String line) {
        String[] idAndName = new String[ARG_LIST_SIZE];
        String[] splitLine = line.split(":");
        idAndName[ID_INDEX] = splitLine[ID_EXTRACTION_INDEX];
        idAndName[PARENTID_INDEX] = splitLine[PARENTID_EXTRACTION_INDEX];
        idAndName[NAME_INDEX] = splitLine[NAME_EXTRACTION_INDEX];
        return idAndName;
    }
}
