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
package com.qumasoft.guitools.qwin.operation;

import com.qumasoft.guitools.qwin.ProgressDialogInterface;
import com.qumasoft.guitools.qwin.ProjectTreeControl;
import com.qumasoft.guitools.qwin.QWinFrame;
import static com.qumasoft.guitools.qwin.QWinUtility.logProblem;
import static com.qumasoft.guitools.qwin.QWinUtility.warnProblem;
import com.qumasoft.guitools.qwin.dialog.AutoAddFilesDialog;
import com.qumasoft.guitools.qwin.dialog.ParentChildProgressDialog;
import com.qumasoft.guitools.qwin.dialog.ProgressDialog;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryManagerFactory;
import com.qumasoft.qvcslib.DirectoryManagerInterface;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ServerProperties;
import com.qumasoft.qvcslib.UserLocationProperties;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Operation auto-add files.
 * @author Jim Voris
 */
public final class OperationAutoAddFiles extends OperationBaseClass {

    private final String appendedPath;
    private final AbstractProjectProperties projectProperties;
    private final File currentWorkfileDirectory;
    private final String currentWorkfileDirectoryPath;

    /**
     * Auto add files.
     *
     * @param serverName the server name.
     * @param projectName the project name.
     * @param branchName the branch name.
     * @param path the appended path.
     * @param userLocationProperties user location properties.
     * @param projectProps project properties.
     * @param currWorkfileDirectory a File that represents the current workfile directory.
     */
    public OperationAutoAddFiles(String serverName, String projectName, String branchName, String path, UserLocationProperties userLocationProperties,
                                 AbstractProjectProperties projectProps, File currWorkfileDirectory) {
        super(null, serverName, projectName, branchName, userLocationProperties);

        appendedPath = path;
        projectProperties = projectProps;
        currentWorkfileDirectory = currWorkfileDirectory;
        currentWorkfileDirectoryPath = currWorkfileDirectory.getAbsolutePath();
    }

    String getAppendedPath() {
        return appendedPath;
    }

    String getProjectType() {
        return projectProperties.getProjectType();
    }

    AbstractProjectProperties getProjectProperties() {
        return projectProperties;
    }

    @Override
    public void executeOperation() {
        AutoAddFilesDialog autoAddFilesDialog = new AutoAddFilesDialog(QWinFrame.getQWinFrame(), true, this);
        autoAddFilesDialog.setVisible(true);
    }

    /**
     * Process the dialog choices.
     * @param recurseDirectories should we recurse directories; true for yes, false for no.
     * @param includeExtensions the extensions (comma separated) to include.
     * @param excludeExtensions the extensions (comma separated) to exclude.
     * @param createAllDirectories should we add an archive directory for any workfile directory (even if we don't add any files from that directory), true means yes,
     * false means no.
     */
    public void processDialogResult(boolean recurseDirectories, String includeExtensions, String excludeExtensions, boolean createAllDirectories) {
        final boolean fRecurseDirectories = recurseDirectories;
        final String[] fIncludeExtensions = buildExtensionList(includeExtensions);
        final String[] fExcludeExtensions = buildExtensionList(excludeExtensions);
        final boolean fCreateAllDirectories = createAllDirectories;
        final Set<String> fCreatedDirectoriesSet = new HashSet<>();

        // These values are invariant over the entire add process.  Capturing them here allows things to
        // work even if the user navigates off the current node.
        final String fServerName = getServerName();
        final AbstractProjectProperties fProjectProperties = getProjectProperties();

        final ProgressDialog fProgressDialog = createProgressDialog("Add Files to source control", 10);
        fProgressDialog.setAutoClose(false);

        final ParentChildProgressDialog fParentProgressDialog = createParentProgressDialog("Add Files to source control", 10);
        fParentProgressDialog.setAutoClose(false);

        final ServerProperties fServerProperties = ProjectTreeControl.getInstance().getActiveServer();

        Runnable worker = () -> {
            int transactionID = 0;
            try {
                transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(fServerProperties);
                if (!fRecurseDirectories) {
                    // We don't have to recurse directories.  This is the simpler case
                    // We just need to look for workfiles in the current directory
                    // that match the criteria defined by the user.  If the given
                    // file/files don't yet have an archive, then we need to create
                    // an archive for that file.
                    processForNonRecursion(fIncludeExtensions, fExcludeExtensions, fServerName, fProjectProperties, fProgressDialog, fCreatedDirectoriesSet);
                } else {
                    // We have to recurse directories.
                    processForRecursion(fIncludeExtensions, fExcludeExtensions, fCreateAllDirectories, fServerName, fProjectProperties, fParentProgressDialog,
                            fCreatedDirectoriesSet);
                }
            } finally {
                if (fRecurseDirectories) {
                    fParentProgressDialog.close();
                } else {
                    fProgressDialog.close();
                }
                ClientTransactionManager.getInstance().sendEndTransaction(fServerProperties, transactionID);
            }
        };

        // Put all this on a separate worker thread.
        new Thread(worker).start();
    }

    private void processForNonRecursion(String[] includeExtensions, String[] excludeExtensions, String serverName, AbstractProjectProperties projectProps,
                                        ProgressDialogInterface progressDialog, Set<String> createdDirectoriesSet) {
        if (includeExtensions != null) {
            processIncludeExtensions(includeExtensions, "", true, serverName, projectProps, progressDialog, createdDirectoriesSet);
        } else if (excludeExtensions != null) {
            processExcludeExtensions(excludeExtensions, "", true, serverName, projectProps, progressDialog, createdDirectoriesSet);
        } else {
            processAllExtensions("", true, serverName, projectProps, progressDialog, createdDirectoriesSet);
        }
    }

    private void processForRecursion(String[] includeExtensions, String[] excludeExtensions, boolean createAllDirectories, String serverName,
                                     AbstractProjectProperties projectProps, final ParentChildProgressDialog progressDialog, Set<String> createdDirectoriesSet) {
        // First create a collection of all the subdirectories beneath the
        // current directory.
        List<String> subDirectories = createSubdirectoryCollection(new ArrayList<>(), currentWorkfileDirectory.getAbsolutePath());

        // Now iterate over that collection, creating the archives for
        // the requested files.
        Iterator it = subDirectories.iterator();

        int max = subDirectories.size() + 1;
        int count = 0;
        OperationBaseClass.initParentChildProgressDialog("Adding files", 0, max, progressDialog);

        while (it.hasNext() && !progressDialog.getIsCancelled()) {
            String subDirectory = (String) it.next();

            updateParentChildProgressDialog(count++, "Adding files for directory " + subDirectory, progressDialog);

            if (includeExtensions != null) {
                processIncludeExtensions(includeExtensions, subDirectory, createAllDirectories, serverName, projectProps, progressDialog, createdDirectoriesSet);
            } else if (excludeExtensions != null) {
                processExcludeExtensions(excludeExtensions, subDirectory, createAllDirectories, serverName, projectProps, progressDialog, createdDirectoriesSet);
            } else {
                processAllExtensions(subDirectory, createAllDirectories, serverName, projectProps, progressDialog, createdDirectoriesSet);
            }
        }

        // And finish up by processing the current directory.
        processForNonRecursion(includeExtensions, excludeExtensions, serverName, projectProps, progressDialog, createdDirectoriesSet);
    }

    private void processIncludeExtensions(String[] extensions, String recursedDirectoryName, boolean createAllDirectories, String serverName,
                                          AbstractProjectProperties projectProps, ProgressDialogInterface progressDialog, Set<String> createdDirectoriesSet) {
        try {
            String appendPath;
            String workfilePath;

            if (getAppendedPath().length() > 0) {
                if (recursedDirectoryName.length() > 0) {
                    appendPath = Utility.endsWithoutPathSeparator(getAppendedPath()) + File.separator + recursedDirectoryName;
                } else {
                    appendPath = getAppendedPath();
                }
            } else {
                appendPath = recursedDirectoryName;
            }
            if (recursedDirectoryName.length() > 0) {
                workfilePath = Utility.endsWithoutPathSeparator(currentWorkfileDirectory.getAbsolutePath()) + File.separator + recursedDirectoryName;
            } else {
                workfilePath = currentWorkfileDirectory.getAbsolutePath();
            }
            String userName = DirectoryManagerFactory.getInstance().getServerUsername(serverName);
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), appendPath);
            DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(),
                    serverName,
                    directoryCoordinate,
                    getProjectType(),
                    projectProps,
                    workfilePath,
                    null, false);
            if (createAllDirectories) {
                createArchiveDirectory(directoryManager, serverName, createdDirectoriesSet);
            }

            // Merge the managers so we'll have a collection to operate on...
            directoryManager.mergeManagers();

            // Get the list of mergedInfo objects we need to operate on...
            Iterator it = directoryManager.getMergedInfoCollection().iterator();
            int size = directoryManager.getMergedInfoCollection().size();
            OperationBaseClass.initProgressDialog("Directory: " + workfilePath, 0, size, progressDialog);
            int createCount = 0;
            int k = 0;
            while (it.hasNext() && !progressDialog.getIsCancelled()) {
                MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();
                if (mergedInfo.getArchiveInfo() == null) {
                    for (String extension : extensions) {
                        String shortWorkfileName = mergedInfo.getShortWorkfileName();
                        if (projectProps.getIgnoreCaseFlag()) {
                            shortWorkfileName = shortWorkfileName.toLowerCase();
                        }
                        if (shortWorkfileName.endsWith(extension)) {
                            createArchive(directoryManager, serverName, mergedInfo, userName, createAllDirectories, createCount++, createdDirectoriesSet);
                            updateProgressDialog(k, "Creating archive for: " + mergedInfo.getShortWorkfileName(), progressDialog);
                            break;
                        }
                    }
                }
                k++;
            }
        } catch (QVCSException e) {
            warnProblem(e.getLocalizedMessage());
        }
    }

    private void processExcludeExtensions(String[] extensions, String recursedDirectoryName, boolean createAllDirectories, String serverName,
                                          AbstractProjectProperties projectProps, ProgressDialogInterface progressDialog, Set<String> createdDirectoriesSet) {
        try {
            String appendPath;
            String workfilePath;

            if (getAppendedPath().length() > 0) {
                if (recursedDirectoryName.length() > 0) {
                    appendPath = Utility.endsWithoutPathSeparator(getAppendedPath()) + File.separator + recursedDirectoryName;
                } else {
                    appendPath = getAppendedPath();
                }
            } else {
                appendPath = recursedDirectoryName;
            }
            if (recursedDirectoryName.length() > 0) {
                workfilePath = Utility.endsWithoutPathSeparator(currentWorkfileDirectory.getAbsolutePath()) + File.separator + recursedDirectoryName;
            } else {
                workfilePath = currentWorkfileDirectory.getAbsolutePath();
            }
            String userName = DirectoryManagerFactory.getInstance().getServerUsername(serverName);
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), appendPath);
            DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(),
                    serverName,
                    directoryCoordinate,
                    getProjectType(),
                    projectProps,
                    workfilePath,
                    null, false);
            if (createAllDirectories) {
                createArchiveDirectory(directoryManager, serverName, createdDirectoriesSet);
            }

            // Merge the managers so we'll have a collection to operate on...
            directoryManager.mergeManagers();

            // Build the list of mergedInfo objects we need to operate on...
            Iterator it = directoryManager.getMergedInfoCollection().iterator();
            int size = directoryManager.getMergedInfoCollection().size();
            OperationBaseClass.initProgressDialog("Directory: " + workfilePath, 0, size, progressDialog);
            int createCount = 0;
            int k = 0;
            while (it.hasNext() && !progressDialog.getIsCancelled()) {
                MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();
                if (mergedInfo.getArchiveInfo() == null) {
                    // There is no archive for this one yet...
                    // See if it matches the include extension filter.
                    boolean excludedExtension = false;
                    for (String extension : extensions) {
                        String shortWorkfileName = mergedInfo.getShortWorkfileName();
                        if (projectProps.getIgnoreCaseFlag()) {
                            shortWorkfileName = shortWorkfileName.toLowerCase();
                        }
                        if (shortWorkfileName.endsWith(extension)) {
                            excludedExtension = true;
                            break;
                        }
                    }
                    // If it's not an excluded extension...
                    if (!excludedExtension) {
                        createArchive(directoryManager, serverName, mergedInfo, userName, createAllDirectories, createCount++, createdDirectoriesSet);
                        OperationBaseClass.updateProgressDialog(k, mergedInfo.getShortWorkfileName(), progressDialog);
                    }
                }
                k++;
            }
        } catch (QVCSException e) {
            warnProblem(e.getLocalizedMessage());
        }
    }

    private void processAllExtensions(String recursedDirectoryName, boolean createAllDirectories, String serverName, AbstractProjectProperties projectProps,
                                      final ProgressDialogInterface progressDialog, Set<String> createdDirectoriesSet) {
        try {
            String appendPath;
            String workfilePath;

            if (getAppendedPath().length() > 0) {
                if (recursedDirectoryName.length() > 0) {
                    appendPath = Utility.endsWithoutPathSeparator(getAppendedPath()) + File.separator + recursedDirectoryName;
                } else {
                    appendPath = getAppendedPath();
                }
            } else {
                appendPath = recursedDirectoryName;
            }
            if (recursedDirectoryName.length() > 0) {
                workfilePath = Utility.endsWithoutPathSeparator(currentWorkfileDirectory.getAbsolutePath()) + File.separator + recursedDirectoryName;
            } else {
                workfilePath = currentWorkfileDirectory.getAbsolutePath();
            }
            String userName = DirectoryManagerFactory.getInstance().getServerUsername(serverName);
            DirectoryCoordinate directoryCoordinate = new DirectoryCoordinate(getProjectName(), getBranchName(), appendPath);
            DirectoryManagerInterface directoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(),
                    serverName,
                    directoryCoordinate,
                    getProjectType(),
                    projectProps,
                    workfilePath,
                    null, false);

            if (createAllDirectories) {
                createArchiveDirectory(directoryManager, serverName, createdDirectoriesSet);
            }

            // Merge the managers so we'll have a collection to operate on...
            directoryManager.mergeManagers();

            // Get the list of mergedInfo objects we need to operate on...
            Iterator it = directoryManager.getMergedInfoCollection().iterator();
            int size = directoryManager.getMergedInfoCollection().size();
            OperationBaseClass.initProgressDialog("Directory: " + workfilePath, 0, size, progressDialog);
            int k = 0;
            int createCount = 0;
            while (it.hasNext() && !progressDialog.getIsCancelled()) {
                MergedInfoInterface mergedInfo = (MergedInfoInterface) it.next();
                if (mergedInfo.getArchiveInfo() == null) {
                    // There is no archive for this one yet...
                    createArchive(directoryManager, serverName, mergedInfo, userName, createAllDirectories, createCount++, createdDirectoriesSet);
                    OperationBaseClass.updateProgressDialog(k, mergedInfo.getShortWorkfileName(), progressDialog);
                }
                k++;
            }
        } catch (QVCSException e) {
            warnProblem(e.getLocalizedMessage());
        }
    }

    private void createArchive(DirectoryManagerInterface directoryManager, String serverName, MergedInfoInterface mergedInfo, String userName, boolean createAllDirectories,
            int createCount, Set<String> createdDirectoriesSet) {
        try {
            // Create the archive directory if it hasn't been created yet...
            if ((!createAllDirectories) && (createCount == 0)) {
                createArchiveDirectory(directoryManager, serverName, createdDirectoriesSet);
            }

            CreateArchiveCommandArgs commandLineArgs = new CreateArchiveCommandArgs();
            commandLineArgs.setArchiveDescription("Auto-Added");
            commandLineArgs.setCommentPrefix("");
            commandLineArgs.setInputfileTimeStamp(new Date(mergedInfo.getWorkfileInfo().getWorkfile().lastModified()));
            commandLineArgs.setLockFlag(false);
            commandLineArgs.setUserName(userName);
            commandLineArgs.setWorkfileName(mergedInfo.getShortWorkfileName());
            logProblem("Requesting creation of archive for:" + mergedInfo.getFullWorkfileName());

            // And create the archive
            directoryManager.createArchive(commandLineArgs, mergedInfo.getFullWorkfileName());
        } catch (IOException | QVCSException e) {
            warnProblem(e.getLocalizedMessage());
        }
    }

    private String[] buildExtensionList(String extensions) {
        String[] extensionList = null;

        if (extensions != null) {
            // Build the list of extensions
            // This splits based on commas, or white space.
            extensionList = extensions.split("[,\\s]");
        }

        return extensionList;
    }

    private List<String> createSubdirectoryCollection(ArrayList<String> collection, String parentDirectoryName) {
        File directory = new File(parentDirectoryName);
        File[] subDirectories = directory.listFiles(new DirectoryFilter());
        String currentWorkfileDirectoryPathWithTermination = Utility.endsWithPathSeparator(currentWorkfileDirectoryPath);
        if (subDirectories != null) {
            for (File subDirectory : subDirectories) {
                String nameToAdd = subDirectory.getAbsolutePath().substring(currentWorkfileDirectoryPathWithTermination.length());
                collection.add(nameToAdd);
                createSubdirectoryCollection(collection, subDirectory.getAbsolutePath());
            }
        }
        return collection;
    }

    /**
     * Create the archive directory if we haven't already requested its creation. (And create any missing parent directories as well!!).
     *
     * @param directoryManager the directory manager for which to create an associated archive directory (if needed).
     * @param serverName the name of the qvcs enterprise server.
     * @param createdDirectories the set of directories that we've already created.
     */
    private void createArchiveDirectory(DirectoryManagerInterface directoryManager, String serverName, Set<String> createdDirectories) {
        if (!createdDirectories.contains(directoryManager.getAppendedPath())) {
            createAnyMissingParentDirectories(directoryManager, serverName, createdDirectories);
            createdDirectories.add(directoryManager.getAppendedPath());
            ArchiveDirManagerInterface archiveDirManager = directoryManager.getArchiveDirManager();
            archiveDirManager.createDirectory();
        }
    }

    private void createAnyMissingParentDirectories(DirectoryManagerInterface directoryManager, String serverName, Set<String> createdDirectories) {
        String appendPath = directoryManager.getAppendedPath();
        do {
            int index = appendPath.lastIndexOf(File.separatorChar);
            if (index >= 0) {
                int chopThisMuchOffWorkfilePath = appendPath.length() - index;
                String workfileDirectory = directoryManager.getWorkfileDirectoryManager().getWorkfileDirectory();
                String parentWorkfileDirectory = workfileDirectory.substring(0, workfileDirectory.length() - chopThisMuchOffWorkfilePath);
                appendPath = appendPath.substring(0, index);
                DirectoryManagerInterface parentDirectoryManager = DirectoryManagerFactory.getInstance().getDirectoryManager(
                        QWinFrame.getQWinFrame().getQvcsClientHomeDirectory(),
                        serverName,
                        new DirectoryCoordinate(getProjectName(), getBranchName(), appendPath),
                        getProjectType(),
                        getProjectProperties(),
                        parentWorkfileDirectory,
                        null,
                        false);
                if (!createdDirectories.contains(parentDirectoryManager.getAppendedPath())) {
                    createdDirectories.add(parentDirectoryManager.getAppendedPath());
                    ArchiveDirManagerInterface parentArchiveDirManager = parentDirectoryManager.getArchiveDirManager();
                    parentArchiveDirManager.createDirectory();
                }
                directoryManager = parentDirectoryManager;
            } else {
                appendPath = "";
            }
        } while (appendPath.length() > 0);
    }

    static class DirectoryFilter implements java.io.FilenameFilter {

        DirectoryFilter() {
        }

        @Override
        public boolean accept(java.io.File dir, String name) {
            boolean retVal = false;
            java.io.File file = new java.io.File(dir.getPath(), name);

            if (file.isDirectory()) {
                boolean ignoreHiddenDirectoriesFlag = QWinFrame.getQWinFrame().getUserProperties().getIgnoreHiddenDirectoriesFlag();
                if (ignoreHiddenDirectoriesFlag) {
                    if (name.startsWith(".")) {
                        retVal = false;
                    } else {
                        retVal = !file.isHidden();
                    }
                } else {
                    retVal = true;
                }
            }
            return retVal;
        }
    }

}
