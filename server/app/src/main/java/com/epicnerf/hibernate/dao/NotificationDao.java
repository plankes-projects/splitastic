package com.epicnerf.hibernate.dao;

import com.epicnerf.api.NotificationManager;
import com.epicnerf.hibernate.model.Device;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.DeviceRepository;
import com.epicnerf.service.PushNotificationService;
import org.apache.commons.lang3.StringUtils;
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
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DeviceRepository deviceRepository;
    @Autowired
    private NotificationManager notificationManager;
    @Autowired
    private PushNotificationService pushNotificationService;

    public void removeUserMapping(User user) {
        entityManager
                .createNativeQuery("delete from device where user_id = :uid")
                .setParameter("uid", user.getId())
                .executeUpdate();
    }

    public void updateUserMapping(String deviceIdentifier, User user) {
        // 99% of the time mapping is ok, thus it is a little bit faster if we check before.
        if (!isMappingOk(deviceIdentifier, user)) {
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
        Device device;
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

    public void insertForAllUsersOfGroup(@NonNull String title, @NonNull String body, @NonNull GroupObject group) {
        insertForAllUsersOfGroup(title, body, group, new ArrayList<>());
    }

    public void insertForAllUsersOfGroup(@NonNull String title, @NonNull String body, @NonNull GroupObject group, @NonNull List<User> exceptions) {
        for (User userInGroup : group.getUsers()) {
            if (!isException(userInGroup, exceptions)) {
                this.insertForAllDevicesOfUser(title, body, userInGroup);
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

    public void insertForAllDevicesOfUser(@NonNull String title, @NonNull String body, @NonNull User user, @NonNull List<Device> exceptions) {
        List<String> keys = getAllDevicesOfUser(user)
                .stream()
                .filter(device -> !isException(device, exceptions))
                .map(Device::getDeviceIdentifier)
                .collect(Collectors.toList());

        title = StringUtils.abbreviate(title, 65);
        body = StringUtils.abbreviate(body, 240);
        pushNotificationService.sendPushNotification(keys, title, body);
    }

    public void insertForAllDevicesOfUser(@NonNull String title, @NonNull String body, @NonNull User user) {
        insertForAllDevicesOfUser(title, body, user, new ArrayList<>());
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