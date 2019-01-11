package com.webtech.service.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alert.service.LiveAlertService;
import com.webtech.service.alert.service.SandboxAlertCommentService;
import com.webtech.service.alert.service.SandboxAlertRelationshipService;
import com.webtech.service.alert.service.SandboxAlertService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.common.security.PrincipalProvider;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.guinotification.GuiNotificationMessage;
import com.webtech.service.guinotification.GuiNotificationPublisher;
import com.webtech.service.guinotification.GuiNotificationTypeEnum;
import com.irisium.service.livealert.model.Alert;
import com.irisium.service.livealert.model.CreateAlertRequest;
import com.irisium.service.sandboxalert.model.CreateSandboxAlertRequest;
import com.irisium.service.sandboxalert.model.PromoteSandboxAlertRequest;
import com.irisium.service.sandboxalert.model.SandboxAlert;
import com.irisium.service.sandboxalert.model.SandboxAlertAudit;
import com.irisium.service.sandboxalert.model.SandboxAlertComment;
import com.irisium.service.sandboxalert.model.SandboxAlertCommentCreateRequest;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityLinkRequest;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationship;
import com.irisium.service.sandboxalert.model.SandboxAlertEntityRelationshipAudit;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertAssigneeRequest;
import com.irisium.service.sandboxalert.model.UpdateSandboxAlertStateRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.webtech.service.common.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class SandboxAlertsApiDelegateImplTest {

  private static final String USER = "User";
  private SandboxAlertsApiDelegateImpl delegate;
  @Mock
  private PrincipalProvider principalProvider;
  @Mock
  private GuiNotificationPublisher guiNotificationPublisher;
  @Mock
  private SandboxAlertService sandboxAlertService;
  @Mock
  private SandboxAlertCommentService commentService;
  @Mock
  private SandboxAlertRelationshipService alertRelationshipService;
  @Mock
  private LiveAlertService liveAlertService;

  @Captor
  private ArgumentCaptor<CreateSandboxAlertRequest> alertRequest;
  @Captor
  private ArgumentCaptor<GuiNotificationMessage> guiNotificationMessageArgumentCaptor;
  @Captor
  private ArgumentCaptor<CreateAlertRequest> createAlertRequestArgumentCaptor;
  @Captor
  private ArgumentCaptor<String> alertIdArgCaptor;
  @Captor
  private ArgumentCaptor<String> runIdArgCaptor;
  @Captor
  private ArgumentCaptor<SandboxAlert> sandboxAlertArgumentCaptor;
  @Captor
  private ArgumentCaptor<SandboxAlertCommentCreateRequest> commentCreateRequestArgumentCaptor;


  private SandboxAlert savedAlert;
  private UUID sandboxAlertId;
  private UUID runId;

  @Before
  public void setUp() throws Exception {
    delegate = new SandboxAlertsApiDelegateImpl(principalProvider, guiNotificationPublisher,
        sandboxAlertService,
        commentService, alertRelationshipService, liveAlertService);

    sandboxAlertId = TestUtils.randomUUID();
    runId = TestUtils.randomUUID();
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    savedAlert = SandboxAlertTestObjects
        .getAlert(sandboxAlertId.toString(), runId.toString(), "alert 1", "OPEN",
            TestUtils.randomInstant(), TestUtils.randomInstant(),
            "Abusive Squeeze", "Equity Configuration A", "PO1809 (Palm Olein Future)",
            Arrays.asList("Eleis Commodities"), "Wash Trade", "Europe/Equity",
            Arrays.asList("Regulatory", "Operational"));

    Mockito
        .when(sandboxAlertService.createSandboxAlert(any(CreateSandboxAlertRequest.class)))
        .thenReturn(savedAlert);

  }

  @Test
  public void shouldReturnUnauthorizedIfUserNotAuthorizedForCreateAlert() {
    CreateSandboxAlertRequest createAlertRequest = SandboxAlertTestObjects
        .getAlertCreateRequest(TestUtils.randomUUID().toString(), "alert 1", "OPEN",
            TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<SandboxAlert> responseEntity = delegate.createSandboxAlert(createAlertRequest);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    Mockito.verifyZeroInteractions(guiNotificationPublisher);
    Mockito.verifyZeroInteractions(sandboxAlertService);
  }

  @Test
  public void shouldCreateAlertWithValidData() {
    CreateSandboxAlertRequest createAlertRequest = SandboxAlertTestObjects
        .getAlertCreateRequest(TestUtils.randomUUID().toString(), "alert 1", "OPEN",
            TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));

    ResponseEntity<SandboxAlert> responseEntity = delegate.createSandboxAlert(createAlertRequest);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(sandboxAlertService)
        .createSandboxAlert(alertRequest.capture());

    //Check the object that was passed to save method
    assertThat(alertRequest.getValue().getDescription())
        .isEqualTo(createAlertRequest.getDescription());
    assertThat(alertRequest.getValue().getState()).isEqualTo(createAlertRequest.getState());
    assertThat(alertRequest.getValue().getInstrumentDescription())
        .isEqualTo(createAlertRequest.getInstrumentDescription());

    //Check the object that was returned by save method
    assertThat(responseEntity.getBody().getDescription())
        .isEqualTo(createAlertRequest.getDescription());
    assertThat(responseEntity.getBody().getState().toString())
        .isEqualTo(createAlertRequest.getState().toString());
    assertThat(responseEntity.getBody().getInstrumentDescription())
        .isEqualTo(createAlertRequest.getInstrumentDescription());

    verify(guiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedAlert.getAlertId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOXALERT.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedAlert.getUpdatedDate());

  }


  @Test
  public void shouldReturn404WhenNoAlertForGetSandboxAlertByIdAndRunId() throws Throwable {
    Mockito.when(sandboxAlertService.getSandboxAlertByIdAndRunId(any(), any()))
        .thenThrow(new EntityNotFoundException("SandboxAlert", sandboxAlertId.toString()));
    ResponseEntity<SandboxAlert> responseEntity = delegate
        .getSandboxAlertByIdAndRunId(sandboxAlertId.toString(), runId.toString());
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn200WhenAlertFoundForGetSandboxAlertByIdAndRunId() {
    ResponseEntity<SandboxAlert> responseEntity = delegate
        .getSandboxAlertByIdAndRunId(sandboxAlertId.toString(), runId.toString());
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturnUnauthorizedResponseWhenUserNotAuthorizedForGetSandboxAlertByIdAndRunId() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<SandboxAlert> result = delegate
        .getSandboxAlertByIdAndRunId(sandboxAlertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404ResponseWhenAlertNotFoundForAlertAudit() throws Throwable {

    when(sandboxAlertService.getAuditForSandboxAlert(sandboxAlertId.toString(), runId.toString()))
        .thenThrow(new EntityNotFoundException("SandboxAlert", sandboxAlertId.toString()));

    ResponseEntity<List<SandboxAlertAudit>> result = delegate.
        getAuditForSandboxAlert(sandboxAlertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnUnauthorizedResponseWhenUserNotAuthorizedForAlertAudit() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<SandboxAlertAudit>> result = delegate.
        getAuditForSandboxAlert(sandboxAlertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnNoContentResponseWhenNoAuditHistoryForAlert() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<List<SandboxAlertAudit>> result = delegate.
        getAuditForSandboxAlert(sandboxAlertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForAlert() throws Throwable {
    List<SandboxAlertAudit> auditHistory = SandboxAlertTestObjects.getAlertAudits(sandboxAlertId);

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(sandboxAlertService.getAuditForSandboxAlert(sandboxAlertId.toString(), runId.toString()))
        .thenReturn(auditHistory);

    ResponseEntity<List<SandboxAlertAudit>> result = delegate.
        getAuditForSandboxAlert(sandboxAlertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).isEqualTo(auditHistory);
  }

  @Test
  public void shouldReturn204WhenNoDataForGetAllAlerts() {
    ResponseEntity<List<SandboxAlert>> responseEntity = delegate.getAllSandboxAlerts(null, null);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  public void shouldReturn200WhenDataFoundForGetAllAlerts() {
    List<SandboxAlert> alertList = SandboxAlertTestObjects.getAlertList();
    Mockito.when(sandboxAlertService.getAllSandboxAlerts(any(), any())).thenReturn(alertList);
    ResponseEntity<List<SandboxAlert>> responseEntity = delegate.getAllSandboxAlerts(null, null);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).hasSize(alertList.size());
  }

  @Test
  public void shouldReturnUnauthorizedResponseWhenUserNotAuthorizedForGetAllAlerts() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<SandboxAlert>> result = delegate.getAllSandboxAlerts(null, null);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldSaveAndReturnWhenValidComment() throws Throwable {
    SandboxAlertComment savedComment = SandboxAlertTestObjects
        .getComment(sandboxAlertId, TestUtils.randomUUID(), USER, "Comment1",
            TestUtils.randomInstant());
    SandboxAlertCommentCreateRequest commentCreateRequest = SandboxAlertTestObjects
        .getCommentCreateRequest();
    when(commentService
        .addAlertComment(savedComment.getAlertId(), runId.toString(), USER, commentCreateRequest))
        .thenReturn(savedComment);

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<SandboxAlertComment> result = delegate
        .addSandboxAlertComment(savedComment.getAlertId(), runId.toString(), commentCreateRequest);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getAlertId()).isEqualTo(savedComment.getAlertId());
    assertThat(result.getBody().getCommentId()).isEqualTo(savedComment.getCommentId());
    assertThat(result.getBody().getUsername()).isEqualTo(savedComment.getUsername());
    assertThat(result.getBody().getComment()).isEqualTo(savedComment.getComment());
    assertThat(result.getBody().getCreationTime()).isEqualTo(savedComment.getCreationTime());

  }

  @Test
  public void shouldReturnNotAuthorisedResponseForAddCommentWhenUserNotAuthorised() {
    SandboxAlertComment savedComment = SandboxAlertTestObjects
        .getComment(TestUtils.randomUUID(), TestUtils.randomUUID(), USER, "Comment1",
            TestUtils.randomInstant());
    SandboxAlertCommentCreateRequest commentCreateRequest = SandboxAlertTestObjects
        .getCommentCreateRequest();
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<SandboxAlertComment> result = delegate
        .addSandboxAlertComment(savedComment.getAlertId(), runId.toString(), commentCreateRequest);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNull();
  }

  @Test
  public void shouldReturnNotFoundResultForAddCommentWhenAlertNotFound() throws Exception {
    SandboxAlertCommentCreateRequest commentCreateRequest = SandboxAlertTestObjects
        .getCommentCreateRequest();
    Mockito.when(commentService
        .addAlertComment(sandboxAlertId.toString(), runId.toString(), USER, commentCreateRequest))
        .thenThrow(new EntityNotFoundException("Alert", sandboxAlertId.toString()));

    ResponseEntity<SandboxAlertComment> result = delegate
        .addSandboxAlertComment(sandboxAlertId.toString(), runId.toString(), commentCreateRequest);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  //Alert-Comments tests
  @Test
  public void shouldReturnEmptyResultWhenNoComments() {
    ResponseEntity<List<SandboxAlertComment>> result = delegate
        .getAllSandboxAlertComments(sandboxAlertId.toString(), runId.toString());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  public void shouldReturnNonEmptyResultwWhenCommentsExist() throws Throwable {
    when(commentService.getCommentsForAlert(sandboxAlertId.toString(), runId.toString()))
        .thenReturn(SandboxAlertTestObjects.getCommentList());
    ResponseEntity<List<SandboxAlertComment>> result = delegate
        .getAllSandboxAlertComments(sandboxAlertId.toString(), runId.toString());

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
  }

  @Test
  public void shouldReturnUnAuthorisedWhenNoUserFoundForGetComments() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<List<SandboxAlertComment>> result = delegate
        .getAllSandboxAlertComments(sandboxAlertId.toString(), runId.toString());

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404WhenAlertNotFoundForAddComment() throws Exception {
    Mockito.when(commentService
        .getCommentsForAlert(sandboxAlertId.toString(), runId.toString()))
        .thenThrow(new EntityNotFoundException("Alert", sandboxAlertId.toString()));
    ResponseEntity<List<SandboxAlertComment>> result = delegate
        .getAllSandboxAlertComments(sandboxAlertId.toString(), runId.toString());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnUnAuthorisedWhenNoUserFoundForUpdateSandboxAlertState() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<SandboxAlert> result = delegate
        .updateSandboxAlertState(sandboxAlertId.toString(), runId.toString(), mock(
            UpdateSandboxAlertStateRequest.class));

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404WhenAlertNotFoundForUpdateSandboxAlertState() throws Exception {
    Mockito.when(sandboxAlertService
        .updateAlert(any(), any(), any(), any()))
        .thenThrow(new EntityNotFoundException("Alert", sandboxAlertId.toString()));
    ResponseEntity<SandboxAlert> result = delegate
        .updateSandboxAlertState(sandboxAlertId.toString(), runId.toString(), mock(
            UpdateSandboxAlertStateRequest.class));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn200WhenAlertFoundForUpdateSandboxAlertState() throws Exception {
    UpdateSandboxAlertStateRequest updateSandboxAlertStateRequest = new UpdateSandboxAlertStateRequest();
    updateSandboxAlertStateRequest.setReason(TestUtils.randomAlphanumeric(5));
    Mockito.when(sandboxAlertService.updateAlert(any(), any(), any(), any()))
        .thenReturn(savedAlert);
    ResponseEntity<SandboxAlert> result = delegate
        .updateSandboxAlertState(sandboxAlertId.toString(), runId.toString(),
            updateSandboxAlertStateRequest);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(guiNotificationPublisher, times(1)).sendGuiNotification(any());
    verify(commentService, times(1)).addAlertComment(anyString(), any(), anyString(), any());
  }

  @Test
  public void shouldReturn200WhenAlertFoundForUpdateSandboxAlertStateWithoutReason()
      throws Exception {
    UpdateSandboxAlertStateRequest updateSandboxAlertStateRequest = new UpdateSandboxAlertStateRequest();
    Mockito.when(sandboxAlertService.updateAlert(any(), any(), any(), any()))
        .thenReturn(savedAlert);
    ResponseEntity<SandboxAlert> result = delegate
        .updateSandboxAlertState(sandboxAlertId.toString(), runId.toString(),
            updateSandboxAlertStateRequest);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(guiNotificationPublisher, times(1)).sendGuiNotification(any());
    Mockito.verifyZeroInteractions(commentService);
  }

  @Test
  public void shouldReturnUnAuthorisedWhenNoUserFoundForUpdateAssignee() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<SandboxAlert> result = delegate
        .updateSandboxAlertAssignee(sandboxAlertId.toString(), runId.toString(), mock(
            UpdateSandboxAlertAssigneeRequest.class));

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404WhenAlertNotFoundForUpdateAssignee() throws Exception {
    Mockito.when(sandboxAlertService
        .updateAssignee(any(), any(), any(), any()))
        .thenThrow(new EntityNotFoundException("Alert", sandboxAlertId.toString()));
    ResponseEntity<SandboxAlert> result = delegate
        .updateSandboxAlertAssignee(sandboxAlertId.toString(), runId.toString(), mock(
            UpdateSandboxAlertAssigneeRequest.class));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn200WhenAlertFoundForUpdateAssignee() throws Exception {
    Mockito.when(sandboxAlertService.updateAssignee(any(), any(), any(), any()))
        .thenReturn(savedAlert);
    ResponseEntity<SandboxAlert> result = delegate
        .updateSandboxAlertAssignee(sandboxAlertId.toString(), runId.toString(), mock(
            UpdateSandboxAlertAssigneeRequest.class));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(guiNotificationPublisher, times(1)).sendGuiNotification(any());
  }

  //Alert-Tag relationship tests
  @Test
  public void shouldLinkTagWhenValidAlert() throws Throwable {
    UUID tagId = TestUtils.randomUUID();
    SandboxAlertEntityRelationship savedRelationship = SandboxAlertTestObjects
        .getEntityRelationship(sandboxAlertId, tagId, EntityType.SANDBOXALERT, EntityType.TAG,
            USER);
    savedRelationship.setWhen(TestUtils.randomInstant());

    SandboxAlertEntityLinkRequest linkRequest = SandboxAlertTestObjects.getEntityLinkRequest(tagId);

    when(alertRelationshipService
        .createRelationship(sandboxAlertId.toString(), runId.toString(), linkRequest,
            EntityType.SANDBOXALERT, EntityType.TAG, USER))
        .thenReturn(savedRelationship);

    ResponseEntity<SandboxAlertEntityRelationship> response = delegate
        .linkTagToSandboxAlert(sandboxAlertId.toString(), runId.toString(), linkRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getUser()).isEqualTo(USER);
    assertThat(response.getBody().getFromId()).isEqualTo(sandboxAlertId.toString());
    assertThat(response.getBody().getFromType()).isEqualTo(EntityType.SANDBOXALERT.toString());
    assertThat(response.getBody().getToId()).isEqualTo(linkRequest.getEntityId());
    assertThat(response.getBody().getToType()).isEqualTo(EntityType.TAG.toString());

    verify(guiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedRelationship.getFromId().toString());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOXALERT_REL_TAG.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedRelationship.getWhen());

  }

  @Test
  public void shouldReturn404ResponseForLinkTagWhenInvalidAlert() throws Throwable {
    UUID tagId = TestUtils.randomUUID();
    SandboxAlertEntityLinkRequest linkRequest = SandboxAlertTestObjects.getEntityLinkRequest(tagId);
    when(alertRelationshipService
        .createRelationship(sandboxAlertId.toString(), runId.toString(), linkRequest,
            EntityType.SANDBOXALERT, EntityType.TAG, USER))
        .thenThrow(new EntityNotFoundException("Alert", sandboxAlertId.toString()));
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<SandboxAlertEntityRelationship> response = delegate
        .linkTagToSandboxAlert(sandboxAlertId.toString(), runId.toString(), linkRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void shouldReturnUnauthorisedResponseForLinkTagWhenInvalidUser() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());

    ResponseEntity<SandboxAlertEntityRelationship> response = delegate
        .linkTagToSandboxAlert(sandboxAlertId.toString(), runId.toString(),
            mock(SandboxAlertEntityLinkRequest.class));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldDelinkTagWhenValidAlert() throws Throwable {
    UUID tagId = TestUtils.randomUUID();
    SandboxAlertEntityRelationship savedRelationship = SandboxAlertTestObjects
        .getEntityRelationship(sandboxAlertId, tagId, EntityType.SANDBOXALERT, EntityType.TAG,
            USER);
    savedRelationship.setWhen(TestUtils.randomInstant());

    when(alertRelationshipService
        .deleteRelationship(sandboxAlertId.toString(), runId.toString(), tagId.toString(),
            EntityType.SANDBOXALERT, EntityType.TAG,
            USER))
        .thenReturn(savedRelationship);

    ResponseEntity<Void> response = delegate
        .delinkTagFromSandboxAlert(sandboxAlertId.toString(), runId.toString(), tagId.toString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    verify(guiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedRelationship.getFromId().toString());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOXALERT_REL_TAG.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedRelationship.getWhen());

  }

  @Test
  public void shouldReturn400ResponseForDelinkTagWhenInvalidAlert() throws Throwable {
    UUID tagId = TestUtils.randomUUID();

    when(alertRelationshipService
        .deleteRelationship(sandboxAlertId.toString(), runId.toString(), tagId.toString(),
            EntityType.SANDBOXALERT, EntityType.TAG,
            USER))
        .thenThrow(new EntityNotFoundException("Alert", sandboxAlertId.toString()));
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    ResponseEntity<Void> response = delegate
        .delinkTagFromSandboxAlert(sandboxAlertId.toString(), runId.toString(), tagId.toString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void shouldReturnNotAuthorisedResponseForDelinkTagWhenUserNotAuthorised() {
    UUID tagId = TestUtils.randomUUID();
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<Void> result = delegate
        .delinkTagFromSandboxAlert(sandboxAlertId.toString(), runId.toString(), tagId.toString());

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn400ResponseWhenAlertNotFoundForTagRelationshipAudit() throws Throwable {
    UUID tagId = TestUtils.randomUUID();

    when(alertRelationshipService
        .getRelationshipAudit(sandboxAlertId.toString(), runId.toString(), tagId.toString()))
        .thenThrow(new EntityNotFoundException("Alert", sandboxAlertId.toString()));

    ResponseEntity<List<SandboxAlertEntityRelationshipAudit>> result = delegate
        .getSandboxAlertTagRelationshipAudit(sandboxAlertId.toString(), runId.toString(),
            tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnUnauthorizedResponseWhenUserNotAuthorizedForTagRelationshipAudit() {
    UUID tagId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<SandboxAlertEntityRelationshipAudit>> result = delegate
        .getSandboxAlertTagRelationshipAudit(sandboxAlertId.toString(), runId.toString(),
            tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturn404ResponseWhenNoAuditHistoryForTagRelationship() {
    UUID tagId = TestUtils.randomUUID();

    ResponseEntity<List<SandboxAlertEntityRelationshipAudit>> result = delegate
        .getSandboxAlertTagRelationshipAudit(sandboxAlertId.toString(), runId.toString(),
            tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForTagRelationship() throws Throwable {
    UUID tagId = TestUtils.randomUUID();
    List<SandboxAlertEntityRelationshipAudit> auditHistory = SandboxAlertTestObjects
        .getRelationshipAuditHistory(sandboxAlertId, tagId, EntityType.ALERT,
            EntityType.TAG);

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(alertRelationshipService
        .getRelationshipAudit(sandboxAlertId.toString(), runId.toString(), tagId.toString()))
        .thenReturn(auditHistory);

    ResponseEntity<List<SandboxAlertEntityRelationshipAudit>> result = delegate
        .getSandboxAlertTagRelationshipAudit(sandboxAlertId.toString(), runId.toString(),
            tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).isEqualTo(auditHistory);
  }

  @Test
  public void shouldReturnResultsWhenLinkedTagsFound() throws Throwable {
    when(alertRelationshipService
        .getRelationshipsByAlertId(sandboxAlertId.toString(), runId.toString(), EntityType.TAG))
        .thenReturn(
            Arrays.asList(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString()));
    ResponseEntity<List<String>> result = delegate
        .getTagsBySandboxAlertId(sandboxAlertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedTagsFound() {
    ResponseEntity<List<String>> result = delegate
        .getTagsBySandboxAlertId(sandboxAlertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(0);
  }

  @Test
  public void shouldReturnUnauthorizedResponseWhenUserNotAuthorizedForGetTagsBySandboxAlertId() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<String>> result = delegate
        .getTagsBySandboxAlertId(sandboxAlertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturn404ResponseWhenNoAuditHistoryForGetTagsBySandboxAlertId()
      throws Throwable {
    when(alertRelationshipService
        .getRelationshipsByAlertId(sandboxAlertId.toString(), runId.toString(), EntityType.TAG))
        .thenThrow(new EntityNotFoundException("Alert", sandboxAlertId.toString()));
    ResponseEntity<List<String>> result = delegate
        .getTagsBySandboxAlertId(sandboxAlertId.toString(), runId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnUnauthorizedResponseWhenUserNotAuthorizedForPromoteSandboxAlertToLive() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<SandboxAlert> result = delegate
        .promoteSandboxAlertToLive(sandboxAlertId.toString(), runId.toString(),
            new PromoteSandboxAlertRequest());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404ResponseWhenNoSandboxAlertFoundForPromoteSandboxAlertToLive()
      throws Throwable {
    when(sandboxAlertService
        .getSandboxAlertByIdAndRunId(sandboxAlertId.toString(), runId.toString()))
        .thenThrow(new EntityNotFoundException("SandboxAlert", sandboxAlertId.toString()));
    ResponseEntity<SandboxAlert> result = delegate
        .promoteSandboxAlertToLive(sandboxAlertId.toString(), runId.toString(),
            new PromoteSandboxAlertRequest());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn200ResponseWhenSandboxAlertFoundForPromoteSandboxAlertToLive()
      throws Throwable {
    PromoteSandboxAlertRequest request = new PromoteSandboxAlertRequest();
    request.setComment(TestUtils.randomAlphanumeric(10));
    when(sandboxAlertService
        .getSandboxAlertByIdAndRunId(sandboxAlertId.toString(), runId.toString()))
        .thenReturn(savedAlert);
    when(sandboxAlertService
        .promoteSandboxAlertToLive(any(), any()))
        .thenReturn(savedAlert);
    Alert liveAlert = new Alert();
    liveAlert.setAlertId(TestUtils.randomUUID().toString());
    when(liveAlertService
        .createAlert(any()))
        .thenReturn(liveAlert);
    ResponseEntity<SandboxAlert> result = delegate
        .promoteSandboxAlertToLive(sandboxAlertId.toString(), runId.toString(), request);
    InOrder inOrder = Mockito
        .inOrder(sandboxAlertService, liveAlertService, sandboxAlertService, commentService,
            guiNotificationPublisher, guiNotificationPublisher);
    inOrder.verify(liveAlertService, times(1))
        .createAlert(createAlertRequestArgumentCaptor.capture());
    assertThat(createAlertRequestArgumentCaptor.getValue().getTitle())
        .isEqualTo(savedAlert.getTitle());

    inOrder.verify(sandboxAlertService, times(1))
        .promoteSandboxAlertToLive(sandboxAlertArgumentCaptor.capture(), any());
    assertThat(sandboxAlertArgumentCaptor.getValue()).isEqualTo(savedAlert);

    inOrder.verify(commentService, times(1))
        .addAlertComment(alertIdArgCaptor.capture(), runIdArgCaptor.capture(), any(),
            commentCreateRequestArgumentCaptor.capture());
    assertThat(alertIdArgCaptor.getValue()).isEqualTo(savedAlert.getAlertId());
    assertThat(runIdArgCaptor.getValue()).isEqualTo(savedAlert.getRunId());
    assertThat(commentCreateRequestArgumentCaptor.getValue().getComment())
        .isEqualTo(request.getComment());

    inOrder.verify(guiNotificationPublisher, times(2))
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getAllValues()).hasSize(2);
    assertThat(guiNotificationMessageArgumentCaptor.getAllValues().get(0).getEntityType())
        .isEqualTo(GuiNotificationTypeEnum.ALERT.name());
    assertThat(guiNotificationMessageArgumentCaptor.getAllValues().get(1).getEntityType())
        .isEqualTo(GuiNotificationTypeEnum.SANDBOXALERT.name());
    assertThat(guiNotificationMessageArgumentCaptor.getAllValues().get(1).getEntityId())
        .isEqualTo(sandboxAlertArgumentCaptor.getValue().getAlertId());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
