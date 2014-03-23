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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Keyword contracted workfile cache. This is a client-side cache of recent workfiles. The workfiles are stored in memory, in a non-keyword expanded state. The idea
 * behind the cache is to capture workfiles so we don't have to do a server round-trip for those cases where we have recently sent the file to the server.
 *
 * @author Jim Voris
 */
public final class KeywordContractedWorkfileCache {

    private static final KeywordContractedWorkfileCache KEYWORD_CONTRACTED_WORKFILE_CACHE = new KeywordContractedWorkfileCache();
    private static final int MAXIMUM_CACHE_SIZE = 20_000_000;   // 20 Megabytes;
    private static final String REVISION_PENDING = "Pending";
    /** Store the workfile bytes in a map that is indexed by a String key. */
    private Map<KeyByName, byte[]> byNameCache;
    private Map<Integer, ByIndexElement> byIndexCache;
    private List<KeyByName> byInsertionOrderList;
    private int nextIndex = 0;
    private int currentCacheSize = 0;

    /**
     * This is a singleton.
     */
    private KeywordContractedWorkfileCache() {
        byNameCache = Collections.synchronizedMap(new TreeMap<KeyByName, byte[]>());
        byIndexCache = Collections.synchronizedMap(new TreeMap<Integer, ByIndexElement>());
        byInsertionOrderList = Collections.synchronizedList(new ArrayList<KeyByName>());
    }

    /**
     * Get the keyword contracted workfile cache singleton.
     * @return the keyword contracted workfile cache singleton.
     */
    public static KeywordContractedWorkfileCache getInstance() {
        return KEYWORD_CONTRACTED_WORKFILE_CACHE;
    }

    /**
     * Add a contracted buffer to the cache.
     * @param projectName the project name.
     * @param appendedPath the appended path.
     * @param shortWorkfileName the short workfile name.
     * @param buffer the buffer that we add to the cache.
     * @return an index the identifies the buffer within the 'byIndex' cache.
     */
    public int addContractedBuffer(String projectName, String appendedPath, String shortWorkfileName, byte[] buffer) {
        KeyByName key = new KeyByName(projectName, appendedPath, shortWorkfileName, REVISION_PENDING);
        int index = getNextIndex();
        getIndexCache().put(Integer.valueOf(index), new ByIndexElement(key, buffer));
        return index;
    }

    /**
     * Add a contracted buffer to the cache.
     * @param projectName the project name.
     * @param appendedPath the appended path.
     * @param shortWorkfileName the short workfile name.
     * @param revisionString the revision string.
     * @param buffer the buffer that we add to the cache.
     */
    public void addContractedBuffer(String projectName, String appendedPath, String shortWorkfileName, String revisionString, byte[] buffer) {
        KeyByName key = new KeyByName(projectName, appendedPath, shortWorkfileName, revisionString);
        addContractedBufferByName(key, buffer);
    }

    /**
     * This method is a little subtle. It is meant to be called by the client code after it receives the server response that updates the logfile information for a file that has
     * been checked in. The index value is the 'key' shared between client and server to correlate the checkin request message with the check-in response message. When the client
     * gets the response, the response will include the revisionString for the revision that was checked in (the client doesn't 'know' that -- the server figures it out).
     * @param index the index used to store the associated buffer. The value for the index is created when the buffer is first saved via the
     * {@link #addContractedBuffer(java.lang.String, java.lang.String, java.lang.String, byte[]) } method. It is echoed in the server response, which is how it's value is known
     * for calls to this method.
     * @param revisionString the revision string to associate with the buffer. On a checkin operation, the client sends the checkin request to the server, along with a buffer
     * containing the workfile bytes. But at checkin time, the client does not know the revision string associated with the buffer. When the server sends the response message,
     * the server will have determined the revision string, which is the source of the value of the revision string parameter here.
     * @return the contracted buffer, or null, if we cannot find it.
     */
    public byte[] getContractedBuffer(int index, String revisionString) {
        byte[] buffer = null;
        Integer indexInteger = Integer.valueOf(index);
        ByIndexElement byIndexElement = getIndexCache().get(indexInteger);
        if (byIndexElement != null) {
            buffer = byIndexElement.getBuffer();

            // Remove this entry from the byIndex cache since we don't need it
            // there anymore.
            getIndexCache().remove(indexInteger);

            // Move it to the byName cache (which we limit in size).
            KeyByName key = new KeyByName(byIndexElement.getKeyByName(), revisionString);
            addContractedBufferByName(key, buffer);
        }
        return buffer;
    }

    /**
     * Get the contracted workfile buffer by name.
     * @param project the project name.
     * @param path the appended path.
     * @param shortName the short workfile name.
     * @param revString the revision string.
     * @return the byte[] for the given parameters, or null if it is not found in the cache.
     */
    public byte[] getContractedBufferByName(String project, String path, String shortName, String revString) {
        KeyByName keyByName = new KeyByName(project, path, shortName, revString);
        return getContractedBufferByName(keyByName);
    }

    private byte[] getContractedBufferByName(KeyByName keyByName) {
        byte[] buffer = getByNameCache().get(keyByName);
        return buffer;
    }

    private Map<Integer, ByIndexElement> getIndexCache() {
        return byIndexCache;
    }

    private int getNextIndex() {
        return nextIndex++;
    }

    private Map<KeyByName, byte[]> getByNameCache() {
        return byNameCache;
    }

    private List<KeyByName> getByInsertionOrderList() {
        return byInsertionOrderList;
    }

    private void addContractedBufferByName(KeyByName keyByName, byte[] buffer) {
        // Only add it to the cache if it is not already there.
        if (null == getContractedBufferByName(keyByName)) {
            if (makeRoomForBuffer(buffer.length)) {
                getByInsertionOrderList().add(keyByName);
                getByNameCache().put(keyByName, buffer);
                currentCacheSize += buffer.length;
            }
        }
    }

    /**
     * Make room for the buffer we just received.
     * @param neededBytes the number of bytes that we need for the just received buffer.
     * @return true if we were able to make room for the buffer.
     */
    private boolean makeRoomForBuffer(int neededBytes) {
        boolean madeRoomFlag = false;
        if (neededBytes < MAXIMUM_CACHE_SIZE) {
            if (getCurrentCacheSize() + neededBytes >= MAXIMUM_CACHE_SIZE) {
                // Discard old entries until there is room for the new entry.
                while ((getCurrentCacheSize() + neededBytes > MAXIMUM_CACHE_SIZE) && (getByInsertionOrderList().size() > 0)) {
                    KeyByName keyByName = getByInsertionOrderList().get(0);
                    byte[] buffer = getContractedBufferByName(keyByName);
                    if (buffer != null) {
                        getByNameCache().remove(keyByName);
                        getByInsertionOrderList().remove(0);
                        currentCacheSize -= buffer.length;
                    } else {
                        throw new QVCSRuntimeException("Keyword contracted cache is broken!!");
                    }
                }
                madeRoomFlag = true;
            } else {
                madeRoomFlag = true;
            }
        }
        return madeRoomFlag;
    }

    private int getCurrentCacheSize() {
        return currentCacheSize;
    }

    private static class KeyByName implements Comparable {

        private final String shortWorkfileName;
        private final String projectName;
        private final String appendedPath;
        private final String byNamekey;

        KeyByName(String project, String path, String shortName, String revString) {
            projectName = project;
            appendedPath = Utility.convertToStandardPath(path);
            shortWorkfileName = shortName;
            byNamekey = project + appendedPath + shortName + revString;
        }

        KeyByName(KeyByName oldKey, String revString) {
            projectName = oldKey.projectName;
            appendedPath = oldKey.appendedPath;
            shortWorkfileName = oldKey.shortWorkfileName;
            byNamekey = projectName + appendedPath + shortWorkfileName + revString;
        }

        @Override
        public boolean equals(Object o) {
            boolean retVal = false;
            if (o instanceof KeyByName) {
                KeyByName key = (KeyByName) o;
                retVal = key.byNamekey.equals(byNamekey);
            }
            return retVal;
        }

        @Override
        public int hashCode() {
            return byNamekey.hashCode();
        }

        private String getKey() {
            return byNamekey;
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof KeyByName) {
                KeyByName keyByName = (KeyByName) o;
                return getKey().compareTo(keyByName.getKey());
            } else {
                return -1;
            }
        }
    }

    private static class ByIndexElement {

        private final KeyByName keyByName;
        private final byte[] buffer;

        ByIndexElement(KeyByName byNameKey, byte[] buf) {
            keyByName = byNameKey;
            buffer = buf;
        }

        byte[] getBuffer() {
            return buffer;
        }

        KeyByName getKeyByName() {
            return keyByName;
        }
    }

}
