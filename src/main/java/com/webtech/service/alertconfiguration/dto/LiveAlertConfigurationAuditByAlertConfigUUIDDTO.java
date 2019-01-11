package com.webtech.service.alertconfiguration.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("live_alert_config_audit_by_alertconfiguuid")
public class LiveAlertConfigurationAuditByAlertConfigUUIDDTO extends AlertConfiguration {

  @PrimaryKey
  private LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey primaryKey;

  private UUID auditUUID;

  public LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(
      LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getAuditUUID() {
    return auditUUID;
  }

  public void setAuditUUID(UUID auditUUID) {
    this.auditUUID = auditUUID;
  }
}
