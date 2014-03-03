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
package com.qumasoft.server;

import com.qumasoft.TestHelper;
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

    /**
     * Test of writeRoleStore method, of class com.qumasoft.server.RoleManager.
     */
    @Test
    public void testWriteRoleStore() {
        System.out.println("testWriteRoleStore");

        // Add your test code below by replacing the default call to fail.
        RoleManager.getRoleManager().writeRoleStore();
    }

    /**
     * Test of isUserInRole method, of class com.qumasoft.server.RoleManager.
     */
    @Test
    public void testAddUserRole() {
        System.out.println("testIsUserInRole");

        boolean testResult = false;
        com.qumasoft.qvcslib.RoleType adminRole = RoleManager.ADMIN_ROLE;
        String adminRoleType = adminRole.toString();
        RolePrivilegesManager.getInstance().initialize();

        if (RoleManager.getRoleManager().addUserRole(adminRoleType, TestHelper.getTestProjectName(), "JoeSmith", RoleManager.PROJECT_ADMIN_ROLE)) {
            if (RolePrivilegesManager.getInstance().isUserPrivileged(TestHelper.getTestProjectName(), "JoeSmith", RolePrivilegesManager.ADD_DIRECTORY)) {
                if (RoleManager.getRoleManager().addUserRole("JoeSmith", TestHelper.getTestProjectName(), "JaneSmith", RoleManager.READER_ROLE)) {
                    if (RolePrivilegesManager.getInstance().isUserPrivileged(TestHelper.getTestProjectName(), "JaneSmith", RolePrivilegesManager.GET)) {
                        if (RoleManager.getRoleManager().addUserRole("JoeSmith", TestHelper.getTestProjectName(), "JeffSmith", RoleManager.WRITER_ROLE)) {
                            if (RolePrivilegesManager.getInstance().isUserPrivileged(TestHelper.getTestProjectName(), "JeffSmith", RolePrivilegesManager.CHECK_IN)) {
                                if (RoleManager.getRoleManager().addUserRole(adminRoleType, TestHelper.getTestProjectName(), "JoeAdmin", RoleManager.PROJECT_ADMIN_ROLE)) {
                                    if (RoleManager.getRoleManager().addUserRole("JoeAdmin", TestHelper.getTestProjectName(), "RalphSmith", RoleManager.READER_ROLE)) {
                                        if (RoleManager.getRoleManager().addUserRole("JoeAdmin", TestHelper.getTestProjectName(), "RalphSmith", RoleManager.WRITER_ROLE)) {
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
    @Test
    public void testRemoveUserRole() {
        boolean testResult = false;

        System.out.println("testRemoveUserRole");

        testAddUserRole(); // populate the store
        if (RoleManager.getRoleManager().removeUserRole("JoeAdmin", TestHelper.getTestProjectName(), "RalphSmith", RoleManagerInterface.WRITER_ROLE)) {
            if (RoleManager.getRoleManager().removeUserRole("JoeAdmin", TestHelper.getTestProjectName(), "RalphSmith", RoleManagerInterface.READER_ROLE)) {
                if (RoleManager.getRoleManager().removeUserRole("JoeAdmin", TestHelper.getTestProjectName(), "JoeAdmin", RoleManagerInterface.PROJECT_ADMIN_ROLE)) {
                    if (RoleManager.getRoleManager().removeUserRole(RoleManagerInterface.ADMIN_ROLE.getRoleType(), TestHelper.getTestProjectName(), "JeffSmith", RoleManagerInterface.WRITER_ROLE)) {
                        if (RoleManager.getRoleManager().removeUserRole(RoleManagerInterface.ADMIN_ROLE.getRoleType(), TestHelper.getTestProjectName(), "JaneSmith", RoleManagerInterface.READER_ROLE)) {
                            if (RoleManager.getRoleManager().removeUserRole(RoleManagerInterface.ADMIN_ROLE.getRoleType(), TestHelper.getTestProjectName(), "JoeSmith", RoleManagerInterface.PROJECT_ADMIN_ROLE)) {
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

    @Test
    public void testAddRolesForDevelopment() {
        System.out.println("testAddRolesForDevelopment");

        RoleManager.getRoleManager().addUserRole("ADMIN", "Remote C++ Code", "JimVoris", RoleManager.PROJECT_ADMIN_ROLE);
        RoleManager.getRoleManager().addUserRole("ADMIN", "Remote C++ Code", "ADMIN", RoleManager.PROJECT_ADMIN_ROLE);
        RoleManager.getRoleManager().addUserRole("ADMIN", "Remote Crypto Code", "JimVoris", RoleManager.PROJECT_ADMIN_ROLE);
        RoleManager.getRoleManager().addUserRole("ADMIN", "Remote Crypto Code", "ADMIN", RoleManager.PROJECT_ADMIN_ROLE);
        RoleManager.getRoleManager().addUserRole("ADMIN", "Remote Secure Java Project", "JimVoris", RoleManager.PROJECT_ADMIN_ROLE);
        RoleManager.getRoleManager().addUserRole("ADMIN", "Remote Secure Java Project", "ADMIN", RoleManager.PROJECT_ADMIN_ROLE);
        RoleManager.getRoleManager().addUserRole("ADMIN", "Remote Test Project", "JimVoris", RoleManager.PROJECT_ADMIN_ROLE);

        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Secure Java Project", "JimVoris", RoleManager.READER_ROLE);
        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Secure Java Project", "JimVoris", RoleManager.WRITER_ROLE);
        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Secure Java Project", "BrianVoris", RoleManager.READER_ROLE);
        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Secure Java Project", "BrianVoris", RoleManager.WRITER_ROLE);
        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Secure Java Project", "BruceVoris", RoleManager.READER_ROLE);

        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote C++ Code", "JimVoris", RoleManager.READER_ROLE);
        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote C++ Code", "JimVoris", RoleManager.WRITER_ROLE);
        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote C++ Code", "BruceVoris", RoleManager.READER_ROLE);

        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Crypto Code", "JimVoris", RoleManager.READER_ROLE);
        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Crypto Code", "JimVoris", RoleManager.WRITER_ROLE);
        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Crypto Code", "BruceVoris", RoleManager.READER_ROLE);

        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Test Project", "JimVoris", RoleManager.READER_ROLE);
        RoleManager.getRoleManager().addUserRole("JimVoris", "Remote Test Project", "JimVoris", RoleManager.WRITER_ROLE);

        RoleManager.getRoleManager().addUserRole(TestHelper.USER_NAME, TestHelper.getTestProjectName(), TestHelper.USER_NAME, RoleManager.DEVELOPER_ROLE);
    }
}
