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
package com.qumasoft.guitools.merge;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import javax.swing.JLabel;

/**
 * Merged descendant file content row.
 * @author Jim Voris
 */
public class MergedDescendentFileContentRow extends JLabel {
    private static final long serialVersionUID = 2094081418324953231L;

    static final byte ROWTYPE_NORMAL = 10;
    static final byte ROWTYPE_INSERT = 11;
    static final byte ROWTYPE_DELETE = 12;
    static final byte ROWTYPE_REPLACE = 13;
    static final byte ROWTYPE_BLANK = 14;    // A blank line (i.e. the string's length is 0;
    static final byte ROWTYPE_EMPTY = 15;    // A null line (i.e. the line does not exist for the given context).
    static final byte ROWTYPE_UNDEFINED = 16;

    private final Font rowFont;
    private boolean overlapFlag;
    private byte ancestorRowType;
    private int ancestorFileLineNumber;
    private int ancestorFileOriginalLineNumber;
    private long ancestorSeekPosition;
    private String ancestorText;
    private int applyDescendentEditIndex;
    private byte firstDescendentRowType;
    private int firstDescendentLineNumber;
    private String firstDescendentText;
    private EditInfo firstDescendentEditInfo = null;
    private boolean firstDescendentCheckBoxVisibleFlag = false;
    private byte secondDescendentRowType;
    private int secondDescendentLineNumber;
    private String secondDescendentText;
    private EditInfo secondDescendentEditInfo = null;
    private boolean secondDescendentCheckBoxVisibleFlag = false;
    private boolean rowDeletedFlag = false;
    private boolean isSelectedFlag = false;

    MergedDescendentFileContentRow(Font f) {
        this.ancestorRowType = ROWTYPE_UNDEFINED;
        this.firstDescendentRowType = ROWTYPE_UNDEFINED;
        this.secondDescendentRowType = ROWTYPE_UNDEFINED;
        this.rowFont = f;
        this.overlapFlag = false;
        this.isSelectedFlag = false;
        this.applyDescendentEditIndex = 0;
    }

    void setApplyDecendentEdit(int decendentIndex) {
        assert (decendentIndex >= 0 && decendentIndex <= 2);
        applyDescendentEditIndex = decendentIndex;
    }

    int getApplyDecendentEditIndex() {
        return applyDescendentEditIndex;
    }

    EditInfo getFirstDecendentEditInfo() {
        return firstDescendentEditInfo;
    }

    void setFirstDecendentEditInfo(EditInfo editInfo) {
        this.firstDescendentEditInfo = editInfo;
    }

    EditInfo getSecondDecendentEditInfo() {
        return secondDescendentEditInfo;
    }

    void setSecondDecendentEditInfo(EditInfo editInfo) {
        secondDescendentEditInfo = editInfo;
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
        return ancestorFileLineNumber;
    }

    void setAncestorLineNumber(int lineNumber) {
        ancestorFileLineNumber = lineNumber;
    }

    int getAncestorOriginalLineNumber() {
        return ancestorFileOriginalLineNumber;
    }

    void setAncestorOriginalLineNumber(int lineNumber) {
        ancestorFileOriginalLineNumber = lineNumber;
    }

    long getAncestorSeekPosition() {
        return ancestorSeekPosition;
    }

    void setAncestorSeekPosition(long seekPosition) {
        ancestorSeekPosition = seekPosition;
    }

    byte getFirstDecendentRowType() {
        return firstDescendentRowType;
    }

    void setFirstDecendentRowType(byte rowType) {
        firstDescendentRowType = rowType;
    }

    int getFirstDecendentLineNumber() {
        return firstDescendentLineNumber;
    }

    void setFirstDecendentLineNumber(int lineNumber) {
        firstDescendentLineNumber = lineNumber;
    }

    String getFirstDecendentText() {
        return firstDescendentText;
    }

    void setFirstDecendentText(String text) {
        firstDescendentText = text;
    }

    byte getSecondDecendentRowType() {
        return secondDescendentRowType;
    }

    void setSecondDecendentRowType(byte rowType) {
        secondDescendentRowType = rowType;
    }

    int getSecondDecendentLineNumber() {
        return secondDescendentLineNumber;
    }

    void setSecondDecendentLineNumber(int lineNumber) {
        secondDescendentLineNumber = lineNumber;
    }

    String getSecondDecendentText() {
        return secondDescendentText;
    }

    void setSecondDecendentText(String text) {
        secondDescendentText = text;
    }

    void setRowDeletedFlag(boolean flag) {
        rowDeletedFlag = flag;
    }

    boolean getRowDeletedFlag() {
        return rowDeletedFlag;
    }

    void setOverlapFlag(boolean flag) {
        overlapFlag = flag;
    }

    boolean getOverlapFlag() {
        return overlapFlag;
    }

    void setIsSelectedFlag(boolean flag) {
        isSelectedFlag = flag;
    }

    boolean getIsSelectedFlag() {
        return isSelectedFlag;
    }

    void setFirstDecendentCheckBoxVisibleFlag(boolean flag) {
        firstDescendentCheckBoxVisibleFlag = flag;
    }

    boolean getFirstDecendentCheckBoxVisibleFlag() {
        return firstDescendentCheckBoxVisibleFlag;
    }

    void setSecondDecendentCheckBoxVisibleFlag(boolean flag) {
        secondDescendentCheckBoxVisibleFlag = flag;
    }

    boolean getSecondDecendentCheckBoxVisibleFlag() {
        return secondDescendentCheckBoxVisibleFlag;
    }

    @Override
    public void paint(Graphics g) {
        if (getRowDeletedFlag()) {
            Graphics2D g2 = (Graphics2D) g;

            String s = getText();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            if (s.length() > 0) {
                AttributedString as = new AttributedString(s);
                as.addAttribute(TextAttribute.FONT, rowFont);
                if (getIsSelectedFlag()) {
                    super.paint(g);
                    as.addAttribute(TextAttribute.FOREGROUND, getForeground());
                    as.addAttribute(TextAttribute.BACKGROUND, getBackground());
                } else {
                    super.paint(g);
                    as.addAttribute(TextAttribute.FOREGROUND, ColorManager.getDeleteForegroundColor());
                    as.addAttribute(TextAttribute.BACKGROUND, ColorManager.getChangeBackgroundColor());
                }
                as.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON);

                g2.drawString(as.getIterator(), 0, rowFont.getSize());
            } else {
                super.paint(g);
            }
        } else {
            super.paint(g);
        }
    }
}
