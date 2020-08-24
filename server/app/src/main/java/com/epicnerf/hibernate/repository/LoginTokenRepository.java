package com.epicnerf.hibernate.repository;

import com.epicnerf.hibernate.model.LoginToken;
import org.springframework.data.repository.CrudRepository;

public interface LoginTokenRepository extends CrudRepository<LoginToken, Integer> {

}