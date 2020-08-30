package com.epicnerf.api;

import com.epicnerf.hibernate.dao.NotificationDao;
import com.epicnerf.hibernate.model.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Collections;

@Service
public class NotificationManager {

    private final DecimalFormat decimalFormat;

    @Autowired
    private NotificationDao notificationDao;

    public NotificationManager() {
        decimalFormat = new DecimalFormat();
        decimalFormat.setMaximumFractionDigits(2);
    }

    public void onFinanceEntryDelete(User user, FinanceEntry entry) {
        String title = "Expense deleted";
        for (User userInGroup : entry.getGroup().getUsers()) {
            if (!user.getId().equals(userInGroup.getId())) {
                float moneyOwned = getMoneyOwned(userInGroup, entry);
                if (moneyOwned > 0) {
                    String body = "Group: " + entry.getGroup().getName() +
                            "\nTitle: " + entry.getTitle() +
                            "\nYou own " + decimalFormat.format(moneyOwned) + "€ less to " + entry.getSpentFrom().getName();
                    notificationDao.insertForAllDevicesOfUser(createNotification(title, body), userInGroup);
                }
            }
        }
    }

    public void onFinanceEntryAdded(FinanceEntry entry) {
        String title = "Expense added";
        for (User userInGroup : entry.getGroup().getUsers()) {
            if (!entry.getSpentFrom().getId().equals(userInGroup.getId())) {
                float moneyOwned = getMoneyOwned(userInGroup, entry);
                if (moneyOwned > 0) {
                    String body = "Group: " + entry.getGroup().getName() +
                            "\nTitle: " + entry.getTitle() +
                            "\nYou own " + decimalFormat.format(moneyOwned) + "€ to " + entry.getSpentFrom().getName();
                    notificationDao.insertForAllDevicesOfUser(createNotification(title, body), userInGroup);
                }
            }
        }

        if (!entry.getSpentFrom().getId().equals(entry.getCreatedBy().getId())) {
            float total = (float) entry.getEntries()
                    .stream()
                    .mapToDouble(e -> e.getAmount().doubleValue())
                    .sum();
            title = "Your expense was added";
            String body = "Group: " + entry.getGroup().getName() +
                    "\nTitle: " + entry.getTitle() +
                    "\nWho added: " + entry.getCreatedBy().getName() +
                    "\nTotal: " + decimalFormat.format(total) + "€";
            notificationDao.insertForAllDevicesOfUser(createNotification(title, body), entry.getSpentFrom());
        }
    }

    private Notification createNotification(String title, String body) {
        Notification notification = new Notification();
        notification.setTitle(StringUtils.abbreviate(title, Notification.MAX_TITLE_LENGTH));
        notification.setBody(StringUtils.abbreviate(body, Notification.MAX_BODY_LENGTH));
        return notification;
    }

    private float getMoneyOwned(User user, FinanceEntry entry) {
        for (FinanceEntryEntry subEntry : entry.getEntries()) {
            if (user.getId().equals(subEntry.getSpentFor().getId())) {
                return subEntry.getAmount();
            }
        }
        return 0;
    }

    public void onChoreDeleted(User user, Chore chore) {
        String title = "Chore category deleted";
        String body = "Group: " + chore.getGroup().getName() +
                "\nChore: " + chore.getTitle();

        notificationDao.insertForAllUsersOfGroup(createNotification(title, body), chore.getGroup(), Collections.singletonList(user));
    }

    public void onChoreAdded(User user, Chore chore) {
        String title = "Chore category added";
        String body = "Group: " + chore.getGroup().getName() +
                "\nChore: " + chore.getTitle();

        notificationDao.insertForAllUsersOfGroup(createNotification(title, body), chore.getGroup(), Collections.singletonList(user));
    }

    public void onChoreEntryDeleted(User user, Chore chore) {
        String title = "Chore deleted";
        String body = "Group: " + chore.getGroup().getName() +
                "\nChore: " + chore.getTitle() +
                "\nUser: " + user.getName();

        notificationDao.insertForAllUsersOfGroup(createNotification(title, body), chore.getGroup(), Collections.singletonList(user));
    }

    public void onChoreEntryAdded(User user, Chore chore) {
        String title = "Chore finished";
        String body = "Group: " + chore.getGroup().getName() +
                "\nChore: " + chore.getTitle() +
                "\nUser: " + user.getName();

        notificationDao.insertForAllUsersOfGroup(createNotification(title, body), chore.getGroup(), Collections.singletonList(user));
    }

    public void onNewDeviceLink(User user, Device device) {
        String title = "New device registered";
        String body = "";
        notificationDao.insertForAllDevicesOfUser(createNotification(title, body), user, Collections.singletonList(device));
    }

    public void onGroupDelete(User user, GroupObject group) {
        String title = "Group deleted";
        String body = "Group: " + group.getName();

        notificationDao.insertForAllUsersOfGroup(createNotification(title, body), group, Collections.singletonList(user));
    }

    public void onGroupInviteSent(GroupInvite groupInvite) {
        String title = "New group invite";
        String body = "Group: " + groupInvite.getGroup().getName();
        notificationDao.insertForAllDevicesOfUser(createNotification(title, body), groupInvite.getInvitedUser());
    }

    public void onGroupInviteDeleted(User user, GroupInvite groupInvite) {
        if (inviteWasRejected(user, groupInvite)) {
            String title = "Group invite rejected";
            String body = "Group: " + groupInvite.getGroup().getName() +
                    "\nEmail: " + groupInvite.getInvitedUser().getEmail();
            notificationDao.insertForAllDevicesOfUser(createNotification(title, body), groupInvite.getGroup().getOwner());
        } else {
            String title = "Group invite withdrawn";
            String body = "Group: " + groupInvite.getGroup().getName();
            notificationDao.insertForAllDevicesOfUser(createNotification(title, body), groupInvite.getInvitedUser());
        }
    }

    private boolean inviteWasRejected(User user, GroupInvite groupInvite) {
        return user.getId().equals(groupInvite.getInvitedUser().getId());
    }

    public void onGroupInviteAccepted(GroupInvite groupInvite) {
        String title = "User joined group";
        String body = "Group: " + groupInvite.getGroup().getName() +
                "\nUser: " + groupInvite.getInvitedUser().getName();
        notificationDao.insertForAllUsersOfGroup(createNotification(title, body), groupInvite.getGroup(), Collections.singletonList(groupInvite.getInvitedUser()));
    }

    public void onGroupLeft(User user, GroupObject group, User userLeft) {
        String title = user.getId().equals(userLeft.getId()) ? "User left group" : "User removed from group";
        String body = "Group: " + group.getName() +
                "\nUser: " + userLeft.getName();

        User userToIgnore = user.getId().equals(userLeft.getId()) ? userLeft : group.getOwner();
        notificationDao.insertForAllUsersOfGroup(createNotification(title, body), group, Collections.singletonList(userToIgnore));
    }

    public void onVirtualUserJoined(GroupObject group, User virtualUser) {
        String title = "User joined group";
        String body = "Group: " + group.getName() +
                "\nUser: " + virtualUser.getName();

        notificationDao.insertForAllUsersOfGroup(createNotification(title, body), group);
    }
}
