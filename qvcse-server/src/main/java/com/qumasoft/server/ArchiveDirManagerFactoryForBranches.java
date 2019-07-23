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
 * Archive directory manager factory for branches.
 * @author Jim Voris
 */
public final class ArchiveDirManagerFactoryForBranches {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDirManagerFactoryForBranches.class);

    private static final ArchiveDirManagerFactoryForBranches ARCHIVE_DIR_MANAGER_FACTORY_FOR_BRANCHES = new ArchiveDirManagerFactoryForBranches();

    /**
     * Creates a new instance of ArchiveDirManagerFactoryForBranches.
     */
    private ArchiveDirManagerFactoryForBranches() {
    }

    static ArchiveDirManagerFactoryForBranches getInstance() {
        return ARCHIVE_DIR_MANAGER_FACTORY_FOR_BRANCHES;
    }

    ArchiveDirManagerInterface getDirectoryManager(String serverName, String projectName, String branchName, String appendedPath, String userName,
            ServerResponseFactoryInterface response) throws QVCSException {
        ProjectBranch projectBranch = BranchManager.getInstance().getBranch(projectName, branchName);
        ArchiveDirManagerInterface directoryManager = null;
        if (projectBranch != null) {
            RemoteBranchProperties remoteBranchProperties = projectBranch.getRemoteBranchProperties();
            String localAppendedPath = Utility.convertToLocalPath(appendedPath);

            // Need to create different ArchiveDirManagers based on the branch settings.
            // e.g. a readonly branch vs. a read/write branch.
            if (remoteBranchProperties.getIsDateBasedBranchFlag()) {
                Date branchAnchorDate = remoteBranchProperties.getDateBasedDate();

                directoryManager = new ArchiveDirManagerForReadOnlyDateBasedBranch(branchAnchorDate, remoteBranchProperties, branchName, localAppendedPath, userName, response);
                LOGGER.info("ArchiveDirManagerFactoryForBranches.getDirectoryManager: creating read-only date based ArchiveDirManager for directory [{}] for [{}] branch.",
                        localAppendedPath, branchName);
            } else if (remoteBranchProperties.getIsOpaqueBranchFlag()) {
                String branchParent = remoteBranchProperties.getBranchParent();
                validateBranchParent(projectName, branchName, branchParent);

                directoryManager = new ArchiveDirManagerForOpaqueBranch(branchParent, remoteBranchProperties, branchName, localAppendedPath, userName, response);
                LOGGER.info("ArchiveDirManagerFactoryForBranches.getDirectoryManager: creating opaque branch ArchiveDirManager for directory [{}] for [{}] branch",
                        localAppendedPath, branchName);
            } else if (remoteBranchProperties.getIsFeatureBranchFlag()) {
                String branchParent = remoteBranchProperties.getBranchParent();
                validateBranchParent(projectName, branchName, branchParent);
                if (0 == localAppendedPath.compareToIgnoreCase(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                    try {
                        directoryManager = new ArchiveDirManagerForFeatureBranchCemetery(projectName, branchName, remoteBranchProperties, response);
                        LOGGER.info("ArchiveDirManagerFactoryForBranches.getDirectoryManager: creating feature branch ArchiveDirManager for directory [{}] for [{}] branch.",
                                localAppendedPath, branchName);
                    } catch (IOException e) {
                        LOGGER.error("Unable to create cemetery directory manager for [{}] // [{}]", projectName, branchName);
                        LOGGER.error(e.getLocalizedMessage(), e);
                    }
                } else {
                    directoryManager = new ArchiveDirManagerForFeatureBranch(branchParent, remoteBranchProperties, branchName, localAppendedPath, userName, response);
                    LOGGER.info("ArchiveDirManagerFactoryForBranches.getDirectoryManager: creating feature branch ArchiveDirManager for directory [{}] for [{}] branch.",
                            localAppendedPath, branchName);
                }
            } else {
                LOGGER.warn("Unknown branch type found for project: [{}] branch: [{}]", projectName, branchName);
            }
        } else {
            LOGGER.info("ArchiveDirManagerFactoryForBranches.getDirectoryManager: branch not found for project: [{}] branch: [{}]", projectName, branchName);
        }
        return directoryManager;
    }

    /**
     * Make sure that the parent of the requested branch is either an opaque branch, or a feature branch. Note that this check
     * is a runtime verification of the validity of the branch. It should always be okay, but we're putting it here as a way to
     * absolutely guarantee that the parent is one that will work.
     *
     * @param projectName the name of the project.
     * @param branchName the name of the branch.
     * @param branchParent the name of the branch's parent branch.
     * @throws com.qumasoft.qvcslib.QVCSException thrown if the parent is not a valid parent.
     */
    private void validateBranchParent(String projectName, String branchName, String branchParent) throws QVCSException {
        if (0 != branchParent.compareTo(QVCSConstants.QVCS_TRUNK_BRANCH)) {
            // Need to make sure that the parent branch is an opaque branch or a feature branch.
            ProjectBranch parentBranch = BranchManager.getInstance().getBranch(projectName, branchParent);
            if (!parentBranch.getRemoteBranchProperties().getIsFeatureBranchFlag()
                    && !parentBranch.getRemoteBranchProperties().getIsOpaqueBranchFlag()) {
                throw new QVCSException("Detected illegal parent branch type for branch: [" + branchName + "]");
            }
        }
    }
}
