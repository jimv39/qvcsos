/*
 * Copyright 2021 Jim Voris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.DirectoryDAO;
import com.qvcsos.server.dataaccess.DirectoryLocationDAO;
import com.qvcsos.server.dataaccess.FileDAO;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FileNameHistoryDAO;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.impl.BranchDAOImpl;
import com.qvcsos.server.dataaccess.impl.DirectoryDAOImpl;
import com.qvcsos.server.dataaccess.impl.DirectoryLocationDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileNameDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileNameHistoryDAOImpl;
import com.qvcsos.server.dataaccess.impl.FileRevisionDAOImpl;
import com.qvcsos.server.dataaccess.impl.FunctionalQueriesDAOImpl;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Directory;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.FileName;
import com.qvcsos.server.datamodel.FileNameHistory;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A helper class for some common methods that help figure out what kind of merge we'll have for a given file.
 *
 * @author Jim Voris
 */
public class MergeTypeHelper {

    private final String projectName;
    private final String branchName;

    private final String schemaName;
    private final DatabaseManager databaseManager;

    private final BranchDAO branchDAO;
    private final DirectoryDAO directoryDAO;
    private final DirectoryLocationDAO directoryLocationDAO;
    private final FileDAO fileDAO;
    private final FileNameDAO fileNameDAO;
    private final FileNameHistoryDAO fileNameHistoryDAO;
    private final FileRevisionDAO fileRevisionDAO;
    private final FunctionalQueriesDAO functionalQueriesDAO;

    /**
     * Merge type helper constructor.
     *
     * @param project the name of the project.
     * @param branch the branch name.
     */
    public MergeTypeHelper(String project, String branch) {
        this.databaseManager = DatabaseManager.getInstance();
        this.schemaName = databaseManager.getSchemaName();
        this.projectName = project;
        this.branchName = branch;

        this.branchDAO = new BranchDAOImpl(schemaName);
        this.directoryDAO = new DirectoryDAOImpl(schemaName);
        this.directoryLocationDAO = new DirectoryLocationDAOImpl(schemaName);
        this.fileDAO = new FileDAOImpl(schemaName);
        this.fileNameDAO = new FileNameDAOImpl(schemaName);
        this.fileNameHistoryDAO = new FileNameHistoryDAOImpl(schemaName);
        this.fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        this.functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
    }

    private String getProjectName() {
        return this.projectName;
    }

    private String getBranchName() {
        return this.branchName;
    }

    /**
     * Build the appended path for a given file id, and commit id.
     * @param fileId the file id.
     * @param commitId the commit id.
     * @return a String with the appended path of the containing directory.
     */
    public String buildAppendedPath(Integer fileId, Integer commitId) {
        Integer directoryId = null;
        FileName fileName = fileNameDAO.findByFileIdAndCommitId(fileId, commitId);
        if (fileName == null) {
            FileNameHistory fileNameHistory = fileNameHistoryDAO.findByFileIdAndCommitId(fileId, commitId);
            directoryId = fileNameHistory.getDirectoryId();
        } else {
            directoryId = fileName.getDirectoryId();
        }

        Directory directory = directoryDAO.findById(directoryId);
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

    /**
     * We figure out whether the filename is different.
     *
     * @param fileId the file id.
     * @param childBranchId the id of the child branch.
     * @param parentBranchId the id of the parent branch.
     * @return true if the file moved on the branch.
     */
    public boolean isFilenameDifferent(Integer fileId, Integer childBranchId, Integer parentBranchId) {
        boolean retFlag = true;
        Branch branch = branchDAO.findById(childBranchId);

        switch (branch.getBranchTypeId()) {
            case QVCSConstants.QVCS_FEATURE_BRANCH_TYPE:
                retFlag = isFilenameDifferentForFeatureBranchType(fileId, childBranchId, parentBranchId);
                break;
            case QVCSConstants.QVCS_RELEASE_BRANCH_TYPE:
                retFlag = isFilenameDifferentForReleaseBranchType(fileId, childBranchId, parentBranchId);
                break;
            default:
            case QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE:
                throw new QVCSRuntimeException("Attempt to promote from Read-Only Tag based branch.");
        }
        return retFlag;
    }

    /**
     * We figure out whether the file location is different.
     *
     * @param fileId the file id.
     * @param fromBranchId the child branch id.
     * @param toBranchId the parent branch Id.
     * @return true if the file moved on the branch.
     */
    public boolean didFileLocationChange(Integer fileId, Integer fromBranchId, Integer toBranchId) {
        boolean retFlag = true;
        Branch branch = branchDAO.findById(fromBranchId);

        switch (branch.getBranchTypeId()) {
            case QVCSConstants.QVCS_FEATURE_BRANCH_TYPE:
                retFlag = didFileLocationChangeForFeatureBranchType(fileId, fromBranchId, toBranchId);
                break;
            case QVCSConstants.QVCS_RELEASE_BRANCH_TYPE:
                retFlag = didFileLocationChangeForReleaseBranchType(fileId, fromBranchId, toBranchId);
                break;
            default:
            case QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE:
                throw new QVCSRuntimeException("Attempt to promote from Read-Only Tag based branch.");
        }
        return retFlag;
    }

    /**
     * Was the file created on the child branch.
     *
     * @param fileId the file id.
     * @param childBranchId the id of the child branch.
     * @param parentBranchId the id of the parent branch.
     * @return true if the was created on the child branch.
     */
    public boolean wasFileCreatedOnBranch(Integer fileId, Integer childBranchId, Integer parentBranchId) {
        boolean retFlag = true;
        Branch childBranch = branchDAO.findById(childBranchId);

        switch (childBranch.getBranchTypeId()) {
            case QVCSConstants.QVCS_FEATURE_BRANCH_TYPE:
                retFlag = wasFileCreatedOnFeatureBranchForFeatureBranchType(fileId, childBranchId);
                break;
            case QVCSConstants.QVCS_RELEASE_BRANCH_TYPE:
                retFlag = wasFileCreatedOnReleaseBranchForReleaseBranchType(fileId, childBranchId, parentBranchId);
                break;
            default:
            case QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE:
                throw new QVCSRuntimeException("Attempt to promote from Read-Only Tag based branch.");
        }
        return retFlag;
    }

    public boolean wasFileDeletedOnBranch(Integer fileId, Integer childBranchId, Integer parentBranchId) {
        boolean retFlag = true;
        Branch childBranch = branchDAO.findById(childBranchId);

        switch (childBranch.getBranchTypeId()) {
            case QVCSConstants.QVCS_FEATURE_BRANCH_TYPE:
                retFlag = wasFileDeletedOnFeatureBranchForFeatureBranchType(fileId, childBranchId);
                break;
            case QVCSConstants.QVCS_RELEASE_BRANCH_TYPE:
                retFlag = wasFileDeletedOnReleaseBranchForReleaseBranchType(fileId, childBranchId);
                break;
            default:
            case QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE:
                throw new QVCSRuntimeException("Attempt to promote from Read-Only Tag based branch.");
        }
        return retFlag;
    }

    private boolean isFilenameDifferentForFeatureBranchType(Integer fileId, Integer childBranchId, Integer parentBranchId) {
        String childBranchList = functionalQueriesDAO.buildBranchesToSearchString(functionalQueriesDAO.getBranchAncestryList(childBranchId));
        String parentBranchList = functionalQueriesDAO.buildBranchesToSearchString(functionalQueriesDAO.getBranchAncestryList(parentBranchId));
        boolean retFlag = fileNameDAO.isFileNameDifferentOnFeatureBranch(fileId, childBranchList, parentBranchList);
        return retFlag;
    }

    private boolean isFilenameDifferentForReleaseBranchType(Integer fileId, Integer childBranchId, Integer parentBranchId) {
        boolean retFlag = false;
        List<FileName> fileNameList = fileNameDAO.findByFileId(fileId);
        if (fileNameList.isEmpty()) {
            throw new QVCSRuntimeException("file_name record not found for fileId: [" + fileId + "]");
        }
        // <editor-fold>
        switch (fileNameList.size()) {
            case 1:
                retFlag = false;
                break;
            default: {
                    Map<Integer, FileName> fileNameMap = new HashMap<>();
                    for (FileName fileName : fileNameList) {
                        fileNameMap.put(fileName.getBranchId(), fileName);
                    }
                    FileName childBranchFileName = fileNameMap.get(childBranchId);
                    FileName parentBranchFileName = fileNameMap.get(parentBranchId);
                    if (parentBranchFileName == null) {
                        // FileName was created on the branch, and it does not exist on the parent.
                        retFlag = false;
                    } else {
                        // Parent branch is not null.
                        if (childBranchFileName != null) {
                            // Are their names different?
                            if (0 != childBranchFileName.getFileName().compareTo(parentBranchFileName.getFileName())) {
                                retFlag = true;
                            }
                        } else {
                            // Child branch fileName does not exist... maybe the parent fileName was changed after the branch was created?
                            Branch childBranch = branchDAO.findById(childBranchId);
                            if (parentBranchFileName.getCommitId() > childBranch.getCommitId()) {
                                // The parent file name was added or changed to the parent after the branch was created.
                                // We need to check history.
                                FileNameHistory fileNameHistory = fileNameHistoryDAO.findByFileIdAndCommitId(fileId, childBranch.getCommitId());
                                if (fileNameHistory != null) {
                                    if (0 != fileNameHistory.getFileName().compareTo(parentBranchFileName.getFileName())) {
                                        // The file now has a different name than when the branch was created.
                                        retFlag = true;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }
        // </editor-fold>
        return retFlag;
    }

    private boolean didFileLocationChangeForFeatureBranchType(Integer fileId, Integer childBranchId, Integer parentBranchId) {
        boolean retFlag = false;
        List<FileName> fileNameList = fileNameDAO.findByFileId(fileId);
        if (fileNameList.isEmpty()) {
            throw new QVCSRuntimeException("file_name record not found for fileId: [" + fileId + "]");
        }
        // <editor-fold>
        switch (fileNameList.size()) {
            case 1:
                retFlag = false;
                break;
            default: {
                    Map<Integer, FileName> fileNameMap = new HashMap<>();
                    for (FileName fileName : fileNameList) {
                        fileNameMap.put(fileName.getBranchId(), fileName);
                    }
                    FileName childBranchFileName = fileNameMap.get(childBranchId);
                    FileName parentBranchFileName = fileNameMap.get(parentBranchId);
                    if (parentBranchFileName == null) {
                        // FileName was created on the branch, and it does not exist on the parent.
                        retFlag = false;
                    } else {
                        // Parent branch is not null.
                        if (childBranchFileName != null) {
                            // Are their locations different?
                            if (childBranchFileName.getDirectoryId().intValue() != parentBranchFileName.getDirectoryId().intValue()) {
                                retFlag = true;
                            }
                        }
                    }
                }
                break;
        }
        // </editor-fold>
        return retFlag;
    }

    private boolean didFileLocationChangeForReleaseBranchType(Integer fileId, Integer childBranchId, Integer parentBranchId) {
        boolean retFlag = false;
        List<FileName> fileNameList = fileNameDAO.findByFileId(fileId);
        if (fileNameList.isEmpty()) {
            throw new QVCSRuntimeException("file_name record not found for fileId: [" + fileId + "]");
        }
        // <editor-fold>
        switch (fileNameList.size()) {
            case 1:
                retFlag = false;
                break;
            default: {
                    Map<Integer, FileName> fileNameMap = new HashMap<>();
                    for (FileName fileName : fileNameList) {
                        fileNameMap.put(fileName.getBranchId(), fileName);
                    }
                    FileName childBranchFileName = fileNameMap.get(childBranchId);
                    FileName parentBranchFileName = fileNameMap.get(parentBranchId);
                    if (parentBranchFileName == null) {
                        // FileName was created on the branch, and it does not exist on the parent.
                        retFlag = false;
                    } else {
                        // Parent branch is not null.
                        if (childBranchFileName != null) {
                            // Are their locations different?
                            if (childBranchFileName.getDirectoryId().intValue() != parentBranchFileName.getDirectoryId().intValue()) {
                                retFlag = true;
                            }
                        } else {
                            // Child branch fileName does not exist... maybe the parent fileName was moved after the branch was created?
                            Branch childBranch = branchDAO.findById(childBranchId);
                            if (parentBranchFileName.getCommitId() > childBranch.getCommitId()) {
                                // The parent file name was added or changed to the parent after the branch was created.
                                // We need to check history.
                                FileNameHistory fileNameHistory = fileNameHistoryDAO.findByFileIdAndCommitId(fileId, childBranch.getCommitId());
                                if (fileNameHistory != null) {
                                    if (fileNameHistory.getDirectoryId().intValue() != parentBranchFileName.getDirectoryId().intValue()) {
                                        // The file is in a different place than it was when the branch was created.
                                        retFlag = true;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
        }
        // </editor-fold>
        return retFlag;
    }

    private boolean wasFileCreatedOnFeatureBranchForFeatureBranchType(Integer fileId, Integer childBranchId) {
        boolean retFlag = false;
        FileName createdFileName = fileNameDAO.findFileCreatedOnBranch(fileId, childBranchId);
        if (createdFileName != null) {
            retFlag = true;
        }
        return retFlag;
    }

    private boolean wasFileDeletedOnFeatureBranchForFeatureBranchType(Integer fileId, Integer childBranchId) {
        boolean retFlag = false;
        List<Branch> branchList = functionalQueriesDAO.getBranchAncestryList(childBranchId);
        String branchListString = functionalQueriesDAO.buildBranchesToSearchString(branchList);

        retFlag = fileNameDAO.wasFileDeletedOnFeatureBranch(fileId, childBranchId, branchListString);
        return retFlag;
    }

    private boolean wasFileCreatedOnReleaseBranchForReleaseBranchType(Integer fileId, Integer childBranchId, Integer parentBranchId) {
        boolean retFlag = false;
        List<FileName> fileNameList = fileNameDAO.findByFileId(fileId);
        if (fileNameList.isEmpty()) {
            throw new QVCSRuntimeException("file_name record not found for fileId: [" + fileId + "]");
        }
        // <editor-fold>
        switch (fileNameList.size()) {
            case 1: {
                    FileName fileName = fileNameList.get(0);
                    if (fileName.getBranchId().intValue() == childBranchId.intValue()) {
                        // FileName was created on the branch, so it does not exist on the parent.
                        retFlag = true;
                    }
                }
                break;
            default: {
                    Map<Integer, FileName> fileNameMap = new HashMap<>();
                    for (FileName fileName : fileNameList) {
                        fileNameMap.put(fileName.getBranchId(), fileName);
                    }
                    FileName childBranchFileName = fileNameMap.get(childBranchId);
                    FileName parentBranchFileName = fileNameMap.get(parentBranchId);
                    if (parentBranchFileName == null) {
                        // FileName was created on the branch, and it does not exist on the parent.
                        retFlag = true;
                    } else {
                        // Parent branch is not null.
                        if (childBranchFileName != null) {
                            // Are their deleted flag's different?
                            if (childBranchFileName.getDeletedFlag().booleanValue() != parentBranchFileName.getDeletedFlag().booleanValue()) {
                                retFlag = true;
                            }
                        } else {
                            // Child branch fileName does not exist... maybe the parent fileName was created after the branch was created?
                            Branch childBranch = branchDAO.findById(childBranchId);
                            if (parentBranchFileName.getCommitId() > childBranch.getCommitId()) {
                                // The file was added to the parent after the branch was created.
                                // For release branches, this means it is invisible to the branch.
                                retFlag = true;
                            }
                        }
                    }
                }
                break;
        }
        // </editor-fold>
        return retFlag;
    }

    private boolean wasFileDeletedOnReleaseBranchForReleaseBranchType(Integer fileId, Integer childBranchId) {
        boolean retFlag = false;

        retFlag = fileNameDAO.wasFileDeletedOnReleaseBranch(fileId, childBranchId);
        return retFlag;
    }

}
