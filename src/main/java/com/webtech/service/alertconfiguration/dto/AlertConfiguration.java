package com.webtech.service.alertconfiguration.dto;

import java.time.Instant;

public abstract class AlertConfiguration {

  private String name;
  private String alertLogicType;
  private String status;
  private String apsHash;
  private String createdBy;
  private Instant createdWhen;
  private String updatedBy;
  private Instant updatedWhen;
  private String comment;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getApsHash() {
    return apsHash;
  }

  public void setApsHash(String apsHash) {
    this.apsHash = apsHash;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  public Instant getCreatedWhen() {
    return createdWhen;
  }

  public void setCreatedWhen(Instant createdWhen) {
    this.createdWhen = createdWhen;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getAlertLogicType() {
    return alertLogicType;
  }

  public void setAlertLogicType(String alertLogicType) {
    this.alertLogicType = alertLogicType;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Instant getUpdatedWhen() {
    return updatedWhen;
  }

  public void setUpdatedWhen(Instant updatedWhen) {
    this.updatedWhen = updatedWhen;
  }

  @Override
  public String toString() {
    return "AlertConfiguration{" +
        "name='" + name + '\'' +
        ", alertLogicType='" + alertLogicType + '\'' +
        ", status='" + status + '\'' +
        ", apsHash='" + apsHash + '\'' +
        ", createdBy='" + createdBy + '\'' +
        ", createdWhen=" + createdWhen +
        ", updatedBy='" + updatedBy + '\'' +
        ", updatedWhen=" + updatedWhen +
        ", comment='" + comment + '\'' +
        '}';
  }
}
