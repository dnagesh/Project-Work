package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class AlertFilterMapperTest {

  private AlertFilterMapper mapper;

  @Before
  public void setUp() throws Exception {
    mapper = new AlertFilterMapper();
  }

  @Test
  public void shouldReturnNullForNullInput() {
    assertThat(mapper.getAlertFilters(null)).isEmpty();
    assertThat(mapper.getAlertParameters(null)).isEmpty();
    assertThat(mapper.getAggregationFields(null)).isEmpty();
    assertThat(mapper.mapAlertFilterDTOToModel(null)).isNull();
  }
}
