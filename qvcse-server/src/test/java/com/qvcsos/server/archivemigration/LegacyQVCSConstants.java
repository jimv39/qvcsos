/*
 * Copyright 2021 jimv.
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
 * @author jimv
 */
class LegacyQVCSConstants {
    /**
     * Archive temp file suffix.
     */
    public static final String QVCS_ARCHIVE_TEMPFILE_SUFFIX = ".temp";
    /**
     * Archive old file suffix.
     */
    public static final String QVCS_ARCHIVE_OLDFILE_SUFFIX = ".old";
    /**
     * The standard QVCS path separator byte.
     */
    public static final byte QVCS_STANDARD_PATH_SEPARATOR = '/';
    public static final int QVCS_MAXIMUM_BRANCH_DEPTH = 20;
    public static final int BYTES_TO_XFER = 2 * 1048576;
    private static final int MAX_PATH_BASE = 260;
    private static final int MAX_PATH_SUPPLEMENT = 256;
    /**
     * The size of the supplemental information in the QVCS Header.
     */
    public static final int QVCS_SUPPLEMENTAL_SIZE = MAX_PATH_BASE + MAX_PATH_SUPPLEMENT;

}
