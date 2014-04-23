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
import com.qumasoft.server.filehistory.FileHistorySummary;
import com.qumasoft.server.filehistory.Revision;

/**
 *
 * @author Jim Voris
 */
public interface SourceControlBehaviorInterface {

    /**
     * Fetch a revision.
     *
     * @param summary the file history summary.
     * @param revisionId the revision id of the revision to fetch.
     * @param result this buffer will be filled in with the given revision.
     * @return true if we're successful; false if not.
     */
    boolean fetchRevision(FileHistorySummary summary, Integer revisionId, MutableByteArray result);

    /**
     * Store a revision to the FileHistory. If the revision is already present in the FileHistory, this will replace the existing revision with the new one. This might happen
     * when (for example) we need to replace a non-delta representation of a revision with one that uses a delta representation of that same revision.
     *
     * @param summary the file history summary.
     * @param context the context of this request.
     * @param revisionToAdd the revision to add, which contains the bytes of the revision that should be added.
     * @return the commit id of the added revision.
     */
    Integer storeRevision(FileHistorySummary summary, BehaviorContext context, Revision revisionToAdd);

    /**
     * Commit any pending FileHistory changes. For example, if an {@link #storeRevision} call has been made, it will not be actually 'committed' to the file's
     * FileHistory until commit is called passing the commitId that was returned from the storeRevision call.
     *
     * @param commitIdentifier identify any pending FileHistory change(s).
     * @return true if the commit succeeds; false if not.
     */
    boolean commit(CommitIdentifier commitIdentifier);
}
