/*
 * Copyright 2021-2022 Jim Voris.
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

import com.qumasoft.qvcslib.ArchiveAttributes;
import com.qumasoft.qvcslib.DirectoryCoordinate;
import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.LogFileHeaderInfo;
import com.qumasoft.qvcslib.LogfileInfo;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.QVCSRuntimeException;
import com.qumasoft.qvcslib.RevisionHeader;
import com.qumasoft.qvcslib.RevisionInformation;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qumasoft.qvcslib.TagInfoData;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.SourceControlBehaviorManager;
import com.qvcsos.server.dataaccess.BranchDAO;
import com.qvcsos.server.dataaccess.CommitDAO;
import com.qvcsos.server.dataaccess.FileNameDAO;
import com.qvcsos.server.dataaccess.FileRevisionDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.ProjectDAO;
import com.qvcsos.server.dataaccess.TagDAO;
import com.qvcsos.server.dataaccess.UserDAO;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.Commit;
import com.qvcsos.server.datamodel.DirectoryLocation;
import com.qvcsos.server.datamodel.FileRevision;
import com.qvcsos.server.datamodel.Project;
import com.qvcsos.server.datamodel.Tag;
import com.qvcsos.server.datamodel.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that supplies the multi-table join queries so we only have a single
 * round trip to the database.
 *
 * @author Jim Voris
 */
public class FunctionalQueriesDAOImpl implements FunctionalQueriesDAO {

    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalQueriesDAOImpl.class);

    private final String schemaName;

    public FunctionalQueriesDAOImpl(String schema) {
        this.schemaName = schema;
    }

    @Override
    public SkinnyLogfileInfo getSkinnyLogfileInfo(Integer fileRevisionId) {
        SkinnyLogfileInfo skinnyInfo = null;
        // <editor-fold>
        int USER_NAME_SET_INDEX = 1;
        int COMMIT_DATE_RESULT_SET_INDEX = 2;
        int FILE_NAME_RESULT_SET_INDEX = 3;
        int FILE_REVISION_ID_RESULT_SET_INDEX = 4;
        int FILE_ID_RESULT_SET_INDEX = 5;
        int BRANCH_ID_RESULT_SET_INDEX = 6;
        int REVISION_DIGEST_RESULT_SET_INDEX = 7;
        // </editor-fold>

        String selectSegment = "SELECT U.USER_NAME, CM.COMMIT_DATE, FN.FILE_NAME, FR.ID AS FRID, FR.FILE_ID, FR.BRANCH_ID, FR.REVISION_DIGEST FROM ";
        String queryString = new StringBuilder(selectSegment)
                .append(this.schemaName).append(".FILE_NAME FN,")
                .append(this.schemaName).append(".DIRECTORY_LOCATION DL,")
                .append(this.schemaName).append(".FILE_REVISION FR,")
                .append(this.schemaName).append(".COMIT CM,")
                .append(this.schemaName).append(".USER U ")
                .append("WHERE ")
                .append("FR.ID = ? AND ")
                .append("FR.COMMIT_ID = CM.ID AND ")
                .append("FN.ID = (SELECT SFN.ID FROM ")
                .append(this.schemaName).append(".FILE_NAME SFN,")
                .append(this.schemaName).append(".FILE_REVISION SFR ")
                .append("WHERE ")
                .append("SFN.BRANCH_ID <= SFR.BRANCH_ID AND ")
                .append("SFN.FILE_ID = SFR.FILE_ID AND ")
                .append("SFR.ID = ? ORDER BY SFN.BRANCH_ID DESC LIMIT 1) AND ")
                .append("CM.USER_ID = U.ID ").toString();
        // TODO -- in the above sub-query (and query?), do I need to a WHERE FN.PROMOTED_FLAG = FALSE ??
        ResultSet resultSet = null;
        LOGGER.debug("getSkinnyLogfileInfo query string: [{}]", queryString);
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileRevisionId);
            preparedStatement.setInt(2, fileRevisionId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                String fetchedUserName = resultSet.getString(USER_NAME_SET_INDEX);
                Date fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Integer fetchedFileRevisionId = resultSet.getInt(FILE_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);

                skinnyInfo = new SkinnyLogfileInfo();
                skinnyInfo.setLastEditByString(fetchedUserName);
                skinnyInfo.setLastCheckInDate(fetchedCommitDate);
                skinnyInfo.setShortWorkfileName(fetchedFilename);
                skinnyInfo.setDefaultRevisionString(String.format("%d.%d", fetchedBranchId, fetchedFileRevisionId));
                skinnyInfo.setBranchId(fetchedBranchId);
                skinnyInfo.setFileRevisionId(fileRevisionId);
                skinnyInfo.setFileID(fetchedFileId);
                skinnyInfo.setDefaultRevisionDigest(fetchedDigest);
                skinnyInfo.setRevisionCount(1);
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in getSkinnyLogfileInfo", e);
            throw new RuntimeException(e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return skinnyInfo;
    }

    @Override
    public SkinnyLogfileInfo getSkinnyLogfileInfoForGet(Integer fileRevisionId) {
        SkinnyLogfileInfo skinnyLogfileInfo = null;
        // First get the FileRevision object as it has some useful info in there.
        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        FileRevision fileRevision = fileRevisionDAO.findById(fileRevisionId);
        List<Branch> branchAncestryList = getBranchAncestryList(fileRevision.getBranchId());

        // <editor-fold>
        int USER_NAME_SET_INDEX = 1;
        int COMMIT_DATE_RESULT_SET_INDEX = 2;
        int FILE_NAME_RESULT_SET_INDEX = 3;
        int FILE_REVISION_ID_RESULT_SET_INDEX = 4;
        int FILE_ID_RESULT_SET_INDEX = 5;
        int REVISION_DIGEST_RESULT_SET_INDEX = 6;
        int BRANCH_ID_RESULT_SET_INDEX = 7;
        int COMMIT_ID_RESULT_SET_INDEX = 8;
        // </editor-fold>

        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();
        String selectSegment = "SELECT U.USER_NAME, CM.COMMIT_DATE, FN.FILE_NAME, FR.ID AS FRID, FR.FILE_ID, FR.REVISION_DIGEST, FR.BRANCH_ID, CM.ID FROM ";
        String queryFormatString = new StringBuilder(selectSegment)
                .append(this.schemaName).append(".FILE_REVISION FR,")
                .append(this.schemaName).append(".FILE_NAME FN,")
                .append(this.schemaName).append(".COMIT CM,")
                .append(this.schemaName).append(".USER U ")
                .append("WHERE ")
                .append("FR.FILE_ID = ? AND ")
                .append("FR.BRANCH_ID IN (%s) AND ")
                .append("FR.FILE_ID = FN.FILE_ID AND ")
                .append("FR.COMMIT_ID = CM.ID AND ")
                .append("CM.USER_ID = U.ID ")
                .append("ORDER BY FR.ID DESC").toString();

        // Create the SQL query string
        String branchesToSearchString = buildBranchesToSearchString(branchAncestryList);
        String queryString = String.format(queryFormatString, branchesToSearchString);
        LOGGER.trace("query string: [{}]", queryString);

        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileRevision.getFileId());
            Map<String, List<SkinnyLogfileInfo>> candidateMap = new TreeMap<>();

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String fetchedUserName = resultSet.getString(USER_NAME_SET_INDEX);
                Date fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Integer fetchedFileRevisionId = resultSet.getInt(FILE_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);

                List<SkinnyLogfileInfo> listForCurrentFile = candidateMap.get(fetchedFilename);
                if (listForCurrentFile == null) {
                    listForCurrentFile = new ArrayList<>();
                    candidateMap.put(fetchedFilename, listForCurrentFile);
                }

                SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo();
                skinnyInfo.setLastEditByString(fetchedUserName);
                skinnyInfo.setLastCheckInDate(fetchedCommitDate);
                skinnyInfo.setShortWorkfileName(fetchedFilename);
                skinnyInfo.setDefaultRevisionString(String.format("%d.%d", fetchedBranchId, fetchedFileRevisionId));
                skinnyInfo.setFileRevisionId(fetchedFileRevisionId);
                skinnyInfo.setFileID(fetchedFileId);
                skinnyInfo.setDefaultRevisionDigest(fetchedDigest);
                skinnyInfo.setBranchId(fetchedBranchId);
                skinnyInfo.setCommitId(fetchedCommitId);
                listForCurrentFile.add(skinnyInfo);

            }
            // Prune the list so that any newer files on a parent branch are discarded.
            List<SkinnyLogfileInfo> usefulSkinnies = new ArrayList<>();
            for (List<SkinnyLogfileInfo> candidateList : candidateMap.values()) {
                // Find our skinny logfileInfo...
                for (SkinnyLogfileInfo skinnyInList : candidateList) {
                    if (skinnyInList.getFileRevisionId().intValue() == fileRevision.getId().intValue()) {
                        skinnyLogfileInfo = skinnyInList;
                    }
                }

                for (SkinnyLogfileInfo skinnyInList : candidateList) {
                    // Add any that are on the branch of the fetched revision.
                    if (skinnyInList.getBranchId().intValue() == fileRevision.getBranchId().intValue()) {
                        usefulSkinnies.add(skinnyInList);
                    } else if (skinnyInList.getBranchId() < fileRevision.getBranchId()) {
                        if (skinnyInList.getCommitId() < fileRevision.getCommitId()) {
                            usefulSkinnies.add(skinnyInList);
                        }
                    }
                }
                skinnyLogfileInfo.setDefaultRevisionDigest(usefulSkinnies.get(0).getDefaultRevisionDigest());
                skinnyLogfileInfo.setDefaultRevisionString(String.format("%d.%d", usefulSkinnies.get(0).getBranchId(), usefulSkinnies.get(0).getFileRevisionId()));
                skinnyLogfileInfo.setRevisionCount(usefulSkinnies.size());
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in getSkinnyLogfileInfoForGet", e);
            throw new RuntimeException(e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return skinnyLogfileInfo;
    }

    @Override
    public List<SkinnyLogfileInfo> getSkinnyLogfileInfo(Integer branchId, Integer directoryId) {
        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();
        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findById(branchId);
        int branchType = branch.getBranchTypeId();
        switch (branchType) {
            case QVCSConstants.QVCS_TRUNK_BRANCH_TYPE:
                skinnyList = getSkinnyLogfileInfoForTrunk(branchId, directoryId);
                break;
            case QVCSConstants.QVCS_FEATURE_BRANCH_TYPE:
                skinnyList = getSkinnyLogfileInfoForFeatureBranch(branchId, directoryId);
                break;
            default:
                break;
        }
        return skinnyList;
    }

    private List<SkinnyLogfileInfo> getSkinnyLogfileInfoForTrunk(Integer branchId, Integer directoryId) {
        // <editor-fold>
        int USER_NAME_SET_INDEX = 1;
        int COMMIT_DATE_RESULT_SET_INDEX = 2;
        int FILE_NAME_RESULT_SET_INDEX = 3;
        int FILE_REVISION_ID_RESULT_SET_INDEX = 4;
        int FILE_ID_RESULT_SET_INDEX = 5;
        int REVISION_DIGEST_RESULT_SET_INDEX = 6;
        int BRANCH_ID_RESULT_SET_INDEX = 7;
        int COMMIT_ID_RESULT_SET_INDEX = 8;
        // </editor-fold>

        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();
        String selectSegment = "SELECT UR.USER_NAME, CM.COMMIT_DATE, FN.FILE_NAME, FR.ID AS FRID, FR.FILE_ID, FR.REVISION_DIGEST, FR.BRANCH_ID, CM.ID FROM ";
        String queryString = new StringBuilder(selectSegment)
                .append(this.schemaName).append(".FILE_REVISION FR,")
                .append(this.schemaName).append(".COMIT CM,")
                .append(this.schemaName).append(".FILE_NAME FN,")
                .append(this.schemaName).append(".DIRECTORY_LOCATION DL,")
                .append(this.schemaName).append(".USER UR ")
                .append("WHERE ")
                .append("FR.BRANCH_ID = ? AND ")
                .append("FR.COMMIT_ID = CM.ID AND ")
                .append("CM.USER_ID = UR.ID AND ")
                .append("FN.FILE_ID = FR.FILE_ID AND ")
                .append("FN.BRANCH_ID = FR.BRANCH_ID AND ")
                .append("FN.DELETED_FLAG = FALSE AND ")
                .append("DL.DIRECTORY_ID = FN.DIRECTORY_ID AND ")
                .append("DL.DIRECTORY_ID = ? ")
                .append("ORDER BY FILE_NAME, FR.ID DESC").toString();
        LOGGER.debug("Trunk query string: [{}]", queryString);

        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);
            preparedStatement.setInt(2, directoryId);
            Map<Integer, Map<Integer, SkinnyLogfileInfo>> candidateMap = new TreeMap<>();

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String fetchedUserName = resultSet.getString(USER_NAME_SET_INDEX);
                Date fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Integer fetchedFileRevisionId = resultSet.getInt(FILE_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);

                Map<Integer, SkinnyLogfileInfo> mapForCurrentFile = candidateMap.get(fetchedFileId);
                if (mapForCurrentFile == null) {
                    mapForCurrentFile = new TreeMap<>();
                    candidateMap.put(fetchedFileId, mapForCurrentFile);
                }

                SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo();
                skinnyInfo.setLastEditByString(fetchedUserName);
                skinnyInfo.setLastCheckInDate(fetchedCommitDate);
                skinnyInfo.setShortWorkfileName(fetchedFilename);
                skinnyInfo.setDefaultRevisionString(String.format("%d.%d", fetchedBranchId, fetchedFileRevisionId));
                skinnyInfo.setFileID(fetchedFileId);
                skinnyInfo.setDefaultRevisionDigest(fetchedDigest);
                skinnyInfo.setBranchId(fetchedBranchId);
                skinnyInfo.setCommitId(fetchedCommitId);
                skinnyInfo.setFileRevisionId(fetchedFileRevisionId);
                mapForCurrentFile.put(fetchedFileRevisionId, skinnyInfo);
            }

            // Harvest just the newest revision... and update its revision count.
            for (Map<Integer, SkinnyLogfileInfo> candidatesMap : candidateMap.values()) {
                Object[] objectArray = candidatesMap.values().toArray();
                SkinnyLogfileInfo skinnyInfo = (SkinnyLogfileInfo) objectArray[objectArray.length - 1];
                skinnyInfo.setRevisionCount(candidatesMap.size());
                skinnyList.add(skinnyInfo);
                LOGGER.debug("***===>>> Trunk::: BranchId: [{}] directoryId: [{}] filename: [{}] Default revision string: [{}]", branchId, directoryId,
                        skinnyInfo.getShortWorkfileName(), skinnyInfo.getDefaultRevisionString());
            }

        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in getSkinnyLogfileInfoForTrunk", e);
            throw new RuntimeException(e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return skinnyList;
    }

    private List<SkinnyLogfileInfo> getSkinnyLogfileInfoForFeatureBranch(Integer branchId, Integer directoryId) {
        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();

        // <editor-fold>
        int USER_NAME_SET_INDEX = 1;
        int COMMIT_DATE_RESULT_SET_INDEX = 2;
        int FILE_NAME_RESULT_SET_INDEX = 3;
        int FILE_REVISION_ID_RESULT_SET_INDEX = 4;
        int FILE_ID_RESULT_SET_INDEX = 5;
        int REVISION_DIGEST_RESULT_SET_INDEX = 6;
        int BRANCH_ID_RESULT_SET_INDEX = 7;
        int COMMIT_ID_RESULT_SET_INDEX = 8;
        // </editor-fold>

        // Create the SQL query string
        String queryString = buildSkinnyInfoQueryStringForBranch(branchId, directoryId);
        LOGGER.debug("Feature branch query string: [{}]", queryString);

        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, directoryId);
            Map<Integer, List<SkinnyLogfileInfo>> candidateMap = new TreeMap<>();

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String fetchedUserName = resultSet.getString(USER_NAME_SET_INDEX);
                Date fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Integer fetchedFileRevisionId = resultSet.getInt(FILE_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);

                List<SkinnyLogfileInfo> listForCurrentFile = candidateMap.get(fetchedFileId);
                if (listForCurrentFile == null) {
                    listForCurrentFile = new ArrayList<>();
                    candidateMap.put(fetchedFileId, listForCurrentFile);
                }

                SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo();
                skinnyInfo.setLastEditByString(fetchedUserName);
                skinnyInfo.setLastCheckInDate(fetchedCommitDate);
                skinnyInfo.setShortWorkfileName(fetchedFilename);
                skinnyInfo.setDefaultRevisionString(String.format("%d.%d", fetchedBranchId, fetchedFileRevisionId));
                skinnyInfo.setFileID(fetchedFileId);
                skinnyInfo.setDefaultRevisionDigest(fetchedDigest);
                skinnyInfo.setBranchId(fetchedBranchId);
                skinnyInfo.setCommitId(fetchedCommitId);
                skinnyInfo.setFileRevisionId(fetchedFileRevisionId);
                listForCurrentFile.add(skinnyInfo);

            }
            // Prune the list so that if a newer file of the same name exists on the branch,
            // ignore any file on the parent branch(es)
            Map<Integer, SkinnyLogfileInfo> skinnyMap = new TreeMap<>();
            for (List<SkinnyLogfileInfo> candidateList : candidateMap.values()) {
                // Include all revisions on the requested branch
                if (candidateList.get(0).getBranchId().intValue() == branchId) {
                    SkinnyLogfileInfo skinnyInfo = candidateList.get(0);
                    skinnyInfo.setRevisionCount(candidateList.size());
                    skinnyList.add(skinnyInfo);
                    skinnyMap.put(skinnyInfo.getFileID(), skinnyInfo);
                } else {
                    // Only take from an ancestor branch if we don't have info from a child branch.
                    SkinnyLogfileInfo tipBranchSkinnyInfo = skinnyMap.get(candidateList.get(0).getFileID());
                    if (tipBranchSkinnyInfo == null) {
                        SkinnyLogfileInfo skinnyInfo = candidateList.get(0);
                        skinnyInfo.setRevisionCount(candidateList.size());
                        skinnyList.add(skinnyInfo);
                        skinnyMap.put(skinnyInfo.getFileID(), skinnyInfo);
                    }
                }
            }
        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in getSkinnyLogfileInfoForFeatureBranch", e);
            throw new RuntimeException(e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return skinnyList;
    }

    private List<SkinnyLogfileInfo> getSkinnyLogfileInfoForTagBasedBranch(Integer branchId, Integer directoryId) {
        // <editor-fold>
        int USER_NAME_SET_INDEX = 1;
        int COMMIT_DATE_RESULT_SET_INDEX = 2;
        int FILE_NAME_RESULT_SET_INDEX = 3;
        int FILE_REVISION_ID_RESULT_SET_INDEX = 4;
        int FILE_ID_RESULT_SET_INDEX = 5;
        int REVISION_DIGEST_RESULT_SET_INDEX = 6;
        int BRANCH_ID_RESULT_SET_INDEX = 7;
        int COMMIT_ID_RESULT_SET_INDEX = 8;
        // </editor-fold>

        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findById(branchId);

        TagDAO tagDAO = new TagDAOImpl(schemaName);
        Tag tag = tagDAO.findById(branch.getTagId());

        Integer tagBranchCommitId = tag.getCommitId();

        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();
        String selectSegment = "SELECT UR.USER_NAME, CM.COMMIT_DATE, FN.FILE_NAME, FR.ID AS FRID, FR.FILE_ID, FR.REVISION_DIGEST, FR.BRANCH_ID, CM.ID FROM ";
        String queryFormatString = new StringBuilder(selectSegment)
                .append(this.schemaName).append(".FILE_REVISION FR,")
                .append(this.schemaName).append(".COMIT CM,")
                .append(this.schemaName).append(".FILE_NAME FN,")
                .append(this.schemaName).append(".DIRECTORY_LOCATION DL,")
                .append(this.schemaName).append(".USER UR ")
                .append("WHERE ")
                .append("FR.BRANCH_ID IN (%s) AND ")
                .append("FR.COMMIT_ID = CM.ID AND ")
                .append("CM.USER_ID = UR.ID AND ")
                .append("FN.FILE_ID = FR.FILE_ID AND ")
                .append("FN.DELETED_FLAG = FALSE AND ")
                .append("DL.DIRECTORY_ID = FN.DIRECTORY_ID AND ")
                .append("DL.DIRECTORY_ID = ? AND ")
                .append("FR.COMMIT_ID < ? ")
                .append("ORDER BY FILE_NAME, BRANCH_ID DESC, FR.ID DESC").toString();

        List<Branch> branchAncestryList = getBranchAncestryList(branchId);

        // Create the SQL query string
        String branchesToSearchString = buildBranchesToSearchString(branchAncestryList);
        String queryString = String.format(queryFormatString, branchesToSearchString);
        LOGGER.debug("Tag branch query string: [{}]", queryString);

        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, directoryId);
            preparedStatement.setInt(2, tagBranchCommitId);
            Map<String, List<SkinnyLogfileInfo>> candidateMap = new TreeMap<>();

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String fetchedUserName = resultSet.getString(USER_NAME_SET_INDEX);
                Date fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Integer fetchedFileRevisionId = resultSet.getInt(FILE_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);

                List<SkinnyLogfileInfo> listForCurrentFile = candidateMap.get(fetchedFilename);
                if (listForCurrentFile == null) {
                    listForCurrentFile = new ArrayList<>();
                    candidateMap.put(fetchedFilename, listForCurrentFile);
                }

                SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo();
                skinnyInfo.setLastEditByString(fetchedUserName);
                skinnyInfo.setLastCheckInDate(fetchedCommitDate);
                skinnyInfo.setShortWorkfileName(fetchedFilename);
                skinnyInfo.setDefaultRevisionString(String.format("%d.%d", fetchedBranchId, fetchedFileRevisionId));
                skinnyInfo.setFileID(fetchedFileId);
                skinnyInfo.setDefaultRevisionDigest(fetchedDigest);
                skinnyInfo.setBranchId(fetchedBranchId);
                skinnyInfo.setCommitId(fetchedCommitId);
                skinnyInfo.setFileRevisionId(fetchedFileRevisionId);
                listForCurrentFile.add(skinnyInfo);

            }
            for (List<SkinnyLogfileInfo> candidateList : candidateMap.values()) {
                SkinnyLogfileInfo skinnyInfo = candidateList.get(0);
                skinnyInfo.setRevisionCount(candidateList.size());
                skinnyList.add(skinnyInfo);
            }

        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in getSkinnyLogfileInfoForTagBasedBranch", e);
            throw new RuntimeException(e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return skinnyList;
    }

    private List<SkinnyLogfileInfo> getSkinnyLogfileInfoForReleaseBranch(Integer branchId, Integer directoryId) {
        List<SkinnyLogfileInfo> skinnyList = new ArrayList<>();

        // <editor-fold>
        int USER_NAME_SET_INDEX = 1;
        int COMMIT_DATE_RESULT_SET_INDEX = 2;
        int FILE_NAME_RESULT_SET_INDEX = 3;
        int FILE_REVISION_ID_RESULT_SET_INDEX = 4;
        int FILE_ID_RESULT_SET_INDEX = 5;
        int REVISION_DIGEST_RESULT_SET_INDEX = 6;
        int BRANCH_ID_RESULT_SET_INDEX = 7;
        int COMMIT_ID_RESULT_SET_INDEX = 8;
        // </editor-fold>

        // Create the SQL query string
        String queryString = buildSkinnyInfoQueryStringForBranch(branchId, directoryId);
        LOGGER.debug("Release branch query string: [{}]", queryString);

        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, directoryId);
            Map<Integer, List<SkinnyLogfileInfo>> candidateMap = new TreeMap<>();

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String fetchedUserName = resultSet.getString(USER_NAME_SET_INDEX);
                Date fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                String fetchedFilename = resultSet.getString(FILE_NAME_RESULT_SET_INDEX);
                Integer fetchedFileRevisionId = resultSet.getInt(FILE_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);

                List<SkinnyLogfileInfo> listForCurrentFile = candidateMap.get(fetchedFileId);
                if (listForCurrentFile == null) {
                    listForCurrentFile = new ArrayList<>();
                    candidateMap.put(fetchedFileId, listForCurrentFile);
                }

                SkinnyLogfileInfo skinnyInfo = new SkinnyLogfileInfo();
                skinnyInfo.setLastEditByString(fetchedUserName);
                skinnyInfo.setLastCheckInDate(fetchedCommitDate);
                skinnyInfo.setShortWorkfileName(fetchedFilename);
                skinnyInfo.setDefaultRevisionString(String.format("%d.%d", fetchedBranchId, fetchedFileRevisionId));
                skinnyInfo.setFileID(fetchedFileId);
                skinnyInfo.setDefaultRevisionDigest(fetchedDigest);
                skinnyInfo.setBranchId(fetchedBranchId);
                skinnyInfo.setCommitId(fetchedCommitId);
                skinnyInfo.setFileRevisionId(fetchedFileRevisionId);
                listForCurrentFile.add(skinnyInfo);

            }
            // Prune the list so that any newer files on a parent branch are discarded.
            Map<Integer, SkinnyLogfileInfo> skinnyMap = new TreeMap<>();
            for (List<SkinnyLogfileInfo> candidateList : candidateMap.values()) {
                // Include all revisions on the requested branch
                if (candidateList.get(0).getBranchId().intValue() == branchId) {
                    SkinnyLogfileInfo skinnyInfo = candidateList.get(0);
                    skinnyInfo.setRevisionCount(candidateList.size());
                    skinnyList.add(skinnyInfo);
                    skinnyMap.put(skinnyInfo.getFileID(), skinnyInfo);
                } else {
                    // Only take from an ancestor branch if we don't have info from a child branch.
                    SkinnyLogfileInfo tipBranchSkinnyInfo = skinnyMap.get(candidateList.get(0).getFileID());
                    if (tipBranchSkinnyInfo == null) {
                        SkinnyLogfileInfo skinnyInfo = candidateList.get(0);
                        skinnyInfo.setRevisionCount(candidateList.size());
                        skinnyList.add(skinnyInfo);
                        skinnyMap.put(skinnyInfo.getFileID(), skinnyInfo);
                    }
                }
            }

        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in getSkinnyLogfileInfoForReleaseBranch", e);
            throw new RuntimeException(e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return skinnyList;
    }

    @Override
    public DirectoryCoordinateIds getDirectoryCoordinateIds(DirectoryCoordinate directoryCoordinate) {
        DirectoryCoordinateIds directoryCoordinateIds = null;
        SourceControlBehaviorManager sourceControlBehaviorManager = SourceControlBehaviorManager.getInstance();
        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(directoryCoordinate.getProjectName());
        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findByProjectIdAndBranchName(project.getId(), directoryCoordinate.getBranchName());
        DirectoryLocation dl = sourceControlBehaviorManager.findDirectoryLocationByAppendedPath(branch.getId(), directoryCoordinate.getAppendedPath());
        Map<Integer, String> writeableBranchMap = new TreeMap<>();
        writeableBranchMap.put(branch.getId(), branch.getBranchName());
        branchDAO.getWriteableChildBranchIdList(branch.getId(), writeableBranchMap);
        if (dl != null) {
            directoryCoordinateIds = new DirectoryCoordinateIds(project.getId(), branch.getId(), dl.getDirectoryId(), dl.getId(), directoryCoordinate, writeableBranchMap);
        } else {
            LOGGER.info("Invalid directory coordinate: {}:{}:{}", directoryCoordinate.getProjectName(), directoryCoordinate.getBranchName(), directoryCoordinate.getAppendedPath());
            throw new QVCSRuntimeException("Invalid directory coordinate");
        }
        return directoryCoordinateIds;
    }

    @Override
    public LogfileInfo getLogfileInfo(DirectoryCoordinate directoryCoordinate, String shortFilename, Integer fileId) {
        LogfileInfo logfileInfo = null;
        DirectoryCoordinateIds dcIds = getDirectoryCoordinateIds(directoryCoordinate);
        if (dcIds != null) {
            logfileInfo = new LogfileInfo();
            populateLogfileInfo(dcIds, logfileInfo, fileId);
        }
        return logfileInfo;
    }

    @Override
    public LogfileInfo getLogfileInfo(DirectoryCoordinateIds dcIds, String shortFilename, Integer fileId) {
        LogfileInfo logfileInfo = null;
        if (dcIds != null) {
            logfileInfo = new LogfileInfo();
            populateLogfileInfo(dcIds, logfileInfo, fileId);
        }
        return logfileInfo;
    }

    @Override
    public LogfileInfo getAllLogfileInfo(DirectoryCoordinate directoryCoordinate, String shortFilename, Integer fileId) {
        LogfileInfo logfileInfo = null;
        DirectoryCoordinateIds dcIds = getDirectoryCoordinateIds(directoryCoordinate);
        if (dcIds != null) {
            logfileInfo = new LogfileInfo();
            if (0 == populateAllLogfileInfo(dcIds, logfileInfo, shortFilename, fileId)) {
                logfileInfo = null;
            }
        }
        return logfileInfo;
    }

    @Override
    public List<Branch> getBranchAncestryList(Integer branchId) {
        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findById(branchId);
        List<Branch> branchArray = new ArrayList<>();
        branchArray.add(branch);

        // Get the ancestry of branches...
        while (branch.getParentBranchId() != null) {
            branch = branchDAO.findById(branch.getParentBranchId());
            branchArray.add(branch);
        }
        return branchArray;
    }

    private void populateLogfileInfo(DirectoryCoordinateIds dcIds, LogfileInfo logfileInfo, Integer fileId) {
        List<Branch> branchArray = getBranchAncestryList(dcIds.getBranchId());

        CommitDAO commitDAO = new CommitDAOImpl(schemaName);
        UserDAO userDAO = new UserDAOImpl(schemaName);

        LogFileHeaderInfo headerInfo = new LogFileHeaderInfo();
        headerInfo.setArchiveAttributes(new ArchiveAttributes());
        headerInfo.setCommentPrefix("// "); // TODO
        logfileInfo.setLogFileHeaderInfo(headerInfo);
        List<FileRevision> fileRevisionList = findFileRevisionInBranches(branchArray, fileId, headerInfo);
        RevisionInformation revisionInformation = new RevisionInformation(fileRevisionList.size());
        int index = 0;
        for (FileRevision fileRevision : fileRevisionList) {
            RevisionHeader revisionHeader = new RevisionHeader();
            revisionHeader.setBranchId(fileRevision.getBranchId());
            revisionHeader.setFileRevisionId(fileRevision.getId());
            revisionHeader.setCommitId(fileRevision.getCommitId());
            Commit commit = commitDAO.findById(fileRevision.getCommitId());
            revisionHeader.setCheckInDate(commit.getCommitDate());
            User user = userDAO.findById(commit.getUserId());
            revisionHeader.setCreator(user.getUserName());
            revisionHeader.setEditDate(fileRevision.getWorkfileEditDate());
            revisionHeader.setIsTip(index == 0);
            if (index == 0) {
                revisionHeader.setParentRevisionHeader(null);
                headerInfo.setLastModifierName(user.getUserName());
                headerInfo.setModuleDescription(commit.getCommitMessage());
                headerInfo.setBranchId(fileRevision.getBranchId());
                headerInfo.setFileID(fileId);
            } else {
                revisionHeader.setParentRevisionHeader(revisionInformation.getRevisionHeader(index - 1));
            }
            revisionHeader.setRevisionDescription(commit.getCommitMessage());
            revisionHeader.setRevisionIndex(index);
            revisionHeader.setRevisionSize(fileRevision.getRevisionDataSize());
            revisionInformation.updateRevision(index, revisionHeader);
            index++;
        }
        logfileInfo.setRevisionInformation(revisionInformation);
    }

    private int populateAllLogfileInfo(DirectoryCoordinateIds dcIds, LogfileInfo logfileInfo, String shortFilename, Integer fileId) {
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        int index = 0;
        CommitDAO commitDAO = new CommitDAOImpl(schemaName);
        UserDAO userDAO = new UserDAOImpl(schemaName);

        LogFileHeaderInfo headerInfo = new LogFileHeaderInfo();
        headerInfo.setArchiveAttributes(new ArchiveAttributes());
        headerInfo.setCommentPrefix("// "); // TODO
        logfileInfo.setLogFileHeaderInfo(headerInfo);
        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        List<FileRevision> fileRevisionList = fileRevisionDAO.findAllFileRevisions(fileId);
        headerInfo.setRevisionCount(fileRevisionList.size());
        RevisionInformation revisionInformation = new RevisionInformation(fileRevisionList.size());
        for (FileRevision fileRevision : fileRevisionList) {
            RevisionHeader revisionHeader = new RevisionHeader();
            revisionHeader.setBranchId(fileRevision.getBranchId());
            revisionHeader.setFileRevisionId(fileRevision.getId());
            revisionHeader.setCommitId(fileRevision.getCommitId());
            Commit commit = commitDAO.findById(fileRevision.getCommitId());
            revisionHeader.setCheckInDate(commit.getCommitDate());
            User user = userDAO.findById(commit.getUserId());
            revisionHeader.setCreator(user.getUserName());
            revisionHeader.setEditDate(fileRevision.getWorkfileEditDate());
            revisionHeader.setIsTip(index == 0);
            if (index == 0) {
                revisionHeader.setParentRevisionHeader(null);
                headerInfo.setLastModifierName(user.getUserName());
                headerInfo.setModuleDescription(commit.getCommitMessage());
                headerInfo.setBranchId(fileRevision.getBranchId());
                headerInfo.setFileID(fileId);
            } else {
                revisionHeader.setParentRevisionHeader(revisionInformation.getRevisionHeader(index - 1));
            }
            revisionHeader.setRevisionDescription(commit.getCommitMessage());
            revisionHeader.setRevisionIndex(index);
            revisionHeader.setRevisionSize(fileRevision.getRevisionDataSize());
            revisionInformation.updateRevision(index, revisionHeader);
            index++;
        }
        logfileInfo.setRevisionInformation(revisionInformation);
        return index;
    }

    private List<FileRevision> findFileRevisionInBranches(List<Branch> branchArray, Integer fileId, LogFileHeaderInfo headerInfo) {
        List<FileRevision> fileRevisionList = null;
        try {
            fileRevisionList = findFileRevisionsInBranches(branchArray, fileId);

            if (fileRevisionList != null && !fileRevisionList.isEmpty()) {
                FileRevision newestFileRevision = fileRevisionList.get(0);

                CommitDAO commitDAO = new CommitDAOImpl(schemaName);
                Commit commit = commitDAO.findById(newestFileRevision.getCommitId());

                UserDAO userDAO = new UserDAOImpl(schemaName);
                User user = userDAO.findById(commit.getUserId());

                headerInfo.setLastArchiveUpdateDate(commit.getCommitDate());
                headerInfo.setLastModifierName(user.getUserName());
                headerInfo.setLastWorkfileSize(newestFileRevision.getRevisionDataSize());
                headerInfo.setLatestRevisionId(newestFileRevision.getId());
                headerInfo.setRevisionCount(fileRevisionList.size());
            }
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: exception in findFileRevisionInBranches", e);
            throw e;
        }
        return fileRevisionList;
    }

    @Override
    public List<FileRevision> findFileRevisionsInBranches(List<Branch> branchArray, Integer fileId) {
        List<FileRevision> fileRevisionList = new ArrayList<>();
        Branch targetBranch = branchArray.get(0);
        int branchType = targetBranch.getBranchTypeId();
        switch (branchType) {
            case QVCSConstants.QVCS_TRUNK_BRANCH_TYPE:
                fileRevisionList = findFileRevisionsInBranchesTrunk(branchArray, fileId);
                break;
            case QVCSConstants.QVCS_FEATURE_BRANCH_TYPE:
                fileRevisionList = findFileRevisionsInBranchesForFeatureBranch(branchArray, fileId);
                break;
            case QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE:
                fileRevisionList = findFileRevisionsInBranchesForTagBasedBranch(branchArray, fileId);
                break;
            case QVCSConstants.QVCS_RELEASE_BRANCH_TYPE:
                fileRevisionList = findFileRevisionsInBranchesForReleaseBranch(branchArray, fileId);
                break;
            default:
                break;
        }
        return fileRevisionList;
    }

    @Override
    public Branch findBranchByProjectNameAndBranchName(String projectName, String branchName) {
        Branch branch = null;

        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(projectName);
        if (project != null) {
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            branch = branchDAO.findByProjectIdAndBranchName(project.getId(), branchName);
        }
        return branch;
    }

    @Override
    public List<Branch> findBranchesForProjectName(String projectName) {
        List<Branch> branchList = null;

        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(projectName);
        if (project != null) {
            BranchDAO branchDAO = new BranchDAOImpl(schemaName);
            branchList = branchDAO.findProjectBranches(project.getId());
        }
        return branchList;
    }

    @Override
    public Integer getChildBranchCount(String projectName, String branchName) {
        ProjectDAO projectDAO = new ProjectDAOImpl(schemaName);
        Project project = projectDAO.findByProjectName(projectName);

        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch parentBranch = branchDAO.findByProjectIdAndBranchName(project.getId(), branchName);
        Integer childBranchCount = branchDAO.getChildBranchCount(project.getId(), parentBranch.getId());

        return childBranchCount;
    }

    @Override
    public Commit findNewestFileRevisionCommitOnBranch(int branchId) {
        BranchDAO branchDAO = new BranchDAOImpl(schemaName);
        Branch branch = branchDAO.findById(branchId);

        FileRevisionDAO fileRevisionDAO = new FileRevisionDAOImpl(schemaName);
        FileRevision fileRevision = fileRevisionDAO.findNewestBranchRevision(branchId);

        CommitDAO commitDAO = new CommitDAOImpl(schemaName);
        Commit commit = commitDAO.findById(fileRevision.getCommitId());
        return commit;
    }

    @Override
    public FileRevision findBranchTipRevisionByBranchIdAndFileId(Integer branchId, Integer fileId) {

        int ID_RESULT_SET_INDEX = 1;
        int BRANCH_ID_RESULT_SET_INDEX = 2;
        int FILE_ID_RESULT_SET_INDEX = 3;
        int ANCESTOR_REVISION_ID_RESULT_SET_INDEX = 4;
        int REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX = 5;
        int COMMIT_ID_RESULT_SET_INDEX = 6;
        int PROMOTED_FLAG_RESULT_SET_INDEX = 7;
        int WORKFILE_EDIT_DATE_RESULT_SET_INDEX = 8;
        int REVISION_DIGEST_RESULT_SET_INDEX = 9;
        int REVISION_SIZE_RESULT_SET_INDEX = 10;

        String selectHeaderSegment = "SELECT ID, BRANCH_ID, FILE_ID, ANCESTOR_REVISION_ID, REVERSE_DELTA_REVISION_ID, COMMIT_ID, PROMOTED_FLAG, WORKFILE_EDIT_DATE, REVISION_DIGEST, "
                + "LENGTH(REVISION_DATA) AS REVISION_SIZE FROM ";
        String findBranchTipRevisionByBranchIdAndFileId = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID IN (%s) AND FILE_ID = ? ORDER BY BRANCH_ID DESC, ID DESC LIMIT 1";

        FileRevision revision = null;
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            String queryString = String.format(findBranchTipRevisionByBranchIdAndFileId, buildBranchesToSearchString(getBranchAncestryList(branchId)));
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Boolean fetchedPromotedFlag = resultSet.getBoolean(PROMOTED_FLAG_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                revision = new FileRevision();
                revision.setId(fetchedId);
                revision.setBranchId(fetchedBranchId);
                revision.setFileId(fetchedFileId);
                revision.setAncestorRevisionId(fetchedAncestorRevisionId);
                revision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                revision.setCommitId(fetchedCommitId);
                revision.setPromotedFlag(fetchedPromotedFlag);
                revision.setWorkfileEditDate(fetchedWorkfileEditDate);
                revision.setRevisionDigest(fetchedDigest);
                revision.setRevisionDataSize(fetchedSize);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in findBranchTipRevisionByBranchIdAndFileId", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: exception in findBranchTipRevisionByBranchIdAndFileId", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return revision;
    }

    @Override
    public List<String> getMostRecentUserCommits(String userName, Integer count) {
        // Get the user id.
        UserDAO userDAO = new UserDAOImpl(schemaName);
        User user = userDAO.findByUserName(userName);

        // <editor-fold>
        int COMMIT_MESSAGE_RESULT_SET_INDEX = 1;
        // </editor-fold>

        List<String> commentList = new ArrayList<>();
        String selectSegment = "SELECT CM.COMMIT_MESSAGE FROM ";
        String queryString = new StringBuilder(selectSegment)
                .append(this.schemaName).append(".COMIT CM ")
                .append("WHERE ")
                .append("CM.USER_ID = ? AND ")
                .append("LENGTH(CM.COMMIT_MESSAGE) > 0 ")
                .append("ORDER BY CM.ID DESC LIMIT ?").toString();

        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            // <editor-fold>
            preparedStatement.setInt(1, user.getId());
            preparedStatement.setInt(2, count);
            preparedStatement.setMaxRows(count);
            // </editor-fold>

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                String fetchedComment = resultSet.getString(COMMIT_MESSAGE_RESULT_SET_INDEX);
                commentList.add(fetchedComment);
            }

        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in getMostRecentUserCommits", e);
            throw new RuntimeException(e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return commentList;
    }

    @Override
    public List<TagInfoData> getTagsInfoData(Integer branchId) {
        List<TagInfoData> tagInfoDataList = new ArrayList<>();

        // <editor-fold>
        int TAG_TEXT_RESULT_SET_INDEX = 1;
        int DESCRIPTION_RESULT_SET_INDEX = 2;
        int COMMIT_DATE_RESULT_SET_INDEX = 3;
        int USER_NAME_RESULT_SET_INDEX = 4;
        int BRANCH_NAME_RESULT_SET_INDEX = 5;
        // </editor-fold>

        String selectSegment = "SELECT T.TAG_TEXT, T.DESCRIPTION, C.COMMIT_DATE, U.USER_NAME, B.BRANCH_NAME FROM ";
        StringBuilder queryFormatStringBuilder = new StringBuilder(selectSegment);
        String queryString = queryFormatStringBuilder
                .append(this.schemaName).append(".TAG T,")
                .append(this.schemaName).append(".COMIT C,")
                .append(this.schemaName).append(".USER U,")
                .append(this.schemaName).append(".BRANCH B ")
                .append("WHERE ")
                .append("C.ID = T.COMMIT_ID AND ")
                .append("C.USER_ID = U.ID AND ")
                .append("T.BRANCH_ID = ? AND ")
                .append("B.ID = T.BRANCH_ID ")
                .append("ORDER BY T.ID DESC").toString();

        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                TagInfoData tagInfoData = new TagInfoData();
                String fetchedTagText = resultSet.getString(TAG_TEXT_RESULT_SET_INDEX);
                String fetchedDescription = null;
                Object fetchedDescriptionObject = resultSet.getObject(DESCRIPTION_RESULT_SET_INDEX);
                if (fetchedDescriptionObject != null) {
                    fetchedDescription = resultSet.getString(DESCRIPTION_RESULT_SET_INDEX);
                }
                Date fetchedCommitDate = resultSet.getTimestamp(COMMIT_DATE_RESULT_SET_INDEX);
                String fetchedUserName = resultSet.getString(USER_NAME_RESULT_SET_INDEX);
                String fetchedBranchName = resultSet.getString(BRANCH_NAME_RESULT_SET_INDEX);

                tagInfoData.setTagText(fetchedTagText);
                tagInfoData.setDescription(fetchedDescription);
                tagInfoData.setCreationDate(fetchedCommitDate);
                tagInfoData.setCreatorName(fetchedUserName);
                tagInfoData.setBranchName(fetchedBranchName);

                tagInfoDataList.add(tagInfoData);
            }

        } catch (SQLException | IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in getMostRecentUserCommits", e);
            throw new RuntimeException(e);
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return tagInfoDataList;
    }

    private List<FileRevision> findFileRevisionsInBranchesTrunk(List<Branch> branchArray, Integer fileId) {
        String selectHeaderSegment = "SELECT ID, BRANCH_ID, FILE_ID, ANCESTOR_REVISION_ID, REVERSE_DELTA_REVISION_ID, COMMIT_ID, WORKFILE_EDIT_DATE, REVISION_DIGEST, "
                + "LENGTH(REVISION_DATA) AS REVISION_SIZE FROM ";
        String findFileRevisionsInBranches = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID = ? AND FILE_ID = ? ORDER BY BRANCH_ID DESC, ID DESC";

        // <editor-fold>
        int ID_RESULT_SET_INDEX = 1;
        int BRANCH_ID_RESULT_SET_INDEX = 2;
        int FILE_ID_RESULT_SET_INDEX = 3;
        int ANCESTOR_REVISION_ID_RESULT_SET_INDEX = 4;
        int REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX = 5;
        int COMMIT_ID_RESULT_SET_INDEX = 6;
        int WORKFILE_EDIT_DATE_RESULT_SET_INDEX = 7;
        int REVISION_DIGEST_RESULT_SET_INDEX = 8;
        int REVISION_SIZE_RESULT_SET_INDEX = 9;
        // </editor-fold>

        List<FileRevision> fileRevisionList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(findFileRevisionsInBranches, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, branchArray.get(0).getId());
            preparedStatement.setInt(2, fileId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                FileRevision fileRevision = new FileRevision();
                fileRevision.setId(fetchedId);
                fileRevision.setBranchId(fetchedBranchId);
                fileRevision.setFileId(fetchedFileId);
                fileRevision.setAncestorRevisionId(fetchedAncestorRevisionId);
                fileRevision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                fileRevision.setCommitId(fetchedCommitId);
                fileRevision.setWorkfileEditDate(fetchedWorkfileEditDate);
                fileRevision.setRevisionDigest(fetchedDigest);
                fileRevision.setRevisionDataSize(fetchedSize);
                fileRevisionList.add(fileRevision);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in findFileRevisionsInBranchesTrunk", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: exception in findFileRevisionsInBranchesTrunk", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileRevisionList;
    }

    private List<FileRevision> findFileRevisionsInBranchesForFeatureBranch(List<Branch> branchArray, Integer fileId) {
        String selectHeaderSegment = "SELECT ID, BRANCH_ID, FILE_ID, ANCESTOR_REVISION_ID, REVERSE_DELTA_REVISION_ID, COMMIT_ID, WORKFILE_EDIT_DATE, REVISION_DIGEST, "
                + "LENGTH(REVISION_DATA) AS REVISION_SIZE FROM ";
        String findFileRevisionsInBranches = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID IN (%s) AND FILE_ID = ? AND PROMOTED_FLAG = FALSE "
                + "ORDER BY BRANCH_ID DESC, ID DESC";

        // <editor-fold>
        int ID_RESULT_SET_INDEX = 1;
        int BRANCH_ID_RESULT_SET_INDEX = 2;
        int FILE_ID_RESULT_SET_INDEX = 3;
        int ANCESTOR_REVISION_ID_RESULT_SET_INDEX = 4;
        int REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX = 5;
        int COMMIT_ID_RESULT_SET_INDEX = 6;
        int WORKFILE_EDIT_DATE_RESULT_SET_INDEX = 7;
        int REVISION_DIGEST_RESULT_SET_INDEX = 8;
        int REVISION_SIZE_RESULT_SET_INDEX = 9;
        // </editor-fold>

        String branchesToSearchString = buildBranchesToSearchString(branchArray);
        String queryString = String.format(findFileRevisionsInBranches, branchesToSearchString);
        LOGGER.debug("findFileRevisionsInBranchesForFeatureBranch query string: [{}]", queryString);

        List<FileRevision> candidateRevisionList = new ArrayList<>();
        List<FileRevision> fileRevisionList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                FileRevision fileRevision = new FileRevision();
                fileRevision.setId(fetchedId);
                fileRevision.setBranchId(fetchedBranchId);
                fileRevision.setFileId(fetchedFileId);
                fileRevision.setAncestorRevisionId(fetchedAncestorRevisionId);
                fileRevision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                fileRevision.setCommitId(fetchedCommitId);
                fileRevision.setWorkfileEditDate(fetchedWorkfileEditDate);
                fileRevision.setRevisionDigest(fetchedDigest);
                fileRevision.setRevisionDataSize(fetchedSize);
                candidateRevisionList.add(fileRevision);
            }
            // This is where we would prune the list of any parent revisions that are newer than the tip branch revision.
            FileRevision branchTipRevision = candidateRevisionList.get(0);
            int counter = 0;
            for (FileRevision fileRevision : candidateRevisionList) {
                if (counter++ == 0) {
                    // Always include the first revision returned by the query.
                    fileRevisionList.add(fileRevision);
                } else {
                    // Include all revisions on the requested branch
                    if (fileRevision.getBranchId().intValue() == branchArray.get(0).getId().intValue()) {
                        fileRevisionList.add(fileRevision);
                    } else {
                        // Only add parent revisions that are older than the newest revision found.
                        if (fileRevision.getCommitId() < branchTipRevision.getCommitId()) {
                            fileRevisionList.add(fileRevision);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in findFileRevisionsInBranchesForFeatureBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: exception in findFileRevisionsInBranchesForFeatureBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileRevisionList;
    }

    private List<FileRevision> findFileRevisionsInBranchesForTagBasedBranch(List<Branch> branchArray, Integer fileId) {
        String selectHeaderSegment = "SELECT ID, BRANCH_ID, FILE_ID, ANCESTOR_REVISION_ID, REVERSE_DELTA_REVISION_ID, COMMIT_ID, WORKFILE_EDIT_DATE, REVISION_DIGEST, "
                + "LENGTH(REVISION_DATA) AS REVISION_SIZE FROM ";
        String findFileRevisionsInBranches = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID IN (%s) AND FILE_ID = ? AND COMMIT_ID < ? ORDER BY BRANCH_ID DESC, ID DESC";

        // <editor-fold>
        int ID_RESULT_SET_INDEX = 1;
        int BRANCH_ID_RESULT_SET_INDEX = 2;
        int FILE_ID_RESULT_SET_INDEX = 3;
        int ANCESTOR_REVISION_ID_RESULT_SET_INDEX = 4;
        int REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX = 5;
        int COMMIT_ID_RESULT_SET_INDEX = 6;
        int WORKFILE_EDIT_DATE_RESULT_SET_INDEX = 7;
        int REVISION_DIGEST_RESULT_SET_INDEX = 8;
        int REVISION_SIZE_RESULT_SET_INDEX = 9;
        // </editor-fold>

        String branchesToSearchString = buildBranchesToSearchString(branchArray);
        String queryString = String.format(findFileRevisionsInBranches, branchesToSearchString);
        LOGGER.trace("query string: [{}]", queryString);

        LOGGER.debug("findFileRevisionsInBranchesForTagBasedBranch query string: [{}]", queryString);

        TagDAO tagDAO = new TagDAOImpl(schemaName);
        Tag tag = tagDAO.findById(branchArray.get(0).getTagId());

        List<FileRevision> fileRevisionList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);
            preparedStatement.setInt(2, tag.getCommitId());

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                FileRevision fileRevision = new FileRevision();
                fileRevision.setId(fetchedId);
                fileRevision.setBranchId(fetchedBranchId);
                fileRevision.setFileId(fetchedFileId);
                fileRevision.setAncestorRevisionId(fetchedAncestorRevisionId);
                fileRevision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                fileRevision.setCommitId(fetchedCommitId);
                fileRevision.setWorkfileEditDate(fetchedWorkfileEditDate);
                fileRevision.setRevisionDigest(fetchedDigest);
                fileRevision.setRevisionDataSize(fetchedSize);
                fileRevisionList.add(fileRevision);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in findFileRevisionsInBranchesForTagBasedBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: exception in findFileRevisionsInBranchesForTagBasedBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileRevisionList;
    }

    private List<FileRevision> findFileRevisionsInBranchesForReleaseBranch(List<Branch> branchArray, Integer fileId) {
        String selectHeaderSegment = "SELECT ID, BRANCH_ID, FILE_ID, ANCESTOR_REVISION_ID, REVERSE_DELTA_REVISION_ID, COMMIT_ID, WORKFILE_EDIT_DATE, REVISION_DIGEST, "
                + "LENGTH(REVISION_DATA) AS REVISION_SIZE FROM ";
        String findFileRevisionsInBranches = selectHeaderSegment + this.schemaName + ".FILE_REVISION WHERE BRANCH_ID IN (%s) AND FILE_ID = ? ORDER BY ID DESC";

        // <editor-fold>
        int ID_RESULT_SET_INDEX = 1;
        int BRANCH_ID_RESULT_SET_INDEX = 2;
        int FILE_ID_RESULT_SET_INDEX = 3;
        int ANCESTOR_REVISION_ID_RESULT_SET_INDEX = 4;
        int REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX = 5;
        int COMMIT_ID_RESULT_SET_INDEX = 6;
        int WORKFILE_EDIT_DATE_RESULT_SET_INDEX = 7;
        int REVISION_DIGEST_RESULT_SET_INDEX = 8;
        int REVISION_SIZE_RESULT_SET_INDEX = 9;
        // </editor-fold>

        String branchesToSearchString = buildBranchesToSearchString(branchArray);
        Integer releaseBranchCommitId = branchArray.get(0).getCommitId();
        Integer releaseBranchId = branchArray.get(0).getId();
        String queryString = String.format(findFileRevisionsInBranches, branchesToSearchString);
        LOGGER.trace("query string: [{}]", queryString);

        LOGGER.debug("findFileRevisionsInBranchesForReleaseBranch query string: [{}]", queryString);

        List<FileRevision> candidateRevisionList = new ArrayList<>();
        List<FileRevision> fileRevisionList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, fileId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Integer fetchedFileId = resultSet.getInt(FILE_ID_RESULT_SET_INDEX);
                Object fetchedAncestorRevisionObject = resultSet.getObject(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedAncestorRevisionId = null;
                if (fetchedAncestorRevisionObject != null) {
                    fetchedAncestorRevisionId = resultSet.getInt(ANCESTOR_REVISION_ID_RESULT_SET_INDEX);
                }
                Object fetchedReverseDeltaRevisionObject = resultSet.getObject(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                Integer fetchedReverseDeltaRevisionId = null;
                if (fetchedReverseDeltaRevisionObject != null) {
                    fetchedReverseDeltaRevisionId = resultSet.getInt(REVERSE_DELTA_REVISION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                Timestamp fetchedWorkfileEditDate = resultSet.getTimestamp(WORKFILE_EDIT_DATE_RESULT_SET_INDEX);
                byte[] fetchedDigest = resultSet.getBytes(REVISION_DIGEST_RESULT_SET_INDEX);
                Integer fetchedSize = resultSet.getInt(REVISION_SIZE_RESULT_SET_INDEX);

                FileRevision fileRevision = new FileRevision();
                fileRevision.setId(fetchedId);
                fileRevision.setBranchId(fetchedBranchId);
                fileRevision.setFileId(fetchedFileId);
                fileRevision.setAncestorRevisionId(fetchedAncestorRevisionId);
                fileRevision.setReverseDeltaRevisionId(fetchedReverseDeltaRevisionId);
                fileRevision.setCommitId(fetchedCommitId);
                fileRevision.setWorkfileEditDate(fetchedWorkfileEditDate);
                fileRevision.setRevisionDigest(fetchedDigest);
                fileRevision.setRevisionDataSize(fetchedSize);
                candidateRevisionList.add(fileRevision);
            }

            // Prune the list so that any newer revisions on a parent branch are discarded.
            for (FileRevision fileRevision : candidateRevisionList) {
                // Include all revisions on the requested branch
                if (fileRevision.getBranchId().intValue() == releaseBranchId.intValue()) {
                    fileRevisionList.add(fileRevision);
                } else {
                    // Only add parent revisions that are older than the commit id of the release branch.
                    if (fileRevision.getCommitId() < releaseBranchCommitId) {
                        fileRevisionList.add(fileRevision);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: SQL exception in findFileRevisionsInBranchesForReleaseBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesDAOImpl: exception in findFileRevisionsInBranchesForReleaseBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return fileRevisionList;
    }

    @Override
    public List<DirectoryLocation> findChildDirectoryLocations(List<Branch> branchArray, Integer parentDirectoryLocationId) {
        int ID_RESULT_SET_INDEX = 1;
        int DIRECTORY_ID_RESULT_SET_INDEX = 2;
        int BRANCH_ID_RESULT_SET_INDEX = 3;
        int PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX = 4;
        int COMMIT_ID_RESULT_SET_INDEX = 5;
        int DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX = 6;
        int DELETED_FLAG_RESULT_SET_INDEX = 7;

        String selectSegment = "SELECT ID, DIRECTORY_ID, BRANCH_ID, PARENT_DIRECTORY_LOCATION_ID, COMMIT_ID, DIRECTORY_SEGMENT_NAME, DELETED_FLAG FROM ";
        String findChildDirectoryLocations = selectSegment + this.schemaName
                + ".DIRECTORY_LOCATION WHERE PARENT_DIRECTORY_LOCATION_ID = ? AND BRANCH_ID IN (%s) AND DELETED_FLAG = FALSE ORDER BY DIRECTORY_SEGMENT_NAME, BRANCH_ID DESC";
        String findChildDirectoryLocationsForTagBranch = selectSegment + this.schemaName
                + ".DIRECTORY_LOCATION WHERE PARENT_DIRECTORY_LOCATION_ID = ? AND BRANCH_ID IN (%s) AND DELETED_FLAG = FALSE AND COMMIT_ID < %d "
                + "ORDER BY DIRECTORY_SEGMENT_NAME, BRANCH_ID DESC";

        // Create the SQL query string
        String branchesToSearchString = buildBranchesToSearchString(branchArray);

        // If this is a for a tagged branch, we use a different query.
        Integer tagCommitId = null;
        if (branchArray.get(0).getBranchTypeId() == QVCSConstants.QVCS_TAG_BASED_BRANCH_TYPE) {
            TagDAO tagDAO = new TagDAOImpl(schemaName);
            Tag tag = tagDAO.findById(branchArray.get(0).getTagId());
            tagCommitId = tag.getCommitId();
        }

        String queryString;
        if (tagCommitId == null) {
            queryString = String.format(findChildDirectoryLocations, branchesToSearchString);
        } else {
            queryString = String.format(findChildDirectoryLocationsForTagBranch, branchesToSearchString, tagCommitId);
        }

        LOGGER.debug("findChildDirectoryLocations query string: [{}]", queryString);
        List<DirectoryLocation> candidateDirectoryLocationList = new ArrayList<>();
        List<DirectoryLocation> directoryLocationList = new ArrayList<>();
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, parentDirectoryLocationId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedDirectoryLocationId = resultSet.getInt(ID_RESULT_SET_INDEX);
                Integer fetchedDirectoryId = resultSet.getInt(DIRECTORY_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(BRANCH_ID_RESULT_SET_INDEX);
                Object fetchedParentDirectoryLocationObject = resultSet.getObject(PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                Integer fetchedParentDirectoryLocationId = null;
                if (fetchedParentDirectoryLocationObject != null) {
                    fetchedParentDirectoryLocationId = resultSet.getInt(PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(COMMIT_ID_RESULT_SET_INDEX);
                String fetchedDirectorySegmentName = resultSet.getString(DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(DELETED_FLAG_RESULT_SET_INDEX);

                DirectoryLocation directoryLocation = new DirectoryLocation();
                directoryLocation.setId(fetchedDirectoryLocationId);
                directoryLocation.setDirectoryId(fetchedDirectoryId);
                directoryLocation.setBranchId(fetchedBranchId);
                directoryLocation.setParentDirectoryLocationId(fetchedParentDirectoryLocationId);
                directoryLocation.setCommitId(fetchedCommitId);
                directoryLocation.setDirectorySegmentName(fetchedDirectorySegmentName);
                directoryLocation.setDeletedFlag(fetchedDeletedFlag);
                candidateDirectoryLocationList.add(directoryLocation);
            }

            // Prune the list.
            Map<String, DirectoryLocation> locationMap = new TreeMap<>();
            for (DirectoryLocation directoryLocation : candidateDirectoryLocationList) {
                // Include all directory locations on the requested branch
                if (directoryLocation.getBranchId().intValue() == branchArray.get(0).getId().intValue()) {
                    directoryLocationList.add(directoryLocation);
                    locationMap.put(directoryLocation.getDirectorySegmentName(), directoryLocation);
                } else {
                    if (!locationMap.containsKey(directoryLocation.getDirectorySegmentName())) {
                        // We have not seen this directory on the branch, so include it.
                        directoryLocationList.add(directoryLocation);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.error("DirectoryLocationDAOImpl: SQL exception in findChildDirectoryLocations", e);
        } catch (IllegalStateException e) {
            LOGGER.error("DirectoryLocationDAOImpl: exception in findChildDirectoryLocations", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
        return directoryLocationList;
    }

    @Override
    public String buildBranchesToSearchString(List<Branch> branchArray) {
        // Create the SQL query string
        StringBuilder branchesToSearch = new StringBuilder();
        for (int i = 0; i < branchArray.size(); i++) {
            branchesToSearch.append(branchArray.get(i).getId());
            if (i < (branchArray.size() - 1)) {
                branchesToSearch.append(",");
            }
        }
        return branchesToSearch.toString();
    }

    @Override
    public String buildIdsToSearchString(List<Integer> idsArray) {
        // Create the SQL query string
        StringBuilder fileIdsToSearch = new StringBuilder();
        for (int i = 0; i < idsArray.size(); i++) {
            fileIdsToSearch.append(idsArray.get(i));
            if (i < (idsArray.size() - 1)) {
                fileIdsToSearch.append(",");
            }
        }
        return fileIdsToSearch.toString();
    }

    private String buildSkinnyInfoQueryStringForBranch(Integer branchId, Integer directoryId) {
        List<Branch> branchAncestryList = getBranchAncestryList(branchId);
        String branchesToSearchString = buildBranchesToSearchString(branchAncestryList);
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        List<Integer> deletedFilesFileIdList = new ArrayList<>();
        List<Integer> fileNameIdList = fileNameDAO.getFileNameIdList(branchesToSearchString, directoryId, deletedFilesFileIdList);
        String fileNameIdsToInclude = buildIdsToSearchString(fileNameIdList);

        String notInFileIdClause = buildNotInFileIdClause(branchId, directoryId, deletedFilesFileIdList);

        String selectSegment = "SELECT UR.USER_NAME, CM.COMMIT_DATE, FN.FILE_NAME, FR.ID AS FRID, FR.FILE_ID, FR.REVISION_DIGEST, FR.BRANCH_ID, CM.ID FROM ";
        StringBuilder queryFormatStringBuilder = new StringBuilder(selectSegment);
        queryFormatStringBuilder.append(this.schemaName).append(".FILE_REVISION FR,")
                .append(this.schemaName).append(".COMIT CM,")
                .append(this.schemaName).append(".FILE_NAME FN,")
                .append(this.schemaName).append(".DIRECTORY_LOCATION DL,")
                .append(this.schemaName).append(".USER UR ")
                .append("WHERE ");
        if (fileNameIdsToInclude.length() > 0) {
            queryFormatStringBuilder.append("FN.ID IN (%s) AND ");
        }
        queryFormatStringBuilder.append("FR.BRANCH_ID IN (%s) AND ")
                .append("FR.COMMIT_ID = CM.ID AND ")
                .append("FR.PROMOTED_FLAG = FALSE AND ")
                .append("CM.USER_ID = UR.ID AND ")
                .append("FN.FILE_ID = FR.FILE_ID AND ")
                .append("FN.DELETED_FLAG = FALSE AND ")
                .append("DL.DIRECTORY_ID = FN.DIRECTORY_ID AND ");
        if (notInFileIdClause.length() > 0) {
            queryFormatStringBuilder.append(notInFileIdClause);
        }
        queryFormatStringBuilder.append("DL.DIRECTORY_ID = ? ")
                .append("ORDER BY FILE_ID, BRANCH_ID DESC, FR.ID DESC");
        String queryFormatString = queryFormatStringBuilder.toString();
        String queryString;
        if (fileNameIdsToInclude.length() > 0) {
            queryString = String.format(queryFormatString, fileNameIdsToInclude, branchesToSearchString);
        } else {
            queryString = String.format(queryFormatString, branchesToSearchString);
        }
        return queryString;
    }

    private String buildNotInFileIdClause(Integer branchId, Integer directoryId, List<Integer> deletedFilesFileIdList) {
        String notInFileIdListQueryClause = "";
        FileNameDAO fileNameDAO = new FileNameDAOImpl(schemaName);
        List<Integer> fileIdList = fileNameDAO.getNotInFileIdList(branchId, directoryId, deletedFilesFileIdList);
        if (!fileIdList.isEmpty()) {
            String notInFileIdList = buildIdsToSearchString(fileIdList);
            notInFileIdListQueryClause = String.format(" FN.FILE_ID NOT IN (%s) AND ", notInFileIdList);
        }
        return notInFileIdListQueryClause;
    }
}
