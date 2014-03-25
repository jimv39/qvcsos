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
package com.qumasoft.qvcslib.logfileaction;

import com.qumasoft.qvcslib.commandargs.CheckOutCommandArgs;

/**
 * Checkout revision action.
 * @author Jim Voris
 */
public class CheckOut extends ActionType {

    private final CheckOutCommandArgs commandArgs;

    /**
     * Creates a new instance of LogfileActionCheckOut.
     * @param args the checkout command args.
     */
    public CheckOut(CheckOutCommandArgs args) {
        super("Check Out", ActionType.CHECKOUT);
        commandArgs = args;
    }

    /**
     * Get the checkout command args.
     * @return the checkout command args.
     */
    public CheckOutCommandArgs getCommandArgs() {
        return commandArgs;
    }
}
