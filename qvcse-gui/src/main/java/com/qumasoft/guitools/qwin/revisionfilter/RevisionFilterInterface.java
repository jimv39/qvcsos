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
package com.qumasoft.guitools.qwin.revisionfilter;

/**
 * Revision filter interface. Define those methods that a revision filter must implement.
 * @author Jim Voris
 */
public interface RevisionFilterInterface {

    /**
     * Test the filter to see if the revision information passes the filter.
     * @param filteredRevisionInfo the revision information to check.
     * @return true if the filter passes; false if the filter does not pass.
     */
    boolean passesFilter(FilteredRevisionInfo filteredRevisionInfo);

    /**
     * Get the filter type.
     * @return the filter type.
     */
    String getFilterType();

    /**
     * Get the filter data. This is the string used to define the filter.
     * @return the filter data. This is the string used to define the filter.
     */
    String getFilterData();

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
}
