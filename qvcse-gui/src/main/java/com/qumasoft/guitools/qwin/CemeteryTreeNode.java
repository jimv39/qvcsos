/*
 * Copyright 2022-2023 Jim Voris.
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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemotePropertiesBaseClass;

/**
 *
 * @author Jim Voris
 */
public class CemeteryTreeNode extends javax.swing.tree.DefaultMutableTreeNode {
    private final String projectName;
    private final String branchName;
    private final RemotePropertiesBaseClass projectProperties;

    /**
     * Create a cemetery tree node.
     * @param project the project name.
     * @param branch the branch name.
     * @param projProperties the project properties.
     */
    public CemeteryTreeNode(String project, String branch, RemotePropertiesBaseClass projProperties) {
        super("");
        this.projectName = project;
        this.branchName = branch;
        this.projectProperties = projProperties;
    }

    /**
     * Get the project name.
     *
     * @return the branch name.
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Get the branch name.
     *
     * @return the branch name.
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Get the project properties.
     *
     * @return the project properties.
     */
    public RemotePropertiesBaseClass getProjectProperties() {
        return projectProperties;
    }

    /**
     * Get the appended path.
     *
     * @return the appended path.
     */
    public String getAppendedPath() {
        return QVCSConstants.QVCSOS_CEMETERY_FAKE_APPENDED_PATH;
    }

    @Override
    public String toString() {
        // It's always named Cemetery.
        return "Cemetery";
    }
}
