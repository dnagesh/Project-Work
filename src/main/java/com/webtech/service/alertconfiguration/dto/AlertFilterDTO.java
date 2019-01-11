package com.webtech.service.alertconfiguration.dto;

import java.util.Set;
import org.springframework.data.cassandra.core.mapping.UserDefinedType;

@UserDefinedType("alert_filter_udt")
public class AlertFilterDTO {

  private String aggregationFieldName;
  private String operation;
  private Set<String> values;

  public AlertFilterDTO(String aggregationFieldName, String operation, Set<String> values) {
    this.aggregationFieldName = aggregationFieldName;
    this.operation = operation;
    this.values = values;
  }

  public AlertFilterDTO() {
  }

  public String getAggregationFieldName() {
    return aggregationFieldName;
  }

  public void setAggregationFieldName(String aggregationFieldName) {
    this.aggregationFieldName = aggregationFieldName;
  }

  public String getOperation() {
    return operation;
  }

  public void setOperation(String operation) {
    this.operation = operation;
  }

  public Set<String> getValues() {
    return values;
  }

  public void setValues(Set<String> values) {
    this.values = values;
  }

  @Override
  public String toString() {
    return "AlertFilterDTO{" +
        "aggregationFieldName='" + aggregationFieldName + '\'' +
        ", operation='" + operation + '\'' +
        ", values=" + values +
        '}';
  }
}
