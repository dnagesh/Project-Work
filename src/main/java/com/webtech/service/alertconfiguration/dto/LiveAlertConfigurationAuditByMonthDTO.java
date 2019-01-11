package com.webtech.service.alertconfiguration.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.Indexed;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("live_alert_config_audit_by_month")
public class LiveAlertConfigurationAuditByMonthDTO extends AlertConfiguration {

  @PrimaryKey
  private LiveAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey;

  @Indexed
  private UUID alertConfigurationUUID;

  public LiveAlertConfigurationAuditByMonthDTOPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(
      LiveAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getAlertConfigurationUUID() {
    return alertConfigurationUUID;
  }

  public void setAlertConfigurationUUID(UUID alertConfigurationUUID) {
    this.alertConfigurationUUID = alertConfigurationUUID;
  }
}
