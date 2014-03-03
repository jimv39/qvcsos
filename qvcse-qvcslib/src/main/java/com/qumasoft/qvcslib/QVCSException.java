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

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * QVCS specific exception. We use this when we need to throw a QVCS specific exception.
 * @author Jim Voris
 */
public class QVCSException extends java.lang.Exception {
    private static final long serialVersionUID = -4739391770035318303L;

    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qvcslib");

    /**
     * Default constructor.
     */
    public QVCSException() {
    }

    /**
     * Constructor with String argument.
     * @param msg describe the problem.
     */
    public QVCSException(String msg) {
        super(msg);
        LOGGER.log(Level.WARNING, msg);
    }
}
