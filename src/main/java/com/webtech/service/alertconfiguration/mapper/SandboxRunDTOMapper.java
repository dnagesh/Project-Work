package com.webtech.service.alertconfiguration.mapper;

import com.webtech.service.alertconfiguration.dto.SandboxRunAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunDTO;
import com.irisium.service.alertconfiguration.model.SandboxRun;
import com.webtech.service.common.Transformer;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

@Component
public class SandboxRunDTOMapper implements Transformer<SandboxRunDTO, SandboxRun> {

  @Override
  public SandboxRun transform(SandboxRunDTO source) {

    SandboxRun run = null;
    if (source != null) {
      run = new SandboxRun();
      run.setAlertConfigurations(
          source.getAlertConfigurationSet() != null ? source.getAlertConfigurationSet().stream()
              .map(
                  SandboxRunAlertConfigurationDTO::getName)
              .collect(Collectors.toList()) : null);
      run.setCreatedBy(source.getOwner());
      run.setDataFromTime(source.getDataFrom());
      run.setDataToTime(source.getDataTo());
      run.setRunEndTime(source.getEndTime());
      run.setRunStartTime(source.getStartTime());
      run.setId(
          source.getPrimaryKey() != null ? source.getPrimaryKey().getRunUUID().toString() : null);
      run.setSandboxId(
          source.getPrimaryKey() != null ? source.getPrimaryKey().getSandboxUUID().toString()
              : null);
    }
    return run;
  }

}

