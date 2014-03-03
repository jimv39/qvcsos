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
package com.qumasoft.server;

import com.qumasoft.qvcslib.QVCSRuntimeException;

/**
 * QVCS shutdown exception. An runtime exception we throw in order to shutdown the server from a client request.
 * @author Jim Voris
 */
public class QVCSShutdownException extends QVCSRuntimeException {
    private static final long serialVersionUID = -2550387456117925541L;

    /**
     * Creates a new instance of QVCSShutdownException without detail message.
     */
    public QVCSShutdownException() {
    }

    /**
     * Constructs an instance of QVCSShutdownException with the specified detail message.
     *
     * @param msg the detail message.
     */
    public QVCSShutdownException(String msg) {
        super(msg);
    }
}
