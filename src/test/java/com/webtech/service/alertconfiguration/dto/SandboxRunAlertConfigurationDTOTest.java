package com.webtech.service.alertconfiguration.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import java.util.UUID;
import org.junit.Test;
import org.meanbean.test.BeanTester;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

public class SandboxRunAlertConfigurationDTOTest {

  @Test
  public void behavesAsBean() {
    Configuration configuration = new ConfigurationBuilder()
        .overrideFactory("sandboxAlertConfigurationUUID", TestUtils::randomUUID).build();
    new BeanTester().testBean(SandboxRunAlertConfigurationDTO.class, configuration);
  }

  @Test
  public void parameterisedConstructor() {
    UUID sandboxRunAlertConfigurationId = TestUtils.randomUUID();
    String name = "Sandbox 123";
    String appHash = "Sandbox 123#";
    SandboxRunAlertConfigurationDTO sandboxRunAlertConfigurationDTO = new SandboxRunAlertConfigurationDTO(
        sandboxRunAlertConfigurationId, name, appHash);
    assertThat(sandboxRunAlertConfigurationDTO).isNotNull();
    assertThat(sandboxRunAlertConfigurationDTO.getSandboxAlertConfigurationUUID().toString())
        .isEqualTo(sandboxRunAlertConfigurationId.toString());
    assertThat(sandboxRunAlertConfigurationDTO.getName()).isEqualTo(name);
    assertThat(sandboxRunAlertConfigurationDTO.getAppHash()).isEqualTo(appHash);
  }
}
