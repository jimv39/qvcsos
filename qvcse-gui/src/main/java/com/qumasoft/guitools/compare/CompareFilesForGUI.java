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

import com.qumasoft.qvcslib.CompareLineInfo;
import com.qumasoft.qvcslib.QVCSOperationException;
import com.qumasoft.qvcslib.QumaAssert;
import org.apache.commons.jrcs.diff.AddDelta;
import org.apache.commons.jrcs.diff.ChangeDelta;
import org.apache.commons.jrcs.diff.DeleteDelta;
import org.apache.commons.jrcs.diff.Delta;
import org.apache.commons.jrcs.diff.Revision;

final class CompareFilesForGUI extends com.qumasoft.qvcslib.CompareFilesWithApacheDiff {

    private Revision apacheRevision = null;
    private byte[] fileARowTypeArray;
    private byte[] fileBRowTypeArray;
    private Delta[] fileADeltaForRowArray;
    private Delta[] fileBDeltaForRowArray;

    CompareFilesForGUI(String[] args) {
        super(args);
        QumaAssert.isTrue(args.length == COMMAND_ARG_COUNT);
    }

    @Override
    protected void writeEditScript(Revision apacheRev, CompareLineInfo[] fileA, CompareLineInfo[] fileB) throws QVCSOperationException {
        this.apacheRevision = apacheRev;

        // deduce row types for lines in fileA and lines in fileB
        deduceRowTypes(apacheRev, fileA, fileB);
    }

    int getNumberOfChanges() {
        return this.apacheRevision.size();
    }

    byte getRowType(int lineIndex, boolean firstFile) {
        byte rowType;
        if (firstFile) {
            rowType = fileARowTypeArray[lineIndex];
        } else {
            rowType = fileBRowTypeArray[lineIndex];
        }
        return rowType;
    }

    private void deduceRowTypes(Revision apacheRev, CompareLineInfo[] fileA, CompareLineInfo[] fileB) throws QVCSOperationException {
        // First get all the deltas...
        Delta[] deltas = new Delta[apacheRev.size()];
        for (int i = 0; i < apacheRev.size(); i++) {
            deltas[i] = apacheRev.getDelta(i);
        }

        fileARowTypeArray = new byte[fileA.length];
        fileBRowTypeArray = new byte[fileB.length];

        fileADeltaForRowArray = new Delta[fileA.length];
        fileBDeltaForRowArray = new Delta[fileB.length];

        // Set all the rows to default to NORMAL.
        for (int i = 0; i < fileA.length; i++) {
            fileARowTypeArray[i] = ContentRow.ROWTYPE_NORMAL;
        }
        for (int i = 0; i < fileB.length; i++) {
            fileBRowTypeArray[i] = ContentRow.ROWTYPE_NORMAL;
        }
        for (Delta delta : deltas) {
            byte rowType;
            if (delta instanceof AddDelta) {
                rowType = ContentRow.ROWTYPE_INSERT;
                // We need to set the row type and delta here for fileA because the
                // for loop doesn't traverse any rows on fileA.
                if (delta.getOriginal().anchor() > 0) {
                    fileARowTypeArray[delta.getOriginal().anchor() - 1] = ContentRow.ROWTYPE_NORMAL;
                    fileADeltaForRowArray[delta.getOriginal().anchor() - 1] = delta;
                }
            } else if (delta instanceof ChangeDelta) {
                rowType = ContentRow.ROWTYPE_REPLACE;
            } else if (delta instanceof DeleteDelta) {
                rowType = ContentRow.ROWTYPE_DELETE;
                // We need to set the row type and delta here for fileB because the
                // for loop doesn't traverse any rows on fileB.
                if (delta.getRevised().anchor() > 0) {
                    fileBRowTypeArray[delta.getRevised().anchor() - 1] = ContentRow.ROWTYPE_NORMAL;
                    fileBDeltaForRowArray[delta.getRevised().anchor() - 1] = delta;
                }
            } else {
                throw new QVCSOperationException("Unknown delta type.");
            }
            @SuppressWarnings("unchecked")
            int firstA = delta.getOriginal().first();
            int lastA = delta.getOriginal().last();
            for (int j = firstA; j <= lastA; j++) {
                fileARowTypeArray[j] = rowType;
                fileADeltaForRowArray[j] = delta;
            }
            int firstB = delta.getRevised().first();
            int lastB = delta.getRevised().last();
            for (int j = firstB; j <= lastB; j++) {
                fileBRowTypeArray[j] = rowType;
                fileBDeltaForRowArray[j] = delta;
            }
        }
    }

    Delta getLastDelta() {
        return apacheRevision.getDelta(apacheRevision.size() - 1);
    }

    Delta getDelta(int rowIndex, boolean firstFileFlag) {
        Delta delta;
        if (firstFileFlag) {
            delta = fileADeltaForRowArray[rowIndex];
        } else {
            delta = fileBDeltaForRowArray[rowIndex];
        }
        return delta;
    }

    Delta getDelta(int deltaIndex) {
        return apacheRevision.getDelta(deltaIndex);
    }
}
