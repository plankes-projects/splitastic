package com.epicnerf.hibernate.dao;

import com.epicnerf.hibernate.model.Device;
import com.epicnerf.hibernate.model.Notification;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class NotificationDao {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DeviceRepository deviceRepository;

    @Transactional
    public List<Notification> getAndDeleteNotifications(String deviceIdentifier) {
        String query = "SELECT notification.* FROM device ";
        query += " join notification on notification.device_id = device.id ";
        query += " where device.device_identifier = :deviceIdentifier ";

        //noinspection unchecked
        List<Notification> result = (List<Notification>) entityManager
                .createNativeQuery(query, Notification.class)
                .setParameter("deviceIdentifier", deviceIdentifier)
                .getResultList();

        if (!result.isEmpty()) {
            List<Long> ids = result.stream()
                    .map(Notification::getId)
                    .collect(Collectors.toList());

            entityManager
                    .createNativeQuery("delete from notification where id in (:ids)")
                    .setParameter("ids", ids)
                    .executeUpdate();
        }
        return result;
    }

    @Transactional
    public void removeUserMappingAndDeletePendingNotifications(String deviceIdentifier) {
        entityManager
                .createNativeQuery("update device set user_id = null where device_identifier = :deviceIdentifier")
                .setParameter("deviceIdentifier", deviceIdentifier)
                .executeUpdate();

        String query = "delete notification from device ";
        query += " join notification on notification.device_id = device.id ";
        query += " where device_identifier = :deviceIdentifier ";
        entityManager
                .createNativeQuery(query)
                .setParameter("deviceIdentifier", deviceIdentifier)
                .executeUpdate();
    }

    @Transactional
    public void updateUserMapping(String deviceIdentifier, User user) {
        // 99% of the time mapping is ok, thus it is a little bit faster if we check before.
        if (!isMappingOk(deviceIdentifier, user)) {
            removeUserMappingAndDeletePendingNotifications(deviceIdentifier);
            addUserMapping(deviceIdentifier, user);
        }
    }

    public boolean isMappingOk(String deviceIdentifier, User user) {
        String query = "SELECT * FROM device where user_id = :userId and device_identifier = :deviceIdentifier";

        BigInteger result = (BigInteger) entityManager
                .createNativeQuery("SELECT EXISTS(" + query + ")")
                .setParameter("userId", user.getId())
                .setParameter("deviceIdentifier", deviceIdentifier)
                .getSingleResult();

        return result.intValue() == 1;
    }

    private void addUserMapping(String deviceIdentifier, User user) {
        Device device = null;
        try {
            String table = Device.class.getSimpleName();
            device = entityManager
                    .createQuery("SELECT u FROM " + table + " u where device_identifier = :deviceIdentifier", Device.class)
                    .setParameter("deviceIdentifier", deviceIdentifier)
                    .getSingleResult();
        } catch (NoResultException e) {
            device = new Device();
            device.setDeviceIdentifier(deviceIdentifier);
        }

        device.setUser(user);
        deviceRepository.save(device);
    }
}