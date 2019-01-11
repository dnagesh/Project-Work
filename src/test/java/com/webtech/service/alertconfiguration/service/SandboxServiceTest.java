package com.webtech.service.alertconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.TestObjects;
import com.webtech.service.alertconfiguration.dto.SandboxDTO;
import com.webtech.service.alertconfiguration.exception.SandboxNotFoundException;
import com.webtech.service.alertconfiguration.mapper.CreateSandboxRequestMapper;
import com.webtech.service.alertconfiguration.mapper.SandboxDTOMapper;
import com.irisium.service.alertconfiguration.model.CreateSandboxRequest;
import com.irisium.service.alertconfiguration.model.CreateSandboxRequest.CreateFromEnum;
import com.irisium.service.alertconfiguration.model.Sandbox;
import com.irisium.service.alertconfiguration.model.Sandbox.StatusEnum;
import com.irisium.service.alertconfiguration.model.SandboxResetOptions;
import com.irisium.service.alertconfiguration.model.SandboxResetOptions.ResetFromEnum;
import com.webtech.service.alertconfiguration.repository.SandboxRepository;
import java.time.Instant;
import java.util.Arrays;
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
public class SandboxServiceTest {

  private static final String USER = TestUtils.randomAlphanumeric(5);
  @Captor
  ArgumentCaptor<String> uuidArgumentCaptor;
  @Captor
  ArgumentCaptor<String> toUuidArgumentCaptor;
  @Captor
  ArgumentCaptor<String> userArgumentCaptor;
  @Captor
  ArgumentCaptor<Instant> instantArgumentCaptor;
  @Captor
  ArgumentCaptor<List<String>> selectedConfigsArgumentCaptor;
  @Mock
  private SandboxRepository mockRepository;
  private SandboxService sandboxService;
  private SandboxDTO dto;
  @Mock
  private SandboxAlertConfigurationService sandboxAlertConfigurationService;
  @Captor
  private ArgumentCaptor<SandboxDTO> argumentDTO;

  @Before
  public void setup() {
    sandboxService = new SandboxService(mockRepository, new SandboxDTOMapper(),
        new CreateSandboxRequestMapper(), sandboxAlertConfigurationService);
    dto = TestObjects.getSandboxDTO();
  }

  @Test
  public void getAllSandboxesShouldReturnEmptyWhenNoResults() {
    List<Sandbox> sandboxes = sandboxService.getAllSandboxes();
    assertThat(sandboxes).isEmpty();
  }

  @Test
  public void getAllSandboxesShouldReturnDataWhenResultsFound() {
    List<SandboxDTO> dtoList = TestObjects.getSanboxesDTOList();
    when(mockRepository.findAll()).thenReturn(dtoList);
    List<Sandbox> sandboxes = sandboxService.getAllSandboxes();
    assertThat(sandboxes).hasSize(dtoList.size());
  }

  @Test
  public void getSandboxByIdShouldReturnSandbox() {

    when(mockRepository.findById(dto.getUuid())).thenReturn(Optional.of(dto));
    Optional<Sandbox> result = sandboxService.getSandboxById(dto.getUuid().toString());
    assertThat(result.get().getCreatedBy()).isEqualTo(dto.getOwner());
    assertThat(result.get().getName()).isEqualTo(dto.getName());
    assertThat(result.get().getId()).isEqualTo(dto.getUuid().toString());
    assertThat(result.get().getCreatedWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.get().getStatus().toString()).isEqualTo(dto.getStatus());
  }

  public void getSandboxByIdShouldThrowSandboxNotFoundException() {
    Optional<Sandbox> result = sandboxService.getSandboxById(dto.getUuid().toString());
    assertThat(result.isPresent()).isFalse();
    assertThat(result.isPresent()).isFalse();
    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void createSandboxShouldReturnCaseWhenValidInput() throws Throwable {

    CreateSandboxRequest createSandboxRequest = new CreateSandboxRequest();
    createSandboxRequest.setName("Case 1234 SandBox");
    Sandbox result = sandboxService.createSandbox(createSandboxRequest, "User1");
    assertThat(result.getStatus().toString()).isEqualTo(StatusEnum.ACTIVE.toString());
    assertThat(result.getName()).isEqualTo(createSandboxRequest.getName());
    assertThat(result.getCreatedWhen()).isNotNull();
    assertThat(result.getCreatedBy()).isEqualTo("User1");
    assertThat(result.getId()).isNotNull();

  }

  @Test
  public void shouldReturnNewSandboxWhenValidInputForCreateSandboxFromLive() throws Throwable {

    CreateSandboxRequest createSandboxRequest = new CreateSandboxRequest();
    createSandboxRequest.setName(TestUtils.randomAlphanumeric(5));
    createSandboxRequest.setCreateFrom(CreateFromEnum.LIVE);

    Sandbox result = sandboxService.createSandbox(createSandboxRequest, USER);

    verify(mockRepository, times(1)).save(argumentDTO.capture());
    assertThat(argumentDTO.getValue()).isNotNull();
    assertThat(argumentDTO.getValue().getOwner()).isEqualTo(USER);

    verify(sandboxAlertConfigurationService, times(1))
        .createConfigurationsFromLive(uuidArgumentCaptor.capture(), userArgumentCaptor.capture());

    assertThat(uuidArgumentCaptor.getValue()).isNotBlank();
    assertThat(uuidArgumentCaptor.getValue())
        .isEqualTo(argumentDTO.getValue().getUuid().toString());
    assertThat(userArgumentCaptor.getValue()).isNotBlank();
    assertThat(userArgumentCaptor.getValue()).isEqualTo(argumentDTO.getValue().getOwner());

  }

  @Test
  public void shouldReturnNewSandboxWhenValidInputForCreateSandboxFromPointInTime()
      throws Throwable {

    CreateSandboxRequest createSandboxRequest = new CreateSandboxRequest();
    createSandboxRequest.setName(TestUtils.randomAlphanumeric(5));
    createSandboxRequest.setCreateFrom(CreateFromEnum.POINT_IN_TIME);
    createSandboxRequest.setPointInTime(TestUtils.randomInstant());

    Sandbox result = sandboxService.createSandbox(createSandboxRequest, USER);

    verify(mockRepository, times(1)).save(argumentDTO.capture());
    assertThat(argumentDTO.getValue()).isNotNull();
    assertThat(argumentDTO.getValue().getOwner()).isEqualTo(USER);

    verify(sandboxAlertConfigurationService, times(1))
        .createConfigurationsFromPointInTime(instantArgumentCaptor.capture(),
            uuidArgumentCaptor.capture(), userArgumentCaptor.capture());

    assertThat(instantArgumentCaptor.getValue()).isNotNull();
    assertThat(instantArgumentCaptor.getValue()).isEqualTo(createSandboxRequest.getPointInTime());
    assertThat(uuidArgumentCaptor.getValue()).isNotBlank();
    assertThat(uuidArgumentCaptor.getValue())
        .isEqualTo(argumentDTO.getValue().getUuid().toString());
    assertThat(userArgumentCaptor.getValue()).isNotBlank();
    assertThat(userArgumentCaptor.getValue()).isEqualTo(argumentDTO.getValue().getOwner());

  }

  @Test
  public void shouldReturnNewSandboxWhenValidInputForCreateSandboxFromAnotherSandbox()
      throws Throwable {
    UUID fromSandboxUUID = TestUtils.randomUUID();

    CreateSandboxRequest createSandboxRequest = new CreateSandboxRequest();
    createSandboxRequest.setName(TestUtils.randomAlphanumeric(5));
    createSandboxRequest.setCreateFrom(CreateFromEnum.SANDBOX_ID);
    createSandboxRequest.setSandboxId(fromSandboxUUID.toString());

    Sandbox result = sandboxService.createSandbox(createSandboxRequest, USER);

    verify(mockRepository, times(1)).save(argumentDTO.capture());
    assertThat(argumentDTO.getValue()).isNotNull();
    assertThat(argumentDTO.getValue().getOwner()).isEqualTo(USER);

    verify(sandboxAlertConfigurationService, times(1))
        .createConfigurationsFromSandboxConfigs(toUuidArgumentCaptor.capture(),
            uuidArgumentCaptor.capture(), userArgumentCaptor.capture());

    assertThat(toUuidArgumentCaptor.getValue()).isNotNull();
    assertThat(toUuidArgumentCaptor.getValue())
        .isEqualTo(argumentDTO.getValue().getUuid().toString());
    assertThat(uuidArgumentCaptor.getValue()).isNotBlank();
    assertThat(uuidArgumentCaptor.getValue()).isEqualTo(fromSandboxUUID.toString());
    assertThat(userArgumentCaptor.getValue()).isNotBlank();
    assertThat(userArgumentCaptor.getValue()).isEqualTo(argumentDTO.getValue().getOwner());

  }

  @Test
  public void deleteSandboxByIdShouldReturnVoidWhenValidId() throws SandboxNotFoundException {
    when(mockRepository.findById(dto.getUuid())).thenReturn(Optional.of(dto));
    sandboxService.deleteSandboxById(dto.getUuid().toString());
    verify(mockRepository, times(1)).save(argumentDTO.capture());
    assertThat(argumentDTO.getValue().getStatus()).isEqualTo(StatusEnum.DELETED.toString());
  }

  @Test(expected = SandboxNotFoundException.class)
  public void deleteSandboxByIdShouldThrowSandboxNotFoundException()
      throws SandboxNotFoundException {
    sandboxService.deleteSandboxById(dto.getUuid().toString());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionIfNoSelectedLiveConfigsProvidedForCreateSandbox()
      throws Throwable {
    CreateSandboxRequest createSandboxRequest = new CreateSandboxRequest();
    createSandboxRequest.setName(TestUtils.randomAlphanumeric(5));
    createSandboxRequest.setCreateFrom(CreateFromEnum.SELECTED_LIVE_CONFIGS);

    Sandbox result = sandboxService.createSandbox(createSandboxRequest, USER);
  }

  @Test
  public void shouldCreateSandboxIfSelectedLiveConfigsProvidedForCreateSandbox() throws Throwable {
    CreateSandboxRequest createSandboxRequest = new CreateSandboxRequest();
    createSandboxRequest.setName(TestUtils.randomAlphanumeric(5));
    createSandboxRequest.setCreateFrom(CreateFromEnum.SELECTED_LIVE_CONFIGS);
    createSandboxRequest.setSelectedLiveConfigs(Arrays.asList(TestUtils.randomUUID().toString()));

    Sandbox result = sandboxService.createSandbox(createSandboxRequest, USER);

    verify(mockRepository, times(1)).save(any());
    verify(sandboxAlertConfigurationService, times(1))
        .createConfigurationsFromSelectedLiveConfigs(uuidArgumentCaptor.capture(),
            selectedConfigsArgumentCaptor.capture(), userArgumentCaptor.capture());
    assertThat(uuidArgumentCaptor.getValue()).isNotNull();
    assertThat(uuidArgumentCaptor.getValue()).isNotEqualTo(createSandboxRequest.getSandboxId());
    assertThat(selectedConfigsArgumentCaptor.getValue()).isNotNull();
    assertThat(selectedConfigsArgumentCaptor.getValue())
        .isEqualTo(createSandboxRequest.getSelectedLiveConfigs());

    assertThat(userArgumentCaptor.getValue()).isNotNull();
    assertThat(userArgumentCaptor.getValue()).isEqualTo(USER);

  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionIfNoPointInTimeProvidedForCreateSandbox() throws Throwable {
    CreateSandboxRequest createSandboxRequest = new CreateSandboxRequest();
    createSandboxRequest.setName(TestUtils.randomAlphanumeric(5));
    createSandboxRequest.setCreateFrom(CreateFromEnum.POINT_IN_TIME);

    Sandbox result = sandboxService.createSandbox(createSandboxRequest, USER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldThrowExceptionIfNoSandboxIdProvidedForCreateSandbox() throws Throwable {
    CreateSandboxRequest createSandboxRequest = new CreateSandboxRequest();
    createSandboxRequest.setName(TestUtils.randomAlphanumeric(5));
    createSandboxRequest.setCreateFrom(CreateFromEnum.SANDBOX_ID);

    Sandbox result = sandboxService.createSandbox(createSandboxRequest, USER);
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldCreateSandboxWhenSelectedConfigsProvidedForCreateSandbox() throws Throwable {
    CreateSandboxRequest createSandboxRequest = new CreateSandboxRequest();
    createSandboxRequest.setName(TestUtils.randomAlphanumeric(5));
    createSandboxRequest.setCreateFrom(CreateFromEnum.SELECTED_LIVE_CONFIGS);

    Sandbox result = sandboxService.createSandbox(createSandboxRequest, USER);
  }

  @Test(expected = SandboxNotFoundException.class)
  public void shouldThrowNotFoundExceptionIfNoSandboxFoundForResetSandbox() throws Throwable {
    when(mockRepository.findById(any())).thenReturn(Optional.empty());
    Sandbox result = sandboxService
        .resetSandbox(TestUtils.randomUUID().toString(), new SandboxResetOptions(), USER);

    verifyZeroInteractions(sandboxAlertConfigurationService);
  }

  @Test
  public void shouldResetSandboxIfSandboxFoundForResetSandboxFromEmpty() throws Throwable {
    SandboxResetOptions resetOptions = new SandboxResetOptions();
    resetOptions.setResetFrom(ResetFromEnum.EMPTY);
    when(mockRepository.findById(any())).thenReturn(Optional.of(dto));
    Sandbox result = sandboxService
        .resetSandbox(TestUtils.randomUUID().toString(), resetOptions, USER);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getId()).isEqualTo(dto.getUuid().toString());
    verify(sandboxAlertConfigurationService, times(1))
        .archiveAllSandboxConfigurations(dto.getUuid(), USER);
    verify(sandboxAlertConfigurationService, times(0)).createConfigurationsFromLive(any(), any());
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromSelectedLiveConfigs(any(), anyList(), any());
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromPointInTime(any(), any(), any());
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromSandboxConfigs(anyString(), anyString(), any());

  }

  @Test
  public void shouldResetSandboxIfSandboxFoundForResetSandboxFromLive() throws Throwable {
    SandboxResetOptions resetOptions = new SandboxResetOptions();
    resetOptions.setResetFrom(ResetFromEnum.LIVE);
    when(mockRepository.findById(any())).thenReturn(Optional.of(dto));
    Sandbox result = sandboxService
        .resetSandbox(TestUtils.randomUUID().toString(), resetOptions, USER);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getId()).isEqualTo(dto.getUuid().toString());
    verify(sandboxAlertConfigurationService, times(1))
        .archiveAllSandboxConfigurations(dto.getUuid(), USER);
    verify(sandboxAlertConfigurationService, times(1))
        .createConfigurationsFromLive(dto.getUuid().toString(), USER);
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromSelectedLiveConfigs(any(), anyList(), any());
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromPointInTime(any(), any(), any());
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromSandboxConfigs(anyString(), anyString(), any());

  }

  @Test
  public void shouldResetSandboxIfSandboxFoundForResetSandboxFromPointInTime() throws Throwable {
    SandboxResetOptions resetOptions = new SandboxResetOptions();
    resetOptions.setResetFrom(ResetFromEnum.POINT_IN_TIME);
    Instant pointInTime = Instant.now();
    resetOptions.setPointInTime(pointInTime);
    when(mockRepository.findById(any())).thenReturn(Optional.of(dto));
    Sandbox result = sandboxService
        .resetSandbox(TestUtils.randomUUID().toString(), resetOptions, USER);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getId()).isEqualTo(dto.getUuid().toString());
    verify(sandboxAlertConfigurationService, times(1))
        .archiveAllSandboxConfigurations(dto.getUuid(), USER);
    verify(sandboxAlertConfigurationService, times(0)).createConfigurationsFromLive(any(), any());
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromSelectedLiveConfigs(any(), anyList(), any());
    verify(sandboxAlertConfigurationService, times(1))
        .createConfigurationsFromPointInTime(pointInTime, dto.getUuid().toString(), USER);
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromSandboxConfigs(anyString(), anyString(), any());

  }

  @Test
  public void shouldResetSandboxIfSandboxFoundForResetSandboxFromAnotherSandbox() throws Throwable {
    SandboxResetOptions resetOptions = new SandboxResetOptions();
    resetOptions.setResetFrom(ResetFromEnum.SANDBOX_ID);
    resetOptions.setSandboxId(TestUtils.randomUUID().toString());
    when(mockRepository.findById(any())).thenReturn(Optional.of(dto));
    Sandbox result = sandboxService
        .resetSandbox(TestUtils.randomUUID().toString(), resetOptions, USER);
    assertThat(result).isNotNull();
    assertThat(result.getId()).isNotNull();
    assertThat(result.getId()).isEqualTo(dto.getUuid().toString());
    verify(sandboxAlertConfigurationService, times(1))
        .archiveAllSandboxConfigurations(dto.getUuid(), USER);
    verify(sandboxAlertConfigurationService, times(0)).createConfigurationsFromLive(any(), any());
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromSelectedLiveConfigs(any(), anyList(), any());
    verify(sandboxAlertConfigurationService, times(0))
        .createConfigurationsFromPointInTime(any(), any(), any());
    verify(sandboxAlertConfigurationService, times(1))
        .createConfigurationsFromSandboxConfigs(dto.getUuid().toString(),
            resetOptions.getSandboxId(), USER);

  }


}
