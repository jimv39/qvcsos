/*   Copyright 2004-2023 Jim Voris
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

import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.ZlibCompressor;
import com.qumasoft.qvcslib.requestdata.ClientRequestAddDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestAddUserPropertyData;
import com.qumasoft.qvcslib.requestdata.ClientRequestApplyTagData;
import com.qumasoft.qvcslib.requestdata.ClientRequestChangePasswordData;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckInData;
import com.qumasoft.qvcslib.requestdata.ClientRequestClientData;
import com.qumasoft.qvcslib.requestdata.ClientRequestCreateArchiveData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetAllLogfileInfoData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetBriefCommitInfoListData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetCommitListForMoveableTagData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetForVisualCompareData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetInfoForMergeData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetLogfileInfoData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetMostRecentActivityData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionForCompareData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetTagsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetTagsInfoData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetUserCommitCommentsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestHeartBeatData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientBranchesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientProjectsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListFilesToPromoteData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLoginData;
import com.qumasoft.qvcslib.requestdata.ClientRequestMoveFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestOperationDataInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestRegisterClientListenerData;
import com.qumasoft.qvcslib.requestdata.ClientRequestRenameData;
import com.qumasoft.qvcslib.requestdata.ClientRequestResolveConflictFromParentBranchData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerAddUserData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerAssignUserRolesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateBranchData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateProjectData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteBranchData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteProjectData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteRoleData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerGetRoleNamesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerGetRolePrivilegesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListProjectUsersData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListProjectsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListUserRolesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerListUsersData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerMaintainProjectData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerRemoveUserData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerShutdownData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerUpdatePrivilegesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestTransactionBeginData;
import com.qumasoft.qvcslib.requestdata.ClientRequestTransactionEndData;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnDeleteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestUpdateTagCommitIdData;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.server.RoleManager;
import com.qumasoft.server.RolePrivilegesManager;
import com.qumasoft.server.ServerAction;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client request factory.
 *
 * @author Jim Voris
 */
public class ClientRequestFactory {

    private static final String LOG_UNEXPECTED_CLIENT_REQUEST_OBJECT =  "Unexpected client request object: [{}]";
    private static final String UNEXPECTED_CLIENT_REQUEST_OBJECT =  "Unexpected client request object: ";
    private static final String UNKNOWN_OPERATION_REQUEST = "Unknown operation request";

    // Create our logger object.
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestFactory.class);
    private final Object syncObject;
    private ObjectInputStream objectInputStreamMember;
    private boolean isUserLoggedInFlagMember;
    private boolean clientVersionMatchesFlagMember;
    private String userNameMember;

    /**
     * Creates new ClientRequestFactory.
     *
     * @param inStream stream from which we read the client request.
     */
    public ClientRequestFactory(java.io.InputStream inStream) {
        syncObject = new Object();
        try {
            objectInputStreamMember = new ObjectInputStream(inStream);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            objectInputStreamMember = null;
        }
    }

    /**
     * Create the client request.
     *
     * @param responseFactory a path back to the client (for reporting errors, for example).
     * @return the object that will do the requested work.
     */
    public ClientRequestInterface createClientRequest(ServerResponseFactory responseFactory) {
        ClientRequestInterface returnObject = null;
        try {
            Object object;
            synchronized (syncObject) {
                object = objectInputStreamMember.readObject();
                if (object instanceof byte[]) {
                    object = decompress(object);
                }
            }

            if (object instanceof ClientRequestChangePasswordData) {
                // Don't need to be logged in to change your password.
                ClientRequestChangePasswordData requestData = (ClientRequestChangePasswordData) object;
                LOGGER.debug("Received: [{}] [{}]", requestData.getOperationType(), requestData.getSyncToken());
                returnObject = new ClientRequestChangePassword(requestData);
            } else if (object instanceof ClientRequestTransactionBeginData) {
                ClientRequestTransactionBeginData requestData = (ClientRequestTransactionBeginData) object;
                LOGGER.debug("Received: [{}] [{}]", requestData.getOperationType(), requestData.getSyncToken());
                returnObject = new ClientRequestTransactionBegin(requestData);
                LOGGER.debug(">>>>>>>>>>>>>>>>>>>  Begin Transaction: [" + requestData.getTransactionID() + "] >>>>>>>>>>>>>>>>>>>");
            } else if (object instanceof ClientRequestTransactionEndData) {
                ClientRequestTransactionEndData requestData = (ClientRequestTransactionEndData) object;
                LOGGER.debug("Received: [{}] [{}]", requestData.getOperationType(), requestData.getSyncToken());
                returnObject = new ClientRequestTransactionEnd(requestData);
                LOGGER.debug("<<<<<<<<<<<<<<<<<<<  End Transaction: [" + requestData.getTransactionID() + "] <<<<<<<<<<<<<<<<<<<");
            } else if (object instanceof ClientRequestHeartBeatData) {
                ClientRequestHeartBeatData heartBeatData = (ClientRequestHeartBeatData) object;
                LOGGER.debug("Received: [{}] [{}]", heartBeatData.getOperationType(), heartBeatData.getSyncToken());
                returnObject = new ClientRequestHeartBeat(heartBeatData);
            } else if (getIsUserLoggedIn() && getClientVersionMatchesFlag()) {
                if (object instanceof ClientRequestOperationDataInterface) {
                    // The user is logged in.. process the request.
                    ClientRequestOperationDataInterface request = (ClientRequestOperationDataInterface) object;
                    ClientRequestDataInterface.RequestOperationType operationType = request.getOperationType();
                    switch (operationType) {
                        case LIST_CLIENT_PROJECTS:
                        case LIST_CLIENT_BRANCHES:
                        case LIST_PROJECTS:
                        case LIST_USERS:
                        case CHANGE_USER_PASSWORD:
                        case GET_REVISION:
                        case GET_DIRECTORY:
                        case GET_FOR_VISUAL_COMPARE:
                        case GET_REVISION_FOR_COMPARE:
                        case GET_USER_COMMIT_COMMENTS:
                        case GET_COMMIT_LIST_FOR_MOVEABLE_TAG_READ_ONLY_BRANCHES:
                        case GET_BRIEF_COMMIT_LIST:
                        case UPDATE_TAG_COMMIT_ID:
                        case GET_TAGS:
                        case GET_TAGS_INFO:
                        case APPLY_TAG:
                        case ADD_USER_PROPERTY:
                            returnObject = handleOperationGroupA(operationType, object, request, responseFactory);
                            break;
                        case CHECK_IN:
                        case RENAME_FILE:
                        case MOVE_FILE:
                            returnObject = handleOperationGroupB(operationType, object, request, responseFactory);
                            break;
                        case DELETE_FILE:
                        case UNDELETE_FILE:
                        case GET_LOGFILE_INFO:
                        case GET_ALL_LOGFILE_INFO:
                        case REGISTER_CLIENT_LISTENER:
                        case ADD_FILE:
                        case ADD_DIRECTORY:
                            returnObject = handleOperationGroupC(operationType, object, request, responseFactory);
                            break;
                        case DELETE_DIRECTORY:
                        case GET_INFO_FOR_MERGE:
                        case RESOLVE_CONFLICT_FROM_PARENT_BRANCH:
                        case LIST_FILES_TO_PROMOTE:
                        case PROMOTE_FILE:
                        case ADD_USER:
                        case REMOVE_USER:
                        case ASSIGN_USER_ROLES:
                        case LIST_PROJECT_USERS:
                        case GET_MOST_RECENT_ACTIVITY:
                            returnObject = handleOperationGroupD(operationType, object, request, responseFactory);
                            break;
                        case LIST_USER_ROLES:
                        case SERVER_GET_ROLES:
                        case SERVER_GET_ROLE_PRIVILEGES:
                        case SERVER_UPDATE_ROLE_PRIVILEGES:
                        case SERVER_DELETE_ROLE:
                        case SERVER_CREATE_PROJECT:
                        case SERVER_DELETE_PROJECT:
                        case SERVER_MAINTAIN_PROJECT:
                        case SERVER_CREATE_BRANCH:
                        case SERVER_DELETE_BRANCH:
                        case SERVER_SHUTDOWN:
                            returnObject = handleOperationGroupE(operationType, object, request, responseFactory);
                            break;
                        default:
                            LOGGER.warn(LOG_UNEXPECTED_CLIENT_REQUEST_OBJECT, object.getClass().toString());
                            returnObject = new ClientRequestError(UNKNOWN_OPERATION_REQUEST, UNEXPECTED_CLIENT_REQUEST_OBJECT + object.getClass().toString(), (ClientRequestClientData) request);
                            break;
                    }
                } else {
                    if (object != null) {
                        LOGGER.warn(LOG_UNEXPECTED_CLIENT_REQUEST_OBJECT, object.getClass().toString());
                        returnObject = new ClientRequestError(UNKNOWN_OPERATION_REQUEST, UNEXPECTED_CLIENT_REQUEST_OBJECT + object.getClass().toString(), (ClientRequestClientData) null);
                    } else {
                        returnObject = new ClientRequestError(UNKNOWN_OPERATION_REQUEST, "Unexpected null client request object", null);
                    }
                }
            } else if (getIsUserLoggedIn() && !getClientVersionMatchesFlag()) {
                LOGGER.info("Client version mismatch.");
                returnObject = new ClientRequestError("Client version mismatch. Update your client to version: [{}]", QVCSConstants.QVCS_RELEASE_VERSION, null);
            } else if (object instanceof ClientRequestLoginData) {
                // The user is not logged in.  We can process login requests,
                // change password requests, and begin/end transactions requests.
                ClientRequestLoginData loginRequestData = (ClientRequestLoginData) object;
                LOGGER.info("Received: [{}]", loginRequestData.getOperationType());
                setUserName(loginRequestData.getUserName());
                returnObject = new ClientRequestLogin(loginRequestData);
            } else {
                if (object != null) {
                    LOGGER.warn("ClientRequestFactory.createClientRequest not logged in for request: " + object.getClass().toString());
                }
                returnObject = new ClientRequestError("Not logged in!!", "Invalid operation request", null);
            }
        } catch (java.io.EOFException e) {
            LOGGER.warn("ClientRequestFactory.createClientRequest EOF Detected.");
        } catch (java.net.SocketException e) {
            LOGGER.warn("ClientRequestFactory.createClientRequest socket exception: " + e.getLocalizedMessage());
        } catch (java.lang.OutOfMemoryError e) {
            // This should cause us to close the socket to the client that caused the problem.
            // Hopefully that will be enough to shed the memory load, and allow the server
            // to remain running.
            returnObject = null;
            LOGGER.warn("ClientRequestFactory.createClientRequest out of memory error!!!");
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        }
        return returnObject;
    }

    private ClientRequestInterface handleOperationGroupA(ClientRequestDataInterface.RequestOperationType operationType, Object object, ClientRequestOperationDataInterface request,
            ServerResponseFactory responseFactory) {
        ClientRequestInterface returnObject = null;
        ClientRequestClientData requestData = (ClientRequestClientData) request;
        LOGGER.debug("Received: [{}] [{}]", requestData.getOperationType(), requestData.getSyncToken());
        switch (operationType) {
            case LIST_CLIENT_PROJECTS:
                ClientRequestListClientProjectsData listClientProjectsData = (ClientRequestListClientProjectsData) object;
                returnObject = new ClientRequestListClientProjects(listClientProjectsData);
                break;
            case LIST_CLIENT_BRANCHES:
                ClientRequestListClientBranchesData listClientBranchesData = (ClientRequestListClientBranchesData) object;
                returnObject = new ClientRequestListClientBranches(listClientBranchesData);
                break;
            case LIST_PROJECTS:
                ClientRequestServerListProjectsData listProjectsData = (ClientRequestServerListProjectsData) object;
                returnObject = new ClientRequestServerListProjects(listProjectsData);
                break;
            case LIST_USERS:
                ClientRequestServerListUsersData listUsersData = (ClientRequestServerListUsersData) object;
                returnObject = new ClientRequestServerListUsers(listUsersData);
                break;
            case CHANGE_USER_PASSWORD:
                ClientRequestChangePasswordData clientRequestChangePasswordData = (ClientRequestChangePasswordData) object;
                returnObject = new ClientRequestChangePassword(clientRequestChangePasswordData);
                break;
            case GET_REVISION:
                ClientRequestGetRevisionData getRevisionData = (ClientRequestGetRevisionData) object;
                LOGGER.debug("Request get revision;  project name: [" + getRevisionData.getProjectName() + "] branch name: [" + getRevisionData.getBranchName()
                        + "] appended path: [" + getRevisionData.getAppendedPath() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestGetRevision(getRevisionData);
                } else {
                    returnObject = reportProblem(request, getRevisionData.getAppendedPath(), getRevisionData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.GET.getAction());
                }
                break;
            case GET_DIRECTORY:
                ClientRequestGetDirectoryData getDirectoryData = (ClientRequestGetDirectoryData) object;
                LOGGER.debug("Request get directory; project name: [" + getDirectoryData.getProjectName() + "] branch name: [" + getDirectoryData.getBranchName()
                        + "] appended path: [" + getDirectoryData.getAppendedPath() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET_DIRECTORY)) {
                    returnObject = new ClientRequestGetDirectory(getDirectoryData);
                } else {
                    returnObject = reportProblem(request, getDirectoryData.getAppendedPath(), null, responseFactory, RolePrivilegesManager.GET_DIRECTORY.getAction());
                }
                break;
            case GET_FOR_VISUAL_COMPARE:
                ClientRequestGetForVisualCompareData getForVisualCompareData = (ClientRequestGetForVisualCompareData) object;
                LOGGER.debug("Request get for visual compare; project name: [" + getForVisualCompareData.getProjectName() + "] branch name: ["
                        + getForVisualCompareData.getBranchName() + "] appended path: [" + getForVisualCompareData.getAppendedPath() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestGetForVisualCompare(getForVisualCompareData);
                } else {
                    returnObject = reportProblem(request, getForVisualCompareData.getAppendedPath(), getForVisualCompareData.getCommandArgs().getShortWorkfileName(),
                            responseFactory,
                            RolePrivilegesManager.GET.getAction());
                }
                break;
            case GET_REVISION_FOR_COMPARE:
                ClientRequestGetRevisionForCompareData getRevisionForCompareData = (ClientRequestGetRevisionForCompareData) object;
                LOGGER.debug("Request get revision for compare; project name: [" + getRevisionForCompareData.getProjectName()
                        + "] appended path: [" + getRevisionForCompareData.getAppendedPath() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestGetRevisionForCompare(getRevisionForCompareData);
                } else {
                    returnObject = reportProblem(request, getRevisionForCompareData.getAppendedPath(), getRevisionForCompareData.getShortWorkfileName(),
                            responseFactory,
                            RolePrivilegesManager.GET.getAction());
                }
                break;
            case GET_USER_COMMIT_COMMENTS:
                ClientRequestGetUserCommitCommentsData getUserCommitCommentsData = (ClientRequestGetUserCommitCommentsData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.CHECK_IN)) {
                    returnObject = new ClientRequestGetUserCommitComments(getUserCommitCommentsData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory,
                            RolePrivilegesManager.CHECK_IN.getAction());
                }
                break;
            case GET_COMMIT_LIST_FOR_MOVEABLE_TAG_READ_ONLY_BRANCHES:
                ClientRequestGetCommitListForMoveableTagData clientRequestGetCommitListForMoveableTagData = (ClientRequestGetCommitListForMoveableTagData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_BRANCH)) {
                    returnObject = new ClientRequestGetCommitListForMoveableTag(clientRequestGetCommitListForMoveableTagData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory,
                            RolePrivilegesManager.SERVER_MAINTAIN_BRANCH.getAction());
                }
                break;
            case GET_BRIEF_COMMIT_LIST:
                ClientRequestGetBriefCommitInfoListData clientRequestGetBriefCommitInfoListData = (ClientRequestGetBriefCommitInfoListData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestGetBriefCommitInfoList(clientRequestGetBriefCommitInfoListData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory,
                            RolePrivilegesManager.SERVER_MAINTAIN_BRANCH.getAction());
                }
                break;
            case UPDATE_TAG_COMMIT_ID:
                ClientRequestUpdateTagCommitIdData clientRequestUpdateTagCommitIdData = (ClientRequestUpdateTagCommitIdData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_BRANCH)) {
                    returnObject = new ClientRequestUpdateTagCommitId(clientRequestUpdateTagCommitIdData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory,
                            RolePrivilegesManager.SERVER_MAINTAIN_BRANCH.getAction());
                }
                break;
            case GET_TAGS:
                ClientRequestGetTagsData getTagsData = (ClientRequestGetTagsData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_BRANCH)) {
                    returnObject = new ClientRequestGetTags(getTagsData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory,
                            RolePrivilegesManager.SERVER_MAINTAIN_BRANCH.getAction());
                }
                break;
            case GET_TAGS_INFO:
                ClientRequestGetTagsInfoData getTagsInfoData = (ClientRequestGetTagsInfoData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_BRANCH)) {
                    returnObject = new ClientRequestGetTagsInfo(getTagsInfoData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory,
                            RolePrivilegesManager.SERVER_MAINTAIN_BRANCH.getAction());
                }
                break;
            case APPLY_TAG:
                ClientRequestApplyTagData applyTagData = (ClientRequestApplyTagData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_BRANCH)) {
                    returnObject = new ClientRequestApplyTag(applyTagData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory,
                            RolePrivilegesManager.SERVER_MAINTAIN_BRANCH.getAction());
                }
                break;
            case ADD_USER_PROPERTY:
                ClientRequestAddUserPropertyData clientRequestAddUserPropertyData = (ClientRequestAddUserPropertyData) object;
                returnObject = new ClientRequestAddUserProperty(clientRequestAddUserPropertyData);
                break;
            default:
                throw new QVCSRuntimeException("Unexpected operation type in handleOperationGroupA");
        }
        return returnObject;
    }

    private ClientRequestInterface handleOperationGroupB(ClientRequestDataInterface.RequestOperationType operationType, Object object, ClientRequestOperationDataInterface request,
            ServerResponseFactory responseFactory) {
        ClientRequestInterface returnObject = null;
        ClientRequestClientData requestData = (ClientRequestClientData) request;
        LOGGER.debug("Received: [{}] [{}]", requestData.getOperationType(), requestData.getSyncToken());
        switch (operationType) {
            case CHECK_IN:
                ClientRequestCheckInData checkInData = (ClientRequestCheckInData) object;
                LOGGER.debug("Request Info: checkin:" + checkInData.getAppendedPath() + " project name: " + checkInData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.CHECK_IN)) {
                    returnObject = new ClientRequestCheckIn(checkInData);
                } else {
                    returnObject = reportProblem(request, checkInData.getAppendedPath(), checkInData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.CHECK_IN.getAction());
                }
                break;
            case RENAME_FILE:
                ClientRequestRenameData clientRequestRenameData = (ClientRequestRenameData) object;
                LOGGER.debug("Request Info: rename file for directory:" + clientRequestRenameData.getAppendedPath() + " project name: "
                        + clientRequestRenameData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.RENAME_FILE)) {
                    returnObject = new ClientRequestRename(clientRequestRenameData);
                } else {
                    returnObject = reportProblem(request, clientRequestRenameData.getAppendedPath(),
                            clientRequestRenameData.getOriginalShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.RENAME_FILE.getAction());
                }
                break;
            case MOVE_FILE:
                ClientRequestMoveFileData clientRequestMoveFileData = (ClientRequestMoveFileData) object;
                LOGGER.debug("Request Info: Move file for project: [{}] branch: [{}] origin appended path: [{}] destination appended path: [{}] fileName: [{}]",
                        clientRequestMoveFileData.getProjectName(),
                        clientRequestMoveFileData.getBranchName(),
                        clientRequestMoveFileData.getOriginalAppendedPath(),
                        clientRequestMoveFileData.getNewAppendedPath(),
                        clientRequestMoveFileData.getShortWorkfileName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.MOVE_FILE)) {
                    returnObject = new ClientRequestMoveFile(clientRequestMoveFileData);
                } else {
                    returnObject = reportProblem(request, clientRequestMoveFileData.getOriginalAppendedPath(),
                            clientRequestMoveFileData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.MOVE_FILE.getAction());
                }
                break;
            default:
                throw new QVCSRuntimeException("Unexpected operation type in handleOperationGroupB");
        }
        return returnObject;
    }

    private ClientRequestInterface handleOperationGroupC(ClientRequestDataInterface.RequestOperationType operationType, Object object, ClientRequestOperationDataInterface request,
            ServerResponseFactory responseFactory) {
        ClientRequestInterface returnObject = null;
        ClientRequestClientData requestData = (ClientRequestClientData) request;
        LOGGER.debug("Received: [{}] [{}]", requestData.getOperationType(), requestData.getSyncToken());
        switch (operationType) {
            case DELETE_FILE:
                ClientRequestDeleteFileData clientRequestDeleteFileData = (ClientRequestDeleteFileData) object;
                LOGGER.debug("Request Info: set obsolete:" + clientRequestDeleteFileData.getAppendedPath()
                        + " project name: " + clientRequestDeleteFileData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.DELETE_FILE)) {
                    returnObject = new ClientRequestDeleteFile(clientRequestDeleteFileData);
                } else {
                    returnObject = reportProblem(request, clientRequestDeleteFileData.getAppendedPath(),
                            clientRequestDeleteFileData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.DELETE_FILE.getAction());
                }
                break;
            case UNDELETE_FILE:
                ClientRequestUnDeleteFileData clientRequestUnDeleteFileData = (ClientRequestUnDeleteFileData) object;
                LOGGER.debug("Request Info: unDeleteFile: project: [{}] branch: [{}] shortworkfileName: [{}]", clientRequestUnDeleteFileData.getProjectName(),
                        clientRequestUnDeleteFileData.getBranchName(), clientRequestUnDeleteFileData.getShortWorkfileName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SHOW_CEMETERY)) {
                    returnObject = new ClientRequestUnDeleteFile(clientRequestUnDeleteFileData);
                } else {
                    returnObject = reportProblem(request, "",
                            clientRequestUnDeleteFileData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.SHOW_CEMETERY.getAction());
                }
                break;
            case GET_LOGFILE_INFO:
                ClientRequestGetLogfileInfoData clientRequestGetLogfileInfoData = (ClientRequestGetLogfileInfoData) object;
                LOGGER.debug("Request Info: get logfile info:" + clientRequestGetLogfileInfoData.getAppendedPath() + " project name: "
                        + clientRequestGetLogfileInfoData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestGetLogfileInfo(clientRequestGetLogfileInfoData);
                } else {
                    returnObject = reportProblem(request, clientRequestGetLogfileInfoData.getAppendedPath(),
                            clientRequestGetLogfileInfoData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.GET.getAction());
                }
                break;
            case GET_ALL_LOGFILE_INFO:
                ClientRequestGetAllLogfileInfoData clientRequestGetAllLogfileInfoData = (ClientRequestGetAllLogfileInfoData) object;
                LOGGER.debug("Request Info: get all logfile info:" + clientRequestGetAllLogfileInfoData.getAppendedPath() + " project name: "
                        + clientRequestGetAllLogfileInfoData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestGetAllLogfileInfo(clientRequestGetAllLogfileInfoData);
                } else {
                    returnObject = reportProblem(request, clientRequestGetAllLogfileInfoData.getAppendedPath(),
                            clientRequestGetAllLogfileInfoData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.GET.getAction());
                }
                break;
            case REGISTER_CLIENT_LISTENER:
                ClientRequestRegisterClientListenerData registerClientListenerData = (ClientRequestRegisterClientListenerData) object;
                LOGGER.debug("Request register client listener; project name: [" + registerClientListenerData.getProjectName()
                        + "] branch name: [" + registerClientListenerData.getBranchName() + "] appended path: [" + registerClientListenerData.getAppendedPath() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestRegisterClientListener(registerClientListenerData);
                    ClientRequestRegisterClientListener clientRequestRegisterClientListener = (ClientRequestRegisterClientListener) returnObject;

                    // See if we should show the cemetery...
                    if (registerClientListenerData.getAppendedPath().length() == 0) {
                        if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SHOW_CEMETERY)) {
                            clientRequestRegisterClientListener.setShowCemeteryFlag(true);
                        }
                    }
                } else {
                    returnObject = reportProblem(request, registerClientListenerData.getAppendedPath(), null, responseFactory, RolePrivilegesManager.GET.getAction());
                }
                break;
            case ADD_FILE:
                ClientRequestCreateArchiveData createArchiveData = (ClientRequestCreateArchiveData) object;
                String fullFileName = Utility.formatFilenameForActivityJournal(createArchiveData.getProjectName(), createArchiveData.getBranchName(),
                        createArchiveData.getAppendedPath(),
                        createArchiveData.getCommandArgs().getWorkfileName());
                LOGGER.debug("Request create archive for file: [" + fullFileName + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.ADD_FILE)) {
                    returnObject = new ClientRequestCreateArchive(createArchiveData);
                } else {
                    returnObject = reportProblem(request, createArchiveData.getAppendedPath(), null, responseFactory, RolePrivilegesManager.ADD_FILE.getAction());
                }
                break;
            case ADD_DIRECTORY:
                ClientRequestAddDirectoryData addDirectoryData = (ClientRequestAddDirectoryData) object;
                String appendedPath = addDirectoryData.getAppendedPath();
                LOGGER.debug(" project name: [" + addDirectoryData.getProjectName() + "] Request Info: add directory: [" + appendedPath + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.ADD_DIRECTORY)) {
                    returnObject = new ClientRequestAddDirectory(addDirectoryData);
                } else {
                    returnObject = reportProblem(request, addDirectoryData.getAppendedPath(), null, responseFactory, RolePrivilegesManager.ADD_DIRECTORY.getAction());
                }
                break;
            default:
                throw new QVCSRuntimeException("Unexpected operation type in handleOperationGroupC");
        }
        return returnObject;
    }

    private ClientRequestInterface handleOperationGroupD(ClientRequestDataInterface.RequestOperationType operationType, Object object, ClientRequestOperationDataInterface request,
            ServerResponseFactory responseFactory) {
        ClientRequestInterface returnObject = null;
        ClientRequestClientData requestData = (ClientRequestClientData) request;
        LOGGER.debug("Received: [{}] [{}]", requestData.getOperationType(), requestData.getSyncToken());
        switch (operationType) {
            case DELETE_DIRECTORY:
                ClientRequestDeleteDirectoryData deleteDirectoryData = (ClientRequestDeleteDirectoryData) object;
                String appendedPath = deleteDirectoryData.getAppendedPath();
                LOGGER.debug(" project name: " + deleteDirectoryData.getProjectName() + "Request Info: delete directory:" + appendedPath);

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.DELETE_DIRECTORY)) {
                    returnObject = new ClientRequestDeleteDirectory(deleteDirectoryData);
                } else {
                    returnObject = reportProblem(request, deleteDirectoryData.getAppendedPath(), null, responseFactory,
                            RolePrivilegesManager.DELETE_DIRECTORY.getAction());
                }
                break;
            case GET_INFO_FOR_MERGE:
                ClientRequestGetInfoForMergeData clientRequestGetInfoForMergeData = (ClientRequestGetInfoForMergeData) object;
                LOGGER.debug("Get info for merge. Project: [" + clientRequestGetInfoForMergeData.getProjectName() + "] branch: "
                        + clientRequestGetInfoForMergeData.getBranchName() + "]");
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.MERGE_FROM_PARENT)) {
                    returnObject = new ClientRequestGetInfoForMerge(clientRequestGetInfoForMergeData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.MERGE_FROM_PARENT.getAction());
                }
                break;
            case RESOLVE_CONFLICT_FROM_PARENT_BRANCH:
                ClientRequestResolveConflictFromParentBranchData clientRequestResolveConflictFromParentBranchData
                        = (ClientRequestResolveConflictFromParentBranchData) object;
                LOGGER.debug("Resolve conflict from parent branch.");
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.MERGE_FROM_PARENT)) {
                    returnObject = new ClientRequestResolveConflictFromParentBranch(clientRequestResolveConflictFromParentBranchData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.MERGE_FROM_PARENT.getAction());
                }
                break;
            case LIST_FILES_TO_PROMOTE:
                ClientRequestListFilesToPromoteData clientRequestListFilesToPromoteData = (ClientRequestListFilesToPromoteData) object;
                LOGGER.debug("List files to promote.");
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.PROMOTE_TO_PARENT)) {
                    returnObject = new ClientRequestListFilesToPromote(clientRequestListFilesToPromoteData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.PROMOTE_TO_PARENT.getAction());
                }
                break;
            case PROMOTE_FILE:
                ClientRequestPromoteFileData clientRequestPromoteFilesData = (ClientRequestPromoteFileData) object;
                LOGGER.debug("Request promote file; project name: [" + clientRequestPromoteFilesData.getProjectName() + "] branch name: ["
                        + clientRequestPromoteFilesData.getBranchName() + "] appended path: [" + clientRequestPromoteFilesData.getFilePromotionInfo().getPromotedFromAppendedPath()
                        + "] file name: [" + clientRequestPromoteFilesData.getFilePromotionInfo().getPromotedFromShortWorkfileName() + "]");
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.PROMOTE_TO_PARENT)) {
                    returnObject = buildPromotionRequestHandler(clientRequestPromoteFilesData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.PROMOTE_TO_PARENT.getAction());
                }
                break;
            case ADD_USER:
                ClientRequestServerAddUserData addUserData = (ClientRequestServerAddUserData) object;
                LOGGER.debug("Request add user: [{}]", addUserData.getUserName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerAddUser(addUserData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Add user");
                }
                break;
            case REMOVE_USER:
                ClientRequestServerRemoveUserData removeUserData = (ClientRequestServerRemoveUserData) object;
                LOGGER.debug("Request remove user: [{}]", removeUserData.getUserName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerRemoveUser(removeUserData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Remove user");
                }
                break;
            case ASSIGN_USER_ROLES:
                ClientRequestServerAssignUserRolesData assignUserRoleData = (ClientRequestServerAssignUserRolesData) object;
                LOGGER.debug("Request assign user roles for user: [{}]", assignUserRoleData.getUserName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.ASSIGN_USER_ROLES)) {
                    returnObject = new ClientRequestServerAssignUserRoles(assignUserRoleData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.ASSIGN_USER_ROLES.getAction());
                }
                break;
            case LIST_PROJECT_USERS:
                ClientRequestServerListProjectUsersData listProjectUsersData = (ClientRequestServerListProjectUsersData) object;
                LOGGER.debug("Request list project users.");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LIST_PROJECT_USERS)) {
                    returnObject = new ClientRequestServerListProjectUsers(listProjectUsersData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.LIST_PROJECT_USERS.getAction());
                }
                break;
            case GET_MOST_RECENT_ACTIVITY:
                ClientRequestGetMostRecentActivityData clientRequestGetMostRecentActivityData = (ClientRequestGetMostRecentActivityData) object;
                LOGGER.debug("Request get most recent activity.");

                // We let anyone who can login in to perform this operation.
                returnObject = new ClientRequestGetMostRecentActivity(clientRequestGetMostRecentActivityData);
                break;
            default:
                throw new QVCSRuntimeException("Unexpected operation type in handleOperationGroupD");
        }
        return returnObject;
    }

    private ClientRequestInterface handleOperationGroupE(ClientRequestDataInterface.RequestOperationType operationType, Object object, ClientRequestOperationDataInterface request,
            ServerResponseFactory responseFactory) {
        ClientRequestInterface returnObject = null;
        ClientRequestClientData requestData = (ClientRequestClientData) request;
        LOGGER.debug("Received: [{}] [{}]", requestData.getOperationType(), requestData.getSyncToken());
        switch (operationType) {
            case LIST_USER_ROLES:
                ClientRequestServerListUserRolesData listUserRolesData = (ClientRequestServerListUserRolesData) object;
                LOGGER.debug("Request list user roles.");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LIST_USER_ROLES)) {
                    returnObject = new ClientRequestServerListUserRoles(listUserRolesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.LIST_USER_ROLES.getAction());
                }
                break;
            case SERVER_GET_ROLES:
                ClientRequestServerGetRoleNamesData clientRequestServerGetRoleNamesData = (ClientRequestServerGetRoleNamesData) object;
                LOGGER.debug("Request list user roles.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerGetRoleNames(clientRequestServerGetRoleNamesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Get roles");
                }
                break;
            case SERVER_GET_ROLE_PRIVILEGES:
                ClientRequestServerGetRolePrivilegesData clientRequestServerGetRolePrivilegesData = (ClientRequestServerGetRolePrivilegesData) object;
                LOGGER.debug("Request list role privileges.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerGetRolePrivileges(clientRequestServerGetRolePrivilegesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Get role privileges");
                }
                break;
            case SERVER_UPDATE_ROLE_PRIVILEGES:
                ClientRequestServerUpdatePrivilegesData clientRequestServerUpdatePrivilegesData = (ClientRequestServerUpdatePrivilegesData) object;
                LOGGER.debug("Request update role privileges.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerUpdatePrivileges(clientRequestServerUpdatePrivilegesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Update role privileges");
                }
                break;
            case SERVER_DELETE_ROLE:
                ClientRequestServerDeleteRoleData clientRequestServerDeleteRoleData = (ClientRequestServerDeleteRoleData) object;
                LOGGER.debug("Request delete role.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerDeleteRole(clientRequestServerDeleteRoleData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Delete role");
                }
                break;
            case SERVER_CREATE_PROJECT:
                ClientRequestServerCreateProjectData createProjectData = (ClientRequestServerCreateProjectData) object;
                LOGGER.debug("Request create project: " + createProjectData.getNewProjectName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerCreateProject(createProjectData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Create project");
                }
                break;
            case SERVER_DELETE_PROJECT:
                ClientRequestServerDeleteProjectData deleteProjectData = (ClientRequestServerDeleteProjectData) object;
                LOGGER.debug("Request delete project: " + deleteProjectData.getDeleteProjectName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerDeleteProject(deleteProjectData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Delete project");
                }
                break;
            case SERVER_MAINTAIN_PROJECT:
                ClientRequestServerMaintainProjectData maintainProjectData = (ClientRequestServerMaintainProjectData) object;
                LOGGER.debug("Request maintain project: " + maintainProjectData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_PROJECT)) {
                    returnObject = new ClientRequestServerMaintainProject(maintainProjectData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.SERVER_MAINTAIN_PROJECT.getAction());
                }
                break;
            case SERVER_CREATE_BRANCH:
                ClientRequestServerCreateBranchData createBranchData = (ClientRequestServerCreateBranchData) object;
                LOGGER.debug("Request create branch: [{}] for project: [{}]", createBranchData.getBranchName(), createBranchData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_BRANCH)) {
                    returnObject = new ClientRequestServerCreateBranch(createBranchData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.SERVER_MAINTAIN_BRANCH.getAction());
                }
                break;
            case SERVER_DELETE_BRANCH:
                ClientRequestServerDeleteBranchData deleteBranchData = (ClientRequestServerDeleteBranchData) object;
                LOGGER.debug("Request delete branch: [{}] for project: [{}]", deleteBranchData.getBranchName(), deleteBranchData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_BRANCH)) {
                    returnObject = new ClientRequestServerDeleteBranch(deleteBranchData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.SERVER_MAINTAIN_BRANCH.getAction());
                }
                break;
            case SERVER_SHUTDOWN:
                ClientRequestServerShutdownData serverShutdownData = (ClientRequestServerShutdownData) object;
                LOGGER.debug("Request server shutdown: [{}]", serverShutdownData.getServerName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerShutdown(serverShutdownData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Server shutdown");
                }
                break;
            default:
                throw new QVCSRuntimeException("Unexpected operation type in handleOperationGroupD");
        }
        return returnObject;
    }

    /**
     * Get the user is logged in flag.
     *
     * @return the user is logged in flag.
     */
    public boolean getIsUserLoggedIn() {
        return isUserLoggedInFlagMember;
    }

    /**
     * Set the user is logged in flag.
     *
     * @param flag the user is logged in flag.
     */
    public void setIsUserLoggedIn(boolean flag) {
        isUserLoggedInFlagMember = flag;
    }

    /**
     * Get the client version matches flag.
     *
     * @return the client version matches flag.
     */
    public boolean getClientVersionMatchesFlag() {
        return this.clientVersionMatchesFlagMember;
    }

    /**
     * Set the client version matches flag.
     *
     * @param flag the client version matches flag.
     */
    public void setClientVersionMatchesFlag(boolean flag) {
        this.clientVersionMatchesFlagMember = flag;
    }

    /**
     * Get the user name.
     *
     * @return the user name.
     */
    public String getUserName() {
        return userNameMember;
    }

    /**
     * Set the user name.
     *
     * @param userName the user name.
     */
    public void setUserName(String userName) {
        userNameMember = userName;
    }

    private boolean isUserPrivileged(String projectName, ServerAction action) {
        return RolePrivilegesManager.getInstance().isUserPrivileged(projectName, getUserName(), action);
    }

    private Object decompress(Object object) {
        Object retVal = null;
        Compressor decompressor = new ZlibCompressor();
        byte[] compressedInput = (byte[]) object;
        byte[] expandedBuffer = decompressor.expand(compressedInput);
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(expandedBuffer);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream);
            retVal = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn("Caught exception trying to decompress an object: " + e.getClass().toString() + " " + e.getLocalizedMessage());
        }
        return retVal;
    }

    private ClientRequestError reportProblem(ClientRequestOperationDataInterface request, String appendedPath, String shortWorkfileName, ServerResponseFactory responseFactory,
            String action) {
        String message = "Unauthorized client request: [" + action + "]. User [" + getUserName() + "] is not authorized to perform requested operation for project : ["
                + request.getProjectName() + "]";
        LOGGER.warn(message);

        ServerResponseMessage infoMessage = new ServerResponseMessage(message, null, null, null, ServerResponseMessage.HIGH_PRIORITY);
        responseFactory.createServerResponse(infoMessage);
        ClientRequestError clientRequestError = new ClientRequestError(action, message, (ClientRequestClientData) request);

        if (shortWorkfileName != null) {
            // Set an alternate response object so we can perform the notify that we need to do
            // on the client side, since the client side may be a synchronous call. If we don't do this
            // then the client will appear to hang, since it is waiting for a notify that it will
            // never get.
            ServerResponseMessage alternateResponse = new ServerResponseMessage(clientRequestError.getErrorMessage(),
                    request.getProjectName(),
                    request.getBranchName(),
                    appendedPath, ServerResponseMessage.MEDIUM_PRIORITY);
            alternateResponse.setShortWorkfileName(shortWorkfileName);
            clientRequestError.setAlternateResponseObject(alternateResponse);
        }

        return clientRequestError;
    }

    private ClientRequestInterface buildPromotionRequestHandler(ClientRequestPromoteFileData data) {
        ClientRequestInterface handler = null;

        switch (data.getFilePromotionInfo().getTypeOfPromotion()) {
            case SIMPLE_PROMOTION_TYPE -> {
                handler = new ClientRequestPromotionSimple(data);
            }
            case FILE_NAME_CHANGE_PROMOTION_TYPE -> {
                handler = new ClientRequestPromotionRename(data);
            }
            case FILE_LOCATION_CHANGE_PROMOTION_TYPE -> {
                handler = new ClientRequestPromotionMove(data);
            }
            case LOCATION_AND_NAME_DIFFER_PROMOTION_TYPE -> {
                handler = new ClientRequestPromotionMoveAndRename(data);
            }
            case FILE_CREATED_PROMOTION_TYPE -> {
                handler = new ClientRequestPromotionCreate(data);
            }
            case FILE_DELETED_PROMOTION_TYPE -> {
                handler = new ClientRequestPromotionDelete(data);
            }
            default -> throw new QVCSRuntimeException("Unknown promotion type.");
        }
        return handler;
    }
}
