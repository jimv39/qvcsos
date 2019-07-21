/*   Copyright 2004-2019 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.server.clientrequest;

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateBranchData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListBranches;
import com.qumasoft.server.ActivityJournalManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ProjectView;
import com.qumasoft.server.QVCSShutdownException;
import com.qumasoft.server.ViewManager;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Create a view.
 * @author Jim Voris
 */
public class ClientRequestServerCreateView implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestServerCreateView.class);
    private final ClientRequestServerCreateBranchData request;

    /**
     * Creates a new instance of ClientRequestServerCreateView.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerCreateView(ClientRequestServerCreateBranchData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        try {
            LOGGER.info("User name: [{}]", request.getUserName());

            // Create a view.
            returnObject = createView();
        } catch (QVCSShutdownException e) {
            // Re-throw this.
            throw e;
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to login user " + request.getUserName(), null, null, null);
            returnObject = error;
        }
        return returnObject;
    }

    private ServerResponseInterface createView() {
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String viewName = request.getBranchName();
        try {
            // Make sure the view doesn't already exist.
            ProjectView projectView = ViewManager.getInstance().getView(projectName, viewName);
            if (projectView == null) {
                projectView = new ProjectView();
                projectView.setProjectName(projectName);
                projectView.setViewName(viewName);

                // The view gets most of its properties from the parent project...
                AbstractProjectProperties projectProperties = ArchiveDirManagerFactoryForServer.getInstance().getProjectProperties(request.getServerName(),
                        projectName, QVCSConstants.QVCS_TRUNK_BRANCH,
                        QVCSConstants.QVCS_SERVED_PROJECT_TYPE);
                RemoteBranchProperties remoteViewProperties = new RemoteBranchProperties(projectName, viewName, projectProperties.getProjectProperties());

                // Set the view specific properties.
                remoteViewProperties.setIsReadOnlyBranchFlag(request.getIsReadOnlyViewFlag());
                remoteViewProperties.setIsDateBasedBranchFlag(request.getIsDateBasedViewFlag());
                remoteViewProperties.setIsTranslucentBranchFlag(request.getIsTranslucentBranchFlag());
                remoteViewProperties.setIsOpaqueBranchFlag(request.getIsOpaqueBranchFlag());

                if (request.getIsDateBasedViewFlag()) {
                    remoteViewProperties.setDateBaseDate(request.getDateBasedDate());
                    remoteViewProperties.setDateBasedBranch(request.getDateBasedViewBranch());
                } else if (request.getIsTranslucentBranchFlag() || request.getIsOpaqueBranchFlag()) {
                    remoteViewProperties.setBranchParent(request.getParentBranchName());
                    remoteViewProperties.setBranchDate(new Date());
                }

                projectView.setRemoteViewProperties(remoteViewProperties);

                // And add this view to the collection of views that we know about.
                ViewManager.getInstance().addView(projectView);

                // The reply is the new list of views.
                ServerResponseListBranches listViewsResponse = new ServerResponseListBranches();
                listViewsResponse.setServerName(request.getServerName());
                listViewsResponse.setProjectName(projectName);

                ClientRequestListClientViews.buildViewInfo(listViewsResponse, projectName);

                returnObject = listViewsResponse;

                // Add an entry to the server journal file.
                ActivityJournalManager.getInstance().addJournalEntry("Created new view named '" + viewName + "'.");
            } else {
                // The view already exists... don't create it again.
                LOGGER.info("View: [" + viewName + "] already exists.");
            }
        } catch (QVCSException e) {
            LOGGER.warn("Caught exception: " + e.getClass().toString() + " : " + e.getLocalizedMessage());

            // Return an error.
            ServerResponseError error = new ServerResponseError("Caught exception trying change project properties: " + e.getLocalizedMessage(), projectName, viewName, null);
            returnObject = error;
        }
        return returnObject;
    }
}
