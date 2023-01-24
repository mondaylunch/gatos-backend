package gay.oss.gatos.api.repository;

import org.springframework.stereotype.Repository;
import gay.oss.gatos.core.models.User;

@Repository
public class SignUpRepository {

    /**
     * add a new user to the db
     *
     * @param User user
     */
    public User addUser(User user) {
        User.objects.insert(user);
        return user;
    }

}
