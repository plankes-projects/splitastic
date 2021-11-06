package com.epicnerf.service;

import com.epicnerf.hibernate.dao.ChoreDao;
import com.epicnerf.hibernate.dao.FinanceDao;
import com.epicnerf.hibernate.model.GroupObject;
import com.epicnerf.hibernate.model.User;
import com.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private CsvUtilsService csvUtilsService;

    @Autowired
    private ChoreDao choreDao;
    @Autowired
    private FinanceDao financeDao;

    public String getFinanceExportDataByGroupIdAsCsvString(GroupObject group) {
        HashMap<Integer, User> userIdToUser = getUserIdToUser(group);

        List<Object[]> result = financeDao.getExportDataByGroupIdAsCsvString(group.getId())
                .stream()
                .collect(Collectors.groupingBy(row -> (int)row[0]))
                .values()
                .stream()
                .map(e -> mapFinanceExportRow(userIdToUser, e))
                .collect(Collectors.toList());

        List<String> header = userIdToUser.values()
                .stream()
                .map(this::userToPrint)
                .collect(Collectors.toList());
        header.add("total");
        header.add("payed");
        header.add("create_date");
        header.add(0, "Title");

        return csvUtilsService.databaseResultToCsv(header, result);
    }

    private HashMap<Integer, User> getUserIdToUser(GroupObject group) {
        HashMap<Integer, User> result = new HashMap<>();
        for(User u : group.getUsers()){
            result.put(u.getId(), u);
        }
        return result;
    }

    private String userToPrint(User user) {
        return user.getName() + "(" + user.getId() + ")";
    }

    private String[] mapFinanceExportRow(HashMap<Integer, User> userIdToUser, List<Object[]> rows) {
        HashMap<Integer, Float> userIdToAmountSum = new HashMap<>();
        float total = 0.0f;
        for(Object[] row : rows){
            int spentForId = (int)(row[3]);
            float amount = (float)row[4];
            userIdToAmountSum.put(spentForId, amount);
            total += amount;
        }

        int spentFromId = (int)rows.get(0)[2];
        String title = (rows.get(0)[1]).toString();
        String created = (rows.get(0)[5]).toString();

        List<String> result = userIdToUser.values()
                .stream()
                .map(u -> userIdToAmountSum.getOrDefault(u.getId(), 0.0f).toString())
                .collect(Collectors.toList());
        result.add(Float.toString(total));
        result.add(userToPrint(userIdToUser.get(spentFromId)));
        result.add(created);
        result.add(0, title);

        return result.toArray(new String[0]);
    }

    public String getChoreExportDataByGroupIdAsCsvString(int groupId) {
        List<String> header = Arrays.asList("chore_id", "chore_title", "user_id", "user_title", "create_date");
        List<Object[]> result = choreDao.getExportDataByGroupIdAsCsvString(groupId);
        return csvUtilsService.databaseResultToCsv(header, result);
    }

    private String databaseResultToCsv(List<String> header, @SuppressWarnings("rawtypes") List databaseResult) {
        StringWriter stringWriter = new StringWriter();
        CSVWriter writer = new CSVWriter(stringWriter);
        writer.writeNext(header.toArray(new String[0]));

        //noinspection unchecked
        databaseResult
                .forEach(row -> {
                    Object[] castedRow= (Object[])row;
                    List<String> strings = new ArrayList<>();
                    for (Object entry : castedRow){
                        strings.add(entry.toString());
                    }
                    writer.writeNext(strings.toArray(new String[0]));
                });
        return stringWriter.toString();
    }
}
