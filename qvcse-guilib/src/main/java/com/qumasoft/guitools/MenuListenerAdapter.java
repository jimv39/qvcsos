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
package com.qumasoft.guitools;

/**
 * Menu listener adapter. Supply no-op implementations for methods required by the MenuListener interface so we only need to implement what we need.
 * @author Jim Voris
 */
public class MenuListenerAdapter implements javax.swing.event.MenuListener {

    /**
     * Creates a new instance of MenuListenerAdapter.
     */
    public MenuListenerAdapter() {
    }

    @Override
    public void menuSelected(final javax.swing.event.MenuEvent e) {
    }

    @Override
    public void menuDeselected(final javax.swing.event.MenuEvent e) {
    }

    @Override
    public void menuCanceled(final javax.swing.event.MenuEvent e) {
    }
}
