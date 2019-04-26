/*   Copyright 2004-2014 Jim Voris
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.qumasoft.qvcslib;

/**
 * Our very own RuntimeException.
 * @author Jim Voris
 */
public class QVCSRuntimeException extends java.lang.RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Default constructor.
     */
    public QVCSRuntimeException() {
    }

    /**
     * Create an exception with a message.
     * @param msg the message.
     */
    public QVCSRuntimeException(String msg) {
        super(msg);
    }
}
