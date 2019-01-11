package com.webtech.service.alertconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.irisium.service.alertDefinition.model.GuiDeploymentAlertType;
import com.webtech.service.alertconfiguration.TestObjects;
import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTOPrimaryKey;
import com.webtech.service.alertconfiguration.dto.SandboxRunAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.AuditNotFoundException;
import com.webtech.service.alertconfiguration.exception.IllegalParameterException;
import com.webtech.service.alertconfiguration.exception.SandboxAlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.mapper.AlertFilterMapper;
import com.webtech.service.alertconfiguration.mapper.SandboxAlertConfigurationObjectMapper;
import com.webtech.service.alertconfiguration.mapper.SandboxRunAlertConfigurationDTOMapper;
import com.irisium.service.alertconfiguration.model.CloneSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.CreateUpdateSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfiguration;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfigurationAudit;
import com.irisium.service.alertconfiguration.model.UpdateStatus;
import com.webtech.service.alertconfiguration.repository.AlertConfigurationRepositories;
import com.webtech.service.alertconfiguration.repository.AlertParameterSetRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByAlertConfigUUIDRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationRepository;
import com.webtech.service.alertconfiguration.repository.SandboxAlertConfigurationAuditByMonthRepository;
import com.webtech.service.alertconfiguration.repository.SandboxAlertConfigurationAuditRepository;
import com.webtech.service.alertconfiguration.repository.SandboxAlertConfigurationRepository;
import com.webtech.service.alerttype.service.AlertTypeService;
import com.webtech.service.common.AppPropertiesProvider;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SandboxAlertConfigurationServiceTest {

  private static final UUID sandboxUUID = TestUtils.randomUUID();
  private static final String USER = TestUtils.randomAlphanumeric(5);
  @Mock
  AlertParameterSetRepository mockAlertParameterSetRepository;
  @Captor
  ArgumentCaptor<List<LocalDate>> listArgumentCaptor;
  @Captor
  ArgumentCaptor<Integer> integerArgumentCaptor;
  @Captor
  ArgumentCaptor<UUID> sandboxArgumentCaptor;
  @Captor
  ArgumentCaptor<SandboxAlertConfigurationDTOPrimaryKey> primaryKeyArgumentCaptor;
  @Captor
  ArgumentCaptor<List<SandboxAlertConfigurationAuditByMonthDTO>> auditDtoListCaptor;
  @Mock
  private SandboxAlertConfigurationRepository mockSandboxAlertConfigurationRepository;
  @Mock
  private SandboxAlertConfigurationAuditRepository mockAuditRepository;
  @Mock
  private LiveAlertConfigurationRepository mockLiveAlertConfigurationRepository;
  @Mock
  private LiveAlertConfigurationAuditByAlertConfigUUIDRepository mockLiveAlertConfigurationAuditByAlertConfigUUIDRepository;
  private SandboxAlertConfigurationObjectMapper sandboxAlertConfigurationObjectMapper;
  private SandboxRunAlertConfigurationDTOMapper sandboxRunAlertConfigurationDTOMapper;
  private SandboxAlertConfigurationService service;
  @Captor
  private ArgumentCaptor<List<SandboxAlertConfigurationAuditDTO>> auditDTOListArgumet;
  @Captor
  private ArgumentCaptor<List<SandboxAlertConfigurationDTO>> dtoListArgument;
  @Captor
  private ArgumentCaptor<SandboxAlertConfigurationDTO> dtoArgumentCaptor;
  @Captor
  private ArgumentCaptor<SandboxAlertConfigurationAuditDTO> auditDTOArgumentCaptor;
  @Mock
  private SandboxAlertConfigurationAuditByMonthRepository mockAuditByMonthRepository;
  @Mock
  private AppPropertiesProvider mockAppPropertiesProvider;
  @Mock
  private AlertTypeService mockAlertTypeService;

  private AlertConfigurationRepositories alertConfigurationRepositories;

  @Before
  public void setUp() {
    alertConfigurationRepositories = new AlertConfigurationRepositories(
        mockAlertParameterSetRepository,
        mockSandboxAlertConfigurationRepository,
        mockAuditRepository,
        mockLiveAlertConfigurationRepository,
        mockLiveAlertConfigurationAuditByAlertConfigUUIDRepository,
        mockAuditByMonthRepository);
    sandboxRunAlertConfigurationDTOMapper = new SandboxRunAlertConfigurationDTOMapper();
    sandboxAlertConfigurationObjectMapper = new SandboxAlertConfigurationObjectMapper(
        new AlertFilterMapper());
    service = new SandboxAlertConfigurationService(
        alertConfigurationRepositories,
        sandboxAlertConfigurationObjectMapper,
        sandboxRunAlertConfigurationDTOMapper,
        mockAppPropertiesProvider, mockAlertTypeService);

    when(mockAppPropertiesProvider.getDefaultAuditRowCountLimit()).thenReturn(10);
    when(mockAppPropertiesProvider.getMinAuditRowCountLimit()).thenReturn(1);
    when(mockAppPropertiesProvider.getMaxAuditRowCountLimit()).thenReturn(100);

  }

  @Test
  public void shouldReturnEmptyListWhenNoLiveDataExists() {

    List<SandboxAlertConfigurationDTO> result = service
        .createConfigurationsFromLive(sandboxUUID.toString(), USER);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldCreateConfigurationsFromLiveWhenDataExists() {
    List<LiveAlertConfigurationDTO> liveDTOList = TestObjects.getLiveConfigurationDTOList();
    when(mockLiveAlertConfigurationRepository.findAll()).thenReturn(liveDTOList);

    List<SandboxAlertConfigurationDTO> result = service
        .createConfigurationsFromLive(sandboxUUID.toString(), USER);

    InOrder inOrder = Mockito.inOrder(mockAuditRepository, mockSandboxAlertConfigurationRepository);

    inOrder.verify(mockAuditRepository, times(1)).saveAll(auditDTOListArgumet.capture());
    assertThat(auditDTOListArgumet.getValue()).isNotNull();
    assertThat(auditDTOListArgumet.getValue()).hasSize(liveDTOList.size());

    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1))
        .saveAll(dtoListArgument.capture());
    assertThat(dtoListArgument.getValue()).isNotNull();
    assertThat(dtoListArgument.getValue()).hasSize(liveDTOList.size());
  }

  @Test
  public void shouldReturnEmptyListWhenNoLiveDataExistsForPointInTime() {

    List<SandboxAlertConfigurationDTO> result = service
        .createConfigurationsFromPointInTime(TestUtils.randomInstant(), sandboxUUID.toString(),
            USER);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldCreateConfigurationsFromLiveWhenDataExistsForPointInTime() {
    Instant pointInTime = TestUtils.randomInstant();
    List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> liveAuditDTOList = TestObjects
        .getLiveConfigurationAuditByAlertConfigUUIDDTOList();
    when(mockLiveAlertConfigurationAuditByAlertConfigUUIDRepository
        .findLiveAuditsForPointInTime(any(), any())).thenReturn(liveAuditDTOList);

    List<SandboxAlertConfigurationDTO> result = service
        .createConfigurationsFromPointInTime(pointInTime, sandboxUUID.toString(), USER);

    InOrder inOrder = Mockito.inOrder(mockAuditRepository, mockSandboxAlertConfigurationRepository);

    inOrder.verify(mockAuditRepository, times(1)).saveAll(auditDTOListArgumet.capture());
    assertThat(auditDTOListArgumet.getValue()).isNotNull();
    assertThat(auditDTOListArgumet.getValue()).hasSize(liveAuditDTOList.size());

    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1))
        .saveAll(dtoListArgument.capture());
    assertThat(dtoListArgument.getValue()).isNotNull();
    assertThat(dtoListArgument.getValue()).hasSize(liveAuditDTOList.size());
  }

  @Test
  public void shouldReturnEmptyListWhenNoConfigsExistsForInputSandbox() {

    List<SandboxAlertConfigurationDTO> result = service
        .createConfigurationsFromSandboxConfigs(sandboxUUID.toString(),
            TestUtils.randomUUID().toString(),
            USER);

    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldCreateConfigurationsWhenConfigsExistsForInputSandbox() {
    UUID fromSandboxUUID = TestUtils.randomUUID();
    List<SandboxAlertConfigurationDTO> sandboxAlertConfigurationDTOS = TestObjects
        .getSandboxConfigurationDTOList();

    when(mockSandboxAlertConfigurationRepository
        .findAllByPrimaryKeySandboxUUID(fromSandboxUUID)).thenReturn(sandboxAlertConfigurationDTOS);

    List<SandboxAlertConfigurationDTO> result = service
        .createConfigurationsFromSandboxConfigs(sandboxUUID.toString(), fromSandboxUUID.toString(),
            USER);

    InOrder inOrder = Mockito.inOrder(mockAuditRepository, mockSandboxAlertConfigurationRepository);

    inOrder.verify(mockAuditRepository, times(1)).saveAll(auditDTOListArgumet.capture());
    assertThat(auditDTOListArgumet.getValue()).isNotNull();
    assertThat(auditDTOListArgumet.getValue()).hasSize(sandboxAlertConfigurationDTOS.size());

    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1))
        .saveAll(dtoListArgument.capture());
    assertThat(dtoListArgument.getValue()).isNotNull();
    assertThat(dtoListArgument.getValue()).hasSize(sandboxAlertConfigurationDTOS.size());
  }

  @Test
  public void shouldAddSandboxConfigurationForValidInput() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();
    AlertParameterSetDTO parameterSetDTO = mock(AlertParameterSetDTO.class);
    SandboxAlertConfigurationDTO dto = mock(SandboxAlertConfigurationDTO.class);
    SandboxAlertConfigurationAuditDTO auditDTO = mock(SandboxAlertConfigurationAuditDTO.class);

    when(mockAlertParameterSetRepository.save(any())).thenReturn(parameterSetDTO);
    when(mockAuditRepository.save(any())).thenReturn(auditDTO);
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        sandboxUUID, TestUtils.randomUUID());
    when(mockSandboxAlertConfigurationRepository.save(any())).thenReturn(dto);
    when(dto.getPrimaryKey()).thenReturn(primaryKey);

    InOrder inOrder = Mockito.inOrder(mockAlertParameterSetRepository, mockAuditRepository,
        mockSandboxAlertConfigurationRepository);

    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);

    SandboxAlertConfiguration result = service
        .addAlertConfigToSandbox(sandboxUUID.toString(), request, USER);

    inOrder.verify(mockAlertParameterSetRepository, times(1)).save(any());
    inOrder.verify(mockAuditRepository, times(1)).save(any());
    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1)).save(any());

  }

  @Test
  public void shouldAddSandboxConfigurationForValidInputWithoutAnyParams() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();
    AlertParameterSetDTO parameterSetDTO = mock(AlertParameterSetDTO.class);
    SandboxAlertConfigurationDTO dto = mock(SandboxAlertConfigurationDTO.class);
    SandboxAlertConfigurationAuditDTO auditDTO = mock(SandboxAlertConfigurationAuditDTO.class);

    when(mockAlertParameterSetRepository.save(any())).thenReturn(parameterSetDTO);
    when(mockAuditRepository.save(any())).thenReturn(auditDTO);
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        sandboxUUID, TestUtils.randomUUID());
    when(mockSandboxAlertConfigurationRepository.save(any())).thenReturn(dto);
    when(dto.getPrimaryKey()).thenReturn(primaryKey);

    InOrder inOrder = Mockito.inOrder(mockAlertParameterSetRepository, mockAuditRepository,
        mockSandboxAlertConfigurationRepository);

    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    request.setAlertParameters(null);
    request.setLogicOverrideSet(null);
    deploymentAlertType.getLogicOverrides().clear();
    deploymentAlertType.getParameters().clear();
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);

    SandboxAlertConfiguration result = service
        .addAlertConfigToSandbox(sandboxUUID.toString(), request, USER);

    inOrder.verify(mockAlertParameterSetRepository, times(1)).save(any());
    inOrder.verify(mockAuditRepository, times(1)).save(any());
    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1)).save(any());

  }

  @Test(expected = IllegalParameterException.class)
  public void shouldThrowExceptionForInvalidInputAlertConfigParameters() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();

    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    deploymentAlertType.setParameters(Collections.emptyList());
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);

    SandboxAlertConfiguration result = service
        .addAlertConfigToSandbox(sandboxUUID.toString(), request, USER);

  }

  @Test(expected = IllegalParameterException.class)
  public void shouldThrowExceptionForInvalidRequiredInputAlertConfigParameters() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();

    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);

    Map<String, String> params = new HashMap<>();
    params.put("key2", "value2");
    request.setAlertParameters(params);

    SandboxAlertConfiguration result = service
        .addAlertConfigToSandbox(sandboxUUID.toString(), request, USER);

  }

  @Test
  public void shouldCreateConfigForValidInputAlertConfigParameters() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();
    AlertParameterSetDTO parameterSetDTO = mock(AlertParameterSetDTO.class);
    SandboxAlertConfigurationDTO dto = mock(SandboxAlertConfigurationDTO.class);
    SandboxAlertConfigurationAuditDTO auditDTO = mock(SandboxAlertConfigurationAuditDTO.class);

    when(mockAlertParameterSetRepository.save(any())).thenReturn(parameterSetDTO);
    when(mockAuditRepository.save(any())).thenReturn(auditDTO);
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        sandboxUUID, TestUtils.randomUUID());
    when(mockSandboxAlertConfigurationRepository.save(any())).thenReturn(dto);
    when(dto.getPrimaryKey()).thenReturn(primaryKey);
    InOrder inOrder = Mockito.inOrder(mockAlertParameterSetRepository, mockAuditRepository,
        mockSandboxAlertConfigurationRepository);

    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();
    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    deploymentAlertType.setParameters(Collections.emptyList());
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);
    request.setAlertParameters(null);

    SandboxAlertConfiguration result = service
        .addAlertConfigToSandbox(sandboxUUID.toString(), request, USER);

    inOrder.verify(mockAlertParameterSetRepository, times(1)).save(any());
    inOrder.verify(mockAuditRepository, times(1)).save(any());
    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1)).save(any());

  }

  @Test(expected = IllegalParameterException.class)
  public void shouldThrowExceptionForInvalidInputAlertConfigLogicOverrides() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();
    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    deploymentAlertType.setLogicOverrides(Collections.emptyList());
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);

    SandboxAlertConfiguration result = service
        .addAlertConfigToSandbox(sandboxUUID.toString(), request, USER);

  }

  @Test(expected = IllegalParameterException.class)
  public void shouldThrowExceptionForInvalidRequiredInputAlertConfigLogicOverrides()
      throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();
    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);

    request.setLogicOverrideSet(Collections.EMPTY_MAP);
    SandboxAlertConfiguration result = service
        .addAlertConfigToSandbox(sandboxUUID.toString(), request, USER);

  }


  @Test
  public void shouldReturnEmptyListWhenNonExistingIdGivenForGetAllSandboxAlertConfigurationBySandboxId() {
    String sandboxId = TestUtils.randomUUID().toString();
    Set<SandboxRunAlertConfigurationDTO> result = service
        .getAllSandboxAlertConfigurationBySandboxId(sandboxId);
    assertThat(result).isNotNull();
    assertThat(result).isEmpty();
  }

  @Test
  public void shouldReturnListWhenValidSandboxIdGivenForgetAllSandboxAlertConfigurationBySandboxId() {
    UUID sandboxUUID = TestUtils.randomUUID();
    List<SandboxAlertConfigurationDTO> dtos = TestObjects.getSandboxConfigurationDTOList();
    when(mockSandboxAlertConfigurationRepository.findAllByPrimaryKeySandboxUUID(sandboxUUID))
        .thenReturn(dtos);
    Set<SandboxRunAlertConfigurationDTO> result = service
        .getAllSandboxAlertConfigurationBySandboxId(sandboxUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result).hasSize(dtos.size());
    SandboxRunAlertConfigurationDTO alertConfigDTO = result.iterator().next();
    List<SandboxAlertConfigurationDTO> dto = dtos.stream().filter(
        e -> e.getPrimaryKey().getAlertConfigurationUUID().toString()
            .equals(alertConfigDTO.getSandboxAlertConfigurationUUID().toString())).collect(
        Collectors.toList());
    assertThat(alertConfigDTO.getAppHash()).isEqualTo(dto.get(0).getApsHash());
    assertThat(alertConfigDTO.getName()).isEqualTo(dto.get(0).getName());
    assertThat(alertConfigDTO.getSandboxAlertConfigurationUUID().toString())
        .isEqualTo(dto.get(0).getPrimaryKey().getAlertConfigurationUUID().toString());

  }

  @Test
  public void shouldReturnAuditForValidInputWithNoParameters() {
    UUID sandboxUUID = TestUtils.randomUUID();
    UUID alertConfigUUID = TestUtils.randomUUID();

    SandboxAlertConfigurationAuditDTO auditDTO = TestObjects
        .getSandboxConfigurationAuditDTO(sandboxUUID,
            alertConfigUUID,
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );

    when(mockAuditRepository
        .findFirstByPrimaryKeySandboxUUIDAndPrimaryKeyAlertConfigurationUUIDOrderByPrimaryKeyAuditUUIDAsc(
            any(), any())).thenReturn(auditDTO);

    Optional<SandboxAlertConfigurationAudit> model = service
        .getOriginalSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString());

    assertThat(model).isNotNull();
    assertThat(model.get()).isNotNull();
    assertThat(model.get().getAlertAggregationFields()).isNullOrEmpty();
    assertThat(model.get().getAlertFilters()).isNullOrEmpty();
    assertThat(model.get().getAlertParameters()).isNullOrEmpty();

  }

  @Test
  public void shouldReturnAuditForValidInputWithParameters() throws Throwable {
    UUID sandboxUUID = TestUtils.randomUUID();
    UUID alertConfigUUID = TestUtils.randomUUID();

    SandboxAlertConfigurationAuditDTO auditDTO = TestObjects
        .getSandboxConfigurationAuditDTO(sandboxUUID,
            alertConfigUUID,
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );

    when(mockAuditRepository
        .findFirstByPrimaryKeySandboxUUIDAndPrimaryKeyAlertConfigurationUUIDOrderByPrimaryKeyAuditUUIDAsc(
            any(), any())).thenReturn(auditDTO);

    when(mockAlertParameterSetRepository.findById(auditDTO.getApsHash())).thenReturn(
        Optional.ofNullable(TestObjects.getAlertParameterSetDTO()));
    Optional<SandboxAlertConfigurationAudit> model = service
        .getOriginalSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString());

    assertThat(model).isNotNull();
    assertThat(model.get()).isNotNull();
    assertThat(model.get().getAlertAggregationFields()).isNotNull();
    assertThat(model.get().getAlertFilters()).isNotNull();
    assertThat(model.get().getAlertParameters()).isNotNull();

  }

  @Test
  public void shouldReturnSandboxAlertConfigurationWhenExists() {
    UUID alertConfigUUID = TestUtils.randomUUID();
    SandboxAlertConfigurationDTO dto = TestObjects
        .getSandboxConfigurationDTO(sandboxUUID,
            alertConfigUUID,
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    when(mockSandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.of(dto));
    Optional<SandboxAlertConfiguration> result = service
        .getSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.isPresent()).isTrue();
    verify(mockAlertParameterSetRepository, times(1)).findById(any());
  }

  @Test
  public void shouldReturnEmptyWhenSandboxAlertConfigurationDTODoesNotExist() {
    UUID alertConfigUUID = TestUtils.randomUUID();
    when(mockSandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.empty());

    Optional<SandboxAlertConfiguration> result = service
        .getSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.isPresent()).isFalse();

    verify(mockAlertParameterSetRepository, times(0)).findById(any());

  }

  @Test
  public void shouldReturnConfigListWhenConfigsExistForSandbox() {
    List<SandboxAlertConfigurationDTO> dtoList = TestObjects.getSandboxConfigurationDTOList();
    when(mockSandboxAlertConfigurationRepository.findAllByPrimaryKeySandboxUUID(any()))
        .thenReturn(dtoList);
    List<SandboxAlertConfiguration> result = service
        .getSandboxAlertConfigurations(sandboxUUID.toString());

    assertThat(result).isNotNull();
    assertThat(result).hasSize(dtoList.size());
  }

  @Test
  public void shouldUpdateConfigWhenFound() throws Throwable {
    UUID alertConfigUUID = TestUtils.randomUUID();
    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    SandboxAlertConfigurationDTO dto = TestObjects
        .getSandboxConfigurationDTO(sandboxUUID,
            alertConfigUUID,
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    dto.setLiveConfigUUID(TestUtils.randomUUID());
    when(mockSandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.of(dto));
    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);

    InOrder inOrder = Mockito
        .inOrder(mockSandboxAlertConfigurationRepository, mockAlertParameterSetRepository,
            mockAuditRepository, mockSandboxAlertConfigurationRepository);
    SandboxAlertConfiguration result = service
        .createUpdateSandboxAlertConfiguration(sandboxUUID.toString(),
            alertConfigUUID.toString(), request, USER);

    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1)).findById(any());
    inOrder.verify(mockAlertParameterSetRepository, times(1)).save(any());
    inOrder.verify(mockAuditRepository, times(1)).save(auditDTOArgumentCaptor.capture());
    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1))
        .save(dtoArgumentCaptor.capture());

    assertThat(auditDTOArgumentCaptor.getValue()).isNotNull();
    assertThat(auditDTOArgumentCaptor.getValue().getLiveConfigUUID()).isNotNull();
    assertThat(auditDTOArgumentCaptor.getValue().getLiveConfigUUID())
        .isEqualTo(dto.getLiveConfigUUID());
    assertThat(dtoArgumentCaptor.getValue().getLiveConfigUUID()).isNotNull();
    assertThat(dtoArgumentCaptor.getValue().getLiveConfigUUID()).isEqualTo(dto.getLiveConfigUUID());

  }

  @Test
  public void shouldUpdateConfigWhenFoundWithNoLiveConfigUUID() throws Throwable {
    UUID alertConfigUUID = TestUtils.randomUUID();
    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    SandboxAlertConfigurationDTO dto = TestObjects
        .getSandboxConfigurationDTO(sandboxUUID,
            alertConfigUUID,
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()

        );

    when(mockSandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.of(dto));

    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);

    InOrder inOrder = Mockito
        .inOrder(mockSandboxAlertConfigurationRepository, mockAlertParameterSetRepository,
            mockAuditRepository, mockSandboxAlertConfigurationRepository);
    SandboxAlertConfiguration result = service
        .createUpdateSandboxAlertConfiguration(sandboxUUID.toString(),
            alertConfigUUID.toString(), request, USER);

    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1)).findById(any());
    inOrder.verify(mockAlertParameterSetRepository, times(1)).save(any());
    inOrder.verify(mockAuditRepository, times(1)).save(auditDTOArgumentCaptor.capture());
    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1))
        .save(dtoArgumentCaptor.capture());

    assertThat(auditDTOArgumentCaptor.getValue()).isNotNull();
    assertThat(auditDTOArgumentCaptor.getValue().getLiveConfigUUID()).isNull();
    assertThat(dtoArgumentCaptor.getValue().getLiveConfigUUID()).isNull();
  }

  @Test
  public void shouldCreateNewConfigWhenNotFound() throws Throwable {

    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    GuiDeploymentAlertType deploymentAlertType = TestObjects.getDeploymentAlertType(request);
    when(mockAlertTypeService.getDeploymentAlertType(any())).thenReturn(deploymentAlertType);

    InOrder inOrder = Mockito.inOrder(mockAlertParameterSetRepository, mockAuditRepository,
        mockSandboxAlertConfigurationRepository);
    SandboxAlertConfiguration result = service
        .createUpdateSandboxAlertConfiguration(sandboxUUID.toString(),
            null, request, USER);

    inOrder.verify(mockAlertParameterSetRepository, times(1)).save(any());
    inOrder.verify(mockAuditRepository, times(1)).save(auditDTOArgumentCaptor.capture());
    inOrder.verify(mockSandboxAlertConfigurationRepository, times(1))
        .save(dtoArgumentCaptor.capture());

    assertThat(auditDTOArgumentCaptor.getValue()).isNotNull();
    assertThat(auditDTOArgumentCaptor.getValue().getLiveConfigUUID()).isNull();
    assertThat(dtoArgumentCaptor.getValue().getLiveConfigUUID()).isNull();
  }

  @Test(expected = SandboxAlertConfigurationNotFoundException.class)
  public void shouldCreateNewConfigWhenNotFoundWithAlertConfigUUID() throws Throwable {

    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();
    when(mockSandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.empty());
    InOrder inOrder = Mockito.inOrder(mockAlertParameterSetRepository, mockAuditRepository,
        mockSandboxAlertConfigurationRepository);
    SandboxAlertConfiguration result = service
        .createUpdateSandboxAlertConfiguration(sandboxUUID.toString(),
            TestUtils.randomUUID().toString(), request, USER);
  }

  @Test
  public void shouldReturnSilentlyIfNullInput() {
    service.updateLiveUUID(null, null, USER);
    verify(mockAuditRepository, times(0)).save(any());
    verify(mockSandboxAlertConfigurationRepository, times(0)).save(any());

  }

  @Test
  public void shouldUpdateLiveUUIDIfValidInput() {
    SandboxAlertConfigurationDTO dto = TestObjects
        .getSandboxConfigurationDTO(sandboxUUID,
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    UUID liveUUID = TestUtils.randomUUID();
    service.updateLiveUUID(dto, liveUUID, USER);
    verify(mockAuditRepository, times(1)).save(auditDTOArgumentCaptor.capture());
    verify(mockSandboxAlertConfigurationRepository, times(1)).save(dtoArgumentCaptor.capture());

    assertThat(auditDTOArgumentCaptor.getValue()).isNotNull();
    assertThat(auditDTOArgumentCaptor.getValue().getLiveConfigUUID()).isNotNull();
    assertThat(auditDTOArgumentCaptor.getValue().getLiveConfigUUID()).isEqualTo(liveUUID);
    assertThat(dtoArgumentCaptor.getValue().getLiveConfigUUID()).isEqualTo(liveUUID);

  }

  @Test
  public void shouldCreateSandboxConfigsIfSelectedLiveAlertsPresent() throws Throwable {
    LiveAlertConfigurationDTO liveDTO = TestObjects.getLiveConfigurationDTOList().get(0);
    List<String> selectedUUIDList = Arrays.asList(liveDTO.getUuid().toString());
    when(mockLiveAlertConfigurationRepository.findById(liveDTO.getUuid()))
        .thenReturn(Optional.ofNullable(liveDTO));

    service.createConfigurationsFromSelectedLiveConfigs(TestUtils.randomUUID().toString(),
        selectedUUIDList, USER);
    verify(mockLiveAlertConfigurationRepository, times(1)).findById(any());
    verify(mockAuditRepository, times(1)).save(any());
    verify(mockSandboxAlertConfigurationRepository, times(1)).save(any());
  }

  @Test(expected = AlertConfigurationNotFoundException.class)
  public void shouldThrowAlertNotFoundIfSelectedLiveAlertsNotFound() throws Throwable {
    LiveAlertConfigurationDTO liveDTO = TestObjects.getLiveConfigurationDTOList().get(0);
    List<String> selectedUUIDList = Arrays.asList(liveDTO.getUuid().toString());
    when(mockLiveAlertConfigurationRepository.findById(liveDTO.getUuid()))
        .thenReturn(Optional.empty());

    service.createConfigurationsFromSelectedLiveConfigs(TestUtils.randomUUID().toString(),
        selectedUUIDList, USER);
    verify(mockLiveAlertConfigurationRepository, times(1)).findById(any());
    verify(mockAuditRepository, times(0)).save(any());
    verify(mockSandboxAlertConfigurationRepository, times(0)).save(any());
  }

  @Test
  public void shouldReturnSilentlyIfNoConfigsFoundForSandbox() {
    UUID sandboxUUID = TestUtils.randomUUID();
    service.archiveAllSandboxConfigurations(sandboxUUID, USER);
    verifyZeroInteractions(mockAuditRepository);
    verify(mockSandboxAlertConfigurationRepository, times(1))
        .findAllByPrimaryKeySandboxUUID(sandboxUUID);
    verify(mockSandboxAlertConfigurationRepository, times(0)).saveAll(anyList());
  }

  @Test
  public void shouldArchiveAllConfigsIfConfigsFoundForSandbox() {

    UUID sandboxUUID = TestUtils.randomUUID();
    List<SandboxAlertConfigurationDTO> dtoList = TestObjects.getSandboxConfigurationDTOList();
    when(mockSandboxAlertConfigurationRepository.findAllByPrimaryKeySandboxUUID(sandboxUUID))
        .thenReturn(dtoList);
    service.archiveAllSandboxConfigurations(sandboxUUID, USER);

    verify(mockSandboxAlertConfigurationRepository, times(1))
        .findAllByPrimaryKeySandboxUUID(sandboxUUID);
    verify(mockAuditRepository, times(1)).saveAll(auditDTOListArgumet.capture());
    verify(mockSandboxAlertConfigurationRepository, times(1)).saveAll(dtoListArgument.capture());

    assertThat(auditDTOListArgumet.getValue()).isNotNull();
    assertThat(auditDTOListArgumet.getValue()).hasSize(dtoList.size());
    auditDTOListArgumet.getValue().stream().forEach(dto -> {
      assertThat(dto.getStatus().equals(SandboxAlertConfiguration.StatusEnum.ARCHIVED.toString()));
      assertThat(dto.getCreatedBy().equals(USER));
      assertThat(dto.getCreatedWhen()).isNotNull();
    });
    assertThat(dtoListArgument.getValue()).isNotNull();
    assertThat(dtoListArgument.getValue()).hasSize(dtoList.size());
    dtoListArgument.getValue().stream().forEach(dto -> {
      assertThat(dto.getStatus().equals(SandboxAlertConfiguration.StatusEnum.ARCHIVED.toString()));
      assertThat(dto.getCreatedBy().equals(USER));
      assertThat(dto.getCreatedWhen()).isNotNull();
    });
  }

  @Test(expected = SandboxAlertConfigurationNotFoundException.class)
  public void shouldReturnSandboxAlertConfigurationNotFoundExceptionWhenAlertConfigurationIdInvalidForUpdateStatus()
      throws SandboxAlertConfigurationNotFoundException {
    service.updateStatus(TestUtils.randomUUID().toString(), TestUtils.randomUUID().toString(), null,
        USER);
  }

  @Test
  public void shouldReturnSandboxAlertConfigurationWhenAlertConfigurationIdValidForUpdateStatus()
      throws Throwable {

    SandboxAlertConfigurationDTO dto = TestObjects
        .getSandboxConfigurationDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    UpdateStatus status = new UpdateStatus();
    status.setStatus(UpdateStatus.StatusEnum.INACTIVE);
    when(mockSandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.of(dto));
    when(mockSandboxAlertConfigurationRepository.save(dto)).thenReturn(dto);
    SandboxAlertConfiguration result = service
        .updateStatus(dto.getPrimaryKey().getSandboxUUID().toString(),
            dto.getPrimaryKey().getAlertConfigurationUUID().toString(), status, USER);
    assertThat(result.getStatus().toString())
        .isEqualTo(status.getStatus().toString());

  }

  @Test
  public void shouldReturnAuditListWhenAuditFoundWithValidLimit() {
    UUID sandboxId = TestUtils.randomUUID();
    Integer maxAge = 35;
    Integer numberOfRecords = Integer.valueOf(1);
    List<SandboxAlertConfigurationAuditByMonthDTO> dtoList = TestObjects
        .getSandboxConfigurationAuditByMonthDTOList();
    when(mockAuditByMonthRepository
        .findLatestTopNAuditsWithAgeLimit(any(), any(), any())).thenReturn(dtoList);

    List<SandboxAlertConfigurationAudit> audits = service
        .getAllSandboxAlertConfigurationsAudit(sandboxId.toString(), numberOfRecords, maxAge);
    verify(mockAuditByMonthRepository, times(1))
        .findLatestTopNAuditsWithAgeLimit(sandboxArgumentCaptor.capture(),
            listArgumentCaptor.capture(),
            integerArgumentCaptor.capture()
        );
    assertThat(integerArgumentCaptor.getValue()).isEqualTo(numberOfRecords);
    assertThat(listArgumentCaptor.getValue()).isNotNull();
    assertThat(listArgumentCaptor.getValue().size()).isBetween(1, 3);
  }

  @Test
  public void shouldReturnAuditListWhenAuditFoundWithInvalidUpperLimit() {

    UUID sandboxId = TestUtils.randomUUID();
    Integer defaultLimit = Integer.valueOf(10);
    List<SandboxAlertConfigurationAuditByMonthDTO> dtoList = TestObjects
        .getSandboxConfigurationAuditByMonthDTOList();
    when(mockAuditByMonthRepository
        .findLatestTopNAuditsWithAgeLimit(any(), any(), any())).thenReturn(dtoList);

    //Test with limit more than max limit
    Integer invalidLimit = Integer.valueOf(101);

    List<SandboxAlertConfigurationAudit> audits = service
        .getAllSandboxAlertConfigurationsAudit(sandboxId.toString(), invalidLimit, 10);

    verify(mockAuditByMonthRepository, times(1))
        .findLatestTopNAuditsWithAgeLimit(sandboxArgumentCaptor.capture(),
            listArgumentCaptor.capture(),
            integerArgumentCaptor.capture()
        );
    assertThat(integerArgumentCaptor.getAllValues().get(0)).isEqualTo(defaultLimit);
    assertThat(listArgumentCaptor.getValue()).isNotNull();
    assertThat(listArgumentCaptor.getValue().size()).isBetween(1, 2);

  }

  @Test
  public void shouldReturnAuditListWhenAuditFoundWithInvalidLowerLimit() {

    UUID sandboxId = TestUtils.randomUUID();
    Integer defaultLimit = Integer.valueOf(10);
    List<SandboxAlertConfigurationAuditByMonthDTO> dtoList = TestObjects
        .getSandboxConfigurationAuditByMonthDTOList();
    when(mockAuditByMonthRepository
        .findLatestTopNAuditsWithAgeLimit(any(), any(), any())).thenReturn(dtoList);

    //Test with invalid lower limit
    Integer invalidLimit = Integer.valueOf(0);

    List<SandboxAlertConfigurationAudit> audits = service
        .getAllSandboxAlertConfigurationsAudit(sandboxId.toString(), invalidLimit, 10);
    verify(mockAuditByMonthRepository, times(1))
        .findLatestTopNAuditsWithAgeLimit(sandboxArgumentCaptor.capture(),
            listArgumentCaptor.capture(),
            integerArgumentCaptor.capture());
    assertThat(integerArgumentCaptor.getAllValues().get(0)).isEqualTo(defaultLimit);
  }

  @Test
  public void shouldReturnAuditListWhenAuditFoundForAlertConfiguration() {

    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    SandboxAlertConfigurationDTOPrimaryKey primaryKey = new SandboxAlertConfigurationDTOPrimaryKey(
        sandboxId, alertConfigurationId);

    SandboxAlertConfigurationDTO dto = TestObjects.getSandboxConfigurationDTO(sandboxId,
        alertConfigurationId,
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    );

    when(mockSandboxAlertConfigurationRepository.findById(primaryKey))
        .thenReturn(Optional.ofNullable(dto));

    try {
      List<SandboxAlertConfigurationAudit> audit = service
          .getAuditHistoryForSandboxAlertConfiguration(sandboxId.toString(),
              alertConfigurationId.toString());

      verify(mockSandboxAlertConfigurationRepository, times(1))
          .findById(primaryKeyArgumentCaptor.capture());
      assertThat(primaryKeyArgumentCaptor.getAllValues().get(0).getSandboxUUID())
          .isEqualTo(primaryKey.getSandboxUUID());
      assertThat(primaryKeyArgumentCaptor.getAllValues().get(0).getAlertConfigurationUUID())
          .isEqualTo(primaryKey.getAlertConfigurationUUID());
    } catch (SandboxAlertConfigurationNotFoundException e) {
      fail("Not expected here");
    }
  }

  @Test(expected = SandboxAlertConfigurationNotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenAlertConfigDoesNotExistForAudit() throws Throwable {

    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    service.getAuditHistoryForSandboxAlertConfiguration(sandboxId.toString(),
        alertConfigurationId.toString());
  }

  @Test(expected = SandboxAlertConfigurationNotFoundException.class)
  public void shouldThrowNotFoundExceptionWhenAuditNotFoundForAuditDetails() throws Throwable {
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID auditId = TestUtils.randomUUID();
    service.getSandboxAlertConfigurationAuditById(sandboxId.toString(),
        alertConfigurationId.toString(),
        auditId.toString());
  }

  @Test(expected = AuditNotFoundException.class)
  public void shouldThrowAuditNotFoundWhenParametersNotFoundForAuditDetails() throws Throwable {
    when(mockSandboxAlertConfigurationRepository.existsById(any()))
        .thenReturn(true);
    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    UUID auditId = TestUtils.randomUUID();
    service.getSandboxAlertConfigurationAuditById(sandboxId.toString(),
        alertConfigurationId.toString(),
        auditId.toString());
  }

  @Test
  public void shouldReturnValidAuditWhenExists() throws Throwable {

    UUID sandboxId = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    SandboxAlertConfigurationAuditDTO auditDTO = TestObjects
        .getSandboxConfigurationAuditDTO(sandboxId,
            alertConfigurationId,
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    UUID auditId = auditDTO.getPrimaryKey().getAuditUUID();

    when(mockSandboxAlertConfigurationRepository.existsById(any()))
        .thenReturn(true);

    SandboxAlertConfigurationAuditDTOPrimaryKey auditPrimaryKey = new SandboxAlertConfigurationAuditDTOPrimaryKey();
    auditPrimaryKey.setAlertConfigurationUUID(alertConfigurationId);
    auditPrimaryKey.setSandboxUUID(sandboxId);
    auditPrimaryKey.setAuditUUID(auditId);
    when(mockAuditRepository.findById(auditPrimaryKey)).thenReturn(Optional.of(auditDTO));
    SandboxAlertConfigurationAudit audit = service
        .getSandboxAlertConfigurationAuditById(sandboxId.toString(),
            alertConfigurationId.toString(),
            auditId.toString());
    assertThat(audit).isNotNull();

  }

  @Test(expected = SandboxAlertConfigurationNotFoundException.class)
  public void shouldThrowExceptionWhenSandboxAlertConfigurationDTODoesNotExist() throws Throwable {
    UUID alertConfigUUID = TestUtils.randomUUID();
    when(mockSandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.empty());

    SandboxAlertConfiguration result = service
        .cloneSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(),
            mock(CloneSandboxAlertConfigRequest.class), USER);
  }

  @Test
  public void shouldReturnCloneWhenSandboxAlertConfigurationDTOExists() throws Throwable {
    UUID alertConfigUUID = TestUtils.randomUUID();
    SandboxAlertConfigurationDTO dto = TestObjects.getSandboxConfigurationDTO(sandboxUUID,
        alertConfigUUID,
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        LiveAlertConfiguration.StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    );
    when(mockSandboxAlertConfigurationRepository.findById(any())).thenReturn(Optional.of(dto));
    CloneSandboxAlertConfigRequest request = new CloneSandboxAlertConfigRequest();
    request.setName(TestUtils.randomAlphanumeric());
    SandboxAlertConfiguration result = service
        .cloneSandboxAlertConfiguration(sandboxUUID.toString(), alertConfigUUID.toString(), request,
            USER);

    InOrder inOrder = Mockito.inOrder(alertConfigurationRepositories.getAuditRepository(),
        alertConfigurationRepositories.getSandboxAuditByMonthRepository(),
        alertConfigurationRepositories.getSandboxAlertConfigurationRepository());
    inOrder.verify(alertConfigurationRepositories.getAuditRepository(), times(1)).save(any());
    inOrder.verify(alertConfigurationRepositories.getSandboxAuditByMonthRepository(), times(1))
        .save(any());
    inOrder
        .verify(alertConfigurationRepositories.getSandboxAlertConfigurationRepository(), times(1))
        .save(any());
  }
}
