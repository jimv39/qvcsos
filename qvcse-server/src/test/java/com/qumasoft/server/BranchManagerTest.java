/*   Copyright 2004-2019 Jim Voris
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
package com.qumasoft.server;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ProjectPropertiesFactory;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteBranchProperties;
import com.qumasoft.server.dataaccess.impl.DAOTestHelper;
import java.io.File;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Branch manager test.
 * @author Jim Voris
 */
public class BranchManagerTest {

    private static final String DERBY_TEST_DIRECTORY_SUFFIX = "branchManagerTest";
    static private AbstractProjectProperties projectProperties = null;
    static private RemoteBranchProperties remoteBranchProperties = null;

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteBranchStore();
        TestHelper.emptyDerbyTestDirectory(TestHelper.buildTestDirectoryName(DERBY_TEST_DIRECTORY_SUFFIX));
        BranchManager.getInstance().initialize();
        DatabaseManager.getInstance().setDerbyHomeDirectory(TestHelper.buildTestDirectoryName(DERBY_TEST_DIRECTORY_SUFFIX));
        DatabaseManager.getInstance().initializeDatabase();
        TestHelper.initProjectProperties();
        projectProperties = ProjectPropertiesFactory.getProjectPropertiesFactory().buildProjectProperties(System.getProperty("user.dir"), TestHelper.getTestProjectName(),
                QVCSConstants.QVCS_SERVED_PROJECT_TYPE);
        remoteBranchProperties = new RemoteBranchProperties(TestHelper.getTestProjectName(), "Test Branch", projectProperties.getProjectProperties());
        remoteBranchProperties.setIsTranslucentBranchFlag(true);
        DAOTestHelper.createTestProject();
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteBranchStore();
    }

    /**
     * Set up the things common to all the tests.
     */
    @Before
    public void setUp() {
    }

    /**
     * We tear this down after each test.
     */
    @After
    public void tearDown() {
    }

    /**
     * Run the tests in order.
     * @throws Exception if there was a problem.
     */
    @Test
    public void testBranchManager() throws Exception {
        testAddBranch();
        testGetBranch();
        testGetBranches();
        testWriteBranchStore();
    }

    /**
     * Test of addBranch method, of class BranchManager.
     *
     * @throws Exception if something goes wrong.
     */
    public void testAddBranch() throws Exception {
        System.out.println("addBranch");
        ProjectBranch projectBranch = new ProjectBranch();
        projectBranch.setProjectName(TestHelper.getTestProjectName());
        projectBranch.setBranchName("Test Branch");
        projectBranch.setRemoteBranchProperties(remoteBranchProperties);
        BranchManager instance = BranchManager.getInstance();
        instance.addBranch(projectBranch);
        Collection<ProjectBranch> result = instance.getBranches(TestHelper.getTestProjectName());
        assertEquals("Unexpected number of branches returned", 1, result.size());
    }

    /**
     * Test of getBranch method, of class BranchManager.
     */
    public void testGetBranch() {
        String projectName = TestHelper.getTestProjectName();
        String branchName = "Test Branch";
        BranchManager instance = BranchManager.getInstance();
        ProjectBranch result = instance.getBranch(projectName, branchName);
        assertEquals("Branch name is not the expected value.", result.getBranchName(), branchName);
        assertEquals("Project name is not the expected value.", result.getProjectName(), projectName);
    }

    /**
     * Test of getBranches method, of class BranchManager.
     */
    public void testGetBranches() {
        String projectName = TestHelper.getTestProjectName();
        BranchManager instance = BranchManager.getInstance();
        Collection<ProjectBranch> result = instance.getBranches(projectName);
        assertEquals("Unexpected number of branches returned", 1, result.size());
    }

    /**
     * Test of writeBranchStore method, of class BranchManager.
     */
    public void testWriteBranchStore() {
        BranchManager.getInstance().writeBranchStore();
    }

    private static void deleteBranchStore() {
        File branchStore = new File(QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY + File.separator + QVCSConstants.QVCS_BRANCH_STORE_NAME + "dat");
        if (branchStore.exists()) {
            branchStore.delete();
        }
        File oldBranchStore = new File(QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY + File.separator + QVCSConstants.QVCS_BRANCH_STORE_NAME + "dat.old");
        if (oldBranchStore.exists()) {
            oldBranchStore.delete();
        }
    }
}
