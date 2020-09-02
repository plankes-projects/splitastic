package com.epicnerf.hibernate.dao;

import com.epicnerf.api.ApiSupport;
import com.epicnerf.hibernate.MapToOpenApiModel;
import com.epicnerf.hibernate.model.Chore;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.User;
import com.epicnerf.hibernate.repository.ChoreRepository;
import com.epicnerf.hibernate.repository.UserRepository;
import com.epicnerf.model.ChoreSummary;
import com.epicnerf.model.ChoreSummaryArrayEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChoreDao {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApiSupport apiSupport;

    @Autowired
    private MapToOpenApiModel mapToOpenApiModel;

    @Autowired
    private ChoreRepository choreRepository;

    @Transactional
    public void deleteChore(Chore chore) {
        entityManager
                .createNativeQuery("delete from chore_entry where chore_id = :choreId")
                .setParameter("choreId", chore.getId())
                .executeUpdate();

        choreRepository.delete(chore);
    }

    @Transactional
    public void moveChoresToUser(int groupId, int fromUserId, int toUserId) {
        String query = "update chore join chore_entry on chore_entry.chore_id = chore.id ";
        query += "set chore_entry.user_id = :toUserId ";
        query += "where chore.group_id = :groupId and chore_entry.user_id = :fromUserId";
        entityManager
                .createNativeQuery(query)
                .setParameter("groupId", groupId)
                .setParameter("fromUserId", fromUserId)
                .setParameter("toUserId", toUserId)
                .executeUpdate();
    }

    public boolean hasChoresData(int groupId, int userId) {
        String query = "SELECT EXISTS(select * from chore join chore_entry on chore_entry.chore_id = chore.id ";
        query += "where chore.group_id = :groupId and chore_entry.user_id = :userId )";
        BigInteger exists = (BigInteger) entityManager
                .createNativeQuery(query)
                .setParameter("groupId", groupId)
                .setParameter("userId", userId)
                .getSingleResult();
        return exists.intValue() > 0;
    }

    public List<ChoreSummary> getAllChoreSummaries(GroupObject group, boolean isOwner) {
        //noinspection unchecked
        return (List<ChoreSummary>) entityManager
                .createNativeQuery("select * from chore where group_id = :groupId", Chore.class)
                .setParameter("groupId", group.getId())
                .getResultList()
                .stream()
                .map(c -> getChoreSummary((Chore) c, isOwner))
                .collect(Collectors.toList());
    }

    public ChoreSummary getChoreSummary(Chore chore, boolean isOwner) {
        //noinspection unchecked
        List<ChoreSummaryArrayEntry> entries = (List<ChoreSummaryArrayEntry>) entityManager
                .createNativeQuery("SELECT user_id, count(*) as count FROM chore_entry where chore_id = :choreId group by user_id;")
                .setParameter("choreId", chore.getId())
                .getResultList()
                .stream()
                .map(c -> {
                    Object[] castedRow = (Object[]) c;
                    ChoreSummaryArrayEntry e = new ChoreSummaryArrayEntry();
                    e.setUserId((Integer) castedRow[0]);
                    e.setCount(((BigInteger) castedRow[1]).intValue());
                    return e;
                })
                .collect(Collectors.toList());

        ChoreSummary sum = new ChoreSummary();
        sum.setChore(mapToOpenApiModel.map(chore, isOwner));
        sum.setDoneArray(entries);
        return sum;
    }

    @Transactional
    public void deleteLatestChoreEntry(Chore chore, User user) {
        entityManager
                .createNativeQuery("delete from chore_entry where chore_id = :choreId and user_id = :userId order by id desc limit 1;")
                .setParameter("choreId", chore.getId())
                .setParameter("userId", user.getId())
                .executeUpdate();
    }
}
