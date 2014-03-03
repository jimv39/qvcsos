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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ClientRequestListClientViewsData;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.qvcslib.ServedProjectProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseListViews;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * List client views.
 * @author Jim Voris
 */
public class ClientRequestListClientViews implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestListClientViewsData request;

    /**
     * Creates a new instance of ClientRequestListClientViews.
     *
     * @param data the request data.
     */
    public ClientRequestListClientViews(ClientRequestListClientViewsData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseListViews listViewsResponse = new ServerResponseListViews();

        listViewsResponse.setServerName(request.getServerName());
        listViewsResponse.setProjectName(request.getProjectName());
        buildViewInfo(listViewsResponse, request.getProjectName());

        return listViewsResponse;
    }

    /**
     * Build the list of views for a given project.
     * @param listViewsResponse the response object into which we populate the list of views.
     * @param projectName the project name.
     */
    public static void buildViewInfo(ServerResponseListViews listViewsResponse, String projectName) {
        // Get the views for this project...
        Collection<ProjectView> views = ViewManager.getInstance().getViews(projectName);

        String[] viewList;
        if (views != null) {
            viewList = new String[1 + views.size()];
        } else {
            viewList = new String[1];
        }
        viewList[0] = QVCSConstants.QVCS_TRUNK_VIEW;

        Properties[] properties = new Properties[viewList.length];

        try {
            ServedProjectProperties projectProperties = new ServedProjectProperties(projectName);
            RemoteViewProperties remoteViewProperties = new RemoteViewProperties(projectName, QVCSConstants.QVCS_TRUNK_VIEW, projectProperties.getProjectProperties());

            // TODO -- Figure out if this user has write access to the trunk view...
            remoteViewProperties.setIsReadOnlyViewFlag(false);
            properties[0] = remoteViewProperties.getProjectProperties();
        } catch (QVCSException e) {
            LOGGER.log(Level.WARNING, "Error finding served project names for project: '" + projectName + "'.");
        }

        int viewListIndex = 1;

        if (views != null) {
            Iterator<ProjectView> it = views.iterator();
            while (it.hasNext()) {
                // TODO -- Figure out whether this user should be able to even 'see'
                // this view.  I haven't decided for sure whether I'll supply this
                // level of granularity for authorization of see views or not. As
                // a first pass, I should just keep it simple, instead of gunning
                // for overkill.
                ProjectView projectView = it.next();
                viewList[viewListIndex] = projectView.getViewName();

                properties[viewListIndex] = projectView.getRemoteViewProperties().getProjectProperties();
                viewListIndex++;
            }
        }

        listViewsResponse.setViewList(viewList);
        listViewsResponse.setViewProperties(properties);
    }
}
