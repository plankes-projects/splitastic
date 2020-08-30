package com.epicnerf.hibernate.repository;

import com.epicnerf.hibernate.model.Chore;
import org.springframework.data.repository.CrudRepository;

public interface ChoreRepository extends CrudRepository<Chore, Integer> {

}