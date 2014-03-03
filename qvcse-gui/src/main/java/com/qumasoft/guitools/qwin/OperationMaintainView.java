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

import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.ServerProperties;

/**
 * Maintain view operation.
 * @author Jim Voris
 */
public class OperationMaintainView {

    private final String projectName;
    private final String viewName;
    private final RemoteViewProperties remoteViewProperties;

    /**
     * Create a maintain view operation.
     * @param serverProps the server properties.
     * @param project the project name.
     * @param view the view name.
     * @param rvProperties the view properties.
     */
    public OperationMaintainView(ServerProperties serverProps, String project, String view, RemoteViewProperties rvProperties) {
        projectName = project;
        viewName = view;
        remoteViewProperties = rvProperties;
    }

    String getProjectName() {
        return projectName;
    }

    String getViewName() {
        return viewName;
    }

    RemoteViewProperties getRemoteViewProperties() {
        return remoteViewProperties;
    }

    void executeOperation() {
        MaintainViewPropertiesDialog maintainViewPropertiesDialog = new MaintainViewPropertiesDialog(QWinFrame.getQWinFrame(), true, getViewName(), getRemoteViewProperties());
        maintainViewPropertiesDialog.setVisible(true);
    }
}
