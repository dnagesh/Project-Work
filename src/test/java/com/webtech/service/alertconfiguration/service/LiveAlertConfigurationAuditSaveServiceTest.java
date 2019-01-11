package com.webtech.service.alertconfiguration.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.TestObjects;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.mapper.LiveAlertConfigurationObjectMapper;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration.StatusEnum;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByAlertConfigUUIDRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditByMonthRepository;
import com.webtech.service.alertconfiguration.repository.LiveAlertConfigurationAuditRepository;
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
public class LiveAlertConfigurationAuditSaveServiceTest {

  InOrder inOrder;
  @Captor
  ArgumentCaptor<LiveAlertConfigurationAuditDTO> auditDTOArgumentCaptor;
  @Captor
  ArgumentCaptor<LiveAlertConfigurationAuditByMonthDTO> auditByMonthDTOArgumentCaptor;
  @Captor
  ArgumentCaptor<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> auditByAlertConfigDTOArgumentCaptor;
  @Mock
  private LiveAlertConfigurationAuditRepository liveAlertConfigurationAuditRepository;
  @Mock
  private LiveAlertConfigurationAuditByAlertConfigUUIDRepository auditByAlertConfigUUIDRepository;
  @Mock
  private LiveAlertConfigurationObjectMapper liveAlertConfigurationObjectMapper;
  @Mock
  private LiveAlertConfigurationAuditByMonthRepository auditByMonthRepository;
  private LiveAlertConfigurationAuditSaveService auditSaveService;
  private LiveAlertConfigurationAuditDTO auditDTO;
  private LiveAlertConfigurationAuditByMonthDTO auditByMonthDTO;
  private LiveAlertConfigurationAuditByAlertConfigUUIDDTO auditByAlertConfigUUIDDTO;

  @Before
  public void setUp() throws Exception {
    auditSaveService = new LiveAlertConfigurationAuditSaveService(
        liveAlertConfigurationAuditRepository, auditByAlertConfigUUIDRepository,
        liveAlertConfigurationObjectMapper, auditByMonthRepository);
    inOrder = Mockito
        .inOrder(liveAlertConfigurationObjectMapper, liveAlertConfigurationAuditRepository,
            auditByAlertConfigUUIDRepository, auditByMonthRepository);
    auditDTO = TestObjects.getLiveConfigurationAuditDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant());
    auditByMonthDTO = TestObjects.getLiveConfigurationAuditByMonthDTO(TestUtils.randomUUID(),
        TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant());
    auditByAlertConfigUUIDDTO = TestObjects
        .getLiveConfigurationAuditByAlertConfigDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());
    when(liveAlertConfigurationObjectMapper.mapLiveDTOToAuditDTO(any()))
        .thenReturn(auditDTO);
    when(liveAlertConfigurationObjectMapper.mapLiveDTOToAuditByAlertConfigUUIDDTO(any()))
        .thenReturn(auditByAlertConfigUUIDDTO);
    when(liveAlertConfigurationObjectMapper.mapLiveDTOToAuditByMonthDTO(any()))
        .thenReturn(auditByMonthDTO);
  }

  @Test
  public void shouldNotAuditWhenNullInput() {
    auditSaveService.saveAudits(null);
    inOrder.verify(liveAlertConfigurationObjectMapper, times(0)).mapLiveDTOToAuditDTO(any());
    inOrder.verify(liveAlertConfigurationObjectMapper, times(0))
        .mapLiveDTOToAuditByAlertConfigUUIDDTO(any());
    inOrder.verify(liveAlertConfigurationObjectMapper, times(0)).mapLiveDTOToAuditByMonthDTO(any());

    inOrder.verify(liveAlertConfigurationAuditRepository, times(0)).save(any());
    inOrder.verify(auditByAlertConfigUUIDRepository, times(0)).save(any());
    inOrder.verify(auditByMonthRepository, times(0)).save(any());

  }

  @Test
  public void shouldAuditWhenValidInput() {
    LiveAlertConfigurationDTO dto = TestObjects.getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5));

    auditSaveService.saveAudits(dto);
    inOrder.verify(liveAlertConfigurationObjectMapper, times(1)).mapLiveDTOToAuditDTO(any());
    inOrder.verify(liveAlertConfigurationObjectMapper, times(1))
        .mapLiveDTOToAuditByAlertConfigUUIDDTO(any());
    inOrder.verify(liveAlertConfigurationObjectMapper, times(1)).mapLiveDTOToAuditByMonthDTO(any());

    inOrder.verify(liveAlertConfigurationAuditRepository, times(1))
        .save(auditDTOArgumentCaptor.capture());
    inOrder.verify(auditByAlertConfigUUIDRepository, times(1))
        .save(auditByAlertConfigDTOArgumentCaptor.capture());
    inOrder.verify(auditByMonthRepository, times(1))
        .save(auditByMonthDTOArgumentCaptor.capture());

    assertThat(auditDTOArgumentCaptor.getValue()).isNotNull();
    assertThat(auditDTOArgumentCaptor.getValue().getAuditUUID()).isNotNull();
    assertThat(auditByAlertConfigDTOArgumentCaptor.getValue()).isNotNull();
    assertThat(auditByAlertConfigDTOArgumentCaptor.getValue().getAuditUUID()).isNotNull();
    assertThat(auditByMonthDTOArgumentCaptor.getValue()).isNotNull();
    assertThat(auditByMonthDTOArgumentCaptor.getValue().getPrimaryKey().getAuditUUID())
        .isNotNull();

    assertThat(auditByAlertConfigDTOArgumentCaptor.getValue().getAuditUUID())
        .isEqualTo(auditDTOArgumentCaptor.getValue().getAuditUUID());
    assertThat(auditByMonthDTOArgumentCaptor.getValue().getPrimaryKey().getAuditUUID())
        .isEqualTo(auditDTOArgumentCaptor.getValue().getAuditUUID());


  }
}
