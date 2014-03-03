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
 * This interface defines just those methods needed to build a skinny logfile info object. The goal is to use this interface to define the kind of object that a
 * view collection needs to have.
 *
 * @author Jim Voris
 */
public interface LogFileInterface {

    /**
     * Get the logfile info.
     * @return the logfile info.
     */
    LogfileInfo getLogfileInfo();

    /**
     * Get the full archive filename.
     * @return the full archive filename.
     */
    String getFullArchiveFilename();

    /**
     * Is this archive obsolete.
     * @return true if obsolete; false otherwise.
     * @deprecated obsolete is such a last century kind of term.
     */
    boolean getIsObsolete();

    /**
     * Get the default revision digest.
     * @return the default revision digest.
     */
    byte[] getDefaultRevisionDigest();

    /**
     * Get the revision information (the collection of all revision infos for the entire archive).
     * @return the revision information.
     */
    RevisionInformation getRevisionInformation();
}
