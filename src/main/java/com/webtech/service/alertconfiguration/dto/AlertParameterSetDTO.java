package com.webtech.service.alertconfiguration.dto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.springframework.data.cassandra.core.mapping.PrimaryKey;
import org.springframework.data.cassandra.core.mapping.Table;
import org.springframework.util.CollectionUtils;

@Table("alert_parameters")
public class AlertParameterSetDTO {

  @PrimaryKey
  private String apsHash;

  private String alertLogicType;

  private String businessUnit;

  private Map<String, String> alertParameters;

  private Set<String> alertAggregationFields;

  private Set<AlertFilterDTO> alertFilterDTOS;

  private Map<String, Boolean> logicOverrides;

  public AlertParameterSetDTO(
      String alertLogicType, String businessUnit,
      Map<String, String> alertParameters,
      Set<String> alertAggregationFields,
      Set<AlertFilterDTO> alertFilterDTOS,
      Map<String, Boolean> logicOverrides) throws NoSuchAlgorithmException {

    this.alertLogicType = alertLogicType;
    this.businessUnit = businessUnit;

    //Sort the parameters so hash remains consistent
    if (!CollectionUtils.isEmpty(alertParameters)) {
      this.alertParameters = new TreeMap<>(alertParameters);
    }
    if (!CollectionUtils.isEmpty(alertAggregationFields)) {
      this.alertAggregationFields = new TreeSet<>(alertAggregationFields);
    }
    if (!CollectionUtils.isEmpty(alertFilterDTOS)) {
      this.alertFilterDTOS = getCopyOfAlertFilterDTOS(alertFilterDTOS);
    }
    if (!CollectionUtils.isEmpty(logicOverrides)) {
      this.logicOverrides = new TreeMap<>(logicOverrides);
    }
    this.apsHash = generateHash(this.getHashInput());
  }

  /*WARNING: Do not change this method unless you know what you are doing! */
  private String getHashInput() {
    return "AlertParameterSetDTO{" +
        "alertLogicType='" + alertLogicType + '\'' +
        ", businessUnit='" + businessUnit + '\'' +
        ", alertParameters=" + alertParameters +
        ", alertAggregationFields=" + alertAggregationFields +
        ", alertFilterDTOS=" + alertFilterDTOS +
        ", logicOverrides=" + logicOverrides +
        '}';
  }

  public String getAlertLogicType() {
    return alertLogicType;
  }

  public String getApsHash() {
    return apsHash;
  }

  public Map<String, String> getAlertParameters() {
    if (CollectionUtils.isEmpty(alertParameters)) {
      return alertParameters;
    }
    return new TreeMap<>(alertParameters);
  }

  public Set<String> getAlertAggregationFields() {
    if (CollectionUtils.isEmpty(alertAggregationFields)) {
      return alertAggregationFields;
    }
    return new TreeSet<>(alertAggregationFields);
  }

  public Set<AlertFilterDTO> getAlertFilterDTOS() {

    return getCopyOfAlertFilterDTOS(alertFilterDTOS);
  }

  private TreeSet<AlertFilterDTO> getCopyOfAlertFilterDTOS(Set<AlertFilterDTO> inputDTOs) {
    if (CollectionUtils.isEmpty(inputDTOs)) {
      return new TreeSet<>(Collections.emptySet());
    }
    TreeSet<AlertFilterDTO> dtoSet = new TreeSet<>(
        Comparator.comparing(AlertFilterDTO::getAggregationFieldName));
    dtoSet.addAll(inputDTOs.stream().filter(Objects::nonNull).map(this::copyAlertFilter)
        .collect(Collectors.toSet()));
    return dtoSet;
  }

  private AlertFilterDTO copyAlertFilter(AlertFilterDTO inputDTO) {
    AlertFilterDTO copy = new AlertFilterDTO();
    copy.setAggregationFieldName(inputDTO.getAggregationFieldName());
    copy.setOperation(inputDTO.getOperation());
    copy.setValues(new TreeSet<>(inputDTO.getValues()));
    return copy;
  }

  public String getBusinessUnit() {
    return businessUnit;
  }

  public Map<String, Boolean> getLogicOverrides() {
    if (CollectionUtils.isEmpty(logicOverrides)) {
      return logicOverrides;
    }
    return new TreeMap<>(logicOverrides);
  }

  private String generateHash(String input) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-512");
    md.update(input.getBytes());
    byte[] aMessageDigest = md.digest();
    String outEncoded = Base64.getEncoder().encodeToString(aMessageDigest);
    return (outEncoded);
  }

  @Override
  public String toString() {
    return "AlertParameterSetDTO{" +
        "apsHash='" + apsHash + '\'' +
        ", alertLogicType='" + alertLogicType + '\'' +
        ", businessUnit='" + businessUnit + '\'' +
        ", alertParameters=" + alertParameters +
        ", alertAggregationFields=" + alertAggregationFields +
        ", alertFilterDTOS=" + alertFilterDTOS +
        ", logicOverrides=" + logicOverrides +
        '}';
  }

}
