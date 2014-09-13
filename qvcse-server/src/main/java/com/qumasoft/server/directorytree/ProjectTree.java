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

    // TODO -- Need to refactor so that we have the directoryNode do the renames, etc. the idea being that there should only be a single place that 'owns' that behavior,
    // and this class is NOT the place for that. This class is the one that captures the entire tree. Whether a file can be renamed within a directory should be 'owned' by
    // the directory.

    /**
     * Default constructor.
     */
    public ProjectTree() {
        idToNodeMap = new TreeMap<>();
        this.nodeId = new AtomicInteger(0);
    }

    /**
     * Get the node associated with the given id.
     * @param id the id of the node to look up.
     * @return the node for the given id, or null if not found.
     */
    public Node getNode(Integer id) {
        return idToNodeMap.get(id);
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
        Integer id = getNextNodeId();
        DirectoryNode directoryNode;
        if (parentId != null) {
            Node parentNode = idToNodeMap.get(parentId);
            if (parentNode != null) {
                if (parentNode instanceof DirectoryNode) {
                    DirectoryNode pNode = (DirectoryNode) parentNode;
                    if (null == pNode.getNode(name)) {
                        directoryNode = new DirectoryNode(id, (DirectoryNode) parentNode, name);
                        pNode.addNode(directoryNode);
                    } else {
                        throw new QVCSRuntimeException("Cannot add directory [" + name + "] to directory [" + pNode.getName() + "] since there is already a directory or file with "
                                + "the same name");
                    }
                } else {
                    throw new QVCSRuntimeException("Invalid type of node for parent for addDirectory. Parent id: [" + parentId + "]");
                }
            } else {
                throw new QVCSRuntimeException("Invalid parentId for addDirectory. Parent node not found.");
            }
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
        DirectoryNode nodeInMap = (DirectoryNode) findNodeInMap(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("Directory node mismatch on moveDirectory.");
        }
        // Verify the new parent directory is where we expect.
        DirectoryNode newParentInMap = (DirectoryNode) findNodeInMap(newParent.getId());
        if (!newParentInMap.equals(newParent)) {
            throw new QVCSRuntimeException("New parent directory node mismatch on moveDirectory");
        }
        nodeInMap.setParentId(newParent.getId());
    }

    /**
     * Delete a directory. Any child nodes of the directory will be orphaned.
     * @param node the directory to delete.
     */
    public synchronized void deleteDirectory(DirectoryNode node) {
        // Verify that the directory is where we expect.
        DirectoryNode nodeInMap = (DirectoryNode) findNodeInMap(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("Directory node mismatch on deleteDirectory.");
        }
        if (node.getParentId() != null) {
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
        if (parentId == null) {
            throw new QVCSRuntimeException("Null parent id for addFile is not allowed.");
        }
        Integer id = getNextNodeId();
        Node parentNode = findNodeInMap(parentId);
        if (parentNode instanceof DirectoryNode) {
            DirectoryNode pNode = (DirectoryNode) parentNode;
            FileNode fileNode = new FileNode(id, (DirectoryNode) parentNode, name);
            if (null == pNode.getNode(name)) {
                pNode.addNode(fileNode);
                idToNodeMap.put(id, fileNode);
            } else {
                throw new QVCSRuntimeException("Cannot add file [" + name + "] to directory [" + pNode.getName() + "] since there is already a directory or file "
                        + "with the same name.");
            }
        } else {
            throw new QVCSRuntimeException("Invalid parent node type for addFile. Filename: [" + name + "]; parentId: [" + parentId + "]");
        }
        return id;
    }

    /**
     * Move a file.
     * @param node the file to move.
     * @param newParent the file's new parent directory.
     */
    public synchronized void moveFile(FileNode node, DirectoryNode newParent) {
        // Verify the file is where we expect.
        FileNode nodeInMap = (FileNode) findNodeInMap(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("File node mismatch on moveFile.");
        }
        DirectoryNode parentInMap = (DirectoryNode) findNodeInMap(newParent.getId());
        if (!parentInMap.equals(newParent)) {
            throw new QVCSRuntimeException("New parent directory node mismatch on moveFile.");
        }
        nodeInMap.setParentId(newParent.getId());
    }

    /**
     * Delete a file.
     * @param node the file to delete.
     */
    public synchronized void deleteFile(FileNode node) {
        // Verify the file is where we expect.
        FileNode nodeInMap = (FileNode) findNodeInMap(node.getId());
        if (!nodeInMap.equals(node)) {
            throw new QVCSRuntimeException("File node mismatch on deleteFile.");
        }
        DirectoryNode directoryNode = (DirectoryNode) idToNodeMap.get(node.getParentId());
        directoryNode.removeNode(node);
        idToNodeMap.remove(node.getId());
    }

    private String[] parseLine(String line) {
        String[] idAndName = new String[ARG_LIST_SIZE];
        String[] splitLine = line.split(":");
        idAndName[ID_INDEX] = splitLine[ID_EXTRACTION_INDEX];
        idAndName[PARENTID_INDEX] = splitLine[PARENTID_EXTRACTION_INDEX];
        idAndName[NAME_INDEX] = splitLine[NAME_EXTRACTION_INDEX];
        return idAndName;
    }

    private Node findNodeInMap(Integer id) {
        Node nodeInMap = idToNodeMap.get(id);
        if (nodeInMap == null) {
            throw new QVCSRuntimeException("Node not found. Node id: [" + id + "]");
        }
        return nodeInMap;
    }

    private Integer getNextNodeId() {
        return nodeId.getAndIncrement();
    }
}
