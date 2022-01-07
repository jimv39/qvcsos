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

import com.qvcsos.server.archivemigration.LegacyCompressor;
import com.qvcsos.server.archivemigration.LegacyDefaultCompressor;
import com.qvcsos.server.archivemigration.LegacyRevisionCompressionHeader;
import com.qvcsos.server.archivemigration.LegacyZlibCompressor;

/**
 *
 * @author Jim Voris
 */
public class LegacyCompressionFactory {

    public static LegacyCompressor getCompressor(LegacyRevisionCompressionHeader compressionHeader) {
        LegacyCompressor compressor;

        switch (compressionHeader.getCompressionType()) {
            case LegacyRevisionCompressionHeader.COMPRESS_ALGORITHM_1:
                compressor = new LegacyDefaultCompressor();
                break;
            case LegacyRevisionCompressionHeader.COMPRESS_ALGORITHM_2:
                compressor = new LegacyZlibCompressor();
                break;

            default:
                compressor = new LegacyDefaultCompressor();
                break;
        }
        return compressor;
    }

}
