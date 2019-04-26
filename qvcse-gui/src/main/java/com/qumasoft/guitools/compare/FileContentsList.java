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
package com.qumasoft.guitools.compare;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

class FileContentsList extends JList {
    private static final long serialVersionUID = -4251349514486534916L;

    private static final int NUMBER_WIDTH = 80;
    private static final int DEFAULT_FONT_SIZE = 12;
    private static final Color NORMAL_COLOR = new Color(0, 0, 0);
    private static final Color INSERT_COLOR = new Color(0, 125, 110);
    private static final Color DELETE_COLOR = new Color(255, 10, 0);
    private static final Color REPLACE_COLOR = new Color(0, 10, 255);
    private static final Color CHANGE_BACKGROUND_COLOR = new Color(210, 210, 210);
    private static FileContentsList lastClick = null;
    private final FileContentsListModel fileContentsListModel;
    private final ImageIcon emptyIcon = new ImageIcon(ClassLoader.getSystemResource("images/ClearTriRight.png"));
    private final ImageIcon currentDiffMarkerIcon = new ImageIcon(ClassLoader.getSystemResource("images/RedTriRight.png"));
    private final MouseClickListener mouseClickListener = new MouseClickListener();
    private int maximumContentWidth = 0;
    private int rowHeight;
    private final CellRenderer cellRenderer;
    private final Font contentFont;

    FileContentsList(FileContentsListModel model, CompareFrame parentFrame) {
        super(model);
        this.contentFont = new Font("monospaced", Font.PLAIN, DEFAULT_FONT_SIZE);
        fileContentsListModel = model;
        cellRenderer = new CellRenderer(this);
        addMouseListener(mouseClickListener);
        if (lastClick == null) {
            lastClick = this;
        }
        setFont(new Font("monospaced", Font.PLAIN, DEFAULT_FONT_SIZE));

        // Trap CTRL-C so we can use that to copy to clipboard.
        javax.swing.KeyStroke keyCopyToClipBoard = javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_DOWN_MASK);
        getInputMap(WHEN_FOCUSED).put(keyCopyToClipBoard, "copyToClipboardKeyAction");
        getActionMap().put("copyToClipboardKeyAction", parentFrame.getEditCopyAction());
    }

    @Override
    public ListCellRenderer getCellRenderer() {
        return cellRenderer;
    }

    static Color getNormalColor() {
        return NORMAL_COLOR;
    }

    static Color getInsertColor() {
        return INSERT_COLOR;
    }

    static Color getDeleteColor() {
        return DELETE_COLOR;
    }

    static Color getReplaceColor() {
        return REPLACE_COLOR;
    }

    static FileContentsList getLastFocus() {
        return lastClick;
    }

    int getRowHeight() {
        return rowHeight;
    }

    class CellRenderer extends JPanel implements ListCellRenderer {
        private static final long serialVersionUID = 3311420399900950454L;

        private ContentRow contentRow;
        private final JLabel lineNumber;
        private final JList jList;
        private static final int CELL_PADDING = 5;

        CellRenderer(JList list) {
            super();
            this.lineNumber = new JLabel();
            jList = list;
            setLayout(new BorderLayout(0, 0));
            lineNumber.setForeground(NORMAL_COLOR);
            lineNumber.setHorizontalTextPosition(SwingConstants.RIGHT);
            lineNumber.setHorizontalAlignment(SwingConstants.RIGHT);
            rowHeight = jList.getFont().getSize() + CELL_PADDING;
            lineNumber.setPreferredSize(new Dimension(NUMBER_WIDTH, rowHeight));
            lineNumber.setOpaque(true);
            setOpaque(true);
        }

        @Override
        public java.awt.Component getListCellRendererComponent(JList list,
                                                               Object value,
                                                               int index,
                                                               boolean isSelected,
                                                               boolean cellHasFocus) {
            Color bkColor;

            removeAll();
            contentRow = (ContentRow) getModel().getElementAt(index);
            if (contentRow.getRowType() != ContentRow.ROWTYPE_BLANK) {
                ContentRow row = contentRow;
                lineNumber.setText(row.getLineNumber());
                if (row.getRowType() == ContentRow.ROWTYPE_DELETE) {
                    contentRow.setForeground(DELETE_COLOR);
                    bkColor = CHANGE_BACKGROUND_COLOR;
                } else if (row.getRowType() == ContentRow.ROWTYPE_REPLACE) {
                    contentRow.setForeground(REPLACE_COLOR);
                    bkColor = CHANGE_BACKGROUND_COLOR;
                } else if (row.getRowType() == ContentRow.ROWTYPE_INSERT) {
                    contentRow.setForeground(INSERT_COLOR);
                    bkColor = CHANGE_BACKGROUND_COLOR;
                } else {
                    contentRow.setForeground(NORMAL_COLOR);
                    bkColor = list.getBackground();
                }
                int currentWidth = contentRow.getFontMetrics(contentRow.getFont()).stringWidth(contentRow.getText());
                if (currentWidth > maximumContentWidth) {
                    maximumContentWidth = currentWidth;
                }
            } else {
                contentRow.setForeground(NORMAL_COLOR);
                contentRow.setText(" ");
                lineNumber.setText(" ");
                bkColor = CHANGE_BACKGROUND_COLOR;
            }

            if (index == fileContentsListModel.getCurrentDifferenceIndex()) {
                lineNumber.setIcon(currentDiffMarkerIcon);
            } else {
                lineNumber.setIcon(emptyIcon);
            }

            add(BorderLayout.WEST, lineNumber);
            add(BorderLayout.CENTER, contentRow);

            if (isSelected) {
                contentRow.setBackground(list.getSelectionBackground());
                contentRow.setForeground(list.getSelectionForeground());
                lineNumber.setBackground(list.getSelectionBackground());
                lineNumber.setForeground(list.getSelectionForeground());
            } else {
                contentRow.setBackground(bkColor);
                lineNumber.setBackground(CHANGE_BACKGROUND_COLOR);
                lineNumber.setForeground(list.getForeground());
            }

            contentRow.setEnabled(list.isEnabled());
            contentRow.setFont(contentFont);
            contentRow.setOpaque(true);
            lineNumber.setFont(list.getFont());
            return this;
        }

        @Override
        public int getWidth() {
            int width = lineNumber.getWidth() + contentRow.getWidth();
            return width;
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(NUMBER_WIDTH + maximumContentWidth, rowHeight);
        }
    }

    class MouseClickListener extends MouseAdapter {

        @Override
        public void mouseClicked(MouseEvent e) {
            lastClick = FileContentsList.this;
        }
    }

}
