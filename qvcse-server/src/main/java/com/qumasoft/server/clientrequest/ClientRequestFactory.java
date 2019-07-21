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

import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.ZlibCompressor;
import com.qumasoft.qvcslib.requestdata.ClientRequestAddDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestBreakLockData;
import com.qumasoft.qvcslib.requestdata.ClientRequestChangePasswordData;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckInData;
import com.qumasoft.qvcslib.requestdata.ClientRequestCheckOutData;
import com.qumasoft.qvcslib.requestdata.ClientRequestCreateArchiveData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDataInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestDeleteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetForVisualCompareData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetInfoForMergeData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetLogfileInfoData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetMostRecentActivityData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestGetRevisionForCompareData;
import com.qumasoft.qvcslib.requestdata.ClientRequestHeartBeatData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLabelData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLabelDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientProjectsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListClientViewsData;
import com.qumasoft.qvcslib.requestdata.ClientRequestListFilesToPromoteData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLockData;
import com.qumasoft.qvcslib.requestdata.ClientRequestLoginData;
import com.qumasoft.qvcslib.requestdata.ClientRequestMoveFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestOperationDataInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.requestdata.ClientRequestRegisterClientListenerData;
import com.qumasoft.qvcslib.requestdata.ClientRequestRenameData;
import com.qumasoft.qvcslib.requestdata.ClientRequestResolveConflictFromParentBranchData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerAddUserData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerAssignUserRolesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateProjectData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerCreateBranchData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteProjectData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteRoleData;
import com.qumasoft.qvcslib.requestdata.ClientRequestServerDeleteBranchData;
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
import com.qumasoft.qvcslib.requestdata.ClientRequestSetAttributesData;
import com.qumasoft.qvcslib.requestdata.ClientRequestSetCommentPrefixData;
import com.qumasoft.qvcslib.requestdata.ClientRequestSetModuleDescriptionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestSetRevisionDescriptionData;
import com.qumasoft.qvcslib.requestdata.ClientRequestTransactionBeginData;
import com.qumasoft.qvcslib.requestdata.ClientRequestTransactionEndData;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnDeleteData;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnLabelData;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnLabelDirectoryData;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnlockData;
import com.qumasoft.qvcslib.requestdata.ClientRequestUpdateClientData;
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
            Object object = objectInputStreamMember.readObject();
            if (object instanceof byte[]) {
                object = decompress(object);
            }

            if (object instanceof ClientRequestChangePasswordData) {
                // Don't need to be logged in to change your password.
                ClientRequestChangePasswordData requestData = (ClientRequestChangePasswordData) object;
                returnObject = new ClientRequestChangePassword(requestData);
            } else if (object instanceof ClientRequestTransactionBeginData) {
                ClientRequestTransactionBeginData requestData = (ClientRequestTransactionBeginData) object;
                returnObject = new ClientRequestTransactionBegin(requestData);
                LOGGER.trace(">>>>>>>>>>>>>>>>>>>  Begin Transaction: [" + requestData.getTransactionID() + "] >>>>>>>>>>>>>>>>>>>");
            } else if (object instanceof ClientRequestTransactionEndData) {
                ClientRequestTransactionEndData requestData = (ClientRequestTransactionEndData) object;
                returnObject = new ClientRequestTransactionEnd(requestData);
                LOGGER.trace("<<<<<<<<<<<<<<<<<<<  End Transaction: [" + requestData.getTransactionID() + "] <<<<<<<<<<<<<<<<<<<");
            } else if (object instanceof ClientRequestHeartBeatData) {
                ClientRequestHeartBeatData heartBeatData = (ClientRequestHeartBeatData) object;
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
                        case CHECK_OUT:
                            returnObject = handleOperationGroupA(operationType, object, request, responseFactory);
                            break;
                        case CHECK_IN:
                        case LOCK:
                        case UNLOCK:
                        case BREAK_LOCK:
                        case LABEL:
                        case LABEL_DIRECTORY:
                        case REMOVE_LABEL:
                        case REMOVE_LABEL_DIRECTORY:
                        case RENAME_FILE:
                        case MOVE_FILE:
                            returnObject = handleOperationGroupB(operationType, object, request, responseFactory);
                            break;
                        case SET_OBSOLETE:
                        case UNDELETE_FILE:
                        case SET_ATTRIBUTES:
                        case SET_COMMENT_PREFIX:
                        case SET_MODULE_DESCRIPTION:
                        case SET_REVISION_DESCRIPTION:
                        case GET_LOGFILE_INFO:
                        case REGISTER_CLIENT_LISTENER:
                        case CREATE_ARCHIVE:
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
                            returnObject = new ClientRequestError(UNKNOWN_OPERATION_REQUEST, UNEXPECTED_CLIENT_REQUEST_OBJECT + object.getClass().toString());
                            break;
                    }
                } else {
                    if (object != null) {
                        LOGGER.warn(LOG_UNEXPECTED_CLIENT_REQUEST_OBJECT, object.getClass().toString());
                        returnObject = new ClientRequestError(UNKNOWN_OPERATION_REQUEST, UNEXPECTED_CLIENT_REQUEST_OBJECT + object.getClass().toString());
                    } else {
                        returnObject = new ClientRequestError(UNKNOWN_OPERATION_REQUEST, "Unexpected null client request object");
                    }
                }
            } else if (getIsUserLoggedIn() && !getClientVersionMatchesFlag()) {

                if (object instanceof ClientRequestUpdateClientData) {
                    ClientRequestUpdateClientData updateClientData = (ClientRequestUpdateClientData) object;
                    LOGGER.info("Request update client file: [" + updateClientData.getRequestedFileName() + "]");
                    returnObject = new ClientRequestUpdateClient(updateClientData);
                } else {
                    if (object != null) {
                        LOGGER.warn("ClientRequestFactory.createClientRequest not logged in for request: " + object.getClass().toString());
                    }
                    returnObject = new ClientRequestError("Not logged in!!", "Invalid operation request");
                }
            } else if (object instanceof ClientRequestLoginData) {
                // The user is not logged in.  We can process login requests,
                // change password requests, and begin/end transactions requests.
                ClientRequestLoginData loginRequestData = (ClientRequestLoginData) object;
                returnObject = new ClientRequestLogin(loginRequestData);
            } else {
                if (object != null) {
                    LOGGER.warn("ClientRequestFactory.createClientRequest not logged in for request: " + object.getClass().toString());
                }
                returnObject = new ClientRequestError("Not logged in!!", "Invalid operation request");
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
        switch (operationType) {
            case LIST_CLIENT_PROJECTS:
                ClientRequestListClientProjectsData listClientProjectsData = (ClientRequestListClientProjectsData) object;
                LOGGER.info("Request list client projects.");
                returnObject = new ClientRequestListClientProjects(listClientProjectsData);
                break;
            case LIST_CLIENT_BRANCHES:
                ClientRequestListClientViewsData listClientViewsData = (ClientRequestListClientViewsData) object;
                LOGGER.info("Request list client views.");
                returnObject = new ClientRequestListClientViews(listClientViewsData);
                break;
            case LIST_PROJECTS:
                ClientRequestServerListProjectsData listProjectsData = (ClientRequestServerListProjectsData) object;
                LOGGER.info("Request list projects.");
                returnObject = new ClientRequestServerListProjects(listProjectsData);
                break;
            case LIST_USERS:
                ClientRequestServerListUsersData listUsersData = (ClientRequestServerListUsersData) object;
                LOGGER.info("Request list users.");
                returnObject = new ClientRequestServerListUsers(listUsersData);
                break;
            case CHANGE_USER_PASSWORD:
                ClientRequestChangePasswordData requestData = (ClientRequestChangePasswordData) object;
                LOGGER.info("Change user password.");
                returnObject = new ClientRequestChangePassword(requestData);
                break;
            case GET_REVISION:
                ClientRequestGetRevisionData getRevisionData = (ClientRequestGetRevisionData) object;
                LOGGER.info("Request get revision;  project name: [" + getRevisionData.getProjectName() + "] view name: [" + getRevisionData.getBranchName()
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
                LOGGER.info("Request get directory; project name: [" + getDirectoryData.getProjectName() + "] view name: [" + getDirectoryData.getBranchName()
                        + "] appended path: [" + getDirectoryData.getAppendedPath() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET_DIRECTORY)) {
                    returnObject = new ClientRequestGetDirectory(getDirectoryData);
                } else {
                    returnObject = reportProblem(request, getDirectoryData.getAppendedPath(), null, responseFactory, RolePrivilegesManager.GET_DIRECTORY.getAction());
                }
                break;
            case GET_FOR_VISUAL_COMPARE:
                ClientRequestGetForVisualCompareData getForVisualCompareData = (ClientRequestGetForVisualCompareData) object;
                LOGGER.info("Request get for visual compare; project name: [" + getForVisualCompareData.getProjectName() + "] view name: ["
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
                LOGGER.info("Request get revision for compare; project name: [" + getRevisionForCompareData.getProjectName()
                        + "] appended path: [" + getRevisionForCompareData.getAppendedPath() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestGetRevisionForCompare(getRevisionForCompareData);
                } else {
                    returnObject = reportProblem(request, getRevisionForCompareData.getAppendedPath(), getRevisionForCompareData.getShortWorkfileName(),
                            responseFactory,
                            RolePrivilegesManager.GET.getAction());
                }
                break;
            case CHECK_OUT:
                ClientRequestCheckOutData checkOutData = (ClientRequestCheckOutData) object;
                LOGGER.info("Request checkout; project name: [" + checkOutData.getProjectName() + "] view name: ["
                        + checkOutData.getBranchName() + "] appended path: ["
                        + checkOutData.getAppendedPath() + "] file name: [" + checkOutData.getCommandArgs().getShortWorkfileName() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.CHECK_OUT)) {
                    returnObject = new ClientRequestCheckOut(checkOutData);
                } else {
                    returnObject = reportProblem(request, checkOutData.getAppendedPath(), checkOutData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.CHECK_OUT.getAction());
                }
                break;
            default:
                throw new QVCSRuntimeException("Unexpected operation type in handleOperationGroupA");
        }
        return returnObject;
    }

    private ClientRequestInterface handleOperationGroupB(ClientRequestDataInterface.RequestOperationType operationType, Object object, ClientRequestOperationDataInterface request,
            ServerResponseFactory responseFactory) {
        ClientRequestInterface returnObject = null;
        switch (operationType) {
            case CHECK_IN:
                ClientRequestCheckInData checkInData = (ClientRequestCheckInData) object;
                LOGGER.info("Request Info: checkin:" + checkInData.getAppendedPath() + " project name: " + checkInData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.CHECK_IN)) {
                    // If they are also applying a label, we need to check that they also have that privilege.
                    if (checkInData.getCommandArgs().getApplyLabelFlag()) {
                        if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LABEL_AT_CHECKIN)) {
                            returnObject = new ClientRequestCheckIn(checkInData);
                        } else {
                            returnObject = reportProblem(request, checkInData.getAppendedPath(), checkInData.getCommandArgs().getShortWorkfileName(), responseFactory,
                                    RolePrivilegesManager.LABEL_AT_CHECKIN.getAction());
                        }
                    } else {
                        returnObject = new ClientRequestCheckIn(checkInData);
                    }
                } else {
                    returnObject = reportProblem(request, checkInData.getAppendedPath(), checkInData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.CHECK_IN.getAction());
                }
                break;
            case LOCK:
                ClientRequestLockData lockData = (ClientRequestLockData) object;
                LOGGER.info("Request Info: lock:" + lockData.getAppendedPath() + " project name: " + lockData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LOCK)) {
                    returnObject = new ClientRequestLock(lockData);
                } else {
                    returnObject = reportProblem(request, lockData.getAppendedPath(), lockData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.LOCK.getAction());
                }
                break;
            case UNLOCK:
                ClientRequestUnlockData unlockData = (ClientRequestUnlockData) object;
                LOGGER.info("Request Info: unlock:" + unlockData.getAppendedPath() + " project name: " + unlockData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.UNLOCK)) {
                    returnObject = new ClientRequestUnlock(unlockData);
                } else {
                    returnObject = reportProblem(request, unlockData.getAppendedPath(), unlockData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.UNLOCK.getAction());
                }
                break;
            case BREAK_LOCK:
                ClientRequestBreakLockData breakLockData = (ClientRequestBreakLockData) object;
                LOGGER.info("Request Info: break lock:" + breakLockData.getAppendedPath() + " project name: " + breakLockData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.BREAK_LOCK)) {
                    ClientRequestUnlockData unLockData = new ClientRequestUnlockData(breakLockData);
                    returnObject = new ClientRequestUnlock(unLockData);
                } else {
                    returnObject = reportProblem(request, breakLockData.getAppendedPath(), breakLockData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.BREAK_LOCK.getAction());
                }
                break;
            case LABEL:
                ClientRequestLabelData labelData = (ClientRequestLabelData) object;
                LOGGER.info("Request Info: apply label:" + labelData.getAppendedPath() + " project name: " + labelData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LABEL)) {
                    returnObject = new ClientRequestLabel(labelData);
                } else {
                    returnObject = reportProblem(request, labelData.getAppendedPath(), labelData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.LABEL.getAction());
                }
                break;
            case LABEL_DIRECTORY:
                ClientRequestLabelDirectoryData labelDirectoryData = (ClientRequestLabelDirectoryData) object;
                LOGGER.info("Request Info: label directory:" + labelDirectoryData.getAppendedPath() + " project name: "
                        + labelDirectoryData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LABEL_DIRECTORY)) {
                    returnObject = new ClientRequestLabelDirectory(labelDirectoryData);
                } else {
                    returnObject = reportProblem(request, labelDirectoryData.getAppendedPath(), null, responseFactory,
                            RolePrivilegesManager.LABEL_DIRECTORY.getAction());
                }
                break;
            case REMOVE_LABEL:
                ClientRequestUnLabelData unLabelData = (ClientRequestUnLabelData) object;
                LOGGER.info("Request Info: remove label:" + unLabelData.getAppendedPath() + " project name: " + unLabelData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.REMOVE_LABEL)) {
                    returnObject = new ClientRequestUnLabel(unLabelData);
                } else {
                    returnObject = reportProblem(request, unLabelData.getAppendedPath(), unLabelData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.REMOVE_LABEL.getAction());
                }
                break;
            case REMOVE_LABEL_DIRECTORY:
                ClientRequestUnLabelDirectoryData unLabelDirectoryData = (ClientRequestUnLabelDirectoryData) object;
                LOGGER.info("Request Info: remove label from directory:" + unLabelDirectoryData.getAppendedPath()
                        + " project name: " + unLabelDirectoryData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.REMOVE_LABEL_DIRECTORY)) {
                    returnObject = new ClientRequestUnLabelDirectory(unLabelDirectoryData);
                } else {
                    returnObject = reportProblem(request, unLabelDirectoryData.getAppendedPath(), null, responseFactory,
                            RolePrivilegesManager.REMOVE_LABEL_DIRECTORY.getAction());
                }
                break;
            case RENAME_FILE:
                ClientRequestRenameData clientRequestRenameData = (ClientRequestRenameData) object;
                LOGGER.info("Request Info: rename file for directory:" + clientRequestRenameData.getAppendedPath() + " project name: "
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
                LOGGER.info("Request Info: move file for directory:" + clientRequestMoveFileData.getOriginalAppendedPath()
                        + " project name: " + clientRequestMoveFileData.getProjectName());

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
        switch (operationType) {
            case SET_OBSOLETE:
                ClientRequestDeleteFileData clientRequestDeleteFileData = (ClientRequestDeleteFileData) object;
                LOGGER.info("Request Info: set obsolete:" + clientRequestDeleteFileData.getAppendedPath()
                        + " project name: " + clientRequestDeleteFileData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SET_OBSOLETE)) {
                    returnObject = new ClientRequestDeleteFile(clientRequestDeleteFileData);
                } else {
                    returnObject = reportProblem(request, clientRequestDeleteFileData.getAppendedPath(),
                            clientRequestDeleteFileData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.SET_OBSOLETE.getAction());
                }
                break;
            case UNDELETE_FILE:
                ClientRequestUnDeleteData clientRequestUnDeleteData = (ClientRequestUnDeleteData) object;
                LOGGER.info("Request Info: undelete:" + clientRequestUnDeleteData.getAppendedPath()
                        + " project name: " + clientRequestUnDeleteData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SET_OBSOLETE)) {
                    returnObject = new ClientRequestUnDelete(clientRequestUnDeleteData);
                } else {
                    returnObject = reportProblem(request, clientRequestUnDeleteData.getAppendedPath(),
                            clientRequestUnDeleteData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.SET_OBSOLETE.getAction());
                }
                break;
            case SET_ATTRIBUTES:
                ClientRequestSetAttributesData clientRequestSetAttributesData = (ClientRequestSetAttributesData) object;
                LOGGER.info("Request Info: set attributes:" + clientRequestSetAttributesData.getAppendedPath()
                        + " project name: " + clientRequestSetAttributesData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SET_ATTRIBUTES)) {
                    returnObject = new ClientRequestSetAttributes(clientRequestSetAttributesData);
                } else {
                    returnObject = reportProblem(request, clientRequestSetAttributesData.getAppendedPath(),
                            clientRequestSetAttributesData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.SET_ATTRIBUTES.getAction());
                }
                break;
            case SET_COMMENT_PREFIX:
                ClientRequestSetCommentPrefixData clientRequestSetCommentPrefixData = (ClientRequestSetCommentPrefixData) object;
                LOGGER.info("Request Info: set comment prefix:" + clientRequestSetCommentPrefixData.getAppendedPath()
                        + " project name: " + clientRequestSetCommentPrefixData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SET_COMMENT_PREFIX)) {
                    returnObject = new ClientRequestSetCommentPrefix(clientRequestSetCommentPrefixData);
                } else {
                    returnObject = reportProblem(request, clientRequestSetCommentPrefixData.getAppendedPath(),
                            clientRequestSetCommentPrefixData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.SET_COMMENT_PREFIX.getAction());
                }
                break;
            case SET_MODULE_DESCRIPTION:
                ClientRequestSetModuleDescriptionData clientRequestSetModuleDescriptionData = (ClientRequestSetModuleDescriptionData) object;
                LOGGER.info("Request Info: set module description:" + clientRequestSetModuleDescriptionData.getAppendedPath()
                        + " project name: " + clientRequestSetModuleDescriptionData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SET_MODULE_DESCRIPTION)) {
                    returnObject = new ClientRequestSetModuleDescription(clientRequestSetModuleDescriptionData);
                } else {
                    returnObject = reportProblem(request, clientRequestSetModuleDescriptionData.getAppendedPath(),
                            clientRequestSetModuleDescriptionData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.SET_MODULE_DESCRIPTION.getAction());
                }
                break;
            case SET_REVISION_DESCRIPTION:
                ClientRequestSetRevisionDescriptionData clientRequestSetRevisionDescriptionData = (ClientRequestSetRevisionDescriptionData) object;
                LOGGER.info("Request Info: set revision description:" + clientRequestSetRevisionDescriptionData.getAppendedPath()
                        + " project name: " + clientRequestSetRevisionDescriptionData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SET_REVISION_DESCRIPTION)) {
                    returnObject = new ClientRequestSetRevisionDescription(clientRequestSetRevisionDescriptionData);
                } else {
                    returnObject = reportProblem(request, clientRequestSetRevisionDescriptionData.getAppendedPath(),
                            clientRequestSetRevisionDescriptionData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.SET_REVISION_DESCRIPTION.getAction());
                }
                break;
            case GET_LOGFILE_INFO:
                ClientRequestGetLogfileInfoData clientRequestGetLogfileInfoData = (ClientRequestGetLogfileInfoData) object;
                LOGGER.info("Request Info: get logfile info:" + clientRequestGetLogfileInfoData.getAppendedPath() + " project name: "
                        + clientRequestGetLogfileInfoData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestGetLogfileInfo(clientRequestGetLogfileInfoData);
                } else {
                    returnObject = reportProblem(request, clientRequestGetLogfileInfoData.getAppendedPath(),
                            clientRequestGetLogfileInfoData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.GET.getAction());
                }
                break;
            case REGISTER_CLIENT_LISTENER:
                ClientRequestRegisterClientListenerData registerClientListenerData = (ClientRequestRegisterClientListenerData) object;
                LOGGER.info("Request register client listener; project name: [" + registerClientListenerData.getProjectName()
                        + "] view name: [" + registerClientListenerData.getBranchName() + "] appended path: [" + registerClientListenerData.getAppendedPath() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET)) {
                    returnObject = new ClientRequestRegisterClientListener(registerClientListenerData);
                    ClientRequestRegisterClientListener clientRequestRegisterClientListener = (ClientRequestRegisterClientListener) returnObject;

                    // See if we should show the cemetery...
                    if (registerClientListenerData.getAppendedPath().length() == 0) {
                        if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SHOW_CEMETERY)) {
                            clientRequestRegisterClientListener.setShowCemeteryFlag(true);
                        }
                    }
                    // See if we should show the branch archives directory...
                    if (registerClientListenerData.getAppendedPath().length() == 0) {
                        if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SHOW_BRANCH_ARCHIVES_DIRECTORY)) {
                            clientRequestRegisterClientListener.setShowBranchArchivesFlag(true);
                        }
                    }
                } else {
                    returnObject = reportProblem(request, registerClientListenerData.getAppendedPath(), null, responseFactory, RolePrivilegesManager.GET.getAction());
                }
                break;
            case CREATE_ARCHIVE:
                ClientRequestCreateArchiveData createArchiveData = (ClientRequestCreateArchiveData) object;
                String fullFileName = Utility.formatFilenameForActivityJournal(createArchiveData.getProjectName(), createArchiveData.getBranchName(),
                        createArchiveData.getAppendedPath(),
                        createArchiveData.getCommandArgs().getWorkfileName());
                LOGGER.info("Request create archive for file: [" + fullFileName + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.CREATE_ARCHIVE)) {
                    returnObject = new ClientRequestCreateArchive(createArchiveData);
                } else {
                    returnObject = reportProblem(request, createArchiveData.getAppendedPath(), null, responseFactory, RolePrivilegesManager.CREATE_ARCHIVE.getAction());
                }
                break;
            case ADD_DIRECTORY:
                ClientRequestAddDirectoryData addDirectoryData = (ClientRequestAddDirectoryData) object;
                String appendedPath = addDirectoryData.getAppendedPath();
                LOGGER.info(" project name: [" + addDirectoryData.getProjectName() + "] Request Info: add directory: [" + appendedPath + "]");

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
        switch (operationType) {
            case DELETE_DIRECTORY:
                ClientRequestDeleteDirectoryData deleteDirectoryData = (ClientRequestDeleteDirectoryData) object;
                String appendedPath = deleteDirectoryData.getAppendedPath();
                LOGGER.info(" project name: " + deleteDirectoryData.getProjectName() + "Request Info: delete directory:" + appendedPath);

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.DELETE_DIRECTORY)) {
                    returnObject = new ClientRequestDeleteDirectory(deleteDirectoryData);
                } else {
                    returnObject = reportProblem(request, deleteDirectoryData.getAppendedPath(), null, responseFactory,
                            RolePrivilegesManager.DELETE_DIRECTORY.getAction());
                }
                break;
            case GET_INFO_FOR_MERGE:
                ClientRequestGetInfoForMergeData clientRequestGetInfoForMergeData = (ClientRequestGetInfoForMergeData) object;
                LOGGER.info("Get info for merge. Project: [" + clientRequestGetInfoForMergeData.getProjectName() + "] view: "
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
                LOGGER.info("Resolve conflict from parent branch.");
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.MERGE_FROM_PARENT)) {
                    returnObject = new ClientRequestResolveConflictFromParentBranch(clientRequestResolveConflictFromParentBranchData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.MERGE_FROM_PARENT.getAction());
                }
                break;
            case LIST_FILES_TO_PROMOTE:
                ClientRequestListFilesToPromoteData clientRequestListFilesToPromoteData = (ClientRequestListFilesToPromoteData) object;
                LOGGER.info("List files to promote.");
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.PROMOTE_TO_PARENT)) {
                    returnObject = new ClientRequestListFilesToPromote(clientRequestListFilesToPromoteData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.PROMOTE_TO_PARENT.getAction());
                }
                break;
            case PROMOTE_FILE:
                ClientRequestPromoteFileData clientRequestPromoteFilesData = (ClientRequestPromoteFileData) object;
                LOGGER.info("Request promote file; project name: [" + clientRequestPromoteFilesData.getProjectName() + "] view name: ["
                        + clientRequestPromoteFilesData.getBranchName() + "] appended path: [" + clientRequestPromoteFilesData.getFilePromotionInfo().getAppendedPath()
                        + "] file name: [" + clientRequestPromoteFilesData.getFilePromotionInfo().getShortWorkfileName() + "]");
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.PROMOTE_TO_PARENT)) {
                    returnObject = new ClientRequestPromoteFile(clientRequestPromoteFilesData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.PROMOTE_TO_PARENT.getAction());
                }
                break;
            case ADD_USER:
                ClientRequestServerAddUserData addUserData = (ClientRequestServerAddUserData) object;
                LOGGER.info("Request add user: [{}]", addUserData.getUserName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerAddUser(addUserData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Add user");
                }
                break;
            case REMOVE_USER:
                ClientRequestServerRemoveUserData removeUserData = (ClientRequestServerRemoveUserData) object;
                LOGGER.info("Request remove user: [{}]", removeUserData.getUserName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerRemoveUser(removeUserData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Remove user");
                }
                break;
            case ASSIGN_USER_ROLES:
                ClientRequestServerAssignUserRolesData assignUserRoleData = (ClientRequestServerAssignUserRolesData) object;
                LOGGER.info("Request assign user roles for user: [{}]", assignUserRoleData.getUserName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.ASSIGN_USER_ROLES)) {
                    returnObject = new ClientRequestServerAssignUserRoles(assignUserRoleData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.ASSIGN_USER_ROLES.getAction());
                }
                break;
            case LIST_PROJECT_USERS:
                ClientRequestServerListProjectUsersData listProjectUsersData = (ClientRequestServerListProjectUsersData) object;
                LOGGER.info("Request list project users.");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LIST_PROJECT_USERS)) {
                    returnObject = new ClientRequestServerListProjectUsers(listProjectUsersData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.LIST_PROJECT_USERS.getAction());
                }
                break;
            case GET_MOST_RECENT_ACTIVITY:
                ClientRequestGetMostRecentActivityData clientRequestGetMostRecentActivityData = (ClientRequestGetMostRecentActivityData) object;
                LOGGER.info("Request get most recent activity.");

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
        switch (operationType) {
            case LIST_USER_ROLES:
                ClientRequestServerListUserRolesData listUserRolesData = (ClientRequestServerListUserRolesData) object;
                LOGGER.info("Request list user roles.");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LIST_USER_ROLES)) {
                    returnObject = new ClientRequestServerListUserRoles(listUserRolesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.LIST_USER_ROLES.getAction());
                }
                break;
            case SERVER_GET_ROLES:
                ClientRequestServerGetRoleNamesData clientRequestServerGetRoleNamesData = (ClientRequestServerGetRoleNamesData) object;
                LOGGER.info("Request list user roles.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerGetRoleNames(clientRequestServerGetRoleNamesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Get roles");
                }
                break;
            case SERVER_GET_ROLE_PRIVILEGES:
                ClientRequestServerGetRolePrivilegesData clientRequestServerGetRolePrivilegesData = (ClientRequestServerGetRolePrivilegesData) object;
                LOGGER.info("Request list role privileges.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerGetRolePrivileges(clientRequestServerGetRolePrivilegesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Get role privileges");
                }
                break;
            case SERVER_UPDATE_ROLE_PRIVILEGES:
                ClientRequestServerUpdatePrivilegesData clientRequestServerUpdatePrivilegesData = (ClientRequestServerUpdatePrivilegesData) object;
                LOGGER.info("Request update role privileges.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerUpdatePrivileges(clientRequestServerUpdatePrivilegesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Update role privileges");
                }
                break;
            case SERVER_DELETE_ROLE:
                ClientRequestServerDeleteRoleData clientRequestServerDeleteRoleData = (ClientRequestServerDeleteRoleData) object;
                LOGGER.info("Request delete role.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerDeleteRole(clientRequestServerDeleteRoleData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Delete role");
                }
                break;
            case SERVER_CREATE_PROJECT:
                ClientRequestServerCreateProjectData createProjectData = (ClientRequestServerCreateProjectData) object;
                LOGGER.info("Request create project: " + createProjectData.getNewProjectName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerCreateProject(createProjectData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Create project");
                }
                break;
            case SERVER_DELETE_PROJECT:
                ClientRequestServerDeleteProjectData deleteProjectData = (ClientRequestServerDeleteProjectData) object;
                LOGGER.info("Request delete project: " + deleteProjectData.getDeleteProjectName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerDeleteProject(deleteProjectData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Delete project");
                }
                break;
            case SERVER_MAINTAIN_PROJECT:
                ClientRequestServerMaintainProjectData maintainProjectData = (ClientRequestServerMaintainProjectData) object;
                LOGGER.info("Request maintain project: " + maintainProjectData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_PROJECT)) {
                    returnObject = new ClientRequestServerMaintainProject(maintainProjectData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.SERVER_MAINTAIN_PROJECT.getAction());
                }
                break;
            case SERVER_CREATE_BRANCH:
                ClientRequestServerCreateBranchData createViewData = (ClientRequestServerCreateBranchData) object;
                LOGGER.info("Request create view: [{}] for project: [{}]", createViewData.getBranchName(), createViewData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_VIEW)) {
                    returnObject = new ClientRequestServerCreateView(createViewData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction());
                }
                break;
            case SERVER_DELETE_BRANCH:
                ClientRequestServerDeleteBranchData deleteViewData = (ClientRequestServerDeleteBranchData) object;
                LOGGER.info("Request delete view: [{}] for project: [{}]", deleteViewData.getBranchName(), deleteViewData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_VIEW)) {
                    returnObject = new ClientRequestServerDeleteView(deleteViewData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction());
                }
                break;
            case SERVER_SHUTDOWN:
                ClientRequestServerShutdownData serverShutdownData = (ClientRequestServerShutdownData) object;
                LOGGER.info("Request server shutdown: [{}]", serverShutdownData.getServerName());

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
        ClientRequestError clientRequestError = new ClientRequestError(action, message);

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
}
