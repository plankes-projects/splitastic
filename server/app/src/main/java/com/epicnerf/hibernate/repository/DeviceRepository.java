package com.epicnerf.hibernate.repository;

import com.epicnerf.hibernate.model.Device;
import org.springframework.data.repository.CrudRepository;

public interface DeviceRepository extends CrudRepository<Device, Integer> {

}