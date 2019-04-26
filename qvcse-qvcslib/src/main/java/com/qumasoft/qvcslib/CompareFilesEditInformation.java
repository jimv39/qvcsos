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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Capture a file edit. There can be 3 types of edits:
 * <ul>
 * <li>insert</li>
 * <li>delete</li>
 * <li>replace</li>
 * </ul>
 * Instances of this object capture the information needed to fully describe these.
 * @author Jim Voris
 */
public class CompareFilesEditInformation {
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(CompareFilesEditInformation.class);
    /** An insert type of edit. */
    public static final short QVCS_EDIT_INSERT = 0;
    /** A delete type of edit. */
    public static final short QVCS_EDIT_DELETE = 1;
    /** A replace type of edit. */
    public static final short QVCS_EDIT_REPLACE = 2;
    private static final int INSTANCE_BYTE_COUNT = 14;
    private final CommonShort editType = new CommonShort();
    private final Common32Long seekPosition = new Common32Long();
    private final Common32Long deletedBytesCount = new Common32Long();
    private final Common32Long insertedBytesCount = new Common32Long();

    /**
     * Default constructor.
     */
    public CompareFilesEditInformation() {
    }

    /**
     * Create instance populated with supplied parameters.
     * @param type the type of edit (insert, delete, or replace).
     * @param seekPos the seek position for the edit.
     * @param deletedCount the number of bytes deleted.
     * @param insertedCount the number of bytes inserted.
     */
    public CompareFilesEditInformation(short type, int seekPos, int deletedCount, int insertedCount) {
        editType.setValue(type);
        seekPosition.setValue(seekPos);
        deletedBytesCount.setValue(deletedCount);
        insertedBytesCount.setValue(insertedCount);
    }

    /**
     * Get the edit type.
     * @return the edit type.
     */
    public int getEditType() {
        return editType.getValue();
    }

    /**
     * Set the edit type.
     * @param value the edit type.
     */
    public void setEditType(int value) {
        if (value < QVCS_EDIT_INSERT || value > QVCS_EDIT_REPLACE) {
            throw new QVCSRuntimeException("Invalid value for edit type: " + value);
        } else {
            editType.setValue(value);
        }
    }

    /**
     * Get the seek position.
     * @return the seek position.
     */
    public long getSeekPosition() {
        return seekPosition.getValue();
    }

    /**
     * Set the seek position.
     * @param value the seek position.
     */
    public void setSeekPosition(int value) {
        seekPosition.setValue(value);
    }

    /**
     * Get the deleted byte count.
     * @return the deleted byte count.
     */
    public long getDeletedBytesCount() {
        return deletedBytesCount.getValue();
    }

    /**
     * Set the deleted byte count.
     * @param value the deleted byte count.
     */
    public void setDeletedBytesCount(int value) {
        deletedBytesCount.setValue(value);
    }

    /**
     * Get the inserted byte count.
     * @return the inserted byte count.
     */
    public long getInsertedBytesCount() {
        return insertedBytesCount.getValue();
    }

    /**
     * Set the inserted byte count.
     * @param value the inserted byte count.
     */
    public void setInsertedBytesCount(int value) {
        insertedBytesCount.setValue(value);
    }

    /**
     * Read from a file.
     * @param inStream the stream to read from.
     * @throws IOException for read problems.
     */
    public void read(RandomAccessFile inStream) throws IOException {
        editType.read(inStream);
        seekPosition.read(inStream);
        deletedBytesCount.read(inStream);
        insertedBytesCount.read(inStream);
    }

    /**
     * Read from a data input stream.
     * @param inStream the stream to read from.
     * @throws IOException for a read problem.
     */
    public void read(DataInputStream inStream) throws IOException {
        editType.read(inStream);
        seekPosition.read(inStream);
        deletedBytesCount.read(inStream);
        insertedBytesCount.read(inStream);
    }

    /**
     * Write to a data output stream.
     * @param outStream the data output stream to write to.
     * @throws IOException for a write problem.
     */
    public void write(DataOutputStream outStream) throws IOException {
        LOGGER.trace("write editInfo: seek: " + seekPosition.getValue() + " delete: " + deletedBytesCount.getValue() + " insert: "
                + insertedBytesCount.getValue());
        editType.write(outStream);
        seekPosition.write(outStream);
        deletedBytesCount.write(outStream);
        insertedBytesCount.write(outStream);
    }

    /**
     * This is the size (in bytes) of this object when written to a file.
     *
     * @return the size (in bytes) of this object when written to a file.
     */
    public static int getSize() {
        return INSTANCE_BYTE_COUNT;
    }
}
