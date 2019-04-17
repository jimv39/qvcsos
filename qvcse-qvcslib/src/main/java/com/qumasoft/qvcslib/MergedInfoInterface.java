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
 * Interface for merged workfile/archive information.
 *
 * @author Jim Voris
 */
public interface MergedInfoInterface extends ArchiveInfoInterface, WorkfileInfoInterface {
    // Define the indexes used for the workfile status values.
    /** Current status index. */
    int CURRENT_STATUS_INDEX = 0;
    /** Stale status index. */
    int STALE_STATUS_INDEX = 1;
    /** Your copy changed status index. */
    int YOUR_COPY_CHANGED_STATUS_INDEX = 2;
    /** Merge required status index. */
    int MERGE_REQUIRED_STATUS_INDEX = 3;
    /** File is different status index. */
    int DIFFERENT_STATUS_INDEX = 4;
    /** File is missing status index. */
    int MISSING_STATUS_INDEX = 5;
    /** File is not version controlled status index. */
    int NOT_CONTROLLED_STATUS_INDEX = 6;
    /** Invalid status index. */
    int INVALID_STATUS_INDEX = 7;

    /**
     * Set the workfile info.
     * @param workInfo the workfile info.
     */
    void setWorkfileInfo(WorkfileInfoInterface workInfo);

    /**
     * Get the workfile info.
     * @return the workfile info.
     */
    WorkfileInfoInterface getWorkfileInfo();

    /**
     * Get the status string.
     * @return the status string.
     */
    String getStatusString();

    /**
     * Get the status value. This returns a string used to define the sort order that we'll use when the user has chosen to sort by the status column. Those files that need
     * attention will sort to the top.
     * @return the status value.
     */
    String getStatusValue();

    /**
     * Get the integer value that represents the current workfile status. This will be a value from 0 - 7, described by the STATUS_INDEX constants defined on this interface.
     * @return the status index that describes the workfile status for this file.
     */
    int getStatusIndex();

    /**
     * The map key used for this file. This will be based on the short workfile name for the file, and uniquely identifies this file within its containing directory.
     * @return The map key used for this file.
     */
    String getMergedInfoKey();

    /**
     * Get the information needed for a merge. This is a synchronous round-trip to the server.
     * @param project the project name.
     * @param view the view name.
     * @param path the appended path.
     * @return an InfoForMerge object containing the information needed for a merge operation.
     */
    InfoForMerge getInfoForMerge(String project, String view, String path);

    /**
     * Resolve a conflict from the parent branch. This is a synchronous round-trip to the server.
     * @param project the project name.
     * @param branch the view name.
     * @return an ResolveConflictResults object.
     */
    ResolveConflictResults resolveConflictFromParentBranch(String project, String branch);

    /**
     * Promote a file from a branch to its parent branch.
     * @param project the project name.
     * @param branch the branch name.
     * @param parentBranch the parent branch name.
     * @param filePromoInfo file promotion info.
     * @param id the file's fileId.
     * @return the results of the promotion.
     */
    PromoteFileResults promoteFile(String project, String branch, String parentBranch, FilePromotionInfo filePromoInfo, int id);

    /**
     * Is this file remote.
     * @return should always return true.
     * @deprecated this is a hold over from long ago when we first ported this stuff from C++, and things could be file based instead of client/server.
     */
    boolean getIsRemote();

    /**
     * Get the project properties.
     * @return the project properties.
     */
    AbstractProjectProperties getProjectProperties();

    /**
     * Get the user name.
     * @return the user name.
     */
    String getUserName();

    /**
     * Get the archive directory manager. This is the container of this file.
     * @return the archive directory manager.
     */
    ArchiveDirManagerInterface getArchiveDirManager();
}
