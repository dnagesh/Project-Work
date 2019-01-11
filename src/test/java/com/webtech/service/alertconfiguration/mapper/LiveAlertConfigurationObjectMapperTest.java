package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.TestObjects;
import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByAlertConfigUUIDDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration.StatusEnum;
import com.irisium.service.alertconfiguration.model.LiveAlertConfigurationAudit;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LiveAlertConfigurationObjectMapperTest {

  private static final String USER = TestUtils.randomAlphanumeric(5);
  private LiveAlertConfigurationObjectMapper mapper;

  @Before
  public void setUp() {
    mapper = new LiveAlertConfigurationObjectMapper(new AlertFilterMapper());
  }

  @Test
  public void shouldReturnInvalidModelForInvalidDTO() {
    LiveAlertConfiguration result = mapper.liveDTOToModelWithParameterSet(null, null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnValidModelForValidDTO() {
    LiveAlertConfigurationDTO dto = TestObjects.getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5));
    LiveAlertConfiguration result = mapper.liveDTOToModelWithParameterSet(dto, null);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(dto.getComment());

    assertThat(result.getAlertAggregationFields()).isNullOrEmpty();
    assertThat(result.getAlertFilters()).isNullOrEmpty();
    assertThat(result.getAlertParameters()).isNullOrEmpty();

  }

  @Test
  public void shouldReturnValidModelWithParametersForValidDTOAndParameterSet()
      throws NoSuchAlgorithmException {
    LiveAlertConfigurationDTO dto = TestObjects.getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5));
    AlertParameterSetDTO parameterSetDTO = TestObjects.getAlertParameterSetDTO();
    LiveAlertConfiguration result = mapper.liveDTOToModelWithParameterSet(dto, parameterSetDTO);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(dto.getComment());

    assertThat(result.getAlertAggregationFields()).isNotNull();
    assertThat(result.getAlertAggregationFields())
        .isEqualTo(Arrays.asList(parameterSetDTO.getAlertAggregationFields().toArray()));
    assertThat(result.getAlertFilters()).isNotNull();
    assertThat(result.getAlertFilters()).hasSize(parameterSetDTO.getAlertFilterDTOS().size());
    assertThat(result.getAlertParameters()).isNotNull();
    assertThat(result.getAlertParameters()).isEqualTo(parameterSetDTO.getAlertParameters());

  }

  @Test
  public void shouldReturnValidModelListForValidDTOList() {
    List<LiveAlertConfigurationDTO> liveAlertConfigurationDTOS = TestObjects
        .getLiveConfigurationDTOList();
    List<LiveAlertConfiguration> modelList = mapper
        .mapDTOListToModelList(liveAlertConfigurationDTOS);

    assertThat(modelList).isNotNull();
    assertThat(modelList).hasSize(liveAlertConfigurationDTOS.size());
  }

  @Test
  public void shouldReturnInvalidModelListForInvalidDTOList() {
    List<LiveAlertConfiguration> modelList = mapper.mapDTOListToModelList(null);

    assertThat(modelList).isNotNull();
    assertThat(modelList).isEmpty();
  }

  @Test
  public void shouldReturnValidAuditModelListForValidAuditDTOList() {
    List<LiveAlertConfigurationAuditByMonthDTO> auditDTOS = TestObjects
        .getLiveConfigurationAuditByMonthDTOList();
    List<LiveAlertConfigurationAudit> modelList = mapper
        .mapAuditByMonthDTOListToModelList(auditDTOS);

    assertThat(modelList).isNotNull();
    assertThat(modelList).hasSize(auditDTOS.size());
  }

  @Test
  public void shouldReturnInvalidAuditModelListForInvalidAuditDTOList() {
    List<LiveAlertConfigurationAudit> modelList = mapper.mapAuditByMonthDTOListToModelList(null);

    assertThat(modelList).isNotNull();
    assertThat(modelList).isEmpty();
  }

  @Test
  public void shouldReturnValidAuditModelWithParametersForDTOWithParameters()
      throws NoSuchAlgorithmException {
    LiveAlertConfigurationAuditByMonthDTO dto = TestObjects
        .getLiveConfigurationAuditByMonthDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());
    AlertParameterSetDTO parameterSetDTO = TestObjects.getAlertParameterSetDTO();

    LiveAlertConfigurationAudit result = mapper.mapAuditDTOByMonthToModel(dto, parameterSetDTO);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(dto.getComment());

    assertThat(result.getAlertAggregationFields()).isNotNull();
    assertThat(result.getAlertAggregationFields())
        .isEqualTo(Arrays.asList(parameterSetDTO.getAlertAggregationFields().toArray()));
    assertThat(result.getAlertFilters()).isNotNull();
    assertThat(result.getAlertFilters()).hasSize(parameterSetDTO.getAlertFilterDTOS().size());
    assertThat(result.getAlertParameters()).isNotNull();
    assertThat(result.getAlertParameters()).isEqualTo(parameterSetDTO.getAlertParameters());
    assertThat(result.getUpdatedWho()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());

  }

  @Test
  public void shouldReturnValidAuditModelWithoutParametersForDTOWithoutParameters()
      throws NoSuchAlgorithmException {
    LiveAlertConfigurationAuditByMonthDTO dto = TestObjects
        .getLiveConfigurationAuditByMonthDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());
    AlertParameterSetDTO parameterSetDTO = TestObjects.getAlertParameterSetDTO();

    LiveAlertConfigurationAudit result = mapper.mapAuditDTOByMonthToModel(dto, null);

    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(dto.getComment());

    assertThat(result.getAlertAggregationFields()).isNullOrEmpty();
    assertThat(result.getAlertFilters()).isNullOrEmpty();
    assertThat(result.getAlertParameters()).isNullOrEmpty();
    assertThat(result.getUpdatedWho()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());


  }

  @Test
  public void shouldReturnInvalidAuditModelForInvalidAuditDTO() {
    LiveAlertConfigurationAudit audit = mapper.mapAuditDTOByMonthToModel(null, null);

    assertThat(audit).isNull();
  }

  @Test
  public void shouldReturnValidLiveConfigForValidSandboxConfigDTOWithoutLiveConfigUUID() {
    SandboxAlertConfigurationDTO dto = TestObjects
        .getSandboxConfigurationDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());

    LiveAlertConfigurationDTO result = mapper.mapSandboxConfigDTOToLiveConfigDTO(dto, USER);

    assertThat(result).isNotNull();
    assertThat(result.getUuid()).isNotEqualTo(dto.getLiveConfigUUID());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getStatus()).isEqualTo(dto.getStatus());
    assertThat(result.getAlertLogicType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(dto.getComment());

    assertThat(result.getCreatedBy()).isEqualTo(USER);
    assertThat(result.getCreatedWhen()).isNotNull();
    assertThat(result.getCreatedWhen()).isNotEqualTo(dto.getCreatedWhen());
    assertThat(result.getUpdatedBy()).isNull();
    assertThat(result.getUpdatedWhen()).isNull();
  }


  @Test
  public void shouldReturnValidLiveConfigForValidSandboxConfigDTOWithLiveConfigUUID() {
    SandboxAlertConfigurationDTO dto = TestObjects
        .getSandboxConfigurationDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());

    dto.setLiveConfigUUID(TestUtils.randomUUID());
    LiveAlertConfigurationDTO result = mapper.mapSandboxConfigDTOToLiveConfigDTO(dto, USER);

    assertThat(result).isNotNull();
    assertThat(result.getUuid()).isEqualTo(dto.getLiveConfigUUID());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getStatus()).isEqualTo(dto.getStatus());
    assertThat(result.getAlertLogicType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(dto.getComment());

    //assertThat(result.getCreatedBy()).isNull();
    //assertThat(result.getCreatedWhen()).isNull();
    assertThat(result.getUpdatedBy()).isEqualTo(USER);
    assertThat(result.getUpdatedWhen()).isNotNull();

  }

  @Test
  public void shouldReturnValidAuditDTOForValidLiveConfigDTO() {
    LiveAlertConfigurationDTO liveDTO = TestObjects.getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5));

    LiveAlertConfigurationAuditDTO auditDTO = mapper.mapLiveDTOToAuditDTO(liveDTO);
    assertThat(auditDTO).isNotNull();
    assertThat(auditDTO.getAlertConfigurationUUID()).isEqualTo(liveDTO.getUuid());
    assertThat(auditDTO.getAlertLogicType()).isEqualTo(liveDTO.getAlertLogicType());
    assertThat(auditDTO.getApsHash()).isEqualTo(liveDTO.getApsHash());
    assertThat(auditDTO.getComment()).isEqualTo(liveDTO.getComment());
    assertThat(auditDTO.getName()).isEqualTo(liveDTO.getName());
    assertThat(auditDTO.getStatus()).isEqualTo(liveDTO.getStatus());
    assertThat(auditDTO.getCreatedBy()).isEqualTo(liveDTO.getCreatedBy());
    assertThat(auditDTO.getCreatedWhen()).isEqualTo(liveDTO.getCreatedWhen());
    assertThat(auditDTO.getUpdatedBy()).isEqualTo(liveDTO.getUpdatedBy());
    assertThat(auditDTO.getUpdatedWhen()).isEqualTo(liveDTO.getUpdatedWhen());

    assertThat(auditDTO.getAuditUUID()).isNotNull();

  }

  @Test
  public void shouldReturnValidAuditByAlertConfigUUIDDTOForValidLiveConfigDTO() {
    LiveAlertConfigurationDTO liveDTO = TestObjects.getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5));

    LiveAlertConfigurationAuditByAlertConfigUUIDDTO auditDTO = mapper
        .mapLiveDTOToAuditByAlertConfigUUIDDTO(liveDTO);
    assertThat(auditDTO).isNotNull();
    assertThat(auditDTO.getAlertLogicType()).isEqualTo(liveDTO.getAlertLogicType());
    assertThat(auditDTO.getApsHash()).isEqualTo(liveDTO.getApsHash());
    assertThat(auditDTO.getComment()).isEqualTo(liveDTO.getComment());
    assertThat(auditDTO.getName()).isEqualTo(liveDTO.getName());
    assertThat(auditDTO.getStatus()).isEqualTo(liveDTO.getStatus());
    assertThat(auditDTO.getCreatedBy()).isEqualTo(liveDTO.getCreatedBy());
    assertThat(auditDTO.getCreatedWhen()).isEqualTo(liveDTO.getCreatedWhen());
    assertThat(auditDTO.getUpdatedBy()).isEqualTo(liveDTO.getUpdatedBy());
    assertThat(auditDTO.getUpdatedWhen()).isEqualTo(liveDTO.getUpdatedWhen());

    assertThat(auditDTO.getPrimaryKey()).isNotNull();
    assertThat(auditDTO.getPrimaryKey().getAlertConfigUUID()).isNotNull();
    assertThat(auditDTO.getPrimaryKey().getAlertConfigUUID()).isEqualTo(liveDTO.getUuid());
    assertThat(auditDTO.getPrimaryKey().getAuditTimestamp()).isNotNull();

  }

  @Test
  public void shouldReturnValidAuditByAlertConfigUUIDDTOForValidLiveConfigDTOAndCreatedWhenIsNull() {
    LiveAlertConfigurationDTO liveDTO = TestObjects.getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        null,
        null,
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5));

    LiveAlertConfigurationAuditByAlertConfigUUIDDTO auditDTO = mapper
        .mapLiveDTOToAuditByAlertConfigUUIDDTO(liveDTO);
    assertThat(auditDTO).isNotNull();
    assertThat(auditDTO.getAlertLogicType()).isEqualTo(liveDTO.getAlertLogicType());
    assertThat(auditDTO.getApsHash()).isEqualTo(liveDTO.getApsHash());
    assertThat(auditDTO.getComment()).isEqualTo(liveDTO.getComment());
    assertThat(auditDTO.getName()).isEqualTo(liveDTO.getName());
    assertThat(auditDTO.getStatus()).isEqualTo(liveDTO.getStatus());
    assertThat(auditDTO.getCreatedBy()).isNull();
    assertThat(auditDTO.getCreatedWhen()).isNull();
    assertThat(auditDTO.getUpdatedBy()).isEqualTo(liveDTO.getUpdatedBy());
    assertThat(auditDTO.getUpdatedWhen()).isEqualTo(liveDTO.getUpdatedWhen());

    assertThat(auditDTO.getPrimaryKey()).isNotNull();
    assertThat(auditDTO.getPrimaryKey().getAlertConfigUUID()).isNotNull();
    assertThat(auditDTO.getPrimaryKey().getAlertConfigUUID()).isEqualTo(liveDTO.getUuid());
    assertThat(auditDTO.getPrimaryKey().getAuditTimestamp()).isNotNull();

  }

  @Test
  public void shouldReturnNullForInvalidLiveConfigDTO() {

    LiveAlertConfigurationAuditDTO auditDTO = mapper.mapLiveDTOToAuditDTO(null);
    assertThat(auditDTO).isNull();

    LiveAlertConfigurationAuditByAlertConfigUUIDDTO auditByAlertConfigUUIDDTO = mapper
        .mapLiveDTOToAuditByAlertConfigUUIDDTO(null);
    assertThat(auditByAlertConfigUUIDDTO).isNull();

  }

  @Test
  public void shouldReturnNullForNullInput() {
    assertThat(mapper.mapSandboxConfigDTOToLiveConfigDTO(null, null)).isNull();
  }

  @Test
  public void shouldReturnEmptyAuditListForInvalidLiveConfigDTOList() {
    assertThat(mapper.mapAuditByAlertConfigDTOListToModelList(null)).isNullOrEmpty();
  }

  @Test
  public void shouldReturnValidAuditListForValidLiveConfigDTOList() {
    List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> audits = TestObjects
        .getLiveConfigurationAuditByAlertConfigUUIDDTOList();
    List<LiveAlertConfigurationAudit> result = mapper
        .mapAuditByAlertConfigDTOListToModelList(audits);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(audits.size());
  }

  @Test
  public void shouldReturnValidModelForValidAuditDTO() {
    LiveAlertConfigurationAuditByAlertConfigUUIDDTO dto = TestObjects
        .getLiveConfigurationAuditByAlertConfigUUIDDTO(
            TestUtils.randomUUID(), TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    LiveAlertConfigurationAudit result = mapper.mapAuditByAlertConfigDTOToModel(dto);
    assertThat(result).isNotNull();
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(dto.getPrimaryKey().getAlertConfigUUID().toString());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getUpdatedWho()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());

    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
  }

  @Test
  public void shouldReturnNullModelForNullAuditDTO() {
    LiveAlertConfigurationAudit result = mapper.mapAuditByAlertConfigDTOToModel(null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnEmptyModelListForEmptyAuditByAlertConfigList() {
    assertThat(mapper.mapAuditByAlertConfigUUIDDTOListToModelList(null)).isEmpty();
  }

  @Test
  public void shouldReturnValidModelListForValidAuditByAlertConfigList() {
    List<LiveAlertConfigurationAuditByAlertConfigUUIDDTO> dtoList = TestObjects
        .getLiveConfigurationAuditByAlertConfigUUIDDTOList();
    List<LiveAlertConfigurationAudit> result = mapper
        .mapAuditByAlertConfigUUIDDTOListToModelList(dtoList);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(dtoList.size());
  }

  @Test
  public void shouldReturnValidModelForValidAuditByAlertConfigWithParamSet() throws Throwable {
    LiveAlertConfigurationAuditByAlertConfigUUIDDTO dto = TestObjects
        .getLiveConfigurationAuditByAlertConfigUUIDDTO(
            TestUtils.randomUUID(), TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );

    AlertParameterSetDTO alertParameterSetDTO = TestObjects.getAlertParameterSetDTO();

    LiveAlertConfigurationAudit result = mapper
        .mapAuditDTOByAlertConfigUUIDToModel(dto, alertParameterSetDTO);
    assertThat(result).isNotNull();
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getUpdatedWho()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(dto.getPrimaryKey().getAlertConfigUUID().toString());
    assertThat(result.getAuditUUID()).isEqualTo(dto.getAuditUUID().toString());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());

    assertThat(result.getAlertParameters()).isNotEmpty();
    assertThat(result.getAlertFilters()).isNotEmpty();
    assertThat(result.getAlertAggregationFields()).isNotEmpty();


  }

  @Test
  public void shouldReturnValidModelForValidAuditByAlertConfigWithoutParamSet() throws Throwable {
    LiveAlertConfigurationAuditByAlertConfigUUIDDTO dto = TestObjects
        .getLiveConfigurationAuditByAlertConfigUUIDDTO(
            TestUtils.randomUUID(), TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );

    LiveAlertConfigurationAudit result = mapper.mapAuditDTOByAlertConfigUUIDToModel(dto, null);
    assertThat(result).isNotNull();
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(dto.getPrimaryKey().getAlertConfigUUID().toString());
    assertThat(result.getAuditUUID()).isEqualTo(dto.getAuditUUID().toString());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());

    assertThat(result.getAlertParameters()).isNull();
    assertThat(result.getAlertFilters()).isNull();
    assertThat(result.getAlertAggregationFields()).isNull();
    assertThat(result.getUpdatedWho()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());

  }

  @Test
  public void shouldReturnNullModelForNullLiveAudit() throws Throwable {
    LiveAlertConfigurationAudit result = mapper.mapLiveAuditDTOToModel(null, null);
    assertThat(result).isNull();

    result = mapper.mapAuditDTOByAlertConfigUUIDToModel(null, null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnValidModelForValidLiveAuditWithParamSet() throws Throwable {
    LiveAlertConfigurationAuditDTO dto = TestObjects.getLiveConfigurationAuditDTO(
        TestUtils.randomUUID(), TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    );

    AlertParameterSetDTO alertParameterSetDTO = TestObjects.getAlertParameterSetDTO();

    LiveAlertConfigurationAudit result = mapper.mapLiveAuditDTOToModel(dto, alertParameterSetDTO);
    assertThat(result).isNotNull();
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(dto.getAlertConfigurationUUID().toString());
    assertThat(result.getAuditUUID()).isEqualTo(dto.getAuditUUID().toString());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());

    assertThat(result.getAlertParameters()).isNotEmpty();
    assertThat(result.getAlertFilters()).isNotEmpty();
    assertThat(result.getAlertAggregationFields()).isNotEmpty();
    assertThat(result.getUpdatedWho()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());


  }

  @Test
  public void shouldReturnValidModelForValidLiveAuditWithoutParamSet() throws Throwable {
    LiveAlertConfigurationAuditDTO dto = TestObjects.getLiveConfigurationAuditDTO(
        TestUtils.randomUUID(), TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    );

    LiveAlertConfigurationAudit result = mapper.mapLiveAuditDTOToModel(dto, null);
    assertThat(result).isNotNull();
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(dto.getAlertConfigurationUUID().toString());
    assertThat(result.getAuditUUID()).isEqualTo(dto.getAuditUUID().toString());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());

    assertThat(result.getAlertParameters()).isNull();
    assertThat(result.getAlertFilters()).isNull();
    assertThat(result.getAlertAggregationFields()).isNull();
    assertThat(result.getUpdatedWho()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());


  }

  @Test
  public void shouldReturnValidAuditByMonthDTOForValidLiveConfigDTO() {
    LiveAlertConfigurationDTO dto = TestObjects.getLiveConfigurationDTO(TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5));
    LiveAlertConfigurationAuditByMonthDTO result = mapper.mapLiveDTOToAuditByMonthDTO(dto);

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(dto.getStatus());
    assertThat(result.getCreatedBy()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getCreatedWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getAlertLogicType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getAlertConfigurationUUID()).isEqualTo(dto.getUuid());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());
    assertThat(result.getUpdatedBy()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());
  }

  @Test
  public void shouldReturnNullAuditByMonthDTOForValidLiveConfigDTO() {
    LiveAlertConfigurationAuditByMonthDTO result = mapper.mapLiveDTOToAuditByMonthDTO(null);
    assertThat(result).isNull();
  }
}
