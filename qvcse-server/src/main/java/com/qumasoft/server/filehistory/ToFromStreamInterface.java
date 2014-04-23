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
 * Define methods needed to read/write file history instances to/from a data input/output stream. We use this approach rather than the Serializable marker interface or the
 * Externalizable interface as it allows us more absolute control over what gets written/read to/from our file history files.
 * @author Jim Voris
 */
public interface ToFromStreamInterface {

    /**
     * Write the object to the given data output stream. The only constraint is that the object state can be fully recovered by creating an object instance via the default
     * no-argument constructor followed by a call to fromStream(i) method required by this same interface. Generally, there need be no class information written to the output
     * stream since the object type can be inferred from our current position in the stream. Meaning, the FileHistory object, and the classes that compose it on in a well known
     * order, and there is absolutely no need to add class information to the stream in order for the fromStream method to know what to read.
     *
     * @param o the output stream to which this object should be written.
     * @throws java.io.IOException for problems writing.
     */
    void toStream(DataOutputStream o) throws IOException;

    /**
     * Read the object from the given data input stream. This is the converse of the toStream method, and the order of the data that is read is, by definition in the same order as
     * it was written by the toStream method.
     *
     * @param i the input stream from which to read.
     * @throws java.io.IOException for problems reading.
     */
    void fromStream(DataInputStream i) throws IOException;
}
