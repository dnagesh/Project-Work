package com.webtech.service.alertconfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.AuditNotFoundException;
import com.webtech.service.alertconfiguration.exception.IllegalParameterException;
import com.webtech.service.alertconfiguration.exception.SandboxAlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.SandboxNotFoundException;
import com.irisium.service.alertconfiguration.model.AlertConfigurationTagsMap;
import com.irisium.service.alertconfiguration.model.CloneSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.CreateSandboxRequest;
import com.irisium.service.alertconfiguration.model.CreateSandboxRunRequest;
import com.irisium.service.alertconfiguration.model.CreateUpdateSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.EntityLinkRequest;
import com.irisium.service.alertconfiguration.model.EntityRelationship;
import com.irisium.service.alertconfiguration.model.EntityRelationshipAudit;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration.StatusEnum;
import com.irisium.service.alertconfiguration.model.LiveAlertConfigurationAudit;
import com.irisium.service.alertconfiguration.model.Sandbox;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfiguration;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfigurationAudit;
import com.irisium.service.alertconfiguration.model.SandboxResetOptions;
import com.irisium.service.alertconfiguration.model.SandboxRun;
import com.irisium.service.alertconfiguration.model.UpdateStatus;
import com.webtech.service.alertconfiguration.service.AlertConfigurationRelationshipService;
import com.webtech.service.alertconfiguration.service.LiveAlertConfigurationQueryService;
import com.webtech.service.alertconfiguration.service.SandboxAlertConfigurationService;
import com.webtech.service.alertconfiguration.service.SandboxGFTImpl;
import com.webtech.service.alertconfiguration.service.SandboxRunService;
import com.webtech.service.alertconfiguration.service.SandboxService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.common.security.PrincipalProvider;
import com.webtech.service.entityrelationship.model.EntityType;
import com.webtech.service.guinotification.GuiNotificationMessage;
import com.webtech.service.guinotification.GuiNotificationPublisher;
import com.webtech.service.guinotification.GuiNotificationTypeEnum;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
public class SandboxesApiDelegateImplTest {

  private static final String USER = "User";
  @Mock
  SandboxAlertConfigurationService mockSandboxAlertConfigurationService;
  @Captor
  ArgumentCaptor<String> sandboxUUIDArgument;
  @Captor
  ArgumentCaptor<String> alertConfigUUIDArgument;
  @Captor
  private ArgumentCaptor<GuiNotificationMessage> guiNotificationMessageArgumentCaptor;

  private SandboxesApiDelegateImpl sandboxesApiDelegateImpl;
  @Mock
  private PrincipalProvider mockPrincipalProvider;
  @Mock
  private SandboxService mockSandboxService;
  @Mock
  private SandboxRunService mockSandboxRunService;
  @Mock
  private SandboxGFTImpl mockSandboxGFTImpl;
  @Mock
  private LiveAlertConfigurationQueryService mockLiveAlertConfigurationQueryService;
  @Mock
  private AlertConfigurationRelationshipService mockAlertConfigurationRelationshipService;
  @Mock
  private GuiNotificationPublisher mockGuiNotificationPublisher;


  private Sandbox sandbox;
  private SandboxRun sandboxRun;

  private UUID sandboxUUID = TestUtils.randomUUID();
  private UUID alertConfigUUID = TestUtils.randomUUID();
  private CreateUpdateSandboxAlertConfigRequest request = mock(
      CreateUpdateSandboxAlertConfigRequest.class);

  @Before
  public void setup() {

    sandboxesApiDelegateImpl = new SandboxesApiDelegateImpl(mockSandboxService,
        mockSandboxRunService, mockPrincipalProvider, mockSandboxAlertConfigurationService,
        mockLiveAlertConfigurationQueryService, mockSandboxGFTImpl,
        mockAlertConfigurationRelationshipService);
    sandboxesApiDelegateImpl.setGuiNotificationPublisher(mockGuiNotificationPublisher);
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.of(USER));
    sandbox = TestObjects.getSandbox();
    sandboxRun = TestObjects.getSandboxRun();
  }


  @Test
  public void createSandboxShouldReturnSuccess() throws Throwable {
    CreateSandboxRequest request = new CreateSandboxRequest();
    when(mockSandboxService.createSandbox(any(), any())).thenReturn(sandbox);
    ResponseEntity<Sandbox> result = sandboxesApiDelegateImpl.createSandbox(request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(sandbox.getId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOX.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(sandbox.getCreatedWhen());

  }

  @Test
  public void createSandboxShouldReturnUnAuthorized() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    CreateSandboxRequest request = new CreateSandboxRequest();
    ResponseEntity<Sandbox> result = sandboxesApiDelegateImpl.createSandbox(request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnBadRequestForCreateSandboxFromSelectedIfLiveConfigNotFound()
      throws Throwable {
    when(mockSandboxService.createSandbox(any(), any()))
        .thenThrow(new AlertConfigurationNotFoundException(TestUtils.randomUUID().toString()));
    ResponseEntity<Sandbox> result = sandboxesApiDelegateImpl
        .createSandbox(new CreateSandboxRequest());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void deleteSandboxByIdShouldReturnSuccess() {

    ResponseEntity<Void> result = sandboxesApiDelegateImpl
        .deleteSandboxById(sandbox.getId());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(sandbox.getId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOX.name());

  }

  @Test
  public void deleteSandboxByIdShouldReturnBadRequest() throws SandboxNotFoundException {

    doThrow(new SandboxNotFoundException(sandbox.getId())).when(mockSandboxService)
        .deleteSandboxById(anyString());
    ResponseEntity<Void> result = sandboxesApiDelegateImpl
        .deleteSandboxById(sandbox.getId());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void getAllSandboxesShouldReturnSuccess() {

    List<Sandbox> sandboxes = TestObjects.getSandboxesList();
    when(mockSandboxService.getAllSandboxes()).thenReturn(sandboxes);
    ResponseEntity<List<Sandbox>> responseEntity = sandboxesApiDelegateImpl.getAllSandboxes();
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseEntity.getBody()).hasSameSizeAs(sandboxes);
    assertThat(responseEntity.getBody().get(0).getId()).isEqualTo(sandboxes.get(0).getId());
    assertThat(responseEntity.getBody().get(1).getId()).isEqualTo(sandboxes.get(1).getId());
  }

  @Test
  public void getSandboxByIdShouldReturnSuccess() {
    when(mockSandboxService.getSandboxById(sandbox.getId())).thenReturn(Optional.of(sandbox));
    ResponseEntity<Sandbox> result = sandboxesApiDelegateImpl.getSandboxById(sandbox.getId());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody().getId()).isEqualTo(sandbox.getId());
  }

  @Test
  public void getSandboxByIdShouldReturnNotFound() {
    ResponseEntity<Sandbox> result = sandboxesApiDelegateImpl
        .getSandboxById(TestUtils.randomAlphanumeric(10));
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnSuccessForAnyGivenSandboxIdIdForGetAllRunsBySandboxId() {

    ResponseEntity<List<SandboxRun>> responseEntity = sandboxesApiDelegateImpl
        .getAllRunsBySandboxId(TestUtils.randomAlphanumeric(10));
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturnSandboxIfSandboxIdExistsForGetSandboxRunById() {

    when(mockSandboxRunService.getSandboxRunByRunId(sandboxRun.getSandboxId(), sandboxRun.getId()))
        .thenReturn(Optional.of(sandboxRun));
    ResponseEntity<SandboxRun> result = sandboxesApiDelegateImpl
        .getSandboxRunById(sandboxRun.getSandboxId(), sandboxRun.getId());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody().getId()).isEqualTo(sandboxRun.getId());
    assertThat(result.getBody().getSandboxId()).isEqualTo(sandboxRun.getSandboxId());
  }

  @Test
  public void shouldReturnNotFoundIfRunIdNotExistsForGetSandboxRunById() {
    ResponseEntity<SandboxRun> result = sandboxesApiDelegateImpl
        .getSandboxRunById(sandboxRun.getSandboxId(), sandboxRun.getId());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnSuccessForAnyGivenSandboxIdForCreateSandboxRun() {

    CreateSandboxRunRequest request = new CreateSandboxRunRequest();
    when(mockSandboxService.getSandboxById(any()))
        .thenReturn(Optional.ofNullable(mock(Sandbox.class)));
    when(mockSandboxRunService.createSandboxRun(any(), any(), any())).thenReturn(sandboxRun);
    ResponseEntity<SandboxRun> result = sandboxesApiDelegateImpl
        .createSandboxRun(TestUtils.randomAlphanumeric(10), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(mockGuiNotificationPublisher, times(2))
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(sandboxRun.getId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOXRUN.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(sandboxRun.getRunStartTime());
  }

  @Test
  public void shouldReturn400WhenSandboxIdInvalidForCreateSandboxRun() {

    CreateSandboxRunRequest request = new CreateSandboxRunRequest();
    when(mockSandboxService.getSandboxById(any()))
        .thenReturn(Optional.empty());
    ResponseEntity<SandboxRun> result = sandboxesApiDelegateImpl
        .createSandboxRun(TestUtils.randomAlphanumeric(10), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldReturnUnAuthorizedWhenPrincipleIsNotGivenForCreateSandboxRun() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    CreateSandboxRunRequest request = new CreateSandboxRunRequest();
    ResponseEntity<SandboxRun> result = sandboxesApiDelegateImpl
        .createSandboxRun(TestUtils.randomAlphanumeric(), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnUnauthorisedIfNoUserFoundForAddSandboxAlertConfiguration() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .addSandboxAlertConfiguration(TestUtils.randomUUID().toString(), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnNotFoundIfNoSandboxFoundForAddSandboxAlertConfiguration() {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.empty());
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .addSandboxAlertConfiguration(TestUtils.randomUUID().toString(), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnInternalErrorIfNoSuchAlgorithmFoundForAddSandboxAlertConfiguration()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any()))
        .thenReturn(Optional.ofNullable(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.addAlertConfigToSandbox(any(), any(), anyString()))
        .thenThrow(new NoSuchAlgorithmException());
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .addSandboxAlertConfiguration(TestUtils.randomUUID().toString(), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void shouldReturnInternalErrorIfIOExceptionForAddSandboxAlertConfiguration()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any()))
        .thenReturn(Optional.ofNullable(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.addAlertConfigToSandbox(any(), any(), anyString()))
        .thenThrow(new IOException("Test"));
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .addSandboxAlertConfiguration(TestUtils.randomUUID().toString(), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
  }

  @Test
  public void shouldReturnInternalErrorIfEntityNotFoundExceptionForAddSandboxAlertConfiguration()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any()))
        .thenReturn(Optional.ofNullable(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.addAlertConfigToSandbox(any(), any(), anyString()))
        .thenThrow(new EntityNotFoundException("Test", "Test"));
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .addSandboxAlertConfiguration(TestUtils.randomUUID().toString(), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
  }

  @Test
  public void shouldReturnBadRequestIfIllegalParameterExceptionForAddSandboxAlertConfiguration()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any()))
        .thenReturn(Optional.ofNullable(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.addAlertConfigToSandbox(any(), any(), anyString()))
        .thenThrow(new IllegalParameterException("Test"));
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .addSandboxAlertConfiguration(TestUtils.randomUUID().toString(), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
  }


  @Test
  public void shouldReturnInternalErrorIfIOExceptionForUpdateSandboxAlertConfiguration()
      throws Throwable {

    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(
        Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .createUpdateSandboxAlertConfiguration(any(), any(), any(), any()))
        .thenThrow(new IOException("Test"));
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(),
            request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
  }

  @Test
  public void shouldReturnInternalErrorIfEntityNotFoundExceptionForUpdateSandboxAlertConfiguration()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any()))
        .thenReturn(Optional.ofNullable(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .createUpdateSandboxAlertConfiguration(any(), any(), any(), any()))
        .thenThrow(new EntityNotFoundException("Test", "Test"));
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(),
            request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
  }

  @Test
  public void shouldReturnBadRequestIfIllegalParameterExceptionForUpdateSandboxAlertConfiguration()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any()))
        .thenReturn(Optional.ofNullable(mock(Sandbox.class)));

    when(mockSandboxAlertConfigurationService
        .createUpdateSandboxAlertConfiguration(any(), any(), any(), any()))
        .thenThrow(new IllegalParameterException("Test"));
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(),
            request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
  }

  @Test
  public void shouldReturnSandboxAlertConfigurationIfSuccessForAddSandboxAlertConfiguration()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any()))
        .thenReturn(Optional.ofNullable(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.addAlertConfigToSandbox(any(), any(), anyString()))
        .thenReturn(mock(SandboxAlertConfiguration.class));
    CreateUpdateSandboxAlertConfigRequest
        request = mock(CreateUpdateSandboxAlertConfigRequest.class);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .addSandboxAlertConfiguration(TestUtils.randomUUID().toString(), request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();

  }

  @Test
  public void shouldReturnUnauthorisedResponseIfNoUserFoundForGetOriginalSandboxConfig() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());

    ResponseEntity<SandboxAlertConfigurationAudit> result = sandboxesApiDelegateImpl
        .getOriginalSandboxAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnBadRequestIfNoSandboxFoundForGetOriginalSandboxConfig() {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.empty());

    ResponseEntity<SandboxAlertConfigurationAudit> result = sandboxesApiDelegateImpl
        .getOriginalSandboxAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldReturnNotFoundIfNoAuditFoundForGetOriginalSandboxConfig() {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.getOriginalSandboxAlertConfiguration(any(), any()))
        .thenReturn(Optional.empty());
    ResponseEntity<SandboxAlertConfigurationAudit> result = sandboxesApiDelegateImpl
        .getOriginalSandboxAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnAuditIfWhenFoundForGetOriginalSandboxConfig() {
    UUID sandboxUUID = TestUtils.randomUUID();
    UUID alertConfigUUID = TestUtils.randomUUID();
    Sandbox sandbox = new Sandbox();
    sandbox.setId(sandboxUUID.toString());
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.ofNullable(sandbox));
    when(mockSandboxAlertConfigurationService.getOriginalSandboxAlertConfiguration(any(), any()))
        .thenReturn(Optional.of(mock(SandboxAlertConfigurationAudit.class)));

    ResponseEntity<SandboxAlertConfigurationAudit> result = sandboxesApiDelegateImpl
        .getOriginalSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString());

    verify(mockSandboxAlertConfigurationService, times(1))
        .getOriginalSandboxAlertConfiguration(sandboxUUIDArgument.capture(),
            alertConfigUUIDArgument.capture());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(sandboxUUIDArgument.getValue()).isNotBlank();
    assertThat(sandboxUUIDArgument.getValue()).isEqualTo(sandboxUUID.toString());
    assertThat(alertConfigUUIDArgument.getValue()).isNotBlank();
    assertThat(alertConfigUUIDArgument.getValue()).isEqualTo(alertConfigUUID.toString());
  }

  @Test
  public void shouldUpdateSandboxAlertConfigForValidInput() throws Throwable {

    SandboxAlertConfiguration savedConfig = TestObjects
        .getSandboxConfiguration(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(
        Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .createUpdateSandboxAlertConfiguration(any(), any(), any(), any())).thenReturn(savedConfig);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(),
            request);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(mockGuiNotificationPublisher, times(2))
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedConfig.getAlertConfigurationUUID());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedConfig.getUpdatedWhen());
  }

  @Test
  public void shouldReturnUnauthorisedForUpdateSandboxAlertConfigIfNoUserFound() throws Throwable {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(),
            request);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnNotFoundForUpdateSandboxAlertConfigIfConfigNotFound() throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .createUpdateSandboxAlertConfiguration(any(), any(), any(), any())).thenThrow(
        new SandboxAlertConfigurationNotFoundException(sandboxUUID.toString(),
            alertConfigUUID.toString()));
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(),
            request);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnInternalErrorForUpdateSandboxAlertConfigIfNoSuchAlgoException()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .createUpdateSandboxAlertConfiguration(any(), any(), any(), any()))
        .thenThrow(new NoSuchAlgorithmException());
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(),
            request);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @Test
  public void shouldReturnBadRequestForUpdateSandboxAlertAlertConfigIfSandboxNotFound()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.empty());
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(),
            request);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

  }

  @Test
  public void shouldReturnUnauthorisedForGetLiveConfigForSandboxConfigIfNoUserFound()
      throws Throwable {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<LiveAlertConfiguration> result = sandboxesApiDelegateImpl
        .getLiveConfigForSandboxAlertConfiguration(sandboxUUID.toString(),
            alertConfigUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnBadRequestForGetLiveConfigForSandboxConfigIfNoSandboxConfigFound()
      throws Throwable {
    when(mockSandboxAlertConfigurationService.getSandboxAlertConfiguration(any(), any()))
        .thenReturn(Optional.empty());
    ResponseEntity<LiveAlertConfiguration> result = sandboxesApiDelegateImpl
        .getLiveConfigForSandboxAlertConfiguration(sandboxUUID.toString(),
            alertConfigUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldReturnNotFoundForGetLiveConfigForSandboxConfigIfNoLiveConfigUUIDFound()
      throws Throwable {
    SandboxAlertConfiguration sandboxAlertConfiguration = new SandboxAlertConfiguration();
    when(mockSandboxAlertConfigurationService.getSandboxAlertConfiguration(any(), any()))
        .thenReturn(Optional.of(sandboxAlertConfiguration));

    ResponseEntity<LiveAlertConfiguration> result = sandboxesApiDelegateImpl
        .getLiveConfigForSandboxAlertConfiguration(sandboxUUID.toString(),
            alertConfigUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnNotFoundForGetLiveConfigForSandboxConfigIfNoLiveConfigFound()
      throws Throwable {
    SandboxAlertConfiguration sandboxAlertConfiguration = new SandboxAlertConfiguration();
    sandboxAlertConfiguration.setLiveConfigUUID(TestUtils.randomUUID().toString());
    when(mockSandboxAlertConfigurationService.getSandboxAlertConfiguration(any(), any()))
        .thenReturn(Optional.of(sandboxAlertConfiguration));
    when(mockLiveAlertConfigurationQueryService
        .getLiveAlertConfigurationById(sandboxAlertConfiguration.getLiveConfigUUID()))
        .thenReturn(Optional.empty());
    ResponseEntity<LiveAlertConfiguration> result = sandboxesApiDelegateImpl
        .getLiveConfigForSandboxAlertConfiguration(sandboxUUID.toString(),
            alertConfigUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnLiveConfigForGetLiveConfigForSandboxConfigIfLiveConfigFound()
      throws Throwable {
    SandboxAlertConfiguration sandboxAlertConfiguration = new SandboxAlertConfiguration();
    sandboxAlertConfiguration.setLiveConfigUUID(TestUtils.randomUUID().toString());
    when(mockSandboxAlertConfigurationService.getSandboxAlertConfiguration(any(), any()))
        .thenReturn(Optional.of(sandboxAlertConfiguration));
    when(mockLiveAlertConfigurationQueryService
        .getLiveAlertConfigurationById(sandboxAlertConfiguration.getLiveConfigUUID()))
        .thenReturn(Optional.of(mock(LiveAlertConfiguration.class)));
    ResponseEntity<LiveAlertConfiguration> result = sandboxesApiDelegateImpl
        .getLiveConfigForSandboxAlertConfiguration(sandboxUUID.toString(),
            alertConfigUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturnUnauthorisedForGetSandboxConfigsIfNoUserFound() throws Throwable {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<List<SandboxAlertConfiguration>> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfigurations(sandboxUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnNotFoundForGetSandboxConfigsIfNoSandboxFound() throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.empty());
    ResponseEntity<List<SandboxAlertConfiguration>> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfigurations(sandboxUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnNoContentForGetSandboxConfigsIfNoSandboxConfigsFound() throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.getSandboxAlertConfigurations(any())).thenReturn(
        Collections.emptyList());
    ResponseEntity<List<SandboxAlertConfiguration>> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfigurations(sandboxUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  public void shouldReturnDataForGetSandboxConfigsIfSandboxConfigsFound() throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.getSandboxAlertConfigurations(any()))
        .thenReturn(TestObjects.getSandboxAlertConfigurations());
    ResponseEntity<List<SandboxAlertConfiguration>> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfigurations(sandboxUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  //SandboxAlertConfiguration-Tag relationship tests

  @Test
  public void whenValidSandboxAlertConfigurationThenLinkTag() throws Throwable {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    EntityRelationship savedRelationship = TestObjects
        .getEntityRelationship(alertConfigId, tagId, EntityType.SANDBOXALERTCONFIGURATION,
            EntityType.TAG,
            USER);
    savedRelationship.setWhen(TestUtils.randomInstant());
    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);

    when(mockAlertConfigurationRelationshipService
        .createRelationship(alertConfigId.toString(), linkRequest,
            EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG,
            USER, sandboxId.toString()))
        .thenReturn(savedRelationship);

    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<EntityRelationship> response = sandboxesApiDelegateImpl
        .linkTagToSandboxAlertConfiguration(sandboxId.toString(), alertConfigId.toString(),
            linkRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getUser()).isEqualTo(USER);
    assertThat(response.getBody().getFromId()).isEqualTo(alertConfigId.toString());
    assertThat(response.getBody().getFromType())
        .isEqualTo(EntityType.SANDBOXALERTCONFIGURATION.toString());
    assertThat(response.getBody().getToId()).isEqualTo(linkRequest.getEntityId());
    assertThat(response.getBody().getToType()).isEqualTo(EntityType.TAG.toString());

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedRelationship.getFromId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION_REL_TAG.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedRelationship.getWhen());

  }

  @Test
  public void shouldReturn404ResponseWhenInvalidSandboxAlertConfigurationForLinkTagToLiveAlertConfiguration()
      throws Throwable {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);
    when(mockAlertConfigurationRelationshipService
        .createRelationship(alertConfigurationId.toString(), linkRequest,
            EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG,
            USER, sandboxId.toString()))
        .thenThrow(new AlertConfigurationNotFoundException("AlertConfiguration not found"));
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    ResponseEntity<EntityRelationship> response = sandboxesApiDelegateImpl
        .linkTagToSandboxAlertConfiguration(sandboxId.toString(), alertConfigurationId.toString(),
            linkRequest);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void shouldReturn205whenValidSandboxAlertConfigurationForDelinkTagFromLiveAlertConfiguration()
      throws Throwable {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();

    EntityRelationship savedRelationship = TestObjects
        .getEntityRelationship(alertConfigurationId, tagId, EntityType.SANDBOXALERTCONFIGURATION,
            EntityType.TAG,
            TestUtils.randomAlphanumeric(10));
    savedRelationship.setWhen(TestUtils.randomInstant());

    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));

    when(mockAlertConfigurationRelationshipService
        .deleteRelationship(alertConfigurationId.toString(), tagId.toString(),
            EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG,
            USER, sandboxId.toString()))
        .thenReturn(savedRelationship);

    ResponseEntity<Void> response = sandboxesApiDelegateImpl
        .delinkTagFromSandboxAlertConfiguration(sandboxId.toString(),
            alertConfigurationId.toString(), tagId.toString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

    verify(mockGuiNotificationPublisher)
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(savedRelationship.getFromId());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION_REL_TAG.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated())
        .isEqualTo(savedRelationship.getWhen());

  }

  @Test
  public void shouldReturn400ResponseWhenInvalidSandboxAlertConfigurationForDelinkTagFromSandboxAlertConfiguration()
      throws Throwable {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();

    when(mockAlertConfigurationRelationshipService
        .deleteRelationship(alertConfigurationId.toString(), tagId.toString(),
            EntityType.SANDBOXALERTCONFIGURATION, EntityType.TAG,
            USER, sandboxId.toString()))
        .thenThrow(new AlertConfigurationNotFoundException("AlertConfiguration not found"));
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    ResponseEntity<Void> response = sandboxesApiDelegateImpl
        .delinkTagFromSandboxAlertConfiguration(sandboxId.toString(),
            alertConfigurationId.toString(), tagId.toString());
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isNull();
  }

  @Test
  public void shouldReturnNotAuthorisedResponseWhenInvalidPrincipleForLinkTagToSandboxAlertConfiguration() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    EntityLinkRequest linkRequest = TestObjects.getEntityLinkRequest(tagId);
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<EntityRelationship> result = sandboxesApiDelegateImpl
        .linkTagToSandboxAlertConfiguration(sandboxId.toString(), alertConfigurationId.toString(),
            linkRequest);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

  }

  @Test
  public void shouldReturnNotAuthorisedResponseWhenUserNotAuthorisedForDelinkTagFromSandboxAlertConfiguration() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<Void> result = sandboxesApiDelegateImpl
        .delinkTagFromSandboxAlertConfiguration(sandboxId.toString(),
            alertConfigurationId.toString(), tagId.toString());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnResultsWhenLinkedTagsFoundForGetTagsBySandboxAlertConfigurationId() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();

    when(mockAlertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationId.toString(), EntityType.TAG))
        .thenReturn(
            Arrays.asList(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString()));
    ResponseEntity<List<String>> result = sandboxesApiDelegateImpl
        .getTagsBySandboxAlertConfigurationId(sandboxId.toString(),
            alertConfigurationId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedTagsFoundForGetTagsBySandboxAlertConfigurationId() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    ResponseEntity<List<String>> result = sandboxesApiDelegateImpl
        .getTagsBySandboxAlertConfigurationId(sandboxId.toString(),
            alertConfigurationId.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(0);
  }

  @Test
  public void shouldReturn400ResponseWhenAlertNotFoundForGetSandboxAlertConfigurationTagRelationshipAudit()
      throws Throwable {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    when(mockAlertConfigurationRelationshipService
        .getRelationshipAudit(alertConfigurationId.toString(), tagId.toString(),
            sandboxId.toString()))
        .thenThrow(new AlertConfigurationNotFoundException(alertConfigurationId.toString()));
    ResponseEntity<List<EntityRelationshipAudit>> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfigurationTagRelationshipAudit(sandboxId.toString(),
            alertConfigurationId.toString(), tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
    assertThat(result.getBody()).isNullOrEmpty();

  }

  @Test
  public void shouldReturn404ResponseWhenNoAuditHistoryForGetSandboxAlertConfigurationTagRelationshipAudit() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    ResponseEntity<List<EntityRelationshipAudit>> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfigurationTagRelationshipAudit(sandboxId.toString(),
            alertConfigurationId.toString(), tagId.toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getBody()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnDataWhenAuditHistoryFoundForGetSandboxAlertConfigurationTagRelationshipAudit()
      throws Throwable {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID tagId = TestUtils.randomUUID();
    List<EntityRelationshipAudit> auditHistory = TestObjects
        .getRelationshipAuditHistory(alertConfigurationId, tagId,
            EntityType.SANDBOXALERTCONFIGURATION,
            EntityType.TAG);
    when(mockAlertConfigurationRelationshipService
        .getRelationshipAudit(alertConfigurationId.toString(), tagId.toString(),
            sandboxId.toString()))
        .thenReturn(auditHistory);

    ResponseEntity<List<EntityRelationshipAudit>> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfigurationTagRelationshipAudit(sandboxId.toString(),
            alertConfigurationId.toString(), tagId.toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).isEqualTo(auditHistory);
  }

  @Test
  public void shouldReturnSandboxAlertConfigurationWhenStatusIsINACTIVEForUpdateSandboxAlertConfigurationStatus()
      throws SandboxAlertConfigurationNotFoundException {
    SandboxAlertConfiguration sandboxConfiguration = TestObjects
        .getSandboxConfiguration(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    UpdateStatus status = new UpdateStatus();
    status.setStatus(UpdateStatus.StatusEnum.INACTIVE);
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.updateStatus(sandboxConfiguration.getSandboxUUID(),
        sandboxConfiguration.getAlertConfigurationUUID(), status, USER))
        .thenReturn(sandboxConfiguration);
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfigurationStatus(sandboxConfiguration.getSandboxUUID(),
            sandboxConfiguration.getAlertConfigurationUUID(), status);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    verify(mockGuiNotificationPublisher, times(2))
        .sendGuiNotification(guiNotificationMessageArgumentCaptor.capture());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityId())
        .isEqualTo(result.getBody().getAlertConfigurationUUID());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getEntityType()).isEqualTo(
        GuiNotificationTypeEnum.SANDBOXALERTCONFIGURATION.name());
    assertThat(guiNotificationMessageArgumentCaptor.getValue().getLastUpdated()).isEqualTo(
        result.getBody().getUpdatedWhen());

  }

  @Test
  public void shouldReturn404WhenConfigurationIdIsInValidForUpdateSandboxAlertConfigurationStatus()
      throws SandboxAlertConfigurationNotFoundException {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    SandboxAlertConfiguration sandboxConfiguration = TestObjects
        .getSandboxConfiguration(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    UpdateStatus status = new UpdateStatus();
    status.setStatus(UpdateStatus.StatusEnum.INACTIVE);
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    when(mockSandboxAlertConfigurationService.updateStatus(sandboxConfiguration.getSandboxUUID(),
        sandboxConfiguration.getAlertConfigurationUUID(), status, USER))
        .thenThrow(
            new SandboxAlertConfigurationNotFoundException(sandboxConfiguration.getSandboxUUID(),
                sandboxConfiguration.getAlertConfigurationUUID()));
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfigurationStatus(sandboxConfiguration.getSandboxUUID(),
            sandboxConfiguration.getAlertConfigurationUUID(), status);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

  }

  @Test
  public void shouldReturn400WhenConfigurationIdIsInValidForUpdateSandboxAlertConfigurationStatus()
      throws SandboxAlertConfigurationNotFoundException {
    SandboxAlertConfiguration sandboxConfiguration = TestObjects
        .getSandboxConfiguration(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    UpdateStatus status = new UpdateStatus();
    status.setStatus(UpdateStatus.StatusEnum.INACTIVE);
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(USER));
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfigurationStatus(sandboxConfiguration.getSandboxUUID(),
            sandboxConfiguration.getAlertConfigurationUUID(), status);
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

  }

  @Test
  public void shouldReturnUnAuthorizedWhenPrincipleIsInValidForUpdateSandboxAlertConfigurationStatus() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .updateSandboxAlertConfigurationStatus("", "", new UpdateStatus());
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnNotFoundIfNoSandboxFoundForResetSandbox() throws Throwable {
    when(mockSandboxService.resetSandbox(any(), any(), any()))
        .thenThrow(new SandboxNotFoundException("sandboxId"));
    ResponseEntity<Sandbox> result = sandboxesApiDelegateImpl
        .resetSandbox(TestUtils.randomUUID().toString(), new SandboxResetOptions());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnUnauthorisedIfNoUserFoundForResetSandbox() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<Sandbox> result = sandboxesApiDelegateImpl
        .resetSandbox(TestUtils.randomUUID().toString(), new SandboxResetOptions());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnSandboxIfSandboxFoundForResetSandbox() throws Throwable {
    when(mockSandboxService.resetSandbox(any(), any(), any())).thenReturn(mock(Sandbox.class));
    ResponseEntity<Sandbox> result = sandboxesApiDelegateImpl
        .resetSandbox(TestUtils.randomUUID().toString(), new SandboxResetOptions());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  //View Audit for Sandbox Alert Configurations tests
  @Test
  public void shouldReturn401ResponseForAuditSandboxConfigurationsWhenUnauthorisedUser() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<SandboxAlertConfigurationAudit>> result = sandboxesApiDelegateImpl
        .getAllSandboxAlertConfigurationsAudit(TestUtils.randomAlphanumeric(5),
            TestUtils.randomInt(100), TestUtils.randomInt(10));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn204ResponseForAuditSandboxConfigurationsWhenNoAuditFound() {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    ResponseEntity<List<SandboxAlertConfigurationAudit>> result = sandboxesApiDelegateImpl
        .getAllSandboxAlertConfigurationsAudit(TestUtils.randomAlphanumeric(5),
            TestUtils.randomInt(100), TestUtils.randomInt(10));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  public void shouldReturn400ResponseForAuditSandboxConfigurationsWhenNoAuditFound() {
    ResponseEntity<List<SandboxAlertConfigurationAudit>> result = sandboxesApiDelegateImpl
        .getAllSandboxAlertConfigurationsAudit(TestUtils.randomAlphanumeric(5),
            TestUtils.randomInt(100), TestUtils.randomInt(10));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }


  @Test
  public void shouldReturn200ResponseWithDataForAuditSandboxConfigurationsWhenAuditFound() {
    List<SandboxAlertConfigurationAudit> audits = TestObjects.getSandboxConfigurationAudits();
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .getAllSandboxAlertConfigurationsAudit(any(), anyInt(), anyInt()))
        .thenReturn(audits);
    ResponseEntity<List<SandboxAlertConfigurationAudit>> result = sandboxesApiDelegateImpl
        .getAllSandboxAlertConfigurationsAudit(TestUtils.randomAlphanumeric(5),
            TestUtils.randomInt(100), TestUtils.randomInt(10));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotEmpty();
    assertThat(result.getBody()).hasSize(audits.size());
  }

  //View details of a SandboxAlertConfigurationAudit
  @Test
  public void shouldReturn401ResponseForViewSandboxConfigurationAuditWhenUnauthorisedUser() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<SandboxAlertConfigurationAudit> result = sandboxesApiDelegateImpl
        .getAuditDetailsForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404ResponseForViewSandboxConfigurationAuditWhenNotFound()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .getSandboxAlertConfigurationAuditById(any(), any(), any()))
        .thenThrow(
            new AuditNotFoundException("Audit not found"));
    ResponseEntity<SandboxAlertConfigurationAudit> result = sandboxesApiDelegateImpl
        .getAuditDetailsForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn400ResponseForViewSandboxConfigurationAuditWhenNotFound()
      throws Throwable {
    ResponseEntity<SandboxAlertConfigurationAudit> result = sandboxesApiDelegateImpl
        .getAuditDetailsForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldReturn400ResponseForViewSandboxConfigurationAuditWhenAlertConfigNotFound()
      throws Throwable {

    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .getSandboxAlertConfigurationAuditById(any(), any(), any()))
        .thenThrow(
            new SandboxAlertConfigurationNotFoundException("sandboxId", "alertConfigId"));
    ResponseEntity<SandboxAlertConfigurationAudit> result = sandboxesApiDelegateImpl
        .getAuditDetailsForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldReturn200ResponseWithDataForViewSandboxConfigurationAuditWhenFound()
      throws Throwable {
    SandboxAlertConfigurationAudit audit = TestObjects
        .getSandboxConfigurationAudit(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfigurationAudit.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .getSandboxAlertConfigurationAuditById(any(), any(), any()))
        .thenReturn(audit);
    ResponseEntity<SandboxAlertConfigurationAudit> result = sandboxesApiDelegateImpl
        .getAuditDetailsForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
  }

  //View audit history for a SandboxAlertConfiguration
  @Test
  public void shouldReturn401ResponseForSandboxConfigurationAuditHistoryWhenUnauthorisedUser() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<List<SandboxAlertConfigurationAudit>> result = sandboxesApiDelegateImpl
        .getAuditHistoryForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404ResponseForSandboxConfigurationAuditHistoryWhenNotFound()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .getAuditHistoryForSandboxAlertConfiguration(any(), any()))
        .thenThrow(
            new SandboxAlertConfigurationNotFoundException("sandboxId", "alertConfigId"));
    ResponseEntity<List<SandboxAlertConfigurationAudit>> result = sandboxesApiDelegateImpl
        .getAuditHistoryForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn400ResponseForSandboxConfigurationAuditHistoryWhenNotFound()
      throws Throwable {
    ResponseEntity<List<SandboxAlertConfigurationAudit>> result = sandboxesApiDelegateImpl
        .getAuditHistoryForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldReturn200ResponseWithDataForSandboxConfigurationAuditHistoryWhenFound()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .getAuditHistoryForSandboxAlertConfiguration(any(), any()))
        .thenReturn(TestObjects.getSandboxConfigurationAudits());
    ResponseEntity<List<SandboxAlertConfigurationAudit>> result = sandboxesApiDelegateImpl
        .getAuditHistoryForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
  }

  @Test
  public void shouldReturn204ResponseForSandboxConfigurationAuditHistoryWhenNoAuditsFound()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .getAuditHistoryForSandboxAlertConfiguration(any(), any()))
        .thenReturn(Collections.emptyList());
    ResponseEntity<List<SandboxAlertConfigurationAudit>> result = sandboxesApiDelegateImpl
        .getAuditHistoryForSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5));
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  public void shouldReturnResultsWhenLinkedTagsFoundForGetTagsBySandboxAlertConfigurationIds() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    List<String> alertConfigurationIds = new ArrayList<>();
    alertConfigurationIds.add(alertConfigurationId.toString());
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockAlertConfigurationRelationshipService
        .getRelationshipsByAlertConfigurationId(alertConfigurationId.toString(), EntityType.TAG))
        .thenReturn(
            Arrays.asList(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString()));
    ResponseEntity<AlertConfigurationTagsMap> result = sandboxesApiDelegateImpl
        .getBulkTagRequestForSandboxAlertConfigurationIds(sandboxId.toString(),
            alertConfigurationIds);

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(1);
    assertThat(result.getBody().get(alertConfigurationId.toString())).hasSize(2);
  }

  @Test
  public void shouldNotReturnResultsWhenNoLinkedTagsFoundForGetTagsBySandboxAlertConfigurationIds() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    List<String> alertConfigurationIds = new ArrayList<>();
    alertConfigurationIds.add(alertConfigurationId.toString());
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    ResponseEntity<AlertConfigurationTagsMap> result = sandboxesApiDelegateImpl
        .getBulkTagRequestForSandboxAlertConfigurationIds(sandboxId.toString(),
            alertConfigurationIds);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(1);
    assertThat(result.getBody().get(alertConfigurationId.toString())).hasSize(0);
  }

  @Test
  public void shouldReturn400WhenSandboxIdInvalidForGetTagsBySandboxAlertConfigurationIds() {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    List<String> alertConfigurationIds = new ArrayList<>();
    alertConfigurationIds.add(alertConfigurationId.toString());
    ResponseEntity<AlertConfigurationTagsMap> result = sandboxesApiDelegateImpl
        .getBulkTagRequestForSandboxAlertConfigurationIds(sandboxId.toString(),
            alertConfigurationIds);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

  }

  @Test
  public void shouldReturn401ResponseWhenUnauthorisedUserForGetTagsBySandboxAlertConfigurationIds() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<AlertConfigurationTagsMap> result = sandboxesApiDelegateImpl
        .getBulkTagRequestForSandboxAlertConfigurationIds(TestUtils.randomAlphanumeric(5),
            null);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  //Clone Sandbox Alert config

  @Test
  public void shouldReturn401ResponseForCloneSandboxConfigurationWhenUnauthorisedUser() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.ofNullable(null));
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .cloneSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5), mock(CloneSandboxAlertConfigRequest.class));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn400ResponseForCloneSandboxConfigurationWhenSandboxNotFound() {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.empty());
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .cloneSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5), mock(CloneSandboxAlertConfigRequest.class));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldReturn404ResponseForCloneSandboxConfigurationWhenSandboxAlertConfigNotFound()
      throws Throwable {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .cloneSandboxAlertConfiguration(any(), any(), any(), anyString())).thenThrow(
        new SandboxAlertConfigurationNotFoundException(sandboxUUID.toString(),
            alertConfigUUID.toString()));
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .cloneSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5), mock(CloneSandboxAlertConfigRequest.class));

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturn200ResponseForCloneSandboxConfigurationWhenSandboxAlertConfigFound()
      throws Throwable {
    CloneSandboxAlertConfigRequest request = new CloneSandboxAlertConfigRequest();
    request.setName(TestUtils.randomAlphanumeric());
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService
        .cloneSandboxAlertConfiguration(any(), any(), any(), anyString()))
        .thenReturn(mock(SandboxAlertConfiguration.class));
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .cloneSandboxAlertConfiguration(TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5), request);

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void shouldReturnErrorWhenUnauthorisedForGetSandboxAlertConfiguration() {
    when(mockPrincipalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturn404WhenNotFoundForGetSandboxAlertConfiguration() {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.getSandboxAlertConfiguration(any(), any()))
        .thenReturn(Optional.empty());
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  public void shouldReturnBadRequestWhenSandboxNotFoundForGetSandboxAlertConfiguration() {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.empty());
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  public void shouldReturnEntityWhenFoundForGetSandboxAlertConfiguration() {
    when(mockSandboxService.getSandboxById(any())).thenReturn(Optional.of(mock(Sandbox.class)));
    when(mockSandboxAlertConfigurationService.getSandboxAlertConfiguration(any(), any()))
        .thenReturn(Optional.of(mock(SandboxAlertConfiguration.class)));
    ResponseEntity<SandboxAlertConfiguration> result = sandboxesApiDelegateImpl
        .getSandboxAlertConfiguration(TestUtils.randomUUID().toString(),
            TestUtils.randomUUID().toString());

    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isInstanceOf(SandboxAlertConfiguration.class);
  }
}