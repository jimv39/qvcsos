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
package com.qumasoft.qvcslib;

/**
 * Client request data interface.
 * @author Jim Voris
 */
public interface ClientRequestDataInterface extends java.io.Serializable {

    /**
     * Get the Set of valid request data element types allowed for this request.
     * @return the Set of valid request data element types allowed for this request.
     */
    ValidRequestElementType[] getValidElements();

    /**
     * Get the operation type.
     * @return the operation type.
     */
    ClientRequestDataInterface.RequestOperationType getOperationType();

    /**
     * The types of operations that may be requested. These identify the different types of request messages that can be sent from the client to the server.
     */
    public enum RequestOperationType {
        /** Begin a transaction. */
        BEGIN_TRANSACTION,
        /** End a transaction. */
        END_TRANSACTION,
        /** List client projects. */
        LIST_CLIENT_PROJECTS,
        /** List client views. */
        LIST_CLIENT_VIEWS,
        /** Get a revision. */
        GET_REVISION,
        /** Get the files in a directory. */
        GET_DIRECTORY,
        /** Get a revision for use in visual compare. */
        GET_FOR_VISUAL_COMPARE,
        /** Get a revision for compare. */
        GET_REVISION_FOR_COMPARE,
        /** Checkout a file revision. */
        CHECK_OUT,
        /** Checkout all files in a directory. */
        CHECK_OUT_DIRECTORY,
        /** Checkin a file. */
        CHECK_IN,
        /** Checkin the files in a directory. */
        CHECK_IN_DIRECTORY,
        /** Lock a file revision. */
        LOCK,
        /** Lock the files in a directory. */
        LOCK_DIRECTORY,
        /** Login to the server. */
        LOGIN,
        /** Unlock a file revision. */
        UNLOCK,
        /** Unlock the files in a directory. */
        UNLOCK_DIRECTORY,
        /** Break any locks on a file. */
        BREAK_LOCK,
        /** Break any locks on the files in a directory. */
        BREAK_LOCK_DIRECTORY,
        /** Register as a client listener to a directory. */
        REGISTER_CLIENT_LISTENER,
        /** Create an archive file. */
        CREATE_ARCHIVE,
        /** Apply a label to a file. */
        LABEL,
        /** Apply a label to the files in a directory. */
        LABEL_DIRECTORY,
        /** Remove a label. */
        REMOVE_LABEL,
        /** Remove a label from the files in a directory. */
        REMOVE_LABEL_DIRECTORY,
        /** Get the logfile information for a file. */
        GET_LOGFILE_INFO,
        /** Set a file obsolete. */
        SET_OBSOLETE,
        /** Undelete a file. */
        UNDELETE_FILE,
        /** Add a directory. */
        ADD_DIRECTORY,
        /** Set QVCS archive attributes for a file. */
        SET_ATTRIBUTES,
        /** Set the comment prefix for a file. */
        SET_COMMENT_PREFIX,
        /** Set the module description for a file. */
        SET_MODULE_DESCRIPTION,
        /** Set the revision description (checkin comment) for a file revision. */
        SET_REVISION_DESCRIPTION,
        /** Rename a file. */
        RENAME_FILE,
        /** Move a file. */
        MOVE_FILE,
        /** Rename a directory. */
        RENAME_DIRECTORY,
        /** Move a directory. */
        MOVE_DIRECTORY,
        /** Delete a directory. */
        DELETE_DIRECTORY,
        /** Get information needed for a merge operation. */
        GET_INFO_FOR_MERGE,
        /** Resolve a conflict from the parent branch. */
        RESOLVE_CONFLICT_FROM_PARENT_BRANCH,
        /** Get a list of files to promote. */
        LIST_FILES_TO_PROMOTE,
        /** Promote a file. */
        PROMOTE_FILE,
        /** List projects. */
        LIST_PROJECTS,
        /** Add a user to the server. */
        ADD_USER,
        /** Remove a user. */
        REMOVE_USER,
        /** Add a user role. */
        ADD_USER_ROLE,
        /** Remove a user role. */
        REMOVE_USER_ROLE,
        /** Assign user roles. */
        ASSIGN_USER_ROLES,
        /** List users. */
        LIST_USERS,
        /** List user roles. */
        LIST_USER_ROLES,
        /** Change user password. */
        CHANGE_USER_PASSWORD,
        /** List the users for a given project. */
        LIST_PROJECT_USERS,
        /** Get the timestamp of the most recent activity for a project. */
        GET_MOST_RECENT_ACTIVITY,
        /** Shutdown the server. */
        SERVER_SHUTDOWN,
        /** Create a new project. */
        SERVER_CREATE_PROJECT,
        /** Delete a project. */
        SERVER_DELETE_PROJECT,
        /** Maintain a project. */
        SERVER_MAINTAIN_PROJECT,
        /** Get the roles known on the server. */
        SERVER_GET_ROLES,
        /** Get the role privileges. */
        SERVER_GET_ROLE_PRIVILEGES,
        /** Update the role privileges. */
        SERVER_UPDATE_ROLE_PRIVILEGES,
        /** Delete a role. */
        SERVER_DELETE_ROLE,
        /** Create a new role. */
        SERVER_CREATE_VIEW,
        /** Delete a view. */
        SERVER_DELETE_VIEW,
        /** Update the client jar. */
        UPDATE_CLIENT_JAR,
        /** Heartbeat message. */
        HEARTBEAT,
        /** A request error. */
        REQUEST_ERROR
    }

    /**
     * This enumeration defines the set of common data elements that <i>may</i> appear in a client request message. Their accessors and mutators are implemented in a base
     * class so each message class need not supply an implementation. A message class indicates that it uses the given data element by including its enumerated value in
     * the array of valid request elements returned by its implementation of the getValidElements() method.
     */
    public enum ValidRequestElementType {
        /** Server name. */
        SERVER_NAME,
        /** Project name. */
        PROJECT_NAME,
        /** View name. */
        VIEW_NAME,
        /** Appended path. */
        APPENDED_PATH,
        /** Short workfile name. */
        SHORT_WORKFILE_NAME,
        /** Transaction id. */
        TRANSACTION_ID,
        /** File id. */
        FILE_ID,
        /** Revision string. */
        REVISION_STRING,
        /** User name. */
        USER_NAME,
        /** Password. */
        PASSWORD,
        /** Role. */
        ROLE
    }
}
