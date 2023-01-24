package gay.oss.gatos.api.repository;

import org.springframework.stereotype.Repository;
import gay.oss.gatos.core.models.User;

@Repository
public class LoginRepository {
    //TODO: make it work after insert is done with the db.

    // just a demo
    public User validateUser(String username, String password) {
        return User.objects.validateUser(username, password);
    }

}
