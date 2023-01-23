package gay.oss.gatos.api.repository;

import org.springframework.stereotype.Repository;

import gay.oss.gatos.api.model.UserModel;

@Repository
public class LoginRepository {
    //TODO: make it work after insert is done with the db.

    // just a demo
    public String findByUsername(String username) {
        if(username.equals("ff")){
            return "you found me!!!";
        }
        return "you didn't find me :(";
    }
}
