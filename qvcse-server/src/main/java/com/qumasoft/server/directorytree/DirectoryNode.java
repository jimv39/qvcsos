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

/**
 * Composite pattern representation of a directory node.
 * @author Jim Voris
 */
public class DirectoryNode implements Node {

    private final Integer nodeId;
    private final Integer parentId;
    private final String nodeName;
    private final Map<Integer, Node> idMap;
    private final Map<String, Node> nameMap;

    /**
     * Create a directory node.
     * @param id the id of this node.
     * @param parent the parent directory node.
     * @param name the name of this directory.
     */
    public DirectoryNode(Integer id, DirectoryNode parent, String name) {
        this.nodeId = id;
        if (parent != null) {
            this.parentId = parent.getId();
        } else {
            this.parentId = null;
        }
        this.nodeName = name;
        idMap = new TreeMap<>();
        nameMap = new TreeMap<>();
    }

    @Override
    public Integer getId() {
        return this.nodeId;
    }

    @Override
    public Integer getParentId() {
        return this.parentId;
    }

    @Override
    public String getName() {
        return this.nodeName;
    }


    @Override
    public NodeType getNodeType() {
        return NodeType.DIRECTORY;
    }

    /**
     * Add a node to this directory.
     * @param node the node to add.
     */
    public synchronized void addNode(Node node) {
        if (idMap.containsKey(node.getId())) {
            throw new QVCSRuntimeException("Directory [" + getName() + "] already contains [" + node.getName() + "]");
        }
        if (nameMap.containsKey(node.getName())) {
            throw new QVCSRuntimeException("Directory [" + getName() + "] already contains [" + node.getName() + "]");
        }
        idMap.put(node.getId(), node);
        nameMap.put(node.getName(), node);
    }

    @Override
    public synchronized String asString() {
        String o = "<D id=:" + this.getId() + ": parentId=:" + this.getParentId() + ": name=:" + this.getName() + ":/>\n";
        return o;
    }

    /**
     * Show this directory in a kind of tree presentation. This method is only for test/debugging purposes.
     * @param depth how deep are we in the directory tree. 0 is for the root of the tree, 1 is for 1 deeper than the root, etc.
     * @return a String representation of the directory tree.
     */
    public synchronized String asTree(int depth) {
        StringBuilder o = new StringBuilder();
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < depth; i++) {
            indent.append("  ");
        }
        o.append(indent.toString());
        String childIndent = indent.toString() + "  ";
        o.append(asString());
        // Show files first...
        for (Node node : idMap.values()) {
            if (node instanceof FileNode) {
                o.append(childIndent).append(node.asString());
            }
        }
        // Then show directories...
        for (Node node : idMap.values()) {
            if (node instanceof DirectoryNode) {
                DirectoryNode childDirectoryNode = (DirectoryNode) node;
                o.append(childDirectoryNode.asTree(depth + 1));
            }
        }
        return o.toString();
    }
}
