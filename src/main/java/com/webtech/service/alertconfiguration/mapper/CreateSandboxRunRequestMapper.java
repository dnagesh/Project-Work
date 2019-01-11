package com.webtech.service.alertconfiguration.mapper;

import com.webtech.service.alertconfiguration.dto.SandboxRunDTO;
import com.irisium.service.alertconfiguration.model.CreateSandboxRunRequest;
import com.webtech.service.common.Transformer;
import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class CreateSandboxRunRequestMapper implements
        Transformer<CreateSandboxRunRequest, SandboxRunDTO> {

  @Override
  public SandboxRunDTO transform(CreateSandboxRunRequest source) {

    SandboxRunDTO sandboxRunDTO = null;
    if (source != null) {
      sandboxRunDTO = new SandboxRunDTO();
      sandboxRunDTO.setStartTime(Instant.now());
      sandboxRunDTO.setDataFrom(source.getDataFromTime());
      sandboxRunDTO.setDataTo(source.getDataToTime());
    }
    return sandboxRunDTO;
  }

}

