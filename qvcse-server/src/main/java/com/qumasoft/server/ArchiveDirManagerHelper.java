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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;
import com.qumasoft.qvcslib.commandargs.LockRevisionCommandArgs;
import com.qumasoft.qvcslib.commandargs.SetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.commandargs.UnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.logfileaction.ActionType;
import com.qumasoft.qvcslib.logfileaction.ChangeOnBranch;
import com.qumasoft.qvcslib.logfileaction.CheckOut;
import com.qumasoft.qvcslib.logfileaction.Lock;
import com.qumasoft.qvcslib.logfileaction.MoveFile;
import com.qumasoft.qvcslib.logfileaction.Rename;
import com.qumasoft.qvcslib.logfileaction.SetRevisionDescription;
import com.qumasoft.qvcslib.logfileaction.Unlock;
import com.qumasoft.qvcslib.notifications.ServerNotificationCheckIn;
import com.qumasoft.qvcslib.notifications.ServerNotificationCheckOut;
import com.qumasoft.qvcslib.notifications.ServerNotificationCreateArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationHeaderChange;
import com.qumasoft.qvcslib.notifications.ServerNotificationInterface;
import com.qumasoft.qvcslib.notifications.ServerNotificationLock;
import com.qumasoft.qvcslib.notifications.ServerNotificationMoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRemoveArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationRenameArchive;
import com.qumasoft.qvcslib.notifications.ServerNotificationSetRevisionDescription;
import com.qumasoft.qvcslib.notifications.ServerNotificationUnlock;
import java.io.File;

/**
 * This helper class creates notification messages for feature branches.
 *
 * @author Jim Voris
 */
public final class ArchiveDirManagerHelper {

    // Hide the default constructor.
    private ArchiveDirManagerHelper() {
    }

    /**
     * Build the notification message that notifies of changes on a feature branch.
     *
     * @param archiveDirManagerInterface the directory where the change occurred.
     * @param subject the file that changed.
     * @param action the type of change.
     * @return a notification message that gets sent to all client listeners of the given directory.
     */
    static ServerNotificationInterface buildLogfileNotification(ArchiveDirManagerInterface archiveDirManagerInterface, ArchiveInfoInterface subject, ActionType action) {
        ServerNotificationInterface info = null;
        byte[] digest = subject.getDefaultRevisionDigest();

        switch (action.getAction()) {
            case ActionType.CHECKOUT:
                if (action instanceof CheckOut) {
                    ServerNotificationCheckOut serverNotificationCheckOut = new ServerNotificationCheckOut();
                    CheckOut checkOutAction = (CheckOut) action;
                    CheckOutCommandArgs commandArgs = checkOutAction.getCommandArgs();
                    serverNotificationCheckOut.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationCheckOut.setBranchName(archiveDirManagerInterface.getBranchName());
                    serverNotificationCheckOut.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationCheckOut.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationCheckOut.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverNotificationCheckOut.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    serverNotificationCheckOut.setRevisionString(commandArgs.getRevisionString());
                    info = serverNotificationCheckOut;
                }
                break;
            case ActionType.CHECKIN:
                ServerNotificationCheckIn serverNotificationCheckIn = new ServerNotificationCheckIn();
                serverNotificationCheckIn.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationCheckIn.setBranchName(archiveDirManagerInterface.getBranchName());
                serverNotificationCheckIn.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationCheckIn.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationCheckIn.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationCheckIn;
                break;
            case ActionType.LOCK:
                if (action instanceof Lock) {
                    ServerNotificationLock serverNotificationLock = new ServerNotificationLock();
                    Lock lockAction = (Lock) action;
                    LockRevisionCommandArgs commandArgs = lockAction.getCommandArgs();
                    serverNotificationLock.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationLock.setBranchName(archiveDirManagerInterface.getBranchName());
                    serverNotificationLock.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationLock.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationLock.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverNotificationLock.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationLock.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationLock;
                }
                break;
            case ActionType.CREATE:
                ServerNotificationCreateArchive serverNotificationCreateArchive = new ServerNotificationCreateArchive();
                serverNotificationCreateArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationCreateArchive.setBranchName(archiveDirManagerInterface.getBranchName());
                serverNotificationCreateArchive.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationCreateArchive.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationCreateArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationCreateArchive;
                break;
            case ActionType.MOVE_FILE:
                if (action instanceof MoveFile) {
                    MoveFile moveFileAction = (MoveFile) action;
                    ServerNotificationMoveArchive serverNotificationMoveArchive = new ServerNotificationMoveArchive();
                    serverNotificationMoveArchive.setShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationMoveArchive.setOriginAppendedPath(moveFileAction.getOriginAppendedPath());
                    serverNotificationMoveArchive.setDestinationAppendedPath(moveFileAction.getDestinationAppendedPath());
                    serverNotificationMoveArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationMoveArchive.setBranchName(archiveDirManagerInterface.getBranchName());
                    serverNotificationMoveArchive.setProjectProperties(archiveDirManagerInterface.getProjectProperties().getProjectProperties());
                    serverNotificationMoveArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationMoveArchive;
                }
                break;
            case ActionType.UNLOCK:
                if (action instanceof Unlock) {
                    ServerNotificationUnlock serverNotificationUnlock = new ServerNotificationUnlock();
                    Unlock unlockAction = (Unlock) action;
                    UnlockRevisionCommandArgs commandArgs = unlockAction.getCommandArgs();
                    serverNotificationUnlock.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationUnlock.setBranchName(archiveDirManagerInterface.getBranchName());
                    serverNotificationUnlock.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationUnlock.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationUnlock.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverNotificationUnlock.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationUnlock.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationUnlock;
                }
                break;
            case ActionType.SET_REVISION_DESCRIPTION:
                if (action instanceof SetRevisionDescription) {
                    ServerNotificationSetRevisionDescription serverNotificationSetRevisionDescription = new ServerNotificationSetRevisionDescription();
                    SetRevisionDescription setRevisionDescriptionAction = (SetRevisionDescription) action;
                    SetRevisionDescriptionCommandArgs commandArgs = setRevisionDescriptionAction.getCommandArgs();
                    serverNotificationSetRevisionDescription.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationSetRevisionDescription.setBranchName(archiveDirManagerInterface.getBranchName());
                    serverNotificationSetRevisionDescription.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationSetRevisionDescription.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationSetRevisionDescription.setRevisionDescription(commandArgs.getRevisionDescription());
                    serverNotificationSetRevisionDescription.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationSetRevisionDescription.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationSetRevisionDescription;
                }
                break;
            case ActionType.REMOVE:
                ServerNotificationRemoveArchive serverNotificationRemoveArchive = new ServerNotificationRemoveArchive();
                serverNotificationRemoveArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationRemoveArchive.setBranchName(archiveDirManagerInterface.getBranchName());
                serverNotificationRemoveArchive.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationRemoveArchive.setShortWorkfileName(subject.getShortWorkfileName());
                info = serverNotificationRemoveArchive;
                break;
            case ActionType.RENAME:
                if (action instanceof Rename) {
                    ServerNotificationRenameArchive serverNotificationRenameArchive = new ServerNotificationRenameArchive();
                    Rename renameAction = (Rename) action;
                    serverNotificationRenameArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationRenameArchive.setBranchName(archiveDirManagerInterface.getBranchName());
                    serverNotificationRenameArchive.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationRenameArchive.setNewShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationRenameArchive.setOldShortWorkfileName(renameAction.getOldShortWorkfileName());
                    serverNotificationRenameArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationRenameArchive;
                }
                break;
            case ActionType.CHANGE_ON_BRANCH:
                if (action instanceof ChangeOnBranch) {
                    ChangeOnBranch logfileActionChangeOnBranch = (ChangeOnBranch) action;
                    info = buildBranchNotification(archiveDirManagerInterface, subject, digest, logfileActionChangeOnBranch);
                }
                break;
            case ActionType.SET_OBSOLETE:
            case ActionType.LABEL:
            case ActionType.UNLABEL:
            case ActionType.CHANGE_HEADER:
            case ActionType.CHANGE_REVHEADER:
            case ActionType.SET_ATTRIBUTES:
            case ActionType.SET_COMMENT_PREFIX:
            case ActionType.SET_MODULE_DESCRIPTION:
            default:
                ServerNotificationHeaderChange serverNotificationHeaderChange = new ServerNotificationHeaderChange();
                serverNotificationHeaderChange.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationHeaderChange.setBranchName(archiveDirManagerInterface.getBranchName());
                serverNotificationHeaderChange.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationHeaderChange.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationHeaderChange.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationHeaderChange;
                break;
        }
        return info;
    }

    /**
     * Build the notification for a change that happened as a result of some change on the feature branch.
     *
     * @param archiveDirManagerInterface the
     * @param subject the archive info for the change.
     * @param digest the digest for the default revision on the branch.
     * @param logfileActionChangeOnBranch the type of change on the branch.
     * @return
     */
    private static ServerNotificationInterface buildBranchNotification(ArchiveDirManagerInterface archiveDirManagerInterface, ArchiveInfoInterface subject, byte[] digest,
            ChangeOnBranch logfileActionChangeOnBranch) {
        ServerNotificationInterface info = null;
        switch (logfileActionChangeOnBranch.getBranchActionType()) {
            case ChangeOnBranch.RENAME_ON_BRANCH:
                ServerNotificationRemoveArchive serverNotificationRemoveArchive = new ServerNotificationRemoveArchive();
                serverNotificationRemoveArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationRemoveArchive.setBranchName(archiveDirManagerInterface.getBranchName());
                serverNotificationRemoveArchive.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationRemoveArchive.setShortWorkfileName(logfileActionChangeOnBranch.getOldShortWorkfileName());
                info = serverNotificationRemoveArchive;
                break;
            case ChangeOnBranch.DELETE_ON_BRANCH:
            case ChangeOnBranch.MOVE_ON_BRANCH:
                // Eat the delete on branch and the move on branch notifications, as the directory manager sends separate notifies
                // for removes and moves.
                break;
            default:
                ServerNotificationHeaderChange serverNotificationHeaderChange = new ServerNotificationHeaderChange();
                serverNotificationHeaderChange.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationHeaderChange.setBranchName(archiveDirManagerInterface.getBranchName());
                serverNotificationHeaderChange.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationHeaderChange.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationHeaderChange.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationHeaderChange;
                break;
        }
        return info;
    }
}
