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
package com.qumasoft.qvcslib;

import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import java.util.Collection;
import javax.swing.event.ChangeListener;

/**
 * Directory manager for the root directory. This is a special case, since this is for the root tree node, which never has any files, etc.
 * @author Jim Voris
 */
public class DirectoryManagerForRoot implements DirectoryManagerInterface {

    private final ArchiveDirManagerForRoot achiveDirManagerForRoot = new ArchiveDirManagerForRoot();
    private final WorkfileDirectoryManagerForRoot workfileDirManagerForRoot = new WorkfileDirectoryManagerForRoot();

    /**
     * Creates a new instance of DirectoryManagerForRoot.
     */
    public DirectoryManagerForRoot() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArchiveDirManagerInterface getArchiveDirManager() {
        return achiveDirManagerForRoot;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WorkfileDirectoryManagerInterface getWorkfileDirectoryManager() {
        return workfileDirManagerForRoot;
    }

    /**
     * {@inheritDoc}
     */
    public void setUserName(String userName) {
        achiveDirManagerForRoot.setUserName(userName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUserName() {
        return achiveDirManagerForRoot.getUserName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAppendedPath() {
        return "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractProjectProperties getProjectProperties() {
        return achiveDirManagerForRoot.getProjectProperties();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
    }

    /**
     * This always returns 0.
     * @return this always returns 0.
     */
    @Override
    public int getCount() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<MergedInfoInterface> getMergedInfoCollection() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProjectName() {
        return QVCSConstants.QWIN_DEFAULT_PROJECT_NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBranchName() {
        return QVCSConstants.QVCS_TRUNK_BRANCH;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MergedInfoInterface getMergedInfo(String shortWorkfileName) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MergedInfoInterface getMergedInfoByFileId(Integer fileId) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mergeManagers() throws QVCSException {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean getHasChanged() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHasChanged(boolean flag) {
    }

    /**
     * This always returns false, since you cannot create an archive here.
     * @param commandLineArgs the create command line arguments. (ignored).
     * @param filename the short workfile name. (ignored).
     * @return returns false always.
     */
    @Override
    public boolean createArchive(CreateArchiveCommandArgs commandLineArgs, String filename) {
        return false;
    }
}
