package com.webtech.service.alert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alert.LiveAlertTestObjects;
import com.webtech.service.alert.dto.LiveAlertCommentDTO;
import com.webtech.service.alert.dto.LiveAlertDTO;
import com.webtech.service.alert.mapper.LiveAlertObjectMapper;
import com.webtech.service.alert.repository.LiveAlertCommentRepository;
import com.webtech.service.alert.repository.LiveAlertRepository;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.irisium.service.livealert.model.Comment;
import com.irisium.service.livealert.model.CommentCreateRequest;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveAlertCommentServiceTest {

  @Mock
  private LiveAlertCommentRepository mockRepository;

  @Mock
  private LiveAlertRepository mockAlertRepository;

  private LiveAlertObjectMapper objectMapper;

  private LiveAlertCommentService commentService;

  @Captor
  private ArgumentCaptor<LiveAlertCommentDTO> argumentDTO;

  private CommentCreateRequest commentCreateRequest;
  private UUID alertId;
  private LiveAlertDTO alertDTO;

  @Before
  public void setup() {
    objectMapper = new LiveAlertObjectMapper();
    commentService = new LiveAlertCommentService(mockRepository, mockAlertRepository, objectMapper);
    commentCreateRequest = LiveAlertTestObjects.getCommentCreateRequest();

    alertId = TestUtils.randomUUID();
    alertDTO = LiveAlertTestObjects
        .getAlertDTO(alertId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");
    when(mockAlertRepository.findById(alertId)).thenReturn(Optional.ofNullable(alertDTO));
  }

  @Test
  public void shouldReturnAlertNotFoundWhenAddCommentWithInvalidAlert() {
    when(mockAlertRepository.findById(alertId)).thenReturn(Optional.empty());
    try {
      commentService.addAlertComment(alertId.toString(), "User1", commentCreateRequest);
      fail("Cannot add comment for Non-existent Alert");
    } catch (EntityNotFoundException e) {
      verify(mockAlertRepository).findById(alertId);
    }
  }


  @Test
  public void shouldReturnAlertForAddCommentWithValidData() {

    try {
      commentService.addAlertComment(alertId.toString(), "User1", commentCreateRequest);
    } catch (EntityNotFoundException e) {
      fail("Cannot add comment for Non-existent Alert");
    }

    verify(mockRepository).save(argumentDTO.capture());
    assertThat(argumentDTO.getValue().getAlertId())
        .isEqualTo(alertId);
    assertThat(argumentDTO.getValue().getUsername()).isEqualTo("User1");
    assertThat(argumentDTO.getValue().getComment()).isEqualTo(commentCreateRequest.getComment());
    assertThat(argumentDTO.getValue().getCommentId()).isNotNull();
    assertThat(argumentDTO.getValue().getCreationTime()).isNotNull();
  }

  @Test
  public void shouldReturnEmptyWhenNoResultsForGetCommentsForAlert() {
    List<Comment> comments = commentService.getCommentsForAlert(TestUtils.randomUUID().toString());
    assertThat(comments).isEmpty();
  }

  @Test
  public void shouldReturnDataWhenResultsFoundForGetCommentsForAlert() {
    List<LiveAlertCommentDTO> dtoList = LiveAlertTestObjects.getCommentDTOList();
    when(mockRepository.findAllByAlertId(any())).thenReturn(dtoList);
    List<Comment> comments = commentService.getCommentsForAlert(TestUtils.randomUUID().toString());
    assertThat(comments).hasSize(dtoList.size());
  }
}
