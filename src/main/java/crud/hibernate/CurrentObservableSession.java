/* Copyright 2015 Rick Warren
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package crud.hibernate;

import org.hibernate.Session;


public class CurrentObservableSession extends ObservableSession {

    /**
     * Don't close the underlying Hibernate {@link Session}: we don't
     * own its life cycle. This method is deprecated to emphasize that
     * callers with static knowledge of this type should not call it.
     */
    @Deprecated
    @Override
    public void close() {
        // do nothing
    }

    /*package*/ CurrentObservableSession(final Session session) {
        super(session);
    }

}
