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
import com.qumasoft.qvcslib.Utility;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Authentication store test.
 * @author Jim Voris
 */
public class AuthenticationStoreTest {

    static final String JIMSPASSWORD = TestHelper.PASSWORD;
    static final String BRIANSPASSWORD = "BriansPassword";
    static final String BRUCESPASSWORD = "BrucesPassword";

    /**
     * Default ctor.
     */
    public AuthenticationStoreTest() {
    }

    /**
     * Run once to set things up for all these tests.
     *
     * @throws java.lang.Exception if something goes wrong.
     */
    @BeforeClass
    public static void setUpClass() throws Exception {
        TestHelper.deleteAuthenticationStore();
        AuthenticationManager.getAuthenticationManager().initialize();
    }

    /**
     * Run once after all tests are finished. Used for cleanup.
     *
     * @throws java.lang.Exception if something goes wrong.
     */
    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Run before <i>each</i> test.
     */
    @Before
    public void setUp() {
    }

    /**
     * Run after each test.
     */
    @After
    public void tearDown() {
    }

    /**
     * Run the tests in order.
     */
    @Test
    public void testAuthenticationStore() {
        testAddUser();
        testRemoveUser();
        testUpdateUser();
        testListUsers();
    }
    /**
     * Test of addUser method, of class com.qumasoft.server.AuthenticationStore.
     */
    public void testAddUser() {
        System.out.println("testAddUser");

        byte[] jimsHashedPassword = Utility.getInstance().hashPassword(JIMSPASSWORD);
        byte[] briansHashedPassword = Utility.getInstance().hashPassword(BRIANSPASSWORD);
        byte[] brucesHashedPassword = Utility.getInstance().hashPassword(BRUCESPASSWORD);
        byte[] guestHashedPassword = Utility.getInstance().hashPassword("guest");

        // Add the guest user that we allow for the JBoss project.
        AuthenticationManager.getAuthenticationManager().addUser("ADMIN", "guest", guestHashedPassword);

        AuthenticationManager.getAuthenticationManager().addUser("ADMIN", "JimVoris", jimsHashedPassword);

        AuthenticationManager.getAuthenticationManager().addUser("ADMIN", "BruceVoris", brucesHashedPassword);

        // Add your test code below by replacing the default call to fail.
        if (AuthenticationManager.getAuthenticationManager().addUser("ADMIN", "JunkUser", jimsHashedPassword)) {
            // If we added the user successfully, then we should be able to authenticate them.
            if (AuthenticationManager.getAuthenticationManager().authenticateUser("JimVoris", jimsHashedPassword)) {
                // This authentication request should fail
                byte[] bogusPasswordArray = Utility.getInstance().hashPassword("bogusPassword");
                if (AuthenticationManager.getAuthenticationManager().authenticateUser("JimVoris", bogusPasswordArray) == false) {
                    // Add the same user.  This should fail.
                    if (AuthenticationManager.getAuthenticationManager().addUser("ADMIN", "JimVoris", jimsHashedPassword) == false) {
                        // Authenticate brian... if he's there, otherwise add him.
                        if (!AuthenticationManager.getAuthenticationManager().authenticateUser("BrianVoris", briansHashedPassword)) {
                            if (AuthenticationManager.getAuthenticationManager().addUser("ADMIN", "BrianVoris", briansHashedPassword)) {
                                System.out.println("testAddUser done");
                            } else {
                                fail("AddUser failed to add 2nd user");
                            }
                        }
                    } else {
                        fail("AddUser failed to detect a duplicate user");
                    }
                } else {
                    fail("AddUser failed to detect a bad password");
                }
            } else {
                fail("AddUser failed to authenticate JimVoris");
            }
        } else {
            fail("AddUser failed to add JimVoris as first user");
        }

    }

    /**
     * Test of removeUser method, of class com.qumasoft.server.AuthenticationStore.
     */
    public void testRemoveUser() {
        System.out.println("testRemoveUser");

        if (!AuthenticationManager.getAuthenticationManager().removeUser("ADMIN", "JunkUser")) {
            fail("Failed to remove JunkUser");
        }
    }

    /**
     * Test of updateUser method, of class com.qumasoft.server.AuthenticationStore.
     */
    public void testUpdateUser() {
        System.out.println("testUpdateUser");

        byte[] oldGuestHashedPassword = Utility.getInstance().hashPassword("guest");
        byte[] newGuestHashedPassword = Utility.getInstance().hashPassword("newguest");

        // ADMIN user can change anybody's password...
        if (AuthenticationManager.getAuthenticationManager().updateUser("ADMIN", "guest", oldGuestHashedPassword, newGuestHashedPassword)) {
            // A user can only change their own password
            if (AuthenticationManager.getAuthenticationManager().updateUser("guest", "guest", newGuestHashedPassword, oldGuestHashedPassword)) {
                // A user cannot change their password unless they supply the correct password
                if (!AuthenticationManager.getAuthenticationManager().updateUser("guest", "guest", newGuestHashedPassword, oldGuestHashedPassword)) {
                    // A user cannot change someone else's password
                    if (!AuthenticationManager.getAuthenticationManager().updateUser("guest", "JimVoris", newGuestHashedPassword, oldGuestHashedPassword)) {
                    } else {
                        fail("Allowed guest to change JimVoris' password!!");
                    }
                } else {
                    fail("Allowed guest to change password, but supplied the incorrect password!!");
                }
            } else {
                fail("guest failed to change guest's password");
            }
        } else {
            fail("Failed to change guest password to new password.");
        }
    }

    /**
     * Test of listUsers method, of class com.qumasoft.server.AuthenticationStore.
     */
    public void testListUsers() {
        System.out.println("testListUsers");

        // List users...
        String[] userList = AuthenticationManager.getAuthenticationManager().listUsers();
        if (userList.length == 0) {
            fail("No users found");
        }
    }
}
