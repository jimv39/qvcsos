/*
 * Copyright 2014 JimVoris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.server.filehistory.behavior;

import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.server.filehistory.BehaviorContext;
import com.qumasoft.server.filehistory.CommitIdentifier;
import com.qumasoft.server.filehistory.Revision;

/**
 *
 * @author Jim Voris
 */
public interface SourceControlBehaviorInterface {

    /**
     * Get a revision.
     *
     * @param revisionId the revision id of the revision to fetch.
     * @param result this buffer will be filled in with the given revision.
     * @return true if we're successful; false if not.
     */
    boolean getRevision(Integer revisionId, MutableByteArray result);

    /**
     * Add a revision to the FileHistory. This will always figure out the reverse delta revision, and update it so that its data represents the reverse delta script needed
     * to hydrate that revision. The revisionToAdd will <i>always</i> be a tip revision, and its revision data will always be the actual file content (though it may be compressed).
     *
     * @param context the context of this request.
     * @param revisionToAdd the revision to add, which contains the bytes of the revision that should be added.
     * @param computeDeltaFlag true if we should compute a delta; false if we should not compute a delta.
     * @return the commit id of the added revision.
     */
    Integer addRevision(BehaviorContext context, Revision revisionToAdd, boolean computeDeltaFlag);

    /**
     * Commit any pending FileHistory changes. For example, if an {@link #addRevision} call has been made, it will not be actually 'committed' to the file's
     * FileHistory until commit is called passing the commitId that was returned from the addRevision call.
     *
     * @param commitIdentifier identify any pending FileHistory change(s).
     * @return true if the commit succeeds; false if not.
     */
    boolean commit(CommitIdentifier commitIdentifier);
}
