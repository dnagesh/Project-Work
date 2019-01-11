package com.webtech.service.alerttype.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.irisium.service.alertDefinition.model.AlertType;
import com.irisium.service.alertDefinition.model.DeploymentAlertType;
import com.irisium.service.alertDefinition.model.GuiDeploymentAlertType;
import com.irisium.service.alertDefinition.model.LogicOverride;
import com.irisium.service.alertDefinition.model.LogicOverridePreset;
import com.irisium.service.alertDefinition.model.Parameter;
import com.irisium.service.alertDefinition.model.Parameter.DataTypeEnum;
import com.irisium.service.alertDefinition.model.ParameterPreset;
import com.webtech.service.alerttype.TestObjects;
import com.webtech.service.alerttype.config.BaseAlertTypeConfiguration;
import com.webtech.service.common.exception.EntityNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AlertTypeServiceTest {

  @Mock
  private BaseAlertTypeConfiguration mockConfig;

  private AlertTypeService alertTypeService;

  private Map<String, DeploymentAlertType> mockDepAlertTypes;

  private Map<String, AlertType> mockAlertTypes;
  private String depAlertTypeId;
  private DeploymentAlertType deploymentAlertType;
  private AlertType baseAlertType;

  private List<Parameter> paramList = new ArrayList<>();
  private List<LogicOverride> overrideList = new ArrayList<>();
  private List<ParameterPreset> baseAlertParamPresets = new ArrayList<>();
  private List<LogicOverridePreset> baseAlertLogicPresets = new ArrayList<>();
  private List<ParameterPreset> depAlertParamPresets = new ArrayList<>();
  private List<LogicOverridePreset> depAlertLogicPresets = new ArrayList<>();

  @Before
  public void setUp() throws Exception {
    alertTypeService = new AlertTypeService(mockConfig);
    mockDepAlertTypes = TestObjects.getDeploymentAlertTypes();
    when(mockConfig.getDeploymentAlertTypes()).thenReturn(mockDepAlertTypes);

    mockAlertTypes = TestObjects.getAlertTypes();
    when(mockConfig.getBaseAlertTypes()).thenReturn(mockAlertTypes);

    overrideList.add(TestObjects.getLogicOverride(Integer.toString(1), 1, 2));
    overrideList.add(TestObjects.getLogicOverride(Integer.toString(2), 3, 4));
    overrideList.add(TestObjects.getLogicOverride(Integer.toString(3), 5, 6));
    overrideList.add(TestObjects.getLogicOverride(Integer.toString(4), 7, 8));

    paramList.add(TestObjects.getParameter("1", DataTypeEnum.INTEGER));
    paramList.add(TestObjects.getParameter("2", DataTypeEnum.STRING));
    paramList.add(TestObjects.getParameter("3", DataTypeEnum.BOOLEAN));
    paramList.add(TestObjects.getParameter("4", DataTypeEnum.NUMERIC));
    IntStream.rangeClosed(5, 8)
        .forEach(i -> paramList
            .add(TestObjects.getParameter(Integer.toString(i), DataTypeEnum.INTEGER)));
    baseAlertParamPresets.add(TestObjects.getParameterPreset("1", 1));
    baseAlertParamPresets.add(TestObjects.getParameterPreset("2", "2"));
    baseAlertParamPresets.add(TestObjects.getParameterPreset("3", Boolean.TRUE));
    baseAlertParamPresets.add(TestObjects.getParameterPreset("4", 4d));

    baseAlertLogicPresets.add(TestObjects.getLogicOverridePreset("3", true));

    depAlertParamPresets.add(TestObjects.getParameterPreset("1", 11));
    depAlertParamPresets.add(TestObjects.getParameterPreset("3", Boolean.FALSE));
    depAlertParamPresets.add(TestObjects.getParameterPreset("5", 55));

    depAlertLogicPresets.add(TestObjects.getLogicOverridePreset("4", true));

    baseAlertType = TestObjects
        .getAlertType(paramList, baseAlertParamPresets, overrideList, baseAlertLogicPresets);
    when(mockConfig.getBaseAlertType(baseAlertType.getAlertTypeId())).thenReturn(baseAlertType);

    depAlertTypeId = TestUtils.randomUUID().toString();
    deploymentAlertType = TestObjects.getDeploymentAlertType(depAlertTypeId,
        baseAlertType.getAlertTypeId(), depAlertParamPresets, depAlertLogicPresets);
    when(mockConfig.getDeploymentAlertType(depAlertTypeId)).thenReturn(deploymentAlertType);


  }

  @Test
  public void shouldReturnAvailableDepAlertTypes() throws IOException {
    Map<String, String> result = alertTypeService.getDeploymentAlertTypes();
    assertThat(result).isNotNull();
    assertThat(result).hasSize(mockDepAlertTypes.size());
    mockDepAlertTypes.keySet().parallelStream().forEach(key -> {
      assertThat(result.containsKey(key));
      assertThat(result.get(key)).isEqualTo(mockDepAlertTypes.get(key).getName());
    });
  }

  @Test
  public void shouldReturnAvailableBaseAlertTypes() throws IOException {
    Map<String, AlertType> result = alertTypeService.getAlertTypes();
    assertThat(result).isNotNull();
    assertThat(result).hasSize(mockAlertTypes.size());
    mockAlertTypes.keySet().parallelStream().forEach(key -> {
      assertThat(result.containsKey(key));
      assertThat(result.get(key)).isEqualTo(mockAlertTypes.get(key));
    });
  }


  @Test
  public void shouldThrowEntityNotFoundWhenNoDeploymentAlertTypeFound() {
    assertThatThrownBy(() -> alertTypeService.getDeploymentAlertType(
        TestUtils.randomAlphanumeric(5))).isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  public void shouldReturnGuiDepTypeWithoutPresetsWhenDeploymentAlertTypeFound() throws Throwable {
    GuiDeploymentAlertType result = alertTypeService.getDeploymentAlertType(depAlertTypeId);

    assertThat(result).isNotNull();
    assertThat(result.getDeploymentAlertTypeId())
        .isEqualTo(deploymentAlertType.getDeploymentAlertTypeId());
    assertThat(result.getName()).isEqualTo(deploymentAlertType.getName());
    assertThat(result.getDescription()).isEqualTo(deploymentAlertType.getDescription());

    assertThat(result.getParameters()).isNotNull();
    assertThat(result.getParameters()).hasSize(0);

    //Should not return logicOverrideId 4 as its preset to true in dep type
    assertThat(result.getLogicOverrides()).isNotNull();
    assertThat(result.getLogicOverrides()).hasSize(2);
    assertThat(
        result.getLogicOverrides().stream().anyMatch(lo -> lo.getLogicOverrideId().equals("3")))
        .isFalse();
    assertThat(
        result.getLogicOverrides().stream().anyMatch(lo -> lo.getLogicOverrideId().equals("4")))
        .isFalse();
  }

  @Test
  public void shouldReturnPresetParamsAndLogicOverridesWhenCleared() throws Throwable {
    deploymentAlertType.setClearedParameterPresets(Arrays.asList("1"));

    //DeploymentAlertType LogicOverridePresets is mutually exclusive to ClearedLogicOverridePresets.
    //They must not contain same logicOverrides
    deploymentAlertType.setClearedLogicOverridePresets(Arrays.asList("3"));

    GuiDeploymentAlertType result = alertTypeService.getDeploymentAlertType(depAlertTypeId);

    assertThat(result).isNotNull();
    assertThat(result.getDeploymentAlertTypeId())
        .isEqualTo(deploymentAlertType.getDeploymentAlertTypeId());
    assertThat(result.getName()).isEqualTo(deploymentAlertType.getName());
    assertThat(result.getDescription()).isEqualTo(deploymentAlertType.getDescription());

    assertThat(result.getParameters()).isNotNull();
    assertThat(result.getParameters()).hasSize(2);
    List<String> validParams = result.getParameters().stream()
        .map(Parameter::getParameterId).collect(Collectors.toList());
    assertThat(validParams.contains("1")).isTrue();
    assertThat(validParams.contains("6")).isTrue();

    assertThat(result.getLogicOverrides()).isNotNull();
    assertThat(result.getLogicOverrides()).hasSize(3);
    List<String> validLogics = result.getLogicOverrides().stream()
        .map(LogicOverride::getLogicOverrideId).collect(Collectors.toList());
    assertThat(validLogics.contains("1")).isTrue();
    assertThat(validLogics.contains("2")).isTrue();
    assertThat(validLogics.contains("3")).isTrue();
    assertThat(validLogics.contains("4")).isFalse();
  }

  @Test
  public void shouldReturnAllParamsAndLogicOverridesWhenNoPresets() throws Throwable {
    deploymentAlertType.getClearedParameterPresets().clear();
    deploymentAlertType.getLogicOverridePresets().clear();
    deploymentAlertType.getParameterPresets().clear();
    baseAlertType.getParameterPresets().clear();
    baseAlertType.getLogicOverridePresets().clear();

    GuiDeploymentAlertType result = alertTypeService.getDeploymentAlertType(depAlertTypeId);

    assertThat(result).isNotNull();
    assertThat(result.getDeploymentAlertTypeId())
        .isEqualTo(deploymentAlertType.getDeploymentAlertTypeId());
    assertThat(result.getName()).isEqualTo(deploymentAlertType.getName());
    assertThat(result.getDescription()).isEqualTo(deploymentAlertType.getDescription());

    assertThat(result.getParameters()).isNotNull();
    assertThat(result.getParameters()).hasSize(8);
    List<String> validParams = result.getParameters().stream()
        .map(Parameter::getParameterId).collect(Collectors.toList());
    IntStream.rangeClosed(1, 8)
        .forEach(i -> assertThat(validParams.contains(Integer.toString(i))).isTrue());

    assertThat(result.getLogicOverrides()).isNotNull();
    assertThat(result.getLogicOverrides()).hasSize(4);
    List<String> validLogics = result.getLogicOverrides().stream()
        .map(LogicOverride::getLogicOverrideId).collect(Collectors.toList());
    assertThat(validLogics.contains("1")).isTrue();
    assertThat(validLogics.contains("2")).isTrue();
    assertThat(validLogics.contains("3")).isTrue();
    assertThat(validLogics.contains("4")).isTrue();
  }

  @Test
  public void shouldReturnAllParamsWhenNoPresetsAndLogicOverrides() throws Throwable {
    deploymentAlertType.getClearedParameterPresets().clear();
    deploymentAlertType.getParameterPresets().clear();
    baseAlertType.getParameterPresets().clear();
    baseAlertType.getLogicOverridePresets().clear();

    baseAlertType.getLogicOverrides().clear();

    GuiDeploymentAlertType result = alertTypeService.getDeploymentAlertType(depAlertTypeId);

    assertThat(result).isNotNull();
    assertThat(result.getDeploymentAlertTypeId())
        .isEqualTo(deploymentAlertType.getDeploymentAlertTypeId());
    assertThat(result.getName()).isEqualTo(deploymentAlertType.getName());
    assertThat(result.getDescription()).isEqualTo(deploymentAlertType.getDescription());

    assertThat(result.getParameters()).isNotNull();
    assertThat(result.getParameters()).hasSize(8);
    List<String> validParams = result.getParameters().stream()
        .map(Parameter::getParameterId).collect(Collectors.toList());
    IntStream.rangeClosed(1, 8)
        .forEach(i -> assertThat(validParams.contains(Integer.toString(i))).isTrue());

    assertThat(result.getLogicOverrides()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnIrrelevantParamsWhenLogicOverridePresetCleared() throws Throwable {
    baseAlertParamPresets.clear();
    baseAlertType = TestObjects
        .getAlertType(paramList, baseAlertParamPresets, overrideList, baseAlertLogicPresets);
    when(mockConfig.getBaseAlertType(baseAlertType.getAlertTypeId())).thenReturn(baseAlertType);

    depAlertTypeId = TestUtils.randomUUID().toString();
    depAlertParamPresets.clear();
    depAlertLogicPresets.clear();
    deploymentAlertType = TestObjects.getDeploymentAlertType(depAlertTypeId,
        baseAlertType.getAlertTypeId(), depAlertParamPresets, depAlertLogicPresets);
    deploymentAlertType.setClearedLogicOverridePresets(Arrays.asList("3"));
    when(mockConfig.getDeploymentAlertType(depAlertTypeId)).thenReturn(deploymentAlertType);

    GuiDeploymentAlertType result = alertTypeService.getDeploymentAlertType(depAlertTypeId);

    assertThat(result).isNotNull();
    assertThat(result.getDeploymentAlertTypeId())
        .isEqualTo(deploymentAlertType.getDeploymentAlertTypeId());
    assertThat(result.getName()).isEqualTo(deploymentAlertType.getName());
    assertThat(result.getDescription()).isEqualTo(deploymentAlertType.getDescription());

    assertThat(result.getParameters()).isNotNull();
    assertThat(result.getParameters()).hasSize(8);
    List<String> validParams = result.getParameters().stream()
        .map(Parameter::getParameterId).collect(Collectors.toList());
    IntStream.rangeClosed(1, 8)
        .forEach(i -> assertThat(validParams.contains(Integer.toString(i))).isTrue());

    assertThat(result.getLogicOverrides()).isNotNull();
    assertThat(result.getLogicOverrides()).hasSize(4);
  }

  @Test
  public void shouldReturnValidParamPresetsForDepAlertType() throws Throwable {
    Map<String, String> result = alertTypeService.getValidParameterPresets(depAlertTypeId);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(5);
  }

  @Test
  public void shouldNotReturnParamPresetsIfNotValidParameter() throws Throwable {
    baseAlertType.getParameters().remove(TestObjects.getParameter("1", DataTypeEnum.INTEGER));
    Map<String, String> result = alertTypeService.getValidParameterPresets(depAlertTypeId);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(4);
  }


  @Test
  public void shouldReturnValidLogicPresetsForDepAlertType() throws Throwable {
    Map<String, Boolean> result = alertTypeService.getValidLogicOverridePresets(depAlertTypeId);

    assertThat(result).isNotNull();
    assertThat(result).hasSize(2);
  }

}
