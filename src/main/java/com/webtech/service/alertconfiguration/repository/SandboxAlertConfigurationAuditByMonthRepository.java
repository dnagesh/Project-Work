package com.webtech.service.alertconfiguration.repository;


import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditByMonthDTOPrimaryKey;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SandboxAlertConfigurationAuditByMonthRepository extends
    CassandraRepository<SandboxAlertConfigurationAuditByMonthDTO, SandboxAlertConfigurationAuditByMonthDTOPrimaryKey> {

  @Query("select * from sandbox_alert_config_audit_by_month WHERE sandboxUUID = ?0 AND whenMonth IN ?1 LIMIT ?2;")
  List<SandboxAlertConfigurationAuditByMonthDTO> findLatestTopNAuditsWithAgeLimit(UUID sandboxId,
      List<LocalDate> months,
      Integer numberOfRecords);
}
