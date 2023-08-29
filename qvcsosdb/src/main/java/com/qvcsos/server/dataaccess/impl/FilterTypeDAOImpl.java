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
import com.qvcsos.server.dataaccess.FilterTypeDAO;
import com.qvcsos.server.datamodel.FilterType;
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
public class FilterTypeDAOImpl implements FilterTypeDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FilterTypeDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int FILTER_TYPE_RESULT_SET_INDEX = 2;

    private final String schemaName;
    private final String findById;
    private final String findAll;

    public FilterTypeDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ID, FILTER_TYPE FROM ";

        this.findById = selectSegment + this.schemaName + ".FILTER_TYPE WHERE ID = ?";
        this.findAll = selectSegment + this.schemaName + ".FILTER_TYPE ORDER BY ID";
    }

    @Override
    public FilterType findById(Integer id) {
        FilterType filterType = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, id);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                String fetchedFilterType = rs.getString(FILTER_TYPE_RESULT_SET_INDEX);

                filterType = new FilterType();
                filterType.setId(fetchedId);
                filterType.setFilterType(fetchedFilterType);
            }
        } catch (SQLException e) {
            LOGGER.error("FilterTypeDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FilterTypeDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return filterType;
    }

    @Override
    public List<FilterType> findAll() {
        List<FilterType> filterTypeList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                String fetchedFilterType = resultSet.getString(FILTER_TYPE_RESULT_SET_INDEX);

                FilterType filterType = new FilterType();
                filterType.setId(fetchedId);
                filterType.setFilterType(fetchedFilterType);

                filterTypeList.add(filterType);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FilterTypeDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return filterTypeList;
    }

}
