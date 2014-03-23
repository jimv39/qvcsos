//   Copyright 2004-2014 Jim Voris
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

import com.qumasoft.qvcslib.ArchiveDirManagerInterface;
import com.qumasoft.qvcslib.ArchiveInfoInterface;
import com.qumasoft.qvcslib.LogFileOperationCheckOutCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationLockRevisionCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationSetRevisionDescriptionCommandArgs;
import com.qumasoft.qvcslib.LogFileOperationUnlockRevisionCommandArgs;
import com.qumasoft.qvcslib.LogfileActionChangeOnBranch;
import com.qumasoft.qvcslib.LogfileActionCheckOut;
import com.qumasoft.qvcslib.LogfileActionLock;
import com.qumasoft.qvcslib.LogfileActionMoveFile;
import com.qumasoft.qvcslib.LogfileActionRename;
import com.qumasoft.qvcslib.LogfileActionSetRevisionDescription;
import com.qumasoft.qvcslib.LogfileActionType;
import com.qumasoft.qvcslib.LogfileActionUnlock;
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
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import java.io.File;

/**
 * This helper class creates notification messages for translucent branches.
 *
 * @author Jim Voris
 */
public final class ArchiveDirManagerHelper {

    // Hide the default constructor.
    private ArchiveDirManagerHelper() {
    }

    /**
     * Build the notification message that notifies of changes on a translucent branch.
     *
     * @param archiveDirManagerInterface the directory where the change occurred.
     * @param subject the file that changed.
     * @param action the type of change.
     * @return a notification message that gets sent to all client listeners of the given directory.
     */
    static ServerNotificationInterface buildLogfileNotification(ArchiveDirManagerInterface archiveDirManagerInterface, ArchiveInfoInterface subject, LogfileActionType action) {
        ServerNotificationInterface info = null;
        byte[] digest = subject.getDefaultRevisionDigest();

        switch (action.getAction()) {
            case LogfileActionType.CHECKOUT:
                if (action instanceof LogfileActionCheckOut) {
                    ServerNotificationCheckOut serverNotificationCheckOut = new ServerNotificationCheckOut();
                    LogfileActionCheckOut checkOutAction = (LogfileActionCheckOut) action;
                    LogFileOperationCheckOutCommandArgs commandArgs = checkOutAction.getCommandArgs();
                    serverNotificationCheckOut.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationCheckOut.setViewName(archiveDirManagerInterface.getViewName());
                    serverNotificationCheckOut.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationCheckOut.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationCheckOut.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverNotificationCheckOut.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    serverNotificationCheckOut.setRevisionString(commandArgs.getRevisionString());
                    info = serverNotificationCheckOut;
                }
                break;
            case LogfileActionType.CHECKIN:
                ServerNotificationCheckIn serverNotificationCheckIn = new ServerNotificationCheckIn();
                serverNotificationCheckIn.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationCheckIn.setViewName(archiveDirManagerInterface.getViewName());
                serverNotificationCheckIn.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationCheckIn.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationCheckIn.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationCheckIn;
                break;
            case LogfileActionType.LOCK:
                if (action instanceof LogfileActionLock) {
                    ServerNotificationLock serverNotificationLock = new ServerNotificationLock();
                    LogfileActionLock lockAction = (LogfileActionLock) action;
                    LogFileOperationLockRevisionCommandArgs commandArgs = lockAction.getCommandArgs();
                    serverNotificationLock.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationLock.setViewName(archiveDirManagerInterface.getViewName());
                    serverNotificationLock.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationLock.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationLock.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverNotificationLock.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationLock.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationLock;
                }
                break;
            case LogfileActionType.CREATE:
                ServerNotificationCreateArchive serverNotificationCreateArchive = new ServerNotificationCreateArchive();
                serverNotificationCreateArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationCreateArchive.setViewName(archiveDirManagerInterface.getViewName());
                serverNotificationCreateArchive.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationCreateArchive.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationCreateArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationCreateArchive;
                break;
            case LogfileActionType.MOVE_FILE:
                if (action instanceof LogfileActionMoveFile) {
                    LogfileActionMoveFile moveFileAction = (LogfileActionMoveFile) action;
                    ServerNotificationMoveArchive serverNotificationMoveArchive = new ServerNotificationMoveArchive();
                    serverNotificationMoveArchive.setShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationMoveArchive.setOriginAppendedPath(moveFileAction.getOriginAppendedPath());
                    serverNotificationMoveArchive.setDestinationAppendedPath(moveFileAction.getDestinationAppendedPath());
                    serverNotificationMoveArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationMoveArchive.setViewName(archiveDirManagerInterface.getViewName());
                    serverNotificationMoveArchive.setProjectProperties(archiveDirManagerInterface.getProjectProperties().getProjectProperties());
                    serverNotificationMoveArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationMoveArchive;
                }
                break;
            case LogfileActionType.UNLOCK:
                if (action instanceof LogfileActionUnlock) {
                    ServerNotificationUnlock serverNotificationUnlock = new ServerNotificationUnlock();
                    LogfileActionUnlock unlockAction = (LogfileActionUnlock) action;
                    LogFileOperationUnlockRevisionCommandArgs commandArgs = unlockAction.getCommandArgs();
                    serverNotificationUnlock.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationUnlock.setViewName(archiveDirManagerInterface.getViewName());
                    serverNotificationUnlock.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationUnlock.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationUnlock.setClientWorkfileName(commandArgs.getOutputFileName());
                    serverNotificationUnlock.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationUnlock.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationUnlock;
                }
                break;
            case LogfileActionType.SET_REVISION_DESCRIPTION:
                if (action instanceof LogfileActionSetRevisionDescription) {
                    ServerNotificationSetRevisionDescription serverNotificationSetRevisionDescription = new ServerNotificationSetRevisionDescription();
                    LogfileActionSetRevisionDescription setRevisionDescriptionAction = (LogfileActionSetRevisionDescription) action;
                    LogFileOperationSetRevisionDescriptionCommandArgs commandArgs = setRevisionDescriptionAction.getCommandArgs();
                    serverNotificationSetRevisionDescription.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationSetRevisionDescription.setViewName(archiveDirManagerInterface.getViewName());
                    serverNotificationSetRevisionDescription.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationSetRevisionDescription.setShortWorkfileName(commandArgs.getShortWorkfileName());
                    serverNotificationSetRevisionDescription.setRevisionDescription(commandArgs.getRevisionDescription());
                    serverNotificationSetRevisionDescription.setRevisionString(commandArgs.getRevisionString());
                    serverNotificationSetRevisionDescription.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationSetRevisionDescription;
                }
                break;
            case LogfileActionType.REMOVE:
                ServerNotificationRemoveArchive serverNotificationRemoveArchive = new ServerNotificationRemoveArchive();
                serverNotificationRemoveArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationRemoveArchive.setViewName(archiveDirManagerInterface.getViewName());
                serverNotificationRemoveArchive.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationRemoveArchive.setShortWorkfileName(subject.getShortWorkfileName());
                info = serverNotificationRemoveArchive;
                break;
            case LogfileActionType.RENAME:
                if (action instanceof LogfileActionRename) {
                    ServerNotificationRenameArchive serverNotificationRenameArchive = new ServerNotificationRenameArchive();
                    LogfileActionRename renameAction = (LogfileActionRename) action;
                    serverNotificationRenameArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                    serverNotificationRenameArchive.setViewName(archiveDirManagerInterface.getViewName());
                    serverNotificationRenameArchive.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                    serverNotificationRenameArchive.setNewShortWorkfileName(subject.getShortWorkfileName());
                    serverNotificationRenameArchive.setOldShortWorkfileName(renameAction.getOldShortWorkfileName());
                    serverNotificationRenameArchive.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                            subject.getShortWorkfileName(), subject.getIsOverlap()));
                    info = serverNotificationRenameArchive;
                }
                break;
            case LogfileActionType.CHANGE_ON_BRANCH:
                if (action instanceof LogfileActionChangeOnBranch) {
                    LogfileActionChangeOnBranch logfileActionChangeOnBranch = (LogfileActionChangeOnBranch) action;
                    info = buildBranchNotification(archiveDirManagerInterface, subject, digest, logfileActionChangeOnBranch);
                }
                break;
            case LogfileActionType.SET_OBSOLETE:
            case LogfileActionType.LABEL:
            case LogfileActionType.UNLABEL:
            case LogfileActionType.CHANGE_HEADER:
            case LogfileActionType.CHANGE_REVHEADER:
            case LogfileActionType.SET_ATTRIBUTES:
            case LogfileActionType.SET_COMMENT_PREFIX:
            case LogfileActionType.SET_MODULE_DESCRIPTION:
            default:
                ServerNotificationHeaderChange serverNotificationHeaderChange = new ServerNotificationHeaderChange();
                serverNotificationHeaderChange.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationHeaderChange.setViewName(archiveDirManagerInterface.getViewName());
                serverNotificationHeaderChange.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationHeaderChange.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationHeaderChange.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationHeaderChange;
                break;
        }
        return info;
    }

    /**
     * Build the notification for a change that happened as a result of some change on the translucent branch.
     *
     * @param archiveDirManagerInterface the
     * @param subject the translucent
     * @param digest the digest for the default revision on the branch.
     * @param logfileActionChangeOnBranch the type of change on the branch.
     * @return
     */
    private static ServerNotificationInterface buildBranchNotification(ArchiveDirManagerInterface archiveDirManagerInterface, ArchiveInfoInterface subject, byte[] digest,
            LogfileActionChangeOnBranch logfileActionChangeOnBranch) {
        ServerNotificationInterface info = null;
        switch (logfileActionChangeOnBranch.getBranchActionType()) {
            case LogfileActionChangeOnBranch.RENAME_ON_BRANCH:
                ServerNotificationRemoveArchive serverNotificationRemoveArchive = new ServerNotificationRemoveArchive();
                serverNotificationRemoveArchive.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationRemoveArchive.setViewName(archiveDirManagerInterface.getViewName());
                serverNotificationRemoveArchive.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationRemoveArchive.setShortWorkfileName(logfileActionChangeOnBranch.getOldShortWorkfileName());
                info = serverNotificationRemoveArchive;
                break;
            case LogfileActionChangeOnBranch.DELETE_ON_BRANCH:
            case LogfileActionChangeOnBranch.MOVE_ON_BRANCH:
                // Eat the delete on branch and the move on branch notifications, as the directory manager sends separate notifies
                // for removes and moves.
                break;
            default:
                ServerNotificationHeaderChange serverNotificationHeaderChange = new ServerNotificationHeaderChange();
                serverNotificationHeaderChange.setProjectName(archiveDirManagerInterface.getProjectName());
                serverNotificationHeaderChange.setViewName(archiveDirManagerInterface.getViewName());
                serverNotificationHeaderChange.setAppendedPath(archiveDirManagerInterface.getAppendedPath());
                serverNotificationHeaderChange.setShortWorkfileName(subject.getShortWorkfileName());
                serverNotificationHeaderChange.setSkinnyLogfileInfo(new SkinnyLogfileInfo(subject.getLogfileInfo(), File.separator, subject.getIsObsolete(), digest,
                        subject.getShortWorkfileName(), subject.getIsOverlap()));
                info = serverNotificationHeaderChange;
                break;
        }
        return info;
    }
}
