package com.webtech.service.alert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alert.service.LiveAlertCommentService;
import com.webtech.service.alert.service.LiveAlertRelationshipService;
import com.webtech.service.alert.service.LiveAlertService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.common.security.PrincipalProvider;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.guinotification.GuiNotificationMessage;
import com.webtech.service.guinotification.GuiNotificationPublisher;
import com.webtech.service.guinotification.GuiNotificationTypeEnum;
import com.irisium.service.livealert.model.Alert;
import com.irisium.service.livealert.model.AlertAudit;
import com.irisium.service.livealert.model.Comment;
import com.irisium.service.livealert.model.CommentCreateRequest;
import com.irisium.service.livealert.model.CreateAlertRequest;
import com.irisium.service.livealert.model.EntityLinkRequest;
import com.irisium.service.livealert.model.EntityRelationship;
import com.irisium.service.livealert.model.EntityRelationshipAudit;
import com.irisium.service.livealert.model.UpdateAssigneeRequest;
import com.irisium.service.livealert.model.UpdateStateRequest;
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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class LiveAlertsApiDelegateImplTest {

  private static final String USER = "User";
  @Mock
  private LiveAlertService mockService;
  @Mock
  private LiveAlertCommentService mockCommentService;
  @Mock
  private LiveAlertRelationshipService mockAlertRelationshipService;
  @Mock
  private GuiNotificationPublisher mockGuiNotificationPublisher;
  @Mock
  private PrincipalProvider principalProvider;
  @Captor
  private ArgumentCaptor<CreateAlertRequest> alertRequest;
  @Captor
  private ArgumentCaptor<GuiNotificationMessage> guiNotificationMessageArgumentCaptor;
  @Captor
  private ArgumentCaptor<String> usernameCaptor;
  private LiveAlertsApiDelegateImpl liveAlertsApiDelegateImpl;

  private CreateAlertRequest createAlertRequest;
  private Alert savedAlert;

  @Before
  public void setup() {
    liveAlertsApiDelegateImpl = new LiveAlertsApiDelegateImpl(mockService, mockCommentService,
        mockAlertRelationshipService,
        mockGuiNotificationPublisher, principalProvider);

    createAlertRequest = LiveAlertTestObjects
        .getAlertCreateRequest("alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));

    savedAlert = LiveAlertTestObjects
        .getAlert("1", "alert 1", "OPEN", TestUtils.randomInstant(), TestUtils.randomInstant(),
            "Abusive Squeeze", "Equity Configuration A", "PO1809 (Palm Olein Future)",
            Arrays.asList("Eleis Commodities"), "Wash Trade", "Europe/Equity",
            Arrays.asList("Regulatory", "Operational"));

    Mockito.when(mockService.createAlert(any(CreateAlertRequest.class)))
        .thenReturn(savedAlert);
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

  }

  @Test
  public void shouldReturnUnauthorizedIfUserNotAuthorizedForCreateAlert() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .createAlert(createAlertRequest);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldCreateAlertWithValidData() {
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .createAlert(createAlertRequest);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(mockService).createAlert(alertRequest.capture());

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

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedAlert.getAlertId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.ALERT.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedAlert.getCreatedDate());

  }


  @Test
  public void shouldReturnEmptyResultsWhenNoDataFound() {
    ResponseEntity<List<Alert>> responseEntity = liveAlertsApiDelegateImpl.getAllAlerts();
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).isEmpty();
  }

  @Test
  public void shouldReturnResultsWhenDataFound() {
    List<Alert> alertList = LiveAlertTestObjects.getAlertList();
    Mockito.when(mockService.getAllAlerts()).thenReturn(alertList);
    ResponseEntity<List<Alert>> responseEntity = liveAlertsApiDelegateImpl.getAllAlerts();
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).hasSize(alertList.size());
  }

  @Test
  public void shouldReturnNotFoundForANonExistingAlertWhenNoData() {
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl.getAlertById("1234");
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnExistingAlertWhenDataFound() {
    Mockito.when(mockService.getAlertById(savedAlert.getAlertId()))
        .thenReturn(Optional.of(savedAlert));
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .getAlertById(savedAlert.getAlertId().toString());
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody().getAlertId()).isEqualTo(savedAlert.getAlertId());
    assertThat(responseEntity.getBody().getDescription()).isEqualTo(savedAlert.getDescription());
    assertThat(responseEntity.getBody().getInstrumentDescription())
        .isEqualTo(savedAlert.getInstrumentDescription());
    assertThat(responseEntity.getBody().getParticipants())
        .hasSameSizeAs(savedAlert.getParticipants());
    assertThat(responseEntity.getBody().getClassification())
        .hasSameSizeAs(savedAlert.getClassification());
    assertThat(responseEntity.getBody().getTitle())
        .isEqualTo(savedAlert.getTitle());
    assertThat(responseEntity.getBody().getBusinessUnit())
        .isEqualTo(savedAlert.getBusinessUnit());
    assertThat(responseEntity.getBody().getConfiguration())
        .isEqualTo(savedAlert.getConfiguration());
    assertThat(responseEntity.getBody().getAssignee())
        .isEqualTo(savedAlert.getAssignee());
  }

  @Test
  public void shouldReturnNotAuthorizedResultWhenNotAuthorizedForUpdateAlert() throws Exception {
    UpdateStateRequest request = LiveAlertTestObjects.getUpdateStateRequest("OPEN", null);
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .updateAlertState("1234", request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnNotFoundResultForUpdateAlertWhenNotFound() throws Exception {
    UpdateStateRequest request = LiveAlertTestObjects
        .getUpdateStateRequest("OPEN", null);
    Mockito.when(mockService.updateAlert("1234", request, USER))
        .thenThrow(new EntityNotFoundException("Alert", "1234"));
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .updateAlertState("1234", request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnSuccessResultForUpdateAlertWhenFound() throws Exception {
    UpdateStateRequest request = LiveAlertTestObjects
        .getUpdateStateRequest("OPEN", null);
    String originalId = TestUtils.randomUUID().toString();
    Alert savedAlert = LiveAlertTestObjects
        .getAlert(originalId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));

    when(mockService.updateAlert(originalId, request, USER)).thenReturn(savedAlert);
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .updateAlertState(originalId, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedAlert.getAlertId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.ALERT.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedAlert.getUpdatedDate());
  }


  @Test
  public void shouldReturnNotAuthorizedResultWhenNotAuthorizedForUpdateAssignee() {
    String alertId = TestUtils.randomUUID().toString();
    UpdateAssigneeRequest request = LiveAlertTestObjects
        .getUpdateAssigneeRequest(TestUtils.randomAlphanumeric(10));
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .updateAssignee(alertId, request);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnNotFoundResultForUpdateAssigneeWhenNotFound() throws Exception {
    String alertId = TestUtils.randomUUID().toString();
    UpdateAssigneeRequest request = LiveAlertTestObjects
        .getUpdateAssigneeRequest(TestUtils.randomAlphanumeric(10));
    Mockito.when(mockService.updateAssignee(alertId, request, USER))
        .thenThrow(new EntityNotFoundException("Alert", alertId));
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .updateAssignee(alertId, request);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnSuccessResultWhenFoundForUpdateAssignee() throws Exception {
    UpdateAssigneeRequest request = LiveAlertTestObjects
        .getUpdateAssigneeRequest(TestUtils.randomAlphanumeric(10));
    String originalId = TestUtils.randomUUID().toString();
    Alert savedAlert = LiveAlertTestObjects
        .getAlert(originalId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));

    when(mockService.updateAssignee(originalId, request, USER)).thenReturn(savedAlert);
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .updateAssignee(originalId, request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedAlert.getAlertId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.ALERT.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedAlert.getUpdatedDate());
  }

  @Test
  public void shouldInvokeCommentServiceUpdateStateOpenToCloseWhenRequestContainsReason()
      throws Exception {

    UpdateStateRequest request = LiveAlertTestObjects
        .getUpdateStateRequest("CLOSED", "Test comment");

    String originalId = TestUtils.randomUUID().toString();
    Alert savedAlert = LiveAlertTestObjects
        .getAlert(originalId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));
    when(mockService.updateAlert(originalId, request, USER)).thenReturn(savedAlert);
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .updateAlertState(originalId, request);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(mockCommentService, times(1))
        .addAlertComment(savedAlert.getAlertId(), "User",
            LiveAlertTestObjects.getCommentCreateRequest());

  }

  @Test
  public void shouldNotInvokeCommentServiceUpdateStateOpenToCloseWhenRequestHasNoReason()
      throws Exception {

    UpdateStateRequest request = LiveAlertTestObjects
        .getUpdateStateRequest("CLOSED", null);
    String originalId = TestUtils.randomUUID().toString();
    Alert savedAlert = LiveAlertTestObjects
        .getAlert(originalId, "alert 1", "OPEN", TestUtils.randomInstant(),
            TestUtils.randomInstant(), "Abusive Squeeze", "Equity Configuration A",
            "PO1809 (Palm Olein Future)", Arrays.asList("Eleis Commodities"), "Wash Trade",
            "Europe/Equity", Arrays.asList("Regulatory", "Operational"));
    when(mockService.updateAlert(originalId, request, USER)).thenReturn(savedAlert);
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    ResponseEntity<Alert> responseEntity = liveAlertsApiDelegateImpl
        .updateAlertState(originalId, request);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(mockCommentService, times(0))
        .addAlertComment(savedAlert.getAlertId(), "User",
            LiveAlertTestObjects.getCommentCreateRequest());

  }


  //Alert-Comments tests
  @Test
  public void shouldReturnEmptyResultWhenNoCommentsFound() {
    ResponseEntity<List<Comment>> result = liveAlertsApiDelegateImpl
        .getAllComments(TestUtils.randomUUID().toString());

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isEmpty();
  }

  @Test
  public void shouldReturnResultWhenCommentsFound() {
    when(mockCommentService.getCommentsForAlert(any()))
        .thenReturn(LiveAlertTestObjects.getCommentList());
    ResponseEntity<List<Comment>> result = liveAlertsApiDelegateImpl
        .getAllComments(TestUtils.randomUUID().toString());

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
  }

  @Test
  public void shouldSaveAndReturnValidComment() throws Throwable {
    Comment savedComment = LiveAlertTestObjects
        .getComment(TestUtils.randomUUID(), TestUtils.randomUUID(), USER, "Comment1",
            TestUtils.randomInstant());
    CommentCreateRequest commentCreateRequest = LiveAlertTestObjects.getCommentCreateRequest();
    when(mockCommentService
        .addAlertComment(savedComment.getAlertId(), USER, commentCreateRequest))
        .thenReturn(savedComment);

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<Comment> result = liveAlertsApiDelegateImpl
        .addComment(savedComment.getAlertId(), commentCreateRequest);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody().getAlertId()).isEqualTo(savedComment.getAlertId());
    assertThat(result.getBody().getCommentId()).isEqualTo(savedComment.getCommentId());
    assertThat(result.getBody().getUsername()).isEqualTo(savedComment.getUsername());
    assertThat(result.getBody().getComment()).isEqualTo(savedComment.getComment());
    assertThat(result.getBody().getCreationTime()).isEqualTo(savedComment.getCreationTime());

  }

  @Test
  public void shouldReturnNotAuthorisedResponseWhenUserNotAuthorisedForAddComment() {
    Comment savedComment = LiveAlertTestObjects
        .getComment(TestUtils.randomUUID(), TestUtils.randomUUID(), USER, "Comment1",
            TestUtils.randomInstant());
    CommentCreateRequest commentCreateRequest = LiveAlertTestObjects.getCommentCreateRequest();
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<Comment> result = liveAlertsApiDelegateImpl
        .addComment(savedComment.getAlertId(), commentCreateRequest);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNull();
  }

  @Test
  public void shouldReturnNotFoundResultWhenAlertNotFoundForAddComment() throws Exception {
    CommentCreateRequest request = LiveAlertTestObjects.getCommentCreateRequest();
    UUID alertId = TestUtils.randomUUID();
    Mockito.when(mockCommentService.addAlertComment(alertId.toString(), USER, request))
        .thenThrow(new EntityNotFoundException("Alert", alertId.toString()));

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<Comment> responseEntity = liveAlertsApiDelegateImpl
        .addComment(alertId.toString(), request);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  //Alert-Tag relationship tests
  @Test
  public void shouldLinkTagWhenValidAlert() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    EntityRelationship savedRelationship = LiveAlertTestObjects
        .getEntityRelationship(alertId, tagId, EntityType.ALERT, EntityType.TAG,
            USER);
    savedRelationship.setWhen(TestUtils.randomInstant());

    EntityLinkRequest linkRequest = LiveAlertTestObjects.getEntityLinkRequest(tagId);

    when(mockAlertRelationshipService
        .createRelationship(alertId.toString(), linkRequest, EntityType.ALERT, EntityType.TAG,
            USER))
        .thenReturn(savedRelationship);

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<EntityRelationship> response = liveAlertsApiDelegateImpl
        .linkTagToAlert(alertId.toString(), linkRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getUser()).isEqualTo(USER);
    assertThat(response.getBody().getFromId()).isEqualTo(alertId.toString());
    assertThat(response.getBody().getFromType()).isEqualTo(EntityType.ALERT.toString());
    assertThat(response.getBody().getToId()).isEqualTo(linkRequest.getEntityId());
    assertThat(response.getBody().getToType()).isEqualTo(EntityType.TAG.toString());

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedRelationship.getFromId().toString());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.ALERT_REL_TAG.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedRelationship.getWhen());

  }

  @Test
  public void shouldReturn404ResponseWhenInvalidAlertForLinkTag() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    EntityLinkRequest linkRequest = LiveAlertTestObjects.getEntityLinkRequest(tagId);
    when(mockAlertRelationshipService
        .createRelationship(alertId.toString(), linkRequest, EntityType.ALERT, EntityType.TAG,
            USER))
        .thenThrow(new EntityNotFoundException("Alert", alertId.toString()));
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<EntityRelationship> response = liveAlertsApiDelegateImpl
        .linkTagToAlert(alertId.toString(), linkRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void shouldDelinkTagWhenValidAlert() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();

    EntityRelationship savedRelationship = LiveAlertTestObjects
        .getEntityRelationship(alertId, tagId, EntityType.ALERT, EntityType.TAG,
            TestUtils.randomAlphanumeric(10));
    savedRelationship.setWhen(TestUtils.randomInstant());

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    when(mockAlertRelationshipService
        .deleteRelationship(alertId.toString(), tagId.toString(), EntityType.ALERT, EntityType.TAG,
            USER))
        .thenReturn(savedRelationship);

    ResponseEntity<Void> response = liveAlertsApiDelegateImpl
        .delinkTagFromAlert(alertId.toString(), tagId.toString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedRelationship.getFromId().toString());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.ALERT_REL_TAG.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedRelationship.getWhen());

  }

  @Test
  public void shouldReturn400ResponseWhenInvalidAlertForDelinkTag() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();

    when(mockAlertRelationshipService
        .deleteRelationship(alertId.toString(), tagId.toString(), EntityType.ALERT, EntityType.TAG,
            USER))
        .thenThrow(new EntityNotFoundException("Alert", alertId.toString()));
    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    ResponseEntity<Void> response = liveAlertsApiDelegateImpl
        .delinkTagFromAlert(alertId.toString(), tagId.toString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void shouldNotAuthorisedResponseWhenUserNotAuthorisedForLinkTag() {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    EntityLinkRequest linkRequest = LiveAlertTestObjects.getEntityLinkRequest(tagId);
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());

    ResponseEntity<EntityRelationship> result = liveAlertsApiDelegateImpl
        .linkTagToAlert(alertId.toString(), linkRequest);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNull();
  }

  @Test
  public void shouldReturnNotAuthorisedResponseWhenUserNotAuthorisedForDelinkTag() {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());

    ResponseEntity<Void> result = liveAlertsApiDelegateImpl
        .delinkTagFromAlert(alertId.toString(), tagId.toString());

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNull();
  }

  @Test
  public void shouldReturnResultsWhenLinkedTagsFound() {
    UUID alertId = TestUtils.randomUUID();

    when(mockAlertRelationshipService.getRelationshipsByAlertId(alertId.toString(), EntityType.TAG))
        .thenReturn(
            Arrays.asList(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString()));
    ResponseEntity<List<String>> result = liveAlertsApiDelegateImpl
        .getTagsByAlertId(alertId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedTagsFound() {
    UUID alertId = TestUtils.randomUUID();
    ResponseEntity<List<String>> result = liveAlertsApiDelegateImpl
        .getTagsByAlertId(alertId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(0);
  }

  //Alert-Case relationship tests
  @Test
  public void shouldReturnResultsWhenLinkedCasesFound() {
    UUID alertId = TestUtils.randomUUID();

    when(
        mockAlertRelationshipService.getRelationshipsByAlertId(alertId.toString(), EntityType.CASE))
        .thenReturn(
            Arrays.asList(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString()));
    ResponseEntity<List<String>> result = liveAlertsApiDelegateImpl
        .getCasesByAlertId(alertId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedCasesFound() {
    UUID alertId = TestUtils.randomUUID();
    ResponseEntity<List<String>> result = liveAlertsApiDelegateImpl
        .getCasesByAlertId(alertId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(0);
  }

  @Test
  public void shouldReturn400ResponseWhenAlertNotFoundForTagRelationshipAudit() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockAlertRelationshipService.getRelationshipAudit(alertId.toString(), tagId.toString()))
        .thenThrow(new EntityNotFoundException("Alert", alertId.toString()));

    ResponseEntity<List<EntityRelationshipAudit>> result = liveAlertsApiDelegateImpl
        .getAlertTagRelationshipAudit(alertId.toString(), tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnUnauthorizedResponseWhenUserNotAuthorizedForTagRelationshipAudit() {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<EntityRelationshipAudit>> result = liveAlertsApiDelegateImpl
        .getAlertTagRelationshipAudit(alertId.toString(), tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturn404ResponseWhenNoAuditHistoryForTagRelationship() {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<List<EntityRelationshipAudit>> result = liveAlertsApiDelegateImpl
        .getAlertTagRelationshipAudit(alertId.toString(), tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForTagRelationship() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    List<EntityRelationshipAudit> auditHistory = LiveAlertTestObjects
        .getRelationshipAuditHistory(alertId, tagId, EntityType.ALERT,
            EntityType.TAG);

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockAlertRelationshipService.getRelationshipAudit(alertId.toString(), tagId.toString()))
        .thenReturn(auditHistory);

    ResponseEntity<List<EntityRelationshipAudit>> result = liveAlertsApiDelegateImpl
        .getAlertTagRelationshipAudit(alertId.toString(), tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).isEqualTo(auditHistory);
  }

  @Test
  public void shouldReturn400ResponseWhenAlertNotFoundForCaseRelationshipAudit() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    UUID caseId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockAlertRelationshipService.getRelationshipAudit(alertId.toString(), caseId.toString()))
        .thenThrow(new EntityNotFoundException("Alert", alertId.toString()));

    ResponseEntity<List<EntityRelationshipAudit>> result = liveAlertsApiDelegateImpl
        .getAlertCaseRelationshipAudit(alertId.toString(), caseId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnUnauthorizedResponseWhenUserNotAuthorizedForCaseRelationshipAudit() {
    UUID alertId = TestUtils.randomUUID();
    UUID caseId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<EntityRelationshipAudit>> result = liveAlertsApiDelegateImpl
        .getAlertCaseRelationshipAudit(alertId.toString(), caseId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturn404ResponseWhenNoAuditHistoryForCaseRelationship() {
    UUID alertId = TestUtils.randomUUID();
    UUID caseId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<List<EntityRelationshipAudit>> result = liveAlertsApiDelegateImpl
        .getAlertCaseRelationshipAudit(alertId.toString(), caseId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForCaseRelationship() throws Throwable {
    UUID alertId = TestUtils.randomUUID();
    UUID caseId = TestUtils.randomUUID();
    List<EntityRelationshipAudit> auditHistory = LiveAlertTestObjects
        .getRelationshipAuditHistory(alertId, caseId, EntityType.ALERT,
            EntityType.CASE);

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockAlertRelationshipService.getRelationshipAudit(alertId.toString(), caseId.toString()))
        .thenReturn(auditHistory);

    ResponseEntity<List<EntityRelationshipAudit>> result = liveAlertsApiDelegateImpl
        .getAlertCaseRelationshipAudit(alertId.toString(), caseId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).isEqualTo(auditHistory);
  }

  @Test
  public void shouldReturn404ResponseWhenAlertNotFoundForAlertAudit() throws Throwable {
    UUID alertId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockService.getAudit(alertId.toString()))
        .thenThrow(new EntityNotFoundException("Alert", alertId.toString()));

    ResponseEntity<List<AlertAudit>> result = liveAlertsApiDelegateImpl
        .getAuditForAlert(alertId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnUnauthorizedResponseWhenUserNotAuthorizedForAlertAudit() {
    UUID alertId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<AlertAudit>> result = liveAlertsApiDelegateImpl
        .getAuditForAlert(alertId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnNoContentResponseWhenNoAuditHistoryForAlert() {
    UUID alertId = TestUtils.randomUUID();

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<List<AlertAudit>> result = liveAlertsApiDelegateImpl
        .getAuditForAlert(alertId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForAlert() throws Throwable {
    UUID alertId = TestUtils.randomUUID();

    List<AlertAudit> auditHistory = LiveAlertTestObjects.getAlertAudits(alertId);

    when(principalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockService.getAudit(alertId.toString())).thenReturn(auditHistory);

    ResponseEntity<List<AlertAudit>> result = liveAlertsApiDelegateImpl
        .getAuditForAlert(alertId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).isEqualTo(auditHistory);
  }
}

