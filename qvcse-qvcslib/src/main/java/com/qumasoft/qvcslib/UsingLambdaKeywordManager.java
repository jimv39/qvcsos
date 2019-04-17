//   Copyright 2004-2015 Jim Voris
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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implement the QVCS keyword manager using Java 8 lambda expressions, and java.util.stream() classes. Note that this implementation is meant to be used only for text files. It
 * will not work for binary files. For now , we also do not support the Log or LogX keywords. The keyword delimiter character is hard-coded to be '$', and all the other keyword
 * strings are hard-coded as well. In other words, this is really just a proof-of-concept implementation.
 *
 */
public class UsingLambdaKeywordManager implements KeywordManagerInterface {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(UsingLambdaKeywordManager.class);
    private static final String AUTHOR_KEYWORD = "Author";
    private static final String DATE_KEYWORD = "Date";
    private static final String FILENAME_KEYWORD = "Filename";
    private static final String FILEPATH_KEYWORD = "FilePath";
    private static final String HEADER_KEYWORD = "Header";
    private static final String HEADERPATH_KEYWORD = "HeaderPath";
    private static final String LABEL_KEYWORD = "Label";
    private static final String LOGFILE_KEYWORD = "Logfile";
    private static final String OWNER_KEYWORD = "Owner";
    private static final String PROJECT_KEYWORD = "Project";
    private static final String REVISION_KEYWORD = "Revision";
    private static final String VER_KEYWORD = "VER";
    private static final String VERSION_KEYWORD = "Version";
    private static final String NO_LABEL_LABEL = "NONE";
    private static final String MULTIPLE_LABELS = "MULTIPLE LABELS";

    @Override
    public void expandKeywords(FileInputStream inStream, KeywordExpansionContext keywordExpansionContext) throws IOException, QVCSException {
        privateExpandKeywords(inStream, keywordExpansionContext);
    }

    @Override
    public void expandKeywords(byte[] inBuffer, KeywordExpansionContext keywordExpansionContext) throws IOException, QVCSException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(inBuffer);
        privateExpandKeywords(byteArrayInputStream, keywordExpansionContext);
    }

    private void privateExpandKeywords(InputStream inputStream, KeywordExpansionContext keywordExpansionContext) throws IOException, QVCSException {
        OutputStream outputStream = keywordExpansionContext.getOutStream();
        LogfileInfo logfileInfo = keywordExpansionContext.getLogfileInfo();
        File outputFile = keywordExpansionContext.getOutputFile();
        int revisionIndex = keywordExpansionContext.getRevisionIndex();
        String labelString = keywordExpansionContext.getLabelString();
        String appendedPath = keywordExpansionContext.getAppendedPath();
        AbstractProjectProperties projectProperties = keywordExpansionContext.getProjectProperties();
        if (logfileInfo.getLogFileHeaderInfo().getLogFileHeader().attributes().getIsBinaryfile()) {
            // We do not support binary files.
            // TODO -- just copy the input stream to the output stream.
            LOGGER.info("Binary files not supported.");
            throw new QVCSRuntimeException("Binary files not supported.");
        } else {
            ExpansionContext expansionContext = new ExpansionContext(logfileInfo, outputFile, revisionIndex, labelString, appendedPath, projectProperties);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            Stream<String> lines = bufferedReader.lines();

            // The expanded output will go in the expandedLines ArrayList.
            List<String> expandedLines = new ArrayList<>();

            // Expand the lines.
            lines.forEachOrdered(contractedLine -> expandLine(contractedLine, expandedLines, expansionContext));

            // Write the expanded lines to the output file...
            PrintWriter printWriter = new PrintWriter(outputStream);
            expandedLines.stream().forEachOrdered(expandedLine -> printWriter.println(expandedLine));
            printWriter.flush();
        }
    }

    @Override
    public void contractKeywords(FileInputStream inStream, OutputStream outStream, AtomicReference<String> checkInComment, AbstractProjectProperties projectProperties,
                                 boolean binaryFileFlag) throws IOException, QVCSException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void expandLine(String contractedLine, List<String> expandedLines, ExpansionContext expansionContext) {
        StringBuilder expandedLine = new StringBuilder();
        String[] keywordCandidates = contractedLine.split("\\$");
        boolean lineEndsWithDollar = contractedLine.endsWith("$");
        if (keywordCandidates.length > 1) {
            int segmentIndex = 0;
            int maxSegmentIndex = keywordCandidates.length - 1;
            for (String segment : keywordCandidates) {
                expandKeywordCandidate(segment, expandedLine, expansionContext, segmentIndex == maxSegmentIndex, lineEndsWithDollar);
                segmentIndex++;
            }
            expandedLines.add(expandedLine.toString());
        } else {
            expandedLines.add(contractedLine);
        }
    }

    /**
     * Expand a candidate keyword. If the input segment has no keywords, then the expanded string will be the same as the input string.
     *
     * @param segment the string that is a keyword candidate. It <i>might</i> be a keyword.
     * @param expandedLine the expansion of the input keyword candidate. This will be the expanded keyword, or it will be same as the input segment (if the input segment is not a
     * keyword).
     * @param expansionContext a collection of information useful for expanding keywords.
     * @param isLastSegment flag that is true if the input segment is the final segment of the split input line.
     * @param lineEndsWithDollar flag that is true if the line from which the segment has been extracted ends with the '$' character.
     */
    private void expandKeywordCandidate(String segment, StringBuilder expandedLine, ExpansionContext expansionContext, boolean isLastSegment, boolean lineEndsWithDollar) {
        RevisionHeader revisionHeader;
        DateFormat dateFormat;
        DateFormat timeFormat;
        String dateTime;
        StringBuilder workfileName;
        LogFileHeaderInfo headerInfo;
        switch (segment) {
            case AUTHOR_KEYWORD:
                AccessList modifierList;
                modifierList = new AccessList(expansionContext.getLogfileInfo().getLogFileHeaderInfo().getModifierList());
                revisionHeader = expansionContext.getLogfileInfo().getRevisionInformation().getRevisionHeader(expansionContext.getRevisionIndex());
                String author;
                author = modifierList.indexToUser(revisionHeader.getCreatorIndex());
                expandedLine.append(AUTHOR_KEYWORD).append(": ").append(author).append(" $");
                break;
            case DATE_KEYWORD:
                dateFormat = new SimpleDateFormat("EEEEE, MMMMM dd, yyyy");
                timeFormat = new SimpleDateFormat("h:mm:ss aaa");
                revisionHeader = expansionContext.getLogfileInfo().getRevisionInformation().getRevisionHeader(expansionContext.getRevisionIndex());
                dateTime = dateFormat.format(revisionHeader.getCheckInDate()) + " "
                        + timeFormat.format(revisionHeader.getCheckInDate());
                expandedLine.append(DATE_KEYWORD).append(": ").append(dateTime).append(" $");
                break;
            case FILENAME_KEYWORD:
                String canonicalFilename = null;
                try {
                    canonicalFilename = expansionContext.getOutputFile().getCanonicalPath();
                } catch (IOException ex) {
                    LOGGER.warn(ex.getLocalizedMessage(), ex);
                }
                expandedLine.append(FILENAME_KEYWORD).append(": ").append(canonicalFilename).append(" $");
                break;
            case FILEPATH_KEYWORD:
                String fileName;
                if (expansionContext.getAppendedPath().length() > 0) {
                    fileName = expansionContext.getAppendedPath() + "/" + expansionContext.getLogfileInfo().getShortWorkfileName();
                } else {
                    fileName = expansionContext.getLogfileInfo().getShortWorkfileName();
                }
                expandedLine.append(FILEPATH_KEYWORD).append(": ").append(fileName).append(" $");
                break;
            case HEADER_KEYWORD:
                revisionHeader = expansionContext.getLogfileInfo().getRevisionInformation().getRevisionHeader(expansionContext.getRevisionIndex());
                dateFormat = new SimpleDateFormat("EEEEE, MMMMM dd, yyyy");
                timeFormat = new SimpleDateFormat("h:mm:ss aaa");
                dateTime = dateFormat.format(revisionHeader.getCheckInDate()) + " "
                        + timeFormat.format(revisionHeader.getCheckInDate());
                headerInfo = expansionContext.getLogfileInfo().getLogFileHeaderInfo();
                StringBuilder headerStringBuilder = new StringBuilder(expansionContext.getOutputFile().getName());
                headerStringBuilder.append(" ")
                        .append(REVISION_KEYWORD)
                        .append(": ")
                        .append(revisionHeader.getRevisionString())
                        .append(" ")
                        .append(dateTime)
                        .append(" ")
                        .append(headerInfo.getOwner());
                expandedLine.append(HEADER_KEYWORD).append(": ").append(headerStringBuilder).append(" $");
                break;
            case HEADERPATH_KEYWORD:
                StringBuilder headerString = new StringBuilder();
                workfileName = new StringBuilder();
                revisionHeader = expansionContext.getLogfileInfo().getRevisionInformation().getRevisionHeader(expansionContext.getRevisionIndex());
                headerInfo = expansionContext.getLogfileInfo().getLogFileHeaderInfo();
                dateFormat = new SimpleDateFormat("EEEEE, MMMMM dd, yyyy");
                timeFormat = new SimpleDateFormat("h:mm:ss aaa");
                dateTime = dateFormat.format(revisionHeader.getCheckInDate()) + " "
                        + timeFormat.format(revisionHeader.getCheckInDate());
                if (expansionContext.getAppendedPath().length() > 0) {
                    workfileName.append(expansionContext.getAppendedPath()).append("/").append(expansionContext.getLogfileInfo().getShortWorkfileName());
                } else {
                    workfileName.append(expansionContext.getLogfileInfo().getShortWorkfileName());
                }

                headerString.append(workfileName.toString())
                            .append(" ")
                            .append(REVISION_KEYWORD)
                            .append(": ")
                            .append(revisionHeader.getRevisionString())
                            .append(" ")
                            .append(dateTime)
                            .append(" ")
                            .append(headerInfo.getOwner());
                expandedLine.append(HEADERPATH_KEYWORD).append(": ").append(headerString).append(" $");
                break;
            case LABEL_KEYWORD:
                LabelInfo[] labelInfo = expansionContext.getLogfileInfo().getLogFileHeaderInfo().getLabelInfo();
                revisionHeader = expansionContext.getLogfileInfo().getRevisionInformation().getRevisionHeader(expansionContext.getRevisionIndex());
                String useLabelString = deduceLabelString(labelInfo, revisionHeader, expansionContext.getLabelString());
                expandedLine.append(LABEL_KEYWORD).append(": ").append(useLabelString).append(" $");
                break;
            case LOGFILE_KEYWORD:
                break;
            case OWNER_KEYWORD:
                break;
            case PROJECT_KEYWORD:
                break;
            case REVISION_KEYWORD:
                revisionHeader = expansionContext.getLogfileInfo().getRevisionInformation().getRevisionHeader(expansionContext.getRevisionIndex());
                String revisionString = revisionHeader.getRevisionString();
                expandedLine.append(REVISION_KEYWORD).append(": ").append(revisionString).append(" $");
                break;
            case VER_KEYWORD:
                break;
            case VERSION_KEYWORD:
                break;
            case "":
                expandedLine.append("$");
                break;
            default:
                if (isLastSegment) {
                    if (lineEndsWithDollar) {
                        expandedLine.append(segment).append("$");
                    } else {
                        expandedLine.append(segment);
                    }
                } else {
                    expandedLine.append(segment).append("$");
                }
                break;
        }
    }

    private String deduceLabelString(LabelInfo[] labelInfos, RevisionHeader revisionHeader, String inputLabelString) {
        String labelString = NO_LABEL_LABEL;
        String revisionString = revisionHeader.getRevisionString();
        boolean labelFoundFlag = false;

        if (inputLabelString == null) {
            if (labelInfos != null) {
                for (LabelInfo labelInfo : labelInfos) {
                    if (labelInfo.getLabelRevisionString().compareTo(revisionString) == 0) {
                        if (!labelFoundFlag) {
                            labelString = labelInfo.getLabelString();
                            labelFoundFlag = true;
                        } else {
                            labelString = MULTIPLE_LABELS;
                            break;
                        }
                    }
                }

                if (!labelFoundFlag) {
                    // See if a floating label matches.
                    if (revisionHeader.isTip()) {
                        revisionString = revisionString.substring(0, revisionString.lastIndexOf('.'));
                        for (LabelInfo labelInfo : labelInfos) {
                            if (labelInfo.getLabelRevisionString().compareTo(revisionString) == 0) {
                                labelString = labelInfo.getLabelString();
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            for (LabelInfo labelInfo : labelInfos) {
                if (labelInfo.getLabelString().compareTo(inputLabelString) == 0) {
                    labelFoundFlag = true;
                    break;
                }
            }
            if (labelFoundFlag) {
                labelString = inputLabelString;
            }
        }
        return labelString;
    }

    /**
     * An immutable class for aggregating information that we need in order to expand QVCS keywords.
     */
    private static class ExpansionContext {

        private final LogfileInfo logfileInfo;
        private final File outputFile;
        private final int revisionIndex;
        private final String labelString;
        private final String appendedPath;
        private final AbstractProjectProperties projectProperties;

        ExpansionContext(LogfileInfo inputLogfileInfo, File inputOutputFile, int inputRevisionIndex, String inputLabelString, String inputAppendedPath,
                AbstractProjectProperties inputProjectProperties) {
            this.logfileInfo = inputLogfileInfo;
            this.outputFile = inputOutputFile;
            this.revisionIndex = inputRevisionIndex;
            this.labelString = inputLabelString;
            this.appendedPath = inputAppendedPath;
            this.projectProperties = inputProjectProperties;
        }

        /**
         * @return the logfileInfo
         */
        public LogfileInfo getLogfileInfo() {
            return logfileInfo;
        }

        /**
         * @return the revisionIndex
         */
        public int getRevisionIndex() {
            return revisionIndex;
        }

        /**
         * @return the labelString
         */
        public String getLabelString() {
            return labelString;
        }

        /**
         * @return the appendedPath
         */
        public String getAppendedPath() {
            return appendedPath;
        }

        /**
         * @return the projectProperties
         */
        public AbstractProjectProperties getProjectProperties() {
            return projectProperties;
        }

        /**
         * @return the outputFile
         */
        public File getOutputFile() {
            return outputFile;
        }
    }

}
