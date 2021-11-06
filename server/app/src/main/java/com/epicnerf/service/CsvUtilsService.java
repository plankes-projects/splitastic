package com.epicnerf.service;

import com.opencsv.CSVWriter;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvUtilsService {
    public String databaseResultToCsv(List<String> header, @SuppressWarnings("rawtypes") List databaseResult) {
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
