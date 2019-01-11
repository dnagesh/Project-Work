package com.webtech.service.alertconfiguration.mapper;

import com.datastax.driver.core.utils.UUIDs;
import com.webtech.service.alertconfiguration.dto.SandboxDTO;
import com.irisium.service.alertconfiguration.model.CreateSandboxRequest;
import com.irisium.service.alertconfiguration.model.Sandbox.StatusEnum;
import com.webtech.service.common.Transformer;
import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class CreateSandboxRequestMapper implements Transformer<CreateSandboxRequest, SandboxDTO> {

  @Override
  public SandboxDTO transform(CreateSandboxRequest source) {

    SandboxDTO sandboxDTO = null;
    if (source != null) {
      sandboxDTO = new SandboxDTO();
      sandboxDTO.setName(source.getName());
      sandboxDTO.setCreatedWhen(Instant.now());
      sandboxDTO.setStatus(StatusEnum.ACTIVE.toString());
      sandboxDTO.setUuid(UUIDs.timeBased());
    }
    return sandboxDTO;
  }

}

