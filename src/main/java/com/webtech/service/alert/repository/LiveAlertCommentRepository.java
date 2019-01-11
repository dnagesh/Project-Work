package com.webtech.service.alert.repository;

import com.webtech.service.alert.dto.LiveAlertCommentDTO;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveAlertCommentRepository extends CassandraRepository<LiveAlertCommentDTO, UUID> {

  List<LiveAlertCommentDTO> findAllByAlertId(UUID alertId);
}
