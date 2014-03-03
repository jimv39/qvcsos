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
package com.qumasoft.guitools.qwin;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;

/**
 * Filter store.
 * @author Jim Voris
 */
public class FilterStore implements Serializable {
    private static final long serialVersionUID = -191772827044011341L;

    /**
     * This map contains the users and their hashed passwords.
     */
    private final Map<String, FilterCollection> filterCollectionMap = Collections.synchronizedMap(new TreeMap<String, FilterCollection>());

    /**
     * Creates a new instance of FilterStore.
     */
    FilterStore() {
    }

    void addFilterCollection(String collectionName, FilterCollection filterCollection) {
        filterCollectionMap.put(collectionName, filterCollection);
        QWinUtility.logProblem(Level.INFO, "adding filter collection: " + collectionName);
    }

    void removeFilterCollection(String collectionName) {
        filterCollectionMap.remove(collectionName);
        QWinUtility.logProblem(Level.INFO, "removing filter collection: " + collectionName);
    }

    FilterCollection[] listFilterCollections() {
        Set<String> keys = filterCollectionMap.keySet();
        FilterCollection[] filterCollections = new FilterCollection[keys.size()];
        int j = 0;
        Iterator<String> i = keys.iterator();
        synchronized (filterCollectionMap) {
            while (i.hasNext()) {
                filterCollections[j++] = filterCollectionMap.get(i.next());
            }
        }
        return filterCollections;
    }

    void dumpMap() {
        Set keys = filterCollectionMap.keySet();
        Iterator i = keys.iterator();
        synchronized (filterCollectionMap) {
            while (i.hasNext()) {
                String filterCollectionName = (String) i.next();
                System.err.println("Filter collection: " + filterCollectionName);
            }
        }
    }
}
