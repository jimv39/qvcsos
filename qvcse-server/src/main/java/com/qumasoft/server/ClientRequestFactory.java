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

import com.qumasoft.qvcslib.ClientRequestAddDirectoryData;
import com.qumasoft.qvcslib.ClientRequestBreakLockData;
import com.qumasoft.qvcslib.ClientRequestChangePasswordData;
import com.qumasoft.qvcslib.ClientRequestCheckInData;
import com.qumasoft.qvcslib.ClientRequestCheckOutData;
import com.qumasoft.qvcslib.ClientRequestCreateArchiveData;
import com.qumasoft.qvcslib.ClientRequestDataInterface;
import com.qumasoft.qvcslib.ClientRequestDeleteDirectoryData;
import com.qumasoft.qvcslib.ClientRequestGetDirectoryData;
import com.qumasoft.qvcslib.ClientRequestGetForVisualCompareData;
import com.qumasoft.qvcslib.ClientRequestGetInfoForMergeData;
import com.qumasoft.qvcslib.ClientRequestGetLogfileInfoData;
import com.qumasoft.qvcslib.ClientRequestGetMostRecentActivityData;
import com.qumasoft.qvcslib.ClientRequestGetRevisionData;
import com.qumasoft.qvcslib.ClientRequestGetRevisionForCompareData;
import com.qumasoft.qvcslib.ClientRequestHeartBeatData;
import com.qumasoft.qvcslib.ClientRequestLabelData;
import com.qumasoft.qvcslib.ClientRequestLabelDirectoryData;
import com.qumasoft.qvcslib.ClientRequestListClientProjectsData;
import com.qumasoft.qvcslib.ClientRequestListClientViewsData;
import com.qumasoft.qvcslib.ClientRequestListFilesToPromoteData;
import com.qumasoft.qvcslib.ClientRequestLockData;
import com.qumasoft.qvcslib.ClientRequestLoginData;
import com.qumasoft.qvcslib.ClientRequestMoveFileData;
import com.qumasoft.qvcslib.ClientRequestOperationDataInterface;
import com.qumasoft.qvcslib.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.ClientRequestRegisterClientListenerData;
import com.qumasoft.qvcslib.ClientRequestRenameData;
import com.qumasoft.qvcslib.ClientRequestResolveConflictFromParentBranchData;
import com.qumasoft.qvcslib.ClientRequestServerAddUserData;
import com.qumasoft.qvcslib.ClientRequestServerAssignUserRolesData;
import com.qumasoft.qvcslib.ClientRequestServerCreateProjectData;
import com.qumasoft.qvcslib.ClientRequestServerCreateViewData;
import com.qumasoft.qvcslib.ClientRequestServerDeleteProjectData;
import com.qumasoft.qvcslib.ClientRequestServerDeleteRoleData;
import com.qumasoft.qvcslib.ClientRequestServerDeleteViewData;
import com.qumasoft.qvcslib.ClientRequestServerGetRoleNamesData;
import com.qumasoft.qvcslib.ClientRequestServerGetRolePrivilegesData;
import com.qumasoft.qvcslib.ClientRequestServerListProjectUsersData;
import com.qumasoft.qvcslib.ClientRequestServerListProjectsData;
import com.qumasoft.qvcslib.ClientRequestServerListUserRolesData;
import com.qumasoft.qvcslib.ClientRequestServerListUsersData;
import com.qumasoft.qvcslib.ClientRequestServerMaintainProjectData;
import com.qumasoft.qvcslib.ClientRequestServerRemoveUserData;
import com.qumasoft.qvcslib.ClientRequestServerShutdownData;
import com.qumasoft.qvcslib.ClientRequestServerUpdatePrivilegesData;
import com.qumasoft.qvcslib.ClientRequestSetAttributesData;
import com.qumasoft.qvcslib.ClientRequestSetCommentPrefixData;
import com.qumasoft.qvcslib.ClientRequestSetIsObsoleteData;
import com.qumasoft.qvcslib.ClientRequestSetModuleDescriptionData;
import com.qumasoft.qvcslib.ClientRequestSetRevisionDescriptionData;
import com.qumasoft.qvcslib.ClientRequestTransactionBeginData;
import com.qumasoft.qvcslib.ClientRequestTransactionEndData;
import com.qumasoft.qvcslib.ClientRequestUnDeleteData;
import com.qumasoft.qvcslib.ClientRequestUnLabelData;
import com.qumasoft.qvcslib.ClientRequestUnLabelDirectoryData;
import com.qumasoft.qvcslib.ClientRequestUnlockData;
import com.qumasoft.qvcslib.ClientRequestUpdateClientData;
import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.DefaultCompressor;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactory;
import com.qumasoft.qvcslib.ServerResponseMessage;
import com.qumasoft.qvcslib.Utility;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client request factory.
 *
 * @author Jim Voris
 */
public class ClientRequestFactory {

    // Create our logger object.
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private java.io.ObjectInputStream objectInputStreamMember;
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
            objectInputStreamMember = new java.io.ObjectInputStream(inStream);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
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
                LOGGER.log(Level.FINE, ">>>>>>>>>>>>>>>>>>>  Begin Transaction: [" + requestData.getTransactionID() + "] >>>>>>>>>>>>>>>>>>>");
            } else if (object instanceof ClientRequestTransactionEndData) {
                ClientRequestTransactionEndData requestData = (ClientRequestTransactionEndData) object;
                returnObject = new ClientRequestTransactionEnd(requestData);
                LOGGER.log(Level.FINE, "<<<<<<<<<<<<<<<<<<<  End Transaction: [" + requestData.getTransactionID() + "] <<<<<<<<<<<<<<<<<<<");
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
                        case LIST_CLIENT_VIEWS:
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
                        case SERVER_CREATE_VIEW:
                        case SERVER_DELETE_VIEW:
                        case SERVER_SHUTDOWN:
                            returnObject = handleOperationGroupE(operationType, object, request, responseFactory);
                            break;
                        default:
                            LOGGER.log(Level.WARNING, "Unexpected client request object: " + object.getClass().toString());
                            returnObject = new ClientRequestError("Unknown operation request", "Unexpected client request object: " + object.getClass().toString());
                            break;
                    }
                } else {
                    LOGGER.log(Level.WARNING, "Unexpected client request object: " + object.getClass().toString());
                    returnObject = new ClientRequestError("Unknown operation request", "Unexpected client request object: " + object.getClass().toString());
                }
            } else if (getIsUserLoggedIn() && !getClientVersionMatchesFlag()) {
                // The user is logged in but the versions don't match... Only process update requests.
                ClientRequestOperationDataInterface request = (ClientRequestOperationDataInterface) object;

                if (object instanceof ClientRequestUpdateClientData) {
                    ClientRequestUpdateClientData updateClientData = (ClientRequestUpdateClientData) object;
                    LOGGER.log(Level.INFO, "Request update client file: [" + updateClientData.getRequestedFileName() + "]");
                    returnObject = new ClientRequestUpdateClient(updateClientData);
                } else {
                    LOGGER.log(Level.WARNING, "ClientRequestFactory.createClientRequest not logged in for request: " + object.getClass().toString());
                    returnObject = new ClientRequestError("Not logged in!!", "Invalid operation request");
                }
            } else if (object instanceof ClientRequestLoginData) {
                // The user is not logged in.  We can process login requests,
                // change password requests, and begin/end transactions requests.
                ClientRequestLoginData loginRequestData = (ClientRequestLoginData) object;
                returnObject = new ClientRequestLogin(loginRequestData);
            } else {
                LOGGER.log(Level.WARNING, "ClientRequestFactory.createClientRequest not logged in for request: " + object.getClass().toString());
                returnObject = new ClientRequestError("Not logged in!!", "Invalid operation request");
            }
        } catch (java.io.EOFException e) {
            LOGGER.log(Level.WARNING, "ClientRequestFactory.createClientRequest EOF Detected.");
        } catch (java.net.SocketException e) {
            LOGGER.log(Level.WARNING, "ClientRequestFactory.createClientRequest socket exception: " + e.getLocalizedMessage());
        } catch (java.lang.OutOfMemoryError e) {
            // This should cause us to close the socket to the client that caused the problem.
            // Hopefully that will be enough to shed the memory load, and allow the server
            // to remain running.
            returnObject = null;
            LOGGER.log(Level.WARNING, "ClientRequestFactory.createClientRequest out of memory error!!!");
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "ClientRequestFactory.createClientRequest exception: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
        return returnObject;
    }

    private ClientRequestInterface handleOperationGroupA(ClientRequestDataInterface.RequestOperationType operationType, Object object, ClientRequestOperationDataInterface request,
            ServerResponseFactory responseFactory) {
        ClientRequestInterface returnObject = null;
        switch (operationType) {
            case LIST_CLIENT_PROJECTS:
                ClientRequestListClientProjectsData listClientProjectsData = (ClientRequestListClientProjectsData) object;
                returnObject = new ClientRequestListClientProjects(listClientProjectsData);
                break;
            case LIST_CLIENT_VIEWS:
                ClientRequestListClientViewsData listClientViewsData = (ClientRequestListClientViewsData) object;
                returnObject = new ClientRequestListClientViews(listClientViewsData);
                break;
            case LIST_PROJECTS:
                ClientRequestServerListProjectsData listProjectsData = (ClientRequestServerListProjectsData) object;
                LOGGER.log(Level.INFO, "Request list projects.");
                returnObject = new ClientRequestServerListProjects(listProjectsData);
                break;
            case LIST_USERS:
                ClientRequestServerListUsersData listUsersData = (ClientRequestServerListUsersData) object;
                LOGGER.log(Level.INFO, "Request list users.");
                returnObject = new ClientRequestServerListUsers(listUsersData);
                break;
            case CHANGE_USER_PASSWORD:
                ClientRequestChangePasswordData requestData = (ClientRequestChangePasswordData) object;
                returnObject = new ClientRequestChangePassword(requestData);
                break;
            case GET_REVISION:
                ClientRequestGetRevisionData getRevisionData = (ClientRequestGetRevisionData) object;
                LOGGER.log(Level.FINE, "Request get revision;  project name: [" + getRevisionData.getProjectName() + "] view name: [" + getRevisionData.getViewName()
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
                LOGGER.log(Level.FINE, "Request get directory; project name: [" + getDirectoryData.getProjectName() + "] view name: [" + getDirectoryData.getViewName()
                        + "] appended path: [" + getDirectoryData.getAppendedPath() + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.GET_DIRECTORY)) {
                    returnObject = new ClientRequestGetDirectory(getDirectoryData);
                } else {
                    returnObject = reportProblem(request, getDirectoryData.getAppendedPath(), null, responseFactory, RolePrivilegesManager.GET_DIRECTORY.getAction());
                }
                break;
            case GET_FOR_VISUAL_COMPARE:
                ClientRequestGetForVisualCompareData getForVisualCompareData = (ClientRequestGetForVisualCompareData) object;
                LOGGER.log(Level.FINE, "Request get for visual compare; project name: [" + getForVisualCompareData.getProjectName() + "] view name: ["
                        + getForVisualCompareData.getViewName() + "] appended path: [" + getForVisualCompareData.getAppendedPath() + "]");

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
                LOGGER.log(Level.FINE, "Request get revision for compare; project name: [" + getRevisionForCompareData.getProjectName()
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
                LOGGER.log(Level.FINE, "Request checkout; project name: [" + checkOutData.getProjectName() + "] view name: ["
                        + checkOutData.getViewName() + "] appended path: ["
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
                LOGGER.log(Level.FINE, "Request Info: checkin:" + checkInData.getAppendedPath() + " project name: " + checkInData.getProjectName());

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
                LOGGER.log(Level.FINE, "Request Info: lock:" + lockData.getAppendedPath() + " project name: " + lockData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LOCK)) {
                    returnObject = new ClientRequestLock(lockData);
                } else {
                    returnObject = reportProblem(request, lockData.getAppendedPath(), lockData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.LOCK.getAction());
                }
                break;
            case UNLOCK:
                ClientRequestUnlockData unlockData = (ClientRequestUnlockData) object;
                LOGGER.log(Level.FINE, "Request Info: unlock:" + unlockData.getAppendedPath() + " project name: " + unlockData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.UNLOCK)) {
                    returnObject = new ClientRequestUnlock(unlockData);
                } else {
                    returnObject = reportProblem(request, unlockData.getAppendedPath(), unlockData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.UNLOCK.getAction());
                }
                break;
            case BREAK_LOCK:
                ClientRequestBreakLockData breakLockData = (ClientRequestBreakLockData) object;
                LOGGER.log(Level.FINE, "Request Info: break lock:" + breakLockData.getAppendedPath() + " project name: " + breakLockData.getProjectName());

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
                LOGGER.log(Level.FINE, "Request Info: apply label:" + labelData.getAppendedPath() + " project name: " + labelData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LABEL)) {
                    returnObject = new ClientRequestLabel(labelData);
                } else {
                    returnObject = reportProblem(request, labelData.getAppendedPath(), labelData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.LABEL.getAction());
                }
                break;
            case LABEL_DIRECTORY:
                ClientRequestLabelDirectoryData labelDirectoryData = (ClientRequestLabelDirectoryData) object;
                LOGGER.log(Level.FINE, "Request Info: label directory:" + labelDirectoryData.getAppendedPath() + " project name: "
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
                LOGGER.log(Level.FINE, "Request Info: remove label:" + unLabelData.getAppendedPath() + " project name: " + unLabelData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.REMOVE_LABEL)) {
                    returnObject = new ClientRequestUnLabel(unLabelData);
                } else {
                    returnObject = reportProblem(request, unLabelData.getAppendedPath(), unLabelData.getCommandArgs().getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.REMOVE_LABEL.getAction());
                }
                break;
            case REMOVE_LABEL_DIRECTORY:
                ClientRequestUnLabelDirectoryData unLabelDirectoryData = (ClientRequestUnLabelDirectoryData) object;
                LOGGER.log(Level.FINE, "Request Info: remove label from directory:" + unLabelDirectoryData.getAppendedPath()
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
                LOGGER.log(Level.FINE, "Request Info: rename file for directory:" + clientRequestRenameData.getAppendedPath() + " project name: "
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
                LOGGER.log(Level.FINE, "Request Info: move file for directory:" + clientRequestMoveFileData.getOriginalAppendedPath()
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
                ClientRequestSetIsObsoleteData clientRequestSetIsObsoleteData = (ClientRequestSetIsObsoleteData) object;
                LOGGER.log(Level.FINE, "Request Info: set obsolete:" + clientRequestSetIsObsoleteData.getAppendedPath()
                        + " project name: " + clientRequestSetIsObsoleteData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SET_OBSOLETE)) {
                    returnObject = new ClientRequestSetIsObsolete(clientRequestSetIsObsoleteData);
                } else {
                    returnObject = reportProblem(request, clientRequestSetIsObsoleteData.getAppendedPath(),
                            clientRequestSetIsObsoleteData.getShortWorkfileName(), responseFactory,
                            RolePrivilegesManager.SET_OBSOLETE.getAction());
                }
                break;
            case UNDELETE_FILE:
                ClientRequestUnDeleteData clientRequestUnDeleteData = (ClientRequestUnDeleteData) object;
                LOGGER.log(Level.FINE, "Request Info: undelete:" + clientRequestUnDeleteData.getAppendedPath()
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
                LOGGER.log(Level.FINE, "Request Info: set attributes:" + clientRequestSetAttributesData.getAppendedPath()
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
                LOGGER.log(Level.FINE, "Request Info: set comment prefix:" + clientRequestSetCommentPrefixData.getAppendedPath()
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
                LOGGER.log(Level.FINE, "Request Info: set module description:" + clientRequestSetModuleDescriptionData.getAppendedPath()
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
                LOGGER.log(Level.FINE, "Request Info: set revision description:" + clientRequestSetRevisionDescriptionData.getAppendedPath()
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
                LOGGER.log(Level.FINE, "Request Info: get logfile info:" + clientRequestGetLogfileInfoData.getAppendedPath() + " project name: "
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
                LOGGER.log(Level.INFO, "Request register client listener; project name: [" + registerClientListenerData.getProjectName()
                        + "] view name: [" + registerClientListenerData.getViewName() + "] appended path: [" + registerClientListenerData.getAppendedPath() + "]");

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
                String fullFileName = Utility.formatFilenameForActivityJournal(createArchiveData.getProjectName(), createArchiveData.getViewName(),
                        createArchiveData.getAppendedPath(),
                        createArchiveData.getCommandArgs().getWorkfileName());
                LOGGER.log(Level.INFO, "Request create archive for file: [" + fullFileName + "]");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.CREATE_ARCHIVE)) {
                    returnObject = new ClientRequestCreateArchive(createArchiveData);
                } else {
                    returnObject = reportProblem(request, createArchiveData.getAppendedPath(), null, responseFactory, RolePrivilegesManager.CREATE_ARCHIVE.getAction());
                }
                break;
            case ADD_DIRECTORY:
                ClientRequestAddDirectoryData addDirectoryData = (ClientRequestAddDirectoryData) object;
                String appendedPath = addDirectoryData.getAppendedPath();
                LOGGER.log(Level.INFO, " project name: [" + addDirectoryData.getProjectName() + "] Request Info: add directory: [" + appendedPath + "]");

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
                LOGGER.log(Level.INFO, " project name: " + deleteDirectoryData.getProjectName() + "Request Info: delete directory:" + appendedPath);

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.DELETE_DIRECTORY)) {
                    returnObject = new ClientRequestDeleteDirectory(deleteDirectoryData);
                } else {
                    returnObject = reportProblem(request, deleteDirectoryData.getAppendedPath(), null, responseFactory,
                            RolePrivilegesManager.DELETE_DIRECTORY.getAction());
                }
                break;
            case GET_INFO_FOR_MERGE:
                ClientRequestGetInfoForMergeData clientRequestGetInfoForMergeData = (ClientRequestGetInfoForMergeData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.MERGE_FROM_PARENT)) {
                    returnObject = new ClientRequestGetInfoForMerge(clientRequestGetInfoForMergeData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.MERGE_FROM_PARENT.getAction());
                }
                break;
            case RESOLVE_CONFLICT_FROM_PARENT_BRANCH:
                ClientRequestResolveConflictFromParentBranchData clientRequestResolveConflictFromParentBranchData
                        = (ClientRequestResolveConflictFromParentBranchData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.MERGE_FROM_PARENT)) {
                    returnObject = new ClientRequestResolveConflictFromParentBranch(clientRequestResolveConflictFromParentBranchData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.MERGE_FROM_PARENT.getAction());
                }
                break;
            case LIST_FILES_TO_PROMOTE:
                ClientRequestListFilesToPromoteData clientRequestListFilesToPromoteData = (ClientRequestListFilesToPromoteData) object;
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.PROMOTE_TO_PARENT)) {
                    returnObject = new ClientRequestListFilesToPromote(clientRequestListFilesToPromoteData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.PROMOTE_TO_PARENT.getAction());
                }
                break;
            case PROMOTE_FILE:
                ClientRequestPromoteFileData clientRequestPromoteFilesData = (ClientRequestPromoteFileData) object;
                LOGGER.log(Level.INFO, "Request promote file; project name: [" + clientRequestPromoteFilesData.getProjectName() + "] view name: ["
                        + clientRequestPromoteFilesData.getViewName() + "] appended path: [" + clientRequestPromoteFilesData.getFilePromotionInfo().getAppendedPath()
                        + "] file name: [" + clientRequestPromoteFilesData.getFilePromotionInfo().getShortWorkfileName() + "]");
                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.PROMOTE_TO_PARENT)) {
                    returnObject = new ClientRequestPromoteFile(clientRequestPromoteFilesData);
                } else {
                    returnObject = reportProblem(request, "", null, responseFactory, RolePrivilegesManager.PROMOTE_TO_PARENT.getAction());
                }
                break;
            case ADD_USER:
                ClientRequestServerAddUserData addUserData = (ClientRequestServerAddUserData) object;
                LOGGER.log(Level.INFO, "Request add user: " + addUserData.getUserName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerAddUser(addUserData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Add user");
                }
                break;
            case REMOVE_USER:
                ClientRequestServerRemoveUserData removeUserData = (ClientRequestServerRemoveUserData) object;
                LOGGER.log(Level.INFO, "Request remove user: " + removeUserData.getUserName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerRemoveUser(removeUserData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Remove user");
                }
                break;
            case ASSIGN_USER_ROLES:
                ClientRequestServerAssignUserRolesData assignUserRoleData = (ClientRequestServerAssignUserRolesData) object;
                LOGGER.log(Level.INFO, "Request assign user roles for user: " + assignUserRoleData.getUserName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.ASSIGN_USER_ROLES)) {
                    returnObject = new ClientRequestServerAssignUserRoles(assignUserRoleData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.ASSIGN_USER_ROLES.getAction());
                }
                break;
            case LIST_PROJECT_USERS:
                ClientRequestServerListProjectUsersData listProjectUsersData = (ClientRequestServerListProjectUsersData) object;
                LOGGER.log(Level.INFO, "Request list project users.");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LIST_PROJECT_USERS)) {
                    returnObject = new ClientRequestServerListProjectUsers(listProjectUsersData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.LIST_PROJECT_USERS.getAction());
                }
                break;
            case GET_MOST_RECENT_ACTIVITY:
                ClientRequestGetMostRecentActivityData clientRequestGetMostRecentActivityData = (ClientRequestGetMostRecentActivityData) object;
                LOGGER.log(Level.INFO, "Request get most recent activity.");

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
                LOGGER.log(Level.INFO, "Request list user roles.");

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.LIST_USER_ROLES)) {
                    returnObject = new ClientRequestServerListUserRoles(listUserRolesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.LIST_USER_ROLES.getAction());
                }
                break;
            case SERVER_GET_ROLES:
                ClientRequestServerGetRoleNamesData clientRequestServerGetRoleNamesData = (ClientRequestServerGetRoleNamesData) object;
                LOGGER.log(Level.INFO, "Request list user roles.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerGetRoleNames(clientRequestServerGetRoleNamesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Get roles");
                }
                break;
            case SERVER_GET_ROLE_PRIVILEGES:
                ClientRequestServerGetRolePrivilegesData clientRequestServerGetRolePrivilegesData = (ClientRequestServerGetRolePrivilegesData) object;
                LOGGER.log(Level.INFO, "Request list role privileges.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerGetRolePrivileges(clientRequestServerGetRolePrivilegesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Get role privileges");
                }
                break;
            case SERVER_UPDATE_ROLE_PRIVILEGES:
                ClientRequestServerUpdatePrivilegesData clientRequestServerUpdatePrivilegesData = (ClientRequestServerUpdatePrivilegesData) object;
                LOGGER.log(Level.INFO, "Request update role privileges.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerUpdatePrivileges(clientRequestServerUpdatePrivilegesData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Update role privileges");
                }
                break;
            case SERVER_DELETE_ROLE:
                ClientRequestServerDeleteRoleData clientRequestServerDeleteRoleData = (ClientRequestServerDeleteRoleData) object;
                LOGGER.log(Level.INFO, "Request delete role.");

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerDeleteRole(clientRequestServerDeleteRoleData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Delete role");
                }
                break;
            case SERVER_CREATE_PROJECT:
                ClientRequestServerCreateProjectData createProjectData = (ClientRequestServerCreateProjectData) object;
                LOGGER.log(Level.INFO, "Request create project: " + createProjectData.getNewProjectName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerCreateProject(createProjectData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Create project");
                }
                break;
            case SERVER_DELETE_PROJECT:
                ClientRequestServerDeleteProjectData deleteProjectData = (ClientRequestServerDeleteProjectData) object;
                LOGGER.log(Level.INFO, "Request delete project: " + deleteProjectData.getDeleteProjectName());

                if (0 == getUserName().compareTo(RoleManager.ADMIN)) {
                    returnObject = new ClientRequestServerDeleteProject(deleteProjectData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, "Delete project");
                }
                break;
            case SERVER_MAINTAIN_PROJECT:
                ClientRequestServerMaintainProjectData maintainProjectData = (ClientRequestServerMaintainProjectData) object;
                LOGGER.log(Level.INFO, "Request maintain project: " + maintainProjectData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_PROJECT)) {
                    returnObject = new ClientRequestServerMaintainProject(maintainProjectData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.SERVER_MAINTAIN_PROJECT.getAction());
                }
                break;
            case SERVER_CREATE_VIEW:
                ClientRequestServerCreateViewData createViewData = (ClientRequestServerCreateViewData) object;
                LOGGER.log(Level.INFO, "Request create view: " + createViewData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_VIEW)) {
                    returnObject = new ClientRequestServerCreateView(createViewData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction());
                }
                break;
            case SERVER_DELETE_VIEW:
                ClientRequestServerDeleteViewData deleteViewData = (ClientRequestServerDeleteViewData) object;
                LOGGER.log(Level.INFO, "Request delete view: " + deleteViewData.getProjectName());

                if (isUserPrivileged(request.getProjectName(), RolePrivilegesManager.SERVER_MAINTAIN_VIEW)) {
                    returnObject = new ClientRequestServerDeleteView(deleteViewData);
                } else {
                    returnObject = reportProblem(request, null, null, responseFactory, RolePrivilegesManager.SERVER_MAINTAIN_VIEW.getAction());
                }
                break;
            case SERVER_SHUTDOWN:
                ClientRequestServerShutdownData serverShutdownData = (ClientRequestServerShutdownData) object;
                LOGGER.log(Level.INFO, "Request server shutdown: " + serverShutdownData.getServerName());

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
        Compressor decompressor = new DefaultCompressor();
        byte[] compressedInput = (byte[]) object;
        byte[] expandedBuffer = decompressor.expand(compressedInput);
        ByteArrayInputStream byteInputStream = new ByteArrayInputStream(expandedBuffer);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteInputStream);
            retVal = objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Caught exception trying to decompress an object: " + e.getClass().toString() + " " + e.getLocalizedMessage());
        }
        return retVal;
    }

    private ClientRequestError reportProblem(ClientRequestOperationDataInterface request, String appendedPath, String shortWorkfileName, ServerResponseFactory responseFactory,
            String action) {
        String message = "Unauthorized client request: [" + action + "]. User [" + getUserName() + "] is not authorized to perform requested operation for project : ["
                + request.getProjectName() + "]";
        LOGGER.log(Level.WARNING, message);

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
                    request.getViewName(),
                    appendedPath, ServerResponseMessage.MEDIUM_PRIORITY);
            alternateResponse.setShortWorkfileName(shortWorkfileName);
            clientRequestError.setAlternateResponseObject(alternateResponse);
        }

        return clientRequestError;
    }
}
