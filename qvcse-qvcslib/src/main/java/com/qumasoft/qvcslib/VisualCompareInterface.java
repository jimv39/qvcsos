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
 * Visual compare interface.
 * @author Jim Voris
 */
public interface VisualCompareInterface {

    /**
     * Perform a visual compare of two files.
     * @param file1Name the first file name.
     * @param file2Name the second file name.
     * @param display1 the string to display for the first file.
     * @param display2 the string to display for the second file.
     */
    void visualCompare(final String file1Name, final String file2Name, final String display1, final String display2);
}