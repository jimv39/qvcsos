/*   Copyright 2004-2015 Jim Voris
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

import com.qumasoft.qvcslib.QumaAssert;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.apache.commons.jrcs.diff.Delta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class FileContentsListModel extends javax.swing.DefaultListModel<ContentRow> {
    private static final long serialVersionUID = -6257269731287651341L;

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(FileContentsListModel.class);
    private static final String TAB_EXPANSION = "    ";
    private int currentDifferenceIndex;

    FileContentsListModel(File file, CompareFilesForGUI compareResult, boolean isFirstFile, FileContentsListModel firstFileListModel) {
        super();
        this.currentDifferenceIndex = -1;
        BufferedReader fileReader = null;
        if (file.canRead()) {
            try {
                fileReader = new BufferedReader(new FileReader(file));
                int lineIndex = 0;
                while (true) {
                    String line = fileReader.readLine();
                    if (line == null) {
                        break;
                    }
                    String formattedLine = formatLine(line);
                    ContentRow row = new ContentRow(formattedLine, line, compareResult, lineIndex, isFirstFile);
                    if (!isFirstFile) {
                        // If this is the 2nd file, for replacement lines figure out where in the line things are different.
                        if (row.getRowType() == ContentRow.ROWTYPE_REPLACE) {
                            System.out.println("lineIndex: [" + lineIndex + "]");
                            ContentRow firstModelRow = firstFileListModel.get(lineIndex);
                            row.decorateDifferences(firstModelRow);
                            firstModelRow.decorateDifferences(row);
                        }
                    }
                    if (row.getBlankRowsBefore() > 0) {
                        for (int i = 0; i < row.getBlankRowsBefore(); i++) {
                            addBlankRow(row.getDelta());
                        }
                    }
                    addElement(row);
                    if (row.getBlankRowsAfter() > 0) {
                        for (int i = 0; i < row.getBlankRowsAfter(); i++) {
                            addBlankRow(row.getDelta());
                        }
                    }
                    lineIndex++;
                }
            } catch (java.io.FileNotFoundException e) {
                LOGGER.warn("Caught exception: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            } catch (java.io.IOException e) {
                LOGGER.warn("Caught exception: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
            } finally {
                try {
                    if (fileReader != null) {
                        fileReader.close();
                    }
                } catch (IOException e) {
                    LOGGER.warn("Caught exception: " + e.getClass().toString() + ": " + e.getLocalizedMessage());
                }
            }
        }
    }

    final void addBlankRow(Delta delta) {
        addElement(new ContentRow(delta));
    }

    FileContentsListModel(String filename, CompareFilesForGUI compareResult, boolean isFirstFile, FileContentsListModel secondListModel) {
        this(new File(filename), compareResult, isFirstFile, secondListModel);
        this.currentDifferenceIndex = -1;
    }

    void setCurrentDifferenceIndex(int index) {
        QumaAssert.isTrue(index <= size());
        this.currentDifferenceIndex = index;
        fireContentsChanged(this, index, index);
    }

    int getCurrentDifferenceIndex() {
        return currentDifferenceIndex;
    }

    final String formatLine(String line) {
        if (line.length() == 0) {
            return ""; // So something shows up on the list control.
        }
        int tabIndex = line.indexOf('\t');
        if (tabIndex >= 0) {
            StringBuilder expandedString = new StringBuilder();
            for (int i = 0; i < line.length(); i++) {
                char currentChar = line.charAt(i);
                if (currentChar == '\t') {
                    expandedString.append(TAB_EXPANSION);
                } else {
                    expandedString.append(currentChar);
                }
            }
            line = expandedString.toString();
        }
        return line;
    }
}
