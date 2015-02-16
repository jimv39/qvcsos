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
package com.qumasoft.guitools.merge;

/**
 * Descendent file content row. Instances of this class are immutable.
 * @author Jim Voris
 */
public class DescendentFileContentRow {

    private final MergedDescendentFileContentRow mergedRow;
    private byte ancestorRowType;
    private int ancestorLineNumber;
    private String ancestorText;
    private byte descendentRowType;
    private int descendentLineNumber;
    private String descendentText;
    private EditInfo descendentEditInfo = null;
    private boolean descendentFirstRowOfEditFlag = false;
    private boolean rowDeletedFlag = false;

    DescendentFileContentRow() {
        this.mergedRow = null;
        this.ancestorRowType = MergedDescendentFileContentRow.ROWTYPE_UNDEFINED;
        this.descendentRowType = MergedDescendentFileContentRow.ROWTYPE_UNDEFINED;
    }

    DescendentFileContentRow(MergedDescendentFileContentRow mr) {
        this.descendentRowType = mr.getAncestorRowType();
        this.descendentText = mr.getAncestorText();
        this.mergedRow = mr;
    }

    MergedDescendentFileContentRow getMergedRow() {
        return mergedRow;
    }

    boolean getDescendentFirstRowOfEditFlag() {
        return descendentFirstRowOfEditFlag;
    }

    void setDescendentFirstRowOfEditFlag(boolean flag) {
        descendentFirstRowOfEditFlag = flag;
    }

    EditInfo getDescendentEditInfo() {
        return descendentEditInfo;
    }

    void setDescendentEditInfo(EditInfo editInfo) {
        this.descendentEditInfo = editInfo;
    }

    byte getAncestorRowType() {
        return ancestorRowType;
    }

    void setAncestorRowType(byte rowType) {
        ancestorRowType = rowType;
    }

    String getAncestorText() {
        return ancestorText;
    }

    void setAncestorText(final String text) {
        ancestorText = text;
    }

    int getAncestorLineNumber() {
        return ancestorLineNumber;
    }

    void setAncestorLineNumber(int lineNumber) {
        ancestorLineNumber = lineNumber;
    }

    long getAncestorSeekPosition() {
        if (mergedRow != null) {
            return mergedRow.getAncestorSeekPosition();
        } else {
            return -1;
        }
    }

    byte getDescendentRowType() {
        return descendentRowType;
    }

    void setDescendentRowType(byte rowType) {
        descendentRowType = rowType;
    }

    int getDescendentLineNumber() {
        return descendentLineNumber;
    }

    void setDescendentLineNumber(int lineNumber) {
        descendentLineNumber = lineNumber;
    }

    String getDescendentText() {
        return descendentText;
    }

    void setDescendentText(String text) {
        descendentText = text;
    }

    void setRowDeletedFlag(boolean flag) {
        rowDeletedFlag = flag;
    }

    boolean getRowDeletedFlag() {
        return rowDeletedFlag;
    }
}
