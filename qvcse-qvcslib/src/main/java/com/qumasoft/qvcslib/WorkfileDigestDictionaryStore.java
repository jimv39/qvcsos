//   Copyright 2004-2015 Jim Voris
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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workfile Digest Dictionary store. This class manages the storage of the workfile digest information.
 * @author Jim Voris
 */
public class WorkfileDigestDictionaryStore implements java.io.Serializable {
    private static final long serialVersionUID = 6010677059933889679L;
    // Create our logger object
    private static final transient Logger LOGGER = LoggerFactory.getLogger(WorkfileDigestDictionaryStore.class);

    private final Map<String, WorkfileDigestDictionaryElement>  map;

    /**
     * Default constructor.
     */
    public WorkfileDigestDictionaryStore() {
        this.map = Collections.synchronizedMap(new TreeMap<String, WorkfileDigestDictionaryElement>());
    }

    void addWorkfileDigest(WorkfileInfoInterface workfileInfo, byte[] digest) {
        if ((workfileInfo != null) && (digest != null)) {
            String key = getDigestKey(workfileInfo);
            map.put(key, new WorkfileDigestDictionaryElement(workfileInfo, digest));
        }
    }

    void removeWorkfileDigest(WorkfileInfoInterface workfileInfo) {
        String key = getDigestKey(workfileInfo);
        if (key != null) {
            map.remove(key);
        }
    }

    byte[] lookupWorkfileDigest(WorkfileInfoInterface workfileInfo) {
        byte[] retVal = null;
        if (workfileInfo != null) {
            String key = getDigestKey(workfileInfo);
            if (key != null) {
                WorkfileDigestDictionaryElement element = map.get(key);
                if (element != null) {
                    retVal = element.getDigest();
                }
            }
        }
        return retVal;
    }

    WorkfileInfoInterface lookupWorkfileInfo(WorkfileInfoInterface workfileInfo) {
        WorkfileInfoInterface retVal = null;
        if (workfileInfo != null) {
            String key = getDigestKey(workfileInfo);
            if (key != null) {
                WorkfileDigestDictionaryElement element = map.get(key);
                if (element != null) {
                    retVal = element.getWorkfileInfo();
                }
            }
        }
        return retVal;
    }

    /**
     * Compute the key we use to lookup the digest for the given workfile
     */
    private String getDigestKey(WorkfileInfoInterface workfileInfo) {
        return workfileInfo.getProjectName() + ":" + workfileInfo.getFullWorkfileName();
    }

    void dumpMap() {
        LOGGER.info("WorkfileDigestDictionaryStore.dumpMap()");
        Set keys = map.keySet();
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            LOGGER.info(i.next().toString());
        }
    }

    /**
     * Workfile digest dictionary element. Hold on to the workfile's digest and workfile info values.
     */
    public static class WorkfileDigestDictionaryElement implements java.io.Serializable {
        private static final long serialVersionUID = -6889544110720053739L;

        private final byte[] digest;
        private final WorkfileInfoInterface workfileInfo;

        WorkfileDigestDictionaryElement(WorkfileInfoInterface workInfo, byte[] dgst) {
            workfileInfo = workInfo;
            digest = dgst;
        }

        byte[] getDigest() {
            return digest;
        }

        WorkfileInfoInterface getWorkfileInfo() {
            return workfileInfo;
        }
    }

}
