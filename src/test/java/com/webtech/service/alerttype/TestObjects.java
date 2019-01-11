package com.webtech.service.alerttype;


import com.irisium.TestUtils;
import com.irisium.service.alertDefinition.model.AlertType;
import com.irisium.service.alertDefinition.model.DeploymentAlertType;
import com.irisium.service.alertDefinition.model.LogicOverride;
import com.irisium.service.alertDefinition.model.LogicOverridePreset;
import com.irisium.service.alertDefinition.model.Parameter;
import com.irisium.service.alertDefinition.model.Parameter.DataTypeEnum;
import com.irisium.service.alertDefinition.model.ParameterPreset;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class TestObjects {

  public static Map<String, DeploymentAlertType> getDeploymentAlertTypes() {
    Map<String, DeploymentAlertType> mockDepAlertTypes = new HashMap<>();
    DeploymentAlertType deploymentAlertType = getDepAlertType(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5));
    mockDepAlertTypes.put(deploymentAlertType.getAlertTypeId(), deploymentAlertType);
    deploymentAlertType = getDepAlertType(TestUtils.randomUUID(), TestUtils.randomAlphanumeric(5));
    mockDepAlertTypes.put(deploymentAlertType.getAlertTypeId(), deploymentAlertType);

    return mockDepAlertTypes;
  }

  private static DeploymentAlertType getDepAlertType(UUID randomUUID, String name) {
    DeploymentAlertType deploymentAlertType = new DeploymentAlertType();
    deploymentAlertType.setAlertTypeId(randomUUID.toString());
    deploymentAlertType.setName(name);
    return deploymentAlertType;
  }

  public static Map<String, AlertType> getAlertTypes() {
    Map<String, AlertType> mockAlertTypes = new HashMap<>();
    AlertType alertType = getAlertType(TestUtils.randomUUID(), TestUtils.randomAlphanumeric(5));
    mockAlertTypes.put(alertType.getAlertTypeId(), alertType);
    alertType = getAlertType(TestUtils.randomUUID(), TestUtils.randomAlphanumeric(5));
    mockAlertTypes.put(alertType.getAlertTypeId(), alertType);
    return mockAlertTypes;
  }

  private static AlertType getAlertType(UUID randomUUID, String name) {
    AlertType alertType = new AlertType();
    alertType.setAlertTypeId(randomUUID.toString());
    alertType.setName(name);
    return alertType;
  }

  public static Map<String, String> getDeploymentTypesMap() {
    Map<String, String> map = new HashMap<>();
    map.put(TestUtils.randomUUID().toString(), TestUtils.randomAlphanumeric(5));
    map.put(TestUtils.randomUUID().toString(), TestUtils.randomAlphanumeric(5));
    return map;
  }

  public static DeploymentAlertType getDeploymentAlertType(String depAlertTypeId,
      String alertTypeId, List<ParameterPreset> depAlertParamPresets,
      List<LogicOverridePreset> depAlertLogicPresets) {
    DeploymentAlertType deploymentAlertType = new DeploymentAlertType();
    deploymentAlertType.setDeploymentAlertTypeId(depAlertTypeId);
    deploymentAlertType.setAlertTypeId(alertTypeId);
    deploymentAlertType.setName(TestUtils.randomAlphanumeric(5));
    deploymentAlertType.setDescription(TestUtils.randomAlphanumeric());

    deploymentAlertType.setParameterPresets(depAlertParamPresets);
    deploymentAlertType.setLogicOverridePresets(depAlertLogicPresets);

    deploymentAlertType.setClearedLogicOverridePresets(Collections.emptyList());
    deploymentAlertType.setClearedParameterPresets(Collections.emptyList());

    return deploymentAlertType;
  }

  public static LogicOverridePreset getLogicOverridePreset(String id, boolean value) {
    LogicOverridePreset loPreset = new LogicOverridePreset();
    loPreset.setLogicOverrideId(id);
    loPreset.setPresetValue(value);
    return loPreset;
  }

  public static ParameterPreset getParameterPreset(String id, Object value) {
    ParameterPreset preset = new ParameterPreset();
    preset.setParameterId(id);
    if (value instanceof Integer) {
      preset.setPresetIntegerValue(Integer.parseInt(value.toString()));
    } else if (value instanceof String) {
      preset.setPresetStringValue(value.toString());
    } else if (value instanceof Boolean) {
      preset.setPresetBooleanValue(Boolean.valueOf(value.toString()));
    } else {
      preset.setPresetNumericValue(new BigDecimal(value.toString()));
    }

    return preset;
  }

  public static AlertType getAlertType(List<Parameter> paramList,
      List<ParameterPreset> baseAlertParamPresets, List<LogicOverride> overrideList,
      List<LogicOverridePreset> baseAlertLogicPresets) {

    AlertType alertType = new AlertType();
    alertType.setAlertTypeId(TestUtils.randomInt(5).toString());
    alertType.setName(TestUtils.randomAlphanumeric(5));
    alertType.setDescription(TestUtils.randomAlphanumeric());
    alertType.setImplementingClass(TestUtils.randomAlphanumeric());

    alertType.setParameters(paramList);
    alertType.setParameterPresets(baseAlertParamPresets);
    alertType.setLogicOverrides(overrideList);
    alertType.setLogicOverridePresets(baseAlertLogicPresets);

    return alertType;
  }

  public static LogicOverride getLogicOverride(String loId, int irrelevantParam1,
      int irrelevantParam2) {
    LogicOverride logicOverride = new LogicOverride();
    logicOverride.setLogicOverrideId(loId);
    logicOverride.setIrrelevantParameters(
        Arrays.asList(Integer.toString(irrelevantParam1), Integer.toString(irrelevantParam2)));
    return logicOverride;
  }

  public static Parameter getParameter(String paramId, DataTypeEnum dataType) {
    Parameter parameter = new Parameter();
    parameter.setParameterId(paramId);
    parameter.setDataType(dataType);
    return parameter;
  }
}
