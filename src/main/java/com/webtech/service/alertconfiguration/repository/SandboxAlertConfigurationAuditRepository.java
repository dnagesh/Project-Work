package com.webtech.service.alertconfiguration.repository;

import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTOPrimaryKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SandboxAlertConfigurationAuditRepository extends
    CassandraRepository<SandboxAlertConfigurationAuditDTO, SandboxAlertConfigurationAuditDTOPrimaryKey> {

  SandboxAlertConfigurationAuditDTO findFirstByPrimaryKeySandboxUUIDAndPrimaryKeyAlertConfigurationUUIDOrderByPrimaryKeyAuditUUIDAsc(
      UUID sandboxUUID, UUID alertConfigurationUUID);

  List<SandboxAlertConfigurationAuditDTO> findByPrimaryKeySandboxUUIDAndPrimaryKeyAlertConfigurationUUID(
      UUID sandboxUUID,
      UUID alertConfigurationUUID);


}
