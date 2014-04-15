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
 * @author Jim Voris
 */

/**
 * Define the interface that must be implemented by a 'node'. A node, in this context is a file or directory, and is used in the GOF composite pattern to represent a
 * directory tree.
 * @author JimVoris
 */
public interface Node {

    /**
     * Get the node id.
     * @return the node id.
     */
    Integer getId();

    /**
     * Get the node's parent node id.
     * @return the node's parent node id.
     */
    Integer getParentId();

    /**
     * Get the name of the node.
     * @return the name of the node.
     */
    String getName();

    /**
     * Get the type of node.
     * @return the type of node.
     */
    NodeType getNodeType();

    /**
     * Get a String representation of the node.
     * @return a String representation of the node.
     */
    String asString();
}
