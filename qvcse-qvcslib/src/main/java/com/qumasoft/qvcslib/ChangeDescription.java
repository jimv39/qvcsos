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
 * Describes a change.
 *
 * @author Jim Voris
 */
public class ChangeDescription {

    /**
     * The number of lines deleted with this change.
     */
    private final int numberOfDeletedLines;
    /**
     * The number of lines inserted with this change
     */
    private final int numberOfInsertedLines;
    /**
     * The number of the first deleted line.
     */
    private final int startingLineOfFileA;
    /**
     * The line number of the first inserted line
     */
    private final int startingLineOfFileB;

    ChangeDescription(int fileALineNo, int fileBLineNo, int deleted, int inserted) {
        numberOfDeletedLines = deleted;
        numberOfInsertedLines = inserted;
        startingLineOfFileA = fileALineNo;
        startingLineOfFileB = fileBLineNo;
    }

    /**
     * Get the number of deleted lines.
     * @return the number of deleted lines.
     */
    public int getNumberOfDeletedLines() {
        return numberOfDeletedLines;
    }

    /**
     * Get the number of inserted lines.
     * @return the number of inserted lines.
     */
    public int getNumberOfInsertedLines() {
        return numberOfInsertedLines;
    }

    /**
     * Get the line number of file A where the change begins.
     * @return the line number of file A where the change begins.
     */
    public int getStartingLineOfFileA() {
        return startingLineOfFileA;
    }

    /**
     * Get the line number of file B where the change begins.
     * @return the line number of file B where the change begins.
     */
    public int getStartingLineOfFileB() {
        return startingLineOfFileB;
    }

    /**
     * Is this an insert type of change.
     * @return true if this is an insert type of change; false otherwise.
     */
    public boolean isInsert() {
        return (numberOfDeletedLines == 0) && (numberOfInsertedLines > 0);
    }

    /**
     * Is this a delete type of change.
     * @return true if this is a delete type of change; false otherwise.
     */
    public boolean isDelete() {
        return (numberOfDeletedLines > 0) && (numberOfInsertedLines == 0);
    }

    /**
     * Is this a replace type of change.
     * @return true if this is a replace type of change; false otherwise.
     */
    public boolean isReplace() {
        return (numberOfDeletedLines > 0) && (numberOfInsertedLines > 0);
    }
}
