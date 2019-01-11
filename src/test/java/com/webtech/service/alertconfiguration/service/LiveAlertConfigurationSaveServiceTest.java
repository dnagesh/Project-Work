package com.webtech.service.alertconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.TestObjects;
import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.SandboxAlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.mapper.LiveAlertConfigurationObjectMapper;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration.StatusEnum;
import com.irisium.service.alertconfiguration.model.UpdateStatus;
import com.webtech.service.alertconfiguration.repository.AlertParameterSetRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByAlertConfigUUIDRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationRepository;
import com.webtech.service.alertconfiguration.repository.SandboxAlertConfigurationRepository;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveAlertConfigurationSaveServiceTest {

  private static final String USER = TestUtils.randomAlphanumeric(5);

  @Mock
  private AlertParameterSetRepository mockAlertParameterSetRepository;
  @Mock
  private LiveAlertConfigurationRepository mockLiveAlertConfigurationRepository;
  @Mock
  private LiveAlertConfigurationAuditRepository mockLiveAlertConfigurationAuditRepository;
  @Mock
  private LiveAlertConfigurationObjectMapper mockLiveAlertConfigurationObjectMapper;
  @Mock
  private SandboxAlertConfigurationRepository sandboxAlertConfigurationRepository;
  @Mock
  private LiveAlertConfigurationAuditByAlertConfigUUIDRepository mockAuditByAlertConfigUUIDRepository;
  @Mock
  private LiveAlertConfigurationAuditSaveService mockAuditSaveService;
  @Mock
  private SandboxAlertConfigurationService mockSandboxAlertConfigurationService;

  private LiveAlertConfigurationSaveService service;

  private AlertParameterSetDTO parameterSetDTO;

  @Before
  public void setUp() throws Exception {
    service = new LiveAlertConfigurationSaveService(mockAlertParameterSetRepository,
        mockLiveAlertConfigurationRepository, mockLiveAlertConfigurationObjectMapper,
        sandboxAlertConfigurationRepository, mockAuditSaveService,
        mockSandboxAlertConfigurationService);

    parameterSetDTO = TestObjects.getAlertParameterSetDTO();

    when(mockAlertParameterSetRepository.findById(any()))
        .thenReturn(Optional.ofNullable(parameterSetDTO));

  }


  @Test
  public void shouldCreateLiveConfigFromValidSandboxConfig() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();
    UUID sandboxConfigUUID = TestUtils.randomUUID();
    SandboxAlertConfigurationDTO dto = TestObjects.getSandboxConfigurationDTO(sandboxUUID,
        sandboxConfigUUID,
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant());
    when(sandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.of(dto));
    when(mockAlertParameterSetRepository.findById(any()))
        .thenReturn(Optional.of(TestObjects.getAlertParameterSetDTO()));

    when(mockLiveAlertConfigurationObjectMapper.mapSandboxConfigDTOToLiveConfigDTO(any(), any()))
        .thenReturn(mock(LiveAlertConfigurationDTO.class));
    try {
      InOrder inOrder = Mockito.inOrder(mockAuditSaveService, mockLiveAlertConfigurationRepository,
          mockSandboxAlertConfigurationService);
      Optional<LiveAlertConfiguration> result = service
          .createFromSandboxConfiguration(sandboxUUID.toString(),
              sandboxConfigUUID.toString(), USER);
      assertThat(result).isNotNull();
      inOrder.verify(mockAuditSaveService, times(1)).saveAudits(any());
      inOrder.verify(mockLiveAlertConfigurationRepository, times(1)).save(any());
      inOrder.verify(mockSandboxAlertConfigurationService, times(1))
          .updateLiveUUID(any(), any(), anyString());

    } catch (SandboxAlertConfigurationNotFoundException e) {
      fail("Not expected");
    }
  }

  @Test
  public void shouldUpdateLiveConfigFromValidSandboxConfig() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();
    UUID sandboxConfigUUID = TestUtils.randomUUID();
    SandboxAlertConfigurationDTO dto = TestObjects.getSandboxConfigurationDTO(sandboxUUID,
        sandboxConfigUUID,
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant());
    dto.setLiveConfigUUID(TestUtils.randomUUID());
    when(sandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.of(dto));
    when(mockAlertParameterSetRepository.findById(any()))
        .thenReturn(Optional.of(TestObjects.getAlertParameterSetDTO()));

    when(mockLiveAlertConfigurationObjectMapper.mapSandboxConfigDTOToLiveConfigDTO(any(), any()))
        .thenReturn(mock(LiveAlertConfigurationDTO.class));
    try {
      InOrder inOrder = Mockito.inOrder(mockAuditSaveService, mockLiveAlertConfigurationRepository
      );
      Optional<LiveAlertConfiguration> result = service
          .createFromSandboxConfiguration(sandboxUUID.toString(),
              sandboxConfigUUID.toString(), USER);
      assertThat(result).isNotNull();
      inOrder.verify(mockAuditSaveService, times(1)).saveAudits(any());
      inOrder.verify(mockLiveAlertConfigurationRepository, times(1)).save(any());
      verify(mockSandboxAlertConfigurationService, times(0))
          .updateLiveUUID(any(), any(), anyString());

    } catch (SandboxAlertConfigurationNotFoundException e) {
      fail("Not expected");
    }
  }

  @Test
  public void shouldThrowNotFoundExceptionForInvalidSandboxConfig() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();
    UUID sandboxConfigUUID = TestUtils.randomUUID();
    when(sandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.empty());
    Optional<LiveAlertConfiguration> result = null;
    try {
      result = service.createFromSandboxConfiguration(sandboxUUID.toString(),
          sandboxConfigUUID.toString(), USER);
      fail("Cannot create live config from non-existent sandbox config");

    } catch (SandboxAlertConfigurationNotFoundException e) {
      verify(mockLiveAlertConfigurationRepository, times(0)).save(any());
    }
  }


  @Test(expected = AlertConfigurationNotFoundException.class)
  public void shouldReturnAlertConfigurationNotFoundExceptionWhenAlertConfigurationIdInvalidForUpdateStatus()
      throws AlertConfigurationNotFoundException {
    service.updateStatus(TestUtils.randomUUID().toString(), null, USER);
  }

  @Test
  public void shouldReturnLiveAlertConfigurationWhenAlertConfigurationIdValidForUpdateStatus()
      throws AlertConfigurationNotFoundException {

    LiveAlertConfigurationDTO dto = TestObjects.getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5)

    );
    UpdateStatus status = new UpdateStatus();
    status.setStatus(UpdateStatus.StatusEnum.INACTIVE);
    when(mockLiveAlertConfigurationRepository.findById(dto.getUuid())).thenReturn(Optional.of(dto));
    when(mockLiveAlertConfigurationRepository.save(dto)).thenReturn(dto);
    when(mockLiveAlertConfigurationObjectMapper.liveDTOToModelWithParameterSet(any(), any()))
        .thenReturn(mock(LiveAlertConfiguration.class));
    LiveAlertConfiguration result = service.updateStatus(dto.getUuid().toString(), status, USER);
    assertThat(result).isNotNull();

  }

}
