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
//   $Header: ProjectDAOImplTest.java Revision:1.3 Wednesday, March 21, 2012 10:31:06 PM JimVoris $
// $Copyright © 2011-2012 Define this string in the qvcs.keywords.properties property file $

package com.qumasoft.server.dataaccess.impl;

import com.qumasoft.TestHelper;
import com.qumasoft.server.DatabaseManager;
import com.qumasoft.server.datamodel.Project;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test the Project DAO implementation.
 *
 * @author $Author: JimVoris $
 */
public class ProjectDAOImplTest {

    private static final String DERBY_TEST_DIRECTORY = "/temp/qvcse/projectDAOImplTest";
    private static int testProjectId = -1;

    /**
     * Execute this stuff once when the class is loaded.
     *
     * @throws Exception if we have a problem with initialization.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.emptyDerbyTestDirectory(DERBY_TEST_DIRECTORY);
        DatabaseManager.getInstance().setDerbyHomeDirectory(DERBY_TEST_DIRECTORY);
        DatabaseManager.getInstance().initializeDatabase();
        testProjectId = DAOTestHelper.createTestProject();
    }

    /**
     * Execute this just once after we complete all the tests defined in this class.
     *
     * @throws Exception if we have a problem tearing things down.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
        DatabaseManager.getInstance().shutdownDatabase();
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
     * Test of findById method, of class ProjectDAOImpl.
     */
    @Test
    public void testFindById() {
        ProjectDAOImpl instance = new ProjectDAOImpl();
        Project result = instance.findById(testProjectId);
        assertNotNull("Did not find test project find by id", result);
    }

    /**
     * Test of findByProjectName method, of class ProjectDAOImpl.
     */
    @Test
    public void testFindByProjectName() {
        String projectName = TestHelper.getTestProjectName();
        ProjectDAOImpl instance = new ProjectDAOImpl();
        Project result = instance.findByProjectName(projectName);
        assertNotNull("Did not find test project find by name", result);
        assertEquals(TestHelper.getTestProjectName(), result.getProjectName());
    }

    /**
     * Test of findAll method, of class ProjectDAOImpl.
     */
    @Test
    public void testFindAll() {
        ProjectDAOImpl instance = new ProjectDAOImpl();
        List<Project> result = instance.findAll();
        assertTrue("Empty find all list", result.size() > 0);
    }

    /**
     * Test of insert method, of class ProjectDAOImpl.
     *
     * @throws Exception if there was a problem.
     */
    @Test
    public void testInsert() throws Exception {
        Project project = new Project();
        String projectName = TestHelper.getTestProjectName() + " Again";
        project.setProjectName(projectName);
        ProjectDAOImpl instance = new ProjectDAOImpl();
        instance.insert(project);
        Project foundProject = instance.findByProjectName(projectName);
        assertNotNull("Did not find new project by name", foundProject);
        assertEquals("Project Names did not match", projectName, foundProject.getProjectName());
        assertTrue("Bad new project id", foundProject.getProjectId() > testProjectId);
    }

    /**
     * Test of delete method, of class ProjectDAOImpl.
     *
     * @throws Exception if there was a problem.
     */
    @Test
    public void testDelete() throws Exception {
        ProjectDAOImpl instance = new ProjectDAOImpl();
        Project testProject = instance.findById(testProjectId);
        instance.delete(testProject);
        List<Project> projectList = instance.findAll();
        boolean foundTestProjectId = false;
        for (Project project : projectList) {
            if (project.getProjectId().intValue() == testProjectId) {
                foundTestProjectId = true;
                break;
            }
        }
        assertTrue("Did not delete the test project!", !foundTestProjectId);
    }
}
