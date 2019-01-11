package com.webtech.service.alertconfiguration.dto;

import java.time.Instant;
import java.util.Set;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;

@Table("sandbox_run")
public class SandboxRunDTO {


  @PrimaryKey
  private SandboxRunDTOPrimaryKey primaryKey;

  private String owner;

  private Set<SandboxRunAlertConfigurationDTO> alertConfigurationSet;

  private Instant startTime;

  private Instant endTime;

  private Instant dataFrom;

  private Instant dataTo;

  public SandboxRunDTOPrimaryKey getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(SandboxRunDTOPrimaryKey primaryKey) {
    this.primaryKey = primaryKey;
  }

  public String getOwner() {
    return owner;
  }

  public void setOwner(String owner) {
    this.owner = owner;
  }

  public Set<SandboxRunAlertConfigurationDTO> getAlertConfigurationSet() {
    return alertConfigurationSet;
  }

  public void setAlertConfigurationSet(Set<SandboxRunAlertConfigurationDTO> alertConfigurationSet) {
    this.alertConfigurationSet = alertConfigurationSet;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public void setStartTime(Instant startTime) {
    this.startTime = startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public void setEndTime(Instant endTime) {
    this.endTime = endTime;
  }

  public Instant getDataFrom() {
    return dataFrom;
  }

  public void setDataFrom(Instant dataFrom) {
    this.dataFrom = dataFrom;
  }

  public Instant getDataTo() {
    return dataTo;
  }

  public void setDataTo(Instant dataTo) {
    this.dataTo = dataTo;
  }
}



