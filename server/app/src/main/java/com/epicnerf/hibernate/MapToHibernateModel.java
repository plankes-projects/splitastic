package com.epicnerf.hibernate;


import com.epicnerf.hibernate.model.FinanceEntry;
import com.epicnerf.hibernate.model.FinanceEntryEntry;
import com.epicnerf.hibernate.model.ImageData;
import com.epicnerf.hibernate.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;


@Component
public class MapToHibernateModel {
    @Autowired
    UserRepository userRepo;

    public static ImageData mapImage(com.epicnerf.model.ImageData image) {
        ImageData i = new ImageData();
        i.setUrl(image.getUrl());
        return i;
    }

    public List<FinanceEntryEntry> mapFinanceEntries(List<com.epicnerf.model.FinanceEntryEntry> entries, boolean withId) {
        return entries.stream()
                .map(o -> mapFinanceEntry(o, withId))
                .collect(Collectors.toList());
    }

    public FinanceEntryEntry mapFinanceEntry(com.epicnerf.model.FinanceEntryEntry entry, boolean withId) {
        FinanceEntryEntry e = new FinanceEntryEntry();
        e.setAmount(entry.getAmount().setScale(5, RoundingMode.DOWN).floatValue());
        e.setSpentFor(userRepo.findById(entry.getSpentFor()).get());
        if (withId) {
            e.setId(entry.getId());
        }
        return e;
    }

    public FinanceEntry mapFinance(com.epicnerf.model.FinanceEntry finance, boolean withId) {
        FinanceEntry f = new FinanceEntry();
        f.setTitle(finance.getTitle());
        f.setSpentFrom(userRepo.findById(finance.getSpentFrom()).get());
        f.setEntries(mapFinanceEntries(finance.getSpent(), withId));
        if (withId) {
            f.setId(finance.getId());
        }
        return f;
    }
}
