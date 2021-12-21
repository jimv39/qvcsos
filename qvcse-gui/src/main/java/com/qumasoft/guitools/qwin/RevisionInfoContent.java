/*   Copyright 2004-2021 Jim Voris
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
package com.qumasoft.guitools.qwin;

import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.MergedInfoInterface;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Revision info content.
 * @author Jim Voris
 */
public class RevisionInfoContent {

    private List<String> revisionInfo = Collections.synchronizedList(new ArrayList<>());
    private static final int DEFAULT_WORD_WRAP_COLUMN = 80;

    /**
     * Creates a new instance of RevisionInfoContent.
     * @param mergedInfo the file that we're showing revision content for.
     */
    public RevisionInfoContent(MergedInfoInterface mergedInfo) {
        if (mergedInfo.getArchiveInfo() != null) {
            addRevisionInformation(mergedInfo);
        }
    }

    private void addRevisionInformation(MergedInfoInterface mergedInfo) {
        LogfileInfo logfileInfo = mergedInfo.getLogfileInfo();
        int revisionCount = logfileInfo.getLogFileHeaderInfo().getRevisionCount();
        RevisionInformation revisionInformation = logfileInfo.getRevisionInformation();
        for (int i = 0; i < revisionCount; i++) {
            RevisionHeader revHeader = revisionInformation.getRevisionHeader(i);
            String revisionCreator = "TODO";
            revisionInfo.add(0, revHeader.getRevisionString() + " check in time: " + revHeader.getCheckInDate().toString() + " by " + revisionCreator + "\n");
            revisionInfo.add(0, "Workfile edit date: " + revHeader.getEditDate().toString() + "\n");
            addWordWrappedRevisionDescription(revHeader);
            revisionInfo.add(0, "----------------------------------------------------------------------------\n");
        }
    }

    private void addWordWrappedRevisionDescription(RevisionHeader revHeader) {
        byte[] descriptionBuffer = revHeader.getRevisionDescription().getBytes();

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
                if ((preceedingSpaceIndex > 0) && (column > DEFAULT_WORD_WRAP_COLUMN)) {
                    descriptionBuffer[preceedingSpaceIndex] = '\n';
                    column = 0;
                }
                preceedingSpaceIndex = i;
            }
        }
        String wrappedDescription = new String(descriptionBuffer);
        revisionInfo.add(0, wrappedDescription + "\n");
    }

     Iterator iterator() {
        return revisionInfo.iterator();
    }
}
