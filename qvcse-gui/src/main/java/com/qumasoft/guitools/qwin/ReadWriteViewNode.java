//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.AbstractProjectProperties;

/**
 * Read/write view node. Used for read/write views.
 * @author Jim Voris
 */
public class ReadWriteViewNode extends ViewTreeNode {
    private static final long serialVersionUID = -2370155679364038191L;

    /**
     * Creates new ReadWriteViewNode.
     * @param projectProperties the project properties.
     * @param viewName the view name.
     */
    public ReadWriteViewNode(AbstractProjectProperties projectProperties, final String viewName) {
        super(projectProperties, viewName);
    }

    @Override
    boolean isReadOnlyView() {
        return false;
    }

    @Override
    boolean isReadWriteView() {
        return true;
    }
}