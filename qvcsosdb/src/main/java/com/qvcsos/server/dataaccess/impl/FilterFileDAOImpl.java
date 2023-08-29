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
import com.qvcsos.server.dataaccess.FilterFileDAO;
import com.qvcsos.server.datamodel.FilterFile;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class FilterFileDAOImpl implements FilterFileDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterFileDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int FILTER_COLLECTION_ID_RESULT_SET_INDEX = 2;
    private static final int FILTER_TYPE_ID_RESULT_SET_INDEX = 3;
    private static final int IS_AND_FLAG_RESULT_SET_INDEX = 4;
    private static final int FILTER_DATA_RESULT_SET_INDEX = 5;

    private final String schemaName;
    private final String findById;
    private final String findByCollectionId;

    private final String insert;
    private final String delete;

    public FilterFileDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, FILTER_COLLECTION_ID, FILTER_TYPE_ID, IS_AND_FLAG, FILTER_DATA FROM ";

        this.findById = selectSegment + this.schemaName + ".FILTER_FILE WHERE ID = ?";
        this.findByCollectionId = selectSegment + this.schemaName + ".FILTER_FILE WHERE FILTER_COLLECTION_ID = ? ORDER BY ID";

        this.insert = "INSERT INTO " + this.schemaName + ".FILTER_FILE (FILTER_COLLECTION_ID, FILTER_TYPE_ID, IS_AND_FLAG, FILTER_DATA) VALUES (?, ?, ?, ?) RETURNING ID";
        this.delete = "DELETE FROM " + this.schemaName + ".FILTER_FILE WHERE ID = ?";
    }

    @Override
    public FilterFile findById(Integer id) {
        FilterFile filterFile = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedFilterCollectionId = rs.getInt(FILTER_COLLECTION_ID_RESULT_SET_INDEX);
                Integer fetchedFilterTypeId = rs.getInt(FILTER_TYPE_ID_RESULT_SET_INDEX);
                Boolean fetchedIsAndFlag = rs.getBoolean(IS_AND_FLAG_RESULT_SET_INDEX);
                String fetchedFilterData = rs.getString(FILTER_DATA_RESULT_SET_INDEX);

                filterFile = new FilterFile();
                filterFile.setId(fetchedId);
                filterFile.setFilterCollectionId(fetchedFilterCollectionId);
                filterFile.setFilterTypeId(fetchedFilterTypeId);
                filterFile.setIsAndFlag(fetchedIsAndFlag);
                filterFile.setFilterData(fetchedFilterData);
            }
        } catch (SQLException e) {
            LOGGER.error("FilterFileDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FilterFileDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return filterFile;
    }

    @Override
    public List<FilterFile> findByCollectionId(Integer collectionId) {
        List<FilterFile> filterFileList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByCollectionId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, collectionId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedFilterCollectionId = rs.getInt(FILTER_COLLECTION_ID_RESULT_SET_INDEX);
                Integer fetchedFilterTypeId = rs.getInt(FILTER_TYPE_ID_RESULT_SET_INDEX);
                Boolean fetchedIsAndFlag = rs.getBoolean(IS_AND_FLAG_RESULT_SET_INDEX);
                String fetchedFilterData = rs.getString(FILTER_DATA_RESULT_SET_INDEX);

                FilterFile filterFile = new FilterFile();
                filterFile.setId(fetchedId);
                filterFile.setFilterCollectionId(fetchedFilterCollectionId);
                filterFile.setFilterTypeId(fetchedFilterTypeId);
                filterFile.setIsAndFlag(fetchedIsAndFlag);
                filterFile.setFilterData(fetchedFilterData);

                filterFileList.add(filterFile);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FilterFileDAOImpl: exception in findByCollectionId", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return filterFileList;
    }

    @Override
    public Integer insert(FilterFile filterFile) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            preparedStatement.setInt(1,filterFile.getFilterCollectionId());
            preparedStatement.setInt(2, filterFile.getFilterTypeId());
            preparedStatement.setBoolean(3, filterFile.getIsAndFlag());
            if (filterFile.getFilterData() == null) {
                preparedStatement.setNull(4, java.sql.Types.VARCHAR);
            } else {
                preparedStatement.setString(4, filterFile.getFilterData());
            }
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("FilterFileDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public void delete(Integer id) throws SQLException {
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.delete);
            preparedStatement.setInt(1, id);

            preparedStatement.execute();
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FilterFileDAOImpl: exception in delete", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, null, preparedStatement);
        }
    }
}
