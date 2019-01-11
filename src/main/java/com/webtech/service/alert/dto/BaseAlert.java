package com.webtech.service.alert.dto;

import java.time.Instant;
import java.util.Set;

public abstract class BaseAlert {

  private String apsHash;

  private String title;

  private String businessUnit;

  private String state;

  private Instant startTime;

  private Instant endTime;

  private String type;

  private String configuration;

  private Set<String> classification;

  private String description;

  private String instrumentDescription;

  private Set<String> participants;

  private String assignee;

  private Instant createdDate;

  private String updatedBy;

  private Instant updatedDate;


  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
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

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getConfiguration() {
    return configuration;
  }

  public void setConfiguration(String configuration) {
    this.configuration = configuration;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getInstrumentDescription() {
    return instrumentDescription;
  }

  public void setInstrumentDescription(String instrumentDescription) {
    this.instrumentDescription = instrumentDescription;
  }

  public Set<String> getParticipants() {
    return participants;
  }

  public void setParticipants(Set<String> participants) {
    this.participants = participants;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBusinessUnit() {
    return businessUnit;
  }

  public void setBusinessUnit(String businessUnit) {
    this.businessUnit = businessUnit;
  }

  public Set<String> getClassification() {
    return classification;
  }

  public void setClassification(Set<String> classification) {
    this.classification = classification;
  }

  public String getAssignee() {
    return assignee;
  }

  public void setAssignee(String assignee) {
    this.assignee = assignee;
  }


  public String getApsHash() {
    return apsHash;
  }

  public void setApsHash(String apsHash) {
    this.apsHash = apsHash;
  }

  public Instant getCreatedDate() {
    return createdDate;
  }

  public void setCreatedDate(Instant createdDate) {
    this.createdDate = createdDate;
  }

  public String getUpdatedBy() {
    return updatedBy;
  }

  public void setUpdatedBy(String updatedBy) {
    this.updatedBy = updatedBy;
  }

  public Instant getUpdatedDate() {
    return updatedDate;
  }

  public void setUpdatedDate(Instant updatedDate) {
    this.updatedDate = updatedDate;
  }

  @Override
  public String toString() {
    return "Alert{" +
        ", apsHash='" + apsHash + '\'' +
        ", title='" + title + '\'' +
        ", businessUnit='" + businessUnit + '\'' +
        ", state='" + state + '\'' +
        ", startTime=" + startTime +
        ", endTime=" + endTime +
        ", type='" + type + '\'' +
        ", configuration='" + configuration + '\'' +
        ", classification=" + classification +
        ", description='" + description + '\'' +
        ", instrumentDescription='" + instrumentDescription + '\'' +
        ", participants=" + participants +
        ", assignee='" + assignee + '\'' +
        ", createdDate=" + createdDate +
        ", updatedBy='" + updatedBy + '\'' +
        ", updatedDate=" + updatedDate +
        '}';
  }
}
