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

import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 *
 * @author Jim Voris
 */
public class FileHistoryHeader implements ToFromStreamInterface {
    private Integer version;
    private Integer fileId;
    private boolean isBinaryFileFlag;
    private String creator;
    private String description;

    /**
     * Default constructor.
     */
    public FileHistoryHeader() {
        version = QVCSConstants.QVCS_FILE_HISTORY_VERSION;
    }

    Integer getVersion() {
        return version;
    }

    Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer id) {
        this.fileId = id;
    }

    boolean getIsBinaryFileFlag() {
        return isBinaryFileFlag;
    }

    public void setIsBinaryFileFlag(boolean flag) {
        this.isBinaryFileFlag = flag;
    }

    String getCreator() {
        return creator;
    }

    public void setCreator(String c) {
        this.creator = c;
    }

    String getDescription() {
        return description;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    @Override
    public void toStream(DataOutputStream o) throws IOException {
        o.writeInt(version);
        o.writeInt(fileId);
        o.writeBoolean(isBinaryFileFlag);
        o.writeUTF(creator);
        o.writeUTF(description);
    }

    @Override
    public void fromStream(DataInputStream i) throws IOException {
        version = i.readInt();
        if (!Objects.equals(version, QVCSConstants.QVCS_FILE_HISTORY_VERSION)) {
            throw new QVCSRuntimeException("Invalid FileHistory version: [" + version + "]. Expecting version to be: [" + QVCSConstants.QVCS_FILE_HISTORY_VERSION + "]");
        }
        fileId = i.readInt();
        isBinaryFileFlag = i.readBoolean();
        creator = i.readUTF();
        description = i.readUTF();
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 3;
        hash = 53 * hash + Objects.hashCode(this.version);
        hash = 53 * hash + Objects.hashCode(this.fileId);
        hash = 53 * hash + (this.isBinaryFileFlag ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.creator);
        hash = 53 * hash + Objects.hashCode(this.description);
        // </editor-fold>
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
        final FileHistoryHeader other = (FileHistoryHeader) obj;
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.fileId, other.fileId)) {
            return false;
        }
        if (this.isBinaryFileFlag != other.isBinaryFileFlag) {
            return false;
        }
        if (!Objects.equals(this.creator, other.creator)) {
            return false;
        }
        return Objects.equals(this.description, other.description);
    }

}
