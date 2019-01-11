package com.webtech.service.alert.service;

import com.webtech.service.alert.dto.SandboxAlertAuditDTO;
import com.webtech.service.alert.dto.SandboxAlertDTO;
import com.webtech.service.alert.dto.SandboxAlertDTOPrimaryKey;
import com.webtech.service.alert.mapper.SandboxAlertObjectMapper;
import com.webtech.service.alert.repository.SandboxAlertAuditDTORepository;
import com.webtech.service.alert.repository.SandboxAlertDTORepository;
import com.webtech.service.alertconfiguration.service.SandboxRunService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.entityrelationship.model.EntityType;
import com.irisium.service.sandboxalert.model.CreateSandboxAlertRequest;
import com.irisium.service.sandboxalert.model.SandboxAlert;
import com.irisium.service.sandboxalert.model.SandboxAlertAudit;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertAssigneeRequest;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertStateRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SandboxAlertService {

  private static final String SANDBOX_ALERT_ID = "(SandboxAlertId: %s, runId: %s )";
  private final SandboxAlertDTORepository sandboxAlertDTORepository;
  private final SandboxAlertAuditDTORepository auditDTORepository;
  private final SandboxAlertObjectMapper mapper;
  private final SandboxRunService sandboxRunService;

  public SandboxAlertService(
      SandboxAlertDTORepository sandboxAlertDTORepository,
      SandboxAlertAuditDTORepository auditDTORepository,
      SandboxAlertObjectMapper mapper,
      SandboxRunService sandboxRunService) {
    this.sandboxAlertDTORepository = sandboxAlertDTORepository;
    this.auditDTORepository = auditDTORepository;
    this.mapper = mapper;
    this.sandboxRunService = sandboxRunService;
  }

  public SandboxAlert createSandboxAlert(CreateSandboxAlertRequest alertCreateRequest) {
    SandboxAlertDTO dto = mapper.requestToDTO(alertCreateRequest);
    return auditAndSave(dto);
  }

  public SandboxAlert getSandboxAlertByIdAndRunId(String sandboxAlertId, String runId)
      throws EntityNotFoundException {
    SandboxAlertDTO alertDTO = sandboxAlertDTORepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(sandboxAlertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(), String.format(
            SANDBOX_ALERT_ID, sandboxAlertId, runId)));

    return mapper.dtoToApi(alertDTO);
  }

  public List<SandboxAlertAudit> getAuditForSandboxAlert(String sandboxAlertId, String runId)
      throws EntityNotFoundException {
    SandboxAlertDTO alertDTO = sandboxAlertDTORepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(sandboxAlertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(), String.format(
            SANDBOX_ALERT_ID, sandboxAlertId, runId)));
    List<SandboxAlertAuditDTO> audits = auditDTORepository
        .findAllByPrimaryKeyAlertId(alertDTO.getPrimaryKey().getAlertId());
    return mapper.auditDTOListToApi(audits);
  }

  public List<SandboxAlert> getAllSandboxAlerts(String sandboxId, String runId) {
    if (StringUtils.isEmpty(sandboxId) && StringUtils.isEmpty(runId)) {
      return getAllSandboxAlertsInSystem();
    } else if (!StringUtils.isEmpty(runId)) {
      return getAllSandboxAlertsByRunId(runId);
    } else {
      return getAllSandboxAlertsBySandboxId(sandboxId);
    }
  }

  private List<SandboxAlert> getAllSandboxAlertsByRunId(String runId) {
    List<SandboxAlertDTO> dtoList = sandboxAlertDTORepository.findAllByPrimaryKeyRunId(
        UUID.fromString(runId));
    return mapper.dtoListToApi(dtoList);
  }

  private List<SandboxAlert> getAllSandboxAlertsBySandboxId(String sandboxId) {
    List<UUID> runIds = sandboxRunService.getAllRunIdsBySandboxId(sandboxId);
    List<SandboxAlertDTO> dtoList = sandboxAlertDTORepository.findAllByRunIds(runIds);
    return mapper.dtoListToApi(dtoList);
  }

  private List<SandboxAlert> getAllSandboxAlertsInSystem() {
    List<SandboxAlertDTO> dtoList = sandboxAlertDTORepository.findAll();
    return mapper.dtoListToApi(dtoList);
  }

  public SandboxAlert updateAlert(String sandboxAlertId, String runId,
      UpdateSandboxAlertStateRequest updateStateRequest, String user)
      throws EntityNotFoundException {

    SandboxAlertDTO alertDTO = sandboxAlertDTORepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(sandboxAlertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(), String.format(
            SANDBOX_ALERT_ID, sandboxAlertId, runId)));

    //Update state
    alertDTO.setState(updateStateRequest.getState().toString());

    return update(alertDTO, user);
  }

  private SandboxAlert update(SandboxAlertDTO alertDTO, String user) {
    alertDTO.setUpdatedBy(user);
    alertDTO.setUpdatedDate(Instant.now());
    return auditAndSave(alertDTO);
  }

  private SandboxAlert auditAndSave(SandboxAlertDTO alertDTO) {
    auditDTORepository.save(mapper.dtoToAuditDTO(alertDTO));
    SandboxAlertDTO savedDTO = sandboxAlertDTORepository.save(alertDTO);
    return mapper.dtoToApi(savedDTO);
  }

  public SandboxAlert updateAssignee(String sandboxAlertId, String runId,
      UpdateSandboxAlertAssigneeRequest updateAssigneeRequest, String user)
      throws EntityNotFoundException {

    SandboxAlertDTO alertDTO = sandboxAlertDTORepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(sandboxAlertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(), String.format(
            SANDBOX_ALERT_ID, sandboxAlertId, runId)));

    //Update assignee
    alertDTO.setAssignee(updateAssigneeRequest.getAssignee());

    return update(alertDTO, user);
  }

  public SandboxAlert promoteSandboxAlertToLive(SandboxAlert sandboxAlert, String user)
      throws EntityNotFoundException {

    SandboxAlertDTO alertDTO = sandboxAlertDTORepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(sandboxAlert.getRunId()), UUID.fromString(sandboxAlert.getAlertId())))
        .orElseThrow(
            () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(), String.format(
                SANDBOX_ALERT_ID, sandboxAlert.getAlertId(), sandboxAlert.getRunId())));

    alertDTO.setPromotedToLive(true);

    return update(alertDTO, user);
  }
}
