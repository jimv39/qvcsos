/*
 * Copyright 2021 Jim Voris.
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
package com.qumasoft.qvcslib.response;

import com.qumasoft.qvcslib.ArchiveDirManagerProxy;
import java.util.List;

/**
 *
 * @author Jim Voris
 */
public class ServerResponseGetUserCommitComments implements ServerResponseInterface {
    // These are serialized:
    private List<String> commitComments;

    @Override
    public void updateDirManagerProxy(ArchiveDirManagerProxy directoryManagerProxy) {
    }

    @Override
    public ResponseOperationType getOperationType() {
        return ResponseOperationType.SR_GET_USER_COMMIT_COMMENTS;
    }

    /**
     * @return the commitComments
     */
    public List<String> getCommitComments() {
        return commitComments;
    }

    /**
     * @param comments the commitComments to set
     */
    public void setCommitComments(List<String> comments) {
        this.commitComments = comments;
    }

}
