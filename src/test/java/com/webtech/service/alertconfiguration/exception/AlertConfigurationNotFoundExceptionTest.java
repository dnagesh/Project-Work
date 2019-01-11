package com.webtech.service.alertconfiguration.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import org.junit.Test;

public class AlertConfigurationNotFoundExceptionTest {

  private AlertConfigurationNotFoundException exception;

  @Test
  public void shouldInitCorrectlyWhenConstructed() {
    String uuid = TestUtils.randomUUID().toString();
    exception = new AlertConfigurationNotFoundException(uuid);
    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isNotBlank();
    assertThat(exception.getMessage()).contains(uuid);
  }
}
