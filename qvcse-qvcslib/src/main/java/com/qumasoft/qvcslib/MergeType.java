/*   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.qvcslib;

/**
 * Types of merges that we can support.
 *
 * @author Jim Voris
 */
public enum MergeType {

    /** Simple merge. */
    SIMPLE_MERGE_TYPE, // 0000 -- no renames or moves.
    /** Renamed merge. */
    PARENT_RENAMED_MERGE_TYPE, // 0001 -- parent rename
    /** Parent moved merge. */
    PARENT_MOVED_MERGE_TYPE, // 0010 -- parent move
    /** Parent renamed and parent moved merge. */
    PARENT_RENAMED_AND_PARENT_MOVED_MERGE_TYPE, // 0011 -- parent rename && parent move
    /** Child renamed merge. */
    CHILD_RENAMED_MERGE_TYPE, // 0100 -- branch rename
    /** Parent rename and child renamed merge. */
    PARENT_RENAMED_AND_CHILD_RENAMED_MERGE_TYPE, // 0101 -- branch rename && parent rename
    /** Parent moved and child renamed merge. */
    PARENT_MOVED_AND_CHILD_RENAMED_MERGE_TYPE, // 0110 -- parent move && branch rename
    /** Parent renamed and parent moved and child renamed merge. */
    PARENT_RENAMED_AND_PARENT_MOVED_AND_CHILD_RENAMED_MERGE_TYPE, // 0111 -- parent rename && parent move && branch rename
    /** Child moved merge. */
    CHILD_MOVED_MERGE_TYPE, // 1000 -- branch move
    /** Parent renamed and child moved merge. */
    PARENT_RENAMED_AND_CHILD_MOVED_MERGE_TYPE, // 1001 -- branch move && parent rename
    /** Parent moved and child moved merge. */
    PARENT_MOVED_AND_CHILD_MOVED_MERGE_TYPE, // 1010 -- branch move && parent move
    /** Parent moved and parent renamed and child moved merge. */
    PARENT_MOVED_AND_PARENT_RENAMED_AND_CHILD_MOVED_MERGE_TYPE, // 1011 -- branch move && parent move && parent rename
    /** Child renamed and child moved merge. */
    CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE, // 1100 -- branch move && branch rename
    /** Parent renamed and childd renamed and child moved merge. */
    PARENT_RENAMED_AND_CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE, // 1101 -- branch move && branch rename && parent rename
    /** Parent moved and child renamed and child moved merge. */
    PARENT_MOVED_AND_CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE, // 1110 -- branch move && branch rename && parent move
    /** Parent renamed and parent moved and child renamed and child moved merge. */
    PARENT_RENAMED_AND_PARENT_MOVED_AND_CHILD_RENAMED_AND_CHILD_MOVED_MERGE_TYPE, // 1111 -- branch move && branch rename && parent move && parent rename
    /** Parent deleted merge. */
    PARENT_DELETED_MERGE_TYPE,
    /** Child deleted merge. */
    CHILD_DELETED_MERGE_TYPE,
    /** Child created merge. */
    CHILD_CREATED_MERGE_TYPE,
    /** Unknown merge. */
    UNKNOWN_MERGE_TYPE
}
