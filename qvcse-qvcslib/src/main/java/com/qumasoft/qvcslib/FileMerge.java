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

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements a file level merge. It requires 3 input files, and one output file. The 3 input files consist of a common ancestor file, and two descendents of that common
 * ancestor file. Basically, it diffs descendent 1 against the common ancestor, then diffs descendent 2 against the common ancestor, and then merges the resulting edit scripts
 * produced by those two diffs. It then applies the merged edit script against the common ancestor to produce a merged result. If there are no collisions between the two diffs,
 * then the resulting merged output will contain the merged edits of the two descendents. If there are collisions, then the merge will fail.
 *
 * @author Jim Voris
 */
public final class FileMerge implements QVCSOperation {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(FileMerge.class);
    private String[] args;
    private String baseFileName;
    private String firstDescendentName;
    private String secondDescendentName;
    private String outputFileName;
    private Map<String, EditInfo> editScript = null;
    private long insertedBytesCount = 0L;
    private static final int ARGUMENT_COUNT = 4;
    private static final int BASE_FILENAME_ARG_INDEX = 0;
    private static final int FIRST_DESCENDENT_FILENAME_ARG_INDEX = 1;
    private static final int SECOND_DESCENDENT_FILENAME_ARG_INDEX = 2;
    private static final int OUTPUT_FILENAME_ARG_INDEX = 3;
    private static final int COMPARE_FILES_ARG_COUNT = 3;

    /**
     * Default constructor.
     */
    public FileMerge() {
    }

    /**
     * File merge constructor with arguments.
     * @param arguments a String[] with the arguments: base file name; first descendent file name; second descendent file name; output file name.
     */
    public FileMerge(final String[] arguments) {
        this.args = arguments;
    }

    @Override
    public boolean execute(String[] arguments) throws QVCSOperationException {
        this.args = arguments;
        copyArguments();
        return execute();
    }

    private void copyArguments() {
        baseFileName = args[BASE_FILENAME_ARG_INDEX];
        firstDescendentName = args[FIRST_DESCENDENT_FILENAME_ARG_INDEX];
        secondDescendentName = args[SECOND_DESCENDENT_FILENAME_ARG_INDEX];
        outputFileName = args[OUTPUT_FILENAME_ARG_INDEX];
    }

    /**
     * Merge the two descendent files of the base file into a single output file.
     *
     * @param baseName the common ancestor of the two descendent files.
     * @param firstDescFileName the filename of the first descendent file.
     * @param secondDescFileName the filename of the second descendent file.
     * @param outputName the filename of the output file that we create.
     * @return true if the merge succeeds. false if the merge fails.
     * @throws com.qumasoft.qvcslib.QVCSOperationException for validation or other QVCS related problems.
     */
    public boolean mergeFiles(final String baseName, final String firstDescFileName, final String secondDescFileName, final String outputName)
            throws QVCSOperationException {
        String[] localArgs = new String[ARGUMENT_COUNT];
        localArgs[BASE_FILENAME_ARG_INDEX] = baseName;
        localArgs[FIRST_DESCENDENT_FILENAME_ARG_INDEX] = firstDescFileName;
        localArgs[SECOND_DESCENDENT_FILENAME_ARG_INDEX] = secondDescFileName;
        localArgs[OUTPUT_FILENAME_ARG_INDEX] = outputName;
        return execute(localArgs);
    }

    @Override
    public boolean execute() throws QVCSOperationException {
        boolean retVal;

        try {
            // Make sure we have the arguments that we need.
            validateArguments();

            // Make a new empty merged edit script.
            editScript = new TreeMap<>();

            // Compare the first descendent to the base file.
            String[] firstCompareArgs = new String[COMPARE_FILES_ARG_COUNT];
            firstCompareArgs[0] = baseFileName;
            firstCompareArgs[1] = firstDescendentName;
            File firstCompareOutputTempFile = File.createTempFile("QVCS", ".tmp");
            firstCompareOutputTempFile.deleteOnExit();
            firstCompareArgs[2] = firstCompareOutputTempFile.getCanonicalPath();

            LOGGER.info("Comparing [" + baseFileName + "] to [" + firstDescendentName + "]");
            CompareFilesWithApacheDiff firstCompareFilesOperator = new CompareFilesWithApacheDiff(firstCompareArgs);
            if (!firstCompareFilesOperator.execute()) {
                throw new QVCSOperationException("Failed to compare [" + baseFileName + "] to file revision [" + firstDescendentName + "]");
            }

            // Compare the second descendent to the base file.
            String[] secondCompareArgs = new String[COMPARE_FILES_ARG_COUNT];
            secondCompareArgs[0] = baseFileName;
            secondCompareArgs[1] = secondDescendentName;
            File secondCompareOutputTempFile = File.createTempFile("QVCS", ".tmp");
            secondCompareOutputTempFile.deleteOnExit();
            secondCompareArgs[2] = secondCompareOutputTempFile.getCanonicalPath();

            LOGGER.info("Comparing [" + baseFileName + "] to [" + secondDescendentName + "]");
            CompareFilesWithApacheDiff secondCompareFilesOperator = new CompareFilesWithApacheDiff(secondCompareArgs);
            if (!secondCompareFilesOperator.execute()) {
                throw new QVCSOperationException("Failed to compare [" + baseFileName + "] to file revision [" + secondDescendentName + "]");
            }

            // Merge the two edit scripts into a single merged edit script (contained in
            // the m_EditScript instance variable.
            mergeEditScripts(firstCompareOutputTempFile, secondCompareOutputTempFile);

            // Apply the edits. We'll get here if and only if there are no
            // overlaps.
            applyEdits();
            retVal = true;
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            retVal = false;
        }

        return retVal;
    }

    /**
     * Walk the list of edits and apply them to the the base file.
     */
    private void applyEdits() throws IOException, QVCSOperationException {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        File baseFile = new File(baseFileName);
        byte[] originalData = new byte[(int) baseFile.length()];

        byte[] editedBuffer = new byte[(int) getInsertedBytesCount() + originalData.length]; // It can't be any bigger than this.
        int inIndex = 0;
        int outIndex = 0;
        int deletedBytesCount;
        int localInsertedBytesCount;
        int bytesTillChange = 0;
        EditInfo editInfo = null;

        try {
            fileInputStream = new FileInputStream(baseFile);
            fileInputStream.read(originalData);

            Iterator<EditInfo> it = editScript.values().iterator();
            while (it.hasNext()) {
                editInfo = it.next();
                bytesTillChange = (int) editInfo.getSeekPosition() - inIndex;
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, bytesTillChange);

                inIndex += bytesTillChange;
                outIndex += bytesTillChange;

                deletedBytesCount = (int) editInfo.getDeletedBytesCount();
                localInsertedBytesCount = (int) editInfo.getInsertedBytesCount();

                switch (editInfo.getEditType()) {
                    case CompareFilesEditInformation.QVCS_EDIT_DELETE:
                        /*
                         * Delete input.
                         * Just skip over deleted bytes.
                         */
                        inIndex += deletedBytesCount;
                        break;
                    case CompareFilesEditInformation.QVCS_EDIT_INSERT:
                        /*
                         * Insert edit lines
                         */
                        System.arraycopy(editInfo.getInsertedBytes(), 0, editedBuffer, outIndex, localInsertedBytesCount);
                        outIndex += localInsertedBytesCount;
                        break;
                    case CompareFilesEditInformation.QVCS_EDIT_REPLACE:
                        /*
                         * Replace input line with edit line
                         * First skip over the bytes to be replaced, then copy the replacing bytes from the edit file to the output file.
                         */
                        inIndex += deletedBytesCount;
                        System.arraycopy(editInfo.getInsertedBytes(), 0, editedBuffer, outIndex, localInsertedBytesCount);
                        outIndex += localInsertedBytesCount;
                        break;
                    default:
                        continue;
                }
            }

            // Copy the rest of the input "file" to the output "file".
            int remainingBytes = originalData.length - inIndex;
            if (remainingBytes > 0) {
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, remainingBytes);
                outIndex += remainingBytes;
            }

            // Write the result to the output file.
            File outputFile = new File(outputFileName);
            fileOutputStream = new FileOutputStream(outputFile);
            fileOutputStream.write(editedBuffer, 0, outIndex);
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            if (editInfo != null) {
                LOGGER.warn(" editInfo.seekPosition: " + editInfo.getSeekPosition() + " originalData.length: " + originalData.length + " inIndex: " + inIndex
                        + " editedBuffer.length: "
                        + editedBuffer.length + " outIndex: " + outIndex + " bytesTillChange: " + bytesTillChange);
            }
            throw new QVCSOperationException("Internal error!! Failed to apply edits in FileMerge.");
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            } catch (IOException e) {
                LOGGER.warn(e.getLocalizedMessage(), e);
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    private String getEditRecordType(FileMerge.EditInfo editInfo) {
        String editInfoType;

        switch (editInfo.getEditType()) {
            case CompareFilesEditInformation.QVCS_EDIT_DELETE:
                /*
                * Delete input
                */
                editInfoType = "DELETE";
                break;
            case CompareFilesEditInformation.QVCS_EDIT_INSERT:
                /*
                 * Insert edit lines
                 */
                editInfoType = "INSERT";
                break;
            case CompareFilesEditInformation.QVCS_EDIT_REPLACE:
                /*
                 * Replace input line with edit line
                 */
                editInfoType = "REPLACE";
                break;
            default:
                editInfoType = "UNKNOWN";
                break;
        }

        return editInfoType;
    }

    private void mergeEditScripts(File firstCompareOutputTempFile, File secondCompareOutputTempFile) throws QVCSOperationException {
        addFileToEditScript(firstCompareOutputTempFile, 1);
        addFileToEditScript(secondCompareOutputTempFile, 2);

        // Check for overlap.
        long lastEditEndingPosition = 0L;
        EditInfo lastEditInfo = null;
        Iterator<EditInfo> it = editScript.values().iterator();
        while (it.hasNext()) {
            EditInfo editInfo = it.next();
            if (editInfo.getSeekPosition() <= lastEditEndingPosition) {
                String overlapMessage;
                if (lastEditInfo != null) {
                    overlapMessage = "Overlap detected between " + getEditRecordType(lastEditInfo) + " edit record from file " + lastEditInfo.getFileIndex() + " at location "
                            + lastEditInfo.getSeekPosition() + " with length " + lastEditInfo.getEditLength()
                            + " and " + getEditRecordType(editInfo) + " edit record from file " + editInfo.getFileIndex() + " at location " + editInfo.getSeekPosition()
                            + " with length " + editInfo.getEditLength();
                } else {
                    overlapMessage = "Overlap detected at " + editInfo.getFileIndex();
                }
                throw new QVCSOperationException(overlapMessage);
            }
            lastEditEndingPosition = editInfo.getSeekPosition() + editInfo.getEditLength();
            lastEditInfo = editInfo;
            insertedBytesCount += editInfo.getInsertedBytesCount();
        }
    }

    private void addFileToEditScript(File editScriptToAdd, int fileIndex) throws QVCSOperationException {
        FileInputStream fileInputStream = null;
        try {
            byte[] fileData = new byte[(int) editScriptToAdd.length()];
            fileInputStream = new FileInputStream(editScriptToAdd);
            fileInputStream.read(fileData);
            DataInputStream editStream = new DataInputStream(new ByteArrayInputStream(fileData));
            CompareFilesEditInformation cfei = new CompareFilesEditInformation();

            // Skip over the header bytes that are a prefix at the beginning of the
            // edit script.
            byte[] headerBytesToSkip = new byte[CompareFilesEditHeader.getEditHeaderSize()];
            editStream.read(headerBytesToSkip);

            while (editStream.available() > 0) {
                byte[] insertBuffer = null;

                cfei.read(editStream);
                if ((cfei.getEditType() == CompareFilesEditInformation.QVCS_EDIT_INSERT)
                        || (cfei.getEditType() == CompareFilesEditInformation.QVCS_EDIT_REPLACE)) {
                    insertBuffer = new byte[(int) cfei.getInsertedBytesCount()];
                    editStream.read(insertBuffer);
                }

                EditInfo editInfo = new EditInfo(cfei.getSeekPosition(),
                        fileIndex,
                        cfei.getEditType(),
                        cfei.getDeletedBytesCount(),
                        cfei.getInsertedBytesCount(),
                        insertBuffer);
                editScript.put(String.format("%015d,%d", cfei.getSeekPosition(), fileIndex), editInfo);
            }
        } catch (IOException e) {
            LOGGER.warn(e.getLocalizedMessage(), e);
            throw new QVCSOperationException(e.getLocalizedMessage());
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    LOGGER.warn(e.getLocalizedMessage(), e);
                    throw new QVCSOperationException(e.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Validate the arguments needed for a file merge. We need 4 arguments: <br> the base file name<br> the file name of the first descendent.<br> the file name of the 2nd
     * descendent.<br> the file name of the output file.... i.e. the file that will contained the merged result. </p>
     *
     * @throws com.qumasoft.operations.QVCSOperationException
     */
    private void validateArguments() throws QVCSOperationException {
        if (args == null) {
            throw new QVCSOperationException("No arguments specified.");
        } else {
            if (args.length != ARGUMENT_COUNT) {
                throw new QVCSOperationException("You must specify 4 separate file names.");
            } else {
                baseFileName = args[BASE_FILENAME_ARG_INDEX];
                firstDescendentName = args[FIRST_DESCENDENT_FILENAME_ARG_INDEX];
                secondDescendentName = args[SECOND_DESCENDENT_FILENAME_ARG_INDEX];
                outputFileName = args[OUTPUT_FILENAME_ARG_INDEX];

                // Verify that each input file exists.
                checkThatFileExists(baseFileName, "Base file");
                checkThatFileExists(firstDescendentName, "First descendent");
                checkThatFileExists(secondDescendentName, "Second descendent");
            }
        }
    }

    private void checkThatFileExists(final String fileName, final String fileUsedAs) throws QVCSOperationException {
        File file = new File(fileName);
        if (!file.exists()) {
            throw new QVCSOperationException(fileUsedAs + " '" + baseFileName + "' is missing.");
        } else {
            // File exists. Make sure we can read it...
            if (!file.canRead()) {
                throw new QVCSOperationException("Don't have rights to read " + fileUsedAs + " : '" + fileName + "'.");
            }
        }
    }

    private long getInsertedBytesCount() {
        return insertedBytesCount;
    }

    static class EditInfo {

        private final long seekPosition;
        /**
         * Track which file this edit info is associated with
         */
        private final int fileIndex;
        private final short editType;
        private final long deletedBytesCount;
        private final long insertedBytesCount;
        private final byte[] insertedBytes;

        EditInfo(long seekPos, int fileIdx, int editTyp, long deletedBytesCnt, long insertedBytesCnt, byte[] insrtedBytes) {
            this.seekPosition = seekPos;
            this.fileIndex = fileIdx;
            this.editType = (short) editTyp;
            this.deletedBytesCount = deletedBytesCnt;
            this.insertedBytesCount = insertedBytesCnt;
            this.insertedBytes = insrtedBytes;
        }

        public long getSeekPosition() {
            return seekPosition;
        }

        /**
         * Return the file index -- i.e. which file this edit info instance is associated with.
         *
         * @return the file index for this instance. The value should be a 1 or a 2.
         */
        public int getFileIndex() {
            return fileIndex;
        }

        public short getEditType() {
            return editType;
        }

        public long getDeletedBytesCount() {
            return deletedBytesCount;
        }

        public long getInsertedBytesCount() {
            return insertedBytesCount;
        }

        public byte[] getInsertedBytes() {
            return insertedBytes;
        }

        /**
         * Figure out how many bytes this edit record affects in base file... i.e. how far into the file from the current position will these changes occur. This is useful for
         * detecting overlap.
         *
         * @return the number of bytes that this edit will affect in the base file.
         * @throws com.qumasoft.operations.QVCSOperationException this should never get thrown. If it does, it is an internal error -- we are seeing an edit script that is corrupt.
         */
        private long getEditLength() throws QVCSOperationException {
            long editLength = -1L;
            switch (editType) {
                case CompareFilesEditInformation.QVCS_EDIT_DELETE:
                    editLength = getDeletedBytesCount();
                    break;
                case CompareFilesEditInformation.QVCS_EDIT_INSERT:
                    editLength = getInsertedBytesCount();
                    break;
                case CompareFilesEditInformation.QVCS_EDIT_REPLACE:
                    if (getDeletedBytesCount() > getInsertedBytesCount()) {
                        editLength = getDeletedBytesCount();
                    } else {
                        editLength = getInsertedBytesCount();
                    }
                    break;
                default:
                    throw new QVCSOperationException("Internal error. Unknown edit type in FileMerge.java");
            }
            return editLength;
        }
    }

}
