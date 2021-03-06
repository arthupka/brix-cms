/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package brix.rmiserver.web.admin;

import org.apache.wicket.injection.web.InjectorHolder;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.spring.injection.annot.SpringBean;

import brix.rmiserver.User;
import brix.rmiserver.UserService;

public class UserModel extends LoadableDetachableModel<User>
{
    private static final long serialVersionUID = 1L;

    @SpringBean
    private UserService users;

    private final Long id;

    public UserModel(User user)
    {
        super(user);
        InjectorHolder.getInjector().inject(this);
        this.id = user.getId();
    }

    @Override
    protected User load()
    {
        return users.load(id);
    }


}
