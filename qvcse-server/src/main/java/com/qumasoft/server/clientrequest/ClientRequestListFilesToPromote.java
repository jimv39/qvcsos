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
import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.InfoForMerge;
import com.qumasoft.qvcslib.MergeType;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestListFilesToPromoteData;
import com.qumasoft.qvcslib.response.ServerResponseError;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListFilesToPromote;
import com.qumasoft.server.ArchiveDirManagerFactoryForServer;
import com.qumasoft.server.BranchManager;
import com.qumasoft.server.DatabaseCache;
import com.qumasoft.server.MergeTypeHelper;
import com.qumasoft.server.ProjectBranch;
import com.qumasoft.server.QVCSEnterpriseServer;
import com.qumasoft.server.dataaccess.FileDAO;
import com.qumasoft.server.dataaccess.impl.FileDAOImpl;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Get the list of files that have been modified on the requested branch.
 *
 * @author Jim Voris
 */
class ClientRequestListFilesToPromote implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ClientRequestListFilesToPromote.class);
    private final ClientRequestListFilesToPromoteData request;
    private FileDAO fileDAO = null;
    private final MergeTypeHelper mergeTypeHelper;

    ClientRequestListFilesToPromote(ClientRequestListFilesToPromoteData data) {
        request = data;
        this.fileDAO = new FileDAOImpl(QVCSEnterpriseServer.getDatabaseManager().getSchemaName());
        this.mergeTypeHelper = new MergeTypeHelper(request.getProjectName(), request.getBranchName());
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String branchName = request.getBranchName();
        Integer projectId = DatabaseCache.getInstance().getProjectId(projectName);
        Integer branchId = DatabaseCache.getInstance().getBranchId(projectId, branchName);
        ProjectBranch projectBranch = BranchManager.getInstance().getBranch(projectName, branchName);
        String parentBranchName = projectBranch.getRemoteBranchProperties().getBranchParent();
        List<FilePromotionInfo> filePromotionInfoList = fileDAO.findFilePromotionInfoByBranchId(branchId);
        if (filePromotionInfoList != null && !filePromotionInfoList.isEmpty()) {
            ServerResponseListFilesToPromote serverResponseListFilesToPromote = new ServerResponseListFilesToPromote();
            filePromotionInfoList.forEach(filePromotionInfo -> {
                try {
                    filePromotionInfo.setTypeOfMerge(deduceTypeOfMerge(filePromotionInfo, parentBranchName));
                    filePromotionInfo.setDescribeTypeOfMerge(deduceMergeDescription(filePromotionInfo));
                    String childBranchTipRevisionString = deduceChildBranchTipRevisionString(filePromotionInfo, response);
                    if (childBranchTipRevisionString != null) {
                        filePromotionInfo.setChildBranchTipRevisionString(childBranchTipRevisionString);
                        serverResponseListFilesToPromote.addToList(filePromotionInfo);
                    }
                } catch (QVCSException e) {
                    // TODO -- Log the error. We'll send it back after we're done.
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            });
            returnObject = serverResponseListFilesToPromote;
        } else {
            // Explain the error.
            ServerResponseError error = new ServerResponseError("No files found to promote for [" + branchName + "] branch.", projectName, branchName, "");
            returnObject = error;
        }
        return returnObject;
    }

    private MergeType deduceTypeOfMerge(FilePromotionInfo filePromotionInfo, String parentBranchName) throws QVCSException {
        Integer fileId = filePromotionInfo.getFileId();
        InfoForMerge infoForMerge = new InfoForMerge(null, null,
                mergeTypeHelper.wasFileRenamedOnParentBranch(fileId, parentBranchName),
                mergeTypeHelper.wasFileRenamedOnBranch(fileId, parentBranchName),
                mergeTypeHelper.didFileMoveOnParentBranch(fileId, parentBranchName),
                mergeTypeHelper.didFileMoveOnBranch(fileId, parentBranchName),
                mergeTypeHelper.wasFileCreatedOnBranch(fileId, parentBranchName));
        MergeType mergeType = Utility.deduceTypeOfMerge(infoForMerge, filePromotionInfo.getShortWorkfileName());
        return mergeType;
    }

    private String deduceMergeDescription(FilePromotionInfo filePromotionInfo) {
        StringBuilder description = new StringBuilder();
        switch (filePromotionInfo.getTypeOfMerge()) {
            case SIMPLE_MERGE_TYPE:
                description.append("Simple merge.");
                break;
            case PARENT_RENAMED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was renamed on the parent branch.");
                break;
            case PARENT_MOVED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was moved on the parent branch.");
                break;
            case PARENT_RENAMED_AND_PARENT_MOVED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was renamed and moved on the parent branch.");
                break;
            case CHILD_RENAMED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was renamed on the branch.");
                break;
            case PARENT_RENAMED_AND_CHILD_RENAMED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was renamed on the parent branch and renamed on the branch.");
                break;
            case PARENT_MOVED_AND_CHILD_RENAMED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was moved on the parent branch and renamed on the branch.");
                break;
            case PARENT_RENAMED_AND_PARENT_MOVED_AND_CHILD_RENAMED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was renamed and moved on the parent branch and renamed on the branch.");
                break;
            case CHILD_MOVED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was moved on the branch.");
                break;
            case PARENT_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was renamed on the parent branch and was moved on the branch.");
                break;
            case PARENT_MOVED_AND_CHILD_MOVED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was moved on the parent branch and was moved on the branch.");
                break;
            case PARENT_MOVED_AND_PARENT_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was moved and renamed on the parent branch and was moved on the branch.");
                break;
            case CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was renamed and moved on the branch.");
                break;
            case PARENT_RENAMED_AND_CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was renamed on the parent branch and was renamed and moved on the branch.");
                break;
            case PARENT_MOVED_AND_CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was moved on the parent branch and was renamed and moved on the branch.");
                break;
            case PARENT_RENAMED_AND_PARENT_MOVED_AND_CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE:
                description.append("File [")
                        .append(filePromotionInfo.getShortWorkfileName()).append("] was renamed and moved on the parent branch and was renamed and moved on the branch.");
                break;
            case PARENT_DELETED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was deleted on the parent branch.");
                break;
            case CHILD_DELETED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was deleted on the branch.");
                break;
            case CHILD_CREATED_MERGE_TYPE:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] was added on the branch.");
                break;
            case UNKNOWN_MERGE_TYPE:
            default:
                description.append("File [").append(filePromotionInfo.getShortWorkfileName()).append("] has an unknown merge type.");
                break;
        }
        return description.toString();
    }

    private String deduceChildBranchTipRevisionString(FilePromotionInfo filePromotionInfo, ServerResponseFactoryInterface response) {
        String childBranchTipRevisionString = null;
        try {
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(request.getProjectName(), request.getBranchName(), filePromotionInfo.getAppendedPath());
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response);
            ArchiveInfoInterface archiveInfo = archiveDirManager.getArchiveInfo(filePromotionInfo.getShortWorkfileName());
            childBranchTipRevisionString = archiveInfo.getDefaultRevisionString();
        } catch (QVCSException e) {
            LOGGER.error("deduceChildBranchTipRevisionString", e);
        }
        return childBranchTipRevisionString;
    }
}
