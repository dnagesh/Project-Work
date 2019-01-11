package com.webtech.service.alert.dto;

import java.util.UUID;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("sandbox_alerts_audit")
public class SandboxAlertAuditDTO extends BaseAlert {

  @PrimaryKey
  private SandboxAlertAuditDTOPrimaryKey primaryKey;

  private UUID runId;

  private boolean isPromotedToLive;


  public UUID getRunId() {
    return runId;
  }

  public void setRunId(UUID runId) {
    this.runId = runId;
  }

  public boolean isPromotedToLive() {
    return isPromotedToLive;
  }

  public void setPromotedToLive(boolean promotedToLive) {
    isPromotedToLive = promotedToLive;
  }

  public SandboxAlertAuditDTOPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(SandboxAlertAuditDTOPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  @Override
  public String toString() {
    return "SandboxAlertAuditDTO{" +
        "primaryKey=" + primaryKey +
        ", runId=" + runId +
        ", isPromotedToLive=" + isPromotedToLive +
        "} " + super.toString();
  }
}
