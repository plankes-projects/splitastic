package com.epicnerf.hibernate.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;

@Component
@Transactional
public class FirebaseTokenSupport {
    @Autowired
    private EntityManager entityManager;

    public void removeUserMapping(String firebaseToken) {
        entityManager
                .createNativeQuery("delete from device where device_identifier = :firebaseToken")
                .setParameter("firebaseToken", firebaseToken)
                .executeUpdate();
    }
}