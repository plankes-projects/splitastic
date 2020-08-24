package com.epicnerf.api;

import com.epicnerf.hibernate.MapToHibernateModel;
import com.epicnerf.hibernate.MapToOpenApiModel;
import com.epicnerf.hibernate.dao.*;
import com.epicnerf.hibernate.model.GroupInvite;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.ImageData;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.FinanceRepository;
import com.epicnerf.hibernate.repository.GroupInviteRepository;
import com.epicnerf.hibernate.repository.GroupObjectRepository;
import com.epicnerf.model.FinanceEntry;
import com.epicnerf.model.FinanceEntryEntry;
import com.epicnerf.model.Group;
import com.epicnerf.model.Invite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.NoResultException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


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
    private FinanceRepository financeRepository;

    @Autowired
    private FinanceEntryDao financeEntryDao;

    @Autowired
    private MapToOpenApiModel openApiMapper;

    public ResponseEntity<List<Group>> groupGet(Integer num, Integer lastId) {
        User user = apiSupport.getCurrentUser();
        List<Group> result = openApiMapper.map(groupObjectDao.paginateGroups(user, num, lastId));
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
        Optional<GroupObject> group = groupObjectRepository.findById(groupId);
        if (group.isPresent()) {
            apiSupport.validateUserIsInGroup(group.get(), financeEntry.getSpentFrom());
            for (FinanceEntryEntry entry : financeEntry.getSpent()) {
                apiSupport.validateUserIsInGroup(group.get(), entry.getSpentFor());
            }

            com.epicnerf.hibernate.model.FinanceEntry f = mapper.mapFinance(financeEntry, false);
            f.setGroup(group.get());
            financeRepository.save(f);

            return ResponseEntity.ok(openApiMapper.map(f));
        }
        throw new NoResultException();
    }

    public ResponseEntity<Group> groupGroupIdGet(Integer groupId) {
        User user = apiSupport.getCurrentUser();
        GroupObject o = groupObjectDao.getViewableGroup(user, groupId);
        Group g = openApiMapper.map(o);
        g.setBalance(BigDecimal.valueOf(financeEntryDao.getBalance(o.getId(), user.getId())));
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
            return new ResponseEntity<>(HttpStatus.OK);
        }

        throw new NoResultException();
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
        User user = apiSupport.getCurrentUser();
        Optional<GroupObject> g = groupObjectRepository.findById(groupId);

        if (g.isPresent() && g.get().getOwner().getId().equals(user.getId())) {
            User u = userDao.getOrCreateUserWithEmail(email);

            GroupInvite invite = new GroupInvite();
            invite.setGroup(g.get());
            invite.setInvitedUser(u);
            groupInviteRepository.save(invite);

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
                g.get().getUsers().removeIf(u -> u.getId().equals(userId));
                groupObjectRepository.save(g.get());
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }

        throw new NoResultException();
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
            return ResponseEntity.ok(openApiMapper.mapInvites(invites));
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
