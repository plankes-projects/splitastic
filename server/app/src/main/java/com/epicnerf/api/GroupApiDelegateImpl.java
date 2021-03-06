package com.epicnerf.api;

import com.epicnerf.hibernate.MapToHibernateModel;
import com.epicnerf.hibernate.MapToOpenApiModel;
import com.epicnerf.hibernate.dao.*;
import com.epicnerf.hibernate.model.GroupInvite;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.ImageData;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.FinanceEntryRepository;
import com.epicnerf.hibernate.repository.GroupInviteRepository;
import com.epicnerf.hibernate.repository.GroupObjectRepository;
import com.epicnerf.hibernate.repository.UserRepository;
import com.epicnerf.model.*;
import com.epicnerf.service.NotificationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class GroupApiDelegateImpl implements GroupApiDelegate {

    @Autowired
    private ImageDataDao imageDataDao;

    @Autowired
    private ApiSupport apiSupport;

    @Autowired
    private GroupObjectDao groupObjectDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private GroupInviteDao groupInviteDao;

    @Autowired
    private GroupObjectRepository groupObjectRepository;

    @Autowired
    private GroupInviteRepository groupInviteRepository;

    @Autowired
    private MapToHibernateModel mapper;

    @Autowired
    private FinanceEntryRepository financeEntryRepository;

    @Autowired
    private FinanceEntryDao financeEntryDao;

    @Autowired
    private MapToOpenApiModel openApiMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationManager notificationManager;
    @Autowired
    private ChoreDao choreDao;

    public ResponseEntity<List<Group>> groupGet(Integer num, Integer lastId) {
        User user = apiSupport.getCurrentUser();
        List<Group> result = openApiMapper.map(groupObjectDao.paginateGroups(user, num, lastId), user);
        result.forEach(e -> e.setBalance(BigDecimal.valueOf(financeEntryDao.getBalance(e.getId(), user.getId()))));
        return ResponseEntity.ok(result);
    }

    public ResponseEntity<Void> groupGroupIdDelete(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);

        if (group.isPresent() && group.get().getOwner().getId().equals(user.getId())) {
            groupInviteDao.deleteAllGroupInvites(group.get());
            financeEntryDao.deleteAllFinanceEntries(group.get());
            groupObjectRepository.delete(group.get());

            notificationManager.onGroupDelete(user, group.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new NoResultException();
    }

    public ResponseEntity<List<FinanceEntry>> groupGroupIdFinanceGet(Integer groupId, Integer num, Integer lastId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);
        if (group.isPresent()) {
            apiSupport.validateUserIsInGroup(group.get(), user.getId());
            List<com.epicnerf.hibernate.model.FinanceEntry> entries = financeEntryDao.paginateFinanceEntries(group.get(), num, lastId);
            return ResponseEntity.ok(openApiMapper.mapFinanceList(entries));
        }
        throw new NoResultException();
    }

    public ResponseEntity<FinanceEntry> groupGroupIdFinancePost(Integer groupId, FinanceEntry financeEntry) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);
        if (group.isPresent()) {
            apiSupport.validateUserIsInGroup(group.get(), financeEntry.getSpentFrom());
            for (FinanceEntryEntry entry : financeEntry.getSpent()) {
                apiSupport.validateUserIsInGroup(group.get(), entry.getSpentFor());
            }

            com.epicnerf.hibernate.model.FinanceEntry f = mapper.mapFinance(financeEntry, false);
            f.setCreatedBy(user);
            f.setGroup(group.get());
            financeEntryRepository.save(f);
            notificationManager.onFinanceEntryAdded(f);
            groupObjectDao.updateActivity(group.get());
            return ResponseEntity.ok(openApiMapper.map(f));
        }
        throw new NoResultException();
    }

    public ResponseEntity<Void> groupGroupIdAddVirtualUserPost(Integer groupId, AddVirtualUserData addVirtualUserData) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);
        if (group.isPresent() && group.get().getOwner().getId().equals(user.getId())) {
            User newUser = createVirtualUser(addVirtualUserData.getName());
            userRepository.save(newUser);
            group.get().getUsers().add(newUser);
            groupObjectRepository.save(group.get());

            notificationManager.onVirtualUserJoined(group.get(), newUser);

            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new NoResultException();
    }

    private User createVirtualUser(String name) {
        User newUser = new User();
        newUser.setEmail(UUID.randomUUID().toString());
        newUser.setName(name);
        newUser.setImage(apiSupport.defaultUserImage());
        newUser.setVirtualUser(true);
        return newUser;
    }

    public ResponseEntity<GroupBalanceData> groupGroupIdBalanceGet(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);
        if (group.isPresent()) {
            GroupBalanceData result = new GroupBalanceData();
            apiSupport.validateUserIsInGroup(group.get(), user.getId());
            for (User userInGroup : group.get().getUsers()) {
                Double balance = financeEntryDao.getBalance(groupId, userInGroup.getId());
                GroupBalanceDataEntry entry = new GroupBalanceDataEntry()
                        .balance(BigDecimal.valueOf(balance))
                        .userId(userInGroup.getId());
                result.addUserBalancesItem(entry);
            }

            return ResponseEntity.ok(result);
        }

        throw new NoResultException();
    }

    public ResponseEntity<Group> groupGroupIdGet(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        GroupObject o = groupObjectDao.getViewableGroup(user, groupId);
        Group g = openApiMapper.map(o, user);
        return ResponseEntity.ok(g);
    }

    public ResponseEntity<Void> groupGroupIdJoinPut(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> g = groupObjectRepository.findById(groupId);
        if (g.isPresent()) {
            if (!userIsInGroup(g.get(), user.getId())) {
                g.get().getUsers().add(user);
                groupObjectRepository.save(g.get());
            }

            GroupInvite invite = groupInviteDao.getGroupInvite(user, g.get());
            groupInviteRepository.delete(invite);
            notificationManager.onGroupInviteAccepted(invite);
            return new ResponseEntity<>(HttpStatus.OK);
        }

        throw new NoResultException();
    }

    public ResponseEntity<Void> groupGroupIdMoveUserDataPut(Integer groupId, MoveUserData moveUserData) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);

        if (group.isPresent() && group.get().getOwner().getId().equals(user.getId())) {
            apiSupport.validateUserIsInGroup(group.get(), moveUserData.getToUserId());
            moveUserData(group.get().getId(), moveUserData);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new NoResultException();
    }

    private void moveUserData(Integer groupId, MoveUserData moveUserData) {
        if (moveUserData.getChores()) {
            choreDao.moveChoresToUser(groupId, moveUserData.getFromUserId(), moveUserData.getToUserId());
        }
        if (moveUserData.getFinance()) {
            financeEntryDao.moveFinanceToUser(groupId, moveUserData.getFromUserId(), moveUserData.getToUserId());
        }
    }

    public boolean userIsInGroup(GroupObject group, Integer userId) {
        for (User user : group.getUsers()) {
            if (user.getId().equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public ResponseEntity<Void> groupPut(Group group) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> g = groupObjectRepository.findById(group.getId());

        if (g.isPresent() && g.get().getOwner().getId().equals(user.getId())) {
            g.get().setName(group.getName());
            g.get().setDescription(group.getDescription());
            if (group.getImage() != null && group.getImage().getUrl() != null) {
                ImageData imageData = imageDataDao.getImageDataFromUpload(group.getImage().getUrl());
                g.get().setImage(imageData);
            }
            groupObjectRepository.save(g.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new NoResultException();
    }

    public ResponseEntity<Void> groupGroupIdUserEmailPut(Integer groupId, String email) {
        email = email.trim();
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> g = groupObjectRepository.findById(groupId);

        if (g.isPresent() && g.get().getOwner().getId().equals(user.getId())) {
            User u = userDao.getOrCreateUserWithEmail(email);

            GroupInvite invite = new GroupInvite();
            invite.setGroup(g.get());
            invite.setInvitedUser(u);
            groupInviteRepository.save(invite);
            notificationManager.onGroupInviteSent(invite);

            return new ResponseEntity<>(HttpStatus.OK);
        }
        throw new NoResultException();
    }

    public ResponseEntity<Void> groupGroupIdUserUserIdDelete(Integer groupId, Integer userId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> g = groupObjectRepository.findById(groupId);
        if (g.isPresent()) {
            boolean isOwner = g.get().getOwner().getId().equals(user.getId());
            boolean isOtherUser = !user.getId().equals(userId);

            if ((isOwner && isOtherUser) || (!isOwner && !isOtherUser)) {
                User userLeft = getUserFromGroup(g.get(), userId);
                g.get().getUsers().removeIf(u -> u.getId().equals(userId));

                if (needGhostUser(groupId, userId)) {
                    User ghost = createNewGhostUserWithName(userLeft.getName());
                    userRepository.save(ghost);

                    g.get().getUsers().add(ghost);
                    groupObjectRepository.save(g.get());

                    MoveUserData moveUserData = new MoveUserData()
                            .chores(true)
                            .finance(true)
                            .fromUserId(userLeft.getId())
                            .toUserId(ghost.getId());
                    moveUserData(groupId, moveUserData);
                } else {
                    groupObjectRepository.save(g.get());
                }
                notificationManager.onGroupLeft(user, g.get(), userLeft);
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }

        throw new NoResultException();
    }

    private boolean needGhostUser(int groupId, int userId) {
        if (choreDao.hasChoresData(groupId, userId)) {
            return true;
        }
        return financeEntryDao.hasFinanceData(groupId, userId);
    }

    private User createNewGhostUserWithName(String name) {
        User user = createVirtualUser(name);
        user.setImage(apiSupport.getGhostUserImage());
        return user;
    }

    @NonNull
    private User getUserFromGroup(GroupObject group, int userId) {
        for (User user : group.getUsers()) {
            if (user.getId().equals(userId)) {
                return user;
            }
        }
        return new User(); //will not be reached
    }

    public ResponseEntity<Integer> groupPost() {
        User user = apiSupport.getCurrentUser();

        GroupObject group = new GroupObject();
        group.setOwner(user);
        group.setName(apiSupport.defaultGroupName());
        group.setImage(apiSupport.defaultGroupImage());
        group.setUsers(Collections.singletonList(user));
        group.setDescription("");

        groupObjectRepository.save(group);

        return ResponseEntity.ok(group.getId());
    }

    public ResponseEntity<List<Invite>> groupInviteGroupIdGet(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> i = groupObjectRepository.findById(groupId);
        if (i.isPresent() && i.get().getOwner().getId().equals(user.getId())) {
            List<GroupInvite> invites = groupInviteDao.getGroupInvites(i.get());
            return ResponseEntity.ok(openApiMapper.mapInvites(invites, user));
        }
        throw new NoResultException();
    }

    public ResponseEntity<Void> groupInviteInviteIdDelete(Integer inviteId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupInvite> i = groupInviteRepository.findById(inviteId);
        if (i.isPresent()) {
            boolean deletable = i.get().getInvitedUser().getId().equals(user.getId());
            deletable = deletable || i.get().getGroup().getOwner().getId().equals(user.getId());
            if (deletable) {
                groupInviteRepository.delete(i.get());
                notificationManager.onGroupInviteDeleted(user, i.get());
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
        throw new NoResultException();
    }

    public ResponseEntity<List<String>> groupGroupIdFinanceTitleSuggestionsGet(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> g = groupObjectRepository.findById(groupId);
        if (g.isPresent() && userIsInGroup(g.get(), user.getId())) {
            return ResponseEntity.ok(financeEntryDao.getSuggestions(groupId, user.getId()));
        }
        throw new NoResultException();
    }
}
