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

import com.qumasoft.guitools.qwin.filefilter.FileFilterInterface;
import com.qumasoft.guitools.qwin.filefilter.FileFilterExcludeObsoleteFilter;
import com.qumasoft.guitools.qwin.filefilter.FilterFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Filter collection. A filter collection defines a set of filters that are applied together to limit the set of files/revisions that are displayed.
 * @author Jim Voris
 */
public class FilterCollection implements java.io.Serializable {
    private static final long serialVersionUID = -5732984702711323785L;

    private final List<FileFilterInterface> filterList;
    private String collectionName;
    private final boolean isBuiltInCollectionFlag;
    private String associateWithProjectName;

    /**
     * Create a filter collection into which we'll add filters. This constructor is used when creating a new filter collection.
     * @param collection the name of the collection.
     * @param isBuiltInCollection is this a built-in collection.
     * @param associateWithProject associate the collection with the given project.
     */
    public FilterCollection(String collection, boolean isBuiltInCollection, String associateWithProject) {
        this.filterList = Collections.synchronizedList(new ArrayList<FileFilterInterface>());
        this.collectionName = collection;
        this.isBuiltInCollectionFlag = isBuiltInCollection;
        this.associateWithProjectName = associateWithProject;
    }

    /**
     * Create a filter collection that is a copy of an existing filter collection. Use this constructor when editing an existing filter collection, or when copying an existing
     * filter collection.
     * @param collection the name of the collection.
     * @param associateWithProject the project with which to associate the collection.
     * @param existingCollection the collection that this new collection becomes a copy of.
     */
    public FilterCollection(String collection, String associateWithProject, final FilterCollection existingCollection) {
        this.filterList = Collections.synchronizedList(new ArrayList<FileFilterInterface>());
        this.collectionName = collection;
        isBuiltInCollectionFlag = false;
        this.associateWithProjectName = associateWithProject;

        FileFilterInterface[] filters = existingCollection.listFilters();
        for (FileFilterInterface filter1 : filters) {
            FileFilterInterface filter = FilterFactory.buildFilter(filter1.getFilterType(), filter1.getFilterData(), filter1.getIsANDFilter());
            addFilter(filter);
        }
    }

    /**
     * Get the collection name.
     * @return the collection name.
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * Set the collection name.
     * @param collection the collection name.
     */
    public void setCollectionName(String collection) {
        this.collectionName = collection;
    }

    /**
     * Get the associated project name.
     * @return the associated project name.
     */
    public String getAssociatedProjectName() {
        if (associateWithProjectName == null) {
            associateWithProjectName = QWinFrame.GLOBAL_PROJECT_NAME;
        }
        return associateWithProjectName;
    }

    /**
     * Is this a built-in collection.
     * @return true if a built-in collection; false otherwise.
     */
    public boolean getIsBuiltInCollection() {
        return isBuiltInCollectionFlag;
    }

    /**
     * Add a file filter to this collection.
     * @param filter the filter to add.
     * @return the new size of the filter collection.
     */
    public final int addFilter(FileFilterInterface filter) {
        if (!(filter instanceof FileFilterExcludeObsoleteFilter)) {
            filterList.add(filter);
        }
        return filterList.size();
    }

    /**
     * Remove a filter from the filter collection.
     * @param filter the filter to remove.
     */
    public void removeFilter(FileFilterInterface filter) {
        filterList.remove(filter);
    }

    /**
     * List the filters in the filter collection.
     * @return the list of filters in the filter collection.
     */
    public FileFilterInterface[] listFilters() {
        FileFilterInterface[] filters = new FileFilterInterface[filterList.size()];
        int j = 0;
        Iterator i = filterList.iterator();
        synchronized (filterList) {
            while (i.hasNext()) {
                FileFilterInterface filter = (FileFilterInterface) i.next();
                filters[j++] = filter;
            }
        }
        return filters;
    }

    @Override
    public String toString() {
        return getCollectionName();
    }
}
