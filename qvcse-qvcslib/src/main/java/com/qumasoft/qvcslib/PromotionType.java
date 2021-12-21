/*   Copyright 2004-2021 Jim Voris
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
 * Types of promotions that we can support.
 *
 * @author Jim Voris
 */
public enum PromotionType {

    /** Simple merge. */
    SIMPLE_PROMOTION_TYPE,                              // 000 -- no rename or move, create or delete.
    /** The files have different names. */
    FILE_NAME_CHANGE_PROMOTION_TYPE,                    // 001 -- files have different names.
    /** The files are in different locations. */
    FILE_LOCATION_CHANGE_PROMOTION_TYPE,                // 010 -- files have different locations.
    /** The files have different names, and are in different locations. */
    LOCATION_AND_NAME_DIFFER_PROMOTION_TYPE,            // 011 -- location and name differ.
    /** The file was created on the branch. */
    FILE_CREATED_PROMOTION_TYPE,                        // file created on branch.
    /** An existing file was deleted on the branch. */
    FILE_DELETED_PROMOTION_TYPE,                        // an existing file was deleted on branch.
    /** Unknown merge. */
    UNKNOWN_PROMOTION_TYPE
}
