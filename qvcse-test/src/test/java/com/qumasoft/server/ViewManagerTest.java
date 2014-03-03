//   Copyright 2004-2014 Jim Voris
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//
// $FilePath$
//     $Date: Wednesday, March 21, 2012 10:31:06 PM $
//   $Header: ViewManagerTest.java Revision:1.4 Wednesday, March 21, 2012 10:31:06 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.server;

import com.qumasoft.TestHelper;
import com.qumasoft.qvcslib.AbstractProjectProperties;
import com.qumasoft.qvcslib.ProjectPropertiesFactory;
import com.qumasoft.qvcslib.QVCSConstants;
import com.qumasoft.qvcslib.RemoteViewProperties;
import com.qumasoft.server.dataaccess.impl.DAOTestHelper;
import java.io.File;
import java.util.Collection;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author Jim Voris
 */
public class ViewManagerTest {

    private static final String DERBY_TEST_DIRECTORY = "/temp/qvcse/viewManagerTest";
    static private AbstractProjectProperties projectProperties = null;
    static private RemoteViewProperties remoteViewProperties = null;

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.emptyDerbyTestDirectory(DERBY_TEST_DIRECTORY);
        ViewManager.getInstance().initialize();
        DatabaseManager.getInstance().setDerbyHomeDirectory(DERBY_TEST_DIRECTORY);
        DatabaseManager.getInstance().initializeDatabase();
        TestHelper.initProjectProperties();
        projectProperties = ProjectPropertiesFactory.getProjectPropertiesFactory().buildProjectProperties(TestHelper.getTestProjectName(), QVCSConstants.QVCS_SERVED_PROJECT_TYPE);
        remoteViewProperties = new RemoteViewProperties(TestHelper.getTestProjectName(), "Test View", projectProperties.getProjectProperties());
        remoteViewProperties.setIsTranslucentBranchFlag(true);
        deleteViewStore();
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
     * Test of addView method, of class ViewManager.
     *
     * @throws Exception if something goes wrong.
     */
    @Test
    public void testAddView() throws Exception {
        System.out.println("addView");
        ProjectView projectView = new ProjectView();
        projectView.setProjectName(TestHelper.getTestProjectName());
        projectView.setViewName("Test View");
        projectView.setRemoteViewProperties(remoteViewProperties);
        ViewManager instance = ViewManager.getInstance();
        instance.addView(projectView);
    }

    /**
     * Test of getView method, of class ViewManager.
     */
    @Test
    public void testGetView() {
        String projectName = TestHelper.getTestProjectName();
        String viewName = "Test View";
        ViewManager instance = ViewManager.getInstance();
        ProjectView result = instance.getView(projectName, viewName);
        assertEquals("View name is not the expected value.", result.getViewName(), viewName);
        assertEquals("Project name is not the expected value.", result.getProjectName(), projectName);
    }

    /**
     * Test of getViews method, of class ViewManager.
     */
    @Test
    public void testGetViews() {
        String projectName = TestHelper.getTestProjectName();
        ViewManager instance = ViewManager.getInstance();
        Collection<ProjectView> result = instance.getViews(projectName);
        assertEquals("Unexpected number of views returned", result.size(), 1);
    }

    /**
     * Test of writeViewStore method, of class ViewManager.
     */
    @Test
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
