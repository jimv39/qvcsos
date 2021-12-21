/*   Copyright 2004-2014 Jim Voris
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
import com.qvcsos.server.datamodel.RoleType;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Role manager test.
 * @author Jim Voris
 */
public class RoleManagerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.resetTestDatabaseViaPsqlScript();
        TestHelper.resetQvcsosTestDatabaseViaPsqlScript();
        RoleManager.getRoleManager().initialize();
        RolePrivilegesManager.getInstance().initialize();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of getRoleManager method, of class com.qumasoft.server.RoleManager.
     */
    @Test
    public void testGetRoleManager() {
        System.out.println("testGetRoleManager");

        // Add your test code below by replacing the default call to fail.
        RoleManager roleManager = RoleManager.getRoleManager();
        if (roleManager == null) {
            fail("Failed getRoleManager().");
        }
    }

    /**
     * Test of initialize method, of class com.qumasoft.server.RoleManager.
     */
    @Test
    public void testInitialize() {
        System.out.println("testInitialize");

        // Add your test code below by replacing the default call to fail.
        if (RoleManager.getRoleManager().initialize() != true) {
            fail("Failed initialize()");
        }
    }

    @Test
    public void testAddAndRemoveRoles() {
        testAddUserRole();
        testRemoveUserRole();
    }

    /**
     * Test of isUserInRole method, of class com.qumasoft.server.RoleManager.
     */
    public void testAddUserRole() {
        System.out.println("testAddUserRole");

        boolean testResult = false;
        RoleType adminRole = RoleManager.getRoleManager().ADMIN_ROLE;
        String adminRoleType = adminRole.getRoleName();

        if (RoleManager.getRoleManager().addUserRole(adminRoleType, TestHelper.getTestProjectName(), "JoeSmith", RoleManager.getRoleManager().PROJECT_ADMIN_ROLE)) {
            if (RolePrivilegesManager.getInstance().isUserPrivileged(TestHelper.getTestProjectName(), "JoeSmith", RolePrivilegesManager.ADD_DIRECTORY)) {
                if (RoleManager.getRoleManager().addUserRole("JoeSmith", TestHelper.getTestProjectName(), "JaneSmith", RoleManager.getRoleManager().READER_ROLE)) {
                    if (RolePrivilegesManager.getInstance().isUserPrivileged(TestHelper.getTestProjectName(), "JaneSmith", RolePrivilegesManager.GET)) {
                        if (RoleManager.getRoleManager().addUserRole("JoeSmith", TestHelper.getTestProjectName(), "JeffSmith", RoleManager.getRoleManager().WRITER_ROLE)) {
                            if (RolePrivilegesManager.getInstance().isUserPrivileged(TestHelper.getTestProjectName(), "JeffSmith", RolePrivilegesManager.CHECK_IN)) {
                                if (RoleManager.getRoleManager().addUserRole(adminRoleType, TestHelper.getTestProjectName(), "JoeAdmin", RoleManager.getRoleManager().PROJECT_ADMIN_ROLE)) {
                                    if (RoleManager.getRoleManager().addUserRole("JoeAdmin", TestHelper.getTestProjectName(), "RalphSmith", RoleManager.getRoleManager().READER_ROLE)) {
                                        if (RoleManager.getRoleManager().addUserRole("JoeAdmin", TestHelper.getTestProjectName(), "RalphSmith", RoleManager.getRoleManager().WRITER_ROLE)) {
                                            testResult = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (testResult == false) {
            fail("Failed isUserInRole()");
        }
    }

    /**
     * Test of removeUserRole method, of class com.qumasoft.server.RoleManager.
     */
    public void testRemoveUserRole() {
        boolean testResult = false;

        System.out.println("testRemoveUserRole");

        if (RoleManager.getRoleManager().removeUserRole("JoeAdmin", TestHelper.getTestProjectName(), "RalphSmith", RoleManager.getRoleManager().WRITER_ROLE)) {
            if (RoleManager.getRoleManager().removeUserRole("JoeAdmin", TestHelper.getTestProjectName(), "RalphSmith", RoleManager.getRoleManager().READER_ROLE)) {
                if (RoleManager.getRoleManager().removeUserRole("JoeAdmin", TestHelper.getTestProjectName(), "JoeAdmin", RoleManager.getRoleManager().PROJECT_ADMIN_ROLE)) {
                    if (RoleManager.getRoleManager().removeUserRole(RoleManager.getRoleManager().ADMIN_ROLE.getRoleType(), TestHelper.getTestProjectName(), "JeffSmith", RoleManager.getRoleManager().WRITER_ROLE)) {
                        if (RoleManager.getRoleManager().removeUserRole(RoleManager.getRoleManager().ADMIN_ROLE.getRoleType(), TestHelper.getTestProjectName(), "JaneSmith", RoleManager.getRoleManager().READER_ROLE)) {
                            if (RoleManager.getRoleManager().removeUserRole(RoleManager.getRoleManager().ADMIN_ROLE.getRoleType(), TestHelper.getTestProjectName(), "JoeSmith", RoleManager.getRoleManager().PROJECT_ADMIN_ROLE)) {
                                testResult = true;
                            }
                        }
                    }
                }
            }
        }

        if (testResult == false) {
            fail("Failed isUserInRole()");
        }
    }

}
