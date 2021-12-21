/*   Copyright 2004-2021 Jim Voris
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

import com.qumasoft.qvcslib.FilePromotionInfo;
import com.qumasoft.qvcslib.InfoForMerge;
import com.qumasoft.qvcslib.PromotionType;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.requestdata.ClientRequestListFilesToPromoteData;
import com.qumasoft.qvcslib.response.ServerResponseInterface;
import com.qumasoft.qvcslib.response.ServerResponseListFilesToPromote;
import com.qumasoft.server.MergeTypeHelper;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.DirectoryDAO;
import com.qvcsos.server.dataaccess.DirectoryLocationDAO;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FileNameHistoryDAO;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.DirectoryDAOImpl;
import com.qvcsos.server.dataaccess.impl.DirectoryLocationDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileNameDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileNameHistoryDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileRevisionDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.dataaccess.impl.ProjectDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Directory;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.FileName;
import com.qvcsos.server.datamodel.FileNameHistory;
import com.qvcsos.server.datamodel.FileRevision;
import com.qvcsos.server.datamodel.Project;
import java.util.ArrayDeque;
import java.util.Deque;
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
    private final MergeTypeHelper mergeTypeHelper;
    private final DatabaseManager databaseManager;
    private final String schemaName;

    ClientRequestListFilesToPromote(ClientRequestListFilesToPromoteData data) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        request = data;
        this.mergeTypeHelper = new MergeTypeHelper(request.getProjectName(), request.getBranchName());
    }

    @Override
    public ServerResponseInterface execute(String userName, ServerResponseFactoryInterface response) {
        ServerResponseInterface returnObject;

        String projectName = request.getProjectName();
        String promoteFromBranchName = request.getBranchName();
        String promoteToBranchName = request.getPromoteToBranchName();

        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(projectName);

        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch promoteFromBranch = branchDAO.findByProjectIdAndBranchName(project.getId(), promoteFromBranchName);
        Branch promoteToBranch = branchDAO.findByProjectIdAndBranchName(project.getId(), promoteToBranchName);

        // Find the revisions created on the promote-from branch...
        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        List<FileRevision> promoteFromFileRevisionList = fileRevisionDAO.findPromotionCandidates(promoteFromBranch, promoteToBranch);
        ServerResponseListFilesToPromote serverResponseListFilesToPromote = new ServerResponseListFilesToPromote();
        serverResponseListFilesToPromote.setProjectName(projectName);
        serverResponseListFilesToPromote.setParentBranchName(promoteToBranchName);
        serverResponseListFilesToPromote.setBranchName(promoteFromBranchName);
        for (FileRevision fileRevision : promoteFromFileRevisionList) {
            try {
                FilePromotionInfo filePromotionInfo = new FilePromotionInfo();
                filePromotionInfo.setPromotedFromAppendedPath(mergeTypeHelper.buildAppendedPath(fileRevision.getFileId(), fileRevision.getCommitId()));
                filePromotionInfo.setTypeOfPromotion(deduceTypeOfPromotion(fileRevision, promoteFromBranch, promoteToBranch, filePromotionInfo));
                filePromotionInfo.setFeatureBranchRevisionId(fileRevision.getId());
                filePromotionInfo.setFileId(fileRevision.getFileId());

                filePromotionInfo.setPromotedFromBranchName(promoteFromBranchName);
                filePromotionInfo.setPromotedToBranchName(promoteToBranchName);

                filePromotionInfo.setPromotedFromBranchId(promoteFromBranch.getId());
                filePromotionInfo.setPromotedToBranchId(promoteToBranch.getId());

                filePromotionInfo.setPromotedFromShortWorkfileName(getShortWorkfileName(fileRevision));
                filePromotionInfo.setPromotedToShortWorkfileName(getParentShortWorkfileName(promoteToBranch.getId(), fileRevision.getFileId()));

                filePromotionInfo.setDescribeTypeOfPromotion(deduceMergeDescription(filePromotionInfo));
                String childBranchTipRevisionString = String.format("%d.%d", promoteFromBranch.getId(), fileRevision.getId());
                filePromotionInfo.setChildBranchTipRevisionString(childBranchTipRevisionString);
                serverResponseListFilesToPromote.addToList(filePromotionInfo);
            } catch (QVCSException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
        }
        returnObject = serverResponseListFilesToPromote;
        return returnObject;
    }

    private PromotionType deduceTypeOfPromotion(FileRevision fileRevision, Branch promoteFromBranch, Branch promoteToBranch, FilePromotionInfo filePromotionInfo) throws QVCSException {
        Integer fileId = fileRevision.getFileId();
        boolean didLocationChangeFlag = mergeTypeHelper.didFileLocationChange(fileId, promoteFromBranch.getId(), promoteToBranch.getId());
        InfoForMerge infoForMerge = new InfoForMerge(null, null,
                mergeTypeHelper.isFilenameDifferent(fileId, promoteFromBranch.getId(), promoteToBranch.getId()),
                didLocationChangeFlag,
                mergeTypeHelper.wasFileCreatedOnBranch(fileId, promoteFromBranch.getId(), promoteToBranch.getId()),
                mergeTypeHelper.wasFileDeletedOnBranch(fileId, promoteFromBranch.getId(), promoteToBranch.getId()));
        PromotionType promotionType = Utility.deduceTypeOfMerge(infoForMerge, filePromotionInfo.getPromotedFromShortWorkfileName());
        if (didLocationChangeFlag) {
            filePromotionInfo.setPromotedToAppendedPath(buildPromotedToAppendedPath(fileId, promoteToBranch.getId()));
        } else {
            filePromotionInfo.setPromotedToAppendedPath(filePromotionInfo.getPromotedFromAppendedPath());
        }
        return promotionType;
    }

    private String deduceMergeDescription(FilePromotionInfo filePromotionInfo) {
        StringBuilder description = new StringBuilder();
        switch (filePromotionInfo.getTypeOfPromotion()) {
            case SIMPLE_PROMOTION_TYPE:
                description.append("Simple merge.");
                break;
            case FILE_NAME_CHANGE_PROMOTION_TYPE:
                description.append("File [").append(filePromotionInfo.getPromotedFromShortWorkfileName()).append("] has a different name.");
                break;
            case FILE_LOCATION_CHANGE_PROMOTION_TYPE:
                description.append("File [").append(filePromotionInfo.getPromotedFromShortWorkfileName()).append("] is in a different location.");
                break;
            case LOCATION_AND_NAME_DIFFER_PROMOTION_TYPE:
                description.append("File [").append(filePromotionInfo.getPromotedFromShortWorkfileName()).append("] has a different name and location.");
                break;
            case FILE_CREATED_PROMOTION_TYPE:
                description.append("File [").append(filePromotionInfo.getPromotedFromShortWorkfileName()).append("] was created on feature branch.");
                break;
            case FILE_DELETED_PROMOTION_TYPE:
                description.append("File [").append(filePromotionInfo.getPromotedFromShortWorkfileName()).append("] was deleted on feature branch.");
                break;
            case UNKNOWN_PROMOTION_TYPE:
            default:
                description.append("File [").append(filePromotionInfo.getPromotedFromShortWorkfileName()).append("] has an unknown merge type.");
                break;
        }
        return description.toString();
    }

    private String getShortWorkfileName(FileRevision fileRevision) {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<Branch> ancestorBranchList = functionalQueriesDAO.getBranchAncestryList(fileRevision.getBranchId());
        String branchesToSearch = functionalQueriesDAO.buildBranchesToSearchString(ancestorBranchList);
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        FileName fileName = fileNameDAO.findByBranchListAndFileId(branchesToSearch, fileRevision.getFileId());
        return fileName.getFileName();
    }

    private String getParentShortWorkfileName(Integer parentBranchId, Integer fileId) {
        String parentShortWorkfileName = null;
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<Branch> ancestorBranchList = functionalQueriesDAO.getBranchAncestryList(parentBranchId);
        String branchesToSearch = functionalQueriesDAO.buildBranchesToSearchString(ancestorBranchList);
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        FileName fileName = fileNameDAO.findByBranchListAndFileId(branchesToSearch, fileId);
        if (fileName != null) {
            parentShortWorkfileName = fileName.getFileName();
        }
        return parentShortWorkfileName;
    }

    private String buildPromotedToAppendedPath(Integer fileId, Integer promotedToBranchId) {
        Integer directoryId = null;
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        String branchList = functionalQueriesDAO.buildBranchesToSearchString(functionalQueriesDAO.getBranchAncestryList(promotedToBranchId));
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        FileName fileName = fileNameDAO.findByBranchListAndFileId(branchList, fileId);
        if (fileName == null) {
            FileNameHistoryDAO fileNameHistoryDAO = new FileNameHistoryDAOImpl(schemaName);
            FileNameHistory fileNameHistory = fileNameHistoryDAO.findByBranchListAndFileId(branchList, fileId);
            directoryId = fileNameHistory.getDirectoryId();
        } else {
            directoryId = fileName.getDirectoryId();
        }

        DirectoryDAO directoryDAO = new DirectoryDAOImpl(schemaName);
        Directory directory = directoryDAO.findById(directoryId);
        DirectoryLocationDAO directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
        DirectoryLocation directoryLocation = directoryLocationDAO.findByDirectoryId(directory.getId());

        Deque<String> segmentStack = new ArrayDeque<>();
        segmentStack.push(directoryLocation.getDirectorySegmentName());
        while (directoryLocation.getParentDirectoryLocationId() != null) {
            DirectoryLocation parentDirectoryLocation = directoryLocationDAO.findById(directoryLocation.getParentDirectoryLocationId());
            segmentStack.push(parentDirectoryLocation.getDirectorySegmentName());
            directoryLocation = parentDirectoryLocation;
        }

        StringBuilder appendedPathBuilder = new StringBuilder();
        // Pop the root directory segment...
        String rootDirSegment = segmentStack.pop();
        while (segmentStack.size() > 0) {
            appendedPathBuilder.append(segmentStack.pop());
            if (segmentStack.size() > 0) {
                appendedPathBuilder.append(java.io.File.separator);
            }
        }
        return appendedPathBuilder.toString();
    }
}
