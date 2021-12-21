/*
 * Copyright 2021 Jim Voris.
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
import com.qvcsos.server.dataaccess.PrivilegedActionDAO;
import com.qvcsos.server.datamodel.PrivilegedAction;
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
public class PrivilegedActionDAOImpl implements PrivilegedActionDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(RoleTypeActionJoinDAOImpl.class);
    private static final int ACTION_ID_RESULT_SET_INDEX = 1;
    private static final int ACTION_NAME_RESULT_SET_INDEX = 2;
    private static final int ADMIN_ONLY_FLAG_RESULT_SET_INDEX = 3;

    private final String schemaName;

    private final String findAll;

    public PrivilegedActionDAOImpl(String schema) {
        this.schemaName = schema;
        String selectSegment = "SELECT ACTION_ID, ACTION_NAME, ADMIN_ONLY_FLAG FROM ";

        this.findAll = selectSegment + this.schemaName + ".PRIVILEGED_ACTION ORDER BY ACTION_ID";

    }

    @Override
    public List<PrivilegedAction> findAll() {
        List<PrivilegedAction> privilegedActionList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer fetchedActionId = rs.getInt(ACTION_ID_RESULT_SET_INDEX);
                String fetchedActionName = rs.getString(ACTION_NAME_RESULT_SET_INDEX);
                Boolean fetchedAdminOnlyFlag = rs.getBoolean(ADMIN_ONLY_FLAG_RESULT_SET_INDEX);

                PrivilegedAction privilegedAction = new PrivilegedAction();
                privilegedAction.setId(fetchedActionId);
                privilegedAction.setActionName(fetchedActionName);
                privilegedAction.setAdminOnlyFlag(fetchedAdminOnlyFlag);
                privilegedActionList.add(privilegedAction);
            }
        } catch (SQLException e) {
            LOGGER.error("PrivilegedActionDAOImpl: SQL exception in findAll", e);
        } catch (IllegalStateException e) {
            LOGGER.error("PrivilegedActionDAOImpl: exception in findAll", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return privilegedActionList;
    }

}
