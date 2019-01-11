package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.webtech.service.alertconfiguration.dto.SandboxRunDTO;
import com.irisium.service.alertconfiguration.model.SandboxRun;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxRunDTOMapperTest {

  private SandboxRunDTOMapper sandboxRunMapper;

  @Before
  public void setup() {
    sandboxRunMapper = new SandboxRunDTOMapper();
  }

  @Test
  public void tranformWhenRequestIsNull() {
    SandboxRun sandboxRun = sandboxRunMapper.transform(null);
    assertThat(sandboxRun).isNull();
  }

  @Test
  public void tranformWhenRequestIsNotNull() {
    SandboxRunDTO dto = new SandboxRunDTO();
    SandboxRun sandboxRun = sandboxRunMapper.transform(dto);
    assertThat(sandboxRun).isNotNull();
  }

}
