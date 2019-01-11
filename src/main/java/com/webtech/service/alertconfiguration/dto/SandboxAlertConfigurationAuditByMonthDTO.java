package com.webtech.service.alertconfiguration.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("sandbox_alert_config_audit_by_month")
public class SandboxAlertConfigurationAuditByMonthDTO extends AlertConfiguration {

  @PrimaryKey
  private SandboxAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey;

  private UUID alertConfigurationUUID;

  public SandboxAlertConfigurationAuditByMonthDTOPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(
      SandboxAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getAlertConfigurationUUID() {
    return alertConfigurationUUID;
  }

  public void setAlertConfigurationUUID(UUID alertConfigurationUUID) {
    this.alertConfigurationUUID = alertConfigurationUUID;
  }

}
