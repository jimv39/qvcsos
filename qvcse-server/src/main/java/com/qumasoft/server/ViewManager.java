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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.RemoteViewProperties;
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
 * View Manager. Manage the views defined on the server. This is a singleton.
 * @author Jim Voris
 */
public final class ViewManager {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewManager.class);
    private static final ViewManager VIEW_MANAGER = new ViewManager();
    private boolean isInitializedFlagMember = false;
    private String viewStoreNameMember = null;
    private String viewStoreNameOldMember = null;
    private ViewStore viewStoreMember;

    /**
     * Creates a new instance of ViewManager.
     */
    private ViewManager() {
    }

    /**
     * Get the ViewManager singleton.
     * @return the ViewManager singleton.
     */
    public static ViewManager getInstance() {
        return VIEW_MANAGER;
    }

    /**
     * Initialize the view manager.
     * @return true if initialization succeeded; false otherwise.
     */
    public synchronized boolean initialize() {
        if (!isInitializedFlagMember) {
            viewStoreNameOldMember = getViewStoreName() + ".old";

            loadViewStore();
            isInitializedFlagMember = true;
        }
        return isInitializedFlagMember;
    }

    /**
     * Reset the view store so it is empty.
     */
    synchronized void resetStore() {
        File viewStoreFile = new File(getViewStoreName());
        if (viewStoreFile.exists()) {
            viewStoreFile.delete();
        }
    }

    private String getViewStoreName() {
        if (viewStoreNameMember == null) {
            viewStoreNameMember = System.getProperty("user.dir")
                    + File.separator
                    + QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY
                    + File.separator
                    + QVCSConstants.QVCS_VIEW_STORE_NAME + "dat";
        }
        return viewStoreNameMember;
    }

    private synchronized void loadViewStore() {
        File viewStoreFile;
        FileInputStream fileStream = null;

        try {
            viewStoreFile = new File(getViewStoreName());
            fileStream = new FileInputStream(viewStoreFile);
            ObjectInputStream inStream = new ObjectInputStream(fileStream);
            viewStoreMember = (ViewStore) inStream.readObject();
        } catch (FileNotFoundException e) {
            // The file doesn't exist yet. Create a default store.
            viewStoreMember = new ViewStore();
            writeViewStore();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);

            if (fileStream != null) {
                try {
                    fileStream.close();
                    fileStream = null;
                } catch (IOException ex) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }

            // Serialization failed.  Create a default store.
            viewStoreMember = new ViewStore();
            writeViewStore();
        } finally {
            viewStoreMember.initProjectViewMap();
            if (fileStream != null) {
                try {
                    fileStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
            viewStoreMember.dump();
        }
    }

    /**
     * Write the view store to disk.
     */
    public synchronized void writeViewStore() {
        FileOutputStream fileStream = null;

        try {
            File storeFile = new File(getViewStoreName());
            File oldStoreFile = new File(viewStoreNameOldMember);

            if (oldStoreFile.exists()) {
                oldStoreFile.delete();
            }

            if (storeFile.exists()) {
                storeFile.renameTo(oldStoreFile);
            }

            File newStoreFile = new File(getViewStoreName());

            // Make sure the needed directories exists
            if (!newStoreFile.getParentFile().exists()) {
                newStoreFile.getParentFile().mkdirs();
            }

            fileStream = new FileOutputStream(newStoreFile);
            ObjectOutputStream outStream = new ObjectOutputStream(fileStream);
            outStream.writeObject(viewStoreMember);
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

    private synchronized ViewStore getViewStore() {
        return viewStoreMember;
    }

    /**
     * Get the views associated with a given project.
     * @param projectName the project name.
     * @return the views associated with the given project.
     */
    public synchronized Collection<ProjectView> getViews(final String projectName) {
        return getViewStore().getViews(projectName);
    }

    /**
     * Get the ProjectView object for the given project and view.
     * @param projectName the project name.
     * @param viewName the view name.
     * @return the ProjectView object that describes the given view.
     */
    public synchronized ProjectView getView(final String projectName, final String viewName) {
        return getViewStore().getView(projectName, viewName);
    }

    /**
     * Add a view.
     * @param projectView the object that describes the view.
     * @throws QVCSException if the branch type is not known, or if we cannot store the view information onto the database.
     */
    public synchronized void addView(ProjectView projectView) throws QVCSException {
        getViewStore().addView(projectView);
        writeViewStore();

        ProjectDAO projectDAO = new ProjectDAOImpl();
        Project project = projectDAO.findByProjectName(projectView.getProjectName());

        BranchDAO branchDAO = new BranchDAOImpl();
        Branch branch = new Branch();
        branch.setBranchName(projectView.getViewName());
        branch.setProjectId(project.getProjectId());
        int branchType = -1;
        if (projectView.getRemoteViewProperties().getIsOpaqueBranchFlag()) {
            branchType = DatabaseManager.OPAQUE_BRANCH_TYPE;
        } else if (projectView.getRemoteViewProperties().getIsTranslucentBranchFlag()) {
            branchType = DatabaseManager.TRANSLUCENT_BRANCH_TYPE;
        } else if (projectView.getRemoteViewProperties().getIsDateBasedViewFlag()) {
            branchType = DatabaseManager.DATE_BASED_BRANCH_TYPE;
        } else {
            throw new QVCSException("Unknown branch type");
        }
        branch.setBranchTypeId(branchType);
        try {
            branchDAO.insert(branch);
        } catch (SQLException e) {
            LOGGER.error(e.getLocalizedMessage(), e);
            throw new QVCSException("Failed to insert view: [" + projectView.getViewName() + "]");
        }
    }

    /**
     * Remove a view. This removes the view from the view store, removes any view based file labels, and gets rid of any file id
     * dictionary entries for the view that we're removing.
     *
     * @param projectView the view that we are to remove.
     * @param response an object that identifies the client.
     */
    public synchronized void removeView(ProjectView projectView, ServerResponseFactoryInterface response) {
        // Discard any directory managers for the view.  Use an empty string for the
        // server name so we create a useful key prefix string since we're running
        // on the server.
        ArchiveDirManagerFactoryForServer.getInstance().discardViewDirectoryManagers("", projectView.getProjectName(), projectView.getViewName());

        getViewStore().removeView(projectView);
        writeViewStore();

        // Remove all file labels used for the view.
        if (!projectView.getRemoteViewProperties().getIsReadOnlyViewFlag()
                || projectView.getRemoteViewProperties().getIsOpaqueBranchFlag()
                || projectView.getRemoteViewProperties().getIsTranslucentBranchFlag()) {
            removeViewLabel(projectView, response);
        }

        // Remove any file id's associated with the view.
        FileIDDictionary.getInstance().removeIDsForView(projectView.getProjectName(), projectView.getViewName());

        // TODO -- Would be a good idea to perform a cascading delete of records from FileHistory, File, Directory, and DirectoryHistory...
        // though strictly speaking, it is not required since there won't be any way to get to the records.
    }

    private synchronized void removeViewLabel(ProjectView projectView, ServerResponseFactoryInterface response) {
        // Use existing code to do the heavy lifting...
        UnLabelDirectoryCommandArgs commandArgs = new UnLabelDirectoryCommandArgs();
        commandArgs.setLabelString(deduceViewLabel(projectView));
        commandArgs.setRecurseFlag(true);
        commandArgs.setUserName(QVCSConstants.QVCS_SERVER_USER);

        ClientRequestUnLabelDirectoryData clientRequestUnLabelDirectoryData = new ClientRequestUnLabelDirectoryData();
        clientRequestUnLabelDirectoryData.setAppendedPath("");
        clientRequestUnLabelDirectoryData.setCommandArgs(commandArgs);
        clientRequestUnLabelDirectoryData.setProjectName(projectView.getProjectName());
        clientRequestUnLabelDirectoryData.setViewName(QVCSConstants.QVCS_TRUNK_VIEW);

        ClientRequestUnLabelDirectory clientRequestUnLabelDirectory = new ClientRequestUnLabelDirectory(clientRequestUnLabelDirectoryData);
        clientRequestUnLabelDirectory.execute(QVCSConstants.QVCS_SERVER_USER, response);
    }

    private synchronized String deduceViewLabel(ProjectView projectView) {
        String label = null;
        RemoteViewProperties remoteViewProperties = projectView.getRemoteViewProperties();
        if (remoteViewProperties.getIsOpaqueBranchFlag()) {
            label = projectView.getOpaqueBranchLabel();
        } else if (remoteViewProperties.getIsTranslucentBranchFlag()) {
            label = projectView.getTranslucentBranchLabel();
        }
        return label;
    }
}
