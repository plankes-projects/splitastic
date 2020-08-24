package com.epicnerf.hibernate.dao;

import com.epicnerf.api.ApiSupport;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.List;

@Component
public class UserDao {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiSupport apiSupport;

    public List<User> paginateUsers(int limit, Integer lastId) {
        String table = User.class.getSimpleName();
        if (lastId == null) {
            lastId = Integer.MAX_VALUE;
        }
        return entityManager
                .createQuery("SELECT u FROM " + table + " u where id < :id ORDER BY u.id desc", User.class)
                .setParameter("id", lastId)
                .setMaxResults(limit)
                .getResultList();
    }

    public User getOrCreateUserWithEmail(String email) {
        apiSupport.validateEmail(email);

        String table = User.class.getSimpleName();

        try {
            return entityManager
                    .createQuery("SELECT u FROM " + table + " u where email = :email", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            User user = new User();
            user.setEmail(email);
            user.setName(getNameFromEmail(email));
            user.setImage(apiSupport.defaultUserImage());
            userRepository.save(user);
            return user;
        }
    }

    public String getNameFromEmail(String email) {
        int index = email.indexOf('@');
        if (index > 0) {
            return email.substring(0, index);
        }
        return email;
    }

    public User getUserWithToken(String token) {
        String table = User.class.getSimpleName();
        return entityManager
                .createQuery("SELECT u FROM " + table + " u where token = :token", User.class)
                .setParameter("token", token)
                .getSingleResult();
    }
}
