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

import java.io.Serializable;

/**
 * Add Revision Info class. Use objects of this class to add revision information to a RevisionInformation object.
 *
 * @author Jim Voris
 */
public class AddRevisionData implements Serializable {
    private static final long serialVersionUID = -931215752420012421L;

    // These are the data that gets serialized.
    private RevisionHeader newRevision = null;
    private RevisionHeader parentRevision = null;
    private int newRevisionIndex = -1;
    private int parentRevisionIndex = -1;

    /**
     * Creates a new instance of AddRevisionData.
     */
    public AddRevisionData() {
    }

    /**
     * Get the new revision header.
     * @return the new revision header.
     */
    public RevisionHeader getNewRevisionHeader() {
        return newRevision;
    }

    /**
     * Set the new revision header.
     * @param newRevHeader the new revision header.
     */
    public void setNewRevisionHeader(RevisionHeader newRevHeader) {
        newRevision = newRevHeader;
    }

    /**
     * Get the parent revision header.
     * @return the parent revision header.
     */
    public RevisionHeader getParentRevisionHeader() {
        return parentRevision;
    }

    /**
     * Set the parent revision header.
     * @param parentRevHeader the parent revision header.
     */
    public void setParentRevisionHeader(RevisionHeader parentRevHeader) {
        parentRevision = parentRevHeader;
    }

    /**
     * Get the new revision index.
     * @return the new revision index.
     */
    public int getNewRevisionIndex() {
        return newRevisionIndex;
    }

    /**
     * Set the new revision index.
     * @param index the new revision index.
     */
    public void setNewRevisionIndex(int index) {
        newRevisionIndex = index;
    }

    /**
     * Get the parent revision index.
     * @return the parent revision index.
     */
    public int getParentRevisionIndex() {
        return parentRevisionIndex;
    }

    /**
     * Set the parent revision index.
     * @param index the parent revision index.
     */
    public void setParentRevisionIndex(int index) {
        parentRevisionIndex = index;
    }

}
