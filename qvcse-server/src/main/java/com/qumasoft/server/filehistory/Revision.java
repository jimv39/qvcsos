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

    /**
     * Default constructor.
     */
    public Revision() {
        header = new RevisionHeader();
    }

    /**
     * Get the revision id.
     *
     * @return the revision id.
     */
    public Integer getId() {
        return getHeader().getId();
    }

    /**
     * Get the revision header.
     *
     * @return the revision header.
     */
    public RevisionHeader getHeader() {
        return header;
    }

    /**
     * Set the revision header.
     *
     * @param h the revision header.
     */
    public void setHeader(RevisionHeader h) {
        this.header = h;
    }

    /**
     * Get the revision data. The revision data could be compressed. See the {@link RevisionHeader#compressionType} to determine whether the contents are compressed. The revision
     * data <i>may</i> be a delta representation instead of the actual revision. If it is a delta representation, the {@link RevisionHeader#reverseDeltaRevisionId} will be the
     * revisionId of the revision to which the delta must be applied in order to hydrate this revision. If {@link RevisionHeader#reverseDeltaRevisionId} is -1, then the data here
     * is <i>not</i> in delta format, and merely needs to be uncompressed (if it is compressed).
     *
     * @return the revision data.
     */
    public byte[] getRevisionData() {
        return revisionData;
    }

    /**
     * Set the revision data.
     *
     * @param data the revision data.
     */
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
