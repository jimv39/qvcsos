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
package com.qumasoft.server.dataaccess.impl;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.server.DirectoryIDManager;
import com.qumasoft.server.FileIDManager;
import com.qumasoft.server.dataaccess.DirectoryDAO;
import com.qumasoft.server.dataaccess.FileDAO;
import com.qumasoft.server.dataaccess.ProjectDAO;
import com.qumasoft.server.datamodel.Branch;
import com.qumasoft.server.datamodel.Directory;
import com.qumasoft.server.datamodel.Project;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test helper for putting things into the database for unit tests.
 *
 * @author Jim Voris
 */
public class DAOTestHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(DAOTestHelper.class);

    /**
     * Create a test project, and return its project id.
     *
     * @return the project id of the test project that we create.
     */
    public static int createTestProject() {
        int projectId = -1;
        try {
            Project project = new Project();
            project.setProjectName(TestHelper.getTestProjectName());
            ProjectDAO projectDAO = new ProjectDAOImpl();
            projectDAO.insert(project);
            Project foundProject = projectDAO.findByProjectName(TestHelper.getTestProjectName());
            projectId = foundProject.getProjectId();
        }
        catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return projectId;
    }

    /**
     * Create a trunk branch for the given project id. This will only work correctly if the database is empty... i.e. only if the Trunk branch has not been created yet.
     *
     * @param projectId the project id.
     * @return the branch id for the given project's trunk.
     */
    public static int createTrunkBranch(int projectId) {
        int branchId = -1;
        try {
            Branch branch = new Branch();
            branch.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);
            branch.setProjectId(projectId);
            branch.setBranchTypeId(1);
            BranchDAOImpl branchDAO = new BranchDAOImpl();
            branchDAO.insert(branch);
            Branch foundBranch = branchDAO.findByProjectIdAndBranchName(projectId, QVCSConstants.QVCS_TRUNK_BRANCH);
            branchId = foundBranch.getBranchId();
        } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return branchId;
    }

    /**
     * Create an opaque branch for the given project id.
     *
     * @param projectId the project id.
     * @param branchName the name of the opaque branch.
     * @return the branch id for the new opaque branch.
     */
    public static int createOpaqueBranch(int projectId, String branchName) {
        int branchId = -1;
        try {
            Branch branch = new Branch();
            branch.setBranchName(branchName);
            branch.setProjectId(projectId);
            branch.setBranchTypeId(4);
            BranchDAOImpl branchDAO = new BranchDAOImpl();
            branchDAO.insert(branch);
            Branch foundBranch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
            branchId = foundBranch.getBranchId();
        } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return branchId;
    }

    /**
     * Create a translucent branch for the given project id.
     *
     * @param projectId the project id.
     * @param branchName the name of the opaque branch.
     * @return the branch id for the new translucent branch.
     */
    public static int createTranslucentBranch(int projectId, String branchName) {
        int branchId = -1;
        try {
            Branch branch = new Branch();
            branch.setBranchName(branchName);
            branch.setProjectId(projectId);
            branch.setBranchTypeId(3);
            BranchDAOImpl branchDAO = new BranchDAOImpl();
            branchDAO.insert(branch);
            Branch foundBranch = branchDAO.findByProjectIdAndBranchName(projectId, branchName);
            branchId = foundBranch.getBranchId();
        } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
        }
        return branchId;
    }

    /**
     * Create the root directory for the given branch.
     *
     * @param directoryId the directory id.
     * @param branchId the branch id.
     * @return the directory id that was passed in, or -1 if there was a problem creating the record.
     */
    public static int createBranchRootDirectory(int directoryId, int branchId) {
        try {
            Directory directory = new Directory();
            directory.setAppendedPath("");
            directory.setBranchId(branchId);
            directory.setDeletedFlag(false);
            directory.setDirectoryId(directoryId);
            directory.setParentDirectoryId(null);
            directory.setRootDirectoryId(directoryId);
            DirectoryDAO directoryDAO = new DirectoryDAOImpl();
            directoryDAO.insert(directory);
        } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            directoryId = -1;
        }
        return directoryId;
    }

    /**
     * Create the root directory for the given branch.
     *
     * @param directoryId the directory id.
     * @param parentDirectoryId the parent directory id.
     * @param rootDirectoryId the root director id.
     * @param appendedPath the appended path.
     * @param branchId the branch id.
     * @return the directory id that was passed in, or -1 if there was a problem creating the record.
     */
    public static int createBranchChildDirectory(int directoryId, int parentDirectoryId, int rootDirectoryId, String appendedPath, int branchId) {
        try {
            Directory directory = new Directory();
            directory.setAppendedPath(appendedPath);
            directory.setBranchId(branchId);
            directory.setDeletedFlag(false);
            directory.setDirectoryId(directoryId);
            directory.setParentDirectoryId(parentDirectoryId);
            directory.setRootDirectoryId(rootDirectoryId);
            DirectoryDAO directoryDAO = new DirectoryDAOImpl();
            directoryDAO.insert(directory);
        } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            directoryId = -1;
        }
        return directoryId;
    }

    public static void populateDbWithTestFiles() {
        // Create the archive files... This probably isn't required, but we'll do it anyway
        TestHelper.initializeArchiveFiles();

        // Create the db entries to match the archive files.
        String projectRootDirName = System.getProperty("user.dir")
                + File.separator
                + QVCSConstants.QVCS_PROJECTS_DIRECTORY
                + File.separator
                + TestHelper.getTestProjectName();
        File projectRootDirectory = new File(projectRootDirName);
        ProjectDAO projectDAO = new ProjectDAOImpl();
        Project foundProject = projectDAO.findByProjectName(TestHelper.getTestProjectName());
        int projectId = foundProject.getProjectId();
        BranchDAOImpl branchDAO = new BranchDAOImpl();
        Branch foundBranch = branchDAO.findByProjectIdAndBranchName(projectId, QVCSConstants.QVCS_TRUNK_BRANCH);
        int branchId = foundBranch.getBranchId();
        int rootDirectoryId = addDirectory(projectRootDirectory, projectRootDirName, branchId, null);
        File[] children = projectRootDirectory.listFiles();
        addFiles(children, projectRootDirName, branchId, rootDirectoryId);
    }

    private static void addFiles(File[] files, String projectRootDirectoryName, int branchId, int directoryId) {
        for (File file : files) {
            if (file.isDirectory()) {
                int newDirectoryId = addDirectory(file, projectRootDirectoryName, branchId, directoryId);
                File[] children = file.listFiles();
                if (children.length > 0) {
                    addFiles(children, projectRootDirectoryName, branchId, newDirectoryId);
                }
            } else {
                addFile(file, projectRootDirectoryName, branchId, directoryId);
            }
        }
    }

    private static int addDirectory(File file, String projectRootDirectoryName, int branchId, Integer parentDirectoryId) {
        int retVal = -1;
        try {
            DirectoryDAO directoryDAO = new DirectoryDAOImpl();
            Directory directory = new Directory();
            String appendedPath = "";
            if (file.getCanonicalPath().substring(projectRootDirectoryName.length()).length() > 0) {
                appendedPath = file.getCanonicalPath().substring(projectRootDirectoryName.length() + 1);
            }
            directory.setAppendedPath(appendedPath);
            directory.setBranchId(branchId);
            directory.setDeletedFlag(false);
            directory.setDirectoryId(DirectoryIDManager.getInstance().getNewDirectoryID());
            directory.setParentDirectoryId(parentDirectoryId);
            directory.setRootDirectoryId(1);
            directoryDAO.insert(directory);
            retVal = directory.getDirectoryId();
        } catch (SQLException e) {
            // Nothing to do.
            System.out.println("Caught SQLException: " + e.getLocalizedMessage());
        } catch (IOException e) {
            // Nothing to do.
            System.out.println("Caught IOException: " + e.getLocalizedMessage());
        }
        return retVal;
    }

    private static void addFile(File file, String projectRootDirectoryName, int branchId, int directoryId) {
        try {
            FileDAO fileDAO = new FileDAOImpl();
            com.qumasoft.server.datamodel.File datamodelFile = new com.qumasoft.server.datamodel.File();
            datamodelFile.setFileId(FileIDManager.getInstance().getNewFileID());
            datamodelFile.setBranchId(branchId);
            datamodelFile.setDeletedFlag(false);
            datamodelFile.setDirectoryId(directoryId);
            datamodelFile.setFileName(file.getName());
            fileDAO.insert(datamodelFile);
        } catch (SQLException e) {
            // Nothing to do.
            System.out.println("Caught SQLException: " + e.getLocalizedMessage());
        }
    }
}
