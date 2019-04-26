/*   Copyright 2004-2014 Jim Voris
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
package com.qumasoft.guitools.merge;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JCheckBox;

/**
 * Edit info for merge.
 * @author Jim Voris
 */
class EditInfo {

    private final long seekPosition;
    private final short editType;
    private final long deletedBytesCount;
    private final long insertedBytesCount;
    private final byte[] insertedBytes;
    private final JCheckBox checkBox;

    EditInfo(long skPosition, int eType, long deletedBytesCnt, long insertedBytesCnt, byte[] insertdBytes) {
        this.seekPosition = skPosition;
        this.editType = (short) eType;
        this.deletedBytesCount = deletedBytesCnt;
        this.insertedBytesCount = insertedBytesCnt;
        this.insertedBytes = insertdBytes;
        this.checkBox = new JCheckBox();
        this.checkBox.addItemListener(new MyItemListener());
    }

    long getSeekPosition() {
        return seekPosition;
    }

    short getEditType() {
        return editType;
    }

    long getDeletedBytesCount() {
        return deletedBytesCount;
    }

    long getInsertedBytesCount() {
        return insertedBytesCount;
    }

    byte[] getInsertedBytes() {
        return insertedBytes;
    }

    JCheckBox getCheckBox() {
        return checkBox;
    }

    class MyItemListener implements ItemListener {

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                int i = 0;
                i++;
            } else {
                int j = 0;
                j++;
            }
        }
    }

}
