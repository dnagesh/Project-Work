package com.webtech.service.alertconfiguration.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;

public class AlertParameterSetDTOTest {

  private Map<String, String> parameters;
  private Set<String> alertAggregationFields;
  private Set<AlertFilterDTO> alertFilterDTOS;
  private Map<String, Boolean> logicOverrides;
  private String alertLogicType;
  private String businessUnit;

  @Before
  public void setUp() throws Exception {
    parameters = new HashMap<>();
    parameters.put(TestUtils.randomAlphanumeric(5), TestUtils.randomAlphanumeric(10));

    alertAggregationFields = new HashSet<>();
    alertAggregationFields.add(TestUtils.randomAlphanumeric(5));

    AlertFilterDTO filter1 = new AlertFilterDTO();
    filter1.setAggregationFieldName("AggregationField2");
    filter1.setOperation("IN");
    filter1.setValues(new HashSet<>(Arrays.asList("v1", "v2")));
    AlertFilterDTO filter2 = new AlertFilterDTO();
    filter2.setAggregationFieldName("AggregationField1");
    filter2.setOperation("NOT IN");
    filter2.setValues(new HashSet<>(Arrays.asList("v1", "v2")));
    alertFilterDTOS = new HashSet<>(Arrays.asList(filter1, filter2, null));

    logicOverrides = new HashMap<>();
    logicOverrides.put(TestUtils.randomAlphanumeric(5), true);

    alertLogicType = TestUtils.randomAlphanumeric(10);
    businessUnit = TestUtils.randomAlphanumeric(10);
  }

  @Test
  public void parameterisedConstructor() throws Throwable {

    AlertParameterSetDTO alertParameterSetDTO = new AlertParameterSetDTO(alertLogicType,
        businessUnit, parameters,
        alertAggregationFields,
        alertFilterDTOS, logicOverrides);
    assertThat(alertParameterSetDTO).isNotNull();
    assertThat(alertParameterSetDTO.getAlertLogicType()).isNotBlank();
    assertThat(alertParameterSetDTO.getBusinessUnit()).isNotBlank();
    assertThat(alertParameterSetDTO.getAlertParameters()).isEqualTo(parameters);
    assertThat(alertParameterSetDTO.getAlertAggregationFields()).isEqualTo(alertAggregationFields);
    assertThat(alertParameterSetDTO.getLogicOverrides()).isEqualTo(logicOverrides);
    assertThat(alertParameterSetDTO.getAlertFilterDTOS()).hasSize(alertFilterDTOS.size() - 1);
    assertThat(alertParameterSetDTO.getApsHash()).isNotBlank();
    assertThat(alertParameterSetDTO.toString()).isNotBlank();
  }

  @Test
  public void parameterisedConstructorWithNullValues() throws Throwable {
    String alertLogicType = TestUtils.randomAlphanumeric(10);

    AlertParameterSetDTO alertParameterSetDTO = new AlertParameterSetDTO(alertLogicType, null,
        null, null, null, null);
    assertThat(alertParameterSetDTO).isNotNull();
    assertThat(alertParameterSetDTO.getApsHash()).isNotBlank();
    assertThat(alertParameterSetDTO.getAlertParameters()).isNullOrEmpty();
    assertThat(alertParameterSetDTO.getLogicOverrides()).isNullOrEmpty();
    assertThat(alertParameterSetDTO.getAlertFilterDTOS()).isNullOrEmpty();
    assertThat(alertParameterSetDTO.getAlertAggregationFields()).isNullOrEmpty();
  }

  @Test
  public void shouldCreateSameHashForDifferentlyOrderedSameParameters() throws Throwable {
    String key1 = TestUtils.randomAlphanumeric(5);
    String value1 = TestUtils.randomAlphanumeric(5);
    String key2 = TestUtils.randomAlphanumeric(5);
    String value2 = TestUtils.randomAlphanumeric(5);
    parameters = new HashMap<>();
    parameters.put(key1, value1);
    parameters.put(key2, value2);
    parameters.put(key2, value2);

    String field1 = TestUtils.randomAlphanumeric(5);
    String field2 = TestUtils.randomAlphanumeric(5);
    String field3 = TestUtils.randomAlphanumeric(5);
    String field4 = TestUtils.randomAlphanumeric(5);
    alertAggregationFields = new HashSet<>();
    alertAggregationFields.addAll(Arrays.asList(field1, field2, field3, field4));

    AlertFilterDTO filter1 = new AlertFilterDTO();
    filter1.setAggregationFieldName("AgggregationField2");
    filter1.setOperation("EQ");
    filter1.setValues(new HashSet<>(Arrays.asList("v1", "v2")));
    AlertFilterDTO filter2 = new AlertFilterDTO();
    filter2.setAggregationFieldName("AgggregationField1");
    filter2.setOperation("EQ");
    filter2.setValues(new HashSet<>(Arrays.asList("v1", "v2")));
    alertFilterDTOS = new HashSet<>(Arrays.asList(filter1, filter2));

    String alertLogicType = TestUtils.randomAlphanumeric(10);

    logicOverrides.put("logic1", true);

    AlertParameterSetDTO alertParameterSetDTO1 = new AlertParameterSetDTO(alertLogicType,
        businessUnit,
        parameters,
        alertAggregationFields, alertFilterDTOS, logicOverrides);

    //Shuffle order of the collections
    parameters.clear();
    parameters.put(key2, value2);
    parameters.put(key2, value2);
    parameters.put(key1, value1);
    alertAggregationFields.clear();
    alertAggregationFields.addAll(Arrays.asList(field3, field4, field1, field2));
    alertFilterDTOS.clear();
    alertFilterDTOS.addAll(Arrays.asList(filter2, filter1));

    AlertParameterSetDTO alertParameterSetDTO2 = new AlertParameterSetDTO(alertLogicType,
        businessUnit,
        parameters,
        alertAggregationFields, alertFilterDTOS, logicOverrides);

    assertThat(alertParameterSetDTO1).isNotNull();
    assertThat(alertParameterSetDTO2).isNotNull();
    assertThat(alertParameterSetDTO1.getAlertLogicType()).isNotBlank();
    assertThat(alertParameterSetDTO1.getApsHash()).isNotBlank();
    assertThat(alertParameterSetDTO1.getApsHash()).isEqualTo(alertParameterSetDTO2.getApsHash());
  }


}
