package com.epicnerf.hibernate.dao;

import com.epicnerf.hibernate.model.LoginToken;
import com.epicnerf.hibernate.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Component
public class LoginTokenDao {

    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void deleteLoginToken(User user) {
        String table = LoginToken.class.getSimpleName();
        entityManager
                .createQuery("delete from " + table + " where user_id = :uid")
                .setParameter("uid", user.getId())
                .executeUpdate();
    }

    @Transactional(dontRollbackOn = RuntimeException.class)
    public LoginToken getLoginTokenWithProof(String proof) {
        deleteOldLoginTokens();
        String table = LoginToken.class.getSimpleName();
        return entityManager
                .createQuery("SELECT u FROM " + table + " u where secret = :secret", LoginToken.class)
                .setParameter("secret", proof)
                .getSingleResult();
    }

    @Transactional(dontRollbackOn = RuntimeException.class)
    public LoginToken getLoginTokenWithToken(String token) {
        deleteOldLoginTokens();
        String table = LoginToken.class.getSimpleName();
        return entityManager
                .createQuery("SELECT u FROM " + table + " u where token = :token", LoginToken.class)
                .setParameter("token", token)
                .getSingleResult();
    }

    private void deleteOldLoginTokens() {
        entityManager
            .createNativeQuery("DELETE FROM login_token where create_date < now() - interval 14 DAY")
            .executeUpdate();
    }
}
