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

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Create nodes.
 * @author Jim Voris
 */
public class NodeFactory {

    private final ProjectTree projectTree;
    private final AtomicInteger nodeId;

    /**
     * Create a node factory.
     * @param p the ProjectTree for which we will create nodes.
     */
    public NodeFactory(ProjectTree p) {
        this.projectTree = p;
        this.nodeId = new AtomicInteger(0);
    }

    /**
     * Add a directory node.
     * @param parentId the directory's parent node id.
     * @param name the name of the directory.
     * @return the node id for the created directory node.
     */
    public Integer addDirectoryNode(Integer parentId, String name) {
        Integer id = nodeId.getAndIncrement();
        DirectoryNode directoryNode;
        if (parentId != null) {
            directoryNode = new DirectoryNode(id, (DirectoryNode) projectTree.getNodeMap().get(parentId), name);
        } else {
            directoryNode = new DirectoryNode(id, (DirectoryNode) null, name);
        }
        projectTree.getNodeMap().put(id, directoryNode);
        return id;
    }

    /**
     * Add a file node.
     * @param parentId the file's parent node id.
     * @param name the file name.
     * @return the node id for the created file node.
     */
    public Integer addFileNode(Integer parentId, String name) {
        Integer id = nodeId.getAndIncrement();
        FileNode fileNode = new FileNode(id, (DirectoryNode) projectTree.getNodeMap().get(parentId), name);
        projectTree.getNodeMap().put(id, fileNode);
        return id;
    }
}
