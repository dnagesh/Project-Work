package com.webtech.service.alertconfiguration.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("sandbox_alert_configuration")
public class SandboxAlertConfigurationDTO extends AlertConfiguration {

  @PrimaryKey
  private SandboxAlertConfigurationDTOPrimaryKey primaryKey;

  private UUID liveConfigUUID;

  public SandboxAlertConfigurationDTOPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(
      SandboxAlertConfigurationDTOPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  public UUID getLiveConfigUUID() {
    return liveConfigUUID;
  }

  public void setLiveConfigUUID(UUID liveConfigUUID) {
    this.liveConfigUUID = liveConfigUUID;
  }
}
