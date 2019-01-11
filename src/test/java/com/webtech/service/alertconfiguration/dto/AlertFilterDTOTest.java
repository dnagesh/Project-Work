package com.webtech.service.alertconfiguration.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class AlertFilterDTOTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder().build();
    new BeanTester().testBean(AlertFilterDTO.class, configuration);
  }

  @Test
  public void parameterisedConstructor() {
    String aggregationFieldName = TestUtils.randomAlphanumeric(5);
    String operation = "IN";
    Set<String> values = new HashSet<>();
    values.add("value1");
    AlertFilterDTO alertFilterDTO = new AlertFilterDTO(aggregationFieldName, operation, values);
    assertThat(alertFilterDTO).isNotNull();
    assertThat(alertFilterDTO.getAggregationFieldName()).isEqualTo(aggregationFieldName);
    assertThat(alertFilterDTO.getOperation()).isEqualTo(operation);
    assertThat(alertFilterDTO.getValues()).isEqualTo(values);
  }
}
