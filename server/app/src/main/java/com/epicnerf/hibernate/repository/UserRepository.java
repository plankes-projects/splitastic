package com.epicnerf.hibernate.repository;

import com.epicnerf.hibernate.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Integer> {

}