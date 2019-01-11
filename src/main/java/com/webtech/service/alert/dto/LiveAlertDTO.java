package com.webtech.service.alert.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("live_alerts")
public class LiveAlertDTO extends BaseLiveAlert {

  @PrimaryKey
  private UUID alertId;


  public UUID getAlertId() {
    return alertId;
  }

  public void setAlertId(UUID alertId) {
    this.alertId = alertId;
  }

  @Override
  public String toString() {
    return "LiveAlertDTO{" +
        "alertId=" + alertId +
        "} " + super.toString();
  }
}
