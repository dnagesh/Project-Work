package com.webtech.service.alertconfiguration.repository;

import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTOPrimaryKey;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SandboxAlertConfigurationRepository extends
    CassandraRepository<SandboxAlertConfigurationDTO, SandboxAlertConfigurationDTOPrimaryKey> {

  List<SandboxAlertConfigurationDTO> findAllByPrimaryKeySandboxUUID(UUID sandboxUUID);
}
