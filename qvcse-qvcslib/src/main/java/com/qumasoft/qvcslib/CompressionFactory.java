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
package com.qumasoft.qvcslib;

/**
 * Compression factory. Build the compressor that we need.
 * @author Jim Voris
 */
public final class CompressionFactory {

    private CompressionFactory() {
    }

    /**
     * Get the compressor to use for a given compression header.
     * @param compressionHeader the revision's compression header.
     * @return the compressor to use for the given compression header.
     */
    public static Compressor getCompressor(RevisionCompressionHeader compressionHeader) {
        Compressor compressor;

        switch (compressionHeader.getCompressionType()) {
            case RevisionCompressionHeader.COMPRESS_ALGORITHM_1:
                compressor = new DefaultCompressor();
                break;
            case RevisionCompressionHeader.COMPRESS_ALGORITHM_2:
                compressor = new ZlibCompressor();
                break;

            default:
                compressor = new DefaultCompressor();
                break;
        }
        return compressor;
    }
}
