package com.epicnerf.hibernate;

import com.epicnerf.hibernate.model.GroupInvite;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MapToOpenApiModel {

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
        return u;
    }

    public ImageData map(com.epicnerf.hibernate.model.ImageData image) {
        ImageData i = new ImageData();
        i.setUrl(image.getUrl());
        return i;
    }

    public List<Group> map(List<GroupObject> groups) {
        return groups.stream()
                .map(this::map)
                .collect(Collectors.toList());
    }

    public Group map(GroupObject group) {
        Group g = new Group();
        g.setOwner(group.getOwner().getId());
        g.setName(group.getName());
        g.setDescription(group.getDescription());
        g.setId(group.getId());
        g.setImage(map(group.getImage()));
        g.setUsers(mapUsers(group.getUsers()));
        return g;
    }

    public Invite map(GroupInvite invite) {
        Invite i = new Invite();
        i.setEmail(invite.getInvitedUser().getEmail());
        i.setGroup(map(invite.getGroup()));
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

    public List<Invite> mapInvites(List<com.epicnerf.hibernate.model.GroupInvite> invites) {
        return invites.stream()
                .map(this::map)
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
