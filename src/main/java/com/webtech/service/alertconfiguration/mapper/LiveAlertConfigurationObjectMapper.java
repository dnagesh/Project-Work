package com.webtech.service.alertconfiguration.mapper;

import com.datastax.driver.core.utils.UUIDs;
import com.webtech.service.alertconfiguration.dto.AlertConfiguration;
import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.LiveAlertConfigurationAudit;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class LiveAlertConfigurationObjectMapper {

  private final AlertFilterMapper alertFilterMapper;

  public LiveAlertConfigurationObjectMapper(
      AlertFilterMapper alertFilterMapper) {
    this.alertFilterMapper = alertFilterMapper;
  }

  private LiveAlertConfiguration liveDTOToModel(
      LiveAlertConfigurationDTO liveAlertConfigurationDTO) {
    return this.liveDTOToModelWithParameterSet(liveAlertConfigurationDTO, null);
  }

  public LiveAlertConfiguration liveDTOToModelWithParameterSet(LiveAlertConfigurationDTO dto,
      AlertParameterSetDTO alertParameterSetDTO) {
    LiveAlertConfiguration model = null;
    if (dto != null) {
      model = new LiveAlertConfiguration();
      model.setAlertConfigurationUUID(dto.getUuid().toString());
      model.setName(dto.getName());
      model.setAlertConfigType(dto.getAlertLogicType());
      model.setApsHash(dto.getApsHash());
      model.setComment(dto.getComment());
      model.setStatus(LiveAlertConfiguration.StatusEnum.fromValue(dto.getStatus()));
      model.setWho(dto.getCreatedBy());
      model.setWhen(dto.getCreatedWhen());
      model.setUpdatedWho(dto.getUpdatedBy());
      model.setUpdatedWhen(dto.getUpdatedWhen());
      if (alertParameterSetDTO != null) {
        model.setBusinessUnit(alertParameterSetDTO.getBusinessUnit());
        model.setAlertAggregationFields(
            alertFilterMapper
                .getAggregationFields(alertParameterSetDTO.getAlertAggregationFields()));
        model.setAlertParameters(
            alertFilterMapper.getAlertParameters(alertParameterSetDTO.getAlertParameters()));
        model.setAlertFilters(
            alertFilterMapper.getAlertFilters(alertParameterSetDTO.getAlertFilterDTOS()));
        model.setLogicOverrideSet(alertParameterSetDTO.getLogicOverrides());
      }
    }
    return model;
  }

  public List<LiveAlertConfiguration> mapDTOListToModelList(
      List<LiveAlertConfigurationDTO> dtoList) {
    List<LiveAlertConfiguration> modelList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtoList)) {
      modelList = dtoList.stream().map(this::liveDTOToModel).collect(Collectors.toList());
    }
    return modelList;
  }


  public List<LiveAlertConfigurationAudit> mapAuditByMonthDTOListToModelList(
      List<LiveAlertConfigurationAuditByMonthDTO> dtoList) {
    List<LiveAlertConfigurationAudit> auditList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtoList)) {
      auditList = dtoList.stream().filter(Objects::nonNull).map(this::mapAuditDTOByMonthToModel)
          .collect(Collectors.toList());
    }
    return auditList;
  }

  private LiveAlertConfigurationAudit mapAuditDTOByMonthToModel(
      LiveAlertConfigurationAuditByMonthDTO dto) {
    return mapAuditDTOByMonthToModel(dto, null);
  }

  public LiveAlertConfigurationAudit mapAuditDTOByMonthToModel(
      LiveAlertConfigurationAuditByMonthDTO dto,
      AlertParameterSetDTO alertParameterSetDTO) {
    LiveAlertConfigurationAudit model = null;
    if (dto != null) {
      model = populateLiveAlertConfigurationAudit(dto.getPrimaryKey().getAuditUUID().toString(),
          dto.getAlertConfigurationUUID().toString(), dto, alertParameterSetDTO);
    }
    return model;
  }

  private LiveAlertConfigurationAudit populateLiveAlertConfigurationAudit(String auditUUID,
      String alertConfigUUID, AlertConfiguration alertConfiguration,
      AlertParameterSetDTO alertParameterSetDTO) {
    LiveAlertConfigurationAudit model = new LiveAlertConfigurationAudit();
    model.setAuditUUID(auditUUID);
    model.setAlertConfigurationUUID(alertConfigUUID);
    model.setAlertConfigType(alertConfiguration.getAlertLogicType());
    model.setWho(alertConfiguration.getCreatedBy());
    model.setWhen(alertConfiguration.getCreatedWhen());
    model.setUpdatedWho(alertConfiguration.getUpdatedBy());
    model.setUpdatedWhen(alertConfiguration.getUpdatedWhen());
    model.setName(alertConfiguration.getName());
    model.setStatus(
        LiveAlertConfigurationAudit.StatusEnum.fromValue(alertConfiguration.getStatus()));
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

  public List<LiveAlertConfigurationAudit> mapAuditByAlertConfigUUIDDTOListToModelList(
      List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> dtoList) {
    List<LiveAlertConfigurationAudit> auditList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(dtoList)) {
      auditList = dtoList.stream().filter(Objects::nonNull)
          .map(this::mapAuditDTOByAlertConfigUUIDToModel)
          .collect(Collectors.toList());
    }
    return auditList;
  }

  private LiveAlertConfigurationAudit mapAuditDTOByAlertConfigUUIDToModel(
      LiveAlertConfigurationAuditByAlertConfigUUIDDTO dto) {
    return mapAuditDTOByAlertConfigUUIDToModel(dto, null);
  }

  public LiveAlertConfigurationAudit mapAuditDTOByAlertConfigUUIDToModel(
      LiveAlertConfigurationAuditByAlertConfigUUIDDTO dto,
      AlertParameterSetDTO alertParameterSetDTO) {
    LiveAlertConfigurationAudit model = null;
    if (dto != null) {
      model = populateLiveAlertConfigurationAudit(dto.getAuditUUID().toString(),
          dto.getPrimaryKey().getAlertConfigUUID().toString(), dto, alertParameterSetDTO);
    }
    return model;
  }

  public LiveAlertConfigurationAudit mapLiveAuditDTOToModel(LiveAlertConfigurationAuditDTO dto,
      AlertParameterSetDTO alertParameterSetDTO) {
    LiveAlertConfigurationAudit model = null;
    if (dto != null) {
      model = populateLiveAlertConfigurationAudit(dto.getAuditUUID().toString(),
          dto.getAlertConfigurationUUID().toString(), dto, alertParameterSetDTO);
    }
    return model;
  }


  public LiveAlertConfigurationDTO mapSandboxConfigDTOToLiveConfigDTO(
      SandboxAlertConfigurationDTO sourceDTO, String user) {
    LiveAlertConfigurationDTO dto = null;
    if (sourceDTO != null) {
      dto = new LiveAlertConfigurationDTO();

      //Update existing record with current liveConfigUUID or insert new with new UUID
      if (sourceDTO.getLiveConfigUUID() != null) {
        dto.setUuid(sourceDTO.getLiveConfigUUID());
        dto.setUpdatedBy(user);
        dto.setUpdatedWhen(Instant.now());
      } else {
        dto.setUuid(UUIDs.timeBased());
        dto.setCreatedBy(user);
        dto.setCreatedWhen(Instant.now());
      }
      dto.setApsHash(sourceDTO.getApsHash());
      dto.setAlertLogicType(sourceDTO.getAlertLogicType());
      dto.setComment(sourceDTO.getComment());
      dto.setName(sourceDTO.getName());
      dto.setStatus(sourceDTO.getStatus());

    }
    return dto;
  }


  public LiveAlertConfigurationAuditDTO mapLiveDTOToAuditDTO(
      LiveAlertConfigurationDTO liveAlertConfigurationDTO) {
    LiveAlertConfigurationAuditDTO auditDTO = null;
    if (liveAlertConfigurationDTO != null) {
      auditDTO = new LiveAlertConfigurationAuditDTO();
      auditDTO.setAlertLogicType(liveAlertConfigurationDTO.getAlertLogicType());
      auditDTO.setAlertConfigurationUUID(liveAlertConfigurationDTO.getUuid());
      auditDTO.setApsHash(liveAlertConfigurationDTO.getApsHash());
      auditDTO.setComment(liveAlertConfigurationDTO.getComment());
      auditDTO.setCreatedBy(liveAlertConfigurationDTO.getCreatedBy());
      auditDTO.setCreatedWhen(liveAlertConfigurationDTO.getCreatedWhen());
      auditDTO.setName(liveAlertConfigurationDTO.getName());
      auditDTO.setStatus(liveAlertConfigurationDTO.getStatus());
      auditDTO.setUpdatedBy(liveAlertConfigurationDTO.getUpdatedBy());
      auditDTO.setUpdatedWhen(liveAlertConfigurationDTO.getUpdatedWhen());

      auditDTO.setAuditUUID(UUIDs.timeBased());
    }
    return auditDTO;
  }

  public LiveAlertConfigurationAuditByAlertConfigUUIDDTO mapLiveDTOToAuditByAlertConfigUUIDDTO(
      LiveAlertConfigurationDTO liveAlertConfigurationDTO) {
    LiveAlertConfigurationAuditByAlertConfigUUIDDTO auditDTO = null;
    if (liveAlertConfigurationDTO != null) {
      auditDTO = new LiveAlertConfigurationAuditByAlertConfigUUIDDTO();
      auditDTO.setAlertLogicType(liveAlertConfigurationDTO.getAlertLogicType());
      auditDTO.setApsHash(liveAlertConfigurationDTO.getApsHash());
      auditDTO.setComment(liveAlertConfigurationDTO.getComment());
      auditDTO.setCreatedBy(liveAlertConfigurationDTO.getCreatedBy());
      auditDTO.setCreatedWhen(liveAlertConfigurationDTO.getCreatedWhen());
      auditDTO.setName(liveAlertConfigurationDTO.getName());
      auditDTO.setStatus(liveAlertConfigurationDTO.getStatus());
      auditDTO.setUpdatedBy(liveAlertConfigurationDTO.getUpdatedBy());
      auditDTO.setUpdatedWhen(liveAlertConfigurationDTO.getUpdatedWhen());

      LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey primaryKey = new LiveAlertConfigurationAuditByAlertConfigUUIDDTOPrimaryKey(
          liveAlertConfigurationDTO.getUuid(),
          liveAlertConfigurationDTO.getCreatedWhen() == null ? liveAlertConfigurationDTO
              .getUpdatedWhen() : liveAlertConfigurationDTO.getCreatedWhen());
      auditDTO.setPrimaryKey(primaryKey);
    }
    return auditDTO;
  }

  public List<LiveAlertConfigurationAudit> mapAuditByAlertConfigDTOListToModelList(
      List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> audits) {
    List<LiveAlertConfigurationAudit> modelList = Collections.emptyList();
    if (!CollectionUtils.isEmpty(audits)) {
      modelList = audits.stream().map(this::mapAuditByAlertConfigDTOToModel)
          .filter(Objects::nonNull).collect(Collectors.toList());
    }
    return modelList;
  }

  public LiveAlertConfigurationAudit mapAuditByAlertConfigDTOToModel(
      LiveAlertConfigurationAuditByAlertConfigUUIDDTO dto) {
    LiveAlertConfigurationAudit model = null;
    if (dto != null) {
      model = new LiveAlertConfigurationAudit();
      model.setAlertConfigurationUUID(dto.getPrimaryKey().getAlertConfigUUID().toString());
      model.setAlertConfigType(dto.getAlertLogicType());
      model.setWho(dto.getCreatedBy());
      model.setWhen(dto.getCreatedWhen());
      model.setUpdatedWho(dto.getUpdatedBy());
      model.setUpdatedWhen(dto.getUpdatedWhen());
      model.setName(dto.getName());
      model.setStatus(LiveAlertConfigurationAudit.StatusEnum.fromValue(dto.getStatus()));
      model.setComment(dto.getComment());
    }
    return model;
  }

  public LiveAlertConfigurationAuditByMonthDTO mapLiveDTOToAuditByMonthDTO(
      LiveAlertConfigurationDTO liveAlertConfigurationDTO) {
    LiveAlertConfigurationAuditByMonthDTO auditByMonthDTO = null;
    if (liveAlertConfigurationDTO != null) {
      auditByMonthDTO = new LiveAlertConfigurationAuditByMonthDTO();
      auditByMonthDTO.setAlertLogicType(liveAlertConfigurationDTO.getAlertLogicType());
      auditByMonthDTO.setApsHash(liveAlertConfigurationDTO.getApsHash());
      auditByMonthDTO.setComment(liveAlertConfigurationDTO.getComment());
      auditByMonthDTO.setCreatedBy(liveAlertConfigurationDTO.getCreatedBy());
      auditByMonthDTO.setCreatedWhen(liveAlertConfigurationDTO.getCreatedWhen());
      auditByMonthDTO.setName(liveAlertConfigurationDTO.getName());
      auditByMonthDTO.setStatus(liveAlertConfigurationDTO.getStatus());
      auditByMonthDTO.setAlertConfigurationUUID(liveAlertConfigurationDTO.getUuid());
      auditByMonthDTO.setUpdatedBy(liveAlertConfigurationDTO.getUpdatedBy());
      auditByMonthDTO.setUpdatedWhen(liveAlertConfigurationDTO.getUpdatedWhen());

      LiveAlertConfigurationAuditByMonthDTOPrimaryKey primaryKey = new LiveAlertConfigurationAuditByMonthDTOPrimaryKey(
          YearMonth.now().atDay(1), null);
      auditByMonthDTO.setPrimaryKey(primaryKey);
    }
    return auditByMonthDTO;
  }
}
