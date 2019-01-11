package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.webtech.service.alertconfiguration.dto.SandboxRunDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateSandboxRunRequestMapperTest {

  private CreateSandboxRunRequestMapper createSandboxRunRequestMapper;

  @Before
  public void setup() {
    createSandboxRunRequestMapper = new CreateSandboxRunRequestMapper();
  }

  @Test
  public void tranformWhenRequestIsNull() {
    SandboxRunDTO dto = createSandboxRunRequestMapper.transform(null);
    assertThat(dto).isNull();
  }

}
