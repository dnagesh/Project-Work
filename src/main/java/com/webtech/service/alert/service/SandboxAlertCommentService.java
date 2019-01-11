package com.webtech.service.alert.service;

import com.webtech.service.alert.dto.SandboxAlertCommentDTO;
import com.webtech.service.alert.dto.SandboxAlertDTO;
import com.webtech.service.alert.dto.SandboxAlertDTOPrimaryKey;
import com.webtech.service.alert.mapper.SandboxAlertObjectMapper;
import com.webtech.service.alert.repository.SandboxAlertCommentDTORepository;
import com.webtech.service.alert.repository.SandboxAlertDTORepository;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.entityrelationship.model.EntityType;
import com.irisium.service.sandboxalert.model.SandboxAlertComment;
import com.irisium.service.sandboxalert.model.SandboxAlertCommentCreateRequest;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class SandboxAlertCommentService {

  private final SandboxAlertCommentDTORepository repository;
  private final SandboxAlertDTORepository alertRepository;
  private final SandboxAlertObjectMapper mapper;

  public SandboxAlertCommentService(
      SandboxAlertCommentDTORepository repository,
      SandboxAlertDTORepository alertRepository,
      SandboxAlertObjectMapper mapper) {
    this.repository = repository;
    this.alertRepository = alertRepository;
    this.mapper = mapper;
  }

  public SandboxAlertComment addAlertComment(String alertId, String runId, String username,
      SandboxAlertCommentCreateRequest commentCreateRequest)
      throws EntityNotFoundException {

    SandboxAlertDTO sandboxAlertDTO = alertRepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(alertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(),
            "(SandboxAlertId: " + alertId + ", runId: " + runId + ")"));

    SandboxAlertCommentDTO commentDTO = mapper.commentRequestToDto(commentCreateRequest, username);
    commentDTO.setAlertId(sandboxAlertDTO.getPrimaryKey().getAlertId());
    SandboxAlertCommentDTO savedCommentDTO = repository.save(commentDTO);
    return mapper.commentDtoToApi(savedCommentDTO);
  }

  public List<SandboxAlertComment> getCommentsForAlert(String alertId, String runId)
      throws EntityNotFoundException {
    SandboxAlertDTO sandboxAlertDTO = alertRepository.findById(new SandboxAlertDTOPrimaryKey(
        UUID.fromString(runId), UUID.fromString(alertId))).orElseThrow(
        () -> new EntityNotFoundException(EntityType.SANDBOXALERT.name(),
            "(SandboxAlertId: " + alertId + ", runId: " + runId + ")"));
    List<SandboxAlertCommentDTO> commentDtoList = repository
        .findAllByAlertId(sandboxAlertDTO.getPrimaryKey().getAlertId());
    return mapper.mapCommentsList(commentDtoList);
  }
}
