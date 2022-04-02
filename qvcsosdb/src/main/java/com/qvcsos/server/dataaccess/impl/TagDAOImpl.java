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

import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.TagDAO;
import com.qvcsos.server.datamodel.Tag;
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
public class TagDAOImpl implements TagDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(UserDAOImpl.class);
    private static final int ID_RESULT_SET_INDEX = 1;
    private static final int COMMIT_ID_RESULT_SET_INDEX = 2;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 3;
    private static final int MOVEABLE_FLAG_RESULT_SET_INDEX = 4;
    private static final int TAG_TEXT_RESULT_SET_INDEX = 5;
    private static final int DESCRIPTION_RESULT_SET_INDEX = 6;

    private final String schemaName;
    private final String findById;
    private final String findAll;
    private final String findByBranchId;
    private final String findByBranchIdAndTagText;

    private final String updateMoveableCommitId;
    private final String insert;

    public TagDAOImpl(String schema) {
        this.schemaName = schema;

        String selectSegment = "SELECT ID, COMMIT_ID, BRANCH_ID, MOVEABLE_FLAG, TAG_TEXT, DESCRIPTION FROM ";

        this.findById = selectSegment + this.schemaName + ".TAG WHERE ID = ?";
        this.findAll = selectSegment + this.schemaName + ".TAG ORDER BY ID DESC";
        this.findByBranchId = selectSegment + this.schemaName + ".TAG WHERE BRANCH_ID = ? ORDER BY ID DESC";
        this.findByBranchIdAndTagText = selectSegment + this.schemaName + ".TAG WHERE BRANCH_ID = ? AND TAG_TEXT = ?";

        this.updateMoveableCommitId = "UPDATE " + this.schemaName + ".TAG SET COMMIT_ID = ? WHERE ID = ? RETURNING ID";
        this.insert = "INSERT INTO " + this.schemaName + ".TAG (COMMIT_ID, BRANCH_ID, MOVEABLE_FLAG, TAG_TEXT, DESCRIPTION) VALUES (?, ?, ?, ?, ?) RETURNING ID";
    }

    @Override
    public Tag findById(Integer tagId) {
        Tag tag = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findById, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, tagId);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = rs.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = rs.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Boolean fetchedMoveableFlag = rs.getBoolean(MOVEABLE_FLAG_RESULT_SET_INDEX);
                String fetchedTagText = rs.getString(TAG_TEXT_RESULT_SET_INDEX);
                String fetchedDescription = null;
                Object fetchedDescriptionObject = rs.getObject(DESCRIPTION_RESULT_SET_INDEX);
                if (fetchedDescriptionObject != null) {
                    fetchedDescription = rs.getString(DESCRIPTION_RESULT_SET_INDEX);
                }

                tag = new Tag();
                tag.setId(id);
                tag.setCommitId(fetchedCommitId);
                tag.setBranchId(fetchedBranchId);
                tag.setMoveableFlag(fetchedMoveableFlag);
                tag.setTagText(fetchedTagText);
                tag.setDescription(fetchedDescription);
            }
        } catch (SQLException e) {
            LOGGER.error("TagDAOImpl: SQL exception in findById", e);
        } catch (IllegalStateException e) {
            LOGGER.error("TagDAOImpl: exception in findById", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return tag;
    }

    @Override
    public List<Tag> findAll() {
        List<Tag> tagList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findAll, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = rs.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = rs.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Boolean fetchedMoveableFlag = rs.getBoolean(MOVEABLE_FLAG_RESULT_SET_INDEX);
                String fetchedTagText = rs.getString(TAG_TEXT_RESULT_SET_INDEX);
                String fetchedDescription = null;
                Object fetchedDescriptionObject = rs.getObject(DESCRIPTION_RESULT_SET_INDEX);
                if (fetchedDescriptionObject != null) {
                    fetchedDescription = rs.getString(DESCRIPTION_RESULT_SET_INDEX);
                }

                Tag tag = new Tag();
                tag.setId(id);
                tag.setCommitId(fetchedCommitId);
                tag.setMoveableFlag(fetchedMoveableFlag);
                tag.setBranchId(fetchedBranchId);
                tag.setTagText(fetchedTagText);
                tag.setDescription(fetchedDescription);

                tagList.add(tag);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("TagDAOImpl: exception in findAll", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return tagList;
    }

    @Override
    public List<Tag> findByBranchId(Integer branchId) {
        List<Tag> tagList = new ArrayList<>();
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByBranchId, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            rs = preparedStatement.executeQuery();
            while (rs.next()) {
                Integer id = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = rs.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = rs.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Boolean fetchedMoveableFlag = rs.getBoolean(MOVEABLE_FLAG_RESULT_SET_INDEX);
                String fetchedTagText = rs.getString(TAG_TEXT_RESULT_SET_INDEX);
                String fetchedDescription = null;
                Object fetchedDescriptionObject = rs.getObject(DESCRIPTION_RESULT_SET_INDEX);
                if (fetchedDescriptionObject != null) {
                    fetchedDescription = rs.getString(DESCRIPTION_RESULT_SET_INDEX);
                }

                Tag tag = new Tag();
                tag.setId(id);
                tag.setCommitId(fetchedCommitId);
                tag.setBranchId(fetchedBranchId);
                tag.setMoveableFlag(fetchedMoveableFlag);
                tag.setTagText(fetchedTagText);
                tag.setDescription(fetchedDescription);

                tagList.add(tag);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("TagDAOImpl: exception in findByBranchId", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return tagList;
    }

    @Override
    public Tag findByBranchIdAndTagText(Integer branchId, String tagText) {
        Tag tag = null;
        ResultSet rs = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.findByBranchIdAndTagText, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setString(2, tagText);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                Integer fetchedId = rs.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = rs.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = rs.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Boolean fetchedMoveableFlag = rs.getBoolean(MOVEABLE_FLAG_RESULT_SET_INDEX);
                String fetchedTagText = rs.getString(TAG_TEXT_RESULT_SET_INDEX);
                String fetchedDescription = null;
                Object fetchedDescriptionObject = rs.getObject(DESCRIPTION_RESULT_SET_INDEX);
                if (fetchedDescriptionObject != null) {
                    fetchedDescription = rs.getString(DESCRIPTION_RESULT_SET_INDEX);
                }

                tag = new Tag();
                tag.setId(fetchedId);
                tag.setCommitId(fetchedCommitId);
                tag.setBranchId(fetchedBranchId);
                tag.setMoveableFlag(fetchedMoveableFlag);
                tag.setTagText(fetchedTagText);
                tag.setDescription(fetchedDescription);
            }
        } catch (SQLException e) {
            LOGGER.error("TagDAOImpl: SQL exception in findByBranchIdAndTagText", e);
        } catch (IllegalStateException e) {
            LOGGER.error("TagDAOImpl: exception in findByBranchIdAndTagText", e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return tag;
    }

    @Override
    public Integer insert(Tag tag) throws SQLException {
        PreparedStatement preparedStatement = null;
        ResultSet rs = null;
        Integer returnId = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.insert);
            // <editor-fold>
            preparedStatement.setInt(1, tag.getCommitId());
            preparedStatement.setInt(2, tag.getBranchId());
            preparedStatement.setBoolean(3, tag.getMoveableFlag());
            preparedStatement.setString(4, tag.getTagText());
            if (tag.getDescription() != null) {
                preparedStatement.setString(5, tag.getDescription());
            } else {
                preparedStatement.setNull(5, java.sql.Types.VARCHAR);
            }
            // </editor-fold>

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnId = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("TagDAOImpl: exception in insert", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnId;
    }

    @Override
    public Integer updateMoveableCommitId(Integer tagId, Integer newCommitId) throws SQLException {
        ResultSet rs = null;
        Tag oldTag = findById(tagId);
        if (!oldTag.getMoveableFlag()) {
            throw new QVCSRuntimeException("Cannot move a non-moveable tag!");
        }
        PreparedStatement preparedStatement = null;
        Integer returnIdFlag = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(this.updateMoveableCommitId);
            preparedStatement.setInt(1, newCommitId);
            preparedStatement.setInt(2, tagId);

            rs = preparedStatement.executeQuery();
            if (rs.next()) {
                returnIdFlag = rs.getInt(1);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("TagDAOImpl: exception in updateMoveableCommitId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, rs, preparedStatement);
        }
        return returnIdFlag;
    }
}
