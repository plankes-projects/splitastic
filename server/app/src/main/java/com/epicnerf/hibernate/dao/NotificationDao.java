package com.epicnerf.hibernate.dao;

import com.epicnerf.api.NotificationManager;
import com.epicnerf.hibernate.model.Device;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.Notification;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.DeviceRepository;
import com.epicnerf.hibernate.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Transactional
public class NotificationDao {

    private final int MAX_NOTIFICATIONS = 10;

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationManager notificationManager;

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

        //this shall be called before the save, such we only send notification to current devices
        notificationManager.onNewDeviceLink(user, device);
        deviceRepository.save(device);
    }

    public void insertForAllUsersOfGroup(@NonNull Notification notification, @NonNull GroupObject group) {
        insertForAllUsersOfGroup(notification, group, new ArrayList<>());
    }

    public void insertForAllUsersOfGroup(@NonNull Notification notification, @NonNull GroupObject group, @NonNull List<User> exceptions) {
        for (User userInGroup : group.getUsers()) {
            if (!isException(userInGroup, exceptions)) {
                this.insertForAllDevicesOfUser(notification, userInGroup);
            }
        }
    }

    private boolean isException(@NonNull User user, @NonNull List<User> exceptions) {
        for (User exception : exceptions) {
            if (exception.getId().equals(user.getId())) {
                return true;
            }
        }
        return false;
    }

    public List<Device> getAllDevicesOfUser(@NonNull User user) {
        //noinspection unchecked
        return (List<Device>) entityManager
                .createNativeQuery("select * from device where user_id = :userId", Device.class)
                .setParameter("userId", user.getId())
                .getResultList();
    }

    public void insertForAllDevicesOfUser(@NonNull Notification notification, @NonNull User user, @NonNull List<Device> exceptions) {
        List<Device> allDevices = getAllDevicesOfUser(user);
        for (Device device : allDevices) {
            if (!isException(device, exceptions)) {

                Notification notificationToStore = new Notification();
                notificationToStore.setTitle(notification.getTitle());
                notificationToStore.setBody(notification.getBody());
                notificationToStore.setData(notification.getData());
                notificationToStore.setDevice(device);

                notificationRepository.save(notificationToStore);
                reduceNotifications(device);
            }
        }
    }

    private void reduceNotifications(Device device) {
        String query = "delete from notification where id in (\n" +
                "\tSELECT id FROM (\n" +
                "\t\tselect notification.id from notification \n" +
                "\t\tjoin device on notification.device_id = device.id\n" +
                "\t\twhere device.device_identifier = :deviceIdentifier\n" +
                "\t\torder by notification.id desc limit 1000 offset :offset\n" +
                "    ) a\n" +
                ");";

        entityManager
                .createNativeQuery(query)
                .setParameter("deviceIdentifier", device.getDeviceIdentifier())
                .setParameter("offset", MAX_NOTIFICATIONS)
                .executeUpdate();
    }

    public void insertForAllDevicesOfUser(@NonNull Notification notification, @NonNull User user) {
        insertForAllDevicesOfUser(notification, user, new ArrayList<>());
    }

    private boolean isException(@NonNull Device device, @NonNull List<Device> exceptions) {
        for (Device exception : exceptions) {
            if (exception.getId().equals(device.getId())) {
                return true;
            }
        }
        return false;
    }
}