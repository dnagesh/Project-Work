package com.webtech.service.alert.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alert.SandboxAlertTestObjects;
import com.webtech.service.alert.dto.SandboxAlertCommentDTO;
import com.webtech.service.alert.dto.SandboxAlertDTO;
import com.webtech.service.alert.dto.SandboxAlertDTOPrimaryKey;
import com.webtech.service.alert.mapper.SandboxAlertObjectMapper;
import com.webtech.service.alert.repository.SandboxAlertCommentDTORepository;
import com.webtech.service.alert.repository.SandboxAlertDTORepository;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.irisium.service.sandboxalert.model.SandboxAlertComment;
import com.irisium.service.sandboxalert.model.SandboxAlertCommentCreateRequest;
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
public class SandboxAlertCommentServiceTest {

  @Captor
  ArgumentCaptor<SandboxAlertDTOPrimaryKey> primaryKeyArgumentCaptor;
  UUID alertId;
  UUID runId;
  private SandboxAlertCommentService commentService;
  @Mock
  private SandboxAlertCommentDTORepository mockRepository;
  @Mock
  private SandboxAlertDTORepository mockAlertRepository;
  private SandboxAlertObjectMapper mapper;
  @Captor
  private ArgumentCaptor<SandboxAlertCommentDTO> argumentDTO;

  @Before
  public void setUp() throws Exception {
    mapper = new SandboxAlertObjectMapper();
    commentService = new SandboxAlertCommentService(mockRepository, mockAlertRepository, mapper);
    alertId = TestUtils.randomUUID();
    runId = TestUtils.randomUUID();
    SandboxAlertDTO alertDTO = SandboxAlertTestObjects
        .getAlertDTO(alertId, runId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", new HashSet<>(Arrays.asList("Eleis Commodities")),
            "Wash Trade", "Europe/Equity", new HashSet(Arrays.asList("Regulatory", "Operational")),
            "Dave Jones");

    when(mockAlertRepository.findById(any())).thenReturn(Optional.ofNullable(alertDTO));
  }

  @Test
  public void shouldReturnAlertNotFoundForAddCommentWithInvalidAlert() {
    SandboxAlertCommentCreateRequest commentCreateRequest = SandboxAlertTestObjects
        .getCommentCreateRequest();
    when(mockAlertRepository.findById(any())).thenReturn(Optional.empty());
    try {
      commentService
          .addAlertComment(alertId.toString(), runId.toString(), "User1", commentCreateRequest);
      fail("Cannot add comment for Non-existent Alert");
    } catch (EntityNotFoundException e) {
      verify(mockAlertRepository).findById(primaryKeyArgumentCaptor.capture());
      assertThat(primaryKeyArgumentCaptor.getValue().getAlertId()).isEqualTo(alertId);
      assertThat(primaryKeyArgumentCaptor.getValue().getRunId()).isEqualTo(runId);
    }
  }


  @Test
  public void shouldAddCommentWithValidData() {
    SandboxAlertCommentCreateRequest commentCreateRequest = SandboxAlertTestObjects
        .getCommentCreateRequest();

    try {
      commentService
          .addAlertComment(alertId.toString(), runId.toString(), "User1", commentCreateRequest);
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
  public void shouldReturnEmptyWhenNoResultsForGetCommentsForAlert() throws Throwable {
    List<SandboxAlertComment> comments = commentService
        .getCommentsForAlert(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString());
    assertThat(comments).isEmpty();
  }

  @Test(expected = EntityNotFoundException.class)
  public void shouldReturnExceptionWhenAlertNotfoundForGetCommentsForAlert() throws Throwable {
    when(mockAlertRepository.findById(any())).thenReturn(Optional.empty());
    List<SandboxAlertComment> comments = commentService
        .getCommentsForAlert(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString());
  }

  @Test
  public void shouldReturnDataWhenResultsFoundForGetCommentsForAlert() throws Throwable {
    List<SandboxAlertCommentDTO> dtoList = SandboxAlertTestObjects.getCommentDTOList();
    when(mockRepository.findAllByAlertId(any())).thenReturn(dtoList);
    List<SandboxAlertComment> comments = commentService
        .getCommentsForAlert(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString());
    assertThat(comments).hasSize(dtoList.size());
  }
}
