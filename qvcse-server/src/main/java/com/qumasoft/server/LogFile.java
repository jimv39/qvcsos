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

import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogFileInterface;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.LogfileListenerInterface;
import com.qumasoft.qvcslib.QVCSException;
import com.qumasoft.qvcslib.ReadWriteLock;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.commandargs.CheckInCommandArgs;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.commandargs.CreateArchiveCommandArgs;
import com.qumasoft.qvcslib.commandargs.GetRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnLabelRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import com.qumasoft.qvcslib.logfileaction.ChangeOnBranch;
import com.qumasoft.qvcslib.logfileaction.CheckIn;
import com.qumasoft.qvcslib.logfileaction.CheckOut;
import com.qumasoft.qvcslib.logfileaction.Create;
import com.qumasoft.qvcslib.logfileaction.Label;
import com.qumasoft.qvcslib.logfileaction.Lock;
import com.qumasoft.qvcslib.logfileaction.Remove;
import com.qumasoft.qvcslib.logfileaction.SetAttributes;
import com.qumasoft.qvcslib.logfileaction.SetCommentPrefix;
import com.qumasoft.qvcslib.logfileaction.SetIsObsolete;
import com.qumasoft.qvcslib.logfileaction.SetModuleDescription;
import com.qumasoft.qvcslib.logfileaction.SetRevisionDescription;
import com.qumasoft.qvcslib.logfileaction.UnLabel;
import com.qumasoft.qvcslib.logfileaction.Unlock;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The logfile class. This is the wrapper class for accessing a QVCS archive file. It guarantees that read and write operations are synchronized. If you want to
 * do anything with a QVCS archive file, you have to use this class, or you will be breaking the world.
 * @author Jim Voris
 */
public class LogFile implements ArchiveInfoInterface, LogFileInterface {

    private String fullArchiveFilename = null;
    private LogFileImpl logFileImpl = null;
    private ReadWriteLock readWriteLock = null;
    private List<LogfileListenerInterface> listeners = null;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(LogFile.class);

    /**
     * Creates a new instance of LogFile.
     *
     * @param fullArchiveName the full name of the archive file.
     */
    public LogFile(String fullArchiveName) {
        fullArchiveFilename = fullArchiveName;
        logFileImpl = new LogFileImpl(fullArchiveName);
        readWriteLock = new ReadWriteLock();
    }

    @Override
    public String getFullArchiveFilename() {
        return fullArchiveFilename;
    }

    /**
     * Read the archive file so that the header and revision information are at hand.
     * @return true if things worked; false otherwise.
     */
    public boolean readInformation() {
        boolean retVal = false;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.readInformation();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public int getFileID() {
        int retVal = -1;
        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getFileID();
        } catch (Exception e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            readWriteLock.releaseReadLock();
        }

        return retVal;
    }

    /**
     * Set the file id and remove view and branch labels. This is used after a 'reset' of the server, where we need to start fresh. In that case, the archive files need
     * to have any QVCS internal labels removed so the archive is in a known state, without any 'internal' labels.
     * @param fileID the new file id to use.
     */
    public void setFileIDAndRemoveViewAndBranchLabels(int fileID) {
        try {
            readWriteLock.getWriteLock();
            logFileImpl.setFileIDAndRemoveViewAndBranchLabels(fileID);
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            readWriteLock.releaseWriteLock();
        }
    }

    @Override
    public ArchiveAttributes getAttributes() {
        ArchiveAttributes retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getAttributes();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public boolean setAttributes(String userName, ArchiveAttributes attributes) throws QVCSException {
        boolean retVal = false;
        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.setAttributes(userName, attributes);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new SetAttributes(attributes));
        }
        return retVal;
    }

    /**
     * Get the comment prefix.
     * @return the comment prefix.
     */
    public String getCommentPrefix() {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getCommentPrefix();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public boolean setCommentPrefix(String userName, String newCommentPrefix) throws QVCSException {
        boolean retVal = false;
        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.setCommentPrefix(userName, newCommentPrefix);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new SetCommentPrefix(newCommentPrefix));
        }
        return retVal;
    }

    /**
     * Get the module description.
     * @return the module description.
     */
    public String getModuleDescription() {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getModuleDescription();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public boolean setModuleDescription(String userName, String moduleDescription) throws QVCSException {
        boolean retVal = false;
        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.setModuleDescription(userName, moduleDescription);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new SetModuleDescription(moduleDescription));
        }
        return retVal;
    }

    @Override
    public boolean setRevisionDescription(SetRevisionDescriptionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.setRevisionDescription(commandLineArgs);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new SetRevisionDescription(commandLineArgs));
        }
        return retVal;
    }

    @Override
    public String getRevisionDescription(final String revisionString) {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getRevisionDescription(revisionString);
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public String getShortWorkfileName() {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getShortWorkfileName();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    /**
     * Get the short <i>archive</i> file name.
     * @return the short <i>archive</i> file name.
     */
    public String getShortArchiveName() {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getShortArchiveName();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public String getLockedByString() {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getLockedByString();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    /**
     * Get a String showing who has locked revisions.
     * @return a String showing who has locked revisions.
     */
    public String getLockedByUser() {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getLockedByUser();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public java.util.Date getLastCheckInDate() {
        java.util.Date retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getLastCheckInDate();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public String getLastEditBy() {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getLastEditBy();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public String getWorkfileInLocation() {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getWorkfileInLocation();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public boolean getIsObsolete() {
        boolean retVal = false;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getIsObsolete();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public int getLockCount() {
        int retVal = 0;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getLockCount();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public byte[] getDefaultRevisionDigest() {
        byte[] retVal = null;

        try {
            readWriteLock.getReadLock();
            String defaultRevisionString = logFileImpl.getDefaultRevisionHeader().getRevisionString();
            retVal = ArchiveDigestManager.getInstance().getArchiveDigest(this, defaultRevisionString);
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    /**
     * Get a revision digest. This is the MD5 digest for the non-expanded workfile associated with the given revision.
     * @param revisionString the revision we're interested in.
     * @return the digest for the given revision.
     */
    public byte[] geRevisionDigest(String revisionString) {
        byte[] retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = ArchiveDigestManager.getInstance().getArchiveDigest(this, revisionString);
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    /**
     * Get the logfile header info.
     * @return the logfile header info.
     */
    public LogFileHeaderInfo getLogFileHeaderInfo() {
        LogFileHeaderInfo retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getLogFileHeaderInfo();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public int getRevisionCount() {
        int retVal = 0;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getRevisionCount();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public RevisionInformation getRevisionInformation() {
        RevisionInformation retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getRevisionInformation();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public String getLockedRevisionString(String userName) {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getLockedRevisionString(userName);
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    /**
     * Create an archive file.
     * @param commandLineArgs the command arguments.
     * @param projectProperties the project properties.
     * @param filename the file that we'll use to create the 1st revision.
     * @return true if things worked; false otherwise.
     */
    public boolean createArchive(CreateArchiveCommandArgs commandLineArgs, AbstractProjectProperties projectProperties, String filename) {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.createArchive(commandLineArgs, projectProperties, filename);
        } catch (QVCSException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new Create(commandLineArgs));
        }
        return retVal;
    }

    /**
     * Rename an archive.
     * @param userName the user name.
     * @param appendedPath the appended path.
     * @param oldShortWorkfileName the original short workfile name.
     * @param newShortWorkfileName the new short workfile name.
     * @param date the date to use for the operation.
     * @return true if things worked; false otherwise.
     */
    public boolean renameArchive(String userName, String appendedPath, String oldShortWorkfileName, String newShortWorkfileName, final Date date) {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.renameArchive(userName, appendedPath, oldShortWorkfileName, newShortWorkfileName, date);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new Remove());
        }
        return retVal;
    }

    /**
     * Move an archive.
     * @param userName user name.
     * @param appendedPath the appended path.
     * @param targetArchiveDirManagerInterface the destination directory manager.
     * @param shortWorkfileName the short workfile name.
     * @param date the date to use for the operation.
     * @return true if things worked; false otherwise.
     */
    public boolean moveArchive(String userName, String appendedPath, ArchiveDirManagerInterface targetArchiveDirManagerInterface, String shortWorkfileName, final Date date) {
        boolean retVal = false;

        try {
            // Cast the target to an actual ArchiveDirManager, since that is what
            // it MUST be here.
            if (targetArchiveDirManagerInterface instanceof ArchiveDirManager) {
                ArchiveDirManager targetArchiveDirManager = (ArchiveDirManager) targetArchiveDirManagerInterface;

                readWriteLock.getWriteLock();
                retVal = logFileImpl.moveArchive(userName, appendedPath, targetArchiveDirManager, shortWorkfileName, date);
            }
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new Remove());
        }
        return retVal;
    }

    /**
     * Delete an archive on a translucent branch.
     * @param userName the user name.
     * @param appendedPath the appended path.
     * @param shortWorkfileName the short workfile name.
     * @param date the date for the operation.
     * @param branchLabel the branch label that identifies the translucent branch.
     * @param archiveInfoForTranslucentBranch the archive info of the file on the translucent branch.
     * @return true if things worked; false otherwise.
     */
    public boolean deleteArchiveOnTranslucentBranch(String userName, String appendedPath, String shortWorkfileName,
            final Date date, final String branchLabel, ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch) {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.deleteArchiveOnTranslucentBranch(userName, appendedPath, shortWorkfileName, date, branchLabel, archiveInfoForTranslucentBranch);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            ChangeOnBranch logfileActionChangeOnBranch = new ChangeOnBranch(this, ChangeOnBranch.DELETE_ON_BRANCH);
            notifyLogfileListeners(logfileActionChangeOnBranch);
        }
        return retVal;
    }

    /**
     * Move a file on a translucent branch.
     * @param userName the user name.
     * @param appendedPath the appended path.
     * @param targetArchiveDirManager the directory manager for the target directory.
     * @param shortWorkfileName the short workfile name.
     * @param date the date for the operation.
     * @param branchLabel the branch label that identifies the translucent branch.
     * @param archiveInfoForTranslucentBranch the archive info of the file on the translucent branch.
     * @return true if things worked; false otherwise.
     */
    public boolean moveArchiveOnTranslucentBranch(String userName, String appendedPath, ArchiveDirManagerForTranslucentBranch targetArchiveDirManager, String shortWorkfileName,
            final Date date, final String branchLabel, ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch) {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.moveArchiveOnTranslucentBranch(userName, appendedPath, targetArchiveDirManager, shortWorkfileName, date, branchLabel,
                    archiveInfoForTranslucentBranch);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            ChangeOnBranch logfileActionChangeOnBranch = new ChangeOnBranch(this, ChangeOnBranch.MOVE_ON_BRANCH);
            notifyLogfileListeners(logfileActionChangeOnBranch);
        }
        return retVal;
    }

    /**
     * Rename a file on a translucent branch.
     *
     * @param userName the user name.
     * @param appendedPath the appended path
     * @param oldShortWorkfileName the old (original) short workfile name.
     * @param newShortWorkfileName the new short workfile name.
     * @param date the date of the transaction.
     * @param branchLabel the label that defines this translucent branch.
     * @param archiveInfoForTranslucentBranch the archive info object for the renamed file.
     * @return true of the rename succeeds; false otherwise.
     */
    public boolean renameArchiveOnTranslucentBranch(String userName, String appendedPath, String oldShortWorkfileName, String newShortWorkfileName,
            final Date date, final String branchLabel, ArchiveInfoForTranslucentBranch archiveInfoForTranslucentBranch) {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.renameArchiveOnTranslucentBranch(userName, appendedPath, oldShortWorkfileName, newShortWorkfileName, date, branchLabel,
                    archiveInfoForTranslucentBranch);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            ChangeOnBranch logfileActionChangeOnBranch = new ChangeOnBranch(this, ChangeOnBranch.RENAME_ON_BRANCH);
            logfileActionChangeOnBranch.setOldShortWorkfileName(oldShortWorkfileName);
            notifyLogfileListeners(logfileActionChangeOnBranch);
        }
        return retVal;
    }

    /**
     * Delete an archive (really move it to the cemetery).
     * @param userName the user name.
     * @param appendedPath the appended path.
     * @param targetArchiveDirManagerInterface the cemetery directory manager.
     * @param shortWorkfileName the short workfile name.
     * @param date the date to use for the operation.
     * @return true if things worked; false otherwise.
     */
    public boolean deleteArchive(String userName, String appendedPath, ArchiveDirManagerInterface targetArchiveDirManagerInterface, String shortWorkfileName, final Date date) {
        boolean retVal = false;

        try {
            // Cast the target to an actual ArchiveDirManager, since that is what
            // it MUST be here.
            if (targetArchiveDirManagerInterface instanceof ArchiveDirManager) {
                ArchiveDirManager targetArchiveDirManager = (ArchiveDirManager) targetArchiveDirManagerInterface;

                readWriteLock.getWriteLock();
                retVal = logFileImpl.deleteArchive(userName, appendedPath, targetArchiveDirManager, shortWorkfileName, date);
            }
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new Remove());
        }
        return retVal;
    }

    @Override
    public boolean checkInRevision(CheckInCommandArgs commandLineArgs, String filename, boolean ignoreLocksToEnableBranchCheckinFlag) throws QVCSException {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.checkInRevision(commandLineArgs, filename, ignoreLocksToEnableBranchCheckinFlag);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new CheckIn(commandLineArgs));
        }
        return retVal;
    }

    @Override
    public boolean getForVisualCompare(GetRevisionCommandArgs commandLineArgs, String outputFileName) throws QVCSException {
        return getRevision(commandLineArgs, outputFileName);
    }

    @Override
    public boolean getRevision(GetRevisionCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException {
        boolean retVal = false;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getRevision(commandLineArgs, fetchToFileName);
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    @Override
    public boolean checkOutRevision(CheckOutCommandArgs commandLineArgs, String fetchToFileName) throws QVCSException {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.checkOutRevision(commandLineArgs, fetchToFileName);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new CheckOut(commandLineArgs));
        }
        return retVal;
    }

    @Override
    public boolean lockRevision(LockRevisionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.lockRevision(commandLineArgs);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new Lock(commandLineArgs));
        }
        return retVal;
    }

    @Override
    public boolean unlockRevision(UnlockRevisionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.unlockRevision(commandLineArgs);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new Unlock(commandLineArgs));
        }
        return retVal;
    }

    @Override
    public boolean breakLock(UnlockRevisionCommandArgs commandLineArgs) throws QVCSException {
        throw new QVCSException("Unexpected call to breakLock method.");
    }

    @Override
    public boolean labelRevision(LabelRevisionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.labelRevision(commandLineArgs);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new Label(commandLineArgs));
        }
        return retVal;
    }

    @Override
    public boolean unLabelRevision(UnLabelRevisionCommandArgs commandLineArgs) throws QVCSException {
        boolean retVal = false;

        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.unLabelRevision(commandLineArgs);
        } finally {
            readWriteLock.releaseWriteLock();
        }
        if (retVal) {
            notifyLogfileListeners(new UnLabel(commandLineArgs));
        }
        return retVal;
    }

    /**
     * Add a logfile listener.
     * @param l the listener to add.
     */
    public synchronized void addListener(LogfileListenerInterface l) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        listeners.add(l);
    }

    /**
     * Remove a logfile listener.
     * @param l the listener to remove.
     */
    public synchronized void removeListener(LogfileListenerInterface l) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        } else {
            listeners.remove(l);
        }
    }

    private synchronized void notifyLogfileListeners(ActionType action) {
        if (listeners != null) {
            // Make a copy of the listener array so we can avoid concurrent modification exceptions
            // which happen when we handle the delete notification (which removes itself as a listener).
            ArrayList<LogfileListenerInterface> listenersCopy = new ArrayList<>(listeners);
            Iterator<LogfileListenerInterface> i = listenersCopy.iterator();
            while (i.hasNext()) {
                LogfileListenerInterface listener = i.next();
                listener.notifyLogfileListener(this, action);
            }
        }
    }

    synchronized List<LogfileListenerInterface> getLogfileListeners() {
        return listeners;
    }

    synchronized void clearLogfileListeners() {
        listeners = null;
    }

    @Override
    public LogfileInfo getLogfileInfo() {
        LogfileInfo retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getLogfileInfo();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    /**
     * Get the LogFileImpl for this LogFile. This should <i>only</i> be used for performing work in the server package -- e.g.
     * moving a file on a translucent branch.
     *
     * @return the LogFileImpl for this LogFile.
     */
    public LogFileImpl getLogFileImpl() {
        return logFileImpl;
    }

    @Override
    public boolean setIsObsolete(String userName, boolean flag) throws QVCSException {
        boolean retVal = false;
        try {
            readWriteLock.getWriteLock();
            retVal = logFileImpl.setIsObsolete(userName, flag);
            if (retVal) {
                notifyLogfileListeners(new SetIsObsolete(flag));
            }
        } finally {
            readWriteLock.releaseWriteLock();
        }
        return retVal;
    }

    /**
     * Return a buffer that contains the requested revision. This method is synchronous.
     *
     * @param revisionString the revision that should be fetched
     * @return a byte array containing the non-keyword expanded copy of the requested revision, or null if the revision cannot be
     * retrieved.
     */
    @Override
    public byte[] getRevisionAsByteArray(String revisionString) {
        byte[] worfileBuffer = null;

        try {
            readWriteLock.getReadLock();
            worfileBuffer = logFileImpl.getRevisionAsByteArray(revisionString);
        } finally {
            readWriteLock.releaseReadLock();
        }
        return worfileBuffer;
    }

    @Override
    public String getDefaultRevisionString() {
        String retVal = null;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.getDefaultRevisionHeader().getRevisionString();
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    /**
     * Has the given label been applied to this logfile.
     * @param label the label to search for.
     * @return true if this archive has the given label; false otherwise.
     */
    public boolean hasLabel(final String label) {
        boolean retVal = false;

        try {
            readWriteLock.getReadLock();
            retVal = logFileImpl.hasLabel(label);
        } finally {
            readWriteLock.releaseReadLock();
        }
        return retVal;
    }

    /**
     * We never have overlap in a logfile... only on branches.
     *
     * @return false, because we never report overlap on the trunk.
     */
    @Override
    public boolean getIsOverlap() {
        return false;
    }
}
