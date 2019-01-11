package com.webtech.service.alertconfiguration.repository;

import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LiveAlertConfigurationAuditByAlertConfigUUIDRepository extends
    CassandraRepository<LiveAlertConfigurationAuditByAlertConfigUUIDDTO, LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey> {

  @Query("SELECT * FROM live_alert_config_audit_by_alertconfiguuid WHERE alertConfigUUID IN ?0 AND auditTimestamp <= ?1 PER PARTITION LIMIT 1;")
  List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> findLiveAuditsForPointInTime(
      List<UUID> liveUUIDList, Instant pointInTime);

  List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> findByPrimaryKeyAlertConfigUUID(
      UUID alertConfigurationUUID);


}
