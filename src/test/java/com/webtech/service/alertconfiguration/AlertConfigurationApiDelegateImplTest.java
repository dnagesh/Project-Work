package com.webtech.service.alertconfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.AuditNotFoundException;
import com.webtech.service.alertconfiguration.exception.SandboxAlertConfigurationNotFoundException;
import com.irisium.service.alertconfiguration.model.AlertConfigurationTagsMap;
import com.irisium.service.alertconfiguration.model.EntityLinkRequest;
import com.irisium.service.alertconfiguration.model.EntityRelationship;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration.StatusEnum;
import com.irisium.service.alertconfiguration.model.LiveAlertConfigurationAudit;
import com.irisium.service.alertconfiguration.model.UpdateStatus;
import com.webtech.service.alertconfiguration.service.AlertConfigurationRelationshipService;
import com.webtech.service.alertconfiguration.service.LiveAlertConfigurationQueryService;
import com.webtech.service.alertconfiguration.service.LiveAlertConfigurationSaveService;
import com.webtech.service.common.security.PrincipalProvider;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.guinotification.GuiNotificationMessage;
import com.webtech.service.guinotification.GuiNotificationPublisher;
import com.webtech.service.guinotification.GuiNotificationTypeEnum;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class AlertConfigurationApiDelegateImplTest {

  private static final String USER = "User";
  @Mock
  private PrincipalProvider mockPrincipalProvider;
  @Mock
  private LiveAlertConfigurationSaveService mockLiveAlertConfigurationSaveService;
  @Mock
  private LiveAlertConfigurationQueryService mockLiveAlertConfigurationQueryService;
  @Mock
  private AlertConfigurationRelationshipService mockAlertConfigurationRelationshipService;
  @Mock
  private GuiNotificationPublisher mockGuiNotificationPublisher;
  @Captor
  private ArgumentCaptor<GuiNotificationMessage> guiNotificationMessageArgumentCaptor;
  private AlertConfigurationApiDelegateImpl alertConfigurationApiDelegate;

  @Before
  public void setUp() throws Exception {
    alertConfigurationApiDelegate = new AlertConfigurationApiDelegateImpl(
        mockPrincipalProvider, mockLiveAlertConfigurationSaveService,
        mockLiveAlertConfigurationQueryService,
        mockAlertConfigurationRelationshipService,
        mockGuiNotificationPublisher);
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
  }

  //View Live Alert Configurations tests
  @Test
  public void shouldReturn401ResponseForViewLiveConfigurationsWhenUnauthorisedUser() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<LiveAlertConfiguration>> result = alertConfigurationApiDelegate
        .getAllLiveAlertConfigurations();

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn204ResponseForViewLiveConfigurationsWhenNoLiveAlertsConfigured() {
    ResponseEntity<List<LiveAlertConfiguration>> result = alertConfigurationApiDelegate
        .getAllLiveAlertConfigurations();

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  public void shouldReturn200ResponseWithDataForViewLiveConfigurationsWhenLiveAlertsConfigured() {
    List<LiveAlertConfiguration> liveAlertConfigurations = TestObjects.getLiveConfigurations();
    when(mockLiveAlertConfigurationQueryService.getAllLiveAlertConfigurations())
        .thenReturn(liveAlertConfigurations);
    ResponseEntity<List<LiveAlertConfiguration>> result = alertConfigurationApiDelegate
        .getAllLiveAlertConfigurations();

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).hasSize(liveAlertConfigurations.size());
  }

  //View Audit for Live Alert Configurations tests
  @Test
  public void shouldReturn401ResponseForAuditLiveConfigurationsWhenUnauthorisedUser() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<LiveAlertConfigurationAudit>> result = alertConfigurationApiDelegate
        .getAllLiveAlertConfigurationsAudit(TestUtils.randomInt(100), TestUtils.randomInt(10));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn204ResponseForAuditLiveConfigurationsWhenNoAuditFound() {
    ResponseEntity<List<LiveAlertConfigurationAudit>> result = alertConfigurationApiDelegate
        .getAllLiveAlertConfigurationsAudit(TestUtils.randomInt(100), TestUtils.randomInt(10));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  public void shouldReturn200ResponseWithDataForAuditLiveConfigurationsWhenAuditFound() {
    List<LiveAlertConfigurationAudit> audits = TestObjects.getLiveConfigurationAudits();
    when(mockLiveAlertConfigurationQueryService
        .getAllLiveAlertConfigurationsAudit(anyInt(), anyInt()))
        .thenReturn(audits);
    ResponseEntity<List<LiveAlertConfigurationAudit>> result = alertConfigurationApiDelegate
        .getAllLiveAlertConfigurationsAudit(TestUtils.randomInt(100), TestUtils.randomInt(10));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).hasSize(audits.size());
  }

  //View details of a LiveAlertConfiguration
  @Test
  public void shouldReturn401ResponseForViewLiveConfigurationWhenUnauthorisedUser() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .getLiveAlertConfiguration(TestUtils.randomAlphanumeric(5));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404ResponseForViewLiveConfigurationWhenNotFound() {
    when(mockLiveAlertConfigurationQueryService.getLiveAlertConfigurationById(any()))
        .thenReturn(Optional.empty());
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .getLiveAlertConfiguration(TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn200ResponseWithDataForViewLiveConfigurationWhenFound() throws Throwable {
    LiveAlertConfiguration liveConfiguration = TestObjects
        .getLiveConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());
    when(mockLiveAlertConfigurationQueryService.getLiveAlertConfigurationById(any()))
        .thenReturn(Optional.of(liveConfiguration));
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .getLiveAlertConfiguration(TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
  }

  //View details of a LiveAlertConfigurationAudit
  @Test
  public void shouldReturn401ResponseForViewLiveConfigurationAuditWhenUnauthorisedUser() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<LiveAlertConfigurationAudit> result = alertConfigurationApiDelegate
        .getAuditDetailsForLiveAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404ResponseForViewLiveConfigurationAuditWhenNotFound() throws Throwable {
    when(mockLiveAlertConfigurationQueryService.getLiveAlertConfigurationAuditById(any(), any()))
        .thenThrow(
            new AuditNotFoundException("Audit not found"));
    ResponseEntity<LiveAlertConfigurationAudit> result = alertConfigurationApiDelegate
        .getAuditDetailsForLiveAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn400ResponseForViewLiveConfigurationAuditWhenAlertConfigNotFound()
      throws Throwable {
    when(mockLiveAlertConfigurationQueryService.getLiveAlertConfigurationAuditById(any(), any()))
        .thenThrow(
            new AlertConfigurationNotFoundException("Alert config not found"));
    ResponseEntity<LiveAlertConfigurationAudit> result = alertConfigurationApiDelegate
        .getAuditDetailsForLiveAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldReturn200ResponseWithDataForViewLiveConfigurationAuditWhenFound()
      throws Throwable {
    LiveAlertConfigurationAudit audit = TestObjects
        .getLiveConfigurationAudit(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfigurationAudit.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());
    when(mockLiveAlertConfigurationQueryService.getLiveAlertConfigurationAuditById(any(), any()))
        .thenReturn(audit);
    ResponseEntity<LiveAlertConfigurationAudit> result = alertConfigurationApiDelegate
        .getAuditDetailsForLiveAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
  }

  //View audit history for a LiveAlertConfiguration
  @Test
  public void shouldReturn401ResponseForLiveConfigurationAuditHistoryWhenUnauthorisedUser() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<LiveAlertConfigurationAudit>> result = alertConfigurationApiDelegate
        .getAuditHistoryForLiveAlertConfiguration(TestUtils.randomAlphanumeric(5));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404ResponseForLiveConfigurationAuditHistoryWhenNotFound()
      throws Throwable {
    when(mockLiveAlertConfigurationQueryService.getAuditHistoryForLiveAlertConfiguration(any()))
        .thenThrow(
            new AlertConfigurationNotFoundException("Alert config not found"));
    ResponseEntity<List<LiveAlertConfigurationAudit>> result = alertConfigurationApiDelegate
        .getAuditHistoryForLiveAlertConfiguration(TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn200ResponseWithDataForLiveConfigurationAuditHistoryWhenFound()
      throws Throwable {
    when(mockLiveAlertConfigurationQueryService.getAuditHistoryForLiveAlertConfiguration(any()))
        .thenReturn(TestObjects.getLiveConfigurationAudits());
    ResponseEntity<List<LiveAlertConfigurationAudit>> result = alertConfigurationApiDelegate
        .getAuditHistoryForLiveAlertConfiguration(TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
  }

  @Test
  public void shouldReturn204ResponseForLiveConfigurationAuditHistoryWhenNoAuditsFound()
      throws Throwable {
    when(mockLiveAlertConfigurationQueryService.getAuditHistoryForLiveAlertConfiguration(any()))
        .thenReturn(Collections.emptyList());
    ResponseEntity<List<LiveAlertConfigurationAudit>> result = alertConfigurationApiDelegate
        .getAuditHistoryForLiveAlertConfiguration(TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  public void shouldCreateLiveAlertConfigForValidInput() throws Throwable {
    LiveAlertConfiguration savedConfig = TestObjects.getLiveConfigurations().get(0);
    when(mockLiveAlertConfigurationSaveService.createFromSandboxConfiguration(any(), any(), any()))
        .thenReturn(Optional.of(savedConfig));
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .createLiveAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedConfig.getAlertConfigurationUUID());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.LIVEALERTCONFIGURATION.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedConfig.getWhen());
  }

  @Test
  public void shouldReturnUnauthorisedForCreateLiveAlertConfigIfNoUserFound() throws Throwable {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .createLiveAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnErrorForCreateLiveAlertConfigIfConfigNotCreated() throws Throwable {
    when(mockLiveAlertConfigurationSaveService.createFromSandboxConfiguration(any(), any(), any()))
        .thenReturn(Optional.empty());
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .createLiveAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @Test
  public void shouldReturnBadRequestForCreateLiveAlertConfigIfSandboxConfigNotFound()
      throws Throwable {
    when(mockLiveAlertConfigurationSaveService.createFromSandboxConfiguration(any(), any(), any()))
        .thenThrow(new SandboxAlertConfigurationNotFoundException(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString()));
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .createLiveAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

  }

  //LiveAlertConfiguration-Tag relationship tests

  @Test
  public void whenValidLiveAlertConfigurationThenLinkTag() throws Throwable {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    EntityRelationship savedRelationship = TestObjects
        .getEntityRelationship(alertConfigurationId, tagId, EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG,
            USER);
    savedRelationship.setWhen(TestUtils.randomInstant());
    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);

    when(mockAlertConfigurationRelationshipService
        .createRelationship(alertConfigurationId.toString(), linkRequest,
            EntityType.LIVEALERTCONFIGURATION, EntityType.TAG,
            USER, null))
        .thenReturn(savedRelationship);

    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<EntityRelationship> response = alertConfigurationApiDelegate
        .linkTagToLiveAlertConfiguration(alertConfigurationId.toString(), linkRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getUser()).isEqualTo(USER);
    assertThat(response.getBody().getFromId()).isEqualTo(alertConfigurationId.toString());
    assertThat(response.getBody().getFromType())
        .isEqualTo(EntityType.LIVEALERTCONFIGURATION.toString());
    assertThat(response.getBody().getToId()).isEqualTo(linkRequest.getEntityId());
    assertThat(response.getBody().getToType()).isEqualTo(EntityType.TAG.toString());

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedRelationship.getFromId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.LIVEALERTCONFIGURATION_REL_TAG.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedRelationship.getWhen());

  }

  @Test
  public void shouldReturn404ResponseWhenInvalidLiveAlertConfigurationForLinkTagToLiveAlertConfiguration()
      throws Throwable {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);
    when(mockAlertConfigurationRelationshipService
        .createRelationship(alertConfigurationId.toString(), linkRequest,
            EntityType.LIVEALERTCONFIGURATION, EntityType.TAG,
            USER, null))
        .thenThrow(new AlertConfigurationNotFoundException("AlertConfiguration not found"));
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<EntityRelationship> response = alertConfigurationApiDelegate
        .linkTagToLiveAlertConfiguration(alertConfigurationId.toString(), linkRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void shouldReturn205whenValidLiveAlertConfigurationForDelinkTagFromLiveAlertConfiguration()
      throws Throwable {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();

    EntityRelationship savedRelationship = TestObjects
        .getEntityRelationship(alertConfigurationId, tagId, EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG,
            TestUtils.randomAlphanumeric(10));
    savedRelationship.setWhen(TestUtils.randomInstant());

    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    when(mockAlertConfigurationRelationshipService
        .deleteRelationship(alertConfigurationId.toString(), tagId.toString(),
            EntityType.LIVEALERTCONFIGURATION, EntityType.TAG,
            USER, null))
        .thenReturn(savedRelationship);

    ResponseEntity<Void> response = alertConfigurationApiDelegate
        .delinkTagFromLiveAlertConfiguration(alertConfigurationId.toString(), tagId.toString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedRelationship.getFromId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.LIVEALERTCONFIGURATION_REL_TAG.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedRelationship.getWhen());

  }

  @Test
  public void shouldReturn400ResponseWhenInvalidLiveAlertConfigurationForDelinkTagFromLiveAlertConfiguration()
      throws Throwable {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();

    when(mockAlertConfigurationRelationshipService
        .deleteRelationship(alertConfigurationId.toString(), tagId.toString(),
            EntityType.LIVEALERTCONFIGURATION, EntityType.TAG,
            USER, null))
        .thenThrow(new AlertConfigurationNotFoundException("AlertConfiguration not found"));
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    ResponseEntity<Void> response = alertConfigurationApiDelegate
        .delinkTagFromLiveAlertConfiguration(alertConfigurationId.toString(), tagId.toString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void shouldReturnNotAuthorisedResponseWhenInvalidPrincipleForLinkTagToLiveAlertConfiguration() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<EntityRelationship> result = alertConfigurationApiDelegate
        .linkTagToLiveAlertConfiguration(alertConfigurationId.toString(), linkRequest);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

  }

  @Test
  public void shouldReturnNotAuthorisedResponseWhenUserNotAuthorisedForDelinkTagFromLiveAlertConfiguration() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<Void> result = alertConfigurationApiDelegate
        .delinkTagFromLiveAlertConfiguration(alertConfigurationId.toString(), tagId.toString());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnResultsWhenLinkedTagsFoundForGetTagsByLiveAlertConfigurationId() {
    UUID alertConfigurationId = TestUtils.randomUUID();

    when(mockAlertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationId.toString(), EntityType.TAG))
        .thenReturn(
            Arrays.asList(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString()));
    ResponseEntity<List<String>> result = alertConfigurationApiDelegate
        .getTagsByLiveAlertConfigurationId(alertConfigurationId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedTagsFoundForGetTagsByLiveAlertConfigurationId() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    ResponseEntity<List<String>> result = alertConfigurationApiDelegate
        .getTagsByLiveAlertConfigurationId(alertConfigurationId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(0);
  }

  @Test
  public void shouldReturn400ResponseWhenAlertNotFoundForGetLiveAlertConfigurationTagRelationshipAudit()
      throws Throwable {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    when(mockAlertConfigurationRelationshipService
        .getRelationshipAudit(alertConfigurationId.toString(), tagId.toString(), null))
        .thenThrow(new AlertConfigurationNotFoundException(alertConfigurationId.toString()));
    ResponseEntity<List<EntityRelationshipAudit>> result = alertConfigurationApiDelegate
        .getLiveAlertConfigurationTagRelationshipAudit(alertConfigurationId.toString(),
            tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
    assertThat(result.getBody()).isNullOrEmpty();

  }

  @Test
  public void shouldReturn404ResponseWhenNoAuditHistoryForGetLiveAlertConfigurationTagRelationshipAudit() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    ResponseEntity<List<EntityRelationshipAudit>> result = alertConfigurationApiDelegate
        .getLiveAlertConfigurationTagRelationshipAudit(alertConfigurationId.toString(),
            tagId.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForGetLiveAlertConfigurationTagRelationshipAudit()
      throws Throwable {
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    List<EntityRelationshipAudit> auditHistory = TestObjects
        .getRelationshipAuditHistory(alertConfigurationId, tagId, EntityType.LIVEALERTCONFIGURATION,
            EntityType.TAG);
    when(mockAlertConfigurationRelationshipService
        .getRelationshipAudit(alertConfigurationId.toString(), tagId.toString(), null))
        .thenReturn(auditHistory);

    ResponseEntity<List<EntityRelationshipAudit>> result = alertConfigurationApiDelegate
        .getLiveAlertConfigurationTagRelationshipAudit(alertConfigurationId.toString(),
            tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).isEqualTo(auditHistory);
  }

  @Test
  public void shouldReturnLiveAlertConfigurationWhenStatusIsINACTIVEForUpdateLiveAlertConfigurationStatus()
      throws AlertConfigurationNotFoundException {
    LiveAlertConfiguration liveConfiguration = TestObjects
        .getLiveConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());
    UpdateStatus status = new UpdateStatus();
    status.setStatus(UpdateStatus.StatusEnum.INACTIVE);
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockLiveAlertConfigurationSaveService
        .updateStatus(liveConfiguration.getAlertConfigurationUUID(), status, USER))
        .thenReturn(liveConfiguration);
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .updateLiveAlertConfigurationStatus(liveConfiguration.getAlertConfigurationUUID(), status);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(result.getBody().getAlertConfigurationUUID());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.LIVEALERTCONFIGURATION.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated()).isEqualTo(
        result.getBody().getUpdatedWhen());

  }

  @Test
  public void shouldReturn404WhenConfigurationIdIsInValidForUpdateLiveAlertConfigurationStatus()
      throws AlertConfigurationNotFoundException {
    LiveAlertConfiguration liveConfiguration = TestObjects
        .getLiveConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());
    UpdateStatus status = new UpdateStatus();
    status.setStatus(UpdateStatus.StatusEnum.INACTIVE);
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockLiveAlertConfigurationSaveService
        .updateStatus(liveConfiguration.getAlertConfigurationUUID(), status, USER))
        .thenThrow(
            new AlertConfigurationNotFoundException(liveConfiguration.getAlertConfigurationUUID()));
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .updateLiveAlertConfigurationStatus(liveConfiguration.getAlertConfigurationUUID(), status);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

  }

  @Test
  public void shouldReturnUnAuthorizedWhenPrincipleIsInValidForUpdateLiveAlertConfigurationStatus() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<LiveAlertConfiguration> result = alertConfigurationApiDelegate
        .updateLiveAlertConfigurationStatus("", new UpdateStatus());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnResultsWhenLinkedTagsFoundForGetTagsByLiveAlertConfigurationIds() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    List<String> alertConfigurationIds = new ArrayList<>();
    alertConfigurationIds.add(alertConfigurationId.toString());

    when(mockAlertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationId.toString(), EntityType.TAG))
        .thenReturn(
            Arrays.asList(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString()));
    ResponseEntity<AlertConfigurationTagsMap> result = alertConfigurationApiDelegate
        .getBulkTagRequestForLiveAlertConfigurationIds(alertConfigurationIds);

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(1);
    assertThat(result.getBody().get(alertConfigurationId.toString())).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedTagsFoundForGetTagsByLiveAlertConfigurationIds() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    List<String> alertConfigurationIds = new ArrayList<>();
    alertConfigurationIds.add(alertConfigurationId.toString());
    ResponseEntity<AlertConfigurationTagsMap> result = alertConfigurationApiDelegate
        .getBulkTagRequestForLiveAlertConfigurationIds(alertConfigurationIds);

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(1);
    assertThat(result.getBody().get(alertConfigurationId.toString())).hasSize(0);
  }

  @Test
  public void shouldReturnUnAuthorizedWhenPrincipleIsInValidForGetTagsByLiveAlertConfigurationIds() {
    UUID alertConfigurationId = TestUtils.randomUUID();
    List<String> alertConfigurationIds = new ArrayList<>();
    alertConfigurationIds.add(alertConfigurationId.toString());
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<AlertConfigurationTagsMap> result = alertConfigurationApiDelegate
        .getBulkTagRequestForLiveAlertConfigurationIds(alertConfigurationIds);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

}
