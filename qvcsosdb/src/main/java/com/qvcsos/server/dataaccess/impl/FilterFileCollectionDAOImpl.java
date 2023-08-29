/*
 * Copyright 2023 Jim Voris.
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
package com.qvcsos.server.dataaccess.impl;

import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.datamodel.FilterFileCollection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.qvcsos.server.dataaccess.FilterFileCollectionDAO;

/**
 *
 * @author Jim Voris
 */
public class FilterFileCollectionDAOImpl implements FilterFileCollectionDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterFileCollectionDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int USER_ID_RESULT_SET_INDEX = 2;
    private static final int BUILT_IN_FLAG_RESULT_SET_INDEX = 3;
    private static final int ASSOCIATED_PROJECT_ID_RESULT_SET_INDEX = 4;
    private static final int COLLECTION_NAME_RESULT_SET_INDEX = 5;

    private final String schemaName;
    private final String findById;
    private final String findAllByUserId;

    private final String insert;
    private final String delete;

    public FilterFileCollectionDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, USER_ID, BUILT_IN_FLAG, ASSOCIATED_PROJECT_ID, COLLECTION_NAME FROM ";

        this.findById = selectSegment + this.schemaName + ".FILTER_COLLECTION WHERE ID = ?";
        this.findAllByUserId = selectSegment + this.schemaName + ".FILTER_COLLECTION WHERE USER_ID = ? OR USER_ID = 1 ORDER BY ID";

        this.insert = "INSERT INTO " + this.schemaName + ".FILTER_COLLECTION (USER_ID, BUILT_IN_FLAG, ASSOCIATED_PROJECT_ID, COLLECTION_NAME) VALUES (?, ?, ?, ?) RETURNING ID";
        this.delete = "DELETE FROM " + this.schemaName + ".FILTER_COLLECTION WHERE ID = ?";
    }

    @Override
    public FilterFileCollection findById(Integer id) {
        FilterFileCollection filterCollection = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedUserId = rs.getInt(USER_ID_RESULT_SET_INDEX);
                Boolean fetchedBuiltInFlag = rs.getBoolean(BUILT_IN_FLAG_RESULT_SET_INDEX);
                Integer fetchedAssociatedProjectId = rs.getInt(ASSOCIATED_PROJECT_ID_RESULT_SET_INDEX);
                String fetchedCollectionName = rs.getString(COLLECTION_NAME_RESULT_SET_INDEX);

                filterCollection = new FilterFileCollection();
                filterCollection.setId(fetchedId);
                filterCollection.setUserId(fetchedUserId);
                filterCollection.setBuiltInFlag(fetchedBuiltInFlag);
                filterCollection.setAssociatedProjectId(fetchedAssociatedProjectId);
                filterCollection.setCollectionName(fetchedCollectionName);
            }
        } catch (SQLException e) {
            LOGGER.error("FilterCollectionDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FilterCollectionDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return filterCollection;
    }

    @Override
    public List<FilterFileCollection> findAllByUserId(Integer userId) {
        List<FilterFileCollection> filterCollectionList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findAllByUserId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, userId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedUserId = rs.getInt(USER_ID_RESULT_SET_INDEX);
                Boolean fetchedBuiltInFlag = rs.getBoolean(BUILT_IN_FLAG_RESULT_SET_INDEX);
                Integer fetchedAssociatedProjectId = rs.getInt(ASSOCIATED_PROJECT_ID_RESULT_SET_INDEX);
                String fetchedCollectionName = rs.getString(COLLECTION_NAME_RESULT_SET_INDEX);

                FilterFileCollection filterCollection = new FilterFileCollection();
                filterCollection.setId(fetchedId);
                filterCollection.setUserId(fetchedUserId);
                filterCollection.setBuiltInFlag(fetchedBuiltInFlag);
                filterCollection.setAssociatedProjectId(fetchedAssociatedProjectId);
                filterCollection.setCollectionName(fetchedCollectionName);

                filterCollectionList.add(filterCollection);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FilterCollectionDAOImpl: exception in findAllByUserId", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return filterCollectionList;
    }

    @Override
    public Integer insert(FilterFileCollection filterCollection) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            preparedStatement.setInt(1,filterCollection.getUserId());
            preparedStatement.setBoolean(2, filterCollection.getBuiltInFlag());
            if (filterCollection.getAssociatedProjectId() == null) {
                preparedStatement.setNull(3, java.sql.Types.INTEGER);
            } else {
                preparedStatement.setInt(3, filterCollection.getAssociatedProjectId());
            }
            preparedStatement.setString(4, filterCollection.getCollectionName());
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("FilterCollectionDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public void delete(Integer filterCollectionId) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.delete);
            preparedStatement.setInt(1, filterCollectionId);

            preparedStatement.execute();
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FilterCollectionDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
    }
}
