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

/**
 * Ascend/descend sort key. A helper wrapper that lets us alter the ascending/descending behavior of a Comparable object without modifying that class.
 * @author Jim Voris
 */
class AscendDecendSortKey implements Comparable {

    private final boolean ascendingSortFlag;
    private final Comparable<Object> comparableObject;

    AscendDecendSortKey(Comparable<Object> sortObject, boolean ascending) {
        ascendingSortFlag = ascending;
        comparableObject = sortObject;
    }

    @Override
    public int compareTo(Object o) {
        AscendDecendSortKey compareObject = (AscendDecendSortKey) o;
        if (ascendingSortFlag) {
            return comparableObject.compareTo(compareObject.comparableObject);
        } else {
            return compareObject.comparableObject.compareTo(comparableObject);
        }
    }
}
