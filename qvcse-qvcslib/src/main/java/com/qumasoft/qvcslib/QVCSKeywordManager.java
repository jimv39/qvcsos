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
package com.qumasoft.qvcslib;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * QVCS Keyword manager. Expand and contract QVCS keywords.
 * @author Jim Voris
 */
public final class QVCSKeywordManager implements KeywordManagerInterface {
    // This class is a singleton.
    private static final QVCSKeywordManager KEYWORDMANAGER = new QVCSKeywordManager();
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(QVCSKeywordManager.class);
    private final String authorKeyword;
    private final String commentKeyword;
    private final String copyrightKeyword;
    private final String dateKeyword;
    private final String endlogKeyword;
    private final String filenameKeyword;
    private final String filePathKeyword;
    private final String headerKeyword;
    private final String headerPathKeyword;
    private final String logKeyword;
    private final String logfileKeyword;
    private final String ownerKeyword;
    private final String revisionKeyword;
    private final String verKeyword;
    private final String versionKeyword;
    private final String labelKeyword;
    private final String projectKeyword;
    private final String markerCharacter;
    private final String terminatorMarker;
    private final String noLabelLabel;
    private final String multipleLabels;
    private final byte keywordPrefix;
    private final byte terminatorByte;
    private byte[] eol = null;
    private byte[] spaces = null;
    private String copyrightMessage = null;
    private int wordWrapColumn;
    private final DateFormat dateFormat = new SimpleDateFormat("EEEEE, MMMMM dd, yyyy");
    private final DateFormat timeFormat = new SimpleDateFormat("h:mm:ss aaa");
    private DateFormat copyrightYearFormat = null;
//    private boolean isBinaryFileFlag = false;
    private boolean useUnixPathSeparatorFlag = false;
    private KeywordProperties keywordProperties = null;
    private static final int DEFAULT_WORD_WRAP_COLUMN = 72;
    private static final int RESERVE_SPACE_FOR_BINARY_COPYRIGHT = 4;
    private static final int MAXIMUM_CHARACTERS_FOR_LOGX_REVISION_COUNT = 5;
    private static final int EXTRACT_COMMENT_START_INDEX_OFFSET = 2;
    private static final int EXTRACT_COMMENT_ADJUST_LENGTH = 3;
    /**
     * Get the singleton keyword manager.
     * @return the singleton keyword manager.
     */
    public static KeywordManagerInterface getInstance() {
        return KEYWORDMANAGER;
    }

    /**
     * Get our own (not the singleton) keyword manager. We build a new one, instead of re-using the singleton.
     * @return a new keyword manager.
     */
    public static KeywordManagerInterface getNewInstance() {
        KeywordManagerInterface keywordManagerInterface;
        keywordManagerInterface = new QVCSKeywordManager();
        return keywordManagerInterface;
    }


    /**
     * Creates a new instance of QVCSKeywordManager.
     */
    private QVCSKeywordManager() {
        keywordProperties = new KeywordProperties();

        authorKeyword = keywordProperties.getStringValue(KeywordProperties.AUTHOR_KEY);
        commentKeyword = keywordProperties.getStringValue(KeywordProperties.COMMENT_KEY);
        copyrightKeyword = keywordProperties.getStringValue(KeywordProperties.COPYRIGHT_KEY);
        dateKeyword = keywordProperties.getStringValue(KeywordProperties.DATE_KEY);
        endlogKeyword = keywordProperties.getStringValue(KeywordProperties.ENDLOG_KEY);
        filenameKeyword = keywordProperties.getStringValue(KeywordProperties.FILENAME_KEY);
        filePathKeyword = keywordProperties.getStringValue(KeywordProperties.FILEPATH_KEY);
        headerKeyword = keywordProperties.getStringValue(KeywordProperties.HEADER_KEY);
        headerPathKeyword = keywordProperties.getStringValue(KeywordProperties.HEADERPATH_KEY);
        logKeyword = keywordProperties.getStringValue(KeywordProperties.LOG_KEY);
        logfileKeyword = keywordProperties.getStringValue(KeywordProperties.LOGFILE_KEY);
        ownerKeyword = keywordProperties.getStringValue(KeywordProperties.OWNER_KEY);
        revisionKeyword = keywordProperties.getStringValue(KeywordProperties.REVISION_KEY);
        verKeyword = keywordProperties.getStringValue(KeywordProperties.VER_KEY);
        versionKeyword = keywordProperties.getStringValue(KeywordProperties.VERSION_KEY);
        labelKeyword = keywordProperties.getStringValue(KeywordProperties.LABEL_KEY);
        projectKeyword = keywordProperties.getStringValue(KeywordProperties.PROJECT_KEY);

        markerCharacter = keywordProperties.getStringValue(KeywordProperties.MARKER_TAG);
        terminatorMarker = keywordProperties.getStringValue(KeywordProperties.TERMINATOR_MARKER);
        wordWrapColumn = keywordProperties.getIntegerValue(KeywordProperties.WORDWRAP_COLUMN_TAG);
        noLabelLabel = keywordProperties.getStringValue(KeywordProperties.NO_LABEL_LABEL_TAG);
        multipleLabels = keywordProperties.getStringValue(KeywordProperties.MULTIPLE_LABELS_TAG);
        eol = keywordProperties.getEOLSequence();
        copyrightMessage = keywordProperties.getStringValue(KeywordProperties.COPYRIGHT_MESSAGE_TAG);
        useUnixPathSeparatorFlag = keywordProperties.getUseUnixPathSeparator();

        if (wordWrapColumn == 0) {
            wordWrapColumn = DEFAULT_WORD_WRAP_COLUMN;
        }

        keywordPrefix = (byte) markerCharacter.charAt(0);
        terminatorByte = (byte) terminatorMarker.charAt(0);

        copyrightYearFormat = DateFormat.getDateInstance(DateFormat.YEAR_FIELD);

        spaces = new byte[2];
        spaces[0] = ' ';
        spaces[1] = ' ';
    }

    @Override
    public void expandKeywords(final FileInputStream inStream, KeywordExpansionContext keywordExpansionContext) throws java.io.IOException, QVCSException {
        RevisionHeader revisionHeader = keywordExpansionContext.getLogfileInfo().getRevisionInformation().getRevisionHeader(keywordExpansionContext.getRevisionIndex());
        if (revisionHeader == null) {
            throw new QVCSException("Revision index: " + keywordExpansionContext.getRevisionIndex() + " does not exist!");
        }

        // Read the entire inStream into a buffer...
        FileChannel inputChannel = null;
        try {
            // We use a channel to figure out the size of the input stream.
            inputChannel = inStream.getChannel();
            long size = inputChannel.size();

            // Read the stream in the vanilla way.
            byte[] inputBuffer = new byte[(int) size];
            inStream.read(inputBuffer);

            expandKeywordsToStream(inputBuffer, revisionHeader, keywordExpansionContext);
        } finally {
            if (inputChannel != null) {
                try {
                    inputChannel.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    @Override
    public void expandKeywords(final byte[] inputBuffer, KeywordExpansionContext keywordExpansionContext) throws java.io.IOException, QVCSException {
        RevisionHeader revisionHeader = keywordExpansionContext.getLogfileInfo().getRevisionInformation().getRevisionHeader(keywordExpansionContext.getRevisionIndex());
        if (revisionHeader == null) {
            throw new QVCSException("Revision index: " + keywordExpansionContext.getRevisionIndex() + " does not exist!");
        }

        expandKeywordsToStream(inputBuffer, revisionHeader, keywordExpansionContext);
    }

    private void expandKeywordsToStream(byte[] inputBuffer, RevisionHeader revisionHeader, KeywordExpansionContext keywordExpansionContext) throws java.io.IOException {
        OutputStream outStream = keywordExpansionContext.getOutStream();
        int arraySize = inputBuffer.length;
        int index;
        int startIndex;
        int count;

        for (index = 0, startIndex = 0, count = 1; index < arraySize; index++, count++) {
            // Check for the keyword prefix character
            if (inputBuffer[index] == keywordPrefix) {
                // Write what we've looked at so far (including the keyword prefix)
                outStream.write(inputBuffer, startIndex, count);
                count = 0;

                if (expandAKeyword(inputBuffer, index, revisionHeader, keywordExpansionContext)) {
                    // We found a keyword.  expandAKeyword expanded it for us,
                    // and wrote it out.  Look for the terminating character so
                    // we can start copying the rest of the file from there.
                    do {
                        index++;
                    } while (inputBuffer[index] != keywordPrefix);
                }
                startIndex = index + 1;
            }
        }

        // Write out what's left.
        if (startIndex < arraySize) {
            outStream.write(inputBuffer, startIndex, arraySize - startIndex);
        }
    }

    private boolean expandAKeyword(byte[] inputBuffer, int index, RevisionHeader revisionHeader, KeywordExpansionContext keywordExpansionContext) throws java.io.IOException {
        LogfileInfo logfileInfo = keywordExpansionContext.getLogfileInfo();
        OutputStream outStream = keywordExpansionContext.getOutStream();
        int revisionIndex = keywordExpansionContext.getRevisionIndex();
        File outputFile = keywordExpansionContext.getOutputFile();
        String appendedPath = keywordExpansionContext.getAppendedPath();
        String labelString = keywordExpansionContext.getLabelString();
        AbstractProjectProperties projectProperties = keywordExpansionContext.getProjectProperties();

        keywordExpansionContext.setBinaryFileFlag(false);
        boolean retVal = false;
        byte searchTerminator = keywordPrefix;
        int useIndex = index + 1;
        LogFileHeaderInfo headerInfo = logfileInfo.getLogFileHeaderInfo();
        AtomicInteger revisionCountForLogX = new AtomicInteger(1);

        // Make some adjustments for binary files.
        if (headerInfo.getLogFileHeader().attributes().getIsBinaryfile()) {
            keywordExpansionContext.setBinaryFileFlag(true);
            searchTerminator = terminatorByte;
        }

        // We are positioned at the keyword candidate... See if it's a keyword we know.
        if (isKeyword(inputBuffer, useIndex, authorKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            AccessList modifierList = new AccessList(headerInfo.getModifierList());
            String author;
            if (keywordExpansionContext.getBinaryFileFlag()) {
                author = "Bogus Author";
            } else {
                author = modifierList.indexToUser(revisionHeader.getCreatorIndex());
            }
            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(authorKeyword, author, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(authorKeyword, author, outStream);
            }
            retVal = true;
        } else if (isCopyrightKeyword(inputBuffer, useIndex, copyrightKeyword, keywordExpansionContext)) {
            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryCopyrightKeyword(copyrightKeyword, revisionIndex, copyrightMessage, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeCopyrightKeyword(copyrightKeyword, revisionIndex, logfileInfo, copyrightMessage, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, dateKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String dateTime;
            if (keywordExpansionContext.getBinaryFileFlag()) {
                dateTime = "Bogus DateTime";
            } else {
                dateTime = dateFormat.format(revisionHeader.getCheckInDate()) + " "
                        + timeFormat.format(revisionHeader.getCheckInDate());
            }
            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(dateKeyword, dateTime, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(dateKeyword, dateTime, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, filenameKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String fileName = outputFile.getCanonicalPath();
            fileName = formatFilename(fileName);
            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(filenameKeyword, fileName, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(filenameKeyword, fileName, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, filePathKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String fileName;
            if (appendedPath.length() > 0) {
                fileName = appendedPath + "/" + logfileInfo.getShortWorkfileName();
            } else {
                fileName = logfileInfo.getShortWorkfileName();
            }
            fileName = formatFilename(fileName);
            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(filePathKeyword, fileName, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(filePathKeyword, fileName, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, headerKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            retVal = expandHeaderKeyword(revisionHeader, headerInfo, inputBuffer, useIndex, keywordExpansionContext);
        } else if (isKeyword(inputBuffer, useIndex, headerPathKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            retVal = expandHeaderPathKeyword(revisionHeader, headerInfo, inputBuffer, useIndex, keywordExpansionContext);
        } else if (isKeyword(inputBuffer, useIndex, logKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            if (!keywordExpansionContext.getBinaryFileFlag()) {
                writeLog(logfileInfo, revisionIndex, outStream, appendedPath, projectProperties, -1);
                retVal = true;
            }
        } else if (isLogXKeyword(inputBuffer, useIndex, logKeyword, revisionCountForLogX, searchTerminator)) {
            if (!keywordExpansionContext.getBinaryFileFlag()) {
                writeLog(logfileInfo, revisionIndex, outStream, appendedPath, projectProperties, revisionCountForLogX.get());
                retVal = true;
            }
        } else if (isKeyword(inputBuffer, useIndex, logfileKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String logfileName;
            if (keywordExpansionContext.getBinaryFileFlag()) {
                logfileName = "Bogus Logfilename";
            } else {
                logfileName = formatArchiveFilename(logfileInfo, appendedPath, projectProperties);
            }
            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(logfileKeyword, logfileName, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(logfileKeyword, logfileName, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, ownerKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String owner;
            if (keywordExpansionContext.getBinaryFileFlag()) {
                owner = "Bogus owner";
            } else {
                owner = headerInfo.getOwner();
            }
            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(ownerKeyword, owner, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(ownerKeyword, owner, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, revisionKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String revision;
            if (keywordExpansionContext.getBinaryFileFlag()) {
                revision = "Bogus revision";
            } else {
                revision = revisionHeader.getRevisionString();
            }
            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(revisionKeyword, revision, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(revisionKeyword, revision, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, versionKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String useLabelString;
            if (keywordExpansionContext.getBinaryFileFlag()) {
                useLabelString = "Bogus version";
            } else {
                LabelInfo[] labelInfo = headerInfo.getLabelInfo();
                useLabelString = deduceLabelString(labelInfo, revisionHeader, labelString);
            }

            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(versionKeyword, useLabelString, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(versionKeyword, useLabelString, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, labelKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String useLabelString;
            if (keywordExpansionContext.getBinaryFileFlag()) {
                useLabelString = "Bogus label";
            } else {
                LabelInfo[] labelInfo = headerInfo.getLabelInfo();
                useLabelString = deduceLabelString(labelInfo, revisionHeader, labelString);
            }

            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(labelKeyword, useLabelString, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(labelKeyword, useLabelString, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, projectKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String projectString;
            if (keywordExpansionContext.getBinaryFileFlag()) {
                projectString = "Bogus Project";
            } else {
                projectString = projectProperties.getProjectName();
            }

            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(projectKeyword, projectString, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(projectKeyword, projectString, outStream);
            }
            retVal = true;
        } else if (isKeyword(inputBuffer, useIndex, verKeyword, searchTerminator, keywordExpansionContext.getBinaryFileFlag())) {
            String verKeywordValue;
            if (keywordExpansionContext.getBinaryFileFlag()) {
                verKeywordValue = "Bogus VER";
            } else {
                String useLabelString;
                LabelInfo[] labelInfo = headerInfo.getLabelInfo();
                String fileNamePrefix = outputFile.getName();
                fileNamePrefix = fileNamePrefix.substring(0, fileNamePrefix.indexOf('.'));
                useLabelString = deduceLabelString(labelInfo, revisionHeader, labelString);
                verKeywordValue = fileNamePrefix + " " + useLabelString;
            }

            if (keywordExpansionContext.getBinaryFileFlag()) {
                writeBinaryKeyword(verKeyword, verKeywordValue, outStream, inputBuffer, useIndex, keywordExpansionContext);
            } else {
                writeKeyword(verKeyword, verKeywordValue, outStream);
            }
            retVal = true;
        }
        return retVal;
    }

    private boolean isKeyword(byte[] inputBuffer, int index, String keyword, byte searchTerminator, boolean binaryFileFlag) {
        boolean retVal;

        // It can only be a keyword if there is room in the input buffer for it.
        if (index + keyword.length() > inputBuffer.length) {
            retVal = false;
        } else {
            retVal = false;
            String candidateString = new String(inputBuffer, index, keyword.length());

            if (candidateString.compareTo(keyword) == 0) {
                // Make sure the following byte is the expected terminator.
                if (inputBuffer[index + keyword.length()] == searchTerminator) {
                    if (binaryFileFlag) {
                        retVal = isExpandedKeyword(inputBuffer, index, keyword, new AtomicInteger());
                    } else {
                        retVal = true;
                    }
                }
            }
        }

        return retVal;
    }

    private boolean isCopyrightKeyword(byte[] inputBuffer, int index, String keyword, KeywordExpansionContext keywordExpansionContext) {
        boolean retVal;

        // It can only be a keyword if there is room in the input buffer for it.
        if (index + keyword.length() > inputBuffer.length) {
            retVal = false;
        } else {
            retVal = false;
            String candidateString = new String(inputBuffer, index, keyword.length());

            if (candidateString.compareTo(keyword) == 0) {
                if (keywordExpansionContext.getBinaryFileFlag()) {
                    // Make sure the following byte is the expected terminator.
                    if (inputBuffer[index + keyword.length()] == (byte) ' ') {
                        retVal = isExpandedCopyrightKeyword(inputBuffer, index, keyword, new AtomicInteger());
                    }
                } else {
                    // Make sure the following byte is the expected terminator.
                    if (inputBuffer[index + keyword.length()] == keywordPrefix) {
                        retVal = true;
                    }
                }
            }
        }

        return retVal;
    }

    private boolean isLogXKeyword(byte[] inputBuffer, int index, String keyword, AtomicInteger revisionCount, byte searchTerminator) {
        boolean retVal;

        // It can only be a keyword if there is room in the input buffer for it.
        if (index + keyword.length() > inputBuffer.length) {
            retVal = false;
        } else {
            retVal = false;
            String candidateString = new String(inputBuffer, index, keyword.length());

            if (candidateString.compareTo(keyword) == 0) {
                // Make sure the following byte(s) are numbers followed by the
                // terminator character.
                int numberIndex = index + keyword.length();
                int startNumberIndex = numberIndex;
                int bufferSize = inputBuffer.length;
                byte contents = inputBuffer[numberIndex];
                while (numberIndex < bufferSize) {
                    contents = inputBuffer[numberIndex++];
                    if ((contents < '0') || (contents > '9')) {
                        break;
                    }
                }
                if (contents == searchTerminator) {
                    retVal = true;
                    try {
                        String revisionCountString = new String(inputBuffer, startNumberIndex, numberIndex - startNumberIndex - 1);
                        revisionCount.set(Integer.parseInt(revisionCountString));
                    } catch (NumberFormatException e) {
                        LOGGER.warn(e.getLocalizedMessage(), e);
                        revisionCount.set(1);
                    }
                }
            }
        }

        return retVal;
    }

    private String formatArchiveFilename(LogfileInfo logfileInfo, String appendedPath, AbstractProjectProperties projectProperties) {
        String retVal;
        String temp = QVCSConstants.QVCS_REMOTE_PROJECT_TYPE + "//" + projectProperties.getProjectName() + "//" + appendedPath + File.separator
                + logfileInfo.getShortWorkfileName();
        retVal = convertToCanonicalFormat(temp, projectProperties);
        return retVal;
    }

    private String formatFilename(String fileName) {
        String formattedFileName = fileName;
        if (useUnixPathSeparatorFlag) {
            byte[] fileNameBytes = fileName.getBytes();
            for (int i = 0; i < fileNameBytes.length; i++) {
                if (fileNameBytes[i] == '\\') {
                    fileNameBytes[i] = '/';
                }
            }
            formattedFileName = new String(fileNameBytes);
        }
        return formattedFileName;
    }

    private String convertToCanonicalFormat(String inputName, AbstractProjectProperties projectProperties) {
        String retVal = inputName;
        if (projectProperties.isRemoteProject() || useUnixPathSeparatorFlag) {
            byte[] bytes = inputName.getBytes();
            for (int i = 0; i < bytes.length; i++) {
                if (bytes[i] == '\\') {
                    bytes[i] = '/';
                }
            }
            retVal = new String(bytes);
        }
        return retVal;
    }

    private String deduceLabelString(LabelInfo[] labelInfos, RevisionHeader revisionHeader, String inputLabelString) {
        String labelString = noLabelLabel;
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
                            labelString = multipleLabels;
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

    private void writeKeyword(String keyword, String keywordInsertionValue, final OutputStream outStream) throws java.io.IOException {
        outStream.write(keyword.getBytes());        // write the keyword
        outStream.write(terminatorByte);            // write the ':' byte
        outStream.write(' ');                       // write a space
        outStream.write(keywordInsertionValue.getBytes());
        outStream.write(' ');                       // write a space
        outStream.write(keywordPrefix);             // write the trailing '$'
    }

    private void writeBinaryKeyword(String keyword, String keywordInsertionValue, final OutputStream outStream, byte[] inputBuffer, int index,
                                    KeywordExpansionContext keywordExpansionContext) throws java.io.IOException {
        // Find the trailing '$' character.  Note that we are guaranteed that
        // the trailing '$' character exists because the isKeyword method makes
        // that guarantee for binary files.
        int i;
        for (i = index + keyword.length(); i < inputBuffer.length; i++) {
            if (inputBuffer[i] == keywordPrefix) {
                break;
            }
        }

        // Figure out how many characters we can write.
        int bytesAvailableToWrite = i - index + 1;
        byte[] bytesToWrite = new byte[bytesAvailableToWrite];

        // Fill this up with space characters.
        for (int j = 0; j < bytesAvailableToWrite; j++) {
            bytesToWrite[j] = ' ';
        }

        // Copy the keyword into the array.
        byte[] keywordBytes = keyword.getBytes();
        for (i = 0; i < keywordBytes.length; i++) {
            bytesToWrite[i] = keywordBytes[i];
        }

        // Put the ':' and ' ' after the keyword.
        bytesToWrite[i++] = terminatorByte;
        bytesToWrite[i++] = ' ';

        if (!keywordExpansionContext.getBinaryFileFlag()) {
            // Copy the insertion value into this array.
            byte[] insertionBytes = keywordInsertionValue.getBytes();
            for (int j = 0; i < bytesAvailableToWrite && j < keywordInsertionValue.length(); j++, i++) {
                bytesToWrite[i] = insertionBytes[j];
            }
        }

        // The last two bytes are ALWAYS space followed by the '$' character.
        bytesToWrite[bytesAvailableToWrite - 2] = ' ';
        bytesToWrite[bytesAvailableToWrite - 1] = '$';

        // And finally write this puppy out.
        outStream.write(bytesToWrite);
    }

    private void writeCopyrightKeyword(String keyword, int revisionIndex, LogfileInfo logfileInfo, String keywordInsertionValue,
                                                                                                   final OutputStream outStream) throws java.io.IOException {
        outStream.write(keyword.getBytes());        // write the keyword
        outStream.write(' ');                       // write a space
        outStream.write(deduceCopyrightYears(revisionIndex, logfileInfo).getBytes());
        outStream.write(' ');                       // write a space
        outStream.write(keywordInsertionValue.getBytes());
        outStream.write(' ');                       // write a space
        outStream.write(keywordPrefix);             // write the trailing '$'
    }

    private void writeBinaryCopyrightKeyword(String keyword, int revisionIndex, String keywordInsertionValue,
                                             byte[] inputBuffer, int index, KeywordExpansionContext keywordExpansionContext) throws java.io.IOException {
        LogfileInfo logfileInfo = keywordExpansionContext.getLogfileInfo();
        OutputStream outStream = keywordExpansionContext.getOutStream();
        // We have to use byte arrays in order to get the copyright
        // character to work correctly.  Otherwise, we could just just StringBuffer
        // to do the concatenation.
        String copyrightInsertionValue;
        if (keywordExpansionContext.getBinaryFileFlag()) {
            copyrightInsertionValue = "Bogus Copyright";
        } else {
            String copyrightYears = deduceCopyrightYears(revisionIndex, logfileInfo);
            byte[] insertionBytes = new byte[RESERVE_SPACE_FOR_BINARY_COPYRIGHT + copyrightYears.length() + keywordInsertionValue.length()];

            int i = 0;
            insertionBytes[i++] = ' ';
            insertionBytes[i++] = ' ';
            insertionBytes[i++] = ' ';

            byte[] copyrightYearBytes = copyrightYears.getBytes();
            for (int j = 0; j < copyrightYears.length(); i++, j++) {
                insertionBytes[i] = copyrightYearBytes[j];
            }
            insertionBytes[i++] = ' ';

            byte[] keywordBytes = keywordInsertionValue.getBytes();
            for (int j = 0; j < keywordInsertionValue.length(); i++, j++) {
                insertionBytes[i] = keywordBytes[j];
            }

            copyrightInsertionValue = new String(insertionBytes);
        }

        // Find the trailing '$' character.  Note that we are guaranteed that
        // the trailing '$' character exists because the isKeyword method makes
        // that guarantee for binary files.
        int i;
        for (i = index + keyword.length(); i < inputBuffer.length; i++) {
            if (inputBuffer[i] == keywordPrefix) {
                break;
            }
        }

        // Figure out how many characters we can write.
        int bytesAvailableToWrite = i - index + 1;
        byte[] bytesToWrite = new byte[bytesAvailableToWrite];

        // Fill this up with space characters.
        for (int j = 0; j < bytesAvailableToWrite; j++) {
            bytesToWrite[j] = ' ';
        }

        // Copy the keyword into the array.
        byte[] keywordBytes = keyword.getBytes();
        for (i = 0; i < keywordBytes.length; i++) {
            bytesToWrite[i] = keywordBytes[i];
        }

        // Put the ':' and ' ' after the keyword.
        bytesToWrite[i++] = ' ';
        bytesToWrite[i++] = ' ';
        bytesToWrite[i++] = ' ';

        if (!keywordExpansionContext.getBinaryFileFlag()) {
            // Copy the insertion value into this array.
            byte[] insertionBytes = copyrightInsertionValue.getBytes();
            for (int j = 0; i < bytesAvailableToWrite && j < copyrightInsertionValue.length(); j++, i++) {
                bytesToWrite[i] = insertionBytes[j];
            }
        }

        // The last two bytes are ALWAYS space followed by the '$' character.
        bytesToWrite[bytesAvailableToWrite - 2] = ' ';
        bytesToWrite[bytesAvailableToWrite - 1] = '$';

        // And finally write this puppy out.
        outStream.write(bytesToWrite);
    }

    private void writeLog(LogfileInfo logfileInfo,
                          int revisionIndex,
                          final OutputStream outStream,
                          String appendedPath,
                          AbstractProjectProperties projectProperties,
                          int revisionCountForLogX) throws java.io.IOException {
        byte[] commentPrefix = logfileInfo.getLogFileHeaderInfo().getCommentPrefix().getBytes();
        int useRevisionCountForLogX = revisionCountForLogX;

        // Write the header portion of the log;
        outStream.write(logKeyword.getBytes());         // write the keyword
        if (revisionCountForLogX > 0) {
            outStream.write(Integer.toString(revisionCountForLogX).getBytes());
        }
        outStream.write(terminatorByte);                // write the ':' byte
        outStream.write(' ');                           // write a space
        outStream.write(formatArchiveFilename(logfileInfo, appendedPath, projectProperties).getBytes());
        outStream.write(' ');                           // write a space
        outStream.write(keywordPrefix);                 // write the trailing '$'
        outStream.write(eol);
        outStream.write(commentPrefix);
        outStream.write(eol);
        writeDescriptionString(commentPrefix, outStream, logfileInfo.getLogFileHeaderInfo().getModuleDescription());

        if (revisionCountForLogX < 0) {
            useRevisionCountForLogX = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
        } else {
            if (revisionCountForLogX > logfileInfo.getLogFileHeaderInfo().getRevisionCount()) {
                useRevisionCountForLogX = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
            }
        }


        int[] revIndexes = deduceRevisionsToShow(revisionIndex, logfileInfo);

        outStream.write(commentPrefix);
        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();

        for (int i = 0; i < useRevisionCountForLogX && i < revIndexes.length; i++) {
            writeRevisionInfo(logfileInfo, revisionInformation.getRevisionHeader(revIndexes[i]), outStream);
        }
        outStream.write(eol);
        outStream.write(commentPrefix);
        outStream.write(keywordPrefix);
        outStream.write(endlogKeyword.getBytes());
        outStream.write(keywordPrefix);
    }

    private void writeRevisionInfo(LogfileInfo logfileInfo, RevisionHeader revisionHeader, final OutputStream outStream) throws java.io.IOException {
        byte[] commentPrefix = logfileInfo.getLogFileHeaderInfo().getCommentPrefix().getBytes();
        AccessList modifierList = new AccessList(logfileInfo.getLogFileHeaderInfo().getModifierList());

        outStream.write(eol);
        outStream.write(commentPrefix);
        outStream.write(revisionKeyword.getBytes());
        outStream.write(' ');
        outStream.write(revisionHeader.getRevisionString().getBytes());
        outStream.write(' ');
        outStream.write(authorKeyword.getBytes());
        outStream.write(terminatorByte);          // write the ':' byte
        outStream.write(' ');
        outStream.write(modifierList.indexToUser(revisionHeader.getCreatorIndex()).getBytes());
        outStream.write(' ');
        outStream.write(dateKeyword.getBytes());
        outStream.write(terminatorByte);          // write the ':' byte
        outStream.write(' ');
        String dateTime = dateFormat.format(revisionHeader.getCheckInDate()) + " "
                + timeFormat.format(revisionHeader.getCheckInDate());
        outStream.write(dateTime.getBytes());
        outStream.write(eol);
        writeDescriptionString(commentPrefix, outStream, revisionHeader.getRevisionDescription());
        outStream.write(commentPrefix);
    }

    private String deduceCopyrightYears(int revisionIndex, LogfileInfo logfileInfo) throws java.io.IOException {
        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
        RevisionHeader revisionHeader = revisionInformation.getRevisionHeader(revisionIndex);
        int revisionCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();

        long newestRevisionSeconds = Long.MIN_VALUE;
        long oldestRevisionSeconds = Long.MAX_VALUE;

        String copyrightYears;

        // The idea is to walk the set of revisions looking for those that are included
        // given that the newest revision is the one passed in.
        if (revisionHeader.getDepth() == 0) {
            // We are dealing with the tip revision.  This is the easy case.
            for (int i = revisionIndex; i < revisionCount; i++) {
                RevisionHeader currentRevision = revisionInformation.getRevisionHeader(i);
                if (currentRevision.getDepth() == 0) {
                    long revisionCheckInTime = currentRevision.getCheckInDate().getTime();
                    if (revisionCheckInTime > newestRevisionSeconds) {
                        newestRevisionSeconds = revisionCheckInTime;
                    }
                    if (revisionCheckInTime < oldestRevisionSeconds) {
                        oldestRevisionSeconds = revisionCheckInTime;
                    }
                }
            }
        } else {
            // For branch revisions, we need to look through all revisions,
            // since some of the revisions we show may be located at a lower
            // revision index than the one we are expanding the log for.
            for (int i = 0; i < revisionCount; i++) {
                RevisionHeader currentRevision = revisionInformation.getRevisionHeader(i);
                if (isAncestor(currentRevision, revisionHeader)) {
                    long revisionCheckInTime = currentRevision.getCheckInDate().getTime();
                    if (revisionCheckInTime > newestRevisionSeconds) {
                        newestRevisionSeconds = revisionCheckInTime;
                    }
                    if (revisionCheckInTime < oldestRevisionSeconds) {
                        oldestRevisionSeconds = revisionCheckInTime;
                    }
                }
            }
        }

        // Get the respective years
        Date oldestDate = new Date(oldestRevisionSeconds);
        FieldPosition oldestDateFieldPosition = new FieldPosition(DateFormat.YEAR_FIELD);
        StringBuffer oldestDateBuffer = new StringBuffer();
        oldestDateBuffer = copyrightYearFormat.format(oldestDate, oldestDateBuffer, oldestDateFieldPosition);
        String oldestYear = oldestDateBuffer.substring(oldestDateFieldPosition.getBeginIndex(), oldestDateFieldPosition.getEndIndex());

        Date newestDate = new Date(newestRevisionSeconds);
        FieldPosition newestDateFieldPosition = new FieldPosition(DateFormat.YEAR_FIELD);
        StringBuffer newestDateBuffer = new StringBuffer();
        newestDateBuffer = copyrightYearFormat.format(newestDate, newestDateBuffer, newestDateFieldPosition);
        String newestYear = newestDateBuffer.substring(newestDateFieldPosition.getBeginIndex(), newestDateFieldPosition.getEndIndex());

        if (0 == oldestYear.compareTo(newestYear)) {
            copyrightYears = oldestYear;
        } else {
            copyrightYears = oldestYear + "-" + newestYear;
        }
        return copyrightYears;
    }

    private int[] deduceRevisionsToShow(int revisionIndex, LogfileInfo logfileInfo) throws java.io.IOException {
        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
        RevisionHeader revisionHeader = revisionInformation.getRevisionHeader(revisionIndex);
        int revisionCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
        Map<String, Integer> revisions = Collections.synchronizedMap(new TreeMap<String, Integer>());

        // The idea is to walk the set of revisions looking for those that need to be displayed,
        // given that the newest revision is the one passed in.
        if (revisionHeader.getDepth() == 0) {
            // We are dealing with the tip revision.  This is the easy case.
            for (int i = revisionIndex; i < revisionCount; i++) {
                RevisionHeader currentRevision = revisionInformation.getRevisionHeader(i);
                if (currentRevision.getDepth() == 0) {
                    revisions.put(currentRevision.getRevisionDescriptor().toString(), i);
                }
            }
        } else {
            // For branch revisions, we need to look through all revisions,
            // since some of the revisions we show may be located at a lower
            // revision index than the one we are expanding the log for.
            for (int i = 0; i < revisionCount; i++) {
                RevisionHeader currentRevision = revisionInformation.getRevisionHeader(i);
                if (isAncestor(currentRevision, revisionHeader)) {
                    revisions.put(currentRevision.getRevisionDescriptor().toString(), i);
                }
            }
        }

        // Bundle the result in a nice package.
        int[] returnedIndexes = new int[revisions.size()];
        int maxIndex = revisions.size() - 1;
        Iterator<Integer> iterator = revisions.values().iterator();
        for (int i = 0; iterator.hasNext(); i++) {
            Integer integerIndex = iterator.next();
            returnedIndexes[maxIndex - i] = integerIndex;
        }
        return returnedIndexes;
    }

    private boolean isAncestor(RevisionHeader ancestorCandidate, RevisionHeader decendantRevision) {
        boolean retVal;

        if (ancestorCandidate.getDepth() > decendantRevision.getDepth()) {
            // There is no way for a revision that is deeper than the decendant
            // to be an ancestor of that decendant.
            retVal = false;
        } else if (ancestorCandidate.getDepth() == decendantRevision.getDepth()) {
            // If the depths are equal, then all elements except the final minor
            // number must match; the final minor number must be less than the
            // minor number of the decendant.
            int i;
            retVal = true;
            for (i = 0; retVal && (i < ancestorCandidate.getDepth()); i++) {
                MajorMinorRevisionPair ancestorPair = ancestorCandidate.getRevisionDescriptor().getRevisionPairs()[i];
                MajorMinorRevisionPair decendantPair = decendantRevision.getRevisionDescriptor().getRevisionPairs()[i];
                if (ancestorPair.getMajorNumber() == decendantPair.getMajorNumber()
                        && ancestorPair.getMinorNumber() == decendantPair.getMinorNumber()) {
                    continue;
                } else {
                    retVal = false;
                }
            }

            // Look at the last pair...
            if (retVal) {
                MajorMinorRevisionPair ancestorPair = ancestorCandidate.getRevisionDescriptor().getRevisionPairs()[i];
                MajorMinorRevisionPair decendantPair = decendantRevision.getRevisionDescriptor().getRevisionPairs()[i];
                if (ancestorPair.getMajorNumber() == decendantPair.getMajorNumber()) {
                    if (ancestorPair.getMinorNumber() > decendantPair.getMinorNumber()) {
                        retVal = false;
                    }
                } else {
                    retVal = false;
                }
            }
        } else {
            // If the candidate depth is less than the decendant, then
            // all the major/minor pairs of the candidate must match the
            // decendant's.
            int i;
            retVal = true;
            for (i = ancestorCandidate.getDepth(); retVal && (i > 0); i--) {
                MajorMinorRevisionPair ancestorPair = ancestorCandidate.getRevisionDescriptor().getRevisionPairs()[i];
                MajorMinorRevisionPair decendantPair = decendantRevision.getRevisionDescriptor().getRevisionPairs()[i];
                if (ancestorPair.getMajorNumber() == decendantPair.getMajorNumber()
                        && ancestorPair.getMinorNumber() == decendantPair.getMinorNumber()) {
                    continue;
                } else {
                    retVal = false;
                }
            }
            // For trunk revisions, the major number must be <=, and the minor number must be lower
            if (retVal) {
                MajorMinorRevisionPair ancestorPair = ancestorCandidate.getRevisionDescriptor().getRevisionPairs()[0];
                MajorMinorRevisionPair decendantPair = decendantRevision.getRevisionDescriptor().getRevisionPairs()[0];
                if (ancestorPair.getMajorNumber() > decendantPair.getMajorNumber()) {
                    retVal = false;
                } else {
                    if (ancestorPair.getMinorNumber() > decendantPair.getMinorNumber()) {
                        retVal = false;
                    }
                }
            }
        }
        return retVal;
    }

    private void writeDescriptionString(byte[] commentPrefix, final OutputStream outStream, String description) throws java.io.IOException {
        byte[] descriptionBuffer = description.getBytes();

        // First convert any newline characters to spaces so we can put
        // word wrap in whatever column the property file says.
        for (int i = 0; i < descriptionBuffer.length; i++) {
            if (descriptionBuffer[i] == '\n') {
                descriptionBuffer[i] = ' ';
            }
        }

        // Figure out where to word wrap.
        int column = 0;
        int preceedingSpaceIndex = 0;
        for (int i = 0; i < descriptionBuffer.length; i++, column++) {
            if (descriptionBuffer[i] == ' ') {
                if ((preceedingSpaceIndex > 0) && (column > wordWrapColumn)) {
                    descriptionBuffer[preceedingSpaceIndex] = '\n';
                    column = 0;
                }
                preceedingSpaceIndex = i;
            }
        }

        // Write the description to the stream, each line preceeded by the
        // comment prefix.
        int startingIndex = 0;
        for (int i = 0; i < descriptionBuffer.length; i++, column++) {
            if (descriptionBuffer[i] == '\n') {
                outStream.write(commentPrefix);
                outStream.write(spaces);
                outStream.write(descriptionBuffer, startingIndex, i - startingIndex);
                outStream.write(eol);
                startingIndex = ++i;
            }
        }

        // Write out any remaining description.
        if (startingIndex < descriptionBuffer.length) {
            outStream.write(commentPrefix);
            outStream.write(spaces);
            outStream.write(descriptionBuffer, startingIndex, descriptionBuffer.length - startingIndex);
            outStream.write(eol);
        }
    }

    @Override
    public void contractKeywords(final FileInputStream inStream, final OutputStream outStream, AtomicReference<String> checkInComment,
                                 AbstractProjectProperties projectProperties, boolean binaryFileFlag) throws java.io.IOException {

        // Read the entire inStream into a buffer...
        FileChannel inputChannel = null;
        try {
            // We use a channel to figure out the size of the input stream.
            inputChannel = inStream.getChannel();
            long size = inputChannel.size();

            // Read the stream in the vanilla way.
            byte[] inputBuffer = new byte[(int) size];
            inStream.read(inputBuffer);

            contractKeywordsToStream(inputBuffer, outStream, checkInComment, binaryFileFlag);
        } finally {
            if (inputChannel != null) {
                try {
                    inputChannel.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private void contractKeywordsToStream(byte[] inputBuffer, final OutputStream outStream, AtomicReference<String> checkInComment,
                                                                                            boolean binaryFileFlag) throws java.io.IOException {
        int arraySize = inputBuffer.length;
        int index;
        int startIndex;
        int count;
        AtomicInteger continueIndex = new AtomicInteger();

        for (index = 0, startIndex = 0, count = 0; index < arraySize; index++, count++) {
            // Check for the keyword prefix character
            if (inputBuffer[index] == keywordPrefix) {
                // Write what we've looked at so far (including the keyword prefix)
                outStream.write(inputBuffer, startIndex, count);
                count = 0;

                if (contractAKeyword(inputBuffer, index, outStream, checkInComment, continueIndex, binaryFileFlag)) {
                    // We found a keyword.  contractAKeyword contracted it for us,
                    // and wrote it out.  This is where we continue.
                    index = continueIndex.get();
                }
                startIndex = index;
            }
        }

        // Write out what's left.
        if (startIndex < arraySize) {
            outStream.write(inputBuffer, startIndex, arraySize - startIndex);
        }
    }

    private boolean contractAKeyword(byte[] inputBuffer, int index, final OutputStream outStream, AtomicReference<String> checkInComment,
                                     AtomicInteger continueIndex, boolean binaryFileFlag) throws java.io.IOException {
        boolean retVal = false;
        int useIndex = index + 1;
        AtomicInteger revisionCountForLogX = new AtomicInteger(1);

        // We are positioned at the keyword candidate... See if it's a keyword we know.
        if (isExpandedKeyword(inputBuffer, useIndex, authorKeyword, continueIndex)) {
            writeContractedKeyword(authorKeyword, outStream);
            retVal = true;
        } else if (isExpandedCopyrightKeyword(inputBuffer, useIndex, copyrightKeyword, continueIndex)) {
            writeContractedKeyword(copyrightKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, dateKeyword, continueIndex)) {
            writeContractedKeyword(dateKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, filenameKeyword, continueIndex)) {
            writeContractedKeyword(filenameKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, filePathKeyword, continueIndex)) {
            writeContractedKeyword(filePathKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, headerKeyword, continueIndex)) {
            writeContractedKeyword(headerKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, headerPathKeyword, continueIndex)) {
            writeContractedKeyword(headerPathKeyword, outStream);
            retVal = true;
        } else if (isExpandedLogKeyword(inputBuffer, useIndex, logKeyword, continueIndex, binaryFileFlag)) {
            writeContractedKeyword(logKeyword, outStream);
            retVal = true;
        } else if (isExpandedLogXKeyword(inputBuffer, useIndex, logKeyword, revisionCountForLogX, continueIndex, binaryFileFlag)) {
            writeContractedLogXKeyword(logKeyword, outStream, revisionCountForLogX.get());
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, logfileKeyword, continueIndex)) {
            writeContractedKeyword(logfileKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, ownerKeyword, continueIndex)) {
            writeContractedKeyword(ownerKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, revisionKeyword, continueIndex)) {
            writeContractedKeyword(revisionKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, versionKeyword, continueIndex)) {
            writeContractedKeyword(versionKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, labelKeyword, continueIndex)) {
            writeContractedKeyword(labelKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, projectKeyword, continueIndex)) {
            writeContractedKeyword(projectKeyword, outStream);
            retVal = true;
        } else if (isExpandedKeyword(inputBuffer, useIndex, verKeyword, continueIndex)) {
            writeContractedKeyword(verKeyword, outStream);
            retVal = true;
        } else if (isExpandedCommentKeyword(inputBuffer, useIndex, commentKeyword, checkInComment, continueIndex)) {
            // Note that we don't write anything for this one!!
            retVal = true;
        }
        return retVal;
    }

    private boolean isExpandedKeyword(byte[] inputBuffer, int index, String keyword, AtomicInteger continueIndex) {
        boolean retVal;

        // It can only be a keyword if there is room in the input buffer for it.
        if (index + keyword.length() > inputBuffer.length) {
            retVal = false;
        } else {
            retVal = false;
            String candidateString = new String(inputBuffer, index, keyword.length());

            if (candidateString.compareTo(keyword) == 0) {
                // Make sure the following byte is the expected terminator.
                if ((index + keyword.length() < inputBuffer.length)
                        && inputBuffer[index + keyword.length()] == terminatorByte) {
                    // Great.  Now we need to make sure that we run into a '$'
                    // character before we run into the eol sequence.
                    int tempIndex = index + keyword.length();
                    boolean continueFlag = true;
                    while (continueFlag && (tempIndex < inputBuffer.length)) {
                        if (inputBuffer[tempIndex] == keywordPrefix) {
                            retVal = true;
                            continueIndex.set(tempIndex + 1);
                            break;
                        }
                        boolean matchEOLFlag = true;
                        for (int i = 0; (i < eol.length) && (tempIndex + i < inputBuffer.length); i++) {
                            if (inputBuffer[tempIndex + i] != eol[i]) {
                                matchEOLFlag = false;
                                break;
                            }
                        }
                        if (matchEOLFlag) {
                            continueFlag = false;
                        }
                        tempIndex++;
                    }
                }
            }
        }

        return retVal;
    }

    private boolean isExpandedLogKeyword(byte[] inputBuffer, int index, String keyword, AtomicInteger continueIndex, boolean binaryFileFlag) {
        boolean retVal = false;
        if (isExpandedKeyword(inputBuffer, index, keyword, continueIndex)) {
            // Need to look for the trailing Endlog keyword.
            for (int i = continueIndex.get(); i < inputBuffer.length; i++) {
                // Check for the keyword prefix character
                if (inputBuffer[i] == keywordPrefix) {
                    if (isKeyword(inputBuffer, i + 1, endlogKeyword, keywordPrefix, binaryFileFlag)) {
                        retVal = true;
                        continueIndex.set(i + 2 + endlogKeyword.length());
                        break;
                    }
                }
            }
        }
        return retVal;
    }

    private boolean isExpandedLogXKeyword(byte[] inputBuffer, int index, String keyword, AtomicInteger x, AtomicInteger continueIndex, boolean binaryFileFlag) {
        boolean retVal;

        // It can only be a keyword if there is room in the input buffer for it.
        if (index + keyword.length() > inputBuffer.length) {
            retVal = false;
        } else {
            try {
                retVal = false;
                String candidateString = new String(inputBuffer, index, keyword.length());

                if (candidateString.compareTo(keyword) == 0) {
                    retVal = false;

                    // Look through the buffer for the ':' character.  Assume they are
                    // going to limit their revision count to less than 99999.
                    int i;
                    int bufferIndex = index + keyword.length();
                    for (i = 0; i + bufferIndex < inputBuffer.length && i < MAXIMUM_CHARACTERS_FOR_LOGX_REVISION_COUNT; i++) {
                        if (inputBuffer[i + bufferIndex] == terminatorByte) {
                            retVal = true;
                            break;
                        }
                    }
                    if (retVal) {
                        String revCount = new String(inputBuffer, index + keyword.length(), i);
                        int revCountInt = Integer.parseInt(revCount);
                        x.set(revCountInt);

                        String logXKeyword = keyword + revCount;
                        retVal = isExpandedLogKeyword(inputBuffer, index, logXKeyword, continueIndex, binaryFileFlag);
                    } else {
                        retVal = false;
                    }
                }
            } catch (NumberFormatException e) {
                retVal = false;
            }
        }
        return retVal;
    }

    private boolean isExpandedCopyrightKeyword(byte[] inputBuffer, int index, String keyword, AtomicInteger continueIndex) {
        boolean retVal;

        // It can only be a keyword if there is room in the input buffer for it.
        if (index + keyword.length() > inputBuffer.length) {
            retVal = false;
        } else {
            retVal = false;
            String candidateString = new String(inputBuffer, index, keyword.length());

            if (candidateString.compareTo(keyword) == 0) {
                // Make sure the following bytes are the expected ones.
                if ((index + keyword.length() < inputBuffer.length + 1)
                        && (inputBuffer[index + keyword.length()] == ' ')) {
                    // Great.  Now we need to make sure that we run into a '$'
                    // character before we run into the eol sequence.
                    int tempIndex = index + keyword.length();
                    boolean continueFlag = true;
                    while (continueFlag && (tempIndex < inputBuffer.length)) {
                        if (inputBuffer[tempIndex] == keywordPrefix) {
                            retVal = true;
                            continueIndex.set(tempIndex + 1);
                            break;
                        }
                        boolean matchEOLFlag = true;
                        for (int i = 0; (i < eol.length) && (tempIndex + i < inputBuffer.length); i++) {
                            if (inputBuffer[tempIndex + i] != eol[i]) {
                                matchEOLFlag = false;
                                break;
                            }
                        }
                        if (matchEOLFlag) {
                            continueFlag = false;
                        }
                        tempIndex++;
                    }
                }
            }
        }

        return retVal;
    }

    private boolean isExpandedCommentKeyword(byte[] inputBuffer, int index, String keyword, AtomicReference<String> checkInComment, AtomicInteger continueIndex) {
        boolean retVal = false;
        if (isExpandedKeyword(inputBuffer, index, keyword, continueIndex)) {
            String commentString = new String(inputBuffer, index + keyword.length() + EXTRACT_COMMENT_START_INDEX_OFFSET, continueIndex.get() - index - keyword.length()
                    - EXTRACT_COMMENT_ADJUST_LENGTH);
            String existingComment = checkInComment.get();
            if ((existingComment != null) && (existingComment.length() > 0)) {
                checkInComment.set(existingComment + "; " + commentString);
            } else {
                checkInComment.set(commentString);
            }
            retVal = true;
        }
        return retVal;
    }

    private void writeContractedKeyword(String keyword, final OutputStream outStream) throws java.io.IOException {
        outStream.write(keywordPrefix);           // write the leading '$'
        outStream.write(keyword.getBytes());        // write the keyword
        outStream.write(keywordPrefix);           // write the trailing '$'
    }

    private void writeContractedLogXKeyword(String keyword, final OutputStream outStream, int x) throws java.io.IOException {
        outStream.write(keywordPrefix);           // write the leading '$'
        outStream.write(keyword.getBytes());        // write the keyword
        outStream.write(Integer.toString(x).getBytes());    // write the X
        outStream.write(keywordPrefix);           // write the trailing '$'
    }

    /**
     * Get the version keyword.
     * @return the version keyword.
     */
    public String getVersionKeyword() {
        return versionKeyword;
    }

    private boolean expandHeaderKeyword(RevisionHeader revisionHeader, LogFileHeaderInfo headerInfo,
                                        byte[] inputBuffer, int useIndex, KeywordExpansionContext keywordExpansionContext) throws IOException {
        String dateTime;
        String headerString;
        File outputFile = keywordExpansionContext.getOutputFile();
        String workfileName = outputFile.getName();
        OutputStream outStream = keywordExpansionContext.getOutStream();

        if (keywordExpansionContext.getBinaryFileFlag()) {
            headerString = "Bogus Header String";
        } else {
            dateTime = dateFormat.format(revisionHeader.getCheckInDate()) + " "
                    + timeFormat.format(revisionHeader.getCheckInDate());
            headerString = workfileName + " " + revisionKeyword + ":" + revisionHeader.getRevisionString() + " " + dateTime + " " + headerInfo.getOwner();
        }
        if (keywordExpansionContext.getBinaryFileFlag()) {
            writeBinaryKeyword(headerKeyword, headerString, outStream, inputBuffer, useIndex, keywordExpansionContext);
        } else {
            writeKeyword(headerKeyword, headerString, outStream);
        }
        return true;
    }

    private boolean expandHeaderPathKeyword(RevisionHeader revisionHeader, LogFileHeaderInfo headerInfo,
                                            byte[] inputBuffer, int useIndex, KeywordExpansionContext keywordExpansionContext) throws IOException {

        String dateTime;
        String headerString;
        String workfileName;
        String appendedPath = keywordExpansionContext.getAppendedPath();
        LogfileInfo logfileInfo = keywordExpansionContext.getLogfileInfo();
        OutputStream outStream = keywordExpansionContext.getOutStream();

        if (appendedPath.length() > 0) {
            workfileName = appendedPath + "/" + logfileInfo.getShortWorkfileName();
        } else {
            workfileName = logfileInfo.getShortWorkfileName();
        }
        workfileName = formatFilename(workfileName);

        if (keywordExpansionContext.getBinaryFileFlag()) {
            headerString = "Bogus Header String";
        } else {
            dateTime = dateFormat.format(revisionHeader.getCheckInDate()) + " "
                    + timeFormat.format(revisionHeader.getCheckInDate());
            headerString = workfileName + " " + revisionKeyword + ":" + revisionHeader.getRevisionString() + " " + dateTime + " " + headerInfo.getOwner();
        }
        if (keywordExpansionContext.getBinaryFileFlag()) {
            writeBinaryKeyword(headerPathKeyword, headerString, outStream, inputBuffer, useIndex, keywordExpansionContext);
        } else {
            writeKeyword(headerPathKeyword, headerString, outStream);
        }
        return true;
    }
}

