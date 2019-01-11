package com.webtech.service.alerttype;

import com.irisium.service.alertDefinition.api.DeploymentAlertTypesApiDelegate;
import com.irisium.service.alertDefinition.model.AlertTypeMap;
import com.irisium.service.alertDefinition.model.GuiDeploymentAlertType;
import com.webtech.service.alerttype.service.AlertTypeService;
import com.webtech.service.common.exception.EntityNotFoundException;
import com.webtech.service.common.security.PrincipalProvider;
import java.io.IOException;
import java.util.Optional;

import com.webtech.service.common.Constants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class DeploymentAlertTypesApiDelegateImpl implements DeploymentAlertTypesApiDelegate {

  private final AlertTypeService alertTypeService;
  private final PrincipalProvider principalProvider;

  public DeploymentAlertTypesApiDelegateImpl(
      AlertTypeService alertTypeService,
      PrincipalProvider principalProvider) {
    this.alertTypeService = alertTypeService;
    this.principalProvider = principalProvider;
  }

  @Override
  public ResponseEntity<AlertTypeMap> getAllDeploymentAlertTypes() {
    Optional<String> user = principalProvider.getPrincipal();
    if (!user.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    AlertTypeMap alertTypeMap = new AlertTypeMap();
    try {
      alertTypeMap.putAll(alertTypeService.getDeploymentAlertTypes());
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();
    }
    return ResponseEntity.ok(alertTypeMap);
  }

  @Override
  public ResponseEntity<GuiDeploymentAlertType> getDeploymentAlertType(
      String deploymentAlertTypeId) {
    Optional<String> user = principalProvider.getPrincipal();
    if (!user.isPresent()) {
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
    try {
      GuiDeploymentAlertType deploymentAlertType = alertTypeService
          .getDeploymentAlertType(deploymentAlertTypeId);
      return ResponseEntity.ok(deploymentAlertType);
    } catch (EntityNotFoundException e) {
      return ResponseEntity.notFound().header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage())
          .build();
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .header(Constants.HEADER_ERROR_DESCRIPTION.getValue(), e.getMessage()).build();

    }
  }
}
