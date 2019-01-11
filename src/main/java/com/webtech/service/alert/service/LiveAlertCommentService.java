package com.webtech.service.alert.service;

import com.datastax.driver.core.utils.UUIDs;
import com.webtech.service.alert.dto.LiveAlertCommentDTO;
import com.webtech.service.alert.dto.LiveAlertDTO;
import com.webtech.service.alert.mapper.LiveAlertObjectMapper;
import com.webtech.service.alert.repository.LiveAlertCommentRepository;
import com.webtech.service.alert.repository.LiveAlertRepository;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.entityrelationship.model.EntityType;
import com.irisium.service.livealert.model.Comment;
import com.irisium.service.livealert.model.CommentCreateRequest;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Component;

@Component
public class LiveAlertCommentService {

  private final LiveAlertCommentRepository repository;
  private final LiveAlertRepository alertRepository;
  private final LiveAlertObjectMapper mapper;

  public LiveAlertCommentService(LiveAlertCommentRepository repository,
      LiveAlertRepository alertRepository,
      LiveAlertObjectMapper mapper) {
    this.repository = repository;
    this.alertRepository = alertRepository;
    this.mapper = mapper;
  }

  public Comment addAlertComment(String alertId, String username,
      CommentCreateRequest commentCreateRequest)
      throws EntityNotFoundException {

    LiveAlertDTO currentAlertDTO = alertRepository.findById(UUID.fromString(alertId))
        .orElseThrow(() -> new EntityNotFoundException(EntityType.ALERT.name(), alertId));

    LiveAlertCommentDTO dto = mapper.commentRequestToDto(commentCreateRequest, username);
    dto.setAlertId(currentAlertDTO.getAlertId());

    //Generate a comment Id
    dto.setCommentId(UUIDs.timeBased());
    //Set creation time
    dto.setCreationTime(Instant.now());

    LiveAlertCommentDTO commentDTO = repository.save(dto);
    return mapper.commentDtoToApi(commentDTO);
  }

  public List<Comment> getCommentsForAlert(String alertId) {
    List<LiveAlertCommentDTO> commentDtoList = repository
        .findAllByAlertId(UUID.fromString(alertId));
    return mapper.mapCommentsList(commentDtoList);
  }
}
