package com.webtech.service.alertconfiguration.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("sandbox_alert_configuration_audit")
public class SandboxAlertConfigurationAuditDTO extends AlertConfiguration {

  @PrimaryKey
  private SandboxAlertConfigurationAuditDTOPrimaryKey primaryKey;

  private UUID liveConfigUUID;

  public SandboxAlertConfigurationAuditDTOPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(
      SandboxAlertConfigurationAuditDTOPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getLiveConfigUUID() {
    return liveConfigUUID;
  }

  public void setLiveConfigUUID(UUID liveConfigUUID) {
    this.liveConfigUUID = liveConfigUUID;
  }
}
