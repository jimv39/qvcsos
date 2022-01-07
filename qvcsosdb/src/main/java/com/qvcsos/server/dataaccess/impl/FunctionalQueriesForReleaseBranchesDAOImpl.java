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

import com.qumasoft.qvcslib.DirectoryCoordinateIds;
import com.qumasoft.qvcslib.SkinnyLogfileInfo;
import com.qvcsos.server.DatabaseManager;
import com.qvcsos.server.dataaccess.FunctionalQueriesDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesForReadOnlyBranchesDAO;
import com.qvcsos.server.dataaccess.FunctionalQueriesForReleaseBranchesDAO;
import com.qvcsos.server.datamodel.Branch;
import com.qvcsos.server.datamodel.DirectoryLocation;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Jim Voris
 */
public class FunctionalQueriesForReleaseBranchesDAOImpl implements FunctionalQueriesForReleaseBranchesDAO {
    /**
     * Create our logger object.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalQueriesForReleaseBranchesDAOImpl.class);

    private final String schemaName;

    // <editor-fold>
    // Result set field indexes for skinny info queries.
    private static final int USER_NAME_SET_INDEX = 1;
    private static final int COMMIT_DATE_RESULT_SET_INDEX = 2;
    private static final int FILE_NAME_RESULT_SET_INDEX = 3;
    private static final int FILE_REVISION_ID_RESULT_SET_INDEX = 4;
    private static final int FILE_ID_RESULT_SET_INDEX = 5;
    private static final int REVISION_DIGEST_RESULT_SET_INDEX = 6;
    private static final int BRANCH_ID_RESULT_SET_INDEX = 7;
    private static final int COMMIT_ID_RESULT_SET_INDEX = 8;
    // </editor-fold>

    // <editor-fold>
    // Result set field indexes for directory location queries.
    private static final int DL_ID_RESULT_SET_INDEX = 1;
    private static final int DL_DIRECTORY_ID_RESULT_SET_INDEX = 2;
    private static final int DL_BRANCH_ID_RESULT_SET_INDEX = 3;
    private static final int DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX = 4;
    private static final int DL_COMMIT_ID_RESULT_SET_INDEX = 5;
    private static final int DL_DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX = 6;
    private static final int DL_DELETED_FLAG_RESULT_SET_INDEX = 7;
    // <editor-fold>

    public FunctionalQueriesForReleaseBranchesDAOImpl(String schema) {
        this.schemaName = schema;
    }

    /**
     * Get the skinnyInfo for a feature branch.
     *
     * @param branch the read-only branch that we're working on.
     * @param boundingCommitId the boundary commit id.
     * @param ids the directory coordinate ids.
     * @return the list of skinnyLogfileInfo's for the requested directory.
     */
    @Override
    public List<SkinnyLogfileInfo> getSkinnyLogfileInfoForReleaseBranches(Branch branch, int boundingCommitId, DirectoryCoordinateIds ids) {
        // The read-only branch query will do a lot of the work we need:
        FunctionalQueriesForReadOnlyBranchesDAO functionalQueriesForReadOnlyBranchesDAO = new FunctionalQueriesForReadOnlyBranchesDAOImpl(schemaName);
        List<SkinnyLogfileInfo> skinnyList = functionalQueriesForReadOnlyBranchesDAO.getSkinnyLogfileInfoForReadOnlyBranch(branch, boundingCommitId, ids);

        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);

        // This is a map of maps; outer key is file_id; inner key is commit_id, String is fileName.
        Map<Integer, Map<Integer, String>> candidateFileNamesMap = new TreeMap<>();
        runFileNameQueryForReleaseBranch(candidateFileNamesMap, ids.getDirectoryId(), branch);

        List<Integer> fileIdList = new ArrayList<>();
        for (Integer fileId : candidateFileNamesMap.keySet()) {
            fileIdList.add(fileId);
        }
        String fileIdsToSearchString = functionalQueriesDAO.buildIdsToSearchString(fileIdList);

        // Keyed by file_id, then by revision id.
        Map<Integer, Map<Integer, SkinnyLogfileInfo>> candidateMap = new TreeMap<>();
        runSkinnyInfoQueryForReleaseBranch(candidateMap, ids.getDirectoryId(), fileIdsToSearchString, branch);

        // Add the skinny info from the read-only query to our Map of Maps so we'll harvest the correct info.
        for (SkinnyLogfileInfo skinnyInfo : skinnyList) {
            Map<Integer, SkinnyLogfileInfo> candidatesMap = candidateMap.get(skinnyInfo.getFileID());
            if (candidatesMap == null) {
                candidatesMap = new TreeMap<>();
                candidateMap.put(skinnyInfo.getFileID(), candidatesMap);
            }
            candidatesMap.put(skinnyInfo.getFileRevisionId(), skinnyInfo);
        }

        // TODO -- if I ever support directory moves and/or renames, then I will need to add queries;
        // one for directory_location_history, and another for file_name_history and directory_location_history.
        // Since I do not yet support directory moves and/or renames, we'll skip that for now.
        for (Map<Integer, SkinnyLogfileInfo> candidatesMap : candidateMap.values()) {
            // Use the sorting of the TreeMap so that the last entry is the one we want, since it is the newest revision.
            Object[] skinnyArray = candidatesMap.values().toArray();
            SkinnyLogfileInfo skinnyInfo = (SkinnyLogfileInfo) skinnyArray[skinnyArray.length - 1];
            skinnyInfo.setRevisionCount(candidatesMap.size());

            // Figure out the filename to use...
            Map<Integer, String> fileNameMap = candidateFileNamesMap.get(skinnyInfo.getFileID());
            if (fileNameMap != null) {
                Object[] fileNameArray = fileNameMap.values().toArray();
                // The newest commit_id wins as the filename.
                String fileName = (String) fileNameArray[fileNameArray.length - 1];
                skinnyInfo.setShortWorkfileName(fileName);
            }

            skinnyList.add(skinnyInfo);
            LOGGER.debug("***===>>> Branch::: BranchId: [{}] directoryId: [{}] filename: [{}] Default revision string: [{}]", branch.getId(), ids.getDirectoryId(),
                    skinnyInfo.getShortWorkfileName(), skinnyInfo.getDefaultRevisionString());
        }

        return skinnyList;
    }

    private void runSkinnyInfoQueryForReleaseBranch(Map<Integer, Map<Integer, SkinnyLogfileInfo>> candidateMap, int directoryId, String fileIdsToSearchString, Branch branch) {
        String queryString = buildSkinnyInfoQueryStringForReleaseBranch(fileIdsToSearchString);
        if (queryString != null) {
            LOGGER.debug("Release branch query string: [{}]", queryString);

            ResultSet resultSet = null;
            PreparedStatement preparedStatement = null;
            try {
                Connection connection = DatabaseManager.getInstance().getConnection();
                preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                preparedStatement.setInt(1, branch.getId());
                preparedStatement.setInt(2, directoryId);

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
                    mapForCurrentFile.put(skinnyInfo.getFileRevisionId(), skinnyInfo);
                }
            } catch (SQLException | IllegalStateException e) {
                LOGGER.error("FunctionalQueriesForReleaseBranchesDAOImpl: SQL exception in run1stQuery", e);
                throw new RuntimeException(e);
            } finally {
                DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
            }
        } else {
            LOGGER.debug("Skipping 3rd query for Release branch for directoryId: [{}]", directoryId);
        }
    }

    @Override
    public List<DirectoryLocation> findChildDirectoryLocationsForBranch(Branch branch, List<Branch> branchArray, int boundingCommitId, int parentDirectoryLocationId) {

        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);

        String branchesToSearchString = functionalQueriesDAO.buildBranchesToSearchString(branchArray);

        Map<String, DirectoryLocation> directoryLocationMap = new TreeMap<>();
        List<DirectoryLocation> directoryLocationList = new ArrayList<>();

        run1stDirectoryLocationQuery(parentDirectoryLocationId, branchesToSearchString, boundingCommitId, directoryLocationMap);
        run2ndDirectoryLocationQuery(parentDirectoryLocationId, branchesToSearchString, boundingCommitId, directoryLocationMap);

        for (DirectoryLocation directoryLocation : directoryLocationMap.values()) {
            directoryLocationList.add(directoryLocation);
        }
        return directoryLocationList;
    }

    private void run1stDirectoryLocationQuery(int parentDirectoryLocationId, String branchesToSearchString, Integer tagCommitId, Map<String, DirectoryLocation> directoryLocationMap) {
        // Create the SQL query string
        String selectSegment = "SELECT ID, DIRECTORY_ID, BRANCH_ID, PARENT_DIRECTORY_LOCATION_ID, COMMIT_ID, DIRECTORY_SEGMENT_NAME, DELETED_FLAG FROM ";
        String findChildDirectoryLocationsForTagBranch = selectSegment + this.schemaName
                + ".DIRECTORY_LOCATION WHERE PARENT_DIRECTORY_LOCATION_ID = ? AND BRANCH_ID IN (%s) AND COMMIT_ID < %d "
                + "ORDER BY DIRECTORY_SEGMENT_NAME, BRANCH_ID DESC";

        String queryString = String.format(findChildDirectoryLocationsForTagBranch, branchesToSearchString, tagCommitId);

        LOGGER.debug("run1stDirectoryLocationQuery query string: [{}]", queryString);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, parentDirectoryLocationId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedDirectoryLocationId = resultSet.getInt(DL_ID_RESULT_SET_INDEX);
                Integer fetchedDirectoryId = resultSet.getInt(DL_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(DL_BRANCH_ID_RESULT_SET_INDEX);
                Object fetchedParentDirectoryLocationObject = resultSet.getObject(DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                Integer fetchedParentDirectoryLocationId = null;
                if (fetchedParentDirectoryLocationObject != null) {
                    fetchedParentDirectoryLocationId = resultSet.getInt(DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(DL_COMMIT_ID_RESULT_SET_INDEX);
                String fetchedDirectorySegmentName = resultSet.getString(DL_DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(DL_DELETED_FLAG_RESULT_SET_INDEX);

                DirectoryLocation directoryLocation = new DirectoryLocation();
                directoryLocation.setId(fetchedDirectoryLocationId);
                directoryLocation.setDirectoryId(fetchedDirectoryId);
                directoryLocation.setBranchId(fetchedBranchId);
                directoryLocation.setParentDirectoryLocationId(fetchedParentDirectoryLocationId);
                directoryLocation.setCommitId(fetchedCommitId);
                directoryLocation.setDirectorySegmentName(fetchedDirectorySegmentName);
                directoryLocation.setDeletedFlag(fetchedDeletedFlag);
                directoryLocationMap.put(directoryLocation.getDirectorySegmentName(), directoryLocation);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesForReleaseBranchesDAOImpl: SQL exception in run1stDirectoryLocationQuery", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesForReleaseBranchesDAOImpl: exception in run1stDirectoryLocationQuery", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
    }

    private void run2ndDirectoryLocationQuery(int parentDirectoryLocationId, String branchesToSearchString, Integer tagCommitId, Map<String, DirectoryLocation> directoryLocationMap) {
        // Create the SQL query string
        String selectSegment = "SELECT DIRECTORY_LOCATION_ID, DIRECTORY_ID, BRANCH_ID, PARENT_DIRECTORY_LOCATION_ID, COMMIT_ID, DIRECTORY_SEGMENT_NAME, DELETED_FLAG FROM ";
        String findChildDirectoryLocationsForTagBranch = selectSegment + this.schemaName
                + ".DIRECTORY_LOCATION_HISTORY WHERE PARENT_DIRECTORY_LOCATION_ID = ? AND BRANCH_ID IN (%s) AND COMMIT_ID < %d "
                + "ORDER BY DIRECTORY_SEGMENT_NAME, BRANCH_ID DESC";

        String queryString = String.format(findChildDirectoryLocationsForTagBranch, branchesToSearchString, tagCommitId);

        LOGGER.debug("run2ndDirectoryLocationQuery query string: [{}]", queryString);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, parentDirectoryLocationId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedDirectoryLocationId = resultSet.getInt(DL_ID_RESULT_SET_INDEX);
                Integer fetchedDirectoryId = resultSet.getInt(DL_DIRECTORY_ID_RESULT_SET_INDEX);
                Integer fetchedBranchId = resultSet.getInt(DL_BRANCH_ID_RESULT_SET_INDEX);
                Object fetchedParentDirectoryLocationObject = resultSet.getObject(DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                Integer fetchedParentDirectoryLocationId = null;
                if (fetchedParentDirectoryLocationObject != null) {
                    fetchedParentDirectoryLocationId = resultSet.getInt(DL_PARENT_DIRECTORY_LOCATION_ID_RESULT_SET_INDEX);
                }
                Integer fetchedCommitId = resultSet.getInt(DL_COMMIT_ID_RESULT_SET_INDEX);
                String fetchedDirectorySegmentName = resultSet.getString(DL_DIRECTORY_SEGMENT_NAME_RESULT_SET_INDEX);
                Boolean fetchedDeletedFlag = resultSet.getBoolean(DL_DELETED_FLAG_RESULT_SET_INDEX);

                DirectoryLocation directoryLocation = new DirectoryLocation();
                directoryLocation.setId(fetchedDirectoryLocationId);
                directoryLocation.setDirectoryId(fetchedDirectoryId);
                directoryLocation.setBranchId(fetchedBranchId);
                directoryLocation.setParentDirectoryLocationId(fetchedParentDirectoryLocationId);
                directoryLocation.setCommitId(fetchedCommitId);
                directoryLocation.setDirectorySegmentName(fetchedDirectorySegmentName);
                directoryLocation.setDeletedFlag(fetchedDeletedFlag);
                directoryLocationMap.put(directoryLocation.getDirectorySegmentName(), directoryLocation);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesForReleaseBranchesDAOImpl: SQL exception in run2ndDirectoryLocationQuery", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesForReleaseBranchesDAOImpl: exception in run2ndDirectoryLocationQuery", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
    }

    private void runFileNameQueryForReleaseBranch(Map<Integer, Map<Integer, String>> candidateFileNamesMap, int directoryId, Branch featureBranch) {
        FunctionalQueriesDAO functionalQueriesDAO = new FunctionalQueriesDAOImpl(schemaName);
        List<Branch> branchAncestryList = functionalQueriesDAO.getBranchAncestryList(featureBranch.getId());
        String branchesToSearchString = functionalQueriesDAO.buildBranchesToSearchString(branchAncestryList);

        String selectSegment = "SELECT FN.FILE_ID, FN.COMMIT_ID, FN.FILE_NAME FROM ";
        StringBuilder featureBranchQueryFormatStringBuilder = new StringBuilder(selectSegment);
        String findFeatureBranchFileNames = featureBranchQueryFormatStringBuilder.append(this.schemaName).append(".FILE_NAME FN ")
                .append("WHERE ")
                .append("BRANCH_ID IN (%s) AND ")
                .append("DIRECTORY_ID = ? AND ")
                .append("DELETED_FLAG = FALSE").toString();
        String queryString = String.format(findFeatureBranchFileNames, branchesToSearchString);
        LOGGER.debug("runFileNameQueryForReleaseBranch query string: [{}]", queryString);
        ResultSet resultSet = null;
        PreparedStatement preparedStatement = null;
        try {
            Connection connection = DatabaseManager.getInstance().getConnection();
            preparedStatement = connection.prepareStatement(queryString, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setInt(1, directoryId);

            resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                Integer fetchedFileId = resultSet.getInt(1);
                Integer fetchedCommitId = resultSet.getInt(2);
                String fetchedFileName = resultSet.getString(3);
                Map<Integer, String> commitIdMap = candidateFileNamesMap.get(fetchedFileId);
                if (commitIdMap == null) {
                    commitIdMap = new TreeMap<>();
                }
                commitIdMap.put(fetchedCommitId, fetchedFileName);
                candidateFileNamesMap.put(fetchedFileId, commitIdMap);
            }
        } catch (SQLException e) {
            LOGGER.error("FunctionalQueriesForReleaseBranchesDAOImpl: SQL exception in run1stFileNameQueryForFeatureBranch", e);
        } catch (IllegalStateException e) {
            LOGGER.error("FunctionalQueriesForReleaseBranchesDAOImpl: exception in run1stFileNameQueryForFeatureBranch", e);
            throw e;
        } finally {
            DAOHelper.closeDbResources(LOGGER, resultSet, preparedStatement);
        }
    }

    private String buildSkinnyInfoQueryStringForReleaseBranch(String fileIdsToSearchString) {
        String queryString = null;
        if (!fileIdsToSearchString.isEmpty()) {

            String selectSegment = "SELECT UR.USER_NAME, CM.COMMIT_DATE, FN.FILE_NAME, FR.ID AS FRID, FR.FILE_ID, FR.REVISION_DIGEST, FR.BRANCH_ID, CM.ID FROM ";
            StringBuilder queryFormatStringBuilder = new StringBuilder(selectSegment);
            queryFormatStringBuilder.append(this.schemaName).append(".FILE_REVISION FR,")
                    .append(this.schemaName).append(".COMIT CM,")
                    .append(this.schemaName).append(".FILE_NAME FN,")
                    .append(this.schemaName).append(".DIRECTORY_LOCATION DL,")
                    .append(this.schemaName).append(".USER UR ")
                    .append("WHERE ");
            if (fileIdsToSearchString.length() > 0) {
                queryFormatStringBuilder.append("FN.FILE_ID IN (%s) AND ");
            }
            queryFormatStringBuilder.append("FR.BRANCH_ID = ? AND ")
                    .append("FR.COMMIT_ID = CM.ID AND ")
                    .append("FR.PROMOTED_FLAG = FALSE AND ")
                    .append("CM.USER_ID = UR.ID AND ")
                    .append("FN.FILE_ID = FR.FILE_ID AND ")
                    .append("FN.DELETED_FLAG = FALSE AND ")
                    .append("DL.DIRECTORY_ID = FN.DIRECTORY_ID AND ")
                    .append("DL.DIRECTORY_ID = ? ")
                    .append("ORDER BY FR.FILE_ID, FR.ID DESC");
            String queryFormatString = queryFormatStringBuilder.toString();
            if (fileIdsToSearchString.length() > 0) {
                queryString = String.format(queryFormatString, fileIdsToSearchString);
            }
        }
        return queryString;
    }
}
