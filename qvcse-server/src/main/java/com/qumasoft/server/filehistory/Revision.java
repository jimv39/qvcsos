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
package com.qumasoft.server.filehistory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Jim Voris
 */
public class Revision implements ToFromStreamInterface {
    private RevisionHeader header;
    private byte[] revisionData;

    public Revision() {
        header = new RevisionHeader();
    }

    /**
     * Get the revision id.
     * @return the revision id.
     */
    public Integer getId() {
        return getHeader().getId();
    }

    public RevisionHeader getHeader() {
        return header;
    }

    public void setHeader(RevisionHeader h) {
        this.header = h;
    }

    public byte[] getRevisionData() {
        return revisionData;
    }

    public void setRevisionData(byte[] data) {
        this.revisionData = data;
    }

    @Override
    public void toStream(DataOutputStream o) throws IOException {
        header.toStream(o);
        o.write(revisionData);
    }

    @Override
    public void fromStream(DataInputStream i) throws IOException {
        header = new RevisionHeader();
        header.fromStream(i);
        revisionData = new byte[header.getDataSize()];
        i.read(revisionData);
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.header);
        hash = 71 * hash + Arrays.hashCode(this.revisionData);
        // <editor-fold>
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Revision other = (Revision) obj;
        if (!Objects.equals(this.header, other.header)) {
            return false;
        }
        if (this.revisionData.length != other.revisionData.length) {
            return false;
        }
        for (int i = 0; i < this.revisionData.length; i++) {
            if (this.revisionData[i] != other.revisionData[i]) {
                return false;
            }
        }
        return true;
    }
}
