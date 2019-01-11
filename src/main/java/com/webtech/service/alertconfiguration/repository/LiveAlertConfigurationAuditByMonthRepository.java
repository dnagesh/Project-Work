package com.webtech.service.alertconfiguration.repository;

import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTOPrimaryKey;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveAlertConfigurationAuditByMonthRepository extends
    CassandraRepository<LiveAlertConfigurationAuditByMonthDTO, LiveAlertConfigurationAuditByMonthDTOPrimaryKey> {

  @Query("select * from live_alert_config_audit_by_month WHERE whenMonth IN ?0 LIMIT ?1;")
  List<LiveAlertConfigurationAuditByMonthDTO> findLatestTopNAuditsWithAgeLimit(
      List<LocalDate> months,
      Integer numberOfRecords);
}
