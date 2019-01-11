package com.webtech.service.alertconfiguration.service;

import com.irisium.service.alertDefinition.model.GuiDeploymentAlertType;
import com.irisium.service.alertDefinition.model.LogicOverride;
import com.irisium.service.alertDefinition.model.Parameter;
import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.SandboxRunAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.AuditNotFoundException;
import com.webtech.service.alertconfiguration.exception.IllegalParameterException;
import com.webtech.service.alertconfiguration.exception.SandboxAlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.mapper.SandboxAlertConfigurationObjectMapper;
import com.webtech.service.alertconfiguration.mapper.SandboxRunAlertConfigurationDTOMapper;
import com.irisium.service.alertconfiguration.model.CloneSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.CreateUpdateSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfiguration;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfiguration.StatusEnum;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfigurationAudit;
import com.irisium.service.alertconfiguration.model.UpdateStatus;
import com.webtech.service.alertconfiguration.repository.AlertConfigurationRepositories;
import com.webtech.service.alerttype.service.AlertTypeService;
import com.webtech.service.common.AppPropertiesProvider;
import com.webtech.service.common.exception.EntityNotFoundException;
import io.netty.util.internal.StringUtil;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class SandboxAlertConfigurationService {

  private final AlertConfigurationRepositories alertRepositories;
  private final SandboxAlertConfigurationObjectMapper sandboxAlertConfigurationObjectMapper;
  private final SandboxRunAlertConfigurationDTOMapper sandboxRunAlertConfigurationDTOMapper;
  private final AppPropertiesProvider appPropertiesProvider;
  private final AlertTypeService alertTypeService;

  public SandboxAlertConfigurationService(
      AlertConfigurationRepositories alertRepositories,
      SandboxAlertConfigurationObjectMapper sandboxAlertConfigurationObjectMapper,
      SandboxRunAlertConfigurationDTOMapper sandboxRunAlertConfigurationDTOMapper,
      AppPropertiesProvider appPropertiesProvider,
      AlertTypeService alertTypeService) {
    this.alertRepositories = alertRepositories;
    this.sandboxAlertConfigurationObjectMapper = sandboxAlertConfigurationObjectMapper;
    this.sandboxRunAlertConfigurationDTOMapper = sandboxRunAlertConfigurationDTOMapper;
    this.appPropertiesProvider = appPropertiesProvider;
    this.alertTypeService = alertTypeService;
  }

  public Set<SandboxRunAlertConfigurationDTO> getAllSandboxAlertConfigurationBySandboxId(
      String sandboxId) {
    List<SandboxAlertConfigurationDTO> dtoList = alertRepositories
        .getSandboxAlertConfigurationRepository()
        .findAllByPrimaryKeySandboxUUID(UUID.fromString(sandboxId));
    if (CollectionUtils.isEmpty(dtoList)) {
      return Collections.emptySet();
    }
    return dtoList.stream().map(sandboxRunAlertConfigurationDTOMapper::transform)
        .collect(Collectors.toSet());
  }

  public List<SandboxAlertConfigurationDTO> createConfigurationsFromLive(String sandboxUUID,
      String user) {

    List<LiveAlertConfigurationDTO> inputDTOList = alertRepositories
        .getLiveAlertConfigurationRepository().findAll();

    if (CollectionUtils.isEmpty(inputDTOList)) {
      return Collections.emptyList();
    }
    List<SandboxAlertConfigurationDTO> dtoList = inputDTOList
        .stream()
        .map(item -> sandboxAlertConfigurationObjectMapper
            .createSandboxConfigDTO(item, sandboxUUID, null, user))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    return auditAndSave(dtoList);
  }

  public List<SandboxAlertConfigurationDTO> createConfigurationsFromPointInTime(
      Instant pointInTime, String sandboxUUID, String user) {

    List<LiveAlertConfigurationDTO> inputDTOList = alertRepositories
        .getLiveAlertConfigurationRepository().findAll();
    List<UUID> liveUUIDList = inputDTOList.stream().map(LiveAlertConfigurationDTO::getUuid)
        .collect(Collectors.toList());
    List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> pointInTimeAudits = alertRepositories
        .getLiveAlertConfigurationAuditByAlertConfigUUIDRepository()
        .findLiveAuditsForPointInTime(liveUUIDList, pointInTime);

    if (CollectionUtils.isEmpty(pointInTimeAudits)) {
      return Collections.emptyList();
    }
    List<SandboxAlertConfigurationDTO> dtoList = pointInTimeAudits
        .stream()
        .map(item -> sandboxAlertConfigurationObjectMapper
            .createSandboxConfigDTO(item, sandboxUUID, null, user))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    return auditAndSave(dtoList);
  }

  public List<SandboxAlertConfigurationDTO> createConfigurationsFromSandboxConfigs(
      String toSandboxUUID, String fromSandBoxUUID, String user) {
    List<SandboxAlertConfigurationDTO> inputDTOList = alertRepositories
        .getSandboxAlertConfigurationRepository()
        .findAllByPrimaryKeySandboxUUID(UUID.fromString(fromSandBoxUUID));
    if (CollectionUtils.isEmpty(inputDTOList)) {
      return Collections.emptyList();
    }
    List<SandboxAlertConfigurationDTO> dtoList = inputDTOList
        .stream()
        .map(item -> sandboxAlertConfigurationObjectMapper
            .createSandboxConfigDTO(item, toSandboxUUID, null, user))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    return auditAndSave(dtoList);
  }

  private List<SandboxAlertConfigurationDTO> auditAndSave(
      List<SandboxAlertConfigurationDTO> dtoList) {

    //save to audit repository
    List<SandboxAlertConfigurationAuditDTO> auditDTOList = dtoList.stream()
        .map(sandboxAlertConfigurationObjectMapper::createSandboxConfigAuditDTO)
        .collect(Collectors.toList());
    alertRepositories.getAuditRepository().saveAll(auditDTOList);
    //save to auditByMonth repository
    List<SandboxAlertConfigurationAuditByMonthDTO> auditByMonthDTOList = auditDTOList.stream()
        .map(sandboxAlertConfigurationObjectMapper::mapSandboxAuditDTOToAuditByMonthDTO)
        .collect(Collectors.toList());
    alertRepositories.getSandboxAuditByMonthRepository().saveAll(auditByMonthDTOList);

    return alertRepositories.getSandboxAlertConfigurationRepository().saveAll(dtoList);
  }

  private SandboxAlertConfiguration auditAndSave(
      SandboxAlertConfigurationDTO sandboxAlertConfigurationDTO,
      AlertParameterSetDTO parameterSetDTO) {

    AlertParameterSetDTO savedParameterSetDTO = alertRepositories.getAlertParameterSetRepository()
        .save(parameterSetDTO);

    //Ensure the auditUUID is same for all three audit objects
    SandboxAlertConfigurationAuditDTO auditDTO = sandboxAlertConfigurationObjectMapper
        .createSandboxConfigAuditDTO(sandboxAlertConfigurationDTO);
    SandboxAlertConfigurationAuditByMonthDTO auditByMonthDTO = sandboxAlertConfigurationObjectMapper
        .mapSandboxDTOToAuditByMonthDTO(sandboxAlertConfigurationDTO,
            auditDTO.getPrimaryKey().getAuditUUID());
    //Save all three audits
    alertRepositories.getAuditRepository().save(auditDTO);
    alertRepositories.getSandboxAuditByMonthRepository().save(auditByMonthDTO);

    SandboxAlertConfigurationDTO savedDTO = alertRepositories
        .getSandboxAlertConfigurationRepository()
        .save(sandboxAlertConfigurationDTO);
    return sandboxAlertConfigurationObjectMapper
        .mapSandboxAlertConfigDTOToModel(savedDTO, savedParameterSetDTO);
  }

  private void auditAndSave(SandboxAlertConfigurationDTO sandboxAlertConfigurationDTO) {

    //Ensure the auditUUID is same for all audit objects
    SandboxAlertConfigurationAuditDTO auditDTO = sandboxAlertConfigurationObjectMapper
        .createSandboxConfigAuditDTO(sandboxAlertConfigurationDTO);
    SandboxAlertConfigurationAuditByMonthDTO auditByMonthDTO = sandboxAlertConfigurationObjectMapper
        .mapSandboxDTOToAuditByMonthDTO(sandboxAlertConfigurationDTO,
            auditDTO.getPrimaryKey().getAuditUUID());
    //Save all audits
    alertRepositories.getAuditRepository().save(auditDTO);
    alertRepositories.getSandboxAuditByMonthRepository().save(auditByMonthDTO);
    alertRepositories.getSandboxAlertConfigurationRepository().save(sandboxAlertConfigurationDTO);

  }

  public SandboxAlertConfiguration addAlertConfigToSandbox(String sandboxId,
      CreateUpdateSandboxAlertConfigRequest request, String user)
      throws NoSuchAlgorithmException, IOException, EntityNotFoundException, IllegalParameterException {

    AlertParameterSetDTO parameterSetDTO = getAlertParameterSetDTO(request);
    SandboxAlertConfigurationDTO dto = sandboxAlertConfigurationObjectMapper
        .createSandboxConfigDTOFromRequest(request, sandboxId, null, null,
            user, parameterSetDTO.getApsHash());
    return auditAndSave(dto, parameterSetDTO);

  }

  private AlertParameterSetDTO getAlertParameterSetDTO(
      CreateUpdateSandboxAlertConfigRequest request)
      throws IOException, EntityNotFoundException, NoSuchAlgorithmException, IllegalParameterException {
    validateAlertParameters(request);
    return sandboxAlertConfigurationObjectMapper
        .createAlertParameterSetDTO(request,
            alertTypeService.getValidParameterPresets(request.getAlertConfigType()),
            alertTypeService.getValidLogicOverridePresets(request.getAlertConfigType()));
  }

  private void validateAlertParameters(CreateUpdateSandboxAlertConfigRequest request)
      throws IOException, EntityNotFoundException, IllegalParameterException {
    GuiDeploymentAlertType deploymentAlertType = alertTypeService
        .getDeploymentAlertType(request.getAlertConfigType());
    Set<String> requiredParams = deploymentAlertType.getParameters().stream()
        .map(Parameter::getParameterId).collect(Collectors.toSet());
    boolean parametersValid = compare(requiredParams, request.getAlertParameters());

    if (!parametersValid) {
      throw new IllegalParameterException("Incorrect parameter found for alert type");
    }
    Set<String> requiredLogicOverrides = deploymentAlertType.getLogicOverrides().stream()
        .map(LogicOverride::getLogicOverrideId).collect(Collectors.toSet());
    boolean logicOverridesValid = compare(requiredLogicOverrides, request.getLogicOverrideSet());

    if (!logicOverridesValid) {
      throw new IllegalParameterException("Incorrect logicOverride found for alert type");
    }

  }

  private <T> boolean compare(Set<String> required, Map<String, T> requested) {
    boolean isValid = false;
    if ((CollectionUtils.isEmpty(required) && CollectionUtils.isEmpty(requested))
    || (!CollectionUtils.isEmpty(required) && !CollectionUtils.isEmpty(requested) && required.equals(requested.keySet()))) {
      isValid = true;
    }
    return isValid;
  }


  public SandboxAlertConfiguration createUpdateSandboxAlertConfiguration(String sandboxId,
      String alertConfigurationId, CreateUpdateSandboxAlertConfigRequest request,
      String user)
      throws NoSuchAlgorithmException, SandboxAlertConfigurationNotFoundException, IOException,
      EntityNotFoundException, IllegalParameterException {
    SandboxAlertConfigurationDTO currentDTO = null;
    if (!StringUtil.isNullOrEmpty(alertConfigurationId)) {
      SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
          UUID.fromString(sandboxId), UUID.fromString(alertConfigurationId));
      currentDTO = alertRepositories.getSandboxAlertConfigurationRepository().findById(primaryKey)
          .orElseThrow(
              () -> new SandboxAlertConfigurationNotFoundException(sandboxId,
                  alertConfigurationId));
    }

    AlertParameterSetDTO parameterSetDTO = getAlertParameterSetDTO(request);

    SandboxAlertConfigurationDTO dto = sandboxAlertConfigurationObjectMapper
        .createSandboxConfigDTOFromRequest(request, sandboxId, alertConfigurationId, currentDTO,
            user,
            parameterSetDTO.getApsHash());
    return auditAndSave(dto, parameterSetDTO);
  }

  public Optional<SandboxAlertConfigurationAudit> getOriginalSandboxAlertConfiguration(
      String sandboxId, String alertConfigurationId) {
    SandboxAlertConfigurationAuditDTO auditDTO = alertRepositories.getAuditRepository()
        .findFirstByPrimaryKeySandboxUUIDAndPrimaryKeyAlertConfigurationUUIDOrderByPrimaryKeyAuditUUIDAsc(
            UUID.fromString(sandboxId), UUID.fromString(alertConfigurationId));
    AlertParameterSetDTO parameterSetDTO = alertRepositories.getAlertParameterSetRepository()
        .findById(auditDTO.getApsHash()).orElse(null);
    return Optional.ofNullable(sandboxAlertConfigurationObjectMapper
        .mapSandboxConfigAuditDTOToModel(auditDTO, parameterSetDTO));
  }

  public Optional<SandboxAlertConfiguration> getSandboxAlertConfiguration(String sandboxId,
      String alertConfigurationId) {
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        UUID.fromString(sandboxId), UUID.fromString(alertConfigurationId));
    Optional<SandboxAlertConfigurationDTO> dto = alertRepositories
        .getSandboxAlertConfigurationRepository()
        .findById(primaryKey);
    if (dto.isPresent()) {
      Optional<AlertParameterSetDTO> parameterSetDTO = alertRepositories
          .getAlertParameterSetRepository()
          .findById(dto.get().getApsHash());
      return Optional.ofNullable(sandboxAlertConfigurationObjectMapper
          .mapSandboxAlertConfigDTOToModel(dto.get(), parameterSetDTO.orElse(null)));
    }
    return Optional.empty();
  }

  public List<SandboxAlertConfiguration> getSandboxAlertConfigurations(String sandboxId) {
    List<SandboxAlertConfigurationDTO> dtoList = alertRepositories
        .getSandboxAlertConfigurationRepository()
        .findAllByPrimaryKeySandboxUUID(UUID.fromString(sandboxId));
    return sandboxAlertConfigurationObjectMapper.mapSandboxAlertConfigDTOListToModel(dtoList);
  }

  public void updateLiveUUID(SandboxAlertConfigurationDTO sandboxAlertConfigurationDTO,
      UUID liveConfigUUID, String user) {
    if (sandboxAlertConfigurationDTO == null) {
      return;
    }
    sandboxAlertConfigurationDTO.setLiveConfigUUID(liveConfigUUID);
    sandboxAlertConfigurationDTO.setCreatedBy(user);
    sandboxAlertConfigurationDTO.setCreatedWhen(Instant.now());
    saveAudits(sandboxAlertConfigurationDTO);
    alertRepositories.getSandboxAlertConfigurationRepository().save(sandboxAlertConfigurationDTO);
  }

  public void createConfigurationsFromSelectedLiveConfigs(String sandboxUUID,
      List<String> selectedLiveConfigs, String user) throws AlertConfigurationNotFoundException {
    for (String liveConfigUUID : selectedLiveConfigs) {
      createSandboxConfigFromLiveConfig(liveConfigUUID, sandboxUUID, user);
    }
  }

  private void createSandboxConfigFromLiveConfig(String liveConfigUUID, String sandboxUUID,
      String user)
      throws AlertConfigurationNotFoundException {
    LiveAlertConfigurationDTO liveDTO = alertRepositories.getLiveAlertConfigurationRepository()
        .findById(UUID.fromString(liveConfigUUID))
        .orElseThrow(() -> new AlertConfigurationNotFoundException(liveConfigUUID));
    SandboxAlertConfigurationDTO sandboxConfigDTO = sandboxAlertConfigurationObjectMapper
        .createSandboxConfigDTO(liveDTO, sandboxUUID, null, user);
    auditAndSave(sandboxConfigDTO);
  }


  public SandboxAlertConfiguration updateStatus(String sandboxId, String alertConfigurationId,
      UpdateStatus status, String user) throws SandboxAlertConfigurationNotFoundException {

    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        UUID.fromString(sandboxId), UUID.fromString(alertConfigurationId));
    SandboxAlertConfigurationDTO dto = alertRepositories.getSandboxAlertConfigurationRepository()
        .findById(primaryKey).orElseThrow(
            () -> new SandboxAlertConfigurationNotFoundException(sandboxId, alertConfigurationId));
    updateStatus(dto, user);
    dto.setStatus(status.getStatus().toString());
    dto.setUpdatedBy(user);
    dto.setUpdatedWhen(Instant.now());
    auditAndSave(dto);
    AlertParameterSetDTO parameterSetDTO = alertRepositories.getAlertParameterSetRepository()
        .findById(dto.getApsHash()).orElse(null);
    return sandboxAlertConfigurationObjectMapper
        .mapSandboxAlertConfigDTOToModel(dto, parameterSetDTO);
  }

  public void archiveAllSandboxConfigurations(UUID sandboxUUID, String user) {
    List<SandboxAlertConfigurationDTO> dtoList = alertRepositories
        .getSandboxAlertConfigurationRepository()
        .findAllByPrimaryKeySandboxUUID(sandboxUUID);
    if (!CollectionUtils.isEmpty(dtoList)) {
      //save to audit repository
      List<SandboxAlertConfigurationAuditDTO> auditDTOList = dtoList.stream()
          .map(dto -> updateStatus(dto, user))
          .map(sandboxAlertConfigurationObjectMapper::createSandboxConfigAuditDTO)
          .collect(Collectors.toList());
      alertRepositories.getAuditRepository().saveAll(auditDTOList);

      //save to auditByMonth repository
      List<SandboxAlertConfigurationAuditByMonthDTO> auditByMonthDTOList = auditDTOList.stream()
          .map(sandboxAlertConfigurationObjectMapper::mapSandboxAuditDTOToAuditByMonthDTO)
          .collect(Collectors.toList());
      alertRepositories.getSandboxAuditByMonthRepository().saveAll(auditByMonthDTOList);
      alertRepositories.getSandboxAlertConfigurationRepository().saveAll(dtoList);
    }
  }

  public List<SandboxAlertConfigurationAudit> getAllSandboxAlertConfigurationsAudit(
      String sandboxId,
      Integer requestedNumberOfRecords, Integer maxAge) {

    Integer numberOfRecords = requestedNumberOfRecords;
    IntStream range = IntStream.rangeClosed(appPropertiesProvider.getMinAuditRowCountLimit(),
        appPropertiesProvider.getMaxAuditRowCountLimit());
    if (range.noneMatch(num -> Integer.valueOf(num).equals(requestedNumberOfRecords))) {
      numberOfRecords = appPropertiesProvider.getDefaultAuditRowCountLimit();
    }

    List<LocalDate> months = getAllMonths(maxAge);

    List<SandboxAlertConfigurationAuditByMonthDTO> audits = alertRepositories
        .getSandboxAuditByMonthRepository()
        .findLatestTopNAuditsWithAgeLimit(UUID.fromString(sandboxId), months, numberOfRecords);
    List<SandboxAlertConfigurationAuditByMonthDTO> result = audits;

    //If result is more than numOfRecords, trim the excess
    if (audits.size() > numberOfRecords) {
      result = audits.subList(0, numberOfRecords);
    }

    return sandboxAlertConfigurationObjectMapper.mapAuditByMonthDTOListToModelList(result);

  }

  public List<SandboxAlertConfigurationAudit> getAuditHistoryForSandboxAlertConfiguration(
      String sandboxId, String alertConfigurationId)
      throws SandboxAlertConfigurationNotFoundException {

    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        UUID.fromString(sandboxId), UUID.fromString(alertConfigurationId));
    SandboxAlertConfigurationDTO dto = alertRepositories.getSandboxAlertConfigurationRepository()
        .findById(primaryKey).orElseThrow(
            () -> new SandboxAlertConfigurationNotFoundException(sandboxId, alertConfigurationId));

    List<SandboxAlertConfigurationAuditDTO> audits = alertRepositories.getAuditRepository()
        .findByPrimaryKeySandboxUUIDAndPrimaryKeyAlertConfigurationUUID(
            dto.getPrimaryKey().getSandboxUUID(), dto.getPrimaryKey().getAlertConfigurationUUID());

    return sandboxAlertConfigurationObjectMapper.mapAuditDTOListToModelList(audits);
  }

  public SandboxAlertConfigurationAudit getSandboxAlertConfigurationAuditById(
      String sandboxId, String alertConfigurationId, String auditId)
      throws AuditNotFoundException, SandboxAlertConfigurationNotFoundException {

    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        UUID.fromString(sandboxId), UUID.fromString(alertConfigurationId));
    if (!alertRepositories.getSandboxAlertConfigurationRepository()
        .existsById(primaryKey)) {
      throw new SandboxAlertConfigurationNotFoundException(sandboxId, alertConfigurationId);
    }

    SandboxAlertConfigurationAuditDTOPrimaryKey auditPrimaryKey = new SandboxAlertConfigurationAuditDTOPrimaryKey();
    auditPrimaryKey.setAlertConfigurationUUID(UUID.fromString(alertConfigurationId));
    auditPrimaryKey.setSandboxUUID(UUID.fromString(sandboxId));
    auditPrimaryKey.setAuditUUID(UUID.fromString(auditId));
    SandboxAlertConfigurationAuditDTO auditDTO = alertRepositories.getAuditRepository()
        .findById(auditPrimaryKey).orElseThrow(
            () -> new AuditNotFoundException(
                "Failed to find audit for Alert Configuration " + sandboxId
                    + " with alert configuration id  " + alertConfigurationId + " with audit id  "
                    + auditId));

    AlertParameterSetDTO parameterSetDTO = alertRepositories.getAlertParameterSetRepository()
        .findById(auditDTO.getApsHash())
        .orElse(null);

    return sandboxAlertConfigurationObjectMapper
        .mapSandboxAuditDTOToModel(auditDTO, parameterSetDTO);
  }

  private SandboxAlertConfigurationDTO updateStatus(SandboxAlertConfigurationDTO dto, String user) {
    dto.setStatus(StatusEnum.ARCHIVED.toString());
    dto.setUpdatedBy(user);
    dto.setUpdatedWhen(Instant.now());
    return dto;
  }

  private void saveAudits(SandboxAlertConfigurationDTO sandboxAlertConfigurationDTO) {

    //Ensure the auditUUID is same for all three audit objects
    SandboxAlertConfigurationAuditDTO auditDTO = sandboxAlertConfigurationObjectMapper
        .createSandboxConfigAuditDTO(sandboxAlertConfigurationDTO);
    SandboxAlertConfigurationAuditByMonthDTO auditByMonthDTO = sandboxAlertConfigurationObjectMapper
        .mapSandboxDTOToAuditByMonthDTO(sandboxAlertConfigurationDTO,
            auditDTO.getPrimaryKey().getAuditUUID());

    //Save all three audits
    alertRepositories.getAuditRepository().save(auditDTO);
    alertRepositories.getSandboxAuditByMonthRepository().save(auditByMonthDTO);

  }

  private List<LocalDate> getAllMonths(Integer maxAge) {
    LocalDate earliestLimit = LocalDate.now().minus(Period.ofDays(maxAge));
    YearMonth yearMonth = YearMonth.of(earliestLimit.getYear(), earliestLimit.getMonthValue());

    List<LocalDate> months = new ArrayList<>();
    YearMonth month = yearMonth;
    while (month.isBefore(YearMonth.now())) {
      months.add(month.atDay(1));
      month = month.plusMonths(1);
    }
    months.add(YearMonth.now().atDay(1));
    return months;
  }

  public SandboxAlertConfiguration cloneSandboxAlertConfiguration(String sandboxId,
      String alertConfigId, CloneSandboxAlertConfigRequest cloneSandboxAlertConfigRequest,
      String user) throws SandboxAlertConfigurationNotFoundException {
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        UUID.fromString(sandboxId), UUID.fromString(alertConfigId));
    SandboxAlertConfigurationDTO dto = alertRepositories.getSandboxAlertConfigurationRepository()
        .findById(primaryKey).orElseThrow(
            () -> new SandboxAlertConfigurationNotFoundException(sandboxId, alertConfigId));
    SandboxAlertConfigurationDTO clone = sandboxAlertConfigurationObjectMapper
        .clone(dto, cloneSandboxAlertConfigRequest.getName(), user);
    auditAndSave(clone);
    return sandboxAlertConfigurationObjectMapper.mapSandboxAlertConfigDTOToModel(clone, null);
  }
}
