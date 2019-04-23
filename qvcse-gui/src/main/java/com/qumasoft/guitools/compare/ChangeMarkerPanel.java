//   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.guitools.compare;

import com.qumasoft.guitools.qwin.QWinFrame;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.event.MouseInputAdapter;

/**
 * Panel that shows where the changes are in a given file.
 *
 * @author Jim Voris
 */
public class ChangeMarkerPanel extends JPanel {
    private static final long serialVersionUID = 8325950361773231444L;

    /** Panel width in pixels. */
    private static final int PANEL_WIDTH = 10;
    private static final int PANEL_HEIGHT = 10;
    private Rectangle pastSize;
    private final int totalRowCount;
    private final int rowHeight;
    private final ArrayList<ColoredRowBlock> coloredRowBlocks;
    private final ArrayList<ColoredRectangle> coloredRectangles;
    private JScrollBar scrollBar;
    private double scrollButtonTotalHeight;
    private double scrollButtonTopHeight;
    private double scrollButtonBottomHeight;

    /**
     * Construct a ChangeMarkerPanel object.
     *
     * @param baseFileLinkedList the list of rows that populate the display.
     * @param panelNumber this should be a 1 or a 2 for the first descendent or second descendent respectively.
     * @param rh the height of a single row.
     */
    ChangeMarkerPanel(FileContentsListModel fileContentsListModel, int rh) {
        this.scrollButtonBottomHeight = 0.0;
        this.scrollButtonTopHeight = 0.0;
        this.scrollButtonTotalHeight = 0.0;
        this.coloredRectangles = new ArrayList<>();
        this.coloredRowBlocks = new ArrayList<>();
        setMinimumSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        this.rowHeight = rh;
        this.totalRowCount = computeRowBlocks(fileContentsListModel);
        this.addMouseListener(new MyMouseListener(this));
    }

    /**
     * Set the scrollbar for this change panel so we can figure out what space to pre-allocate for the
     * scroll bar buttons.
     *
     * @param sb the vertical scroll bar for this change marker panel.
     */
    void setScrollBar(final JScrollBar sb) {
        this.scrollBar = sb;
    }

    /**
     * Get the vertical scroll bar for this change marker panel.
     *
     * @return return the scroll bar for this change marker panel.
     */
    JScrollBar getScrollBar() {
        return scrollBar;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(PANEL_WIDTH, PANEL_HEIGHT);
    }

    /**
     * Scroll the scroll bar so it matches the given y position.
     *
     * @param y the vertical y coordinate to scroll to.
     */
    void scrollToY(int y) {
        double adjustedY = (double) y - scrollButtonTotalHeight;
        double percentage = adjustedY / ((double) pastSize.height - scrollButtonTotalHeight);
        int minimum = getScrollBar().getModel().getMinimum();
        int maximum = getScrollBar().getModel().getMaximum();
        double range = (double) maximum - (double) minimum;
        int modelValue = (int) (percentage * range);
        getScrollBar().getModel().setValue(modelValue);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Rectangle ourRectangle = getBounds();
        if ((pastSize == null) || (pastSize.height != ourRectangle.height)) {
            pastSize = new Rectangle(ourRectangle);
            scaleRectangles(ourRectangle);
        }

        for (int i = 0; i < coloredRectangles.size(); i++) {
            ColoredRectangle coloredRectangle = coloredRectangles.get(i);
            g2d.setColor(coloredRectangle.getColor());
            g2d.setBackground(coloredRectangle.getColor());
            g2d.fillRect(coloredRectangle.getRectangle().x,
                    coloredRectangle.getRectangle().y,
                    coloredRectangle.getRectangle().width,
                    coloredRectangle.getRectangle().height);
        }
    }

    private void computeScrollButtonOffsets(Rectangle ourBoundingRectangle) {
        scrollButtonTotalHeight = 0.0;
        scrollButtonTopHeight = 0.0;
        scrollButtonBottomHeight = 0.0;

        Component[] components = scrollBar.getComponents();
        if (components.length > 0) {
            for (Component component : components) {
                // These turn out to be the scroll buttons...
                Rectangle rectangle = component.getBounds();
                scrollButtonTotalHeight += rectangle.getHeight();
                if (rectangle.getY() > 0.0) {
                    scrollButtonBottomHeight += rectangle.getHeight();
                } else {
                    scrollButtonTopHeight += rectangle.getHeight();
                }
            }
        } else {
            // This is for iMac... This is an approximation at best.
            Rectangle rectangle = scrollBar.getBounds();
            scrollButtonTopHeight = rectangle.getY();
            if (rectangle.getHeight() > 0.0) {
                scrollButtonBottomHeight = ourBoundingRectangle.getY() + (ourBoundingRectangle.getHeight() - rectangle.getHeight());
            }
            scrollButtonTotalHeight = scrollButtonTopHeight + scrollButtonBottomHeight;
        }
    }

    /**
     * Figure out the array of rectangles that we need to have to provide a summary of where
     * the edits are in the given file.
     *
     * @param ourBoundingRectangle our bounding rectangle. We only really care about the height.
     */
    private void scaleRectangles(Rectangle ourBoundingRectangle) {
        coloredRectangles.clear();

        // Figure out the maximum vertical space needed to display all the rows...
        double totalHeightOfAllRows = (double) this.rowHeight * (double) this.totalRowCount;

        computeScrollButtonOffsets(ourBoundingRectangle);

        // If the total needed to display all rows is less than our bounding rectangle, then we need to use that
        // smaller size to figure out the scaling factor per row, so things will be scaled correctly for the
        // case where the text does not fill the entire panel.
        double totalHeightToUse;
        if ((ourBoundingRectangle.getHeight() - scrollButtonTotalHeight) > totalHeightOfAllRows) {
            totalHeightToUse = totalHeightOfAllRows;
        } else {
            totalHeightToUse = ourBoundingRectangle.getHeight() - scrollButtonTotalHeight;
        }
        double scalingFactorPerRow = totalHeightToUse / this.totalRowCount;
        Iterator<ColoredRowBlock> it = coloredRowBlocks.iterator();
        int currentY = 0;
        int anchoredX = 0;
        int adjustRowCount = 0;

        // Add a rectangle for the top scroll bar button.
        if (scrollButtonTopHeight > 0) {
            coloredRectangles.add(new ColoredRectangle(QWinFrame.getQWinFrame().getBackground(),
                    new Rectangle(anchoredX + 1, currentY, PANEL_WIDTH - 2, (int) scrollButtonTopHeight)));
            currentY += (int) scrollButtonTopHeight;
        }
        while (it.hasNext()) {
            ColoredRowBlock coloredRowBlock = it.next();
            int scaledHeight = (int) ((double) (adjustRowCount + coloredRowBlock.getRowCount()) * scalingFactorPerRow);

            // Make each edit at least one pixel high.
            if (scaledHeight <= 0) {
                int increaseRowCount = 0;
                while (scaledHeight <= 0) {
                    increaseRowCount++;
                    scaledHeight = (int) ((double) (increaseRowCount + coloredRowBlock.getRowCount()) * scalingFactorPerRow);
                }

                // So the next block will be smaller by this amount.
                adjustRowCount = -increaseRowCount;
            }
            coloredRectangles.add(new ColoredRectangle(coloredRowBlock.getColor(),
                    new Rectangle(anchoredX + 1, currentY, PANEL_WIDTH - 2, scaledHeight)));
            currentY += scaledHeight;
        }

        // Add a rectangle for the bottom scroll bar button(s).
        if (scrollButtonBottomHeight > 0) {
            coloredRectangles.add(new ColoredRectangle(QWinFrame.getQWinFrame().getBackground(),
                    new Rectangle(anchoredX + 1, currentY, PANEL_WIDTH - 2, (int) scrollButtonBottomHeight)));
        }
    }

    /**
     * Figure out the minimum number of rectangles that will represent the changes in the file.
     *
     * @param baseFileLinkedList the list of rows in the file.
     * @param panelNumber a 1 or a 2 to indicate the changes are for the first (1) or second (2) decendent file.
     * @return the total number of rows in the display.
     */
    private int computeRowBlocks(FileContentsListModel fileContentsListModel) {
        // Make sure to start with an empty list of row blocks.
        coloredRowBlocks.clear();

        Enumeration it = fileContentsListModel.elements();
        int blockSize = 0;
        int rowCount = 0;
        int rowType = -1;
        while (it.hasMoreElements()) {
            int currentRowType;
            ContentRow contentRow = (ContentRow) it.nextElement();
            rowCount++;
            if (blockSize == 0) {
                rowType = translateRowType(contentRow.getRowType());
                currentRowType = rowType;
            } else {
                currentRowType = translateRowType(contentRow.getRowType());
            }

            if (currentRowType == rowType) {
                blockSize++;
            } else {
                coloredRowBlocks.add(new ColoredRowBlock(rowType, blockSize));
                blockSize = 1;
                rowType = currentRowType;
            }
        }

        if (blockSize > 0) {
            coloredRowBlocks.add(new ColoredRowBlock(rowType, blockSize));
        }
        return rowCount;
    }

    /**
     * We only care about inserts and deletes. Everthing else is a 'normal' row.
     *
     * @param rowType input row type.
     * @return translated row type.
     */
    private int translateRowType(int rowType) {
        int translatedRowType;
        switch (rowType) {
            case ContentRow.ROWTYPE_INSERT:
                translatedRowType = ContentRow.ROWTYPE_INSERT;
                break;
            case ContentRow.ROWTYPE_DELETE:
                translatedRowType = ContentRow.ROWTYPE_DELETE;
                break;
            case ContentRow.ROWTYPE_REPLACE:
                translatedRowType = ContentRow.ROWTYPE_REPLACE;
                break;
            default:
                translatedRowType = 0;
                break;
        }
        return translatedRowType;
    }

    /**
     * Entity class to hold info about a rectangle that represents a block of rows that share the same color.
     */
    class ColoredRectangle {

        private final Color color;
        private final Rectangle rectangle;

        ColoredRectangle(Color c, Rectangle r) {
            this.color = c;
            this.rectangle = r;
        }

        Color getColor() {
            return color;
        }

        Rectangle getRectangle() {
            return rectangle;
        }
    }

    /**
     * Entity class to hold info about a block of rows that share the same color.
     */
    class ColoredRowBlock {

        private Color color;
        private final int rowCount;

        ColoredRowBlock(int rowType, int rc) {
            switch (rowType) {
                case ContentRow.ROWTYPE_DELETE:
                    this.color = FileContentsList.getDeleteColor();
                    break;
                case ContentRow.ROWTYPE_INSERT:
                    this.color = FileContentsList.getInsertColor();
                    break;
                case ContentRow.ROWTYPE_REPLACE:
                    this.color = FileContentsList.getReplaceColor();
                    break;
                default:
                    this.color = QWinFrame.getQWinFrame().getBackground();
                    break;
            }
            this.rowCount = rc;
        }

        Color getColor() {
            return color;
        }

        int getRowCount() {
            return rowCount;
        }
    }

    class MyMouseListener extends MouseInputAdapter {

        private final ChangeMarkerPanel changeMarkerPanel;

        MyMouseListener(ChangeMarkerPanel cmp) {
            this.changeMarkerPanel = cmp;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            int y = e.getY();
            changeMarkerPanel.scrollToY(y);
        }
    }

}
