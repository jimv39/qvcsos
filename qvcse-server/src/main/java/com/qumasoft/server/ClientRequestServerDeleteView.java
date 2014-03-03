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

import com.qumasoft.qvcslib.ClientRequestServerDeleteViewData;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseListViews;
import com.qumasoft.qvcslib.ServerResponseMessage;
import com.qumasoft.qvcslib.Utility;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Delete a view.
 * @author Jim Voris
 */
public class ClientRequestServerDeleteView implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestServerDeleteViewData request;

    /**
     * Creates a new instance of ClientRequestServerDeleteView.
     *
     * @param data command line arguments, etc.
     */
    public ClientRequestServerDeleteView(ClientRequestServerDeleteViewData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        try {
            LOGGER.log(Level.INFO, "User name: " + request.getUserName());

            returnObject = deleteView(response);
        } catch (QVCSShutdownException e) {
            // Re-throw this.
            throw e;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to login user " + request.getUserName(), null, null, null);
            returnObject = error;
        }
        return returnObject;
    }

    private ServerResponseInterface deleteView(ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject = null;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        String serverName = request.getServerName();
        if (0 == viewName.compareTo(QVCSConstants.QVCS_TRUNK_VIEW)) {
            ServerResponseMessage message = new ServerResponseMessage("You are not allowed to delete the Trunk view", projectName, viewName, null,
                    ServerResponseMessage.HIGH_PRIORITY);
            returnObject = message;
        } else if (viewHasChildren()) {
            // There are branches that have this view/branch as their parent branch... so we do not allow this
            // view to be deleted until child branches have been pruned.
            ServerResponseMessage message = new ServerResponseMessage("You are not allowed to delete a branch that has child branches.", projectName, viewName, null,
                    ServerResponseMessage.HIGH_PRIORITY);
            returnObject = message;
        } else {
            ProjectView projectView = ViewManager.getInstance().getView(projectName, viewName);
            if (projectView != null) {
                ViewManager.getInstance().removeView(projectView, response);

                // The reply is the new list of projects.
                ServerResponseListViews listViewsResponse = new ServerResponseListViews();
                listViewsResponse.setServerName(serverName);
                listViewsResponse.setProjectName(projectName);

                ClientRequestListClientViews.buildViewInfo(listViewsResponse, projectName);

                returnObject = listViewsResponse;

                // Add an entry to the server journal file.
                ActivityJournalManager.getInstance().addJournalEntry("Deleted view [" + viewName + "].");
            } else {
                // The project properties file is already gone...
                LOGGER.log(Level.WARNING, "Failed to delete non-existant view: [" + viewName + "].");
            }
        }
        return returnObject;
    }

    /**
     * Determine whether this view has any child branches -- i.e. other branches that use this view/branch as their parent. Most
     * branches will use the Trunk as their parent, but some may use another branch as their parent branch.
     *
     * @return true if this view has <i>any</i> child branches; false if there are no child branches.
     */
    private boolean viewHasChildren() {
        boolean retVal = false;
        Collection<ProjectView> views = ViewManager.getInstance().getViews(request.getProjectName());
        for (ProjectView projectView : views) {
            String branchParent = projectView.getRemoteViewProperties().getBranchParent();
            if (branchParent != null) {
                if (0 == branchParent.compareTo(request.getViewName())) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }
}
