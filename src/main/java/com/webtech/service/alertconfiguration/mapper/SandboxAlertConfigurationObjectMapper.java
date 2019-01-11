package com.webtech.service.alertconfiguration.mapper;

import com.datastax.driver.core.utils.UUIDs;
import com.webtech.service.alertconfiguration.dto.AlertConfiguration;
import com.webtech.service.alertconfiguration.dto.AlertFilterDTO;
import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditByMonthDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTOPrimaryKey;
import com.irisium.service.alertconfiguration.model.AlertFilter;
import com.irisium.service.alertconfiguration.model.CreateUpdateSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfiguration;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfigurationAudit;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Component
public class SandboxAlertConfigurationObjectMapper {

  private final AlertFilterMapper alertFilterMapper;

  public SandboxAlertConfigurationObjectMapper(
      AlertFilterMapper alertFilterMapper) {
    this.alertFilterMapper = alertFilterMapper;
  }

  public SandboxAlertConfigurationDTO createSandboxConfigDTO(
      AlertConfiguration inputDTO, String sandboxUUID, String alertConfigurationId, String user) {
    UUID alertConfigUUID =
        (!StringUtils.isEmpty(alertConfigurationId)) ? UUID.fromString(alertConfigurationId)
            : UUIDs.timeBased();
    SandboxAlertConfigurationDTO dto = createDTOWithPrimaryKey(UUID.fromString(sandboxUUID),
        alertConfigUUID);

    populateCommonProperties(dto, inputDTO, user);
    //Retain the live config reference only if its being created directly from live
    // (ie. not from live audit or sandbox config)
    if (inputDTO instanceof LiveAlertConfigurationDTO) {
      dto.setLiveConfigUUID(((LiveAlertConfigurationDTO) inputDTO).getUuid());
    }

    return dto;
  }

  private SandboxAlertConfigurationDTO createDTOWithPrimaryKey(UUID sandboxUUID,
      UUID alertConfigUUID) {
    SandboxAlertConfigurationDTO dto = new SandboxAlertConfigurationDTO();
    dto.setPrimaryKey(new SandboxAlertConfigurationDTOPrimaryKey(sandboxUUID,
        alertConfigUUID));
    return dto;
  }


  private void populateCommonProperties(AlertConfiguration destination, AlertConfiguration source,
      String user) {
    destination.setApsHash(source.getApsHash());
    destination.setAlertLogicType(source.getAlertLogicType());
    destination.setComment(source.getComment());
    destination.setCreatedBy(user);
    destination.setCreatedWhen(Instant.now());
    destination.setName(source.getName());
    destination.setStatus(source.getStatus());
  }

  public SandboxAlertConfigurationAuditDTO createSandboxConfigAuditDTO(
      SandboxAlertConfigurationDTO dto) {
    SandboxAlertConfigurationAuditDTOPrimaryKey primaryKey = new SandboxAlertConfigurationAuditDTOPrimaryKey(
        dto.getPrimaryKey().getSandboxUUID(), dto.getPrimaryKey().getAlertConfigurationUUID(),
        UUIDs.timeBased());
    SandboxAlertConfigurationAuditDTO auditDTO = new SandboxAlertConfigurationAuditDTO();

    auditDTO.setPrimaryKey(primaryKey);
    populateCommonProperties(auditDTO, dto, dto.getCreatedBy());
    auditDTO.setCreatedWhen(dto.getCreatedWhen());
    auditDTO.setLiveConfigUUID(dto.getLiveConfigUUID());
    auditDTO.setUpdatedBy(dto.getUpdatedBy());
    auditDTO.setUpdatedWhen(dto.getUpdatedWhen());

    return auditDTO;
  }

  public SandboxAlertConfigurationDTO createSandboxConfigDTOFromRequest(
      CreateUpdateSandboxAlertConfigRequest request, String sandboxUUID,
      String alertConfigurationId, SandboxAlertConfigurationDTO currentDto, String user,
      String apsHash) {
    UUID alertConfigUUID =
        (!StringUtils.isEmpty(alertConfigurationId)) ? UUID.fromString(alertConfigurationId)
            : UUIDs.timeBased();
    SandboxAlertConfigurationDTO dto = createDTOWithPrimaryKey(UUID.fromString(sandboxUUID),
        alertConfigUUID);

    if (currentDto != null) {
      dto.setLiveConfigUUID(currentDto.getLiveConfigUUID());
      dto.setCreatedBy(currentDto.getCreatedBy());
      dto.setCreatedWhen(currentDto.getCreatedWhen());
      dto.setUpdatedBy(user);
      dto.setUpdatedWhen(Instant.now());
    } else {
      dto.setCreatedBy(user);
      dto.setCreatedWhen(Instant.now());
    }
    dto.setApsHash(apsHash);
    dto.setAlertLogicType(request.getAlertConfigType());
    dto.setComment(request.getComment());
    dto.setName(request.getName());
    dto.setStatus(request.getStatus().toString());

    return dto;
  }

  public AlertParameterSetDTO createAlertParameterSetDTO(
      CreateUpdateSandboxAlertConfigRequest request,
      Map<String, String> paramPresets, Map<String, Boolean> logicPresets)
      throws NoSuchAlgorithmException {
    Set<String> aggregationFields = new HashSet<>();
    if (!CollectionUtils.isEmpty(request.getAlertAggregationFields())) {
      aggregationFields.addAll(request.getAlertAggregationFields());
    }
    Set<AlertFilterDTO> alertFilterDTOS = mapAlertFilterModelListToDTOs(request.getAlertFilters());

    Map<String, String> paramValues = merge(request.getAlertParameters(), paramPresets);
    Map<String, Boolean> logicOverrideValues = merge(request.getLogicOverrideSet(), logicPresets);
    return new AlertParameterSetDTO(request.getAlertConfigType(), request.getBusinessUnit(),
        paramValues, aggregationFields, alertFilterDTOS, logicOverrideValues);
  }

  private <T> Map<String, T> merge(Map<String, T> requestParams, Map<String, T> paramPresets) {
    Map<String, T> paramValues = new HashMap<>();
    if (!CollectionUtils.isEmpty(requestParams)) {
      paramValues.putAll(requestParams);
    }
    if (!CollectionUtils.isEmpty(paramPresets)) {
      paramValues.putAll(paramPresets);
    }
    return paramValues;
  }

  private Set<AlertFilterDTO> mapAlertFilterModelListToDTOs(List<AlertFilter> alertFilters) {
    Set<AlertFilterDTO> dtoSet = Collections.emptySet();
    if (CollectionUtils.isEmpty(alertFilters)) {
      return dtoSet;
    }
    return alertFilters.stream().map(this::mapAlertFilterModelToDTO).collect(Collectors.toSet());
  }

  public AlertFilterDTO mapAlertFilterModelToDTO(AlertFilter alertFilter) {
    AlertFilterDTO dto = null;
    if (alertFilter != null) {
      dto = new AlertFilterDTO(alertFilter.getAggregationFieldName(),
          alertFilter.getOperation().toString(),
          new HashSet<>(alertFilter.getValues()));
    }
    return dto;
  }

  public SandboxAlertConfiguration mapSandboxAlertConfigDTOToModel(
      SandboxAlertConfigurationDTO savedDTO, AlertParameterSetDTO parameterSetDTO) {
    SandboxAlertConfiguration model = null;
    if (savedDTO != null) {
      model = new SandboxAlertConfiguration();
      model.setSandboxUUID(savedDTO.getPrimaryKey().getSandboxUUID().toString());
      model.setAlertConfigurationUUID(
          savedDTO.getPrimaryKey().getAlertConfigurationUUID().toString());
      model.setAlertConfigType(savedDTO.getAlertLogicType());
      model.setComment(savedDTO.getComment());
      model.setWho(savedDTO.getCreatedBy());
      model.setWhen(savedDTO.getCreatedWhen());
      model.setName(savedDTO.getName());
      model.setStatus(SandboxAlertConfiguration.StatusEnum.fromValue(savedDTO.getStatus()));
      model.setApsHash(savedDTO.getApsHash());
      if (savedDTO.getLiveConfigUUID() != null) {
        model.setLiveConfigUUID(savedDTO.getLiveConfigUUID().toString());
      }
      model.setUpdatedWhen(savedDTO.getUpdatedWhen());
      model.setUpdatedWho(savedDTO.getUpdatedBy());
      if (parameterSetDTO != null) {
        model.setBusinessUnit(parameterSetDTO.getBusinessUnit());
        model.setAlertAggregationFields(
            alertFilterMapper.getAggregationFields(parameterSetDTO.getAlertAggregationFields()));
        model.setAlertParameters(
            alertFilterMapper.getAlertParameters(parameterSetDTO.getAlertParameters()));
        model.setAlertFilters(
            alertFilterMapper.getAlertFilters(parameterSetDTO.getAlertFilterDTOS()));
        model.setLogicOverrideSet(parameterSetDTO.getLogicOverrides());
      }

    }

    return model;
  }


  public SandboxAlertConfigurationAudit mapSandboxConfigAuditDTOToModel(
      SandboxAlertConfigurationAuditDTO auditDTO, AlertParameterSetDTO parameterSetDTO) {
    SandboxAlertConfigurationAudit model = null;
    if (auditDTO != null) {
      model = new SandboxAlertConfigurationAudit();
      model.setApsHash(auditDTO.getApsHash());
      model.setAuditUUID(auditDTO.getPrimaryKey().getAuditUUID().toString());
      model.setSandboxUUID(auditDTO.getPrimaryKey().getSandboxUUID().toString());
      model.setAlertConfigurationUUID(
          auditDTO.getPrimaryKey().getAlertConfigurationUUID().toString());

      model.setComment(auditDTO.getComment());
      model.setWho(auditDTO.getCreatedBy());
      model.setWhen(auditDTO.getCreatedWhen());
      model.setName(auditDTO.getName());
      model.setUpdatedWho(auditDTO.getUpdatedBy());
      model.setUpdatedWhen(auditDTO.getUpdatedWhen());
      model.setStatus(SandboxAlertConfigurationAudit.StatusEnum.fromValue(auditDTO.getStatus()));
      model.setAlertConfigType(auditDTO.getAlertLogicType());

      if (auditDTO.getLiveConfigUUID() != null) {
        model.setLiveConfigUUID(auditDTO.getLiveConfigUUID().toString());
      }

      if (parameterSetDTO != null) {
        model.setBusinessUnit(parameterSetDTO.getBusinessUnit());
        model.setAlertAggregationFields(
            alertFilterMapper.getAggregationFields(parameterSetDTO.getAlertAggregationFields()));
        model.setAlertParameters(
            alertFilterMapper.getAlertParameters(parameterSetDTO.getAlertParameters()));
        model.setAlertFilters(
            alertFilterMapper.getAlertFilters(parameterSetDTO.getAlertFilterDTOS()));
        model.setLogicOverrideSet(parameterSetDTO.getLogicOverrides());
      }

    }

    return model;
  }


  public List<SandboxAlertConfiguration> mapSandboxAlertConfigDTOListToModel(
      List<SandboxAlertConfigurationDTO> dtoList) {
    List<SandboxAlertConfiguration> modelList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtoList)) {
      modelList = dtoList.stream().map(this::mapSandboxConfigAuditDTOToModelNoParameters).filter(
          Objects::nonNull).collect(Collectors.toList());
    }
    return modelList;
  }


  public List<SandboxAlertConfigurationAudit> mapAuditByMonthDTOListToModelList(
      List<SandboxAlertConfigurationAuditByMonthDTO> dtoList) {
    List<SandboxAlertConfigurationAudit> auditList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtoList)) {
      auditList = dtoList.stream().filter(Objects::nonNull).map(this::mapAuditDTOByMonthToModel)
          .collect(Collectors.toList());
    }
    return auditList;
  }

  public SandboxAlertConfigurationAudit mapAuditDTOByMonthToModel(
      SandboxAlertConfigurationAuditByMonthDTO dto,
      AlertParameterSetDTO alertParameterSetDTO) {
    SandboxAlertConfigurationAudit model = null;
    if (dto != null) {
      model = populateSandboxAlertConfigurationAudit(
          dto.getPrimaryKey().getSandboxUUID().toString(),
          dto.getPrimaryKey().getAuditUUID().toString(),
          dto.getAlertConfigurationUUID().toString(), dto, alertParameterSetDTO);
    }
    return model;
  }

  private SandboxAlertConfigurationAudit populateSandboxAlertConfigurationAudit(String sandboxId,
      String auditUUID,
      String alertConfigUUID, AlertConfiguration alertConfiguration,
      AlertParameterSetDTO alertParameterSetDTO) {
    SandboxAlertConfigurationAudit model = new SandboxAlertConfigurationAudit();
    model.setSandboxUUID(sandboxId);
    model.setAuditUUID(auditUUID);
    model.setAlertConfigurationUUID(alertConfigUUID);
    model.setAlertConfigType(alertConfiguration.getAlertLogicType());
    model.setWho(alertConfiguration.getCreatedBy());
    model.setWhen(alertConfiguration.getCreatedWhen());
    model.setUpdatedWho(alertConfiguration.getUpdatedBy());
    model.setUpdatedWhen(alertConfiguration.getUpdatedWhen());
    model.setName(alertConfiguration.getName());
    model.setStatus(
        SandboxAlertConfigurationAudit.StatusEnum.fromValue(alertConfiguration.getStatus()));
    model.setComment(alertConfiguration.getComment());

    if (alertParameterSetDTO != null) {
      model.setAlertAggregationFields(
          alertFilterMapper
              .getAggregationFields(alertParameterSetDTO.getAlertAggregationFields()));
      model.setAlertParameters(
          alertFilterMapper.getAlertParameters(alertParameterSetDTO.getAlertParameters()));
      model.setAlertFilters(
          alertFilterMapper.getAlertFilters(alertParameterSetDTO.getAlertFilterDTOS()));
      model.setBusinessUnit(alertParameterSetDTO.getBusinessUnit());
      model.setLogicOverrideSet(alertParameterSetDTO.getLogicOverrides());
    }
    return model;
  }

  public List<SandboxAlertConfigurationAudit> mapAuditDTOListToModelList(
      List<SandboxAlertConfigurationAuditDTO> dtoList) {
    List<SandboxAlertConfigurationAudit> auditList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtoList)) {
      auditList = dtoList.stream().filter(Objects::nonNull)
          .map(this::mapAuditDTOToModel)
          .collect(Collectors.toList());
    }
    return auditList;
  }

  public SandboxAlertConfigurationAudit mapSandboxAuditDTOToModel(
      SandboxAlertConfigurationAuditDTO dto,
      AlertParameterSetDTO alertParameterSetDTO) {
    SandboxAlertConfigurationAudit model = null;
    if (dto != null) {
      model = populateSandboxAlertConfigurationAudit(
          dto.getPrimaryKey().getSandboxUUID().toString(),
          dto.getPrimaryKey().getAuditUUID().toString(),
          dto.getPrimaryKey().getAlertConfigurationUUID().toString(), dto, alertParameterSetDTO);
    }
    return model;
  }

  private SandboxAlertConfigurationAuditByMonthDTO mapCommonAlertConfiguration(
      AlertConfiguration configurationDTO) {

    SandboxAlertConfigurationAuditByMonthDTO auditByMonthDTO = new SandboxAlertConfigurationAuditByMonthDTO();
    auditByMonthDTO.setAlertLogicType(configurationDTO.getAlertLogicType());
    auditByMonthDTO.setApsHash(configurationDTO.getApsHash());
    auditByMonthDTO.setComment(configurationDTO.getComment());
    auditByMonthDTO.setCreatedBy(configurationDTO.getCreatedBy());
    auditByMonthDTO.setCreatedWhen(configurationDTO.getCreatedWhen());
    auditByMonthDTO.setName(configurationDTO.getName());
    auditByMonthDTO.setStatus(configurationDTO.getStatus());
    auditByMonthDTO.setUpdatedBy(configurationDTO.getUpdatedBy());
    auditByMonthDTO.setUpdatedWhen(configurationDTO.getUpdatedWhen());
    return auditByMonthDTO;

  }

  public SandboxAlertConfigurationAuditByMonthDTO mapSandboxDTOToAuditByMonthDTO(
      SandboxAlertConfigurationDTO sandboxAlertConfigurationDTO, UUID auditUUID) {
    SandboxAlertConfigurationAuditByMonthDTO auditByMonthDTO = null;
    if (sandboxAlertConfigurationDTO != null) {
      auditByMonthDTO = mapCommonAlertConfiguration(sandboxAlertConfigurationDTO);
      auditByMonthDTO.setAlertConfigurationUUID(
          sandboxAlertConfigurationDTO.getPrimaryKey().getAlertConfigurationUUID());
      SandboxAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey = new SandboxAlertConfigurationAuditByMonthDTOPrimaryKey(
          YearMonth.now().atDay(1), sandboxAlertConfigurationDTO.getPrimaryKey().getSandboxUUID(),
          auditUUID);
      auditByMonthDTO.setPrimaryKey(primaryKey);
    }
    return auditByMonthDTO;
  }

  public SandboxAlertConfigurationAuditByMonthDTO mapSandboxAuditDTOToAuditByMonthDTO(
      SandboxAlertConfigurationAuditDTO sandboxAlertConfigurationAuditDTO) {
    SandboxAlertConfigurationAuditByMonthDTO auditByMonthDTO = null;
    if (sandboxAlertConfigurationAuditDTO != null) {
      auditByMonthDTO = mapCommonAlertConfiguration(sandboxAlertConfigurationAuditDTO);
      auditByMonthDTO.setAlertConfigurationUUID(
          sandboxAlertConfigurationAuditDTO.getPrimaryKey().getAlertConfigurationUUID());
      SandboxAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey = new SandboxAlertConfigurationAuditByMonthDTOPrimaryKey(
          YearMonth.now().atDay(1),
          sandboxAlertConfigurationAuditDTO.getPrimaryKey().getSandboxUUID(),
          sandboxAlertConfigurationAuditDTO.getPrimaryKey().getAuditUUID());
      auditByMonthDTO.setPrimaryKey(primaryKey);

    }
    return auditByMonthDTO;
  }

  private SandboxAlertConfigurationAudit mapAuditDTOByMonthToModel(
      SandboxAlertConfigurationAuditByMonthDTO dto) {
    return mapAuditDTOByMonthToModel(dto, null);
  }

  private SandboxAlertConfigurationAudit mapAuditDTOToModel(
      SandboxAlertConfigurationAuditDTO dto) {
    return mapSandboxAuditDTOToModel(dto, null);
  }

  private SandboxAlertConfiguration mapSandboxConfigAuditDTOToModelNoParameters(
      SandboxAlertConfigurationDTO sandboxAlertConfigurationDTO) {
    return this.mapSandboxAlertConfigDTOToModel(sandboxAlertConfigurationDTO, null);
  }

  public SandboxAlertConfigurationDTO clone(SandboxAlertConfigurationDTO dto, String name,
      String user) {
    SandboxAlertConfigurationDTO clone = createDTOWithPrimaryKey(
        dto.getPrimaryKey().getSandboxUUID(), UUIDs.timeBased());

    clone.setName(name);
    clone.setLiveConfigUUID(dto.getLiveConfigUUID());
    clone.setApsHash(dto.getApsHash());
    clone.setAlertLogicType(dto.getAlertLogicType());
    clone.setComment(dto.getComment());
    clone.setStatus(dto.getStatus());
    clone.setCreatedBy(user);
    clone.setCreatedWhen(Instant.now());

    return clone;
  }
}
