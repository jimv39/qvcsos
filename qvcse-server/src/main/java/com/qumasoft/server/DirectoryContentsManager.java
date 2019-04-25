//   Copyright 2004-2019 Jim Voris
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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.server.dataaccess.BranchDAO;
import com.qumasoft.server.dataaccess.DirectoryDAO;
import com.qumasoft.server.dataaccess.DirectoryHistoryDAO;
import com.qumasoft.server.dataaccess.FileDAO;
import com.qumasoft.server.dataaccess.FileHistoryDAO;
import com.qumasoft.server.dataaccess.ProjectDAO;
import com.qumasoft.server.dataaccess.impl.BranchDAOImpl;
import com.qumasoft.server.dataaccess.impl.DirectoryDAOImpl;
import com.qumasoft.server.dataaccess.impl.DirectoryHistoryDAOImpl;
import com.qumasoft.server.dataaccess.impl.FileDAOImpl;
import com.qumasoft.server.dataaccess.impl.FileHistoryDAOImpl;
import com.qumasoft.server.dataaccess.impl.ProjectDAOImpl;
import com.qumasoft.server.datamodel.Branch;
import com.qumasoft.server.datamodel.Directory;
import com.qumasoft.server.datamodel.Project;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Directory Contents Manager.
 *
 * @author Jim Voris
 */
public class DirectoryContentsManager implements TransactionParticipantInterface {
    // Create our logger object.
    private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryContentsManager.class);
    private final String projectName;
    private final Integer projectId;
    private Integer trunkBranchId;
    private final ProjectDAO projectDAO;
    private final BranchDAO branchDAO;
    private final DirectoryDAO directoryDAO;
    private final FileDAO fileDAO;

    /**
     * Creates a new instance of DirectoryContentsManager.
     *
     * @param p the name of the project.
     */
    public DirectoryContentsManager(final String p) {
        this.projectName = p;

        this.projectDAO = new ProjectDAOImpl();
        this.directoryDAO = new DirectoryDAOImpl();
        this.branchDAO = new BranchDAOImpl();
        this.fileDAO = new FileDAOImpl();

        Project project = projectDAO.findByProjectName(p);
        this.projectId = project.getProjectId();
    }

    /**
     * Get the project name.
     *
     * @return the project name.
     */
    public String getProjectName() {
        return this.projectName;
    }

    /**
     * Get the collection of directory id's for a date based view.
     *
     * @param viewName the name of the view we are interested in.
     * @param appendedPath the appended path for the directory we're looking in.
     * @param directoryId the directory ID of the directory in which we look for directory ID's. i.e. this is the parent directory
     * of the directory ID collection that we return.
     * @param response the response object that identifies the client for this request.
     * @return a Map of directory id/directory names.
     * @throws QVCSException if there is a problem in QVCS code.
     */
    public synchronized Map<Integer, String> getDirectoryIDCollectionForDateBasedView(final String viewName, final String appendedPath,
            final int directoryId, ServerResponseFactoryInterface response)
            throws QVCSException {
        ProjectView projectView = ViewManager.getInstance().getView(getProjectName(), viewName);
        DirectoryContents directoryContents = getDirectoryContentsForDateBasedView(projectView, appendedPath, directoryId, response);
        return directoryContents.getChildDirectories();
    }

    /**
     * Get the collection of directory id's for a translucent branch directory.
     *
     * @param projectView the projectView we are interested in.
     * @param appendedPath the appended path for the directory we're looking in.
     * @param directoryId the directory ID of the directory in which we look for directory ID's. i.e. this is the parent directory
     * of the directory ID collection that we return.
     * @param response the response object that identifies the client for this request.
     * @return a Map of directory id/directory names.
     * @throws QVCSException if there is a problem in QVCS code.
     */
    public synchronized Map<Integer, String> getDirectoryIDCollectionForTranslucentBranch(final ProjectView projectView, final String appendedPath, final int directoryId,
            ServerResponseFactoryInterface response) throws QVCSException {
        LOGGER.info("getDirectoryIDCollectionForTranslucentBranch viewName: [{}] appendedPath: [{}] directoryId: [{}]", projectView.getViewName(), appendedPath, directoryId);
        DirectoryContents directoryContents = getDirectoryContentsForTranslucentBranch(projectView, appendedPath, directoryId, response);
        return directoryContents.getChildDirectories();
    }

    /**
     * Get the collection of directory id's for an opaque branch directory.
     *
     * @param projectView the projectView we are interested in.
     * @param appendedPath the appended path for the directory we're looking in.
     * @param directoryId the directory ID of the directory in which we look for directory ID's. i.e. this is the parent directory
     * of the directory ID collection that we return.
     * @param response the response object that identifies the client for this request.
     * @return a Map of directory id/directory names.
     * @throws QVCSException if there is a problem in QVCS code.
     */
    public synchronized Map<Integer, String> getDirectoryIDCollectionForOpaqueBranch(final ProjectView projectView, final String appendedPath, final int directoryId,
            ServerResponseFactoryInterface response) throws QVCSException {
        DirectoryContents directoryContents = getDirectoryContentsForOpaqueBranch(projectView, appendedPath, directoryId, response);
        return directoryContents.getChildDirectories();
    }

    /**
     * Add a file to the trunk.
     *
     * @param directoryId the directory where we'll add the file.
     * @param fileId the file id.
     * @param shortWorkfileName the short workfile name of the file.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction or if the branch is not found.
     * @throws SQLException if there is a problem inserting the record.
     */
    public synchronized void addFileToTrunk(final int directoryId, final int fileId, final String shortWorkfileName,
            ServerResponseFactoryInterface response) throws QVCSException, SQLException {
        addFile(QVCSConstants.QVCS_TRUNK_VIEW, directoryId, fileId, shortWorkfileName, response);
    }

    /**
     * Add a file to an opaque branch.
     *
     * @param branchName the name of the opaque branch.
     * @param directoryId the directory where we'll add the file.
     * @param fileId the file id.
     * @param shortWorkfileName the short workfile name of the file.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction or if the branch is not found.
     * @throws SQLException if there is a problem inserting the record.
     */
    public synchronized void addFileToOpaqueBranch(final String branchName, final int directoryId, final int fileId, final String shortWorkfileName,
            ServerResponseFactoryInterface response)
            throws QVCSException, SQLException {
        addFile(branchName, directoryId, fileId, shortWorkfileName, response);
    }

    /**
     * Add a file to a translucent branch.
     *
     * @param branchName the name of the translucent branch.
     * @param directoryId the directory where we'll add the file.
     * @param fileId the file id.
     * @param shortWorkfileName the short workfile name of the file.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the branch is not found.
     * @throws SQLException if there is a problem inserting the record.
     */
    public synchronized void addFileToTranslucentBranch(final String branchName, final int directoryId, final int fileId, final String shortWorkfileName,
            ServerResponseFactoryInterface response)
            throws QVCSException, SQLException {
        addFile(branchName, directoryId, fileId, shortWorkfileName, response);
    }

    /**
     * Add a file to the given branch/directory.
     *
     * @param branchName the branch name.
     * @param directoryId the directory id.
     * @param fileId the file id.
     * @param shortWorkfileName the short workfile name
     * @param response the response object that identifies the client for this request.
     * @throws SQLException if there is a problem inserting the record.
     * @throws QVCSException if the branch is not found.
     */
    private void addFile(final String branchName, final int directoryId, final int fileId, final String shortWorkfileName,
            ServerResponseFactoryInterface response) throws SQLException, QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to create an archive file without a surrounding transaction.", response);
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
        if (branch != null) {
            com.qumasoft.server.datamodel.File file = new com.qumasoft.server.datamodel.File();
            file.setFileId(fileId);
            file.setBranchId(branch.getBranchId());
            file.setDirectoryId(directoryId);
            file.setDeletedFlag(false);
            file.setFileName(shortWorkfileName);
            fileDAO.insert(file);
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
    }

    /**
     * Rename a file.
     *
     * @param directoryId the directory where the rename is done
     * @param fileId the file id of the file that is to be renamed.
     * @param oldWorkfileName the old workfile name.
     * @param newWorkfileName the new workfile name.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the branch is not found, or if the old workfile name
     * fails to match the existing db record file name.
     */
    public synchronized void renameFileOnTrunk(final int directoryId, final int fileId, final String oldWorkfileName, final String newWorkfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to rename a file without a surrounding transaction.", response);
        String branchName = QVCSConstants.QVCS_TRUNK_VIEW;
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
        if (branch != null) {
            // Verify that the file is where the caller claims it is to begin with, etc.
            com.qumasoft.server.datamodel.File datamodelFile = fileDAO.findById(branch.getBranchId(), fileId);
            if (!datamodelFile.getFileName().equals(oldWorkfileName)) {
                throw new QVCSException("Original filename does not match: db record name: [" + datamodelFile.getFileName()
                        + "], caller original filename: [" + oldWorkfileName + "]");
            }
            datamodelFile.setFileName(newWorkfileName);
            try {
                fileDAO.update(datamodelFile, false);
            } catch (SQLException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
                throw new QVCSException("SQLException on rename: " + e.getLocalizedMessage());
            }
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
    }

    /**
     * Rename file on opaque branch.
     *
     * @param branchName branch name.
     * @param fileId file id.
     * @param oldWorkfileName old workfile name.
     * @param newWorkfileName new workfile name.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the branch is not found, or if the old workfile name
     * fails to match the existing db record file name, or if there is a SQL exception.
     */
    public synchronized void renameFileOnOpaqueBranch(final String branchName, final int fileId, final String oldWorkfileName, final String newWorkfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        renameFileOnBranch(branchName, fileId, oldWorkfileName, newWorkfileName, response);
    }

    /**
     * Rename file on translucent branch.
     *
     * @param branchName branch name.
     * @param fileId file id.
     * @param oldWorkfileName old workfile name.
     * @param newWorkfileName new workfile name.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the branch is not found, or if the old workfile name
     * fails to match the existing db record file name, or if there is a SQL exception.
     */
    public synchronized void renameFileOnTranslucentBranch(final String branchName, final int fileId, final String oldWorkfileName, final String newWorkfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        renameFileOnBranch(branchName, fileId, oldWorkfileName, newWorkfileName, response);
    }

    /**
     * Rename file on branch.
     *
     * @param branchName branch name.
     * @param fileId file id.
     * @param oldWorkfileName old workfile name.
     * @param newWorkfileName new workfile name.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the branch is not found, or if the old workfile name
     * fails to match the existing db record file name, or if there is a SQL exception.
     */
    private synchronized void renameFileOnBranch(final String branchName, final int fileId, final String oldWorkfileName, final String newWorkfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to rename a file without a surrounding transaction.", response);
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
        if (branch != null) {
            deleteBranchFileRecordWithIsDeletedFlag(branch.getBranchId(), fileId);
            com.qumasoft.server.datamodel.File datamodelFile = fileDAO.findById(branch.getBranchId(), fileId);
            if (datamodelFile != null) {
                /*
                 * if (file record on branch exists)...
                 */
                // Verify that it's directory id matches what we found in the database.
                if (!datamodelFile.getFileName().equals(oldWorkfileName)) {
                    throw new QVCSException("Existing filename does not match: db record directory: [" + datamodelFile.getFileName()
                            + "], caller filename: [" + oldWorkfileName + "]");
                }
                datamodelFile.setFileName(newWorkfileName);
                try {
                    // Update the branch record with the new name.
                    fileDAO.update(datamodelFile, false);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when updating filename on branch: [").append(datamodelFile.getFileName()).append("].")
                            .append(" Exception: ").append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }
            } else {
                /*
                 * else -- there is no file record for the branch, ergo it uses the parent branch's file record. create a record for
                 * the branch that has the new name.
                 */
                // Walk up the parent branch tree until we find the file.
                datamodelFile = findFile(branch, fileId);
                if (!datamodelFile.getFileName().equals(oldWorkfileName)) {
                    throw new QVCSException("Existing filename does not match: db record directory: [" + datamodelFile.getFileName()
                            + "], caller filename: [" + oldWorkfileName + "]");
                }
                datamodelFile.setBranchId(branch.getBranchId());
                datamodelFile.setFileName(newWorkfileName);
                try {
                    fileDAO.insert(datamodelFile);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when creating renamed file record on branch file: [")
                            .append(datamodelFile.getFileName()).append("].").append(" Exception: ")
                            .append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }
            }
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
    }

    /**
     * Move a file from one directory to another on the trunk.
     *
     * @param branchName the name of the branch on which the move is to occur.
     * @param originDirectoryId the origin directory id. This is where the file is currently located.
     * @param destinationDirectoryId the destination directory id. This is where the file is to be moved.
     * @param fileId the file id.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the branch is not found, or if the old directory id fails
     * to match the existing db record directory id.
     */
    public synchronized void moveFileOnTrunk(final String branchName, final int originDirectoryId, final int destinationDirectoryId,
            final int fileId, ServerResponseFactoryInterface response)
            throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to move a file without a surrounding transaction.", response);
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
        if (branch != null) {
            // Verify that the file is where the caller claims it is to begin with, etc.
            com.qumasoft.server.datamodel.File datamodelFile = fileDAO.findById(branch.getBranchId(), fileId);
            if (!datamodelFile.getDirectoryId().equals(originDirectoryId)) {
                throw new QVCSException("Original directory does not match: db record directory: [" + datamodelFile.getDirectoryId()
                        + "], caller original filename: [" + originDirectoryId + "]");
            }
            datamodelFile.setDirectoryId(destinationDirectoryId);
            try {
                fileDAO.update(datamodelFile, false);
            } catch (SQLException e) {
                LOGGER.error(e.getLocalizedMessage(), e);
                throw new QVCSException("SQLException on move: " + e.getLocalizedMessage());
            }
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
    }

    /**
     * Move a file from one directory to another on a translucent branch.
     *
     * @param branchName the name of the branch on which the move is to occur.
     * @param originDirectoryId the origin directory id. This is where the file is currently located.
     * @param destinationDirectoryId the destination directory id. This is where the file is to be moved.
     * @param fileId the file id.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the branch is not found, or if the old directory id fails
     * to match the existing db record directory id.
     */
    public synchronized void moveFileOnTranslucentBranch(final String branchName, final int originDirectoryId, final int destinationDirectoryId, final int fileId,
            ServerResponseFactoryInterface response) throws QVCSException {
        moveFileOnBranch(branchName, originDirectoryId, destinationDirectoryId, fileId, response);
    }

    /**
     * Move a file from one directory to another on an opaque branch.
     *
     * @param branchName the name of the branch on which the move is to occur.
     * @param originDirectoryId the origin directory id. This is where the file is currently located.
     * @param destinationDirectoryId the destination directory id. This is where the file is to be moved.
     * @param fileId the file id.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the branch is not found, or if the old directory id fails
     * to match the existing db record directory id.
     */
    public synchronized void moveFileOnOpaqueBranch(final String branchName, final int originDirectoryId, final int destinationDirectoryId, final int fileId,
            ServerResponseFactoryInterface response) throws QVCSException {
        moveFileOnBranch(branchName, originDirectoryId, destinationDirectoryId, fileId, response);
    }

    /**
     * Move a file from one directory to another on a branch.
     *
     * @param branchName the name of the branch on which the move is to occur.
     * @param originDirectoryId the origin directory id. This is where the file is currently located.
     * @param destinationDirectoryId the destination directory id. This is where the file is to be moved.
     * @param fileId the file id.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the branch is not found, or if the old directory id fails
     * to match the existing db record directory id.
     */
    private void moveFileOnBranch(final String branchName, final int originDirectoryId, final int destinationDirectoryId, final int fileId,
            ServerResponseFactoryInterface response) throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to move file on branch without a surrounding transaction.", response);
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);

        if (branch != null) {
            deleteBranchFileRecordWithIsDeletedFlag(branch.getBranchId(), fileId);
            com.qumasoft.server.datamodel.File datamodelFile = fileDAO.findById(branch.getBranchId(), fileId);
            if (datamodelFile != null) {
                /*
                 * if (file record on branch exists)...
                 */
                // Verify that it's origin directory id matches what we found in the database.
                if (!datamodelFile.getDirectoryId().equals(originDirectoryId)) {
                    throw new QVCSException("Original directory does not match: db record directory: [" + datamodelFile.getDirectoryId()
                            + "], caller original filename: [" + originDirectoryId + "]");
                }
                /*
                 * if (file record exists on parent branch) mark the record deleted on the branch create an entry for the file in
                 * the branch's destination directory. else update the record to the new destination directory on the branch.
                 */
                boolean fileExistsOnParentBranch = doesFileExistOnParentBranch(branch, fileId);
                if (fileExistsOnParentBranch) {
                    datamodelFile.setDeletedFlag(true);
                    try {
                        // Update the branch record to show it is deleted from the directory so we won't show it in the
                        // current location.
                        fileDAO.update(datamodelFile, false);
                    } catch (SQLException e) {
                        StringBuilder message = new StringBuilder();
                        message.append("SQLException when setting deleted flag on branch: [").append(datamodelFile.getFileName()).append("].")
                                .append(" Exception: ").append(e.getLocalizedMessage());
                        LOGGER.error(message.toString());
                        throw new QVCSException(message.toString());
                    }
                    datamodelFile.setDeletedFlag(false);
                    datamodelFile.setDirectoryId(destinationDirectoryId);
                    try {
                        fileDAO.insert(datamodelFile);
                    } catch (SQLException e) {
                        StringBuilder message = new StringBuilder();
                        message.append("SQLException when creating destination record on branch file: [").append(datamodelFile.getFileName())
                                .append("].").append(" Exception: ").
                                append(e.getLocalizedMessage());
                        LOGGER.error(message.toString());
                        throw new QVCSException(message.toString());
                    }
                } else {
                    datamodelFile.setDirectoryId(destinationDirectoryId);
                    try {
                        // Move the file to the destination directory.
                        fileDAO.update(datamodelFile, false);
                    } catch (SQLException e) {
                        StringBuilder message = new StringBuilder();
                        message.append("SQLException when setting moving file on branch: [").append(datamodelFile.getFileName()).append("].")
                                .append(" Exception: ").append(e.getLocalizedMessage());
                        LOGGER.error(message.toString());
                        throw new QVCSException(message.toString());
                    }
                }
            } else {
                /*
                 * else -- there is no file record for the branch, ergo it uses the parent branch's file record. create a record for
                 * the branch that has its deleted flag set to true. create an entry for the file in the branch's destination
                 * directory.
                 */
                // Walk up the parent branch tree until we find the file.
                datamodelFile = findFile(branch, fileId);
                if (!datamodelFile.getDirectoryId().equals(originDirectoryId)) {
                    throw new QVCSException("Original directory in parent branch does not match: db record directory: ["
                            + datamodelFile.getDirectoryId() + "], caller original filename: ["
                            + originDirectoryId + "]");
                }
                datamodelFile.setBranchId(branch.getBranchId());
                datamodelFile.setDeletedFlag(true);
                try {
                    fileDAO.insert(datamodelFile);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when creating deleted flag record on branch file: [").append(datamodelFile.getFileName()).append("].").append(" Exception: ").
                            append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }

                // Create an entry for the file in the branch's destination directory.
                datamodelFile.setDeletedFlag(false);
                datamodelFile.setBranchId(branch.getBranchId());
                datamodelFile.setDirectoryId(destinationDirectoryId);
                try {
                    fileDAO.insert(datamodelFile);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when creating file record for branch file: [").append(datamodelFile.getFileName()).append("].").append(" Exception: ").
                            append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }
            }
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
    }

    /**
     * Move a file from the opaque branch cemetery... i.e. restore a file from the cemetery to its former location.
     *
     * @param branchName the name of the branch.
     * @param destinationDirectoryId the directory to which the file will be restored.
     * @param fileId the file id.
     * @param workfileName the name that the file will be restored to.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction; if the file was not found in the branch cemetery, or if there
     * is a SQL exception in updating the file record.
     */
    public synchronized void moveFileFromOpaqueBranchCemetery(final String branchName, final int destinationDirectoryId, final int fileId, final String workfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        moveFileFromBranchCemetery(branchName, destinationDirectoryId, fileId, workfileName, response);
    }

    /**
     * Move a file from the translucent branch cemetery... i.e. restore a file from the cemetery to its former location.
     *
     * @param branchName the name of the branch.
     * @param destinationDirectoryId the directory to which the file will be restored.
     * @param fileId the file id.
     * @param workfileName the name that the file will be restored to.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction; if the file was not found in the branch cemetery, or if there
     * is a SQL exception in updating the file record.
     */
    public synchronized void moveFileFromTranslucentBranchCemetery(final String branchName, final int destinationDirectoryId, final int fileId, final String workfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        moveFileFromBranchCemetery(branchName, destinationDirectoryId, fileId, workfileName, response);
    }

    /**
     * Move a file from a branch cemetery... i.e. restore a file from the cemetery to its former location.
     *
     * @param branchName the name of the branch.
     * @param destinationDirectoryId the directory to which the file will be restored.
     * @param fileId the file id.
     * @param workfileName the name that the file will be restored to.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction; if the file was not found in the branch cemetery, or if there
     * is a SQL exception in updating the file record.
     */
    private synchronized void moveFileFromBranchCemetery(final String branchName, final int destinationDirectoryId, final int fileId, final String workfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to move file from branch cemetery without a surrounding transaction.", response);
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);

        if (branch != null) {
            com.qumasoft.server.datamodel.File datamodelFile = fileDAO.findById(branch.getBranchId(), fileId);
            if (datamodelFile != null) {
                // Verify that the current record is in the 'cemetery' for the branch.
                if (!datamodelFile.getDirectoryId().equals(-1)) {
                    throw new QVCSException("File not found in branch cemetery; fileId: [" + fileId + "]. Was found in directory with directory id of: ["
                            + datamodelFile.getDirectoryId() + "]");
                }
                datamodelFile.setDirectoryId(destinationDirectoryId);
                datamodelFile.setFileName(workfileName);
                datamodelFile.setDeletedFlag(false);
                try {
                    // Move the file to the destination directory.
                    fileDAO.update(datamodelFile, false);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when restoring file from cemetery on branch: [").append(datamodelFile.getFileName()).append("].").append(" Exception: ").
                            append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }

            }
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
    }

    /**
     * Delete a file from the trunk.
     *
     * @param originAppendedPath the origin appended path.
     * @param originDirectoryId the origin directory id.
     * @param cemeteryDirectoryId the cemetery directory id.
     * @param fileId the file id.
     * @param workfileName the workfile name.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction, or if the origin directory does not match what's in the
     * database, or if there is a SQL exception.
     */
    public synchronized void deleteFileFromTrunk(final String originAppendedPath, final int originDirectoryId, final int cemeteryDirectoryId,
            final int fileId, final String workfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to delete a file without a surrounding transaction.", response);
        String branchName = QVCSConstants.QVCS_TRUNK_VIEW;

        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
        if (branch != null) {
            com.qumasoft.server.datamodel.File datamodelFile = fileDAO.findById(branch.getBranchId(), fileId);
            if (!datamodelFile.getDirectoryId().equals(originDirectoryId)) {
                throw new QVCSException("Original directory does not match: db record directory: [" + datamodelFile.getDirectoryId()
                        + "], caller original filename: [" + originDirectoryId + "]");
            }
            datamodelFile.setDirectoryId(cemeteryDirectoryId);
            datamodelFile.setFileName(Utility.convertArchiveNameToShortWorkfileName(Utility.createCemeteryShortArchiveName(fileId)));
            try {
                fileDAO.update(datamodelFile, false);
            } catch (SQLException e) {
                StringBuilder message = new StringBuilder();
                message.append("SQLException when deleting file on trunk: [").append(workfileName).append("].").append(" Exception: ").append(e.getLocalizedMessage());
                LOGGER.error(message.toString());
                throw new QVCSException(message.toString());
            }
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
    }

    /**
     * Delete a file from an opaque branch.
     * @param branchName the branch name.
     * @param originDirectoryId the directory id of the directory containing the file.
     * @param cemeteryDirectoryId the cemetery directory id.
     * @param fileId the file id.
     * @param workfileName the workfile name.
     * @param response link to the client.
     * @throws QVCSException if there is no enclosing transaction, or for a number of other problems.
     */
    public synchronized void deleteFileFromOpaqueBranch(final String branchName, final int originDirectoryId, final int cemeteryDirectoryId,
            final int fileId, final String workfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        deleteFileFromBranch(branchName, originDirectoryId, cemeteryDirectoryId, fileId, response);
    }

    /**
     * Delete a file from a translucent branch.
     * @param branchName the branch name.
     * @param originDirectoryId the directory id of the directory containing the file.
     * @param cemeteryDirectoryId the cemetery directory id.
     * @param fileId the file id.
     * @param workfileName the workfile name.
     * @param response link to the client.
     * @throws QVCSException if there is no enclosing transaction, or for a number of other problems.
     */
    public synchronized void deleteFileFromTranslucentBranch(final String branchName, final int originDirectoryId, final int cemeteryDirectoryId,
            final int fileId, final String workfileName,
            ServerResponseFactoryInterface response) throws QVCSException {
        deleteFileFromBranch(branchName, originDirectoryId, cemeteryDirectoryId, fileId, response);
    }

    private synchronized void deleteFileFromBranch(final String branchName, final int originDirectoryId, final int cemeteryDirectoryId,
            final int fileId, ServerResponseFactoryInterface response) throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to delete file on branch without a surrounding transaction.", response);
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);

        if (branch != null) {
            deleteBranchFileRecordWithIsDeletedFlag(branch.getBranchId(), fileId);
            com.qumasoft.server.datamodel.File datamodelFile = fileDAO.findById(branch.getBranchId(), fileId);
            if (datamodelFile != null) {
                /*
                 * if (file record on branch exists)...
                 */
                // Verify that it's origin directory id matches what we found in the database.
                if (!datamodelFile.getDirectoryId().equals(originDirectoryId)) {
                    throw new QVCSException("Original directory does not match: db record directory: [" + datamodelFile.getDirectoryId()
                            + "], caller original filename: [" + originDirectoryId + "]");
                }
                /*
                 * if (file record exists on parent branch) mark the record deleted on the branch create an entry for the file in
                 * the branch's cemeter directory. else update the record to the cemeter directory on the branch.
                 */
                boolean fileExistsOnParentBranch = doesFileExistOnParentBranch(branch, fileId);
                if (fileExistsOnParentBranch) {
                    datamodelFile.setDeletedFlag(true);
                    try {
                        // Update the branch record to show it is deleted from the directory so we won't show it in the
                        // current location.
                        fileDAO.update(datamodelFile, false);
                    } catch (SQLException e) {
                        StringBuilder message = new StringBuilder();
                        message.append("SQLException when setting deleted flag on branch: [").append(datamodelFile.getFileName()).append("].")
                                .append(" Exception: ").append(e.getLocalizedMessage());
                        LOGGER.error(message.toString());
                        throw new QVCSException(message.toString());
                    }
                    datamodelFile.setDeletedFlag(false);
                    datamodelFile.setDirectoryId(-1);
                    try {
                        fileDAO.insert(datamodelFile);
                    } catch (SQLException e) {
                        StringBuilder message = new StringBuilder();
                        message.append("SQLException when creating cemetery record on branch file: [").append(datamodelFile.getFileName()).append("].").append(" Exception: ").
                                append(e.getLocalizedMessage());
                        LOGGER.error(message.toString());
                        throw new QVCSException(message.toString());
                    }
                } else {
                    datamodelFile.setDirectoryId(-1);
                    try {
                        // Move the file to the cemetery directory.
                        fileDAO.update(datamodelFile, false);
                    } catch (SQLException e) {
                        StringBuilder message = new StringBuilder();
                        message.append("SQLException when setting deleting file on branch: [").append(datamodelFile.getFileName()).append("].")
                                .append(" Exception: ").append(e.getLocalizedMessage());
                        LOGGER.error(message.toString());
                        throw new QVCSException(message.toString());
                    }
                }
            } else {
                /*
                 * else -- there is no file record for the branch, ergo it uses the parent branch's file record. create a record for
                 * the branch that has its deleted flag set to true. create an entry for the file in the branch's cemetery
                 * directory.
                 */
                // Walk up the parent branch tree until we find the file.
                datamodelFile = findFile(branch, fileId);
                if (!datamodelFile.getDirectoryId().equals(originDirectoryId)) {
                    throw new QVCSException("Original directory in parent branch does not match: db record directory: ["
                            + datamodelFile.getDirectoryId() + "], caller original filename: ["
                            + originDirectoryId + "]");
                }
                datamodelFile.setBranchId(branch.getBranchId());
                datamodelFile.setDeletedFlag(true);
                try {
                    fileDAO.insert(datamodelFile);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when creating deleted flag record on branch file: [").append(datamodelFile.getFileName()).append("].").append(" Exception: ").
                            append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }

                // Create an entry for the file in the branch's destination directory.
                datamodelFile.setDeletedFlag(false);
                datamodelFile.setBranchId(branch.getBranchId());
                datamodelFile.setDirectoryId(-1);
                try {
                    fileDAO.insert(datamodelFile);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when creating cemetery record for branch file: [").append(datamodelFile.getFileName()).append("].").append(" Exception: ").
                            append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }
            }
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
    }

    /**
     * Add a directory.
     *
     * @param viewName the branch name.
     * @param rootDirectoryId the root directory id for this project.
     * @param parentAppendedPath the appended path of the parent directory.
     * @param parentDirectoryID the parent directory id.
     * @param childDirectoryID the child directory id -- i.e. the directory id of the directory that we are adding to the parent
     * directory.
     * @param childDirectoryName the child directory name.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no surrounding transaction or if the branch does not exist.
     * @throws SQLException if the insert fails.
     */
    public synchronized void addDirectory(final String viewName, final int rootDirectoryId, final String parentAppendedPath, final int parentDirectoryID,
            final int childDirectoryID,
            final String childDirectoryName, ServerResponseFactoryInterface response) throws QVCSException, SQLException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to add a directory without a surrounding transaction.", response);
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, viewName);
        if (branch != null) {
            Directory directory = new Directory();
            directory.setDirectoryId(childDirectoryID);
            directory.setParentDirectoryId(parentDirectoryID);
            if (parentAppendedPath != null && parentAppendedPath.length() > 0) {
                directory.setAppendedPath(parentAppendedPath + QVCSConstants.QVCS_STANDARD_PATH_SEPARATOR_STRING + childDirectoryName);
            } else {
                directory.setAppendedPath(childDirectoryName);
            }
            directory.setBranchId(branch.getBranchId());
            directory.setDeletedFlag(false);
            directory.setRootDirectoryId(rootDirectoryId);
            // Prevent insertion of duplicates.
            if (null == directoryDAO.findById(directory.getBranchId(), directory.getDirectoryId())) {
                directoryDAO.insert(directory);
            }
        } else {
            throw new QVCSException("Did not find branch: [" + viewName + "]");
        }
    }

    /**
     * Delete a directory on the trunk. This does <b>not</b> check to see if the given directory is empty. This does not actually
     * delete the directory record; it merely updates the parent and root directory id's to -1.
     *
     * @param directoryId the directory id.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is no transaction, if an attempt is made to delete the root directory, or if the given
     * directory is not found, or if there is a SQL problem on the update.
     */
    public synchronized void deleteDirectoryOnTrunk(final int directoryId, ServerResponseFactoryInterface response)
            throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to delete a directory without a surrounding transaction.", response);
        String viewName = QVCSConstants.QVCS_TRUNK_VIEW;
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, viewName);
        if (branch != null) {
            Directory directory = directoryDAO.findById(branch.getBranchId(), directoryId);
            if (directory != null) {
                // Make sure we're not trying to delete the root directory -- we do not allow that.
                if (directory.getAppendedPath().equals("")) {
                    throw new QVCSException("Invalid attempt to delete the root directory.");
                }
                directory.setParentDirectoryId(-1);
                try {
                    directoryDAO.update(directory, false);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when updating directory for branch file: [").append(directory.getAppendedPath()).append("].").append(" Exception: ").
                            append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }
            } else {
                throw new QVCSException("Did not find directory to delete on trunk. Directory id: [" + directoryId + "]");
            }
        } else {
            throw new QVCSException("Did not find branch: [" + viewName + "]");
        }
    }


    /**
     * Delete a directory on an opaque branch.
     * @param viewName the branch name.
     * @param directoryId the directory id of the directory to delete.
     * @param response link to the client.
     * @throws QVCSException if there is no enclosing transaction, or for a number of other problems.
     */
    public synchronized void deleteDirectoryOnOpaqueBranch(final String viewName, final int directoryId, ServerResponseFactoryInterface response) throws QVCSException {
        deleteDirectoryOnBranch(viewName, directoryId, response);
    }

    /**
     * Delete a directory on a translucent branch.
     * @param viewName the branch name.
     * @param directoryId the directory id of the directory to delete.
     * @param response link to the client.
     * @throws QVCSException if there is no enclosing transaction, or for a number of other problems.
     */
    public synchronized void deleteDirectoryOnTranslucentBranch(final String viewName, final int directoryId, ServerResponseFactoryInterface response) throws QVCSException {
        deleteDirectoryOnBranch(viewName, directoryId, response);
    }

    private void deleteDirectoryOnBranch(final String branchName, final int directoryId, ServerResponseFactoryInterface response) throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to delete a directory on branch without a surrounding transaction.", response);
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);

        if (branch != null) {
            Directory datamodelDirectory = directoryDAO.findById(branch.getBranchId(), directoryId);
            if (datamodelDirectory != null) {
                // Make sure we're not trying to delete the root directory -- we do not allow that.
                if (datamodelDirectory.getAppendedPath().equals("")) {
                    throw new QVCSException("Invalid attempt to delete the root directory.");
                }
                /*
                 * if (directory record exists on parent branch) mark the record deleted on the branch create an entry for the file
                 * in the branch's cemetery directory. else update the record to the cemetery directory on the branch.
                 */
                boolean directoryExistsOnParentBranch = doesDirectoryExistOnParentBranch(branch, directoryId);
                if (directoryExistsOnParentBranch) {
                    datamodelDirectory.setDeletedFlag(true);
                    try {
                        // Update the branch record to show it is deleted so we won't show it in the current location.
                        directoryDAO.update(datamodelDirectory, false);
                    } catch (SQLException e) {
                        StringBuilder message = new StringBuilder();
                        message.append("SQLException when setting deleted flag on branch: [").append(datamodelDirectory.getAppendedPath()).append("].").append(" Exception: ").
                                append(e.getLocalizedMessage());
                        LOGGER.error(message.toString());
                        throw new QVCSException(message.toString());
                    }
                    datamodelDirectory.setDeletedFlag(false);
                    datamodelDirectory.setParentDirectoryId(-1);
                    try {
                        directoryDAO.insert(datamodelDirectory);
                    } catch (SQLException e) {
                        StringBuilder message = new StringBuilder();
                        message.append("SQLException when creating cemetery record on branch: [").append(datamodelDirectory.getAppendedPath()).append("].").append(" Exception: ").
                                append(e.getLocalizedMessage());
                        LOGGER.error(message.toString());
                        throw new QVCSException(message.toString());
                    }
                } else {
                    datamodelDirectory.setParentDirectoryId(-1);
                    try {
                        // Move the file to the cemetery directory.
                        directoryDAO.update(datamodelDirectory, false);
                    } catch (SQLException e) {
                        StringBuilder message = new StringBuilder();
                        message.append("SQLException when setting deleting file on branch: [").append(datamodelDirectory.getAppendedPath()).append("].").
                                append(" Exception: ").append(e.getLocalizedMessage());
                        LOGGER.error(message.toString());
                        throw new QVCSException(message.toString());
                    }
                }
            } else {
                /*
                 * else -- there is no directory record for the branch, ergo it uses the parent branch's directory record. create a
                 * record for the branch that has its deleted flag set to true. create an entry for the directory in the branch's
                 * cemetery directory.
                 */
                // Walk up the parent branch tree until we find the file.
                datamodelDirectory = findDirectory(branch, directoryId);
                // Make sure we're not trying to delete the root directory -- we do not allow that.
                if (datamodelDirectory.getAppendedPath().equals("")) {
                    throw new QVCSException("Invalid attempt to delete the root directory.");
                }
                datamodelDirectory.setBranchId(branch.getBranchId());
                datamodelDirectory.setDeletedFlag(true);
                try {
                    directoryDAO.insert(datamodelDirectory);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when creating deleted flag record on branch: [").append(datamodelDirectory.getAppendedPath()).append("].").append(" Exception: ").
                            append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }

                // Create an entry for the directory in the branch's cemetery.
                datamodelDirectory.setDeletedFlag(false);
                datamodelDirectory.setBranchId(branch.getBranchId());
                datamodelDirectory.setParentDirectoryId(-1);
                try {
                    directoryDAO.insert(datamodelDirectory);
                } catch (SQLException e) {
                    StringBuilder message = new StringBuilder();
                    message.append("SQLException when creating cemetery record for branch: [").append(datamodelDirectory.getAppendedPath()).append("].").append(" Exception: ").
                            append(e.getLocalizedMessage());
                    LOGGER.error(message.toString());
                    throw new QVCSException(message.toString());
                }
            }
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
    }

    @Override
    public synchronized void commitPendingChanges(ServerResponseFactoryInterface response, Date date) throws QVCSException {
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            if (!connection.getAutoCommit()) {
                connection.commit();
                connection.setAutoCommit(true);
            }
        } catch (IllegalStateException | SQLException e) {
            throw new QVCSException(Utility.expandStackTraceToString(e));
        }
    }

    /**
     * Get the directory contents for a translucent (a.k.a. feature) branch.
     *
     * @param projectView the project view that describes the translucent branch.
     * @param appendedPath the appended path for the directory.
     * @param directoryId the directory id.
     * @param response object to identify the client.
     * @return the directory contents for the translucent branch.
     * @throws QVCSException if we're not in a transaction.
     */
    public synchronized DirectoryContents getDirectoryContentsForTranslucentBranch(ProjectView projectView, String appendedPath, int directoryId,
            ServerResponseFactoryInterface response)
            throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to getDirectoryContentsForTranslucentBranch without a surrounding transaction.", response);
        DirectoryContents directoryContents;
        String parentBranch = projectView.getRemoteViewProperties().getBranchParent();
        DirectoryContents parentDirectoryContents = getParentBranchDirectoryContents(parentBranch, appendedPath, directoryId);

        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, projectView.getViewName());

        // Find the files on this branch.
        List<com.qumasoft.server.datamodel.File> fileList = fileDAO.findByBranchId(branch.getBranchId());

        // Find the child directories on this branch.
        List<com.qumasoft.server.datamodel.Directory> directoryList = directoryDAO.findByBranchId(branch.getBranchId());

        directoryContents = mergeChildBranchToParentBranchDirectoryContentsForTranslucentBranch(parentDirectoryContents, directoryId, appendedPath, fileList, directoryList);

        return directoryContents;
    }

    /**
     * Get the directory contents for the trunk.
     *
     * @param appendedPath the appended path.
     * @param directoryId the directory id.
     * @param response object to identify the client.
     * @return the directory contents for the trunk for the given directory id.
     * @throws QVCSException if we're not in a transaction.
     */
    public synchronized DirectoryContents getDirectoryContentsForTrunk(String appendedPath, int directoryId, ServerResponseFactoryInterface response) throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to getDirectoryContentsForTrunk without a surrounding transaction.", response);
        DirectoryContents directoryContents;

        // Find the files on the trunk.
        List<com.qumasoft.server.datamodel.File> fileList = fileDAO.findByBranchAndDirectoryId(getTrunkBranchId(), directoryId);

        // Find the child directories on the trunk.
        List<com.qumasoft.server.datamodel.Directory> directoryList = directoryDAO.findChildDirectories(getTrunkBranchId(), directoryId);

        directoryContents = new DirectoryContents(getProjectName(), directoryId, appendedPath);
        fileList.stream().forEach((file) -> {
            directoryContents.addFileID(file.getFileId(), file.getFileName());
        });

        directoryList.stream().forEach((directory) -> {
            directoryContents.addDirectoryID(directory.getDirectoryId(), Utility.getLastDirectorySegment(directory.getAppendedPath()));
        });

        return directoryContents;
    }

    /**
     * Get the directory contents for the trunk cemetery. This can return null if the cemetery has not been created yet.
     *
     * @param response object to identify the client.
     * @return the directory contents for the trunk's cemetery. This will be null if the cemetery has not been created yet (which
     * will happen if no files have been deleted).
     * @throws QVCSException if we're not in a transaction.
     */
    public synchronized DirectoryContents getDirectoryContentsForTrunkCemetery(ServerResponseFactoryInterface response) throws QVCSException {
        Integer cemeteryDirectoryId = null;
        DirectoryContents cemeteryDirectoryContents = null;
        Directory rootTrunkDirectory = directoryDAO.findByAppendedPath(getTrunkBranchId(), "");
        DirectoryContents trunkDirectoryContents = getDirectoryContentsForTrunk("", rootTrunkDirectory.getDirectoryId(), response);

        // Look in the root directory to see if there is a cemetery directory...
        Set<Integer> keys = trunkDirectoryContents.getChildDirectories().keySet();
        Map<Integer, String> map = trunkDirectoryContents.getChildDirectories();
        for (Integer key : keys) {
            String directoryName = map.get(key);
            if (directoryName.equals(QVCSConstants.QVCS_CEMETERY_DIRECTORY)) {
                cemeteryDirectoryId = key;
                break;
            }
        }

        if (cemeteryDirectoryId != null) {
            cemeteryDirectoryContents = getDirectoryContentsForTrunk(QVCSConstants.QVCS_CEMETERY_DIRECTORY, cemeteryDirectoryId, response);
        }
        return cemeteryDirectoryContents;
    }

    /**
     * Get the directory contents for a date based view.
     *
     * <p><b>This is currently coded to support the Trunk as the 'parent' of the date based view... i.e. we're only supporting a
     * date based view of the Trunk at this time.</b></p>
     *
     * @param projectView the project view.
     * @param appendedPath the appended path.
     * @param directoryId the directory id.
     * @param response object to identify the client.
     * @return the directory contents for the given directory at the view's point in time.
     * @throws QVCSException if we're not in a transaction, or if this is called for a non-date based view.
     */
    public synchronized DirectoryContents getDirectoryContentsForDateBasedView(ProjectView projectView, String appendedPath, int directoryId,
            ServerResponseFactoryInterface response)
            throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to getDirectoryContentsForDateBasedView without a surrounding transaction.", response);
        DirectoryContents directoryContents;
        if (!projectView.getRemoteViewProperties().getIsDateBasedViewFlag()) {
            throw new QVCSException("Invalid call to getDirectoryContentsForDateBasedView for non-date based view.");
        }
        Date viewDate = projectView.getRemoteViewProperties().getDateBasedDate();

        // Find the files on the trunk.
        List<com.qumasoft.server.datamodel.File> fileList = fileDAO.findByBranchAndDirectoryIdAndViewDate(getTrunkBranchId(), directoryId, viewDate);

        // Find the child directories that were created on or before the base date of the view.
        List<com.qumasoft.server.datamodel.Directory> directoryList = directoryDAO.findChildDirectoriesOnOrBeforeViewDate(getTrunkBranchId(), directoryId, viewDate);

        directoryContents = new DirectoryContents(getProjectName(), directoryId, appendedPath);
        fileList.stream().forEach((file) -> {
            directoryContents.addFileID(file.getFileId(), file.getFileName());
        });

        directoryList.stream().forEach((directory) -> {
            directoryContents.addDirectoryID(directory.getDirectoryId(), Utility.getLastDirectorySegment(directory.getAppendedPath()));
        });

        // Look through the history tables to see if there are any records there that affect things:
        // Look in file history for any records that we need to look at.
        FileHistoryDAO fileHistoryDAO = new FileHistoryDAOImpl();
        List<com.qumasoft.server.datamodel.FileHistory> fileHistoryList = fileHistoryDAO.findByBranchAndDirectoryIdAndViewDate(getTrunkBranchId(), directoryId, viewDate);

        // Find all the child directories in history created on or before the base date of the view.
        DirectoryHistoryDAO directoryHistoryDAO = new DirectoryHistoryDAOImpl();
        List<com.qumasoft.server.datamodel.DirectoryHistory> directoryHistoryList = directoryHistoryDAO.findChildDirectoriesOnOrBeforeViewDate(getTrunkBranchId(),
                directoryId, viewDate);

        Integer mostRecentFileId = -1;
        for (com.qumasoft.server.datamodel.FileHistory fileHistory : fileHistoryList) {
            if (!fileHistory.getFileId().equals(mostRecentFileId)) {
                mostRecentFileId = fileHistory.getFileId();
                if (!fileHistory.isDeletedFlag()) {
                    directoryContents.addFileID(fileHistory.getFileId(), fileHistory.getFileName());
                }
            }
            directoryContents.addFileID(fileHistory.getFileId(), fileHistory.getFileName());
        }

        Integer mostRecentDirectoryId = -1;
        for (com.qumasoft.server.datamodel.DirectoryHistory directoryHistory : directoryHistoryList) {
            if (!directoryHistory.getDirectoryId().equals(mostRecentDirectoryId)) {
                mostRecentDirectoryId = directoryHistory.getDirectoryId();
                if (!directoryHistory.isDeletedFlag()) {
                    directoryContents.addDirectoryID(directoryHistory.getDirectoryId(), Utility.getLastDirectorySegment(directoryHistory.getAppendedPath()));
                }
            }
        }
        return directoryContents;
    }

    /**
     * Get the directory contents for an opaque branch. <p><b>The current implementation only supports the Trunk as the parent
     * branch. We should be able to lift this restriction, but the current code enforces this restriction, and the algorithm will
     * only work for opaque branches that have the Trunk as their parent branch.</b></p>
     *
     * @param projectView the projectView that describes the opaque branch.
     * @param appendedPath the appended path.
     * @param directoryId the directory id.
     * @param response object to identify the client.
     * @return the directory contents for the given directory for the opaque branch.
     * @throws QVCSException if we're not in a transaction, or if this is called for an opaque branch that does not have the Trunk
     * as its parent branch.
     */
    public synchronized DirectoryContents getDirectoryContentsForOpaqueBranch(ProjectView projectView, String appendedPath, int directoryId,
            ServerResponseFactoryInterface response)
            throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to getDirectoryContentsForOpaqueBranch without a surrounding transaction.", response);
        DirectoryContents directoryContents;
        String parentBranchName = projectView.getRemoteViewProperties().getBranchParent();
        if (!parentBranchName.equals(QVCSConstants.QVCS_TRUNK_VIEW)) {
            throw new QVCSException("Opaque branch must have trunk as its parent branch.");
        }

        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, projectView.getViewName());

        Date branchDate = projectView.getRemoteViewProperties().getBranchDate();

        // Find the files on the trunk created on or before the branch creation time.
        List<com.qumasoft.server.datamodel.File> fileList = fileDAO.findByBranchAndDirectoryIdAndViewDate(getTrunkBranchId(), directoryId, branchDate);

        // Find the child directories that were created on or before the branch creation time.
        List<com.qumasoft.server.datamodel.Directory> directoryList = directoryDAO.findChildDirectoriesOnOrBeforeViewDate(getTrunkBranchId(), directoryId, branchDate);

        DirectoryContents parentBranchDirectoryContents = new DirectoryContents(getProjectName(), directoryId, appendedPath);
        fileList.stream().forEach((file) -> {
            parentBranchDirectoryContents.addFileID(file.getFileId(), file.getFileName());
        });

        directoryList.stream().forEach((directory) -> {
            parentBranchDirectoryContents.addDirectoryID(directory.getDirectoryId(), Utility.getLastDirectorySegment(directory.getAppendedPath()));
        });

        // Find the files on this branch.
        List<com.qumasoft.server.datamodel.File> branchFileList = fileDAO.findByBranchAndDirectoryId(branch.getBranchId(), directoryId);

        // Find the child directories on this branch.
        List<com.qumasoft.server.datamodel.Directory> branchDirectoryList = directoryDAO.findChildDirectories(branch.getBranchId(), directoryId);

        directoryContents = mergeChildBranchToParentBranchDirectoryContents(parentBranchDirectoryContents, directoryId, appendedPath, branchFileList, branchDirectoryList);

        return directoryContents;
    }

    /**
     * Get the directory contents for the translucent branch cemetery for the given branch name.
     * @param branchName the name of the branch.
     * @param response link to the client.
     * @return a directory contents object for the translucent branch cemetery for the given branch name.
     * @throws QVCSException if there is no containing transaction, or if the branch cannot be found.
     */
    public synchronized DirectoryContents getDirectoryContentsForTranslucentBranchCemetery(String branchName, ServerResponseFactoryInterface response)
            throws QVCSException {
        return getDirectoryContentsForBranchCemetery(branchName, response);
    }

    private DirectoryContents getDirectoryContentsForBranchCemetery(String branchName, ServerResponseFactoryInterface response) throws QVCSException {
        checkForContainingTransaction("#### INTERNAL ERROR: Attempt to get directory contents for branch cemetery without a surrounding transaction.", response);
        DirectoryContents directoryContents = null;
        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
        if (branch != null) {
            // Find the deleted files on this branch.
            List<com.qumasoft.server.datamodel.File> fileList = fileDAO.findByBranchId(branch.getBranchId());

            // Find the deleted child directories on this branch.
            List<com.qumasoft.server.datamodel.Directory> directoryList = directoryDAO.findByBranchId(branch.getBranchId());
            directoryContents = mergeChildBranchToParentBranchDirectoryContentsForTranslucentBranch(null, -1, QVCSConstants.QVCS_CEMETERY_DIRECTORY, fileList, directoryList);
        } else {
            throw new QVCSException("Branch not found: [" + branchName + "]");
        }
        return directoryContents;
    }

    @Override
    public int getPriority() {
        return TransactionParticipantInterface.HIGH_PRIORITY;
    }

    /**
     * Check that there is a containing active transaction for the client identified by the response object.
     *
     * @param errorMessage the message to include in the exception if there is no transaction.
     * @param response the response object that identifies the client for this request.
     * @throws QVCSException if there is not an active transaction.
     */
    private void checkForContainingTransaction(final String errorMessage, ServerResponseFactoryInterface response) throws QVCSException {
        if (!ServerTransactionManager.getInstance().transactionIsInProgress(response)) {
            LOGGER.warn(errorMessage);
            throw new QVCSException(errorMessage);
        }
    }

    /**
     * Does the directory exist on a parent branch. This is called only when the given directoryId is not found on a child branch.
     * We want to determine if the directory exists on the branch's parent branches, so this method 'walks' up the branch tree until
     * it gets to the Trunk. If it gets to the trunk without finding the directory then it returns false, otherwise, it will
     * presumably have found the directory somewhere along the path to the trunk, and it will return true.
     *
     * @param branch the branch from which we begin the search... i.e. we begin looking in this branch's parent branch.
     * @param directoryId the directory id to look for.
     * @return true if we find the directory on the given branch's parent branches somewhere.
     */
    private boolean doesDirectoryExistOnParentBranch(Branch branch, int directoryId) {
        boolean foundDirectoryOnParentBranch = false;
        ProjectView projectView = ViewManager.getInstance().getView(projectName, branch.getBranchName());
        if (projectView.getRemoteViewProperties().getParentProjectName() != null) {
            Branch parentBranch = branchDAO.findByProjectIdAndBranchName(projectId, projectView.getRemoteViewProperties().getBranchParent());
            Directory directory = directoryDAO.findById(parentBranch.getBranchId(), directoryId);
            if (directory != null) {
                foundDirectoryOnParentBranch = true;
            } else {
                if (!parentBranch.getBranchName().equals(QVCSConstants.QVCS_TRUNK_VIEW)) {
                    foundDirectoryOnParentBranch = doesDirectoryExistOnParentBranch(parentBranch, directoryId);
                }
            }
        }
        return foundDirectoryOnParentBranch;
    }

    /**
     * Does the file exist on a parent branch. This is called only when the given fileId is not found on a child branch. We want to
     * determine if the file exists on the branch's parent branches, so this method 'walks' up the branch tree until it gets to the
     * Trunk. If it gets to the trunk without finding the file then it returns false, otherwise, it will presumably have found the
     * file somewhere along the path to the trunk, and it will return true.
     *
     * @param branch the branch from which we begin the search... i.e. we begin looking in this branch's parent branch.
     * @param fileId the file id to look for.
     * @return true if we find the file on the given branch's parent branches somewhere.
     */
    private boolean doesFileExistOnParentBranch(Branch branch, int fileId) {
        boolean foundFileOnParentBranch = false;
        ProjectView projectView = ViewManager.getInstance().getView(projectName, branch.getBranchName());
        if (projectView.getRemoteViewProperties().getParentProjectName() != null) {
            Branch parentBranch = branchDAO.findByProjectIdAndBranchName(projectId, projectView.getRemoteViewProperties().getBranchParent());
            com.qumasoft.server.datamodel.File file = fileDAO.findById(parentBranch.getBranchId(), fileId);
            if (file != null) {
                Directory containingDirectory = directoryDAO.findById(file.getBranchId(), file.getDirectoryId());
                if (!containingDirectory.getAppendedPath().equals(QVCSConstants.QVCS_BRANCH_ARCHIVES_DIRECTORY)) {
                    foundFileOnParentBranch = true;
                }
            } else {
                if (!parentBranch.getBranchName().equals(QVCSConstants.QVCS_TRUNK_VIEW)) {
                    foundFileOnParentBranch = doesFileExistOnParentBranch(parentBranch, fileId);
                }
            }
        }
        return foundFileOnParentBranch;
    }

    /**
     * Walk up the branch tree until we find the directory.
     *
     * @param branch the branch where we'll look.
     * @param directoryId the directory id.
     * @return the directory (from the database).
     */
    private Directory findDirectory(Branch branch, int directoryId) {
        Directory directory = directoryDAO.findById(branch.getBranchId(), directoryId);
        if (directory == null) {
            ProjectView projectView = ViewManager.getInstance().getView(projectName, branch.getBranchName());
            if (projectView.getRemoteViewProperties().getParentProjectName() != null) {
                Branch parentBranch = branchDAO.findByProjectIdAndBranchName(projectId, projectView.getRemoteViewProperties().getBranchParent());
                directory = findDirectory(parentBranch, directoryId);
            }
        }
        if (directory == null) {
            throw new QVCSRuntimeException("Unable to find directory in database for branch: [" + branch.getBranchName() + "] directoryId: [" + directoryId + "]");
        }
        return directory;
    }

    /**
     * Walk up the branch tree until we find the file.
     *
     * @param branch the branch where we'll look.
     * @param fileId the file id.
     * @return the file (from the database).
     */
    private com.qumasoft.server.datamodel.File findFile(Branch branch, int fileId) {
        com.qumasoft.server.datamodel.File file = fileDAO.findById(branch.getBranchId(), fileId);
        if (file == null) {
            ProjectView projectView = ViewManager.getInstance().getView(projectName, branch.getBranchName());
            if (projectView.getRemoteViewProperties().getParentProjectName() != null) {
                Branch parentBranch = branchDAO.findByProjectIdAndBranchName(projectId, projectView.getRemoteViewProperties().getBranchParent());
                file = findFile(parentBranch, fileId);
            }
        }
        if (file == null) {
            throw new QVCSRuntimeException("Unable to find file in database for branch: [" + branch.getBranchName() + "] fileId: [" + fileId + "]");
        }
        return file;
    }

    /**
     * Get the database branch id for the project's trunk branch.
     *
     * @return the branch id for this project's trunk branch.
     * @throws QVCSException if we could not find the trunk branch.
     */
    private int getTrunkBranchId() throws QVCSException {
        if (trunkBranchId == null) {
            Branch trunk = branchDAO.findByProjectIdAndBranchName(projectId, QVCSConstants.QVCS_TRUNK_VIEW);
            if (trunk != null) {
                trunkBranchId = trunk.getBranchId();
            } else {
                throw new QVCSException("Did not find Trunk for project [" + getProjectName() + "]");
            }
        }
        return trunkBranchId;
    }

    /**
     * Recursive method to build the directory contents for a branch's parent branch. This method 'walks' its way to the trunk to
     * build the directory contents for the given parent branch.
     *
     * @param parentBranch the name of the parent branch.
     * @param appendedPath the appended path.
     * @param directoryId the directory id.
     * @return the directory contents of the given parent branch.
     */
    private DirectoryContents getParentBranchDirectoryContents(String parentBranch, String appendedPath, int directoryId) {
        DirectoryContents directoryContents;
        DirectoryContents parentDirectoryContents = null;
        if (!parentBranch.equals(QVCSConstants.QVCS_TRUNK_VIEW)) {
            // Walk up to the next parent branch... we'll only stop when we reach the trunk.
            ProjectView projectView = ViewManager.getInstance().getView(projectName, parentBranch);
            String parentParentBranch = projectView.getRemoteViewProperties().getBranchParent();
            parentDirectoryContents = getParentBranchDirectoryContents(parentParentBranch, appendedPath, directoryId);
        }

        Branch branch = branchDAO.findByProjectIdAndBranchName(projectId, parentBranch);

        // Find the files on this branch.
        List<com.qumasoft.server.datamodel.File> fileList = fileDAO.findByBranchAndDirectoryId(branch.getBranchId(), directoryId);

        // Find the child directories on this branch.
        List<com.qumasoft.server.datamodel.Directory> directoryList = directoryDAO.findChildDirectories(branch.getBranchId(), directoryId);

        directoryContents = mergeChildBranchToParentBranchDirectoryContents(parentDirectoryContents, directoryId, appendedPath, fileList, directoryList);
        return directoryContents;
    }

    /**
     * Merge the directory contents of the parent with the directory contents supplied in the file list and directory list.
     *
     * @param parentDirectoryContents the parent directory contents (may be null).
     * @param directoryId the directory id.
     * @param appendedPath the appended path.
     * @param fileList the list of files to update in the parent directory contents.
     * @param directoryList the list of directories to update in the parent directory contents.
     * @return
     */
    private DirectoryContents mergeChildBranchToParentBranchDirectoryContents(DirectoryContents parentDirectoryContents, int directoryId, String appendedPath,
            List<com.qumasoft.server.datamodel.File> fileList, List<Directory> directoryList) {
        DirectoryContents directoryContents;
        if (parentDirectoryContents == null) {
            directoryContents = new DirectoryContents(projectName, directoryId, appendedPath);
        } else {
            directoryContents = parentDirectoryContents;
        }

        fileList.stream().forEach((file) -> {
            if (file.isDeletedFlag()) {
                directoryContents.removeFileID(file.getFileId());
            } else {
                directoryContents.addFileID(file.getFileId(), file.getFileName());
            }
        });

        directoryList.stream().forEach((directory) -> {
            if (directory.isDeletedFlag()) {
                directoryContents.removeDirectoryID(directory.getDirectoryId());
            } else {
                directoryContents.addDirectoryID(directory.getDirectoryId(), Utility.getLastDirectorySegment(directory.getAppendedPath()));
            }
        });
        return directoryContents;
    }

    /**
     * Merge the directory contents of the parent with the directory contents supplied in the file list and directory list.
     *
     * @param parentDirectoryContents the parent directory contents (may be null).
     * @param directoryId the directory id.
     * @param appendedPath the appended path.
     * @param branchFileList the list of files on the branch.
     * @param branchDirectoryList the list of directories on the branch.
     * @return
     */
    private DirectoryContents mergeChildBranchToParentBranchDirectoryContentsForTranslucentBranch(DirectoryContents parentDirectoryContents, int directoryId, String appendedPath,
            List<com.qumasoft.server.datamodel.File> branchFileList, List<Directory> branchDirectoryList) {
        DirectoryContents directoryContents;
        if (parentDirectoryContents == null) {
            directoryContents = new DirectoryContents(projectName, directoryId, appendedPath);
        } else {
            directoryContents = parentDirectoryContents;
        }

        branchFileList.stream().forEach((file) -> {
            if (file.isDeletedFlag()) {
                if (file.getDirectoryId().equals(directoryId)) {
                    directoryContents.removeFileID(file.getFileId());
                }
            } else {
                if (file.getDirectoryId().equals(directoryId)) {
                    directoryContents.addFileID(file.getFileId(), file.getFileName());
                } else {
                    // The file is in a different directory on the branch.
                    directoryContents.removeFileID(file.getFileId());
                }
            }
        });

        branchDirectoryList.stream().forEach((directory) -> {
            if (directory.isDeletedFlag()) {
                if (directory.getParentDirectoryId().equals(directoryId)) {
                    directoryContents.removeDirectoryID(directory.getDirectoryId());
                }
            } else {
                if (directory.getParentDirectoryId().equals(directoryId)) {
                    directoryContents.addDirectoryID(directory.getDirectoryId(), Utility.getLastDirectorySegment(directory.getAppendedPath()));
                } else {
                    // The directory has a different parent directory on the branch.
                    directoryContents.removeDirectoryID(directory.getDirectoryId());
                }
            }
        });
        return directoryContents;
    }

    /**
     * Delete any file record on the branch that has the is deleted flag set to true.
     *
     * @param branchId the branch id.
     * @param fileId the file id.
     */
    private void deleteBranchFileRecordWithIsDeletedFlag(final int branchId, final int fileId) {
        com.qumasoft.server.datamodel.File markedForDeleteFile = fileDAO.findIsDeletedById(branchId, fileId);
        if (markedForDeleteFile != null) {
            fileDAO.deleteWithIsDeletedFlag(markedForDeleteFile);
        }
    }
}
