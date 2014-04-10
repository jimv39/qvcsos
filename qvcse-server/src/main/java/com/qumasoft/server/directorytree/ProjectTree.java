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

import java.util.Map;
import java.util.TreeMap;

/**
 * Manage a String representation of the directory tree for a project/view.
 *
 * @author Jim Voris
 */
public class ProjectTree {

    private final Map<Integer, Node> idToNodeMap;

    public ProjectTree() {
        idToNodeMap = new TreeMap<>();
    }

    Map<Integer, Node> getNodeMap() {
        return idToNodeMap;
    }

    public String asString() {
        StringBuilder asString = new StringBuilder();
        for (Node node : idToNodeMap.values()) {
            asString.append(node.asString());
        }
        return asString.toString();
    }

    public void fromString(String asString) {
        String[] lines = asString.split("\n");
        for (String line : lines) {
            String[] idsAndName = parseLine(line);
            Integer id = Integer.parseInt(idsAndName[0]);
            String name = idsAndName[2];
            if (line.startsWith("<D")) {
                Integer parentId;
                DirectoryNode directoryNode;
                try {
                    parentId = Integer.parseInt(idsAndName[1]);
                    directoryNode = new DirectoryNode(id, (DirectoryNode) idToNodeMap.get(parentId), name);
                } catch (NumberFormatException e) {
                    System.out.println("Number format exception for [" + idsAndName[1] + "]");
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
                    System.out.println("Number format exception for [" + idsAndName[1] + "]");
                    fileNode = new FileNode(id, null, name);
                }
                idToNodeMap.put(id, fileNode);
            }
        }
    }

    private String[] parseLine(String line) {
        String[] idAndName = new String[3];
        String[] splitLine = line.split(":");
        int idIndex = line.indexOf(':');
        int endIdIndex = line.substring(idIndex + 1).indexOf(':') + idIndex;
        String id = line.substring(idIndex + 1, endIdIndex + 1);
        int nameIndex = line.substring(endIdIndex + 2).indexOf(':') + endIdIndex + 2;
        int endNameIndex = line.substring(nameIndex + 1).indexOf(':') + nameIndex;
        String name = line.substring(nameIndex + 1, endNameIndex + 1);
        idAndName[0] = splitLine[1];
        idAndName[1] = splitLine[3];
        idAndName[2] = splitLine[5];
        return idAndName;
    }
}
