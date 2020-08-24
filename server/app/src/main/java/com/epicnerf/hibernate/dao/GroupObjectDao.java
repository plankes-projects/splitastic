package com.epicnerf.hibernate.dao;

import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.GroupObjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;

@Component
public class GroupObjectDao {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private GroupObjectRepository groupObjectRepository;

    public List<GroupObject> paginateGroups(User user, int limit, Integer lastId) {
        String query = "SELECT u.* FROM group_object_users m ";
        query += "join group_object u on m.group_object_id = u.id ";
        query += "where id < :id ";
        query += "and m.users_id = :user_id ORDER BY u.id desc";

        if (lastId == null) {
            lastId = Integer.MAX_VALUE;
        }

        //noinspection unchecked
        return (List<GroupObject>) entityManager
                .createNativeQuery(query, GroupObject.class)
                .setParameter("id", lastId)
                .setParameter("user_id", user.getId())
                .setMaxResults(limit)
                .getResultList();
    }

    public GroupObject getViewableGroup(User user, Integer groupId) {
        String query = "SELECT u.* FROM group_object_users m ";
        query += "join group_object u on m.group_object_id = u.id ";
        query += "where id = :id ";
        query += "and m.users_id = :user_id ORDER BY u.id desc";

        return (GroupObject) entityManager
                .createNativeQuery(query, GroupObject.class)
                .setParameter("id", groupId)
                .setParameter("user_id", user.getId())
                .getSingleResult();
    }
}
