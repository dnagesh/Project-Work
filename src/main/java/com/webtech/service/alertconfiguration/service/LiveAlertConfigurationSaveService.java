package com.webtech.service.alertconfiguration.service;

import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTOPrimaryKey;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.SandboxAlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.mapper.LiveAlertConfigurationObjectMapper;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.UpdateStatus;
import com.webtech.service.alertconfiguration.repository.AlertParameterSetRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationRepository;
import com.webtech.service.alertconfiguration.repository.SandboxAlertConfigurationRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class LiveAlertConfigurationSaveService {

  private final AlertParameterSetRepository alertParameterSetRepository;
  private final LiveAlertConfigurationRepository liveAlertConfigurationRepository;
  private final LiveAlertConfigurationAuditSaveService auditSaveService;
  private final LiveAlertConfigurationObjectMapper liveAlertConfigurationObjectMapper;
  private final SandboxAlertConfigurationRepository sandboxAlertConfigurationRepository;
  private final SandboxAlertConfigurationService sandboxAlertConfigurationService;


  public LiveAlertConfigurationSaveService(
      AlertParameterSetRepository alertParameterSetRepository,
      LiveAlertConfigurationRepository liveAlertConfigurationRepository,
      LiveAlertConfigurationObjectMapper liveAlertConfigurationObjectMapper,
      SandboxAlertConfigurationRepository sandboxAlertConfigurationRepository,
      LiveAlertConfigurationAuditSaveService auditSaveService,
      SandboxAlertConfigurationService sandboxAlertConfigurationService) {
    this.alertParameterSetRepository = alertParameterSetRepository;
    this.liveAlertConfigurationRepository = liveAlertConfigurationRepository;
    this.liveAlertConfigurationObjectMapper = liveAlertConfigurationObjectMapper;
    this.sandboxAlertConfigurationRepository = sandboxAlertConfigurationRepository;
    this.auditSaveService = auditSaveService;
    this.sandboxAlertConfigurationService = sandboxAlertConfigurationService;
  }


  public Optional<LiveAlertConfiguration> createFromSandboxConfiguration(String sandboxUUID,
      String sandboxConfigUUID, String user) throws SandboxAlertConfigurationNotFoundException {

    SandboxAlertConfigurationDTO sandboxAlertConfigurationDTO = sandboxAlertConfigurationRepository
        .findById(new SandboxAlertConfigurationDTOPrimaryKey(UUID.fromString(sandboxUUID),
            UUID.fromString(sandboxConfigUUID))).orElseThrow(
            () -> new SandboxAlertConfigurationNotFoundException(sandboxUUID, sandboxConfigUUID));

    AlertParameterSetDTO parameterSetDTO = alertParameterSetRepository
        .findById(sandboxAlertConfigurationDTO.getApsHash()).orElse(null);

    LiveAlertConfigurationDTO liveAlertConfigurationDTO = liveAlertConfigurationObjectMapper
        .mapSandboxConfigDTOToLiveConfigDTO(sandboxAlertConfigurationDTO, user);

    auditSaveService.saveAudits(liveAlertConfigurationDTO);

    LiveAlertConfigurationDTO savedDTO = liveAlertConfigurationRepository
        .save(liveAlertConfigurationDTO);

    //Set liveConfigUUID for sandboxAlertConfigurationDTO for INSERT
    if (sandboxAlertConfigurationDTO.getLiveConfigUUID() == null) {
      sandboxAlertConfigurationService
          .updateLiveUUID(sandboxAlertConfigurationDTO, liveAlertConfigurationDTO.getUuid(), user);
    }

    return Optional.ofNullable(liveAlertConfigurationObjectMapper.liveDTOToModelWithParameterSet(
        savedDTO, parameterSetDTO));
  }


  public LiveAlertConfiguration updateStatus(String alertConfigurationUUID,
      UpdateStatus status, String user) throws AlertConfigurationNotFoundException {

    LiveAlertConfigurationDTO liveAlertConfigurationDTO = liveAlertConfigurationRepository
        .findById(UUID.fromString(alertConfigurationUUID)).orElseThrow(
            () -> new AlertConfigurationNotFoundException(alertConfigurationUUID));

    liveAlertConfigurationDTO.setStatus(status.getStatus().toString());
    liveAlertConfigurationDTO.setUpdatedBy(user);
    liveAlertConfigurationDTO.setUpdatedWhen(Instant.now());

    auditSaveService.saveAudits(liveAlertConfigurationDTO);

    LiveAlertConfigurationDTO savedDTO = liveAlertConfigurationRepository
        .save(liveAlertConfigurationDTO);

    AlertParameterSetDTO parameterSetDTO = alertParameterSetRepository
        .findById(liveAlertConfigurationDTO.getApsHash()).orElse(null);

    return liveAlertConfigurationObjectMapper
        .liveDTOToModelWithParameterSet(savedDTO, parameterSetDTO);
  }
}
