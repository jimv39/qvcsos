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
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.qvcslib.ServerResponseFactoryInterface;
import com.qumasoft.qvcslib.commandargs.UnLabelDirectoryCommandArgs;
import com.qumasoft.qvcslib.requestdata.ClientRequestUnLabelDirectoryData;
import com.qumasoft.server.clientrequest.ClientRequestUnLabelDirectory;
import com.qumasoft.server.dataaccess.BranchDAO;
import com.qumasoft.server.dataaccess.ProjectDAO;
import com.qumasoft.server.dataaccess.impl.BranchDAOImpl;
import com.qumasoft.server.dataaccess.impl.ProjectDAOImpl;
import com.qumasoft.server.datamodel.Branch;
import com.qumasoft.server.datamodel.Project;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.SQLException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Branch Manager. Manage the branches defined on the server. This is a singleton.
 * @author Jim Voris
 */
public final class BranchManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(BranchManager.class);
    private static final BranchManager BRANCH_MANAGER = new BranchManager();
    private boolean isInitializedFlagMember = false;
    private String branchStoreNameMember = null;
    private String branchStoreNameOldMember = null;
    private BranchStore branchStoreMember;

    /**
     * Creates a new instance of BranchManager.
     */
    private BranchManager() {
    }

    /**
     * Get the BranchManager singleton.
     * @return the BranchManager singleton.
     */
    public static BranchManager getInstance() {
        return BRANCH_MANAGER;
    }

    /**
     * Initialize the branch manager.
     * @return true if initialization succeeded; false otherwise.
     */
    public synchronized boolean initialize() {
        if (!isInitializedFlagMember) {
            branchStoreNameOldMember = getBranchStoreName() + ".old";

            loadBranchStore();
            isInitializedFlagMember = true;
        }
        return isInitializedFlagMember;
    }

    /**
     * Reset the branch store so it is empty.
     */
    synchronized void resetStore() {
        File branchStoreFile = new File(getBranchStoreName());
        if (branchStoreFile.exists()) {
            branchStoreFile.delete();
        }
    }

    private String getBranchStoreName() {
        if (branchStoreNameMember == null) {
            branchStoreNameMember = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_BRANCH_STORE_NAME + "dat";
        }
        return branchStoreNameMember;
    }

    private synchronized void loadBranchStore() {
        File branchStoreFile;

        try {
            branchStoreFile = new File(getBranchStoreName());

            // Use try with resources so we're guaranteed the file input stream is closed.
            try (FileInputStream fileInputStream = new FileInputStream(branchStoreFile)) {

                // Use try with resources so we're guaranteed the object input stream is closed.
                try (ObjectInputStream inStream = new ObjectInputStream(fileInputStream)) {
                    branchStoreMember = (BranchStore) inStream.readObject();
                }
            }
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            branchStoreMember = new BranchStore();
            writeBranchStore();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            // Serialization failed.  Create a default store.
            branchStoreMember = new BranchStore();
            writeBranchStore();
        } finally {
            branchStoreMember.initProjectBranchMap();
            branchStoreMember.dump();
        }
    }

    /**
     * Write the branch store to disk.
     */
    public synchronized void writeBranchStore() {
        FileOutputStream fileStream = null;

        try {
            File storeFile = new File(getBranchStoreName());
            File oldStoreFile = new File(branchStoreNameOldMember);

            if (oldStoreFile.exists()) {
                oldStoreFile.delete();
            }

            if (storeFile.exists()) {
                storeFile.renameTo(oldStoreFile);
            }

            File newStoreFile = new File(getBranchStoreName());

            // Make sure the needed directories exists
            if (!newStoreFile.getParentFile().exists()) {
                newStoreFile.getParentFile().mkdirs();
            }

            fileStream = new FileOutputStream(newStoreFile);

            // Use try with resources so we're guaranteed the object output stream is closed.
            try (ObjectOutputStream outStream = new ObjectOutputStream(fileStream)) {
                outStream.writeObject(branchStoreMember);
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private synchronized BranchStore getBranchStore() {
        return branchStoreMember;
    }

    /**
     * Get the branches associated with a given project.
     * @param projectName the project name.
     * @return the branches associated with the given project.
     */
    public synchronized Collection<ProjectBranch> getBranches(final String projectName) {
        return getBranchStore().getBranches(projectName);
    }

    /**
     * Get the ProjectBranch object for the given project and branch.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @return the ProjectBranch object that describes the given branch.
     */
    public synchronized ProjectBranch getBranch(final String projectName, final String branchName) {
        return getBranchStore().getBranch(projectName, branchName);
    }

    /**
     * Add a branch.
     * @param projectBranch the object that describes the branch.
     * @param schema the schema name.
     * @throws QVCSException if the branch type is not known, or if we cannot store the branch information onto the database.
     */
    public synchronized void addBranch(ProjectBranch projectBranch, String schema) throws QVCSException {
        getBranchStore().addBranch(projectBranch);
        writeBranchStore();

        ProjectDAO projectDAO = new ProjectDAOImpl(schema);
        Project project = projectDAO.findByProjectName(projectBranch.getProjectName());

        BranchDAO branchDAO = new BranchDAOImpl(schema);
        Branch branch = new Branch();
        branch.setBranchName(projectBranch.getBranchName());
        branch.setProjectId(project.getProjectId());
        int branchType = -1;
        if (projectBranch.getRemoteBranchProperties().getIsOpaqueBranchFlag()) {
            branchType = DatabaseManager.OPAQUE_BRANCH_TYPE;
        } else if (projectBranch.getRemoteBranchProperties().getIsFeatureBranchFlag()) {
            branchType = DatabaseManager.FEATURE_BRANCH_TYPE;
        } else if (projectBranch.getRemoteBranchProperties().getIsDateBasedBranchFlag()) {
            branchType = DatabaseManager.DATE_BASED_BRANCH_TYPE;
        } else {
            throw new QVCSException("Unknown branch type");
        }
        branch.setBranchTypeId(branchType);
        try {
            branchDAO.insert(branch);
        } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw new QVCSException("Failed to insert branch: [" + projectBranch.getBranchName() + "]");
        }
    }

    /**
     * Remove a branch. This removes the branch from the branch store, removes any branch based file labels, and gets rid of any file id
     * dictionary entries for the branch that we're removing.
     *
     * @param projectBranch the branch that we are to remove.
     * @param response an object that identifies the client.
     */
    public synchronized void removeBranch(ProjectBranch projectBranch, ServerResponseFactoryInterface response) {
        // Discard any directory managers for the branch.  Use an empty string for the
        // server name so we create a useful key prefix string since we're running
        // on the server.
        ArchiveDirManagerFactoryForServer.getInstance().discardBranchDirectoryManagers("", projectBranch.getProjectName(), projectBranch.getBranchName());

        getBranchStore().removeBranch(projectBranch);
        writeBranchStore();

        // Remove all file labels used for the branch.
        if (!projectBranch.getRemoteBranchProperties().getIsReadOnlyBranchFlag()
                || projectBranch.getRemoteBranchProperties().getIsOpaqueBranchFlag()
                || projectBranch.getRemoteBranchProperties().getIsFeatureBranchFlag()) {
            removeBranchLabel(projectBranch, response);
        }

        // Remove any file id's associated with the branch.
        FileIDDictionary.getInstance().removeIDsForBranch(projectBranch.getProjectName(), projectBranch.getBranchName());

        // TODO -- Would be a good idea to perform a cascading delete of records from FileHistory, File, Directory, and DirectoryHistory...
        // though strictly speaking, it is not required since there won't be any way to get to the records.

        // TODO -- Should we also delete the branch's record from the branch table? Why not?
    }

    private synchronized void removeBranchLabel(ProjectBranch projectBranch, ServerResponseFactoryInterface response) {
        // Use existing code to do the heavy lifting...
        UnLabelDirectoryCommandArgs commandArgs = new UnLabelDirectoryCommandArgs();
        commandArgs.setLabelString(deduceBranchLabel(projectBranch));
        commandArgs.setRecurseFlag(true);
        commandArgs.setUserName(QVCSConstants.QVCS_SERVER_USER);

        ClientRequestUnLabelDirectoryData clientRequestUnLabelDirectoryData = new ClientRequestUnLabelDirectoryData();
        clientRequestUnLabelDirectoryData.setAppendedPath("");
        clientRequestUnLabelDirectoryData.setCommandArgs(commandArgs);
        clientRequestUnLabelDirectoryData.setProjectName(projectBranch.getProjectName());
        clientRequestUnLabelDirectoryData.setBranchName(QVCSConstants.QVCS_TRUNK_BRANCH);

        ClientRequestUnLabelDirectory clientRequestUnLabelDirectory = new ClientRequestUnLabelDirectory(clientRequestUnLabelDirectoryData);
        clientRequestUnLabelDirectory.execute(QVCSConstants.QVCS_SERVER_USER, response);
    }

    private synchronized String deduceBranchLabel(ProjectBranch projectBranch) {
        String label = null;
        RemoteBranchProperties remoteBranchProperties = projectBranch.getRemoteBranchProperties();
        if (remoteBranchProperties.getIsOpaqueBranchFlag()) {
            label = projectBranch.getOpaqueBranchLabel();
        } else if (remoteBranchProperties.getIsFeatureBranchFlag()) {
            label = projectBranch.getFeatureBranchLabel();
        }
        return label;
    }
}
