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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.server.dataaccess.BranchDAO;
import com.qumasoft.server.dataaccess.DirectoryDAO;
import com.qumasoft.server.dataaccess.FileDAO;
import com.qumasoft.server.dataaccess.FileHistoryDAO;
import com.qumasoft.server.dataaccess.ProjectDAO;
import com.qumasoft.server.dataaccess.impl.BranchDAOImpl;
import com.qumasoft.server.dataaccess.impl.DirectoryDAOImpl;
import com.qumasoft.server.dataaccess.impl.FileDAOImpl;
import com.qumasoft.server.dataaccess.impl.FileHistoryDAOImpl;
import com.qumasoft.server.dataaccess.impl.ProjectDAOImpl;
import com.qumasoft.server.datamodel.Branch;
import com.qumasoft.server.datamodel.Directory;
import com.qumasoft.server.datamodel.FileHistory;
import com.qumasoft.server.datamodel.Project;
import java.util.List;

/**
 * A helper class for some common methods that help figure out what kind of merge we'll have for a given file.
 *
 * @author Jim Voris
 */
public class MergeTypeHelper {

    private final String projectName;
    private final String viewName;

    /**
     * Merge type helper constructor.
     *
     * @param project the name of the project.
     * @param view the view name.
     */
    public MergeTypeHelper(String project, String view) {
        this.projectName = project;
        this.viewName = view;
    }

    private String getProjectName() {
        return this.projectName;
    }

    private String getViewName() {
        return this.viewName;
    }

    /**
     * We figure out whether the file has been renamed on the parent branch.
     *
     * @param fileId the file id.
     * @param parentBranchname the name of the parent branch.
     * @return true if the file moved on the branch.
     */
    public boolean wasFileRenamedOnParentBranch(Integer fileId, String parentBranchname) {
        boolean retFlag = false;
        ProjectDAO projectDAO = new ProjectDAOImpl();
        BranchDAO branchDAO = new BranchDAOImpl();
        FileDAO fileDAO = new FileDAOImpl();
        FileHistoryDAO fileHistoryDAO = new FileHistoryDAOImpl();
        Project project = projectDAO.findByProjectName(getProjectName());
        Branch parentBranch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), parentBranchname);
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), getViewName());
        if ((parentBranch != null) && (branch != null)) {
            ProjectView projectView = ViewManager.getInstance().getView(getProjectName(), branch.getBranchName());
            com.qumasoft.server.datamodel.File foundFile = findFile(branchDAO, fileDAO, branch, project.getProjectId(), fileId);
            if ((foundFile != null) && foundFile.getBranchId().equals(branch.getBranchId())) {
                com.qumasoft.server.datamodel.File parentBranchFile = fileDAO.findById(parentBranch.getBranchId(), fileId);
                if (parentBranchFile != null) {
                    if (!parentBranchFile.getFileName().equals(foundFile.getFileName())) {
                        // The file has in different name on the branch than on the parent branch. We now have to decide whether
                        // that's a result of the file having renamed on the parent.
                        if (parentBranchFile.getUpdateDate().after(projectView.getRemoteViewProperties().getBranchDate())) {
                            // The parent file record has been updated since the branch was created. We need to find the last history
                            // record for the parent branch that was created before the branch was created, and from that get the filename.
                            // If the filename of that history record is different than the filename of the current record, then the file
                            // has been renamed on the parent branch.
                            List<FileHistory> fileHistoryList = fileHistoryDAO.findHistoryForFileId(parentBranch.getBranchId(), fileId);
                            FileHistory foundFileHistory = null;
                            for (FileHistory fileHistory : fileHistoryList) {
                                if (fileHistory.getUpdateDate().before(projectView.getRemoteViewProperties().getBranchDate())) {
                                    foundFileHistory = fileHistory;
                                    break;
                                }
                            }
                            if (foundFileHistory != null) {
                                if (!foundFileHistory.getFileName().equals(parentBranchFile.getFileName())) {
                                    // The parent branch file has a different name than when the branch was created.
                                    retFlag = true;
                                }
                            }
                        }
                    }
                }
            } else if ((foundFile != null) && foundFile.getBranchId().equals(parentBranch.getBranchId())) {
                // The file did not have a record on the branch... we just need to see if it was renamed on the parent branch.
                if (foundFile.getUpdateDate().after(projectView.getRemoteViewProperties().getBranchDate())) {
                    // The parent file record has been updated since the branch was created. We need to find the last history
                    // record for the parent branch that was created before the branch was created, and from that get the filename.
                    // If the filename of that history record is different than the filename of the current record, then the file
                    // has been renamed on the parent branch.
                    List<FileHistory> fileHistoryList = fileHistoryDAO.findHistoryForFileId(parentBranch.getBranchId(), fileId);
                    FileHistory foundFileHistory = null;
                    for (FileHistory fileHistory : fileHistoryList) {
                        if (fileHistory.getUpdateDate().before(projectView.getRemoteViewProperties().getBranchDate())) {
                            foundFileHistory = fileHistory;
                            break;
                        }
                    }
                    if (foundFileHistory != null) {
                        if (!foundFileHistory.getFileName().equals(foundFile.getFileName())) {
                            // The parent branch file has a different name than when the branch was created.
                            retFlag = true;
                        }
                    }
                }
            }
        }
        return retFlag;
    }

    /**
     * We figure out whether the file has been renamed on the branch. If the file is new to the branch (i.e. it doesn't exist on the
     * parent), then it <i>cannot</i> have 'renamed' on the branch compared to the parent branch, since it doesn't exist on the
     * parent. If the file does exist on the parent, then it has been renamed on the branch if its current filename is different
     * than the filename it had at the time the branch was created. Note that if the file has the same name on the branch as it has
     * on the parent branch, then we declare that it has not been renamed, meaning that if it has renamed, there was a compensating
     * (identical) rename on the parent branch, so that we don't need to take the rename into account when we perform the merge --
     * i.e. for merge purposes we can ignore the rename.
     *
     * @param fileId the file id.
     * @param parentBranchname the name of the parent branch.
     * @return true if the file moved on the branch.
     */
    public boolean wasFileRenamedOnBranch(Integer fileId, String parentBranchname) {
        boolean retFlag = false;
        ProjectDAO projectDAO = new ProjectDAOImpl();
        BranchDAO branchDAO = new BranchDAOImpl();
        FileDAO fileDAO = new FileDAOImpl();
        FileHistoryDAO fileHistoryDAO = new FileHistoryDAOImpl();
        Project project = projectDAO.findByProjectName(getProjectName());
        Branch parentBranch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), parentBranchname);
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), getViewName());
        if ((parentBranch != null) && (branch != null)) {
            ProjectView projectView = ViewManager.getInstance().getView(getProjectName(), branch.getBranchName());
            com.qumasoft.server.datamodel.File foundFile = findFile(branchDAO, fileDAO, branch, project.getProjectId(), fileId);
            if ((foundFile != null) && foundFile.getBranchId().equals(branch.getBranchId())) {
                com.qumasoft.server.datamodel.File parentBranchFile = fileDAO.findById(parentBranch.getBranchId(), fileId);
                if (parentBranchFile != null) {
                    if (!parentBranchFile.getFileName().equals(foundFile.getFileName())) {
                        // The file has a different name on the branch than on the parent branch. We now have to decide whether
                        // that's a result of the file having renamed on the parent.
                        if (parentBranchFile.getUpdateDate().after(projectView.getRemoteViewProperties().getBranchDate())) {
                            // The parent file record has been updated since the branch was created. We need to find the last history
                            // record for the parent branch that was created before the branch was created, and from that get the filename.
                            // If the filename of that history record is different than the filename of the current record, then the file
                            // has been renamed on the parent branch.
                            List<FileHistory> fileHistoryList = fileHistoryDAO.findHistoryForFileId(parentBranch.getBranchId(), fileId);
                            FileHistory foundParentBranchFileHistory = null;
                            for (FileHistory fileHistory : fileHistoryList) {
                                if (fileHistory.getUpdateDate().before(projectView.getRemoteViewProperties().getBranchDate())) {
                                    foundParentBranchFileHistory = fileHistory;
                                    break;
                                }
                            }
                            if (foundParentBranchFileHistory != null) {
                                if (!foundParentBranchFileHistory.getFileName().equals(foundFile.getFileName())) {
                                    // The  branch file has a different name than when the branch was created.
                                    retFlag = true;
                                }
                            }
                        } else {
                            // The file has not been renamed on the parent branch, ergo, it must have been renamed on the branch.
                            retFlag = true;
                        }
                    }
                }
            }
        }
        return retFlag;
    }

    /**
     * We figure out whether the file has moved on the branch. If the file is new to the branch (i.e. it doesn't exist on the
     * parent), then it <i>cannot</i> have 'moved' on the branch compared to the parent branch, since it doesn't exist on the
     * parent. If the file does exist on the parent, then it has moved on the branch if its current branch directory is different
     * than the directory where it was located at the time the branch was created. Note that if the file is in the same directory on
     * the branch as it is on the parent branch, then we declare that it has not moved, meaning that if it has moved, there was a
     * compensating (identical) move on the parent branch, so that we don't need to take the move into account when we perform the
     * merge -- i.e. for merge purposes we can ignore the move.
     *
     * @param fileId the file id.
     * @param parentBranchname the name of the parent branch.
     * @return true if the file moved on the branch.
     */
    public boolean didFileMoveOnBranch(Integer fileId, String parentBranchname) {
        boolean retFlag = false;
        ProjectDAO projectDAO = new ProjectDAOImpl();
        BranchDAO branchDAO = new BranchDAOImpl();
        FileDAO fileDAO = new FileDAOImpl();
        FileHistoryDAO fileHistoryDAO = new FileHistoryDAOImpl();
        Project project = projectDAO.findByProjectName(getProjectName());
        Branch parentBranch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), parentBranchname);
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), getViewName());
        if ((parentBranch != null) && (branch != null)) {
            ProjectView projectView = ViewManager.getInstance().getView(getProjectName(), branch.getBranchName());
            com.qumasoft.server.datamodel.File foundFile = findFile(branchDAO, fileDAO, branch, project.getProjectId(), fileId);
            if ((foundFile != null) && foundFile.getBranchId().equals(branch.getBranchId())) {
                com.qumasoft.server.datamodel.File parentBranchFile = fileDAO.findById(parentBranch.getBranchId(), fileId);
                if (parentBranchFile != null) {
                    if (!parentBranchFile.getDirectoryId().equals(foundFile.getDirectoryId())) {
                        // The file is in a different directory on the branch than on the parent branch. We now have to decide whether
                        // that's a result of the file having moved on the branch.
                        if (parentBranchFile.getUpdateDate().after(projectView.getRemoteViewProperties().getBranchDate())) {
                            // The parent file record has been updated since the branch was created. We need to find the last history
                            // record for the parent branch that was created before the branch was created, and from that get the directory id.
                            // If the directory id of that history record is different than the directory id of the current branch record, then the file
                            // has been moved on the branch.
                            List<FileHistory> fileHistoryList = fileHistoryDAO.findHistoryForFileId(parentBranch.getBranchId(), fileId);
                            FileHistory foundParentBranchFileHistory = null;
                            for (FileHistory fileHistory : fileHistoryList) {
                                if (fileHistory.getUpdateDate().before(projectView.getRemoteViewProperties().getBranchDate())) {
                                    foundParentBranchFileHistory = fileHistory;
                                    break;
                                }
                            }
                            if (foundParentBranchFileHistory != null) {
                                if (!foundParentBranchFileHistory.getDirectoryId().equals(foundFile.getDirectoryId())) {
                                    // The branch file is in a different directory than it was when the branch was created.
                                    retFlag = true;
                                }
                            }
                        } else {
                            // The file has not moved on the parent branch, ergo, it must have moved on the branch.
                            retFlag = true;
                        }
                    }
                }
            }
        }
        return retFlag;
    }

    /**
     * Did the file move on the parent branch. If the file on the parent is in the same directory as on the child, then we conclude
     * there was no move for merge purposes at least.
     *
     * @param fileId the file id.
     * @param parentBranchname the name of the parent branch.
     * @return true if the file moved on the parent branch; false if it did not.
     */
    public boolean didFileMoveOnParentBranch(int fileId, String parentBranchname) {
        boolean retFlag = false;
        ProjectDAO projectDAO = new ProjectDAOImpl();
        BranchDAO branchDAO = new BranchDAOImpl();
        FileDAO fileDAO = new FileDAOImpl();
        FileHistoryDAO fileHistoryDAO = new FileHistoryDAOImpl();
        Project project = projectDAO.findByProjectName(getProjectName());
        Branch parentBranch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), parentBranchname);
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), getViewName());
        if ((parentBranch != null) && (branch != null)) {
            ProjectView projectView = ViewManager.getInstance().getView(getProjectName(), branch.getBranchName());
            com.qumasoft.server.datamodel.File foundFile = findFile(branchDAO, fileDAO, branch, project.getProjectId(), fileId);
            if ((foundFile != null) && foundFile.getBranchId().equals(branch.getBranchId())) {
                com.qumasoft.server.datamodel.File parentBranchFile = fileDAO.findById(parentBranch.getBranchId(), fileId);
                if (parentBranchFile != null) {
                    if (!parentBranchFile.getDirectoryId().equals(foundFile.getDirectoryId())) {
                        // The file is in a different directory on the branch than on the parent branch. We now have to decide whether
                        // that's a result of the file having moved on the parent.
                        if (parentBranchFile.getUpdateDate().after(projectView.getRemoteViewProperties().getBranchDate())) {
                            // The parent file record has been updated since the branch was created. We need to find the last history
                            // record for the parent branch that was created before the branch was created, and from that get the directory id.
                            // If the directory id of that history record is different than the directory id of the current record, then the file
                            // has been moved on the parent branch.
                            List<FileHistory> fileHistoryList = fileHistoryDAO.findHistoryForFileId(parentBranch.getBranchId(), fileId);
                            FileHistory foundFileHistory = null;
                            for (FileHistory fileHistory : fileHistoryList) {
                                if (fileHistory.getUpdateDate().before(projectView.getRemoteViewProperties().getBranchDate())) {
                                    foundFileHistory = fileHistory;
                                    break;
                                }
                            }
                            if (foundFileHistory != null) {
                                if (!foundFileHistory.getDirectoryId().equals(parentBranchFile.getDirectoryId())) {
                                    // The parent branch file is in a different directory than it was when the branch was created.
                                    retFlag = true;
                                }
                            }
                        }
                    }
                }
            } else if ((foundFile != null) && foundFile.getBranchId().equals(parentBranch.getBranchId())) {
                // The file did not have a record on the branch... we just need to see if it moved on the parent branch.
                if (foundFile.getUpdateDate().after(projectView.getRemoteViewProperties().getBranchDate())) {
                    // The parent file record has been updated since the branch was created. We need to find the last history
                    // record for the parent branch that was created before the branch was created, and from that get the directory id.
                    // If the directory id of that history record is different than the directory id of the current record, then the file
                    // has been moved on the parent branch.
                    List<FileHistory> fileHistoryList = fileHistoryDAO.findHistoryForFileId(parentBranch.getBranchId(), fileId);
                    FileHistory foundFileHistory = null;
                    for (FileHistory fileHistory : fileHistoryList) {
                        if (fileHistory.getUpdateDate().before(projectView.getRemoteViewProperties().getBranchDate())) {
                            foundFileHistory = fileHistory;
                            break;
                        }
                    }
                    if (foundFileHistory != null) {
                        if (!foundFileHistory.getDirectoryId().equals(foundFile.getDirectoryId())) {
                            // The parent branch file is in a different directory than it was when the branch was created.
                            retFlag = true;
                        }
                    }
                }
            }
        }
        return retFlag;
    }

    /**
     * Was the file created on the branch. If the file doesn't have a record for a parent branch, then it was created on the branch.
     *
     * @param fileId the file id.
     * @param parentBranchname the name of the parent branch.
     * @return true if the file was created on the branch (i.e. it doesn't exist on any parent branch).
     */
    public boolean wasFileCreatedOnBranch(Integer fileId, String parentBranchname) {
        boolean retFlag = false;
        ProjectDAO projectDAO = new ProjectDAOImpl();
        BranchDAO branchDAO = new BranchDAOImpl();
        FileDAO fileDAO = new FileDAOImpl();
        DirectoryDAO directoryDAO = new DirectoryDAOImpl();
        Project project = projectDAO.findByProjectName(getProjectName());
        Branch parentBranch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), parentBranchname);
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getProjectId(), getViewName());
        if ((parentBranch != null) && (branch != null)) {
            com.qumasoft.server.datamodel.File foundFile = findFile(branchDAO, fileDAO, branch, project.getProjectId(), fileId);
            if ((foundFile != null) && foundFile.getBranchId().equals(branch.getBranchId())) {
                com.qumasoft.server.datamodel.File foundFileOnParentBranch = findFile(branchDAO, fileDAO, parentBranch, project.getProjectId(), fileId);
                if (foundFileOnParentBranch == null) {
                    retFlag = true;
                } else {
                    Directory directory = directoryDAO.findById(parentBranch.getBranchId(), foundFileOnParentBranch.getDirectoryId());
                    if (directory.getAppendedPath().equals(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                        retFlag = true;
                    }
                }
            }
        }
        return retFlag;
    }

    /**
     * Walk up the branch tree until we find the file. <p><b>(This is adapted from the very similar code in
     * DirectoryContentsManager).</b></p>
     *
     * @param branchDAO the branch DAO.
     * @param fileDAO the file DAO.
     * @param branch the branch where we'll look.
     * @param projectId the project id.
     * @param fileId the file id.
     * @return the file (from the database).
     */
    private com.qumasoft.server.datamodel.File findFile(BranchDAO branchDAO, FileDAO fileDAO, Branch branch, int projectId, int fileId) {
        com.qumasoft.server.datamodel.File file = fileDAO.findById(branch.getBranchId(), fileId);
        if (file == null) {
            ProjectView projectView = ViewManager.getInstance().getView(getProjectName(), branch.getBranchName());
            if (projectView.getRemoteViewProperties().getParentProjectName() != null) {
                Branch parentBranch = branchDAO.findByProjectIdAndBranchName(projectId, projectView.getRemoteViewProperties().getBranchParent());
                file = findFile(branchDAO, fileDAO, parentBranch, projectId, fileId);
            }
        }
        return file;
    }
}
