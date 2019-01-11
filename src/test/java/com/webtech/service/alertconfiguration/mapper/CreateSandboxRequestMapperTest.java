package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.webtech.service.alertconfiguration.dto.SandboxDTO;
import com.irisium.service.alertconfiguration.model.CreateSandboxRequest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CreateSandboxRequestMapperTest {

  private CreateSandboxRequestMapper createSandboxRequestMapper;

  @Before
  public void setup() {
    createSandboxRequestMapper = new CreateSandboxRequestMapper();
  }

  @Test
  public void tranformWhenRequestIsNull() {
    SandboxDTO dto = createSandboxRequestMapper.transform(null);
    assertThat(dto).isNull();
  }

  @Test
  public void tranformWhenRequestIsNotNull() {
    CreateSandboxRequest request = new CreateSandboxRequest();
    SandboxDTO dto = createSandboxRequestMapper.transform(request);
    assertThat(dto).isNotNull();
  }

}
