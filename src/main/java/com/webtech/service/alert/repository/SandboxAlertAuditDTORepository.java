package com.webtech.service.alert.repository;

import com.webtech.service.alert.dto.SandboxAlertAuditDTO;
import com.webtech.service.alert.dto.SandboxAlertAuditDTOPrimaryKey;
import java.util.List;
import java.util.UUID;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SandboxAlertAuditDTORepository extends
    CassandraRepository<SandboxAlertAuditDTO, SandboxAlertAuditDTOPrimaryKey> {

  List<SandboxAlertAuditDTO> findAllByPrimaryKeyAlertId(UUID alertId);
}
