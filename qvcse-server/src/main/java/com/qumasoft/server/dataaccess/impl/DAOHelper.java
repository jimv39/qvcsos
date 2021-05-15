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
package com.qumasoft.server.dataaccess.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;

/**
 * DAO helper for DAO common code.
 * @author Jim Voris
 */
public final class DAOHelper {

    private DAOHelper() {
    }

    /**
     * Close db resources.
     * @param logger the logger to use if we need to report a problem.
     * @param resultSet the result set to close.
     * @param preparedStatement the prepared statement to close.
     */
    public static void closeDbResources(Logger logger, ResultSet resultSet, PreparedStatement preparedStatement) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("DirectoryHistoryDAOImpl: exception closing resultSet", e);
            }
        }
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                logger.error("DirectoryHistoryDAOImpl: exception closing preparedStatment", e);
            }
        }
    }
}
