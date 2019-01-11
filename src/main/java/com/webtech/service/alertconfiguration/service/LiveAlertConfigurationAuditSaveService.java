package com.webtech.service.alertconfiguration.service;

import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.mapper.LiveAlertConfigurationObjectMapper;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByAlertConfigUUIDRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByMonthRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditRepository;
import org.springframework.stereotype.Service;

@Service
public class LiveAlertConfigurationAuditSaveService {

  private final LiveAlertConfigurationAuditRepository liveAlertConfigurationAuditRepository;
  private final LiveAlertConfigurationAuditByAlertConfigUUIDRepository auditByAlertConfigUUIDRepository;
  private final LiveAlertConfigurationObjectMapper liveAlertConfigurationObjectMapper;
  private final LiveAlertConfigurationAuditByMonthRepository auditByMonthRepository;

  public LiveAlertConfigurationAuditSaveService(
      LiveAlertConfigurationAuditRepository liveAlertConfigurationAuditRepository,
      LiveAlertConfigurationAuditByAlertConfigUUIDRepository auditByAlertConfigUUIDRepository,
      LiveAlertConfigurationObjectMapper liveAlertConfigurationObjectMapper,
      LiveAlertConfigurationAuditByMonthRepository auditByMonthRepository) {
    this.liveAlertConfigurationAuditRepository = liveAlertConfigurationAuditRepository;
    this.auditByAlertConfigUUIDRepository = auditByAlertConfigUUIDRepository;
    this.liveAlertConfigurationObjectMapper = liveAlertConfigurationObjectMapper;
    this.auditByMonthRepository = auditByMonthRepository;
  }

  public void saveAudits(LiveAlertConfigurationDTO liveAlertConfigurationDTO) {
    if (liveAlertConfigurationDTO == null) {
      return;
    }
    //Ensure the auditUUID is same for all three audit objects
    LiveAlertConfigurationAuditDTO auditDTO = liveAlertConfigurationObjectMapper
        .mapLiveDTOToAuditDTO(liveAlertConfigurationDTO);
    LiveAlertConfigurationAuditByAlertConfigUUIDDTO auditByAlertConfigUUIDDTO = liveAlertConfigurationObjectMapper
        .mapLiveDTOToAuditByAlertConfigUUIDDTO(liveAlertConfigurationDTO);
    LiveAlertConfigurationAuditByMonthDTO auditByMonthDTO = liveAlertConfigurationObjectMapper
        .mapLiveDTOToAuditByMonthDTO(liveAlertConfigurationDTO);

    auditByMonthDTO.getPrimaryKey().setAuditUUID(auditDTO.getAuditUUID());
    auditByAlertConfigUUIDDTO.setAuditUUID(auditDTO.getAuditUUID());

    //Save all three audits
    liveAlertConfigurationAuditRepository.save(auditDTO);
    auditByAlertConfigUUIDRepository.save(auditByAlertConfigUUIDDTO);
    auditByMonthRepository.save(auditByMonthDTO);
  }
}
