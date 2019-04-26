/*   Copyright 2004-2015 Jim Voris
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
package com.qumasoft.server;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ArchiveDigestDictionaryStore implements java.io.Serializable {
    private static final long serialVersionUID = 4242133006303531886L;
    // Create our logger object
    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveDigestDictionaryStore.class);

    private final Map<String, ArchiveDigestDictionaryElement> digestMap;

    ArchiveDigestDictionaryStore() {
        this.digestMap = Collections.synchronizedMap(new TreeMap<String, ArchiveDigestDictionaryElement>());
    }

    void addDigest(LogFile logfile, String revisionString, byte[] digest) {
        if ((logfile != null) && (revisionString != null) && (digest != null)) {
            String key = getDigestKey(logfile, revisionString);
            digestMap.put(key, new ArchiveDigestDictionaryElement(digest));
        }
    }

    void addDigest(LogFileImpl logfile, String revisionString, byte[] digest) {
        if ((logfile != null) && (revisionString != null) && (digest != null)) {
            String key = getDigestKey(logfile, revisionString);
            digestMap.put(key, new ArchiveDigestDictionaryElement(digest));
        }
    }

    void removeArchiveDigest(LogFile logfile, String revisionString) {
        String key = getDigestKey(logfile, revisionString);
        if (key != null) {
            digestMap.remove(key);
        }
    }

    byte[] lookupArchiveDigest(LogFile logfile, String revisionString) {
        byte[] retVal = null;
        if (logfile != null) {
            String key = getDigestKey(logfile, revisionString);
            if (key != null) {
                ArchiveDigestDictionaryElement element = digestMap.get(key);
                if (element != null) {
                    retVal = element.getDigest();
                }
            }
        }
        return retVal;
    }

    /**
     * Compute the key we use to lookup the digest for the given archive revision.
     */
    private String getDigestKey(LogFile logfile, String revisionString) {
        return logfile.getFullArchiveFilename() + ":" + revisionString;
    }

    private String getDigestKey(LogFileImpl logfile, String revisionString) {
        return logfile.getFileName() + ":" + revisionString;
    }

    void dumpMap() {
        LOGGER.info("ArchiveDigestDictionaryStore.dumpMap()");
        Set keys = digestMap.keySet();
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            LOGGER.info(i.next().toString());
        }
    }

    static class ArchiveDigestDictionaryElement implements java.io.Serializable {
        private static final long serialVersionUID = -409295613880671848L;

        private final byte[] digest;

        ArchiveDigestDictionaryElement(byte[] d) {
            digest = d;
        }

        byte[] getDigest() {
            return digest;
        }
    }
}
