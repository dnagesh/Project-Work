package com.webtech.service.alertconfiguration.repository;

import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditDTO;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveAlertConfigurationAuditRepository extends
    CassandraRepository<LiveAlertConfigurationAuditDTO, UUID> {

  Optional<LiveAlertConfigurationAuditDTO> findByAuditUUID(UUID auditUUID);
}
