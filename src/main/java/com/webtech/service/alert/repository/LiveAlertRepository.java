package com.webtech.service.alert.repository;

import com.webtech.service.alert.dto.LiveAlertDTO;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveAlertRepository extends CassandraRepository<LiveAlertDTO, UUID> {

}
