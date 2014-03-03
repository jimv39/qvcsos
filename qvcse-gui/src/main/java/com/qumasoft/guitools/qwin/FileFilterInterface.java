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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.TreeMap;

/**
 * File filter interface. File filters must supply these methods.
 * @author Jim Voris
 */
public interface FileFilterInterface extends java.io.Serializable {

    /**
     * Does the given file pass the filter.
     * @param mergedInfo the file to examine.
     * @param revisionHeaderMap a map of revision headers (for convenience).
     * @return true if the file passes the filter (should be included); false if not.
     */
    boolean passesFilter(MergedInfoInterface mergedInfo, TreeMap<Integer, RevisionHeader> revisionHeaderMap);

    /**
     * Get the type of filter.
     * @return the type of filter.
     */
    String getFilterType();

    /**
     * Get the data used to define the filter.
     * @return the data used to define the filter.
     */
    String getFilterData();

    /**
     * Get the raw filter data.
     * @return the raw filter data.
     */
    String getRawFilterData();

    /**
     * Is this an 'AND' filter.
     * @return true if this is an 'AND' filter.
     */
    boolean getIsANDFilter();

    /**
     * Is this an 'OR' filter.
     * @return true if this is an 'OR' filter.
     */
    boolean getIsORFilter();

    /**
     * Does this filter need revision detail information in order to work.
     * @return true if this filter needs revision detail information; false otherwise.
     */
    boolean requiresRevisionDetailInfo();
}
