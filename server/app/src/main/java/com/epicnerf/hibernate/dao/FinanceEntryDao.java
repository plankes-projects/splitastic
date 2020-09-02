package com.epicnerf.hibernate.dao;

import com.epicnerf.hibernate.model.FinanceEntry;
import com.epicnerf.hibernate.model.FinanceEntryEntry;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.repository.FinanceEntryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.math.BigInteger;
import java.util.List;

@Component
public class FinanceEntryDao {

    @Autowired
    private EntityManager entityManager;
    @Autowired
    private FinanceEntryRepository financeEntryRepository;

    @Transactional
    public void deleteAllFinanceEntries(GroupObject group) {
        //proper way of deleting did not work, so we just delete the n to n table and then all orphans
        String q = "delete finance_entry_entries from finance_entry \n" +
                "join finance_entry_entries on finance_entry_entries.finance_entry_id = finance_entry.id \n" +
                "where group_id = :groupId";
        entityManager
                .createNativeQuery(q)
                .setParameter("groupId", group.getId())
                .executeUpdate();

        q = "delete finance_entry from finance_entry \n" +
                "left join finance_entry_entries on finance_entry_id = finance_entry.id \n" +
                "where finance_entry_id is null;";
        entityManager
                .createNativeQuery(q)
                .executeUpdate();

        q = "delete finance_entry_entry from finance_entry_entry \n" +
                "left join finance_entry_entries on entries_id = finance_entry_entry.id \n" +
                "where entries_id is null";
        entityManager
                .createNativeQuery(q)
                .executeUpdate();
    }

    @Transactional
    public void moveFinanceToUser(int groupId, int fromUserId, int toUserId) {
        distributeRedundantEntries(toUserId);

        String query = "update finance_entry  join finance_entry_entries on finance_entry_entries.finance_entry_id = finance_entry.id ";
        query += "join finance_entry_entry on finance_entry_entries.entries_id = finance_entry_entry.id ";
        query += "set finance_entry_entry.spent_for_id = :toUserId ";
        query += "where finance_entry.group_id = :groupId and finance_entry_entry.spent_for_id = :fromUserId";
        entityManager
                .createNativeQuery(query)
                .setParameter("groupId", groupId)
                .setParameter("fromUserId", fromUserId)
                .setParameter("toUserId", toUserId)
                .executeUpdate();

        entityManager
                .createNativeQuery("update finance_entry set spent_from_id = :toUserId where group_id = :groupId and spent_from_id = :fromUserId")
                .setParameter("groupId", groupId)
                .setParameter("fromUserId", fromUserId)
                .setParameter("toUserId", toUserId)
                .executeUpdate();

        entityManager
                .createNativeQuery("update finance_entry set created_by_id = :toUserId where group_id = :groupId and created_by_id = :fromUserId")
                .setParameter("groupId", groupId)
                .setParameter("fromUserId", fromUserId)
                .setParameter("toUserId", toUserId)
                .executeUpdate();
    }

    public boolean hasFinanceData(int groupId, int userId) {
        String query = "SELECT EXISTS(select * from finance_entry where group_id = :groupId and ";
        query += " (created_by_id = :userId or spent_from_id = :userId))";
        BigInteger exists = (BigInteger) entityManager
                .createNativeQuery(query)
                .setParameter("groupId", groupId)
                .setParameter("userId", userId)
                .getSingleResult();
        if (exists.intValue() > 0) {
            return true;
        }

        query = "SELECT EXISTS(select * from finance_entry  join finance_entry_entries on finance_entry_entries.finance_entry_id = finance_entry.id ";
        query += "join finance_entry_entry on finance_entry_entries.entries_id = finance_entry_entry.id ";
        query += "where finance_entry.group_id = :groupId and finance_entry_entry.spent_for_id = :userId)";
        exists = (BigInteger) entityManager
                .createNativeQuery(query)
                .setParameter("groupId", groupId)
                .setParameter("userId", userId)
                .getSingleResult();
        return exists.intValue() > 0;
    }

    private void distributeRedundantEntries(int toUserId) {
        //noinspection unchecked
        List<Integer> redundantEntryIds = (List<Integer>) entityManager
                .createNativeQuery("select id from finance_entry_entry where spent_for_id = :toUserId")
                .setParameter("toUserId", toUserId)
                .getResultList();
        redundantEntryIds.forEach(this::fixRedundantEntry);
    }

    private void fixRedundantEntry(int redundantEntryId) {
        String query = "select finance_entry_id from finance_entry_entries where finance_entry_entries.entries_id = :entryId";

        Integer financeId = (Integer) entityManager
                .createNativeQuery(query)
                .setParameter("entryId", redundantEntryId)
                .getSingleResult();

        FinanceEntry entry = financeEntryRepository.findById(financeId).orElse(null);


        assert entry != null;
        FinanceEntryEntry redundantEntry = entry.getEntries().stream()
                .filter(e -> e.getId().equals(redundantEntryId))
                .findAny()
                .orElse(null);

        assert redundantEntry != null;
        float addToOther = redundantEntry.getAmount() / (entry.getEntries().size() - 1);
        entry.getEntries().forEach(e -> e.setAmount(e.getAmount() + addToOther));
        entry.getEntries().remove(redundantEntry);

        financeEntryRepository.save(entry);
    }

    public List<FinanceEntry> paginateFinanceEntries(GroupObject group, int limit, Integer lastId) {
        String query = "SELECT * FROM finance_entry ";
        query += "where id < :id and group_id = :group_id ORDER BY id desc";

        if (lastId == null) {
            lastId = Integer.MAX_VALUE;
        }

        //noinspection unchecked
        return (List<FinanceEntry>) entityManager
                .createNativeQuery(query, FinanceEntry.class)
                .setParameter("id", lastId)
                .setParameter("group_id", group.getId())
                .setMaxResults(limit)
                .getResultList();
    }

    public Double getTotalExpenses(int groupId) {
        String query = "SELECT sum(amount) FROM finance_entry_entry\n" +
                "join finance_entry_entries on finance_entry_entries.entries_id = finance_entry_entry.id\n" +
                "join finance_entry on finance_entry_entries.finance_entry_id = finance_entry.id\n" +
                "where group_id = :groupId ";
        Double get = (Double) entityManager
                .createNativeQuery(query)
                .setParameter("groupId", groupId)
                .getSingleResult();
        if (get == null) {
            get = 0.0;
        }
        return get;
    }

    public Double getBalance(int groupId, int userId) {
        String baseQuery = "SELECT sum(amount) FROM finance_entry_entry\n" +
                "join finance_entry_entries on finance_entry_entries.entries_id = finance_entry_entry.id\n" +
                "join finance_entry on finance_entry_entries.finance_entry_id = finance_entry.id\n" +
                "where group_id = :groupId ";

        String getMoneyQuery = baseQuery + "and spent_from_id = :userId and spent_for_id != :userId";
        String oweMoneyQuery = baseQuery + "and spent_from_id != :userId and spent_for_id = :userId";

        Double get = (Double) entityManager
                .createNativeQuery(getMoneyQuery)
                .setParameter("userId", userId)
                .setParameter("groupId", groupId)
                .getSingleResult();
        Double owe = (Double) entityManager
                .createNativeQuery(oweMoneyQuery)
                .setParameter("userId", userId)
                .setParameter("groupId", groupId)
                .getSingleResult();
        if (get == null) {
            get = 0.0;
        }
        if (owe == null) {
            owe = 0.0;
        }
        return get - owe;
    }

    public List<String> getSuggestions(int groupId, int userId) {

        int numResults = 50;
        //noinspection unchecked
        List<String> suggestions = (List<String>) entityManager
                .createNativeQuery("select distinct title from finance_entry where spent_from_id = :userId and group_id = :groupId order by id desc limit " + numResults)
                .setParameter("userId", userId)
                .setParameter("groupId", groupId)
                .getResultList();

        if (suggestions.size() < numResults) {
            int limit = numResults - suggestions.size();

            String titlePart = suggestions.size() == 0 ? "" : "and title not in (:titles)";

            Query q = entityManager
                    .createNativeQuery("select distinct title from finance_entry where spent_from_id = :userId " + titlePart + " order by id desc limit " + limit)
                    .setParameter("userId", userId);

            if (suggestions.size() != 0) {
                q.setParameter("titles", suggestions);
            }

            //noinspection unchecked
            q.getResultList().forEach(o -> suggestions.add((String) o));
        }
        return suggestions;
    }
}