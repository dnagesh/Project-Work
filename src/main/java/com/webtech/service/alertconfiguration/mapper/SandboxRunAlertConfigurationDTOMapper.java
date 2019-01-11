package com.webtech.service.alertconfiguration.mapper;

import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunAlertConfigurationDTO;
import com.webtech.service.common.Transformer;
import org.springframework.stereotype.Component;

@Component
public class SandboxRunAlertConfigurationDTOMapper implements
    Transformer<SandboxAlertConfigurationDTO, SandboxRunAlertConfigurationDTO> {

  @Override
  public SandboxRunAlertConfigurationDTO transform(SandboxAlertConfigurationDTO source) {

    SandboxRunAlertConfigurationDTO sandboxRunAlertConfigurationDTO = null;
    if (source != null) {
      sandboxRunAlertConfigurationDTO = new SandboxRunAlertConfigurationDTO();
      sandboxRunAlertConfigurationDTO
          .setSandboxAlertConfigurationUUID(source.getPrimaryKey().getAlertConfigurationUUID());
      sandboxRunAlertConfigurationDTO.setName(source.getName());
      sandboxRunAlertConfigurationDTO.setAppHash(source.getApsHash());
    }
    return sandboxRunAlertConfigurationDTO;
  }

}

