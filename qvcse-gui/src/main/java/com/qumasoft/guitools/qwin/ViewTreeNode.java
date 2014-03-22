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
 * View tree node. The node base class for view nodes.
 * @author Jim Voris
 */
public abstract class ViewTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private static final long serialVersionUID = 8488358470534894969L;

    private final AbstractProjectProperties projectProperties;
    private final String viewName;

    /**
     * Creates a new instance of ViewTreeNode.
     * @param projectProps the project properties.
     * @param view the view name.
     */
    public ViewTreeNode(AbstractProjectProperties projectProps, final String view) {
        super(projectProps);
        projectProperties = projectProps;
        viewName = view;
    }

    /**
     * Get the project name.
     * @return the project name.
     */
    public String getProjectName() {
        return projectProperties.getProjectName();
    }

    /**
     * Get the project properties.
     * @return the project properties.
     */
    public AbstractProjectProperties getProjectProperties() {
        return projectProperties;
    }

    /**
     * Get the view name.
     * @return the view name.
     */
    public String getViewName() {
        return viewName;
    }

    /**
     * Is this a read only view.
     * @return true if a read-only view; false otherwise.
     */
    abstract boolean isReadOnlyView();

    /**
     * Is this a read-write view.
     * @return true if a read-write view; false otherwise.
     */
    public abstract boolean isReadWriteView();
}
