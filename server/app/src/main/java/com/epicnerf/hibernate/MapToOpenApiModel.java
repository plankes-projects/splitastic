package com.epicnerf.hibernate;

import com.epicnerf.hibernate.dao.FinanceEntryDao;
import com.epicnerf.hibernate.model.GroupInvite;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapToOpenApiModel {

    @Autowired
    private FinanceEntryDao financeEntryDao;

    public List<User> mapUsers(List<com.epicnerf.hibernate.model.User> users) {
        return users.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public User map(com.epicnerf.hibernate.model.User user) {
        User u = new User();
        u.setEmail(user.getEmail());
        u.setId(user.getId());
        u.setName(user.getName());
        u.setImage(map(user.getImage()));
        u.setIsVirtual(user.isVirtualUser());
        return u;
    }

    public ImageData map(com.epicnerf.hibernate.model.ImageData image) {
        ImageData i = new ImageData();
        i.setUrl(image.getUrl());
        return i;
    }

    public List<Group> map(List<GroupObject> groups, com.epicnerf.hibernate.model.User currentUser) {
        return groups.stream()
                .map(group -> map(group, currentUser))
                .collect(Collectors.toList());
    }

    public Group map(GroupObject group, com.epicnerf.hibernate.model.User currentUser) {
        Group g = new Group();
        g.setOwner(group.getOwner().getId());
        g.setName(group.getName());
        g.setDescription(group.getDescription());
        g.setId(group.getId());
        g.setImage(map(group.getImage()));
        g.setUsers(mapUsers(group.getUsers()));
        g.setBalance(BigDecimal.valueOf(financeEntryDao.getBalance(group.getId(), currentUser.getId())));
        g.setTotalExpenses(BigDecimal.valueOf(financeEntryDao.getTotalExpenses(group.getId())));
        return g;
    }

    public Invite map(GroupInvite invite, com.epicnerf.hibernate.model.User currentUser) {
        Invite i = new Invite();
        i.setEmail(invite.getInvitedUser().getEmail());
        i.setGroup(map(invite.getGroup(), currentUser));
        i.setId(invite.getId());
        return i;
    }

    public Chore map(com.epicnerf.hibernate.model.Chore chore, boolean isOwner) {
        Chore c = new Chore();
        c.setCreated(map(chore.getCreateDate()));
        c.setDescription(chore.getDescription());
        c.setTitle(chore.getTitle());
        c.setId(chore.getId());
        c.setIsOwner(isOwner);
        return c;
    }

    public List<Invite> mapInvites(List<com.epicnerf.hibernate.model.GroupInvite> invites, com.epicnerf.hibernate.model.User currentUser) {
        return invites.stream()
                .map(invite -> map(invite, currentUser))
                .collect(Collectors.toList());
    }

    public FinanceEntry map(com.epicnerf.hibernate.model.FinanceEntry finance) {
        FinanceEntry f = new FinanceEntry();
        f.setId(finance.getId());
        f.setSpentFrom(finance.getSpentFrom().getId());
        f.setTitle(finance.getTitle());
        f.setSpent(mapFinanceEntries(finance.getEntries()));
        f.setCreated(map(finance.getCreateDate()));

        f.setCreatedBy(finance.getCreatedBy() != null ? finance.getCreatedBy().getId() : finance.getSpentFrom().getId());
        return f;
    }

    private OffsetDateTime map(Date date) {
        return date.toInstant().atOffset(ZoneOffset.UTC);
    }

    public FinanceEntryEntry map(com.epicnerf.hibernate.model.FinanceEntryEntry entry) {
        FinanceEntryEntry e = new FinanceEntryEntry();
        e.setAmount(BigDecimal.valueOf(entry.getAmount()));
        e.setId(entry.getId());
        e.setSpentFor(entry.getSpentFor().getId());
        return e;
    }

    public List<FinanceEntryEntry> mapFinanceEntries(List<com.epicnerf.hibernate.model.FinanceEntryEntry> entries) {
        return entries.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public List<FinanceEntry> mapFinanceList(List<com.epicnerf.hibernate.model.FinanceEntry> entries) {
        return entries.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }
}
