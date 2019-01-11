package com.webtech.service.alertconfiguration.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("live_alert_configuration")
public class LiveAlertConfigurationDTO extends AlertConfiguration {

  @PrimaryKey
  private UUID uuid;

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  @Override
  public String toString() {
    return "LiveAlertConfigurationDTO{" +
        "uuid=" + uuid +
        "} " + super.toString();
  }
}
