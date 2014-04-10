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

/**
 *
 * @author $Author$
 */
public class FileNode implements Node {

    private final Integer nodeId;
    private final Integer parentId;
    private final String nodeName;

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
    public String getName() {
        return this.nodeName;
    }

}
