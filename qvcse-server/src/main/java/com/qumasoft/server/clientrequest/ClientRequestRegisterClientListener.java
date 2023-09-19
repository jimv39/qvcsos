/*   Copyright 2004-2022 Jim Voris
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

import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.requestdata.ClientRequestRegisterClientListenerData;
import com.qumasoft.qvcslib.response.AbstractServerResponse;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseProjectControl;
import com.qumasoft.qvcslib.response.ServerResponseRegisterClientListener;
import com.qumasoft.server.NotificationManager;
import com.qumasoft.server.RolePrivilegesManager;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.ServerTransactionManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesForReadOnlyBranchesDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesForReleaseBranchesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.TagDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesForReadOnlyBranchesDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesForReleaseBranchesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.dataaccess.impl.TagDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.Project;
import com.qvcsos.server.datamodel.Tag;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request register client listener.
 *
 * @author Jim Voris
 */
public class ClientRequestRegisterClientListener extends AbstractClientRequest {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestRegisterClientListener.class);
    private boolean showCemeteryFlag = false;
    private boolean showBranchArchivesFlag = false;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    /**
     * Creates a new instance of ClientRequestRegisterClientListener.
     *
     * @param data the command line data, etc.
     */
    public ClientRequestRegisterClientListener(ClientRequestRegisterClientListenerData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        setRequest(data);
    }

    /**
     * Get the show cemetery flag.
     *
     * @return the show cemetery flag.
     */
    public boolean getShowCemeteryFlag() {
        return showCemeteryFlag;
    }

    /**
     * Set the show cemetery flag.
     *
     * @param flag the show cemetery flag.
     */
    public void setShowCemeteryFlag(boolean flag) {
        showCemeteryFlag = flag;
    }

    /**
     * Get the show branch archives flag.
     *
     * @return the show branch archives flag.
     */
    public boolean getShowBranchArchivesFlag() {
        return showBranchArchivesFlag;
    }

    /**
     * Set the show branch archives flag.
     *
     * @param flag the show branch archives flag.
     */
    public void setShowBranchArchivesFlag(boolean flag) {
        showBranchArchivesFlag = flag;
    }

    /**
     * Perform the operation... which results in sending information about the requested directory back to the client.
     *
     * @param userName the user name.
     * @param response the object used to identify the client.
     * @return a response that will get sent back to the client.
     */
    @Override
    public AbstractServerResponse execute(String userName, ServerResponseFactoryInterface response) {
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        sourceControlBehaviorManager.setUserAndResponse(userName, response);
        ServerResponseRegisterClientListener serverResponse;
        AbstractServerResponse returnObject = null;
        String projectName = getRequest().getProjectName();
        String branchName = getRequest().getBranchName();
        String appendedPath = getRequest().getAppendedPath();
        Integer transactionId = null;
        try {
            databaseManager.getConnection();
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, branchName, appendedPath);
            FunctionalQueriesDAOImpl functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
            DirectoryCoordinateIds ids = functionalQueriesDAO.getDirectoryCoordinateIds(directoryCoordinate);

            ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
            Project project = projectDAO.findByProjectName(projectName);

            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), branchName);
            Integer parentBranchId = branch.getParentBranchId();
            List<SkinnyLogfileInfo> skinnyArray = null;
            switch (branch.getBranchTypeId()) {
                case QVCSConstants.QVCS_TRUNK_BRANCH_TYPE:
                case QVCSConstants.QVCS_FEATURE_BRANCH_TYPE:
                    skinnyArray = buildResponseForTrunkOrFeatureBranch(branch, ids);
                    break;
                case QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE:
                    skinnyArray = buildResponseForReadOnlyBranch(branch, ids);
                    break;
                case QVCSConstants.QVCS_RELEASE_BRANCH_TYPE:
                    skinnyArray = buildResponseForReleaseBranch(branch, ids);
                    break;
                default:
                    break;
            }

            LOGGER.info("ClientRequestRegisterClientListener.execute project: [{}], branch: [{}], appendedPath: [{}]", projectName, branchName, appendedPath);
            NotificationManager.getNotificationManager().addDirectoryCoordinateListener(response, directoryCoordinate, skinnyArray);

            // Add notification listeners for any parent branches
            NotificationManager.getNotificationManager().addNotificationListenersForParentBranches(response, directoryCoordinate, ids);

            serverResponse = new ServerResponseRegisterClientListener();
            if (skinnyArray != null) {
                skinnyArray.forEach(skinnyInfo -> {
                    serverResponse.addLogfileInformation(skinnyInfo);
                });
            }
            serverResponse.setAppendedPath(appendedPath);
            serverResponse.setProjectName(projectName);
            serverResponse.setBranchName(branchName);
            serverResponse.setSyncToken(getRequest().getSyncToken());
            if (ids != null) {
                serverResponse.setBranchId(ids.getBranchId());
                serverResponse.setParentBranchId(parentBranchId);
            }
            returnObject = serverResponse;

            // Let the client know about any sub-directories.
            if (0 == appendedPath.length()) {
                transactionId = ServerTransactionManager.getInstance().sendBeginTransaction(response);
                switch (branch.getBranchTypeId()) {
                    case QVCSConstants.QVCS_TRUNK_BRANCH_TYPE:
                    case QVCSConstants.QVCS_FEATURE_BRANCH_TYPE:
                        sendListOfSubDirectoriesForTrunkOrFeatureBranch(ids, parentBranchId, response);
                        break;
                    case QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE:
                        TagDAO tagDAO = new TagDAOImpl(schemaName);
                        Tag tag = tagDAO.findById(branch.getTagId());
                        Integer boundingCommitId = tag.getCommitId();
                        sendListOfSubDirectoriesForReadOnlyBranch(branch, ids, boundingCommitId, parentBranchId, response);
                        break;
                    case QVCSConstants.QVCS_RELEASE_BRANCH_TYPE:
                        sendListOfSubDirectoriesForReleaseBranch(branch, ids, branch.getCommitId(), parentBranchId, response);
                    default:
                        break;
                }

                // If the user has cemetery privileges, we need to send the project control message for the cemetery.
                if (RolePrivilegesManager.getInstance().isUserPrivileged(projectName, userName, RolePrivilegesManager.SHOW_CEMETERY)) {
                    LOGGER.info("We should show the cemetery!");
                    ServerResponseProjectControl responseControlMsg = new ServerResponseProjectControl();
                    responseControlMsg.setShowCemeteryFlag(true);
                    responseControlMsg.setServerName(response.getServerName());
                    responseControlMsg.setProjectName(getRequest().getProjectName());
                    responseControlMsg.setBranchName(getRequest().getBranchName());

                    // Send the project control message to create the Cemetery node on the client.
                    response.createServerResponse(responseControlMsg);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Return a command error.
            ServerResponseError error = new ServerResponseError("Caught exception trying to register client listener for [" + appendedPath + "]",
                    projectName, branchName, appendedPath);
            response.createServerResponse(error);
        } finally {
            if (transactionId != null) {
                ServerTransactionManager.getInstance().sendEndTransaction(response, transactionId);
            }
        }
        sourceControlBehaviorManager.clearThreadLocals();
        if (returnObject != null) {
            returnObject.setSyncToken(getRequest().getSyncToken());
        }
        return returnObject;
    }

    /**
     * Send the list of sub directories for the branch.
     *
     * @param ids the directory coordinate ids.
     * @param parentBranchId the parent branch id.
     * @param response the object that identifies the client.
     */
    private void sendListOfSubDirectoriesForTrunkOrFeatureBranch(DirectoryCoordinateIds ids, Integer parentBranchId, ServerResponseFactoryInterface response) {
        // Find all the project/branch sub-directories...
        List<String> segments = new ArrayList<>();
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);

        List<Branch> branchArray = functionalQueriesDAO.getBranchAncestryList(ids.getBranchId());

        // Add all the child directories.
        addChildDirectoriesForTrunkOrFeatureBranch(ids.getBranchId(), parentBranchId, ids.getDirectoryLocationId(), branchArray, segments, response);
    }

    private void sendListOfSubDirectoriesForReadOnlyBranch(Branch branch, DirectoryCoordinateIds ids, int boundingCommitId, Integer parentBranchId,
            ServerResponseFactoryInterface response) {
        // Find all the project/branch sub-directories...
        List<String> segments = new ArrayList<>();
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);

        List<Branch> branchArray = functionalQueriesDAO.getBranchAncestryList(parentBranchId);

        // Add all the child directories.
        addChildDirectoriesForReadOnlyBranch(branch, parentBranchId, boundingCommitId, ids.getDirectoryLocationId(), branchArray, segments, response);
    }

    private void sendListOfSubDirectoriesForReleaseBranch(Branch branch, DirectoryCoordinateIds ids, Integer boundingCommitId, Integer parentBranchId,
            ServerResponseFactoryInterface response) {
        // Find all the project/branch sub-directories...
        List<String> segments = new ArrayList<>();
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);

        List<Branch> branchArray = functionalQueriesDAO.getBranchAncestryList(ids.getBranchId());

        // Add all the child directories.
        addChildDirectoriesForReleaseBranch(branch, parentBranchId, boundingCommitId, ids.getDirectoryLocationId(), branchArray, segments, response);
    }

    /**
     * Add subdirectories.
     *
     * @param branchId the branch id.
     * @param parentBranchId the parent branch id.
     * @param parentDirectoryLocationId the parent directory_location id.
     * @param branchArray the array of branch ancestry. Usually, this will have just one element.
     * @param segments the directory segments that we're interested in.
     * @param response the object that identifies the client.
     */
    private void addChildDirectoriesForTrunkOrFeatureBranch(Integer branchId, Integer parentBranchId, Integer parentDirectoryLocationId, List<Branch> branchArray,
            List<String> segments, ServerResponseFactoryInterface response) {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<DirectoryLocation> directoryLocationList = functionalQueriesDAO.findChildDirectoryLocations(branchArray, parentDirectoryLocationId);
        if (directoryLocationList != null) {
            for (DirectoryLocation directoryLocation : directoryLocationList) {
                segments.add(directoryLocation.getDirectorySegmentName());
                String[] stringSegments = new String[segments.size()];
                for (int j = 0; j < stringSegments.length; j++) {
                    stringSegments[j] = segments.get(j);
                }
                ServerResponseProjectControl responseControlMsg = new ServerResponseProjectControl();
                responseControlMsg.setAddFlag(true);
                responseControlMsg.setServerName(response.getServerName());
                responseControlMsg.setProjectName(getRequest().getProjectName());
                responseControlMsg.setBranchName(getRequest().getBranchName());
                responseControlMsg.setBranchId(branchId);
                responseControlMsg.setParentBranchId(parentBranchId);
                responseControlMsg.setDirectorySegments(stringSegments);

                // Let the client know about this child directory.
                response.createServerResponse(responseControlMsg);
                addChildDirectoriesForTrunkOrFeatureBranch(branchId, parentBranchId, directoryLocation.getId(), branchArray, segments, response);

                // Remove the last segment (the one we just added).
                segments.remove(segments.size() - 1);
            }
        }
    }

    private void addChildDirectoriesForReadOnlyBranch(Branch branch, Integer parentBranchId, int boundingCommitId, int parentDirectoryLocationId, List<Branch> branchArray,
            List<String> segments, ServerResponseFactoryInterface response) {

        FunctionalQueriesForReadOnlyBranchesDAO functionalQueriesForReadOnlyBranchesDAO = new FunctionalQueriesForReadOnlyBranchesDAOImpl(schemaName);
        List<DirectoryLocation> directoryLocationList = functionalQueriesForReadOnlyBranchesDAO.findChildDirectoryLocationsForReadOnlyBranch(branch,
                branchArray, boundingCommitId, parentDirectoryLocationId);
        if (directoryLocationList != null) {
            for (DirectoryLocation directoryLocation : directoryLocationList) {
                segments.add(directoryLocation.getDirectorySegmentName());
                String[] stringSegments = new String[segments.size()];
                for (int j = 0; j < stringSegments.length; j++) {
                    stringSegments[j] = segments.get(j);
                }
                ServerResponseProjectControl responseControlMsg = new ServerResponseProjectControl();
                responseControlMsg.setAddFlag(true);
                responseControlMsg.setServerName(response.getServerName());
                responseControlMsg.setProjectName(getRequest().getProjectName());
                responseControlMsg.setBranchName(getRequest().getBranchName());
                responseControlMsg.setBranchId(branch.getId());
                responseControlMsg.setParentBranchId(parentBranchId);
                responseControlMsg.setDirectorySegments(stringSegments);

                // Let the client know about this child directory.
                response.createServerResponse(responseControlMsg);
                addChildDirectoriesForReadOnlyBranch(branch, parentBranchId, boundingCommitId, directoryLocation.getId(), branchArray, segments, response);

                // Remove the last segment (the one we just added).
                segments.remove(segments.size() - 1);
            }
        }
    }

    private void addChildDirectoriesForReleaseBranch(Branch branch, Integer parentBranchId, Integer boundingCommitId, int parentDirectoryLocationId, List<Branch> branchArray,
            List<String> segments, ServerResponseFactoryInterface response) {

        FunctionalQueriesForReleaseBranchesDAO functionalQueriesForReleaseBranchesDAO = new FunctionalQueriesForReleaseBranchesDAOImpl(schemaName);
        List<DirectoryLocation> directoryLocationList = functionalQueriesForReleaseBranchesDAO.findChildDirectoryLocationsForBranch(branch,
                branchArray, boundingCommitId, parentDirectoryLocationId);
        if (directoryLocationList != null) {
            for (DirectoryLocation directoryLocation : directoryLocationList) {
                segments.add(directoryLocation.getDirectorySegmentName());
                String[] stringSegments = new String[segments.size()];
                for (int j = 0; j < stringSegments.length; j++) {
                    stringSegments[j] = segments.get(j);
                }
                ServerResponseProjectControl responseControlMsg = new ServerResponseProjectControl();
                responseControlMsg.setAddFlag(true);
                responseControlMsg.setServerName(response.getServerName());
                responseControlMsg.setProjectName(getRequest().getProjectName());
                responseControlMsg.setBranchName(getRequest().getBranchName());
                responseControlMsg.setBranchId(branch.getId());
                responseControlMsg.setParentBranchId(parentBranchId);
                responseControlMsg.setDirectorySegments(stringSegments);

                // Let the client know about this child directory.
                response.createServerResponse(responseControlMsg);
                addChildDirectoriesForReleaseBranch(branch, parentBranchId, boundingCommitId, directoryLocation.getId(), branchArray, segments, response);

                // Remove the last segment (the one we just added).
                segments.remove(segments.size() - 1);
            }
        }
    }

    /**
     * Build a list of SkinnyLogfileInfo objects for the given
     * project/branch/directory.
     *
     * @param branch the branch we are interested in.
     * @param ids project, branch, directory, and directory_location ids.
     * @return a list of SkinnyLogfileInfo objects for the given directory.
     */
    private List<SkinnyLogfileInfo> buildResponseForTrunkOrFeatureBranch(Branch branch, DirectoryCoordinateIds ids) {
        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();
        if (ids != null && ids.getDirectoryLocationId() != null) {
            if (ids.getDirectoryLocationId() == -1) {
                skinnyList = buildResponseForCemetery(branch);
            } else {
                FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
                skinnyList = functionalQueriesDAO.getSkinnyLogfileInfo(ids.getBranchId(), ids.getDirectoryId());
            }
        }
        return skinnyList;
    }

    private List<SkinnyLogfileInfo> buildResponseForReadOnlyBranch(Branch branch, DirectoryCoordinateIds ids) {
        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();
        if (ids != null) {

            TagDAO tagDAO = new TagDAOImpl(schemaName);
            Tag tag = tagDAO.findById(branch.getTagId());
            Integer boundingCommitId = tag.getCommitId();

            FunctionalQueriesForReadOnlyBranchesDAO functionalQueriesForReadOnlyBranchesDAO = new FunctionalQueriesForReadOnlyBranchesDAOImpl(schemaName);
            skinnyList = functionalQueriesForReadOnlyBranchesDAO.getSkinnyLogfileInfoForReadOnlyBranch(branch, boundingCommitId, ids);
        }
        return skinnyList;
    }

    private List<SkinnyLogfileInfo> buildResponseForReleaseBranch(Branch branch, DirectoryCoordinateIds ids) {
        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();
        FunctionalQueriesForReleaseBranchesDAO functionalQueriesForReleaseBranchesDAO = new FunctionalQueriesForReleaseBranchesDAOImpl(schemaName);
        if (ids != null) {
            if (ids.getDirectoryLocationId() == -1) {
                skinnyList = buildResponseForCemetery(branch);
            } else {
                skinnyList = functionalQueriesForReleaseBranchesDAO.getSkinnyLogfileInfoForReleaseBranches(branch, branch.getCommitId(), ids);
            }
        }
        return skinnyList;
    }

    private List<SkinnyLogfileInfo> buildResponseForCemetery(Branch branch) {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<SkinnyLogfileInfo> skinnyList = functionalQueriesDAO.getSkinnyLogfileInfoForCemetery(branch);
        return skinnyList;
    }
}
