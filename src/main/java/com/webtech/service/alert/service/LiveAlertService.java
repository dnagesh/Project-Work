package com.webtech.service.alert.service;

import com.datastax.driver.core.utils.UUIDs;
import com.webtech.service.alert.dto.LiveAlertAuditDTO;
import com.webtech.service.alert.dto.LiveAlertDTO;
import com.webtech.service.alert.exception.UpdateStateReasonMissingException;
import com.webtech.service.alert.mapper.LiveAlertObjectMapper;
import com.webtech.service.alert.repository.LiveAlertAuditRepository;
import com.webtech.service.alert.repository.LiveAlertRepository;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.entityrelationship.model.EntityType;
import com.irisium.service.livealert.model.Alert;
import com.irisium.service.livealert.model.Alert.StateEnum;
import com.irisium.service.livealert.model.AlertAudit;
import com.irisium.service.livealert.model.CreateAlertRequest;
import com.irisium.service.livealert.model.UpdateAssigneeRequest;
import com.irisium.service.livealert.model.UpdateStateRequest;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class LiveAlertService {

  private final LiveAlertRepository repository;
  private final LiveAlertAuditRepository auditRepository;
  private final LiveAlertObjectMapper objectMapper;

  public LiveAlertService(LiveAlertRepository repository, LiveAlertAuditRepository auditRepository,
      LiveAlertObjectMapper objectMapper) {
    this.repository = repository;
    this.auditRepository = auditRepository;
    this.objectMapper = objectMapper;
  }

  public Alert createAlert(CreateAlertRequest createAlertRequest) {
    LiveAlertDTO alertDTO = objectMapper.requestToDto(createAlertRequest);
    //Generate Primary Key
    alertDTO.setAlertId(UUIDs.timeBased());

    return auditAndSave(alertDTO);
  }

  public Optional<Alert> getAlertById(String alertId) {
    Optional<LiveAlertDTO> dto = repository.findById(UUID.fromString(alertId));
    Alert alert = null;
    if (dto.isPresent()) {
      alert = objectMapper.dtoToApi(dto.get());
    }
    return Optional.ofNullable(alert);
  }

  public Alert updateAlert(String alertId,
      UpdateStateRequest updateStateRequest, String username)
      throws EntityNotFoundException, UpdateStateReasonMissingException {

    LiveAlertDTO alertDTO = repository.findById(UUID.fromString(alertId))
        .orElseThrow(() -> new EntityNotFoundException(EntityType.ALERT.name(), alertId));

    //check the reason exists where necessary
    if (isUpdateStateCommentMandatory(alertDTO, updateStateRequest) && StringUtils
        .isEmpty(updateStateRequest.getReason())) {
      throw new UpdateStateReasonMissingException(
          "A comment is required for changing the state of an alert : " + alertId);
    }

    //Update state
    alertDTO.setState(updateStateRequest.getState().toString());
    alertDTO.setUpdatedBy(username);
    alertDTO.setUpdatedDate(Instant.now());
    return auditAndSave(alertDTO);
  }

  public Alert updateAssignee(String alertId,
      UpdateAssigneeRequest updateAssigneeRequest, String username) throws EntityNotFoundException {

    LiveAlertDTO alertDTO = repository.findById(UUID.fromString(alertId))
        .orElseThrow(() -> new EntityNotFoundException(EntityType.ALERT.name(), alertId));

    //Update assignee
    alertDTO.setAssignee(updateAssigneeRequest.getAssignee());
    alertDTO.setUpdatedBy(username);
    alertDTO.setUpdatedDate(Instant.now());
    return auditAndSave(alertDTO);
  }

  public List<Alert> getAllAlerts() {
    List<LiveAlertDTO> dtoList = repository.findAll();
    return objectMapper.mapList(dtoList);
  }

  public List<AlertAudit> getAudit(String alertId) throws EntityNotFoundException {
    LiveAlertDTO alertDTO = repository.findById(UUID.fromString(alertId))
        .orElseThrow(() -> new EntityNotFoundException(EntityType.ALERT.name(), alertId));
    List<LiveAlertAuditDTO> alertAuditDTOS = auditRepository
        .findAllByPrimaryKeyAlertId(alertDTO.getAlertId());
    return objectMapper.mapAuditDTOListToApiList(alertAuditDTOS);
  }

  private boolean isUpdateStateCommentMandatory(LiveAlertDTO alertDTO, UpdateStateRequest state) {

    boolean isMandatory = false;
    //validation - open -closed/archived
    if (StateEnum.OPEN == StateEnum.fromValue(alertDTO.getState()) &&
        (UpdateStateRequest.StateEnum.CLOSED == state.getState()
            || UpdateStateRequest.StateEnum.ARCHIVED == state.getState())) {
      isMandatory = true;
    }
    //validation - close - open or archive to open
    if ((StateEnum.CLOSED == StateEnum.fromValue(alertDTO.getState())
        || StateEnum.ARCHIVED == StateEnum.fromValue(alertDTO.getState())) &&
        (UpdateStateRequest.StateEnum.OPEN == state.getState())) {
      isMandatory = true;
    }

    return isMandatory;
  }

  private Alert auditAndSave(LiveAlertDTO alertDTO) {
    LiveAlertAuditDTO auditDTO = objectMapper.alertDTOToAlertAuditDTO(alertDTO);
    auditRepository.save(auditDTO);
    LiveAlertDTO savedAlertDTO = repository.save(alertDTO);
    return objectMapper.dtoToApi(savedAlertDTO);
  }
}
