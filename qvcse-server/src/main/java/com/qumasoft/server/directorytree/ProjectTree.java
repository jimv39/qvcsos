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

    /**
     * Default constructor.
     */
    public ProjectTree() {
        idToNodeMap = new TreeMap<>();
    }

    Map<Integer, Node> getNodeMap() {
        return idToNodeMap;
    }

    /**
     * Create a String representation of this ProjectTree.
     * @return a String representation of this ProjectTree.
     */
    public String asString() {
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
    public void fromString(String asString) {
        String[] lines = asString.split("\n");
        for (String line : lines) {
            String[] idsAndName = parseLine(line);
            Integer id = Integer.parseInt(idsAndName[ID_INDEX]);
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
