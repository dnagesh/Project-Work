package com.webtech.service.alertconfiguration.mapper;

import com.webtech.service.alertconfiguration.dto.AlertFilterDTO;
import com.irisium.service.alertconfiguration.model.AlertFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class AlertFilterMapper {

  public List<AlertFilter> getAlertFilters(Set<AlertFilterDTO> alertFilterDTOS) {
    List<AlertFilter> alertFilters = Collections.emptyList();
    if (!CollectionUtils.isEmpty(alertFilterDTOS)) {
      alertFilters = alertFilterDTOS.stream()
          .map(this::mapAlertFilterDTOToModel)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
      return (alertFilters);
    }
    return alertFilters;
  }

  public Map<String, String> getAlertParameters(Map<String, String> alertParameters) {
    if (!CollectionUtils.isEmpty(alertParameters)) {
      return (
          new HashMap<>(alertParameters));
    }
    return Collections.emptyMap();
  }

  public List<String> getAggregationFields(Set<String> alertAggregationFields) {
    if (!CollectionUtils.isEmpty(alertAggregationFields)) {
      return (
          new ArrayList<>(alertAggregationFields));
    }
    return Collections.emptyList();
  }

  public AlertFilter mapAlertFilterDTOToModel(AlertFilterDTO alertFilterDTO) {
    AlertFilter model = null;
    if (alertFilterDTO != null) {
      model = new AlertFilter();
      model.setAggregationFieldName(alertFilterDTO.getAggregationFieldName());
      model.setOperation(AlertFilter.OperationEnum.fromValue(alertFilterDTO.getOperation()));
      model.setValues(new ArrayList<>(alertFilterDTO.getValues()));
    }
    return model;
  }

}
