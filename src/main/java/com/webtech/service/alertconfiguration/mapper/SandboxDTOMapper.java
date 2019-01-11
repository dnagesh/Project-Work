package com.webtech.service.alertconfiguration.mapper;

import com.webtech.service.alertconfiguration.dto.SandboxDTO;
import com.irisium.service.alertconfiguration.model.Sandbox;
import com.irisium.service.alertconfiguration.model.Sandbox.StatusEnum;
import com.webtech.service.common.Transformer;
import org.springframework.stereotype.Component;

@Component
public class SandboxDTOMapper implements Transformer<SandboxDTO, Sandbox> {

  @Override
  public Sandbox transform(SandboxDTO source) {

    Sandbox sandbox = null;
    if (source != null) {
      sandbox = new Sandbox();
      sandbox.setName(source.getName());
      sandbox.setCreatedBy(source.getOwner());
      sandbox.setCreatedWhen(source.getCreatedWhen());
      sandbox.setStatus(StatusEnum.fromValue(source.getStatus()));
      sandbox.setId(source.getUuid() != null ? source.getUuid().toString() : null);
    }
    return sandbox;
  }

}

