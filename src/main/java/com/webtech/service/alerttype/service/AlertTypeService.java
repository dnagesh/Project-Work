package com.webtech.service.alerttype.service;

import com.irisium.service.alertDefinition.model.AlertType;
import com.irisium.service.alertDefinition.model.DeploymentAlertType;
import com.irisium.service.alertDefinition.model.GuiDeploymentAlertType;
import com.irisium.service.alertDefinition.model.LogicOverride;
import com.irisium.service.alertDefinition.model.LogicOverridePreset;
import com.irisium.service.alertDefinition.model.Parameter;
import com.irisium.service.alertDefinition.model.Parameter.DataTypeEnum;
import com.irisium.service.alertDefinition.model.ParameterPreset;
import com.webtech.service.alerttype.config.BaseAlertTypeConfiguration;
import com.webtech.service.common.exception.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class AlertTypeService {

  private final BaseAlertTypeConfiguration alertTypeConfiguration;

  public AlertTypeService(
      BaseAlertTypeConfiguration alertTypeConfiguration) {
    this.alertTypeConfiguration = alertTypeConfiguration;
  }

  public Map<String, String> getDeploymentAlertTypes() throws IOException {
    return alertTypeConfiguration.getDeploymentAlertTypes().entrySet()
        .stream()
        .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getName()));
  }

  public Map<String, AlertType> getAlertTypes() throws IOException {
    return alertTypeConfiguration.getBaseAlertTypes();
  }

  public GuiDeploymentAlertType getDeploymentAlertType(String depAlertTypeId)
      throws EntityNotFoundException, IOException {
    DeploymentAlertType depAlertType = alertTypeConfiguration
        .getDeploymentAlertType(depAlertTypeId);
    if (depAlertType == null) {
      throw new EntityNotFoundException("DeploymentAlertType", depAlertTypeId);
    }

    return createGuiDeploymentAlertType(depAlertType);
  }

  private GuiDeploymentAlertType createGuiDeploymentAlertType(DeploymentAlertType depAlertType)
      throws IOException {
    GuiDeploymentAlertType guiType = new GuiDeploymentAlertType();
    guiType.setDeploymentAlertTypeId(depAlertType.getDeploymentAlertTypeId());
    guiType.setName(depAlertType.getName());
    guiType.setDescription(depAlertType.getDescription());

    AlertType baseAlertType = alertTypeConfiguration
        .getBaseAlertType(depAlertType.getAlertTypeId());

    //1. Extract user editable logicOverrides based on presets and cleared presets
    Set<String> validPresetIds = populateGuiLogicOverrides(guiType, baseAlertType, depAlertType);

    //2. Extract user editable parameters based on presets, cleared presets
    // and Logic overrides populated above
    populateGuiParameters(guiType, baseAlertType, depAlertType, validPresetIds);

    return guiType;
  }

  private Set<String> populateGuiLogicOverrides(GuiDeploymentAlertType guiType,
      AlertType baseAlertType, DeploymentAlertType depAlertType) {
    List<LogicOverride> guiLogicOverrides = new ArrayList<>();

    Set<LogicOverridePreset> validLogicPresets = getValidLogicOverridePresets(baseAlertType,
        depAlertType);

    //Extract preset Ids
    Set<String> validPresetIds = validLogicPresets.stream()
        .map(LogicOverridePreset::getLogicOverrideId).collect(Collectors.toSet());

    for (LogicOverride logicOverride : baseAlertType.getLogicOverrides()) {
      if (isUserEditable(logicOverride.getLogicOverrideId(), validPresetIds,
          depAlertType.getClearedLogicOverridePresets())) {
        guiLogicOverrides.add(logicOverride);
      }
    }
    guiType.setLogicOverrides(guiLogicOverrides);
    return validPresetIds;
  }

  //Override the LogicPresets defined in baseAlertType with LogicPresets in DeploymentAlertType
  //Return only valid LogicPresets ie. presetValue = TRUE
  private Set<LogicOverridePreset> getValidLogicOverridePresets(AlertType baseAlertType,
      DeploymentAlertType depAlertType) {
    Map<String, LogicOverridePreset> presetMap = new HashMap<>();
    if (!CollectionUtils.isEmpty(baseAlertType.getLogicOverridePresets())) {
      presetMap.putAll(baseAlertType.getLogicOverridePresets().stream()
          .collect(Collectors.toMap(LogicOverridePreset::getLogicOverrideId, Function.identity())));
    }

    //Override baseAlertType LogicPresets with DeploymentAlertType LogicPresets
    if (!CollectionUtils.isEmpty(depAlertType.getLogicOverridePresets())) {
      presetMap.putAll(depAlertType.getLogicOverridePresets().stream()
          .collect(Collectors.toMap(LogicOverridePreset::getLogicOverrideId, Function.identity())));
    }

    //Extract only valid presets
    Set<LogicOverridePreset> validPresets = new HashSet<>();
    validPresets.addAll(presetMap.values().stream()
        .filter(logicPreset -> Boolean.TRUE.equals(logicPreset.isPresetValue()))
        .filter(logicPreset -> !depAlertType.getClearedLogicOverridePresets()
            .contains(logicPreset.getLogicOverrideId()))
        .collect(Collectors.toSet()));

    return validPresets;
  }

  public Map<String, Boolean> getValidLogicOverridePresets(String depAlertTypeId)
      throws IOException {
    DeploymentAlertType depAlertType = alertTypeConfiguration
        .getDeploymentAlertType(depAlertTypeId);
    AlertType baseAlertType = alertTypeConfiguration
        .getBaseAlertType(depAlertType.getAlertTypeId());
    Set<LogicOverridePreset> presets = getValidLogicOverridePresets(baseAlertType, depAlertType);
    Map<String, Boolean> presetValues = new HashMap<>();
    for (LogicOverridePreset preset : presets) {
      presetValues.put(preset.getLogicOverrideId(), preset.isPresetValue());
    }
    return presetValues;
  }

  private void populateGuiParameters(GuiDeploymentAlertType guiType, AlertType baseAlertType,
      DeploymentAlertType depAlertType, Set<String> validLogicPresetIds) {
    List<Parameter> guiParams = new ArrayList<>();

    Set<ParameterPreset> parameterPresets = getValidParameterPresets(baseAlertType, depAlertType);

    Set<String> parameterPresetIds = parameterPresets.stream()
        .map(ParameterPreset::getParameterId)
        .collect(Collectors.toSet());

    //Filter out irrelevant parameters based on logic overrides already valid
    Set<String> irrelevantParams = getIrrelevantParameters(validLogicPresetIds,
        baseAlertType.getLogicOverrides());

    for (Parameter param : baseAlertType.getParameters()) {
      if (!irrelevantParams.contains(param.getParameterId()) &&
          isUserEditable(param.getParameterId(), parameterPresetIds,
              depAlertType.getClearedParameterPresets())) {
        guiParams.add(param);
      }
    }
    guiType.setParameters(guiParams);
  }

  //Override the ParameterPresets in baseAlertType with ParameterPresets in DeploymentAlertType
  private Set<ParameterPreset> getValidParameterPresets(AlertType baseAlertType,
      DeploymentAlertType depAlertType) {
    Set<ParameterPreset> parameterPresets = new HashSet<>();
    Map<String, ParameterPreset> presetMap = new HashMap<>();
    if (!CollectionUtils.isEmpty(baseAlertType.getParameterPresets())) {
      presetMap.putAll(baseAlertType.getParameterPresets().stream()
          .collect(Collectors.toMap(ParameterPreset::getParameterId, Function.identity())));
    }
    if (!CollectionUtils.isEmpty(depAlertType.getParameterPresets())) {
      presetMap.putAll(depAlertType.getParameterPresets().stream()
          .collect(Collectors.toMap(ParameterPreset::getParameterId, Function.identity())));
    }

    parameterPresets.addAll(presetMap.values());
    return parameterPresets;
  }

  public Map<String, String> getValidParameterPresets(String depAlertTypeId) throws IOException {
    DeploymentAlertType depAlertType = alertTypeConfiguration
        .getDeploymentAlertType(depAlertTypeId);
    AlertType baseAlertType = alertTypeConfiguration
        .getBaseAlertType(depAlertType.getAlertTypeId());
    Set<ParameterPreset> presets = getValidParameterPresets(baseAlertType, depAlertType);
    Map<String, String> presetValues = new HashMap<>();
    for (ParameterPreset preset : presets) {
      Optional<Parameter>
          param = baseAlertType.getParameters().stream()
          .filter(p -> p.getParameterId().equals(preset.getParameterId())).findFirst();
      if (param.isPresent()) {
        presetValues.put(preset.getParameterId(), extractPresetValue(preset,
            param.get().getDataType()));
      }
    }
    return presetValues;
  }

  private String extractPresetValue(ParameterPreset preset, DataTypeEnum dataType) {
    switch (dataType) {
      case BOOLEAN:
        return preset.isPresetBooleanValue().toString();
      case INTEGER:
        return preset.getPresetIntegerValue().toString();
      case STRING:
        return preset.getPresetStringValue();
      default:
        return preset.getPresetNumericValue().toString();
    }
  }


  private Set<String> getIrrelevantParameters(Set<String> validLogicPresetIds,
      List<LogicOverride> overrides) {
    Set<String> irrelevantParams = new HashSet<>();
    Set<LogicOverride> logicOverridesSet = new HashSet<>();
    if (!CollectionUtils.isEmpty(overrides)) {
      logicOverridesSet.addAll(overrides);
    }

    for (String logicPresetId : validLogicPresetIds) {
      irrelevantParams.addAll(overrides.stream()
          .filter(lo -> lo.getLogicOverrideId().equals(logicPresetId))
          .map(LogicOverride::getIrrelevantParameters).flatMap(
              Collection::stream).collect(Collectors.toSet()));
    }
    return irrelevantParams;
  }


  private <T extends String> boolean isUserEditable(String id, Set<T> allPresets,
      List<String> clearedPresets) {
    boolean userEditable = false;
    if (!allPresets.contains(id)) {
      userEditable = true;
    } else {
      if (clearedPresets.contains(id)) {
        userEditable = true;
      }
    }
    return userEditable;
  }

}
