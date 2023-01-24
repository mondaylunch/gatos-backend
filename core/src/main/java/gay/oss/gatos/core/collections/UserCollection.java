package gay.oss.gatos.core.collections;

import gay.oss.gatos.core.models.User;

public class UserCollection extends BaseCollection<User> {
    public UserCollection() {
        super("users", User.class);
    }
}