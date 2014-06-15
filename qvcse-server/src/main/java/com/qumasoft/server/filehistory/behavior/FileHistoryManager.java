/*
 * Copyright 2014 JimVoris.
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
package com.qumasoft.server.filehistory.behavior;

import com.qumasoft.qvcslib.CompareFilesEditInformation;
import com.qumasoft.qvcslib.CompressionFactory;
import com.qumasoft.qvcslib.Compressor;
import com.qumasoft.qvcslib.MutableByteArray;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.RevisionCompressionHeader;
import com.qumasoft.qvcslib.Utility;
import com.qumasoft.qvcslib.ZlibCompressor;
import com.qumasoft.server.filehistory.BehaviorContext;
import com.qumasoft.server.filehistory.CommitIdentifier;
import com.qumasoft.server.filehistory.CompressionType;
import com.qumasoft.server.filehistory.FileHistory;
import com.qumasoft.server.filehistory.Revision;
import com.qumasoft.server.filehistory.RevisionHeader;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.jrcs.diff.AddDelta;
import org.apache.commons.jrcs.diff.ChangeDelta;
import org.apache.commons.jrcs.diff.DeleteDelta;
import org.apache.commons.jrcs.diff.Delta;
import org.apache.commons.jrcs.diff.Diff;
import org.apache.commons.jrcs.diff.DifferentiationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class supplies behavior for a {@link FileHistory} object. Use instances of this class to perform source control behaviors for a given {@link FileHistory} instance.
 *
 * @author Jim Voris
 */
public class FileHistoryManager implements SourceControlBehaviorInterface {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileHistoryManager.class.getName());
    private static final String UTF8 = "UTF-8";

    private FileHistory fileHistory;
    private final File fileHistoryFile;

    /**
     * Create a {@link FileHistory} instance for the given file.
     *
     * @param f the file containing file history.
     */
    public FileHistoryManager(File f) {
        this.fileHistory = null;
        this.fileHistoryFile = f;
    }

    /**
     * Read the {@link FileHistory}.
     *
     * @return the {@link FileHistory} associated with this FileHistoryManager.
     * @throws IOException if the file doesn't exist; or if there are problems reading the file.
     */
    public FileHistory readFileHistory() throws IOException {
        if (this.fileHistory == null) {
            this.fileHistory = new FileHistory();
            if (this.fileHistoryFile.length() > 0) {
                try (FileInputStream fileInputStream = new FileInputStream(fileHistoryFile);
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
                        DataInputStream dataInputStream = new DataInputStream(bufferedInputStream)) {
                    fileHistory.fromStream(dataInputStream);
                }
            }
        }
        return this.fileHistory;
    }

    @Override
    public boolean getRevision(Integer revisionId, MutableByteArray result) {
        LOGGER.info("getRevision revisionId: [" + revisionId + "]");
        boolean flag = false;
        try {
            // Make sure we have read the fileHistory...
            readFileHistory();
            Revision revision = fileHistory.getRevisionByIdMap().get(revisionId);
            if (revision.getHeader().getReverseDeltaRevisionId() == -1) {
                flag = fetchRevisionData(revisionId, result);
            } else {
                // We need to 'hydrate' this revision, since it is represented by a delta from its revisionDeltaRevision.
                // We need to 'walk' the reverseDeltaRevision links until we find a 'tip' revision where we can begin to
                // apply the deltas in order to create the requested revision.
                List<Integer> editScriptList = new ArrayList<>();

                // Add the edit script for the requested revision.
                editScriptList.add(revisionId);

                // Add the other edit scripts until we reach a tip.
                Integer reverseDeltaRevisionId = revision.getHeader().getReverseDeltaRevisionId();
                while (reverseDeltaRevisionId != -1) {
                    editScriptList.add(reverseDeltaRevisionId);
                    LOGGER.trace("Adding reverse delta id: [" + reverseDeltaRevisionId + "] for file id: [" + fileHistory.getHeader().getFileId() + "]");
                    revision = fileHistory.getRevisionByIdMap().get(reverseDeltaRevisionId);
                    reverseDeltaRevisionId = revision.getHeader().getReverseDeltaRevisionId();
                }

                // Fetch the 'tip' revision, and we'll apply the edits to it, until we get to the requested revision.
                MutableByteArray buffer = new MutableByteArray();
                MutableByteArray resultBuffer = new MutableByteArray();
                if (fetchRevisionData(editScriptList.get(editScriptList.size() - 1), buffer)) {
                    for (int walkBackIndex = editScriptList.size() - 2; walkBackIndex >= 0; walkBackIndex--) {
                        MutableByteArray editBuffer = new MutableByteArray();
                        if (fetchRevisionData(editScriptList.get(walkBackIndex), editBuffer)) {
                            if (applyEdit(buffer, editBuffer, resultBuffer)) {
                                buffer = resultBuffer;
                            } else {
                                throw new QVCSRuntimeException("Failed to apply edit for file id: " + fileHistory.getHeader().getFileId() + "]");
                            }
                        }
                    }
                    result.setValue(buffer.getValue());
                    flag = true;
                } else {
                    // Huh? We couldn't fetch the 'tip' revision. Things are badly broken here.
                    throw new QVCSRuntimeException("Failed to fetch anchor tip revision id: [" + editScriptList.get(editScriptList.size() - 1)
                            + "] for file id: " + fileHistory.getHeader().getFileId() + "]");
                }
            }
        } catch (IOException e) {
            LOGGER.warn(null, e);
        }
        return flag;
    }

    private boolean applyEdit(MutableByteArray buffer, MutableByteArray editBuffer, MutableByteArray resultBuffer) {
        boolean retVal = false;
        CompareFilesEditInformation editInfo = new CompareFilesEditInformation();
        int inIndex = 0;
        int outIndex = 0;
        int bytesTillChange = 0;
        byte[] originalData = buffer.getValue();
        byte[] edits = editBuffer.getValue();
        byte[] editedBuffer = new byte[edits.length + originalData.length]; // It can't be any bigger than this.
        DataInputStream editStream = new DataInputStream(new ByteArrayInputStream(edits));

        try {
            int deletedBytesCount;
            int insertedBytesCount;

            while (editStream.available() > 0) {
                editInfo.read(editStream);
                bytesTillChange = (int) editInfo.getSeekPosition() - inIndex;
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, bytesTillChange);

                inIndex += bytesTillChange;
                outIndex += bytesTillChange;

                deletedBytesCount = (int) editInfo.getDeletedBytesCount();
                insertedBytesCount = (int) editInfo.getInsertedBytesCount();

                switch (editInfo.getEditType()) {
                    case CompareFilesEditInformation.QVCS_EDIT_DELETE:
                        /*
                         * Delete input
                         */
                        // Just skip over deleted bytes
                        inIndex += deletedBytesCount;
                        break;

                    case CompareFilesEditInformation.QVCS_EDIT_INSERT:
                        /*
                         * Insert edit lines
                         */
                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    case CompareFilesEditInformation.QVCS_EDIT_REPLACE:
                        /*
                         * Replace input line with edit line.
                         * First skip over the bytes to be replaced, then copy the replacing bytes from the edit file to the output
                         * file.
                         */
                        inIndex += deletedBytesCount;

                        editStream.read(editedBuffer, outIndex, insertedBytesCount);
                        outIndex += insertedBytesCount;
                        break;

                    default:
                        break;
                }
            }

            // Copy the rest of the input "file" to the output "file".
            int remainingBytes = originalData.length - inIndex;
            if (remainingBytes > 0) {
                System.arraycopy(originalData, inIndex, editedBuffer, outIndex, remainingBytes);
                outIndex += remainingBytes;
            }
            byte[] returnedBuffer = new byte[outIndex];
            System.arraycopy(editedBuffer, 0, returnedBuffer, 0, outIndex);
            resultBuffer.setValue(returnedBuffer);
            retVal = true;
        } catch (ArrayIndexOutOfBoundsException | IOException e) {
            LOGGER.warn(Utility.expandStackTraceToString(e));
            LOGGER.warn(" editInfo.seekPosition: " + editInfo.getSeekPosition() + " originalData.length: " + originalData.length + " inIndex: "
                    + inIndex + " editedBuffer.length: "
                    + editedBuffer.length + " outIndex: " + outIndex + " bytesTillChange: " + bytesTillChange);
            LOGGER.warn(e.getLocalizedMessage());
        } finally {
            try {
                editStream.close();
            } catch (IOException e) {
                LOGGER.warn(Utility.expandStackTraceToString(e));
            }
        }
        return retVal;
    }

    /**
     * Get the revision data for a given revision. If the revision is a tip revision, then the result will contain a fully hydrated revision. If the revision is not a tip revision,
     * then the result will be the edit script that needs to be applied to the revision's reverse delta revision in order to hydrate the revision.
     *
     * @param revisionId the id of the revision to fetch.
     * @param result [output] where we put the result.
     * @return true if we found the revision, and the result contains useful bits.
     */
    public boolean fetchRevisionData(Integer revisionId, MutableByteArray result) {
        LOGGER.debug("Fetching revision: [" + revisionId + "]");
        Revision revision = fileHistory.getRevisionByIdMap().get(revisionId);
        byte[] revisionData;
        if (revision.getHeader().getCompressionType().equals(CompressionType.ZLIB_COMPRESSED)) {
            Compressor compressor = new ZlibCompressor();
            revisionData = compressor.expand(revision.getRevisionData());
        } else {
            revisionData = revision.getRevisionData();
        }
        result.setValue(revisionData);
        return true;
    }

    @Override
    public Integer addRevision(BehaviorContext context, Revision revisionToAdd, boolean computeDeltaFlag) {
        if (revisionToAdd.getHeader().getAncestorRevisionId() != -1) {
            // Need to verify that the ancestor actually exists.
            Revision ancestor = fileHistory.getRevisionByIdMap().get(revisionToAdd.getHeader().getAncestorRevisionId());
            if (ancestor == null) {
                throw new QVCSRuntimeException("Ancestor not found: [" + revisionToAdd.getHeader().getAncestorRevisionId() + "]");
            }

            if (computeDeltaFlag) {
                // We may need to update the ancestor revision so that its content is the reverse delta script that is needed to hydrate it from this revision. We do not need
                // to update it if it already has its reverse delta defined. This could happen for a branch point, where the ancestor revision is one that already has at least one
                // other 'child' revision.... i.e. its reverse delta revision id has already been set to some value other than -1.
                if (ancestor.getHeader().getReverseDeltaRevisionId() == -1) {
                    try {
                        // Since the ancestor's reverse delta revision id is -1, it means that its contents can just be fetched -- no edit scripts need to be applied in order
                        // to hydrate it.
                        byte[] uncompressedAncestorRevisionData;
                        if (ancestor.getHeader().getCompressionType().equals(CompressionType.ZLIB_COMPRESSED)) {
                            Compressor compressor = new ZlibCompressor();
                            uncompressedAncestorRevisionData = compressor.expand(ancestor.getRevisionData());
                        } else {
                            uncompressedAncestorRevisionData = ancestor.getRevisionData();
                        }

                        byte[] reverseDeltaScript = computeReverseDeltaScript(revisionToAdd.getRevisionData(), uncompressedAncestorRevisionData);
                        ancestor.setRevisionData(reverseDeltaScript);
                        compressContent(ancestor);

                        // This new revision is the one that the ancestor will need in order to be hydrated.
                        ancestor.getHeader().setReverseDeltaRevisionId(revisionToAdd.getId());
                    } catch (DifferentiationFailedException | UnsupportedEncodingException | QVCSOperationException e) {
                        LOGGER.error(Utility.expandStackTraceToString(e), e);
                    }
                }
            }
        }

        compressContent(revisionToAdd);
        fileHistory.getRevisionByIdMap().put(revisionToAdd.getId(), revisionToAdd);
        return revisionToAdd.getHeader().getCommitIdentifier().getCommitId();
    }

    @Override
    public boolean commit(CommitIdentifier commitIdentifier) {
        boolean flag = true;
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileHistoryFile);
                BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
                DataOutputStream dataOutputStream = new DataOutputStream(bufferedOutputStream)) {
            fileHistory.toStream(dataOutputStream);
        }
        catch (IOException e) {
            LOGGER.error(null, e);
            flag = false;
        }
        return flag;
    }

    private void compressContent(Revision revision) {
        byte[] revisionContent = revision.getRevisionData();
        RevisionHeader revisionHeader = revision.getHeader();
        RevisionCompressionHeader revisionCompressionHeader = new RevisionCompressionHeader();
        revisionCompressionHeader.setCompressionType(RevisionCompressionHeader.COMPRESS_ALGORITHM_2);
        revisionCompressionHeader.setInputSize(revisionContent.length);
        Compressor compressor = CompressionFactory.getCompressor(revisionCompressionHeader);
        if (compressor.compress(revisionContent)) {
            revisionHeader.setCompressionType(CompressionType.ZLIB_COMPRESSED);
            revision.setRevisionData(compressor.getCompressedBuffer());
            revisionHeader.setDataSize(compressor.getCompressedBuffer().length);
        } else {
            revisionHeader.setCompressionType(CompressionType.NOT_COMPRESSED);
            revisionHeader.setDataSize(revisionContent.length);
            revision.setRevisionData(revisionContent);
        }
    }

    private byte[] computeReverseDeltaScript(byte[] revisionData, byte[] uncompressedAncestorRevisionData)
            throws DifferentiationFailedException, UnsupportedEncodingException, QVCSOperationException {
        HistoryCompareLineInfo[] fileA = buildLines(revisionData);
        HistoryCompareLineInfo[] fileB = buildLines(uncompressedAncestorRevisionData);

        org.apache.commons.jrcs.diff.Revision apacheRevision = Diff.diff(fileA, fileB);

        // Create the edit script that describes the changes we found.
        return createEditScript(apacheRevision, revisionData.length, fileA, fileB);
    }

    private HistoryCompareLineInfo[] buildLines(byte[] revisionData) throws UnsupportedEncodingException {
        List<HistoryCompareLineInfo> lineInfoList = new ArrayList<>();
        long fileLength = revisionData.length;
        int startOfLineSeekPosition = 0;
        int currentSeekPosition = 0;
        byte character;
        while (currentSeekPosition < fileLength) {
            character = revisionData[currentSeekPosition++];
            if (character == '\n') {
                int endOfLine = currentSeekPosition;
                byte[] buffer = new byte[endOfLine - startOfLineSeekPosition];
                System.arraycopy(revisionData, startOfLineSeekPosition, buffer, 0, buffer.length);
                HistoryCompareLineInfo lineInfo = new HistoryCompareLineInfo(startOfLineSeekPosition, buffer);
                lineInfoList.add(lineInfo);
                startOfLineSeekPosition = currentSeekPosition;
            }
        }
        // Add the final line which can happen if it doesn't end in a newline.
        if ((fileLength - startOfLineSeekPosition) > 0L) {
            byte[] buffer = new byte[(int) fileLength - startOfLineSeekPosition];
            System.arraycopy(revisionData, startOfLineSeekPosition, buffer, 0, buffer.length);
            HistoryCompareLineInfo lineInfo = new HistoryCompareLineInfo(startOfLineSeekPosition, buffer);
            lineInfoList.add(lineInfo);
        }
        HistoryCompareLineInfo[] lineInfoArray = new HistoryCompareLineInfo[lineInfoList.size()];
        int i = 0;
        for (HistoryCompareLineInfo lineInfo : lineInfoList) {
            lineInfoArray[i++] = lineInfo;
        }
        return lineInfoArray;
    }

    private byte[] createEditScript(org.apache.commons.jrcs.diff.Revision apacheRevision, int fileALength, HistoryCompareLineInfo[] fileA, HistoryCompareLineInfo[] fileB)
            throws QVCSOperationException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream outStream = new DataOutputStream(byteArrayOutputStream);
        int count = apacheRevision.size();
        for (int index = 0; index < count; index++) {
            Delta delta = apacheRevision.getDelta(index);
            formatEditScript(delta, outStream, fileA);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void formatEditScript(Delta delta, DataOutputStream outStream, HistoryCompareLineInfo[] fileA) throws QVCSOperationException {
        try {
            short editType;
            int seekPosition = -1;
            int deletedByteCount = 0;
            int insertedByteCount = 0;
            byte[] secondFileByteBuffer = null;
            if (delta instanceof ChangeDelta) {
                HistoryCompareLineInfo originalStartingLine = fileA[delta.getOriginal().anchor()];
                seekPosition = originalStartingLine.getLineSeekPosition();
                editType = CompareFilesEditInformation.QVCS_EDIT_REPLACE;
                deletedByteCount = computeDeletedByteCount(delta);
                insertedByteCount = computeInsertedByteCount(delta);
                secondFileByteBuffer = computeSecondFileByteBufferForInsert(delta, insertedByteCount);
            } else if (delta instanceof AddDelta) {
                int anchor = delta.getOriginal().anchor();
                if (anchor == 0) {
                    HistoryCompareLineInfo originalStartingLine = fileA[delta.getOriginal().anchor()];
                    seekPosition = originalStartingLine.getLineSeekPosition();
                } else {
                    HistoryCompareLineInfo originalStartingLine = fileA[delta.getOriginal().anchor() - 1];
                    byte[] lineAsByteArray = originalStartingLine.getLineBuffer();
                    seekPosition = originalStartingLine.getLineSeekPosition() + lineAsByteArray.length;
                }
                editType = CompareFilesEditInformation.QVCS_EDIT_INSERT;
                insertedByteCount = computeInsertedByteCount(delta);
                AddDelta insertDelta = (AddDelta) delta;
                secondFileByteBuffer = computeSecondFileByteBufferForInsert(insertDelta, insertedByteCount);
            } else if (delta instanceof DeleteDelta) {
                HistoryCompareLineInfo originalStartingLine = fileA[delta.getOriginal().anchor()];
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
        List<HistoryCompareLineInfo> originalChunk = delta.getOriginal().chunk();
        int seekPosition = originalChunk.get(0).getLineSeekPosition();
        HistoryCompareLineInfo lastLine = originalChunk.get(originalChunk.size() - 1);
        int lastLineSeekStart = lastLine.getLineSeekPosition();
        byte[] lastLineAsByteArray = lastLine.getLineBuffer();
        int lastLineEnd = lastLineSeekStart + lastLineAsByteArray.length;
        return lastLineEnd - seekPosition;
    }

    private int computeInsertedByteCount(Delta replaceDelta) throws UnsupportedEncodingException {
        // This should be the byte count of the revised chunk.
        @SuppressWarnings("unchecked")
        List<HistoryCompareLineInfo> revisedChunk = replaceDelta.getRevised().chunk();
        int seekPosition = revisedChunk.get(0).getLineSeekPosition();
        HistoryCompareLineInfo lastLine = revisedChunk.get(revisedChunk.size() - 1);
        int lastLineSeekStart = lastLine.getLineSeekPosition();
        byte[] lastLineAsByteArray = lastLine.getLineBuffer();
        int lastLineEnd = lastLineSeekStart + lastLineAsByteArray.length;
        return lastLineEnd - seekPosition;
    }

    private byte[] computeSecondFileByteBufferForInsert(Delta delta, int insertedByteCount) throws UnsupportedEncodingException {
        byte[] insertedBytes = new byte[insertedByteCount];
        @SuppressWarnings("unchecked")
        List<HistoryCompareLineInfo> insertedChunk = delta.getRevised().chunk();
        int insertionIndex = 0;
        for (HistoryCompareLineInfo lineInfo : insertedChunk) {
            byte[] chunkBytes = lineInfo.getLineBuffer();
            for (int i = 0; i < chunkBytes.length; i++) {
                insertedBytes[insertionIndex++] = chunkBytes[i];
            }
        }
        if (insertionIndex != insertedByteCount) {
            LOGGER.warn("insertion index != inserted byte count; error in compare with apache.");
            throw new QVCSRuntimeException("insertion index != inserted byte count; error in compare with apache.");
        }
        return insertedBytes;
    }

}
