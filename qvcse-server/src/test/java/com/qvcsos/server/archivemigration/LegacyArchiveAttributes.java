/*
 * Copyright 2021 Jim Voris.
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
package com.qvcsos.server.archivemigration;

/**
 *
 * @author Jim Voris.
 */
public class LegacyArchiveAttributes {
    private int attributes;

    private static final short QVCS_BINARYFILE_BIT = 0x20;

    public LegacyArchiveAttributes(int attribs) {
        attributes = attribs;
    }
    public boolean getIsBinaryfile() {
        return ((attributes & QVCS_BINARYFILE_BIT) != 0);
    }

}
