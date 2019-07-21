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
 * View manager test.
 * @author Jim Voris
 */
public class ViewManagerTest {

    private static final String DERBY_TEST_DIRECTORY_SUFFIX = "viewManagerTest";
    static private AbstractProjectProperties projectProperties = null;
    static private RemoteBranchProperties remoteViewProperties = null;

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        deleteViewStore();
        TestHelper.emptyDerbyTestDirectory(TestHelper.buildTestDirectoryName(DERBY_TEST_DIRECTORY_SUFFIX));
        ViewManager.getInstance().initialize();
        DatabaseManager.getInstance().setDerbyHomeDirectory(TestHelper.buildTestDirectoryName(DERBY_TEST_DIRECTORY_SUFFIX));
        DatabaseManager.getInstance().initializeDatabase();
        TestHelper.initProjectProperties();
        projectProperties = ProjectPropertiesFactory.getProjectPropertiesFactory().buildProjectProperties(System.getProperty("user.dir"), TestHelper.getTestProjectName(),
                QVCSConstants.QVCS_SERVED_PROJECT_TYPE);
        remoteViewProperties = new RemoteBranchProperties(TestHelper.getTestProjectName(), "Test View", projectProperties.getProjectProperties());
        remoteViewProperties.setIsTranslucentBranchFlag(true);
        DAOTestHelper.createTestProject();
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        deleteViewStore();
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
    public void testViewManager() throws Exception {
        testAddView();
        testGetView();
        testGetViews();
        testWriteViewStore();
    }

    /**
     * Test of addView method, of class ViewManager.
     *
     * @throws Exception if something goes wrong.
     */
    public void testAddView() throws Exception {
        System.out.println("addView");
        ProjectBranch projectView = new ProjectBranch();
        projectView.setProjectName(TestHelper.getTestProjectName());
        projectView.setBranchName("Test View");
        projectView.setRemoteBranchProperties(remoteViewProperties);
        ViewManager instance = ViewManager.getInstance();
        instance.addView(projectView);
        Collection<ProjectBranch> result = instance.getViews(TestHelper.getTestProjectName());
        assertEquals("Unexpected number of views returned", 1, result.size());
    }

    /**
     * Test of getView method, of class ViewManager.
     */
    public void testGetView() {
        String projectName = TestHelper.getTestProjectName();
        String viewName = "Test View";
        ViewManager instance = ViewManager.getInstance();
        ProjectBranch result = instance.getView(projectName, viewName);
        assertEquals("View name is not the expected value.", result.getBranchName(), viewName);
        assertEquals("Project name is not the expected value.", result.getProjectName(), projectName);
    }

    /**
     * Test of getViews method, of class ViewManager.
     */
    public void testGetViews() {
        String projectName = TestHelper.getTestProjectName();
        ViewManager instance = ViewManager.getInstance();
        Collection<ProjectBranch> result = instance.getViews(projectName);
        assertEquals("Unexpected number of views returned", 1, result.size());
    }

    /**
     * Test of writeViewStore method, of class ViewManager.
     */
    public void testWriteViewStore() {
        ViewManager.getInstance().writeViewStore();
    }

    private static void deleteViewStore() {
        File viewStore = new File(QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY + File.separator + QVCSConstants.QVCS_VIEW_STORE_NAME + "dat");
        if (viewStore.exists()) {
            viewStore.delete();
        }
        File oldViewStore = new File(QVCSConstants.QVCS_ADMIN_DATA_DIRECTORY + File.separator + QVCSConstants.QVCS_VIEW_STORE_NAME + "dat.old");
        if (oldViewStore.exists()) {
            oldViewStore.delete();
        }
    }
}
