package com.epicnerf.hibernate.repository;

import com.epicnerf.hibernate.model.FinanceEntry;
import org.springframework.data.repository.CrudRepository;

public interface FinanceEntryRepository extends CrudRepository<FinanceEntry, Integer> {

}