package com.webtech.service.alertconfiguration.service;

import com.datastax.driver.core.utils.UUIDs;
import com.webtech.service.alertconfiguration.dto.SandboxRunAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunDTO;
import com.webtech.service.alertconfiguration.dto.SandboxRunDTOPrimaryKey;
import com.webtech.service.alertconfiguration.exception.SandboxRunNotFoundException;
import com.webtech.service.alertconfiguration.mapper.CreateSandboxRunRequestMapper;
import com.webtech.service.alertconfiguration.mapper.SandboxRunDTOMapper;
import com.irisium.service.alertconfiguration.model.CreateSandboxRunRequest;
import com.irisium.service.alertconfiguration.model.SandboxRun;
import com.webtech.service.alertconfiguration.repository.SandboxRunRepository;
import com.webtech.service.common.Transformer;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SandboxRunService {

  private final SandboxRunRepository repository;
  private final Transformer<SandboxRunDTO, SandboxRun> sandboxRunDTOMapper;
  private final Transformer<CreateSandboxRunRequest, SandboxRunDTO> createSandboxRunRequestMapper;
  private final SandboxAlertConfigurationService sandboxAlertConfigurationService;

  public SandboxRunService(SandboxRunRepository repository, SandboxRunDTOMapper sandboxRunDTOMapper,
      CreateSandboxRunRequestMapper createSandboxRunRequestMapper,
      SandboxAlertConfigurationService sandboxAlertConfigurationService) {
    this.repository = repository;
    this.sandboxRunDTOMapper = sandboxRunDTOMapper;
    this.createSandboxRunRequestMapper = createSandboxRunRequestMapper;
    this.sandboxAlertConfigurationService = sandboxAlertConfigurationService;
  }

  public List<SandboxRun> getAllRunsBySandboxId(String sandboxId) {
    List<SandboxRunDTO> dtoList = repository
        .findAllByPrimaryKeySandboxUUID(UUID.fromString(sandboxId));
    List<SandboxRun> sandboxRunsList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtoList)) {
      sandboxRunsList = dtoList.stream().map(sandboxRunDTOMapper::transform)
          .collect(Collectors.toList());
    }
    return sandboxRunsList;
  }


  public Optional<SandboxRun> getSandboxRunByRunId(String sandboxId, String sandboxRunId) {
    SandboxRunDTOPrimaryKey primaryKey = new SandboxRunDTOPrimaryKey();
    primaryKey.setSandboxUUID(UUID.fromString(sandboxId));
    primaryKey.setRunUUID(UUID.fromString(sandboxRunId));
    SandboxRunDTO sandboxRunDTO = repository.findById(primaryKey).orElse(null);
    return Optional.ofNullable(sandboxRunDTOMapper.transform(sandboxRunDTO));
  }

  public void updateSandboxRunWhenFinished(String sandboxId, String sandboxRunId)
      throws SandboxRunNotFoundException {
    SandboxRunDTOPrimaryKey primaryKey = new SandboxRunDTOPrimaryKey();
    primaryKey.setSandboxUUID(UUID.fromString(sandboxId));
    primaryKey.setRunUUID(UUID.fromString(sandboxRunId));
    SandboxRunDTO sandboxRunDTO = repository.findById(primaryKey).orElseThrow(
        () -> new SandboxRunNotFoundException(
            " Sandbox run not found when tries to update the runEndTime : " + sandboxRunId));
    sandboxRunDTO.setEndTime(Instant.now());
    repository.save(sandboxRunDTO);
  }

  public SandboxRun createSandboxRun(String sandboxId,
      CreateSandboxRunRequest createSandboxRunRequest, String user) {
    SandboxRunDTO sandboxRunDTO = createSandboxRunRequestMapper.transform(createSandboxRunRequest);
    SandboxRunDTOPrimaryKey primaryKey = new SandboxRunDTOPrimaryKey();
    primaryKey.setRunUUID(UUIDs.timeBased());
    primaryKey.setSandboxUUID(UUID.fromString(sandboxId));
    sandboxRunDTO.setPrimaryKey(primaryKey);
    sandboxRunDTO.setOwner(user);
    Set<SandboxRunAlertConfigurationDTO> dtoSet = sandboxAlertConfigurationService
        .getAllSandboxAlertConfigurationBySandboxId(sandboxId);
    dtoSet = dtoSet.stream().filter(e -> createSandboxRunRequest.getAlertConfigurationIds()
        .contains(e.getSandboxAlertConfigurationUUID().toString())).collect(Collectors.toSet());
    sandboxRunDTO.setAlertConfigurationSet(dtoSet);
    repository.save(sandboxRunDTO);
    return sandboxRunDTOMapper.transform(sandboxRunDTO);
  }


  public List<UUID> getAllRunIdsBySandboxId(String sandboxId) {
    List<UUID> runIds = Collections.emptyList();
    List<SandboxRunDTO> dtoList = repository
        .findAllByPrimaryKeySandboxUUID(UUID.fromString(sandboxId));
    if (!CollectionUtils.isEmpty(dtoList)) {
      runIds = dtoList.stream().map(SandboxRunDTO::getPrimaryKey)
          .map(SandboxRunDTOPrimaryKey::getRunUUID)
          .collect(Collectors.toList());
    }
    return runIds;

  }
}
