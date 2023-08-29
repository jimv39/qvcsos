/*   Copyright 2004-2023 Jim Voris
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
package com.qumasoft.guitools.qwin;

import com.qumasoft.guitools.qwin.filefilter.FileFilterInterface;
import com.qumasoft.guitools.qwin.filefilter.FilterFactory;
import com.qumasoft.qvcslib.ClientTransactionManager;
import com.qumasoft.qvcslib.CommonFilterFile;
import com.qumasoft.qvcslib.CommonFilterFileCollection;
import com.qumasoft.qvcslib.FileFilterResponseListenerInterface;
import com.qumasoft.qvcslib.SynchronizationManager;
import com.qumasoft.qvcslib.TransportProxyFactory;
import com.qumasoft.qvcslib.TransportProxyInterface;
import com.qumasoft.qvcslib.requestdata.ClientRequestUpdateFilterFileCollectionData;
import com.qumasoft.qvcslib.response.ServerResponseLogin;
import com.qumasoft.qvcslib.response.ServerResponseUpdateFilterFileCollection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class to manage filters for QVCS-Enterprise client.
 *
 * @author Jim Voris
 */
public final class FilterManager implements FileFilterResponseListenerInterface {

    private static final FilterManager FILTER_MANAGER = new FilterManager();
    private boolean isInitializedFlag = false;

    /** A Map (keyed by server name) of maps keyed by collection id. */
    private final Map<String, Map<Integer, FilterCollection>> filterCollectionByServerMap;
    /** Transport proxy map keyed by server name. */
    private final Map<String, TransportProxyInterface> transPortProxyMap = Collections.synchronizedMap(new TreeMap<>());

    /**
     * Creates a new instance of FilterManager.
     */
    private FilterManager() {
        this.filterCollectionByServerMap = new TreeMap<>();
    }

    /**
     * Get the filter manager singleton.
     * @return the filter manager singleton.
     */
    public static FilterManager getFilterManager() {
        return FILTER_MANAGER;
    }

    /**
     * Initialize the filter manager.
     * @param serverResponseLogin the login response that contains filter information.
     * @param transportProxy our connection to the server.
     * @return true if initialization was successful; false if not.
     */
    public boolean initialize(ServerResponseLogin serverResponseLogin, TransportProxyInterface transportProxy) {
        String serverName = serverResponseLogin.getServerName();
        transPortProxyMap.put(serverName, transportProxy);
        Map<Integer, FilterCollection> filterCollectionsMap = createOrGetFilterCollectionMap(serverName);
        TransportProxyFactory.getInstance().addFileFilterResponseListener(this);
        List<CommonFilterFileCollection> commonFilterCollectionList = serverResponseLogin.getFilterCollectionList();
        for (CommonFilterFileCollection cffc : commonFilterCollectionList) {
            FilterCollection fc = new FilterCollection(cffc.getId(), cffc.getCollectionName(), cffc.getBuiltInFlag(), cffc.getAssociatedProjectName());
            for (CommonFilterFile cff : cffc.getFilterFileList()) {
                fc.addFilter(FilterFactory.buildFilter(cff.getFilterType(), cff.getFilterData(), cff.getIsAndFlag(), cff.getId()));
            }
            filterCollectionsMap.put(cffc.getId(), fc);
        }
        isInitializedFlag = true;
        return isInitializedFlag;
    }

    /**
     * Reset the collections to the default set of filter collections.
     * @param serverName the server name.
     */
    public void resetCollections(String serverName) {
        Map<Integer, FilterCollection> map = this.filterCollectionByServerMap.get(serverName);
        List<Integer> collectionIdList = new ArrayList<>();
        for (FilterCollection fc : map.values()) {
            if (!fc.getIsBuiltInCollection()) {
                collectionIdList.add(fc.getCollectionId());
            }
        }
        for (Integer cId : collectionIdList) {
            map.remove(cId);
        }
        String userName = QWinFrame.getQWinFrame().getLoggedInUserName();
        ClientRequestUpdateFilterFileCollectionData cruffc = new ClientRequestUpdateFilterFileCollectionData();
        cruffc.setUpdateType(ClientRequestUpdateFilterFileCollectionData.RESET_FILE_FILTER_COLLECTION_REQUEST);
        cruffc.setUserName(userName);
        cruffc.setServerName(serverName);
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transPortProxyMap.get(serverName));
        SynchronizationManager.getSynchronizationManager().waitOnToken(transPortProxyMap.get(serverName), cruffc);
        ClientTransactionManager.getInstance().sendEndTransaction(transPortProxyMap.get(serverName), transactionID);
    }

    private Map<Integer, FilterCollection> createOrGetFilterCollectionMap(String serverName) {
        Map<Integer, FilterCollection> map = this.filterCollectionByServerMap.get(serverName);
        if (map == null) {
            map = new TreeMap<>();
            this.filterCollectionByServerMap.put(serverName, map);
        }
        return map;
    }

    /**
     * Get the list of filter collections.
     * @param serverName the server name.
     * @return the list of filter collections.
     */
    public FilterCollection[] listFilterCollections(String serverName) {
        Map<Integer, FilterCollection> map = this.filterCollectionByServerMap.get(serverName);
        FilterCollection[] filterCollections = new FilterCollection[map.size()];
        int index = 0;
        for (FilterCollection fc : map.values()) {
            filterCollections[index++] = fc;
        }
        return filterCollections;
    }

    public void addFilterCollections(String serverName, List<FilterCollection> fcList) {
        String userName = QWinFrame.getQWinFrame().getLoggedInUserName();
        ClientRequestUpdateFilterFileCollectionData cruffc = new ClientRequestUpdateFilterFileCollectionData();
        cruffc.setUpdateType(ClientRequestUpdateFilterFileCollectionData.ADD_FILE_FILTER_COLLECTION_REQUEST);
        cruffc.setUserName(userName);
        cruffc.setServerName(serverName);
        for (FilterCollection fc : fcList) {
            addFilterCollection(serverName, cruffc, fc);
        }
        int transactionID = ClientTransactionManager.getInstance().sendBeginTransaction(transPortProxyMap.get(serverName));
        SynchronizationManager.getSynchronizationManager().waitOnToken(transPortProxyMap.get(serverName), cruffc);
        ClientTransactionManager.getInstance().sendEndTransaction(transPortProxyMap.get(serverName), transactionID);
    }

    private void addFilterCollection(String serverName, ClientRequestUpdateFilterFileCollectionData cruffc, FilterCollection filterCollection) {
        CommonFilterFileCollection cffc = new CommonFilterFileCollection();
        cffc.setAssociatedProjectName(filterCollection.getAssociatedProjectName());
        cffc.setBuiltInFlag(filterCollection.getIsBuiltInCollection());
        cffc.setCollectionName(filterCollection.getCollectionName());
        cffc.setId(filterCollection.getCollectionId());
        List<CommonFilterFile> cffList = new ArrayList<>();
        for (FileFilterInterface filter : filterCollection.listFilters()) {
            CommonFilterFile cff = new CommonFilterFile();
            cff.setFilterCollectionId(filterCollection.getCollectionId());
            cff.setFilterData(filter.getFilterData());
            cff.setFilterType(filter.getFilterType());
            cff.setFilterTypeId(filter.getFilterTypeId());
            cff.setId(filter.getFilterId());
            cff.setIsAndFlag(filter.getIsANDFilter());
            cffList.add(cff);
        }
        cffc.setFilterFileList(cffList);
        cruffc.addCommonFilterFileCollection(cffc);
    }

    @Override
    public void notifyFileFilterResponse(ServerResponseUpdateFilterFileCollection response) {
        String serverName = response.getServerName();
        Map<Integer, FilterCollection> filterCollectionsMap = createOrGetFilterCollectionMap(serverName);
        List<CommonFilterFileCollection> commonFilterCollectionList = response.getCommonFilterFileCollectionList();
        for (CommonFilterFileCollection cffc : commonFilterCollectionList) {
            FilterCollection fc = new FilterCollection(cffc.getId(), cffc.getCollectionName(), cffc.getBuiltInFlag(), cffc.getAssociatedProjectName());
            for (CommonFilterFile cff : cffc.getFilterFileList()) {
                fc.addFilter(FilterFactory.buildFilter(cff.getFilterType(), cff.getFilterData(), cff.getIsAndFlag(), cff.getId()));
            }
            filterCollectionsMap.put(cffc.getId(), fc);
        }
    }

}
