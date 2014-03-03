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
package com.qumasoft.guitools.compare;

import com.qumasoft.guitools.merge.ColorManager;
import com.qumasoft.qvcslib.QumaAssert;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import org.apache.commons.jrcs.diff.AddDelta;
import org.apache.commons.jrcs.diff.ChangeDelta;
import org.apache.commons.jrcs.diff.DeleteDelta;
import org.apache.commons.jrcs.diff.Delta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;

class ContentRow extends JLabel {
    private static final long serialVersionUID = -4490555483346212013L;

    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.guitools.compare");
    public static final byte ROWTYPE_NORMAL = 10;
    public static final byte ROWTYPE_INSERT = 11;
    public static final byte ROWTYPE_DELETE = 12;
    public static final byte ROWTYPE_REPLACE = 13;
    public static final byte ROWTYPE_BLANK = 14;
    private byte rowType;
    private int blankRowsAfter = 0;
    private int blankRowsBefore = 0;
    private int lineIndex;
    private String actualText;
    private Delta delta = null;
    private byte[] fileACharacterTypeArray;
    private boolean rowHadAnnotations = false;

    ContentRow(Delta d) {
        rowType = ROWTYPE_BLANK;
        this.delta = d;
        QumaAssert.isTrue(this.delta != null);
    }

    ContentRow(String formattedText, String actText, CompareFilesForGUI compareResult, int lineIdx, boolean isFirstFile) {
        super(formattedText);
        this.actualText = actText;
        setRowType(determineRowType(compareResult, lineIdx, isFirstFile));
        delta = compareResult.getDelta(lineIdx, isFirstFile);
        int deletedLineCount;
        int insertedLineCount;
        this.lineIndex = lineIdx;
        if (delta != null) {
            deletedLineCount = delta.getOriginal().size();
            insertedLineCount = delta.getRevised().size();
            switch (rowType) {
                case ROWTYPE_INSERT:
                    if (isFirstFile) {
                        blankRowsAfter = insertedLineCount;
                    }
                    break;
                case ROWTYPE_DELETE:
                    if (!isFirstFile) {
                        blankRowsAfter = deletedLineCount;
                    }
                    break;
                case ROWTYPE_REPLACE:
                    if (insertedLineCount > deletedLineCount) {
                        if (isFirstFile) {
                            int lastLineOfReplace = delta.getOriginal().last();
                            if (lineIdx == lastLineOfReplace) {
                                blankRowsAfter = insertedLineCount - deletedLineCount;
                            }
                        }
                    } else if (deletedLineCount > insertedLineCount) {
                        if (!isFirstFile) {
                            int lastLineOfReplace = delta.getRevised().last();
                            if (lineIdx == lastLineOfReplace) {
                                blankRowsAfter = deletedLineCount - insertedLineCount;
                            }
                        }
                    }
                    break;
                case ROWTYPE_NORMAL:
                    if (isFirstFile) {
                        if (lineIdx == 0) {
                            blankRowsBefore = insertedLineCount;
                        } else if ((lineIdx == delta.getOriginal().first()) && (deletedLineCount == 0)) {
                            blankRowsBefore = insertedLineCount;
                        } else if (delta instanceof AddDelta) {
                            blankRowsAfter = insertedLineCount;
                        }
                    } else {
                        if (lineIdx == 0) {
                            blankRowsBefore = deletedLineCount;
                        }
                        if ((lineIdx == delta.getRevised().first()) && (insertedLineCount == 0)) {
                            blankRowsBefore = deletedLineCount;
                        } else if (delta instanceof DeleteDelta) {
                            blankRowsAfter = deletedLineCount;
                        }
                    }
                    break;
                default:
                    LOGGER.log(Level.WARNING, "Unknown content row type for line index: [" + lineIdx + "]");
                    break;
            }
        }
    }

    Delta getDelta() {
        return delta;
    }

    String getActualText() {
        return actualText;
    }

    final void setRowType(byte rType) {
        QumaAssert.isTrue((rType >= ROWTYPE_NORMAL) && (rType <= ROWTYPE_REPLACE));
        this.rowType = rType;
    }

    byte getRowType() {
        return rowType;
    }

    int getBlankRowsAfter() {
        return blankRowsAfter;
    }

    int getBlankRowsBefore() {
        return blankRowsBefore;
    }

    int getLineNumberIndex() {
        return lineIndex;
    }

    final byte determineRowType(CompareFilesForGUI compareResult, int lineIdx, boolean isFirstFile) {
        byte rType = compareResult.getRowType(lineIdx, isFirstFile);
        return rType;
    }

    String getLineNumber() {
        return Integer.toString(lineIndex + 1) + "   ";
    }

    /**
     * Decorate the differences between this row and its peer. This is used for replacement rows only.
     * Use the apache algorithm.
     *
     * @param peerRow the peer row from the other file.
     */
    void decorateDifferences(ContentRow peerRow) {
        String s = getText();
        String peer = peerRow.getText();
        if (s.length() > 0 && peer.length() > 0) {
            Byte[] sBytes = new Byte[s.getBytes().length];
            Byte[] peerBytes = new Byte[peer.getBytes().length];
            int sIndex = 0;
            for (Byte sourceByte : s.getBytes()) {
                sBytes[sIndex++] = sourceByte;
            }
            int peerIndex = 0;
            for (Byte peerByte : peer.getBytes()) {
                peerBytes[peerIndex++] = peerByte;
            }
            try {
                Revision differences = Diff.diff(sBytes, peerBytes);
                deduceCharacterAnnotations(differences, sBytes);
            } catch (DifferentiationFailedException e) {
                LOGGER.log(Level.WARNING, "Decoration failed: " + e.getLocalizedMessage());
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        if ((getRowType() == ROWTYPE_REPLACE) && rowHadAnnotations) {
            Graphics2D g2 = (Graphics2D) g;

            String s = getText();

            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (s.length() > 0) {
                int index = 0;
                AttributedString attributedString = new AttributedString(s, getFont().getAttributes());
                try {
                    for (byte rType : fileACharacterTypeArray) {
                        switch (rType) {
                            case ContentRow.ROWTYPE_DELETE:
                                attributedString.addAttribute(TextAttribute.STRIKETHROUGH, null, index, index + 1);
                                break;
                            case ContentRow.ROWTYPE_REPLACE:
                                attributedString.addAttribute(TextAttribute.BACKGROUND, ColorManager.getReplaceCompareHiliteBackgroundColor(), index, index + 1);
                                break;
                            default:
                                break;
                        }
                        index++;
                    }
                    g2.drawString(attributedString.getIterator(), 0, getFont().getSize());
                } catch (java.lang.IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, "bad replace indexes. begin index: ["
                            + index + "] end index: ["
                            + index + "]. String length: [" + s.length() + "]");
                }
            } else {
                super.paint(g);
            }
        } else {
            super.paint(g);
        }
    }

    private void deduceCharacterAnnotations(Revision differences, Byte[] sBytes) {
        // First get all the deltas...
        Delta[] deltas = new Delta[differences.size()];
        for (int i = 0; i < differences.size(); i++) {
            deltas[i] = differences.getDelta(i);
        }
        fileACharacterTypeArray = new byte[sBytes.length];
        // Set all the rows to default to NORMAL.
        for (int i = 0; i < sBytes.length; i++) {
            fileACharacterTypeArray[i] = ContentRow.ROWTYPE_NORMAL;
        }
        for (Delta characterDelta : deltas) {
            byte rType;
            if (characterDelta instanceof AddDelta) {
                rType = ContentRow.ROWTYPE_INSERT;
                rowHadAnnotations = true;
            } else if (characterDelta instanceof ChangeDelta) {
                rType = ContentRow.ROWTYPE_REPLACE;
                rowHadAnnotations = true;
            } else if (characterDelta instanceof DeleteDelta) {
                rType = ContentRow.ROWTYPE_DELETE;
                rowHadAnnotations = true;
            } else {
                continue;   // this is goofy... and should never happen. We'll ignore the problem.
            }
            @SuppressWarnings("unchecked")
                    int firstA = characterDelta.getOriginal().first();
            int lastA = characterDelta.getOriginal().last();
            for (int j = firstA; j <= lastA; j++) {
                fileACharacterTypeArray[j] = rType;
            }
        }
    }
}
