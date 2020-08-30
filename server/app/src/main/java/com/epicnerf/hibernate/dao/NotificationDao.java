package com.epicnerf.hibernate.dao;

import com.epicnerf.hibernate.model.GroupInvite;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Component
public class GroupInviteDao {

    @Autowired
    private EntityManager entityManager;


    @Transactional
    public void deleteAllGroupInvites(GroupObject group) {
        entityManager
                .createNativeQuery("delete from group_invite where group_id = :groupId")
                .setParameter("groupId", group.getId())
                .executeUpdate();
    }

    public List<GroupInvite> getGroupInvites(GroupObject group) {
        String query = "SELECT * FROM group_invite where group_id = :group_id ";

        //noinspection unchecked
        return (List<GroupInvite>) entityManager
                .createNativeQuery(query, GroupInvite.class)
                .setParameter("group_id", group.getId())
                .getResultList();
    }

    public List<GroupInvite> getGroupInvites(User user) {
        String query = "SELECT * FROM group_invite where invited_user_id = :user_id ";

        //noinspection unchecked
        return (List<GroupInvite>) entityManager
                .createNativeQuery(query, GroupInvite.class)
                .setParameter("user_id", user.getId())
                .getResultList();
    }

    public GroupInvite getGroupInvite(User user, GroupObject group) {
        String query = "SELECT * FROM group_invite where group_id = :group_id and invited_user_id = :user_id";

        return (GroupInvite) entityManager
                .createNativeQuery(query, GroupInvite.class)
                .setParameter("group_id", group.getId())
                .setParameter("user_id", user.getId())
                .getSingleResult();
    }
}