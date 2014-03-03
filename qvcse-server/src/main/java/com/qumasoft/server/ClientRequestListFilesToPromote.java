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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.ClientRequestListFilesToPromoteData;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.InfoForMerge;
import com.qumasoft.qvcslib.MergeType;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseError;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.ServerResponseInterface;
import com.qumasoft.qvcslib.ServerResponseListFilesToPromote;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.server.dataaccess.FileDAO;
import com.qumasoft.server.dataaccess.impl.FileDAOImpl;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Get the list of files that have been modified on the requested branch.
 *
 * @author Jim Voris
 */
class ClientRequestListFilesToPromote implements ClientRequestInterface {
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.server");
    private final ClientRequestListFilesToPromoteData request;
    private FileDAO fileDAO = null;
    private final MergeTypeHelper mergeTypeHelper;

    public ClientRequestListFilesToPromote(ClientRequestListFilesToPromoteData data) {
        request = data;
        this.fileDAO = new FileDAOImpl();
        this.mergeTypeHelper = new MergeTypeHelper(request.getProjectName(), request.getViewName());
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;
        String projectName = request.getProjectName();
        String viewName = request.getViewName();
        Integer projectId = DatabaseCache.getInstance().getProjectId(projectName);
        Integer branchId = DatabaseCache.getInstance().getBranchId(projectId, viewName);
        ProjectView projectView = ViewManager.getInstance().getView(projectName, viewName);
        String parentBranchName = projectView.getRemoteViewProperties().getBranchParent();
        List<FilePromotionInfo> filePromotionInfoList = fileDAO.findFilePromotionInfoByBranchId(branchId);
        if (filePromotionInfoList != null && !filePromotionInfoList.isEmpty()) {
            ServerResponseListFilesToPromote serverResponseListFilesToPromote = new ServerResponseListFilesToPromote();
            for (FilePromotionInfo filePromotionInfo : filePromotionInfoList) {
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
                    LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
                }
            }
            returnObject = serverResponseListFilesToPromote;
        } else {
            // Explain the error.
            ServerResponseError error = new ServerResponseError("No files found to promote for [" + viewName + "] branch.", projectName, viewName, "");
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
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(request.getProjectName(), request.getViewName(), filePromotionInfo.getAppendedPath());
            ArchiveDirManagerInterface archiveDirManager = ArchiveDirManagerFactoryForServer.getInstance().getDirectoryManager(QVCSConstants.QVCS_SERVER_SERVER_NAME,
                    directoryCoordinate, QVCSConstants.QVCS_SERVED_PROJECT_TYPE, QVCSConstants.QVCS_SERVER_USER, response, true);
            ArchiveInfoInterface archiveInfo = archiveDirManager.getArchiveInfo(filePromotionInfo.getShortWorkfileName());
            childBranchTipRevisionString = archiveInfo.getDefaultRevisionString();
        } catch (QVCSException e) {
            LOGGER.log(Level.SEVERE, "deduceChildBranchTipRevisionString", e);
        }
        return childBranchTipRevisionString;
    }
}
