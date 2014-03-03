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

import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.QumaAssert;
import com.qumasoft.qvcslib.Utility;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Descendent file contents list model.
 *
 * @author Jim Voris
 */
public class DescendentFileContentsListModel extends javax.swing.DefaultListModel<MergedDescendentFileContentRow> {

    private static final long serialVersionUID = 4159916548853677380L;
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.guitools.merge");
    private int currentDifferenceIndex = -1;
    private int maximumLeftEditIndex = -1;
    private int minimumLeftEditIndex = Integer.MAX_VALUE;
    private int maximumRightEditIndex = -1;
    private int minimumRightEditIndex = Integer.MAX_VALUE;

    DescendentFileContentsListModel(LinkedList<MergedDescendentFileContentRow> baseFileLinkedList, int fileIndex) {
        super();
        int index = 0;
        try {
            ListIterator<MergedDescendentFileContentRow> listIterator = baseFileLinkedList.listIterator();
            while (listIterator.hasNext()) {
                MergedDescendentFileContentRow row = listIterator.next();
                addElement(row);
                if (fileIndex == 0) {
                    deduceMinMaxEditIndexes(index, row);
                }
                index++;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, Utility.expandStackTraceToString(e));
        }
    }

    void applyNonCollidingEdits() {
        Enumeration<MergedDescendentFileContentRow> enumeration = elements();
        while (enumeration.hasMoreElements()) {
            MergedDescendentFileContentRow row = enumeration.nextElement();
            if (row.getOverlapFlag()) {
                continue;
            }

            if (row.getFirstDecendentEditInfo() != null
                    && row.getFirstDecendentCheckBoxVisibleFlag()) {
                row.getFirstDecendentEditInfo().getCheckBox().setSelected(true);
            } else if (row.getSecondDecendentEditInfo() != null
                    && row.getSecondDecendentCheckBoxVisibleFlag()) {
                row.getSecondDecendentEditInfo().getCheckBox().setSelected(true);
            } else {
                continue;
            }
        }
        reNumberMergedResult();
    }

    /**
     * Use this method to renumber the line numbers of the merged result a.k.a. ancestor. When the user alters which edits apply, we need to renumber the line numbers of the merged
     * result file.
     */
    void reNumberMergedResult() {
        Enumeration<MergedDescendentFileContentRow> enumeration = elements();
        int rowNumber = 1;
        while (enumeration.hasMoreElements()) {
            MergedDescendentFileContentRow row = enumeration.nextElement();

            // Set it blank (that's what a 0 means) by default.
            row.setAncestorLineNumber(0);

            // Working on the merged result (a.k.a the ancestor file).
            if (row.getFirstDecendentEditInfo() != null
                    && row.getFirstDecendentEditInfo().getCheckBox().isSelected()) {
                if (row.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                    continue;
                } else {
                    assert (row.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT);
                    row.setAncestorLineNumber(rowNumber++);
                }
            } else if (row.getSecondDecendentEditInfo() != null
                    && row.getSecondDecendentEditInfo().getCheckBox().isSelected()) {
                if (row.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                    continue;
                } else {
                    assert (row.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT);
                    row.setAncestorLineNumber(rowNumber++);
                }
            } else {
                if (row.getFirstDecendentEditInfo() == null && row.getSecondDecendentEditInfo() == null) {
                    row.setAncestorLineNumber(rowNumber++);
                } else if (row.getFirstDecendentEditInfo() == null && row.getSecondDecendentEditInfo() != null) {
                    if (row.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                        row.setAncestorLineNumber(rowNumber++);
                    } else {
                        continue;
                    }
                } else if (row.getFirstDecendentEditInfo() != null && row.getSecondDecendentEditInfo() == null) {
                    if (row.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                        row.setAncestorLineNumber(rowNumber++);
                    } else {
                        continue;
                    }
                } else if (row.getFirstDecendentEditInfo() != null && row.getSecondDecendentEditInfo() != null) {
                    if ((row.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE)
                            && (row.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE)) {
                        row.setAncestorLineNumber(rowNumber++);
                    } else {
                        continue;
                    }
                } else {
                    continue;
                }
            }
        }
    }

    void setCurrentDifferenceIndex(int index) {
        QumaAssert.isTrue(index <= size());
        currentDifferenceIndex = index;
        fireContentsChanged(this, index, index);
    }

    int getCurrentDifferenceIndex() {
        return currentDifferenceIndex;
    }

    void writeChanges(PrintWriter printWriter, String eolSequence) {
        Enumeration<MergedDescendentFileContentRow> enumeration = elements();
        while (enumeration.hasMoreElements()) {
            MergedDescendentFileContentRow row = enumeration.nextElement();

            if (row.getAncestorLineNumber() == 0) {
                continue;
            } else {
                printWriter.printf("%s%s", deduceMergedText(row), eolSequence);
            }
        }
    }

    private String deduceMergedText(MergedDescendentFileContentRow row) {
        // Working on the merged result (a.k.a the ancestor file).
        String mergedText = null;
        assert (row.getAncestorLineNumber() != 0);
        if (row.getFirstDecendentEditInfo() != null
                && row.getFirstDecendentEditInfo().getCheckBox().isSelected()) {
            if (row.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                throw new QVCSRuntimeException("Invalid row in deduceMergedText");
            } else {
                assert (row.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT);
                mergedText = row.getFirstDecendentText();
            }
        } else if (row.getSecondDecendentEditInfo() != null
                && row.getSecondDecendentEditInfo().getCheckBox().isSelected()) {
            if (row.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                throw new QVCSRuntimeException("Invalid row in deduceMergedText");
            } else {
                assert (row.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT);
                mergedText = row.getSecondDecendentText();
            }
        } else {
            if (row.getAncestorLineNumber() != 0) {
                mergedText = row.getAncestorText();
            } else {
                throw new QVCSRuntimeException("Invalid row in deduceMergedText");
            }
        }
        return mergedText;
    }

    private void deduceMinMaxEditIndexes(int index, MergedDescendentFileContentRow row) {
        if (minimumLeftEditIndex == Integer.MAX_VALUE) {
            if (row.getFirstDecendentCheckBoxVisibleFlag()) {
                minimumLeftEditIndex = index;
            }
        }
        if (minimumRightEditIndex == Integer.MAX_VALUE) {
            if (row.getSecondDecendentCheckBoxVisibleFlag()) {
                minimumRightEditIndex = index;
            }
        }
        if (index > maximumLeftEditIndex) {
            if (row.getFirstDecendentCheckBoxVisibleFlag()) {
                maximumLeftEditIndex = index;
            }
        }
        if (index > maximumRightEditIndex) {
            if (row.getSecondDecendentCheckBoxVisibleFlag()) {
                maximumRightEditIndex = index;
            }
        }
    }

    int getMinimumLeftEditIndex() {
        return minimumLeftEditIndex;
    }

    int getMaximumLeftEditIndex() {
        return maximumLeftEditIndex;
    }

    int getMinimumRightEditIndex() {
        return minimumRightEditIndex;
    }

    int getMaximumRightEditIndex() {
        return maximumRightEditIndex;
    }
}
