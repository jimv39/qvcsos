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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.requestdata.ClientRequestResolveConflictFromParentBranchData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponseResolveConflictFromParentBranch;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ArchiveInfoForTranslucentBranch;
import com.qumasoft.server.FileIDDictionary;
import com.qumasoft.server.FileIDInfo;
import com.qumasoft.server.ServerTransactionManager;
import com.qumasoft.server.ServerUtility;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Client request resolve conflict from parent branch.</p> <p>Do the work to remove a conflict with the parent branch. Basically,
 * all we need to do is remove the branch label from the archive file... but there is also data that the client will need in order
 * to complete the work, and we gather that additional data here, as well as remove the branch label from the archive file.</p>
 *
 * @author Jim Voris
 */
class ClientRequestResolveConflictFromParentBranch implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestResolveConflictFromParentBranch.class);
    private final ClientRequestResolveConflictFromParentBranchData request;
    private final MutableByteArray commonAncestorBuffer = new MutableByteArray();
    private final MutableByteArray branchParentTipRevisionBuffer = new MutableByteArray();
    private final MutableByteArray branchTipRevisionBuffer = new MutableByteArray();

    ClientRequestResolveConflictFromParentBranch(ClientRequestResolveConflictFromParentBranchData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        int fileId = request.getFileID();
        // Lookup the file.
        FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(projectName, viewName, fileId);
        if (fileIDInfo != null) {
            try {
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, fileIDInfo.getAppendedPath());
                ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                        directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
                LOGGER.info("Resolve conflict from parent branch -- project name: [" + projectName + "] branch name: [" + viewName + "] appended path: ["
                        + fileIDInfo.getAppendedPath() + "] short workfile name: [" + fileIDInfo.getShortFilename() + "]");
                ArchiveInfoInterface archiveInfo = directoryManager.getArchiveInfo(fileIDInfo.getShortFilename());
                if (archiveInfo != null) {
                    if (archiveInfo instanceof ArchiveInfoForTranslucentBranch) {
                        ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch = (ArchiveInfoForTranslucentBranch) archiveInfo;
                        Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);
                        ServerResponseResolveConflictFromParentBranch serverResponseResolveConflictFromParentBranch = buildResponseData(fileIDInfo,
                                archiveInfoForTranslucentBranch);
                        if (archiveInfoForTranslucentBranch.resolveConflictFromParentBranch(userName, date)) {
                            // Send back the logfile info if it's needed for keyword expansion.
                            if (archiveInfoForTranslucentBranch.getAttributes().getIsExpandKeywords()) {
                                serverResponseResolveConflictFromParentBranch.setLogfileInfo(archiveInfoForTranslucentBranch.getLogfileInfo());
                            }
                            LogFileInterface logFileInterface = (LogFileInterface) archiveInfoForTranslucentBranch;
                            serverResponseResolveConflictFromParentBranch.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator,
                                    logFileInterface.getIsObsolete(), logFileInterface.getDefaultRevisionDigest(), archiveInfoForTranslucentBranch.getShortWorkfileName(),
                                    archiveInfoForTranslucentBranch.getIsOverlap()));
                            returnObject = serverResponseResolveConflictFromParentBranch;
                        } else {
                            // Return an error message.
                            ServerResponseMessage message = new ServerResponseMessage("Did not succeed in removing branch label for some reason. "
                                    + "Check the Trunk; maybe is was already removed?.",
                                    projectName, viewName, fileIDInfo.getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                            message.setShortWorkfileName(fileIDInfo.getShortFilename());
                            LOGGER.warn(message.getMessage());
                            returnObject = message;
                        }
                    } else {
                        // Return an error message.
                        ServerResponseMessage message = new ServerResponseMessage("Resolve conflict from parent branch is only supported for translucent branches.",
                                projectName, viewName, fileIDInfo.getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(fileIDInfo.getShortFilename());
                        LOGGER.warn(message.getMessage());
                        returnObject = message;
                    }
                } else {
                    // Return an error message.
                    ServerResponseMessage message = new ServerResponseMessage("Archive not found for [" + fileIDInfo.getShortFilename() + "]",
                            projectName, viewName, fileIDInfo.getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(fileIDInfo.getShortFilename());
                    LOGGER.warn(message.getMessage());
                    returnObject = message;
                }
            } catch (QVCSException | IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);

                // Return an error message.
                ServerResponseMessage message = new ServerResponseMessage("Caught exception trying to resolve conflict from parent branch for file: ["
                        + fileIDInfo.getShortFilename()
                        + "]. Exception string: " + e.getMessage(),
                        projectName, viewName, fileIDInfo.getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName(fileIDInfo.getShortFilename());
                returnObject = message;
            }
        } else {
            // Return an error message.
            ServerResponseMessage message = new ServerResponseMessage("Did not find file information for file id: [" + fileId + "]",
                    projectName, viewName, "", ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName("UNKNOWN");
            LOGGER.warn(message.getMessage());
            returnObject = message;
        }
        return returnObject;
    }

    /**
     * Build the data that goes into the response message. This is where we perform the merge to a temp file and discover it that
     * merge is successful, etc.
     *
     * @param archiveInfoForTranslucentBranch the archive info for the translucent branch.
     *
     * @return a populated response filled in with those 'files' that the client will need to complete the merge.
     */
    private ServerResponseResolveConflictFromParentBranch buildResponseData(FileIDInfo fileIDInfo, ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch)
            throws QVCSException, IOException {
        ServerResponseResolveConflictFromParentBranch serverResponseResolveConflictFromParentBranch = new ServerResponseResolveConflictFromParentBranch();
        serverResponseResolveConflictFromParentBranch.setAppendedPath(fileIDInfo.getAppendedPath());
        serverResponseResolveConflictFromParentBranch.setBranchName(request.getViewName());
        serverResponseResolveConflictFromParentBranch.setProjectName(request.getProjectName());
        serverResponseResolveConflictFromParentBranch.setShortWorkfileName(fileIDInfo.getShortFilename());

        byte[] mergedResultBuffer = ServerUtility.createMergedResultBuffer(archiveInfoForTranslucentBranch, commonAncestorBuffer, branchTipRevisionBuffer,
                branchParentTipRevisionBuffer);
        if (mergedResultBuffer != null) {
            serverResponseResolveConflictFromParentBranch.setMergedResultBuffer(mergedResultBuffer);
            serverResponseResolveConflictFromParentBranch.setBranchParentTipRevisionBuffer(branchParentTipRevisionBuffer.getValue());
        } else {
            serverResponseResolveConflictFromParentBranch.setBranchParentTipRevisionBuffer(branchParentTipRevisionBuffer.getValue());
            serverResponseResolveConflictFromParentBranch.setBranchTipRevisionBuffer(branchTipRevisionBuffer.getValue());
            serverResponseResolveConflictFromParentBranch.setCommonAncestorBuffer(commonAncestorBuffer.getValue());
        }

        return serverResponseResolveConflictFromParentBranch;
    }
}
