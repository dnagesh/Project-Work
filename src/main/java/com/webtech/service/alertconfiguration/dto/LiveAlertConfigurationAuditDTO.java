package com.webtech.service.alertconfiguration.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("live_alert_configuration_audit")
public class LiveAlertConfigurationAuditDTO extends AlertConfiguration {

  @PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, ordinal = 1)
  private UUID auditUUID;

  private UUID alertConfigurationUUID;

  public UUID getAuditUUID() {
    return auditUUID;
  }

  public void setAuditUUID(UUID auditUUID) {
    this.auditUUID = auditUUID;
  }

  public UUID getAlertConfigurationUUID() {
    return alertConfigurationUUID;
  }

  public void setAlertConfigurationUUID(UUID alertConfigurationUUID) {
    this.alertConfigurationUUID = alertConfigurationUUID;
  }
}
