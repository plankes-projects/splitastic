package com.epicnerf.hibernate.repository;

import com.epicnerf.hibernate.model.Notification;
import org.springframework.data.repository.CrudRepository;

public interface NotificationRepository extends CrudRepository<Notification, Long> {

}