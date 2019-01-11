package com.webtech.service.alertconfiguration.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import org.junit.Test;

public class SandboxAlertConfigurationNotFoundExceptionTest {

  private SandboxAlertConfigurationNotFoundException exception;

  @Test
  public void shouldInitCorrectlyWhenConstructed() {
    String sandboxUUID = TestUtils.randomUUID().toString();
    String sandboxConfigUUID = TestUtils.randomUUID().toString();

    exception = new SandboxAlertConfigurationNotFoundException(sandboxUUID, sandboxConfigUUID);
    assertThat(exception).isNotNull();
    assertThat(exception.getMessage()).isNotBlank();
    assertThat(exception.getMessage()).contains(sandboxUUID);
    assertThat(exception.getMessage()).contains(sandboxConfigUUID);
  }
}
