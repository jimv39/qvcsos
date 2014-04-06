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

/**
 * Manage a String representation of the directory tree for a project/view. The plan is for this class to supercede the DirectoryContents class. We will use this class to capture
 * directory structure changes, including creates, deletes, moves, renames, etc.
 *
 * @author Jim Voris
 */
public class ProjectTree {

    private DirectoryNode projectRoot;

    /**
     * This is a map of directory id's to the parent directory of that directory id. This makes it so we don't need to maintain the parent directory id within the DirectoryNode
     * instance, and makes it fast to move a directory from one parent to a different parent.... at least that's the theory. The key is the directory id of a given DirectoryNode,
     * and the value is its <i>parent</i> directory node. For the root directory, the parent node is null -- a valid map entry.
     */
    Map<Integer, DirectoryNode> mapToParentNodes;

    public ProjectTree() {
    }


}
