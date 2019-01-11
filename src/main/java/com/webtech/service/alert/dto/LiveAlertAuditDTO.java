package com.webtech.service.alert.dto;

import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("live_alerts_audit")
public class LiveAlertAuditDTO extends BaseLiveAlert {

  @PrimaryKey
  private LiveAlertAuditDTOPrimaryKey primaryKey;


  public LiveAlertAuditDTOPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(LiveAlertAuditDTOPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }


}
