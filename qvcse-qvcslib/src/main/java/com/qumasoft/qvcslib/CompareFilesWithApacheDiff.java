/*
 * Copyright 2004-2019 JimVoris.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qumasoft.qvcslib;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.jrcs.diff.AddDelta;
import org.apache.commons.jrcs.diff.ChangeDelta;
import org.apache.commons.jrcs.diff.DeleteDelta;
import org.apache.commons.jrcs.diff.Delta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.apache.commons.jrcs.diff.Revision;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Wrap the apache difference algorithm so it produces results that are useful.</p>
 * <p>
 * The CompareFilesWithApacheDiff class implements the QVCSOperation interface. It compares two files and creates an output file that contains a list of the differences between the
 * two files.</p>
 * <p>
 * The argument array consists of just three arguments: the first input filename, the second input filename, and the output filename. The argument array may be set in the
 * constructor to the CompareFiles object, or via a version of the execute command that takes an argument array.</p>
 * <p>
 * If the argument array isn't specified before the execute() method is called, or if the argument array doesn't have the necessary number of elements in it, the object will throw
 * a QVCSCommandException.</p>
 *
 * @see QVCSOperation
 * @author Jim Voris
 */
public class CompareFilesWithApacheDiff implements QVCSOperation {

    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareFilesWithApacheDiff.class);
    private static final String UTF8 = "UTF-8";
    /** The number of arguments that we need. */
    protected static final int COMMAND_ARG_COUNT = 3;

    private String[] args = null;
    private boolean compareAttemptedFlag = false;
    private boolean comparisonResultFlag = false;
    private boolean ignoreAllWhitespaceFlag = false;
    private boolean ignoreLeadingWhitespaceFlag = false;
    private boolean ignoreCaseFlag = false;
    private boolean ignoreEOLChangesFlag = false;

    private File inFileA;
    private File inFileB;
    private File outFile;

    private int file1LineCount = 0;
    private int file2LineCount = 0;

    /**
     * Constructor with arguments defined.
     * @param arguments the arguments that define what we should compare.
     */
    public CompareFilesWithApacheDiff(String[] arguments) {
        this.args = arguments;
    }

    /**
     * Default constructor.
     */
    public CompareFilesWithApacheDiff() {
    }

    /**
     * Get the number of lines in file 1.
     * @return the number of lines in file 1.
     */
    int getFile1LineCount() {
        return this.file1LineCount;
    }

    private void setFile1LineCount(int file1LineCnt) {
        this.file1LineCount = file1LineCnt;
    }

    /**
     * Get the number of lines in file 2.
     * @return the number of lines in file 2.
     */
    int getFile2LineCount() {
        return this.file2LineCount;
    }

    /**
     * Get the arguments.
     * @return the arguments.
     */
    public String[] getArgs() {
        return this.args;
    }

    private void setFile2LineCount(int file2LineCnt) {
        this.file2LineCount = file2LineCnt;
    }

    /**
     * Return true if the two files are equal; false if they are not equal. Throws QVCSCommandException if the comparison hasn't yet been made.
     *
     * @return true if the two files are equal; false otherwise.
     * @throws com.qumasoft.qvcslib.QVCSOperationException Throws this exception if the command arguments haven't yet been supplied.
     */
    public boolean isEqual() throws QVCSOperationException {
        if (compareAttemptedFlag) {
            return comparisonResultFlag;
        } else {
            throw new QVCSOperationException("CompareFiles -- no results available; comparison not yet attempted!");
        }
    }

    /**
     * Get the ignore all white space flag.
     * @return the ignore all white space flag.
     */
    public boolean getIgnoreAllWhiteSpace() {
        return ignoreAllWhitespaceFlag;
    }

    /**
     * Set the ignore all white space flag.
     * @param flag the ignore all white space flag.
     */
    public void setIgnoreAllWhiteSpace(boolean flag) {
        ignoreAllWhitespaceFlag = flag;
    }

    /**
     * Get the ignore leading white space flag.
     * @return the ignore leading white space flag.
     */
    public boolean getIgnoreLeadingWhiteSpace() {
        return ignoreLeadingWhitespaceFlag;
    }

    /**
     * Set the ignore leading white space flag.
     * @param flag the ignore leading white space flag.
     */
    public void setIgnoreLeadingWhiteSpace(boolean flag) {
        ignoreLeadingWhitespaceFlag = flag;
    }

    /**
     * Get the ignore case flag.
     * @return the ignore case flag.
     */
    public boolean getIgnoreCaseFlag() {
        return ignoreCaseFlag;
    }

    /**
     * Set the ignore case flag.
     * @param flag the ignore case flag.
     */
    public void setIgnoreCaseFlag(boolean flag) {
        ignoreCaseFlag = flag;
    }

    /**
     * Get the ignore end-of-line changes flag.
     * @return the ignore end-of-line changes flag.
     */
    public boolean getIgnoreEOLChangesFlag() {
        return ignoreEOLChangesFlag;
    }

    /**
     * Set the ignore end-of-line changes flag.
     * @param flag the ignore end-of-line changes flag.
     */
    public void setIgnoreEOLChangesFlag(boolean flag) {
        ignoreEOLChangesFlag = flag;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute(String[] arguments) throws QVCSOperationException {
        this.args = arguments;
        return execute();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean execute() throws QVCSOperationException {
        boolean returnValue = true;

        if (this.args.length < COMMAND_ARG_COUNT) {
            throw new QVCSOperationException("Compare Files -- invalid arguments!");
        }

        // Make file objects for the files we've been asked to compare
        inFileA = new File(args[0]);
        inFileB = new File(args[1]);
        outFile = new File(args[2]);

        // Make sure we can read/write these as needed
        if (!canReadWriteNecessaryFiles()) {
            returnValue = false;
        }

        // Do the compare
        if (returnValue) {
            setCompareAttempted(true);
            try {
                returnValue = compareFiles();
            } catch (DifferentiationFailedException | IOException | QVCSOperationException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
                outFile.delete();
                returnValue = false;
            }
        }

        return returnValue;
    }

    private boolean canReadWriteNecessaryFiles() {
        boolean returnValue = true;
        try {
            if (!inFileA.canRead()
                    || !inFileB.canRead()
                    || (outFile.exists() && !outFile.canWrite())) {
                returnValue = false;
            }
        } catch (SecurityException e) {
            returnValue = false;
        }
        return returnValue;
    }

    private boolean compareFiles() throws DifferentiationFailedException, IOException, QVCSOperationException {
        CompareLineInfo[] fileA = buildLinesFromFile(inFileA);
        setFile1LineCount(fileA.length);
        CompareLineInfo[] fileB = buildLinesFromFile(inFileB);
        setFile2LineCount(fileB.length);

        Revision apacheRevision = Diff.diff(fileA, fileB);

        if (apacheRevision.size() == 0) {
            // The files are identical.
            comparisonResultFlag = true;
        }

        // Create the edit script that describes the changes we found.
        writeEditScript(apacheRevision, fileA, fileB);
        return true;
    }

    private CompareLineInfo[] buildLinesFromFile(File inFile) throws IOException {
        List<CompareLineInfo> lineInfoList = new ArrayList<>();
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(inFile, "r")) {
            long fileLength = randomAccessFile.length();
            int startOfLineSeekPosition = 0;
            int currentSeekPosition = 0;
            byte character;
            while (currentSeekPosition < fileLength) {
                character = randomAccessFile.readByte();
                currentSeekPosition = (int) randomAccessFile.getFilePointer();
                if (character == '\n') {
                    int endOfLine = (int) randomAccessFile.getFilePointer();
                    byte[] buffer = new byte[endOfLine - startOfLineSeekPosition];
                    randomAccessFile.seek(startOfLineSeekPosition);
                    randomAccessFile.readFully(buffer);
                    CompareLineInfo lineInfo = new CompareLineInfo(startOfLineSeekPosition, createCompareLine(buffer));
                    lineInfoList.add(lineInfo);
                    startOfLineSeekPosition = endOfLine;
                }
            }
            // Add the final line which can happen if it doesn't end in a newline.
            if ((fileLength - startOfLineSeekPosition) > 0L) {
                byte[] buffer = new byte[(int) fileLength - startOfLineSeekPosition];
                randomAccessFile.seek(startOfLineSeekPosition);
                randomAccessFile.readFully(buffer);
                CompareLineInfo lineInfo = new CompareLineInfo(startOfLineSeekPosition, createCompareLine(buffer));
                lineInfoList.add(lineInfo);
            }
        }
        CompareLineInfo[] lineInfoArray = new CompareLineInfo[lineInfoList.size()];
        int i = 0;
        for (CompareLineInfo lineInfo : lineInfoList) {
            lineInfoArray[i++] = lineInfo;
        }
        return lineInfoArray;
    }

    protected void writeEditScript(Revision apacheRevision, CompareLineInfo[] fileA, CompareLineInfo[] fileB) throws QVCSOperationException {
        DataOutputStream outStream = null;
        try {
            outStream = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));

            // Write the header
            CompareFilesEditHeader editHeader = new CompareFilesEditHeader();
            editHeader.setBaseFileSize(new Common32Long((int) inFileA.length()));
            editHeader.setTimeOfTarget(new CommonTime());
            editHeader.write(outStream);

            int count = apacheRevision.size();
            for (int index = 0; index < count; index++) {
                Delta delta = apacheRevision.getDelta(index);
                formatEditScript(delta, outStream, fileA);
            }
        } catch (IOException e) {
            throw new QVCSOperationException("IO Exception in writeEditScript() " + e.getLocalizedMessage());
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private void formatEditScript(Delta delta, DataOutputStream outStream, CompareLineInfo[] fileA) throws QVCSOperationException {
        try {
            short editType;
            int seekPosition = -1;
            int deletedByteCount = 0;
            int insertedByteCount = 0;
            byte[] secondFileByteBuffer = null;
            if (delta instanceof ChangeDelta) {
                CompareLineInfo originalStartingLine = fileA[delta.getOriginal().anchor()];
                seekPosition = originalStartingLine.getLineSeekPosition();
                editType = CompareFilesEditInformation.QVCS_EDIT_REPLACE;
                deletedByteCount = computeDeletedByteCount(delta);
                insertedByteCount = computeInsertedByteCount(delta);
                secondFileByteBuffer = computeSecondFileByteBufferForInsert(delta, insertedByteCount);
            } else if (delta instanceof AddDelta) {
                int anchor = delta.getOriginal().anchor();
                if (anchor == 0) {
                    CompareLineInfo originalStartingLine = fileA[delta.getOriginal().anchor()];
                    seekPosition = originalStartingLine.getLineSeekPosition();
                } else {
                    CompareLineInfo originalStartingLine = fileA[delta.getOriginal().anchor() - 1];
                    byte[] lineAsByteArray = originalStartingLine.getLineBuffer();
                    seekPosition = originalStartingLine.getLineSeekPosition() + lineAsByteArray.length;
                }
                editType = CompareFilesEditInformation.QVCS_EDIT_INSERT;
                insertedByteCount = computeInsertedByteCount(delta);
                AddDelta insertDelta = (AddDelta) delta;
                secondFileByteBuffer = computeSecondFileByteBufferForInsert(insertDelta, insertedByteCount);
            } else if (delta instanceof DeleteDelta) {
                CompareLineInfo originalStartingLine = fileA[delta.getOriginal().anchor()];
                seekPosition = originalStartingLine.getLineSeekPosition();
                editType = CompareFilesEditInformation.QVCS_EDIT_DELETE;
                deletedByteCount = computeDeletedByteCount(delta);
            } else {
                throw new QVCSOperationException("Internal error -- invalid edit type");
            }
            CompareFilesEditInformation editInfo = new CompareFilesEditInformation(editType, seekPosition, deletedByteCount, insertedByteCount);

            switch (editType) {
                case CompareFilesEditInformation.QVCS_EDIT_DELETE:
                    editInfo.write(outStream);
                    break;
                case CompareFilesEditInformation.QVCS_EDIT_INSERT:
                case CompareFilesEditInformation.QVCS_EDIT_REPLACE:
                    editInfo.write(outStream);
                    outStream.write(secondFileByteBuffer, 0, insertedByteCount);
                    break;
                default:
                    throw new QVCSOperationException("Internal error -- invalid edit type");
            }
        } catch (IOException e) {
            throw new QVCSOperationException("IOException in formatEditScript() " + e.getLocalizedMessage());
        }
    }

    private int computeDeletedByteCount(Delta delta) throws UnsupportedEncodingException {
        // This should be the byte count of the original chunk.
        @SuppressWarnings("unchecked")
        List<CompareLineInfo> originalChunk = delta.getOriginal().chunk();
        int seekPosition = originalChunk.get(0).getLineSeekPosition();
        CompareLineInfo lastLine = originalChunk.get(originalChunk.size() - 1);
        int lastLineSeekStart = lastLine.getLineSeekPosition();
        byte[] lastLineAsByteArray = lastLine.getLineBuffer();
        int lastLineEnd = lastLineSeekStart + lastLineAsByteArray.length;
        return lastLineEnd - seekPosition;
    }

    private int computeInsertedByteCount(Delta replaceDelta) throws UnsupportedEncodingException {
        // This should be the byte count of the revised chunk.
        @SuppressWarnings("unchecked")
        List<CompareLineInfo> revisedChunk = replaceDelta.getRevised().chunk();
        int seekPosition = revisedChunk.get(0).getLineSeekPosition();
        CompareLineInfo lastLine = revisedChunk.get(revisedChunk.size() - 1);
        int lastLineSeekStart = lastLine.getLineSeekPosition();
        byte[] lastLineAsByteArray = lastLine.getLineBuffer();
        int lastLineEnd = lastLineSeekStart + lastLineAsByteArray.length;
        return lastLineEnd - seekPosition;
    }

    private byte[] computeSecondFileByteBufferForInsert(Delta delta, int insertedByteCount) throws UnsupportedEncodingException {
        byte[] insertedBytes = new byte[insertedByteCount];
        @SuppressWarnings("unchecked")
        List<CompareLineInfo> insertedChunk = delta.getRevised().chunk();
        int insertionIndex = 0;
        for (CompareLineInfo lineInfo : insertedChunk) {
            byte[] chunkBytes = lineInfo.getLineBuffer();
            for (int i = 0; i < chunkBytes.length; i++) {
                insertedBytes[insertionIndex++] = chunkBytes[i];
            }
        }
        if (insertionIndex != insertedByteCount) {
            throw new QVCSRuntimeException("Error in compare with apache.");
        }
        return insertedBytes;
    }

    /**
     * Set the compare attempted flag.
     * @param flag true if the compare has been attempted; false if not yet attempted.
     */
    public void setCompareAttempted(boolean flag) {
        compareAttemptedFlag = true;
    }

    private byte[] createCompareLine(byte[] line) throws UnsupportedEncodingException {
        String alteredLine = new String(line, UTF8);
        byte[] result = line;
        if (getIgnoreCaseFlag()) {
            String originalLine = new String(line, UTF8);
            alteredLine = originalLine.toUpperCase();
            result = alteredLine.getBytes(UTF8);
        }
        if (getIgnoreEOLChangesFlag()) {
            if (alteredLine.endsWith("\r\n")) {
                alteredLine = alteredLine.substring(0, alteredLine.length() - 2);
                result = alteredLine.getBytes(UTF8);
            } else if (alteredLine.endsWith("\n")) {
                alteredLine = alteredLine.substring(0, alteredLine.length() - 1);
                result = alteredLine.getBytes(UTF8);
            }
        }
        if (getIgnoreAllWhiteSpace()) {
            String[] segments = alteredLine.split("\t| ");
            StringBuilder stringBuilder = new StringBuilder();
            for (String segment : segments) {
                stringBuilder.append(segment);
            }
            alteredLine = stringBuilder.toString();
            result = alteredLine.getBytes(UTF8);
        } else if (getIgnoreLeadingWhiteSpace()) {
            alteredLine = alteredLine.trim();
            result = alteredLine.getBytes(UTF8);
        }
        return result;
    }
}
