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

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checkout comment store.
 * @author Jim Voris
 */
public class CheckOutCommentStore implements java.io.Serializable {
    private static final long serialVersionUID = 6048018477486855481L;
    // Create our logger object
    private static final Logger LOGGER = Logger.getLogger("com.qumasoft.qwin");

    private final Map<String, String> commentMap;

    /**
     * Create the comment store.
     */
    CheckOutCommentStore() {
        this.commentMap = Collections.synchronizedMap(new TreeMap<String, String>());
    }

    /**
     * Store a comment.
     * @param key the key.
     * @param comment the comment.
     */
    void storeComment(String key, String comment) {
        commentMap.put(key, comment);
    }

    /**
     * Does a comment exist.
     * @param key the key for the comment.
     * @return true if the comment exists; false otherwise.
     */
    boolean commentExists(String key) {
        return commentMap.containsKey(key);
    }

    /**
     * Lookup a comment using the given key.
     * @param key the key for lookup.
     * @return the comment string.
     */
    String lookupComment(String key) {
        return commentMap.get(key);
    }

    /**
     * Remove a comment from the store.
     * @param key the comment's key value.
     */
    void removeComment(String key) {
        commentMap.remove(key);
    }

    /**
     * Dump the contents of the store.
     */
    void dumpMap() {
        LOGGER.log(Level.INFO, "CheckOutCommentStore.dumpMap()");
        Set keys = commentMap.keySet();
        Iterator i = keys.iterator();
        while (i.hasNext()) {
            LOGGER.log(Level.INFO, i.next().toString());
        }
    }
}
