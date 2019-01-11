package com.webtech.service.alertconfiguration.service;

import com.webtech.service.alertconfiguration.dto.SandboxDTO;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.SandboxNotFoundException;
import com.webtech.service.alertconfiguration.mapper.CreateSandboxRequestMapper;
import com.webtech.service.alertconfiguration.mapper.SandboxDTOMapper;
import com.irisium.service.alertconfiguration.model.CreateSandboxRequest;
import com.irisium.service.alertconfiguration.model.CreateSandboxRequest.CreateFromEnum;
import com.irisium.service.alertconfiguration.model.Sandbox;
import com.irisium.service.alertconfiguration.model.Sandbox.StatusEnum;
import com.irisium.service.alertconfiguration.model.SandboxResetOptions;
import com.irisium.service.alertconfiguration.model.SandboxResetOptions.ResetFromEnum;
import com.webtech.service.alertconfiguration.repository.SandboxRepository;
import com.webtech.service.common.Transformer;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Service
public class SandboxService {

  private final SandboxRepository repository;
  private final Transformer<SandboxDTO, Sandbox> sandboxDTOMapper;
  private final Transformer<CreateSandboxRequest, SandboxDTO> createSandboxRequestMapper;
  private final SandboxAlertConfigurationService sandboxAlertConfigurationService;

  public SandboxService(SandboxRepository repository, SandboxDTOMapper sandboxDTOMapper,
      CreateSandboxRequestMapper createSandboxRequestMapper,
      SandboxAlertConfigurationService sandboxAlertConfigurationService) {
    this.repository = repository;
    this.sandboxDTOMapper = sandboxDTOMapper;
    this.createSandboxRequestMapper = createSandboxRequestMapper;
    this.sandboxAlertConfigurationService = sandboxAlertConfigurationService;
  }

  public List<Sandbox> getAllSandboxes() {
    List<SandboxDTO> dtoList = repository.findAll();
    List<Sandbox> sandboxesList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtoList)) {
      sandboxesList = dtoList.stream().map(sandboxDTOMapper::transform)
          .collect(Collectors.toList());
    }
    return sandboxesList;
  }


  public Optional<Sandbox> getSandboxById(String sandboxId) {
    SandboxDTO sandboxDTO = repository.findById(UUID.fromString(sandboxId)).orElse(null);
    return Optional.ofNullable(sandboxDTOMapper.transform(sandboxDTO));
  }

  public Sandbox createSandbox(CreateSandboxRequest createSandboxRequest, String user)
      throws AlertConfigurationNotFoundException {
    SandboxDTO sandboxDTO = createSandboxRequestMapper.transform(createSandboxRequest);
    sandboxDTO.setOwner(user);
    repository.save(sandboxDTO);

    if (CreateFromEnum.LIVE.equals(createSandboxRequest.getCreateFrom())) {
      initFromLive(user, sandboxDTO);
    } else if (CreateFromEnum.POINT_IN_TIME.equals(createSandboxRequest.getCreateFrom())) {
      initFromPointInTime(createSandboxRequest.getPointInTime(), user, sandboxDTO);
    } else if (CreateFromEnum.SANDBOX_ID.equals(createSandboxRequest.getCreateFrom())) {
      initFromAnotherSandbox(createSandboxRequest.getSandboxId(), user, sandboxDTO);
    } else if (CreateFromEnum.SELECTED_LIVE_CONFIGS.equals(createSandboxRequest.getCreateFrom())) {
      initFromSelectedLiveConfigs(createSandboxRequest.getSelectedLiveConfigs(), user, sandboxDTO);
    }
    return sandboxDTOMapper.transform(sandboxDTO);
  }

  private void initFromSelectedLiveConfigs(List<String> selectedLiveConfigs, String user,
      SandboxDTO sandboxDTO) throws AlertConfigurationNotFoundException {
    if (CollectionUtils.isEmpty(selectedLiveConfigs)) {
      throw new IllegalArgumentException("Required parameter not found: selectedLiveConfigs");
    }
    sandboxAlertConfigurationService
        .createConfigurationsFromSelectedLiveConfigs(sandboxDTO.getUuid().toString(),
            selectedLiveConfigs, user);
  }

  private void initFromAnotherSandbox(String sandboxId, String user,
      SandboxDTO sandboxDTO) {
    if (StringUtils.isEmpty(sandboxId)) {
      throw new IllegalArgumentException("Required parameter not found: sandboxId");
    }
    sandboxAlertConfigurationService
        .createConfigurationsFromSandboxConfigs(sandboxDTO.getUuid().toString(),
            sandboxId, user);
  }

  private void initFromPointInTime(Instant pointInTime, String user,
      SandboxDTO sandboxDTO) {
    if (pointInTime == null) {
      throw new IllegalArgumentException("Required parameter not found: pointInTime");
    }
    sandboxAlertConfigurationService
        .createConfigurationsFromPointInTime(pointInTime, sandboxDTO.getUuid().toString(), user);
  }

  private void initFromLive(String user, SandboxDTO sandboxDTO) {
    sandboxAlertConfigurationService
        .createConfigurationsFromLive(sandboxDTO.getUuid().toString(), user);
  }

  public void deleteSandboxById(String sandboxId) throws SandboxNotFoundException {
    SandboxDTO sandboxDTO = repository.findById(UUID.fromString(sandboxId))
        .orElseThrow(() -> new SandboxNotFoundException(sandboxId));
    sandboxDTO.setStatus(StatusEnum.DELETED.toString());
    repository.save(sandboxDTO);
  }

  public Sandbox resetSandbox(String sandboxId, SandboxResetOptions resetSandboxRequest,
      String user) throws SandboxNotFoundException {
    SandboxDTO sandboxToReset = repository.findById(UUID.fromString(sandboxId))
        .orElseThrow(() -> new SandboxNotFoundException(sandboxId));

    //1. Archive all current Sandbox configurations associated with this sandbox
    sandboxAlertConfigurationService
        .archiveAllSandboxConfigurations(sandboxToReset.getUuid(), user);

    //2. Reset from requested option
    if (ResetFromEnum.LIVE.equals(resetSandboxRequest.getResetFrom())) {
      initFromLive(user, sandboxToReset);
    } else if (ResetFromEnum.POINT_IN_TIME.equals(resetSandboxRequest.getResetFrom())) {
      initFromPointInTime(resetSandboxRequest.getPointInTime(), user, sandboxToReset);
    } else if (ResetFromEnum.SANDBOX_ID.equals(resetSandboxRequest.getResetFrom())) {
      initFromAnotherSandbox(resetSandboxRequest.getSandboxId(), user, sandboxToReset);
    }
    return sandboxDTOMapper.transform(sandboxToReset);

  }
}
