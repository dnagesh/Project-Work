package com.webtech.service.alertconfiguration.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.irisium.TestUtils;
import com.webtech.service.alertconfiguration.TestObjects;
import com.webtech.service.alertconfiguration.dto.AlertParameterSetDTO;
import com.webtech.service.alertconfiguration.dto.LiveAlertConfigurationDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditByMonthDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationAuditDTO;
import com.webtech.service.alertconfiguration.dto.SandboxAlertConfigurationDTO;
import com.irisium.service.alertconfiguration.model.CreateUpdateSandboxAlertConfigRequest;
import com.irisium.service.alertconfiguration.model.LiveAlertConfiguration.StatusEnum;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfiguration;
import com.irisium.service.alertconfiguration.model.SandboxAlertConfigurationAudit;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;

public class SandboxAlertConfigurationObjectMapperTest {

  private static final String USER = TestUtils.randomAlphanumeric(5);
  private SandboxAlertConfigurationObjectMapper mapper;

  @Before
  public void setUp() {
    this.mapper = new SandboxAlertConfigurationObjectMapper(new AlertFilterMapper());
  }

  @Test
  public void shouldCreateSandboxConfigDTOFromValidInputForNewConfig() {
    LiveAlertConfigurationDTO alertConfiguration = TestObjects
        .getLiveConfigurationDTO(TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5)
        );
    UUID sandboxUUID = TestUtils.randomUUID();
    String user = TestUtils.randomAlphanumeric(5);
    SandboxAlertConfigurationDTO dto = mapper
        .createSandboxConfigDTO(alertConfiguration, sandboxUUID.toString(), null, user);

    assertThat(dto).isNotNull();
    assertThat(dto.getPrimaryKey()).isNotNull();
    assertThat(dto.getPrimaryKey().getSandboxUUID()).isEqualTo(sandboxUUID);

    //New AlertConfigUUID should be generated
    assertThat(dto.getPrimaryKey().getAlertConfigurationUUID())
        .isNotEqualTo(alertConfiguration.getUuid());

    assertThat(dto.getAlertLogicType()).isEqualTo(alertConfiguration.getAlertLogicType());
    assertThat(dto.getApsHash()).isEqualTo(alertConfiguration.getApsHash());
    assertThat(dto.getComment()).isEqualTo(alertConfiguration.getComment());
    assertThat(dto.getName()).isEqualTo(alertConfiguration.getName());
    assertThat(dto.getStatus()).isEqualTo(alertConfiguration.getStatus());

    //User and creation timestamp should not be copied from input
    assertThat(dto.getCreatedBy()).isEqualTo(user);
    assertThat(dto.getCreatedWhen()).isNotNull();
    assertThat(dto.getCreatedWhen()).isNotEqualTo(alertConfiguration.getCreatedWhen());
  }

  @Test
  public void shouldCreateSandboxConfigDTOFromValidInputForUpdateConfig() {
    LiveAlertConfigurationDTO alertConfiguration = TestObjects
        .getLiveConfigurationDTO(TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5)
        );
    UUID sandboxUUID = TestUtils.randomUUID();
    String user = TestUtils.randomAlphanumeric(5);
    UUID alertConfigUUID = TestUtils.randomUUID();
    SandboxAlertConfigurationDTO dto = mapper
        .createSandboxConfigDTO(alertConfiguration, sandboxUUID.toString(),
            alertConfigUUID.toString(), user);

    assertThat(dto).isNotNull();
    assertThat(dto.getPrimaryKey()).isNotNull();
    assertThat(dto.getPrimaryKey().getSandboxUUID()).isEqualTo(sandboxUUID);

    //Should be passed AlertConfigUUID
    assertThat(dto.getPrimaryKey().getAlertConfigurationUUID())
        .isEqualTo(alertConfigUUID);

    assertThat(dto.getAlertLogicType()).isEqualTo(alertConfiguration.getAlertLogicType());
    assertThat(dto.getApsHash()).isEqualTo(alertConfiguration.getApsHash());
    assertThat(dto.getComment()).isEqualTo(alertConfiguration.getComment());
    assertThat(dto.getName()).isEqualTo(alertConfiguration.getName());
    assertThat(dto.getStatus()).isEqualTo(alertConfiguration.getStatus());

    //User and creation timestamp should not be copied from input
    assertThat(dto.getCreatedBy()).isEqualTo(user);
    assertThat(dto.getCreatedWhen()).isNotNull();
    assertThat(dto.getCreatedWhen()).isNotEqualTo(alertConfiguration.getCreatedWhen());
  }

  @Test
  public void shouldCreateSandboxConfigAuditDTOFromValidInput() {
    SandboxAlertConfigurationDTO dto = TestObjects.
        getSandboxConfigurationDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    UUID sandboxUUID = TestUtils.randomUUID();
    String user = TestUtils.randomAlphanumeric(5);
    SandboxAlertConfigurationAuditDTO auditDTO = mapper
        .createSandboxConfigAuditDTO(dto);

    assertThat(auditDTO).isNotNull();
    assertThat(auditDTO.getPrimaryKey()).isNotNull();
    assertThat(auditDTO.getPrimaryKey().getSandboxUUID())
        .isEqualTo(dto.getPrimaryKey().getSandboxUUID());
    assertThat(auditDTO.getPrimaryKey().getAlertConfigurationUUID())
        .isEqualTo(dto.getPrimaryKey().getAlertConfigurationUUID());

    //New AlertConfigUUID should be generated
    assertThat(auditDTO.getPrimaryKey().getAuditUUID()).isNotNull();

    assertThat(auditDTO.getAlertLogicType()).isEqualTo(dto.getAlertLogicType());
    assertThat(auditDTO.getApsHash()).isEqualTo(dto.getApsHash());
    assertThat(auditDTO.getComment()).isEqualTo(dto.getComment());
    assertThat(auditDTO.getName()).isEqualTo(dto.getName());
    assertThat(auditDTO.getStatus()).isEqualTo(dto.getStatus());
    assertThat(auditDTO.getCreatedBy()).isEqualTo(dto.getCreatedBy());
    assertThat(auditDTO.getCreatedWhen()).isEqualTo(dto.getCreatedWhen());
  }

  @Test
  public void shouldReturnValidAlertParameterDTOForValidParameters() throws Throwable {
    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    AlertParameterSetDTO dto = mapper.createAlertParameterSetDTO(request, null, null);

    assertThat(dto).isNotNull();
    assertThat(dto.getApsHash()).isNotBlank();
    assertThat(dto.getAlertLogicType()).isEqualTo(request.getAlertConfigType());
    assertThat(dto.getAlertAggregationFields()).hasSize(request.getAlertAggregationFields().size());
    assertThat(dto.getAlertParameters()).hasSize(request.getAlertParameters().size());
    assertThat(dto.getAlertFilterDTOS()).hasSize(request.getAlertFilters().size());
  }

  @Test
  public void shouldReturnValidAlertParameterDTOForValidParametersWithPresets() throws Throwable {
    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();

    AlertParameterSetDTO dto = mapper.createAlertParameterSetDTO(request, null, null);

    assertThat(dto).isNotNull();
    assertThat(dto.getApsHash()).isNotBlank();
    assertThat(dto.getAlertLogicType()).isEqualTo(request.getAlertConfigType());
    assertThat(dto.getAlertAggregationFields()).hasSize(request.getAlertAggregationFields().size());
    assertThat(dto.getAlertParameters()).hasSize(request.getAlertParameters().size());
    assertThat(dto.getAlertFilterDTOS()).hasSize(request.getAlertFilters().size());

    Map<String, String> paramPresets = new HashMap<>();
    paramPresets.put(TestUtils.randomAlphanumeric(5), TestUtils.randomAlphanumeric(10));

    Map<String, Boolean> logicPresets = new HashMap<>();
    logicPresets.put(TestUtils.randomAlphanumeric(5), TestUtils.randomBoolean());

    dto = mapper.createAlertParameterSetDTO(request, paramPresets, logicPresets);

    assertThat(dto).isNotNull();
    assertThat(dto.getApsHash()).isNotBlank();
    assertThat(dto.getAlertLogicType()).isEqualTo(request.getAlertConfigType());
    assertThat(dto.getAlertAggregationFields()).hasSize(request.getAlertAggregationFields().size());
    assertThat(dto.getAlertParameters())
        .hasSize(request.getAlertParameters().size() + paramPresets.size());
    assertThat(dto.getLogicOverrides())
        .hasSize(request.getLogicOverrideSet().size() + logicPresets.size());
  }


  @Test
  public void shouldReturnEmptyFilterDTOsWhenNoneExist() throws Throwable {
    CreateUpdateSandboxAlertConfigRequest request = TestObjects
        .getCreateUpdateSandboxAlertConfigRequest();
    request.setAlertFilters(null);
    AlertParameterSetDTO dto = mapper.createAlertParameterSetDTO(request, null, null);

    assertThat(dto).isNotNull();
    assertThat(dto.getApsHash()).isNotBlank();
    assertThat(dto.getAlertLogicType()).isEqualTo(request.getAlertConfigType());
    assertThat(dto.getAlertAggregationFields()).hasSize(request.getAlertAggregationFields().size());
    assertThat(dto.getAlertParameters()).hasSize(request.getAlertParameters().size());
    assertThat(dto.getAlertFilterDTOS()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnValidSandboxAlertConfigurationForValidParameters() throws Throwable {
    SandboxAlertConfigurationDTO savedDTO = TestObjects
        .getSandboxConfigurationDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    AlertParameterSetDTO savedParameterSetDTO = TestObjects.getAlertParameterSetDTO();
    SandboxAlertConfiguration result = mapper
        .mapSandboxAlertConfigDTOToModel(savedDTO, savedParameterSetDTO);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(savedDTO.getName());
    assertThat(result.getSandboxUUID())
        .isEqualTo(savedDTO.getPrimaryKey().getSandboxUUID().toString());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(savedDTO.getPrimaryKey().getAlertConfigurationUUID().toString());
    assertThat(result.getLiveConfigUUID()).isNull();
    assertThat(result.getAlertConfigType()).isEqualTo(savedDTO.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(savedDTO.getComment());
    assertThat(result.getWho()).isEqualTo(savedDTO.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(savedDTO.getCreatedWhen());

    assertThat(result.getApsHash()).isEqualTo(savedDTO.getApsHash());
    assertThat(result.getAlertAggregationFields())
        .hasSize(savedParameterSetDTO.getAlertAggregationFields().size());
    assertThat(result.getAlertFilters()).hasSize(savedParameterSetDTO.getAlertFilterDTOS().size());
    assertThat(result.getAlertParameters())
        .hasSize(savedParameterSetDTO.getAlertParameters().size());

  }

  @Test
  public void shouldReturnValidSandboxAlertConfigurationWithLiveConfigUUIDForValidParameters()
      throws Throwable {
    SandboxAlertConfigurationDTO savedDTO = TestObjects
        .getSandboxConfigurationDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    savedDTO.setLiveConfigUUID(TestUtils.randomUUID());
    AlertParameterSetDTO savedParameterSetDTO = TestObjects.getAlertParameterSetDTO();

    SandboxAlertConfiguration result = mapper
        .mapSandboxAlertConfigDTOToModel(savedDTO, savedParameterSetDTO);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(savedDTO.getName());
    assertThat(result.getSandboxUUID())
        .isEqualTo(savedDTO.getPrimaryKey().getSandboxUUID().toString());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(savedDTO.getPrimaryKey().getAlertConfigurationUUID().toString());
    assertThat(result.getLiveConfigUUID()).isNotNull();
    assertThat(result.getLiveConfigUUID()).isEqualTo(savedDTO.getLiveConfigUUID().toString());
    assertThat(result.getAlertConfigType()).isEqualTo(savedDTO.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(savedDTO.getComment());
    assertThat(result.getWho()).isEqualTo(savedDTO.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(savedDTO.getCreatedWhen());

    assertThat(result.getApsHash()).isEqualTo(savedDTO.getApsHash());
    assertThat(result.getAlertAggregationFields())
        .hasSize(savedParameterSetDTO.getAlertAggregationFields().size());
    assertThat(result.getAlertFilters()).hasSize(savedParameterSetDTO.getAlertFilterDTOS().size());
    assertThat(result.getAlertParameters())
        .hasSize(savedParameterSetDTO.getAlertParameters().size());

  }


  @Test
  public void shouldReturnValidSandboxAlertConfigurationAuditForValidParameters() throws Throwable {
    SandboxAlertConfigurationAuditDTO savedDTO = TestObjects
        .getSandboxConfigurationAuditDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    AlertParameterSetDTO savedParameterSetDTO = TestObjects.getAlertParameterSetDTO();
    SandboxAlertConfigurationAudit result = mapper
        .mapSandboxConfigAuditDTOToModel(savedDTO, savedParameterSetDTO);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(savedDTO.getName());
    assertThat(result.getSandboxUUID())
        .isEqualTo(savedDTO.getPrimaryKey().getSandboxUUID().toString());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(savedDTO.getPrimaryKey().getAlertConfigurationUUID().toString());
    assertThat(result.getLiveConfigUUID()).isNull();
    assertThat(result.getAlertConfigType()).isEqualTo(savedDTO.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(savedDTO.getComment());
    assertThat(result.getWho()).isEqualTo(savedDTO.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(savedDTO.getCreatedWhen());

    assertThat(result.getApsHash()).isEqualTo(savedDTO.getApsHash());
    assertThat(result.getAlertAggregationFields())
        .hasSize(savedParameterSetDTO.getAlertAggregationFields().size());
    assertThat(result.getAlertFilters()).hasSize(savedParameterSetDTO.getAlertFilterDTOS().size());
    assertThat(result.getAlertParameters())
        .hasSize(savedParameterSetDTO.getAlertParameters().size());

  }

  @Test
  public void shouldReturnValidSandboxAlertConfigurationAuditForValidParametersWithLiveConfigUUID()
      throws Throwable {
    SandboxAlertConfigurationAuditDTO savedDTO = TestObjects
        .getSandboxConfigurationAuditDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    savedDTO.setLiveConfigUUID(TestUtils.randomUUID());
    AlertParameterSetDTO savedParameterSetDTO = TestObjects.getAlertParameterSetDTO();
    SandboxAlertConfigurationAudit result = mapper
        .mapSandboxConfigAuditDTOToModel(savedDTO, savedParameterSetDTO);
    assertThat(result).isNotNull();
    assertThat(result.getName()).isEqualTo(savedDTO.getName());
    assertThat(result.getSandboxUUID())
        .isEqualTo(savedDTO.getPrimaryKey().getSandboxUUID().toString());
    assertThat(result.getLiveConfigUUID())
        .isEqualTo(savedDTO.getLiveConfigUUID().toString());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(savedDTO.getPrimaryKey().getAlertConfigurationUUID().toString());
    assertThat(result.getAlertConfigType()).isEqualTo(savedDTO.getAlertLogicType());
    assertThat(result.getComment()).isEqualTo(savedDTO.getComment());
    assertThat(result.getWho()).isEqualTo(savedDTO.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(savedDTO.getCreatedWhen());

    assertThat(result.getApsHash()).isEqualTo(savedDTO.getApsHash());
    assertThat(result.getAlertAggregationFields())
        .hasSize(savedParameterSetDTO.getAlertAggregationFields().size());
    assertThat(result.getAlertFilters()).hasSize(savedParameterSetDTO.getAlertFilterDTOS().size());
    assertThat(result.getAlertParameters())
        .hasSize(savedParameterSetDTO.getAlertParameters().size());

  }

  @Test
  public void shouldReturnAuditForInvalidAlertParameters() {
    SandboxAlertConfigurationAuditDTO savedDTO = TestObjects
        .getSandboxConfigurationAuditDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    SandboxAlertConfigurationAudit result = mapper
        .mapSandboxConfigAuditDTOToModel(savedDTO, null);
    assertThat(result).isNotNull();
    assertThat(result.getAlertParameters()).isNullOrEmpty();
    assertThat(result.getAlertFilters()).isNullOrEmpty();
    assertThat(result.getAlertAggregationFields()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnAuditForNullOrEmptyInputAlertParameters() throws Throwable {
    SandboxAlertConfigurationAuditDTO savedDTO = TestObjects
        .getSandboxConfigurationAuditDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );

    AlertParameterSetDTO parameterSetDTO = new AlertParameterSetDTO(TestUtils.randomAlphanumeric(5),
        null, null, null, null, null);
    SandboxAlertConfigurationAudit result = mapper
        .mapSandboxConfigAuditDTOToModel(savedDTO, parameterSetDTO);
    assertThat(result).isNotNull();
    assertThat(result.getAlertParameters()).isNullOrEmpty();
    assertThat(result.getAlertFilters()).isNullOrEmpty();
    assertThat(result.getAlertAggregationFields()).isNullOrEmpty();
  }

  @Test
  public void shouldReturnValidDTOFromRequestWithAlertConfigUUID() {
    CreateUpdateSandboxAlertConfigRequest request = new CreateUpdateSandboxAlertConfigRequest();
    request.setStatus(CreateUpdateSandboxAlertConfigRequest.StatusEnum.ACTIVE);
    UUID sandboxUUID = TestUtils.randomUUID();
    UUID alertConfigurationId = TestUtils.randomUUID();
    SandboxAlertConfigurationDTO currentDTO = new SandboxAlertConfigurationDTO();
    currentDTO.setLiveConfigUUID(TestUtils.randomUUID());
    currentDTO.setCreatedBy(USER);
    currentDTO.setCreatedWhen(Instant.now().minusMillis(1000));
    String apsHash = TestUtils.randomAlphanumeric(10);
    SandboxAlertConfigurationDTO dto = mapper
        .createSandboxConfigDTOFromRequest(request, sandboxUUID.toString(),
            alertConfigurationId.toString(), currentDTO, USER, apsHash);

    assertThat(dto).isNotNull();
    assertThat(dto.getPrimaryKey()).isNotNull();
    assertThat(dto.getPrimaryKey().getSandboxUUID()).isEqualTo(sandboxUUID);
    assertThat(dto.getPrimaryKey().getAlertConfigurationUUID()).isEqualTo(alertConfigurationId);
    assertThat(dto.getLiveConfigUUID()).isEqualTo(currentDTO.getLiveConfigUUID());
    assertThat(dto.getApsHash()).isEqualTo(apsHash);
    assertThat(dto.getUpdatedWhen()).isNotNull();
    assertThat(dto.getUpdatedBy()).isEqualTo(USER);
    assertThat(dto.getStatus()).isEqualTo(request.getStatus().toString());
    assertThat(dto.getComment()).isEqualTo(request.getComment());
    assertThat(dto.getAlertLogicType()).isEqualTo(request.getAlertConfigType());
    assertThat(dto.getName()).isEqualTo(request.getName());

  }

  @Test
  public void shouldReturnValidDTOFromRequestWithoutAlertConfigUUID() {
    CreateUpdateSandboxAlertConfigRequest request = new CreateUpdateSandboxAlertConfigRequest();
    request.setStatus(CreateUpdateSandboxAlertConfigRequest.StatusEnum.ACTIVE);
    UUID sandboxUUID = TestUtils.randomUUID();
    String apsHash = TestUtils.randomAlphanumeric(10);
    SandboxAlertConfigurationDTO currentDTO = new SandboxAlertConfigurationDTO();
    currentDTO.setCreatedBy(USER);
    currentDTO.setCreatedWhen(Instant.now().minusMillis(1000));
    currentDTO.setLiveConfigUUID(TestUtils.randomUUID());
    SandboxAlertConfigurationDTO dto = mapper
        .createSandboxConfigDTOFromRequest(request, sandboxUUID.toString(), null,
            currentDTO, USER, apsHash);

    assertThat(dto).isNotNull();
    assertThat(dto.getPrimaryKey()).isNotNull();
    assertThat(dto.getPrimaryKey().getSandboxUUID()).isEqualTo(sandboxUUID);
    assertThat(dto.getPrimaryKey().getAlertConfigurationUUID()).isNotNull();
    assertThat(dto.getLiveConfigUUID()).isEqualTo(currentDTO.getLiveConfigUUID());
    assertThat(dto.getApsHash()).isEqualTo(apsHash);
    assertThat(dto.getCreatedWhen()).isNotNull();
    assertThat(dto.getCreatedBy()).isEqualTo(USER);
    assertThat(dto.getStatus()).isEqualTo(request.getStatus().toString());
    assertThat(dto.getComment()).isEqualTo(request.getComment());
    assertThat(dto.getAlertLogicType()).isEqualTo(request.getAlertConfigType());
    assertThat(dto.getName()).isEqualTo(request.getName());

  }

  @Test
  public void shouldReturnValidDTOFromRequestWithoutAlertConfigUUIDAndWithoutSandboxAlertConfigurationDTO() {
    CreateUpdateSandboxAlertConfigRequest request = new CreateUpdateSandboxAlertConfigRequest();
    request.setStatus(CreateUpdateSandboxAlertConfigRequest.StatusEnum.ACTIVE);
    UUID sandboxUUID = TestUtils.randomUUID();
    String apsHash = TestUtils.randomAlphanumeric(10);
    SandboxAlertConfigurationDTO dto = mapper
        .createSandboxConfigDTOFromRequest(request, sandboxUUID.toString(), null,
            null, USER, apsHash);

    assertThat(dto).isNotNull();
    assertThat(dto.getPrimaryKey()).isNotNull();
    assertThat(dto.getPrimaryKey().getSandboxUUID()).isEqualTo(sandboxUUID);
    assertThat(dto.getPrimaryKey().getAlertConfigurationUUID()).isNotNull();
    assertThat(dto.getApsHash()).isEqualTo(apsHash);
    assertThat(dto.getCreatedWhen()).isNotNull();
    assertThat(dto.getCreatedBy()).isEqualTo(USER);
    assertThat(dto.getStatus()).isEqualTo(request.getStatus().toString());
    assertThat(dto.getComment()).isEqualTo(request.getComment());
    assertThat(dto.getAlertLogicType()).isEqualTo(request.getAlertConfigType());
    assertThat(dto.getName()).isEqualTo(request.getName());

  }


  @Test
  public void shouldReturnValidSandboxConfigModelListForDTOList() {
    List<SandboxAlertConfigurationDTO> dtoList = TestObjects.getSandboxConfigurationDTOList();
    List<SandboxAlertConfiguration> result = mapper.mapSandboxAlertConfigDTOListToModel(dtoList);
    assertThat(result).isNotNull();
    assertThat(result).hasSize(dtoList.size());

  }

  @Test
  public void shouldReturnNullForNullAlertFilterModel() {
    assertThat(mapper.mapAlertFilterModelToDTO(null)).isNull();
  }

  @Test
  public void shouldReturnNullSandboxConfigAuditDTO() {
    assertThat(mapper.mapSandboxConfigAuditDTOToModel(null, null)).isNull();
  }

  @Test
  public void shouldReturnEmptyModelListForEmptyDTOList() {
    assertThat(mapper.mapSandboxAlertConfigDTOListToModel(Collections.emptyList())).isEmpty();
  }


  @Test
  public void shouldReturnCollectionEmptyWhenNoAuditDTOExists() {
    List<SandboxAlertConfigurationAuditDTO> auditDTOS = null;
    List<SandboxAlertConfigurationAudit> modelList = mapper
        .mapAuditDTOListToModelList(auditDTOS);
    assertThat(modelList).isEmpty();
  }

  @Test
  public void shouldReturnValidAuditModelListForValidAuditDTOList() {
    List<SandboxAlertConfigurationAuditDTO> auditDTOS = TestObjects
        .getSandboxConfigurationAuditDTOList();
    List<SandboxAlertConfigurationAudit> modelList = mapper
        .mapAuditDTOListToModelList(auditDTOS);
    assertThat(modelList).isNotNull();
    assertThat(modelList).hasSize(auditDTOS.size());
  }

  @Test
  public void shouldReturnValidAuditModelListForValidAuditByMonthDTOList() {
    List<SandboxAlertConfigurationAuditByMonthDTO> auditDTOS = TestObjects
        .getSandboxConfigurationAuditByMonthDTOList();
    List<SandboxAlertConfigurationAudit> modelList = mapper
        .mapAuditByMonthDTOListToModelList(auditDTOS);
    assertThat(modelList).isNotNull();
    assertThat(modelList).hasSize(auditDTOS.size());
  }

  @Test
  public void shouldReturnNullWhenSandboxAlertConfigurationAuditByMonthDTOIsNull() {
    SandboxAlertConfigurationAudit result = mapper.mapAuditDTOByMonthToModel(null, null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnEmptyWhenInValidAuditByMonthDTOList() {
    List<SandboxAlertConfigurationAudit> modelList = mapper
        .mapAuditByMonthDTOListToModelList(null);
    assertThat(modelList).isEmpty();

  }

  @Test
  public void shouldReturnEmptyWhenAuditByMonthDTOListIsEmpty() {
    List<SandboxAlertConfigurationAuditByMonthDTO> auditDTOS = new ArrayList<>();
    List<SandboxAlertConfigurationAudit> modelList = mapper
        .mapAuditByMonthDTOListToModelList(auditDTOS);
    assertThat(modelList).isEmpty();

  }

  @Test
  public void shouldReturnValidModelForValidSandboxAuditWithParamSet() throws Throwable {
    SandboxAlertConfigurationAuditDTO dto = TestObjects.getSandboxConfigurationAuditDTO(
        TestUtils.randomUUID(), TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    );

    AlertParameterSetDTO alertParameterSetDTO = TestObjects.getAlertParameterSetDTO();

    SandboxAlertConfigurationAudit result = mapper
        .mapSandboxAuditDTOToModel(dto, alertParameterSetDTO);
    assertThat(result).isNotNull();
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(dto.getPrimaryKey().getAlertConfigurationUUID().toString());
    assertThat(result.getAuditUUID()).isEqualTo(dto.getPrimaryKey().getAuditUUID().toString());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());

    assertThat(result.getAlertParameters()).isNotEmpty();
    assertThat(result.getAlertFilters()).isNotEmpty();
    assertThat(result.getAlertAggregationFields()).isNotEmpty();
    assertThat(result.getUpdatedWho()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());
  }

  @Test
  public void shouldReturnValidModelForValidSandboxAuditWithoutParamSet() throws Throwable {
    SandboxAlertConfigurationAuditDTO dto = TestObjects.getSandboxConfigurationAuditDTO(
        TestUtils.randomUUID(), TestUtils.randomUUID(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomAlphanumeric(5),
        StatusEnum.ACTIVE.toString(),
        TestUtils.randomAlphanumeric(5),
        TestUtils.randomInstant()
    );

    SandboxAlertConfigurationAudit result = mapper.mapSandboxAuditDTOToModel(dto, null);
    assertThat(result).isNotNull();
    assertThat(result.getStatus().toString()).isEqualTo(dto.getStatus());
    assertThat(result.getWho()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getAlertConfigType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(dto.getPrimaryKey().getAlertConfigurationUUID().toString());
    assertThat(result.getAuditUUID()).isEqualTo(dto.getPrimaryKey().getAuditUUID().toString());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());

    assertThat(result.getAlertParameters()).isNull();
    assertThat(result.getAlertFilters()).isNull();
    assertThat(result.getAlertAggregationFields()).isNull();
    assertThat(result.getUpdatedWho()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());
  }

  @Test
  public void shouldReturnValidModelForInValidSandboxAuditWithoutParamSet() {
    SandboxAlertConfigurationAudit result = mapper.mapSandboxAuditDTOToModel(null, null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnValidAuditByMonthDTOForValidSandboxConfigDTO() {
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

    SandboxAlertConfigurationAuditByMonthDTO result = mapper
        .mapSandboxDTOToAuditByMonthDTO(dto, TestUtils.randomUUID());

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(dto.getStatus());
    assertThat(result.getCreatedBy()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getCreatedWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getAlertLogicType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(dto.getPrimaryKey().getAlertConfigurationUUID());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());
    assertThat(result.getUpdatedBy()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());
  }

  @Test
  public void shouldReturnValidAuditByMonthDTOForValidSandboxConfigAuditDTO() {
    SandboxAlertConfigurationAuditDTO dto = TestObjects
        .getSandboxConfigurationAuditDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant());

    SandboxAlertConfigurationAuditByMonthDTO result = mapper
        .mapSandboxAuditDTOToAuditByMonthDTO(dto);

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(dto.getStatus());
    assertThat(result.getCreatedBy()).isEqualTo(dto.getCreatedBy());
    assertThat(result.getCreatedWhen()).isEqualTo(dto.getCreatedWhen());
    assertThat(result.getComment()).isEqualTo(dto.getComment());
    assertThat(result.getName()).isEqualTo(dto.getName());
    assertThat(result.getAlertLogicType()).isEqualTo(dto.getAlertLogicType());
    assertThat(result.getAlertConfigurationUUID())
        .isEqualTo(dto.getPrimaryKey().getAlertConfigurationUUID());
    assertThat(result.getApsHash()).isEqualTo(dto.getApsHash());
    assertThat(result.getUpdatedBy()).isEqualTo(dto.getUpdatedBy());
    assertThat(result.getUpdatedWhen()).isEqualTo(dto.getUpdatedWhen());
  }

  @Test
  public void shouldReturnNullAuditByMonthDTOForInValidSandboxConfigAuditDTO() {
    SandboxAlertConfigurationAuditByMonthDTO result = mapper
        .mapSandboxAuditDTOToAuditByMonthDTO(null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldReturnNullAuditByMonthDTOForInValidSandboxConfigDTO() {
    SandboxAlertConfigurationAuditByMonthDTO result = mapper
        .mapSandboxDTOToAuditByMonthDTO(null, null);
    assertThat(result).isNull();
  }

  @Test
  public void shouldCloneSandboxConfigDTOFromValidInput() {
    SandboxAlertConfigurationDTO dto = TestObjects.
        getSandboxConfigurationDTO(TestUtils.randomUUID(),
            TestUtils.randomUUID(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomAlphanumeric(5),
            StatusEnum.ACTIVE.toString(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant(),
            TestUtils.randomAlphanumeric(5),
            TestUtils.randomInstant()
        );
    String name = TestUtils.randomAlphanumeric();
    SandboxAlertConfigurationDTO clone = mapper
        .clone(dto, name, USER);

    assertThat(clone).isNotNull();
    assertThat(clone.getPrimaryKey()).isNotNull();
    assertThat(clone.getPrimaryKey().getSandboxUUID())
        .isEqualTo(dto.getPrimaryKey().getSandboxUUID());
    //New AlertConfigUUID should be generated
    assertThat(clone.getPrimaryKey().getAlertConfigurationUUID())
        .isNotEqualTo(dto.getPrimaryKey().getAlertConfigurationUUID());

    assertThat(clone.getAlertLogicType()).isEqualTo(dto.getAlertLogicType());
    assertThat(clone.getApsHash()).isEqualTo(dto.getApsHash());
    assertThat(clone.getComment()).isEqualTo(dto.getComment());
    assertThat(clone.getName()).isEqualTo(name);
    assertThat(clone.getStatus()).isEqualTo(dto.getStatus());

    //CreatedBy should be cloning user
    assertThat(clone.getCreatedBy()).isEqualTo(USER);
    assertThat(clone.getCreatedWhen()).isNotEqualTo(dto.getCreatedWhen());

    assertThat(clone.getUpdatedBy()).isNull();
    assertThat(clone.getUpdatedWhen()).isNull();
  }
}
