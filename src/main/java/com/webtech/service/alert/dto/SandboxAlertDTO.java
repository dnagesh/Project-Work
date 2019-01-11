package com.webtech.service.alert.dto;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("sandbox_alerts")
public class SandboxAlertDTO extends BaseAlert {

  @PrimaryKey
  private SandboxAlertDTOPrimaryKey primaryKey;

  private boolean isPromotedToLive;

  public SandboxAlertDTOPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(
      SandboxAlertDTOPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  public boolean isPromotedToLive() {
    return isPromotedToLive;
  }

  public void setPromotedToLive(boolean promotedToLive) {
    isPromotedToLive = promotedToLive;
  }

  @Override
  public String toString() {
    return "SandboxAlertDTO{" +
        "primaryKey=" + primaryKey +
        ", isPromotedToLive=" + isPromotedToLive +
        "} " + super.toString();
  }
}
