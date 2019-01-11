package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.webtech.service.alertconfiguration.dto.SandboxDTO;
import com.irisium.service.alertconfiguration.model.Sandbox;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxDTOMapperTest {

  private SandboxDTOMapper sandboxMapper;

  @Before
  public void setup() {
    sandboxMapper = new SandboxDTOMapper();
  }

  @Test
  public void tranformWhenRequestIsNull() {
    Sandbox sandbox = sandboxMapper.transform(null);
    assertThat(sandbox).isNull();
  }

  @Test
  public void tranformWhenRequestIsNotNull() {
    SandboxDTO dto = new SandboxDTO();
    Sandbox ssandbox = sandboxMapper.transform(dto);
    assertThat(ssandbox).isNotNull();
  }

}
