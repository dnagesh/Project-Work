package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.webtech.service.alertconfiguration.dto.SandboxRunAlertConfigurationDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxRunAlertConfigurationDTOMapperTest {

  private SandboxRunAlertConfigurationDTOMapper sandboxRunAlertConfigMapper;

  @Before
  public void setup() {
    sandboxRunAlertConfigMapper = new SandboxRunAlertConfigurationDTOMapper();
  }

  @Test
  public void tranformWhenRequestIsNull() {
    SandboxRunAlertConfigurationDTO sandboxRunAlertConfigurationDTO = sandboxRunAlertConfigMapper
        .transform(null);
    assertThat(sandboxRunAlertConfigurationDTO).isNull();
  }

}
