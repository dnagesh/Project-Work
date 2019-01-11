package com.webtech.service.alertconfiguration.service;

import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.AuditNotFoundException;
import com.webtech.service.alertconfiguration.mapper.LiveAlertConfigurationObjectMapper;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.LiveAlertConfigurationAudit;
import com.webtech.service.alertconfiguration.repository.AlertParameterSetRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByAlertConfigUUIDRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByMonthRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationRepository;
import com.webtech.service.common.AppPropertiesProvider;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.springframework.stereotype.Service;

@Service
public class LiveAlertConfigurationQueryService {

  private final AlertParameterSetRepository alertParameterSetRepository;
  private final LiveAlertConfigurationRepository liveAlertConfigurationRepository;
  private final LiveAlertConfigurationAuditRepository liveAlertConfigurationAuditRepository;
  private final LiveAlertConfigurationAuditByMonthRepository auditByMonthRepository;
  private final LiveAlertConfigurationObjectMapper liveAlertConfigurationObjectMapper;
  private final LiveAlertConfigurationAuditByAlertConfigUUIDRepository auditByAlertConfigUUIDRepository;

  private final AppPropertiesProvider appPropertiesProvider;

  public LiveAlertConfigurationQueryService(
      AlertParameterSetRepository alertParameterSetRepository,
      LiveAlertConfigurationRepository liveAlertConfigurationRepository,
      LiveAlertConfigurationAuditRepository liveAlertConfigurationAuditRepository,
      LiveAlertConfigurationAuditByMonthRepository auditByMonthRepository,
      LiveAlertConfigurationObjectMapper liveAlertConfigurationObjectMapper,
      LiveAlertConfigurationAuditByAlertConfigUUIDRepository auditByAlertConfigUUIDRepository,
      AppPropertiesProvider appPropertiesProvider) {
    this.alertParameterSetRepository = alertParameterSetRepository;
    this.liveAlertConfigurationRepository = liveAlertConfigurationRepository;
    this.liveAlertConfigurationAuditRepository = liveAlertConfigurationAuditRepository;
    this.auditByMonthRepository = auditByMonthRepository;
    this.liveAlertConfigurationObjectMapper = liveAlertConfigurationObjectMapper;
    this.auditByAlertConfigUUIDRepository = auditByAlertConfigUUIDRepository;
    this.appPropertiesProvider = appPropertiesProvider;
  }

  public List<LiveAlertConfiguration> getAllLiveAlertConfigurations() {
    List<LiveAlertConfigurationDTO> dtoList = liveAlertConfigurationRepository.findAll();
    return liveAlertConfigurationObjectMapper.mapDTOListToModelList(dtoList);
  }

  public List<LiveAlertConfigurationAudit> getAllLiveAlertConfigurationsAudit(
      Integer requestedNumberOfRecords, Integer maxAge) {

    Integer numberOfRecords = requestedNumberOfRecords;
    IntStream range = IntStream.rangeClosed(appPropertiesProvider.getMinAuditRowCountLimit(),
        appPropertiesProvider.getMaxAuditRowCountLimit());
    if (range.noneMatch(num -> Integer.valueOf(num).equals(requestedNumberOfRecords))) {
      numberOfRecords = appPropertiesProvider.getDefaultAuditRowCountLimit();
    }

    List<LocalDate> months = getApplicableMonths(maxAge);

    List<LiveAlertConfigurationAuditByMonthDTO> audits = auditByMonthRepository
        .findLatestTopNAuditsWithAgeLimit(months, numberOfRecords);
    List<LiveAlertConfigurationAuditByMonthDTO> result = audits;

    //If result is more than numOfRecords, trim the excess
    if (audits.size() > numberOfRecords) {
      result = audits.subList(0, numberOfRecords);
    }

    return liveAlertConfigurationObjectMapper.mapAuditByMonthDTOListToModelList(result);

  }

  private List<LocalDate> getApplicableMonths(Integer maxAge) {
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

  public Optional<LiveAlertConfiguration> getLiveAlertConfigurationById(
      String alertConfigurationUUID) {
    Optional<LiveAlertConfigurationDTO> dto = liveAlertConfigurationRepository
        .findById(UUID.fromString(alertConfigurationUUID));

    AlertParameterSetDTO parameterSetDTO = null;
    if (dto.isPresent()) {
      parameterSetDTO = alertParameterSetRepository.findById(dto.get().getApsHash())
          .orElse(null);
      return Optional.ofNullable(liveAlertConfigurationObjectMapper
          .liveDTOToModelWithParameterSet(dto.get(), parameterSetDTO));
    }
    return Optional.empty();
  }

  public LiveAlertConfigurationAudit getLiveAlertConfigurationAuditById(
      String alertConfigurationUUID, String auditUUID)
      throws AuditNotFoundException, AlertConfigurationNotFoundException {
    if (!liveAlertConfigurationRepository
        .existsById(UUID.fromString(alertConfigurationUUID))) {
      throw new AlertConfigurationNotFoundException(alertConfigurationUUID);
    }

    LiveAlertConfigurationAuditDTO auditDTO = liveAlertConfigurationAuditRepository
        .findByAuditUUID(UUID.fromString(auditUUID)).orElseThrow(
            () -> new AuditNotFoundException(
                "Failed to find audit for Alert Configuration " + alertConfigurationUUID
                    + " with UUID " + auditUUID));

    AlertParameterSetDTO parameterSetDTO = alertParameterSetRepository
        .findById(auditDTO.getApsHash())
        .orElse(null);

    return liveAlertConfigurationObjectMapper.mapLiveAuditDTOToModel(auditDTO, parameterSetDTO);
  }

  public List<LiveAlertConfigurationAudit> getAuditHistoryForLiveAlertConfiguration(
      String alertConfigurationUUID) throws AlertConfigurationNotFoundException {

    LiveAlertConfigurationDTO dto = liveAlertConfigurationRepository
        .findById(UUID.fromString(alertConfigurationUUID)).orElseThrow(
            () -> new AlertConfigurationNotFoundException(alertConfigurationUUID));

    List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> audits = auditByAlertConfigUUIDRepository
        .findByPrimaryKeyAlertConfigUUID(dto.getUuid());
    return liveAlertConfigurationObjectMapper.mapAuditByAlertConfigUUIDDTOListToModelList(audits);
  }

}
