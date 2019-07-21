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
package com.qumasoft.server;

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import java.io.IOException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Archive directory manager factory for views.
 * @author Jim Voris
 */
public final class ArchiveDirManagerFactoryForViews {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDirManagerFactoryForViews.class);

    private static final ArchiveDirManagerFactoryForViews ARCHIVE_DIR_MANAGER_FACTORY_FOR_VIEWS = new ArchiveDirManagerFactoryForViews();

    /**
     * Creates a new instance of ArchiveDirManagerFactoryForViews.
     */
    private ArchiveDirManagerFactoryForViews() {
    }

    static ArchiveDirManagerFactoryForViews getInstance() {
        return ARCHIVE_DIR_MANAGER_FACTORY_FOR_VIEWS;
    }

    ArchiveDirManagerInterface getDirectoryManager(String serverName, String projectName, String viewName, String appendedPath, String userName,
            ServerResponseFactoryInterface response) throws QVCSException {
        ProjectView projectView = ViewManager.getInstance().getView(projectName, viewName);
        ArchiveDirManagerInterface directoryManager = null;
        if (projectView != null) {
            RemoteBranchProperties remoteViewProperties = projectView.getRemoteViewProperties();
            String localAppendedPath = Utility.convertToLocalPath(appendedPath);

            // Need to create different ArchiveDirManagers based on the view settings.
            // e.g. a readonly view vs. a read/write view.
            if (remoteViewProperties.getIsDateBasedBranchFlag()) {
                Date viewAnchorDate = remoteViewProperties.getDateBasedDate();

                directoryManager = new ArchiveDirManagerForReadOnlyDateBasedView(viewAnchorDate, remoteViewProperties, viewName, localAppendedPath, userName, response);
                LOGGER.info("ArchiveDirManagerFactoryForViews.getDirectoryManager: creating read-only date based ArchiveDirManager for directory [{}] for [{}] branch.",
                        localAppendedPath, viewName);
            } else if (remoteViewProperties.getIsOpaqueBranchFlag()) {
                String branchParent = remoteViewProperties.getBranchParent();
                validateBranchParent(projectName, viewName, branchParent);

                directoryManager = new ArchiveDirManagerForOpaqueBranch(branchParent, remoteViewProperties, viewName, localAppendedPath, userName, response);
                LOGGER.info("ArchiveDirManagerFactoryForViews.getDirectoryManager: creating opaque branch ArchiveDirManager for directory [{}] for [{}] branch",
                        localAppendedPath, viewName);
            } else if (remoteViewProperties.getIsTranslucentBranchFlag()) {
                String branchParent = remoteViewProperties.getBranchParent();
                validateBranchParent(projectName, viewName, branchParent);
                if (0 == localAppendedPath.compareToIgnoreCase(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                    try {
                        directoryManager = new ArchiveDirManagerForTranslucentBranchCemetery(projectName, viewName, remoteViewProperties, response);
                        LOGGER.info("ArchiveDirManagerFactoryForViews.getDirectoryManager: creating translucent branch ArchiveDirManager for directory [{}] for [{}] branch.",
                                localAppendedPath, viewName);
                    } catch (IOException e) {
                        LOGGER.error("Unable to create cemetery directory manager for [{}] // [{}]", projectName, viewName);
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                } else {
                    directoryManager = new ArchiveDirManagerForTranslucentBranch(branchParent, remoteViewProperties, viewName, localAppendedPath, userName, response);
                    LOGGER.info("ArchiveDirManagerFactoryForViews.getDirectoryManager: creating translucent branch ArchiveDirManager for directory [{}] for [{}] branch.",
                            localAppendedPath, viewName);
                }
            } else {
                LOGGER.warn("Unknown view type found for project: [{}] view: [{}]", projectName, viewName);
            }
        } else {
            LOGGER.info("ArchiveDirManagerFactoryForViews.getDirectoryManager: view not found for project: [{}] branch: [{}]", projectName, viewName);
        }
        return directoryManager;
    }

    /**
     * Make sure that the parent of the requested branch is either an opaque branch, or a translucent branch. Note that this check
     * is a runtime verification of the validity of the branch. It should always be okay, but we're putting it here as a way to
     * absolutely guarantee that the parent is one that will work.
     *
     * @param projectName the name of the project.
     * @param viewName the name of the view.
     * @param branchParent the name of the view's parent view.
     * @throws com.qumasoft.qvcslib.QVCSException thrown if the parent is not a valid parent.
     */
    private void validateBranchParent(String projectName, String viewName, String branchParent) throws QVCSException {
        if (0 != branchParent.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            // Need to make sure that the parent view is an opaque branch or a translucent branch.
            ProjectView parentView = ViewManager.getInstance().getView(projectName, branchParent);
            if (!parentView.getRemoteViewProperties().getIsTranslucentBranchFlag()
                    && !parentView.getRemoteViewProperties().getIsOpaqueBranchFlag()) {
                throw new QVCSException("Detected illegal parent branch type for branch view: [" + viewName + "]");
            }
        }
    }
}
