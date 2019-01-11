package com.webtech.service.alertconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.TestObjects;
import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.exception.AlertConfigurationNotFoundException;
import com.webtech.service.alertconfiguration.exception.AuditNotFoundException;
import com.webtech.service.alertconfiguration.mapper.LiveAlertConfigurationObjectMapper;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration.StatusEnum;
import com.irisium.service.alertconfiguration.model.LiveAlertConfigurationAudit;
import com.webtech.service.alertconfiguration.repository.AlertParameterSetRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByAlertConfigUUIDRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByMonthRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationRepository;
import com.webtech.service.common.AppPropertiesProvider;
import java.time.LocalDate;
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
public class LiveAlertConfigurationQueryServiceTest {

  private static final String USER = TestUtils.randomAlphanumeric(5);
  @Captor
  ArgumentCaptor<List<LiveAlertConfigurationDTO>> dtoListCaptor;
  @Captor
  ArgumentCaptor<Integer> integerArgumentCaptor;
  @Captor
  ArgumentCaptor<List<LiveAlertConfigurationAuditByMonthDTO>> auditDtoListCaptor;
  @Captor
  ArgumentCaptor<List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO>> auditByAlertConfigDtoListCaptor;
  @Captor
  ArgumentCaptor<UUID> uuidArgumentCaptor;
  @Captor
  ArgumentCaptor<String> apsHashArgumentCaptor;
  @Captor
  ArgumentCaptor<LiveAlertConfigurationDTO> dtoArgumentCaptor;
  @Captor
  ArgumentCaptor<AlertParameterSetDTO> parameterSetDTOArgumentCaptor;
  @Captor
  ArgumentCaptor<LiveAlertConfigurationAuditDTO> auditDTOArgumentCaptor;
  @Captor
  ArgumentCaptor<List<LocalDate>> listArgumentCaptor;
  @Mock
  private AlertParameterSetRepository mockAlertParameterSetRepository;
  @Mock
  private LiveAlertConfigurationRepository mockLiveAlertConfigurationRepository;
  @Mock
  private LiveAlertConfigurationAuditRepository mockLiveAlertConfigurationAuditRepository;
  @Mock
  private LiveAlertConfigurationObjectMapper mockLiveAlertConfigurationObjectMapper;
  @Mock
  private LiveAlertConfigurationAuditByMonthRepository mockAuditByMonthRepository;
  @Mock
  private LiveAlertConfigurationAuditByAlertConfigUUIDRepository mockAuditByAlertConfigUUIDRepository;
  @Mock
  private AppPropertiesProvider mockAppPropertiesProvider;

  private LiveAlertConfigurationQueryService service;

  private UUID alertConfigurationUUID;
  private LiveAlertConfigurationDTO dto;
  private AlertParameterSetDTO parameterSetDTO;
  private UUID auditUUID;
  private LiveAlertConfigurationAuditDTO auditDTO;

  @Before
  public void setUp() throws Exception {
    service = new LiveAlertConfigurationQueryService(mockAlertParameterSetRepository,
        mockLiveAlertConfigurationRepository, mockLiveAlertConfigurationAuditRepository,
        mockAuditByMonthRepository, mockLiveAlertConfigurationObjectMapper,
        mockAuditByAlertConfigUUIDRepository, mockAppPropertiesProvider);

    when(mockAppPropertiesProvider.getDefaultAuditRowCountLimit()).thenReturn(10);
    when(mockAppPropertiesProvider.getMinAuditRowCountLimit()).thenReturn(1);
    when(mockAppPropertiesProvider.getMaxAuditRowCountLimit()).thenReturn(100);

    alertConfigurationUUID = TestUtils.randomUUID();
    dto = TestObjects.getLiveConfigurationDTO(alertConfigurationUUID,
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5)
    );
    when(mockLiveAlertConfigurationRepository.findById(alertConfigurationUUID)).thenReturn(
        Optional.ofNullable(dto));
    when(mockLiveAlertConfigurationRepository.existsById(any()))
        .thenReturn(true);
    parameterSetDTO = TestObjects.getAlertParameterSetDTO();

    when(mockAlertParameterSetRepository.findById(any()))
        .thenReturn(Optional.ofNullable(parameterSetDTO));

    auditUUID = TestUtils.randomUUID();
    auditDTO = TestObjects.getLiveConfigurationAuditDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant());
    when(mockLiveAlertConfigurationAuditRepository.findByAuditUUID(any()))
        .thenReturn(Optional.ofNullable(auditDTO));
  }

  @Test
  public void shouldReturnDataReturnedByRepository() {
    List<LiveAlertConfigurationDTO> dtoList = TestObjects.getLiveConfigurationDTOList();
    when(mockLiveAlertConfigurationRepository.findAll()).thenReturn(dtoList);

    service.getAllLiveAlertConfigurations();

    verify(mockLiveAlertConfigurationRepository, times(1)).findAll();
    verify(mockLiveAlertConfigurationObjectMapper, times(1))
        .mapDTOListToModelList(dtoListCaptor.capture());
    assertThat(dtoListCaptor.getValue()).isNotNull();
    assertThat(dtoListCaptor.getValue()).hasSize(dtoList.size());
  }

  @Test
  public void shouldReturnAuditListWhenAuditFoundWithValidLimit() {
    Integer numberOfRecords = Integer.valueOf(1);
    Integer maxAge = 35;
    List<LiveAlertConfigurationAuditByMonthDTO> dtoList = TestObjects
        .getLiveConfigurationAuditByMonthDTOList();
    when(mockAuditByMonthRepository
        .findLatestTopNAuditsWithAgeLimit(any(), any())).thenReturn(dtoList);

    List<LiveAlertConfigurationAudit> audits = service
        .getAllLiveAlertConfigurationsAudit(numberOfRecords, maxAge);
    verify(mockAuditByMonthRepository, times(1))
        .findLatestTopNAuditsWithAgeLimit(listArgumentCaptor.capture(),
            integerArgumentCaptor.capture()
        );
    assertThat(integerArgumentCaptor.getValue()).isEqualTo(numberOfRecords);
    assertThat(listArgumentCaptor.getValue()).isNotNull();

    verify(mockLiveAlertConfigurationObjectMapper, times(1))
        .mapAuditByMonthDTOListToModelList(auditDtoListCaptor.capture());
    assertThat(auditDtoListCaptor.getValue()).isNotNull();
    assertThat(auditDtoListCaptor.getValue()).hasSize(numberOfRecords);

  }

  @Test
  public void shouldReturnAuditListWhenAuditFoundWithInvalidUpperLimit() {

    Integer defaultLimit = Integer.valueOf(10);
    List<LiveAlertConfigurationAuditByMonthDTO> dtoList = TestObjects
        .getLiveConfigurationAuditByMonthDTOList();
    when(mockAuditByMonthRepository
        .findLatestTopNAuditsWithAgeLimit(any(), any())).thenReturn(dtoList);

    //Test with limit more than max limit
    Integer invalidLimit = Integer.valueOf(101);

    List<LiveAlertConfigurationAudit> audits = service
        .getAllLiveAlertConfigurationsAudit(invalidLimit, 10);

    verify(mockAuditByMonthRepository, times(1))
        .findLatestTopNAuditsWithAgeLimit(listArgumentCaptor.capture(),
            integerArgumentCaptor.capture()
        );
    assertThat(integerArgumentCaptor.getAllValues().get(0)).isEqualTo(defaultLimit);
    assertThat(listArgumentCaptor.getValue()).isNotNull();
    assertThat(listArgumentCaptor.getValue().size()).isBetween(1, 2);

    verify(mockLiveAlertConfigurationObjectMapper, times(1))
        .mapAuditByMonthDTOListToModelList(auditDtoListCaptor.capture());
    assertThat(auditDtoListCaptor.getValue()).isNotNull();
    assertThat(auditDtoListCaptor.getValue()).hasSize(dtoList.size());

  }

  @Test
  public void shouldReturnAuditListWhenAuditFoundWithInvalidLowerLimit() {

    Integer defaultLimit = Integer.valueOf(10);
    List<LiveAlertConfigurationAuditByMonthDTO> dtoList = TestObjects
        .getLiveConfigurationAuditByMonthDTOList();
    when(mockAuditByMonthRepository
        .findLatestTopNAuditsWithAgeLimit(any(), any())).thenReturn(dtoList);

    //Test with invalid lower limit
    Integer invalidLimit = Integer.valueOf(0);

    List<LiveAlertConfigurationAudit> audits = service
        .getAllLiveAlertConfigurationsAudit(invalidLimit, 10);
    verify(mockAuditByMonthRepository, times(1))
        .findLatestTopNAuditsWithAgeLimit(listArgumentCaptor.capture(),
            integerArgumentCaptor.capture());
    assertThat(integerArgumentCaptor.getAllValues().get(0)).isEqualTo(defaultLimit);
  }

  @Test
  public void shouldReturnDataWhenAlertConfigExists() throws Throwable {
    Optional<LiveAlertConfiguration> result = service
        .getLiveAlertConfigurationById(alertConfigurationUUID.toString());

    verify(mockLiveAlertConfigurationRepository, times(1))
        .findById(uuidArgumentCaptor.capture());
    verify(mockAlertParameterSetRepository, times(1))
        .findById(apsHashArgumentCaptor.capture());

    assertThat(uuidArgumentCaptor.getValue()).isNotNull();
    assertThat(uuidArgumentCaptor.getValue()).isEqualTo(alertConfigurationUUID);

    assertThat(apsHashArgumentCaptor.getValue()).isEqualTo(dto.getApsHash());

    verify(mockLiveAlertConfigurationObjectMapper, times(1))
        .liveDTOToModelWithParameterSet(dtoArgumentCaptor.capture(),
            parameterSetDTOArgumentCaptor.capture());

    assertThat(dtoArgumentCaptor.getValue()).isNotNull();
    assertThat(dtoArgumentCaptor.getValue()).isEqualTo(dto);
    assertThat(parameterSetDTOArgumentCaptor.getValue()).isNotNull();
    assertThat(parameterSetDTOArgumentCaptor.getValue()).isEqualTo(parameterSetDTO);


  }

  @Test
  public void shouldReturnEmptyWhenAlertConfigDoesNotExist() {
    when(mockLiveAlertConfigurationRepository.findById(alertConfigurationUUID)).thenReturn(
        Optional.empty());
    Optional<LiveAlertConfiguration> result = service
        .getLiveAlertConfigurationById(alertConfigurationUUID.toString());
    assertThat(result).isNotNull();
    assertThat(result.isPresent()).isFalse();
  }

  @Test
  public void shouldNotPopulateParametersWhenParametersNotFoundForAlertConfigDetails() {
    when(mockAlertParameterSetRepository.findById(any())).thenReturn(
        Optional.ofNullable(null));
    Optional<LiveAlertConfiguration> result = service
        .getLiveAlertConfigurationById(alertConfigurationUUID.toString());
    verify(mockLiveAlertConfigurationObjectMapper, times(1))
        .liveDTOToModelWithParameterSet(dtoArgumentCaptor.capture(),
            parameterSetDTOArgumentCaptor.capture());
    assertThat(dtoArgumentCaptor.getValue()).isNotNull();
    assertThat(dtoArgumentCaptor.getValue()).isEqualTo(dto);

    assertThat(parameterSetDTOArgumentCaptor.getValue()).isNull();

  }

  @Test
  public void shouldReturnAuditListWhenAuditFoundForAlertConfiguration() {

    List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> dtoList = TestObjects
        .getLiveConfigurationAuditByAlertConfigUUIDDTOList();
    when(mockAuditByAlertConfigUUIDRepository
        .findByPrimaryKeyAlertConfigUUID(
            alertConfigurationUUID)).thenReturn(dtoList);

    when(mockLiveAlertConfigurationRepository.findById(alertConfigurationUUID))
        .thenReturn(Optional.ofNullable(dto));

    try {
      List<LiveAlertConfigurationAudit> audits = service
          .getAuditHistoryForLiveAlertConfiguration(alertConfigurationUUID.toString());

      verify(mockLiveAlertConfigurationRepository, times(1))
          .findById(uuidArgumentCaptor.capture());
      verify(mockAuditByAlertConfigUUIDRepository, times(1))
          .findByPrimaryKeyAlertConfigUUID(
              uuidArgumentCaptor.capture());
      assertThat(uuidArgumentCaptor.getAllValues().get(0)).isEqualTo(alertConfigurationUUID);
      assertThat(uuidArgumentCaptor.getAllValues().get(1)).isEqualTo(alertConfigurationUUID);

      verify(mockLiveAlertConfigurationObjectMapper, times(1))
          .mapAuditByAlertConfigUUIDDTOListToModelList(auditByAlertConfigDtoListCaptor.capture());
      assertThat(auditByAlertConfigDtoListCaptor.getValue()).isNotNull();
      assertThat(auditByAlertConfigDtoListCaptor.getValue()).hasSize(dtoList.size());
    } catch (AlertConfigurationNotFoundException e) {
      fail("Not expected here");
    }
  }


  @Test
  public void shouldThrowNotFoundExceptionWhenAlertConfigDoesNotExistForAudit() {
    when(mockLiveAlertConfigurationRepository.findById(alertConfigurationUUID)).thenReturn(
        Optional.ofNullable(null));
    try {
      List<LiveAlertConfigurationAudit> result = service
          .getAuditHistoryForLiveAlertConfiguration(alertConfigurationUUID.toString());
      fail("Cannot return audit history for non-existent Alert Config");
    } catch (AlertConfigurationNotFoundException e) {
      assertThat(e.getMessage()).contains(alertConfigurationUUID.toString());
    }
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenAuditNotFoundForAuditDetails() throws Throwable {
    when(mockLiveAlertConfigurationAuditRepository
        .findByAuditUUID(any())).thenReturn(
        Optional.ofNullable(null));
    try {
      LiveAlertConfigurationAudit result = service
          .getLiveAlertConfigurationAuditById(alertConfigurationUUID.toString(),
              auditUUID.toString());
      fail("Cannot return details for non-existent Audit record");
    } catch (AuditNotFoundException e) {
      assertThat(e.getMessage()).contains(alertConfigurationUUID.toString());
      assertThat(e.getMessage()).contains(auditUUID.toString());
    }
  }

  @Test
  public void shouldThrowNotFoundExceptionWhenAAlertConfigNotFoundForAuditDetails()
      throws Throwable {
    when(mockLiveAlertConfigurationRepository.existsById(any()))
        .thenReturn(false);
    try {
      LiveAlertConfigurationAudit result = service
          .getLiveAlertConfigurationAuditById(alertConfigurationUUID.toString(),
              auditUUID.toString());
      fail("Cannot return audit details for non-existent Alert configuration record");
    } catch (AlertConfigurationNotFoundException e) {
      assertThat(e.getMessage()).contains(alertConfigurationUUID.toString());
    }
  }

  @Test
  public void shouldNotPopulateParametersWhenParametersNotFoundForAuditDetails() throws Throwable {
    when(mockAlertParameterSetRepository.findById(any())).thenReturn(
        Optional.ofNullable(null));

    try {
      LiveAlertConfigurationAudit result = service
          .getLiveAlertConfigurationAuditById(alertConfigurationUUID.toString(),
              auditUUID.toString());
      verify(mockLiveAlertConfigurationObjectMapper, times(1))
          .mapLiveAuditDTOToModel(auditDTOArgumentCaptor.capture(),
              parameterSetDTOArgumentCaptor.capture());
      assertThat(auditDTOArgumentCaptor.getValue()).isNotNull();
      assertThat(auditDTOArgumentCaptor.getValue()).isEqualTo(auditDTO);

      assertThat(parameterSetDTOArgumentCaptor.getValue()).isNull();

    } catch (AuditNotFoundException e) {
      fail("Cannot return details for non-existent Audit record");
    }
  }

}
