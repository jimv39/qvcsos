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
 * Specific runtime exception for compression problems.
 * @author Jim Voris
 */
public class CompressionRuntimeException extends com.qumasoft.qvcslib.QVCSRuntimeException {
    private static final long serialVersionUID = 8516095986981210801L;

    /**
     * Creates a new instance of CompressionRuntimeException.
     */
    public CompressionRuntimeException() {
    }

    /**
     * Create an exception with a useful message.
     * @param msg a useful message.
     */
    public CompressionRuntimeException(String msg) {
        super(msg);
    }
}
