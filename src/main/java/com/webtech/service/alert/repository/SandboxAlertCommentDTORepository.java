package com.webtech.service.alert.repository;

import com.webtech.service.alert.dto.SandboxAlertCommentDTO;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SandboxAlertCommentDTORepository extends
    CassandraRepository<SandboxAlertCommentDTO, UUID> {

  List<SandboxAlertCommentDTO> findAllByAlertId(UUID alertId);
}
