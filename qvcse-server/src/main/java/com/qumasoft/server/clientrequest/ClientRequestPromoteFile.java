/*   Copyright 2004-2015 Jim Voris
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
import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestPromoteFileData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseMessage;
import com.qumasoft.qvcslib.response.ServerResponsePromoteFile;
import com.qumasoft.server.ArchiveDirManager;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.ArchiveInfoForTranslucentBranch;
import com.qumasoft.server.DatabaseCache;
import com.qumasoft.server.FileIDDictionary;
import com.qumasoft.server.FileIDInfo;
import com.qumasoft.server.ServerTransactionManager;
import com.qumasoft.server.ServerUtility;
import com.qumasoft.server.dataaccess.FileDAO;
import com.qumasoft.server.dataaccess.PromotionCandidateDAO;
import com.qumasoft.server.dataaccess.impl.FileDAOImpl;
import com.qumasoft.server.dataaccess.impl.PromotionCandidateDAOImpl;
import com.qumasoft.server.datamodel.PromotionCandidate;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Promote a file.
 *
 * @author Jim Voris
 */
class ClientRequestPromoteFile implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestPromoteFile.class);
    private final ClientRequestPromoteFileData request;
    private final MutableByteArray commonAncestorBuffer = new MutableByteArray();
    private final MutableByteArray branchParentTipRevisionBuffer = new MutableByteArray();
    private final MutableByteArray branchTipRevisionBuffer = new MutableByteArray();


    /**
     * Creates a new instance of ClientRequestPromoteFile.
     *
     * @param data the command line data, etc.
     */
    public ClientRequestPromoteFile(ClientRequestPromoteFileData data) {
        request = data;
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        int fileId = request.getFileID();
        FilePromotionInfo filePromotionInfo = request.getFilePromotionInfo();

        // Lookup the file.
        FileIDInfo fileIDInfo = FileIDDictionary.getInstance().lookupFileIDInfo(projectName, viewName, fileId);
        if (fileIDInfo != null) {
            try {
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(projectName, viewName, filePromotionInfo.getAppendedPath());
                ArchiveDirManagerInterface directoryManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                        directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
                LOGGER.info("Promote file: project name: [" + projectName + "] branch name: [" + viewName + "] appended path: ["
                        + filePromotionInfo.getAppendedPath() + "] short workfile name: [" + filePromotionInfo.getShortWorkfileName() + "]");
                ArchiveInfoInterface archiveInfo = directoryManager.getArchiveInfo(filePromotionInfo.getShortWorkfileName());
                if (archiveInfo != null) {
                    if (archiveInfo instanceof ArchiveInfoForTranslucentBranch) {
                        ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch = (ArchiveInfoForTranslucentBranch) archiveInfo;
                        Date date = ServerTransactionManager.getInstance().getTransactionTimeStamp(response);
                        ServerResponsePromoteFile serverResponsePromoteFile;
                        switch (filePromotionInfo.getTypeOfMerge()) {
                            case SIMPLE_MERGE_TYPE:
                                serverResponsePromoteFile = buildResponseData(fileIDInfo, archiveInfoForTranslucentBranch);
                                if (archiveInfoForTranslucentBranch.promoteFile(userName, date)) {
                                    returnObject = handleSimpleMerge(archiveInfoForTranslucentBranch, serverResponsePromoteFile);
                                } else {
                                    returnObject = buildPromoteFailedErrorMessage();
                                }
                                break;
                            case CHILD_CREATED_MERGE_TYPE:
                                serverResponsePromoteFile = buildResponseDataForCreate(fileIDInfo, archiveInfoForTranslucentBranch);
                                returnObject = handleChildCreatedMerge(archiveInfoForTranslucentBranch, serverResponsePromoteFile, response);
                                break;
                            default:
                                // Return an error message.
                                ServerResponseMessage message = new ServerResponseMessage("Merge type is not supported yet: [" + filePromotionInfo.getTypeOfMerge()
                                        + "]", projectName,
                                        viewName, filePromotionInfo.getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                                message.setShortWorkfileName(filePromotionInfo.getShortWorkfileName());
                                LOGGER.warn(message.getMessage());
                                returnObject = message;
                                break;
                        }
                    } else {
                        // Return an error message.
                        ServerResponseMessage message = new ServerResponseMessage("Promote file is only supported for translucent branches.", projectName, viewName,
                                filePromotionInfo.getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                        message.setShortWorkfileName(filePromotionInfo.getShortWorkfileName());
                        LOGGER.warn(message.getMessage());
                        returnObject = message;
                    }
                } else {
                    // Return an error message.
                    ServerResponseMessage message = new ServerResponseMessage("Archive not found for " + filePromotionInfo.getShortWorkfileName(), projectName, viewName,
                            filePromotionInfo.getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                    message.setShortWorkfileName(filePromotionInfo.getShortWorkfileName());
                    LOGGER.warn(message.getMessage());
                    returnObject = message;
                }
            } catch (QVCSException | IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);

                // Return an error message.
                ServerResponseMessage message = new ServerResponseMessage("Caught exception trying to promote a file: [" + filePromotionInfo.getShortWorkfileName()
                        + "]. Exception string: " + e.getMessage(), projectName, viewName, filePromotionInfo.getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
                message.setShortWorkfileName(filePromotionInfo.getShortWorkfileName());
                returnObject = message;
            }
        } else {
            // Return an error message.
            ServerResponseMessage message = new ServerResponseMessage("Did not find file information for file id: [" + fileId + "]", projectName, viewName,
                    filePromotionInfo.getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
            message.setShortWorkfileName(filePromotionInfo.getShortWorkfileName());
            LOGGER.warn(message.getMessage());
            returnObject = message;
        }
        return returnObject;
    }

    private ServerResponseMessage buildPromoteFailedErrorMessage() {
        ServerResponseMessage message = new ServerResponseMessage("Promote file failed for " + request.getFilePromotionInfo().getShortWorkfileName(), request.getProjectName(),
                request.getViewName(), request.getFilePromotionInfo().getAppendedPath(), ServerResponseMessage.HIGH_PRIORITY);
        message.setShortWorkfileName(request.getFilePromotionInfo().getShortWorkfileName());
        LOGGER.warn(message.getMessage());
        return message;
    }

    /**
     * Build the data that goes into the response message. This is where we perform the merge to a temp file and discover it that
     * merge is successful, etc.
     *
     * @param archiveInfoForTranslucentBranch the archive info for the translucent branch.
     *
     * @return a populated response filled in with those 'files' that the client will need to complete the merge.
     */
    private ServerResponsePromoteFile buildResponseData(FileIDInfo fileIDInfo, ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch) throws QVCSException, IOException {
        ServerResponsePromoteFile serverResponsePromoteFile = new ServerResponsePromoteFile();
        serverResponsePromoteFile.setAppendedPath(fileIDInfo.getAppendedPath());
        serverResponsePromoteFile.setBranchName(request.getMergedInfoBranchName());
        serverResponsePromoteFile.setProjectName(request.getProjectName());
        serverResponsePromoteFile.setShortWorkfileName(fileIDInfo.getShortFilename());
        serverResponsePromoteFile.setMergeType(request.getFilePromotionInfo().getTypeOfMerge());

        byte[] mergedResultBuffer = ServerUtility.createMergedResultBuffer(archiveInfoForTranslucentBranch, commonAncestorBuffer,
                branchTipRevisionBuffer, branchParentTipRevisionBuffer);
        if (mergedResultBuffer != null) {
            serverResponsePromoteFile.setMergedResultBuffer(mergedResultBuffer);
        } else {
            serverResponsePromoteFile.setBranchTipRevisionBuffer(branchTipRevisionBuffer.getValue());
            serverResponsePromoteFile.setCommonAncestorBuffer(commonAncestorBuffer.getValue());
            serverResponsePromoteFile.setBranchParentTipRevisionBuffer(branchParentTipRevisionBuffer.getValue());
        }
        return serverResponsePromoteFile;
    }

    private ServerResponsePromoteFile buildResponseDataForCreate(FileIDInfo fileIDInfo,
                                                                 ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch) throws QVCSException, IOException {
        ServerResponsePromoteFile serverResponsePromoteFile = new ServerResponsePromoteFile();
        serverResponsePromoteFile.setAppendedPath(fileIDInfo.getAppendedPath());
        serverResponsePromoteFile.setBranchName(request.getMergedInfoBranchName());
        serverResponsePromoteFile.setProjectName(request.getProjectName());
        serverResponsePromoteFile.setShortWorkfileName(fileIDInfo.getShortFilename());
        serverResponsePromoteFile.setMergeType(request.getFilePromotionInfo().getTypeOfMerge());

        serverResponsePromoteFile.setMergedResultBuffer(archiveInfoForTranslucentBranch.getCurrentLogFile()
                .getRevisionAsByteArray(archiveInfoForTranslucentBranch.getBranchTipRevisionString()));
        return serverResponsePromoteFile;
    }

    /**
     * Handle the simple merge use case.
     *
     * @param archiveInfoForTranslucentBranch the archive info for the translucent branch.
     * @param serverResponsePromoteFile the response that we're building.
     * @return the completed response.
     * @throws QVCSException if we cannot delete the promotion candidate record.
     */
    private ServerResponseInterface handleSimpleMerge(ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch,
                                                      ServerResponsePromoteFile serverResponsePromoteFile) throws QVCSException {
        deletePromotionCandidate(archiveInfoForTranslucentBranch);

        // Send back the logfile info if it's needed for keyword expansion.
        if (archiveInfoForTranslucentBranch.getAttributes().getIsExpandKeywords()) {
            serverResponsePromoteFile.setLogfileInfo(archiveInfoForTranslucentBranch.getLogfileInfo());
        }
        LogFileInterface logFileInterface = (LogFileInterface) archiveInfoForTranslucentBranch;
        serverResponsePromoteFile.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator,
                logFileInterface.getIsObsolete(), logFileInterface.getDefaultRevisionDigest(), archiveInfoForTranslucentBranch.getShortWorkfileName(),
                archiveInfoForTranslucentBranch.getIsOverlap()));
        return serverResponsePromoteFile;
    }

    private void deletePromotionCandidate(ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch) throws QVCSException {
        PromotionCandidateDAO promotionCandidateDAO = new PromotionCandidateDAOImpl();
        try {
            Integer projectId = DatabaseCache.getInstance().getProjectId(request.getProjectName());
            Integer branchId = DatabaseCache.getInstance().getBranchId(projectId, request.getViewName());
            PromotionCandidate promotionCandidate = new PromotionCandidate(archiveInfoForTranslucentBranch.getFileID(), branchId);
            promotionCandidateDAO.delete(promotionCandidate);
        } catch (SQLException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new QVCSException("Failed to delete promotion candidate record for [" + archiveInfoForTranslucentBranch.getShortWorkfileName() + "]");
        }
    }

    private ServerResponseInterface handleChildCreatedMerge(ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch, ServerResponsePromoteFile serverResponsePromoteFile,
            ServerResponseFactoryInterface response) throws QVCSException {
        try {
            // Step 1: Delete promotion candidate row.
            deletePromotionCandidate(archiveInfoForTranslucentBranch);

            // Step 2: If parent is trunk: move archive file from branch archive directory to correct appended path and rename it to have the right name.
            if (request.getParentBranchName().equals(QVCSConstants.QVCS_TRUNK_VIEW)) {
                // Make sure the target directory manager exists.
                DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(request.getProjectName(), QVCSConstants.QVCS_TRUNK_VIEW,
                        request.getFilePromotionInfo().getAppendedPath());
                ArchiveDirManagerInterface targetArchiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                        directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);

                ArchiveDirManagerInterface branchArchiveDirManagerInterface = ServerUtility.getBranchArchiveDirManager(request.getProjectName(), response);
                ArchiveDirManager branchArchiveDirManager = (ArchiveDirManager) branchArchiveDirManagerInterface;
                String shortWorkfilenameInBranchArchiveDirectory = lookupShortWorkfilenameForBranchArchive(branchArchiveDirManager, request.getFilePromotionInfo().getFileId());
                if (branchArchiveDirManager.moveArchive(request.getUserName(), shortWorkfilenameInBranchArchiveDirectory, targetArchiveDirManager, response)) {
                    // And now we have to rename the file in its new home.
                    if (!targetArchiveDirManager.renameArchive(request.getUserName(), shortWorkfilenameInBranchArchiveDirectory,
                            request.getFilePromotionInfo().getShortWorkfileName(), response)) {
                        throw new QVCSException("Rename failed when promoting file to trunk.");
                    }
                    LogFileInterface logFileInterface = (LogFileInterface) archiveInfoForTranslucentBranch;
                    serverResponsePromoteFile.setSkinnyLogfileInfo(new SkinnyLogfileInfo(logFileInterface.getLogfileInfo(), File.separator,
                            logFileInterface.getIsObsolete(), logFileInterface.getDefaultRevisionDigest(), archiveInfoForTranslucentBranch.getShortWorkfileName(),
                            archiveInfoForTranslucentBranch.getIsOverlap()));
                }
            } else {
                // Step 3: Update file record to identify branch id as the parent's branch id.
                Integer projectId = DatabaseCache.getInstance().getProjectId(request.getProjectName());
                Integer branchId = DatabaseCache.getInstance().getBranchId(projectId, request.getViewName());
                Integer parentBranchId = DatabaseCache.getInstance().getBranchId(projectId, request.getParentBranchName());
                FileDAO fileDAO = new FileDAOImpl();
                com.qumasoft.server.datamodel.File file = fileDAO.findById(branchId, archiveInfoForTranslucentBranch.getFileID());
                file.setBranchId(parentBranchId);
                fileDAO.update(file, false);

                // Step 4: Update parent's archive directory manager to include this file.
                // Step 5: Make sure all listeners are adjusted correctly: parent's archive directory manager should now be a listener of of the 'moved' archive info,
                //         child archive info should be a listener of the moved (parent) archive info, child's archive directory manager should be a listener to the child's
                //         archive info.
            }
        } catch (IOException e) {
            throw new QVCSException("File move failed: " + Utility.expandStackTraceToString(e));
        } catch (SQLException e) {
            throw new QVCSException("File update failed: " + Utility.expandStackTraceToString(e));
        }
        return serverResponsePromoteFile;
    }

    private String lookupShortWorkfilenameForBranchArchive(ArchiveDirManager branchArchiveDirManager, Integer fileId) {
        String shortWorkfileName = null;
        Collection<ArchiveInfoInterface> archiveInfoCollection = branchArchiveDirManager.getArchiveInfoCollection().values();
        int fileID = fileId;
        for (ArchiveInfoInterface archiveInfo : archiveInfoCollection) {
            if (archiveInfo.getFileID() == fileID) {
                shortWorkfileName = archiveInfo.getShortWorkfileName();
            }
        }
        return shortWorkfileName;
    }
}
