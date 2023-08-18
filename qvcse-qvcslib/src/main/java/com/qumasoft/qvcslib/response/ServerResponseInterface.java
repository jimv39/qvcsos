/*   Copyright 2004-2023 Jim Voris
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
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import java.io.Serializable;

/**
 * Identify those methods that must be implemented on the client on receipt of a response from the server.
 * @author Jim Voris
 */
public interface ServerResponseInterface extends Serializable {

    /**
     * Update the directory manager proxy. Use the given directory manager proxy to perform any message specific updates.
     *
     * @param directoryManagerProxy the directory manager proxy.
     */
    void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy);

    /**
     * Get the type of response.
     * @return the type of response.
     */
    ResponseOperationType getOperationType();

    /**
     * The different response types.
     */
    enum ResponseOperationType {
        /** Begin transaction response. */
        SR_BEGIN_TRANSACTION,
        /** End transaction response. */
        SR_END_TRANSACTION,
        /** List client projects response. */
        SR_LIST_CLIENT_PROJECTS,
        /** Get revision response. */
        SR_GET_REVISION,
        /** Get directory response. */
        SR_GET_DIRECTORY,
        /** Get for visual compare response. */
        SR_GET_FOR_VISUAL_COMPARE,
        /** Get revision for compare response. */
        SR_GET_REVISION_FOR_COMPARE,
        /** Get user commit comments. */
        SR_GET_USER_COMMIT_COMMENTS,
        /** Add user property. */
        SR_ADD_USER_PROPERTY,
        /** Delete user property. */
        SR_DELETE_USER_PROPERTY,
        /** Get user properties. */
        SR_GET_USER_PROPERTIES,
        /** Update user property. */
        SR_UPDATE_USER_PROPERTY,
        /** Update view utility command. */
        SR_UPDATE_VIEW_UTILITY_COMMAND,
        /** Get commit list for moveable tag read-only branches response. */
        SR_GET_COMMIT_LIST_FOR_MOVEABLE_TAG_READ_ONLY_BRANCHES,
        /** Get brief commit info list for commit id file filter. */
        SR_GET_BRIEF_COMMIT_INFO_LIST,
        /** Check in response. */
        SR_CHECK_IN,
        /** Login response. */
        SR_LOGIN,
        /** Register client listener response. */
        SR_REGISTER_CLIENT_LISTENER,
        /** Create archive response. */
        SR_CREATE_ARCHIVE,
        /** Get logfile info response. */
        SR_GET_LOGFILE_INFO,
        /** Get all logfile info response. */
        SR_GET_ALL_LOGFILE_INFO,
        /** Set obsolete response. */
        SR_SET_OBSOLETE,
        /** Add directory response. */
        SR_ADD_DIRECTORY,
        /** Set comment prefix response. */
        SR_SET_COMMENT_PREFIX,
        /** Set module description response. */
        SR_SET_MODULE_DESCRIPTION,
        /** Set revision description response. */
        SR_SET_REVISION_DESCRIPTION,
        /** Rename file response. */
        SR_RENAME_FILE,
        /** Move file response. */
        SR_MOVE_FILE,
        /** Rename directory response. */
        SR_RENAME_DIRECTORY,
        /** Move directory response. */
        SR_MOVE_DIRECTORY,
        /** Delete directory response. */
        SR_DELETE_DIRECTORY,
        /** Get tags response. */
        SR_GET_TAGS,
        /** Get tags info response. */
        SR_GET_TAGS_INFO,
        /** Apply tag response. */
        SR_APPLY_TAG,
        /** Add revision response. */
        SR_ADD_REVISION,
        /** Get info for merge response. */
        SR_GET_INFO_FOR_MERGE,
        /** Resolve conflict from parent branch response. */
        SR_RESOLVE_CONFLICT_FROM_PARENT_BRANCH,
        /** List files to promote response. */
        SR_LIST_FILES_TO_PROMOTE,
        /** Promote file response. */
        SR_PROMOTE_FILE_SIMPLE,
        /** Promote file create response. */
        SR_PROMOTE_FILE_CREATE,
        /** Promote file rename response. */
        SR_PROMOTE_FILE_RENAME,
        /** Promote file move response. */
        SR_PROMOTE_FILE_MOVE,
        /** Promote file move and rename response. */
        SR_PROMOTE_FILE_MOVE_AND_RENAME,
        /** Promote file delete response. */
        SR_PROMOTE_FILE_DELETE,
        /** List projects response. */
        SR_LIST_PROJECTS,
        /** List branches response. */
        SR_LIST_BRANCHES,
        /** Add user response. */
        SR_ADD_USER,
        /** Remove user response. */
        SR_REMOVE_USER,
        /** Add user role response. */
        SR_ADD_USER_ROLE,
        /** Remove user role response. */
        SR_REMOVE_USER_ROLE,
        /** Assign user roles response. */
        SR_ASSIGN_USER_ROLES,
        /** List users response. */
        SR_LIST_USERS,
        /** List user roles response. */
        SR_LIST_USER_ROLES,
        /** Change user password response. */
        SR_CHANGE_USER_PASSWORD,
        /** List project users response. */
        SR_LIST_PROJECT_USERS,
        /** Get most recent activity response. */
        SR_GET_MOST_RECENT_ACTIVITY,
        /** Server shutdown response. */
        SR_SERVER_SHUTDOWN,
        /** Server create project response. */
        SR_SERVER_CREATE_PROJECT,
        /** Server delete project response. */
        SR_SERVER_DELETE_PROJECT,
        /** Server maintains project response. */
        SR_SERVER_MAINTAIN_PROJECT,
        /** Server list role names response. */
        SR_SERVER_LIST_ROLE_NAMES,
        /** Server list role privileges response. */
        SR_SERVER_LIST_ROLE_PRIVILEGES,
        /** Vanilla success response. */
        SR_RESPONSE_SUCCESS,
        /** Response message response. */
        SR_RESPONSE_MESSAGE,
        /** Response error response. */
        SR_RESPONSE_ERROR,
        /** Project control response. */
        SR_PROJECT_CONTROL,
        /** Heartbeat response. */
        SR_HEARTBEAT
    }
}
