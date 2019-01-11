package com.webtech.service.alerttype;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.irisium.TestUtils;
import com.irisium.service.alertDefinition.model.AlertTypeMap;
import com.irisium.service.alertDefinition.model.GuiDeploymentAlertType;
import com.webtech.service.alerttype.service.AlertTypeService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.common.security.PrincipalProvider;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.webtech.service.common.Constants;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class DeploymentAlertTypesApiDelegateImplTest {

  private DeploymentAlertTypesApiDelegateImpl delegate;

  @Mock
  private AlertTypeService mockService;
  @Mock
  private PrincipalProvider principalProvider;

  private Map<String, String> depTypeMap;
  private String depAlertTypeId;
  private GuiDeploymentAlertType deploymentAlertType;

  @Before
  public void setUp() throws Exception {
    delegate = new DeploymentAlertTypesApiDelegateImpl(mockService, principalProvider);
    depTypeMap = TestObjects.getDeploymentTypesMap();
    when(mockService.getDeploymentAlertTypes()).thenReturn(depTypeMap);
    when(principalProvider.getPrincipal()).thenReturn(Optional.of(TestUtils.randomAlphanumeric(5)));

    depAlertTypeId = TestUtils.randomUUID().toString();
    deploymentAlertType = mock(GuiDeploymentAlertType.class);
    when(mockService.getDeploymentAlertType(depAlertTypeId)).thenReturn(deploymentAlertType);
  }

  @Test
  public void shouldReturnAvailableDeploymentTypesWhenSuccess() {
    ResponseEntity<AlertTypeMap> result = delegate.getAllDeploymentAlertTypes();
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isNotNull();
    assertThat(result.getBody()).hasSize(depTypeMap.size());
  }

  @Test
  public void shouldReturnUnauthorisedWhenInvalidUserForGetAllDeploymentAlertTypes() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<AlertTypeMap> result = delegate.getAllDeploymentAlertTypes();
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnInternalErrorWhenIOExceptionForGetAllDeploymentAlertTypes()
      throws IOException {
    when(mockService.getDeploymentAlertTypes())
        .thenThrow(new IOException("Error when creating deepCopy"));
    ResponseEntity<AlertTypeMap> result = delegate.getAllDeploymentAlertTypes();
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
  }

  @Test
  public void shouldReturnUnauthorisedWhenInvalidUserForGetDeploymentAlertType() {
    when(principalProvider.getPrincipal()).thenReturn(Optional.empty());
    ResponseEntity<GuiDeploymentAlertType> result = delegate
        .getDeploymentAlertType(TestUtils.randomUUID().toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void shouldReturnInternalErrorWhenIOExceptionForGetDeploymentAlertType() throws Throwable {
    when(mockService.getDeploymentAlertType(any()))
        .thenThrow(new IOException("Error when creating deepCopy"));
    ResponseEntity<GuiDeploymentAlertType> result = delegate
        .getDeploymentAlertType(TestUtils.randomUUID().toString());
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
  }

  @Test
  public void shouldReturnInternalErrorWhenEntityNotFoundExceptionForGetDeploymentAlertType()
      throws Throwable {
    when(mockService.getDeploymentAlertType(depAlertTypeId))
        .thenThrow(new EntityNotFoundException("DeploymentAlertType", depAlertTypeId));
    ResponseEntity<GuiDeploymentAlertType> result = delegate.getDeploymentAlertType(depAlertTypeId);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(result.getHeaders()).containsKey(Constants.HEADER_ERROR_DESCRIPTION.getValue());
  }

  @Test
  public void shouldReturnGuiDeploymentAlertTypeWhenFoundForGetDeploymentAlertType() {
    ResponseEntity<GuiDeploymentAlertType> result = delegate.getDeploymentAlertType(depAlertTypeId);
    assertThat(result).isNotNull();
    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(result.getBody()).isInstanceOf(GuiDeploymentAlertType.class);
  }
}
