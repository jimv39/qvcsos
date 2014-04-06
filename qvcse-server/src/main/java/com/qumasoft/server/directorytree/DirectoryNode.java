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
import java.util.Stack;
import java.util.TreeMap;

/**
 *
 * @author $Author$
 */
class DirectoryNode implements Node {

    private Integer nodeId;
    private String nodeName;
    Map<Integer, Node> idMap;
    Map<String, Node> nameMap;

    DirectoryNode(Integer id, String name) {
        this.nodeId = id;
        this.nodeName = name;
        idMap = new TreeMap<>();
        nameMap = new TreeMap<>();
    }

    DirectoryNode(String fromString) {
        this.nodeId = 0;
        this.nodeName = "root";
        idMap = new TreeMap<>();
        nameMap = new TreeMap<>();
        traverseString(fromString);
    }

    @Override
    public Integer getId() {
        return this.nodeId;
    }

    @Override
    public String getName() {
        return this.nodeName;
    }


    @Override
    public NodeType getNodeType() {
        return NodeType.DIRECTORY;
    }

    synchronized void addNode(Node node) {
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
        StringBuilder o = new StringBuilder("<D id=:" + this.getId() + ": name=:" + this.getName() + ":>\n");
        for (Node node : idMap.values()) {
            o.append(node.asString());
        }
        o.append("</D>\n");
        return o.toString();
    }
    private void traverseString(String asString) {
        String[] lines = asString.split("\n");
        int lineIndex = 0;
        DirectoryNode currentContainer = null;
        Stack<DirectoryNode> directoryStack = new Stack<>();
        for (String line : lines) {
            if (lineIndex == 0) {
                // First row... we're creating the root directory node.
                String[] idAndName = parseLine(line);
                this.nodeId = Integer.valueOf(idAndName[0]);
                this.nodeName = idAndName[1];
                currentContainer = this;
                directoryStack.push(this);
            } else {
                if (currentContainer != null) {
                    if (line.startsWith("<D")) {
                        String[] idAndName = parseLine(line);
                        DirectoryNode directoryNode = new DirectoryNode(Integer.valueOf(idAndName[0]), idAndName[1]);
                        currentContainer.addNode(directoryNode);
                        directoryStack.push(currentContainer);
                        currentContainer = directoryNode;
                    } else if (line.startsWith("<F")) {
                        String[] idAndName = parseLine(line);
                        FileNode fileNode = new FileNode(Integer.valueOf(idAndName[0]), idAndName[1]);
                        currentContainer.addNode(fileNode);
                    } else if (line.startsWith("</D")) {
                        currentContainer = directoryStack.pop();
                    }
                }
            }
            lineIndex++;
        }
    }

    private String[] parseLine(String line) {
        String[] idAndName = new String[2];
        int idIndex = line.indexOf(':');
        int endIdIndex = line.substring(idIndex + 1).indexOf(':') + idIndex;
        String id = line.substring(idIndex + 1, endIdIndex + 1);
        int nameIndex = line.substring(endIdIndex + 2).indexOf(':') + endIdIndex + 2;
        int endNameIndex = line.substring(nameIndex + 1).indexOf(':') + nameIndex;
        String name = line.substring(nameIndex + 1, endNameIndex + 1);
        idAndName[0] = id;
        idAndName[1] = name;
        return idAndName;
    }
}
