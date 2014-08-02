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

import java.util.Objects;

/**
 * File node. Represent a file in a directory tree.
 * @author Jim Voris
 */
public class FileNode implements Node {

    private final Integer nodeId;
    private Integer parentId;
    private String nodeName;
    private static final int HASH_PRIME_1 = 7;
    private static final int HASH_PRIME_2 = 83;

    /**
     * Create a file node.
     * @param id the id of this file node.
     * @param parent the parent directory node.
     * @param name the name of this file node.
     */
    public FileNode(Integer id, DirectoryNode parent, String name) {
        this.nodeId = id;
        if (parent != null) {
            this.parentId = parent.getId();
        } else {
            this.parentId = null;
        }
        this.nodeName = name;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.FILE;
    }

    @Override
    public String asString() {
        return "<F id=:" + getId() + ": parentId=:" + getParentId() + ": name=:" + getName() + ":/>\n";
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
    public void setParentId(Integer id) {
        this.parentId = id;
    }

    @Override
    public String getName() {
        return this.nodeName;
    }

    @Override
    public void setName(String name) {
        this.nodeName = name;
    }

    @Override
    public int hashCode() {
        int hash = HASH_PRIME_1;
        hash = HASH_PRIME_2 * hash + Objects.hashCode(this.nodeId);
        hash = HASH_PRIME_2 * hash + Objects.hashCode(this.parentId);
        hash = HASH_PRIME_2 * hash + Objects.hashCode(this.nodeName);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileNode other = (FileNode) obj;
        if (!Objects.equals(this.nodeId, other.nodeId)) {
            return false;
        }
        if (!Objects.equals(this.parentId, other.parentId)) {
            return false;
        }
        return Objects.equals(this.nodeName, other.nodeName);
    }

}
