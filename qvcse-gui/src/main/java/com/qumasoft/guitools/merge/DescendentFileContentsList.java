/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.guitools.merge;

import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.QumaAssert;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

/**
 * Descendent file contents list.
 * @author Jim Voris
 */
public class DescendentFileContentsList extends JList<MergedDescendentFileContentRow> {
    private static final long serialVersionUID = 6832787751335861590L;
    private static final int NUMBER_WIDTH = 70;
    private final ImageIcon emptyIcon;
    private final ImageIcon currentDiffMarkerIcon;
    private int maximumContentWidth = 0;
    private int rowHeight;
    private final int fileIndex;
    private final CellRenderer cellRenderer;
    private final MergeFrame parentFrame;

    DescendentFileContentsList(DescendentFileContentsListModel model, int fileIdx, MergeFrame pFrame, Font font) {
        super(model);
        this.currentDiffMarkerIcon = new ImageIcon(ClassLoader.getSystemResource("images/RedTriRight.png"));
        this.emptyIcon = new ImageIcon(ClassLoader.getSystemResource("images/ClearTriRight.png"));
        this.parentFrame = pFrame;
        this.fileIndex = fileIdx;
        this.cellRenderer = new CellRenderer(this);
        addMouseListener(new MouseClickListener(this));
        setFont(font);
    }

    @Override
    public ListCellRenderer getCellRenderer() {
        return cellRenderer;
    }

    int getRowHeight() {
        return rowHeight;
    }

    class CellRenderer extends JPanel implements ListCellRenderer {
        private static final long serialVersionUID = -6337963574924920715L;

        private MergedDescendentFileContentRow mergedDescendentContentRow;
        private final JLabel lineNumber;
        private final JList list;
        private final JCheckBox privateDoNothingCheckBox;

        CellRenderer(JList lst) {
            super();
            this.privateDoNothingCheckBox = new JCheckBox();
            this.lineNumber = new JLabel();
            list = lst;
            setLayout(new BorderLayout(0, 0));
            lineNumber.setForeground(ColorManager.getNormalColor());
            lineNumber.setHorizontalTextPosition(SwingConstants.RIGHT);
            lineNumber.setHorizontalAlignment(SwingConstants.RIGHT);
            // <editor-fold>
            rowHeight = list.getFont().getSize() + 5;
            // </editor-fold>
            lineNumber.setPreferredSize(new Dimension(NUMBER_WIDTH, rowHeight));
            lineNumber.setOpaque(true);
            privateDoNothingCheckBox.setEnabled(true);
            privateDoNothingCheckBox.setVisible(false);
            setOpaque(true);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JPanel lineNumberPanel = new JPanel();
            lineNumberPanel.setBorder(new EmptyBorder(0, 0, 0, 10));
            lineNumberPanel.setLayout(new BorderLayout(0, 0));
            JPanel checkBoxPanel = new JPanel();
            checkBoxPanel.setLayout(new GridLayout(1, 1, 0, 0));
            Color bkColor = null;
            Font font = list.getFont();
            boolean showOverlapFlag = false;
            boolean showDeletedFlag = false;
            removeAll();
            mergedDescendentContentRow = getModel().getElementAt(index);
            mergedDescendentContentRow.setRowDeletedFlag(false);
            mergedDescendentContentRow.setForeground(list.getForeground());
            mergedDescendentContentRow.setBackground(list.getBackground());
            EditInfo editInfo = null;
            int currentWidth;
            switch (fileIndex) {
                case 0: // Working on the merged result (a.k.a the ancestor file).
                    lineNumber.setText("");
                    if ((mergedDescendentContentRow.getFirstDecendentEditInfo() != null)
                            && (mergedDescendentContentRow.getFirstDecendentEditInfo().getCheckBox().isSelected())) {
                        if (mergedDescendentContentRow.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                            mergedDescendentContentRow.setText("");
                            bkColor = ColorManager.getFirstDecendentChangeBackgroundColor();
                        } else {
                            QumaAssert.isTrue(mergedDescendentContentRow.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT);
                            mergedDescendentContentRow.setText(mergedDescendentContentRow.getFirstDecendentText());
                            lineNumber.setText(Integer.toString(mergedDescendentContentRow.getAncestorLineNumber()));
                            bkColor = ColorManager.getFirstDecendentChangeBackgroundColor();
                        }
                    } else if ((mergedDescendentContentRow.getSecondDecendentEditInfo() != null)
                            && (mergedDescendentContentRow.getSecondDecendentEditInfo().getCheckBox().isSelected())) {
                        if (mergedDescendentContentRow.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                            mergedDescendentContentRow.setText("");
                            bkColor = ColorManager.getSecondDecendentChangeBackgroundColor();
                        } else {
                            QumaAssert.isTrue(mergedDescendentContentRow.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT);
                            mergedDescendentContentRow.setText(mergedDescendentContentRow.getSecondDecendentText());
                            lineNumber.setText(Integer.toString(mergedDescendentContentRow.getAncestorLineNumber()));
                            bkColor = ColorManager.getSecondDecendentChangeBackgroundColor();
                        }
                    } else {
                        mergedDescendentContentRow.setText(mergedDescendentContentRow.getAncestorText());
                        if (mergedDescendentContentRow.getAncestorLineNumber() != 0) {
                            lineNumber.setText(Integer.toString(mergedDescendentContentRow.getAncestorLineNumber()));
                        }
                        if ((mergedDescendentContentRow.getFirstDecendentEditInfo() != null) || (mergedDescendentContentRow.getSecondDecendentEditInfo() != null)) {
                            bkColor = ColorManager.getChangeBackgroundColor();
                        } else {
                            bkColor = list.getBackground();
                        }
                    }
                    break;
                case 1: // Working on the first decendent file...
                    if (mergedDescendentContentRow.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                        mergedDescendentContentRow.setForeground(ColorManager.getDeleteForegroundColor());
                        bkColor = ColorManager.getChangeBackgroundColor();
                        mergedDescendentContentRow.setRowDeletedFlag(true);
                        showDeletedFlag = true;
                    } else if (mergedDescendentContentRow.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_REPLACE) {
                        mergedDescendentContentRow.setForeground(ColorManager.getReplaceForegroundColor());
                        bkColor = ColorManager.getChangeBackgroundColor();
                    } else if (mergedDescendentContentRow.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT) {
                        mergedDescendentContentRow.setForeground(ColorManager.getInsertForegroundColor());
                        bkColor = ColorManager.getChangeBackgroundColor();
                    } else if (mergedDescendentContentRow.getFirstDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_UNDEFINED) {
                        mergedDescendentContentRow.setFirstDecendentText("Undefined");
                        mergedDescendentContentRow.setForeground(ColorManager.getInsertForegroundColor());
                        bkColor = ColorManager.getChangeBackgroundColor();
                    } else {
                        mergedDescendentContentRow.setForeground(ColorManager.getNormalColor());
                        bkColor = list.getBackground();
                    }
                    mergedDescendentContentRow.setText(mergedDescendentContentRow.getFirstDecendentText());
                    currentWidth = mergedDescendentContentRow.getFontMetrics(mergedDescendentContentRow.getFont()).stringWidth(mergedDescendentContentRow.getFirstDecendentText());
                    if (currentWidth > maximumContentWidth) {
                        maximumContentWidth = currentWidth;
                    }
                    if (mergedDescendentContentRow.getFirstDecendentLineNumber() == 0) {
                        lineNumber.setText("");
                        bkColor = ColorManager.getChangeBackgroundColor();
                    } else {
                        lineNumber.setText(Integer.toString(mergedDescendentContentRow.getFirstDecendentLineNumber()));
                    }
                    showOverlapFlag = mergedDescendentContentRow.getOverlapFlag();
                    editInfo = mergedDescendentContentRow.getFirstDecendentEditInfo();
                    break;
                case 2: // Working on the second decendent file...
                    if (mergedDescendentContentRow.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_DELETE) {
                        mergedDescendentContentRow.setForeground(ColorManager.getDeleteForegroundColor());
                        bkColor = ColorManager.getChangeBackgroundColor();
                        mergedDescendentContentRow.setRowDeletedFlag(true);
                        showDeletedFlag = true;
                    } else if (mergedDescendentContentRow.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_REPLACE) {
                        mergedDescendentContentRow.setForeground(ColorManager.getReplaceForegroundColor());
                        bkColor = ColorManager.getChangeBackgroundColor();
                    } else if (mergedDescendentContentRow.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_INSERT) {
                        mergedDescendentContentRow.setForeground(ColorManager.getInsertForegroundColor());
                        bkColor = ColorManager.getChangeBackgroundColor();
                    } else if (mergedDescendentContentRow.getSecondDecendentRowType() == MergedDescendentFileContentRow.ROWTYPE_UNDEFINED) {
                        mergedDescendentContentRow.setSecondDecendentText("Undefined");
                        mergedDescendentContentRow.setForeground(ColorManager.getInsertForegroundColor());
                        bkColor = ColorManager.getChangeBackgroundColor();
                    } else {
                        mergedDescendentContentRow.setForeground(ColorManager.getNormalColor());
                        bkColor = list.getBackground();
                    }
                    if (mergedDescendentContentRow.getSecondDecendentText() != null) {
                        mergedDescendentContentRow.setText(mergedDescendentContentRow.getSecondDecendentText());
                    } else {
                        mergedDescendentContentRow.setSecondDecendentText("second decendent text was null");
                        mergedDescendentContentRow.setText("second decendent text was null");
                    }
                    currentWidth = mergedDescendentContentRow.getFontMetrics(mergedDescendentContentRow.getFont()).stringWidth(mergedDescendentContentRow.getSecondDecendentText());
                    if (currentWidth > maximumContentWidth) {
                        maximumContentWidth = currentWidth;
                    }
                    if (mergedDescendentContentRow.getSecondDecendentLineNumber() == 0) {
                        lineNumber.setText("");
                        bkColor = ColorManager.getChangeBackgroundColor();
                    } else {
                        lineNumber.setText(Integer.toString(mergedDescendentContentRow.getSecondDecendentLineNumber()));
                    }
                    showOverlapFlag = mergedDescendentContentRow.getOverlapFlag();
                    editInfo = mergedDescendentContentRow.getSecondDecendentEditInfo();
                    break;
                default:
                    throw new QVCSRuntimeException("Internal error -- illegal file index");
            }
            if (index == parentFrame.getCurrentRowIndex()) {
                lineNumber.setIcon(currentDiffMarkerIcon);
            } else {
                lineNumber.setIcon(emptyIcon);
            }
            if (fileIndex > 0 && editInfo != null) {
                switch (fileIndex) {
                    case 1:
                        if (mergedDescendentContentRow.getFirstDecendentCheckBoxVisibleFlag()) {
                            editInfo.getCheckBox().setVisible(true);
                            editInfo.getCheckBox().setEnabled(true);
                            checkBoxPanel.add(editInfo.getCheckBox());
                        } else {
                            checkBoxPanel.add(privateDoNothingCheckBox);
                        }
                        break;
                    case 2:
                        if (mergedDescendentContentRow.getSecondDecendentCheckBoxVisibleFlag()) {
                            editInfo.getCheckBox().setVisible(true);
                            editInfo.getCheckBox().setEnabled(true);
                            checkBoxPanel.add(editInfo.getCheckBox());
                        } else {
                            checkBoxPanel.add(privateDoNothingCheckBox);
                        }
                        break;
                    default:
                        throw new QVCSRuntimeException("Internal error -- illegal file index");
                }
            } else {
                checkBoxPanel.add(privateDoNothingCheckBox);
            }
            lineNumberPanel.add(BorderLayout.WEST, checkBoxPanel);
            lineNumberPanel.add(BorderLayout.CENTER, lineNumber);
            add(BorderLayout.WEST, lineNumberPanel);
            add(BorderLayout.CENTER, mergedDescendentContentRow);
            if (isSelected) {
                mergedDescendentContentRow.setBackground(list.getSelectionBackground());
                mergedDescendentContentRow.setForeground(list.getSelectionForeground());
                mergedDescendentContentRow.setIsSelectedFlag(true);
                checkBoxPanel.setBackground(list.getSelectionBackground());
                checkBoxPanel.setForeground(list.getSelectionForeground());
                lineNumberPanel.setBackground(list.getSelectionBackground());
                lineNumberPanel.setForeground(list.getSelectionForeground());
                lineNumber.setBackground(list.getSelectionBackground());
                lineNumber.setForeground(list.getSelectionForeground());
            } else {
                mergedDescendentContentRow.setIsSelectedFlag(false);
                mergedDescendentContentRow.setBackground(bkColor);
                checkBoxPanel.setForeground(list.getForeground());
                lineNumberPanel.setForeground(list.getForeground());
                lineNumber.setForeground(list.getForeground());
                if (showOverlapFlag) {
                    checkBoxPanel.setBackground(ColorManager.getCollisionBackgroundColor());
                    lineNumberPanel.setBackground(ColorManager.getCollisionBackgroundColor());
                    lineNumber.setBackground(ColorManager.getCollisionBackgroundColor());
                } else if (showDeletedFlag) {
                    checkBoxPanel.setBackground(ColorManager.getDeletedRowLineNumberBackgroundColor());
                    lineNumberPanel.setBackground(ColorManager.getDeletedRowLineNumberBackgroundColor());
                    lineNumber.setBackground(ColorManager.getDeletedRowLineNumberBackgroundColor());
                } else {
                    checkBoxPanel.setBackground(ColorManager.getChangeBackgroundColor());
                    lineNumberPanel.setBackground(ColorManager.getChangeBackgroundColor());
                    lineNumber.setBackground(ColorManager.getChangeBackgroundColor());
                }
            }
            mergedDescendentContentRow.setEnabled(list.isEnabled());
            mergedDescendentContentRow.setFont(font);
            mergedDescendentContentRow.setOpaque(true);
            lineNumber.setFont(list.getFont());
            return this;
        }

        @Override
        public int getWidth() {
            int width = lineNumber.getWidth() + mergedDescendentContentRow.getWidth();
            return width;
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension preferredSize = new Dimension(NUMBER_WIDTH + maximumContentWidth, rowHeight);
            return preferredSize;
        }
    }

    class MouseClickListener extends MouseAdapter {
        private final JList list;

        MouseClickListener(JList l) {
            list = l;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // We need to check for overlap and prevent two checkboxes from
            // being turned on in the case of overlap.
            int index = list.locationToIndex(e.getPoint());
            MergedDescendentFileContentRow row = getModel().getElementAt(index);
            switch (fileIndex) {
                case 1:
                    if (row.getFirstDecendentEditInfo() != null) {
                        EditInfo editInfo = row.getFirstDecendentEditInfo();
                        if (editInfo.getCheckBox().isSelected()) {
                            editInfo.getCheckBox().setSelected(false);
                        } else {
                            if (isOverLapDetected(row) == false) {
                                editInfo.getCheckBox().setSelected(true);
                            } else {
                                JOptionPane.showMessageDialog(parentFrame, "In an overlap region, you can choose an edit from only one file!", "Collision Area!", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        parentFrame.reNumberMergedResult();
                        parentFrame.repaint();
                    }
                    break;
                case 2:
                    if (row.getSecondDecendentEditInfo() != null) {
                        EditInfo editInfo = row.getSecondDecendentEditInfo();
                        if (editInfo.getCheckBox().isSelected()) {
                            editInfo.getCheckBox().setSelected(false);
                        } else {
                            if (isOverLapDetected(row) == false) {
                                editInfo.getCheckBox().setSelected(true);
                            } else {
                                JOptionPane.showMessageDialog(parentFrame, "In an overlap region, you can choose an edit from only one file!", "Collision Area!", JOptionPane.INFORMATION_MESSAGE);
                            }
                        }
                        parentFrame.reNumberMergedResult();
                        parentFrame.repaint();
                    }
                    break;
                default:
                    throw new QVCSRuntimeException("Internal error -- illegal file index");
            }
        }

        /**
         * Return true if there is overlap and the other panel's checkbox is already enabled.
         *
         * @param row
         * @return true if the checkbox for a colliding edit is selected.
         */
        private boolean isOverLapDetected(MergedDescendentFileContentRow row) {
            boolean retVal = false;
            switch (fileIndex) {
                case 1:
                    // They clicked the checkbox on file1. Only allow the check
                    // to be enabled if there is no overlap.
                    if (row.getSecondDecendentEditInfo() != null) {
                        if (row.getSecondDecendentEditInfo().getCheckBox().isSelected()) {
                            retVal = true;
                        }
                    }
                    break;
                case 2:
                    // They clicked the checkbox on the second decendent. Only
                    // allow the checkbox be enabled if there is no overlap.
                    if (row.getFirstDecendentEditInfo() != null) {
                        if (row.getFirstDecendentEditInfo().getCheckBox().isSelected()) {
                            retVal = true;
                        }
                    }
                    break;
                default:
                    throw new QVCSRuntimeException("Internal error -- illegal file index");
            }
            return retVal;
        }
    }

}
