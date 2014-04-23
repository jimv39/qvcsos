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

/**
 * Identify the workfile attributes for a revision.
 * @author Jim Voris
 */
public class WorkfileAttributes implements ToFromStreamInterface {
    private boolean isExecutable;

    /**
     * Default constructor.
     */
    public WorkfileAttributes() {
        this.isExecutable = false;
    }

    boolean getIsExecutable() {
        return isExecutable;
    }

    void setIsExecutable(boolean flag) {
        this.isExecutable = flag;
    }

    @Override
    public void toStream(DataOutputStream o) throws IOException {
        o.writeBoolean(isExecutable);
    }

    @Override
    public void fromStream(DataInputStream i) throws IOException {
        isExecutable = i.readBoolean();
    }

    @Override
    public int hashCode() {
        // <editor-fold>
        int hash = 7;
        hash = 89 * hash + (this.isExecutable ? 1 : 0);
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
        final WorkfileAttributes other = (WorkfileAttributes) obj;
        return this.isExecutable == other.isExecutable;
    }
}
