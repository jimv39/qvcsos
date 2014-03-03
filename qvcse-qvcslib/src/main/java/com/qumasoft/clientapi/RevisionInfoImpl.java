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
package com.qumasoft.clientapi;

import com.qumasoft.qvcslib.AccessList;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.RevisionHeader;
import java.util.Date;

/**
 * Revision info implementation.
 * @author Jim Voris
 */
class RevisionInfoImpl implements RevisionInfo {

    private final RevisionHeader revisionHeader;
    private final AccessList accessList;

    RevisionInfoImpl(LogfileInfo info, RevisionHeader revHeader) {
        this.revisionHeader = revHeader;
        accessList = new AccessList(info.getLogFileHeaderInfo().getModifierList());
    }

    @Override
    public Date getCheckInDate() {
        return revisionHeader.getCheckInDate();
    }

    @Override
    public Date getEditDate() {
        return revisionHeader.getEditDate();
    }

    @Override
    public String getRevisionAuthor() {
        return accessList.indexToUser(revisionHeader.getCreatorIndex());
    }

    @Override
    public String getRevisionLocker() {
        String locker = "";
        if (revisionHeader.isLocked()) {
            locker = accessList.indexToUser(revisionHeader.getLockerIndex());
        }
        return locker;
    }

    @Override
    public String getRevisionString() {
        return revisionHeader.getRevisionString();
    }

    @Override
    public String getRevisionDescription() {
        return revisionHeader.getRevisionDescription();
    }

    @Override
    public boolean getIsRevisionLocked() {
        return revisionHeader.isLocked();
    }

    @Override
    public boolean getIsTipRevision() {
        return revisionHeader.isTip();
    }
}
