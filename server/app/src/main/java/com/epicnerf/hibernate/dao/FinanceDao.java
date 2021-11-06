package com.epicnerf.hibernate.dao;

import com.epicnerf.service.CsvUtilsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.List;

@Component
public class FinanceDao {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CsvUtilsService csvUtilsService;

    @Transactional
    public List<Object[]> getExportDataByGroupIdAsCsvString(int groupId) {
        String query = "select finance_entry.id, finance_entry.title, spent_from.id as spent_from_id, spent_for.id as spent_for_id, finance_entry_entry.amount, finance_entry_entry.create_date from finance_entry\n" +
                "join finance_entry_entries on finance_entry_entries.finance_entry_id = finance_entry.id\n" +
                "join finance_entry_entry on finance_entry_entry.id = finance_entry_entries.entries_id\n" +
                "join user spent_from on spent_from_id = spent_from.id\n" +
                "join user spent_for on spent_for_id = spent_for.id\n" +
                "where group_id = :group_id\n" +
                "order by finance_entry_entry.id;";

        //noinspection unchecked
        return (List<Object[]>) entityManager
                .createNativeQuery(query)
                .setParameter("group_id", groupId)
                .getResultList();
    }
}
