package com.webtech.service.alertconfiguration.dto;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyClass;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;

@PrimaryKeyClass
public class LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private UUID alertConfigUUID;

  @PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordinal = 2, ordering = Ordering.DESCENDING)
  private Instant auditTimestamp;

  public LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey() {
  }

  public LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey(UUID alertConfigUUID,
      Instant auditTimestamp) {
    this.alertConfigUUID = alertConfigUUID;
    this.auditTimestamp = auditTimestamp;
  }

  public UUID getAlertConfigUUID() {
    return alertConfigUUID;
  }

  public void setAlertConfigUUID(UUID alertConfigUUID) {
    this.alertConfigUUID = alertConfigUUID;
  }

  public Instant getAuditTimestamp() {
    return auditTimestamp;
  }

  public void setAuditTimestamp(Instant auditTimestamp) {
    this.auditTimestamp = auditTimestamp;
  }
}
